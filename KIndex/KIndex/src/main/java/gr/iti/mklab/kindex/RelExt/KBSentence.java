package gr.iti.mklab.kindex.RelExt;

import java.util.ArrayList;

/**
 * Class represents a sentence (Subject, Predicate, Object) in KBResponse
 *
 * Created by Thodoris Tsompanidis on 7/1/2016.
 */
public class KBSentence {

	ArrayList<ConllPhrase> Subject;
	ConllLine Predicate;
	ArrayList<ConllPhrase> Object;
	ArrayList<KBSentence> ObjectSentence;

	public KBSentence(ArrayList<ConllPhrase> sPhrases, ConllLine predicate, ArrayList<ConllPhrase> oPhrases, ArrayList<KBSentence> osPhrases) {
		this.Subject = sPhrases;
		this.Predicate=predicate;
		this.Object = oPhrases;
		this.ObjectSentence = osPhrases;
	}

	public ArrayList<ConllPhrase> getSubject() {
		return Subject;
	}

	public ConllLine getPredicate() {
		return Predicate;
	}

	public ArrayList<ConllPhrase> getObject() {
		return Object;
	}

	public ArrayList<KBSentence> getObjectSentence() {
		return ObjectSentence;
	}

	/**
	 * Returns a KBResponse in JSON as String
	 * @return String. the instance in KBResponse in JSON
	 */
	public KBResponse toKBResponse(){
		KBResponse resp = new KBResponse();
		resp.addSubject(this.Subject);
		resp.addPredicate(this.Predicate);
		resp.addObject(this.Object);
		resp.addObjectFromSentences(this.ObjectSentence);

		return resp;
		//Gson gson = new Gson();
		//String json = gson.toJson(resp);
		//return json;
	}
}
