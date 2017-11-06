package WordEmbeddings;

import Functions.MapFunctions;
import Indexing.LuceneCustomClasses.SnowBallSpanishAnalyzer;
import Indexing.LuceneCustomClasses.StopwordLists;
import Indexing.PassageIndexHandler;
import javafx.scene.paint.Stop;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.stopwords.StopWords;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by spyridons on 3/14/2017.
 */
public class Word2VecHandler {

    public static final int EMBEDDING_SIZE = 400;

    public Word2Vec vec;
    private List<String> vocabulary;
    private INDArray embeddingMatrix;

    public void createModel(List<String> sentences){
        System.out.println("Load sentences...");
        SentenceIterator iter = new CollectionSentenceIterator(sentences);
        iter.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.toLowerCase();
            }
        });
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        System.out.println("Building model....");
        vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(EMBEDDING_SIZE)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        System.out.println("Fitting Word2Vec model....");
        vec.fit();
    }

    public void saveModel(String fileName){
        WordVectorSerializer.writeWord2VecModel(vec, fileName);
    }

    public void loadModel(String fileName){
        vec = WordVectorSerializer.readWord2VecModel(fileName);
    }

    public void formVocabEmbeddingMatrix(){
        // save vocabulary to a list
        vocabulary = new ArrayList<>();
        Iterator<VocabWord> iter = vec.getVocab().vocabWords().iterator();
        while(iter.hasNext()){
            vocabulary.add(iter.next().getWord());
        }

        // save embeddings at the same order
        embeddingMatrix = Nd4j.zeros(vocabulary.size(),this.EMBEDDING_SIZE);
        for (int i = 0; i < vocabulary.size(); i++) {
            String word = vocabulary.get(i);
            INDArray wordVector = vec.getWordVectorMatrix(word);
            embeddingMatrix.putRow(i, wordVector);
        }
    }

    public List<String> getClosestWords(String query, int numWords){
        String[] queryWords = query.split(" ");
        if (vec == null){
            System.out.println("A word2vec model has NOT been trained or loaded!!!");
            System.out.println("Returning zero number of words");
            return new ArrayList<>();
        }
        Collection<String> closestWords = vec.wordsNearestSum(Arrays.asList(queryWords), new ArrayList<String>(), numWords);
        return new ArrayList<>(closestWords);
    }

    public List<String> getClosestWordsUsingTokenization(String query, int numWords){

        query = query.replaceAll("\\?"," ").replaceAll("¿"," " ).toLowerCase();

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        Tokenizer tokenizer = tokenizerFactory.create(query);

        //get the whole list of tokens
        List<String> tokens = tokenizer.getTokens();
        Collection<String> closestWords = vec.wordsNearestSum(tokens, new ArrayList<String>(), numWords);
        return new ArrayList<>(closestWords);
    }

    public List<String> getClosestWordsUsingEmbeddingMatrix(String query, int numWords){

        if(vocabulary == null){
            System.out.println("Embedding matrices are not created in the w2v handler!!!");
            System.out.println("Returning empty results...");
            return new ArrayList<>();
        }

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        Tokenizer tokenizer = tokenizerFactory.create(query);

        //get the whole list of tokens
        List<String> tokens = tokenizer.getTokens();

        // create query term frequency vector
        INDArray queryVector = Nd4j.zeros(vocabulary.size(),1);
        for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
            String token = tokens.get(tokenIndex);
            for (int vocabIndex = 0; vocabIndex < vocabulary.size(); vocabIndex++){
                String vocabWord = vocabulary.get(vocabIndex);
                if(token.equals(vocabWord)){
                    double newValue = queryVector.getDouble(vocabIndex) + 1;
                    queryVector.putScalar(vocabIndex, newValue);
                    break;
                }
            }
        }

        INDArray termWeights = null;
        boolean success = false;

        while(!success){
            try {
                termWeights = embeddingMatrix.mmul(embeddingMatrix.transpose()).mmul(queryVector);
                success = true;
            }
            catch(Throwable e){
                System.out.println("Out of memory error in query expansion. A line of code will be executed again.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    System.out.println("Exception in Thread.sleep()!!!");
                }
            }
        }

        // find words with the largest term weights
        Map<String, Double> termWeightsMap = new HashMap<>();
        for (int vocabIndex = 0; vocabIndex < vocabulary.size(); vocabIndex++){
            String vocabWord = vocabulary.get(vocabIndex);
            double weight = termWeights.getDouble(vocabIndex);
            termWeightsMap.put(vocabWord,weight);
        }

        Map<String, Double> sortedTermWeightsMap = MapFunctions.sortByValue(termWeightsMap);


        List<String> closestWords = sortedTermWeightsMap.keySet().stream()
                .limit(numWords)
                .collect(toList());

        return closestWords;

    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        PassageIndexHandler pih = new PassageIndexHandler("pl", 10);
        List<String> sentences = pih.getAllSentences();
        Word2VecHandler w2v = new Word2VecHandler();
        w2v.createModel(sentences);
        w2v.saveModel("w2v_model_pl");

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time: " + elapsed);
    }
}
