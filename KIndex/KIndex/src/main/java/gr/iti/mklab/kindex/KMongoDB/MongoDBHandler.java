package gr.iti.mklab.kindex.KMongoDB;


import com.google.gson.Gson;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import gr.iti.mklab.kindex.Scrapping.ScrappingConstants;
import org.bson.Document;

import java.net.URL;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;


public class MongoDBHandler {

	private MongoClient mongoClient;
	private MongoCursor<Document> srcIter; 
	private MongoCursor<Document> trgTextIter;
	
	/**
	 * Constructor <br>
	 * creates Mongo Constats Instances <br>
	 * and Client
	 */
	public MongoDBHandler() {
		mongoClient = new MongoClient();
		srcIter = null;
		trgTextIter = null;
	}

	/**
	 * Close Client connection
	 * @return Nothing
	 */
	public void close(){
		mongoClient.close();
	}


	/**
	 * Read All Source urls . <br>
	 * Results can be Iterated with <b>srcIterHasNext()</b> & <b>srcIterNext()</b>
	 * 
	 * @return long. Number of results
	 */
	public long srcRead() {
		srcIter=null;
		MongoDatabase Mdb = mongoClient.getDatabase(MongoNutchConstants.DB_NAME);
		srcIter= Mdb.getCollection(MongoNutchConstants.COLLECTION_NAME).find().noCursorTimeout(true).iterator();
		return Mdb.getCollection(MongoNutchConstants.COLLECTION_NAME).count();
	}

	/**
	 * Read Source urls with crawlingId: crawlingId. <br>
	 * Results can be Iterated with <b>srcIterHasNext()</b> & <b>srcIterNext()</b>
	 *
	 * @param crawlingId String. CrawlingId to set as filter in query.
	 * @return long. Number of results
	 */
	public long srcRead(String crawlingId) {
		srcIter=null;
		MongoDatabase Mdb = mongoClient.getDatabase(MongoNutchConstants.DB_NAME);
		srcIter= Mdb.getCollection(MongoNutchConstants.COLLECTION_NAME).find(eq(MongoNutchConstants.CRAWLING_ID_FIELD, Integer.parseInt(crawlingId))).noCursorTimeout(true).iterator();
		return Mdb.getCollection(MongoNutchConstants.COLLECTION_NAME).count(eq(MongoNutchConstants.CRAWLING_ID_FIELD, Integer.parseInt(crawlingId)));
	}

	/**
	 * True if Source Iterator has next, else false
	 * 
	 * @return <b>boolean</b> 
	 */
	public boolean srcIterHasNext(){
		return srcIter.hasNext();
	}
	
	/**
	 * Return next Source Iterator's Document in json
	 * 
	 * @return <b>String</b> 
	 */
	public String srcIterNext(){
		return srcIter.next().toJson();
	}

	/**
	 * Return Target's DataBase Name. 
	 * It is the DataBase to store content in Simmo Schema
	 * 
	 * @return String 
	 */
	public static String getTrgDBName() {
		return MongoSimmoContentConstants.DB_NAME;
	}

	/**
	 * Read all records from provided type of content in {@value MongoSimmoContentConstants#DB_NAME} DB (Simmo)
	 * Results can be Iterated with <b>trgIterHasNext()</b> & <b>trgTextIterNext()</b>
	 *
	 * @return long. Number Of Results
	 */
	public long trgReadAll(String Medium){
		trgTextIter=null;
		MongoDatabase Mdb = mongoClient.getDatabase(MongoSimmoContentConstants.DB_NAME);
		if (Medium.equals(IndexCONSTANTS.MEDIUM_WEBSITE)) {
			trgTextIter = Mdb.getCollection(MongoSimmoContentConstants.WEBPAGE_COLLECTIO_NAME).find(new Document("items.$ref", "Text")).noCursorTimeout(true).iterator();
			return Mdb.getCollection(MongoSimmoContentConstants.WEBPAGE_COLLECTIO_NAME).count(new Document("items.$ref", "Text"));
		}
		else if (Medium.equals(IndexCONSTANTS.MEDIUM_PDF)) {
			trgTextIter = Mdb.getCollection(MongoSimmoContentConstants.PDF_COLLECTIO_NAME).find(new Document("items.$ref", "Text")).noCursorTimeout(true).iterator();
			return Mdb.getCollection(MongoSimmoContentConstants.PDF_COLLECTIO_NAME).count(new Document("items.$ref", "Text"));
		}
		else if (Medium.equals(IndexCONSTANTS.MEDIUM_POST)) { //means the forums posts, not the tweets
			trgTextIter = Mdb.getCollection(MongoSimmoContentConstants.POST_COLLECTION_NAME).find(new Document("$and", asList(
																											new Document("items.$ref", "Text"),
																											new Document("type", ScrappingConstants.FORUM)
																										)
																									)
																								).noCursorTimeout(true).iterator();
			return Mdb.getCollection(MongoSimmoContentConstants.POST_COLLECTION_NAME).count(new Document("$and", asList(
																									new Document("items.$ref", "Text"),
																									new Document("type", ScrappingConstants.FORUM)
																								)
																							)
																						);
		}
		return 0;
	}



