package LexParser;

import java.util.HashMap;

/**
 * Created by spyridons on 8/29/2016.
 */
public class SentenceWithTags {

    private int id;
    private String text;
    private int beginPos;
    private int endPos;
    private HashMap<Integer, WordWithTags> words;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public HashMap<Integer, WordWithTags> getWords() {
        return words;
    }

    public void setWords(HashMap<Integer, WordWithTags> words) {
        this.words = words;
    }

}
