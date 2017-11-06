package Indexing;

import ConceptExtraction.UPF.ConceptExtractionHandler;
import Functions.ParsingFunctions;
import Indexing.LuceneCustomClasses.*;
import Indexing.ContextRetrieval.DocPassageIDPair;
import Indexing.ContextRetrieval.ContextRetrievalHandler;
import KnowledgeBase.DocumentResult;
import KnowledgeBase.QueryObject;
import Version.PassageRetrievalVersion;
import WebKIndex.DefaultValues;
import WordEmbeddings.Word2VecHandler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nd4j.linalg.api.ops.impl.transforms.Exp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;


public class PassageIndexHandler {

	private static Map<String,Word2VecHandler> w2vHandlers = new HashMap<>();
	static {
		Word2VecHandler w2vES = new Word2VecHandler();
		w2vES.loadModel("embeddings/w2v_model_es");
		w2vES.formVocabEmbeddingMatrix();
		Word2VecHandler w2vDE = new Word2VecHandler();
		w2vDE.loadModel("embeddings/w2v_model_de");
		w2vDE.formVocabEmbeddingMatrix();
		w2vHandlers.put("es",w2vES);
		w2vHandlers.put("de",w2vDE);
	}

	private int topN;
	private String language;
	private String version; // set as String in order to be able to handle any version input in the service
	private String rootDir; // based on language and version
	private String [] targetIndices;
	private boolean queryExpansion;

	// enum to store the similarity(-ies) based on which we retrieve docs or paragraphs in doc and paragraph based implementations
	private enum DocBasedModes {BOTH, ONLY_LMJM};
	private DocBasedModes docBasedMode;
	private DocBasedModes paragraphBasedMode;
	private int docBasedModeTopN;
	private int paragraphBasedModeTopN;
	private int minWordsFilter;

	DirectoryReader reader;
	StopwordAnalyzerBase analyzer;
	IndexWriterConfig config1S;
	IndexWriterConfig config2S;
	IndexWriterConfig config3S;
	IndexWriterConfig configP;
	IndexWriterConfig configAS;
	IndexWriterConfig configD;
	Directory directory1S; //directory for one sentence index
	Directory directory2S; //directory for two sentences index
	Directory directory3S; //directory for three sentences index
	Directory directoryP; //directory for paragraph index
	Directory directoryAS; //directory for All segmentaion index
	Directory directoryD; //directory for document index

	public PassageIndexHandler() {

		this.topN = 10;

		try {
//			analyzer = new ClassicAnalyzer();
			analyzer = new CustomAnalyzerSpanish();


			config1S = new IndexWriterConfig(analyzer);
			config1S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory1S = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_1_SENTENCE));

