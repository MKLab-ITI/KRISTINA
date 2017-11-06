package Version;

import java.util.*;

/**
 * Created by spyridons on 11/30/2016.
 */
public class PassageRetrievalVersion {

    // STATIC VARIABLES AND FUNCTIONS START

    // passage retrieval component info per version
    // info: index version and retrieval method
    public static final Map<String, PassageRetrievalVersion> componentVersionItems = new TreeMap<>();
    static {
        componentVersionItems.put("0.0", new PassageRetrievalVersion(0,"indexBased"));
        componentVersionItems.put("1.0", new PassageRetrievalVersion(1,"indexBased"));
        componentVersionItems.put("2.0", new PassageRetrievalVersion(2,"indexBased"));
        componentVersionItems.put("2.1", new PassageRetrievalVersion(2,"documentBased"));
        componentVersionItems.put("3.0", new PassageRetrievalVersion(3,"documentBased"));
        componentVersionItems.put("4.1", new PassageRetrievalVersion(4,"documentBased"));
        componentVersionItems.put("4.2", new PassageRetrievalVersion(4,"documentBasedRefined"));
        componentVersionItems.put("4.3", new PassageRetrievalVersion(4,"documentParagraphBased"));
        componentVersionItems.put("5.1", new PassageRetrievalVersion(5,"documentBased"));
        componentVersionItems.put("5.2", new PassageRetrievalVersion(5,"documentBasedRefined"));
        componentVersionItems.put("6.1", new PassageRetrievalVersion(6,"documentBased"));
        componentVersionItems.put("6.2", new PassageRetrievalVersion(6,"documentBasedRefined"));
        componentVersionItems.put("6.3", new PassageRetrievalVersion(6,"documentParagraphBased"));
        componentVersionItems.put("6.4", new PassageRetrievalVersion(6,"documentParagraphContextBased"));
        componentVersionItems.put("7.0", new PassageRetrievalVersion(7,"indexBased"));
        componentVersionItems.put("7.1", new PassageRetrievalVersion(7,"documentBased"));
        componentVersionItems.put("7.2", new PassageRetrievalVersion(7,"documentBasedRefined"));
        componentVersionItems.put("7.3", new PassageRetrievalVersion(7,"documentParagraphBased"));
        componentVersionItems.put("7.4", new PassageRetrievalVersion(7,"documentParagraphContextBased"));
        componentVersionItems.put("8.0", new PassageRetrievalVersion(8,"indexBased"));
        componentVersionItems.put("8.1", new PassageRetrievalVersion(8,"documentBased"));
        componentVersionItems.put("8.2", new PassageRetrievalVersion(8,"documentBasedRefined"));
        componentVersionItems.put("8.3", new PassageRetrievalVersion(8,"documentParagraphBased"));
        componentVersionItems.put("8.4", new PassageRetrievalVersion(8,"documentParagraphContextBased"));
        componentVersionItems.put("9.0", new PassageRetrievalVersion(9,"indexBased"));
        componentVersionItems.put("9.1", new PassageRetrievalVersion(9,"documentBased"));
        componentVersionItems.put("9.2", new PassageRetrievalVersion(9,"documentBasedRefined"));
        componentVersionItems.put("9.3", new PassageRetrievalVersion(9,"documentParagraphBased"));
        componentVersionItems.put("9.4", new PassageRetrievalVersion(9,"documentParagraphContextBased"));
        componentVersionItems.put("10.0", new PassageRetrievalVersion(10,"indexBased"));
        componentVersionItems.put("10.1", new PassageRetrievalVersion(10,"documentBased"));
        componentVersionItems.put("10.2", new PassageRetrievalVersion(10,"documentBasedRefined"));
        componentVersionItems.put("10.3", new PassageRetrievalVersion(10,"documentParagraphBased"));
        componentVersionItems.put("10.4", new PassageRetrievalVersion(10,"documentParagraphContextBased"));

    }

