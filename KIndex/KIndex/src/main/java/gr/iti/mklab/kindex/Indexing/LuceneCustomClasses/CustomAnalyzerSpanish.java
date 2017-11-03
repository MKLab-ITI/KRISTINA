package gr.iti.mklab.kindex.Indexing.LuceneCustomClasses;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.es.SpanishLightStemmer;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;

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
public final class CustomAnalyzerSpanish extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/** An unmodifiable set containing some common English words that are usually not
	 useful for searching. */
	public static final CharArraySet STOP_WORDS_SET = CustomStopAnalyzerSpanish.SPANISH_STOP_WORDS_SET;

	/** Builds an analyzer with the given stop words.
	 * @param stopWords stop words */
	public CustomAnalyzerSpanish(CharArraySet stopWords) {
		super(stopWords);
	}

	/** Builds an analyzer with the default stop words ({@link
	 * #STOP_WORDS_SET}).
	 */
	public CustomAnalyzerSpanish() {
		this(STOP_WORDS_SET);
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwords Reader to read stop words from */
	public CustomAnalyzerSpanish(Reader stopwords) throws IOException {
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

	/**
	 * Creates a
	 * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 * which tokenizes all the text in the provided {@link Reader}.
	 *
	 * @return A
	 *         {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 *         built from an {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter}
	 *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is
	 *         provided and {@link SpanishLightStemFilter}.
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source;
		if (getVersion().onOrAfter(Version.LUCENE_4_7_0)) {
			source = new StandardTokenizer();
		} else {
			source = new StandardTokenizer40();
		}
		TokenStream result = new StandardFilter(source);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopwords);
		/*if(!stemExclusionSet.isEmpty())
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);*/
		result = new SpanishLightStemFilter(result);
		return new TokenStreamComponents(source, result);
	}
}

