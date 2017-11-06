package RelExtFusion;

import ConceptExtraction.AnnotationConcept;
import ConceptExtraction.DBpediaSpotlight.DBPediaSpotlightConcept;
import ConceptExtraction.DBpediaSpotlight.DBpediaSpotlightHandler;
import Functions.FileFunctions;
import KnowledgeBase.KBConstants;
import MetaMap.LocalMetaMapNewHandler;
import MetaMap.MetaMapCONSTANTS;
import MetaMap.MetaMapConcept;
import RelExtFusion.demo.DemoRelationObject;
import RelExtFusion.demo.DemoResult;
import Unitex.UnitexConstants;
import com.google.gson.Gson;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import Functions.MapFunctions;
import Functions.XmlFunctions;
import LexParser.NLPFunctions;
import LexParser.NounPhraseDetector;
import LexParser.SentenceWithTags;
import LexParser.WordWithTags;
import MetaMap.LocalMetaMapHandler;
import Unitex.UnitexJniCaller;
import Weka.SentenceRepresentation;
import Weka.WekaHandler;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.management.relation.Relation;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 9/19/2016.
 */
public class RelExtPipeline {

    private RelExtHandler relext;
    private UnitexJniCaller ujc;
    private Pattern regexForTagsToRemove;
    private WekaHandler wekaHandler;
    private List<String> diseaseListLex;
    private List<String> treatmentListLex;
    private List<String> testListLex;

    public RelExtPipeline() {
        loadLexicons();
        relext = new RelExtHandler(RelExtConstants.pipelineWeights);
        ujc = new UnitexJniCaller();
        //build regex
        StringBuilder patternSB = new StringBuilder();
        patternSB.append("<(");
        for(String tag: KBConstants.tagsToRemove) {
            patternSB.append(tag + "|");
        }
        Iterator<String> itemIterator = Arrays.asList(KBConstants.tagsToRemove).iterator();
        while (itemIterator.hasNext()) {
            String tag = itemIterator.next();
            patternSB.append("/" + tag);
            if (itemIterator.hasNext()) {
                patternSB.append("|");
            }
        }
        patternSB.append(")>");
        String regexp = patternSB.toString();
        this.regexForTagsToRemove = Pattern.compile(regexp);
        wekaHandler = new WekaHandler();
    }

    private void loadLexicons(){

        String diseaseFileContent = FileFunctions.readInputFile(RelExtConstants.diseaseLexiconFile);
        diseaseListLex = Arrays.asList(diseaseFileContent.split("\r\n"));
        String treatmentFileContent = FileFunctions.readInputFile(RelExtConstants.treatmentLexiconFile);
        treatmentListLex = Arrays.asList(treatmentFileContent.split("\r\n"));
        String testFileContent = FileFunctions.readInputFile(RelExtConstants.testLexiconFile);
        testListLex = Arrays.asList(testFileContent.split("\r\n"));

    }


    /**
     * new pipeline developed at December 2016
     * @param text
     * @param pageID
     * @param textID
     * @param url
     * @return
     */
    public ArrayList<KBResult> executeNewPipeline(String text, String pageID, String textID, String url){
        try {
            // program start
            System.out.println("Relation extraction START");
            long start = System.currentTimeMillis();

            // split sentences
            ArrayList<String> sentences = splitSentences(text);

            if (sentences.size() > KBConstants.maxSentenceLimit) {
                System.out.println("Url contains too many sentences...");
                System.out.println("Stopping pipeline for this url...");
                return new ArrayList<>();
            }

            String textWithLineBreaks = "";
            for (String sentence : sentences) {
                textWithLineBreaks += sentence + "{S} \n";
            }

            // annotate text with MetaMap
            System.out.print("Extracting concepts...");
            String annotatedWithConcepts = getMetaMapConcepts(textWithLineBreaks);
//            String annotatedWithConcepts = "";
            System.out.println(" DONE!!!");

            // extract unitex relations
            System.out.print("Extracting unitex relations... ");
            Map<Integer,Set<RelationObject>> relationsMap = getUnitexRelationsMap(sentences, annotatedWithConcepts);
            System.out.print("Pruning duplicate relations... ");
            for (Map.Entry<Integer,Set<RelationObject>> entry : relationsMap.entrySet())
            {
                int key = entry.getKey();
                Set<RelationObject> newSet = pruneRelationSet(entry.getValue());
                relationsMap.put(key, newSet);
            }
            System.out.println(" DONE!!!");

            // annotate relation objects
            System.out.print("Extracting annotations (rule based, machine learning and fusion) ...");
            annotateRelationsMap(relationsMap);
            System.out.println(" DONE!!!");

            // extract relation and add it to KB response
            System.out.print("Forming KB input...");
            ArrayList<KBResult> knowledgeBaseInput = formKBInputFromRelObjects(relationsMap, pageID, textID, url);
            System.out.println(" DONE!!!");

            // print elapsed time and program end
            System.out.println("Relation extraction END");
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Elapsed time (millis): " + elapsed);

            return knowledgeBaseInput;
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Exception thrown in the pipeline!!!");
            return new ArrayList<>();
        }
    }

