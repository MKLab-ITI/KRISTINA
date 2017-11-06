package ConceptExtraction.DBpediaSpotlight;

import ConceptExtraction.AnnotationConcept;
import Functions.FileFunctions;
import Functions.ServiceFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 11/8/2016.
 */
public class DBpediaSpotlightHandler {

    /**
     * Get annotations from a text using DBpedia Spotlight service
     * @param text
     * @param language
     * @param confidence
     * @return
     */
    public Set<String> getAnnotations(String text, String language, String confidence){
        Set<String> annotations = new HashSet<>();

        // get appropriate url according to the requested language
        String url = getUrl(language);

        // remove control chars from the text
        String inputText = text.replaceAll("\\p{Cntrl}", "");

        HashMap<String,String> params = new HashMap<>();
        params.put("text",inputText);
        params.put("confidence", confidence);
        String response = ServiceFunctions.sendPostRequest(url, params, true);

        if(isJSONValid(response)){

            //get distinct annotations
            JSONObject obj = new JSONObject(response);
            if(obj.has("Resources"))
            {
                JSONArray arr = obj.getJSONArray("Resources");

                System.out.print("Selected Tags: ");
                for (int j = 0; j < arr.length(); j++)
                {
                    //there is a bug in the dbpedia spotlight service when the "null" String is given, so we ignore it
                    if(arr.getJSONObject(j).get("@surfaceForm").equals(null))
                        System.out.println("NULL string will be ignored");
                    else
                    {
                        String surfaceForm = arr.getJSONObject(j).getString("@surfaceForm").replace(" ", "_");
                        annotations.add(surfaceForm);
                        System.out.print(surfaceForm+", ");
                    }
                }
                System.out.println();
            }
        }

        return annotations;
    }

    public String getAnnotationsXML(String text, String language, String confidence, ArrayList<String> selectTypes){
        String annotatedText = null;

        // get appropriate url according to the requested language
        String url = getUrl(language);

        // remove control chars from the text
        String inputText = text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");

        HashMap<String,String> params = new HashMap<>();
        params.put("text",inputText);
        params.put("confidence", confidence);

        // form types String
        String typesParam = "";
        String prefix = "";
        for (String type: selectTypes){
            typesParam+= prefix + type;
            prefix = ",";
        }
        params.put("types", typesParam);

        String response = ServiceFunctions.sendPostRequest(url, params, true);

        // get concepts
        ArrayList<DBPediaSpotlightConcept> concepts = new ArrayList<>();
        if(isJSONValid(response)){

            //get distinct annotations
            JSONObject obj = new JSONObject(response);
            if(obj.has("Resources"))
            {
                JSONArray arr = obj.getJSONArray("Resources");

                System.out.print("Selected Tags: ");
                for (int j = 0; j < arr.length(); j++)
                {
                    //there is a bug in the dbpedia spotlight service when the "null" String is given, so we ignore it
                    if(arr.getJSONObject(j).get("@surfaceForm").equals(null))
                        System.out.println("NULL string will be ignored");
                    else
                    {
                        String conceptText = arr.getJSONObject(j).getString("@surfaceForm");
                        int start = Integer.parseInt(arr.getJSONObject(j).getString("@offset"));
                        Set<String> types = parseTypes(arr.getJSONObject(j).getString("@types"));
                        DBPediaSpotlightConcept concept = new DBPediaSpotlightConcept(conceptText,start,types);
                        concepts.add(concept);
                        System.out.print(conceptText+", ");
                    }
                }
                System.out.println();
            }
        }

        // sort concepts by their start index
        Collections.sort(concepts, AnnotationConcept.COMPARE_BY_START_INDEX);

        // form xml tagged text
        int charOffset = 0; // the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
        HashSet<Integer> startPos = new HashSet<Integer>();
        for (DBPediaSpotlightConcept concept: concepts){
            int initialTextLength = inputText.length();
            int start = concept.getStartIndex();
            int length = concept.getConcept().length();
            String typesString = concept.getTypesString();

            String prev = inputText.substring(0, start + charOffset);
            String conceptTerms = inputText.substring(start + charOffset, start + length + charOffset );
            String next = inputText.substring(start + length + charOffset /*+ nlChar*/, inputText.length());

            //DBPedia handler must not "cut" the words
            if ((prev.equals("") || prev.endsWith(" ")) && (next.startsWith(" ") || next.matches("\\p{Punct} .*") || next.startsWith("\r\n") || next.startsWith("\n"))) {
                inputText = prev + "<concept DBPedia=\"" + typesString + "\">" + conceptTerms + "</concept>" + next;
                charOffset += inputText.length() - initialTextLength;
                //System.out.println("Offset: " + charOffset);
                //System.out.println("New Text: ");
                //System.out.println(text);
            }

        }

        return inputText;
    }

