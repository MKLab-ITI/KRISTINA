package RelExtFusion;


/**
 * Created by spyridons on 9/23/2016.
 */
public class KBResult {

    private ConceptResult c1;
    private ConceptResult c2;
    private String relation;
    private double relationScore;
    private String pageID;
    private String textID;
    private String url;
    private int sentenceID;
    private String sentenceText;


    public ConceptResult getC1() {
        return c1;
    }

    public void setC1(ConceptResult c1) {
        this.c1 = c1;
    }

    public ConceptResult getC2() {
        return c2;
    }

    public void setC2(ConceptResult c2) {
        this.c2 = c2;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public double getRelationScore() {
        return relationScore;
    }

    public void setRelationScore(double relationScore) {
        this.relationScore = relationScore;
    }

    public String getPageID() {
        return pageID;
    }

    public void setPageID(String pageID) {
        this.pageID = pageID;
    }

    public String getTextID() {
        return textID;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }

    public int getSentenceID() {
        return sentenceID;
    }

    public void setSentenceID(int sentenceID) {
        this.sentenceID = sentenceID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSentenceText() {
        return sentenceText;
    }

    public void setSentenceText(String sentenceText) {
        this.sentenceText = sentenceText;
    }
}
