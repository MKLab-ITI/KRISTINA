package Indexing.WebAP;


/**
 * Created by Thodoris Tsompanidis on 28/6/2016.
 */
public class WebAPPassage {

	private String type;
	private String text;
	private String relevance;
	private String docID;
	private String sentID; //sentenceID
	private float vsmScore; //Vector Space Model Score
	private float LMDScore; //LMDirichlet Model Score
	private float LMJMScore; //LMJelinekMercer Model Score

	public WebAPPassage(String type, String text, String docID, String SentID, String rel) {
		this.type = type;
		this.text = text;
		this.docID = docID;
		this.sentID = SentID;
		this.relevance = rel;
		/*
		this.vsmScore = 0;
		this.LMDScore = 0;
		this.LMJMScore = 0;*/
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setVsmScore(float vsmScore) {
		this.vsmScore = vsmScore;
	}

	public float getVsmScore() {
		return vsmScore;
	}

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	public float getLMDScore() {
		//if (LMDScore == (float)0.0) return null;
		return LMDScore;
	}

	public void setLMDScore(float LMDScore) {
		this.LMDScore = LMDScore;
	}

	public float getLMJMScore() {
		return LMJMScore;
	}

	public void setLMJMScore(float LMJMScore) {
		this.LMJMScore = LMJMScore;
	}

	public String getRelevance() {
		return relevance;
	}

	public void setRelevance(String relevance) {
		this.relevance = relevance;
	}

	public String getSentID() {
		return sentID;
	}

	public void setSentID(String sentID) {
		this.sentID = sentID;
	}
}