    /**
     * Get annotations in DBPediaSpotlightConcept format
     * @param text
     * @param language
     * @param confidence
     * @param selectTypes
     * @return
     */
    public List<DBPediaSpotlightConcept> getAnnotations(String text, String language, String confidence, ArrayList<String> selectTypes){
        String annotatedText = null;

        // get appropriate url according to the requested language
        String url = getUrl(language);

        // remove control chars from the text
        String inputText = text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");

        HashMap<String,String> params = new HashMap<>();
        params.put("text",inputText);
        params.put("confidence", confidence);

        // form types String
        String typesParam = "";
        String prefix = "";
        for (String type: selectTypes){
            typesParam+= prefix + type;
            prefix = ",";
        }
        params.put("types", typesParam);

        String response = ServiceFunctions.sendPostRequest(url, params, true);

        // get concepts
        ArrayList<DBPediaSpotlightConcept> concepts = new ArrayList<>();
        if(isJSONValid(response)){

            //get distinct annotations
            JSONObject obj = new JSONObject(response);
            if(obj.has("Resources"))
            {
                JSONArray arr = obj.getJSONArray("Resources");

                System.out.print("Selected Tags: ");
                for (int j = 0; j < arr.length(); j++)
                {
                    //there is a bug in the dbpedia spotlight service when the "null" String is given, so we ignore it
                    if(arr.getJSONObject(j).get("@surfaceForm").equals(null))
                        System.out.println("NULL string will be ignored");
                    else
                    {
                        String conceptText = arr.getJSONObject(j).getString("@surfaceForm");
                        int start = Integer.parseInt(arr.getJSONObject(j).getString("@offset"));
                        Set<String> types = parseTypes(arr.getJSONObject(j).getString("@types"));
                        DBPediaSpotlightConcept concept = new DBPediaSpotlightConcept(conceptText,start,types);
                        concepts.add(concept);
                        System.out.print(conceptText+", ");
                    }
                }
                System.out.println();
            }
        }

        return concepts;
    }

    private Set<String> parseTypes(String typesString){
        Set<String> types = new HashSet<>();
        String[] typesArray = typesString.split(",");
        for (String typeString: typesArray) {
            String[] typeStringParts = typeString.split(":");
            if(typeStringParts[0].equals("DBpedia") || typeStringParts[0].equals("Schema"))
                types.add(typeStringParts[1]);
        }
        return types;
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public String getUrl(String language){
        if(language.equals("de"))
            return DBpediaSpotlightConstants.ANNOTATION_URL_DE;
        else if(language.equals("tr"))
            return DBpediaSpotlightConstants.ANNOTATION_URL_TR;
        else if(language.equals("es"))
            return DBpediaSpotlightConstants.ANNOTATION_URL_ES;
        else
            return DBpediaSpotlightConstants.ANNOTATION_URL_EN;
    }

    public boolean getAnnotationsUsingFiles(String fileIn, String fileOut, String language, String confidence, ArrayList<String> selectTypes){
        String inText = FileFunctions.readInputFile(fileIn);
        String outText = getAnnotationsXML(inText, language, confidence, selectTypes);
        boolean success = FileFunctions.writeToFile(fileOut, outText);
        return success;
    }
}
