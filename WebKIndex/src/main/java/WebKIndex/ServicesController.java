package WebKIndex;

import Functions.FileFunctions;
import Functions.ParsingFunctions;
import Functions.ParsingNewsArticleFunctions;
import Indexing.*;
import Indexing.LuceneCustomClasses.CustomStopAnalyzerSpanish;
import Indexing.Newspapers.NewspapersRetriever;
import Indexing.Queries.QueryTopicMapper;
import Indexing.Recipes.RecipesRetriever;
import Indexing.WebAP.WebAPPassage;
import Indexing.WebAP.WebAPPassageIndexHandler;
import KnowledgeBase.KBConstants;
import KnowledgeBase.KBHandler;
import MetaMap.LocalMetaMapHandler;
import TopicDetection.CategoryClassification.CategoryClassificationHandler;
import TopicDetection.TopicDetectionService;
import TopicDetection.mongo.GettingDataFromMongo;
import Version.PassageRetrievalVersion;
import WordEmbeddings.Word2VecHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.document.TextDocument;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import com.kohlschutter.boilerpipe.sax.BoilerpipeSAXInput;
import com.kohlschutter.boilerpipe.sax.HTMLFetcher;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.Analyzer;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static CoreNLP.CoreNLPHandler.getNEasjson;



@CrossOrigin
@EnableScheduling
@RestController
public class ServicesController {

	static Logger log = LogManager.getLogger("Global");

	/**
	 * Returns the matching passage from All segmentation index
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/getpassage", produces = "application/json" )
	@ResponseBody
	public String getPassage(@RequestParam(value="query", required=false, defaultValue = "") String query,
							 @RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
							 @RequestParam(value="version", required=false, defaultValue = DefaultValues.version) String version,
							 @RequestParam(value="docBasedMode", required=false, defaultValue = DefaultValues.docBasedMode) String docBasedMode,
							 @RequestParam(value="docBasedTopN", required=false, defaultValue = DefaultValues.docBasedModeTopN) String docBasedModeTopNString,
							 @RequestParam(value="paragraphBasedMode", required=false, defaultValue = DefaultValues.paragraphBasedMode) String paragraphBasedMode,
							 @RequestParam(value="paragraphBasedTopN", required=false, defaultValue = DefaultValues.paragraphBasedModeTopN) String paragraphBasedModeTopNString,
							 @RequestParam(value="queryExpansion", required=false, defaultValue = DefaultValues.queryExpansion) boolean queryExpansion
){

		SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
		log.info("Requested passage for query: \"" + query + "\" ");
		System.out.println("Requested passage for query: \"" + query + "\" at " + ft.format(new Date()));

		long start = System.currentTimeMillis();

		if (query.equals("") || query.equals(" ")){
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("Elapsed time (millis): " + elapsed);
			return "{}";
		}

		// check if version and input language are supported
		Map.Entry<Boolean,String> validation = PassageIndexHandler.validateInput(language,version);
		if(!validation.getKey()){
			return validation.getValue();
		}

		PassageRetrievalVersion versionItem = PassageRetrievalVersion.componentVersionItems.get(version);
		System.out.println(versionItem.getVersionInfo());

		PassageIndexHandler pih = new PassageIndexHandler(language, versionItem.getIndexVersion(), queryExpansion);
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

		String retrievalMethod = versionItem.getRetrievalMethod();
		String response = pih.singleResultQueryFromTable(query, retrievalMethod);

		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time (millis): " + elapsed);

		return response;
	}

	/**
	 * Get passage retrieval result for the bioasq annotation tool
	 * @param query
	 * @param language
	 * @param version
	 * @return
	 */
	@RequestMapping(value = "/annotationToolResult", produces = "application/json" )
	@ResponseBody
	public String getResultForAnnotationTool(@RequestParam(value="query", required=false, defaultValue = "") String query,
							 @RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
							 @RequestParam(value="version", required=false, defaultValue = DefaultValues.version) String version
							 ){

		SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
		log.info("Requested annotation tool results for query: \"" + query + "\" ");
		System.out.println("Requested annotation tool results for query: \"" + query + "\" at " + ft.format(new Date()));
		System.out.println("Language: " + language);

		long start = System.currentTimeMillis();

		if (query.equals("") || query.equals(" ")){
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("Elapsed time (millis): " + elapsed);
			return "{}";
		}

		// check if version and input language are supported
		Map.Entry<Boolean,String> validation = PassageIndexHandler.validateInput(language,version);
		if(!validation.getKey()){
			return validation.getValue();
		}

		QueryTopicMapper mapper = new QueryTopicMapper();
		mapper.formTopicMap(language);
		Map<String,ArrayList<String>> topicMap = mapper.getTopicMap();
		ArrayList<String> questionsList = topicMap.get(query);
		for (String question : questionsList){
			log.info("Query to be executed: " + question);
		}

		PassageRetrievalVersion versionItem = PassageRetrievalVersion.componentVersionItems.get(version);
		System.out.println(versionItem.getVersionInfo());

		PassageIndexHandler pih = new PassageIndexHandler(language, versionItem.getIndexVersion());
		pih.setTopN(5);

		String response = pih.getResultsForAnnotationTool(query, questionsList);

		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time (millis): " + elapsed);

		return response;
	}

