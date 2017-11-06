package Indexing.WebAP;

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
import java.util.*;

/**
 * Class for calculating the distribution of the number of sentences in each level of relevance
 *
 * Created by Thodoris Tsompanidis on 12/7/2016.
 */
public class DistributionCalculator {

	public static void doCalculation(){

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

		//read the documents
		PrintWriter writer = null;
		PrintWriter writerQ = null;
		PrintWriter writerSR = null;
		PrintWriter writerPR = null;
		PrintWriter writerSenteceText = null;
		try {
			writer = new PrintWriter(WebAP_CONSTANTS.OUTPUT_DISTRIBUTION_FILE_PATH, "UTF-8");
			writerQ = new PrintWriter(WebAP_CONSTANTS.OUTPUT_QUERIES_RELEVANCE_FILE_PATH, "UTF-8");
			writerSR = new PrintWriter(WebAP_CONSTANTS.OUTPUT_SENTENCE_RELEVANCE_NUMBER_FILE_PATH, "UTF-8");
			writerPR = new PrintWriter(WebAP_CONSTANTS.OUTPUT_PASSAGE_RELEVANCE_NUMBER_FILE_PATH, "UTF-8");
			writerSenteceText = new PrintWriter(WebAP_CONSTANTS.OUTPUT_SENTENCES_TEXT_FILE_PATH, "UTF-8");
		} catch (FileNotFoundException e) {
			System.err.print("WebKIndex :: DistributionCalculator.doCalculation() Could not find the output file");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.err.print("WebKIndex :: DistributionCalculator.doCalculation() Could not handle the encoding for output file");
			e.printStackTrace();
		}

		String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("WebAP dataset's distribution calculation started at " + startTime);
		//read the input file
		File ipnutFile = new File(WebAP_CONSTANTS.INPUT_DOCUMENTS_FILE_PATH);
		Document inputDoc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			inputDoc = docBuilder.parse(ipnutFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: DistributionCalculator.doCalculation() Could not create a document from input file");
			e.printStackTrace();
			return;
		}

		inputDoc.getDocumentElement().normalize();

		Map<String, Map<Integer,Integer>> distributionMap = new HashMap<String, Map<Integer,Integer>>();
		//outer Map: Relevance -> segmentation
		//inner Map: number of sentences that contains the segmentation -> number of times appears

		Map<String,HashSet<String>> queriesRelevance = new HashMap<String,HashSet<String>>();
		//Map to store the queries id, and the relevance of segmentation that answers the query, if they exist

		Map<String, Integer> SentRelNumber = new HashMap<String, Integer>();

		Map<String, Integer> PassRelNumber = new HashMap<String, Integer>();

		int segmentationMax = 0;

		//iterate docs in xml
		NodeList docs = inputDoc.getElementsByTagName("DOC");

		for (int temp = 0; temp < docs.getLength(); temp++) {

			Node doc = docs.item(temp);

			if (doc.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) doc;

				String docNo = eElement.getElementsByTagName("DOCNO").item(0).getTextContent();
				String targetQID = eElement.getElementsByTagName("TARGET_QID").item(0).getTextContent();

				if (QIDs.contains(targetQID)) {
					Node text = eElement.getElementsByTagName("TEXT").item(0);

					NodeList segments = text.getChildNodes();
					for (int s = 0; s < segments.getLength(); s++) {

						Node seg = segments.item(s);

						if (seg.getNodeType() == Node.ELEMENT_NODE) {
							String relevance = seg.getNodeName(); //query - sentence relevance

							if (!queriesRelevance.containsKey(targetQID)) {
								queriesRelevance.put(targetQID, new HashSet<String>());
							}
							queriesRelevance.get(targetQID).add(relevance);

							if (!PassRelNumber.containsKey(relevance)) {
								PassRelNumber.put(relevance, 0);
							}
							PassRelNumber.put(relevance, PassRelNumber.get(relevance) + 1);

							int count = 0;
							NodeList sentences = seg.getChildNodes();
							for (int c = 0; c < sentences.getLength(); c++) {
								Node sentence = sentences.item(c);
								if (sentence.getNodeType() == Node.ELEMENT_NODE) {

									String sen = sentence.getTextContent();
									writerSenteceText.println(relevance + " - " + sen);

									count++;
									if (!SentRelNumber.containsKey(relevance)) {
										SentRelNumber.put(relevance, 0);
									}
									SentRelNumber.put(relevance, SentRelNumber.get(relevance) + 1);

								}
							}

							if (!relevance.equals("NONE")) {

								if (!distributionMap.containsKey(relevance)) {
									distributionMap.put(relevance, new HashMap<Integer, Integer>());
								}

								Map<Integer, Integer> segmentation = distributionMap.get(relevance);
								if (!segmentation.containsKey(count)) {
									segmentation.put(count, 1);
								} else {
									int tempInt = segmentation.get(count) + 1;
									segmentation.put(count, tempInt);
								}
								if (segmentationMax < count) {
									segmentationMax = count;
								}
							}
						}
					}
				}
			}
		}

		String firstLine = "Relevance";
		for (int i = 1; i < segmentationMax+1; i++) {
			firstLine += ", " + i ;
		}
		writer.println(firstLine);
		Set<String> rel = distributionMap.keySet();
		for (String r : rel) {
			String line = r;
			Map<Integer, Integer> counts = distributionMap.get(r);
			for (int j = 1; j < segmentationMax+1; j++) {
				if (counts.containsKey(j)){
					line += ", " + counts.get(j);
				}
				else{
					line += ", 0";
				}
			}
			writer.println(line);
		}

		Set<String> queries = queriesRelevance.keySet();
		for (String q : queries) {
			String query = q + ": ";
			HashSet<String> rels = queriesRelevance.get(q);
			String prefix = "";
			for (String s : rels) {
				query += prefix + s;
				prefix = ",";
			}
			writerQ.println(query);
		}

		Set<String> relev = SentRelNumber.keySet();
		for (String re : relev) {
			writerSR.println(re + ": " + SentRelNumber.get(re));
		}

		Set<String> relevP = PassRelNumber.keySet();
		for (String re : relevP) {
			writerPR.println(re + ": " + PassRelNumber.get(re));
		}

		writer.close();
		writerQ.close();
		writerSR.close();
		writerPR.close();
		writerSenteceText.close();

		String endTimeDoc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("WebAP dataset's distribution calculation ended at " + endTimeDoc);

	}
}
