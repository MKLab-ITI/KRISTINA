package gr.iti.mklab.kindex;

import com.kohlschutter.boilerpipe.BoilerpipeExtractor;
import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;
import gr.iti.mklab.kindex.KMongoDB.MongoDBHandler;
import gr.iti.mklab.kindex.KMongoDB.MongoNutchConstants;
import gr.iti.mklab.kindex.KMongoDB.MongoSimmoContentConstants;
import gr.iti.mklab.kindex.KMongoDB.MorphiaConstants;
import gr.iti.mklab.kindex.Scrapping.ScrappingConstants;
import gr.iti.mklab.kindex.Scrapping.ScrappingHandler;
import gr.iti.mklab.simmo.core.documents.PDF;
import gr.iti.mklab.simmo.core.documents.Post;
import gr.iti.mklab.simmo.core.documents.Webpage;
import gr.iti.mklab.simmo.core.items.Text;
import gr.iti.mklab.simmo.core.morphia.DAOManager;
import gr.iti.mklab.simmo.core.morphia.MorphiaManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Class for handling scraping.<br>
 * Scraping starts by reading unique urls from 
 * {@value MongoNutchConstants#DB_NAME}.{@value MongoNutchConstants#COLLECTION_NAME} <br>
 * and store website content to {@value MongoSimmoContentConstants#DB_NAME} <br>
 * in Simmo based Structure
 *
 */
public class Scraper {

	private String crawlingId=null;
	MongoDBHandler mDBh = null;


	/**
	 * Constructor without parameters. Use when there is no crawlingID
	 */
	public Scraper() {

		super();
		MorphiaManager.setup(MorphiaConstants.HOST, MorphiaConstants.PORT);
		this.mDBh= new MongoDBHandler();
	}

	/**
	 * Constructor with parameters. Use to scrape for unique crawling
	 * @param cId String. CrawlingID
	 */
	public Scraper(String cId) {

		crawlingId=cId;
		MorphiaManager.setup(MorphiaConstants.HOST, MorphiaConstants.PORT);
		mDBh= new MongoDBHandler();
	}


	/**
	 * Do scrapipng.
	 * Read urls from MongoDB: {@value MongoNutchConstants#DB_NAME}.{@value MongoNutchConstants#COLLECTION_NAME} <br>
	 * Stores content in Simmo based Structure in {@value MongoSimmoContentConstants#DB_NAME} <br>
	 * Find More for Simmo here: <a href="http://github.com/MKLab-ITI/simmo">http://github.com/MKLab-ITI/simmo</a>
	 *
	 *@return Nothing
	 */
	public void doScraping(){

		Date start_time = new Date();

		URL url = null;

		long numberOfResults;
		if (crawlingId == null) {
			numberOfResults = mDBh.srcRead();
			System.out.println("Scraping without crawlingId");
			System.out.println(start_time.toString());
			System.out.println("Scraping "+ numberOfResults + " urls");
		}
		else{
			numberOfResults = mDBh.srcRead(crawlingId);
			System.out.println("Scraping with crawlingId: "+crawlingId);
			System.out.println(start_time.toString());
			System.out.println("Scraping "+ numberOfResults + " urls");
		}
		int i =1;
		while (mDBh.srcIterHasNext()){
			try {
				
				//get next url from from MongoDB
				String json = mDBh.srcIterNext();
				JSONObject jsonObj = new JSONObject(json);
				String strUrl = jsonObj.getString("url").replaceAll(" ","%20");
				url = new URL(strUrl);

				//find out the type of url content [Forum | Website], to assign url to proper extractor
				//if a url is annotated as forum, it will be extracted by forum extractor
				//if a page is annotated as forum, but the content is not a discussion (ex. the page with terms of use),
				//url will be assigned to forumExtractor, and extractor will not extract any information from that page

				boolean done=false;
				String type = "NotValid	Url";
				if (validateUrl(url)) {
					type = ScrappingHandler.whatTypeIsIt(url);
					if (type.equals(ScrappingConstants.PDF)) {
						done = extractTextFromPDF(url);
					} else if (type.equals(ScrappingConstants.FORUM)) {
						done = extractPostsFromForum(url, mDBh);
					} else if (type.equals(ScrappingConstants.WEBSITE)) {
						done = extractArticleFromUrl(url);
					}
				}
				String done_string = (done) ? "DONE" : "NOT DONE";
				System.out.println(i+"/"+numberOfResults+" Scraping ["+type+"] ("+done_string + ") " + url.toString());
				i++;

			} catch (IOException e) {
				System.err.println("KIndex :: Scraper.doScraping() General Exception in url: "+url);
				e.printStackTrace();
			}
		}
		
		//close connection for Simmo Storing
		MorphiaManager.tearDown();

		//Close MongoDBHandler
		mDBh.close();

		System.out.println("Scraping started at: " + start_time.toString());
		System.out.println("Scraping ended at: " + (new Date()).toString());
	}

	/**
	 * Scrape from list .
	 * Read urls from dir: 'input/ScraperList.txt' <br>
	 * In this file all urls must be separated by newLine character
	 * Stores content in Simmo based Structure in {@value MongoSimmoContentConstants#DB_NAME} <br>
	 * Find More for Simmo here: <a href="http://github.com/MKLab-ITI/simmo">http://github.com/MKLab-ITI/simmo</a>
	 *
	 */
	public void scrapeFromList() {

		//System.out.println("scrapeFromList Function");
		Date start_time = new Date();

		File url_list_file = new File(ScrappingConstants.URL_LIST_FILE);
		//System.out.println("Url List: "+url_list_file.getAbsolutePath());
		try {

			LineNumberReader  lnr = new LineNumberReader(new FileReader(url_list_file));
			lnr.skip(Long.MAX_VALUE);
			int numberOfResults = (lnr.getLineNumber() + 1);
			System.out.println(); //Add 1 because line index starts at 0
			System.out.println("Scraping from List");
			System.out.println(start_time.toString());
			System.out.println("Scraping "+ numberOfResults + " urls");
			lnr.close();

			BufferedReader br = new BufferedReader(new FileReader(url_list_file));

			String line = null;
			int i =1;
			while ((line = br.readLine()) != null) {
				URL url = new URL(line);

				//find out the type of url content [Forum | Website], to assign url to proper extractor
				//if a url is annotated as forum, it will be extracted by forum extractor
				//if a page is annotated as forum, but the content is not a discussion (ex. the page with terms of use),
				//url will be assigned to forumExtractor, and extractor will not extract any information from that page

				boolean done=false;
				String type = "NotValid	Url";
				if (validateUrl(url)) {
					type = ScrappingHandler.whatTypeIsIt(url);
					if (type.equals(ScrappingConstants.PDF)) {
						done = extractTextFromPDF(url);
					} else if (type.equals(ScrappingConstants.FORUM)) {
						done = extractPostsFromForum(url, mDBh);
					} else if (type.equals(ScrappingConstants.WEBSITE)) {
						done = extractArticleFromUrl(url);
					}
				}
				String done_string = (done) ? "DONE" : "NOT DONE";
				System.out.println(i+"/"+numberOfResults+" Scraping ["+type+"] ("+done_string + ") " + url.toString());
				i++;
			}

			br.close();
		} catch ( IOException e) {
			System.err.println("KIndex :: Scraper.scrapeFromList() IO Exception ");
			e.printStackTrace();
		}

	}



	/**
	 * Extracts text from pdf and stores it to SIMMO DB as PDF
	 * @param url
	 * @return
	 */
	private boolean extractTextFromPDF(URL url) {

		if (!mDBh.urlExistsInPDF(url)) {
			String url_string = url.toString();
			if (!url_string.endsWith("pdf")){
				return false;
			}

			try {
				PDDocument pdDoc = PDDocument.load(url.openStream());

				//extract the pdf to HTML and then to string in order to keep formatting (for changing lines n stuff).
				PDFText2HTML converter = new PDFText2HTML();
				String HTMLText = converter.getText(pdDoc);


				PDF pdf = new PDF();
				pdf.setUrl(url.toString());

				Calendar cal = Calendar.getInstance();
				pdf.setCreationDate(cal.getTime());

				Text txt = new Text();
				txt.setContent(HTMLText);
				txt.setTextType(Text.TEXT_TYPE.HTML);

				pdf.addItem(txt);

				//Store Webpage to db
				DAOManager manager = new DAOManager(MongoDBHandler.getTrgDBName());
				manager.savePDF(pdf);
				pdDoc.close();

				return true;

			} catch (IOException e) {
				System.out.println("KIndex::Scraper.extractTextFromPDF() Could not open Stream for url");
				e.printStackTrace();
				return false;
			}
		}
		System.out.println("Already exist in database: " + url.toString());
		return false;
	}


	/**
	 * Stores all discussion replies in simmo as posts
	 * @param url url to store the replies
	 * @param mDBh
	 * @return true is extract is completed, false otherwise
	 */
	private static boolean extractPostsFromForum(URL url, MongoDBHandler mDBh) {

		// url containing # is internal page link. In order not to process multiple times the same url, ignore these urls
		if (ScrappingHandler.isForumExtractable(url) && (!url.toString().contains("#"))){

			//Every reply in a forum thread is represented as post in SIMMO structure linked to a text


			ArrayList<String> replies = getAllForumPostsReplies(url);

			if (replies.size() == 0){
				return false;
			}

			int i=1;
			for (String reply : replies) {

				//for every post url there are multiple replies.
				//identifier for every reply is url and the counter i
				//if there is already a post with these identifiers in DB, don't add the reply

				if (!mDBh.postExists(url, String.valueOf(i))){

					Post post = new Post();
					post.setUrl(url.toString());
					post.setLabel(String.valueOf(i));
					post.setType(ScrappingConstants.FORUM);

					Calendar cal = Calendar.getInstance();
					post.setCreationDate(cal.getTime());

					Text txt = new Text();
					txt.setContent(reply);
					txt.setTextType(Text.TEXT_TYPE.TXT);

					post.addItem(txt);

					//Store Webpage to db
					DAOManager manager = new DAOManager(MongoDBHandler.getTrgDBName());
					manager.savePost(post);

					System.out.println(url.toString()+" #" + i +" Added!");
				}
				else{
					System.out.println(url.toString()+" #" + i +" already exists");
				}

				i++;
			}
			return true;
		}
		else{
			return false;
		}

	}

	/**
	 * returns a list with a discussion page's forum replies in String
	 * @param url url to extract the replies
	 * @return a list with the replies
	 */
	private static ArrayList<String> getAllForumPostsReplies(URL url) {

		ArrayList<String> replies = new ArrayList<String>();

		String replyHTMLElement = ScrappingHandler.getForum(url).getReply_element();
		String replyQuoteHTMLElement = ScrappingHandler.getForum(url).getQuote_element();

		if (replyHTMLElement != null && (!replyHTMLElement.equals(""))){
			Document doc = null;
			try {
				doc = Jsoup.connect(url.toString()).timeout(10*1000).get(); //set timeout 10s
			} catch (IOException e) {
				System.out.println("KIndex :: Scrapper.getAllForumPostsReplies() JSOUP Could NOT get url");
				System.out.println("url: " + url.toString());
				e.printStackTrace();
				return replies;
			}

			//first remove all quotes from doc
			doc.select(replyQuoteHTMLElement).remove();

			//get all replies
			Elements posts = doc.select(replyHTMLElement);
			//int i =0;
			for (Element p : posts) {

				replies.add(p.text());

			}
		}
		return replies;
	}

	/**
	 * Store extracted article from url as webpage/text in Simmo DB
	 * @param url Url to extract
	 */
	private boolean extractArticleFromUrl(URL url) {

		if (!mDBh.urlExistsInWebsite(url)) {
			String content = null;

			//REMEMBER TO: when switching content format text <-> HTML, change the textType in SIMMO Document

			//Extract the content as plain text
//			try {
//				final InputSource is = HTMLFetcher.fetch(url).toInputSource();
//				final BoilerpipeSAXInput in;
//				in = new BoilerpipeSAXInput(is);
//				final TextDocument doc = in.getTextDocument();
//				content = ArticleExtractor.INSTANCE.getText(doc);
//			} catch (SAXException e) {
//				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() SAXException for url: " + url.toString());
//				e.printStackTrace();
//				return false;
//			} catch (BoilerpipeProcessingException e) {
//				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() BoilerpipeProcessingException for url: " + url.toString());
//				e.printStackTrace();
//				return false;
//			} catch (IOException e) {
//				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() IOException for url: " + url.toString());
//				e.printStackTrace();
//				return false;
//			}

			//extract the content as HTML
			final BoilerpipeExtractor extractor = CommonExtractors.DEFAULT_EXTRACTOR;
			final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
			try {
				content = hh.process(url, extractor);
			} catch (IOException e) {
				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() IOException for url: " + url.toString());
				e.printStackTrace();
				return false;
			} catch (BoilerpipeProcessingException e) {
				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() BoilerpipeProcessingException for url: " + url.toString());
				e.printStackTrace();
				return false;
			} catch (SAXException e) {
				System.out.println("KIndex :: Scrapper.extractArticleFromUrl() SAXException for url: " + url.toString());
				e.printStackTrace();
				return false;
			}


			//checking if website already exist

			//Storing content in Simmo
			//Create a Simmo Webpage
			Webpage wp = new Webpage();
			wp.setUrl(url.toString());

			Calendar cal = Calendar.getInstance();
			wp.setCreationDate(cal.getTime());

			//Create a Simmo Text
			Text txt = new Text();
			txt.setContent(content);
			txt.setTextType(Text.TEXT_TYPE.HTML);
//			txt.setTextType(Text.TEXT_TYPE.TXT);

			//Add Text to Webpage
			wp.addItem(txt);

			//Store Webpage to db
			DAOManager manager = new DAOManager(MongoDBHandler.getTrgDBName());
			manager.saveWebpage(wp);


			//Politeness policy of one second.
			try {
				Thread.sleep(1000);                 //1000 milliseconds is one second.
			} catch (InterruptedException ex) {
				//Thread.currentThread().interrupt();
				System.out.println("Could not make the thread sleep");
			}

			return true;
		}
		System.out.println("Already exist in database: " + url.toString());
		return false;
	}



	/**
	 * Function for closing scraper.
	 */
	public void close() {
		System.out.println("Scraper terminated");
	}


	private boolean validateUrl(URL url) {

		String st_url = url.toString();
		if ( 	(!st_url.startsWith("http")) ||
				st_url.endsWith(".png") || st_url.endsWith(".PNG")||
				st_url.endsWith(".jpg")|| st_url.endsWith(".JPG")||
				st_url.endsWith(".jpeg")|| st_url.endsWith(".JPEG")||
				st_url.endsWith(".css")|| st_url.endsWith(".CSS")||
				st_url.endsWith(".js")|| st_url.endsWith(".JS")||
				st_url.endsWith(".gif")|| st_url.endsWith(".GIF")||
				st_url.endsWith(".ico")|| st_url.endsWith(".ICO")||
				st_url.endsWith(".zip")|| st_url.endsWith(".ZIP")||
				st_url.endsWith(".ppt")|| st_url.endsWith(".PPT")||
				st_url.endsWith(".mpg")|| st_url.endsWith(".MPG")||
				st_url.endsWith(".xls")|| st_url.endsWith(".XLS")||
				st_url.endsWith(".exe")|| st_url.endsWith(".EXE")||
				st_url.contains("/../")){
			return false;
		}
		return true;
	}

}
