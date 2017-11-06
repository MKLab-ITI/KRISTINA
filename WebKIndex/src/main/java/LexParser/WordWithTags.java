package LexParser;

import java.util.ArrayList;

/**
 * Created by spyridons on 8/24/2016.
 */
public class WordWithTags {

    private int index;
    private String text;
    private String lemma;
    private int beginPos;
    private int endPos;
    private String posTag;
    private ArrayList<String> dependencies;

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(int beginPos) {
        this.beginPos = beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public ArrayList<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(String dependency){
        this.dependencies.add(dependency);
    }

    public void printWord(){
        System.out.println("Text: " + this.text + " Pos Tag: " + this.posTag);
    }

}
