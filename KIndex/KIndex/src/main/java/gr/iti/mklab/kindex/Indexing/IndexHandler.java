package gr.iti.mklab.kindex.Indexing;



import gr.iti.mklab.kindex.CoreNLP.CoreNLPHandler;
import gr.iti.mklab.kindex.MetaMap.LocalMetaMapHandler;
import gr.iti.mklab.kindex.MetaMap.MetaMapHandler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * 	Class for handling Index
 */
public class IndexHandler {

	Analyzer analyzer;
	Directory directory;
	IndexWriterConfig config;
	IndexWriter writer;
	String fileName;
	int NO_OF_RESULTS_QUERY = 1;
	private final static String DEFAULT_PATH = IndexCONSTANTS.INDEX_PATH;


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
			System.out.println("KIndex :: TextIndexHandler()  Could NOT create writer");
			e.printStackTrace();
		}

	}
	/**
	 * Constructor , creates File index in default directoryy ({@value DEFAULT_PATH}).<br>
	 *
	 */
	public IndexHandler() {

		analyzer = new ClassicAnalyzer();
		Path path= Paths.get(DEFAULT_PATH);
		fileName=DEFAULT_PATH;
		try {
			directory = FSDirectory.open(path);
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
			writer.close();
			writer = null;
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler()  Could NOT create writer");
			e.printStackTrace();
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
			System.out.println("KIndex :: TextIndexHandler.close() Could NOT close writer.");
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
			System.out.println("KIndex :: TextIndexHandler.isEmpty() Could NOT get Doc Count.");
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
			System.out.println("KIndex :: IndexHandler.addDoc() Could NOt add Document to writer.");
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
				System.out.println("KIndex :: TextIndexHandler.addDoc() Could Not create QueryParser.");
				System.out.println("Key: \"" + key + "\", value: \"" + value + "\"");
				e.printStackTrace();
				return false;
			}
		}

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.indexJson()  Reader could NOT open directory");
			e.printStackTrace();
			return false;
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			results = searcher.search(finalQuery.build(), 1);
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.indexJson() Could Not search in index to find out if doc exists.");
			e.printStackTrace();
			return false;
		}

		if (results.totalHits == 0) { //if doc created above does not exist in index
			try {

				//extract concepts and Named Entities (Only when doc is going to be indexed)
				//doc.add(new TextField(IndexCONSTANTS.FIELD_CONCEPTS, MetaMapHandler.getAllConceptsInString(doc.get(IndexCONSTANTS.FIELD_CONTENT)), Field.Store.YES));
				//doc.add(new TextField(IndexCONSTANTS.FIELD_NAMED_ENTITIES, CoreNLPHandler.getNEasString(doc.get(IndexCONSTANTS.FIELD_CONTENT)), Field.Store.YES));

				JSONObject concepts = LocalMetaMapHandler.getConceptsasJson(doc.get(IndexCONSTANTS.FIELD_CONTENT));
				Iterator<?> concept = concepts.keys();
				while( concept.hasNext() ) {
					String c = (String) concept.next();
					if (concepts.get(c) instanceof JSONArray) {
						JSONArray array = (JSONArray) concepts.get(c);
						for (Object a : array) {
							doc.add(new TextField(IndexCONSTANTS.FIELD_CONCEPTS, c + ": "+ a, Field.Store.YES));
						}
					}
				}

				JSONObject nes = CoreNLPHandler.getNEasjson(doc.get(IndexCONSTANTS.FIELD_CONTENT));
				Iterator<?> ne = nes.keys();
				while( ne.hasNext() ) {
					String c = (String) ne.next();
					if (nes.get(c) instanceof JSONArray) {
						JSONArray array = (JSONArray) nes.get(c);
						for (Object a : array) {
							doc.add(new TextField(IndexCONSTANTS.FIELD_NAMED_ENTITIES, c + ": "+ a, Field.Store.YES));
						}
					}
				}

				writer.addDocument(doc);
				writer.commit();
			} catch (IOException e) {
				System.out.println("KIndex :: TextIndexHandler.addDoc() Could Not add Document to writer.");
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
	 * Index KB texts. Returns true if indexing completed successfully
	 *
	 * @param content
	 * @return
	 */
	public boolean indexKBText(String content) {

		Document doc = new Document(); //doc to add in index
		doc.add(new TextField("content", content, Field.Store.YES));
		try {
			writer.addDocument(doc);
			writer.commit();
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.indexKBText() Could Not add Document to writer.");
			System.out.println("Document: " + doc.toString());
			e.printStackTrace();
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
			System.out.println("KIndex :: TextIndexHandler.deleteAll() Could Not delete All Documents.");
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
	public String makeVSQueryString(String medium, String queryString) {

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString()  Reader could NOT open directory");
			e.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
		searcher.setSimilarity(similarityVS);

		TopDocs candidate = null;
		Query query = null;
		if (medium.equals("general")) {
			query = createQuery(queryString);
		}
		else{
			query = createQuery(medium, queryString);
		}

		try {
			candidate = searcher.search(query, NO_OF_RESULTS_QUERY);
			//candidate = searcher.search(query, reader.maxDoc());
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			//e.printStackTrace();
			return null;
		}
		//Debugging. when NO_OF_RESULTS_QUERY is more than one, class is in debug mode
		if (NO_OF_RESULTS_QUERY>1) {
			try {
				printAllResults(candidate);
			} catch (IOException e) {
				System.out.println("KIndex :: TextIndexHandler.makeQueryString() Could NOT print QRel Results");
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
				System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Get top document for query: " + queryString);
				//e.printStackTrace();
			}
		}

		return response;
	}



	/**
	 * Get a Document and return an json as String only with fieldName and fieldData of each field in Document
	 * @param doc
	 * @return
	 */
	private String docToJSON(Document doc) {

		JSONObject obj = new JSONObject();

		Iterator<IndexableField>  iter = doc.iterator();
		while (iter.hasNext()) {
			IndexableField temp = iter.next();
			obj.put(temp.name(),temp.stringValue());
		}

		return obj.toString();
	}

	/**
	 * Convert Sting to Lucene.Search.Qeury
	 *
	 * @param queryString String.
	 * @return QRel.
	 */
	private Query createQuery(String medium, String queryString) {
		try {
			QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
			Query query1 = parser1.parse(queryString);

			QueryParser parser2 = new QueryParser(IndexCONSTANTS.FIELD_MEDIUM, analyzer);
			Query query2 = parser2.parse(medium);

			BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
			finalQuery.add(query1, BooleanClause.Occur.MUST); // MUST implies that the keyword must occur.
			finalQuery.add(query2, BooleanClause.Occur.MUST);
			return finalQuery.build() ;

		} catch (ParseException e) {
			System.out.println("KIndex :: IndexHandler.createQuery() Could NOT parse the queryString.");
			return null;
			//e.printStackTrace();
		}
	}

	/**
	 * Create query for KB Index
	 * @param queryString
	 * @return
	 */
	private Query createKBQuery(String queryString) {
		try {
			QueryParser parser1 = new QueryParser("content", analyzer);
			Query query1 = parser1.parse(queryString);
			return query1;
		} catch (ParseException e) {
			System.out.println("KIndex :: IndexHandler.createKBQuery() Could NOT parse the queryString.");
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
			System.out.println("KIndex :: IndexHandler.createQuery() Could NOT parse the queryString.");
			e.printStackTrace();
			return null;
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
			System.out.println("KIndex :: IndexHandler.printAllResults() Reader could NOT open directory");
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
			System.out.println("KIndex :: IndexHandler.openWriter()  Could NOT create writer");
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
			System.out.println("KIndex :: IndexHandler.closeWriter()  Could NOT close writer");
			e.printStackTrace();
		}
	}


	/**
	 * Return all documents in KB index.
	 * For now, query is not in use
	 *
	 * @param query
	 * @return JSONArray, in String, including all KB Index Documents
	 */
	public String makeKBQuery(String query) {

		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.makeKBQuery()  Reader could NOT open directory");
			e.printStackTrace();
			return null;
		}

		JSONArray arr = new JSONArray();
		for (int i=0; i<reader.maxDoc(); i++) {

			Document doc = null;
			try {
				doc = reader.document(i);
			} catch (IOException e) {
				System.out.println("KIndex :: TextIndexHandler.makeKBQuery() Could NOT retrieve doc from reader");
				e.printStackTrace();
				return null;
			}

			String content = doc.get("content");
			JSONObject obj = new JSONObject();
			obj.put("text", content);
			arr.put(obj);
		}

		return arr.toString();

	}


}



























