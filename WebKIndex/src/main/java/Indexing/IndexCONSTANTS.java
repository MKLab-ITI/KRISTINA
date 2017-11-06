package Indexing;

import java.util.HashMap;
import java.util.Map;


public final class IndexCONSTANTS {

	//index documets' fields names
	public static final String FIELD_URL="url";
	public static final String FIELD_MEDIUM="medium";
	public static final String FIELD_CONTENT="content";
	public static final String FIELD_CONTENT_RESPONSE="content_response";
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

	public final static String INDEX_PATH = ("INDEX");
	public final static String PASSAGE_INDEX_PATH = ("Indices/");
	public final static String INDEX_PATH_1_SENTENCE = ("/One_Sentence");
	public final static String INDEX_PATH_2_SENTENCES = ("/Two_Sentences");
	public final static String INDEX_PATH_3_SENTENCES = ("/Three_Sentences");
	public final static String INDEX_PATH_PARAGRAPH = ("/Paragraph");
	public final static String INDEX_PATH_ALL_SEGMENTATION = ("/All_Segmentation");
	public final static String INDEX_PATH_DOCUMENT = ("/Document");

	public final static String INDEX_TYPE_ONE_SENTNCE = ("OneSentence");
	public final static String INDEX_TYPE_TWO_SENTNCES = ("TwoSentences");
	public final static String INDEX_TYPE_THREE_SENTNCES = ("ThreeSentences");
	public final static String INDEX_TYPE_PARAGRAPH = ("Paragraph");
	public final static String INDEX_TYPE_ALL_SEGMENTATION = ("AllSegmentation");
	public final static String INDEX_TYPE_DOCUMENT = ("Document");

	public final static String INDEX_MODEL_VECTOR_SPACE = ("VectorSpaceModel");
	public final static String INDEX_MODEL_LM_DIRICHLET= ("LM_Dirichlet");
	public final static String INDEX_MODEL_LM_JELINEK_MERCER= ("JelinekMercer");

	public final static String MEDIUM_TWEET = ("TWEET");
	public final static String MEDIUM_WEBSITE= ("WEBSITE");
	public final static String MEDIUM_POST = ("POST");
	public final static String MEDIUM_PDF = ("PDF");
	public final static String MEDIUM_NEWSPAPER= ("NEWSPAPER");

	public final static String TYPE_TEXT= ("TEXT");

	//newspaper
	public final static String FIELD_TITLE= ("title");
	public final static String FIELD_SUBTITLE= ("subtitle");
	public final static String FIELD_DESCRIPTION= ("description");
	public final static String FIELD_DATE_STRING= ("date");

	//Passage Retrieval
	public static final int DEFAULT_PRIMARY_WINDOW_SIZE = 25;
	public static final int DEFAULT_ADJACENT_WINDOW_SIZE = 25;
	public static final int DEFAULT_SECONDARY_WINDOW_SIZE = 25;

	public static final float DEFAULT_ADJACENT_WEIGHT = 0.5f;
	public static final float DEFAULT_SECOND_ADJACENT_WEIGHT = 0.25f;
	public static final float DEFAULT_BIGRAM_WEIGHT = 1.0f;

	//url list
	public static final String URLS_LIST_FILE = "input/urls/urls.txt";

	// choi retrieval method constants
	public static final float SINGLE_TERM_WEIGHT = 1f;
	public static final float ORDERED_PAIR_WEIGHT = 1f;
	public static final float UNORDERED_PAIR_WEIGHT = 1f;

	public static final int ORDERED_PAIR_WINDOW = 1;
	public static final int UNORDERED_PAIR_WINDOW = 8;

	public static final double MINIMUM_SIMILARITY = 0.5;

	public static final String NEWSPAPER_LANGUAGE = "de";

	public final static String RECIPE_INDEX_PATH = ("INDEX/Recipes/");
	public final static String NEWSPAPER_INDEX_PATH = ("INDEX/Newspapers/");

}
