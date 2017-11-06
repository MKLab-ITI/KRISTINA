package Indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.coyote.http11.Constants.a;


public class IndexHandler {

	Analyzer analyzer;
	Directory directory;
	IndexWriterConfig config;
	IndexWriter writer;
	String fileName;
	int NO_OF_RESULTS_QUERY = 1;
	//private final static String DEFAULT_PATH = ("D:\\INDEX");


	/**
	 * Constructor with String parameter, creates File index in fileName directory (fileName can be absolute/relative path).<br>
	 *
	 * @param fileNameToRead String. Absolute/Relative path for storing index
	 */
	public IndexHandler(String fileNameToRead)  {

		analyzer = new ClassicAnalyzer();
		Path path= Paths.get(fileNameToRead);
		fileName=fileNameToRead;
		try {
			directory = FSDirectory.open(path);
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
			writer.close();
			writer = null;
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler()  Could NOT create writer");
			//e.printStackTrace();
		}

	}
	/**
	 * Constructor , creates File index in default directoryy ({@link IndexCONSTANTS#INDEX_PATH}).<br>
	 *
	 */
	public IndexHandler(){

		analyzer = new ClassicAnalyzer();
		Path path= Paths.get(IndexCONSTANTS.INDEX_PATH);
		try {
			directory = FSDirectory.open(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileName=IndexCONSTANTS.INDEX_PATH;
		try {
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
			writer.close();
			writer = null;
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler()  Could NOT create writer");
			//e.printStackTrace();
		}

	}

	/**
	 * Close TextIndexHandler
	 *
	 * @return Nothing
	 */
	public void close(){
		try {
			directory.close();
			analyzer.close();

		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.close() Could NOT close writer.");
			e.printStackTrace();
		}
	}

	/**
	 * If Index handling by TextIndexHandler is empty
	 *
	 * @return boolean. True if Empty.
	 */
	public boolean isEmpty(){
		try {
			DirectoryReader reader = DirectoryReader.open(directory);
			if (reader.getDocCount(IndexCONSTANTS.FIELD_CONTENT) > 0){
				reader.close();
				return false;
			}
			reader.close();
			
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.isEmpty() Could NOT get Doc Count.");
			e.printStackTrace();
			return true;
		}
		return true;
	}

	/**
	 * @return String. FileName of Index
	 */
	public String getFilename(){
		return fileName;
	}

	/**
	 * Add content to index.
	 * Keep in mind that before using this function, you have to openWriter()
	 * And closeWriter() when finish
	 *
	 * @param content String. Text too index
	 * @return boolean. True if content indexed successfully. False if not.
	 */
	public boolean indexString(String content){
		return addDoc(createDoc(content));
	}

	/**
	 * Private function.
	 * Add Doc to index
	 *
	 * @param doc Document to index
	 * @return boolean. True if doc index successfully. False if not.
	 */
	private boolean addDoc(Document doc) {
		try {
			writer.addDocument(doc);
			writer.commit();
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.addDoc() Could NOt add Document to writer.");
			System.out.println("Document: " + doc.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Private function.
	 * Convert String to Lucene.Document
	 *
	 * @param content String to convert
	 * @return Lucene.Document.
	 */
	private Document createDoc(String content){
		Document doc = new Document();
		doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, content, Field.Store.YES));
		return doc;
	}


	public void indexNewspaperJson(JSONObject jsonObj){

		BooleanQuery.Builder finalQuery = new BooleanQuery.Builder(); //query to check if doc already exists
		try {
			String value = jsonObj.getString(IndexCONSTANTS.FIELD_URL).replaceAll("(?=[]\\[+&|!(){}^\"~*?:/\\\\-])", "\\\\");
			finalQuery.add(((new QueryParser(IndexCONSTANTS.FIELD_URL, analyzer)).parse("\"" + value + "\"")), BooleanClause.Occur.MUST); //double quotes because we need want exact matching
		} catch (ParseException e) {
			System.out.println("WebKIndex.IndexHandler.indexNewspaperJson() Could Not create QueryParser.");
			System.out.println("url: \"" + jsonObj.getString("url") );
			e.printStackTrace();
		}

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex.IndexHandler.indexNewspaperJson()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results = null;
		try {
			results = searcher.search(finalQuery.build(), 1);
		} catch (IOException e) {
			System.out.println("WebKIndex.IndexHandler.indexNewspaperJson() Could Not search in index to find out if doc exists.");
			e.printStackTrace();
		}

		if (results.totalHits == 0) { //if doc created above does not exist in index
			try {

				Document doc = new Document(); //doc to add in index
				Iterator<?> keys = jsonObj.keys();
				while (keys.hasNext()) {
					String key = keys.next().toString();
					String value = jsonObj.getString(key);
					if(key.equals(IndexCONSTANTS.FIELD_DATE_STRING)){
						doc.add(new StringField(key, value, Field.Store.YES));
//						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, ");
//						Date date = null;
//						try {
//							date = dateFormat.parse("23/09/2007");
//							long time = date.getTime();
//						} catch (java.text.ParseException e) {
//							e.printStackTrace();
//						}
					}
					else{
						doc.add(new TextField(key, value, Field.Store.YES));
					}
				}
				writer.addDocument(doc);
				writer.commit();
				System.out.println("Indexing: " + jsonObj.getString(IndexCONSTANTS.FIELD_URL));
			} catch (IOException e) {
				System.out.println("WebKIndex.IndexHandler.indexNewspaperJson() Could Not add Document to writer.");
				e.printStackTrace();
			}
		}
		else{
			/*try {
				printAllResults(results);
			} catch (IOException e) {
				System.out.println("IndexHandler.indexNewspaperJson() Could Not Print all Documents.");
				e.printStackTrace();
			}*/
			System.out.println("Already Indexed: " + jsonObj.getString(IndexCONSTANTS.FIELD_URL));
		}
	}

	public boolean indexJson(JSONObject jsonObj) {

		Document doc = new Document(); //doc to add in index
		BooleanQuery.Builder finalQuery = new BooleanQuery.Builder(); //query to check if doc already exists

		Iterator<?> keys = jsonObj.keys();

		while (keys.hasNext()) {
			String key = keys.next().toString();
			String value =  jsonObj.getString(key);
			doc.add(new TextField(key, value, Field.Store.YES));
			try {
				//add key->value to a query to check if record already exists
				//don't add content field to query. It's value may big larger than character max and trigger exceptions
				if (!key.equals(IndexCONSTANTS.FIELD_CONTENT)) {
					value = value.replaceAll("(?=[]\\[+&|!(){}^\"~*?:/\\\\-])", "\\\\");
					finalQuery.add(((new QueryParser(key, analyzer)).parse(value)), BooleanClause.Occur.MUST);
				}
			} catch (ParseException e) {
				System.out.println("WebKIndex :: IndexHandler.indexJson() Could Not create QueryParser.");
				System.out.println("Key: \"" + key + "\", value: \"" + value + "\"");
				e.printStackTrace();
				return false;
			}
		}

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.indexJson()  Reader could NOT open directory");
			e.printStackTrace();
			return false;
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			results = searcher.search(finalQuery.build(), 1);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.indexJson() Could Not search in index to find out if doc exists.");
			e.printStackTrace();
			return false;
		}

		if (results.totalHits == 0) { //if doc created above does not exist in index
			try {
				writer.addDocument(doc);
				writer.commit();
			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexHandler.indexJson() Could Not add Document to writer.");
				System.out.println("Document: " + doc.toString());
				e.printStackTrace();
				return false;
			}
		}
		else{
			System.out.println("Doc \"" + doc.get("url") + "\" already exists in index");
			return false;
		}
		return true;
	}

	/**
	 * Delete all Indexed Documents
	 * Keep in mind that before using this function, you have to openWriter()
	 * And closeWriter() when finish
	 * @return boolean. True if All documents deleted
	 */
	public boolean deleteAll(){
		try {
			writer.deleteAll();
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.deleteAll() Could Not delete All Documents.");
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * Make query to index, using Vector Space Similarity
	 *
	 * @param queryString String query
	 * @return String. The Document best matching the query
	 */
	public String makeVSQueryString(String medium, String queryString, String ne, String concept) {


		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.makeVSQueryString()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
		searcher.setSimilarity(similarityVS);

		TopDocs candidate = null;

		Query query = createQuery(medium, queryString, ne, concept);

		try {
			candidate = searcher.search(query, NO_OF_RESULTS_QUERY);
			//candidate = searcher.search(query, reader.maxDoc());
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			//e.printStackTrace();
			return null;
		}


		//Debugging. when NO_OF_RESULTS_QUERY is more than one, class is in debug mode
		if (NO_OF_RESULTS_QUERY>1) {
			try {
				printAllResults(candidate);
			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexHandler.makeVSQueryString() Could NOT print QueryObject Results");
				//e.printStackTrace();
			}
		}
		//Get the Higher Score document and return content in string
		String response="";
		if (candidate.scoreDocs.length>0){
			try {
				//response=searcher.doc(candidate.scoreDocs[0].doc).get(IndexCONSTANTS.FIELD_CONTENT);
				response=docToJSON(searcher.doc(candidate.scoreDocs[0].doc));
			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexHandler.makeVSQueryString() Could NOT Get top document for query: " + queryString);
				//e.printStackTrace();
			}
		}

		return response;
	}

	public String makeNewspaperQueryByTitle(String title){

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.makeNewspaperQueryByTitle()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
		searcher.setSimilarity(similarityVS);

		TopDocs candidate = null;

		BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
		try {

			QueryParser parser = new QueryParser(IndexCONSTANTS.FIELD_TITLE, analyzer);
			Query query = parser.parse(QueryParser.escape(title));
			finalQuery.add(query, BooleanClause.Occur.MUST); // MUST implies that the keyword must occur.
		} catch (ParseException e) {
			System.out.println("WebKIndex :: IndexHandler.makeNewspaperQueryByTitle() Could NOT parse the queryString.");
			e.printStackTrace();
			return null;
		}
		Query query1 = finalQuery.build();
		try {
			candidate = searcher.search(query1, NO_OF_RESULTS_QUERY);
			//candidate = searcher.search(query, reader.maxDoc());
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.makeNewspaperQueryByTitle() Could NOT Search Newspaper for title: " + title);
			//e.printStackTrace();
			return null;
		}
		String response="";
		if (candidate.scoreDocs.length>0){
			try {
				//response=searcher.doc(candidate.scoreDocs[0].doc).get(IndexCONSTANTS.FIELD_CONTENT);

//				response=docToJSON(searcher.doc(candidate.scoreDocs[0].doc));
				response = docToJSONContentOnly(searcher.doc(candidate.scoreDocs[0].doc));
			} catch (IOException e) {
				System.out.println("WebKIndex :: IndexHandler.makeNewspaperQueryByTitle() Could NOT Get top document for query: " + title);
				//e.printStackTrace();
			}
		}

		return response;

	}

	/**
	 * get today or yesterday posts
	 * @return
	 */
	public String getLatestNewspaperArticles(int maxResults){

		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		String todayStr = dateFormat.format(today);

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.makeNewspaperQueryByTitle()  Reader could NOT open directory");
			e.printStackTrace();
		}

		IndexSearcher searcher = new IndexSearcher(reader);
		List<String> newArticles = dateQuery(searcher, todayStr, maxResults);
		if(newArticles.size() == 0){
            System.out.println("No today's articles available...");
            System.out.println("Retrieving articles from yesterday...");
            cal.add(Calendar.DAY_OF_YEAR,-1);
			Date yesterday = cal.getTime();
			String yesterdayStr = dateFormat.format(yesterday);
			newArticles = dateQuery(searcher, yesterdayStr, maxResults);
		}

		JSONArray resultsJsonArray = new JSONArray(newArticles);
		String response = resultsJsonArray.toString();

		return response;
	}

	private List<String> dateQuery(IndexSearcher searcher, String dateStr, int maxResults){
		List<String> results = new ArrayList<>();
		TopDocs candidates = null;
		Query query = new TermQuery(new Term(IndexCONSTANTS.FIELD_DATE_STRING, dateStr));
		try {
			candidates = searcher.search(query, maxResults);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.dateQuery() Could NOT Search Newspaper for date: " + dateStr);
			//e.printStackTrace();
			return null;
		}
		if (candidates.scoreDocs.length>0){
			ScoreDoc[] hits = candidates.scoreDocs;
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = null;
				try {
					d = searcher.doc(docId);
				} catch (IOException e) {
					e.printStackTrace();
				}
				String url = d.get(IndexCONSTANTS.FIELD_URL);
				results.add(url);
			}
		}
		return results;
	}

	public List<String> getAllNewspaperUrls(){
		List<String> urls = new ArrayList<>();
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);for (int i=0; i<reader.maxDoc(); i++) {
			Document doc = reader.document(i);
			String url = doc.get("url");
			urls.add(url);
			}
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.getAllNewspaperUrls()  Reader could NOT open directory");
			e.printStackTrace();
		}
		return urls;
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
			if (temp.name().equals(IndexCONSTANTS.FIELD_CONCEPTS)){
				String[] s = temp.stringValue().split(": ");
				concepts.put(new JSONObject().put(s[0],s[1]));
			}
			else if (temp.name().equals(IndexCONSTANTS.FIELD_NAMED_ENTITIES)){
				String[] s = temp.stringValue().split(": ");
				nes.put(new JSONObject().put(s[0],s[1]));
			}
			else {
				obj.put(temp.name(), temp.stringValue());
			}
		}

