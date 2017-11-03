package gr.iti.mklab.kindex.RelExt;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Class for handling multiple ConllLines making a phrase.
 * This class is mainly used in merging terms
 *
 * Created by Thodoris Tsompanidis on 5/1/2016.
 */
public class ConllPhrase {
	ArrayList<ConllLine> terms;
	HashSet ids = new HashSet();
	HashSet heads = new HashSet();

	//line's dependencies that indicates that line is added before/after head's line
	String[] beforeString = {"advmod", "case", "amod", "conj:or", "nmod", "subj", "comp", "compound", "det"};
	String[] afterString = {"obj", "advcl"};
	//line's dependencies that indicates that a new phrase must be constructed
	String[] newPhraseString = {"conj:and", "appos" };
	private String responseTerm;


	public ConllPhrase() {
		terms = new ArrayList<ConllLine>();
	}

	/**
	 * Adds a ConllLine to ConllPhrase
	 * Some syntactic relations does not allow line's addition to phrase. In that case function returns false
	 *
	 * @param line ConnlLine. The line to be added in phrase
	 * @return boolean. True if line was added successfully. False if line cannot be added to phrase. In case of false,
	 * a new ConllPhrase must be constructed by this line.
	 */
	public boolean add(ConllLine line) {

		//if lines relation is indicates that line must be in new phrase, return false
		//if (newPhraseContains(line.getDepRel())){
		//	return false;
		//}

		//if the line is the first Phrase'e line
		if (terms.isEmpty()) {
			terms.add(line);
			ids.add(line.getId());
			heads.add(line.getHead());
			return true;
		}
		//if there already are lines in that phrase
		else {
			int head = line.getHead();
			int id = line.getId();
			String rel = line.getDepRel();
			String pos = line.getPos();
			String concept = line.getConcept();

			//in case phrase contains lines head
			if (ids.contains(head)) {
				//if relation is one of these that merging is enabled, and if it is the same concept with existing OR it is not a noun
				if (isRelToAdd(rel) && (sameConcept(concept) || !pos.contains("NN"))) {
					terms.add(line);
					ids.add(id);
					heads.add(head);
					reorganizeTermsOrder();
					return true;
				}
				//if phrase contains the line's head line but relation is not to be add, return false, to create a new phrase
				return false;
			}


		}
		return false;
	}

	/**
	 * Returns true if incoming "concept" is the same concept as all the terms already included in phrase
	 * @param concept the concept to check
	 * @return true if all terms' concepts are the same with incoming concept
	 */
	private boolean sameConcept(String concept) {
		for (ConllLine term : terms) {
			if ((term.getConcept() == null) || (!term.getConcept().equals(concept))){
				return false;
			}
		}
		return true;
	}


	/**
	 * Add ConllLines from an existing ConllPhrase to this object
	 * If there is a line in incoming phrase that has relation that indicates it cannot be added to phrase (newPhraseString),
	 * incoming phrase will not be added and false will be returned
	 *
	 * @param phrase ConllPhrase.
	 * @return boolean. True if ConllPhrase was added successfully. False if there is a line having relation that indicates it cannot be added
	 */
	public boolean add(ConllPhrase phrase) {
		ArrayList<ConllLine> oTerms = phrase.getTerms();
		//first check if all lines can be added, they may have relations that are not allowing phrases addition
		for (ConllLine oTerm : oTerms) {
			if(!isRelToAdd(oTerm.getDepRel())){
				return false;
			}
			if (!sameConcept(oTerm.getConcept()) && oTerm.getPos().contains("NN")){
				return false;
			}
		}
		for (ConllLine oTerm : oTerms) {
			add(oTerm);
		}
		reorganizeTermsOrder();
		return true;
	}

