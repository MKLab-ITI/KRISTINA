package gr.iti.mklab.kindex.MetaMap;
/*
Example of MetaMap output

Processing 00000000.tx.1: On the basis of comparisons of large groups of people with Alzheimer's disease with others who have not been affected, researchers suggest that there are a number of risk factors.

Phrase: On the basis of comparisons of large groups of people
Meta Mapping (646):
   573   BASIS (Basis) [Pharmacologic Substance]
   706   Comparison [Activity]
   593   large group [Group Attribute]
   573   People (Persons) [Population Group]
Meta Mapping (646):
   573   Basis (Basis - conceptual entity) [Functional Concept]
   706   Comparison [Activity]
   593   large group [Group Attribute]
   573   People (Persons) [Population Group]

Phrase: with Alzheimer's disease with others
Meta Mapping (761):
   593   With (In addition to) [Qualitative Concept]
   806   ALZHEIMER DISEASE (Alzheimer's Disease) [Disease or Syndrome]
   593   Others (Others - Allergy) [Finding]
Meta Mapping (761):
   593   With (In addition to) [Qualitative Concept]
   806   ALZHEIMER DISEASE (Alzheimer's Disease) [Disease or Syndrome]
   593   others (other medicated shampoos in ATC) [Pharmacologic Substance]
Meta Mapping (761):
   593   With (In addition to) [Qualitative Concept]
   806   Alzheimer's Disease (Alzheimer's Disease Pathway KEGG) [Pathologic Function]
   593   Others (Others - Allergy) [Finding]
Meta Mapping (761):
   593   With (In addition to) [Qualitative Concept]
   806   Alzheimer's Disease (Alzheimer's Disease Pathway KEGG) [Pathologic Function]
   593   others (other medicated shampoos in ATC) [Pharmacologic Substance]
Meta Mapping (761):
   593   With (With - dosing instruction fragment) [Intellectual Product]
   806   ALZHEIMER DISEASE (Alzheimer's Disease) [Disease or Syndrome]
   593   Others (Others - Allergy) [Finding]
Meta Mapping (761):
   593   With (With - dosing instruction fragment) [Intellectual Product]
   806   ALZHEIMER DISEASE (Alzheimer's Disease) [Disease or Syndrome]
   593   others (other medicated shampoos in ATC) [Pharmacologic Substance]
Meta Mapping (761):
   593   With (With - dosing instruction fragment) [Intellectual Product]
   806   Alzheimer's Disease (Alzheimer's Disease Pathway KEGG) [Pathologic Function]
   593   Others (Others - Allergy) [Finding]
Meta Mapping (761):
   593   With (With - dosing instruction fragment) [Intellectual Product]
   806   Alzheimer's Disease (Alzheimer's Disease Pathway KEGG) [Pathologic Function]
   593   others (other medicated shampoos in ATC) [Pharmacologic Substance]

Phrase: who

Phrase: have
...
*/


import gr.iti.mklab.kindex.RelExt.MetaMapWord;

import java.util.ArrayList;

/**
 * Class to parse MetaMap output
 *
 * Created by Thodoris Tsompanidis on 14/12/2015.
 */
public final class CustomMetaMapParser {

