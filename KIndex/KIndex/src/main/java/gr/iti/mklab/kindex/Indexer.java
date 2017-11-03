package gr.iti.mklab.kindex;

import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import gr.iti.mklab.kindex.Indexing.IndexHandler;
import gr.iti.mklab.kindex.Indexing.TwiterIndexHandler;
import gr.iti.mklab.kindex.KMongoDB.MongoDBHandler;
import gr.iti.mklab.kindex.KMongoDB.MorphiaConstants;
import gr.iti.mklab.kindex.KMongoDB.MongoSimmoContentConstants;
import gr.iti.mklab.simmo.core.morphia.MorphiaManager;
import gr.iti.mklab.sm.Shutdown;
import gr.iti.mklab.sm.StreamsManager;
import gr.iti.mklab.sm.streams.StreamException;
import gr.iti.mklab.sm.streams.StreamsManagerConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Class uses gr.iti.mklab.kindex.Indexing.IndexHandler and gr.iti.mklab.kindex.KMongoDB.MongoDBHandler for handling Index.
 *
 * Indexer reads text from
 * {@value MongoSimmoContentConstants#DB_NAME}.{@value MongoSimmoContentConstants#TEXT_COLLECTION_NAME} <br>
 * and Index them in default or provided directory
 *
 */
public class Indexer {

	private IndexHandler inxh;

	/**
	 * Constructor without parameter, set Index path the default value
	 */
    public Indexer() {

		inxh = new IndexHandler();
		System.out.println(inxh.getFilename()+" Index Opened");
	}

	/**
	 * Constructor with String parameter, handles Index in provided file (fileName can be absolute/relative path).<br>
	 *
	 * @param fileName String. Absolute/Relative path for storing index
	 */
	public Indexer(String fileName) {
		inxh = new IndexHandler(fileName);
		System.out.println(fileName+" Index Opened");
	}

	public boolean isEmpty(){
		return inxh.isEmpty();
	}

	/**
     * Read the recordings of provided medium, in Simmo DB. Get all texts, <br>
     * and create index them
     *
     * @return Nothing
     */
    public void  index(String Medium){

		Date start_time=new Date();
		System.out.println("Indexing started at: "+start_time.toString());


        //initialization for Simmo reading DB
        MorphiaManager.setup(MorphiaConstants.HOST, MorphiaConstants.PORT);
		MongoDBHandler mDBh= new MongoDBHandler();
		long numberOfResults = mDBh.trgReadAll(Medium);

		int i=1;

        while(mDBh.trgIterHasNext()) {

            String json = mDBh.trgIterNext();
            JSONArray jsonObjs = new JSONArray(json);
			for (Object jsonObjO : jsonObjs) {

				/*if(txtType.equals("HTML")){
					//extract text from html
					extracted_content = extractHTML(content, id);
				}*/

				JSONObject jsonObjFromMongo = new JSONObject(jsonObjO.toString());
				JSONObject temjObj = new JSONObject();
				temjObj.put(IndexCONSTANTS.FIELD_MEDIUM, Medium);
				temjObj.put(IndexCONSTANTS.FIELD_URL, jsonObjFromMongo.getString("url"));
				temjObj.put(IndexCONSTANTS.FIELD_CONTENT, jsonObjFromMongo.getString("content"));
				temjObj.put(IndexCONSTANTS.FIELD_TYPE, jsonObjFromMongo.getString("type")); // ex. TEXT
				temjObj.put(IndexCONSTANTS.FIELD_TEXT_ID, jsonObjFromMongo.getString("text_id"));
				if (Medium.equals(IndexCONSTANTS.MEDIUM_WEBSITE)){
					temjObj.put(IndexCONSTANTS.FIELD_WEBPAGE_ID, jsonObjFromMongo.getString("id"));
				}
				else if (Medium.equals(IndexCONSTANTS.MEDIUM_TWEET)){
					temjObj.put(IndexCONSTANTS.FIELD_TWEET_ID, jsonObjFromMongo.getString("id"));
				}
				else if (Medium.equals(IndexCONSTANTS.MEDIUM_PDF)){
					temjObj.put(IndexCONSTANTS.FIELD_PDF_ID, jsonObjFromMongo.getString("id"));
				}
				else if (Medium.equals(IndexCONSTANTS.MEDIUM_POST)){
					temjObj.put(IndexCONSTANTS.FIELD_POST_ID, jsonObjFromMongo.getString("id"));
				}

				inxh.openWriter();
				boolean indOK = inxh.indexJson(temjObj);
				//boolean indOK = inxh.indexString(content);
				inxh.closeWriter();

				if (indOK) {
					System.out.println(" ");
					System.out.print(i + "/" + numberOfResults + " -> Indexing: \"" + temjObj.getString("url") + "\",Medium: " + Medium );
					if (Medium.equals(IndexCONSTANTS.MEDIUM_WEBSITE)){
						System.out.print(", " + IndexCONSTANTS.FIELD_WEBPAGE_ID + ": \""+ temjObj.getString(IndexCONSTANTS.FIELD_WEBPAGE_ID));
					}
					else if (Medium.equals(IndexCONSTANTS.MEDIUM_POST)){
						System.out.print(", " + IndexCONSTANTS.FIELD_POST_ID + ": \""+ temjObj.getString(IndexCONSTANTS.FIELD_POST_ID));
					}
					else if (Medium.equals(IndexCONSTANTS.MEDIUM_TWEET)){
						System.out.print(", " + IndexCONSTANTS.FIELD_TWEET_ID+ ": \""+ temjObj.getString(IndexCONSTANTS.FIELD_TWEET_ID));
					}
					else if (Medium.equals(IndexCONSTANTS.MEDIUM_PDF)){
						System.out.print(", " + IndexCONSTANTS.FIELD_PDF_ID + ": \""+ temjObj.getString(IndexCONSTANTS.FIELD_PDF_ID));
					}
					System.out.println("\", " + IndexCONSTANTS.FIELD_TEXT_ID + ": \""+temjObj.getString(IndexCONSTANTS.FIELD_TEXT_ID)+"\"");
				}
				else{
					System.out.println(i + "/" + numberOfResults + " -> NOT INDEXED: \"" + temjObj.getString("url") + "\",Medium: " + Medium );
				}

				i++;
			}

        }
		System.out.println(" ");
		System.out.println("Indexed " + (i-1) + " Documents");
		System.out.println("Indexing started at: " + start_time.toString());
		System.out.println("Indexing ended at: " + (new Date()).toString());

    }



	/**
	 * Makes query to index
	 *
	 * @param query String. Query to search in index
	 * @return String. Document Best matching query
	 */
	public String queryIndex(String medium, String query){
		//make query to check
		String response = inxh.makeVSQueryString(medium, query);

		//System.out.println("`````````````````````````````````````````````" );
		//System.out.println("Response got : " + response);

		return response;
	}

	public String queryIndex(String query){
		//make query to check
		String response = inxh.makeVSQueryString("general", query);

		//System.out.println("`````````````````````````````````````````````" );
		//System.out.println("Response got : " + response);

		return response;
	}


	public void deleteIndex() {

		inxh.openWriter();
		boolean delOK = inxh.deleteAll();
		inxh.closeWriter();
		if (delOK){
			System.out.println("All Documents deleted from " + inxh.getFilename() + " Index ");
		}
		else{
			System.out.println("Could not delete " + inxh.getFilename() + " Index Documents  ");
		}

		/*String fName = inxh.getFilename();
		inxh.close();
		inxh = null;

		//delete dir and create new IndexRAMHandler

		try {
			File f= new File(fName);
			//f.delete();
			FileDeleteStrategy.FORCE.delete(f);
			System.out.println(fName+" Index Deleted");
			inxh = new IndexHandler(fName);
			System.out.println(fName+" Index Created");
		} catch (IOException e) {
			System.err.println("KIndex :: Indexer.deleteIndex() Could NOT delete folder: " + fName);
			e.printStackTrace();
		}*/

	}

	public void close(){
		//Close index handler
		inxh.close();
		System.out.println("Indexer terminated");
	}

	/**
	 * Makes query to Twiter index
	 *
	 * @param query String. Query to search in index
	 * @return String. Document Best matching query
	 */
	final static public String queryTwiterIndex(String query){
		//make query to check
		TwiterIndexHandler tih = new TwiterIndexHandler();
		String response = tih.makeVSQueryString(query);
		tih=null;
		return response;
	}


	final static public void startTwiterStream(String language){
		Logger logger = Logger.getLogger(StreamsManager.class);

		String filename;
		if(language.equals("tr"))
			filename = "./conf/streams.conf.tr.xml";
		else if(language.equals("es"))
			filename = "./conf/streams.conf.es.xml";
		else if(language.equals("de"))
			filename = "./conf/streams.conf.de.xml";
		else
			filename = "./conf/streams.conf.xml";

		File streamConfigFile;
		streamConfigFile = new File(filename);


		StreamsManager manager = null;
		try {
			StreamsManagerConfiguration config = StreamsManagerConfiguration.readFromFile(streamConfigFile);

			//tweets are indexed directly during simmo stream manager is reading them
			//for this purpose gr.iti.mklab.kindex is added to gr.iti.mklab/sm/retrievers/impl/TwiterRetriever l:~250

			manager = new StreamsManager(config);
			manager.open();

			Runtime.getRuntime().addShutdownHook(new Shutdown(manager));

			Thread thread = new Thread(manager);
			thread.start();


		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (StreamException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}


	/**
     * Get as parameter a string with HTML gr.iti.mklab.kindex, remove all tags and return plain text
     *
     * @param content HTML text
     * @return String Plain Text of conent
     */
    private String extractHTML(String content, String id) {
        try {
            return ArticleExtractor.INSTANCE.getText(content);
        } catch (BoilerpipeProcessingException e) {
            System.err.println("KIndex :: Indexer.extractHTML() Could not boilerpipe content for id: " + id);
            return content;
            //e.printStackTrace();
        }
    }

	/**
	 * Index files from folder D:\KBFeed\input
	 */
	public void indexKB() {

		//read files from dir
		File folder = new File("D:\\KBFeed\\input");
		File[] listOfFiles = folder.listFiles();

		//for each txt file
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(".txt")) {
				try {
					String content = FileUtils.readFileToString(file);

					inxh.openWriter();
					boolean indOK = inxh.indexKBText(content);
					inxh.closeWriter();

					if (indOK){
						System.out.println("Text indexed Successfully: " + file.getName());
					}
					else{
						System.out.println("Text indexed FAILED: " + file.getName());
					}

				} catch (IOException e) {
					System.err.println("KIndex :: Indexer.indexKB() Could not read content form file " + file.getName());
					e.printStackTrace();
				}


			}
		}


	}

	/**
	 * query KB Index
	 * @param query
	 * @return
	 */
	public String queryKBIndex(String query) {

		//make query to check
		String response = inxh.makeKBQuery( query);

		return response;
	}
}
































