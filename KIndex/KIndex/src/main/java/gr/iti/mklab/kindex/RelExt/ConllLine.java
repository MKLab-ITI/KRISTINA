package gr.iti.mklab.kindex.RelExt;

/**
 *  A class for representing the conll line
 *
 * Created by Thodoris Tsompanidis on 22/12/2015.
 */
public class ConllLine {

	int id;
	String word;
	String pos; //Part Of Speech
	int head; //0 for ROOT
	String depRel;
	String concept;

	public ConllLine(int id, String word, String pos, int head, String depRel) {
		this.id = id;
		this.word = word;
		this.pos = pos;
		this.head = head;
		this.depRel=depRel;
		this.concept=null;
	}

	/**
	 * Constructor. Gets as parameter a conll line (annotated or not)
	 * @param line String. A connl line
	 */
	public ConllLine(String line) {
		line=line.replaceAll(" ","\t");
		String[] l = line.split("\t");
		this.id = Integer.parseInt(l[0]);
		this.word = l[1];
		this.pos = l[3];
		this.head = Integer.parseInt(l[5]);
		this.depRel= l[6];
		this.concept=null;
		if (line.contains("(MetaMap\tAnnotated)")){
			String concept = line.substring(line.lastIndexOf("[")+1,line.lastIndexOf("]")).replaceAll("\t"," ");
			this.concept = concept;
		}
	}

	public String getDepRel() {
		return depRel;
	}

	public void setDepRel(String depRel) {
		this.depRel = depRel;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public KBItem getAsKBItem() {
		String tempConcept = (this.concept == null) ? " " : this.concept;
		return new KBItem(this.word,tempConcept);
	}
}
