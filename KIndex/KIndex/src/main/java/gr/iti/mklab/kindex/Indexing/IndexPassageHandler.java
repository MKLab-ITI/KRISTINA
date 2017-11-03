package gr.iti.mklab.kindex.Indexing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import gr.iti.mklab.kindex.ConceptExtraction.UPF.ConceptExtractionHandler;
import gr.iti.mklab.kindex.Indexing.ContentManagers.german.ContentManagerDE;
import gr.iti.mklab.kindex.Indexing.ContentManagers.german.ContentPDFManagerDE;
import gr.iti.mklab.kindex.Indexing.ContentManagers.polish.ContentManagerPL;
import gr.iti.mklab.kindex.Indexing.ContentManagers.polish.ContentPDFManagerPL;
import gr.iti.mklab.kindex.Indexing.ContentManagers.spanish.ContentManagerES;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManager;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentPDFManager;
import gr.iti.mklab.kindex.Indexing.ContentManagers.spanish.ContentPDFManagerES;
import gr.iti.mklab.kindex.Indexing.LuceneCustomClasses.CustomAnalyzerSpanish;
import gr.iti.mklab.kindex.Indexing.LuceneCustomClasses.LightStemSpanishAnalyzer;
import gr.iti.mklab.kindex.Indexing.LuceneCustomClasses.SnowBallSpanishAnalyzer;
import gr.iti.mklab.kindex.Indexing.LuceneCustomClasses.StopwordLists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IndexPassageHandler {

	DirectoryReader reader;
	Analyzer analyzer;
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
	Directory directoryAS; //directory for All segmentation index
	Directory directoryD; //directory for document index
	IndexWriter writer1S; //Index Writer for one sentence index
	IndexWriter writer2S; //Index Writer for two sentences index
	IndexWriter writer3S; //Index Writer for three sentences index
	IndexWriter writerP; //Index Writer for paragraph index
	IndexWriter writerAS; //Index Writer for paragraph index
	IndexWriter writerD; //Index Writer for document index
	String language;

	int segmentID = 0;
	/**
	 * Constructor without parameters
	 * Update: From now on it calls another constructor with language as a parameter
	 */
	public IndexPassageHandler() {
		this(IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE);

	}

	public IndexPassageHandler(String language) {
		this.language = language;

		System.out.println("Passage index language: " + this.language);
		System.out.println("Passage index version: " + IndexCONSTANTS.PASSAGE_INDEX_VERSION);

		initializeAnalyzer();
		System.out.println("Initialized analyzer: " + this.analyzer.getClass());
		initializeDir();
	}

	private void initializeAnalyzer(){

		if(this.language.equals("es"))
			analyzer = new SnowBallSpanishAnalyzer();
		else if(this.language.equals("de"))
			analyzer = new GermanAnalyzer(new CharArraySet(StopwordLists.stopWordsDE,false));
			// use english in any other case, also set language to find appropriate folder
		else if(this.language.equals("pl"))
			analyzer = new PolishAnalyzer();
		else {
			this.language = "en";
			analyzer = new ClassicAnalyzer();
		}

	}

	private void initializeDir(){

		try {
			config1S = new IndexWriterConfig(analyzer);
			config1S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory1S = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_1_SENTENCE));
			writer1S = new IndexWriter(directory1S, config1S);writer1S.close();writer1S = null;config1S=null;

			config2S = new IndexWriterConfig(analyzer);
			config2S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory2S = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_2_SENTENCES));
			writer2S = new IndexWriter(directory2S, config2S);writer2S.close();writer2S = null;config2S=null;

			config3S = new IndexWriterConfig(analyzer);
			config3S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directory3S = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_3_SENTENCES));
			writer3S = new IndexWriter(directory3S, config3S);writer3S.close();writer3S = null;config3S=null;

			configP = new IndexWriterConfig(analyzer);
			configP.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryP = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_PARAGRAPH));
			writerP = new IndexWriter(directoryP, configP);writerP.close();writerP = null;configP=null;

			configAS = new IndexWriterConfig(analyzer);
			configAS.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryAS = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_ALL_SEGMENTATION));
			writerAS = new IndexWriter(directoryAS, configAS);writerAS.close();writerAS = null;configAS=null;

			configD = new IndexWriterConfig(analyzer);
			configD.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.directoryD = FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
					+ this.language + IndexCONSTANTS.INDEX_PATH_DOCUMENT));
			writerD = new IndexWriter(directoryD, configD);writerD.close();writerD = null;configD=null;

		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler()  Could NOT open Passage Index Directories Paths");
			e.printStackTrace();
		}

	}

	/**
	 * Function for opening index to write
	 */
	public void open() {
		try {
			this.reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.open()  Could NOT read Passage Index Directories");
			e.printStackTrace();
		}

		//open writers for the directories
		try {
			config1S = new IndexWriterConfig(analyzer);
			config1S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer1S = new IndexWriter(directory1S, config1S);

			config2S = new IndexWriterConfig(analyzer);
			config2S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer2S = new IndexWriter(directory2S, config2S);

			config3S = new IndexWriterConfig(analyzer);
			config3S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer3S = new IndexWriter(directory3S, config3S);

			configP = new IndexWriterConfig(analyzer);
			configP.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writerP = new IndexWriter(directoryP, configP);

			configAS = new IndexWriterConfig(analyzer);
			configAS.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writerAS = new IndexWriter(directoryAS, configAS);

			configD = new IndexWriterConfig(analyzer);
			configD.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writerD = new IndexWriter(directoryD, configD);
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.open()  Could NOT open Writers Index Directories");
			e.printStackTrace();
		}
	}

	/**
	 * Function for closing index after writing
	 */
	public void close(){
		try {
			writer1S.close();
			writer2S.close();
			writer3S.close();
			writerP.close();
			writerAS.close();
			writerD.close();
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.close() Could NOT close writer.");
			e.printStackTrace();
		}
	}

	/**
	 * Function for indexing a JSONObject.<br>
 	 * It splits the {@value IndexCONSTANTS#FIELD_CONTENT} field in passages (1 sentence, 2 sentences, 3 sentences and paragraph).<br>
	 * For every passage it indexes a document including all other json's fields in the corresponding index. <br>
	 * The Jsons containing field: {@value IndexCONSTANTS#FIELD_MEDIUM} and values: {@value IndexCONSTANTS#MEDIUM_WEBSITE},{@value IndexCONSTANTS#MEDIUM_PDF} will be identified form {@value IndexCONSTANTS#FIELD_URL}.<br>
	 * No duplicates allowed for these jsons.
	 *
	 * @param jsonObj The json to index
	 * @return
	 */
	public boolean indexJson(JSONObject jsonObj){

		this.segmentID = 0;

		int oneSentenceID = 0;
		int twoSentencesID = 0;
		int threeSentencesID = 0;
		int paragraphID = 0;

		Document doc = new Document(); //doc to add in index

		Iterator<?> keys = jsonObj.keys();

		boolean toCheck = false; //to check (or not) if document already exists. Check only in PDFs and Websites
		String url = "";
		String content = "";
		String medium = "";

		//Create a document with all json's fields but content
		while (keys.hasNext()) {
			String key = keys.next().toString();
			String value =  jsonObj.getString(key);

			if (key.equals(IndexCONSTANTS.FIELD_CONTENT)) {
				content = value;
			}
			else if (key.equals(IndexCONSTANTS.FIELD_URL)){
					url = value;
					doc.add(new StringField(key, value, Field.Store.YES));
				}
			else if (key.equals(IndexCONSTANTS.FIELD_TEXT_ID) || key.equals(IndexCONSTANTS.FIELD_PDF_ID)
					|| key.equals(IndexCONSTANTS.FIELD_WEBPAGE_ID)){
				doc.add(new StringField(key, value, Field.Store.YES));
			}
			else{
				doc.add(new TextField(key, value, Field.Store.YES));
				if (key.equals(IndexCONSTANTS.FIELD_MEDIUM)){
					medium = value;
					if (value.equals(IndexCONSTANTS.MEDIUM_WEBSITE) || value.equals(IndexCONSTANTS.MEDIUM_PDF)){
						toCheck=true;
					}

				}
			}


		}

		boolean doNotAdd=false;


		if(url.contains("#")){
			url = url.substring(0, url.indexOf("#"));
			while (doc.get(IndexCONSTANTS.FIELD_URL) != null){
				doc.removeField(IndexCONSTANTS.FIELD_URL);
			}
			doc.add(new StringField(IndexCONSTANTS.FIELD_URL, url, Field.Store.YES));
		}

		String[] forumPatterns = {"whattoexpect.com/forums/", "forums.webmd"};

		// do not add forums to index
		for(String pattern: forumPatterns){
			if(url.contains(pattern)){
				System.out.println("Forum detected. It will not be added to Index.");
				return false;
			}
		}


		for(String pattern: IndexCONSTANTS.URL_PATTERNS_TO_REMOVE){
			if(url.contains(pattern)){
				System.out.println("Url pattern to remove detected. It will not be added to Index.");
				return false;
			}
		}

		for(String pattern: IndexCONSTANTS.URLS_TO_REMOVE){
			if(url.equals(pattern)){
				System.out.println("Url to remove detected. It will not be added to Index.");
				return false;
			}
		}

		if(url.contains("medonet.pl")){
			if(!url.contains("medonet.pl/choroby-od-a-do-z") && !url.contains("medonet.pl/zdrowie")){
				System.out.println("Url to remove detected. It will not be added to Index.");
				return false;
			}
		}

		if (toCheck && (!url.equals(""))){

			TopDocs hits = null;
			try {
				Query finalQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_URL,url));
				DirectoryReader reader1 = DirectoryReader.open(FSDirectory.open(Paths.get(IndexCONSTANTS.PASSAGE_INDEX_PATH + IndexCONSTANTS.PASSAGE_INDEX_VERSION
						+ this.language + IndexCONSTANTS.INDEX_PATH_DOCUMENT)));
				IndexSearcher searcher = new IndexSearcher(reader1);
				hits = searcher.search(finalQuery,1);
			} catch (IOException e) {
				System.out.println("KIndex :: IndexPassageHandler.indexJson()  Could NOT Search the 1 sentence Index if url exists - " + url);
				e.printStackTrace();
			}

			if ((hits != null) && (hits.totalHits > 0)){
				doNotAdd = true;
			}
		}

		if (doNotAdd){
//			System.out.println("NOT added url:" + url );
			return false;
		}
		else{
			//if the document has to be added, extract all passages and index them to corresponding index

			List<String> refinedParagraphs = null;
			// get text and paragraph content from html
			if (medium.equals(IndexCONSTANTS.MEDIUM_WEBSITE)){
				ContentManager cm = null;
				if(IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("es")){
					cm = new ContentManagerES(url, content);
				}
				else if (IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("pl")){
					cm = new ContentManagerPL(url, content);
				}
				else if (IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("de")){
					cm = new ContentManagerDE(url, content);
				}
				cm.parseHtml();
				content = cm.getText();
				refinedParagraphs = cm.getParagraphs();

				//add content field to be returned
				doc.add(new StoredField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE, content));
				content = content.replace("{s_o_t} ","").replace(" {e_o_t}","");

				// add title
				String title = cm.getTitle();
				doc.add(new TextField(IndexCONSTANTS.FIELD_TITLE, title, Field.Store.YES));
			}
			else if (medium.equals(IndexCONSTANTS.MEDIUM_PDF)){
				ContentPDFManager cpm = null;
				if(IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("es")){
					cpm = new ContentPDFManagerES(url, content);
				}
				else if (IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("pl")){
					cpm = new ContentPDFManagerPL(url, content);
				}
				else if (IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("de")){
					cpm = new ContentPDFManagerDE(url, content);
				}
				cpm.parseHtmlFromPdf();
				content = cpm.getText();
				refinedParagraphs = cpm.getParagraphs();
			}

			//first add the content to doc and index the whole document
			doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, content, Field.Store.YES));

			indexDoc("Document", doc);

			//remove the content and title fields from document
			removeContentFieldFromDocument(doc);
			doc.removeFields(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);
			doc.removeFields(IndexCONSTANTS.FIELD_TITLE);

			//paragraph indexing

			for (String paragraph : refinedParagraphs) {
				String [] sentences = splitSentences(paragraph);

				if(doc.get(IndexCONSTANTS.FIELD_MEDIUM).equals(IndexCONSTANTS.MEDIUM_PDF))
					sentences = reformSentences(sentences);


				//add content field to be returned
				doc.add(new StoredField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE, paragraph));
				paragraph = paragraph.replace("{s_o_t} ","").replace(" {e_o_t}","");

				doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, paragraph, Field.Store.YES));
				doc.add(new StringField(IndexCONSTANTS.FIELD_SEGMENT_ID, String.valueOf(paragraphID), Field.Store.YES));
				indexDoc("Paragraph",doc);
				removeContentFieldFromDocument(doc);
				doc.removeFields(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);

				//index group of three sentences
				if (sentences.length >= 3){
					int endPos = 2;
					while (endPos < sentences.length) {
						int startPos = endPos-2;
						String groupOfSentences = "";
						String prefix = "";
						while (startPos <= endPos) {
							groupOfSentences += prefix + sentences[startPos];
							prefix = " ";
							startPos++;
						}

						//add content field to be returned
						doc.add(new StoredField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE, groupOfSentences));
						groupOfSentences = groupOfSentences.replace("{s_o_t} ","").replace("{e_o_t}","");

						doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, groupOfSentences, Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_SEGMENT_ID, String.valueOf(threeSentencesID), Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_PARAGRAPH_ID, String.valueOf(paragraphID), Field.Store.YES));

						++threeSentencesID;
						indexDoc("ThreeSentences",doc);
						removeContentFieldFromDocument(doc);
						doc.removeFields(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);
						endPos ++;
					}
				}

				//index group of two sentences
				if (sentences.length >= 2){
					int endPos = 1;
					while (endPos < sentences.length) {
						int startPos = endPos-1;
						String groupOfSentences = "";
						String prefix = "";
						while (startPos <= endPos) {
							groupOfSentences += prefix + sentences[startPos];
							prefix = " ";
							startPos++;
						}

						//add content field to be returned
						doc.add(new StoredField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE, groupOfSentences));
						groupOfSentences = groupOfSentences.replace("{s_o_t} ","").replace("{e_o_t}","");

						doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, groupOfSentences, Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_SEGMENT_ID, String.valueOf(twoSentencesID), Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_PARAGRAPH_ID, String.valueOf(paragraphID), Field.Store.YES));


						++twoSentencesID;
						indexDoc("TwoSentences",doc);
						removeContentFieldFromDocument(doc);
						doc.removeFields(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);
						endPos ++;
					}
				}

				//index for a sentence
				if (sentences.length >= 1){
					int startPos = 0;
					while (startPos < sentences.length) {
						//add content field to be returned
						doc.add(new StoredField(IndexCONSTANTS.FIELD_CONTENT_RESPONSE, sentences[startPos]));
						sentences[startPos] = sentences[startPos].replace("{s_o_t} ","").replace("{e_o_t}","");
						doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, sentences[startPos], Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_SEGMENT_ID, String.valueOf(oneSentenceID), Field.Store.YES));
						doc.add(new StringField(IndexCONSTANTS.FIELD_PARAGRAPH_ID, String.valueOf(paragraphID), Field.Store.YES));

						++oneSentenceID;
						indexDoc("OneSentence",doc);
						removeContentFieldFromDocument(doc);
						doc.removeFields(IndexCONSTANTS.FIELD_CONTENT_RESPONSE);
						startPos ++;
					}
				}
				++paragraphID;

			}
		}

		return true;
	}

	private String[] cleanFromSpaces(String[] array) {
		List list=new ArrayList();
		for (String string : array) {
			if((string!=null) && (!string.equals("")) && (!string.equals(" ")) ){
				list.add(string);
			}
		}

		// Converting list object to string array
		String[] response=(String[]) list.toArray(new String[list.size()]);

		return response;
	}

	/**
	 * Indexes the document in the mentioned Index.<br>
	 *     Available Indexers:<br>
 	 *     <ul>
	 *         <li>OneSentence</li>
	 *         <li>TwoSentences</li>
	 *         <li>ThreeSentences</li>
	 *         <li>Paragraph</li>
	 *         <li>Document</li>
 	 *     </ul>
	 * @param index
	 * @param doc
	 */
	private void indexDoc(String index, Document doc){
		System.out.println("Indexing " + index + ": " + (doc.get(IndexCONSTANTS.FIELD_CONTENT)));
		try {

			//if content is less than 3 words OR it contains url, do not index it
			String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

			Pattern p = Pattern.compile(URL_REGEX);
			String content = doc.get(IndexCONSTANTS.FIELD_CONTENT);
			Matcher m = p.matcher(content);
			if (doc.get(IndexCONSTANTS.FIELD_CONTENT).split(" ").length > 2 || m.find()) {
				doc.add(new TextField("IndexType", index, Field.Store.YES));

				switch (index) {
					case "OneSentence":
						writer1S.addDocument(doc);
						break;
					case "TwoSentences":
						writer2S.addDocument(doc);
						break;
					case "ThreeSentences":
						writer3S.addDocument(doc);
						break;
					case "Paragraph":
						writerP.addDocument(doc);
						break;
					case "Document":
						writerD.addDocument(doc);
						break;
				}

				//if doc does not contain a whole document, index it in Al Segmentation index
				if (!index.equals("Document")) {

					//to add the document in All_segmentation index, we have to add the segment id
					//Every segment will be unique. text-id+:+segmentID
					//We do it that way for identifing the segments in evaluation
					removeFieldFromDocument(IndexCONSTANTS.FIELD_SEGMENT_ID, doc); // remove numbering given in the last index
					removeFieldFromDocument(IndexCONSTANTS.FIELD_PARAGRAPH_ID, doc);
					doc.add(new TextField("SegmentID", Integer.toString(this.segmentID), Field.Store.YES));
					doc.add(new TextField("SegmentType", index, Field.Store.YES)); // for index v7 and later
					writerAS.addDocument(doc);
					this.segmentID++;
					doc.removeFields("SegmentID");
					doc.removeFields("SegmentType");
//					doc.removeFields(IndexCONSTANTS.FIELD_CONCEPTS);
				}

				doc.removeFields("IndexType");
			}
			else {
				removeFieldFromDocument(IndexCONSTANTS.FIELD_SEGMENT_ID, doc); // remove numbering given in the last index
				removeFieldFromDocument(IndexCONSTANTS.FIELD_PARAGRAPH_ID, doc);
				System.out.println("-- Too short OR contains url. Not indexed" );
			}
		}catch(IOException e){
			System.out.println("KIndex :: IndexPassageHandler.indexJson()  Could NOT write to " + index + " Index ");
			e.printStackTrace();
		}


	}

	public void commitWriters(){

		try {
			writer1S.commit();
			writer2S.commit();
			writer3S.commit();
			writerP.commit();
			writerAS.commit();
			writerD.commit();
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.commitWriters()  Could NOT commit writers" );
			e.printStackTrace();
		}
	}

	private void removeContentFieldFromDocument(Document doc) {
		while (doc.get(IndexCONSTANTS.FIELD_CONTENT) != null){
			doc.removeField(IndexCONSTANTS.FIELD_CONTENT);
		}
	}

	private void removeFieldFromDocument(String field, Document doc) {
		while (doc.get(field) != null){
			doc.removeField(field);
		}
	}



	/**
	 * Prints the top 10 of results for each index
	 *
	 * @param queryString String query
	 */
	public void printResults( String queryString) {


		//Print most top 10 relevant documents
		System.out.println("---------------------------------------------");
		System.out.println("Printing Relevant Documents");
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directoryD);
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.printResults()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
		searcher.setSimilarity(similarityVS);

		TopDocs candidates = null;
		QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
		Query query = null;
		try {
			query = parser1.parse(queryString);
		} catch (ParseException e) {
			System.out.println("KIndex :: IndexPassageHandler.printResults()  Cannot create query");
			e.printStackTrace();
		}

		try {
			candidates = searcher.search(query, 10);
			printCandidates(candidates,searcher);


		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			e.printStackTrace();
		}
		System.out.println("---------------------------------------------");
		System.out.println("Printing Relevant Paragraphs");

		//Print most top 10 relevant paragraphs
		reader = null;
		try {
			reader = DirectoryReader.open(directoryP);
		} catch (IOException e) {
			System.out.println("KIndex :: IndexPassageHandler.printResults()  Reader could NOT open directory");
			e.printStackTrace();
		}
		searcher = new IndexSearcher(reader);
		searcher.setSimilarity(similarityVS);

		candidates = null;
		try {
			query = parser1.parse(queryString);
		} catch (ParseException e) {
			System.out.println("KIndex :: IndexPassageHandler.printResults()  Cannot create query");
			e.printStackTrace();
		}

		try {
			candidates = searcher.search(query, 10);
			printCandidates(candidates,searcher);


		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			e.printStackTrace();
		}

		System.out.println("---------------------------------------------");

	}

	private void printCandidates(TopDocs candidates, IndexSearcher searcher) {

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

			System.out.println((i + 1)
							+ ". Score: " + score
						/*+ " " + d.get(IndexCONSTANTS.FIELD_CONTENT) + "\t"*/
			);
			JSONObject obj = new JSONObject();

			Iterator<IndexableField>  iter = d.iterator();
			while (iter.hasNext()) {
				IndexableField temp = iter.next();
				obj.put(temp.name(),temp.stringValue());
			}
			System.out.println(obj.toString());
		}
	}

	/**
	 * Refines paragraphs split by a simple "\n" character
	 * @param paragraphs
	 * @return
	 */
	public String[] reformParagraphs(String[] paragraphs){
		List<String> paragraphsList = new ArrayList<>(Arrays.asList(paragraphs));

		// first strip all empty characters
		for (int i = 0; i< paragraphsList.size() ; i++) {
			String paragraph = paragraphsList.get(i);
			if(paragraph!=null){
				paragraph = paragraph.trim();
				paragraphsList.set(i, paragraph);
			}
		}

		// remove empty and null strings
		paragraphsList.removeAll((Arrays.asList("", null)));

		if(paragraphsList.size() == 0)
			return paragraphs;

		List<String> newParagraphsList = new ArrayList<>();
		// then merge paragraphs that should not be splitted
		for (int i = 0; i<paragraphsList.size() - 1 ; i++) {

			// compare previous and next paragraph, merge it to the next paragraph index if they should not be splitted
			// else add it to the new, refined list
			String prev = paragraphsList.get(i);
			String next = paragraphsList.get(i + 1);
//			if (Pattern.matches("\\p{Punct}", prev.substring(prev.length() - 1)) && Character.isUpperCase(next.charAt(0))) {
			if (Character.isUpperCase(next.charAt(0)) || next.charAt(0) == '¿') {
				newParagraphsList.add(prev);
			}
			else{
				String newParagraph = prev + "\n" + next;
				paragraphsList.set(i+1, newParagraph);
			}
		}

		// previous loop does not add last element to the new list so we add it here
		newParagraphsList.add(paragraphsList.get(paragraphsList.size() - 1 ));

		String[] newParagraphs = newParagraphsList.toArray(new String[newParagraphsList.size()]);

		return newParagraphs;
	}

	public String[] splitSentences(String text){
		ArrayList<String> sentencesList = new ArrayList<>();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit");
		if(IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE.equals("pl"))
			props.setProperty("tokenize.language", "en");
		else
			props.setProperty("tokenize.language", IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE);

		// shut off the annoying intialization messages
		RedwoodConfiguration.empty().capture(System.err).apply();

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// enable stderr again
		RedwoodConfiguration.current().clear().apply();

		Annotation document = new Annotation(text);
		// run all Annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//		System.out.println("Number of sentences detected: " + sentences.size());
		for (CoreMap sentence : sentences) {

			// get sentence text and add it to the sentences list
			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);

			// clear line breaks inside sentence (replace it with a tab character)
			sentenceText = sentenceText.replace("\n","\t");

			// remove control characters
			sentenceText = sentenceText.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");

			sentencesList.add(sentenceText);
		}

		String[] sentencesArray = sentencesList.toArray(new String[sentencesList.size()]);

		return sentencesArray;
	}

	/**
	 * Clear bullets from pdfs converted into "O"
	 * @param sentences
	 * @return
	 */
	public String[] reformSentences(String[] sentences){
		ArrayList<String> newSentenceList = new ArrayList<>();
		for (String sentence: sentences){
			String[] splitted = sentence.split(" O ");
			for (String chunk: splitted){
				newSentenceList.add(chunk);
			}
		}
		String[] newSentenceArray = new String[newSentenceList.size()];
		newSentenceArray = newSentenceList.toArray(newSentenceArray);
		return newSentenceArray;
	}

	private List<List<String>> getConceptsPerSentence(String[] sentences){
		List<List<String>> concepts = new ArrayList<>();
		for (String sentence: sentences){
			List<String> sentenceConcepts = ConceptExtractionHandler.extractConcepts(sentence, IndexCONSTANTS.PASSAGE_INDEX_lANGUAGE);
			concepts.add(sentenceConcepts);
		}
		return concepts;
	}

	private List<String> getParagraphConcepts(List<List<String>> concepts){
		List<String> paragraphConcepts = new ArrayList<>();
		for (List<String> sentenceConcepts : concepts)
			paragraphConcepts.addAll(sentenceConcepts);
		return paragraphConcepts;
	}

}






















