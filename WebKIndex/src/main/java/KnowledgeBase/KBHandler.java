package KnowledgeBase;

import Functions.FileFunctions;
import Indexing.IndexCONSTANTS;
import Indexing.PassageIndexHandler;
import RelExtFusion.KBResult;
import RelExtFusion.RelExtPipeline;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by spyridons on 10/3/2016.
 */
public class KBHandler {

    private List<QueryObject> queries;
    private Set<DocumentResult> queryResults;

    public KBHandler(){
        String filePath = KBConstants.queryFileName;
        this.queries = new ArrayList<>();
        this.queryResults = new HashSet<>();
        generateQueries(filePath);
    }

    public KBHandler(String filePath){
        this.queries = new ArrayList<>();
        this.queryResults = new HashSet<>();
        generateQueries(filePath);
    }

    private void generateQueries(String filePath){
        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.out.println("Error while reading concepts file!!!");
            System.out.println("QueryObject cannot be generated!!!");
            return;
        }
        List<String> queryLines = Arrays.asList(fileContent.split("\r\n"));
        for (String queryLine: queryLines){
            addQuery(queryLine);
        }
    }

    private void addQuery(String queryLine){
        QueryObject query = new QueryObject();
        query.setMaxResults(KBConstants.maxQueryResults);
        query.setQueryLine(queryLine);
        if(queryLine.contains("+")){
            query.setOperator(QueryObject.Operator.AND);
            String[] terms = queryLine.split("[+]");
            for(String term: terms){
                query.addTerm(term.trim());
            }
        }
        else if(queryLine.contains("/")){
            query.setOperator(QueryObject.Operator.OR);
            String[] terms = queryLine.split("[/]");
            for(String term: terms){
                query.addTerm(term.trim());
            }
        }
        else{
            query.setOperator(QueryObject.Operator.NONE);
            query.addTerm(queryLine.trim());
        }
        this.queries.add(query);
    }

    public boolean generateInputKB()
    {
        // query lucene
        for(QueryObject query: this.queries){
            System.out.println("Query: " + query.getQueryLine());
            PassageIndexHandler pih = new PassageIndexHandler(KBConstants.language, KBConstants.indexVersionToUse);
            List<DocumentResult> results = pih.getDocsForRelExt(query);
            for (DocumentResult result: results)
                System.out.println("Result: " + result.getUrl());
            this.queryResults.addAll(results);
            System.out.println("Num results:" + results.size());
            System.out.println();
        }
        setZeroResultScores();

        System.out.println();
        System.out.println("Total unique urls: " + this.queryResults.size());
        System.out.println();

        ArrayList<KBResult> inputList = new ArrayList<>();
        int urlCount = 1;
        for (DocumentResult queryResult: queryResults) {
            String resultPageUrl = queryResult.getUrl();
            System.out.println("Relation extraction on url " + urlCount +" : " + resultPageUrl);
            String queryResultContent = queryResult.getText();
            String resultPageID = queryResult.getPageID();
            String resultTextID = queryResult.getTextID();
            // execute relation extraction pipeline
            RelExtPipeline pipeline = new RelExtPipeline();
            ArrayList<KBResult> queryKBResult = pipeline.executeNewPipeline(queryResultContent,
                    resultPageID, resultTextID, resultPageUrl);
            System.out.println("Number of extracted relations: " + queryKBResult.size());
            inputList.addAll(queryKBResult);
            System.out.println();
            ++urlCount;
        }

        if(inputList.size() == 0){
            System.out.println("Zero total number of relations!!!");
            return false;
        }
        String knowledgeBaseInput = new Gson().toJson(inputList);
        FileFunctions.writeToFile(KBConstants.inputKBFileName, knowledgeBaseInput);
        return true;
    }

    public boolean generateConceptFiles() throws IOException {
        // query lucene
        for(QueryObject query: this.queries){
            System.out.println("Query: " + query.getQueryLine());
            PassageIndexHandler pih = new PassageIndexHandler(KBConstants.language, KBConstants.indexVersionToUse);
            List<DocumentResult> results = pih.getDocsForRelExt(query);
            for (DocumentResult result: results)
                System.out.println("Result: " + result.getUrl());
            this.queryResults.addAll(results);
            System.out.println("Num results:" + results.size());
            System.out.println();
        }
        setZeroResultScores();

        System.out.println();
        System.out.println("Total unique urls: " + this.queryResults.size());
        System.out.println();

        ArrayList<KBResult> inputList = new ArrayList<>();
        int urlCount = 1;
        for (DocumentResult queryResult: queryResults) {
            System.out.println("Concept extraction on url " + urlCount +" : " + queryResult.getUrl());
            String queryResultContent = queryResult.getText();
            String resultPageID = queryResult.getPageID();
            String resultTextID = queryResult.getTextID();
            // execute relation extraction pipeline
            RelExtPipeline pipeline = new RelExtPipeline();
            ArrayList<String> selectTypes = new ArrayList<>(Arrays.asList(KBConstants.conceptTypesDBPedia));
            String annotatedText = pipeline.getDBPediaSpotlightConcepts(queryResultContent, selectTypes);
            // write original and annotated text to files
            FileFunctions.writeToFile("MetaMap/DBPedia_concepts/text/" + urlCount + ".txt", queryResultContent);
            FileFunctions.writeToFile("MetaMap/DBPedia_concepts/annotatedText/" + urlCount + ".txt", annotatedText);
            ++urlCount;
        }

        return true;
    }

    /**
     * set scores of results to zero as there is no point in storing them
     */
    private void setZeroResultScores()
    {
        for (DocumentResult queryResult: queryResults){
            queryResult.setScore(0);
        }
    }


    private String getConcepts(String url) throws IOException {

        System.out.println("Sending request to KB...");

        // Open url connection to server
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;

        // Set connection parameters
//        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        // Open outputstream to write the request
//        OutputStream out = connection.getOutputStream();
//        OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");
//        wout.write(requestBody);

//        wout.flush();
//        out.close();

        System.out.println("Response code: " + connection.getResponseCode());

        // Get the response
        InputStream in = connection.getInputStream();
        StringBuffer response = new StringBuffer();

        int c;
        while ((c = in.read()) != -1){
            response.append((char) c);
        }


        // Close streams and url connection
        in.close();
//        out.close();
        connection.disconnect();

        return response.toString();
    }


    public List<QueryObject> getQueries() {
        return queries;
    }

    public static boolean updateKB(String json){

        System.out.print("Updating KB...");

        String server = "http://160.40.X.Y:Z/kristina-j2ee-web/api/populate/update";
        String requestMethod = "POST";
        String request = "data=" + json;

        try {
            URL u = new URL(server);
            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;

            // Set connection parameters
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod(requestMethod);

            // Open outputstream to write the request
            OutputStream out = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");
            wout.write(request);

            wout.flush();
            out.close();

            System.out.println("Response code: " + connection.getResponseCode());

            // Get the response
            InputStream in = connection.getInputStream();
            StringBuffer response = new StringBuffer();

            int c;
            while ((c = in.read()) != -1){
                response.append((char) c);
            }


            // Close streams and url connection
            in.close();
            out.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("DONE!!!");
        return true;
    }
}