    // language support per index version
    private static final Map<Integer, String[]> indexVersionToLanguageMap = new TreeMap<>();
    static {
        indexVersionToLanguageMap.put(0, new String[] {"en","es","de","es_short"});
        indexVersionToLanguageMap.put(1, new String[] {"en","es","de"});
        indexVersionToLanguageMap.put(2, new String[] {"es"});
        indexVersionToLanguageMap.put(3, new String[] {"es"});
        indexVersionToLanguageMap.put(4, new String[] {"es"});
        indexVersionToLanguageMap.put(5, new String[] {"es"});
        indexVersionToLanguageMap.put(6, new String[] {"es"});
        indexVersionToLanguageMap.put(7, new String[] {"es"});
        indexVersionToLanguageMap.put(8, new String[] {"es"});
        indexVersionToLanguageMap.put(9, new String[] {"es","de","pl"});
        indexVersionToLanguageMap.put(10, new String[] {"es","de","pl"});
    }


    public static boolean isVersionSupported(String version){
        if(componentVersionItems.keySet().contains(version))
            return true;
        else
            return false;
    }

    public static String getSupportedVersions(){
        StringBuilder sb = new StringBuilder();
        String prefix = "Supported versions: ";
        for (String version: componentVersionItems.keySet()){
            sb.append(prefix + version);
            prefix = ",";
        }
        return sb.toString();
    }

    public static String getSupportedVersionsDetailed(){
        StringBuilder sb = new StringBuilder();
        sb.append("\nSUPPORTED VERSIONS\n\n");
        for (String version: componentVersionItems.keySet()){
            sb.append("Version " + version + "\n");
            sb.append("------------\n");
            sb.append(componentVersionItems.get(version).getVersionInfo() + "\n\n");
        }
        return sb.toString();
    }

    public static boolean isLanguageSupportedInIndexVersion(int indexVersion, String language){
        if( indexVersionToLanguageMap.containsKey(indexVersion)){
            List<String> availableLanguages = Arrays.asList(indexVersionToLanguageMap.get(indexVersion));
            if(availableLanguages.contains(language))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    public static String getSupportedLanguagesPerVersion(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nSUPPORTED LANGUAGES PER INDEX VERSION\n\n");
        String prefix = "";
        for (int version: indexVersionToLanguageMap.keySet()){
            sb.append("Version " + version + "\n");
            sb.append("------------\n");
            String[] availableLanguages = indexVersionToLanguageMap.get(version);
            for (String language: availableLanguages){
                sb.append(prefix + language);
                prefix = ",";
            }
            prefix = "";
            sb.append("\n\n");
        }
        return sb.toString();
    }

    // STATIC VARIABLES AND FUNCTIONS END

    private int indexVersion;
    private String retrievalMethod;

    public PassageRetrievalVersion(int indexVersion, String retrievalMethod){
        this.indexVersion = indexVersion;
        this.retrievalMethod = retrievalMethod;
    }

    public int getIndexVersion() {
        return indexVersion;
    }

    public void setIndexVersion(int indexVersion) {
        this.indexVersion = indexVersion;
    }

    public String getRetrievalMethod() {
        return retrievalMethod;
    }

    public void setRetrievalMethod(String retrievalMethod) {
        this.retrievalMethod = retrievalMethod;
    }

    public boolean isLanguageSupported(String language){
        List<String> availableLanguages = Arrays.asList(indexVersionToLanguageMap.get(indexVersion));
        if(availableLanguages.contains(language))
            return true;
        else
            return false;
    }

    public String getSupportedLanguages(){
        List<String> availableLanguages = Arrays.asList(indexVersionToLanguageMap.get(indexVersion));
        StringBuilder sb = new StringBuilder();
        String prefix = "Supported languages: ";
        for (String language: availableLanguages){
            sb.append(prefix + language);
            prefix = ",";
        }
        return sb.toString();
    }

    public String getVersionInfo(){
        return "Index version: " + this.indexVersion + "\nRetrieval method: " + this.retrievalMethod;
    }
}
