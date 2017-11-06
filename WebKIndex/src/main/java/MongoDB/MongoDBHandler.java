package MongoDB;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.bson.BsonDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;


/**
 * Created by spyridons on 3/2/2017.
 */
public class MongoDBHandler {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String databaseName;
    private List<Document> results;

    public MongoDBHandler(String db, String userName, String password){
        this.databaseName = db;
        MongoCredential credentials = MongoCredential.createScramSha1Credential(userName, MongoDBConstants.authenticationDB, password.toCharArray());
        this.mongoClient = new MongoClient(new ServerAddress(MongoDBConstants.host + ":" + MongoDBConstants.port), Arrays.asList(credentials));
        this.database = this.mongoClient.getDatabase(db);
    }

    public MongoDBHandler(String db, String userName, String password, String authenticationDb){
        this.databaseName = db;
        MongoCredential credentials = MongoCredential.createScramSha1Credential(userName, authenticationDb, password.toCharArray());
        this.mongoClient = new MongoClient(new ServerAddress(MongoDBConstants.host + ":" + MongoDBConstants.port), Arrays.asList(credentials));
        this.database = this.mongoClient.getDatabase(db);
    }

    public void close(){
        mongoClient.close();
    }

    public void insertMapToDB(Map<String,Object> docMap, String collectionName){
        Document document = new Document(docMap);
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        collection.insertOne(document);
    }

    public void insertDocToDB(Document document, String collectionName){
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        collection.insertOne(document);
    }

    public void clearCollection(String collectionName){
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        collection.deleteMany(new BsonDocument());
    }

    public void findAll(String collectionName){
        // get search iterable object
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        FindIterable<Document> iterable = collection.find();
//                .projection(excludeId());
        // set document results
        setResults(iterable);
    }

    public void findAll(String collectionName, List<String> fields){
        // get search iterable object
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        FindIterable<Document> iterable = collection.find()
                .projection(fields(include(fields), excludeId()));
        // set document results
        setResults(iterable);
    }

    public void find(String collectionName, String fieldName, String value, Bson sortBy){
        // get search iterable object
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(Filters.eq(fieldName,value));

        if(sortBy !=null)
            iterable = iterable.sort(sortBy);
//                .projection(excludeId());
        // set document results
        setResults(iterable);
    }

    public void find(String collectionName, String fieldName, String value, List<String> fields, Bson sortBy){
        // get search iterable object
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(Filters.eq(fieldName,value))
                .projection(fields(include(fields), excludeId()));

        if(sortBy !=null)
            iterable = iterable.sort(sortBy);
        // set document results
        setResults(iterable);
    }

    public void findContains(String collectionName, String fieldName, String value){
        // get search iterable object
        MongoCollection<Document> collection = this.database.getCollection(collectionName);
        String regexContains = ".*" + value + ".*";
        FindIterable<Document> iterable = collection.find(Filters.regex(fieldName, regexContains))
                .projection(excludeId());
        // set document results
        setResults(iterable);
    }

    /**
     * iterates an iterable object to set document search results
     * @param iterable
     */
    private void setResults(FindIterable<Document> iterable){
        MongoCursor<Document> cursor = iterable.iterator();
        List<Document> docs = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                // add new doc to list, after removing id field
                Document newDoc = cursor.next();
                docs.add(newDoc);
            }
        } finally {
            cursor.close();
        }
        this.results = docs;
    }

    public List<Document> getResults() {
        return this.results;
    }

    public String getResultsJSON() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.results);
    }
}