	/**
	 * True if TargetWebpage Iterator has next, else false
	 *
	 * @return <b>boolean</b>
	 */
	public boolean trgIterHasNext(){
		return trgTextIter.hasNext();
	}

	/**
	 * Return an array of custom document in json for next TargetWebpage.
	 * [{
	 * "url" : url
	 * "id" : id (medium_id, ex. webside id or post id)
	 * "text_id" : text_id
	 * "content" : text_content
	 * "type" : text_type
	 * },
	 * {...}]
	 *
	 * @return <b>String</b>. JSON Array
	 */
	public String trgIterNext(){

		ArrayList docs = new ArrayList();
		Document webpage = trgTextIter.next();
		String url = webpage.getString("url");
		String wid = webpage.getString("_id");
		ArrayList<DBRef> items = (ArrayList<DBRef>) webpage.get("items");
		for (DBRef item : items) {

			String tid=item.getId().toString();
			Document item_doc=mongoClient.getDatabase(MongoSimmoContentConstants.DB_NAME).getCollection(item.getCollectionName()).find(new Document("_id",item.getId())).first();
			String content=item_doc.getString("content");
			String type=item_doc.getString("type");

			Document doc=new Document();
			doc.append("url",url);
			doc.append("id",wid);
			doc.append("text_id",tid);
			doc.append("content",content);
			doc.append("type",type);
			docs.add(doc);
		}

		Gson gson = new Gson();
		return gson.toJson(docs);
	}

	/**
	 * Returns true if there is at least one post with the provided url
	 * @param url
	 * @param i
	 * @return
	 */
	public boolean postExists(URL url, String i){
		MongoDatabase Mdb = mongoClient.getDatabase(MongoSimmoContentConstants.DB_NAME);
		FindIterable<Document> iterable =
					Mdb.getCollection(MongoSimmoContentConstants.POST_COLLECTION_NAME).find(
							and(
								eq(MongoSimmoContentConstants.URL_FIELD, url.toString()),
								eq(MongoSimmoContentConstants.POST_LABEL, i)
							)
					);

		//if at least one Document exists, true is returned
		for (Document document : iterable) {
			return true;
		}

		return false;
	}


	public boolean urlExistsInWebsite(URL url) {
		MongoDatabase Mdb = mongoClient.getDatabase(MongoSimmoContentConstants.DB_NAME);
		FindIterable<Document> iterable =
				Mdb.getCollection(MongoSimmoContentConstants.WEBPAGE_COLLECTIO_NAME).find(
						eq(MongoSimmoContentConstants.URL_FIELD, url.toString())
				);

		//if at least one Document exists, true is returned
		for (Document document : iterable) {
			return true;
		}

		return false;
	}


	public boolean urlExistsInPDF(URL url) {
		MongoDatabase Mdb = mongoClient.getDatabase(MongoSimmoContentConstants.DB_NAME);
		FindIterable<Document> iterable =
				Mdb.getCollection(MongoSimmoContentConstants.PDF_COLLECTIO_NAME).find(
						eq(MongoSimmoContentConstants.URL_FIELD, url.toString())
				);

		//if at least one Document exists, true is returned
		for (Document document : iterable) {
			return true;
		}

		return false;
	}
}