			config2S = new IndexWriterConfig(analyzer);
			config2S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory2S = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_2_SENTENCES));

			config3S = new IndexWriterConfig(analyzer);
			config3S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory3S = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_3_SENTENCES));

			configP = new IndexWriterConfig(analyzer);
			configP.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryP = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_PARAGRAPH));

			configAS = new IndexWriterConfig(analyzer);
			configAS.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryAS = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_ALL_SEGMENTATION));

			configD = new IndexWriterConfig(analyzer);
			configD.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryD = FSDirectory.open(Paths.get(IndexCONSTANTS.INDEX_PATH_DOCUMENT));

		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler() Could NOT open Passage Index Directories Paths");
			e.printStackTrace();
		}


	}

	public PassageIndexHandler(String language, int version) {
		this.topN = 10;
		this.language = language;
		this.version = "v." + version;
		this.queryExpansion = false;
		initializeAnalyzer(version);
		initializeDir();

		// just to have these values initialized
		this.docBasedMode = DocBasedModes.ONLY_LMJM;
		this.docBasedModeTopN = 5;
		this.paragraphBasedMode = DocBasedModes.ONLY_LMJM;
		this.paragraphBasedModeTopN = 5;
	}

	public PassageIndexHandler(String language, int version, boolean queryExpansion) {
		this.topN = 10;
		this.language = language;
		this.version = "v." + version;
		this.queryExpansion = queryExpansion;
		initializeAnalyzer(version);
		initializeDir();

		// just to have these values initialized
		this.docBasedMode = DocBasedModes.ONLY_LMJM;
		this.docBasedModeTopN = 5;
		this.paragraphBasedMode = DocBasedModes.ONLY_LMJM;
		this.paragraphBasedModeTopN = 5;

		if(this.language.equals("pl"))
			minWordsFilter = 10;
		else
			minWordsFilter = 5;
	}

	private void initializeAnalyzer(int version){

		if(this.language.equals("es") || this.language.equals("es_short")){
			// handle old versions differently
//			if(this.version.equals("v.0") || this.version.equals("v.1"))
			if(version <= 1)
				analyzer = new CustomAnalyzerSpanish();
			// for version 2 or later
			else if(version >= 5 && version <= 7 )
				analyzer = new LightStemSpanishAnalyzer();
			else if(version < 9)
				analyzer = new SnowBallSpanishAnalyzer(new CharArraySet(StopwordListsOld.stopWordsES_V5, false)); // old stopword set
			else
				analyzer = new SnowBallSpanishAnalyzer();
			// TODO put analyzer
		}
		else if(this.language.equals("de")){
			if(version==0)
				analyzer = new GermanAnalyzer();
			else
				analyzer = new GermanAnalyzer(new CharArraySet(StopwordLists.stopWordsDE, false));
		}
		else if(this.language.equals("pl")){
			analyzer = new PolishAnalyzer();
			System.out.println("Query expansion set by default to false!!!");
			this.queryExpansion = false; //	WARNING REMOVE THIS AFTER TRAINING THE W2V MODELS
		}
		// use english in any other case, also set language to find appropriate folder
		else {
			this.language = "en";
			analyzer = new ClassicAnalyzer();
		}

	}

	private void initializeDir(){

		this.rootDir = IndexCONSTANTS.PASSAGE_INDEX_PATH + this.version + File.separator + this.language;

		try {

			config1S = new IndexWriterConfig(analyzer);
			config1S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory1S = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_1_SENTENCE));

			config2S = new IndexWriterConfig(analyzer);
			config2S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory2S = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_2_SENTENCES));

			config3S = new IndexWriterConfig(analyzer);
			config3S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory3S = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_3_SENTENCES));

			configP = new IndexWriterConfig(analyzer);
			configP.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryP = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_PARAGRAPH));

			configAS = new IndexWriterConfig(analyzer);
			configAS.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryAS = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_ALL_SEGMENTATION));

			configD = new IndexWriterConfig(analyzer);
			configD.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryD = FSDirectory.open(Paths.get(this.rootDir + IndexCONSTANTS.INDEX_PATH_DOCUMENT));

		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.initializeDir()  Could NOT open Passage Index Directories Paths");
			e.printStackTrace();
		}

	}

	public DocBasedModes getDocBasedMode() {
		return docBasedMode;
	}

	public void setDocBasedMode(DocBasedModes docBasedMode) {
		this.docBasedMode = docBasedMode;
	}

	public void setDocBasedMode(String docBasedMode) {
		if (docBasedMode.equals("both"))
			this.docBasedMode = DocBasedModes.BOTH;
		else
			this.docBasedMode = DocBasedModes.ONLY_LMJM;
	}

	public DocBasedModes getParagraphBasedMode() {
		return paragraphBasedMode;
	}

	public void setParagraphBasedMode(DocBasedModes paragraphBasedMode) {
		this.paragraphBasedMode = paragraphBasedMode;
	}

	public void setParagraphBasedMode(String paragraphBasedMode) {
		if (paragraphBasedMode.equals("both"))
			this.paragraphBasedMode = DocBasedModes.BOTH;
		else
			this.paragraphBasedMode = DocBasedModes.ONLY_LMJM;
	}

	public void setQueryExpansion(boolean queryExpansion) {
		this.queryExpansion = queryExpansion;
	}

	public String getDocUsingUrl(String url){
		String content = "";
		try {
			reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.queryAndAddToMap()  Reader could NOT open directory");
			e.printStackTrace();
		}
		Query query = new TermQuery(new Term(IndexCONSTANTS.FIELD_URL,url));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs candidates = null;
		try {
			candidates = searcher.search(query, 1);

			ScoreDoc[] hits = candidates.scoreDocs;
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = null;
				try {
					d = searcher.doc(docId);
				} catch (IOException e) {
					e.printStackTrace();
				}
				content = d.get(IndexCONSTANTS.FIELD_CONTENT);
			}

		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.queryAndAddToMap() Could NOT Search for query: " + query.toString());
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Prints the top 10 of results for each index
	 *
	 * @param queryString String query
	 */
	public Map<String, ArrayList<Passage>> getResults( String queryString) {

		QueryParser contentQueryParser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);

		String expandedQueryString = "";
		Query expandedQuery = null;
		if(this.queryExpansion){
//			expandedQueryString = cleanseQuery(getExpandedQuery(queryString));
			expandedQueryString = getExpandedQuery(cleanseAndRemoveStopwords(queryString));
			try {
				expandedQuery = contentQueryParser.parse(expandedQueryString);
			} catch (ParseException e) {
				System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsParagraphs()  Cannot create query");
				e.printStackTrace();
			}
		}

		//remove questioners
		queryString = queryString.replaceAll("\\?"," ").replaceAll("¿"," " );

		queryString = removeStopPhrases(queryString);

		queryString = QueryParser.escape(queryString);

		// build query
		QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
		Query query = null;
		try {
			query = parser1.parse(queryString);
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResults()  Cannot create query");
			e.printStackTrace();
		}

		Map<String, ArrayList<Passage>> results= new HashMap<>();

		String [] indexTypes = this.targetIndices;
		// if not defined, all indices must be queried (for passagetable response)
		if (indexTypes == null)
			indexTypes = new String[]{IndexCONSTANTS.INDEX_TYPE_DOCUMENT, IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION,
					IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES, IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
					IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE};

		String [] indexSimilarityModels={IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
										IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER};

		for (String indexType : indexTypes) {

			Map<Integer, Passage> passages = new HashMap<>();
			if(this.queryExpansion)
				queryAndAddToMap(expandedQuery, indexType, passages, indexSimilarityModels, topN);
			else
				queryAndAddToMap(query, indexType, passages, indexSimilarityModels, topN);
			// add to results, after filtering duplicates
			Set<Passage> passagesSet = new HashSet<Passage>();
			for(Map.Entry<Integer,Passage> map : passages.entrySet()){
				passagesSet.add(map.getValue());
			}

			// calculate normalized sums for the list
			float maxLMD = Collections.max(passagesSet, Passage.COMPARE_BY_LMD_SCORE).getLMDScore();
			float maxLMJM = Collections.max(passagesSet).getLMJMScore();
			for (Passage passage: passagesSet)
				passage.computeNormalizedSum(maxLMD, maxLMJM);

			results.put(indexType, new ArrayList<>(passagesSet));
		}

		return results;
	}

	public Map<String, ArrayList<Passage>> getResultsUsingDocuments( String queryString) {

		//remove questioners
		queryString = queryString.replaceAll("\\?"," ").replaceAll("¿"," " );

		queryString = removeStopPhrases(queryString);

		queryString = QueryParser.escape(queryString);

		// build query for documents
		QueryParser contentQueryParser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
		Query contentQuery = null;
		try {
			contentQuery = contentQueryParser.parse(queryString);
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocuments()  Cannot create query");
			e.printStackTrace();
		}

		Map<String, ArrayList<Passage>> results= new HashMap<>();
		Set<String> resultIds= new HashSet<>();


		String [] indexTypes = this.targetIndices;
		// if not defined, all indices must be queried (for passagetable response)
		if (indexTypes == null)
			// all except documents
			indexTypes = new String[]{IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION,
					IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES, IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
					IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE};

		String [] indexSimilarityModels={IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
				IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER};


		// first retrieve documents
		Map<Integer, Passage> passages = new HashMap<>();
		queryAndAddToMap(contentQuery, IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passages, indexSimilarityModels, topN);

		// add to results, after filtering duplicates
		Set<Passage> passagesSet = new HashSet<>();
//		ArrayList<Passage> passagesSet = new ArrayList<Passage>();
		for(Map.Entry<Integer,Passage> map : passages.entrySet()){
			passagesSet.add(map.getValue());
		}

        // sort docs and return at most docBasedModeTopN results
		ArrayList<Passage> passagesList = filterDocResults(passagesSet, IndexCONSTANTS.INDEX_TYPE_DOCUMENT);

		// store final doc ids
		for(Passage passage : passagesList){
			resultIds.add(passage.getTextID());
		}

        results.put(IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passagesList);

		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		Query idQuery = null, query = null;
		try {
			// text_id is StringField from index version 6 and it does not need to be analyzed
			if(Integer.parseInt(this.version.split("\\.")[1]) >= 6)
			{
				BooleanQuery.Builder idQueryBuilder = new BooleanQuery.Builder();
				for (String id: resultIds){
					Query oneIdQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_TEXT_ID, id));
					idQueryBuilder.add(oneIdQuery, BooleanClause.Occur.SHOULD);
				}
				idQuery = idQueryBuilder.build();
			}
			else{
				// form query string for other indices
				String idQueryString = "";
				String prefix = "";
				for (String id: resultIds){
					idQueryString += prefix + "\"" + id + "\"";
					prefix = " OR ";
				}

				// analyze query string
				QueryParser idQueryParser = new QueryParser(IndexCONSTANTS.FIELD_TEXT_ID, analyzer);
				idQuery = idQueryParser.parse(idQueryString);
			}
			queryBuilder.add(contentQuery, BooleanClause.Occur.MUST);
			queryBuilder.add(idQuery, BooleanClause.Occur.FILTER);
			query = queryBuilder.build();
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocuments()  Cannot create query");
			e.printStackTrace();
		}


		for (String indexType : indexTypes){
			passages = new HashMap<>();
			queryAndAddToMap(query, indexType, passages, indexSimilarityModels, topN);
			// add to results, use hashset to filter duplicates
//			passagesSet = new HashSet<>();
			passagesList = new ArrayList<>();
			for(Map.Entry<Integer,Passage> map : passages.entrySet()){
                passagesList.add(map.getValue());
			}
			results.put(indexType, passagesList);
		}

		return results;
	}

	public Map<String, ArrayList<Passage>> getResultsUsingDocumentsParagraphs( String queryString, boolean contextRetrieval) {
		QueryParser contentQueryParser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);

		String expandedQueryString = "";
		Query expandedContentQuery = null;
		if(this.queryExpansion){
//			expandedQueryString = cleanseQuery(getExpandedQuery(queryString));
			expandedQueryString = getExpandedQuery(cleanseAndRemoveStopwords(queryString));
			try {
				expandedContentQuery = contentQueryParser.parse(expandedQueryString);
			} catch (ParseException e) {
				System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsParagraphs()  Cannot create query");
				e.printStackTrace();
			}
		}

		//remove questioners
		queryString = cleanseQuery(queryString);

		// build query for documents
		Query contentQuery = null;
		try {
			contentQuery = contentQueryParser.parse(queryString);
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsParagraphs()  Cannot create query");
			e.printStackTrace();
		}

		Map<String, ArrayList<Passage>> results= new HashMap<>();
		Set<String> resultIds= new HashSet<>();


		String [] indexTypes = this.targetIndices;
		// if not defined, all indices must be queried (for passagetable response)
		if (indexTypes == null)
			// all except documents and paragraphs
			indexTypes = new String[]{IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION,
					IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES, IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
					IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE};

		// min number of words each retrieved segment can have
		Map<String, Integer> thresholds = new HashMap<>();
		thresholds.put(IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE, 3);
		thresholds.put(IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES, 6);
		thresholds.put(IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES, 9);
		thresholds.put(IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION, 5);

		String [] indexSimilarityModels={IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
				IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER};


		// first retrieve documents
		Map<Integer, Passage> passages = new HashMap<>();
		queryAndAddToMap(contentQuery, IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passages, indexSimilarityModels, topN);

		// add to results, after filtering duplicates
		Set<Passage> passagesSet = new HashSet<>();