	/**
	 * This function get as input a MetaMap output formatted text.
	 * Merges the terms back to text and annotates concepts bold
	 * Returns the reconstructed text to html format
	 *
	 * @param input String. MetaMap output formatted text
	 * @return String. html-ready string
	 */
	public static String mergeResutlstoHTML(String input){
		//merge all phrases of the output and make bold all concepts found
		//if a phrase is noted as concept phrase with multiple/different annotations, mark terms as concepts only the first time, and ignore next meanings

		String output="<!DOCTYPE html><html><body>";

		String[] inputLines = input.split("\\r?\\n");
		int i=0;
		String phrase=""; //stores the processing Phrase
		boolean phraseMode = false; // true when parser reads a phrase and search for Meta Mapping Strings
		boolean metaMappingMode = false; // true when parser process a Meta Mapping sequence

		for (String inputLine : inputLines) {

			String action="NA";

			if(inputLine.startsWith("Phrase:")){
				//add previous phrase to output
				output+=" "+phrase;

				phrase=inputLine.replace("Phrase: ","").toLowerCase();
				//System.out.println("|"+phrase+"|");
				action= "Ph";
				phraseMode=true;
			}
			else if(inputLine.startsWith("Meta Mapping")){
				if (phraseMode){//true, this is the first Meta Mapping sequence of this phrase
					//switch to metaMappingMode to process the lines below
					metaMappingMode=true;
					phraseMode=false;
					action= "MMst";
				}
				else if (metaMappingMode){
					metaMappingMode=false; //set false to second MetaMap in row
					action= "MMend";
				}
			}
			else if (metaMappingMode){
				if (inputLine.trim().equals("")){ //in first empty line in metaMappingMode, finish this mode
					metaMappingMode=false;
					action= "MMend";
				}
				else if (inputLine.startsWith("Processing")){ //if a new processing is starting, and there is MetaMap mode is on, finish this mode
					metaMappingMode=false;
					action= "MMend";
				}
				else{
					String term = extractTermFromLine(inputLine).toLowerCase(); //first "word" is position, second is the term
					phrase = replaceTermsToPhrase(phrase, term);

					action= "MM ("+term+")";

				}
			}

			System.out.println(/*i+" "+action+": "+*/inputLine);
			i++;
		}
		output+=" "+phrase;
		output+="</body></html>";
		return output;
	}

	/**
	 * Function for marking <b>BOLD</b> all terms
	 *
	 * @param phrase, String.Initial phrase
	 * @param term, String. The term to make bold in phrase
	 * @return String, thw phrase
	 */
	private static String replaceTermsToPhrase(String phrase, String term) {

		//remove all special characters and starting/ending spaces
		term=term.replaceAll("[^a-zA-Z0-9 ]+","");
		if (term.startsWith(" ")) term=term.substring(1,term.length());
		if (term.lastIndexOf(" ") == term.length()) term=term.substring(0,term.length()-1);

		String response = null;
		if (phrase.contains(term)){ //if phrase contains term, just make the term bold
			if (phrase.contains(" "+term+" ")){
				response = phrase.replace(" "+term+" ",(" <b>"+term+"</b> "));
			}
			else if (phrase.contains(" "+term)){
				response = phrase.replace(" "+term,(" <b>"+term+"</b>"));
				//response = (" "+phrase+" ").replace(" "+term+" ",(" <b>"+term+"</b> "));
				//response = response.replaceFirst(" ","");
				//response = response.substring(0,response.lastIndexOf(" "));

			}
			else if (phrase.contains(term+" ")){
				response = phrase.replace(term+" ",("<b>"+term+"</b> "));
			}
			else{
					response = phrase.replace(term,("" +
							"<b>"+term+"</b>"));
				}
		}
		else if(term.split(" ").length>1){ //if term contains more than one word and phrase does not contain term, break term down to words and search every word separately
			//System.out.println("Phrase: |"+phrase+"|");
			//System.out.println("Terms: |"+term+"|");
			String[] t = term.split(" ");
			for (String s : t) {
				//System.out.println("Term: |"+s+"|");
				phrase = replaceTermsToPhrase(phrase, s);
			}
			response=phrase;
		}
		else { //else return the phrase as is
			response = phrase;
		}

		return response;
	}



