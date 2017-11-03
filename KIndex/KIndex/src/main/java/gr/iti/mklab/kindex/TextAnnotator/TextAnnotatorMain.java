package gr.iti.mklab.kindex.TextAnnotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A Main class for text Annotator
 *
 * Created by Thodoris Tsompanidis on 16/3/2016.
 */
public class TextAnnotatorMain {

//	static String TextAnnotatorInputFilepath= "input/TextAnnotatorInput.txt";
	static String TextAnnotatorInputFilepath= "input/input_metamap_2.txt";

	public static void main(String[] args) {

		System.out.println("------------------------------------------");
		System.out.println("=====  Text Annotator Main Started  ===== ");
		System.out.println(" ");

		//String input
		//String text = "Dementia with Lewy bodies (DLB) is a form of dementia that shares characteristics with both Alzheimer's and Parkinson's diseases. Lewy bodies(named after FH Lewy who discovered them in 1912) are tiny spherical protein deposits found in nerve cells. Their presence in the brain disrupt's the brain's normal functioning, interupting the action of important chemical messengers including acetylcholine and dopamine.";

		//file input
		File file = new File(TextAnnotatorInputFilepath);
		String text = null;
		try {
			text = new Scanner(file).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			System.out.println("Text Annotator Main: Cannot open "+ TextAnnotatorInputFilepath +"file.");
			e.printStackTrace();
		}

		String results = Pipeline.execute("MRC+POS+NER",text, true);

		System.out.println(" ");
		System.out.println("=====  Text Annotator Main Ended  ===== ");
		System.out.println("------------------------------------------");

	}

}
