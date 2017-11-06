package Indexing;


import java.util.Comparator;
import java.util.List;


public class Passage implements Comparable<Passage> {

	private String type;
	private String text;
	private String textResponse;
	private String textWithoutSpaces;
	private int docID;
	private int segmentationID;
	private float vsmScore; //Vector Space Model Score
	private float LMDScore; //LMDirichlet Model Score
	private float LMJMScore; //LMJelinekMercer Model Score
	private float normalizedSum; //sum of LMD + LMJM
	private String textID;
	private String url;
	private String explanationLMD;
	private String explanationLMJM;
	private List<String> matchingTerms;

	public Passage(String type, String text, int docID) {
		this.type = type;
		this.text = text;
		this.textWithoutSpaces = text.replace("\\s+","");
		this.docID = docID;
		this.segmentationID = -1;
		/*
		this.vsmScore = 0;
		this.LMDScore = 0;
		this.LMJMScore = 0;*/
	}

	public Passage(String indexType, String content, int docId, int segID) {
		this.type = indexType;
		this.text = content;
		this.textWithoutSpaces = text.replace("\\s+","");
		this.docID = docId;
		this.segmentationID = segID;
	}

	public Passage(String indexType, String content, int docId, int segID, String textID) {
		this.type = indexType;
		this.text = content;
		this.textWithoutSpaces = text.replace("\\s+","");
		this.docID = docId;
		this.segmentationID = segID;
		this.textID = textID;
	}

	public Passage(String indexType, String content, int docId, int segID, String textID, String url) {
		this.type = indexType;
		this.text = content;
		this.textWithoutSpaces = text.replace("\\s+","");
		this.docID = docId;
		this.segmentationID = segID;
		this.textID = textID;
		this.url = url;
	}

	public Passage(String indexType, String content, String contentResponse,
				   int docId, int segID, String textID, String url) {
		this.type = indexType;
		this.text = content;
		this.textResponse = contentResponse;
		this.textWithoutSpaces = text.replace("\\s+","");
		this.docID = docId;
		this.segmentationID = segID;
		this.textID = textID;
		this.url = url;
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

	public String getTextResponse() {
		return textResponse;
	}

	public void setTextResponse(String textResponse) {
		this.textResponse = textResponse;
	}

	public void setVsmScore(float vsmScore) {
		this.vsmScore = vsmScore;
	}

	public float getVsmScore() {
		return vsmScore;
	}

	public int getDocID() {
		return docID;
	}

	public void setDocID(int docID) {
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

	public int getSegmentationID() {
		return segmentationID;
	}

	public void setSegmentationID(int segmentationID) {
		segmentationID = segmentationID;
	}

	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		this.textID = textID;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getExplanationLMD() {
		return explanationLMD;
	}

	public void setExplanationLMD(String explanationLMD) {
		this.explanationLMD = explanationLMD;
	}

	public String getExplanationLMJM() {
		return explanationLMJM;
	}

	public void setExplanationLMJM(String explanationLMJM) {
		this.explanationLMJM = explanationLMJM;
	}

	public List<String> getMatchingTerms() {
		return matchingTerms;
	}

	public void setMatchingTerms(List<String> matchingTerms) {
		this.matchingTerms = matchingTerms;
	}

	public String getMatchingTermsString(){
		String matchingTermsString = "";
		String prefix = "";
		for (String term :
				this.matchingTerms) {
			matchingTermsString += prefix + term;
			prefix = " ";
		}
		return matchingTermsString;
	}

	public void computeNormalizedSum(float maxLMD, float maxLMJM){
		this.normalizedSum = (this.LMDScore / maxLMD + this.LMJMScore / maxLMJM) / 2;
	}

	public float getNormalizedSum() {
		return normalizedSum;
	}

	public void equalizeScores(Object o){
		Passage passage = (Passage) o;

		// check Vsm
		if (this.getVsmScore() > passage.getVsmScore())
			passage.setVsmScore(this.getVsmScore());
		else
			this.setVsmScore(passage.getVsmScore());

		// check LMJM
		if (this.getLMJMScore() > passage.getLMJMScore())
		{
			passage.setLMJMScore(this.getLMJMScore());
			passage.setExplanationLMJM(this.getExplanationLMJM());
		}
		else
			{
			this.setLMJMScore(passage.getLMJMScore());
			this.setExplanationLMJM(passage.getExplanationLMJM());
		}

		// check LMD
		if (this.getLMDScore() > passage.getLMDScore())
		{
			passage.setLMDScore(this.getLMDScore());
			passage.setExplanationLMD(this.getExplanationLMD());
		}
		else
		{
			this.setLMDScore(passage.getLMDScore());
			this.setExplanationLMD(passage.getExplanationLMD());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Passage passage = (Passage) o;

		boolean equal =  textWithoutSpaces != null ? textWithoutSpaces.equals(passage.textWithoutSpaces) : passage.textWithoutSpaces == null;

		// if they are equal, toggle scores to be equal to the highest score
		if(equal)
			equalizeScores(o);

		return equal;
	}

	@Override
	public int hashCode() {
		return textWithoutSpaces != null ? textWithoutSpaces.hashCode() : 0;
	}

	public int compareTo(Passage o)
	{
		return Float.compare(this.LMJMScore, o.getLMJMScore());
	}

	public static Comparator<Passage> COMPARE_BY_LMD_SCORE = new Comparator<Passage>() {
		public int compare(Passage one, Passage other) {
			return Float.compare(one.LMDScore, other.LMDScore);
		}
	};

	public static Comparator<Passage> COMPARE_BY_SCORES_SUM = new Comparator<Passage>() {
		public int compare(Passage one, Passage other) {
			return Float.compare(one.LMDScore + one.LMJMScore, other.LMDScore + other.LMJMScore);
		}
	};

	public static Comparator<Passage> COMPARE_BY_NORMALIZED_SUM = new Comparator<Passage>() {
		public int compare(Passage one, Passage other) {
			return Float.compare(one.normalizedSum, other.normalizedSum);
		}
	};

	public double getScoresSum(){return this.getLMDScore() + this.getLMJMScore();}
}
