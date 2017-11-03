package gr.iti.mklab.kindex.RelExt;

import com.google.gson.Gson;

import java.util.*;

/**
 * Class for handling Annotated Conll Strings
 *
 *   Example of custom annotated Conll:
 * 1	pneumonia	pneumonia	NN	O	3	nsubj [disease or syndrome](MetaMap Annotated)
 * 2	often	often	RB	O	3	advmod
 * 3	have	have	VBP	O	0	ROOT
 * 4	a	a	DT	O	6	det
 * 5	productive	productive	JJ	O	6	amod [sign or symptom](MetaMap Annotated)
 * 6	cough	cough	NN	O	3	dobj
 * 7	,	,	,	O	_	_
 * 8	fever	fever	NN	O	6	appos [finding](MetaMap Annotated)
 *
 * Created by Thodoris Tsompanidis on 21/12/2015.
 */
public class AnnotatedConll {

	/**
	 * Return a list with string. Each String is a sentence in clean annotated Conll
	 *
	 * @param conll String. Annotated conll  String
	 */
	public static  ArrayList<String> cleanAnnotatedConll(String conll){

		ArrayList<String> output= new ArrayList<String>();

		int i =1;
		//every sentence in conll is separated by two new line characters
		String[] sentences = conll.split("\n\n");
		for (String sentence : sentences) {

			String soutput = "";

			if (RelExt.MODE.equals("debug")) {
				System.out.println("-----------------------------");
				System.out.println("Sentence: " + i);
				i++;
				System.out.println(sentence);
				System.out.println("->");
			}

			//Annotated conll will be processed so that only meaningful lines remain.
			//meaningful lines are lines containing concepts OR ROOT elements (from syntactic analysis) OR Verbs

			String[] lines = sentence.split("\n");
			for (String line : lines) {
				//keep the lines where: [concept] OR ROOT OR VB* (Verb)
				if (line.contains("(MetaMap Annotated)") || line.contains("ROOT")|| line.contains("VB")) {
					soutput +=line+"\n";
					if (RelExt.MODE.equals("debug")) {
						System.out.println(line);
					}
				}
			}

			output.add(soutput);

			//soutput is in clean annotated conll format. Clean because it contains only the meaningful lines
			//extractRelationsFromAnnotatedConll(soutput);
			//makeSentencesFromAnnotatedConll(soutput);

		}

		return output;
	}

	/**
	 * Return a list with sentences made of input String
	 * @param input String. A clean annotated conll
	 */
	public static ArrayList<Sentence> makeSentencesFromAnnotatedConll(String input) {

		//input is a clean  annotated conll
		//Do the following:
		// - find all Verbs, and set verbs as "predicate" of the sentence (Class: Sentence)
		// - find connected terms to these verbs, and add them to sentences as subject/object (depending in their dependencies)
		// - find connected terms to last added terms of each sentence, and add them to sentences
		// - proceed that way until there are no more terms to add in sentences.

		if (RelExt.MODE.equals("debug")){
			System.out.println("---------------------------------");
			System.out.println("Clean Conll");
			System.out.println(input);
			System.out.println("---------------------------------");
		}



		//term id (from Connl line)-> Sentence
		Map<Integer, Sentence> sentences = new HashMap(); //map the line_ID to sentence that belongs to
		ArrayList<Sentence> uniqueSentences = new ArrayList<Sentence>(); // array for all sentences

		String[] lines = input.split("\n");
		ArrayList<ConllLine> clines = new ArrayList<ConllLine>();
		for (String line : lines) {
			ConllLine l = new ConllLine(line);
			clines.add(l);
		}

		//first find all verbs and create sentences for each one
		Iterator<ConllLine> clIter = clines.iterator();
		while (clIter.hasNext()) {
			ConllLine cline = clIter.next();
			if (cline.getPos().contains("VB")) {
				Sentence s = new Sentence(cline);
				uniqueSentences.add(s);
				sentences.put(cline.getId(), s);
				clIter.remove();
			}
		}

		//the second step is:
		//If verb of every sentence is not ROOT(syntactic analysis)
		// find verb's head and add it to the sentence

		Iterator<Sentence> sIter = uniqueSentences.iterator();
		while (sIter.hasNext()) {
			Sentence s = sIter.next();
			ConllLine pr = s.getPredicate();
			int head = pr.getHead();
			if (head > 0) {
				clIter = clines.iterator();
				while (clIter.hasNext()) {
					ConllLine cline = clIter.next();
					if (cline.getId() == head) { //this is the head's line
						s.add(cline);
						sentences.put(cline.getId(), s);
						clIter.remove();
						break;
					}
				}
			}
		}


		//add remaining terms to sentences
		boolean go = true;
		int previousClinesSize = clines.size();
		while (go) {
			for (int i = 0; i < clines.size(); i++) {
				if (sentences.get(clines.get(i).getHead()) != null) {
					Sentence s = sentences.get(clines.get(i).getHead());
					s.add(clines.get(i));
					sentences.put(clines.get(i).getId(), s);
					clines.remove(i);
				}
			}
			if (clines.isEmpty() || clines.size() == previousClinesSize)
				go = false;
			previousClinesSize = clines.size();
		}

		Gson gson = new Gson();
		String json;

		//print sentences to json
		if (RelExt.MODE.equals("debug")) {
			json = gson.toJson(uniqueSentences);
			System.out.println("-------------------------------");
			System.out.println("Unique Sentences ");
			System.out.println(json);
			System.out.println("--------------------------------");
		}

		//merge sentences if there is connection between them
		//can make a sentence object of other sentence
		mergeUniqueSentences(uniqueSentences);

		return uniqueSentences;

	}