		if (concepts.length()>0){
			obj.put(IndexCONSTANTS.FIELD_CONCEPTS, concepts);
		}
		if (nes.length()>0){
			obj.put(IndexCONSTANTS.FIELD_NAMED_ENTITIES, nes);
		}

		if(!obj.getString(IndexCONSTANTS.FIELD_MEDIUM).equals(IndexCONSTANTS.MEDIUM_NEWSPAPER)){
			obj.put("passage", obj.getString(IndexCONSTANTS.FIELD_CONTENT).substring(0, 200));
		}
		return obj.toString();
	}

	private String docToJSONContentOnly(Document doc) {
		String content = "";
		JSONObject obj = new JSONObject();
		Iterator<IndexableField>  iter = doc.iterator();
		while (iter.hasNext()) {
			IndexableField temp = iter.next();
			if (temp.name().equals(IndexCONSTANTS.FIELD_CONTENT)){
				content = temp.stringValue();
			}
		}
		return content;
	}

	/**
	 * Convert Sting to Lucene.Search.Qeury
	 *
	 * @param queryString String.
	 * @return QueryObject.
	 */
	private Query createQuery(String medium, String queryString, String ne, String concept) {
		try {
			BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();

			if (!queryString.equals("")) {
				QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
				Query query1 = parser1.parse(queryString);
				finalQuery.add(query1, BooleanClause.Occur.MUST); // MUST implies that the keyword must occur.
			}
			if(!medium.equals("general")) {
				QueryParser parser2 = new QueryParser(IndexCONSTANTS.FIELD_MEDIUM, analyzer);
				Query query2 = parser2.parse(medium);
				finalQuery.add(query2, BooleanClause.Occur.MUST);
			}

			if(!ne.equals("")) {
				String[] values = ne.split(" ");
				for (String value : values) {
					QueryParser parser2 = new QueryParser(IndexCONSTANTS.FIELD_NAMED_ENTITIES, analyzer);
					Query query2 = parser2.parse(value);
					finalQuery.add(query2, BooleanClause.Occur.MUST);
				}
			}

			if(!concept.equals("")) {
				String[] values = concept.split(" ");
				for (String value : values) {
					QueryParser parser2 = new QueryParser(IndexCONSTANTS.FIELD_CONCEPTS, analyzer);
					Query query2 = parser2.parse(value);
					finalQuery.add(query2, BooleanClause.Occur.MUST);
				}
			}

			return finalQuery.build() ;

		} catch (ParseException e) {
			System.out.println("WebKIndex :: IndexHandler.createQuery() Could NOT parse the queryString.");
			e.printStackTrace();
			return null;
		}
	}


	private Query createQuery(String queryString){
		try {
			QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
			Query query1 = parser1.parse(queryString);
			return query1;
		} catch (ParseException e) {
			System.out.println("WebKIndex :: IndexHandler.createQuery() Could NOT parse the queryString.");
			return null;
			//e.printStackTrace();
		}
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
			System.out.println("WebKIndex :: IndexHandler.printAllResults() Reader could NOT open directory");
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

			System.out.println((i + 1)
					+ ". Score: " + score
					+ " " + d.get(IndexCONSTANTS.FIELD_CONTENT) + "\t"
			);
		}
	}

	/**
	 * Open Index writer
	 */
	public void openWriter() {
		try {
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.openWriter()  Could NOT create writer");
			e.printStackTrace();
		}
	}

	/**
	 * Close Index Writer
	 */
	public void closeWriter() {
		try {
			writer.close();
			writer = null;
		} catch (IOException e) {
			System.out.println("WebKIndex :: IndexHandler.closeWriter()  Could NOT close writer");
			e.printStackTrace();
		}
	}


}



























