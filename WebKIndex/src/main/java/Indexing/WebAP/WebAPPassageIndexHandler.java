package Indexing.WebAP;

import Indexing.IndexCONSTANTS;
import Indexing.LuceneCustomClasses.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Thodoris Tsompanidis on 28/6/2016.
 */
public class WebAPPassageIndexHandler {



	DirectoryReader reader;
	Analyzer analyzer;
	Directory directory1S; //directory for one sentence index
	Directory directory2S; //directory for two sentences index
	Directory directory3S; //directory for three sentences index
	Directory directoryAS; //directory for paragraph index
	Directory directoryD; //directory for document index

	public WebAPPassageIndexHandler() {

		try {
			analyzer = new CustomAnalyzer();

			this.directory1S = FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_ONE_SENTENCE_PATH));

			this.directory2S = FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_TWO_SENTENCES_PATH));

			this.directory3S = FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_THREE_SENTENCES_PATH));

			this.directoryAS = FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_ALL_SEGMENTATIONS_PATH));

			this.directoryD = FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_DOCUMENT_PATH));

		} catch (IOException e) {
			System.out.println("WebKIndex :: WebAPPassageIndexHandler()  Could NOT open Passage Index Directories Paths");
			e.printStackTrace();
		}


	}

	/**
	 * Gets a QueryObject Id and returns the corresponding query text from WebAP dataset
	 * @param qID
	 * @return
	 */
	private String getQueryFromID(int qID){
		try {

			File file = new File(WebAP_CONSTANTS.INPUT_QUERIES_FILE_PATH);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String json = new String(data, "UTF-8");



			JSONObject jsonObject = new JSONObject(json);

			JSONArray queries = jsonObject.getJSONArray("queries");

			Iterator<Object> iterator =  queries.iterator();
			while (iterator.hasNext()) {
				JSONObject q = (JSONObject)iterator.next();
				if (Integer.parseInt( (String) q.get("number")) == qID ){
					return (String) q.get("text");
				}
			}
		} catch (Exception e) {
			System.out.println("WebKIndex :: WebAPPassageIndexHandler()  Could NOT Parse the query json ");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Prints the top_N of results for each index
	 *
	 * @param qID int QueryObject ID
	 */
	public Map<String, ArrayList<WebAPPassage>> getResults( int qID) {

		String queryString = getQueryFromID(qID);

		Map<String, ArrayList<WebAPPassage>> results= new HashMap<>();

		String [] indexTypes = {WebAP_CONSTANTS.INDEX_ONE_SENTENCE_TYPE,WebAP_CONSTANTS.INDEX_TWO_SENTENCES_TYPE,WebAP_CONSTANTS.INDEX_THREE_SENTENCES_TYPE, WebAP_CONSTANTS.INDEX_ALL_SEGMENTATIONS_TYPE};

		String [] indexSimilarityModels={IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE, IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
										IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER};

		for (String indexType : indexTypes) {

			Map<String, WebAPPassage> passages = new HashMap<>();
			DirectoryReader reader = null;
			try {
				switch (indexType) {
					case WebAP_CONSTANTS.INDEX_ONE_SENTENCE_TYPE:
						reader = DirectoryReader.open(directory1S);
						break;
					case WebAP_CONSTANTS.INDEX_TWO_SENTENCES_TYPE:
						reader = DirectoryReader.open(directory2S);
						break;
					case WebAP_CONSTANTS.INDEX_THREE_SENTENCES_TYPE:
						reader = DirectoryReader.open(directory3S);
						break;
					case WebAP_CONSTANTS.INDEX_ALL_SEGMENTATIONS_TYPE:
						reader = DirectoryReader.open(directoryAS);
						break;
				}
			} catch (IOException e) {
				System.out.println("KIndex :: IndexPassageHandler.printResults()  Reader could NOT open directory");
				e.printStackTrace();
			}
			for (String model : indexSimilarityModels) {
				Similarity similarity = null;
				switch (model){
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

				IndexSearcher searcher = new IndexSearcher(reader);
				searcher.setSimilarity(similarity);

				TopDocs candidates = null;
				QueryParser parser1 = new QueryParser(WebAP_CONSTANTS.DOCUMENT_FIELD_CONTENT, analyzer);
				Query query = null;
				try {
					query = parser1.parse(queryString);
				} catch (ParseException e) {
					System.out.println("KIndex :: IndexPassageHandler.printResults()  Cannot create query");
					e.printStackTrace();
				}

				try {
					candidates = searcher.search(query, WebAP_CONSTANTS.GET_RESULTS_TOP_N);

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

						addToMap(passages, d, indexType, model, score, qID);

					}

				} catch (IOException e) {
					System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
					e.printStackTrace();
				}
			}

			ArrayList<WebAPPassage> passagesArray = new ArrayList<WebAPPassage>();
			for(Map.Entry<String,WebAPPassage> map : passages.entrySet()){
				passagesArray.add(map.getValue());
			}
			results.put(indexType, passagesArray);
		}

		return results;
	}

	/**
	 * If passage with this docID exists in passages Map, add the score for this model. Else create a new passage and add the score
	 *
	 * @param passages
	 * @param indexType
	 * @param model
	 * @param score
	 */
	private void addToMap(Map<String, WebAPPassage> passages, Document doc, String indexType, String model, float score, int qID) {

		String gID = doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_DOC_NO).stringValue() + ":" + doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_SENTENCE_ID).stringValue();
		if(!passages.containsKey(gID)){
			//for the relevance, it may concern other query, so check it
			int target_QID = Integer.parseInt(doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_TARGET_QID).stringValue());
			String relevance = (target_QID == qID) ? doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_RELEVANCE_WITH_QUERY).stringValue() : "NONE";

			WebAPPassage p = new WebAPPassage(
					indexType,
					doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_CONTENT ).stringValue(),
					doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_DOC_NO).stringValue(),
					doc.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_SENTENCE_ID).stringValue(),
					relevance);
			passages.put(gID,p);
		}
		WebAPPassage p = passages.get(gID);
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


}
