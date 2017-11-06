package Indexing.ContextRetrieval;

import Functions.MapFunctions;
import Indexing.IndexCONSTANTS;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

import javax.print.Doc;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Class handling context based retrieval.
 * Selecting root folder based on language and version (NOT passage type) is already handled by PassageIndexHandler,
 * so it must be explicitly provided in this class
 * Created by spyridons on 1/23/2017.
 */
public class ContextRetrievalHandler {

    private static double docWeight = 0.85;
    private static double prevDocWeight = 0.1;
    private static double nextDocWeight = 0.05;

    private String indexType;
    private String rootDir;
    private String similarityModel;
    private IndexReader reader;
    private IndexSearcher searcher;

    // <pair, lucene doc id> where pair an object containing text_id and segment_id
//    private Map<DocPassageIDPair, Integer> documentMap;
    // <pair, query score>
    private Map<DocPassageIDPair, Double> sortedScoresMap;

    /**
     * Default values: Index -> Paragraph , Similarity -> LMJM, Index version -> 6 , Language -> es
     */
    public ContextRetrievalHandler(){
        this(IndexCONSTANTS.INDEX_TYPE_PARAGRAPH, IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER, "Indices/v.6/es");
    }

    public ContextRetrievalHandler(String indexType, String similarityModel, String rootDir){
        this.indexType = indexType;
        this.similarityModel = similarityModel;
        this.rootDir = rootDir;
        initializeSearcher(rootDir);
    }