	/**
	 * Get sentences in KBResponse ready to print items
	 *
	 * @param uniqueSentences
	 * @return
	 */
	public static ArrayList<KBResponse> makeKBResponse(ArrayList<Sentence> uniqueSentences){

		if (RelExt.MODE.equals("debug")) {
			Gson gson = new Gson();
			String json = gson.toJson(uniqueSentences);
			System.out.println("--------------------------------");
			System.out.println("Merged Unique Sentences");
			System.out.println(json);
			System.out.println("--------------------------------");
		}

		//at this point, uniqueSentences are merged, that means that a sentence could have as object other sentence
		//KBSent is an array with KBSentences.
		//KBSentence is a sentence ready to be printed as JSON (in KB exchange model)
		ArrayList<KBSentence> KBSent = mergeTermsInSenetceParts(uniqueSentences);

		ArrayList<KBResponse> response = new ArrayList<KBResponse>();

		for (KBSentence k : KBSent) {
			response.add(k.toKBResponse());
		}

		if (RelExt.MODE.equals("debug")) {
			System.out.println("--------------------");
			System.out.println("KB Response ");

			Gson gson = new Gson();

			for (KBResponse k : response) {
				System.out.println(gson.toJson(k));
			}
			System.out.println("---------------------");
		}

		return response;
	}

	/**
	 * Merge Sentence parts' lines (Object, Subject). It creates ConllPhrases out of merging ConllLines where needed.
	 * Returns a list with KBSentences ready to be printed
	 * @param uniqueSentences ArrayList<Sentence>. List with sentences to be merged
	 * @return  ArrayList<KBSentence>. List with KBSentences ready to be printed
	 */
	private static ArrayList<KBSentence> mergeTermsInSenetceParts(ArrayList<Sentence> uniqueSentences) {
		ArrayList<KBSentence> KBSentences = new ArrayList<KBSentence>();
		Iterator<Sentence> uSIter = uniqueSentences.iterator();//UniqueSentence Iterator
		while (uSIter.hasNext()){
			Sentence s = uSIter.next();
			ArrayList<ConllPhrase> sPhrases = megreTerms(s.getSubject());
			ConllLine predicate=s.getPredicate();
			ArrayList<ConllPhrase> oPhrases = megreTerms(s.getObject());
			ArrayList<KBSentence> osPhrases = mergeTermsInSenetceParts(s.getObjectSentence());
			KBSentences.add(new KBSentence(sPhrases, predicate, oPhrases, osPhrases));
		}
		return KBSentences;
	}

	/**
	 * Gets a list of ConllLines and merge them tou Conll Phrases
	 * Each phrase have at least one ConllLine
	 *
	 * @param clArray ArrayList<ConllLine>. lines to be merged
	 * @return ArrayList<ConllPhrase>. List of Phrases
	 */
	private static ArrayList<ConllPhrase> megreTerms(ArrayList<ConllLine> clArray) {

		//Mapping conllLine Id to ConllPhrase
		HashMap<Integer, ConllPhrase> phraseMapIds = new HashMap<Integer, ConllPhrase>();
		ArrayList<ConllPhrase> phrases = new ArrayList<ConllPhrase>();
		for (ConllLine line : clArray) {
			int chead = line.getHead();
			int cid = line.getId();
			boolean added = false;
			//if line's head is contained already to a ConllPhrase try to add it in that phrase
			if (phraseMapIds.containsKey(chead)){
				ConllPhrase phrase = phraseMapIds.get(chead);
				added = phrase.add(line);
				if (added){
					phraseMapIds.put(cid,phrase);
				}
			}
			//if line was not added in phrase, check if the id is others line head and try to add line to that phrase
			if (!added){
				//for each phrase, check if there phrase.line's head narrowing line's id
				for (ConllPhrase phrase : phrases) {
					if (phrase.containsHead(cid)){
						added = phrase.add(line);
						if(added){
							break;
						}
					}
				}
			}
			//if line is not contained in any ConllPhase, create a new Phrase
			if (!added){
				ConllPhrase cp = new ConllPhrase();
				cp.add(line);
				phraseMapIds.put(line.getId(),cp);
				phrases.add(cp);
			}
		}

		mergePhrasesIfNeeded(phrases);

		//System.out.println("---->");
		//Gson gson = new Gson();
		//String json = gson.toJson(phrases);
		//System.out.println(json);

		return phrases;
	}

