package Indexing;

import Functions.MapFunctions;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Class supporting passage retrieval functionality using Sequential Dependecence Model (SDM) proposed in (Choi et al., 2014)
 * Created by spyridons on 12/19/2016.
 */
public class SdmRetrievalHandler {

    private BooleanQuery singleTermQuery;
    private BooleanQuery orderedPairQuery;
    private BooleanQuery unorderedPairQuery;

    public BooleanQuery getSingleTermQuery() {
        return singleTermQuery;
    }

    public BooleanQuery getOrderedPairQuery() {
        return orderedPairQuery;
    }

    public BooleanQuery getUnorderedPairQuery() {
        return unorderedPairQuery;
    }

    public void buildQueries(List<String> terms, Query idFilterQuery){

        BooleanQuery.Builder singleTermBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder orderedPairBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder unorderedPairBuilder = new BooleanQuery.Builder();

        // single term
        for(String term:terms)
            singleTermBuilder.add(new TermQuery(new Term(IndexCONSTANTS.FIELD_CONTENT,term)), BooleanClause.Occur.SHOULD);

        // ordered and unordered pairs
        for(int i = 0; i < terms.size() - 1; i++){
            String firstTerm = terms.get(i);
            String secondTerm = terms.get(i+1);

            // ordered
            PhraseQuery.Builder pqBuilder = new PhraseQuery.Builder();
            pqBuilder.add(new Term(IndexCONSTANTS.FIELD_CONTENT,firstTerm));
            pqBuilder.add(new Term(IndexCONSTANTS.FIELD_CONTENT,secondTerm));
            pqBuilder.setSlop(IndexCONSTANTS.ORDERED_PAIR_WINDOW);
            PhraseQuery orderedQuery = pqBuilder.build();
            orderedPairBuilder.add(orderedQuery, BooleanClause.Occur.SHOULD);

            // unordered
            SpanQuery first = new SpanTermQuery(new Term(IndexCONSTANTS.FIELD_CONTENT, firstTerm));
            SpanQuery second = new SpanTermQuery(new Term(IndexCONSTANTS.FIELD_CONTENT, secondTerm));
            SpanNearQuery unorderedQuery = new SpanNearQuery(new SpanQuery[] {first, second}, IndexCONSTANTS.UNORDERED_PAIR_WINDOW, false);
            unorderedPairBuilder.add(unorderedQuery, BooleanClause.Occur.SHOULD);
        }

        // add doc ids to the queries
        singleTermBuilder.add(idFilterQuery, BooleanClause.Occur.FILTER);
        orderedPairBuilder.add(idFilterQuery, BooleanClause.Occur.FILTER);
        unorderedPairBuilder.add(idFilterQuery, BooleanClause.Occur.FILTER);


        // build queries
        singleTermQuery = singleTermBuilder.build();
        orderedPairQuery = orderedPairBuilder.build();
        unorderedPairQuery = unorderedPairBuilder.build();
    }

    public List<Map.Entry<Integer, Double>> retrieveDocuments(IndexSearcher searcher, int numDocs) throws IOException{
        // map with score for each document, initialize scores to 0
        Map<Integer, Double> scores = new HashMap<>();
        IndexReader reader = searcher.getIndexReader();
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        for (int i=0; i<reader.maxDoc(); i++) {
            // if doc is deleted
            if (liveDocs != null && !liveDocs.get(i)){
                continue;
            }
            scores.put(i, 0.0);
        }

        // SEARCH OPERATIONS
        // 1. Search for the single term query
        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(singleTermQuery, collector);
        int totalHits = collector.getTotalHits();
        if(totalHits>0){
            TopDocs hits = searcher.search(singleTermQuery, collector.getTotalHits());

            for(ScoreDoc scoreDoc : hits.scoreDocs) {
                int docID = scoreDoc.doc;

                Document doc = searcher.doc(docID);
                double oldScore = scores.get(docID);
                scores.put(docID, oldScore + IndexCONSTANTS.SINGLE_TERM_WEIGHT * scoreDoc.score);
            }
        }


        // 2. Search for the ordered pair query
        collector = new TotalHitCountCollector();
        searcher.search(orderedPairQuery, collector);
        totalHits = collector.getTotalHits();
        if(totalHits>0){
            TopDocs hits = searcher.search(orderedPairQuery, totalHits);

            for(ScoreDoc scoreDoc : hits.scoreDocs) {
                int docID = scoreDoc.doc;

                double oldScore = scores.get(docID);
                scores.put(docID, oldScore + IndexCONSTANTS.ORDERED_PAIR_WEIGHT * scoreDoc.score);
            }
        }

        // 3. Search for the unordered pair query
        collector = new TotalHitCountCollector();
        searcher.search(unorderedPairQuery, collector);
        totalHits = collector.getTotalHits();
        if(totalHits>0){
            TopDocs hits = searcher.search(unorderedPairQuery, totalHits);

            for(ScoreDoc scoreDoc : hits.scoreDocs) {
                int docID = scoreDoc.doc;

                double oldScore = scores.get(docID);
                scores.put(docID, oldScore + IndexCONSTANTS.UNORDERED_PAIR_WEIGHT * scoreDoc.score);
            }
        }

        // sort docs by score and get top N of them
        HashMap<Integer, Double> sortedScores = MapFunctions.sortByValue(scores);
        List<Map.Entry<Integer, Double>> topEntries = sortedScores.entrySet().stream()
                .limit(numDocs)
                .collect(toList());

        return topEntries;
    }
}
