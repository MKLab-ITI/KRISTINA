package WebKIndex;

import Indexing.PassageIndexHandler;
import TopicDetection.CategoryClassification.CategoryClassificationHandler;
import TopicDetection.TopicDetectionService;
import TopicDetection.mongo.GettingDataFromMongo;
import TopicDetection.result.TopicDetectionResultObject;
import Version.PassageRetrievalVersion;
import Version.Versioning;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class TemplateController {

	static Logger log = LogManager.getLogger("Global");

	@RequestMapping("/")
	public String index(Model model){
		return "index";
	}

	@RequestMapping("/version")
	public String version(Model model){

		log.info("Version.html Request");
		System.out.println("Version.html Request");

		Versioning v = new Versioning();
		model.addAttribute("current",v.getCurernt());
		model.addAttribute("oldItems",v.getPreviousVersions());
		return "version";
	}

	@RequestMapping("/demo")
	public String demoResponse(Model model){

		log.info("Demo.html Request");
		System.out.println("Demo.html Request");

		return "demo";
	}

	@RequestMapping("/help")
	public String helpResponse(Model model){

		log.info("Help.html Request");
		System.out.println("Help.html Request");

		return "Help";
	}

	@RequestMapping("/testing")
	@ResponseBody
	//just tesing page. Put your wanna test code here
	public String test(){
		String response = "Testing Started!!!<br>";
		System.out.println("------------------");
		System.out.println("Testing Started!!!");
		System.out.println("------------------");
		try {
			//Testing Code Block


			//End of Testing Code Block
		}
		catch (Exception e){
			System.out.println("----------------------------");
			System.out.println("Exception Caught in Testing ");
			e.printStackTrace();
			System.out.println("----------------------------");
		}
		System.out.println("-------------------");
		System.out.println("Testing Finished!!!");
		System.out.println("-------------------");
		response += "Testing finished successfully!";
		return response;
	}


	@RequestMapping("/passagetable")
	public String passagetableResponse(@RequestParam(value="query", required=true, defaultValue = "") String query,
									   @RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
									   @RequestParam(value="version", required=false, defaultValue = DefaultValues.version) String version,
//									   @RequestParam(value="retrieval", required=false, defaultValue = DefaultValues.retrieval) String retrievalMethod,
									   @RequestParam(value="topN", required=false, defaultValue = DefaultValues.topN) String topNString,
									   @RequestParam(value="docBasedMode", required=false, defaultValue = DefaultValues.docBasedMode) String docBasedMode,
									   @RequestParam(value="docBasedTopN", required=false, defaultValue = DefaultValues.docBasedModeTopN) String docBasedModeTopNString,
									   @RequestParam(value="paragraphBasedMode", required=false, defaultValue = DefaultValues.paragraphBasedMode) String paragraphBasedMode,
									   @RequestParam(value="paragraphBasedTopN", required=false, defaultValue = DefaultValues.paragraphBasedModeTopN) String paragraphBasedModeTopNString,
									   @RequestParam(value="queryExpansion", required=false, defaultValue = DefaultValues.queryExpansion) boolean queryExpansion,
									   Model model){

		SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
		log.info("\nRequested PassageTable for query: \"" + query + "\" ");
		System.out.println("\nRequested PassageTable for query: \"" + query + "\" at " + ft.format(new Date()));

		long start = System.currentTimeMillis();

		// check if version and input language are supported
		Map.Entry<Boolean,String> validation = PassageIndexHandler.validateInput(language,version);
		if(!validation.getKey()){
			model.addAttribute("message", validation.getValue());
			return "Error";
		}

		PassageRetrievalVersion versionItem = PassageRetrievalVersion.componentVersionItems.get(version);
		System.out.println(versionItem.getVersionInfo());

		model.addAttribute("query",query);
		PassageIndexHandler pih = new PassageIndexHandler(language, versionItem.getIndexVersion(),queryExpansion);
		pih.setDocBasedMode(docBasedMode);
		pih.setParagraphBasedMode(paragraphBasedMode);

		// if input has not number format, if retains the default value
		try{
			int docBasedModeTopN = Integer.parseInt(docBasedModeTopNString);
			pih.setDocBasedModeTopN(docBasedModeTopN);
		}
		catch(NumberFormatException nfe) {}
		try{
			int paragraphBasedModeTopN = Integer.parseInt(paragraphBasedModeTopNString);
			pih.setParagraphBasedModeTopN(paragraphBasedModeTopN);
		}
		catch(NumberFormatException nfe) {}

		System.out.println("Doc based mode: " + docBasedMode + " (" + docBasedModeTopNString + " docs)");
		System.out.println("Paragraph based mode: " + paragraphBasedMode + " (" + paragraphBasedModeTopNString + " paragraphs)");
		System.out.println("Query expansion: " + queryExpansion);

		// set number of retrieval results per similarity measure
		// if input has not number format, if retains the default value
		try{
			int topN = Integer.parseInt(topNString);
			pih.setTopN(topN);
		}
		catch(NumberFormatException nfe) {}

		// indexBased: just retrieve the results from each index
		// documentBased: first retrieve from document index, use the results to search from the other indices
		if(versionItem.getRetrievalMethod().equals("indexBased"))
			model.addAttribute("passages",pih.getResults(query));
		else if(versionItem.getRetrievalMethod().equals("documentBased"))
			model.addAttribute("passages",pih.getResultsUsingDocuments(query));
		else if(versionItem.getRetrievalMethod().equals("documentParagraphBased"))
			model.addAttribute("passages",pih.getResultsUsingDocumentsParagraphs(query,false));
		else if(versionItem.getRetrievalMethod().equals("documentParagraphContextBased"))
			model.addAttribute("passages",pih.getResultsUsingDocumentsParagraphs(query,true));
		else if(versionItem.getRetrievalMethod().equals("documentBasedRefined"))
			model.addAttribute("passages",pih.getResultsUsingDocumentsRefined(query));


		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time (millis): " + elapsed);
		return "PassageTable";
	}

	@RequestMapping("/webappassagetable")
	public String webappassagetableResponse(Model model){

		log.info("Requested WebApPassageTable ");
		System.out.println("Requested WebApPassageTable ");

		return "WebAPPassageTable";
	}

	@RequestMapping("/query")
	public String getQueryExamples(Model model){

		log.info("Query.html request");
		System.out.println("Query.html request");

		return "Query";
	}

	@RequestMapping("/query2ndProto")
	public String getQueryExamples2ndProto(Model model){

		log.info("Query2ndProto.html request");
		System.out.println("Query2ndProto.html request");

		return "Query2ndProto";
	}

	@RequestMapping("/info")
	public String getInfo(Model model){

		log.info("Info.html request");
		System.out.println("Info.html request");

		return "Info";
	}

	/**
	 * if a keyword is not added, topic detection will be made using date
	 * @param language
	 * @param days
	 * @param keyword
	 * @param keepF
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/topicDetectionHTML")
	public String topicDetectionHTML(@RequestParam(value="language", required=false, defaultValue = "de") String language,
									 @RequestParam(value="days", required=false, defaultValue = "2") int days,
									 @RequestParam(value="keyword", required=false, defaultValue = "") String keyword,
									 @RequestParam(value="keepF", required=false, defaultValue = "false") boolean keepF,
									 Model model){
		System.out.println();
		System.out.println("Requested topic detection (HTML)...");
		System.out.println("Language: " + language);
		TopicDetectionService tds = new TopicDetectionService();
		String json;
		if(keyword.equals("")){
			json = tds.detector(keepF, language, days);
		}
		else{
			json = tds.detectorUsingKeyword(keepF, language, keyword);
		}
		List<TopicDetectionResultObject> response = tds.convertJsonToTopicDetectionResponse(json);
		int numClusteredTweets = tds.getNumClusteredObjects();
		model.addAttribute("response", response);
		model.addAttribute("numTweets", numClusteredTweets);
		return "socialMedia/TopicDetection";
	}

	@RequestMapping(value = "/tweetSearch")
	public String tweetSearch(@RequestParam(value="language", required=false, defaultValue = "de") String language,
									 @RequestParam(value="keyword", required=false, defaultValue = "") String keyword,
									 @RequestParam(value="maxResults", required=false, defaultValue = "10") int maxResults,
									 Model model){
		System.out.println();
		System.out.println("Requested tweet search...");
		System.out.println("Language: " + language);
		System.out.println("Keyword: " + keyword);
		if(isJSONValid(keyword) || keyword.equals("")){
			GettingDataFromMongo gdm = new GettingDataFromMongo(language);
			List<String> response = gdm.gettingDataHTMLByKeyword(keyword, maxResults);
			int numTweets = response.size();
			model.addAttribute("response", response);
			model.addAttribute("keyword", keyword);
			model.addAttribute("numTweets", numTweets);
		}
		else{
			model.addAttribute("response", "");
			model.addAttribute("keyword", "Keyword input format is not valid!");
			model.addAttribute("numTweets", 0);
		}
		return "socialMedia/TweetSearchResults";
	}

	@RequestMapping(value = "/getTweetsByCategory")
	public String getTweetsByCategory(@RequestParam(value="language", required=false, defaultValue = "de") String language,
									  @RequestParam(value="category", required=false, defaultValue = "Health") String category,
									  @RequestParam(value="maxResults", required=false, defaultValue = "10") int maxResults,
									  Model model
	){
		System.out.println();
		System.out.println("Requested tweet search using category...");
		System.out.println("Language: " + language);
		System.out.println("Category: " + category);
		CategoryClassificationHandler cch = new CategoryClassificationHandler(language);
		List<String> response = cch.getDataHTMLByCategory(category, maxResults);
		int numTweets = response.size();
		model.addAttribute("response", response);
		model.addAttribute("keyword", category);
		model.addAttribute("numTweets", numTweets);
		return "socialMedia/TweetSearchResults";
	}

	public boolean isJSONValid(String text) {
		try {
			new JSONObject(text);
		} catch (JSONException ex) {
			try {
				new JSONArray(text);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}



}
