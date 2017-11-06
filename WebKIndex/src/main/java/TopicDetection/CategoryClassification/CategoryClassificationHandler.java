package TopicDetection.CategoryClassification;

import Functions.FileFunctions;
import Functions.ServiceFunctions;
import MongoDB.MongoDBConstants;
import MongoDB.MongoDBHandler;
import TopicDetection.TopicDetectionConstants;
import TopicDetection.mongo.GettingDataFromMongo;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CategoryClassificationHandler {

    private String language;
    private String db;

    public CategoryClassificationHandler(String language_code){
        if(language_code.equals("de"))
            this.language= "german";
        else if(language_code.equals("tr"))
            this.language = "turkish";
        if(this.language.equals("german"))
            this.db = CategoryClassificationConstants.DB_DE;
        else if(this.language.equals("turkish"))
            this.db = CategoryClassificationConstants.DB_TR;
    }

    public List<String> getDataHTMLByCategory(String category, int numTweets){
        List<String> dataHtml = new ArrayList<>();
        MongoDBHandler mdbh = new MongoDBHandler(this.db, CategoryClassificationConstants.USERNAME,
                CategoryClassificationConstants.PASSWORD, this.db);
        mdbh.find(CategoryClassificationConstants.CATEGORY_COLLECTION, CategoryClassificationConstants.CATEGORY_FIELD,
                getCategoryCode(category), Sorts.descending("creationDate"));
        List<Document> results = mdbh.getResults();
        int count = 1;
        for (Document result : results){
            String id = result.get("_id").toString();
            String tweetID = id.split("#")[1];
            String oembedJson = ServiceFunctions.sendGetRequest(TopicDetectionConstants.TWITTER_BASE_URL + tweetID);
            if(oembedJson.startsWith("Error code returned: "))
                continue;
            JSONObject oembedRoot = new JSONObject(oembedJson);
            String html = oembedRoot.getString("html");
            dataHtml.add(html);
            count++;
            if(count > numTweets)
                break;
        }
        return dataHtml;
    }

    public String getCategoryCode(String category){
        if(category.equalsIgnoreCase("Economy"))
            return "ebf";
        else if(category.equalsIgnoreCase("Health"))
            return "h";
        else if(category.equalsIgnoreCase("Lifestyle"))
            return "ll";
        else if(category.equalsIgnoreCase("Nature"))
            return "ne";
        else if(category.equalsIgnoreCase("Politics"))
            return "p";
        else if(category.equalsIgnoreCase("Science"))
            return "st";
        else
            return "p";
    }

    public boolean updateTweetCategories(){
        try {
            executePythonScriptCmd();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void executePythonScriptCmd() throws IOException,
            InterruptedException {

        String command = CategoryClassificationConstants.PYTHON_PATH + " " + CategoryClassificationConstants.SCRIPT;
        Process p = new ProcessBuilder("cmd.exe", "/C",
                command + " " + this.language)
                .directory(new File(CategoryClassificationConstants.SCRIPT_PATH))
                .redirectErrorStream(true).start();
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String line="";
        while((line=reader.readLine())!=null)
        {
            System.out.println(line);
        }
        reader.close();
    }
}
