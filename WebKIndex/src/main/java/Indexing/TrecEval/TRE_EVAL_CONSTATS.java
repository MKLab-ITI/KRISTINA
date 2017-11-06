package Indexing.TrecEval;

import Indexing.IndexCONSTANTS;

/**
 * Created by Thodoris Tsompanidis on 12/7/2016.
 */
public class TRE_EVAL_CONSTATS {

	public static double LAMDA_START = 0.0;
	public static double LAMDA_END = 1.0;
	public static double LAMDA_STEP= 0.1;
	public static double MU_START = 500.0;
	public static double MU_END = 5000.0;
	public static double MU_STEP = 500.0;

	public static int RESULTS_TOP_N_PER_QUERY=1000;

	public static final String OUTPUT_VS_RESULT_FILE_PATH = "output\\trec_eval\\TREC_EVAL_INPUT_VS\\resultsVS";  //Vector Space Model
	public static final String OUTPUT_LMD_RESULT_FILE_PATH = "output\\trec_eval\\TREC_EVAL_INPUT_LMD\\resultsLMD";  //LM Dirichlet
	public static final String OUTPUT_LMJM_RESULT_FILE_PATH = "output\\trec_eval\\TREC_EVAL_INPUT_LMJM\\resultsLMJM";  //LM Jelinek Mercer

	public static String [] INDEX_SIMILARITY_MODELS={
			IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER,
			IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET,
			IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE
	};
}
