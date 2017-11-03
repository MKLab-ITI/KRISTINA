package gr.iti.mklab.kindex.RelExt;

/**
 * Created by Thodoris Tsompanidis on 18/12/2015.
 */
public class RelExtMain {

	public static void main(String[] args) {
		System.out.println("RelExtMain Started");
		System.out.println("---------------------------");
		//String text = "pneumonia often have a productive cough,fever accompanied by shaking chills,shortness of breath, sharp or stabbing chest pain during deep breaths, and an increasedrate of breathing. In the elderly, confusion may be the most prominent sign. The typical signs and symptoms in children under five are fever, cough, and fast or difficult breathing";
		String text = "Yellow fever only cause a mild infection with fever, headache, chills, back pain, fatigue, loss of appetite, muscle pain, nausea, and vomiting.";
		RelExt.extract(text);
		//RelExt.testAnnotatedConll();
		System.out.println("---------------------------");
		System.out.println("RelExtMain Ended");
	}


}
