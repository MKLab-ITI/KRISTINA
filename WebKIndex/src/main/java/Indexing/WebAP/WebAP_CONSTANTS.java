package Indexing.WebAP;

/**
 * Created by Thodoris Tsompanidis on 12/7/2016.
 */
public class WebAP_CONSTANTS {


	public static int GET_RESULTS_TOP_N = 10;

	public static final String INPUT_DOCUMENTS_FILE_PATH = "input\\WebAP\\gradedText\\grade.trectext_patched";
	public static final String INPUT_QUERIES_FILE_PATH = "input\\WebAP\\gradedText\\gov2.query.json";

	public static final String OUTPUT_DISTRIBUTION_FILE_PATH = "output\\WebAP\\distribution.csv";
	public static final String OUTPUT_QUERIES_RELEVANCE_FILE_PATH = "output\\WebAP\\queriesRel.txt";
	public static final String OUTPUT_SENTENCE_RELEVANCE_NUMBER_FILE_PATH = "output\\WebAP\\SesntenceRelevanceNumber.txt";
	public static final String OUTPUT_PASSAGE_RELEVANCE_NUMBER_FILE_PATH = "output\\WebAP\\PassageRelevanceNumber.txt";
	public static final String OUTPUT_SENTENCES_TEXT_FILE_PATH = "output\\WebAP\\SentencesText.txt";

	public static final String INDEX_ONE_SENTENCE_PATH = "Indexes\\WebApIndex\\One_Sentence";
	public static final String INDEX_TWO_SENTENCES_PATH = "Indexes\\WebApIndex\\Two_Sentences";
	public static final String INDEX_THREE_SENTENCES_PATH = "Indexes\\WebApIndex\\Three_Sentences";
	public static final String INDEX_ALL_SEGMENTATIONS_PATH = "Indexes\\WebApIndex\\All_segmentation";

	public static final String INDEX_DOCUMENT_PATH = "Indexes\\WebApIndex\\DOCUMENT";
	public static final String INDEX_ONE_SENTENCE_TYPE = "OneSentence";
	public static final String INDEX_TWO_SENTENCES_TYPE = "TwoSentences";
	public static final String INDEX_THREE_SENTENCES_TYPE = "ThreeSentences";
	public static final String INDEX_ALL_SEGMENTATIONS_TYPE = "AllSegmentation";

	public static final String INDEX_DOCUMENT_TYPE = "Document";
	public static final String DOCUMENT_FIELD_DOC_NO = "DocNo";
	public static final String DOCUMENT_FIELD_TARGET_QID = "targetQID";
	public static final String DOCUMENT_FIELD_ORIGINAL_DOC_NO = "originalDocNop";
	public static final String DOCUMENT_FIELD_CONTENT = "content";
	public static final String DOCUMENT_FIELD_RELEVANCE_WITH_QUERY = "relevance";
	public static final String DOCUMENT_FIELD_SENTENCE_ID = "SenID";


	public static final String DOCUMENT_FIELD_INDEX_TYPE = "IndexType";
}
