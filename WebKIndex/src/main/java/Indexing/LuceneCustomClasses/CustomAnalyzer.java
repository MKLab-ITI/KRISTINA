package Indexing.LuceneCustomClasses;

/**
 * Created by Thodoris Tsompanidis on 6/7/2016.
 */


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;

import java.io.IOException;
import java.io.Reader;

/**
 * Custom analyzer was created for KIndex Indexing process.
 * It is a ClassicAnalyzer using different list for stopwords
 *
 * Filters {@link ClassicTokenizer} with {@link ClassicFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of
 * English stop words.
 *
 * ClassicAnalyzer was named StandardAnalyzer in Lucene versions prior to 3.1.
 * As of 3.1, {@link StandardAnalyzer} implements Unicode text segmentation,
 * as specified by UAX#29.
 */
public class CustomAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/** An unmodifiable set containing some common English words that are usually not
	 useful for searching. */
	public static final CharArraySet STOP_WORDS_SET = CustomStopAnalyzer.ENGLISH_STOP_WORDS_SET;

	/** Builds an analyzer with the given stop words.
	 * @param stopWords stop words */
	public CustomAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	/** Builds an analyzer with the default stop words ({@link
	 * #STOP_WORDS_SET}).
	 */
	public CustomAnalyzer() {
		this(STOP_WORDS_SET);
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwords Reader to read stop words from */
	public CustomAnalyzer(Reader stopwords) throws IOException {
		this(loadStopwordSet(stopwords));
	}

	/**
	 * Set maximum allowed token length.  If a token is seen
	 * that exceeds this length then it is discarded.  This
	 * setting only takes effect the next time tokenStream or
	 * tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {
		final ClassicTokenizer src = new ClassicTokenizer();
		src.setMaxTokenLength(maxTokenLength);

		//To stemming or not to stemming-that is the question!
		//switch comments in two lines below
		//TokenStream tok = new ClassicFilter(src);
		TokenStream tok = new PorterStemFilter(src);

		tok = new LowerCaseFilter(tok);
		tok = new StopFilter(tok, stopwords);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				src.setMaxTokenLength(maxTokenLength);
				super.setReader(reader);
			}
		};
	}


	//Add this function for use the Porter Stemmer
	/*@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new LowerCaseTokenizer(version, reader);
		return new TokenStreamComponents(source, new PorterStemFilter(source));
	}*/
}
