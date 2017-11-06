package Indexing.ContextRetrieval;

/**
 * Created by spyridons on 1/20/2017.
 */
public class DocPassageIDPair {
    private String docID;
    private int segmentID;

    public DocPassageIDPair(){}

    public DocPassageIDPair(String docID, int segmentID){
        this.docID = docID;
        this.segmentID = segmentID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public int getSegmentID() {
        return segmentID;
    }

    public void setSegmentID(int segmentID) {
        this.segmentID = segmentID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocPassageIDPair that = (DocPassageIDPair) o;

        if (segmentID != that.segmentID) return false;
        return docID != null ? docID.equals(that.docID) : that.docID == null;
    }

    @Override
    public int hashCode() {
        int result = docID != null ? docID.hashCode() : 0;
        result = 31 * result + segmentID;
        return result;
    }

    public void printFields(){
        System.out.println("Doc id: " + this.docID + "\tSegment id: " + this.segmentID);
    }
}