	public static ArrayList<MetaMapWord> extractToWords(String input){

		ArrayList<MetaMapWord> words = new ArrayList<MetaMapWord>();
		ArrayList<MetaMapWord> phraseWords = new ArrayList<MetaMapWord>();

		String[] inputLines = input.split("\\r?\\n");
		int i = 0; //line of MetaMap output
		int pos = 1;//position of word in text
		String phrase=""; //stores the processing Phrase
		boolean phraseMode = false; // true when parser reads a phrase and search for Meta Mapping Strings
		boolean metaMappingMode = false; // true when parser process a Meta Mapping sequence

		for (String inputLine : inputLines) {

			String action="NA";

			if(inputLine.startsWith("Phrase:")){

				//add previous phrase to words
				words.addAll(phraseWords);

				//empty phraseWords
				phraseWords.clear();

				//create an array of words for this phrase
				//if there are concepts in this phrase, just change the word.concept value in phrase processing
				phrase=inputLine.replace("Phrase: ","").toLowerCase();
				String[] phrase_words = phrase.split(" ");
				for (String phrase_word : phrase_words) {
					MetaMapWord w = new MetaMapWord(pos, phrase_word.replaceAll("[^a-zA-Z0-9 ]", "")); //remove everything except letters and numbers
					phraseWords.add(w);
					pos++;
				}
				//System.out.println("|"+phrase+"|");
				action= "Ph";
				phraseMode=true;
			}
			else if(inputLine.startsWith("Meta Mapping")){
				if (phraseMode){//true, this is the first Meta Mapping sequence of this phrase
					//switch to metaMappingMode to process the lines below
					metaMappingMode=true;
					phraseMode=false;
					action= "MMst";
				}
				else if (metaMappingMode){
					metaMappingMode=false; //set false to second MetaMap in row
					action= "MMend";
				}
			}
			else if (metaMappingMode){
				if (inputLine.trim().equals("")){ //in first empty line in metaMappingMode, finish this mode
					metaMappingMode=false;
					action= "MMend";
				}
				else if (inputLine.startsWith("Processing")){ //if a new processing is starting, and there is MetaMap mode is on, finish this mode
					metaMappingMode=false;
					action= "MMend";
				}
				else{
					String term = extractTermFromLine(inputLine).toLowerCase(); //first "word" is position, second is the term
					String termInParenthesis = extractTermInParenthesisFromLine(inputLine).toLowerCase();
					String concept = extractConceptFromLine(inputLine).toLowerCase();
					setConceptToPhraseWords(phraseWords, term, termInParenthesis, concept);
					//action= "MM ("+term+"[c:"+concept+"])";

				}
			}

			//System.out.println(i+" "+action+": "+inputLine);
			i++;
		}

		//add the last phrase to words
		words.addAll(phraseWords);

		//for (Word word : words) {
		//	System.out.println(word.toString());
		//}
		return words;
	}


	private static void setConceptToPhraseWords(ArrayList<MetaMapWord> phraseWords, String term,String termInParenthesis, String concept) {

		boolean found = false;

		//term maybe contain more than one word
		//in that case, the last word in term will be annotated as concept
		String[] terms ;
		if (term.split(" ").length>1){
			//split the string in white spaces and remove anything except letter and numbers
			terms = term.replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
		}
		else {
			terms= new String[]{term};

		}
		for (MetaMapWord phraseWord : phraseWords) {
			for (int i=0; i<terms.length;i++) {
				if (phraseWord.getWord().toLowerCase().contains(terms[i].toLowerCase())) {
					phraseWord.setConcept(concept);
					found = true;
				}
			}
		}

		if (!found) {
			//if term was not found in phraseWords, there is a chance of termInParenthsis to be found in phraseWords,
			String[] termsInParenthesis;
			if (termInParenthesis.split(" ").length > 1) {
				//split the string in white spaces and remove anything except letter and numbers
				termsInParenthesis = termInParenthesis.replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
			} else {
				termsInParenthesis = new String[]{termInParenthesis};

			}
			for (MetaMapWord phraseWord : phraseWords) {
				for (int i = 0; i < termsInParenthesis.length; i++) {
					if (phraseWord.getWord().toLowerCase().contains(termsInParenthesis[i].toLowerCase())) {
						phraseWord.setConcept(concept);
					}
				}
			}
		}
	}

	private static String extractConceptFromLine(String line) {

		String response=line.substring(line.lastIndexOf("[")+1,line.lastIndexOf("]"));
		return response;
	}

	/**
	 *
	 * @param line, String a line in format  "593   large group [Group Attribute]"
	 * @return String, the term. Ex: "large group"
	 */

	public static String extractTermFromLine(String line){
		String response="";
		String[] words=line.split("\\s+");
		//the first word is always a number, word's position
		int i=2;
		String prefix="";
		while ((!words[i].startsWith("(")) && (!words[i].startsWith("[")) && i<words.length){
			response+=prefix+words[i];
			prefix=" ";
			i++;
		}
		return response;
	}

	/**
	 * Extract the term(s) in parenthesis, next to initial term, from a MetaMap output line
	 * @param line MetaMap output line
	 * @return term(s) in parenthesis in line
	 */
	private static String extractTermInParenthesisFromLine(String line) {
		String response="";
		boolean writting = false;
		for (char ch: line.toCharArray()) {
			if (ch == ')' || (writting && ch == '(')){
				return response;
			}
			if (writting){
				response += ch;
			}
			if (ch == '(' ) {
				writting = true;
			}

		}
		return response;
	}
}


























