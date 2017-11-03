package gr.iti.mklab.kindex;

import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import gr.iti.mklab.kindex.Indexing.IndexHandler;
import gr.iti.mklab.kindex.Indexing.IndexPassageHandler;
import gr.iti.mklab.kindex.KMongoDB.MongoDBHandler;
import gr.iti.mklab.kindex.KMongoDB.MongoSimmoContentConstants;
import gr.iti.mklab.kindex.KMongoDB.MorphiaConstants;
import gr.iti.mklab.simmo.core.morphia.MorphiaManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 *  Class uses gr.iti.mklab.kindex.Indexing.IndexPassageHandler and gr.iti.mklab.kindex.KMongoDB.MongoDBHandler for handling Index.
 *
 * Passage Indexer reads text from
 * {@value MongoSimmoContentConstants#DB_NAME}.{@value MongoSimmoContentConstants#TEXT_COLLECTION_NAME} <br>
 *
 */
public class PassageIndexer {

	private IndexPassageHandler pinxh;

	/**
	 * Constructor without parameter
	 */
	public PassageIndexer() {

		pinxh = new IndexPassageHandler();
		System.out.println("Passage Index Opened");
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
		System.out.println("DB: "+MongoSimmoContentConstants.DB_NAME);


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

				pinxh.open();
				boolean indOK = pinxh.indexJson(temjObj);
				pinxh.commitWriters();
				//boolean indOK = inxh.indexString(content);
				pinxh.close();

				if (indOK) {
					System.out.println(" ");
					System.out.print(i + "/" + numberOfResults + " -> Indexed: \"" + temjObj.getString("url") + "\",Medium: " + Medium );
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
	 * Makes query to passage index
	 *
	 * @param query String. Query to search in index
	 * @return String. Document Best matching query
	 */
	public void queryIndex( String query){
		//make query to check
		 pinxh.printResults( query);

		//System.out.println("`````````````````````````````````````````````" );
		//System.out.println("Response got : " + response);

	}
	public void close(){
		//Close index handler
		pinxh.close();
		System.out.println("Indexer terminated");
	}


}
