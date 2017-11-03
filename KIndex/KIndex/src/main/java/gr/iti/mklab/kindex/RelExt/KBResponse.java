package gr.iti.mklab.kindex.RelExt;

import java.util.ArrayList;

/**
 * Class represent the response for Kb
 * A KBResponse contains: Subject, Predicate, Object, and ObjectSentence (KBResponse)
 *
 * Created by Thodoris Tsompanidis on 7/1/2016.
 */
public class KBResponse {
	ArrayList<KBItem> Subject;
	KBItem Predicate;
	ArrayList<KBItem> Object;
	ArrayList<KBResponse> ObjectSentence;

	public KBResponse() {
		this.Subject = new ArrayList<KBItem>();
		this.Object = new ArrayList<KBItem>();
		this.ObjectSentence= new ArrayList<KBResponse>();
	}

	public ArrayList<KBItem> getSubject() {
		return Subject;
	}

	public void setSubject(ArrayList<KBItem> subject) {
		Subject = subject;
	}

	public KBItem getPredicate() {
		return Predicate;
	}

	public void setPredicate(KBItem predicate) {
		Predicate = predicate;
	}

	public ArrayList<KBItem> getObject() {
		return Object;
	}

	public void setObject(ArrayList<KBItem> object) {
		Object = object;
	}

	/**
	 * Adds all ConllPhrases in subject
	 * @param subjects ArrayList<ConllPhrase>. the subjects to be added
	 */
	public void addSubject(ArrayList<ConllPhrase> subjects) {
		for (ConllPhrase s : subjects) {
			this.Subject.add(s.getAsKBItem());
		}
	}

	/**
	 * Adds all ConllPhrases to Object
	 * @param objects ArrayList<ConllPhrase>. Objects to be added
	 */
	public void addObject(ArrayList<ConllPhrase> objects) {
		for (ConllPhrase s : objects) {
			this.Object.add(s.getAsKBItem());
		}
	}

	/**
	 * add ConllLine to predicate
	 * @param predicate
	 */
	public void addPredicate(ConllLine predicate) {
		this.Predicate = predicate.getAsKBItem();
	}

	/**
	 * add KBSentences to ObjectSentence
	 * @param objectSentence ArrayList<KBSentence>. KBSentences to be added
	 */
	public void addObjectFromSentences(ArrayList<KBSentence> objectSentence) {
		for (KBSentence s : objectSentence) {
			KBResponse resp = new KBResponse();
			resp.addSubject(s.getSubject());
			resp.addPredicate(s.getPredicate());
			resp.addObject(s.getObject());
			resp.addObjectFromSentences(s.getObjectSentence());
			ObjectSentence.add(resp);
		}
	}

	public void babelfyMe() {
		for (KBItem kbItem : this.Subject) {
			kbItem.babelfyMe();
		}
		this.Predicate.babelfyMe();
		for (KBItem kbItem : this.Object) {
			kbItem.babelfyMe();
		}
		for (KBResponse kbResponse : this.ObjectSentence) {
			kbResponse.babelfyMe();
		}
	}
}
