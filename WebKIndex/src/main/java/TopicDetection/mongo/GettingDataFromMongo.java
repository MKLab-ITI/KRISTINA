package TopicDetection.mongo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Filter;
import java.util.regex.Pattern;

import Functions.ServiceFunctions;
import TopicDetection.TopicDetectionConstants;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

public class GettingDataFromMongo {

	MongoDatabase db = null;
	MongoClient mongoClient = null;

	private String databaseName;

	public GettingDataFromMongo(String language){

		if(language.equals("de"))
			this.databaseName = "1";
		else if(language.equals("tr"))
			this.databaseName = "2";
		else if(language.equals("es"))
			this.databaseName = "3";
		else
			this.databaseName = "4";

		if(TopicDetectionConstants.authenticationUserMechamism.equalsIgnoreCase("MONGODB-CR")){
			MongoCredential credential1 = MongoCredential.createMongoCRCredential(TopicDetectionConstants.mongoUsername, this.databaseName, TopicDetectionConstants.mongoPassword.toCharArray());
			mongoClient = new MongoClient(new ServerAddress(TopicDetectionConstants.host + ":" + TopicDetectionConstants.port), Arrays.asList(credential1));
		}
		if(TopicDetectionConstants.authenticationUserMechamism.equalsIgnoreCase("SCRAM-SHA-1")){
			MongoCredential credential2 = MongoCredential.createScramSha1Credential(TopicDetectionConstants.mongoUsername, this.databaseName, TopicDetectionConstants.mongoPassword.toCharArray());
			mongoClient = new MongoClient(new ServerAddress(TopicDetectionConstants.host + ":" + TopicDetectionConstants.port), Arrays.asList(credential2));
		}

		db = mongoClient.getDatabase(this.databaseName);
	}

	/**
	 * get data using a list of ids
	 * @param dataPath
	 * @param ids
	 * @return
	 */
	public Map<String, String> gettingData(String dataPath, List<String> ids){

		MongoCollection<Document> collectionW = db.getCollection("Post");

		WebpageData pageData = gettingTextIds(collectionW, ids);
		Map<String, String> idsWithDates = pageData.getDocIdDocDateMap();


		gettingTextDataTags(collectionW, ids, dataPath);
		return idsWithDates;
	}

	/**
	 * get data since a given input date
	 * @param dataPath
	 * @param since
	 * @return
	 */
	public Map<String, String> gettingDataByDate(String dataPath, long since){

		MongoCollection<Document> collectionW = db.getCollection("Post");

		WebpageData pageData = gettingTextIdsSinceDate(collectionW, since);
		Map<String, String> idsWithDates = pageData.getDocIdDocDateMap();
		List<String> ids = new ArrayList<>();
		ids.addAll(idsWithDates.keySet());
		
		
//		gettingTextData(collectionW, ids, dataPath);
		gettingTextDataTags(collectionW, ids, dataPath);
		return idsWithDates;
	}

	public Map<String, String> gettingDataByKeyword(String dataPath, String keyword){

		MongoCollection<Document> collectionW = db.getCollection("Post");

		WebpageData pageData = gettingTextIdsByKeyword(collectionW, keyword);
		Map<String, String> idsWithDates = pageData.getDocIdDocDateMap();
		List<String> ids = new ArrayList<>();
		ids.addAll(idsWithDates.keySet());


//		gettingTextData(collectionW, ids, dataPath);
		gettingTextDataTags(collectionW, ids, dataPath);
		return idsWithDates;
	}

	public List<String> gettingDataHTMLByKeyword(String keyword, int numTweets){

		MongoCollection<Document> collectionW = db.getCollection("Post");

		WebpageData pageData = gettingTextIdsByKeyword(collectionW, keyword);
		Map<String, String> idsWithDates = pageData.getDocIdDocDateMap();
		List<String> ids = new ArrayList<>();
		ids.addAll(idsWithDates.keySet());

		List<String> dataHtml = new ArrayList<>();
		int count = 1;
		for (String id: ids) {
			String tweetID = id.split("#")[1];
			String oembedJson = ServiceFunctions.sendGetRequest(TopicDetectionConstants.TWITTER_BASE_URL + tweetID);
			if(oembedJson.startsWith("Error code returned: "))
				continue;
			JSONObject oembedRoot = new JSONObject(oembedJson);
			String html = oembedRoot.getString("html");
			dataHtml.add(html);
			count++;
			if(count > numTweets)
				break;
		}
		return dataHtml;
	}

