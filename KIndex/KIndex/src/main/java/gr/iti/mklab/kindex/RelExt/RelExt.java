package gr.iti.mklab.kindex.RelExt;

import gr.iti.mklab.kindex.CoreNLP.CoreNLPHandler;
import gr.iti.mklab.kindex.MetaMap.MetaMapHandler;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Custom Relation Extractor
 * This class merge outputs of MetaMap and CoreNLP components to extract useful concepts relations
 *
 *
 * Created by Thodoris Tsompanidis on 18/12/2015.
 */
public class RelExt {

	//for debuging mode set MODE = "debug", in debug mode, application prints information in all transformation levels
	//MODE = "production"
	public static final String MODE = "debug";


	public static final String testAnnotatedConllFilePath = "CoreNLPFiles/AnnotatedConll.txt" ;

	/**
	 * Extract relations from text provided
	 * Invoke MetaMap API and CoreNLP, merge outputs, and proceeds for the relation extraction
	 * Returns a json in string, with "extracted" as key, and value a JSONArray containing responses of relation extraction
	 *
	 * @param text String. Relation Extractino input
	 */
	public static String extract(String text){

		String output="";

		//Concepts from MetaMap.
		//words contains all text and some terms are annotated in Metamap concepts
		ArrayList<MetaMapWord> words = MetaMapHandler.extractFromTextToWords(text);

		//Syntactic analisys
		CoreNLPHandler nlph = new CoreNLPHandler(text);
		nlph.annotatateText();
		//conll String containing text's syntactic realations
		String conll = nlph.getCCProcessedCoNLLasString();


		//WCMapper for merge MetaMap and CoreNLP outputs
		WCMapper wcm = new WCMapper();
		//String containitg a conll string with MetaMap annotated terms
		String annotatedConll = wcm.MergeWordsToConll(conll, words);

		if (MODE.equals("debug")) {
			//Print results for debugging
			System.out.println("-------------------------------------------");
			System.out.println("Words");
			for (MetaMapWord word : words) {
				System.out.println(word.toString());
			}
			System.out.println("-------------------------------------------");
			System.out.println("CoNLL");
			System.out.println(conll);
			System.out.println("-------------------------------------------");
			System.out.println("Annotated CoNLL");
			System.out.println(annotatedConll);
			System.out.println("-------------------------------------------");
			System.out.println("Extracted Relations");
		}

		JSONObject response = new JSONObject();

		//in Conll keep only annotated terms, verbs and roots
		ArrayList<String> cleanAnnotatedConlls = AnnotatedConll.cleanAnnotatedConll(annotatedConll);

		for (String cleanAnnotatedConll : cleanAnnotatedConlls) {

			//get the sentences from conll String
			ArrayList<Sentence> sentences = AnnotatedConll.makeSentencesFromAnnotatedConll(cleanAnnotatedConll);

			//get list with KBResponse from Sentences (KBSentences )
			ArrayList<KBResponse> KBResp = AnnotatedConll.makeKBResponse(sentences);

			//add babelfy annotation (babelNet & DBPedia resources)
			String outputString = AnnotatedConll.babelfyKBResponse(KBResp);

			//Write the text in temp file
			PrintWriter inputWriter = null;
			java.util.Date date= new java.util.Date();
			try {
				inputWriter = new PrintWriter("output/"+date.getTime()+".txt", "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				System.out.println("KIndex :: RelExt.extract() Could not create file to store the output");
				e.printStackTrace();
			}
			inputWriter.print(outputString);
			inputWriter.close();

			try {
				response.append("extracted", outputString);
			} catch (JSONException e) {
				System.out.println("KIndex :: RelExt.extract() Could not add string to json Object");
				e.printStackTrace();
			}

		}

		return response.toString();
	}


	/**
	 * Just for testing. Getting Annotated Conll from {@value this#testAnnotatedConllFilePath}
	 * and extracts relations
	 *
	 */
	public static void testAnnotatedConll() {
		try {
			String contents = null;
			File file = new File(testAnnotatedConllFilePath);
			contents = new Scanner(file).useDelimiter("\\Z").next();

			if (MODE.equals("debug")) {
				System.out.println("---------------------------------------");
				System.out.println("Testing Mode. Annotated Conll was loaded from file: "+testAnnotatedConllFilePath);
				System.out.println("---------------------------------------");
				System.out.println("Annotated CoNLL");
				System.out.println(contents);
				System.out.println("------------------");
				System.out.println("Extracted Relations");
			}

			//extract relations from annotated conll
			//in Conll keep only annotated terms, verbs and roots
			ArrayList<String> cleanAnnotatedConlls = AnnotatedConll.cleanAnnotatedConll(contents);

			for (String cleanAnnotatedConll : cleanAnnotatedConlls) {

				//get the sentences from conll String
				ArrayList<Sentence> sentences = AnnotatedConll.makeSentencesFromAnnotatedConll(cleanAnnotatedConll);

				//get list with KBResponse from Sentences (KBSentences )
				ArrayList<KBResponse> KBResp = AnnotatedConll.makeKBResponse(sentences);

				//add babelfy annotation (babelNet & DBPedia resources)
				AnnotatedConll.babelfyKBResponse(KBResp);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
