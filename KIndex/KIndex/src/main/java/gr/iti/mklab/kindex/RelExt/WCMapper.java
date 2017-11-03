package gr.iti.mklab.kindex.RelExt;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Words Conll Mapper
 * class to merge words[] and conll 
 * 
 * Created by Thodoris Tsompanidis on 21/12/2015.
 */
public final class WCMapper {
	
	public static String MergeWordsToConll(String conll, ArrayList<MetaMapWord> words){
		
		//for each line of conll look to find the term in words[] and if it is annotated then set annotation to conll line
		
		String output="";
		String[] conllLines = conll.split("\\r?\\n");
		for (String cline : conllLines) {
			output  += findFirstMatch(cline,words);

		}

		//write it to file
		try {
			PrintWriter writer = null;
			writer = new PrintWriter("CoreNLPFiles/AnnotatedConll.txt", "UTF-8");
			writer.print(output);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			//e.printStackTrace();
			System.out.println("KIndex :: WCMapper.MergeWordsToConll() Could not write Annotated Conll to File");
		}

		return output;
		
	}

	/**
	 * Function to find the first word in words matching the term of conll line term
	 * If a word is found in line, then word is removed from ArrayList words
	 *
	 * @param cline String. Line of conll string
	 * @param words ArrayList<Word>. List of words.
	 * @return String. The cline with annotation if concept is found
	 */
	private static String findFirstMatch(String cline, ArrayList<MetaMapWord> words) {
		if (!cline.equals("")) {
			for (MetaMapWord word : words) {
				if (getTerm(cline).equals(word.getWord())) {
					if (word.isConcept()) {
						words.remove(word);
						return cline + " [" + word.getConcept() + "](MetaMap Annotated)\n";
					}
					words.remove(word);
					return cline+"\n";
				}
			}
		}
		return cline+"\n";
	}

	/**
	 * Function to get the tern from conll line
	 * Always is the second word from line
	 *
	 * @param cline String. a conll string line
	 * @return String. the term of cline lower case
	 */
	private static String getTerm(String cline) {
		//words in cline are separated by special character "\t"
		return cline.split("\t")[1].toLowerCase();
	}

}