	@RequestMapping("/getScrapedUrls")
	public String getScrapedUrls(@RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
								 @RequestParam(value="version", required=false, defaultValue = DefaultValues.majorVersionNum) String version){
		log.info("Requested crawled urls");
		System.out.println("Requested crawled urls");

		String response = "";
		int versionNum = 0;
		try{
			versionNum = Integer.parseInt(version);
		}
		catch(NumberFormatException nfe) {
			response = "Invalid input version given: "+ version;
			return response;
		}

		// check if version and input language are supported
		if ( PassageRetrievalVersion.isLanguageSupportedInIndexVersion(versionNum,language)){
			PassageIndexHandler pih = new PassageIndexHandler(language, versionNum);
			List<String> urls = pih.getScrapedUrls();
			for (String url: urls) {
				response += url +"<br>";
			}
		}
		else{
			response = "Language " + language + " is not supported is version "+ version +"!!!\n"
					+ PassageRetrievalVersion.getSupportedLanguagesPerVersion();
		}

		return response;
	}

	@RequestMapping(value = "/getScrapedContentFromUrl", produces = "application/json")
	public String getScrapedContentFromUrl(@RequestParam(value="url") String url,
										   @RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
										   @RequestParam(value="version", required=false, defaultValue = DefaultValues.majorVersionNum) String version){
		log.info("Requested scraped content from url: " + url);
		System.out.println("Requested scraped content from url: " + url);

		String response = "";
		int versionNum = 0;
		try{
			versionNum = Integer.parseInt(version);
		}
		catch(NumberFormatException nfe) {
			response = "Invalid input version given: "+ version;
			return response;
		}

		// check if version and input language are supported
		if ( PassageRetrievalVersion.isLanguageSupportedInIndexVersion(versionNum,language)){
			PassageIndexHandler pih = new PassageIndexHandler(language, versionNum);
			response = pih.getScrapedContentFromUrl(url.trim());
		}
		else{
			response = "Language " + language + " is not supported is version "+ version +"!!!\n"
					+ PassageRetrievalVersion.getSupportedLanguagesPerVersion();
		}

		return response;
	}

	@RequestMapping(value = "/getpassagebyid", produces = "text/plain" )
	@ResponseBody
	public String getPassageById(@RequestParam(value="index", required=false, defaultValue = "Document") String index,
								 @RequestParam(value="id", required=false, defaultValue = "1") String id,
								 @RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
								 @RequestParam(value="version", required=false, defaultValue = DefaultValues.majorVersionNum) String version){

		String response;
		int versionNum = 0;
		try{
			versionNum = Integer.parseInt(version);
		}
		catch(NumberFormatException nfe) {
			response = "Invalid input version given: "+ version;
			return response;
		}

		// check if version and input language are supported
		if ( PassageRetrievalVersion.isLanguageSupportedInIndexVersion(versionNum,language)){
			PassageIndexHandler pih = new PassageIndexHandler(language, versionNum);
			response = pih.getDocumentUsingID(index,id);
		}
		else{
			response = "Language " + language + " is not supported is version "+ version +"!!!\n"
					+ PassageRetrievalVersion.getSupportedLanguagesPerVersion();
		}


		return response;
	}

