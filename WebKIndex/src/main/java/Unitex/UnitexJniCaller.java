package Unitex;

import fr.umlv.unitex.jni.UnitexJni;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by spyridons on 10/7/2016.
 */
public class UnitexJniCaller {


    /***
     * get unitex annotations from raw text (for service)
     * @param text
     * @return
     */
    public String getUnitexAnnotationsFromText(String text, String graphName){
        String graphResDir = UnitexJni.combineUnitexFileComponent(UnitexConstants.resourceDir, "graph");
        String dictionnaryResDir = UnitexJni.combineUnitexFileComponent(UnitexConstants.resourceDir, "dictionnary");
        String othersResDir = UnitexJni.combineUnitexFileComponent(UnitexConstants.resourceDir, "others");
        UnitexJni.setStdOutTrashMode(true);
        List<String> workingDicoFileNames = Arrays.asList(
                UnitexJni.combineUnitexFileComponent(dictionnaryResDir, "dela-en-public.bin"),
                UnitexJni.combineUnitexFileComponent(dictionnaryResDir, "diseaseDELACF_v1.bin"),
                UnitexJni.combineUnitexFileComponent(dictionnaryResDir, "testDELACF_v1.bin"),
                UnitexJni.combineUnitexFileComponent(dictionnaryResDir, "TreatDELACF_v1.bin"),
                UnitexJni.combineUnitexFileComponent(dictionnaryResDir, "vaccineDELAF_v1.bin")
        );
        String graphFileName = UnitexJni.combineUnitexFileComponent(graphResDir, graphName);
        String workingGraphFileName = graphFileName;
        String CorpusWorkPath = UnitexConstants.baseWorkDir;
        UnitexJni.createUnitexFolder(CorpusWorkPath);
        UnitexJni.createUnitexFolder(UnitexJni.combineUnitexFileComponent(CorpusWorkPath,"output_snt"));

        String res = "";
        res= UnitexJniHandler.processUnitexWork(othersResDir,workingDicoFileNames,workingGraphFileName,CorpusWorkPath, text);
        return res;
    }

}
