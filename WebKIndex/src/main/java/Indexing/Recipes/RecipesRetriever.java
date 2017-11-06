package Indexing.Recipes;

import Indexing.IndexCONSTANTS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by spyridons on 2/24/2017.
 */
public class RecipesRetriever {
    private String language;
    Analyzer analyzer;
    Directory directory;
    IndexWriterConfig config;
    IndexWriter writer;
    String folderName;
    int NO_OF_RESULTS_QUERY = 1;

    /**
     * Constructor , creates File index in default directory ({@link IndexCONSTANTS#INDEX_PATH}).<br>
     *
     */
    public RecipesRetriever(String language){
        this.language = language;
        analyzer = new ClassicAnalyzer();
        if(language.equals("pl"))
            analyzer = new PolishAnalyzer();
        Path path= Paths.get(IndexCONSTANTS.RECIPE_INDEX_PATH + language);
        try {
            directory = FSDirectory.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        folderName=IndexCONSTANTS.RECIPE_INDEX_PATH + language;
        try {
            config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(directory, config);
            writer.close();
            writer = null;
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever()  Could NOT create writer");
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
            System.out.println("WebKIndex :: RecipesRetriever.openWriter()  Could NOT create writer");
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
            System.out.println("WebKIndex :: RecipesRetriever.closeWriter()  Could NOT close writer");
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
            System.out.println("WebKIndex :: RecipesRetriever.close() Could NOT close writer.");
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
            System.out.println("WebKIndex :: RecipesRetriever.isEmpty() Could NOT get Doc Count.");
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public String makeRecipeQuery(String queryStr){

        DirectoryReader reader = null;
        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever.makeRecipeQuery()  Reader could NOT open directory");
            e.printStackTrace();
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
        searcher.setSimilarity(similarityVS);

        TopDocs candidate = null;

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
        try {

            // query recipe title
            QueryParser titleParser = new QueryParser(IndexCONSTANTS.FIELD_TITLE, analyzer);
            Query titleQuery = titleParser.parse(QueryParser.escape(queryStr));
            finalQuery.add(titleQuery, BooleanClause.Occur.SHOULD);

            // query recipe content
            QueryParser contentParser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
            Query contentQuery = contentParser.parse(QueryParser.escape(queryStr));
            finalQuery.add(contentQuery, BooleanClause.Occur.SHOULD);
        } catch (ParseException e) {
            System.out.println("WebKIndex :: RecipesRetriever.makeRecipeQuery() Could NOT parse the queryString.");
            e.printStackTrace();
            return null;
        }
        Query query = finalQuery.build();
        try {
            candidate = searcher.search(query, NO_OF_RESULTS_QUERY);
            //candidate = searcher.search(query, reader.maxDoc());
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever.makeRecipeQuery() Could NOT Search REcipes for query: " + queryStr);
            //e.printStackTrace();
            return null;
        }
        String response="";
        if (candidate.scoreDocs.length>0){
            try {
//                response = docToContent(searcher.doc(candidate.scoreDocs[0].doc));
                response = searcher.doc(candidate.scoreDocs[0].doc).get(IndexCONSTANTS.FIELD_URL);
            } catch (IOException e) {
                System.out.println("WebKIndex :: RecipesRetriever.makeRecipeQuery() Could NOT Get top document for query: " + queryStr);
                //e.printStackTrace();
            }
        }

        return response;

    }

    private String docToContent(Document doc) {
        String content = "";
        Iterator<IndexableField> iter = doc.iterator();
        while (iter.hasNext()) {
            IndexableField temp = iter.next();
            if (temp.name().equals(IndexCONSTANTS.FIELD_CONTENT)){
                content = temp.stringValue();
            }
        }
        return content;
    }

    public String randomRecipeQuery(){
        int[] ids = null;
        if(this.language.equals("pl"))
            ids = new int[]{211, 505, 185, 691, 162, 698, 222, 564, 1, 525, 558, 125, 0};
        else if(this.language.equals("de"))
            ids = new int[]{108, 311, 641, 386, 608, 10, 77, 7, 335, 417, 456, 8, 12, 130};
        DirectoryReader reader = null;
        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever.randomRecipeQuery()  Reader could NOT open directory");
            e.printStackTrace();
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        Random random = new Random();
        int randomId = random.nextInt(ids.length);
        System.out.println("Id: " + randomId);
        String response = null;
        try {
            response = searcher.doc(ids[randomId]).get(IndexCONSTANTS.FIELD_URL);
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever.randomRecipeQuery()  " +
                    "Could NOT get random document with id: " + randomId);
            e.printStackTrace();
        }
        return response;
    }
}