    public DemoResult executeDemoPipeline(String text, boolean json){
        try {
            // program start
            System.out.println("Relation extraction START");
            long start = System.currentTimeMillis();

            DemoResult result = new DemoResult();

            if(text.trim().equals("")){
                System.out.println("Relation extraction END");
                return result;
            }

            // split sentences
            ArrayList<String> sentences = new ArrayList<>();
            sentences.add(text);


            // annotate text with MetaMap
            System.out.print("Extracting concepts...");
            LocalMetaMapNewHandler metamap = annotateWithMetamap(text);
            String annotatedWithConcepts = metamap.getXmlConcepts();
            result.setSentence(text);
            result.setSentenceWithConcepts(annotatedWithConcepts
                    .replaceAll("<concept[^>]*>","<span style=\"background-color:yellow\">")
                    .replaceAll("</concept>","</span>"));
            result.setConcepts(metamap.getConceptsMap());
            System.out.println(" DONE!!!");

            // extract unitex relations
            System.out.print("Extracting unitex relations... ");
            Map<Integer,Set<RelationObject>> relationsMap = getUnitexRelationsMap(sentences, annotatedWithConcepts);
            System.out.print("Pruning duplicate relations... ");
            for (Map.Entry<Integer,Set<RelationObject>> entry : relationsMap.entrySet())
            {
                int key = entry.getKey();
                Set<RelationObject> newSet = pruneRelationSet(entry.getValue());
                relationsMap.put(key, newSet);
            }
            System.out.println(" DONE!!!");

            // annotate relation objects
            System.out.print("Extracting annotations (rule based, machine learning and fusion) ...");
            annotateRelationsMap(relationsMap);
            System.out.println(" DONE!!!");

            // extract relation and add it to KB response
            if(json){
                System.out.print("Forming KB input...");
                ArrayList<KBResult> knowledgeBaseInput = formKBInputFromRelObjects(relationsMap, "test", "test", "www.test.com");
                System.out.println(" DONE!!!");
                result.setJson(new Gson().toJson(knowledgeBaseInput));
            }
            else{
                System.out.print("Forming demo output...");
                for (Map.Entry<Integer, Set<RelationObject>> entry : relationsMap.entrySet()){
                    int sentenceID = entry.getKey();
                    Set<RelationObject> relationObjects = entry.getValue();
                    for (RelationObject relationObject : relationObjects){
                        SentenceRepresentation sentence = relationObject.getSentenceRepresentation();
                        DemoRelationObject demoRelationObject = new DemoRelationObject();
                        demoRelationObject.setC1(sentence.getConcept1());
                        demoRelationObject.setC2(sentence.getConcept2());

                        // find relation (label with max score)
                        HashMap<String,Double> fusedLabelMap = relationObject.getFusedLabels();
                        HashMap<String,Double> rbLabelMap = relationObject.getRbLabels();
                        HashMap<String,Double> mlLabelMap = relationObject.getMlLabels();
                        if(fusedLabelMap.size()==0){
                            demoRelationObject.setRelationType("None");
                            demoRelationObject.setRbScore(0.0);
                            demoRelationObject.setMlScore(0.0);
                            demoRelationObject.setRelationScore(0.0);
                        }
                        else{
                            Map.Entry<String,Double> maxEntry = MapFunctions.getMaxEntry(fusedLabelMap);
                            String relationType = maxEntry.getKey();
                            demoRelationObject.setRelationType(RelExtConstants.relationsExplained.get(relationType)
                                    + " (" + relationType + ")");
                            demoRelationObject.setRelationScore(maxEntry.getValue());
                            demoRelationObject.setRbScore(rbLabelMap.get(relationType));
                            demoRelationObject.setMlScore(mlLabelMap.get(relationType));
                        }
                        result.addRelation(demoRelationObject);
                    }
                }
                System.out.println(" DONE!!!");
            }

            // print elapsed time and program end
            System.out.println("Relation extraction END");
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Elapsed time (millis): " + elapsed);

            return result;
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Exception thrown in the pipeline!!!");
            System.out.println("Relation extraction END");
            return new DemoResult();
        }
    }