	@RequestMapping(value = "/getindexstats", produces = "text/plain" )
	@ResponseBody
	public String getPassageIndexStats(@RequestParam(value="language", required=false, defaultValue = DefaultValues.language) String language,
								 @RequestParam(value="version", required=false, defaultValue = DefaultValues.majorVersionNum) String version){

		String response;
		int versionNum = 0;
		try{
			versionNum = Integer.parseInt(version);
		}
		catch(NumberFormatException nfe) {
			response = "Invalid input version given: "+ version;
			return response;
		}

		// check if version and input language are supported
		if ( PassageRetrievalVersion.isLanguageSupportedInIndexVersion(versionNum,language)){
			PassageIndexHandler pih = new PassageIndexHandler(language, versionNum);
			response = pih.getNumDocsPerIndex();
		}
		else{
			response = "Language " + language + " is not supported is version "+ version +"!!!\n"
					+ PassageRetrievalVersion.getSupportedLanguagesPerVersion();
		}


		return response;
	}

	@RequestMapping(value = "/getprversions", produces = "text/plain" )
	@ResponseBody
	public String getPassageRetrievalAvailableVersions(){

		String response = PassageRetrievalVersion.getSupportedVersionsDetailed() + PassageRetrievalVersion.getSupportedLanguagesPerVersion();
		return response;
	}

	@RequestMapping("/index")
	@ResponseBody
	//query parameter is required
	//source parameter is not required, and when it is absence, the default value will be "all"
	public String indexResponse(@RequestParam(value="query", required=false, defaultValue = "") String query,
						   @RequestParam(value="source", required=false, defaultValue = "all") String source,
						   @RequestParam(value="ne", required=false, defaultValue = "") String ne,
						   @RequestParam(value="concept", required=false, defaultValue = "") String concept){

		IndexHandler inxh = new IndexHandler();
		String response = "";
		System.out.println(" Source:" + source + ", query: " + query + ", ne: " + ne + ", concepts: "+ concept);
		if (source.equals("all")){
			response = inxh.makeVSQueryString("general", query, ne, concept);
		}
		else if (source.equals("website")){
			response = inxh.makeVSQueryString(IndexCONSTANTS.MEDIUM_WEBSITE, query, ne, concept);
		}
		else if (source.equals("forum")){
			response = inxh.makeVSQueryString(IndexCONSTANTS.MEDIUM_POST, query, ne, concept);
		}
		else if (source.equals("pdf")){
			response = inxh.makeVSQueryString(IndexCONSTANTS.MEDIUM_PDF, query, ne, concept);
		}
		else if (source.equals("twiter")){
			response = inxh.makeVSQueryString(IndexCONSTANTS.MEDIUM_TWEET, query, ne, concept);
		}
		else{
			return "Unknown Source";
		}

		if (response.equals("")){
			return "{}";
		}
		else {
			return response;
		}
	}

	@RequestMapping("/WebAPGetQuery")
	public Map<String, ArrayList<WebAPPassage>> WebAPPGetQuery(@RequestParam(value="qid", required=true, defaultValue = "") int qID){
		WebAPPassageIndexHandler wpih = new WebAPPassageIndexHandler();
		return wpih.getResults(qID);
	}

	@RequestMapping("/extractWebsite")
	@ResponseBody
	public String extractWebsiteResponse(@RequestParam(value="url", required=true, defaultValue = "") String urlSt){

		String errorMessage = "An Error Occurred!";

		if (urlSt.equals("")){
			log.info("Extract Website, no url parameter" );
			System.out.println("Extract Website, no url parameter");
			return "";
		}

		String content = null;

		//Extract the content as plain text
		try {
			URL url = new URL(urlSt.replaceAll(" ","%20"));

			final InputSource is = HTMLFetcher.fetch(url).toInputSource();
			final BoilerpipeSAXInput in;
			in = new BoilerpipeSAXInput(is);
			final TextDocument doc = in.getTextDocument();
			content = ArticleExtractor.INSTANCE.getText(doc);
		} catch (SAXException e) {
			log.error("SAXException in extracting Website, url: " + urlSt  );
			System.out.println("SAXException in extracting Website, url: " + urlSt );
			e.printStackTrace();
			return errorMessage;
		} catch (BoilerpipeProcessingException e) {
			log.error("BoilerpipeProcessingException in extracting Website, url: " + urlSt  );
			System.out.println("BoilerpipeProcessingException in extracting Website, url: " + urlSt );
			e.printStackTrace();
			return errorMessage;
		} catch (IOException e) {
			log.error("IOException in extracting Website, url: " + urlSt  );
			System.out.println("IOException in extracting Website, url: " + urlSt );
			e.printStackTrace();
			return errorMessage;
		}

		log.info("Extract Website, url: " + urlSt  );
		System.out.println("Extract Website, url: " + urlSt );
		return content;
	}

