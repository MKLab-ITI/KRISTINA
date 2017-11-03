package gr.iti.mklab.kindex.ConceptExtraction.DBpediaSpotlight;

import gr.iti.mklab.kindex.Functions.ServiceFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

        // try reserve url if service did not respond
        if (response.startsWith("Error code returned: ") || response.startsWith("IO Exception: ")){
            String reserveUrl = getReserveUrl(language);
            response = ServiceFunctions.sendPostRequest(reserveUrl, params, true);
        }

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

    public String getReserveUrl(String language){
        if(language.equals("de"))
            return DBpediaSpotlightConstants.RESERVE_ANNOTATION_URL_DE;
        else if(language.equals("tr"))
            return DBpediaSpotlightConstants.RESERVE_ANNOTATION_URL_TR;
        else if(language.equals("es"))
            return DBpediaSpotlightConstants.RESERVE_ANNOTATION_URL_ES;
        else
            return DBpediaSpotlightConstants.RESERVE_ANNOTATION_URL_EN;
    }

}
