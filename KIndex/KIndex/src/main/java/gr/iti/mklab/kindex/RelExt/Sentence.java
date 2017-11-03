package gr.iti.mklab.kindex.RelExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represent a sentence containing subject-predicate-objects
 * It is the first level of sentence created during relation extraction.
 * This class contains just ConllLines in Subject and Object.
 *
 * The sentence comes as custom text analysis output
 * (MetaMap Concepts and Stanford Parser Dependencies)
 *
 * Created by Thodoris Tsompanidis on 23/12/2015.
 */
public class Sentence {

	//Subject is not a single value because it can be more than one term, ex: young children
	//Also, object is a two dimensions array because there can be multiple objects of multiple terms.
	Map<Integer, ConllLine> SubjectMap;
	ArrayList<ConllLine> Subject;
	ConllLine Predicate;
	Map <Integer, ConllLine> ObjectMap;
	ArrayList<ConllLine> Object;
	ArrayList<Sentence> ObjectSentence;


	public Sentence() {
		Predicate = null;
		SubjectMap = new HashMap<Integer, ConllLine>();
		Subject = new ArrayList<ConllLine>();
		ObjectMap = new HashMap<Integer, ConllLine>();
		Object = new ArrayList<ConllLine>();
		ObjectSentence = new ArrayList<Sentence>();
	}

	public Sentence(ConllLine predicate) {
		Predicate = predicate;
		SubjectMap = new HashMap<Integer, ConllLine>();
		Subject = new ArrayList<ConllLine>();
		ObjectMap = new HashMap<Integer, ConllLine>();
		Object = new ArrayList<ConllLine>();
		ObjectSentence = new ArrayList<Sentence>();

	}

	/**
	 * Add cline to sentence. During adding the cline, this function selects on which part
	 * cline is going to be add to (Subject, Object)
	 * @param cline ConllLine. The cline to be added to this sentence
	 */
	public void add(ConllLine cline) {

		//if cline is subject, always, insert as subject, independently where it is connected to
		if (cline.getDepRel().contains("subj")){ //if it is subject
			addSubject(cline);
		}

		//else if it is connected to predicate add cline to Objects
		else if (cline.getHead() == Predicate.getId()){
				addObject(cline);
		}

		//if it is connected to Subject add cline to Subject
		else if (this.SubjectMap.containsKey(cline.getHead())){
			addSubject(cline);
		}

		//if it is connected to Object add cline to Objects
		else if (this.ObjectMap.containsKey(cline.getHead())){
			addObject(cline);
		}

		//when predicate of sentence is not ROOT, there is a chance of predicate having head the incoming cline
		else if (Predicate.getHead() == cline.getId()){
			if (Predicate.getDepRel().contains("cop")){ // cop stands for Copula
				addObject(cline);
			}
			else if (Predicate.getDepRel().contains("acl")){
				addSubject(cline);
			}
		}
	}

	/**
	 * Adds sentence in ObjectSentence.
	 * A sentence can contain an other sentence as Object
	 * @param sentence
	 */
	public void addObjectSentence(Sentence sentence){
		ObjectSentence.add(sentence);
	}

	/**
	 * Add the cline to Object
	 * @param cline ConllLine.
	 */
	private void addObject(ConllLine cline) {
			this.Object.add(cline);
			this.ObjectMap.put(cline.getId(), cline); //add cline.Id to map
	}

	/**
	 * Add the clineto Subject
	 * @param cline ConllLine.
	 */
	private void addSubject(ConllLine cline) {
		this.Subject.add(cline);
		this.SubjectMap.put(cline.getId(),cline);
	}

	public ConllLine getPredicate() {
		return Predicate;
	}

	public ArrayList<ConllLine> getSubject() {
		return Subject;
	}

	public ArrayList<ConllLine> getObject() {
		return Object;
	}

	public ArrayList<Sentence> getObjectSentence() {
		return ObjectSentence;
	}

	/**
	 * Get an ArrayList of Integers containing the ConllLines' head ids of Subjects add Predicate
	 * @return ArrayList<Integer> with Subjects' and Predicate's Heads
	 */
	public ArrayList<Integer> getSPHeads(){
		ArrayList<Integer> ints = new ArrayList<Integer>();
		ints.add(this.Predicate.getHead());
		Iterator<ConllLine> sIter = this.Subject.iterator();
		while (sIter.hasNext()){
			ints.add(sIter.next().getHead());
		}
		return ints;
	}

	/**
	 * Get all ConllLines' ids from this sentence
	 * @return ArrayList<Integer>
	 */
	private ArrayList<Integer> getAllIDs(){
		ArrayList<Integer> ints = new ArrayList<Integer>();
		ints.add(this.Predicate.getId());
		Iterator<ConllLine> sIter = this.Subject.iterator();
		while (sIter.hasNext()){
			ints.add(sIter.next().getId());
		}
		Iterator<ConllLine> oIter = this.Object.iterator();
		while (oIter.hasNext()){
			ints.add(oIter.next().getId());
		}
		return ints;
	}

	/**
	 * Return True if at least one id of ids is this sentence's id.
	 * @param ids ArrayList<Integer>. ids to check
	 * @return boolean
	 */
	public boolean containsAtLeastOneId(ArrayList<Integer> ids) {
		ArrayList<Integer> curIds = this.getAllIDs();
		for (Integer id : ids) {
			if (curIds.contains(id)){
				return true;
			}
		}
		return false;
	}
}