    private void initializeSearcher(String rootDir){
        try {
            switch (this.indexType) {
                case IndexCONSTANTS.INDEX_TYPE_DOCUMENT:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_DOCUMENT)));
                    break;
                case IndexCONSTANTS.INDEX_TYPE_ALL_SEGMENTATION:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_ALL_SEGMENTATION)));
                    break;
                case IndexCONSTANTS.INDEX_TYPE_PARAGRAPH:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_PARAGRAPH)));
                    break;
                case IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_3_SENTENCES)));
                    break;
                case IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_2_SENTENCES)));
                    break;
                case IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE:
                    this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(rootDir + IndexCONSTANTS.INDEX_PATH_1_SENTENCE)));
                    break;
            }
        } catch (IOException e) {
            System.out.println("WebKIndex :: ContextRetrievalHandler.initializeSearcher()  Reader could NOT open directory");
            e.printStackTrace();
        }

        this.searcher = new IndexSearcher(this.reader);
        Similarity similarity = null;
        switch (similarityModel){
            case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
                similarity =  new DefaultSimilarity();
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
                similarity =  new LMDirichletSimilarity();
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
                similarity =  new LMJelinekMercerSimilarity((float)0.5);
                break;
        }
        this.searcher.setSimilarity(similarity);
    }


    public Map<DocPassageIDPair, Double> getSortedScoresMap() {
        return sortedScoresMap;
    }

    public void setSimilarityModel(String similarityModel){
        Similarity similarity = null;
        switch (similarityModel){
            case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
                similarity =  new DefaultSimilarity();
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
                similarity =  new LMDirichletSimilarity();
                break;
            case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
                similarity =  new LMJelinekMercerSimilarity((float)0.5);
                break;
        }
        this.searcher.setSimilarity(similarity);
    }

    public Document getDocumentByID(int id){
        try {
            return searcher.doc(id);
        } catch (IOException e) {
            System.out.println("WebKIndex :: ContextRetrievalHandler.getDocumentByID() IOException");
            return null;
        }
    }

    /**
     * execute a given query and fill documentMap and sortedScoresMap with the results
     * @param query
     */
    public void queryAndConstructResultMaps(Query query){
        List<String> singleItems = Arrays.asList(new String[] {IndexCONSTANTS.INDEX_TYPE_PARAGRAPH,
                IndexCONSTANTS.INDEX_TYPE_ONE_SENTNCE});
        List<String> multipleItems = Arrays.asList(new String[] {IndexCONSTANTS.INDEX_TYPE_TWO_SENTNCES,
                IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES});
        if(singleItems.contains(this.indexType))
            queryAndConstructResultMapsSingleItem(query);
        else if(multipleItems.contains(this.indexType))
            queryAndConstructResultMapsMultipleItems(query);
    }

    private void queryAndConstructResultMapsSingleItem(Query query){

        Map<DocPassageIDPair, Double> initialScoresMap = new HashMap<>();

        // execute query, collect initial scores (scores per doc that do not depend on neighbour doc scores)
        // and store them into maps
        TotalHitCountCollector collector = new TotalHitCountCollector();
        try {
            this.searcher.search(query, collector);
            int totalHits = collector.getTotalHits();
            if (totalHits > 0) {
                TopDocs hits = this.searcher.search(query, totalHits);

                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    int docID = scoreDoc.doc;
                    Document doc = this.searcher.doc(docID);
                    String docTextID = doc.get(IndexCONSTANTS.FIELD_TEXT_ID);
                    int segmentID = Integer.parseInt(doc.get(IndexCONSTANTS.FIELD_SEGMENT_ID));
                    DocPassageIDPair pair = new DocPassageIDPair(docTextID, segmentID);
                    double score = scoreDoc.score;
                    initialScoresMap.put(pair, score);
                }
            }
        }
        catch(IOException e){
            System.out.println("Exception while constructing ordered retrieval maps!!");
            return;
        }

        // calculate new scores based on neighbour segments
        Map<DocPassageIDPair, Double> finalScoresMap = new HashMap<>();
        for (Map.Entry<DocPassageIDPair,Double> entry: initialScoresMap.entrySet()){
            double docScore = entry.getValue();
            // for now exclude docs with score equal to 0
            if(docScore!=0){
                DocPassageIDPair docPair = entry.getKey();
                String docTextID = docPair.getDocID();
                int segmentID = docPair.getSegmentID();

//                if(getDocIdFromIndexUsingPair(docPair) == 20162)
//                    System.out.println("here");

                DocPassageIDPair prevDocPair = new DocPassageIDPair(docTextID, segmentID - 1);
                double prevDocScore = 0;
                if(initialScoresMap.containsKey(prevDocPair))
                    prevDocScore = initialScoresMap.get(prevDocPair);
                DocPassageIDPair nextDocPair = new DocPassageIDPair(docTextID, segmentID + 1);
                double nextDocScore = 0;
                if(initialScoresMap.containsKey(nextDocPair))
                    nextDocScore = initialScoresMap.get(nextDocPair);

                double finalScore = docWeight * docScore + prevDocWeight * prevDocScore + nextDocWeight * nextDocScore;
                finalScoresMap.put(docPair, finalScore);
            }

        }

        // sort passages based on score
        sortedScoresMap = MapFunctions.sortByValue(finalScoresMap);
    }

    private void queryAndConstructResultMapsMultipleItems(Query query){

        int window = 2;

        if (this.indexType.equals(IndexCONSTANTS.INDEX_TYPE_THREE_SENTNCES))
            window = 3;

        Map<DocPassageIDPair, Double> initialScoresMap = new HashMap<>();

        // execute query, collect initial scores (scores per doc that do not depend on neighbour doc scores)
        // and store them into maps
        TotalHitCountCollector collector = new TotalHitCountCollector();
        try {
            this.searcher.search(query, collector);
            int totalHits = collector.getTotalHits();
            if (totalHits > 0) {
                TopDocs hits = this.searcher.search(query, totalHits);

                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    int docID = scoreDoc.doc;
                    Document doc = this.searcher.doc(docID);
                    String docTextID = doc.get(IndexCONSTANTS.FIELD_TEXT_ID);
                    int segmentID = Integer.parseInt(doc.get(IndexCONSTANTS.FIELD_SEGMENT_ID));
                    DocPassageIDPair pair = new DocPassageIDPair(docTextID, segmentID);
                    double score = scoreDoc.score;
                    initialScoresMap.put(pair, score);
                }
            }
        }
        catch(IOException e){
            System.out.println("Exception while constructing ordered retrieval maps!!");
            return;
        }
        // calculate new scores based on neighbour segments
        Map<DocPassageIDPair, Double> finalScoresMap = new HashMap<>();
        for (Map.Entry<DocPassageIDPair,Double> entry: initialScoresMap.entrySet()){
            double docScore = entry.getValue();
            // for now exclude docs with score equal to 0
            if(docScore!=0){
                DocPassageIDPair docPair = entry.getKey();
                String docTextID = docPair.getDocID();
                int segmentID = docPair.getSegmentID();
                int paragraphID = Integer.parseInt(getDocFromIndexUsingPair(docPair).get(IndexCONSTANTS.FIELD_PARAGRAPH_ID));

                int wordCount = 1;
                DocPassageIDPair prevDocPair = new DocPassageIDPair(docTextID, segmentID - wordCount);
                double prevDocScore = 0;
                // get back 2 (or 3) ids to get previous context,
                // except from the case that a paragraph changes and there is no overlap between items with adjacent ids
                Document prevDoc;
                while((prevDoc = getDocFromIndexUsingPair(prevDocPair)) != null){
                    int prevParagraphID = Integer.parseInt(prevDoc.get(IndexCONSTANTS.FIELD_PARAGRAPH_ID));
                    // if doc exists in the previous paragraph OR we reached the window and we are still in the same paragraph
                    if( paragraphID == (prevParagraphID + 1) || (wordCount == window && paragraphID == prevParagraphID)){
                        if(initialScoresMap.containsKey(prevDocPair))
                            prevDocScore = initialScoresMap.get(prevDocPair);
                        break;
                    }
                    ++wordCount;
                    prevDocPair = new DocPassageIDPair(docTextID, segmentID - wordCount);
                }

                wordCount=1;
                DocPassageIDPair nextDocPair = new DocPassageIDPair(docTextID, segmentID + wordCount);
                double nextDocScore = 0;
                Document nextDoc;
                while((nextDoc = getDocFromIndexUsingPair(nextDocPair)) != null){
                    int nextParagraphID = Integer.parseInt(nextDoc.get(IndexCONSTANTS.FIELD_PARAGRAPH_ID));
                    // if doc exists in the previous paragraph OR we reached the window and we are still in the same paragraph
                    if(paragraphID == (nextParagraphID - 1) || (wordCount == window && paragraphID == nextParagraphID)){
                        if(initialScoresMap.containsKey(nextDocPair))
                            nextDocScore = initialScoresMap.get(nextDocPair);
                        break;
                    }
                    ++wordCount;
                    nextDocPair = new DocPassageIDPair(docTextID, segmentID + wordCount);
                }

                double finalScore = docWeight * docScore + prevDocWeight * prevDocScore + nextDocWeight * nextDocScore;
                finalScoresMap.put(docPair, finalScore);
            }

        }

        // sort passages based on score
        sortedScoresMap = MapFunctions.sortByValue(finalScoresMap);
    }

    /**
     * return top docs ( queryAndConstructResultMaps must be executed first )
     * @param numDocs
     * @return
     */
    public List<Map.Entry<DocPassageIDPair, Double>> getTopNDocs(int numDocs){

        if(sortedScoresMap == null){
            System.out.println("Result maps are not constructed using a query!!!");
            System.out.println("You must first call queryAndConstructResultMaps() method!!!");
            System.out.println("Null will be returned...");
            return null;
        }

        List<Map.Entry<DocPassageIDPair, Double>> topEntries = sortedScoresMap.entrySet().stream()
                .limit(numDocs)
                .collect(toList());
        return topEntries;
    }

    public void printContextRetrievalInfo(){
        System.out.println("CONTEXT RETRIEVAL INFO");
        System.out.println("----------------------");
        System.out.println("Root folder: " + this.rootDir);
        System.out.println("Index type: " + this.indexType);
    }

    /**
     * call executeDocPairQuery() using a <text_id,segment_id> pair and tell if results exist
     * @param docPair
     * @return
     */
    private boolean isPairExisting(DocPassageIDPair docPair){

        ScoreDoc[] hits = executeDocPairQuery(docPair);
        if(hits.length == 0)
            return false;
        else
            return true;
    }

    /**
     * call executeDocPairQuery() using a <text_id,segment_id> pair and return the first document
     * @param docPair
     * @return
     */
    public Document getDocFromIndexUsingPair(DocPassageIDPair docPair){

        ScoreDoc[] hits = executeDocPairQuery(docPair);
        if(hits.length == 0)
            return null;
        else if(hits.length == 1){
            int docId = hits[0].doc;
            Document d = null;
            try {
                d = searcher.doc(docId);
            } catch (IOException e) {
                System.out.println("WebKIndex :: ContextRetrievalHandler.getDocFromIndexUsingPair() Could NOT get doc with id: " + docId);
            }
            return d;
        }
        else{
            System.out.println("Problem!! More results returned!!");
            int docId = hits[0].doc;
            Document d = null;
            try {
                d = searcher.doc(docId);
            } catch (IOException e) {
                System.out.println("WebKIndex :: ContextRetrievalHandler.getDocFromIndexUsingPair() Could NOT get doc with id: " + docId);
            }
            return d;
        }

    }

    /**
     * call executeDocPairQuery() using a <text_id,segment_id> pair and return the id of the first result
     * @param docPair
     * @return
     */
    public int getDocIdFromIndexUsingPair(DocPassageIDPair docPair){
        ScoreDoc[] hits = executeDocPairQuery(docPair);
        if(hits==null || hits.length == 0)
            return -1;
        else if(hits.length == 1){
            int docId = hits[0].doc;
            return docId;
        }
        else{
            System.out.println("Problem!! More results returned!!");
            int docId = hits[0].doc;
            return docId;
        }
    }

    /**
     * execute a query based on text_id and segment_id and return the results in ScoreDoc[] format
     * @param docPair
     * @return
     */
    public ScoreDoc[] executeDocPairQuery(DocPassageIDPair docPair){
        String textID = docPair.getDocID();
        int segmentID = docPair.getSegmentID();

        Query textIDQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_TEXT_ID, textID));
        Query segmentIDQuery = new TermQuery(new Term(IndexCONSTANTS.FIELD_SEGMENT_ID, String.valueOf(segmentID)));

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        queryBuilder.add(textIDQuery, BooleanClause.Occur.MUST);
        queryBuilder.add(segmentIDQuery, BooleanClause.Occur.MUST);
        Query query = queryBuilder.build();

        TopDocs candidates = null;
        try {
            candidates = searcher.search(query, 2);

            ScoreDoc[] hits = candidates.scoreDocs;
            return hits;
        }
        catch (IOException e) {
            System.out.println("WebKIndex :: ContextRetrievalHandler.executeDocPairQuery() Could NOT Search for query: " + query.toString());
            return null;
        }
    }


}
