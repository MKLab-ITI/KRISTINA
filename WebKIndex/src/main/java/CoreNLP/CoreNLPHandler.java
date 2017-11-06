package CoreNLP;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
				word = replaceSpecialCharactersTags(word);

				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				boolean notPOS = false;
				if (word.matches("\\p{Punct}") || word.equals("&gt;") || word.equals("&lt;") ){
					pos = "Punctuation";
				}
				else if (pos.toLowerCase().equals(word)){
					notPOS = true;
				}

				pos = pos.replaceAll("[^a-zA-Z ]", "");
				String openTag = notPOS ? "<NoPOS>" : "<" + pos + ">";
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


	public static String[] splitSentences(String text, String language){
		ArrayList<String> sentencesList = new ArrayList<>();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty("tokenize.language", language);

		// shut off the annoying intialization messages
		RedwoodConfiguration.empty().capture(System.err).apply();

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// enable stderr again
		RedwoodConfiguration.current().clear().apply();

		Annotation document = new Annotation(text);
		// run all Annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//		System.out.println("Number of sentences detected: " + sentences.size());
		for (CoreMap sentence : sentences) {

			// get sentence text and add it to the sentences list
			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);

			// clear line breaks inside sentence (replace it with a tab character)
			sentenceText = sentenceText.replace("\n","\t");

			// remove non-ascii characters
//			sentenceText = Normalizer.normalize(sentenceText, Normalizer.Form.NFD);
//			sentenceText = sentenceText.replaceAll("[^\\x00-\\x7F]", "");
			// remove control characters
			sentenceText = sentenceText.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");

			sentencesList.add(sentenceText);
		}

		String[] sentencesArray = sentencesList.toArray(new String[sentencesList.size()]);

		return sentencesArray;
	}

}






















