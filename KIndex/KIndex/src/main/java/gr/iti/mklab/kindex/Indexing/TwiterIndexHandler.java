package gr.iti.mklab.kindex.Indexing;

import gr.iti.mklab.kindex.ConceptExtraction.DBpediaSpotlight.DBpediaSpotlightHandler;
import gr.iti.mklab.simmo.core.documents.Post;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
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
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Class to handle Index for Twitter Posts in KIndex
 */
public class TwiterIndexHandler {

	Analyzer analyzer;
	Directory directory;
	IndexWriterConfig config;
	IndexWriter writer;
	DirectoryReader reader;
	/*
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
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			System.out.println("KIndex :: TwiterIndexHandler()  Could NOT create writer");
			e.printStackTrace();
		}
		System.out.println("Twitter Index Handler Created.");
	}

	public boolean store(List<Post> posts){

		//TODO check for tweet's attributes if stored correctly
		for (Post post : posts) {
			try {
				writer.addDocument(castToDoc(post));
				writer.commit();
				System.out.println("Twitter Post Indexed: " + post.getUrl());
			} catch (IOException e) {
				System.out.println("KIndex :: TwiterIndexHandler.store()  Writer could NOT index document: "+post.toString());
				e.printStackTrace();
			}
		}
		return true;
	}

	private Document castToDoc(Post post) {
		Document doc = new Document();
		doc.add(new TextField(IndexCONSTANTS.FIELD_MEDIUM, IndexCONSTANTS.MEDIUM_TWEET, Field.Store.YES));
		doc.add(new StringField(IndexCONSTANTS.FIELD_URL,post.getUrl(), Field.Store.YES));
		doc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT,post.getTitle(), Field.Store.YES));
		doc.add(new TextField(IndexCONSTANTS.FIELD_TYPE, IndexCONSTANTS.TYPE_TEXT, Field.Store.YES));
		/*doc.add(new TextField(IndexCONSTANTS.FIELD_TWEET_ID,post.getId(), Field.Store.YES));*/
		return doc;
	}

	public void closeWriter() {
		try {
			writer.close();
			writer = null;
		} catch (IOException e) {
			System.out.println("KIndex :: TextIndexHandler.closeWriter()  Could NOT close writer");
			e.printStackTrace();
		}
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
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString()  Reader could NOT open directory");
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
			System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Search for query: " + queryString);
			//e.printStackTrace();
			return null;
		}

		//Debugging. when NO_OF_RESULTS_QUERY is more than one, class is in debug mode
		if (NO_OF_RESULTS_QUERY>1) {
			try {
				printAllResults(candidate);
			} catch (IOException e) {
				System.out.println("KIndex :: TextIndexHandler.makeQueryString() Could NOT print Query Results");
				//e.printStackTrace();
			}
		}
		//Get the Higher Score document and return content in string
		String response="";
		if (candidate.scoreDocs.length>0){
			try {
				response=searcher.doc(candidate.scoreDocs[0].doc).get(IndexCONSTANTS.FIELD_CONTENT);
			} catch (IOException e) {
				System.out.println("KIndex :: TextIndexHandler.makeVSQueryString() Could NOT Get top document for query: " + queryString);
				//e.printStackTrace();
			}
		}

		return response;
	}

	/**
	 * Convert Sting to Lucene.Search.Qeury
	 *
	 * @param queryString String.
	 * @return Query.
	 */
	private Query createQuery(String queryString) {
		QueryParser parser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT,analyzer);
		Query query = null;
		try {
			query = parser.parse(queryString);
		} catch (ParseException e) {
			System.out.println("KIndex :: TextIndexHandler.createQuery() Could NOT parse the queryString.");
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
			System.out.println("KIndex :: TextIndexHandler.printAllResults() Reader could NOT open directory");
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

	/**
	 * insert concepts to index
	 * not safe, due to the complexity of the update operations in lucene, it has to be checked
	 * NOT CONTINUED, until we eventually index tweets with lucene
	 */
	public void updateIndexWithConcepts(){
		Bits liveDocs = MultiFields.getLiveDocs(reader);
		for (int i=0; i<reader.maxDoc(); i++) {

            System.out.println("Updating document: " + i);

            if (liveDocs != null && !liveDocs.get(i))
				continue;

			Document doc = null;
			try {
				doc = reader.document(i);
                System.out.println(doc);
            } catch (IOException e) {
                System.out.println("Error while reading document " + i);
                continue;
            }

            if(doc.get(IndexCONSTANTS.FIELD_CONCEPTS) != null){
                System.out.println("Document " + i + " already contains concepts...");
                continue;
            }

			// rebuild document, as info about tokenization are not returned on retrieval
			Document newDoc = new Document();
			String content = doc.get(IndexCONSTANTS.FIELD_CONTENT);
			String url = doc.get(IndexCONSTANTS.FIELD_URL);
			newDoc.add(new TextField(IndexCONSTANTS.FIELD_MEDIUM, IndexCONSTANTS.MEDIUM_TWEET, Field.Store.YES));
			newDoc.add(new StringField(IndexCONSTANTS.FIELD_URL, url, Field.Store.YES));
			newDoc.add(new TextField(IndexCONSTANTS.FIELD_CONTENT, content, Field.Store.YES));
			newDoc.add(new TextField(IndexCONSTANTS.FIELD_TYPE, IndexCONSTANTS.TYPE_TEXT, Field.Store.YES));

			// extract concepts
            String language = "de";
			DBpediaSpotlightHandler dbpedia = new DBpediaSpotlightHandler();
			Set<String> annotations = dbpedia.getAnnotations(content, language, "0.5");
            for (String annotation : annotations) {
                newDoc.add(new TextField(IndexCONSTANTS.FIELD_CONCEPTS, annotation, Field.Store.YES));
            }

            System.out.println(newDoc);
            System.out.println();

            try {
                writer.updateDocument(new Term(IndexCONSTANTS.FIELD_URL, url), newDoc);
                writer.commit();
            } catch (IOException e) {
                System.out.println("Error while updating document " + i);
            }
        }
	}

    public static void main(String[] args) {
        // for testing purposes
        TwiterIndexHandler tih = new TwiterIndexHandler();
        tih.updateIndexWithConcepts();
        tih.closeWriter();
    }


}
