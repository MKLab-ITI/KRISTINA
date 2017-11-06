package RelExtFusion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by spyridons on 9/5/2016.
 */
public class RelExtConstants {
    public static String rootFolder = "relExtFiles/";
    public static String wekaFolder = rootFolder + "weka/";
    public static String wekaDatasetsFolder = wekaFolder + "datasets/";
    public static String attributeSampleFilePath = wekaDatasetsFolder + "attribute_sample_v3.arff";
    public static String wekaModelVersion = "v3.1";
    public static String wekaModelsFolder = wekaFolder + "models/" + wekaModelVersion + "/";
    public static String pipelineWeights = "equal";

    public static String lexiconsFolder = rootFolder + "lexicons/";
    public static String diseaseLexiconFile = lexiconsFolder + "disease.txt";
    public static String treatmentLexiconFile = lexiconsFolder + "treatment.txt";
    public static String testLexiconFile = lexiconsFolder + "test.txt";

    public static String[] labels = {"TrIP", "TrWP", "TrCP", "TrAP", "TrNAP", "PIP", "TeRP", "TeCP"};
    public static String[] labelsWithNum = {"TrIP1", "TrIP2", "TrIP3", "TrWP1", "TrWP2", "TrWP3", "TrCP1", "TrCP2", "TrCP3",
            "TrAP1", "TrAP2", "TrAP3", "TrNAP1", "TrNAP2", "TrNAP3", "PIP1", "PIP2", "PIP3", "TeRP1", "TeRP2", "TeRP3", "TeCP1", "TeCP2", "TeCP3"};

    // paths for experiments only
    public static String outFolder = rootFolder + "output/";
    public static String resultFileName = "result.txt";
    public static String gtFileName = "gt.txt";

    public static final Map<String,String> relationsExplained = new HashMap<>();
    static {
        relationsExplained.put("TrIP","Treatment improves medical problem");
        relationsExplained.put("TrWP","Treatment worsens medical problem");
        relationsExplained.put("TrCP","Treatment causes medical problem");
        relationsExplained.put("TrAP","Treatment is administered for medical problem");
        relationsExplained.put("TrNAP","Treatment is not administered because of medical problem");
        relationsExplained.put("PIP","Medical problem indicates medical problem");
        relationsExplained.put("TeRP","Treatment reveals medical problem");
        relationsExplained.put("TeCP","Treatment conducted to investigate medical problem");
    }

}