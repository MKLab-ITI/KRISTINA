package Indexing.TrecEval;

import Indexing.WebAP.WebAP_CONSTANTS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Thodoris Tsompanidis on 11/7/2016.
 */
public class QRel {

	private static final String LABEL_PERFECT= "PERFECT";
	private static final String LABEL_EXCEL= "EXCEL";
	private static final String LABEL_GOOD= "GOOD";
	private static final String LABEL_FAIR= "FAIR";
	private static final String LABEL_NONE= "NONE";

	private static final int RANK_PERFECT= 4;
	private static final int RANK_EXCEL= 3;
	private static final int RANK_GOOD= 2;
	private static final int RANK_FAIR= 1;
	private static final int RANK_NONE= 0;

	private static final String OUTPUT_QREL_FILE_PATH = "output\\trec_eval\\qrel.out";
	private static final String OUTPUT_UNIQUE_LINES_FILE_PATH = "output\\trec_eval\\uniqueLines.txt";

	/**
	 * Creates the qrel file (trec_eval input file) for WebAP dataset.<br>
 	 * This function uses the same algorithm for sentence IDentification as the one was used in Indexing process.
	 */
	public static void create_qrel_file_WebAP(){

		//first read the queries and keep qIDs
		ArrayList<String> QIDs = new ArrayList<String>();

		try {

			File file = new File(WebAP_CONSTANTS.INPUT_QUERIES_FILE_PATH);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String json = new String(data, "UTF-8");



			JSONObject jsonObject = new JSONObject(json);

			JSONArray queries = jsonObject.getJSONArray("queries");

			Iterator<Object> iterator =  queries.iterator();
			while (iterator.hasNext()) {
				JSONObject q = (JSONObject)iterator.next();
				QIDs.add((String) q.get("number"));

			}
		} catch (Exception e) {
			System.out.println("WebKIndex :: WebAPPassageIndexHandler()  Could NOT Parse the query json ");
			e.printStackTrace();
		}


		String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Creating the qrel file started at " + startTime);
		//read the input file
		File ipnutFile = new File(WebAP_CONSTANTS.INPUT_DOCUMENTS_FILE_PATH);
		PrintWriter writer = null;
		PrintWriter writerUL = null; //uniqueLine writer
		try {
			writer = new PrintWriter(OUTPUT_QREL_FILE_PATH, "UTF-8");
			writerUL = new PrintWriter(OUTPUT_UNIQUE_LINES_FILE_PATH, "UTF-8");
		} catch (FileNotFoundException e) {
			System.err.print("KIndex :: QRel.create_qrel_file() Could not find the output file");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.err.print("KIndex :: QRel.create_qrel_file() Could not handle the encoding for output file");
			e.printStackTrace();
		}
		Document inputDoc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			inputDoc = docBuilder.parse(ipnutFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: QRel.create_qrel_file() Could not create a document from input file");
			e.printStackTrace();
			return;
		}

		inputDoc.getDocumentElement().normalize();

		//iterate docs in xml
		NodeList docs = inputDoc.getElementsByTagName("DOC");

		HashSet<String> uniqueLines = new HashSet<String>();

		for (int temp = 0; temp < docs.getLength(); temp++) {

			Node doc = docs.item(temp);

			if (doc.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) doc;

				String docNo = eElement.getElementsByTagName("DOCNO").item(0).getTextContent();
				String targetQID = eElement.getElementsByTagName("TARGET_QID").item(0).getTextContent();
				String originalDocNop = eElement.getElementsByTagName("ORIGINAL_DOCNO").item(0).getTextContent();
				Node text = eElement.getElementsByTagName("TEXT").item(0);
				if (QIDs.contains(targetQID)) {

					int senID = 0; //sentence ID
					NodeList segments = text.getChildNodes();
					for (int s = 0; s < segments.getLength(); s++) {

						Node seg = segments.item(s);

						if (seg.getNodeType() == Node.ELEMENT_NODE) {
							String relevance = seg.getNodeName(); //query - sentence relevance
							NodeList sentences = seg.getChildNodes();
							for (int c = 0; c < sentences.getLength(); c++) {
								Node sentence = sentences.item(c);
								if (sentence.getNodeType() == Node.ELEMENT_NODE) {
									String sen = sentence.getTextContent();

									String queryID = targetQID;
									String documentID = docNo + ":" + senID;
									String relRank = "";
									switch (relevance) {
										case LABEL_PERFECT:
											relRank = Integer.toString(RANK_PERFECT);
											break;
										case LABEL_EXCEL:
											relRank = Integer.toString(RANK_EXCEL);
											break;
										case LABEL_GOOD:
											relRank = Integer.toString(RANK_GOOD);
											break;
										case LABEL_FAIR:
											relRank = Integer.toString(RANK_FAIR);
											break;
										case LABEL_NONE:
											relRank = Integer.toString(RANK_NONE);
											break;
									}
									//query-number 0 document-id relevance

									if (!relevance.equals(LABEL_NONE)) {
										writer.println(queryID + "    0    " + documentID + "    " + relRank);
										uniqueLines.add(queryID + "    " + relRank);
										//writer.println(queryID + "    0    " + documentID + "    " + relRank + "         (" + sen + ")");
									}
									senID++;
								}
							}
						}
					}
				}
			}
		}
		writer.close();

		writerUL.println("Uniqu lines with QueryObject & Level of Relevance");
		for (String u : uniqueLines) {
			writerUL.println(u);
		}
		writerUL.close();

		String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Creating the qrel file ended at " + endTime);
	}
}
