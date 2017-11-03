package gr.iti.mklab.kindex.ConceptExtraction.UPF;

import gr.iti.mklab.kindex.Functions.ServiceFunctions;
import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConceptExtractionHandler {

    public static final String SERVICE_BASE_URL = "http://kristina.taln.upf.edu/services_dev/language_analysis/get_concepts_from_forms";

    public static List<String> extractConcepts(String content, String language){

        String completeUrl = SERVICE_BASE_URL + "?input-language=" + language;
        String requestBodyFormat = "application/json";
        String tokens = new JSONArray(content.split("\\s+")).toString();

        // call service
        String serviceOutput = ServiceFunctions.sendPostRequestRaw(completeUrl, tokens, requestBodyFormat);
        System.out.println("Service output:" + serviceOutput);
        if(serviceOutput.startsWith("Error code") || serviceOutput.startsWith("IO Exception"))
            serviceOutput = "[]";
        JSONArray arr = new JSONArray(serviceOutput);
        List<String> concepts = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            concepts.add(arr.get(i).toString());
        }

        return concepts;
    }

    public static List<String> extractConcepts(List<String> tokensList, String language){

        String completeUrl = SERVICE_BASE_URL + "?input-language=" + language;
        String requestBodyFormat = "application/json";
        String tokens = new JSONArray(tokensList).toString();

        // call service
        String serviceOutput = ServiceFunctions.sendPostRequestRaw(completeUrl, tokens, requestBodyFormat);
        System.out.println("Service output:" + serviceOutput);
        JSONArray arr = new JSONArray(serviceOutput);
        List<String> concepts = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            concepts.add(arr.get(i).toString());
        }

        return concepts;
    }

    public static void main(String[] args) {
//        String text = "Wetter gut Zeitung";
        String text = "¿Cuándo me subirá la leche?";
        JSONArray arr = new JSONArray(text.split("\\s+"));
        List<String> concepts = extractConcepts(text, "es");
        for (String concept: concepts) {
            System.out.println(concept);
        }
    }
}
