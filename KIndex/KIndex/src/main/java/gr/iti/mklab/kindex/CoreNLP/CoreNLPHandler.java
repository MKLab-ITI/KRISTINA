package gr.iti.mklab.kindex.CoreNLP;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Class for handling Stanford Parser coreNLP component
 * coreNLP was imported in this project as library and it is not using any installed server or Web to process and serve the content
 *
 * Created by Thodoris Tsompanidis on 17/12/2015.
 */
public class CoreNLPHandler {

	String text;
	Annotation document;

	/**
	 * Constructor without parameter.
	 * In case you use this constructor, you have to set the text with setText(text) before any requesting process
	 */
	public CoreNLPHandler() {
		this.text="";
		this.document=null;
	}

	/**
	 * Constructor with String parameter.
	 *
	 * @param text String. The text to be processed
	 */
	public CoreNLPHandler(String text) {
		this.text = text;
		this.document=null;
	}

	/**
	 * Text Setter
	 *
	 * @param text String. Text to process
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Function for annotating the text.
	 * Mandatory procedure before any output extraction
	 *
	 * @return boolean. True if annotation is successful
	 */
	public boolean annotatateText(){

		//code imported form http://stanfordnlp.github.io/CoreNLP/api.html
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		document = new Annotation(this.text);
		// run all Annotators on this text
		pipeline.annotate(document);

		return true;
	}



	/**
	 * Important! you have to Obj.annotatateText() before Obj.printCCProcessedCoNLLtoFile()
	 * Print CCProcessed text to file provided (CoNLL format)
	 * Path to file: "/CoreNLPFiles/[filename]"
	 *
	 * @param filename String. output will be at file: "/CoreNLPFiles/[filename]"
	 */
	public void printCCProcessedCoNLLtoFile( String filename) {
		try {


			//long time = System.currentTimeMillis() / 1000l;
			//filename= time + ".conll";
			FileWriter w = new FileWriter("CoreNLPFiles/" + filename);

			System.out.println("Start printing CoNLL to File: CoreNLPFiles/"+filename);
			//pipeline.conllPrint(document, w);
			w.write(getCCProcessedCoNLLasString());
			w.flush();
		} catch (IOException e) {
			System.out.println("KIndex :: CoreNLPHandler.printCCProcessedCoNLLtoFile() Could not print conll");
			e.printStackTrace();
		}
	}

	/**
	 * Important! you have to Obj.annotatateText() before Obj.printCCProcessedCoNLL()
	 * Print CCProcessed text to console (CoNLL format)
	 *
	 */
	private void printCCProcessedCoNLL() {
		System.out.println(getCCProcessedCoNLLasString());
	}

	/**
	 * Returns CCProcessed text as String  (CoNLL format)
	 *
	 * @return String. CoNLL of CCProcessed document
	 */
	public String getCCProcessedCoNLLasString(){
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			CustomCoNLLOutputter.conllPrint(document, os);
			return new String(os.toByteArray());
		} catch (IOException e) {
			System.out.println("KIndex :: CoreNLPHandler.getCCProcessedCoNLLasString() Could not get conll");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns an XML string . The wrapping element is content Tag. Every word is wrapped in a POS tag.
	 * punctuation or words not identified with pos, are wrapped in NoPOS tag
	 *
	 * @param text
	 * @return an XML as String
	 */
	static public String getXMLwithNERandPOS(String text){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		text = text.replaceAll("[{}!@#$%^*()\\[\\]]"," $0 ");

		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		String output = "<content> ";
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String currNeToken = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
				word = replaceSpecialCharactersTags(word);
				lemma = replaceSpecialCharactersTags(lemma);

				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				boolean notPOS = false;
				if (word.matches("\\p{Punct}") || word.equals("&gt;") || word.equals("&lt;") ){
					pos = "Punctuation";
				}
				/*else if (pos.toLowerCase().equals(word)){
					notPOS = true;
				}*/

				pos = pos.replaceAll("[^a-zA-Z ]", "");
				pos = (pos.equals("")) ? "Punctuation" : pos;
				String openTag = notPOS ? "<NoPOS lemma=\""+lemma+"\">" : "<" + pos + " lemma=\""+lemma+"\">";
				String closeTag = notPOS ? "</NoPOS> " : "</" + pos + "> ";

				output += (currNeToken.equals("O")) ? "" : "<ne type=\"" + currNeToken + "\">";
				output += openTag + word + closeTag;
				output += (currNeToken.equals("O")) ? "" : "</ne> ";
				output += " ";
			}
		}

		output += "</content>";
		return output;
	}

	private static String replaceSpecialCharactersTags(String word) {
			 if  (word.equals("-LRB-"))  {return "(";}
		else if  (word.equals("-RRB-"))  {return ")";}
		else if  (word.equals("-LSB-"))  {return "[";}
		else if  (word.equals("-RSB-"))  {return "]";}
		else if  (word.equals(">"))      {return "&gt;";}
		else if  (word.equals("<"))      {return "&lt;";}
		return word;
	}

	public static String getNEasString(String text){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		text = text.replaceAll("[{}!@#$%^*()\\[\\]]"," $0 ");

		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		HashSet<String> nes = new HashSet<String>();

		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String currNeToken = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				word = replaceSpecialCharactersTags(word);

				if (!currNeToken.equals("O")) {
					nes.add(currNeToken);
				}

			}
		}

		return nes.toString().replaceAll("[\\[\\],]"," ");
	}

	public static JSONObject getNEasjson(String text){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		text = text.replaceAll("[{}!@#$%^*()\\[\\]]"," $0 ");

		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		int counter = 0;
		int prevCount = 0;
		String prevNe = null;
		String prevNeTerm = null;

		Multimap<String,String> nes = ArrayListMultimap.create();
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			prevNe = null;
			prevNeTerm = null;
			prevCount = 0;
			counter = 0;
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String currNeToken = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				word = replaceSpecialCharactersTags(word);

				if (!currNeToken.equals("O")) {
					if (currNeToken.equals(prevNe) && (counter == (prevCount + 1))){
						prevNeTerm +=  " " + word ;
						prevNe = currNeToken;
						prevCount = counter;
					}
					else{
						if(prevNe != null){
							nes.put(prevNe, prevNeTerm);
						}
						prevNe = currNeToken;
						prevNeTerm = word;
						prevCount = counter;
					}
				}
				counter++;
			}
			if(prevNe != null){
				nes.put(prevNe, prevNeTerm);
				prevNe = null;
				prevNeTerm = null;
			}
		}

		JSONObject obj = new JSONObject();
		//convert to json Object
		for (String key : nes.keys()) {
			JSONArray array = new JSONArray();
			Collection<String> collection = nes.get(key);
			for (String c : collection) {
				if(!array.toString().contains("\""+c+"\"")) {
					array.put(c);
				}
			}
			obj.put(key,array);
		}

		return obj;
	}
}






