	public void closeConnectionToMongo(){
		mongoClient.close();
	}


	/**
	 * query mongodb using date
	 * @param collection
	 * @param since
	 * @return
	 */
	private WebpageData gettingTextIdsSinceDate(MongoCollection<Document> collection, long since){
		WebpageData wpd = new WebpageData();
//		Map<String, String> hmT = new HashMap<String, String>();
		Map<String, String> hmD = new HashMap<String, String>();

		FindIterable<Document> dd = collection.find(Filters.gt("creationDate",new Date(since))).sort(com.mongodb.client.model.Sorts.ascending("creationDate")).projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("creationDate", "title", "_id")));
		for (Document document : dd) {
			String documentId = document.get("_id").toString();
			String title = document.get("title").toString();
			String documentDate = document.get("creationDate").toString();
//			hmT.put(documentId, title);
			hmD.put(documentId, documentDate);
		}

//		wpd.setDocIdDocTitleMap(hmT);
		wpd.setDocIdDocDateMap(hmD);
		
		return wpd;
	}

	/**
	 * query mongodb using a list of ids
	 * @param collection
	 * @param ids
	 * @return
	 */
	private WebpageData gettingTextIds(MongoCollection<Document> collection, List<String>  ids){
		WebpageData wpd = new WebpageData();
		Map<String, String> hm = new HashMap<String, String>();
		Map<String, String> hmT = new HashMap<String, String>();
		Map<String, String> hmD = new HashMap<String, String>();

		FindIterable<Document> dd = collection.find(com.mongodb.client.model.Filters.in("_id", ids)).sort(com.mongodb.client.model.Sorts.ascending("creationDate")).projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("creationDate", "title")));
		for (Document document : dd) {
			String documentId = document.get("_id").toString();
			String documentDate = document.get("creationDate").toString();
			hmD.put(documentId, documentDate);
		}
		wpd.setDocIdDocDateMap(hmD);
		return wpd;
	}

	private WebpageData gettingTextIdsByKeyword(MongoCollection<Document> collection, String keyword){
		WebpageData wpd = new WebpageData();
		Map<String, String> hm = new HashMap<String, String>();
		Map<String, String> hmT = new HashMap<String, String>();
		Map<String, String> hmD = new LinkedHashMap<String, String>();

		// specify filters
		Bson filtersArg = new BsonDocument();
		if(!keyword.equals("")){
			List<Bson> filters =new ArrayList<>();
			JSONArray array = new JSONArray(keyword);
			for (int i = 0; i < array.length(); i++) {
				String word = array.get(i).toString();
				System.out.println("Keyword added: " + word);
				filters.add(Filters.regex("title", Pattern.quote(word),"i"));
			}
			filtersArg = Filters.or(filters);
		}

		MongoCursor<Document> cursor = collection.find(filtersArg)
				.sort(com.mongodb.client.model.Sorts.descending("creationDate"))
				.projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("creationDate", "title"))).iterator();
		try {
			while (cursor.hasNext()) {
				// add new doc to list, after removing id field
				Document document = cursor.next();
				String documentId = document.get("_id").toString();
				String documentDate = document.get("creationDate").toString();
				hmD.put(documentId, documentDate);
			}
		} finally {
			cursor.close();
		}

		wpd.setDocIdDocDateMap(hmD);
		return wpd;
	}


	/**
	 * retrieve concept list given the input ids and store them to separate files
	 * @param collection
	 * @param ids
	 * @param dataPath
	 */
	private void gettingTextData(MongoCollection<Document> collection, List<String> ids, String dataPath){
				
		FindIterable<Document> dd = collection.find(com.mongodb.client.model.Filters.in("_id", ids)).projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("_id","annotations")));
		for (Document document : dd) {
		
			String documentId = document.getString("_id").toString();
			String data = "";
			
			ArrayList<Document> item = (ArrayList<Document>)document.get("annotations");
			for (Document doc: item){
				if(doc.get("className").toString().equalsIgnoreCase("gr.iti.mklab.simmo.core.annotations.Concepts")){
					if(doc.get("conceptsList")!=null){
						ArrayList<Document> conceptsList = (ArrayList<Document>)doc.get("conceptsList");
						data = data + gettingConceptsPerText(conceptsList);
					}
				}
//				if(doc.get("className").toString().equalsIgnoreCase("gr.iti.mklab.simmo.core.annotations.NamedEntities")){
//					if(doc.get("namedEntitiesList")!=null){
//						ArrayList<Document> namedEntities = (ArrayList<Document>)doc.get("namedEntitiesList");
//						String[] nes = gettingNamedEntitiesPerText(namedEntities);
//						data = data + " " + nes[1];
//					}
//				}
			}

		
			// write data to File
			writeFile(dataPath + "\\" + documentId + ".txt", data.trim(), true);
		}
	}

	/**
	 * retrieve tags given the input ids and store them to separate files
	 * @param collection
	 * @param ids
	 * @param dataPath
	 */
	private void gettingTextDataTags(MongoCollection<Document> collection, List<String> ids, String dataPath){

		FindIterable<Document> dd = collection.find(com.mongodb.client.model.Filters.in("_id", ids)).projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("_id","tags","annotations")));
		for (Document document : dd) {

			String documentId = document.getString("_id").toString();
			String data = "";
			ArrayList<String> tagsList = (ArrayList<String>)document.get("tags");
			if (tagsList!=null){
				for (String tag: tagsList){
					data += tag + " ";
				}
			}

			ArrayList<Document> item = (ArrayList<Document>)document.get("annotations");
			for (Document doc: item){
				if(doc.get("className").toString().equalsIgnoreCase("gr.iti.mklab.simmo.core.annotations.Concepts")){
					if(doc.get("conceptsList")!=null){
						ArrayList<Document> conceptsList = (ArrayList<Document>)doc.get("conceptsList");
						data = data + gettingConceptsPerText(conceptsList);
					}
				}
			}

			// write data to File
			writeFile(dataPath + "\\" + documentId + ".txt", data.trim(), true);
		}
	}
	
	
	private String gettingConceptsPerText(ArrayList<Document> conceptsList){
		StringBuffer sb = new StringBuffer();
		for (Document cons: conceptsList) {
			sb.append(cons.get("concept") + " ");
		}
		
		return sb.toString().trim();
	}
	
	
	private String[] gettingNamedEntitiesPerText(ArrayList<Document> namedEntitiesList){
		StringBuffer sb = new StringBuffer();
		StringBuffer sbForLabelling = new StringBuffer();
		for (Document nes: namedEntitiesList) {
			if(nes.get("namedEntityType").toString().equalsIgnoreCase("LOCATION") ||nes.get("namedEntityType").toString().equalsIgnoreCase("ORGANISATION") ||nes.get("namedEntityType").toString().equalsIgnoreCase("PERSON")){
				sb.append(nes.get("token") + " ");
				sbForLabelling.append(nes.get("token").toString().replaceAll("\\s+", "_") + " ");
			}
		}
		
		String[] out = {sb.toString().trim(), sbForLabelling.toString().trim()};
		
		return out;
	}
	
	

	
	private void writeFile(String filename, String content, boolean append) {
		File file = new File(filename);
		Writer fw = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			if (append)
				fw = new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8);
//				fw = new FileWriter(file.getAbsoluteFile(), true);
			else
				fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
//				fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTweet(String id){
		MongoCollection<Document> collection = db.getCollection("Post");
		FindIterable<Document> dd = collection.find(new Document("_id", id)).projection(com.mongodb.client.model.Projections.fields(com.mongodb.client.model.Projections.include("_id","title")));
		String title = "";
		for (Document document : dd) {
			title = document.get("title").toString();
		}
		return title;
	}


}
