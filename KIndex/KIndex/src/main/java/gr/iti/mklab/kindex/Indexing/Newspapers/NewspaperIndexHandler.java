package gr.iti.mklab.kindex.Indexing.Newspapers;

import gr.iti.mklab.kindex.Indexing.IndexCONSTANTS;
import gr.iti.mklab.kindex.KMongoDB.MongoDBHandler;
import gr.iti.mklab.kindex.KMongoDB.MongoSimmoContentConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by spyridons on 2/23/2017.
 */
public class NewspaperIndexHandler {
    Analyzer analyzer;
    Directory directory;
    IndexWriterConfig config;
    IndexWriter writer;
    String folderName;

    /**
     * Constructor , creates File index in default directory ({@link IndexCONSTANTS#INDEX_PATH}).<br>
     *
     */
    public NewspaperIndexHandler(String language){

        analyzer = new ClassicAnalyzer();
        if(language.equals("tr"))
            analyzer = new TurkishAnalyzer();
        System.out.println("Analyzer: " + analyzer.getClass());
        Path path= Paths.get(IndexCONSTANTS.NEWSPAPER_INDEX_PATH + language);
        try {
            directory = FSDirectory.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        folderName=IndexCONSTANTS.NEWSPAPER_INDEX_PATH + language;
        try {
            config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(directory, config);
            writer.close();
            writer = null;
        } catch (IOException e) {
            System.out.println("IndexHandler()  Could NOT create writer");
            //e.printStackTrace();
        }

    }

    /**
     * Open Index writer
     */
    public void openWriter() {
        try {
            config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(directory, config);
        } catch (IOException e) {
            System.out.println("KIndex :: IndexHandler.openWriter()  Could NOT create writer");
            e.printStackTrace();
        }
    }

    /**
     * Close Index Writer
     */
    public void closeWriter() {
        try {
            writer.close();
            writer = null;
        } catch (IOException e) {
            System.out.println("KIndex :: IndexHandler.closeWriter()  Could NOT close writer");
            e.printStackTrace();
        }
    }

    /**
     * Close TextIndexHandler
     *
     * @return Nothing
     */
    public void close(){
        try {
            directory.close();
            analyzer.close();

        } catch (IOException e) {
            System.out.println("KIndex :: TextIndexHandler.close() Could NOT close writer.");
            e.printStackTrace();
        }
    }

    /**
     * If Index handling by TextIndexHandler is empty
     *
     * @return boolean. True if Empty.
     */
    public boolean isEmpty(){
        try {
            DirectoryReader reader = DirectoryReader.open(directory);
            if (reader.getDocCount(IndexCONSTANTS.FIELD_CONTENT) > 0){
                reader.close();
                return false;
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("KIndex :: TextIndexHandler.isEmpty() Could NOT get Doc Count.");
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public boolean indexNewspaperJson(JSONObject jsonObj){

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder(); //query to check if doc already exists
        try {
            String value = jsonObj.getString(IndexCONSTANTS.FIELD_URL).replaceAll("(?=[]\\[+&|!(){}^\"~*?:/\\\\-])", "\\\\");
            if(!value.contains("article,"))
                return false;
            finalQuery.add(((new QueryParser(IndexCONSTANTS.FIELD_URL, analyzer)).parse("\"" + value + "\"")), BooleanClause.Occur.MUST); //double quotes because we need want exact matching
        } catch (ParseException e) {
            System.out.println("NewspaperIndexHandler.indexNewspaperJson() Could Not create QueryParser.");
            System.out.println("url: \"" + jsonObj.getString("url") );
            e.printStackTrace();
            return false;
        }

        DirectoryReader reader = null;
        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("NewspaperIndexHandler.indexNewspaperJson()  Reader could NOT open directory");
            e.printStackTrace();
            return false;
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs results = null;
        try {
            results = searcher.search(finalQuery.build(), 1);
        } catch (IOException e) {
            System.out.println("NewspaperIndexHandler.indexNewspaperJson() Could Not search in index to find out if doc exists.");
            e.printStackTrace();
            return false;
        }

        if (results.totalHits == 0) { //if doc created above does not exist in index
            try {

                Document doc = new Document(); //doc to add in index
                Iterator<?> keys = jsonObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    String value = jsonObj.getString(key);
                    doc.add(new TextField(key, value, Field.Store.YES));
                }
                writer.addDocument(doc);
                writer.commit();
                System.out.println("Indexing: " + jsonObj.getString(IndexCONSTANTS.FIELD_URL));
            } catch (IOException e) {
                System.out.println("NewspaperIndexHandler.indexNewspaperJson() Could Not add Document to writer.");
                e.printStackTrace();
                return false;
            }
        }
        else{
            System.out.println("Already Indexed: " + jsonObj.getString(IndexCONSTANTS.FIELD_URL));
            return false;
        }
        return true;
    }

    /**
     * Read the recordings of provided medium, in Simmo DB. Get all texts, <br>
     * and create index them
     *
     * @return Nothing
     */
    public void index(){

        String medium = IndexCONSTANTS.MEDIUM_WEBSITE;

        Date start_time=new Date();
        System.out.println("Indexing started at: "+start_time.toString());
        System.out.println("DB: "+ MongoSimmoContentConstants.NEWSPAPER_DB_NAME);


        //initialization for Simmo reading DB
        MongoDBHandler mDBh= new MongoDBHandler();
        long numberOfResults = mDBh.trgReadAll(medium);

        int i=1;

        while(mDBh.trgIterHasNext()) {

            String json = mDBh.trgIterNext();
            JSONArray jsonObjs = new JSONArray(json);
            for (Object jsonObjO : jsonObjs) {

				/*if(txtType.equals("HTML")){
					//extract text from html
					extracted_content = extractHTML(content, id);
				}*/

                JSONObject jsonObjFromMongo = new JSONObject(jsonObjO.toString());
                JSONObject temjObj = new JSONObject();
                String url = jsonObjFromMongo.getString("url");
                String content = jsonObjFromMongo.getString("content");

                NewspaperContentManager manager = new NewspaperContentManager(content);
                manager.parseHtml();
                temjObj.put(IndexCONSTANTS.FIELD_NEWSPAPER_TITLE, manager.getTitle());
                temjObj.put(IndexCONSTANTS.FIELD_NEWSPAPER_SUBTITLE, manager.getSubtitle());
                temjObj.put(IndexCONSTANTS.FIELD_CONTENT, manager.getContent());
                temjObj.put(IndexCONSTANTS.FIELD_MEDIUM, medium);
                temjObj.put(IndexCONSTANTS.FIELD_URL, url);
                temjObj.put(IndexCONSTANTS.FIELD_TYPE, jsonObjFromMongo.getString("type")); // ex. TEXT
                temjObj.put(IndexCONSTANTS.FIELD_TEXT_ID, jsonObjFromMongo.getString("text_id"));
                temjObj.put(IndexCONSTANTS.FIELD_WEBPAGE_ID, jsonObjFromMongo.getString("id"));

                openWriter();
                boolean indOK = false;
                if (temjObj.has(IndexCONSTANTS.FIELD_NEWSPAPER_TITLE) && temjObj.has(IndexCONSTANTS.FIELD_CONTENT))
                    indOK = indexNewspaperJson(temjObj);
                else
                    System.out.println("Title or content field is missing!!");
                //boolean indOK = inxh.indexString(content);
                closeWriter();

                if (indOK) {
                    System.out.println(" ");
                    System.out.print(i + "/" + numberOfResults + " -> Indexing: \"" + temjObj.getString("url") + "\",Medium: " + medium );
                    System.out.print(", " + IndexCONSTANTS.FIELD_WEBPAGE_ID + ": \""+ temjObj.getString(IndexCONSTANTS.FIELD_WEBPAGE_ID));

                    System.out.println("\", " + IndexCONSTANTS.FIELD_TEXT_ID + ": \""+temjObj.getString(IndexCONSTANTS.FIELD_TEXT_ID)+"\"");
                }
                else{
                    System.out.println(i + "/" + numberOfResults + " -> NOT INDEXED: \"" + temjObj.getString("url") + "\",Medium: " + medium );
                }

                i++;
            }

        }
        System.out.println(" ");
        System.out.println("Indexed " + (i-1) + " Documents");
        System.out.println("Indexing started at: " + start_time.toString());
        System.out.println("Indexing ended at: " + (new Date()).toString());

    }

    public static void main(String[] args) {
        NewspaperIndexHandler nih = new NewspaperIndexHandler("tr");
        nih.index();
    }
}
