package gr.iti.mklab.kindex.RelExt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represent a conll sentence containing ConllLines
 *
 * Created by Thodoris Tsompanidis on 22/12/2015.
 */
public class ConllSentence {

	ArrayList<ConllLine> lines;
	Iterator<ConllLine> it;

	public ConllSentence() {
		lines = new ArrayList<ConllLine>();
		it=null;
	}

	/**
	 * Add a line to the sentence
	 * @param c ConllLine. ConllLine to add
	 */
	public void addLine(ConllLine c){
		lines.add(c);
	}

	/**
	 * Find line with id in sentence, if exists, and return the conllLine
	 *
	 * @param id int. Line Id you are looking for
	 * @return ConllLine. The requesting line
	 */
	public ConllLine findId(int id){
		for (ConllLine line : lines) {
			if (line.getId() == id)
				return line;
		}
		return null;
	}

	/**
	 * function for helping lines iteration
	 * @return boolean. True if there is next line
	 */
	public boolean hasNext(){
		if (it == null){
			it = lines.iterator();
		}
		return it.hasNext();
	}

	/**
	 * function to iterate the lines. It iterates from first to last line
	 * @return ConllLine. The next line
	 */
	public ConllLine getNext(){
		return it.next();
	}
}
