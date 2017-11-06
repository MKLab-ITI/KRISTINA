package Weka;


import java.util.ArrayList;

/**
 * Class containing features needed for relation extraction machine learning models
 * Created by spyridons on 9/21/2016.
 */
public class SentenceRepresentation {


    private boolean empty;
    private String text;
    private boolean hasProperNoun;

    private String concept1;
    private int c1Length;
    private String concept2;
    private int c2Length;

    private String c1MetaMapConcept;
    private String c1Pos; //Part of speech
    private boolean isc1Disease;
    private boolean isc1DiseaseLex;
    private boolean isc1Treatment;
    private boolean isc1TreatmentLex;
    private boolean isc1Test;
    private boolean isc1TestLex;
    private String c2MetaMapConcept;
    private String c2Pos; //Part of speech
    private boolean isc2Disease;
    private boolean isc2DiseaseLex;
    private boolean isc2Treatment;
    private boolean isc2TreatmentLex;
    private boolean isc2Test;
    private boolean isc2TestLex;

    private String w1bc1; //word1 before concept1
    private String lw1bc1; //lemma of word1 before concept1
    private String w2bc1;
    private String lw2bc1;
    private String w3bc1;
    private String lw3bc1;

    private String w1ac2; //word1 after concept2
    private String lw1ac2; //lemma of word1 after concept2
    private String w2ac2;
    private String lw2ac2;
    private String w3ac2;
    private String lw3ac2;

    private ArrayList<String> verbsBc1c2; // Verbs between concept1 and concept2
    private String verbbc1; // the first verb before concept1
    private String verbac2; // the first verb after concept2

    private ArrayList<String> conceptsBc1c2; //concepts between concept1 and concept2

    private String semRepRel;

