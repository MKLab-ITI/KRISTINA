package ConceptExtraction;

import ConceptExtraction.DBpediaSpotlight.DBPediaSpotlightConcept;
import MetaMap.MetaMapConcept;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by spyridons on 12/21/2016.
 */
public class AnnotationConcept {
    private int startIndex;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public static Comparator<AnnotationConcept> COMPARE_BY_START_INDEX = new Comparator<AnnotationConcept>() {
        public int compare(AnnotationConcept one, AnnotationConcept other) {
            return one.startIndex - other.startIndex;
        }
    };

    public static List<AnnotationConcept> clearOverlappingAnnotations(List<AnnotationConcept> concepts){
        List<AnnotationConcept> newConcepts = new ArrayList<>(concepts);
        List<AnnotationConcept> toRemove = new ArrayList<>();

        for (int i = 0; i < concepts.size(); i++) {
            AnnotationConcept conceptI = concepts.get(i);
            for (int j = i+1; j < concepts.size(); j++) {
                AnnotationConcept conceptJ = concepts.get(j);
                if(!toRemove.contains(conceptI) && !toRemove.contains(conceptJ)){
                    int conceptIStart = conceptI.getStartIndex();
                    int conceptJStart = conceptJ.getStartIndex();
                    int conceptIEnd = 0;
                    int conceptJEnd = 0;
                    if (conceptI instanceof MetaMapConcept)
                        conceptIEnd = conceptIStart + ((MetaMapConcept) conceptI).getLength();
                    else if (conceptI instanceof DBPediaSpotlightConcept)
                        conceptIEnd = conceptIStart + ((DBPediaSpotlightConcept) conceptI).getConcept().length();
                    if (conceptJ instanceof MetaMapConcept)
                        conceptJEnd = conceptJStart + ((MetaMapConcept) conceptJ).getLength();
                    else if (conceptJ instanceof DBPediaSpotlightConcept)
                        conceptJEnd = conceptJStart + ((DBPediaSpotlightConcept) conceptJ).getConcept().length();

                    if(conceptIEnd > conceptJStart && conceptJEnd > conceptIStart){
                        if (conceptI instanceof MetaMapConcept)
                            toRemove.add(conceptJ);
                        else if (conceptJ instanceof MetaMapConcept)
                            toRemove.add(conceptI);
                        else
                            toRemove.add(conceptJ);
                    }

                }
            }
        }

        newConcepts.removeAll(toRemove);

        return newConcepts;

    }
}
