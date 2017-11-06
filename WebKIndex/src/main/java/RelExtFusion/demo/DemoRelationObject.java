package RelExtFusion.demo;

/**
 * Created by spyridons on 4/4/2017.
 */
public class DemoRelationObject {
    private String sentenceID;
    private String c1;
    private String c2;
    private String relationType;
    private double relationScore;
    private double rbScore;
    private double mlScore;

    public String getSentenceID() {
        return sentenceID;
    }

    public void setSentenceID(String sentenceID) {
        this.sentenceID = sentenceID;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public String getC2() {
        return c2;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public double getRelationScore() {
        return relationScore;
    }

    public void setRelationScore(double relationScore) {
        this.relationScore = relationScore;
    }

    public double getRbScore() {
        return rbScore;
    }

    public void setRbScore(double rbScore) {
        this.rbScore = rbScore;
    }

    public double getMlScore() {
        return mlScore;
    }

    public void setMlScore(double mlScore) {
        this.mlScore = mlScore;
    }
}
