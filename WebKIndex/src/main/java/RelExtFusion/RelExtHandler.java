package RelExtFusion;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.math.DoubleAD;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import fr.umlv.unitex.jni.UnitexJni;
import Functions.FileFunctions;
import Functions.MapFunctions;
import LexParser.LexParserConstants;
import LexParser.NounPhraseDetector;
import Unitex.UnitexConstants;
import Unitex.UnitexJniHandler;
import Weka.WekaHandler;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by spyridons on 9/5/2016.
 */
public class RelExtHandler {

    private HashMap<String,Double> labelWeightsRB;
    private HashMap<String,Double> labelWeightsML;

    public RelExtHandler(){
        initializeWeights("");
    }

    public RelExtHandler(String weighting){
        initializeWeights(weighting);
    }

    /**
     * define weights for the fusion relation extraction framework
     * @param weighting
     */
    private void initializeWeights(String weighting)
    {
        labelWeightsRB = new HashMap<>();
        labelWeightsML = new HashMap<>();
        if(weighting.equals("rf")){
            labelWeightsML.put("TrIP", 0.098);
            labelWeightsML.put("TrWP", 0.0);
            labelWeightsML.put("TrCP", 0.25);
            labelWeightsML.put("TrAP", 0.85);
            labelWeightsML.put("TrNAP", 0.355);
            labelWeightsML.put("PIP", 0.809);
            labelWeightsML.put("TeRP", 0.87);
            labelWeightsML.put("TeCP", 0.12);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        if(weighting.equals("rfb")){
            labelWeightsML.put("TrIP", 0.255);
            labelWeightsML.put("TrWP", 0.0);
            labelWeightsML.put("TrCP", 0.255);
            labelWeightsML.put("TrAP", 0.847);
            labelWeightsML.put("TrNAP", 0.419);
            labelWeightsML.put("PIP", 0.743);
            labelWeightsML.put("TeRP", 0.829);
            labelWeightsML.put("TeCP", 0.108);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        //5-fold cv weights
        else if(weighting.equals("svm")){
            labelWeightsML.put("TrIP", 0.412);
            labelWeightsML.put("TrWP", 0.208);
            labelWeightsML.put("TrCP", 0.554);
            labelWeightsML.put("TrAP", 0.826);
            labelWeightsML.put("TrNAP", 0.548);
            labelWeightsML.put("PIP", 0.826);
            labelWeightsML.put("TeRP", 0.872);
            labelWeightsML.put("TeCP", 0.367);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        //3-fold cv weights
        else if(weighting.equals("svm2")){
            labelWeightsML.put("TrIP", 0.412);
            labelWeightsML.put("TrWP", 0.542);
            labelWeightsML.put("TrCP", 0.505);
            labelWeightsML.put("TrAP", 0.769);
            labelWeightsML.put("TrNAP", 0.5);
            labelWeightsML.put("PIP", 0.813);
            labelWeightsML.put("TeRP", 0.84);
            labelWeightsML.put("TeCP", 0.434);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        // using stratified 2/3 split on training set
        else if(weighting.equals("svm3")){
            labelWeightsML.put("TrIP", 0.471);
            labelWeightsML.put("TrWP", 0.5);
            labelWeightsML.put("TrCP", 0.574);
            labelWeightsML.put("TrAP", 0.766);
            labelWeightsML.put("TrNAP", 0.381);
            labelWeightsML.put("PIP", 0.821);
            labelWeightsML.put("TeRP", 0.84);
            labelWeightsML.put("TeCP", 0.418);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        //5-fold cv weights
        else if(weighting.equals("svm-multi")){
            labelWeightsML.put("TrIP", 0.0);
            labelWeightsML.put("TrWP", 0.417);
            labelWeightsML.put("TrCP", 0.582);
            labelWeightsML.put("TrAP", 0.897);
            labelWeightsML.put("TrNAP", 0.5);
            labelWeightsML.put("PIP", 0.865);
            labelWeightsML.put("TeRP", 0.918);
            labelWeightsML.put("TeCP", 0.452);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        else if(weighting.equals("liblinear")){
            labelWeightsML.put("TrIP", 0.431);
            labelWeightsML.put("TrWP", 0.333);
            labelWeightsML.put("TrCP", 0.533);
            labelWeightsML.put("TrAP", 0.82);
            labelWeightsML.put("TrNAP", 0.565);
            labelWeightsML.put("PIP", 0.812);
            labelWeightsML.put("TeRP", 0.87);
            labelWeightsML.put("TeCP", 0.361);
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 1 - labelWeightsML.get(label));
            }
        }
        else if(weighting.equals("equal")){
            for (String label: RelExtConstants.labels) {
                labelWeightsRB.put(label, 0.5);
                labelWeightsML.put(label, 0.5);
            }
        }
        else{
            labelWeightsRB.put("TrIP", 0.984);
            labelWeightsRB.put("TrWP", 0.993);
            labelWeightsRB.put("TrCP", 0.94);
            labelWeightsRB.put("TrAP", 0.716);
            labelWeightsRB.put("TrNAP", 0.98);
            labelWeightsRB.put("PIP", 0.758);
            labelWeightsRB.put("TeRP", 0.682);
            labelWeightsRB.put("TeCP", 0.947);
            labelWeightsML.put("TrIP", 0.016);
            labelWeightsML.put("TrWP", 0.007);
            labelWeightsML.put("TrCP", 0.06);
            labelWeightsML.put("TrAP", 0.284);
            labelWeightsML.put("TrNAP", 0.02);
            labelWeightsML.put("PIP", 0.242);
            labelWeightsML.put("TeRP", 0.318);
            labelWeightsML.put("TeCP", 0.053);
        }
    }



    private List<String> getSentences(String text){
        List<String> sentenceList = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        // shut off the annoying intialization messages
        RedwoodConfiguration.empty().capture(System.err).apply();

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // enable stderr again
        RedwoodConfiguration.current().clear().apply();

        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
            sentenceList.add(sentenceText);
        }
        return sentenceList;
    }


    /**
     * under construction...
     * @param concordFile
     * @return
     */
    public ArrayList<String> selectSentencesFromConcord(String concordFile){
        ArrayList<String> selectedLines = new ArrayList<>();

        String idSeperator = "\t<ID>\t";
        String concordText = FileFunctions.readInputFile(concordFile);
        List<String> concordLines = Arrays.asList(concordText.split("[\n\r]"));

        // map concord sentences to their respective ids
        TreeMap<Integer,ArrayList<String>> concordMap = new TreeMap<>();
        concordLines.remove(0);
        for (String concordLine: concordLines){
            String[] split = concordLine.split(idSeperator);
            int id = Integer.parseInt(split[0]);
            String content = split[1];
            if(concordMap.containsKey(id)) {
                concordMap.get(id).add(content);
            }
            else{
                ArrayList<String> contents = new ArrayList<>();
                contents.add(content);
                concordMap.put(id,contents);
            }
        }

        // extract one sentence for each id
        for (Map.Entry<Integer, ArrayList<String>> entry : concordMap.entrySet())
        {
            ArrayList<String> possibleSentences = entry.getValue();
            String selectedSentence = null;
            for (String sentence : possibleSentences){
                if(selectedSentence == null){
                    selectedSentence = sentence;
                }
                else{
                    if(hasMoreConceptWords(sentence,selectedSentence))
                        selectedSentence = sentence;
                }
            }
            selectedLines.add(selectedSentence);
        }

        return selectedLines;
    }

    /**
     * under construction...
     * code to see if candidate sentence concepts consist of more words than the (at that time) optimal sentence
     * @param candidateSentence
     * @param bestSentence
     * @return
     */
    private boolean hasMoreConceptWords(String candidateSentence, String bestSentence)
    {
        return true;
    }

    /***
     * Get rule-based annotations
     * @param sentences , a list of sentences
     * @return
     * @throws IOException
     */
    public ArrayList<HashMap<String,Double>> calculateRuleBasedLabelsOnText(List<String> sentences) throws IOException {
        ArrayList<HashMap<String,Double>> ruleBasedLabels = new ArrayList<>();

        ArrayList<Double> nounPhraseList = new ArrayList<>();


        // calculate noun phrases
        NounPhraseDetector npd = new NounPhraseDetector();

//        for(String sentence: sentences)
//            nounPhraseList.add(npd.extractNounPhrasesOneSentence(sentence).doubleValue());

        String sentence = "";
        for (int i = 0; i < sentences.size() ; i++){
            if(i%500 == 0 && i!=0)
                System.out.print(i + "...");
            HashMap<String,Double> labelScores = new HashMap<>();
            sentence = sentences.get(i);

            // store scores per label
            for(String label : RelExtConstants.labelsWithNum){
                String labelWithoutNum = label.substring(0, label.length()-1);
                String labelXML = "<" + label + ">";
                if(sentence.contains(labelXML)){

//                    double nounPhrases = nounPhraseList.get(i);

                    // faster way than getting noun phrases from all sentences before checking label tags
                    double nounPhrases = npd.extractNounPhrasesOneSentence(sentence).doubleValue();

                    // get weights
                    double weight = 1.0;
                    if(label.contains("2"))
                        weight = 0.75;
                    else if (label.contains("3"))
                        weight = 0.5;

                    // calculate confidence index
                    double confidenceIndex = calculateConfidenceIndex(weight,nounPhrases);
//                    double score = labelWeightsRB.get(labelWithoutNum) * confidenceIndex ;
                    double score = confidenceIndex ;

                    // keep max score for the same label
                    if(labelScores.containsKey(labelWithoutNum)){
                        double existingScore = labelScores.get(labelWithoutNum);
                        if(score > existingScore){
                            labelScores.put(labelWithoutNum,score);
                        }
                    }
                    else{
                        labelScores.put(labelWithoutNum,score);
                    }
                }
                else{
                    if(!(labelScores.containsKey(labelWithoutNum)))
                        labelScores.put(labelWithoutNum,0.0);
                }
            }


            // store predicted label of sentence
            ruleBasedLabels.add(labelScores);
        }

        return ruleBasedLabels;
    }

    public HashMap<String, Double> calculateRuleBasedLabelsOnOneSentence(String sentence){

        // extract noun phrases
        NounPhraseDetector npd = new NounPhraseDetector();
        double nounPhrases = npd.extractNounPhrasesOneSentence(sentence).doubleValue();

        HashMap<String,Double> labelScores = new HashMap<>();

        // store scores per label
        for(String label : RelExtConstants.labelsWithNum){
            String labelWithoutNum = label.substring(0, label.length()-1);
            String labelXML = "<" + label + ">";
            if(sentence.contains(labelXML)){

                // get weights
                double weight = 1.0;
                if(label.contains("2"))
                    weight = 0.75;
                else if (label.contains("3"))
                    weight = 0.5;

                // calculate confidence index
                double confidenceIndex = calculateConfidenceIndex(weight,nounPhrases);
//                    double score = labelWeightsRB.get(labelWithoutNum) * confidenceIndex ;
                double score = confidenceIndex ;

                // keep max score for the same label
                if(labelScores.containsKey(labelWithoutNum)){
                    double existingScore = labelScores.get(labelWithoutNum);
                    if(score > existingScore){
                        labelScores.put(labelWithoutNum,score);
                    }
                }
                else{
                    labelScores.put(labelWithoutNum,score);
                }
            }
            else{
                if(!(labelScores.containsKey(labelWithoutNum)))
                    labelScores.put(labelWithoutNum,0.0);
            }
        }

        return labelScores;
    }


    /***
     * Calculate a parameter of the rule based classifier
     * @param weight
     * @param nounPhrases
     * @return
     */
    private double calculateConfidenceIndex(double weight, double nounPhrases){
//        double conf = weight / Math.exp(nounPhrases);
        double conf = weight / Math.pow(Math.sqrt(2),nounPhrases);
        return conf;
//        return weight;
    }

    /***
     * Fuse rule-based and machine learning annotations (experiments)
     * @param ruleBasedLabels
     * @param machineLearningLabels
     * @throws IOException
     */
    public ArrayList<HashMap<String,Double>> fuseAnnotations(ArrayList<HashMap<String,Double>> ruleBasedLabels, ArrayList<HashMap<String,Double>> machineLearningLabels) throws IOException {
        ArrayList<HashMap<String,Double>> fusedLabels = new ArrayList<>();
        BufferedWriter out = new BufferedWriter(new FileWriter(RelExtConstants.outFolder + RelExtConstants.resultFileName));
        for (int i = 0; i < ruleBasedLabels.size(); i++) {
            HashMap<String,Double> rbLabelMap = ruleBasedLabels.get(i);
            HashMap<String,Double> mlLabelMap = machineLearningLabels.get(i);
            HashMap<String,Double> fusedLabelMap = new HashMap<>();

            if(mlLabelMap.size()==0){
                fusedLabels.add(fusedLabelMap);
                continue;
            }


            for (Map.Entry<String, Double> entry : rbLabelMap.entrySet())
            {
                String label = entry.getKey();
                double rbValue = entry.getValue();
                double mlValue = mlLabelMap.get(label);
//                double fusedValue = rbValue + mlValue;
                double fusedValue = labelWeightsRB.get(label) * rbValue + labelWeightsML.get(label) * mlValue;
                fusedLabelMap.put(label, fusedValue);
            }

            // find label with max score
            Map.Entry<String,Double> maxEntry = MapFunctions.getMaxEntry(fusedLabelMap);

            out.write(maxEntry.getKey() + "\t" + maxEntry.getValue() + "\n");
            fusedLabels.add(fusedLabelMap);

        }
        out.close();
        return fusedLabels;
    }

    public HashMap<String,Double> fuseAnnotations(HashMap<String,Double> rbLabelMap, HashMap<String,Double> mlLabelMap) {

            HashMap<String,Double> fusedLabelMap = new HashMap<>();

            if(mlLabelMap.size()==0){
                return fusedLabelMap;
            }

            for (Map.Entry<String, Double> entry : rbLabelMap.entrySet())
            {
                String label = entry.getKey();
                double rbValue = entry.getValue();
                double mlValue = mlLabelMap.get(label);
//                double fusedValue = rbValue + mlValue;
                double fusedValue = labelWeightsRB.get(label) * rbValue + labelWeightsML.get(label) * mlValue;
                fusedLabelMap.put(label, fusedValue);
            }

            return fusedLabelMap;

    }


    /**
     * saves a prediction map to a tab-seperated file
     * @param labels
     * @param fileName
     * @throws IOException
     */
    public void saveMapScoresToFile(ArrayList<HashMap<String,Double>> labels, String fileName) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write("ID\t");
        for (String label: RelExtConstants.labels) {
            out.write(label+"\t");
        }
        out.write(System.lineSeparator());

        for (int i = 0; i < labels.size(); i++) {

            HashMap<String,Double> labelMap = labels.get(i);

            int id = i + 1;
            out.write(id + "\t");
            for (String label: RelExtConstants.labels) {
                double labelScore = labelMap.get(label);
                out.write(labelScore + "\t");
            }
            out.write(System.lineSeparator());

        }
        out.close();
    }

}
