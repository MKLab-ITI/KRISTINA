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
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Analyzer} for Spanish.
 */
public final class LightStemSpanishAnalyzer extends StopwordAnalyzerBase {
	private final CharArraySet stemExclusionSet;

	public static final CharArraySet SPANISH_STOP_WORDS_SET;

	static {
		final List<String> stopWords = Arrays.asList(
				"a","actualmente","acuerdo","adelante","ademas",
				"además","adrede","afirmó","agregó","ahi",
				"ahora","ahí","al","algo","alguna",
				"algunas","alguno","algunos","algún","alli",
				"allí","alrededor","ambos","ampleamos","antano",
				"antaño","ante","anterior","antes","apenas",
				"aproximadamente","aquel","aquella","aquellas","aquello",
				"aquellos","aqui","aquél","aquélla","aquéllas",
				"aquéllos","aquí","arriba","arribaabajo","aseguró",
				"asi","así","atras","aun","aunque",
				"ayer","añadió","aún","b","bajo",
				"bastante","bien","breve","buen","buena",
				"buenas","bueno","buenos","c",
				"casi","cerca","cierta","ciertas","cierto",
				"ciertos","cinco","claro","comentó","como",
				"con","conmigo","conocer","conseguimos","conseguir",
				"considera","consideró","consigo","consigue","consiguen",
				"consigues","contigo","contra","cosas","creo",
				"cual","cuales","cualquier","cuando","cuanta",
				"cuantas","cuanto","cuantos","cuatro","cuenta",
				"cuál","cuáles","cuándo","cuánta","cuántas",
				"cuánto","cuántos","cómo","d","da",
				"dado","dan","dar","de","debajo",
				"debe", "debería", "deben","debo","debido","decir","dejó",
				"del","delante","demasiado","demás","dentro",
				"deprisa","desde","despacio","despues","después",
				"detras","detrás","dia","dias","dice",
				"dicen","dicho","dieron","diferente","diferentes",
				"dijeron","dijo","dio","donde","dos",
				"durante","día","días","dónde","e",
				"ejemplo","el","ella","ellas","ello",
				"ellos","embargo","empleais","emplean","emplear",
				"empleas","empleo","en","encima","encuentra",
				"enfrente","enseguida","entonces","entre","era",
				"eramos","eran","eras","eres","es",
				"esa","esas","ese","eso","esos",
				"esta","estaba","estaban","estado","estados",
				"estais","estamos","estan","estar","estará",
				"estas","este","esto","estos","estoy",
				"estuvo","está","están","ex","excepto",
				"existe","existen","explicó","expresó","f",
				"fin","final","fue","fuera","fueron",
				"fui","fuimos","g","general","gran",
				"grandes","gueno","h","ha","haber",
				"habia","habla","hablan","habrá","había",
				"habían","hace","haceis","hacemos","hacen",
				"hacer","hacerlo","haces","hacia","haciendo",
				"hago","han","hasta","hay","haya",
				"he","hecho","hemos","hicieron","hizo",
				"horas","hoy","hubo","i","igual",
				"incluso","indicó","informo","informó","intenta",
				"intentais","intentamos","intentan","intentar","intentas",
				"intento","ir","j","junto","k",
				"l","la","lado","largo","las",
				"le","lejos","les","llegó","lleva",
				"llevar","lo","los","luego","lugar",
				"m","mal","manera","manifestó","mas",
				"mayor","me","mediante","medio","mejor",
				"mencionó","menos","menudo","mi","mia",
				"mias","mientras","mio","mios","mis",
				"misma","mismas","mismo","mismos","modo",
				"momento","mucha","muchas","mucho","muchos",
				"muy","más","mí","mía","mías",
				"mío","míos","n","nada","nadie","necesito",
				"ni","ninguna","ningunas","ninguno","ningunos",
				"ningún","no","nos","nosotras","nosotros",
				"nuestra","nuestras","nuestro","nuestros","nueva",
				"nuevas","nuevo","nuevos","nunca","o",
				"ocho","os","otra","otras","otro",
				"otros","p","pais","para","parece",
				"parte","partir","pasada","pasado","paìs", "pág",
				"peor","pero","pesar","poca","pocas",
				"poco","pocos","podeis","podemos","poder",
				"podria","podriais","podriamos","podrian","podrias",
				"podrá","podrán","podría","podrías","podrían","poner","por favor",
				"por", "porque","posible","primer","primera",
				"primero","primeros","principalmente","pronto","propia",
				"propias","propio","propios","proximo","próximo",
				"próximos","pudo","pueda","puede","pueden",
				"puedo","pues","q","qeu","que",
				"quedó","queremos","quien","quienes","quiere",
				"quiza","quizas","quizá","quizás","quién",
				"quiénes","qué","r","raras","realizado",
				"realizar","realizó","repente","respecto","s",
				"sabe","sabeis","sabemos","saben","saber",
				"sabes","salvo","se","sea","sean",
				"segun","segunda","segundo","según","seis",
				"ser","sera","será","serán","sería",
				"señaló","si","sido","siempre","siendo",
				"siete","sigue","siguiente","sin","sino",
				"sobre","sois","sola","solamente","solas",
				"solo","solos","somos","son","soy",
				"soyos","su","supuesto","sus","suya",
				"suyas","suyo","sé","sí","sólo",
				"t","tal","tambien","también","tampoco",
				"tan","tanto","tarde", "tendré", "te","temprano",
				"tendrá","tendrán","teneis","tenemos","tener",
				"tenga","tengo","tenido","tenía","tercera",
				"ti","tiempo","tiene","tienen","toda",
				"todas","todavia","todavía","todo","todos",
				"total","trabaja","trabajais","trabajamos","trabajan",
				"trabajar","trabajas","trabajo","tras","trata",
				"través","tres","tu","tus","tuvo",
				"tuya","tuyas","tuyo","tuyos","tú",
				"u","ultimo","un","una","unas",
				"uno","unos","usa","usais","usamos",
				"usan","usar","usas","uso","usted",
				"ustedes","v","va","vais","valor",
				"vamos","van","varias","varios","vaya",
				"veces","ver","verdad","verdadera","verdadero",
				"vez","vosotras","vosotros","voy","vuestra",
				"vuestras","vuestro","vuestros","w","x",
				"y","ya","yo","z","él",
				"ésa","ésas","ése","ésos","ésta",
				"éstas","éste","éstos","última","últimas",
				"último","últimos",
                "preguntando",
                "sabré",
                "información",
                "gustaría",
                "favor",
                "puedes",
                "explicarme",
                "facilitarme",
                "informarme", "aconsejable", "específico", "oído"
		);
		final CharArraySet stopSet = new CharArraySet(stopWords, false);
		SPANISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}


	/**
	 * Builds an analyzer with the default stop words: {@link #DEFAULT_STOPWORD_FILE}.
	 */
	public LightStemSpanishAnalyzer() {
		this(SPANISH_STOP_WORDS_SET);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 *
	 * @param stopwords a stopword set
	 */
	public LightStemSpanishAnalyzer(CharArraySet stopwords) {
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
	public LightStemSpanishAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
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
		if(!stemExclusionSet.isEmpty())
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		result = new SpanishLightStemFilter(result);
		return new TokenStreamComponents(source, result);
	}
}
