package Indexing.TrecEval;

/**
 * Created by Thodoris Tsompanidis on 11/7/2016.
 */
public class Trec_Eval_Main {

	public static void main(String[] args) {

		QRel.create_qrel_file_WebAP();
		Results.create_result_file_WebAP();

//		FPResults.create_result_file_WebAP();

	}

}
