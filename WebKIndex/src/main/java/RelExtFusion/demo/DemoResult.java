package RelExtFusion.demo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * Created by spyridons on 4/4/2017.
 */
public class DemoResult {
    private String sentence;
    private String sentenceWithConcepts;
    private Multimap<String, String> concepts;
    private List<DemoRelationObject> relations;
    private String json; // for KB

    public DemoResult(){
        concepts = ArrayListMultimap.create();
        relations = new ArrayList<>();
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSentenceWithConcepts() {
        return sentenceWithConcepts;
    }

    public void setSentenceWithConcepts(String sentenceWithConcepts) {
        this.sentenceWithConcepts = sentenceWithConcepts;
    }

    public Map<String, Collection<String>> getConcepts() {
        return concepts.asMap();
    }

    public List<DemoRelationObject> getRelations() {
        return relations;
    }

    public void setConcepts(Multimap<String, String> concepts) {
        this.concepts = concepts;
    }

    public void addConcept(String conceptType, String concept){
        concepts.put(conceptType, concept);
    }

    public void addRelation(DemoRelationObject relation){
        relations.add(relation);
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
