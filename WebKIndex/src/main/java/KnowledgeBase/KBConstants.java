package KnowledgeBase;

import RelExtFusion.RelExtConstants;

/**
 * Created by spyridons on 10/14/2016.
 */
public class KBConstants {
    public static int maxQueryResults = 10;
    public static int maxSentenceLimit = 100;
    public static String language = "en";
    public static int indexVersionToUse = 0;
    public static String queryFileName = RelExtConstants.rootFolder + "query/concepts.txt";
    public static String inputKBFileName = RelExtConstants.rootFolder + "query/kbInput.txt";
    public static String[] tagsToRemove = {"manual", "TREAT", "DRUG", "manual_drug", "Cad_drug", "conj_no", "GENEU",
            "manual_DIS", "Disease", "SignORSymptom", "TEST", "Guest", "without", "DIS_NO"};
    public static String[] conceptTypesDBPedia = {"DBpedia:AnatomicalStructure", "DBpedia:ChemicalStructure",
            "DBpedia:Disease", "DBpedia:Drug", "DBpedia:Database", "DBpedia:Protein", "DBpedia:Species",
            "Schema:Hospital"};
}
