package Unitex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spyridons on 8/18/2016.
 */
public class UnitexJniResult {

    private String term;
    private String concept;
    private List<String> BabelNet;
    private List<String> DBPedia;

    public UnitexJniResult()
    {
        this.BabelNet = new ArrayList<>();
        this.DBPedia = new ArrayList<>();
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public List<String> getBabelNet() {
        return BabelNet;
    }

    public void setBabelNet(List<String> babelNet) {
        BabelNet = babelNet;
    }

    public List<String> getDBPedia() {
        return DBPedia;
    }

    public void setDBPedia(List<String> DBPedia) {
        this.DBPedia = DBPedia;
    }

    public void addToBabelNet(String url)
    {
        this.BabelNet.add(url);
    }

    public void addToDBPedia(String url)
    {
        this.DBPedia.add(url);
    }


}