	@RequestMapping("/extractConceptsNe")
	@ResponseBody
	public String extractConceptsNeResponse(@RequestParam(value="text", required=true, defaultValue = "") String text){

		String errorMessage = "Error";

		if (text.equals("")){
			log.info("Extract Concept/NE, no text parameter" );
			System.out.println("Extract Concept/NE, no text parameter");
			return "";
		}

		JSONObject obj = new JSONObject();

		JSONObject nes = getNEasjson(text);
		obj.put("ne",nes);
		JSONObject concepts= LocalMetaMapHandler.getConceptsasJson(text);
		obj.put("concepts",concepts);

		log.info("Extract Concept/NE text: " + text.substring(0,50) + " ... ");
		System.out.println("Extract Concept/NE text: " + text.substring(0,50) + " ... ");
		return obj.toString();
	}

	@RequestMapping(value = "/newspaper", produces = "text/plain")
	@ResponseBody
	public String newspaperResponse(@RequestParam(value="title", required=true, defaultValue = "") String title,
									@RequestParam(value="onlyFirstParagraph", required=false, defaultValue = "false") String onlyFirstParagraph){

		log.info("Requested Newspaper with title: \"" + title + "\" ");
		System.out.println("Requested Newspaper with title: \"" + title + "\" ");

		if (title.equals("")) {
//			return "{}";
			return "";
		}
		else{
			IndexHandler ih = new IndexHandler();
			String response = ih.makeNewspaperQueryByTitle(title);
			if(onlyFirstParagraph.equals("true")){
				String firstParagraph = response.split("\n\n")[0];
				response = firstParagraph;
			}
			ih.close();
			return response;
		}
	}


