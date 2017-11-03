package gr.iti.mklab.kindex;


import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import gr.iti.mklab.kindex.Indexing.IndexWebAP;
import gr.iti.mklab.kindex.RelExt.RelExt;
import gr.iti.mklab.kindex.TextAnnotator.Pipeline;
import gr.iti.mklab.kindex.Version.Versioning;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class InputHandler {

	String type = "";
	Indexer inxh = null;
	PassageIndexer pinxh = null;
	Scraper sc = null;
	Versioning version = new Versioning();

	/**
	 * Constructor used when using console mode. No arguments provided
	 *
	 * @param inputType String. Type of Input (ex. "console")
	 */
	public InputHandler( String inputType) {
		type = inputType;
		if (inputType.equals("console")){
			handleConsole();
		}
	}
	/**
	 * Constructor used when using Arguments mode. Arguments are provided as paremeter
	 *
	 * @param inputType String. Type of Input (ex. "arguments")
	 * @param args ArrayList<String>. Arguments provided in calling KIndex
	 */
	public InputHandler(String inputType, ArrayList<String> args) {
		type = inputType;
		if (inputType.equals("arguments")){
			handleArguments(args);
		}
	}

	/**
	 * Function for handling Console input and output.
	 */
	private void handleConsole() {
		Scanner scanIn = new Scanner(System.in);

		System.out.println("Welcome to KIndex. ");
		System.out.println("Available handlers are: \"Scraper\" and \"Indexer\"");
		System.out.println("Type \"relext\" for relation extraction");
		System.out.println("If you need more help, type \"help\".");
		System.out.println(" ");

		System.out.print(">>KIndex> ");
		String command = scanIn.nextLine();
		command = command.toLowerCase();

		while (!command.equals("exit")){
			if (command.equals("indexer")){
				handleIndexerConsole(scanIn);
			}
			else if (command.equals("passageindexer")){
				handlePassageIndexerConsole(scanIn);
			}
			else if (command.equals("webappassageindex")){
				webAPIndex();
			}
			else if (command.equals("scraper")){
				handleScraperConsole(scanIn);
			}
			else if (command.equals("relext")){
				extractRelations();
			}
			else if (command.equals("nlp.ne")){
				nlp("ne", true);
			}
			else if (command.equals("nlp.onlyconcept")){
				nlp("onlyconcept", true);
			}
			else if (command.equals("nlp.concept")){
				nlp("concept", true);
			}
			else if (command.equals("nlp.debug")){
				nlp("", true);
			}
			else if (command.equals("nlp")){
				nlp("", true);
			}
			else if (command.equals("kbfeed")){
				new java.util.Timer().schedule(
					new java.util.TimerTask() {
						@Override
						public void run() {
							scheduledKBFeed();
						}
					},
					1000, //delay in milliseconds
					1000*60*60*12 //period in milliseconds
				);
			}
			else if (command.equals("version")){
				version.printCurrentVersion();
			}
			else if (command.equals("versionall")){
				version.printVersionAll();
			}
			else if (command.equals("help")){
				System.out.println("-----------------------------------------------");
				System.out.println("HELP");
				System.out.println("  ");
				System.out.println("- Type \"indexer \" to access index.");
				System.out.println("       ");
				System.out.println("- Type \"PassageIndexer \" to access passage index.");
				System.out.println("       ");
				System.out.println("- Type \"WebAPPassageIndex \" to index the WebAP dataset in passages.");
				System.out.println("       ");
				System.out.println("- Type \"scraper\" to access scraper.");
				System.out.println("       ");
				System.out.println("- Type \"relext\" to Extract Relations from \"input/text.txt\". Output at \"output\" folder");
				System.out.println("       ");
				System.out.println("- Type \"nlp\" to execute Concept, NE and POS analysis in  \"input/TextAnnotationInput\" folder. Output at \"output/nlp\" folder");
				System.out.println("- Type \"nlp.ne\" to execute only NE and POS analysis and \"nlp.concept\" to execute Concept analysis only ");
				System.out.println("- Type \"nlp.onlyconcept\" to extract only <concept> tags from Concept analysis");
				System.out.println("       ");
				System.out.println("- Type \"lexparser\" to run lexicalized parser for the files in the lexParser folder");
				System.out.println("       ");
				System.out.println("- Type \"kbfeed\" to start kbfeeding routine. ");
				System.out.println("       ");
				System.out.println("- Type \"version\" to see the current version of KIndex");
				System.out.println("       ");
				System.out.println("- Type \"versionAll\" to see the current and older versions of KIndex");
				System.out.println("       ");
				System.out.println("- Type \"exit\" to exit KIndex");
				System.out.println("       ");
				System.out.println("-----------------------------------------------");
			}
			else if(command.equals("lexparser")){
				lexParse();
			}
			else{
				System.out.println("Command \""+command+"\" not found");
				System.out.println("Type \"help\" to see options");
			}

			System.out.print(">>KIndex> ");
			command = scanIn.nextLine();
			command = command.toLowerCase();
		}
	}

	private void webAPIndex() {
		System.out.println("Input handler:: WebAP Indexing started!");
		IndexWebAP iWebApp = new IndexWebAP();
		iWebApp.openWriters();
		iWebApp.doIndexing();
		iWebApp.closeWriters();
		System.out.println("Input handler:: WebAP Indexing finished! ");
	}


	/**
	 * Function for handling Console input and output when Indexer is selected.
	 */
	private  void handleIndexerConsole(Scanner scanIn) {

		System.out.println("Welcome to KIndex Indexer. ");
		System.out.println("First you have to \"start\" a indexer and then you can \"insert\", \"query\", etc ");
		System.out.println("If you need more help, type \"help\".");
		System.out.println(" ");

		System.out.print(">>KIndex.Indexer> ");
		String command = scanIn.nextLine();
		command = command.toLowerCase();
		while (!command.equals("exit")) {

			if (command.startsWith("start")) {
				if (command.split(" ").length > 1) {
					indexerCommands("indexer."+command.replace(" ","."));
				}
				else{
					indexerCommands("indexer."+command);
				}
			}
			else if (command.startsWith("index")){

				//Twiter: there is no meaning of indexing tweets, because they are being indexed
				//during the retrieval, at twiter stream

				if (inxh == null){
					System.out.println("No Index Started. You have to start Index first. Type help for more");
				}
				else{
					String[] commands = command.split(" ");
					if (commands.length>1) {
						if (commands[1].equals("website")) {
							indexerCommands("indexer.index.website");
						}
						else if (commands[1].equals("pdf")) {
							indexerCommands("indexer.index.pdf");
						}
						else if (commands[1].equals("post")) {
							indexerCommands("indexer.index.post");
						}
						else if (commands[1].equals("kbtexts")) {
							indexerCommands("indexer.index.kb");
						}
						else{
							System.out.println("No valid index functionality. Type help for more");
						}
					}
					else{
						System.out.println("No valid index functionality. Type help for more");
					}
				}
			}

			/*else if (command.startsWith("querytwiter")){
				indexerCommands("indexer."+command.replace(" ","."));
			}*/
			else if (command.startsWith("query")){
				if (inxh == null){
					System.out.println("No Index Started. You have to start Index first. Type help for more");
				}
				else {
					String[] commands = command.split(" ");
					if (commands.length>1) {
						indexerCommands("indexer."+command.replace(" ","."));
						/*if (commands[1].equals("website")) {
							indexerCommands("indexer."+command.replace(" ","."));
						} else if (commands[1].equals("twiter")) {
							indexerCommands("indexer.query.twiter");
						}
						else if (commands[1].equals("forum")) {
							indexerCommands("indexer.query.forum");
						}
						else if (commands[1].equals("pdf")) {
							indexerCommands("indexer.query.pdf");
						}
						else{
							indexerCommands("indexer.query");
						}*/
					}
					else{
						System.out.println("Set at least one term in query. Type help for more");
					}
					//indexerCommands("indexer."+command.replace(" ","."));
				}
			}
			else if (command.startsWith("starttwiterstream")){
				indexerCommands("indexer."+command.replace(" ","."));
			}
			else if (command.startsWith("delete")){
				if (inxh == null){
					System.out.println("No Index Started. You have to start Index first. Type help for more");
				}
				else {
					indexerCommands("indexer."+command.replace(" ","."));
				}
			}
			else if (command.startsWith("close")){
				if (inxh == null){
					System.out.println("No Index Started. You have to start Index first. Type help for more");
				}
				else {
					indexerCommands("indexer."+command.replace(" ","."));
				}
			}

			else if(command.startsWith("help")){
				System.out.println("-----------------------------------------------");
				System.out.println("HELP");
				System.out.println("  ");
				System.out.println("- Type \"start dir1\" to start index in \"dir1\".  If \"dir1\" does not exist, it will be created. dir1 can be full path (ex. D:/dir1/test) of relative path (ex. test1)");
				System.out.println("       ");
				System.out.println("- Type \"start\" to start default index.");
				System.out.println("       ");
				System.out.println("- Type \"startTwiterStream\" to start the twiter stream manager. ");
				System.out.println("       ");
				System.out.println("- Type \"delete\" to delete opened Indexer");
				System.out.println("       ");
				System.out.println("- Type \"close\" to close opened Indexer");
				System.out.println("       ");
				System.out.println("- Type \"exit\" to exit Indexer");
				System.out.println("       ");
				System.out.println("- Type \"index website\"to index all websites in DB. If some Websites were already indexed they will not be indexed again. ");
				System.out.println("       ");
				System.out.println("- Type \"index post\"to index all posts in DB. If some posts were already indexed they will not be indexed again. ");
				System.out.println("       ");
				System.out.println("- Type \"index pdf\"to index all PDFs in DB. If some PDFs were already indexed they will not be indexed again. ");
				System.out.println("       ");
				System.out.println("- Type \"query query_terms\" to retrieve most relevant document. query_phrase: terms separeted by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query website query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query twiter query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query forum query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query pdf query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"index kbtexts\"to index all texts files in D:\\KBFeed\\input. Better start indexer in KB index dir \"D:\\KBFeed\\INDEX\". ");
				System.out.println("       ");
				System.out.println("- Type \"query kb query_terms\" to retrieve  relevant texts. query_phrase: terms separated by space. Ex: \"query kb health care problems\" ");
				System.out.println("       ");
				System.out.println("-----------------------------------------------");
			}
			else{
				System.out.println("Command \""+command+"\" not found");
				System.out.println("Type \"help\" to see options");
			}

			System.out.print(">>KIndex.Indexer> ");
			command = scanIn.nextLine();
			command = command.toLowerCase();
		}

		//set null to in when exiting Indexer
		if (inxh != null){
			inxh.close();
			inxh=null;
		}
	}


	/**
	 * Function for handling Console input and output when Passage Indexer is selected.
	 */
	private  void handlePassageIndexerConsole(Scanner scanIn) {

		System.out.println("Welcome to KIndex Passage Indexer. ");
		System.out.println("First you have to \"start\" a passage indexer.");
		System.out.println("If you need more help, type \"help\".");
		System.out.println(" ");

		System.out.print(">>KIndex.PassageIndexer> ");
		String command = scanIn.nextLine();
		command = command.toLowerCase();
		while (!command.equals("exit")) {

			if (command.startsWith("start")) {
				if (command.split(" ").length > 1) {
					passageIndexerCommands("indexer."+command.replace(" ","."));
				}
				else{
					passageIndexerCommands("indexer."+command);
				}
			}
			else if (command.startsWith("index")){

				//Twiter: there is no meaning of indexing tweets, because they are being indexed
				//during the retrieval, at twiter stream

				if (pinxh == null){
					System.out.println("No Passage Index Started. You have to start Passage Index first. Type help for more");
				}
				else{
					String[] commands = command.split(" ");
					if (commands.length>1) {
						if (commands[1].equals("website")) {
							passageIndexerCommands("indexer.index.website");
						}
						else if (commands[1].equals("pdf")) {
							passageIndexerCommands("indexer.index.pdf");
						}/*
						else if (commands[1].equals("post")) {
							passageIndexerCommands("indexer.index.post");
						}
						else if (commands[1].equals("kbtexts")) {
							passageIndexerCommands("indexer.index.kb");
						}
						else{
							System.out.println("No valid index functionality. Type help for more");
						}*/
					}
					else{
						System.out.println("No valid passage index functionality. Type help for more");
					}
				}
			}

			/*else if (command.startsWith("querytwiter")){
				indexerCommands("indexer."+command.replace(" ","."));
			}*/
			else if (command.startsWith("query")){
				if (pinxh == null){
					System.out.println("No Passage Index Started. You have to start Passage Index first. Type help for more");
				}
				else {
					String[] commands = command.split(" ");
					if (commands.length>1) {
						passageIndexerCommands("indexer."+command.replace(" ","."));
						/*if (commands[1].equals("website")) {
							indexerCommands("indexer."+command.replace(" ","."));
						} else if (commands[1].equals("twiter")) {
							indexerCommands("indexer.query.twiter");
						}
						else if (commands[1].equals("forum")) {
							indexerCommands("indexer.query.forum");
						}
						else if (commands[1].equals("pdf")) {
							indexerCommands("indexer.query.pdf");
						}
						else{
							indexerCommands("indexer.query");
						}*/
					}
					else{
						System.out.println("Set at least one term in query. Type help for more");
					}
					//indexerCommands("indexer."+command.replace(" ","."));
				}
			}/*
			else if (command.startsWith("starttwiterstream")){
				indexerCommands("indexer."+command.replace(" ","."));
			}*/
			else if (command.startsWith("delete")){
				if (pinxh == null){
					System.out.println("No Passage Index Started. You have to start Passage Index first. Type help for more");
				}
				else {
					passageIndexerCommands("indexer."+command.replace(" ","."));
				}
			}
			else if (command.startsWith("close")){
				if (pinxh == null){
					System.out.println("No Passage Index Started. You have to start Passage Index first. Type help for more");
				}
				else {
					passageIndexerCommands("indexer."+command.replace(" ","."));
				}
			}

			else if(command.startsWith("help")){
				System.out.println("-----------------------------------------------");
				System.out.println("HELP");
				System.out.println("  ");/*
				System.out.println("- Type \"start dir1\" to start index in \"dir1\".  If \"dir1\" does not exist, it will be created. dir1 can be full path (ex. D:/dir1/test) of relative path (ex. test1)");
				System.out.println("       ");*/
				System.out.println("- Type \"start\" to start default index.");
				System.out.println("       ");/*
				System.out.println("- Type \"startTwiterStream\" to start the twiter stream manager. ");
				System.out.println("       ");*/
				System.out.println("- Type \"delete\" to delete opened Indexer");
				System.out.println("       ");
				System.out.println("- Type \"close\" to close opened Indexer");
				System.out.println("       ");
				System.out.println("- Type \"exit\" to exit Indexer");
				System.out.println("       ");
				System.out.println("- Type \"index website\"to index all websites in DB. If some Websites were already indexed they will not be indexed again. ");
				System.out.println("       ");/*
				System.out.println("- Type \"index post\"to index all posts in DB. If some posts were already indexed they will not be indexed again. ");
				System.out.println("       ");*/
				System.out.println("- Type \"index pdf\"to index all PDFs in DB. If some PDFs were already indexed they will not be indexed again. ");
				System.out.println("       ");
				System.out.println("- Type \"query query_terms\" to retrieve most relevant document. query_phrase: terms separeted by space. Ex: \"query health care problems\" ");
				System.out.println("       ");/*
				System.out.println("- Type \"query website query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query twiter query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query forum query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"query pdf query_terms\" to retrieve most relevant tweets. query_phrase: terms separated by space. Ex: \"query health care problems\" ");
				System.out.println("       ");
				System.out.println("- Type \"index kbtexts\"to index all texts files in D:\\KBFeed\\input. Better start indexer in KB index dir \"D:\\KBFeed\\INDEX\". ");
				System.out.println("       ");
				System.out.println("- Type \"query kb query_terms\" to retrieve  relevant texts. query_phrase: terms separated by space. Ex: \"query kb health care problems\" ");
				System.out.println("       ");*/
				System.out.println("-----------------------------------------------");
			}
			else{
				System.out.println("Command \""+command+"\" not found");
				System.out.println("Type \"help\" to see options");
			}

			System.out.print(">>KIndex.PassageIndexer> ");
			command = scanIn.nextLine();
			command = command.toLowerCase();
		}

		//set null to in when exiting Indexer
		if (pinxh != null){
			pinxh.close();
			pinxh=null;
		}
	}

	/**
	 * Function for handling Console input and output when Scraper is selected.
	 */
	private void handleScraperConsole(Scanner scanIn) {
		System.out.println("Welcome to KIndex Scraper. ");
		System.out.println("First you have to \"start\" a scraper and then you can \"doScraping\" to scrape");
		System.out.println("If you need more help, type \"help\".");
		System.out.println(" ");

		System.out.print(">>KIndex.Scraper> ");
		String command = scanIn.nextLine();
		command = command.toLowerCase();
		while (!command.equals("exit")) {
			if (command.startsWith("start")) {
				command = command.replace(" ",".");
				scraperCommands("scraper."+command);
			}
			else if (command.startsWith("doscraping")) {
				if (sc == null) {
					System.out.println("No Sraper Started. You have to start Scraper first. Type help for more");
				} else {
					scraperCommands("scraper."+command);

				}
			}
			else if (command.startsWith("scrapefromlist")) {
				if (sc == null) {
					System.out.println("No Sraper Started. You have to start Scraper first. Type help for more");
				} else {
					scraperCommands("scraper."+command);

				}
			}
			else if (command.startsWith("close")) {
				if (sc == null) {
					System.out.println("No Sraper Started. You have to start Scraper first. Type help for more");
				} else {
					scraperCommands("scraper."+command);

				}
			}
			else if (command.startsWith("help")) {
				System.out.println("-----------------------------------------------");
				System.out.println("HELP");
				System.out.println("  ");
				System.out.println("- Type \"start \" to start scraper. ");
				System.out.println("       ");
				System.out.println("- Type \"start [crawlingId]\" to start scraper for only for [crawlingId] documents. ([crawlingId] must be numeric value) ");
				System.out.println("       ");
				System.out.println("- Type \"doScraping\" to start scraping. To dScraping, you have to start scraper first. Scraper will visit all urls in DB");
				System.out.println("       ");
				System.out.println("- Type \"scrapeFromList\" to scrape the urls in input\\ScraperList.txt. To scrapeFromList, you have to start scraper first.");
				System.out.println("       ");
				System.out.println("- Type \"close\" to close open Scraper");
				System.out.println("       ");
				System.out.println("- Type \"exit\" to exit Scraper");
				System.out.println("       ");
				System.out.println("-----------------------------------------------");
			}
			else if (!command.equals("")) {
				System.out.println("Command \"" + command + "\" not found");
				System.out.println("Type \"help\" to see options");
			}
			System.out.print(">>KIndex.Scraper> ");
			command = scanIn.nextLine();
			command = command.toLowerCase();
		}
		//In exiting KIndexScraper, close scraper
		if (sc != null){
			sc.close();
			sc = null;
		}
	}

	/**
	 * Function for handling Arguments input and output.
	 * @param args ArrayList<String>. KIndex Calling Arguments
	 */
	private void handleArguments(ArrayList<String> args) {
		for (String arg : args) {
			arg=arg.toLowerCase();
			if(arg.startsWith("indexer.")){
				indexerCommands(arg);
			}
			else if (arg.startsWith("passageindexer.")){
				scraperCommands(arg);
			}
			else if (arg.startsWith("scraper.")){
				scraperCommands(arg);
			}
			else if (arg.startsWith("relext")){
				extractRelations();
			}
			else if (arg.startsWith("nlp.ne")){
				nlp("ne", true);
			}
			else if (arg.startsWith("nlp.onlyconcept")){
				nlp("onlyconcept", true);
			}
			else if (arg.startsWith("nlp.concept")){
				nlp("concept", true);
			}
			else if (arg.startsWith("nlp.debug")){
				nlp("", true);
			}
			else if (arg.startsWith("nlp")){
				nlp("", true);
			}
			else if(arg.startsWith("lexparser")){
				lexParse();
			}
			else{
				System.out.println("Command \"" + arg + "\" not found");
			}
		}

		//do not close the console
		System.out.print(" ");
		System.out.print("Press ENTER to exit");
		(new Scanner(System.in)).nextLine();

	}

	/**
	 * Function for executing Indexer commands
	 * @param command String. Must be in format handler.command (ex. indexer.start OR indexer.start.C:/bin/example)
	  */
	private void indexerCommands(String command){
		command = command.toLowerCase();
		if (command.startsWith("indexer.")){

			String[] parts = command.split("[.]");
			String indexerCommand = parts[1];

			if (indexerCommand.equals("start")){
				System.out.println("Indexer Started");

				//if parts.length > 2, call comes with file parameter (ex start C:/bin/example)
				//otherwise just use default directory (by calling empty indexer constructor)

				if (parts.length<3){
					System.out.println("Loading default Index...");
					//Create default index
					inxh = new Indexer();
					System.out.println("Default Index Loaded!");
				}
				else{
					String param = parts[2];
					System.out.println("Loading " + param + " Index...");
					//create indexer handling index given
					inxh = new Indexer(param);
					System.out.println("Index " + param + " Loaded!");
				}
			}
			else if (indexerCommand.equals("index")){
				if (inxh == null){
					System.out.println("No Index Started. You have to start Index first.");
				}
				else{

					if (parts.length > 2) {
						String indexType = parts[2];
						if (indexType.equals("website")) {
							System.out.println("Indexing Websites from DB.....");
							inxh.index(IndexCONSTANTS.MEDIUM_WEBSITE);
							System.out.println("Website Index created.");
						}
						else if (indexType.equals("pdf")) {
							System.out.println("Indexing PDFs from DB.....");
							inxh.index(IndexCONSTANTS.MEDIUM_PDF);
							System.out.println("PDF Index created.");
						}
						else if (indexType.equals("post")) {
							System.out.println("Indexing posts from DB.....");
							inxh.index(IndexCONSTANTS.MEDIUM_POST);
							System.out.println("Posts Index created.");
						}
						else if (indexType.equals("kb")) {
							System.out.println("Indexing KB text files");
							inxh.indexKB();
							System.out.println("KB text files Index created.");
						}
						else{
							System.out.println("No valid index functionality. Type help for more");
						}
					}
					else{ //if parts.length <=2, index command comes without type (>>indexer index)
						System.out.println("No valid index functionality. Type help for more");
					}


					//in console mode there is option for replace/merge previous index if exists.
					//It is handled in handleIndexerConsole()
					//in arguments mode there is no option. Previous index  is going to be merged!
					/*System.out.println("Creating Index from DB.....");
					inxh.createIndex();
					System.out.println("Index created.");*/
				}
			}
			/*else if (indexerCommand.equals("querytwiter")){
				String query = command.replace("indexer.", "").replace("querytwiter.", "").replace(".", " ");
				System.out.println("Query: " + query);
				String response = inxh.queryTwiterIndex(query);
				if (response.equals("")){
					System.out.println("No Tweets found matching this query");

				}
				else {
					System.out.println("RESPONSE ");
					System.out.println("------------------------------------");
					System.out.println(response);
					System.out.println("------------------------------------");
				}
			}*/
			else if (indexerCommand.equals("query")){

				String response = "";
				if (parts.length > 1){
					if (parts[2].equals("website")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("website.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_WEBSITE + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_WEBSITE, query);
					}
					else if (parts[2].equals("forum")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("forum.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_POST + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_POST, query);
					}
					else if (parts[2].equals("pdf")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("pdf.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_PDF + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_PDF, query);
					}
					else if (parts[2].equals("twiter")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("twiter.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_TWEET+ " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_TWEET, query);
					}else if (parts[2].equals("kb")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("kb.", "").replace(".", " ");
						System.out.println("Searching Kb Index for \"" + query + "\"");
						response = inxh.queryKBIndex( query);
					}
					else{
						String query = command.replace("indexer.", "").replace("query.", "").replace(".", " ");
						System.out.println("Searching Everything for \"" + query + "\"");
						response = inxh.queryIndex(query);
					}

					//Print Response
					if (response.equals("")){
						System.out.println("No Document found matching this query");

					}
					else {
						System.out.println("------------------------------------");
						System.out.println(response);
						System.out.println("------------------------------------");
					}

				}
				else { // !(parts.length > 1)
					System.out.println("No terms defined for the query.");
				}

			}
			else if (indexerCommand.equals("starttwiterstream")){
				String language = parts[2];
				inxh.startTwiterStream(language);
			}
			else if (indexerCommand.equals("delete")){
				inxh.deleteIndex();
			}
			else if (indexerCommand.equals("close")){
				inxh.close();
			}
		}
		else{
			System.out.println("Command \""+command+"\" not found in Indexer.");
		}
	}

	/**
	 * Function for executing Passage Indexer commands
	 * @param command String. Must be in format handler.command (ex. indexer.start OR indexer.start.C:/bin/example)
	  */
	private void passageIndexerCommands(String command){
		command = command.toLowerCase();
		if (command.startsWith("indexer.")){

			String[] parts = command.split("[.]");
			String indexerCommand = parts[1];

			if (indexerCommand.equals("start")){
				System.out.println("Passage Indexer Started");

				//if parts.length > 2, call comes with file parameter (ex start C:/bin/example)
				//otherwise just use default directory (by calling empty indexer constructor)

				//if (parts.length<3){
					System.out.println("Loading default Index...");
					//Create default index
					pinxh = new PassageIndexer();
					System.out.println("Default Index Loaded!");
				/*}
				else{
					String param = parts[2];
					System.out.println("Loading " + param + " Index...");
					//create indexer handling index given
					inxh = new Indexer(param);
					System.out.println("Index " + param + " Loaded!");
				}*/
			}
			else if (indexerCommand.equals("index")){
				if (pinxh == null){
					System.out.println("No Passage Index Started. You have to start Index first.");
				}
				else{

					if (parts.length > 2) {
						String indexType = parts[2];
						if (indexType.equals("website")) {
							System.out.println("Indexing Websites from DB.....");
							pinxh.index(IndexCONSTANTS.MEDIUM_WEBSITE);
							System.out.println("Website Index created.");
						}
						else if (indexType.equals("pdf")) {
							System.out.println("Indexing PDFs from DB.....");
							pinxh.index(IndexCONSTANTS.MEDIUM_PDF);
							System.out.println("PDF Index created.");
						}/*
						else if (indexType.equals("post")) {
							System.out.println("Indexing posts from DB.....");
							inxh.index(IndexCONSTANTS.MEDIUM_POST);
							System.out.println("Posts Index created.");
						}
						else if (indexType.equals("kb")) {
							System.out.println("Indexing KB text files");
							inxh.indexKB();
							System.out.println("KB text files Index created.");
						}*/
						else{
							System.out.println("No valid index functionality. Type help for more");
						}
					}
					else{ //if parts.length <=2, index command comes without type (>>indexer index)
						System.out.println("No valid index functionality. Type help for more");
					}


					//in console mode there is option for replace/merge previous index if exists.
					//It is handled in handleIndexerConsole()
					//in arguments mode there is no option. Previous index  is going to be merged!
					/*System.out.println("Creating Index from DB.....");
					inxh.createIndex();
					System.out.println("Index created.");*/
				}
			}
			/*else if (indexerCommand.equals("querytwiter")){
				String query = command.replace("indexer.", "").replace("querytwiter.", "").replace(".", " ");
				System.out.println("Query: " + query);
				String response = inxh.queryTwiterIndex(query);
				if (response.equals("")){
					System.out.println("No Tweets found matching this query");

				}
				else {
					System.out.println("RESPONSE ");
					System.out.println("------------------------------------");
					System.out.println(response);
					System.out.println("------------------------------------");
				}
			}*/
			else if (indexerCommand.equals("query")){

				String response = "";
				if (parts.length > 1){
					/*if (parts[2].equals("website")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("website.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_WEBSITE + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_WEBSITE, query);
					}
					else if (parts[2].equals("forum")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("forum.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_POST + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_POST, query);
					}
					else if (parts[2].equals("pdf")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("pdf.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_PDF + " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_PDF, query);
					}
					else if (parts[2].equals("twiter")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("twiter.", "").replace(".", " ");
						System.out.println("Searching : " + IndexCONSTANTS.MEDIUM_TWEET+ " for \"" + query + "\"");
						response = inxh.queryIndex(IndexCONSTANTS.MEDIUM_TWEET, query);
					}else if (parts[2].equals("kb")){
						String query = command.replace("indexer.", "").replace("query.", "").replace("kb.", "").replace(".", " ");
						System.out.println("Searching Kb Index for \"" + query + "\"");
						response = inxh.queryKBIndex( query);
					}
					else{*/
						String query = command.replace("indexer.", "").replace("query.", "").replace(".", " ");
						System.out.println("Searching  for \"" + query + "\"");
						pinxh.queryIndex( query); //it prints the results
					//}

					//Print Response
					/*if (response.equals("")){
						System.out.println("No Document found matching this query");

					}
					else {
						System.out.println("------------------------------------");
						System.out.println(response);
						System.out.println("------------------------------------");
					}*/

				}
				else { // !(parts.length > 1)
					System.out.println("No terms defined for the query.");
				}

			}
			/*else if (indexerCommand.equals("starttwiterstream")){
				inxh.startTwiterStream();
			}*/
			else if (indexerCommand.equals("delete")){
				pinxh.deleteIndex();
			}
			else if (indexerCommand.equals("close")){
				pinxh.close();
			}
		}
		else{
			System.out.println("Command \""+command+"\" not found in Indexer.");
		}
	}


	/**
	 * Function for executing Scraper commands
	 * @param command String. Must be in format handler.command (ex. scraper.doscraping OR scraper.start.129921223)
	 */
	private void scraperCommands(String command){
		command = command.toLowerCase();
		if (command.startsWith("scraper.")){
			String[] parts = command.split("[.]");
			command = parts[1];
			if (command.equals("start")){
				System.out.println("Debugging :: length: "+parts.length+", command: "+command);
				//if no poarameter
				if (parts.length<3){
					sc = new Scraper();
					System.out.println("Scraper Started.");
				}
				//if there is crawlingId parameter
				else{
					String crawlingId = parts[2];

					sc = new Scraper(crawlingId);
					System.out.println("Scraper Started for crawlingId: "+crawlingId);
				}
			}
			else if (command.equals("doscraping")){
				if (sc == null){
					System.out.println("No Scraper Started. You have to start scraper first.");
				}
				else{
					sc.doScraping();
				}
			}
			else if (command.equals("scrapefromlist")){
				if (sc == null){
					System.out.println("No Scraper Started. You have to start scraper first.");
				}
				else{
					sc.scrapeFromList();
				}
			}
			else if (command.equals("close")){
				if (sc == null){
					System.out.println("No Scraper Started. You have to start scraper first.");
				}
				else{
					sc.close();
					sc=null;
				}
			}
		}
		else{
			System.out.println("Command \""+command+"\" not found in Scraper.");
		}
	}

	private void extractRelations() {

		try {
			System.out.println("Relation Extraction procedure started.");
			Scanner scanner = null;
			scanner = new Scanner( new File("input/text.txt") );
			String text = scanner.useDelimiter("\\A").next();
			scanner.close();
			RelExt.extract(text);
		} catch (FileNotFoundException e) {
			System.out.println("KIndex :: InputHandler.extractRelations() Could not read the \"input/text.txt\" file");
			e.printStackTrace();
		}
	}

	/**
	 * Start the routine to feed the KB
	 */
	private void kbFeedRoutine() {



		System.out.println("Starting KB Feeding");
		indexerCommands("indexer.start.D:\\KBFeed\\INDEX");
		String array = inxh.queryKBIndex("smt"); //parameter does not play any role
		//System.out.println(array);
		JSONArray ar = new JSONArray(array);
		for (int i=0;i<ar.length();i++) {
			JSONObject obj = (JSONObject) ar.get(i);
			String text = obj.getString("text");
			String relExtOutput = RelExt.extract(text);
			JSONObject obj2=new JSONObject(relExtOutput);
			JSONArray output = (JSONArray) obj2.get("extracted");

			//send output to KB Feed service
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = null;
			try {

				//get output as string and revome the square brackets of outer json array
				String content = output.toString();
				if (content.startsWith("[\"")) {
					content = content.replaceFirst("\\[\"","");
				}
				if (content.endsWith("\"]")) {
					content = content.replace("\"]","");
				}

				//remove all "\" characters
				content = content.replaceAll("\\\\","");

				//make a post request with parameters on url.
				httppost = new HttpPost("http://160.40.50.196:8080/api/kb/update?content="+ URLEncoder.encode(content, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				System.out.println("KIndex :: InputHandler.kbFeedRoutine() Could not create http post request");
				e.printStackTrace();
			}

			try {
				//Execute and get the response.
				HttpResponse response = httpclient.execute(httppost);
				if (response.getStatusLine().getStatusCode() % 200 < 100) {
					System.out.println("KB was fed successfully");
				} else {
					System.out.println("Error while feeding KB");
					System.out.println(response.toString());
				}

			} catch (ClientProtocolException e) {
				System.out.println("KIndex :: InputHandler.kbFeedRoutine() ClientProtocolException");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("KIndex :: InputHandler.kbFeedRoutine() IOException");
				e.printStackTrace();
			}


		}

		System.out.println("KB Feeding Finished");

	}

	private void scheduledKBFeed(){
		System.out.println("-------------------------------------------------------------------------------------------------");
		System.out.println("Scheduled KB Feed started at: "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()));
		kbFeedRoutine();
		System.out.println("Scheduled KB Feed finished at: "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()));
		System.out.println("-------------------------------------------------------------------------------------------------");
	}

	private void nlp(String analysis, boolean debugMode){
		System.out.println(" ");
		System.out.println("===== NLP Analysis Started ======");
		System.out.println(" ");

		 System.out.println("Reading input folder (/input/textAnnotationInput)");

		File inputFolder = new File("input/textAnnotationInput");
		File[] inputFiles = inputFolder.listFiles();
		for (File inputFile : inputFiles) {
			String inputFilename = inputFile.getName();
			File outputFile = new File("output/nlp/" + inputFilename);
			if (outputFile.exists()){
				System.out.println("File: " + outputFile.getName() + " already exists!");
			}
			else {
				//read the input
				System.out.println("Reading " + inputFilename + " file");
				File file = new File(inputFile.getAbsolutePath());
				InputStreamReader r = null;
				String encoding = " ";
				try {
					r = new InputStreamReader(new FileInputStream(file));
					encoding = r.getEncoding();
					System.out.println("Encoding: " + encoding);
				} catch (FileNotFoundException e) {
					System.out.println("Cannot print file's encoding.");
				}
				encoding = (encoding.equals(" ")) ?  StandardCharsets.UTF_8.toString() : encoding;
				String text = null;
				try {
					byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
					text = new String(encoded, encoding);
					//text = new Scanner(file).useDelimiter("\\A").next();
				} catch (IOException e) {
					System.out.println("KIndex :: InputHandler.nlp()  Cannot open " + inputFilename + " file.");
					e.printStackTrace();
				}

				// do analysis for every line and append to output file
				String [] lines = text.split("[\r\n]");

				for (String line : lines) {
					if ((!line.equals("")) && (!line.equals(" ")) && (!line.equals("\uFEFF")) &&
							(line.replaceAll("[^\\x00-\\x7F]", "").length() != 0)) { //
						//execute pipeline
						/*System.out.println("line: |"+line+"|");
						for (int i = 0; i < line.length(); i++) {
							System.out.println( i + ": " +line.charAt(i) + " -> "+ (int)line.charAt(i));
						}*/
						String results = "";
						switch (analysis) {
							case "":
								results = Pipeline.execute("MRC+POS+NER", line, debugMode);
								break;
							case "ne":
								results = Pipeline.execute("POS+NER", line, debugMode);
								break;
							case "concept":
								results = Pipeline.execute("MRC", line, debugMode);
								break;
							case "onlyconcept":
								results = Pipeline.execute("MRC_OCT", line, debugMode);
								break;
						}

						if (results.startsWith("--ERROR--")) {
							System.out.println(results);
						} /*else {
				System.out.println("NLP Analysis executed successfully ");
			}*/
						//
						try {
							Thread.sleep(1000);                 //1000 milliseconds is one second.
						} catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}

						//save output in file
						PrintWriter inputWriter = null;
						java.util.Date date = new java.util.Date();
						try {
							//inputWriter = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
							inputWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile.getAbsolutePath(), true)));;
						} catch (IOException e) {
							System.out.println("KIndex :: InputHandler.nlp() Could not create " + outputFile.getName());
							e.printStackTrace();
						}
						inputWriter.println(results);
						inputWriter.close();
						System.out.println("Append Output to file " + outputFile.getAbsolutePath());
					}
					else {
						System.out.println("Empty line!");
					}
				}
				System.out.println("File " + outputFile.getAbsolutePath() + " ready");
			}
		}
		System.out.println(" ");
		System.out.println("===== NLP Analysis Ended ======");
		System.out.println(" ");
	}

	private void lexParse(){
	}
}

