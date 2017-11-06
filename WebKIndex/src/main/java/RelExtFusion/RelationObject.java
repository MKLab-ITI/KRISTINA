package RelExtFusion;

import Weka.SentenceRepresentation;

import java.util.HashMap;

/**
 * Created by spyridons on 12/21/2016.
 */
public class RelationObject {
    private String sentence;
    private String sentenceWithRelations;
    private int replacedTextStartIndexInSentence;
    private int replacedTextEndIndexInSentence;
    private SentenceRepresentation sentenceRepresentation;
    private HashMap<String,Double> rbLabels;
    private HashMap<String,Double> mlLabels;
    private HashMap<String,Double> fusedLabels;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSentenceWithRelations() {
        return sentenceWithRelations;
    }

    public void setSentenceWithRelations(String sentenceWithRelations) {
        this.sentenceWithRelations = sentenceWithRelations;
    }

    public SentenceRepresentation getSentenceRepresentation() {
        return sentenceRepresentation;
    }

    public void setSentenceRepresentation(SentenceRepresentation sentenceRepresentation) {
        this.sentenceRepresentation = sentenceRepresentation;
    }

    public HashMap<String, Double> getRbLabels() {
        return rbLabels;
    }

    public void setRbLabels(HashMap<String, Double> rbLabels) {
        this.rbLabels = rbLabels;
    }

    public HashMap<String, Double> getMlLabels() {
        return mlLabels;
    }

    public void setMlLabels(HashMap<String, Double> mlLabels) {
        this.mlLabels = mlLabels;
    }

    public HashMap<String, Double> getFusedLabels() {
        return fusedLabels;
    }

    public void setFusedLabels(HashMap<String, Double> fusedLabels) {
        this.fusedLabels = fusedLabels;
    }

    public int getReplacedTextStartIndexInSentence() {
        return replacedTextStartIndexInSentence;
    }

    public void setReplacedTextStartIndexInSentence(int replacedTextStartIndexInSentence) {
        this.replacedTextStartIndexInSentence = replacedTextStartIndexInSentence;
    }

    public int getReplacedTextEndIndexInSentence() {
        return replacedTextEndIndexInSentence;
    }

    public void setReplacedTextEndIndexInSentence(int replacedTextEndIndexInSentence) {
        this.replacedTextEndIndexInSentence = replacedTextEndIndexInSentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelationObject that = (RelationObject) o;

        return sentenceWithRelations != null ? sentenceWithRelations.equals(that.sentenceWithRelations) : that.sentenceWithRelations == null;
    }

    @Override
    public int hashCode() {
        return sentenceWithRelations != null ? sentenceWithRelations.hashCode() : 0;
    }
}
