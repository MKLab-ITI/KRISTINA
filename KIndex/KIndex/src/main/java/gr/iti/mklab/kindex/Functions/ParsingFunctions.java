package gr.iti.mklab.kindex.Functions;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by spyridons on 3/20/2017.
 */
public class ParsingFunctions {

    /**
     * get query words after analyzing them
     * @param analyzer
     * @param query
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getTokens(Analyzer analyzer, String query){
        ArrayList<String> tokens = new ArrayList<>();
        TokenStream tokenStream = null;
        try {
            tokenStream = analyzer.tokenStream("",query);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                tokens.add(term);
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    /**
     * get the text words after tokenizing and removing stopwords
     * @param analyzer
     * @param text
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getTextTokens(StopwordAnalyzerBase analyzer, String text){
        ArrayList<String> tokens = new ArrayList<>();
        TokenStream tokenStream = null;
        try {
            tokenStream = new StandardFilter(new StandardTokenizer());
            tokenStream = new LowerCaseFilter(tokenStream);
            tokenStream = new StopFilter(tokenStream, analyzer.getStopwordSet());
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                tokens.add(term);
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }
}
