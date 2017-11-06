package Indexing.Newspapers;

import Indexing.IndexCONSTANTS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by spyridons on 2/24/2017.
 */
public class NewspapersRetriever {
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
    public NewspapersRetriever(String language){

        analyzer = new ClassicAnalyzer();
        if(language.equals("tr"))
            analyzer = new TurkishAnalyzer();
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
            System.out.println("WebKIndex :: NewspapersRetriever()  Could NOT create writer");
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
            System.out.println("WebKIndex :: NewspapersRetriever.openWriter()  Could NOT create writer");
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
            System.out.println("WebKIndex :: NewspapersRetriever.closeWriter()  Could NOT close writer");
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
            System.out.println("WebKIndex :: NewspapersRetriever.close() Could NOT close writer.");
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
            System.out.println("WebKIndex :: NewspapersRetriever.isEmpty() Could NOT get Doc Count.");
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public String makeNewspaperQuery(String queryStr){

        DirectoryReader reader = null;
        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("WebKIndex :: NewspapersRetriever.makeNewspaperQuery()  Reader could NOT open directory");
            e.printStackTrace();
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        Similarity similarityVS = new DefaultSimilarity(); //lucene default similarity is Vector Space
        searcher.setSimilarity(similarityVS);

        TopDocs candidate = null;

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
        try {

            // query newspaper title
            QueryParser titleParser = new QueryParser(IndexCONSTANTS.FIELD_TITLE, analyzer);
            Query titleQuery = titleParser.parse(QueryParser.escape(queryStr));
            finalQuery.add(titleQuery, BooleanClause.Occur.SHOULD);

            // query newspaper subtitle
            QueryParser subtitleParser = new QueryParser(IndexCONSTANTS.FIELD_SUBTITLE, analyzer);
            Query subtitleQuery = subtitleParser.parse(QueryParser.escape(queryStr));
            finalQuery.add(subtitleQuery, BooleanClause.Occur.SHOULD);

        } catch (ParseException e) {
            System.out.println("WebKIndex :: RecipesRetriever.makeNewspaperQuery() Could NOT parse the queryString.");
            e.printStackTrace();
            return null;
        }
        Query query = finalQuery.build();
        try {
            candidate = searcher.search(query, NO_OF_RESULTS_QUERY);
            //candidate = searcher.search(query, reader.maxDoc());
        } catch (IOException e) {
            System.out.println("WebKIndex :: RecipesRetriever.makeNewspaperQuery() Could NOT Search REcipes for query: " + queryStr);
            //e.printStackTrace();
            return null;
        }
        String response="";
        if (candidate.scoreDocs.length>0){
            try {
                response = docToContent(searcher.doc(candidate.scoreDocs[0].doc));
            } catch (IOException e) {
                System.out.println("WebKIndex :: RecipesRetriever.makeNewspaperQuery() Could NOT Get top document for query: " + queryStr);
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
}