	/**
	 * sort terms according to relations they have, in order phrase to be extracted as syntactical right phrase
	 */
	private void reorganizeTermsOrder() {

		boolean goOn = true;
		int counterGoOn = terms.size();
		// Complexity of this loop is O(n^2)
		while (goOn && (counterGoOn >= 0)) { //counterGoOn is to prevent infinite loop to happen
			goOn = false;
			counterGoOn--;
			for (int j = 0; j < terms.size(); j++) {

				String rel = terms.get(j).getDepRel();
				int head = terms.get(j).getHead();
				int id = terms.get(j).getId();

				if (ids.contains(head)) {
					//positionToAdd = "before"/"after"/"end" //add before/after head's line, end: add it in the end of phrase
					String positionToAdd = "end";
					if (beforeContains(rel)) {
						positionToAdd = "before";
					} else if (afterContains(rel)) {
						positionToAdd = "after";
					}

					//loop for every other term and if find head, proceed
					for (int i = 0; i < terms.size(); i++) {
						if (terms.get(i).getId() != id) { //don't check for the same item
							//if head line is found
							if (head == terms.get(i).getId()) {
								if (positionToAdd.contains("before")) {
									//if term is not in the right place
									if (i - 1 != j) {
										//if there is previous term from i
										//and the previous term is not in the right place, it is not meant to be before i
										if((i == 0) || !(terms.get(i-1).getHead() == terms.get(j).getHead() && beforeContains(terms.get(i-1).getDepRel()) )) {
											terms.add(i, terms.get(j));
											goOn = true;
											//line was added in the right place in terms, and now the old line has to be removed
											if (i < j)
												terms.remove(j + 1);
											else
												terms.remove(j);
											break;
										}
									}
								}
								if (positionToAdd.contains("after")) {
									//if term is not in the right place
									if (i + 1 != j) {
										//if there is previous term from i
										//and the previous term is not in the right place, it is not meant to be before i
										if((i == 0) || !(terms.get(i+1).getHead() == terms.get(j).getHead() && afterContains(terms.get(i+1).getDepRel()) )) {
											terms.add(i+1, terms.get(j));
											goOn = true;
											//line was added in the right place in terms, and now the old line has to be removed
											if (i+1 < j)
												terms.remove(j + 1);
											else
												terms.remove(j);
											break;
										}
									}
								}
							}
						}
					}
					if (goOn) break;
				}
			}
		}
	}

	/**
	 * Check if rel is relation that indicates that line containing this relation have to be added in phrase
	 *
	 * @param rel String. The relation
	 * @return boolean. True is line containing the relation must be added. False if line must not be added
	 */
	private boolean isRelToAdd(String rel) {
		if (beforeContains(rel) || afterContains(rel)) {
			return true;
		}
		return false;
	}

	/**
	 * returns true if phrase contains a id incoming id
	 * @param id int.
	 * @return boolean
	 */
	public boolean containsId(int id) {
		return ids.contains(id);
	}

	/**
	 * Returns true if there is at least one int in ints contained in this phrases ids
	 * @param ints ArrayList<Integer>
	 * @return boolean
	 */
	public boolean containsId(ArrayList<Integer> ints) {
		for (Integer oPhHead : ints) {
			if (ids.contains(oPhHead))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if phrase ids contain id
	 * @param id
	 * @return
	 */
	public boolean containsHead(int id) {
		return heads.contains(id);
	}

	/**
	 * Returns true if dep is contained in relations that in reforming procedure, they are placed before their head
	 * @param dep String.
	 * @return boolean
	 */
	private boolean beforeContains(String dep) {
		for (String s : beforeString) {
			if (!s.equals("")) {
				//dep contains s and not the contrary because:
				//dep can be "conj:or" and s "conj"
				if (dep.contains(s)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if dep is contained in relations that in reforming procedure, they are placed after their head
	 * @param dep String.
	 * @return boolean
	 */
	private boolean afterContains(String dep) {
		for (String s : afterString) {
			if (!s.equals("")) {
				//dep contains s and not the contrary because:
				//dep can be "conj:or" and s "conj"
				if (dep.contains(s)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if dep is contained in relations that in reforming procedure, they are force to create a new phrase
	 * @param dep String.
	 * @return boolean
	 */
	private boolean newPhraseContains(String dep) {
		for (String s : newPhraseString) {
			if (!s.equals("")) {
				//dep contains s and not the contrary because:
				//dep can be "conj:or" and s "conj"
				if (dep.contains(s)) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Integer> getHeads() {
		return new ArrayList<Integer>(this.heads);
	}

	public ArrayList<Integer> getIds() {
		return new ArrayList<Integer>(this.ids);
	}

	public ArrayList<ConllLine> getTerms() {
		return terms;
	}

	/**
	 * Returns current instance in KBItem
	 * @return KBItem.
	 */
	public KBItem getAsKBItem() {
		String termString = "";
		String conceptString = " ";
		String prefix = "";
		for (ConllLine term : terms) {
			prefix = " ";
			termString += prefix + term.getWord();
			if (conceptString.equals(" ") || term.getPos().contains("NN")){
				conceptString = term.getConcept();
			}
		}
		KBItem item = new KBItem(termString,conceptString);
		return item;
	}
}
