package gr.iti.mklab.kindex.Indexing.LuceneCustomClasses;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
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
				"último","últimos"
		);
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

