package gr.iti.mklab.kindex.CoreNLP;

import java.io.IOException;

/**
 * Created by Thodoris Tsompanidis on 17/12/2015.
 */
public class CoreNLPMain {

	public static void main(String[] args) throws IOException {
		System.out.println("Beginning of Main() function / CoreNLP Package.");
		System.out.println("------------------------------------------------");
		String text = "Yellow fever begins after an incubation period of three to six days. Most cases only cause a mild infection with fever, headache, chills, back pain, fatigue, loss of appetite, muscle pain, nausea, and vomiting. In these cases, the infection lasts only three to four days.";
		CoreNLPHandler nlph= new CoreNLPHandler(text);
		nlph.annotatateText();
		//nlph.testOutput();
		System.out.println("End of  Main() function / CoreNLP Package.");
		System.out.println("------------------------------------------");
	}
}
