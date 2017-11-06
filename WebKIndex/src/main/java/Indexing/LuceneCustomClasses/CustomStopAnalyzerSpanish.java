package Indexing.LuceneCustomClasses;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;

/**
 * Filters {@link LetterTokenizer} with {@link LowerCaseFilter} and {@link StopFilter}.
 */
public final class CustomStopAnalyzerSpanish extends StopwordAnalyzerBase {

	/** An unmodifiable set containing some common English words that are not usually useful
	 for searching.*/
	public static final CharArraySet SPANISH_STOP_WORDS_SET;

	static {
		final List<String> stopWords = new ArrayList<>(StopwordLists.stopWordsES);
		final CharArraySet stopSet = new CharArraySet(stopWords, false);
		SPANISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}

	/** Builds an analyzer which removes words in
	 *  {@link #SPANISH_STOP_WORDS_SET}.
	 */
	public CustomStopAnalyzerSpanish() {
		this(SPANISH_STOP_WORDS_SET);
	}

	/** Builds an analyzer with the stop words from the given set.
	 * @param stopWords Set of stop words */
	public CustomStopAnalyzerSpanish(CharArraySet stopWords) {
		super(stopWords);
	}

	/** Builds an analyzer with the stop words from the given path.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwordsFile File to load stop words from */
	public CustomStopAnalyzerSpanish(Path stopwordsFile) throws IOException {
		this(loadStopwordSet(stopwordsFile));
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwords Reader to load stop words from */
	public CustomStopAnalyzerSpanish(Reader stopwords) throws IOException {
		this(loadStopwordSet(stopwords));
	}

	/**
	 * Creates
	 * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 * used to tokenize all the text in the provided {@link Reader}.
	 *
	 * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 *         built from a {@link LowerCaseTokenizer} filtered with
	 *         {@link StopFilter}
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = new LowerCaseTokenizer();
		return new TokenStreamComponents(source, new StopFilter(source, stopwords));
	}
}

