package Indexing.LuceneCustomClasses;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;

/**
 * Filters {@link LetterTokenizer} with {@link LowerCaseFilter} and {@link StopFilter}.
 */
public final class CustomStopAnalyzer extends StopwordAnalyzerBase {

	/** An unmodifiable set containing some common English words that are not usually useful
	 for searching.*/
	public static final CharArraySet ENGLISH_STOP_WORDS_SET;

	static {
		final List<String> stopWords = Arrays.asList(
				"a", "about", "above", "according", "across", "after",
				"afterwards", "again", "against", "albeit", "all",
				"almost", "alone", "along", "already", "also",
				"although", "always", "am", "among", "amongst",
				"an", "and", "another", "any", "anybody",
				"anyhow", "anyone", "anything", "anyway", "anywhere",
				"apart", "are", "around", "as", "at",
				"av", "be", "became", "because", "become",
				"becomes", "becoming", "been", "before", "beforehand",
				"behind", "being", "below", "beside", "besides",
				"between", "beyond", "both", "but", "by",
				"can", "cannot", "canst", "certain", "cf",
				"choose", "contrariwise", "cos", "could", "cu",
				"day", "do", "does", "doesn't", "doing",
				"dost", "doth", "double", "down", "dual",
				"during", "each", "either", "else", "elsewhere",
				"enough", "et", "etc", "even", "ever",
				"every", "everybody", "everyone", "everything", "everywhere",
				"except", "excepted", "excepting", "exception", "exclude",
				"excluding", "exclusive", "far", "farther", "farthest",
				"few", "ff", "first", "for", "formerly",
				"forth", "forward", "from", "front", "further",
				"furthermore", "furthest", "get", "go", "had",
				"halves", "hardly", "has", "hast", "hath",
				"have", "he", "hence", "henceforth", "her",
				"here", "hereabouts", "hereafter", "hereby", "herein",
				"hereto", "hereupon", "hers", "herself", "him",
				"himself", "hindmost", "his", "hither", "hitherto",
				"how", "however", "howsoever", "i", "ie",
				"if", "in", "inasmuch", "inc", "include",
				"included", "including", "indeed", "indoors", "inside",
				"insomuch", "instead", "into", "inward", "inwards",
				"is", "it", "its", "itself", "just",
				"kind", "kg", "km", "last", "latter",
				"latterly", "less", "lest", "let", "like",
				"little", "ltd", "many", "may", "maybe",
				"me", "meantime", "meanwhile", "might", "moreover",
				"most", "mostly", "more", "mr", "mrs",
				"ms", "much", "must", "my", "myself",
				"namely", "need", "neither", "never", "nevertheless",
				"next", "no", "nobody", "none", "nonetheless",
				"noone", "nope", "nor", "not", "nothing",
				"notwithstanding", "now", "nowadays", "nowhere", "of",
				"off", "often", "ok", "on", "once",
				"one", "only", "onto", "or", "other",
				"others", "otherwise", "ought", "our", "ours",
				"ourselves", "out", "outside", "over", "own",
				"per", "perhaps", "plenty", "provide", "quite",
				"rather", "really", "round", "said", "sake",
				"same", "sang", "save", "saw", "see",
				"seeing", "seem", "seemed", "seeming", "seems",
				"seen", "seldom", "selves", "sent", "several",
				"shalt", "she", "should", "shown", "sideways",
				"since", "slept", "slew", "slung", "slunk",
				"smote", "so", "some", "somebody", "somehow",
				"someone", "something", "sometime", "sometimes", "somewhat",
				"somewhere", "spake", "spat", "spoke", "spoken",
				"sprang", "sprung", "stave", "staves", "still",
				"such", "supposing", "than", "that", "the",
				"thee", "their", "them", "themselves", "then",
				"thence", "thenceforth", "there", "thereabout", "thereabouts",
				"thereafter", "thereby", "therefore", "therein", "thereof",
				"thereon", "thereto", "thereupon", "these", "they",
				"this", "those", "thou", "though", "thrice",
				"through", "throughout", "thru", "thus", "thy",
				"thyself", "till", "to", "together", "too",
				"toward", "towards", "ugh", "unable", "under",
				"underneath", "unless", "unlike", "until", "up",
				"upon", "upward", "upwards", "us", "use",
				"used", "using", "very", "via", "vs",
				"want", "was", "we", "week", "well",
				"were", "what", "whatever", "whatsoever", "when",
				"whence", "whenever", "whensoever", "where", "whereabouts",
				"whereafter", "whereas", "whereat", "whereby", "wherefore",
				"wherefrom", "wherein", "whereinto", "whereof", "whereon",
				"wheresoever", "whereto", "whereunto", "whereupon", "wherever",
				"wherewith", "whether", "whew", "which", "whichever",
				"whichsoever", "while", "whilst", "whither", "who",
				"whoa", "whoever", "whole", "whom", "whomever",
				"whomsoever", "whose", "whosoever", "why", "will",
				"wilt", "with", "within", "without", "worse",
				"worst", "would", "wow", "ye", "yet",
				"year", "yippee", "you", "your", "yours",
				"yourself", "yourselves"
		);
		final CharArraySet stopSet = new CharArraySet(stopWords, false);
		ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}

	/** Builds an analyzer which removes words in
	 *  {@link #ENGLISH_STOP_WORDS_SET}.
	 */
	public CustomStopAnalyzer() {
		this(ENGLISH_STOP_WORDS_SET);
	}

	/** Builds an analyzer with the stop words from the given set.
	 * @param stopWords Set of stop words */
	public CustomStopAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	/** Builds an analyzer with the stop words from the given path.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwordsFile File to load stop words from */
	public CustomStopAnalyzer(Path stopwordsFile) throws IOException {
		this(loadStopwordSet(stopwordsFile));
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	 * @param stopwords Reader to load stop words from */
	public CustomStopAnalyzer(Reader stopwords) throws IOException {
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
		final Tokenizer source;
		if (getVersion().onOrAfter(Version.LUCENE_4_7_0)) {
			source = new StandardTokenizer();
		} else {
			source = new StandardTokenizer40();
		}
		TokenStream result = new StandardFilter(source);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopwords);
		return new TokenStreamComponents(source, result);
	}
}

