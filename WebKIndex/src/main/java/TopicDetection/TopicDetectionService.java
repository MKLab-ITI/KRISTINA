package TopicDetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Functions.ServiceFunctions;
import TopicDetection.result.TopicDetectionResultObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import TopicDetection.mongo.GettingDataFromMongo;


public class TopicDetectionService {

	private int numClusteredObjects;

	public int getNumClusteredObjects() {
		return numClusteredObjects;
	}

	public String detector(boolean keepF, String language, int numDays) {

		// start timer
		long start = System.currentTimeMillis();

		System.out.println("TOPIC DETECTION SERVICE ( MONGO DB ) WAS CALLED");
		System.out.println("Timestamp: "+start);
		System.out.println("keepF: "+keepF);
		System.out.println("Days: "+numDays);

//		long twoDaysMillis = TopicDetectionConstants.NUM_DAYS*24*3600*1000;
		long twoDaysMillis = numDays*24*3600*1000;
		long since = start - twoDaysMillis;

		String responseString = "";

		String dataPath = TopicDetectionConstants.filesPath + "/Articles_Concepts_NamedEntities/"+start+"/";
		new File(dataPath).mkdir();

		long mongoStart=System.currentTimeMillis();
		
		GettingDataFromMongo gdm = new GettingDataFromMongo(language);
		Map<String, String> mapDate = gdm.gettingDataByDate(dataPath, since);
		this.numClusteredObjects = mapDate.size();
		System.out.println("IDs size: "+ this.numClusteredObjects);
		gdm.closeConnectionToMongo();
		 

		// Get elapsed time in milliseconds
		long elapsedTimeMillisMongo = System.currentTimeMillis()- mongoStart;

		// Get elapsed time in seconds
		float elapsedTimeSecMongo = elapsedTimeMillisMongo/1000F;

		responseString = getResult(start, mapDate, language);

		//	Delete created folder
		if(!keepF){
			emptyFolders(new File(dataPath));
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()- start;

		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;

		// Delete json response file
		if(!keepF)
			new File(TopicDetectionConstants.filesPath+"/topics_"+start+".json").delete();

		// print elapsed time
		System.out.println("Mongo query execution time: " + elapsedTimeSecMongo+" seconds");
		System.out.println("Total execution time: "+elapsedTimeSec+" seconds");
		System.out.println();

		return responseString;
	}

	public String detectorUsingIds(boolean keepF, String language, String ids) {

		// start timer
		long start = System.currentTimeMillis();

		System.out.println("TOPIC DETECTION SERVICE ( MONGO DB ) WAS CALLED");
		System.out.println("Timestamp: "+start);
		System.out.println("keepF: "+keepF);

		String responseString = "";

		String dataPath = TopicDetectionConstants.filesPath + "/Articles_Concepts_NamedEntities/"+start+"/";
		new File(dataPath).mkdir();

		JSONArray jsonArray = new JSONArray(ids);
		ArrayList<String> lines = new ArrayList<String>();
		if (jsonArray != null) {
			int len = jsonArray.length();
			for (int i=0;i<len;i++){
				lines.add(jsonArray.get(i).toString());
			}
		}

		this.numClusteredObjects = lines.size();
		System.out.println("IDs size: "+ this.numClusteredObjects);

		long mongoStart=System.currentTimeMillis();

		GettingDataFromMongo gdm = new GettingDataFromMongo(language);
		Map<String, String> mapDate = gdm.gettingData(dataPath, lines);
		gdm.closeConnectionToMongo();


		// Get elapsed time in milliseconds
		long elapsedTimeMillisMongo = System.currentTimeMillis()- mongoStart;

		// Get elapsed time in seconds
		float elapsedTimeSecMongo = elapsedTimeMillisMongo/1000F;

		responseString = getResult(start, mapDate, language);

		//	Delete created folder
		if(!keepF){
			emptyFolders(new File(dataPath));
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()- start;

		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;

		// Delete json response file
		if(!keepF)
			new File(TopicDetectionConstants.filesPath+"/topics_"+start+".json").delete();

		// print elapsed time
		System.out.println("Mongo query execution time: " + elapsedTimeSecMongo+" seconds");
		System.out.println("Total execution time: "+elapsedTimeSec+" seconds");
		System.out.println();

		return responseString;
	}

	public String detectorUsingKeyword(boolean keepF, String language, String keyword) {

		// start timer
		long start = System.currentTimeMillis();

		System.out.println("TOPIC DETECTION SERVICE ( MONGO DB ) WAS CALLED");
		System.out.println("Timestamp: "+start);
		System.out.println("keepF: "+keepF);
		System.out.println("Keyword: "+keyword);

		if(!isJSONValid(keyword)){
			return "Not valid JSON in the keywords parameter!";
		}

		String responseString = "";

		String dataPath = TopicDetectionConstants.filesPath + "/Articles_Concepts_NamedEntities/"+start+"/";
		new File(dataPath).mkdir();

		long mongoStart=System.currentTimeMillis();

		GettingDataFromMongo gdm = new GettingDataFromMongo(language);
		Map<String, String> mapDate = gdm.gettingDataByKeyword(dataPath, keyword);
		this.numClusteredObjects = mapDate.size();
		System.out.println("IDs size: "+ this.numClusteredObjects);
		gdm.closeConnectionToMongo();


		// Get elapsed time in milliseconds
		long elapsedTimeMillisMongo = System.currentTimeMillis()- mongoStart;

		// Get elapsed time in seconds
		float elapsedTimeSecMongo = elapsedTimeMillisMongo/1000F;

		responseString = getResult(start, mapDate, language);

		//	Delete created folder
		if(!keepF){
			emptyFolders(new File(dataPath));
		}

		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()- start;

		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;

		// Delete json response file
		if(!keepF)
			new File(TopicDetectionConstants.filesPath+"/topics_"+start+".json").delete();

		// print elapsed time
		System.out.println("Mongo query execution time: " + elapsedTimeSecMongo+" seconds");
		System.out.println("Total execution time: "+elapsedTimeSec+" seconds");
		System.out.println();

		return responseString;
	}

	/**
	 * execute R and form the service response
	 * @param start
	 * @param mapDate
	 * @return
	 */
	private String getResult(long start, Map<String, String> mapDate, String language){
		String responseString = "";

		//working path for R commands and json files (local)
		String workingPath= TopicDetectionConstants.filesPath;

		System.out.println("Running command: " + TopicDetectionConstants.exePath
				+" topic_detection_service_kristina_"+language+".R Articles_Concepts_NamedEntities/"+start);

		String line;

		//execute command
		Process p = null;
		try {
			p = new ProcessBuilder("cmd.exe", "/C",
					TopicDetectionConstants.exePath+" topic_detection_service_kristina_" + language
							+ ".R Articles_Concepts_NamedEntities/"+start)
					.directory(new File(workingPath))
					.redirectErrorStream(true).start();
			p.waitFor();

			//print output to stdout
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error executing topic detection R script!!!");
			return "Error executing topic detection!!!\n";
		}

		// return nothing if there are no results
		if(!(new File(TopicDetectionConstants.filesPath+"/topics_"+start+".json").exists()))
		{
			System.out.println("No results...");
			responseString+="No results...";
			return responseString;
		}

		try {
			// read output json file and add it to response
			BufferedReader json = new BufferedReader(new FileReader(workingPath+"/topics_"+start+".json"));
			while((line = json.readLine()) != null) {
				responseString+=line;
			}
			json.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error reading topic detection output!!!");
			return "Error reading topic detection output!!!\n";
		}

		responseString = responseString.replaceAll(".txt", "");

		// parse json
		JSONObject root = new JSONObject(responseString.trim());

		// iterate through topics
		Iterator<?> keys = root.keys();
		while( keys.hasNext() ) {
			String key = (String)keys.next();
			if ( root.get(key) instanceof JSONObject ) {

				//get topic
				JSONObject topic = (JSONObject) root.get(key);

				if((topic.get("articles") instanceof String)){
					String id = (String) topic.get("articles");
					String date = mapDate.get(id);
					JSONObject idWithDate = new JSONObject();
					idWithDate.put("id", id);
					idWithDate.put("date", date);
					JSONArray newArr = new JSONArray();
					newArr.put(0, idWithDate);
					topic.put("articles", newArr);
				}
				else{
					JSONArray articles = (JSONArray) topic.get("articles");
					for (int i = 0; i < articles.length(); ++i) {
						String id = articles.get(i).toString();
						String date = mapDate.get(id);
						JSONObject idWithDate = new JSONObject();
						idWithDate.put("id", id);
						idWithDate.put("date", date);
						articles.put(i, idWithDate);
					}
				}
			}
		}

		responseString = root.toString();

		return responseString;
	}

	public List<TopicDetectionResultObject> convertJsonToTopicDetectionResponse(String json){
		List<TopicDetectionResultObject> topicDetectionResponse = new ArrayList<>();
		// parse json
		try {
			JSONObject root = new JSONObject(json);
			int id = 1;
			boolean stop = false;
			while(!stop){
				String idString = String.valueOf(id);
				if(root.has(idString)){
					TopicDetectionResultObject tdObj = new TopicDetectionResultObject();
					tdObj.setId(idString);
					JSONObject topic = root.getJSONObject(idString);
					String labels = topic.getString("labels");
					tdObj.setKeywords(labels);
					Object topDocs = topic.get("top_ranked_docs");
					if(topDocs instanceof JSONArray){
						JSONArray ar = (JSONArray) topDocs;
						for (int i = 0; i < TopicDetectionConstants.NUM_TWEETS_PER_TOPIC; i++) {
							// add only number (without the "Twitter#" prefix)
							String tweetID = ar.get(i).toString().split("#")[1];
	//						tdObj.addTopPost(tweetID);
							String oembedJson = ServiceFunctions.sendGetRequest(TopicDetectionConstants.TWITTER_BASE_URL + tweetID);
							JSONObject oembedRoot = new JSONObject(oembedJson);
							String html = oembedRoot.getString("html");
							tdObj.addTopPostHTML(html);
						}
					}
					else if(topDocs instanceof String){
						String tweetID = topDocs.toString().split("#")[1];
						String oembedJson = ServiceFunctions.sendGetRequest(TopicDetectionConstants.TWITTER_BASE_URL + tweetID);
						JSONObject oembedRoot = new JSONObject(oembedJson);
						String html = oembedRoot.getString("html");
						tdObj.addTopPostHTML(html);
					}

					topicDetectionResponse.add(tdObj);
					++id;
				}
				else {
					stop = true;
				}

			}
		}
		catch(JSONException ex){
			System.out.println("Returning empty response...");
		}
		return topicDetectionResponse;
	}


	public void emptyFolders(File directory)
	{
		if(directory.exists()){
			String[] fileList = directory.list();
			File f = null;
			for(int i=0;i<fileList.length;i++)
			{
				f = new File(directory.getPath()+"\\"+fileList[i]);
				f.delete();
			}
			directory.delete();
		}
	}

	public boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		TopicDetectionService td = new TopicDetectionService();
		td.detector(true, "tr", TopicDetectionConstants.NUM_DAYS);
	}


}
