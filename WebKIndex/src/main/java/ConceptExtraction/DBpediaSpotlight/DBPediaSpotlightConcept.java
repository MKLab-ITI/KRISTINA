package ConceptExtraction.DBpediaSpotlight;

import ConceptExtraction.AnnotationConcept;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by spyridons on 11/21/2016.
 */
public class DBPediaSpotlightConcept extends AnnotationConcept {
    private String concept;
    private Set<String> types;

    public DBPediaSpotlightConcept(){}

    public DBPediaSpotlightConcept(String concept, int startIndex, Set<String> types){
        this.concept = concept;
        this.setStartIndex(startIndex);
        this.types = types;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public String getTypesString(){
        if(this.types.size() == 0)
            return "";

        StringBuilder typesSB = new StringBuilder();
        Iterator iter = this.types.iterator();
        while (iter.hasNext()) {
            typesSB.append(iter.next());
            if (iter.hasNext()) {
                typesSB.append(",");
            }
        }

        // returns null for empty types set (it is handled at the start of the method)
        return typesSB.toString();
    }

}
