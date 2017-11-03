package gr.iti.mklab.kindex.RelExt;

/**
 * Class word represents Concept/NotConcept word in a text
 * It handles the information of if and what concept is this specific word
 *
 * Created by Thodoris Tsompanidis on 18/12/2015.
 */
public class MetaMapWord {

	int pos;
	String word;
	String concept;
	boolean isConcept;

	public MetaMapWord(int pos, String word) {
		this.pos = pos; //word's position in text
		this.word = word;
		this.concept = null;
		this.isConcept = false;
	}

	public MetaMapWord(int pos, String word, String concept) {
		this.pos = pos;
		this.word = word;
		this.concept = concept;
		this.isConcept = true;
	}

	@Override
	public String toString() {
		String output = "[p: " + pos +", w: '" + word;
		output += (isConcept()) ? ", c: " + concept : "";
		output +="]";
		return output;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
		this.isConcept = (concept.equals("")) ? false : true;
	}

	public boolean isConcept() {
		return isConcept;
	}

	public void setConcept(boolean concept) {
		isConcept = concept;
	}
}
