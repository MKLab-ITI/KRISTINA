package Indexing.ContextRetrieval;

import Indexing.IndexCONSTANTS;
import Indexing.LuceneCustomClasses.SnowBallSpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.List;
import java.util.Map;

/**
 * Created by spyridons on 2/2/2017.
 */
public class ContextRetrievalMain {
    public static void main(String[] args) throws ParseException {

        long start = System.currentTimeMillis();

        ContextRetrievalHandler crhTest = new ContextRetrievalHandler(IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
                IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER, "Indices/v.6/es");
        QueryParser parser = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, new SnowBallSpanishAnalyzer());
        Query query = parser.parse("espalda");
        crhTest.queryAndConstructResultMaps(query);
        List<Map.Entry<DocPassageIDPair,Double>> topDocs = crhTest.getTopNDocs(5);
        for (Map.Entry<DocPassageIDPair,Double> doc : topDocs){
            doc.getKey().printFields();
            System.out.println("Score: " + doc.getValue());
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println();
        System.out.println("Elapsed time (millis): " + elapsed);
    }

}