	@RequestMapping("/newspaperCrawling")
	//run this function after 12 hours, and re-run every 12 hours
	@Scheduled(initialDelay=43200000, fixedRate=43200000) //in milliseconds
	private void newspaperCrawling(){

		SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
		System.out.println("Scheduled task for newspaper indexing started at " + ft.format(new Date()));


		IndexHandler ih = new IndexHandler();
		ih.openWriter();

		String[] rssPages = {"http://www.tagblatt.de/T%C3%BCbingen.rss",
				"http://www.tagblatt.de/Kreis%20Freudenstadt.rss",
				"http://www.tagblatt.de/Kreis%20Reutlingen.rss",
				"http://www.tagblatt.de/Kreis%20T%C3%BCbingen.rss",
				"http://www.tagblatt.de/Schwitzkasten.rss",
				"http://www.tagblatt.de/Freudenstadt.rss",
				"http://www.tagblatt.de/Horb.rss",
				"http://www.tagblatt.de/Sulz.rss",
				"http://www.tagblatt.de/Ammerbuch.rss",
				"http://www.tagblatt.de/Kirchentellinsfurt.rss",
				"http://www.tagblatt.de/M%C3%B6ssingen.rss",
				"http://www.tagblatt.de/Rottenburg.rss"};

		for (String rssPage : rssPages) {
			URL rssInput = null;
			try {

				rssInput = new URL(rssPage);
				SyndFeedInput input = new SyndFeedInput();
				System.out.println("Getting the rss page: " + rssPage);
				SyndFeed inFeed = input.build(new XmlReader(rssInput));
				System.out.println("Extracting pages");
				List<SyndEntry> entries = inFeed.getEntries();
				for (SyndEntry entry : entries) {
					String rssTitle = entry.getTitle();
					SyndContent rssDescription = entry.getDescription();
					String rssDescriptionText = "";
					if (rssDescription.getType().equals("text/html")) {
						Document doc = Jsoup.parse(rssDescription.getValue());
						rssDescriptionText = doc.body().text();
					} else {
						rssDescriptionText = rssDescription.getValue();
					}
					String url = entry.getLink();

					Document document = Jsoup.connect(url).get();
					Elements mainClasses = document.getElementsByClass("main-article");
					int mainSize = mainClasses.size();
					if (mainSize == 0) {
						/*System.out.println(url);
						System.out.println("     Title: " + rssTitle);
						System.out.println("     PAID CONTENT");*/

					} else {
						Element mainClass = mainClasses.get(0);
						Elements header = mainClass.getElementsByTag("header");
						String title = header.get(0).getElementsByTag("h2").text();
						Elements p = mainClass.getElementsByTag("p");
						String description = p.get(0).text();
						Elements dateDiv = document.getElementsByClass("artikelhead");
						String date = dateDiv.get(0).getElementsByTag("span").get(0).text();
//						Elements dateDiv = document.select("div.datum");
//						String date = dateDiv.get(0).getElementsByTag("span").get(0).text()
//								.replace(" Uhr", "");
//						if(date.contains(" | "))
//							date = date.split(" | ")[0];
						Elements contentDivElements = document.getElementsByClass("divcontent");

						// handle for paid content that cannot be viewed
						if(contentDivElements.size() == 0){
							System.out.println("Not Indexed, paid content in url: " + url);
							continue;
						}

						Element contentDiv = contentDivElements.get(0).children().get(1);
						String content = "";
						String prefix = "";
						for (Element paragraph : contentDiv.children()) {
//							content += paragraph.text();
							String paragraphText = paragraph.text();
							String paragraphWithSentenceBreaks = ParsingNewsArticleFunctions.insertSentenceBreaksToParagraph(
									paragraphText, IndexCONSTANTS.NEWSPAPER_LANGUAGE);
							content += prefix + paragraphWithSentenceBreaks;
							prefix = "\n\n";
						}

						// cut content after the last full stop
						int fullstopLastIndex = content.lastIndexOf(".");
						if((fullstopLastIndex+1) != content.length())
							if(content.charAt(fullstopLastIndex+1)==' ' || content.charAt(fullstopLastIndex+1)=='/')
								content = content.substring(0,fullstopLastIndex + 1);
							// else, we suppose that the last fullstop is part of a url or an email
							// we inspect the end of the sentence in the '. ' string
							else
								content = content.substring(0,content.lastIndexOf(". ") + 1);

						// parse content according to upf instructions
						content = ParsingNewsArticleFunctions.refineNewspaperArticleText(content);


						JSONObject jsonObj = new JSONObject();
						jsonObj.put(IndexCONSTANTS.FIELD_URL, url);
						jsonObj.put(IndexCONSTANTS.FIELD_TITLE, title);
						jsonObj.put(IndexCONSTANTS.FIELD_DESCRIPTION, description);
						jsonObj.put(IndexCONSTANTS.FIELD_DATE_STRING, date);
						jsonObj.put(IndexCONSTANTS.FIELD_CONTENT, content);
						jsonObj.put(IndexCONSTANTS.FIELD_MEDIUM, IndexCONSTANTS.MEDIUM_NEWSPAPER);
						ih.indexNewspaperJson(jsonObj);

					}



					//Politeness policy of one second.
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						System.out.println("Could not make the thread sleep");
					}
				}
				System.out.println("Links extracted");
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException in RSS process");
//				e.printStackTrace();
			} catch (FeedException e) {
				System.out.println("FeedException in RSS process");
//				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException in RSS process");
//				e.printStackTrace();
			}
		}

		ih.closeWriter();
		ih.close();
		System.out.println("Scheduled task for newspaper indexing finished successfully at " + ft.format(new Date()));
	}

