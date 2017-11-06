package MetaMap;

import ConceptExtraction.AnnotationConcept;
import ConceptExtraction.DBpediaSpotlight.DBPediaSpotlightConcept;

import java.util.Comparator;

/**
 * Created by spyridons on 12/21/2016.
 */
public class MetaMapConcept extends AnnotationConcept {
    private int length;
    private String type;

    public MetaMapConcept(){}

    public MetaMapConcept(int startIndex, int length, String type){
        this.setStartIndex(startIndex);
        this.length = length;
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
