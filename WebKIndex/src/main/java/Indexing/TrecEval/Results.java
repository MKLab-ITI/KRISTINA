package Indexing.TrecEval;

import Indexing.IndexCONSTANTS;
import Indexing.LuceneCustomClasses.CustomAnalyzer;
import Indexing.LuceneCustomClasses.OriginalFormulaLMDirichletSimilarity;
import Indexing.LuceneCustomClasses.OriginalFormulaLMJelenekMercerSimilarity;
import Indexing.WebAP.WebAP_CONSTANTS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Created by Thodoris Tsompanidis on 11/7/2016.
 */
public class Results {

	/**
	 * Creates the result files for WebAP dataset, for models. <br>
	 *     <ul>
	 *         <li>Vector Space Model</li>
	 *         <li>LM Dirichlet</li>
	 *         <li>LM Jelinek Mercer</li>
	 *     </ul>
	 */
	public static void create_result_file_WebAP(){

		String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Creating the results file started at " + startTime);

		//Create writers for output files
		//PrintWriter writerVS = null;
		//PrintWriter writerLMD = null;
		//PrintWriter writerLMJM = null;
		//try {
		//	writerVS = new PrintWriter(TRE_EVAL_CONSTATS.OUTPUT_VS_RESULT_FILE_PATH, "UTF-8");
		//	writerLMD = new PrintWriter(TRE_EVAL_CONSTATS.OUTPUT_LMD_RESULT_FILE_PATH, "UTF-8");
		//	writerLMJM = new PrintWriter(TRE_EVAL_CONSTATS.OUTPUT_LMJM_RESULT_FILE_PATH, "UTF-8");
		//} catch (FileNotFoundException e) {
		//	System.err.print("WebKIndex :: Results.create_result_file_WebAP() Could not find the output file");
		//	e.printStackTrace();
		//} catch (UnsupportedEncodingException e) {
		//	System.err.print("WebKIndex :: Results.create_result_file_WebAP() Could not handle the encoding for output file");
		//	e.printStackTrace();
		//}

		//open OneSentence Index
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader.open( FSDirectory.open(Paths.get(WebAP_CONSTANTS.INDEX_ONE_SENTENCE_PATH)));
		} catch (IOException e) {
			System.err.print("WebKIndex :: Results.create_result_file_WebAP() Could not open one sentence index directory ");
			e.printStackTrace();
		}