//	@RequestMapping("/newspaperCrawlFromFile")
	public void newspaperCrawlFromFile(){
		log.info("Requested to crawl newspaper article urls from file: " + IndexCONSTANTS.URLS_LIST_FILE);
		System.out.println("Requested to crawl newspaper article urls from file: " + IndexCONSTANTS.URLS_LIST_FILE);
		List<String> urls = null;
		try {
			urls = Files.readAllLines(new File(IndexCONSTANTS.URLS_LIST_FILE).toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Error reading urls file: " + IndexCONSTANTS.URLS_LIST_FILE);
			return;
		}
		for (String url : urls) {
			newspaperCrawlOneUrl(url);
		}
		System.out.println("Finished crawling newspaper article urls from file: " + IndexCONSTANTS.URLS_LIST_FILE);
	}

	@RequestMapping("/newspaperCrawlOne")
	public void newspaperCrawlOneUrl(@RequestParam(value="url", required=false, defaultValue = "") String url){

		log.info("Requested to crawl newspaper article with url: " + url);
		System.out.println("Requested to crawl newspaper article with url: " + url);

		if(!(url.equals(""))){
			IndexHandler ih = new IndexHandler();
			ih.openWriter();

			Document document = null;
			try {
				document = Jsoup.connect(url).get();
			} catch (Exception e) {
				System.out.println("Error connecting to url: " + url);
				System.out.println(e.getMessage());
				ih.closeWriter();
				ih.close();
				return;
			}
			Elements mainClasses = document.getElementsByClass("main-article");
			int mainSize = mainClasses.size();
			if (mainSize == 0) {
						/*System.out.println(url);
						System.out.println("     Title: " + rssTitle);
						System.out.println("     PAID CONTENT");*/

			} else {
				Element mainClass = mainClasses.get(0);
				Elements header = mainClass.getElementsByTag("header");
				String title = header.get(0).getElementsByTag("h2").text();
				Elements p = mainClass.getElementsByTag("p");
				String description = p.get(0).text();
				Elements dateDiv = document.getElementsByClass("artikelhead");
				String date = dateDiv.get(0).getElementsByTag("span").get(0).text();
				Elements contentDivElements = document.getElementsByClass("divcontent");

				// handle for paid content that cannot be viewed
				if(contentDivElements.size() == 0){
					System.out.println("Not Indexed, paid content in url: " + url);
					return;
				}

				Element contentDiv = contentDivElements.get(0).children().get(1);
				String content = "";
				String prefix = "";
				for (Element paragraph : contentDiv.children()) {
//							content += paragraph.text();
					String paragraphText = paragraph.text();
					String paragraphWithSentenceBreaks = ParsingNewsArticleFunctions.insertSentenceBreaksToParagraph(
							paragraphText, IndexCONSTANTS.NEWSPAPER_LANGUAGE);
					content += prefix + paragraphWithSentenceBreaks;
					prefix = "\n\n";
				}

				// cut content after the last full stop
				int fullstopLastIndex = content.lastIndexOf(".");
				if((fullstopLastIndex+1) != content.length())
					if(content.charAt(fullstopLastIndex+1)==' ' || content.charAt(fullstopLastIndex+1)=='/')
						content = content.substring(0,fullstopLastIndex + 1);
					// else, we suppose that the last fullstop is part of a url or an email
					// we inspect the end of the sentence in the '. ' string
					else
						content = content.substring(0,content.lastIndexOf(". ") + 1);

				// parse content according to upf instructions
				content = ParsingNewsArticleFunctions.refineNewspaperArticleText(content);

				JSONObject jsonObj = new JSONObject();
				jsonObj.put(IndexCONSTANTS.FIELD_URL, url);
				jsonObj.put(IndexCONSTANTS.FIELD_TITLE, title);
				jsonObj.put(IndexCONSTANTS.FIELD_DESCRIPTION, description);
				jsonObj.put(IndexCONSTANTS.FIELD_DATE_STRING, date);
				jsonObj.put(IndexCONSTANTS.FIELD_CONTENT, content);
				jsonObj.put(IndexCONSTANTS.FIELD_MEDIUM, IndexCONSTANTS.MEDIUM_NEWSPAPER);
				ih.indexNewspaperJson(jsonObj);


			}
			ih.closeWriter();
			ih.close();
			System.out.println("Finished crawling newspaper article with url: " + url);
			System.out.println();
		}
	}

	@RequestMapping("/getCrawledNewspaperUrls")
	public String getCrawledNewspaperUrls(){
		log.info("Requested crawled newspaper urls");
		System.out.println("Requested crawled newspaper urls");
		IndexHandler ih = new IndexHandler();
		List<String> urls = ih.getAllNewspaperUrls();
		String response = "";
		for (String url: urls) {
			response += url +"<br>";
		}
		return response;
	}

	@RequestMapping(value = "/getLatestArticleUrls", produces = "application/json")
	public String getLatestArticleUrls(@RequestParam(value="maxArticles", required=false, defaultValue = "10") int maxArticles){
		log.info("Requested latest newspaper articles urls");
		System.out.println("Requested latest newspaper articles urls");
		IndexHandler ih = new IndexHandler();
		String response = ih.getLatestNewspaperArticles(maxArticles);
		return response;
	}

	@RequestMapping(value = "/recipe", produces = "text/plain")
	@ResponseBody
	public String recipeResponse(@RequestParam(value="query", required=false, defaultValue = "") String query,
								 @RequestParam(value="language", required=false, defaultValue = "de") String language){

		log.info("Requested Recipe with query: \"" + query + "\" ");
		System.out.println("Requested Recipe with query: \"" + query + "\" ");

		RecipesRetriever rr = new RecipesRetriever(language);
		String response = "";
		if (query.equals("")) {
			response = rr.randomRecipeQuery();
		}
		else{
			response = rr.makeRecipeQuery(query);
		}
		rr.close();
		return response;
	}

	@RequestMapping(value = "/newspaperTR", produces = "text/plain")
	@ResponseBody
	public String newspaperResponseTR(@RequestParam(value="query", required=false, defaultValue = "") String query){

		log.info("Requested Newspaper (TR) with query: \"" + query + "\" ");
		System.out.println("Requested Newspaper (TR) with query: \"" + query + "\" ");

		if (query.equals("")) {
			return "";
		}
		else{
			NewspapersRetriever nr = new NewspapersRetriever("tr");
			String response = nr.makeNewspaperQuery(query);
			nr.close();
			return response;
		}
	}

//	@RequestMapping("/createInputKB")
//	@ResponseBody
	public String createInputKB(){

		long start = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();
		boolean success = false;

		// initialize knowledge base input generator, generates query object in this procedure
		KBHandler kbh = new KBHandler();

		// if query is read successfully from file
		if(kbh.getQueries()!=null)
			success = kbh.generateInputKB();
		else
			sb.append("Empty query. Cannot start pipeline!!!\n");

		// if pipeline executed successfully and there is input to insert in knowledge base
		if(success)
			sb.append("Success: Input file for KB is updated!!!\n");
		else
			sb.append("Failure: Input file for KB is NOT updated!!!\n");

		long elapsed = System.currentTimeMillis() - start;
		sb.append("Elapsed time (milliseconds): " + elapsed);

		String response = sb.toString();
		return response;
	}

	@RequestMapping(value = "/getInputKB", produces = "application/json")
	@ResponseBody
	public String getInputKB(){
		System.out.println("Requested knowledge base input...");
		String kbInput = FileFunctions.readInputFile(KBConstants.inputKBFileName);
		return kbInput;
	}

	@RequestMapping(value = "/updateKB")
	@ResponseBody
	public String updateKB(){
		System.out.println("Requested update of the knowledge base...");
		String kbInput = FileFunctions.readInputFile(KBConstants.inputKBFileName);
		String response;
		boolean success = KBHandler.updateKB(kbInput);
		if (success)
			response = "KB updated successfully!!!";
		else
			response = "Error while updating KB!!!";
		return response;
	}

	/**
	 * if a keyword is not added, topic detection will be made using date
	 * @param language
	 * @param days
	 * @param keyword
	 * @param keepF
	 * @return
	 */
	@RequestMapping(value = "/topicDetection", produces = "application/json")
	@ResponseBody
	public String topicDetection(@RequestParam(value="language", required=false, defaultValue = "de") String language,
								 @RequestParam(value="days", required=false, defaultValue = "2") int days,
								 @RequestParam(value="keyword", required=false, defaultValue = "") String keyword,
								 @RequestParam(value="keepF", required=false, defaultValue = "false") boolean keepF){
		System.out.println();
		System.out.println("Requested topic detection...");
		System.out.println("Language: " + language);
		TopicDetectionService tds = new TopicDetectionService();
		String response = "";
		if(keyword.equals("")){
			response = tds.detector(keepF, language, days);
		}
		else{
			response = tds.detectorUsingKeyword(keepF, language, keyword);
		}
		return response;
	}

	@RequestMapping(value = "/topicDetectionUrl", produces = "text/plain")
	@ResponseBody
	public String topicDetectionUrl(@RequestParam(value="language", required=false, defaultValue = "de") String language,
									@RequestParam(value="keyword", required=false, defaultValue = "") String keyword,
								 	@RequestParam(value="days", required=false, defaultValue = "2") int days){
		System.out.println();
		System.out.println("Requested topic detection URL...");
		System.out.println("Language: " + language);
		String response = "";
		if(keyword.equals(""))
			response = DefaultValues.baseURL + "/topicDetectionHTML?language=" + language + "&days=" + days;
		else
			response = DefaultValues.baseURL + "/topicDetectionHTML?language=" + language + "&keyword=" + keyword;
		return response;
	}

	@RequestMapping(value = "/topicDetectionUsingResults", produces = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String topicDetectionUsingResults(@RequestParam(value="language", required=false, defaultValue = "de") String language,
								 @RequestParam(value="ids") String ids,
								 @RequestParam(value="keepF", required=false, defaultValue = "false") boolean keepF){
		System.out.println();
		System.out.println("Requested topic detection...");
		System.out.println("Language: " + language);
		System.out.println("ids: " + ids);
		TopicDetectionService tds = new TopicDetectionService();
		String response = tds.detectorUsingIds(keepF, language, ids);
		return response;
	}

	@RequestMapping(value = "/test", produces = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public String test(@RequestParam(value="language", required=false, defaultValue = "de") String language,
											 @RequestParam(value="ids") String ids,
											 @RequestParam(value="keepF", required=false, defaultValue = "false") boolean keepF){
		System.out.println();
		System.out.println("Requested topic detection...");
		System.out.println("Language: " + language);
		System.out.println(ids);
		return ids;
	}

	@RequestMapping("/getTweet")
	@ResponseBody
	public String getTweet(@RequestParam(value="id") String id,
						   @RequestParam(value="language", required=false, defaultValue = "de") String language){
		System.out.println();
		System.out.println("Requested tweet with id: " + id);
		System.out.println("Language: " + language);
		String twitterID = "Twitter#" + id;
		GettingDataFromMongo gdm = new GettingDataFromMongo(language);
		String response = gdm.getTweet(twitterID);
		gdm.closeConnectionToMongo();
		return response;
	}

	@RequestMapping(value = "/updateTweetCategories", produces = "text/plain")
	@ResponseBody
	public String updateTweetCategories(){System.out.println();
		System.out.println("Tweet categorization start");
		String[] languages = {"de", "tr"};
		String response = "";
		for (String lng: languages){
			CategoryClassificationHandler cch = new CategoryClassificationHandler(lng);
			boolean success = cch.updateTweetCategories();
			if(success)
				response += "Tweet categorization succeeded on language: " + lng + "\n";
			else
				response += "Tweet categorization failed on language: " + lng + "\n";
		}
		return response;
	}

	@RequestMapping("/getClosestWords")
	@ResponseBody
	public String getClosestWords(@RequestParam(value="query", required=false, defaultValue = "") String query,
						   @RequestParam(value="num", required=false, defaultValue = "10") int num){
		System.out.println("Requested closest words for query: " + query);
		Word2VecHandler w2v = new Word2VecHandler();
		w2v.loadModel("embeddings/w2v_model_es");
		String reformedQuery = reformQuery(query);
		List<String> closestWords = w2v.getClosestWordsUsingTokenization(reformedQuery, num);
		Gson gson = new Gson();
		JsonObject json = new JsonObject();
		json.addProperty("tokens", reformedQuery);
		json.addProperty("closestWords", closestWords.toString());
		return gson.toJson(closestWords);
	}

	@RequestMapping(value = "/getClosestWordsEmbed", produces = "application/json")
	@ResponseBody
	public String getClosestWordsEmbed(@RequestParam(value="query", required=false, defaultValue = "") String query,
								  @RequestParam(value="num", required=false, defaultValue = "10") int num){
		System.out.println("Requested closest words (embed) for query: " + query);
		Word2VecHandler w2v = new Word2VecHandler();
		w2v.loadModel("embeddings/w2v_model_es");
		w2v.formVocabEmbeddingMatrix();
		String reformedQuery = reformQuery(query);
		List<String> closestWords = w2v.getClosestWordsUsingEmbeddingMatrix(reformedQuery, num);
		Gson gson = new Gson();
		JsonObject json = new JsonObject();
		json.addProperty("tokens", reformedQuery);
		json.addProperty("closestWords", closestWords.toString());
		return gson.toJson(json);
	}

	private String reformQuery(String query){
		String reformedQuery = "";
		String prefix = "";
		Analyzer analyzer = new CustomStopAnalyzerSpanish();
		List<String> tokens = ParsingFunctions.getTokens(analyzer,query);
		for (String token: tokens){
			reformedQuery += prefix + token;
			prefix = " ";
		}
		System.out.println("Reformed query: " + reformedQuery);
		return reformedQuery;
	}

}
