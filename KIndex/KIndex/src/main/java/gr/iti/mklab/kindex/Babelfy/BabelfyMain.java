package gr.iti.mklab.kindex.Babelfy;

/**
 * Created by Thodoris Tsompanidis on 12/1/2016.
 */
public class BabelfyMain {

	public static void main1(String[] args) {
		System.out.println("---------------------------------------------------");
		System.out.println("Babelnet Main Start");
		System.out.println("");

		String text = "Yellow fever begins after an incubation period of three to six days. Most cases only cause a mild infection with fever, headache, chills, back pain, fatigue, loss of appetite, muscle pain, nausea, and vomiting. In these cases, the infection lasts only three to four days.";
		System.out.println(BabelfyHandler.annotateText(text));

		System.out.println("");
		System.out.println("Babelnet Main End");
		System.out.println("----------------------------------------------------");
	}

	public static void main(String[] args) {
		String text = " Presenilin polymorphisms in  Alzheimer 's disease  .\n" +
				"  Autoimmune enteropathy in adults .\n" +
				"  Cyclosporiasis  and raspberries .\n" +
				" Protean agonists .";

		System.out.println("Named Entities in Babelfy Started");
		System.out.println("");

		System.out.println(BabelfyHandler.extractNE(text));

		System.out.println("");
		System.out.println("Babelnet Main End");
		System.out.println("----------------------------------------------------");

	}
}