    /**
     * split text into its sentences using the stanford parser
     * @param text
     * @return
     */
    private ArrayList<String> splitSentences(String text){
        ArrayList<String> sentencesList = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        // shut off the annoying intialization messages
        RedwoodConfiguration.empty().capture(System.err).apply();

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // enable stderr again
        RedwoodConfiguration.current().clear().apply();

        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println("Number of sentences detected: " + sentences.size());
        for (CoreMap sentence : sentences) {

            // get sentence text and add it to the sentences list
            String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);

            // clear line breaks inside sentence (replace it with a tab character)
            sentenceText = sentenceText.replace("\n","\t");

            // remove non-ascii characters
            sentenceText = Normalizer.normalize(sentenceText, Normalizer.Form.NFD);
            sentenceText = sentenceText.replaceAll("[^\\x00-\\x7F]", "");

            sentencesList.add(sentenceText);
        }
        return sentencesList;
    }

    private LocalMetaMapNewHandler annotateWithMetamap(String text){
        LocalMetaMapNewHandler metamap = new LocalMetaMapNewHandler();
        metamap.parse(text);
        return metamap;
    }

    private String getMetaMapConcepts(String text){
        String annotatedWithConcepts = LocalMetaMapHandler.getAnnotatedXMLTags(text);
        return annotatedWithConcepts;
    }

    public String getDBPediaSpotlightConcepts(String text, ArrayList<String> selectTypes){
        DBpediaSpotlightHandler dbpedia = new DBpediaSpotlightHandler();
        String annotatedWithConcepts = dbpedia.getAnnotationsXML(text, KBConstants.language, "0.3", selectTypes);
        return annotatedWithConcepts;
    }

    /**
     * get both dbpedia spotlight and metamap concepts
     * @param text
     * @return
     */
    private String getConcepts(String text){
        // get concepts from the two extractors, merge them into one list
        // and sort them based on the start index of the annotated word
        List<MetaMapConcept> metamapConcepts = LocalMetaMapHandler.getAnnotations(text);
        ArrayList<String> selectTypes = new ArrayList<>(Arrays.asList(KBConstants.conceptTypesDBPedia));
        DBpediaSpotlightHandler dbpedia = new DBpediaSpotlightHandler();
        List<DBPediaSpotlightConcept> dbpediaConcepts = dbpedia.getAnnotations(text,"en", "0.3", selectTypes);
        List<AnnotationConcept> allConcepts = new ArrayList<>();
        allConcepts.addAll(metamapConcepts);
        allConcepts.addAll(dbpediaConcepts);
        Collections.sort(allConcepts, AnnotationConcept.COMPARE_BY_START_INDEX);

        // form xml tagged text
        String annotatedWithConcepts = text;
        int charOffset = 0; // the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
        HashSet<Integer> startPos = new HashSet<Integer>();
        for (AnnotationConcept concept: allConcepts){
            int initialTextLength = annotatedWithConcepts.length();
            int start = concept.getStartIndex();

            // DBPediaSpotlightConcept and MetaMapConcept have different fields and they should be handled in a different way
            if(concept instanceof DBPediaSpotlightConcept){
                DBPediaSpotlightConcept dbpediaConcept = (DBPediaSpotlightConcept) concept;
                int length = dbpediaConcept.getConcept().length();
                String typesString = dbpediaConcept.getTypesString();

                String prev = annotatedWithConcepts.substring(0, start + charOffset);
                String conceptTerms = annotatedWithConcepts.substring(start + charOffset, start + length + charOffset );
                String next = annotatedWithConcepts.substring(start + length + charOffset, annotatedWithConcepts.length());

                // words must not be cut
                if ((prev.equals("") || prev.endsWith(" ")) && (next.startsWith(" ") || next.matches("\\p{Punct} .*") || next.startsWith("\r\n") || next.startsWith("\n"))) {
                    annotatedWithConcepts = prev + "<concept DBPedia=\"" + typesString + "\">" + conceptTerms + "</concept>" + next;
                    charOffset += annotatedWithConcepts.length() - initialTextLength;
                }
            }
            else if(concept instanceof MetaMapConcept){
                MetaMapConcept metaMapConcept = (MetaMapConcept) concept;
                int length = metaMapConcept.getLength();
                String typeString = metaMapConcept.getType();

                String prev = annotatedWithConcepts.substring(0, start + charOffset);
                String conceptTerms = annotatedWithConcepts.substring(start + charOffset, start + length + charOffset );
                String next = annotatedWithConcepts.substring(start + length + charOffset, annotatedWithConcepts.length());

                // words must not be cut
                if ((prev.equals("") || prev.endsWith(" ")) && (next.startsWith(" ") || next.matches("\\p{Punct} .*") || next.startsWith("\r\n") || next.startsWith("\n"))) {
                    annotatedWithConcepts = prev + "<concept MetaMap=\"" + typeString + "\">" + conceptTerms + "</concept>" + next;
                    charOffset += annotatedWithConcepts.length() - initialTextLength;
                }
            }
        }
        return annotatedWithConcepts;
    }

    /**
     * get a map containing the extracted relations from each sentence
     * map format: <sentence_id, list of relation in RelationObject format>
     * @param sentences
     * @param annotatedWithConcepts
     * @return
     */
    private Map<Integer, Set<RelationObject>> getUnitexRelationsMap(List<String> sentences, String annotatedWithConcepts){

        // create list of tags that should NOT be removed
        List<String> notToRemoveList = new ArrayList<>();
        for(String label: RelExtConstants.labelsWithNum){
            notToRemoveList.add(label);
            notToRemoveList.add("/"+label);
        }
        notToRemoveList.add("E1");
        notToRemoveList.add("/E1");
        notToRemoveList.add("E2");
        notToRemoveList.add("/E2");

        Map<Integer, Set<RelationObject>> relationsMap = new TreeMap<>();

        List<String> sentencesWithConcepts = Arrays.asList(annotatedWithConcepts.split("\\{S\\} \n"));

        // sequence that marks end of a sentence in unitex input and output
        String endOfSentence = "{S} \n";

        String annotatedWithRelations = ujc.getUnitexAnnotationsFromText(annotatedWithConcepts,"Master_WHOLE_service.fst2");

        // remove tags that cause problems in the relation extraction process
        StringBuffer sb = new StringBuffer();
        Matcher m = this.regexForTagsToRemove.matcher(annotatedWithRelations);
        while (m.find())
            m.appendReplacement(sb, "");
        m.appendTail(sb);
        String annotatedWithRelationsFiltered = sb.toString();

        List<String> relationLines = Arrays.asList(annotatedWithRelationsFiltered.split("\r\n"));
        relationLines = Arrays.asList(annotatedWithRelationsFiltered.split("\n"));
        for (String relationLine: relationLines){

            if(relationLine.equals(""))
                continue;

            // get indices in full text
            String indices = relationLine.substring(0, relationLine.indexOf("\t"));
            String replacedText = relationLine.substring(relationLine.indexOf("\t") + 1);
            replacedText = cleanseUnitexText(replacedText);
//            replacedText = clearSpecificXML(replacedText, notToRemoveList);
            String[] beginEndSplit = indices.split(" ");
            int beginIndex = Integer.parseInt(beginEndSplit[0]);
            int endIndex = Integer.parseInt(beginEndSplit[1]);

            // get sentence id by counting the {S} sentence breaks
            String textBeforeRelation = annotatedWithConcepts.substring(0, beginIndex);
            int sentenceID = StringUtils.countMatches(textBeforeRelation, "{S}");

            // get indices in sentence
            int sentenceStart = 0;
            if(sentenceID!=0)
                sentenceStart = textBeforeRelation.lastIndexOf(endOfSentence) + endOfSentence.length();
            int beginIndexInSentence = beginIndex - sentenceStart;
            int endIndexInSentence = endIndex - sentenceStart;

            int c1Count = StringUtils.countMatches(replacedText, "<E1>");
            int c2Count = StringUtils.countMatches(replacedText, "<E2>");

            if(c1Count == 1 && c2Count == 1){
                // get full sentence
                sb = new StringBuffer(sentencesWithConcepts.get(sentenceID));
                sb.replace(beginIndexInSentence, endIndexInSentence, replacedText);
                String sentenceWithRelations = sb.toString();

                // form relation object
                RelationObject relationObject = new RelationObject();
                String originalSentence = sentences.get(sentenceID);
                relationObject.setSentence(originalSentence);
                relationObject.setSentenceWithRelations(sentenceWithRelations);
                relationObject.setReplacedTextStartIndexInSentence(beginIndexInSentence);
                relationObject.setReplacedTextEndIndexInSentence(endIndexInSentence);

                // add to relations map
                if(relationsMap.containsKey(sentenceID)) {
                    relationsMap.get(sentenceID).add(relationObject);
                }
                else{
                    relationsMap.put(sentenceID, new HashSet<>());
                    relationsMap.get(sentenceID).add(relationObject);
                }
            }
            else{
                // get reformed relation lines
                // create relation object for each line
                List<String> reformedRelationLines = getRelationLines(replacedText);
                for (String reformedRelationLine : reformedRelationLines){
                    // get full sentence
                    sb = new StringBuffer(sentencesWithConcepts.get(sentenceID));
                    sb.replace(beginIndexInSentence, endIndexInSentence, reformedRelationLine);
                    String sentenceWithRelations = sb.toString();

                    // form relation object
                    RelationObject relationObject = new RelationObject();
                    String originalSentence = sentences.get(sentenceID);
                    relationObject.setSentence(originalSentence);
                    relationObject.setSentenceWithRelations(sentenceWithRelations);
                    relationObject.setReplacedTextStartIndexInSentence(beginIndexInSentence);
                    relationObject.setReplacedTextEndIndexInSentence(endIndexInSentence);

                    // add to relations map
                    if(relationsMap.containsKey(sentenceID)) {
                        relationsMap.get(sentenceID).add(relationObject);
                    }
                    else{
                        relationsMap.put(sentenceID, new HashSet<>());
                        relationsMap.get(sentenceID).add(relationObject);
                    }
                }

            }

        }

        return relationsMap;
    }

    /**
     * 1) replace tags not closed properly
     * 2) delete any xml tags inside <concept></concept> tags
     * @param initialText
     * @return
     */
    private String cleanseUnitexText(String initialText){

        List<Node> toRemove = new ArrayList<>();

        String correctText = initialText.replace("</E1></concept>","</concept></E1>"); // text with correct tag ordering
        correctText = correctText.replace("</E2></concept>","</concept></E2>");
        correctText = correctText.replaceAll("<concept([^>]*)><E([12])>", "<E$2><concept$1>");

        //separate text before first E1 or E2 (next parse will be made on text after the first E1/2)
        Pattern p = Pattern.compile("<E([12])>");  // insert your pattern here
        Matcher m = p.matcher(correctText);
        m.find(); // we are sure that it will find at least one E1 or E2
        int position = m.start();
        String beforeFirstConcept = correctText.substring(0,position);
        String toClean = correctText.substring(position);
        toClean = "<xml>" + toClean + "</xml>";

        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            // spaces between xml are considered as separate xml elements
            is.setCharacterStream(new StringReader(toClean));
            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("concept");
            for (int i = 0 ; i < nodes.getLength() ; i++){
                NodeList children = nodes.item(i).getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if(child instanceof Element)
                        toRemove.add(child);
                }
            }
            for (int i = 0; i < toRemove.size(); i++) {
                Node remove = toRemove.get(i);
                Text text = doc.createTextNode(remove.getTextContent());
                remove.getParentNode().replaceChild(text, remove);
            }
            String cleanedText = XmlFunctions.node2String(doc.getDocumentElement()).replace("<xml>","").replace("</xml>","");
            return beforeFirstConcept + cleanedText;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Exception caught while cleaning the text!!! Initial text will be returned.");
            return initialText;
        }

    }

    /**
     * get multiple relation lines if they contain multiple E1 or E2
     * @param replacedText
     * @return
     */
    private List<String> getRelationLines(String replacedText){

        // create list of tags that should NOT be removed between concepts
        List<String> notToRemoveList = new ArrayList<>();
        for(String label: RelExtConstants.labelsWithNum){
            notToRemoveList.add(label);
            notToRemoveList.add("/"+label);
        }

        List<String> relationLines = new ArrayList<>();

        // find E1 and E2 tag indices
        List<Integer> c1StartTagStartIndices = new ArrayList<>();
        List<Integer> c2StartTagStartIndices = new ArrayList<>();
        List<Integer> c1EndTagEndIndices = new ArrayList<>();
        List<Integer> c2EndTagEndIndices = new ArrayList<>();

        String checkedTag = "<E1>";
        int index = replacedText.indexOf(checkedTag);
        while(index >= 0) {
            c1StartTagStartIndices.add(index);
            index = replacedText.indexOf(checkedTag, index+1);
        }

        checkedTag = "<E2>";
        index = replacedText.indexOf(checkedTag);
        while(index >= 0) {
            c2StartTagStartIndices.add(index);
            index = replacedText.indexOf(checkedTag, index+1);
        }

        checkedTag = "</E1>";
        index = replacedText.indexOf(checkedTag);
        while(index >= 0) {
            c1EndTagEndIndices.add(index + checkedTag.length());
            index = replacedText.indexOf(checkedTag, index + checkedTag.length() + 1);
        }

        checkedTag = "</E2>";
        index = replacedText.indexOf(checkedTag);
        while(index >= 0) {
            c2EndTagEndIndices.add(index + checkedTag.length());
            index = replacedText.indexOf(checkedTag, index + checkedTag.length() + 1);
        }

        for (int i = 0; i < c1StartTagStartIndices.size() ; i++){
            int c1StartTagStartIndex = c1StartTagStartIndices.get(i);
            int c1EndTagEndIndex = c1EndTagEndIndices.get(i);
            for (int j = 0; j < c2StartTagStartIndices.size() ; j++){
                int c2StartTagStartIndex = c2StartTagStartIndices.get(j);
                int c2EndTagEndIndex = c2EndTagEndIndices.get(j);

                if(c1StartTagStartIndex < c2StartTagStartIndex){
                    String beforeFirstConcept = replacedText.substring(0, c1StartTagStartIndex);
                    String beforeFirstConceptNoXML = XmlFunctions.clearXML(beforeFirstConcept);
                    String firstConcept = replacedText.substring(c1StartTagStartIndex, c1EndTagEndIndex);
                    String betweenConcepts = replacedText.substring(c1EndTagEndIndex, c2StartTagStartIndex);
                    String betweenConceptsNoXML = clearSpecificXML(betweenConcepts, notToRemoveList);
                    String secondConcept = replacedText.substring(c2StartTagStartIndex, c2EndTagEndIndex);
                    String afterSecondConcept = replacedText.substring(c2EndTagEndIndex);
                    String afterSecondConceptNoXML = XmlFunctions.clearXML(afterSecondConcept);
                    String relationLine = beforeFirstConceptNoXML + firstConcept + betweenConceptsNoXML
                            + secondConcept + afterSecondConceptNoXML;
                    relationLines.add(relationLine);
                }
                else{
                    String beforeFirstConcept = replacedText.substring(0, c2StartTagStartIndex);
                    String beforeFirstConceptNoXML = XmlFunctions.clearXML(beforeFirstConcept);
                    String firstConcept = replacedText.substring(c2StartTagStartIndex, c2EndTagEndIndex);
                    String betweenConcepts = replacedText.substring(c2EndTagEndIndex, c1StartTagStartIndex);
                    String betweenConceptsNoXML = clearSpecificXML(betweenConcepts, notToRemoveList);
                    String secondConcept = replacedText.substring(c1StartTagStartIndex, c1EndTagEndIndex);
                    String afterSecondConcept = replacedText.substring(c1EndTagEndIndex);
                    String afterSecondConceptNoXML = XmlFunctions.clearXML(afterSecondConcept);
                    String relationLine = beforeFirstConceptNoXML + firstConcept + betweenConceptsNoXML
                            + secondConcept + afterSecondConceptNoXML;
                    relationLines.add(relationLine);
                }
            }
        }

        return relationLines;
    }

    /***
     * Xml tags remover configured for the needs of the relation extraction pipeline
     * @param text
     * @return
     */
    private String clearSpecificXML(String text, List<String> notToRemoveList) {

        //build regex to clear xml
        String regexp = "<[^>]+>";
        Pattern regexForUnitexLabels = Pattern.compile(regexp);

        StringBuffer sb = new StringBuffer();
        Matcher m = regexForUnitexLabels.matcher(text);

        while (m.find()) {
            String matched = m.group(0);
            String tag = matched.substring(1, matched.length() - 1);
            if(!(notToRemoveList.contains(tag)))
                m.appendReplacement(sb, "");
        }
        m.appendTail(sb);

        String textWithoutXML = sb.toString();
        return textWithoutXML;
    }

    /**
     * Handle overlapping concepts and relations in RelationObject instances referring to the same sentence
     */
    private Set<RelationObject> pruneRelationSet(Set<RelationObject> relations){
        ArrayList<RelationObject> relationsList = new ArrayList<>(relations);
        ArrayList<RelationObject> newRelationsList = new ArrayList<>();
        for (int i = 0; i< relationsList.size(); i++){
            RelationObject first = relationsList.get(i);
            boolean keepRelation = true;
            for (int j = 0 ; j < relationsList.size() ; j++){
                if(j!=i){
                    RelationObject second = relationsList.get(j);
                    if(first.getReplacedTextStartIndexInSentence() == second.getReplacedTextStartIndexInSentence()
                            && first.getReplacedTextEndIndexInSentence() == second.getReplacedTextEndIndexInSentence()){
                        if(isFirstRelationOverlapped(first,second)) {
                            keepRelation = false;
                            break;
                        }
                    }
                }
            }
            if(keepRelation)
                newRelationsList.add(first);
        }

        return new HashSet<>(newRelationsList);
    }

    /**
     * finds if any of the concepts of the second RelationObject overlaps the respective of the first RelationObject
     * if true, first RelationObject will be deleted from the set
     * @param first
     * @param second
     * @return
     */
    private boolean isFirstRelationOverlapped(RelationObject first, RelationObject second){
        String sentenceFirst = first.getSentenceWithRelations();
        String sentenceSecond = second.getSentenceWithRelations();
        String c1First = XmlFunctions.clearXML(sentenceFirst.substring(sentenceFirst.indexOf("<E1>") + 4 , sentenceFirst.indexOf("</E1>")));
        String c1Second = XmlFunctions.clearXML(sentenceSecond.substring(sentenceSecond.indexOf("<E1>") + 4 , sentenceSecond.indexOf("</E1>")));
        String c2First = XmlFunctions.clearXML(sentenceFirst.substring(sentenceFirst.indexOf("<E2>") + 4 , sentenceFirst.indexOf("</E2>")));
        String c2Second = XmlFunctions.clearXML(sentenceSecond.substring(sentenceSecond.indexOf("<E2>") + 4 , sentenceSecond.indexOf("</E2>")));
        // if c1 of second relation overlaps c1 of first relation and c2 of second relation is equal or overlap c2 of first relation
        if (c1Second.contains(c1First) && c2Second.contains(c2First))
            if(c1Second.length() > c1First.length() || c2Second.length() > c2First.length())
                return true;


        // check if relations are overlapped
        // TODO check this filter
        double sentenceFirstWeight = 0;
        double sentenceSecondWeight = 0;
        for(String label : RelExtConstants.labelsWithNum){
            String labelXML = "<" + label + ">";
            if(sentenceFirst.contains(labelXML)){
                // get weights
                sentenceFirstWeight = 1.0;
                if(label.contains("2"))
                    sentenceFirstWeight = 0.75;
                else if (label.contains("3"))
                    sentenceFirstWeight = 0.5;
            }
            if(sentenceSecond.contains(labelXML)){
                // get weights
                sentenceSecondWeight = 1.0;
                if(label.contains("2"))
                    sentenceSecondWeight = 0.75;
                else if (label.contains("3"))
                    sentenceSecondWeight = 0.5;
            }
        }

        if ( c1First.equals(c1Second) && c2First.equals(c2Second) && sentenceFirstWeight < sentenceSecondWeight)
            return true;

        return false;
    }

    /**
     * add labels (rule-based, machine-learning and fused) to sets of RelationObject instances
     * @param relationsMap
     */
    private void annotateRelationsMap(Map<Integer, Set<RelationObject>> relationsMap){
        Map<Integer,String> semanticNetworkLines = new HashMap<>();
        for (Map.Entry<Integer, Set<RelationObject>> entry : relationsMap.entrySet()){
            int sentenceID = entry.getKey();
            Set<RelationObject> relationObjects = entry.getValue();
            for (RelationObject relationObject : relationObjects){
                // calculate rule based labels
                String originalSentence = relationObject.getSentence();
                String sentenceWithRelations = relationObject.getSentenceWithRelations();
                HashMap<String,Double> rbLabels = relext.calculateRuleBasedLabelsOnOneSentence(sentenceWithRelations);
                relationObject.setRbLabels(rbLabels);

                if (!semanticNetworkLines.containsKey(sentenceID)) {
                    String semanticNetworkLine = LocalMetaMapHandler.getSemRepOutput(originalSentence);
                    if (semanticNetworkLine.startsWith("--ERROR--")) {
                        semanticNetworkLine = null;
                    }
                    semanticNetworkLines.put(sentenceID,semanticNetworkLine);
                }

                // calculate machine learning labels
                SentenceRepresentation sentRep = formSentenceWekaRepresentation(originalSentence, sentenceWithRelations,
                        semanticNetworkLines.get(sentenceID));
                if(sentRep==null)
                    continue;
                relationObject.setSentenceRepresentation(sentRep);
                HashMap<String,Double> mlLabels = new HashMap<>();
                if(!(sentRep.isEmpty()))
                    mlLabels= wekaHandler.getLabelsFromSentenceRepresentation(sentRep);
                relationObject.setMlLabels(mlLabels);

                // fuse labels
                HashMap<String,Double> fusedLabels = relext.fuseAnnotations(rbLabels,mlLabels);
                relationObject.setFusedLabels(fusedLabels);
            }
        }
    }

    /**
     * form an object containing attributes needed for the weka models
     * @param sentence
     * @param sentenceWithConcepts
     * @return
     */
    private SentenceRepresentation formSentenceWekaRepresentation(String sentence, String sentenceWithConcepts,
                                                                  String semanticNetworkLine) {
        try {
            SentenceRepresentation sentRep = new SentenceRepresentation();

            boolean hasProperNoun = false;
            boolean isc1Disease = false;
            boolean isc1Treatment = false;
            boolean isc1Test = false;
            boolean isc1DiseaseLex = false;
            boolean isc1TreatmentLex = false;
            boolean isc1TestLex = false;
            boolean isc2Disease = false;
            boolean isc2Treatment = false;
            boolean isc2Test = false;
            boolean isc2DiseaseLex = false;
            boolean isc2TreatmentLex = false;
            boolean isc2TestLex = false;

            // first: opening tag first concept
            // second: closing tag first concept
            // third: opening tag second concept
            // fourth: closing tag second concept
            String[] conceptOrder = getConceptOrder(sentenceWithConcepts);

            if(conceptOrder == null){
                // define that the representation is empty
                sentRep.setEmpty(true);
                return sentRep;
            }

            // get concepts (clear xml tags first)
            // always set concept1 to E1 and concept2 to E2
            String concept1xml = null, concept2xml = null, beforeConcept1xml = null, beforeConcept2xml = null;
            if(conceptOrder[0].equals("<E1>")){
                concept1xml = sentenceWithConcepts.substring(sentenceWithConcepts.indexOf(conceptOrder[0]), sentenceWithConcepts.indexOf(conceptOrder[1]) + 5);
                beforeConcept1xml = sentenceWithConcepts.substring(0, sentenceWithConcepts.indexOf(conceptOrder[0]));
                concept2xml = sentenceWithConcepts.substring(sentenceWithConcepts.lastIndexOf(conceptOrder[2]), sentenceWithConcepts.lastIndexOf(conceptOrder[3]) + 5);
                beforeConcept2xml = sentenceWithConcepts.substring(0, sentenceWithConcepts.lastIndexOf(conceptOrder[2]));
            }
            else{
                concept1xml = sentenceWithConcepts.substring(sentenceWithConcepts.lastIndexOf(conceptOrder[2]), sentenceWithConcepts.lastIndexOf(conceptOrder[3]) + 5);
                beforeConcept1xml = sentenceWithConcepts.substring(0, sentenceWithConcepts.indexOf(conceptOrder[2]));
                concept2xml = sentenceWithConcepts.substring(sentenceWithConcepts.indexOf(conceptOrder[0]), sentenceWithConcepts.indexOf(conceptOrder[1]) + 5);
                beforeConcept2xml = sentenceWithConcepts.substring(0, sentenceWithConcepts.lastIndexOf(conceptOrder[0]));
            }
            String concept1 = XmlFunctions.clearXML(concept1xml).trim();
            String concept2 = XmlFunctions.clearXML(concept2xml).trim();

            // get c1 and c2 metamap concepts
            String c1MetaMap = "";
            String c2MetaMap = "";
            String prefix = "";
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            // spaces between xml are considered as separate xml elements
            is.setCharacterStream(new StringReader(concept1xml.replaceAll(">\\s+<", "><")));
            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("concept");
            if(nodes.getLength()==0){
                Node node = doc.getDocumentElement().getFirstChild();
                if(node instanceof Element){
                    c1MetaMap = doc.getDocumentElement().getFirstChild().getNodeName();
                    if(MetaMapCONSTANTS.diseaseList.contains(c1MetaMap))
                        isc1Disease=true;
                    else if(MetaMapCONSTANTS.treatmentList.contains(c1MetaMap))
                        isc1Treatment=true;
                    else if(MetaMapCONSTANTS.testList.contains(c1MetaMap))
                        isc1Test=true;
                }
                else
                    c1MetaMap = "";
//                System.out.println("c1:" + c1MetaMap);
            }
            else{
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    String metaMapConcept = element.getAttribute("MetaMap");
                    if(MetaMapCONSTANTS.diseaseList.contains(metaMapConcept))
                        isc1Disease=true;
                    else if(MetaMapCONSTANTS.treatmentList.contains(metaMapConcept))
                        isc1Treatment=true;
                    else if(MetaMapCONSTANTS.testList.contains(metaMapConcept))
                        isc1Test=true;
                    c1MetaMap += prefix + metaMapConcept;
                    prefix = " ";
                }
            }
            prefix = "";
            sentRep.setC1MetaMapConcept(c1MetaMap);

            // spaces between xml are considered as separate xml elements
            is.setCharacterStream(new StringReader(concept2xml.replaceAll(">\\s+<", "><"))); // TODO spaces must be removed before this program
            doc = db.parse(is);
            nodes = doc.getElementsByTagName("concept");
            if(nodes.getLength()==0){
                Node node = doc.getDocumentElement().getFirstChild();
                if(node instanceof Element){
                    c2MetaMap = node.getNodeName();
                    if(MetaMapCONSTANTS.diseaseList.contains(c2MetaMap))
                        isc2Disease=true;
                    else if(MetaMapCONSTANTS.treatmentList.contains(c2MetaMap))
                        isc2Treatment=true;
                    else if(MetaMapCONSTANTS.testList.contains(c2MetaMap))
                        isc2Test=true;
                }
                else
                    c2MetaMap = "";
//                System.out.println("c2:" + c2MetaMap);
            }
            else{
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    String metaMapConcept = element.getAttribute("MetaMap");
                    if(MetaMapCONSTANTS.diseaseList.contains(metaMapConcept))
                        isc2Disease=true;
                    else if(MetaMapCONSTANTS.treatmentList.contains(metaMapConcept))
                        isc2Treatment=true;
                    else if(MetaMapCONSTANTS.testList.contains(metaMapConcept))
                        isc2Test=true;
                    c2MetaMap += prefix + metaMapConcept;
                    prefix = " ";
                }
            }
            sentRep.setC2MetaMapConcept(c2MetaMap);

            // find concepts between C1 and C2
            String betweenC1C2xml = sentenceWithConcepts.substring(sentenceWithConcepts.indexOf(conceptOrder[1]) + 5,
                    sentenceWithConcepts.lastIndexOf(conceptOrder[2]));
            Pattern r = Pattern.compile("<(/??)concept(.*?)>(.*?)</concept>");

            // Now create matcher object.
            Matcher m = r.matcher(betweenC1C2xml);
            while (m.find()) {
//            System.out.println("Found value: " + m.group(0) );
                is.setCharacterStream(new StringReader(m.group(0)));
                doc = db.parse(is);
                nodes = doc.getElementsByTagName("concept");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    sentRep.addConceptBetweenc1c2(element.getAttribute("MetaMap"));
                }
            }

            // find concept 1 and 2 word ids (to be corresponded to the ids of the stanford nlp parser)
            String beforeConcept1 = XmlFunctions.clearXML(beforeConcept1xml).trim();
            String beforeConcept2 = XmlFunctions.clearXML(beforeConcept2xml).trim();
            List<Integer> concept1WordIds = findConceptIds(beforeConcept1, concept1);
            List<Integer> concept2WordIds = findConceptIds(beforeConcept2, concept2);

            // save concepts and their size
            sentRep.setConcept1(concept1);
            sentRep.setConcept2(concept2);
            sentRep.setC1Length(concept1WordIds.size());
            sentRep.setC2Length(concept2WordIds.size());

            String[] words = sentence.split("\\s+");
            String[] wordsWithConcepts = sentenceWithConcepts.split("\\s+");
            SentenceWithTags swt = NLPFunctions.annotateSentence(sentence, true, true);
            HashMap<Integer, WordWithTags> wwt = swt.getWords();

            // check concepts 1 and 2 if they match the lexicon concepts
            if(diseaseListLex.contains(concept1))
                isc1DiseaseLex=true;
            else if(treatmentListLex.contains(concept1))
                isc1TreatmentLex=true;
            else if(testListLex.contains(concept1))
                isc1TestLex=true;
            if(diseaseListLex.contains(concept2))
                isc2DiseaseLex=true;
            else if(treatmentListLex.contains(concept2))
                isc2TreatmentLex=true;
            else if(testListLex.contains(concept2))
                isc2TestLex=true;

            // check if sentence has any proper nouns
            for (Map.Entry<Integer, WordWithTags> entry : wwt.entrySet()){
                WordWithTags word = entry.getValue();
                String pos = word.getPosTag();
                if(pos.equals("NNP") || pos.equals("NNPS")){
                    hasProperNoun = true;
                    break;
                }
            }

            // add features being considered since jan 2017
            sentRep.setIsc1Disease(isc1Disease);
            sentRep.setIsc1Treatment(isc1Treatment);
            sentRep.setIsc1Test(isc1Test);
            sentRep.setIsc2Disease(isc2Disease);
            sentRep.setIsc2Treatment(isc2Treatment);
            sentRep.setIsc2Test(isc2Test);
            sentRep.setIsc1DiseaseLex(isc1DiseaseLex);
            sentRep.setIsc1TreatmentLex(isc1TreatmentLex);
            sentRep.setIsc1TestLex(isc1TestLex);
            sentRep.setIsc2DiseaseLex(isc2DiseaseLex);
            sentRep.setIsc2TreatmentLex(isc2TreatmentLex);
            sentRep.setIsc2TestLex(isc2TestLex);
            sentRep.setHasProperNoun(hasProperNoun);
            sentRep.setText(sentence);

            // add pos and concept type (concept 1)
            String pos = "";
            prefix = "";
            for (int id : concept1WordIds) {
                WordWithTags word = wwt.get(id);
                pos += prefix + word.getPosTag();
                prefix = " ";

            }
            sentRep.setC1Pos(pos);

            // add words before concept 1
            int tempInt = concept1WordIds.get(0) - 1; // start checks before concept1
            int posToAdd = 1; // 1 for first word, 2 for second, 3 for third
            boolean verbIsSet = false; // flag to check if verb before concept1 is added
            while (tempInt > 0) {
                switch (posToAdd) {
                    case 1:
                        sentRep.setW1bc1(wwt.get(tempInt).getText());
                        sentRep.setLw1bc1(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                    case 2:
                        sentRep.setW2bc1(wwt.get(tempInt).getText());
                        sentRep.setLw2bc1(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                    case 3:
                        sentRep.setW3bc1(wwt.get(tempInt).getText());
                        sentRep.setLw3bc1(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                }
                if (!verbIsSet && wwt.get(tempInt).getPosTag().startsWith("VB")) {
                    sentRep.setVerbbc1(wwt.get(tempInt).getText());
                    verbIsSet = true;
                }
                --tempInt;
            }

            // add pos and concept type (concept 2)
            pos = "";
            prefix = "";
            for (int id : concept2WordIds) {
                WordWithTags word = wwt.get(id);
                pos += prefix + word.getPosTag();
                prefix = " ";

            }
            sentRep.setC2Pos(pos);

            tempInt = concept2WordIds.get(concept2WordIds.size() - 1) - 1; // start checks after concept2
            posToAdd = 1; // 1 for first word, 2 for second, 3 for third
            verbIsSet = false; // flag to check if verb after concept2 is added
            while (tempInt <= wwt.size()) {
                switch (posToAdd) {
                    case 1:
                        sentRep.setW1ac2(wwt.get(tempInt).getText());
                        sentRep.setLw1ac2(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                    case 2:
                        sentRep.setW2ac2(wwt.get(tempInt).getText());
                        sentRep.setLw2ac2(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                    case 3:
                        sentRep.setW3ac2(wwt.get(tempInt).getText());
                        sentRep.setLw3ac2(wwt.get(tempInt).getLemma());
                        ++posToAdd;
                        break;
                }
                if (!verbIsSet && wwt.get(tempInt).getPosTag().startsWith("VB")) {
                    sentRep.setVerbac2(wwt.get(tempInt).getText());
                    verbIsSet = true;
                }
                ++tempInt;
            }

            //set the verbs and concepts between c1 and c2
            int bstart; //"between" (c1 & c2) start
            int bend; //"between" (c1 & c2) start

//        if (conceptOrder[0].equals("<E1>")) {
            bstart = concept1WordIds.get(concept1WordIds.size() - 1) + 1;
            bend = concept2WordIds.get(0);
//        } else {
//            bstart = relLine.getC2EndPos() + 1;
//            bend = relLine.getC1StartPos();
//        }
            while (bstart < bend) {
                WordWithTags tempWwt = wwt.get(bstart);

                if (tempWwt.getPosTag().startsWith("VB")) {
                    sentRep.addVerbBetweenc1c2(tempWwt.getText());
                }
                bstart++;
            }

            // relation type
//            String semanticNetworkLine = LocalMetaMapHandler.getSemRepOutput(sentence);
//            if (semanticNetworkLine.startsWith("--ERROR--")) {
//                semanticNetworkLine = null;
//            }

            sentRep.setSemRepRel(getReationshipBetween(concept1, concept2, semanticNetworkLine));

            return sentRep;
        }
        catch(Exception e){
            System.out.println("Exception while forming weka representation!!!\n");
            e.printStackTrace();
//            System.out.println("\nExiting...");
//            System.exit(0);
            return null;
        }
    }

    /**
     * get tag order depending on which concept (E1 or E2) is found first in a given sentence
     * @param sentence
     * @return
     */
    private String[] getConceptOrder(String sentence){
        int indexE1 = sentence.indexOf("<E1>");
        int indexE2 = sentence.indexOf("<E2>");
        if(indexE1 == -1 || indexE2 == -1)
            return null;
        else if(indexE1<indexE2)
            return new String[]{"<E1>", "</E1>", "<E2>", "</E2>"};
        else
            return new String[]{"<E2>", "</E2>", "<E1>", "</E1>"};
    }

    /**
     * get ids of words contained in a concept
     * @param textBeforeConcept
     * @param textWithConcepts
     * @return
     */
    private List<Integer> findConceptIds(String textBeforeConcept, String textWithConcepts){
        List<Integer> ids = new ArrayList<>();
        int lengthBeforeConcepts = textBeforeConcept.split("\\s+").length;
        int lengthConcepts = textWithConcepts.split("\\s+").length;
        for (int i = lengthBeforeConcepts + 1; i <= lengthBeforeConcepts + lengthConcepts; i++) {
            ids.add(i);
        }
        return ids;
    }

    /**
     * get semrep labels
     * @param c1
     * @param c2
     * @param semanticNetworkLine
     * @return
     */
    private String getReationshipBetween(String c1, String c2, String semanticNetworkLine){

        Document docNePos = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputStream streamNePos = new ByteArrayInputStream(semanticNetworkLine.getBytes(StandardCharsets.UTF_8));
            docNePos = docBuilder.parse(streamNePos);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.err.print("KIndex :: Pipeline.getCorrespondingNEPosSegment() Could not create a document text.");
            e.printStackTrace();
        }

        NodeList predictions = docNePos.getElementsByTagName("Predication"); // concept or text segments (children of content)
        try {
            for (int i = 0; i < predictions.getLength(); i++) {
                String subject = "";
                String object = "";
                String predicate = "";

                Node prediction = predictions.item(i);
                NodeList elements = prediction.getChildNodes();
                for (int j = 0; j < elements.getLength(); j++) {
                    Node el = elements.item(j);
                    if (el.hasAttributes()) {

                        String elName = el.getNodeName();
                        if (elName.equals("Subject") || elName.equals("Object")) {
                            //System.out.println(el.getNodeName() + ": " + el.getAttributes().getNamedItem("entityID").getNodeValue());
                            //get element by attribute
                            XPathFactory xPathfactory = XPathFactory.newInstance();
                            XPath xpath = xPathfactory.newXPath();
                            String exp = "SemRepAnnotation/Document/Utterance/Entity[@id='" + el.getAttributes().getNamedItem("entityID").getNodeValue() + "']";
                            XPathExpression expr = xpath.compile(exp);
                            NodeList nl = (NodeList) expr.evaluate(docNePos, XPathConstants.NODESET);
                            Node n = nl.item(0);
                            switch (elName){
                                case "Subject":
                                    subject = n.getAttributes().getNamedItem("text").getNodeValue();
                                    break;
                                case "Object":
                                    object = n.getAttributes().getNamedItem("text").getNodeValue();
                                    break;
                            }
                            //System.out.println(n.getAttributes().getNamedItem("name").getNodeValue());
                        }
                        else if (elName.equals("Predicate")){
                            predicate = el.getAttributes().getNamedItem("type").getNodeValue();
                            //System.out.println(el.getNodeName() + ": " + el.getAttributes().getNamedItem("type").getNodeValue());
                        }
                    }
                }
//                System.out.println("Subject: " + subject);
//                System.out.println("Predicate: " + predicate);
//                System.out.println("Object: " + object);

                if (((c1.contains(subject) || subject.contains(c1)) && (c2.contains(object) || object.contains(c2))) ||
                        ((c2.contains(subject) || subject.contains(c2)) && (c1.contains(object) || object.contains(c1)))){
//                    System.out.println("Relationship returned!!!");
                    return predicate;
                }
                return "0";
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }


        return "0";
    }

    /**
     * form knowledge base input
     * @param relationsMap
     * @param pageID
     * @param textID
     * @param url
     * @return
     */
    private ArrayList<KBResult> formKBInputFromRelObjects(Map<Integer,Set<RelationObject>> relationsMap,
                                                         String pageID, String textID, String url){
        ArrayList<KBResult> result = new ArrayList<>();
        Babelfy bfy = new Babelfy();
        for (Map.Entry<Integer, Set<RelationObject>> entry : relationsMap.entrySet()){
            int sentenceID = entry.getKey();
            Set<RelationObject> relationObjects = entry.getValue();
            for (RelationObject relationObject : relationObjects){
                String sentenceText = relationObject.getSentence();
                SentenceRepresentation sentence = relationObject.getSentenceRepresentation();

                // ignore sentences with no concepts and relations
                // null check is not needed as it's handled while forming the representations
                if(sentence == null || sentence.isEmpty())
                    continue;

                // get babelfy annotations
                KBResult resultItem = new KBResult();
                ConceptResult c1 = new ConceptResult();
                ConceptResult c2 = new ConceptResult();
                c1.setText(sentence.getConcept1());
                c1.setConcept(sentence.getC1MetaMapConcept());
                List<SemanticAnnotation> bfyAnnotationsC1 = bfy.babelfy(sentence.getConcept1(), Language.EN);
                for (SemanticAnnotation annotation : bfyAnnotationsC1) {
                    String babelNetURL = annotation.getBabelNetURL();
                    if(babelNetURL == null)
                        c1.addToBabelNet("");
                    else
                        c1.addToBabelNet(babelNetURL);
                    String dbPediaURL = annotation.getDBpediaURL();
                    if(dbPediaURL == null)
                        c1.addToDBPedia("");
                    else
                        c1.addToDBPedia(dbPediaURL);
                }
                c2.setText(sentence.getConcept2());
                c2.setConcept(sentence.getC2MetaMapConcept());
                List<SemanticAnnotation> bfyAnnotationsC2 = bfy.babelfy(sentence.getConcept2(), Language.EN);
                for (SemanticAnnotation annotation : bfyAnnotationsC2) {
                    String babelNetURL = annotation.getBabelNetURL();
                    if(babelNetURL == null)
                        c2.addToBabelNet("");
                    else
                        c2.addToBabelNet(babelNetURL);
                    String dbPediaURL = annotation.getDBpediaURL();
                    if(dbPediaURL == null)
                        c2.addToDBPedia("");
                    else
                        c2.addToDBPedia(dbPediaURL);
                }
                resultItem.setC1(c1);
                resultItem.setC2(c2);

                // find relation (label with max score)
                HashMap<String,Double> fusedLabelMap = relationObject.getFusedLabels();
                if(fusedLabelMap.size()==0)
                    resultItem.setRelation("None");
                else{
                    Map.Entry<String,Double> maxEntry = MapFunctions.getMaxEntry(fusedLabelMap);
                    resultItem.setRelation(maxEntry.getKey());
                    resultItem.setRelationScore(maxEntry.getValue());
                }

                // finally add necessary ids
                resultItem.setPageID(pageID);
                resultItem.setTextID(textID);
                resultItem.setUrl(url);
                resultItem.setSentenceID(sentenceID);
                resultItem.setSentenceText(sentenceText);

                result.add(resultItem);
            }
        }

        String kbInput = new Gson().toJson(result);

        return result;
    }

    public static void main(String[] args) {
        RelExtPipeline pipeline = new RelExtPipeline();
        String text = "";
        System.out.println(text);
        text = text.replace("\\n","\n");
        text = FileFunctions.readInputFile("input/temp.txt");
        ArrayList<KBResult> result = pipeline.executeNewPipeline(text,"test","test", "www.test.com");
        System.out.println(new Gson().toJson(result));
    }

}