//		ArrayList<Passage> passagesSet = new ArrayList<Passage>();
		for(Map.Entry<Integer,Passage> map : passages.entrySet()){
			passagesSet.add(map.getValue());
		}

		// sort docs and return at most docBasedModeTopN results
		ArrayList<Passage> passagesList = filterDocResults(passagesSet, IndexCONSTANTS.INDEX_TYPE_DOCUMENT);

		// store final doc ids
		for(Passage passage : passagesList){
			resultIds.add(passage.getTextID());
		}

		// add docs to results
        results.put(IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passagesList);

		// form query string for paragraphs and the rest of the indices (they contain the id filter)
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		BooleanQuery.Builder expandedQueryBuilder = new BooleanQuery.Builder();
		Query idQuery = null, query = null, expandedQuery = null;
		try {
			// text_id is StringField from index version 6 and it does not need to be analyzed
			if(Integer.parseInt(this.version.split("\\.")[1]) >= 6)
			{
				BooleanQuery.Builder idQueryBuilder = new BooleanQuery.Builder();
				for (String id: resultIds){
					Query oneIdQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_TEXT_ID, id));
					idQueryBuilder.add(oneIdQuery, BooleanClause.Occur.SHOULD);
				}
				idQuery = idQueryBuilder.build();
			}
			else{
				String idQueryString = "";
				String prefix = "";
				for (String id: resultIds){
					idQueryString += prefix + "\"" + id + "\"";
					prefix = " OR ";
				}

				// analyze query string
				QueryParser idQueryParser = new QueryParser(IndexCONSTANTS.FIELD_TEXT_ID, analyzer);
				idQuery = idQueryParser.parse(idQueryString);
			}
			queryBuilder.add(contentQuery, BooleanClause.Occur.MUST);
			queryBuilder.add(idQuery, BooleanClause.Occur.FILTER);
			query = queryBuilder.build();
			if(queryExpansion){
				expandedQueryBuilder.add(expandedContentQuery, BooleanClause.Occur.MUST);
				expandedQueryBuilder.add(idQuery, BooleanClause.Occur.FILTER);
				expandedQuery = expandedQueryBuilder.build();
			}
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsParagraphs()  Cannot create query");
			e.printStackTrace();
		}


		// query paragraphs
        ArrayList<String> paragraphList = new ArrayList<>();
		passages = new HashMap<>();
		long start = System.currentTimeMillis();
		Query paragraphQuery = query;
		if (contextRetrieval){
			contextRetrievalCall(paragraphQuery, IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, passages, indexSimilarityModels, topN);
		}
		else {
			queryAndAddToMap(paragraphQuery, IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, passages, indexSimilarityModels, topN);
		}


		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time of paragraph query (millis): " + elapsed);

		// add to results, use set to filter duplicates
		passagesSet = new HashSet<>();
		for(Map.Entry<Integer,Passage> map : passages.entrySet()){
			passagesSet.add(map.getValue());
		}

		// sort paragraphs and return at most paragraphBasedModeTopN results
		passagesList = filterDocResults(passagesSet, IndexCONSTANTS.INDEX_TYPE_PARAGRAPH);

		// store returned paragraphs text
		for(Passage passage : passagesList){
			paragraphList.add(passage.getText());
		}


		results.put(IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, passagesList);

		// query the rest of the indices
		for (String indexType : indexTypes){
			passages = new HashMap<>();
			if(indexType.equals(IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION) && this.queryExpansion){
				System.out.println("Expanded query:" + expandedQuery);
				queryAndAddToMap(expandedQuery, indexType, passages, indexSimilarityModels, topN);
			}
			else{
//				System.out.println(query);
				queryAndAddToMap(query, indexType, passages, indexSimilarityModels, topN);
			}
			// add to results, use set to filter duplicates
//			passagesSet = new HashSet<>();
			passagesList = new ArrayList<>();
			for(Map.Entry<Integer,Passage> map : passages.entrySet()){
				String passageContent = map.getValue().getText();
				if(thresholds.containsKey(indexType)){
					int threshold = thresholds.get(indexType);
					String[] passageWords = passageContent.split("\\s+");
					if(passageWords.length < threshold)
						continue;
				}
				for (String paragraph: paragraphList){
					if(paragraph.contains(passageContent) && !passageContent.endsWith("?")){ // remove passages that end with "?"
//					if(paragraph.contains(passageContent)){
						passagesList.add(map.getValue());
						break;
					}
				}
			}
			// calculate normalized sums for the list
			if(passagesList.size() != 0){
				float maxLMD = Collections.max(passagesList, Passage.COMPARE_BY_LMD_SCORE).getLMDScore();
				float maxLMJM = Collections.max(passagesList).getLMJMScore();
				for (Passage passage: passagesList)
					passage.computeNormalizedSum(maxLMD, maxLMJM);
			}
			results.put(indexType, passagesList);
		}

		return results;
	}

	public Map<String, ArrayList<Passage>> getResultsUsingDocumentsRefined( String queryString) {
		//remove questioners
		queryString = queryString.replaceAll("\\?"," ").replaceAll("¿"," " );

		queryString = removeStopPhrases(queryString);

		queryString = QueryParser.escape(queryString);

		// build query for documents
		QueryParser contentQueryParser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
		Query contentQuery = null;
		try {
			contentQuery = contentQueryParser.parse(queryString);
		} catch (ParseException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsRefined()  Cannot create query");
			e.printStackTrace();
		}

		Map<String, ArrayList<Passage>> results= new HashMap<>();
		Set<String> resultIds= new HashSet<>();

		String [] indexTypes = this.targetIndices;
		// if not defined, all indices must be queried (for passagetable response)
		if (indexTypes == null)
			// all except documents
			indexTypes = new String[]{IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION,
					IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES, IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
					IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE};

		String [] indexSimilarityModels={IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
				IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER};


		// first retrieve documents
		Map<Integer, Passage> passages = new HashMap<>();
		queryAndAddToMap(contentQuery, IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passages, indexSimilarityModels, topN);

		// add to results, after filtering duplicates
        Set<Passage> passagesSet = new HashSet<>();
		for(Map.Entry<Integer,Passage> map : passages.entrySet()){
			passagesSet.add(map.getValue());
		}

		// sort docs and return at most docBasedModeTopN results
		ArrayList<Passage> passagesList = filterDocResults(passagesSet, IndexCONSTANTS.INDEX_TYPE_DOCUMENT);

		// store final doc ids
		for(Passage passage : passagesList){
			resultIds.add(passage.getTextID());
		}


		results.put(IndexCONSTANTS.INDEX_TYPE_DOCUMENT, passagesList);

		// list containing query terms
        ArrayList<String> terms = ParsingFunctions.getTokens(analyzer,queryString);


		// build query that filters based on document text ids
		Query idQuery = null;
		try {
			// text_id is StringField from index version 6 and it does not need to be analyzed
			if(Integer.parseInt(this.version.split("\\.")[1]) >= 6)
			{
				BooleanQuery.Builder idQueryBuilder = new BooleanQuery.Builder();
				for (String id: resultIds){
					Query oneIdQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_TEXT_ID, id));
					idQueryBuilder.add(oneIdQuery, BooleanClause.Occur.SHOULD);
				}
				idQuery = idQueryBuilder.build();
			}
			else{
				// form query string for other indices
				String idQueryString = "";
				String prefix = "";
				for (String id: resultIds){
					idQueryString += prefix + "\"" + id + "\"";
					prefix = " OR ";
				}

				// analyze query string
				QueryParser idQueryParser = new QueryParser(IndexCONSTANTS.FIELD_TEXT_ID, analyzer);
				idQuery = idQueryParser.parse(idQueryString);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		SdmRetrievalHandler sdm = new SdmRetrievalHandler();
		sdm.buildQueries(terms, idQuery);

		for (String indexType : indexTypes){
			passages = new HashMap<>();
			try {
				switch (indexType) {
					case IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION:
						reader = DirectoryReader.open(directoryAS);
						break;
					case IndexCONSTANTS.INDEX_TYPE_PARAGRAPH:
						reader = DirectoryReader.open(directoryP);
						break;
					case IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES:
						reader = DirectoryReader.open(directory3S);
						break;
					case IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES:
						reader = DirectoryReader.open(directory2S);
						break;
					case IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE:
						reader = DirectoryReader.open(directory1S);
						break;
				}
			} catch (IOException e) {
				System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsRefined()  Reader could NOT open directory");
				e.printStackTrace();
			}


			IndexSearcher searcher = new IndexSearcher(reader);
			for (String model : indexSimilarityModels) {

			    // define similarity model
				Similarity similarity = null;
				switch (model){
					case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
						similarity =  new LMDirichletSimilarity();
						break;
					case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
						similarity =  new LMJelinekMercerSimilarity((float)0.5);
						break;
				}
                searcher.setSimilarity(similarity);

				try {
					// retrieve top N docs
					List<Map.Entry<Integer, Double>> topEntries = sdm.retrieveDocuments(searcher,topN);

                    // add top N docs to map
                    for (Map.Entry<Integer,Double> entry : topEntries)
                    {
                        int docID = entry.getKey();
                        float score = entry.getValue().floatValue();
                        if (score == 0)
                            break;
                        Document d = null;
                        try {
                            d = searcher.doc(docID);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String content = d.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue();
                        int segID = (d.getField("SegmentID") == null) ? -1 : Integer.parseInt(d.getField("SegmentID").stringValue());

                        addToMap(passages, docID, content, indexType, model, score, segID);
//                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }
                }
                catch(IOException e){
                    System.out.println("WebKIndex :: PassageIndexHandler.getResultsUsingDocumentsRefined() Could NOT Search for query: " + queryString);
                    e.printStackTrace();
                }


			}
			// add to results
			passagesList = new ArrayList<>();
			for(Map.Entry<Integer,Passage> map : passages.entrySet()){
				passagesList.add(map.getValue());
			}

			results.put(indexType, passagesList);
		}

		return results;
	}

	/**
	 * Executes an input query, and adds results to an <Integer, Passage> map
	 * @param query
	 * @param indexType
	 * @param passages
	 * @param similarityModels
	 * @param numResults
	 */
	private void queryAndAddToMap(Query query, String indexType, Map<Integer,Passage> passages,
							  String[] similarityModels, int numResults){
		reader = null;
		try {
			switch (indexType) {
				case IndexCONSTANTS.INDEX_TYPE_DOCUMENT:
					reader = DirectoryReader.open(directoryD);
					break;
				case IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION:
					reader = DirectoryReader.open(directoryAS);
					break;
				case IndexCONSTANTS.INDEX_TYPE_PARAGRAPH:
					reader = DirectoryReader.open(directoryP);
					break;
				case IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES:
					reader = DirectoryReader.open(directory3S);
					break;
				case IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES:
					reader = DirectoryReader.open(directory2S);
					break;
				case IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE:
					reader = DirectoryReader.open(directory1S);
					break;
			}
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.queryAndAddToMap()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		for(String similarityModel: similarityModels){
			Similarity similarity = null;
			switch (similarityModel){
				case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
					similarity =  new DefaultSimilarity();
					break;
				case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
					similarity =  new LMDirichletSimilarity();
					break;
				case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
					similarity =  new LMJelinekMercerSimilarity((float)0.5);
					break;
			}

			searcher.setSimilarity(similarity);


			TopDocs candidates = null;
			try {
				candidates = searcher.search(query, numResults);

				ScoreDoc[] hits = candidates.scoreDocs;
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = null;
					try {
						d = searcher.doc(docId);
					} catch (IOException e) {
						e.printStackTrace();
					}
					float score = hits[i].score;

					String content = d.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue();
					String contentResponse = "";
					IndexableField contentResponseField = d.getField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);
					if(contentResponseField!=null)
						contentResponse = contentResponseField.stringValue();
					else
						contentResponse = content;

					d.add(new TextField("score", Float.toString(score) , Field.Store.YES));

					int segID = (d.getField("SegmentID") == null) ? -1 : Integer.parseInt(d.getField("SegmentID").stringValue());

					// add text ids
					String textID = d.getField(IndexCONSTANTS.FIELD_TEXT_ID).stringValue();
					String url = d.getField(IndexCONSTANTS.FIELD_URL).stringValue();

					Explanation explanationObj = searcher.explain(query, docId);
					String explanation = explanationObj.toString();

					List<String> matchingTerms = new ArrayList<>();
					addMatchingTermsToList(matchingTerms, query, searcher, docId);

					addToMap(passages, docId, content, contentResponse, indexType, similarityModel, score, segID, textID, url,
							explanation, matchingTerms);

				}

			} catch (IOException e) {
				System.out.println("WebKIndex :: PassageIndexHandler.queryAndAddToMap() Could NOT Search for query: " + query.toString());
				e.printStackTrace();
			}
		}
	}

	private Set<String> queryAndGetTextIDs(Query query, ArrayList<Similarity> similarities, int numResults){
		Set<String> resultIds = new HashSet<>();
		// search documents first
		try {
			reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.queryAndGetTextIDs()  Reader could NOT open All segmentation directory");
			e.printStackTrace();
		}

		IndexSearcher searcher = new IndexSearcher(reader);

		for (Similarity similarity : similarities) {
			searcher.setSimilarity(similarity);
			TopDocs candidates = null;

			try {
				candidates = searcher.search(query, numResults);

				ScoreDoc[] hits = candidates.scoreDocs;
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = null;
					try {
						d = searcher.doc(docId);
					} catch (IOException e) {
						e.printStackTrace();
					}

					String textID = d.getField(IndexCONSTANTS.FIELD_TEXT_ID).stringValue();
					resultIds.add(textID);
				}

			} catch (IOException e) {
				System.out.println("WebKIndex :: PassageIndexHandler.queryAndGetTextIDs() Could NOT Search for query: " + query.toString());
				e.printStackTrace();
			}
		}
		return resultIds;
	}

	/**
	 * sort and filter document (or paragraph) results
	 * @param passagesSet
	 * @return
	 */
	private ArrayList<Passage> filterDocResults(Set<Passage> passagesSet, String indexType){

		DocBasedModes correctMode = this.docBasedMode;
		int correctTopN = this.docBasedModeTopN;
		if(indexType.equals(IndexCONSTANTS.INDEX_TYPE_PARAGRAPH)){
			correctTopN = this.paragraphBasedModeTopN;
			correctMode = this.paragraphBasedMode;
		}

		ArrayList<Passage> passagesList = new ArrayList<>(passagesSet);

		// remove docs that do not have at least one fullstop
		ArrayList<Passage> passagesWithNoDot = new ArrayList<>();
		for(Passage passage : passagesList){
			if( !passage.getText().contains(".") )
				passagesWithNoDot.add(passage);
		}
		passagesList.removeAll(passagesWithNoDot);

		// remove docs that have few words
		ArrayList<Passage> shortPassages = new ArrayList<>();
		for(Passage passage : passagesList){
			if( passage.getText().split("\\s+").length < this.minWordsFilter )
				shortPassages.add(passage);
		}
		passagesList.removeAll(shortPassages);

		if(correctMode.equals(DocBasedModes.BOTH)){
			List<Passage> lmjmList = null;
			List<Passage> lmdList = null;

			// sort docs based on LMJM score and return at most correctTopN results
			Collections.sort(passagesList, Collections.reverseOrder());
			if(correctTopN < passagesList.size())
				lmjmList = new ArrayList<>(passagesList.subList(0, correctTopN));
			else
				lmjmList = new ArrayList<>(passagesList);

			// add text ids for the next queries and
			// remove passages with LMJM score equal to 0
			ArrayList<Passage> passagesWithZeroLMJM = new ArrayList<>();
			for(Passage passage : lmjmList){
				if(passage.getLMJMScore()==0)
					passagesWithZeroLMJM.add(passage);
			}
			lmjmList.removeAll(passagesWithZeroLMJM);

			// sort docs based on LMD score and return at most correctTopN results
			Collections.sort(passagesList, Collections.reverseOrder(Passage.COMPARE_BY_LMD_SCORE));
			if(correctTopN < passagesList.size())
				lmdList = new ArrayList<>(passagesList.subList(0, correctTopN));
			else
				lmdList = new ArrayList<>(passagesList);

			// add text ids for the next queries and
			// remove passages with LMJM score equal to 0
			ArrayList<Passage> passagesWithZeroLMD = new ArrayList<>();
			for(Passage passage : lmdList){
				if(passage.getLMDScore()==0)
					passagesWithZeroLMD.add(passage);
			}
			lmdList.removeAll(passagesWithZeroLMD);

			// create new passage list
			Set<Passage> newPassageSet = new HashSet<>();
			newPassageSet.addAll(lmjmList);
			newPassageSet.addAll(lmdList);
			passagesList = new ArrayList<>(newPassageSet);
		}
		else{
			// sort docs based on LMJM score and return at most 5 results
			Collections.sort(passagesList, Collections.reverseOrder());
			if(correctTopN < passagesList.size())
				passagesList = new ArrayList<>(passagesList.subList(0, correctTopN));

			// add text ids for the next queries and
			// remove passages with LMJM score equal to 0
			ArrayList<Passage> passagesWithZeroLMJM = new ArrayList<>();
			for(Passage passage : passagesList){
				if(passage.getLMJMScore()==0)
					passagesWithZeroLMJM.add(passage);
			}
			passagesList.removeAll(passagesWithZeroLMJM);
		}



		return passagesList;
	}

	/**
	 * If passage with this docID exists in passages Map, add the score for this model. Else create a new passage and add the score
	 *
	 * @param passages
	 * @param docId
	 * @param content
	 * @param indexType
	 * @param model
	 * @param score
	 */
	private void addToMap(Map<Integer, Passage> passages, int docId, String content, String indexType, String model, float score, int segID) {
		if(!passages.containsKey(docId)){
			Passage p = new Passage(indexType, content, docId, segID);
			passages.put(docId,p);

		}
		Passage p = passages.get(docId);
		switch (model){
			case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
				p.setVsmScore(score);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
				p.setLMDScore(score);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
				p.setLMJMScore(score);
				break;
		}
	}

    private void addToMap(Map<Integer, Passage> passages, int docId, String content, String indexType, String model,
						  float score, int segID, String textID, String url) {
        if(!passages.containsKey(docId)){
            Passage p = new Passage(indexType, content, docId, segID, textID, url);
            passages.put(docId,p);

        }
        Passage p = passages.get(docId);
        switch (model){
            case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
                p.setVsmScore(score);
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
                p.setLMDScore(score);
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
                p.setLMJMScore(score);
                break;
        }
    }

	private void addToMap(Map<Integer, Passage> passages, int docId, String content, String indexType, String model,
						  float score, int segID, String textID, String url, String explanation) {
		if(!passages.containsKey(docId)){
			Passage p = new Passage(indexType, content, docId, segID, textID, url);
			passages.put(docId,p);

		}
		Passage p = passages.get(docId);
		switch (model){
			case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
				p.setVsmScore(score);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
				p.setLMDScore(score);
				p.setExplanationLMD(explanation);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
				p.setLMJMScore(score);
				p.setExplanationLMJM(explanation);
				break;
		}
	}

	private void addToMap(Map<Integer, Passage> passages, int docId, String content, String contentResponse, String indexType, String model,
						  float score, int segID, String textID, String url, String explanation, List<String> matchingTerms) {
		if(!passages.containsKey(docId)){
			Passage p = new Passage(indexType, content, contentResponse, docId, segID, textID, url);
			p.setMatchingTerms(matchingTerms);
			passages.put(docId,p);

		}
		Passage p = passages.get(docId);
		switch (model){
			case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
				p.setVsmScore(score);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
				p.setLMDScore(score);
				p.setExplanationLMD(explanation);
				break;
			case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
				p.setLMJMScore(score);
				p.setExplanationLMJM(explanation);
				break;
		}
	}

	private String printCandidates(TopDocs candidates, IndexSearcher searcher) {

		String response = "";
		ScoreDoc[] hits = candidates.scoreDocs;
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = null;
			try {
				d = searcher.doc(docId);
			} catch (IOException e) {
				e.printStackTrace();
			}
			float score = hits[i].score;
			response += (i + 1)	+ ". Score: " + score;
			System.out.println((i + 1)
							+ ". Score: " + score
						/*+ " " + d.get(IndexCONSTANTS.FIELD_CONTENT) + "\t"*/
			);
			JSONObject obj = new JSONObject();

			Iterator<IndexableField> iter = d.iterator();
			while (iter.hasNext()) {
				IndexableField temp = iter.next();
				obj.put(temp.name(),temp.stringValue());
			}
			System.out.println(obj.toString());
			response += obj.toString();
		}
		return response;
	}

	private void contextRetrievalCall(Query query, String indexType, Map<Integer,Passage> passages,
									  String[] similarityModels, int numResults){

		for(String similarityModel: similarityModels){
			ContextRetrievalHandler crh = new ContextRetrievalHandler(indexType, similarityModel, this.rootDir);
			crh.queryAndConstructResultMaps(query);
			List<Map.Entry<DocPassageIDPair, Double>> topEntries = crh.getTopNDocs(numResults);

			// add top N docs to map
			for (Map.Entry<DocPassageIDPair,Double> entry : topEntries)
			{
				DocPassageIDPair key = entry.getKey();
				int docID = crh.getDocIdFromIndexUsingPair(key);
				float score = entry.getValue().floatValue();
				if (score == 0)
					break;
				Document d = crh.getDocumentByID(docID);
				String content = d.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue();
				int segID = (d.getField("SegmentID") == null) ? -1 : Integer.parseInt(d.getField("SegmentID").stringValue());

				addToMap(passages, docID, content, indexType, similarityModel, score, segID);
			}
		}
	}

	private String cleanseQuery(String queryString){
		String cleansedQuery = queryString.replaceAll("\\?"," ").replaceAll("¿"," " );
		cleansedQuery = removeStopPhrases(cleansedQuery);
		cleansedQuery = QueryParser.escape(cleansedQuery);
		return cleansedQuery;
	}

	private String cleanseAndRemoveStopwords(String queryString){
		String cleansedQuery = removeStopPhrases(queryString);
		String cleansedNoStopWords = "";
		String prefix = "";
		List<String> tokens = ParsingFunctions.getTokens(new CustomStopAnalyzer(this.analyzer.getStopwordSet()), cleansedQuery);
		for (String token :tokens) {
			cleansedNoStopWords += prefix + token;
			prefix = " ";
		}
		return cleansedNoStopWords;
	}

	private String getExpandedQuery(String query){
		StringBuilder sb = new StringBuilder(query);
//		Word2VecHandler w2v = new Word2VecHandler();
//		w2v.loadModel("embeddings/w2v_model_es");
//		w2v.formVocabEmbeddingMatrix();
		int queryLength = query.split("\\s+").length;
		List<String> closestWords = w2vHandlers.get(this.language)
				.getClosestWordsUsingEmbeddingMatrix(query, queryLength + 3);
		for (String word: closestWords){
			sb.append(" " + word);
		}
		return sb.toString();
	}

	public String singleResultQueryFromTable(String queryString, String retrievalMethod){


		String targetIndex = IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION;
		this.targetIndices = new String[]{targetIndex};

		// get response intended for passage table service
		Map<String, ArrayList<Passage>> response = new HashMap<>();
		if(retrievalMethod.equals("indexBased"))
			response = getResults(queryString);
		else if(retrievalMethod.equals("documentBased"))
			response = getResultsUsingDocuments(queryString);
		else if(retrievalMethod.equals("documentParagraphBased"))
			response = getResultsUsingDocumentsParagraphs(queryString,false);
		else if(retrievalMethod.equals("documentParagraphContextBased"))
			response = getResultsUsingDocumentsParagraphs(queryString,true);
		else if(retrievalMethod.equals("documentBasedRefined"))
			response = getResultsUsingDocumentsRefined(queryString);

		// get all segmentation response and sort it, to return the first result
		ArrayList<Passage> allSegmentationResponse = response.get(targetIndex);
		if (allSegmentationResponse.size() == 0)
			return "";

		Passage topResult = null;
		if(this.language.equals("de") || this.language.equals("pl")){
			Collections.sort(allSegmentationResponse, Collections.reverseOrder(Passage.COMPARE_BY_LMD_SCORE));
			topResult = allSegmentationResponse.get(0);

			if(topResult.getLMDScore() == 0){
				Collections.sort(allSegmentationResponse, Collections.reverseOrder());
				topResult = allSegmentationResponse.get(0);
			}
			else{
				// if any passage contains the top response, designate it as top
				for (Passage passage: allSegmentationResponse){
					if (passage.getLMDScore()==0)
						break;
					if (passage.getText().contains(topResult.getText())){
						topResult = passage;
					}
				}
			}
		}
		else{
			Collections.sort(allSegmentationResponse, Collections.reverseOrder(Passage.COMPARE_BY_SCORES_SUM));
//			Collections.sort(allSegmentationResponse, Collections.reverseOrder());
			topResult = allSegmentationResponse.get(0);

//			if(topResult.getLMJMScore() == 0){
//				Collections.sort(allSegmentationResponse, Collections.reverseOrder(Passage.COMPARE_BY_LMD_SCORE));
//				topResult = allSegmentationResponse.get(0);
//			}
//			else{
				// if any passage contains the top response, designate it as top
				for (Passage passage: allSegmentationResponse){
//					if (passage.getLMJMScore()==0)
//						break;
					if (passage.getText().contains(topResult.getText())){
						topResult = passage;
					}
				}
//			}
		}

		// clean some patterns at the start of the sentence
		String trailingNumbersPattern = "(?s)^" // start of input
				+ "\\d+" // followed by a number
				+ "[.)]" // followed by a dot or a parentheses
				+ "\\s*"; // followed by zero or more space characters

		String content = topResult.getTextResponse().replaceAll(trailingNumbersPattern,"");

		String startWithQuestionPattern = "(?s)^" // start of input
				+ "[^\\p{Punct}]+" // followed by any non-punctuation character
				+ "\\?+" // followed by one or more question mark characters
				+ "\\s*"; // followed by zero or more space characters

		content = content.replaceAll(startWithQuestionPattern,"");

		JSONObject json = new JSONObject();
		String url = topResult.getUrl();
		json.put(IndexCONSTANTS.FIELD_URL, url);
		json.put(IndexCONSTANTS.FIELD_CONTENT, content);
		List<String> matchingTerms = topResult.getMatchingTerms();
		if(matchingTerms!=null)
			json.put("matchingTerms", matchingTerms);
		URI uri = null;
		try {
			uri = new URI(url);
			String domain = uri.getHost();
			json.put("domain", domain);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

//		System.out.println();
//		System.out.println(content);
//		System.out.println();
//		System.out.println(json);

//		return topResult.getText();
		return json.toString();
	}

	public String getResultsForAnnotationTool(String queryString){
		String targetIndex = IndexCONSTANTS.INDEX_TYPE_DOCUMENT;
		this.targetIndices = new String[]{targetIndex};
		Map<String, ArrayList<Passage>> response = getResults(queryString);
		ArrayList<Passage> documentsResponse = response.get(targetIndex);
		Collections.sort(documentsResponse, Collections.reverseOrder(Passage.COMPARE_BY_NORMALIZED_SUM));
		JSONArray documentsJSON = new JSONArray();
		for (Passage documentReponse: documentsResponse){
			String url = documentReponse.getUrl();
			String content = documentReponse.getText();
			double score = documentReponse.getNormalizedSum();
			JSONObject documentJSON = new JSONObject();
			documentJSON.put(IndexCONSTANTS.FIELD_URL, url);
			documentJSON.put(IndexCONSTANTS.FIELD_CONTENT, content);
			documentJSON.put("score", score);
			documentsJSON.put(documentJSON);
		}
		JSONObject result = new JSONObject();
		result.put("documents", documentsJSON);
		result.put("keywords", queryString);
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("result", result);
		return jsonResponse.toString();
	}

	public String getResultsForAnnotationTool(String topicQuery, ArrayList<String> queryList){
		String targetIndex = IndexCONSTANTS.INDEX_TYPE_DOCUMENT;
		this.targetIndices = new String[]{targetIndex};
		Set<Passage> totalDocumentsSet = new HashSet<>();
		for(String query : queryList){
			System.out.println("Executing query: " + query);

			// without QE
			this.queryExpansion = false;
			Map<String, ArrayList<Passage>> response = getResults(query);
			ArrayList<Passage> documentsResponse = response.get(targetIndex);
			totalDocumentsSet.addAll(documentsResponse);

			//with QE
			this.queryExpansion = true;
			Map<String, ArrayList<Passage>> responseQE = getResults(query);
			ArrayList<Passage> documentsResponseQE = responseQE.get(targetIndex);
			totalDocumentsSet.addAll(documentsResponseQE);
		}
		// WARNING: IF WE USE NORMALIZED SUM SCORE, IT HAS TO BE RECOMPUTED
		ArrayList<Passage> totalDocumentsResponse = new ArrayList<>(totalDocumentsSet);
//		Collections.sort(totalDocumentsResponse, Collections.reverseOrder(Passage.COMPARE_BY_NORMALIZED_SUM));
		JSONArray documentsJSON = new JSONArray();
		for (Passage documentReponse: totalDocumentsResponse){
			String url = documentReponse.getUrl();
			String content = documentReponse.getText();
//			double score = documentReponse.getNormalizedSum();
			JSONObject documentJSON = new JSONObject();
			documentJSON.put(IndexCONSTANTS.FIELD_URL, url);
			documentJSON.put(IndexCONSTANTS.FIELD_CONTENT, content);
			documentJSON.put("score", 1); // hard coded because there are many different scores
			documentsJSON.put(documentJSON);
		}
		JSONObject result = new JSONObject();
		result.put("documents", documentsJSON);
		result.put("keywords", topicQuery);
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("result", result);
		jsonResponse.put("resultsNum", totalDocumentsResponse.size());
		return jsonResponse.toString();
	}

	public ArrayList<Passage> getResultsForAnnotationTool(ArrayList<String> queryList){
		String targetIndex = IndexCONSTANTS.INDEX_TYPE_DOCUMENT;
		this.targetIndices = new String[]{targetIndex};
		Set<Passage> totalDocumentsSet = new HashSet<>();
		for(String query : queryList){
			Map<String, ArrayList<Passage>> response = getResults(query);
			ArrayList<Passage> documentsResponse = response.get(targetIndex);
			totalDocumentsSet.addAll(documentsResponse);
		}
		ArrayList<Passage> totalDocumentsResponse = new ArrayList<>(totalDocumentsSet);
		return totalDocumentsResponse;
	}

	public String getScrapedContentFromUrl(String url){
		String result = "";
		Query query = new TermQuery(new Term(IndexCONSTANTS.FIELD_URL,url));
		try {
			reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getScrapedContentFromUrl()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs candidates = null;
		try {
			candidates = searcher.search(query, 1);
			ScoreDoc[] hits = candidates.scoreDocs;
			if(hits.length !=0){
			int docId = hits[0].doc;
			Document d = null;
			try {
				d = searcher.doc(docId);
			} catch (IOException e) {
				e.printStackTrace();
			}
				String content = d.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue();
				JSONObject json = new JSONObject();
				json.put("document_id", docId);
				json.put(IndexCONSTANTS.FIELD_CONTENT, content);
				result = json.toString();
			}
		}
		catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getScrapedContentFromUrl() Could NOT Search for query: " + query.toString());
			e.printStackTrace();
		}
		return result;
	}

	public List<String> getScrapedUrls(){
		List<String> urls = new ArrayList<>();
		reader = null;
		try {
			reader = DirectoryReader.open(directoryD);for (int i=0; i<reader.maxDoc(); i++) {
				Document doc = reader.document(i);
				String url = doc.get("url");
				urls.add(url);
			}
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getScrapedUrls()  Reader could NOT open directory");
			e.printStackTrace();
		}
		return urls;
	}

	public String getDocumentUsingID(String indexType, String id){
		DirectoryReader reader = null;
		try {
			switch (indexType) {
				case IndexCONSTANTS.INDEX_TYPE_DOCUMENT:
					reader = DirectoryReader.open(directoryD);
					break;
				case IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION:
					reader = DirectoryReader.open(directoryAS);
					break;
				case IndexCONSTANTS.INDEX_TYPE_PARAGRAPH:
					reader = DirectoryReader.open(directoryP);
					break;
				case IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES:
					reader = DirectoryReader.open(directory3S);
					break;
				case IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES:
					reader = DirectoryReader.open(directory2S);
					break;
				case IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE:
					reader = DirectoryReader.open(directory1S);
					break;
				default:
					return "ERROR: Wrong index type was requested.";
			}
			Document doc = reader.document(Integer.parseInt(id));
			if(doc!=null)
				return doc.toString();
			else
				return "ERROR: Requested id does not exist.";
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexPassageIndexHandler.getDocumentUsingID()  Reader could NOT open directory");
			e.printStackTrace();
			return "ERROR: IOException.";
		}
	}

	public String getNumDocsPerIndex(){
		DirectoryReader reader = null;
		String [] indexTypes = {IndexCONSTANTS.INDEX_TYPE_DOCUMENT,IndexCONSTANTS.INDEX_TYPE_PARAGRAPH,IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION,
				IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES,IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
				IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE};
		StringBuilder sb = new StringBuilder();
		sb.append("NUMBER OF DOCUMENTS PER INDEX\n");
		sb.append("-----------------------------\n");
		for (String indexType : indexTypes) {
			try {
				switch (indexType) {
					case IndexCONSTANTS.INDEX_TYPE_DOCUMENT:
						reader = DirectoryReader.open(directoryD);
						break;
					case IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION:
						reader = DirectoryReader.open(directoryAS);
						break;
					case IndexCONSTANTS.INDEX_TYPE_PARAGRAPH:
						reader = DirectoryReader.open(directoryP);
						break;
					case IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES:
						reader = DirectoryReader.open(directory3S);
						break;
					case IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES:
						reader = DirectoryReader.open(directory2S);
						break;
					case IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE:
						reader = DirectoryReader.open(directory1S);
						break;
				}
				int numDocs = reader.numDocs();
				sb.append(indexType + " : " + numDocs + "\n");
			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexPassageIndexHandler.getNumDocsPerIndex()  Reader could NOT open directory");
				e.printStackTrace();
				return "ERROR: IOException.";
			}
		}
		return sb.toString();
	}

	public List<DocumentResult> getDocsForRelExt(QueryObject queryObj) {

		List<DocumentResult> results = new ArrayList<>();
		List<String> terms = queryObj.getTerms();
		QueryObject.Operator operator = queryObj.getOperator();
		int maxResults = queryObj.getMaxResults();
		BooleanClause.Occur occur;
		if(operator == QueryObject.Operator.OR)
			occur = BooleanClause.Occur.SHOULD;
		else
			occur = BooleanClause.Occur.MUST;

		String indexType = IndexCONSTANTS.INDEX_TYPE_DOCUMENT;
		try {
			reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexPassageIndexHandler.getDocsForRelExt()  Reader could NOT open Document directory");
			e.printStackTrace();
		}


		// We are using 2 Similarity models combined. LMJM & LMD
		// we get top ten passage from both models
		//each passage is scored according to ranking.
		//first passage is scored with 1, second with 1/2, third with 1/3 etc.
		//the passages out of top ten is scored 0
		//the two scores of each passage are added
		//the top scored passage is returned

		IndexSearcher searcher = new IndexSearcher(reader);
		ArrayList<Similarity> similarities = new ArrayList<Similarity>();
		Similarity similarityLMJM =  new LMJelinekMercerSimilarity((float)0.5);
		similarities.add(similarityLMJM);
		//Similarity similarityLMD =  new LMDirichletSimilarity(2500);
		//similarities.add(similarityLMD);
		//Similarity similarity =  new BM25Similarity();

		Map<String, Document> docs = new HashMap<String, Document>();
		for (Similarity similarity : similarities) {
			searcher.setSimilarity(similarity);
			TopDocs candidates = null;
			Query query = null;
			try {
				//if query is definitional, search only for paragraphs
				BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
				for(String term: terms){
					QueryParser parser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
					Query termQuery = parser.parse("\""+term+"\"");
					finalQuery.add(termQuery, occur);
				}
				query = finalQuery.build();

			} catch (ParseException e) {
				System.out.println("WebKIndex :: IndexPassageIndexHandler.getDocsForRelExt()  Cannot create query");
				e.printStackTrace();
			}

//			int querySize = ((BooleanQuery) query).clauses().size();
//			if(querySize == 0)
//				return "{}";

			try {
				candidates = searcher.search(query, maxResults);

				ScoreDoc[] hits = candidates.scoreDocs;
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = null;
					try {
						d = searcher.doc(docId);
					} catch (IOException e) {
						e.printStackTrace();
					}
					float score = hits[i].score;
					String content = d.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue();
					String url = d.getField(IndexCONSTANTS.FIELD_URL).stringValue();
					String pageID = d.getField(IndexCONSTANTS.FIELD_WEBPAGE_ID).stringValue();
					String textID = d.getField(IndexCONSTANTS.FIELD_TEXT_ID).stringValue();

					DocumentResult result = new DocumentResult();
					result.setScore(score);
					result.setText(content);
					result.setUrl(url);
					result.setPageID(pageID);
					result.setTextID(textID);
					results.add(result);
				}

			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexPassageIndexHandler.getDocsForRelExt() Could NOT Search for query");
				e.printStackTrace();
			}
		}

		return results;
	}

	private boolean isDefinitionalQuery(String queryString) {
		String[] definitionalQueries = {"qué es","explicarme", "informarme sobre", "información sobre"};
		for (String definitionalQuery : definitionalQueries) {
			if (queryString.toLowerCase().contains(definitionalQuery)){
				return true;
			}
		}
		return false;
	}

	private String removeStopPhrases(String queryString) {

		String[] stopPhrases = {"por favor"};

		for (String stopPhrase : stopPhrases) {
			queryString = queryString.toLowerCase().replaceAll(stopPhrase, "");
		}
		return queryString;
	}

	private ArrayList<Document> cleanDocs(ArrayList<Document> docs, String query) {


		//first check if document is substring of query (stemmed and stopworded)
		try {
			TokenStream queryStream = analyzer.tokenStream(IndexCONSTANTS.FIELD_CONTENT, query);
			OffsetAttribute offsetAttribute = queryStream.addAttribute(OffsetAttribute.class);
			CharTermAttribute charTermAttribute = queryStream.addAttribute(CharTermAttribute.class);

			queryStream.reset();
			ArrayList<String> queryTerms = new ArrayList<String>();
			while (queryStream.incrementToken()) {
				int startOffset = offsetAttribute.startOffset();
				int endOffset = offsetAttribute.endOffset();
				String term = charTermAttribute.toString();
				queryTerms.add(term);
			}
			queryStream.end();
			queryStream.close();
			Iterator<Document> dIter = docs.iterator();
			while(dIter.hasNext()){
				Document doc = dIter.next();
				TokenStream contentStream = analyzer.tokenStream(IndexCONSTANTS.FIELD_CONTENT, doc.getField(IndexCONSTANTS.FIELD_CONTENT).stringValue());
				OffsetAttribute offsetAttributeC = contentStream.addAttribute(OffsetAttribute.class);
				CharTermAttribute charTermAttributeC = contentStream.addAttribute(CharTermAttribute.class);

				contentStream.reset();
				ArrayList<String> contentTerms = new ArrayList<String>();
				while (contentStream.incrementToken()) {
					int startOffset = offsetAttributeC.startOffset();
					int endOffset = offsetAttributeC.endOffset();
					String term = charTermAttributeC.toString();

					//remove all words containing no letter
					if (!term.replaceAll("[^A-Za-z]","").equals("")) {
						contentTerms.add(term);
					}
				}

				if (queryTerms.size() >= contentTerms.size()){
					for (String queryTerm : queryTerms) {
						Iterator<String> cIter = contentTerms.iterator();
						while (cIter.hasNext()) {
							String cont = cIter.next();
							if (cont.equals(queryTerm)){
								cIter.remove();
							}
						}
					}
					if (contentTerms.size() == 0){
						dIter.remove();
					}
				}
				contentStream.end();
				contentStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		//boost documents which are supersets of the first response
		for (int i = 1; i < docs.size(); i++) {
			if (docs.get(i).getField(IndexCONSTANTS.FIELD_CONTENT).stringValue().contains(docs.get(0).getField(IndexCONSTANTS.FIELD_CONTENT).stringValue())){
				float prevScore = Float.parseFloat(docs.get(i).getField("score").stringValue());
				docs.get(i).removeFields("score");
				docs.get(i).add(new TextField("score",Float.toString(5f + prevScore), Field.Store.YES));
			}
		}

		return sortDocs(docs);
	}

	private ArrayList<Document> sortDocs(Map<String, Document> docs) {
		ArrayList<Document> newList = new ArrayList<Document>();
		while (!docs.isEmpty()){
			float score = 0f;
			String id = null;

			Set<String> keys = docs.keySet();
			for (String key : keys) {
				if (Float.parseFloat(docs.get(key).getField("score").stringValue()) > score){
					id = key;
					score = Float.parseFloat(docs.get(key).getField("score").stringValue());
				}
			}
			newList.add(docs.get(id));
			docs.remove(id);
		}
		return newList;
	}

	private ArrayList<Document> sortDocs(ArrayList<Document> docs) {
		ArrayList<Document> newList = new ArrayList<Document>();
		while (!docs.isEmpty()){
			float score = 0f;
			int id = -1;

			for (int i=0; i< docs.size(); i++) {
				if (Float.parseFloat(docs.get(i).getField("score").stringValue()) > score){
					score = Float.parseFloat(docs.get(i).getField("score").stringValue());
					id = i;
				}
			}
			newList.add(docs.get(id));
			docs.remove(id);
		}
		return newList;
	}

	/**
	 * Get a Document and return an json as String only with fieldName and fieldData of each field in Document
	 * @param doc
	 * @return
	 */
	private String docToJSON(Document doc) {

		JSONObject obj = new JSONObject();

		JSONArray concepts = new JSONArray();
		JSONArray nes = new JSONArray();

		Iterator<IndexableField>  iter = doc.iterator();
		while (iter.hasNext()) {
			IndexableField temp = iter.next();
			obj.put(temp.name(), temp.stringValue());
		}



		return obj.toString();
	}

	public int getTopN() {
		return topN;
	}

	public void setTopN(int topN) {
		this.topN = topN;
	}

	public int getDocBasedModeTopN() {
		return docBasedModeTopN;
	}

	public void setDocBasedModeTopN(int docBasedModeTopN) {
		this.docBasedModeTopN = docBasedModeTopN;
	}

	public int getParagraphBasedModeTopN() {
		return paragraphBasedModeTopN;
	}

	public void setParagraphBasedModeTopN(int paragraphBasedModeTopN) {
		this.paragraphBasedModeTopN = paragraphBasedModeTopN;
	}

	/**
	 * validates input (passage retrieval component) version and language
	 * @param language
	 * @param version
	 * @return
	 */
	public static Map.Entry<Boolean,String> validateInput(String language, String version){
		PassageRetrievalVersion versionItem = PassageRetrievalVersion.componentVersionItems.get(version);
		if(versionItem == null){
			String response = "Input version " + version + " does not exist!!!\n" +  PassageRetrievalVersion.getSupportedVersionsDetailed();
			return new AbstractMap.SimpleEntry<>(false,response);
		}
		if(!(versionItem.isLanguageSupported(language))){
			String response = "Language " + language + " is not supported is version "+ version +"!!!\n" + versionItem.getSupportedLanguages();
			return new AbstractMap.SimpleEntry<>(false,response);
		}
		return new AbstractMap.SimpleEntry<>(true,"");
	}


	/**
	 * calculates jaccard similarity between two strings
	 * @param s1
	 * @param s2
	 * @return
	 */
    private double jaccardSimilarity(String s1, String s2){
        ArrayList<String> terms1 = ParsingFunctions.getTokens(analyzer, s1.replaceAll("\\p{Punct}", ""));
        Set<String> set1 = new HashSet<String>(terms1);
        ArrayList<String> terms2 = ParsingFunctions.getTokens(analyzer, s2.replaceAll("\\p{Punct}", ""));
        Set<String> set2 = new HashSet<String>(terms2);
        Set<String> union = new HashSet<String>(set1);
        union.addAll(set2);
        Set<String> intersection = new HashSet<String>(set1);
        intersection.retainAll(set2);
        double numerator = intersection.size();
        double denominator = union.size();
        double similarity = numerator / denominator;
        return similarity;
    }

	/**
	 * method not used for now, utilizes jaccard similarity to clean similar documents
	 * @param passagesSet
	 * @param indexType
	 * @return
	 */
    private ArrayList<Passage> filterPassagesUsingSimilarity(Set<Passage> passagesSet, String indexType){
		ArrayList<Passage> passageList = new ArrayList<>();
		for (Passage passageInCheck: passagesSet){
			String textInCheck = passageInCheck.getText();
			Passage toRemove = null;
			for (Passage insertedPassage: passageList){
				String insertedText = insertedPassage.getText();
				double similarity = jaccardSimilarity(textInCheck, insertedText);
				// if they are similar and not-inserted text is bigger, replace, else do not insert it
				if(similarity > IndexCONSTANTS.MINIMUM_SIMILARITY){
					if(textInCheck.split("\\s+").length > insertedText.split("\\s+").length){
						toRemove = insertedPassage;
					}
					break;
				}
			}
			// if there is a passage to replace
			if(toRemove!=null){
				passageList.remove(toRemove);
				passageList.add(passageInCheck);
			}
			// else check if it should be just added to the results
			else{
				if(indexType.equals(IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES)
						|| indexType.equals(IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES)
						|| indexType.equals(IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION))
				{
					if(textInCheck.split("\\s+").length > 10)
						passageList.add(passageInCheck);
				}
				else
				{
					passageList.add(passageInCheck);
				}
			}
		}
		return passageList;
	}

	public List<String> getAllSentences(){
		List<String> sentences = new ArrayList<>();
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory1S);for (int i=0; i<reader.maxDoc(); i++) {
				Document doc = reader.document(i);
				String url = doc.get("content");
				sentences.add(url);
			}
		} catch (IOException e) {
			System.out.println("WebKIndex :: PassageIndexHandler.getAllSentences()  Reader could NOT open directory");
			e.printStackTrace();
		}
		return sentences;
	}

	private void addMatchingTermsToList(List<String> terms, Query query,IndexSearcher searcher,int docId) throws IOException
	{
		if(query instanceof TermQuery )
		{
			if (searcher.explain(query, docId).isMatch() == true
					// add only matches in the "content" field (exclude the ones in the "text_id" field)
					&& ((TermQuery) query).getTerm().field().equals(IndexCONSTANTS.FIELD_CONTENT))
				terms.add(((TermQuery) query).getTerm().text());
			return;
		}

		if(query instanceof BooleanQuery )
		{
			for (BooleanClause clause : (BooleanQuery)query) {
				addMatchingTermsToList(terms, clause.getQuery(), searcher, docId);
			}
			return;
		}

		if (query instanceof MultiTermQuery)
		{
			if (!(query instanceof FuzzyQuery)) //FuzzQuery doesn't support SetRewriteMethod
				((MultiTermQuery)query).setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);

			addMatchingTermsToList(terms, query.rewrite(searcher.getIndexReader()), searcher, docId);
		}

	}

	public List<String> getConceptsUPF(String query){
//		List<String> tokens = ParsingFunctions.getTextTokens(this.analyzer, query);
		List<String> concepts = ConceptExtractionHandler.extractConcepts(query, this.language);
		return concepts;
	}
}
