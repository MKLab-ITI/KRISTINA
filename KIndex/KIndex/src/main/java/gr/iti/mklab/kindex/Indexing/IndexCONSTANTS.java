package gr.iti.mklab.kindex.Indexing;

public final class IndexCONSTANTS {

	//index documets' fields names
	public static final String FIELD_URL="url";
	public static final String FIELD_MEDIUM="medium";
	public static final String FIELD_CONTENT="content";
	public static final String FIELD_CONTENT_RESPONSE="content_response";
	public static final String FIELD_TITLE="title";
	public static final String FIELD_TYPE="type";
	public static final String FIELD_WEBPAGE_ID="webpage_id";
	public static final String FIELD_TWEET_ID="tweet_id";
	public static final String FIELD_POST_ID="post_id";
	public static final String FIELD_PDF_ID="pdf_id";
	public static final String FIELD_TEXT_ID="text_id";
	public static final String FIELD_CONCEPTS="concept";
	public static final String FIELD_NAMED_ENTITIES="named_entity";
	public static final String FIELD_SEGMENT_ID="segment_id";
	public static final String FIELD_PARAGRAPH_ID="paragraph_id";

	public final static String INDEX_PATH = ("D:\\INDEX");

	public final static String PASSAGE_INDEX_PATH = ("Indexes/");
	public final static String PASSAGE_INDEX_VERSION = ("v.10/");
	public final static String PASSAGE_INDEX_lANGUAGE = ("pl");
	public final static String INDEX_PATH_1_SENTENCE = ("/One_Sentence");
	public final static String INDEX_PATH_2_SENTENCES = ("/Two_Sentences");
	public final static String INDEX_PATH_3_SENTENCES = ("/Three_Sentences");
	public final static String INDEX_PATH_PARAGRAPH = ("/Paragraph");
	public final static String INDEX_PATH_ALL_SEGMENTATION = ("/All_Segmentation");
	public final static String INDEX_PATH_DOCUMENT = ("/Document");

	public final static String MEDIUM_TWEET = ("TWEET");
	public final static String MEDIUM_WEBSITE= ("WEBSITE");
	public final static String MEDIUM_POST = ("POST");
	public final static String MEDIUM_PDF = ("PDF");

	public final static String TYPE_TEXT= ("TEXT");

	public final static String INDEX_MODEL_VECTOR_SPACE = ("VectorSpaceModel");
	public final static String INDEX_MODEL_LM_DIRICHLET= ("LM_Dirichlet");
	public final static String INDEX_MODEL_LM_JELINEK_MERCER= ("JelinekMercer");

	public final static String[] URL_PATTERNS_TO_REMOVE = {
			"https://support.nlm.nih.gov/ics/support/ticketnewwizard.asp?style=classic&lang=es&deptID=28054&category=medlineplus_spanish&from",
			"http://enfamilia.aeped.es/autores/",
			"https://apps.nlm.nih.gov/medlineplus/contact/index.cfm?lang=es&from=",
			"http://enfamilia.aeped.es/indice/",
			"http://enfamilia.aeped.es/glosario/",
			"http://enfamilia.aeped.es/poll",
			"http://enfamilia.aeped.es/user",
			"https://medlineplus.gov/spanish/druginfo/",
			"https://medlineplus.gov/spanish/ency/patientinstructions/"};

	public final static String[] URLS_TO_REMOVE = {
			"http://enfamilia.aeped.es"};

	public final static String RECIPE_INDEX_PATH = ("INDEX/Recipes/");
	public final static String NEWSPAPER_INDEX_PATH = ("INDEX/Newspapers/");
	public final static String FIELD_RECIPE_TITLE= ("title");
	public final static String FIELD_NEWSPAPER_TITLE= ("title");
	public final static String FIELD_NEWSPAPER_SUBTITLE= ("subtitle");

}