	/**
	 * Gets a list with ComllPhrases and merge the ones connected.
	 * @param phrases ArrayList<ConllPhrase>. merge the conntected phrases
	 */
	private static void mergePhrasesIfNeeded(ArrayList<ConllPhrase> phrases) {
		boolean goOn = true;
		while (goOn) {
			goOn = false;
			for (ConllPhrase outerPhrase : phrases) {
				ArrayList<Integer> oPhHeads = outerPhrase.getHeads();
				ArrayList<Integer> oPhIds = outerPhrase.getIds();
				for (ConllPhrase innerPhrase : phrases) {
					//not for the same phrase
					if (!same(oPhIds,innerPhrase.getIds())){
						if (innerPhrase.containsId(oPhHeads)){
							if (innerPhrase.add(outerPhrase)) {
								phrases.remove(outerPhrase);
								goOn=true;
								break;
							}
						}
					}
				}
				if(goOn) break;
			}
		}
	}

	/**
	 * Check if two lists have the same ints
	 * integers are compared by "==" Operator.
	 * Lists don't have to have same objects by reference. Objects can be different.
	 *
	 * @param ints1 ArrayList<Integer>
	 * @param ints2 ArrayList<Integer>
	 * @return boolean. True if they contain the same integers, false if not.
	 */
	private static boolean same(ArrayList<Integer> ints1, ArrayList<Integer> ints2) {
		for (int i = 0; i < ints1.size(); i++) {
			if ((i < ints2.size()) && (ints1.get(i) != ints2.get(i))){
				return false;
			}
		}
		if (ints1.size() == ints2.size()){
			return true;
		}
		return false;
	}


	/**
	 * Checks if there is connection between sentences, and merge them
	 * uniqueSentences now may contain sentences that have other sentences as object
	 * parameter is processed by reference so there is no need of returning anything
	 *
	 * @param uniqueSentences ArrayList<Sentence>. the sentences to be merged.
	 */
	private static void mergeUniqueSentences(ArrayList<Sentence> uniqueSentences) {
		boolean smtChanged = true; //as something changes it is true. finished when false
		while (smtChanged){
			smtChanged=false;
			Iterator<Sentence> uSIter = uniqueSentences.iterator();//UniqueSentence Iterator
			while (uSIter.hasNext()){
				Sentence s = uSIter.next();
				boolean tempBool = mergeIfConnected(uniqueSentences,s);
				if (tempBool){
					if (uniqueSentences.contains(s)){
						uniqueSentences.remove(s);
					}
					smtChanged = true;
					break;
				}
			}
		}
	}

	/**
	 * Check if any sentence subject/predicate headId is Id in any part of the Sentences,
	 * and if yes, sentence is added to proper Sentences.senetence ObjectSentences[] and returns true
	 * else returns false
	 *
	 * @param Sentences ArrayList<Sentence>.
	 * @param sentence Sentence
	 * @return boolean. True if sentence merged to Sentences, else false
	 */
	private static boolean mergeIfConnected (ArrayList<Sentence> Sentences, Sentence sentence) {
		Iterator<Sentence> sIter = Sentences.iterator();
		while (sIter.hasNext()){
			Sentence currentSentence = sIter.next();
			//Sentences may contain sentence, so check for not proceed for the same sentence
			if (currentSentence.getPredicate().getId() != sentence.getPredicate().getId()){
				ArrayList<Integer> heads = sentence.getSPHeads();
				boolean containsHead = currentSentence.containsAtLeastOneId(heads);
				if (containsHead) {
					currentSentence.addObjectSentence(sentence);
					return true;
				}
				else{ // else get all Object Sentences and check there
					ArrayList<Sentence> oSent = currentSentence.getObjectSentence();
					boolean merged = mergeIfConnected(oSent, sentence);
					if (merged){
						if (oSent.contains(sentence)){
							oSent.remove(sentence);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String babelfyKBResponse(ArrayList<KBResponse> kbResp) {
		for (KBResponse kbResponse : kbResp) {
			kbResponse.babelfyMe();
		}

		Gson gson = new Gson();
		if (RelExt.MODE.equals("debug")){
			System.out.println("----------------------");
			System.out.println("BabelFied KBResponse");
			System.out.println(gson.toJson(kbResp));
			System.out.println("----------------------");


		}
		return gson.toJson(kbResp);
	}
}