    public SentenceRepresentation()
    {
        verbsBc1c2 = new ArrayList<>();
        conceptsBc1c2 = new ArrayList<>();

        // all representations are NOT considered to be empty unless defined so (by the setEmpty() function)
        empty = false;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public void setEmpty(boolean empty){ this.empty = empty;}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getConcept1() {
        return concept1;
    }

    public void setConcept1(String concept1) {
        this.concept1 = concept1;
    }

    public int getC1Length() {
        return c1Length;
    }

    public void setC1Length(int c1Length) {
        this.c1Length = c1Length;
    }

    public String getConcept2() {
        return concept2;
    }

    public void setConcept2(String concept2) {
        this.concept2 = concept2;
    }

    public int getC2Length() {
        return c2Length;
    }

    public void setC2Length(int c2Length) {
        this.c2Length = c2Length;
    }

    public String getC1MetaMapConcept() {
        return c1MetaMapConcept;
    }

    public void setC1MetaMapConcept(String c1MetaMapConcept) {
        this.c1MetaMapConcept = c1MetaMapConcept;
    }

    public String getC1Pos() {
        return c1Pos;
    }

    public void setC1Pos(String c1Pos) {
        this.c1Pos = c1Pos;
    }

    public String getC2MetaMapConcept() {
        return c2MetaMapConcept;
    }

    public void setC2MetaMapConcept(String c2MetaMapConcept) {
        this.c2MetaMapConcept = c2MetaMapConcept;
    }

    public String getC2Pos() {
        return c2Pos;
    }

    public void setC2Pos(String c2Pos) {
        this.c2Pos = c2Pos;
    }

    public String getW1bc1() {
        return w1bc1;
    }

    public void setW1bc1(String w1bc1) {
        this.w1bc1 = w1bc1;
    }

    public String getLw1bc1() {
        return lw1bc1;
    }

    public void setLw1bc1(String lw1bc1) {
        this.lw1bc1 = lw1bc1;
    }

    public String getW2bc1() {
        return w2bc1;
    }

    public void setW2bc1(String w2bc1) {
        this.w2bc1 = w2bc1;
    }

    public String getLw2bc1() {
        return lw2bc1;
    }

    public void setLw2bc1(String lw2bc1) {
        this.lw2bc1 = lw2bc1;
    }

    public String getW3bc1() {
        return w3bc1;
    }

    public void setW3bc1(String w3bc1) {
        this.w3bc1 = w3bc1;
    }

    public String getLw3bc1() {
        return lw3bc1;
    }

    public void setLw3bc1(String lw3bc1) {
        this.lw3bc1 = lw3bc1;
    }

    public String getW1ac2() {
        return w1ac2;
    }

    public void setW1ac2(String w1ac2) {
        this.w1ac2 = w1ac2;
    }

    public String getLw1ac2() {
        return lw1ac2;
    }

    public void setLw1ac2(String lw1ac2) {
        this.lw1ac2 = lw1ac2;
    }

    public String getW2ac2() {
        return w2ac2;
    }

    public void setW2ac2(String w2ac2) {
        this.w2ac2 = w2ac2;
    }

    public String getLw2ac2() {
        return lw2ac2;
    }

    public void setLw2ac2(String lw2ac2) {
        this.lw2ac2 = lw2ac2;
    }

    public String getW3ac2() {
        return w3ac2;
    }

    public void setW3ac2(String w3ac2) {
        this.w3ac2 = w3ac2;
    }

    public String getLw3ac2() {
        return lw3ac2;
    }

    public void setLw3ac2(String lw3ac2) {
        this.lw3ac2 = lw3ac2;
    }

    public ArrayList<String> getVerbsBc1c2() {
        return verbsBc1c2;
    }

    public void setVerbsBc1c2(ArrayList<String> verbsBc1c2) {
        this.verbsBc1c2 = verbsBc1c2;
    }

    public String getVerbbc1() {
        return verbbc1;
    }

    public void setVerbbc1(String verbbc1) {
        this.verbbc1 = verbbc1;
    }

    public String getVerbac2() {
        return verbac2;
    }

    public void setVerbac2(String verbac2) {
        this.verbac2 = verbac2;
    }

    public ArrayList<String> getConceptsBc1c2() {
        return conceptsBc1c2;
    }

    public void setConceptsBc1c2(ArrayList<String> conceptsBc1c2) {
        this.conceptsBc1c2 = conceptsBc1c2;
    }

    public String getSemRepRel() {
        return semRepRel;
    }

    public void setSemRepRel(String semRepRel) {
        this.semRepRel = semRepRel;
    }

    public void addVerbBetweenc1c2(String verb){
        this.verbsBc1c2.add(verb);
    }

    public void addConceptBetweenc1c2(String verb){
        this.conceptsBc1c2.add(verb);
    }

    public boolean hasProperNoun() {
        return hasProperNoun;
    }

    public void setHasProperNoun(boolean hasProperNoun) {
        this.hasProperNoun = hasProperNoun;
    }

    public boolean isc1Disease() {
        return isc1Disease;
    }

    public void setIsc1Disease(boolean isc1Disease) {
        this.isc1Disease = isc1Disease;
    }

    public boolean isc1DiseaseLex() {
        return isc1DiseaseLex;
    }

    public void setIsc1DiseaseLex(boolean isc1DiseaseLex) {
        this.isc1DiseaseLex = isc1DiseaseLex;
    }

    public boolean isc1Treatment() {
        return isc1Treatment;
    }

    public void setIsc1Treatment(boolean isc1Treatment) {
        this.isc1Treatment = isc1Treatment;
    }

    public boolean isc1TreatmentLex() {
        return isc1TreatmentLex;
    }

    public void setIsc1TreatmentLex(boolean isc1TreatmentLex) {
        this.isc1TreatmentLex = isc1TreatmentLex;
    }

    public boolean isc1Test() {
        return isc1Test;
    }

    public void setIsc1Test(boolean isc1Test) {
        this.isc1Test = isc1Test;
    }

    public boolean isc1TestLex() {
        return isc1TestLex;
    }

    public void setIsc1TestLex(boolean isc1TestLex) {
        this.isc1TestLex = isc1TestLex;
    }

    public boolean isc2Disease() {
        return isc2Disease;
    }

    public void setIsc2Disease(boolean isc2Disease) {
        this.isc2Disease = isc2Disease;
    }

    public boolean isc2DiseaseLex() {
        return isc2DiseaseLex;
    }

    public void setIsc2DiseaseLex(boolean isc2DiseaseLex) {
        this.isc2DiseaseLex = isc2DiseaseLex;
    }

    public boolean isc2Treatment() {
        return isc2Treatment;
    }

    public void setIsc2Treatment(boolean isc2Treatment) {
        this.isc2Treatment = isc2Treatment;
    }

    public boolean isc2TreatmentLex() {
        return isc2TreatmentLex;
    }

    public void setIsc2TreatmentLex(boolean isc2TreatmentLex) {
        this.isc2TreatmentLex = isc2TreatmentLex;
    }

    public boolean isc2Test() {
        return isc2Test;
    }

    public void setIsc2Test(boolean isc2Test) {
        this.isc2Test = isc2Test;
    }

    public boolean isc2TestLex() {
        return isc2TestLex;
    }

    public void setIsc2TestLex(boolean isc2TestLex) {
        this.isc2TestLex = isc2TestLex;
    }
}
