package Weka;

import RelExtFusion.RelExtConstants;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.util.*;


/**
 * Created by spyridons on 9/7/2016.
 */
public class WekaHandler {

    private StringToWordVector bow;

    public WekaHandler(){
        try {
            bow = (StringToWordVector) SerializationHelper.read(RelExtConstants.wekaModelsFolder + "/bow.filter");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading bow filter!!!");
        }
    }

    public void saveInstancesToCSV(Instances data, String fileName){
        System.out.println("\nSaving data as csv to file " + fileName + "...");
        CSVSaver saver = new CSVSaver();
        saver.setInstances(data);
        try {
            saver.setFile(new File(fileName));
            saver.writeBatch();
        } catch (IOException e) {
            System.out.println("Exception while saving data to csv!!!\n");
            e.printStackTrace();
        }
    }

    /**
     * get labels in a hashmap (key:label, value:score) representation
     * @param sentRep
     * @return
     */
    public HashMap<String,Double> getLabelsFromSentenceRepresentation(SentenceRepresentation sentRep) {
        try{
            int maxVerbs = 9;
            int maxConcepts = 13;

            // load attributes
            DataSource attributeSource = new DataSource(RelExtConstants.attributeSampleFilePath);
            Instances attributeData = attributeSource.getDataSet();

            Instances unlabeled = new Instances(attributeData,0);
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
            Instance inst = new DenseInstance(unlabeled.numAttributes());
            inst.setDataset(unlabeled);



            inst.setValue(0, sentRep.getText());
            inst.setValue(1, String.valueOf(sentRep.hasProperNoun()));
            addValueToInstance(inst,2,sentRep.getConcept1());
            addValueToInstance(inst,3,sentRep.getC1Pos());
            addValueToInstance(inst,4,sentRep.getC1MetaMapConcept());
            inst.setValue(5,sentRep.getC2Length());
            inst.setValue(6,String.valueOf(sentRep.isc1Disease()));
            inst.setValue(7,String.valueOf(sentRep.isc1DiseaseLex()));
            inst.setValue(8,String.valueOf(sentRep.isc1Treatment()));
            inst.setValue(9,String.valueOf(sentRep.isc1TreatmentLex()));
            inst.setValue(10,String.valueOf(sentRep.isc1Test()));
            inst.setValue(11,String.valueOf(sentRep.isc1TestLex()));
            addValueToInstance(inst,12,sentRep.getConcept2());
            addValueToInstance(inst,13,sentRep.getC2Pos());
            addValueToInstance(inst,14,sentRep.getC2MetaMapConcept());
            inst.setValue(15,sentRep.getC2Length());
            inst.setValue(16,String.valueOf(sentRep.isc2Disease()));
            inst.setValue(17,String.valueOf(sentRep.isc2DiseaseLex()));
            inst.setValue(18,String.valueOf(sentRep.isc2Treatment()));
            inst.setValue(19,String.valueOf(sentRep.isc2TreatmentLex()));
            inst.setValue(20,String.valueOf(sentRep.isc2Test()));
            inst.setValue(21,String.valueOf(sentRep.isc2TestLex()));
            addValueToInstance(inst,22,sentRep.getW1bc1());
            addValueToInstance(inst,23,sentRep.getLw1bc1());
            addValueToInstance(inst,24,sentRep.getW2bc1());
            addValueToInstance(inst,25,sentRep.getLw2bc1());
            addValueToInstance(inst,26,sentRep.getW3bc1());
            addValueToInstance(inst,27,sentRep.getLw3bc1());
            addValueToInstance(inst,28,sentRep.getW1ac2());
            addValueToInstance(inst,29,sentRep.getLw1ac2());
            addValueToInstance(inst,30,sentRep.getW2ac2());
            addValueToInstance(inst,31,sentRep.getLw2ac2());
            addValueToInstance(inst,32,sentRep.getW3ac2());
            addValueToInstance(inst,33,sentRep.getLw3ac2());
            addValueToInstance(inst,43,sentRep.getVerbbc1());
            addValueToInstance(inst,44,sentRep.getVerbac2());
            addValueToInstance(inst,58,sentRep.getSemRepRel());
            int offset = 0;
            ArrayList<String> verbsBc1c2 = sentRep.getVerbsBc1c2();
            int numVerbsBc1c2 = verbsBc1c2.size();
            while(offset<maxVerbs){
                if(offset < numVerbsBc1c2)
                    addValueToInstance(inst,34+offset,verbsBc1c2.get(offset));
                else
                    inst.setValue(34+offset,0);
                ++offset;
            }

            offset = 0;
            ArrayList<String> conceptsBc1c2 = sentRep.getConceptsBc1c2();
            int numConceptsBc1c2 = conceptsBc1c2.size();
            while(offset<maxConcepts){
                if(offset < numConceptsBc1c2)
                    addValueToInstance(inst,45+offset,conceptsBc1c2.get(offset));
                else
                    inst.setValue(45+offset,0);
                ++offset;
            }

            unlabeled.add(inst);
            Instances unlabeledFiltered = Filter.useFilter(unlabeled, this.bow);

            String modelFolderPath = RelExtConstants.wekaModelsFolder;
            HashMap<String,Double> labelsMap = new HashMap<>();
            for (int j = 0; j < RelExtConstants.labels.length ; j++){
                String label = RelExtConstants.labels[j];
                String modelPath = modelFolderPath + label + "_libsvm.model";
                Instance toLabel = unlabeledFiltered.get(0);

                // deserialize model
                Classifier classifier = (Classifier) SerializationHelper.read(modelPath);
                double[] probarr = classifier.distributionForInstance(toLabel);
                double prediction = probarr[0];
    //                double score = weights.get(label) * prediction;
                double score = prediction;
                labelsMap.put(label,score);
            }


            return labelsMap;
        }
        catch(Exception e){
            System.out.println("Exception while calculating machine learning labels!!!\n");
            e.printStackTrace();
            System.out.println("\nExiting...");
            System.exit(0);
            return null;
        }
    }

    /**
     * insert value to instance only if it fits with a value in the attribue values of the header
     * @param inst
     * @param index
     * @param value
     */
    private void addValueToInstance(Instance inst, int index, String value){
        ArrayList<Object> possibleValues = Collections.list(inst.attribute(index).enumerateValues());
        if(possibleValues.contains(value))
            inst.setValue(index,value);
        else
            inst.setValue(index,0);

    }
}
