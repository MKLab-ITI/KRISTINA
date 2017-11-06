package MetaMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nih.nlm.nls.metamap.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 4/4/2017.
 */
public class LocalMetaMapNewHandler {

    private String xmlConcepts;
    private Multimap<String, String> conceptsMap;

    public LocalMetaMapNewHandler(){
        conceptsMap = ArrayListMultimap.create();
    }

    public String getXmlConcepts() {
        return xmlConcepts;
    }

    public void setXmlConcepts(String xmlConcepts) {
        this.xmlConcepts = xmlConcepts;
    }

    public Multimap<String, String> getConceptsMap() {
        return conceptsMap;
    }

    public void setConceptsMap(Multimap<String, String> conceptsMap) {
        this.conceptsMap = conceptsMap;
    }

    public void parse(String text){

        //remove the empty lines from the text
        text = text.replaceAll("(?m)^[ \\t]*\\r?\\n", "");

        int charOffset = 0; // the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
        //int nlChar = 0; //the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually
        int minPos = -1;
        HashSet<Integer> startPos = new HashSet<Integer>();


        List<Result> resultList = null;
        boolean metamapWorked = false;
        while (!metamapWorked) {
            try {
                MetaMapApi api = new MetaMapApiImpl(MetaMapCONSTANTS.METAMAP_LOCAL_IP);
                api.setOptions("-J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs");
                resultList = api.processCitationsFromString(text);
                metamapWorked = true;
                api.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                try {Thread.sleep(1000);} catch(InterruptedException ex) {} //wait for one second
                System.out.println(" ");
                System.out.println("MetaMap Not Working!!!");
                System.out.println("If MetaMap is not running in the local installation, start it.");
                throw e;
//                System.out.println("When MetaMap is running again, press ENTER to continue.");
//                try {
//                    System.in.read();
//                } catch (IOException e1) {
//                    System.out.println("Could not read the input");
//                }
            }
        }

        try {
            for (Result result : resultList) {
                for (Utterance utterance : result.getUtteranceList()) {
                    for (PCM pcm : utterance.getPCMList()) {
                        for (Mapping map : pcm.getMappingList()) {
                            for (Ev mapEv : map.getEvList()) {

                                int initialTextLength = text.length();

                                String semType = mapEv.getSemanticTypes().get(0); //get always the first one
                                //later on the semTypes will be weighted
                                String semLabel = getConceptLabel(semType);

                                // for json output
                                String prefix = "";
                                String wordsString = "";
                                List<String> words = mapEv.getMatchedWords();
                                for (String word : words) {
                                    wordsString += prefix + word;
                                    prefix = " ";
                                }
                                if (semLabel != null) {
                                    conceptsMap.put(semLabel, wordsString);
                                }

                                Position position = mapEv.getPositionalInfo().get(0);
                                int start = position.getX();
                                int length = position.getY();

                                //check if the same Term is already annotated
                                if ((!startPos.contains(start)) && (start > minPos) && semLabel != null) {

                                    minPos = start + length - 1;
                                    startPos.add(start);
                                    //charOffset: the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
                                    //nlChar: the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually

                                    //nlChar = text.substring(0, start + charOffset + nlChar).split(System.getProperty("line.separator")).length - 1;

                                    String prev = text.substring(0, start + charOffset /*+ nlChar*/);
                                    String conceptTerms = text.substring(start + charOffset/* + nlChar*/, start + length + charOffset /*+ nlChar*/);
                                    //String conceptTerms = mapEv.getConceptName().replaceAll("[\\*\\^]","");
                                    //if (mapEv.getMatchedWords().size()>1){
                                    //	System.out.println("sad");
                                    //}
                                    //int length = conceptTerms.length();
                                    String next = text.substring(start + length + charOffset /*+ nlChar*/, text.length());
                                    if (!conceptTerms.matches("[a-zA-Z0-9].*")) { //in case concept Term's first character is not a letter or number
                                        Pattern p = Pattern.compile("\\p{L}");
                                        Matcher m = p.matcher(conceptTerms);
                                        if (m.find()) {
                                            int letterPos = m.start();
                                            int offset = conceptTerms.substring(0, letterPos).length();
                                            prev = text.substring(0, start + charOffset /*+ nlChar */ + offset);
                                            conceptTerms = text.substring(start + charOffset /*+ nlChar */ + offset, start + length + charOffset /*+ nlChar*/ + offset);
                                            next = text.substring(start + length + charOffset /*+ nlChar */ + offset, text.length());
                                        }
                                    }

                                    //MetaMapHandler must not "cut" the words
                                    if ((prev.equals("") || prev.endsWith(" ")) && (next.startsWith(" ") || next.matches("\\p{Punct} .*") || next.startsWith("\r\n"))) {
                                        text = prev + "<concept MetaMap=\"" + semLabel + "\">" + conceptTerms + "</concept>" + next;
                                        charOffset += text.length() - initialTextLength;
                                        //System.out.println("Offset: " + charOffset);
                                        //System.out.println("New Text: ");
                                        //System.out.println(text);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.xmlConcepts = text;
    }

    private String getConceptLabel(String concept){
        switch (concept) {
            //acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs
            case "acab":
                return "AcquiredAbnormality";
            case "anab":
                return "AnatomicalAbnormality";
            case "antb":
                return "Antibiotic";
            case "bact":
                return "Bacterium";
            case "bodm":
                return "BiomedicalOrDentalMaterial";
            case "comd":
                return "CellOrMolecularDysfunction";
            case "clnd":
                return "ClinicalDrug";
            case "cgab":
                return "CongenitalAbnormality";
            case "diap":
                return "DiagnosticProcedure";
            case "dsyn":
                return "DiseaseOrSyndrome";
            case "drdd":
                return "DrugDeliveryDevice";
            case "food":
                return "Food";
            case "inpo":
                return "InjuryOrPoisoning";
            case "lbpr":
                return "LaboratoryProcedure";
            case "medd":
                return "MedicalDevice";
            case "mobd":
                return "MentalOrBehavioralDysfunction";
            case "neop":
                return "NeoplasticProcess";
            case "patf":
                return "PathologicFunction";
            case "podg":
                return "PatientOrDisabledGroup";
            case "phsu":
                return "PharmacologicSubstance";
            case "sosy":
                return "SignOrSymptom";
            case "topp":
                return "TherapeuticOrPreventiveProcedure";
            case "virs":
                return "Virus";
        }
        return null;
    }
}