		try {
			File file = new File(WebAP_CONSTANTS.INPUT_QUERIES_FILE_PATH);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String json = new String(data, "UTF-8");
			//String json = "{\"queries\":[{\"number\":\"701\",\"text\":\" describe history oil industry\"}]}";


			JSONObject jsonObject = new JSONObject(json);
			JSONArray queries = (JSONArray) jsonObject.getJSONArray("queries");
			Iterator<Object> iterator =  queries.iterator();
			while (iterator.hasNext()) {
				JSONObject q = (JSONObject)iterator.next();
				String queryID = (String) q.get("number");
				String queryText = (String) q.get("text");

				System.out.println("Querying for query: " + queryID + " - " + queryText);

				for (String model : TRE_EVAL_CONSTATS.INDEX_SIMILARITY_MODELS) {

					double start = 0, end = 0, step = 0;
					switch (model){
						case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
							//execute only one time
							start = 0.1;
							end = 0.1;
							step = 0.1;
							break;
						case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
							start = TRE_EVAL_CONSTATS.MU_START;
							end = TRE_EVAL_CONSTATS.MU_END;
							step = TRE_EVAL_CONSTATS.MU_STEP;
							break;
						case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
							start = TRE_EVAL_CONSTATS.LAMDA_START;
							end = TRE_EVAL_CONSTATS.LAMDA_END;
							step = TRE_EVAL_CONSTATS.LAMDA_STEP;
							break;
					}

					while (start <= end) {
						Similarity similarity = null;
						PrintWriter writer = null;
						switch (model) {
							case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
								similarity = new DefaultSimilarity();
								writer = new PrintWriter(new FileOutputStream(new File(TRE_EVAL_CONSTATS.OUTPUT_VS_RESULT_FILE_PATH+".out"),true));
								break;
							case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
								//similarity =  new LMDirichletSimilarity((float)start);
								similarity = new OriginalFormulaLMDirichletSimilarity((float)start);
								writer = new PrintWriter(new FileOutputStream(new File(TRE_EVAL_CONSTATS.OUTPUT_LMD_RESULT_FILE_PATH+"_"+new Double(start).intValue()+".out"),true));
								break;
							case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
								//similarity =  new LMJelinekMercerSimilarity((float)start);
								similarity = new OriginalFormulaLMJelenekMercerSimilarity((float)start);
								writer = new PrintWriter(new FileOutputStream(new File(TRE_EVAL_CONSTATS.OUTPUT_LMJM_RESULT_FILE_PATH+"."+ start+".out"),true));
								break;
						}

						IndexSearcher searcher = new IndexSearcher(reader);
						Analyzer analyzer = new CustomAnalyzer();
						searcher.setSimilarity(similarity);

						TopDocs candidates = null;
						QueryParser parser1 = new QueryParser(IndexCONSTANTS.FIELD_CONTENT, analyzer);
						Query query = null;
						try {
							query = parser1.parse(queryText);
						} catch (ParseException e) {
							System.out.println("WebKIndex :: Results.create_result_file_WebAP()  Cannot create query");
							e.printStackTrace();
						}

						try {
							candidates = searcher.search(query, TRE_EVAL_CONSTATS.RESULTS_TOP_N_PER_QUERY);

							ScoreDoc[] hits = candidates.scoreDocs;
							//PrintWriter writerq  = new PrintWriter(new FileOutputStream(new File("output\\trec_eval\\score.out"),true));
							//writerq.println("QueryObject:" + queryText);
							//writerq.println("Lamda:" + start);
							//writerq.println("------------------" );
							for (int i = 0; i < hits.length; ++i) {
								int docId = hits[i].doc;
								Document d = null;
								try {
									d = searcher.doc(docId);
									//writerq.println("Document: \""+d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_CONTENT).stringValue()+"\"");
									//writerq.println(" ");
									//writerq.println(searcher.explain(query, docId).toString());
									//writerq.println("-------------------------------------------------------");
								} catch (IOException e) {
									e.printStackTrace();
								}

								//String qID = d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_TARGET_QID).stringValue();
								String docID = d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_DOC_NO).stringValue() + ":" + d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_SENTENCE_ID).stringValue();
								//rank is i
								float score = hits[i].score;

								//Print the most relevant document
							/*if (i == 0){
								System.out.println(model + ": " + score);
								System.out.println(d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_CONTENT).stringValue());
							}*/

								//query-number Q0 document-id rank score Exp
								String line = queryID + "    0    " + docID + "    " + i + "    " + score + "     STANDARD";
								//String line = queryID + "    0    " + docID + "    " + i + "    " + score + "     STANDARD     (" + d.getField(WebAP_CONSTANTS.DOCUMENT_FIELD_CONTENT).stringValue() + ")";
								writer.println(line);

								//switch (model) {
								//	case IndexCONSTANTS.INDEX_MODEL_VECTOR_SPACE:
								//		writer.println(line);
								//		writerVS.close();
								//		break;
								//	case IndexCONSTANTS.INDEX_MODEL_LM_DIRICHLET:
								//		writerLMD.println(line);
								//		writerLMD.close();
								//		break;
								//	case IndexCONSTANTS.INDEX_MODEL_LM_JELINEK_MERCER:
								//		writerLMJM.println(line);
								//		writerLMJM.close();
								//		break;
								//}

							}
							//writerq.close();

						} catch (IOException e) {
							System.out.println("WebKIndex :: Results.create_result_file_WebAP() Could NOT Search for query: " + queryID);
							e.printStackTrace();
						}
						start = round(start + step,1);
						writer.close();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("WebKIndex :: Results.create_result_file_WebAP()  Could NOT Parse the query json ");
			e.printStackTrace();
		}
		//
		//writerVS.close();
		//writerLMD.close();
		//writerLMJM.close();

		String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Creating the result file ended at " + endTime);

	}


	//calculate the number of queries in the output of trec_eval
	public static void countQueries() {
		File fin = new File("output\\trec_eval\\VS.metrics");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fin);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		HashSet<String> queries = new HashSet<String>();

		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				queries.add(line.split("\t")[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Queries: "+queries.size());
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}
