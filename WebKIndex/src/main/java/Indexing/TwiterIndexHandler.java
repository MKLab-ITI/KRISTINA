package Indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to handle Index for Twitter Posts in KIndex
 *
 * Created by Thodoris Tsompanidis on 26/11/2015.
 */
public class TwiterIndexHandler {

	Analyzer analyzer;
	Directory directory;
	IndexWriterConfig config;
	IndexWriter writer;/*
	String FIELD_ID = "ID";
	String FIELD_URL = "URL";
	String FIELD_CONTENT = "CONTENT";*/
	int NO_OF_RESULTS_QUERY = 1;/*
	private final static String DEFAULT_PATH = ("D:\\INDEX\\Twitter");*/

	public TwiterIndexHandler() {
		analyzer = new ClassicAnalyzer();
		Path path= Paths.get(IndexCONSTANTS.INDEX_PATH);
		try {
			directory = FSDirectory.open(path);
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
		} catch (IOException e) {
			System.out.println("WebKIndex :: TwiterIndexHandler()  Could NOT create writer");
			e.printStackTrace();
		}
		System.out.println("Twitter Index Handler Created.");
	}


	/**
	 * Make query to index, using Vector Space Similarity
	 *
	 * @param queryString String query
	 * @return String. The Document best matching the query
	 */
	public String makeVSQueryString(String queryString) {

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex :: TextIndexHandler.makeVSQueryString()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
		searcher.setSimilarity(similarityVS);

		Query query = createQuery(queryString);

		TopDocs candidate;
		try {
			candidate = searcher.search(query, NO_OF_RESULTS_QUERY);
			//candidate = searcher.search(query, reader.maxDoc());
		} catch (IOException e) {
			System.out.println("WebKIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			//e.printStackTrace();
			return null;
		}

		//Debugging. when NO_OF_RESULTS_QUERY is more than one, class is in debug mode
		if (NO_OF_RESULTS_QUERY>1) {
			try {
				printAllResults(candidate);
			} catch (IOException e) {
				System.out.println("WebKIndex :: TextIndexHandler.makeQueryString() Could NOT print QueryObject Results");
				//e.printStackTrace();
			}
		}
		//Get the Higher Score document and return content in string
		String response="";
		if (candidate.scoreDocs.length>0){
			try {
				response=searcher.doc(candidate.scoreDocs[0].doc).get(IndexCONSTANTS.FIELD_CONTENT);
			} catch (IOException e) {
				System.out.println("WebKIndex :: TextIndexHandler.makeVSQueryString() Could NOT Get top document for query: " + queryString);
				//e.printStackTrace();
			}
		}

		return response;
	}

	/**
	 * Convert Sting to Lucene.Search.Qeury
	 *
	 * @param queryString String.
	 * @return QueryObject.
	 */
	private Query createQuery(String queryString) {
		QueryParser parser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT,analyzer);
		Query query = null;
		try {
			query = parser.parse(queryString);
		} catch (ParseException e) {
			System.out.println("WebKIndex :: TextIndexHandler.createQuery() Could NOT parse the queryString.");
			return null;
			//e.printStackTrace();
		}
		return query;

	}

	/**
	 *	Function used for debuging. Print all candidates results from query
	 *
	 * @param candidate TopDoc.
	 */
	private void printAllResults(TopDocs candidate) throws IOException {
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);

		} catch (IOException e) {
			System.out.println("WebKIndex :: TextIndexHandler.printAllResults() Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		float maxScore = candidate.getMaxScore();
		ScoreDoc[] hits = candidate.scoreDocs;

		System.out.println("Found " + hits.length + " hits.");
		System.out.println("MaxScore:" + maxScore);

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			float score = hits[i].score;

			System.out.println("---------------------------------");
			System.out.println((i + 1));
			System.out.println("url: "+d.get(IndexCONSTANTS.FIELD_URL));
			System.out.println("content: "+d.get(IndexCONSTANTS.FIELD_CONTENT));
			System.out.println("---------------------------------");
		}
	}


}
