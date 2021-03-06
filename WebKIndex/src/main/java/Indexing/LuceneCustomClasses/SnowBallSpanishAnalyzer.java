package Indexing.LuceneCustomClasses;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Analyzer} for Spanish.
 */
public final class SnowBallSpanishAnalyzer extends StopwordAnalyzerBase {
	private final CharArraySet stemExclusionSet;

	/** File containing default Spanish stopwords. */
	public final static String DEFAULT_STOPWORD_FILE = "spanish_stop.txt";

	public static final CharArraySet SPANISH_STOP_WORDS_SET;

	static {
		final List<String> stopWords = new ArrayList<>(StopwordLists.stopWordsES);
		final CharArraySet stopSet = new CharArraySet(stopWords, false);
		SPANISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}

	/**
	 * Builds an analyzer with the default stop words: {@link #DEFAULT_STOPWORD_FILE}.
	 */
	public SnowBallSpanishAnalyzer() {
		this(SPANISH_STOP_WORDS_SET);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 *
	 * @param stopwords a stopword set
	 */
	public SnowBallSpanishAnalyzer(CharArraySet stopwords) {
		this(stopwords, CharArraySet.EMPTY_SET);
	}

	/**
	 * Builds an analyzer with the given stop words. If a non-empty stem exclusion set is
	 * provided this analyzer will add a {@link SetKeywordMarkerFilter} before
	 * stemming.
	 *
	 * @param stopwords a stopword set
	 * @param stemExclusionSet a set of terms not to be stemmed
	 */
	public SnowBallSpanishAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
		super(stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
	}

	/**
	 * Creates a
	 * {@link TokenStreamComponents}
	 * which tokenizes all the text in the provided {@link Reader}.
	 *
	 * @return A
	 *         {@link TokenStreamComponents}
	 *         built from an {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter}
	 *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is
	 *         provided and {@link SnowballFilter}.
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
		if(!stemExclusionSet.isEmpty())
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		result = new SnowballFilter(result, new SpanishStemmer());
		return new TokenStreamComponents(source, result);
	}
}

