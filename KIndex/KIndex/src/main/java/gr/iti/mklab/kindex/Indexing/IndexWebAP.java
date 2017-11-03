package gr.iti.mklab.kindex.Indexing;

import gr.iti.mklab.kindex.Indexing.LuceneCustomClasses.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tools.ant.types.resources.comparators.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Thodoris Tsompanidis on 6/7/2016.
 */
public class IndexWebAP {

	public static final String INPUT_DOCUMENTS_FILE_PATH = "input\\WebAP\\gradedText\\grade.trectext_patched";
	public static final String INPUT_QUERIES_FILE_PATH = "input\\WebAP\\gradedText\\gov2.query.json";

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


	private IndexWriter writer1S;
	private IndexWriter writer2S;
	private IndexWriter writer3S;
	private IndexWriter writerAS;
	private IndexWriter writerD;

	public void doIndexing(){

		String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Indexing of WebAP dataset started at " + startTime);
		//read the input file
		File ipnutFile = new File(INPUT_DOCUMENTS_FILE_PATH);
		Document inputDoc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			inputDoc = docBuilder.parse(ipnutFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: IndexWebAP.index() Could not create a document from input file");
			e.printStackTrace();
			return;
		}

		inputDoc.getDocumentElement().normalize();

		//iterate docs in xml
		NodeList docs = inputDoc.getElementsByTagName("DOC");

		for (int temp = 0; temp < docs.getLength(); temp++) {

			Node doc = docs.item(temp);


			if (doc.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) doc;

				String docNo = eElement.getElementsByTagName("DOCNO").item(0).getTextContent();
				String targetQID = eElement.getElementsByTagName("TARGET_QID").item(0).getTextContent();
				String originalDocNop = eElement.getElementsByTagName("ORIGINAL_DOCNO").item(0).getTextContent();
				Node text = eElement.getElementsByTagName("TEXT").item(0);

				String firstSentence = null;
				String secondSentence = null;
				String thirdSentence = null;
				//the three sentences segment will be:
				//firstSentence + secondSentece + thirdSentence
				String completeDocumentText = "";

				int senID = 0; //sentence ID
				String startTimeDoc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				System.out.println(docNo + " Doc index started at " + startTimeDoc);
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

								firstSentence = secondSentence;
								secondSentence = thirdSentence;
								thirdSentence =  sen ;

								System.out.println("Indexing Doc: "+ docNo + ", sentence: " + senID + ", relevance: " + relevance);
								System.out.println(thirdSentence);
								System.out.println("------");

								completeDocumentText += thirdSentence;

								index(docNo, targetQID,originalDocNop, thirdSentence, relevance, senID, INDEX_ONE_SENTENCE_TYPE);

								if (secondSentence !=null){
									String twoSentencesText = secondSentence + " " + thirdSentence;
									index(docNo, targetQID,originalDocNop, twoSentencesText, relevance, senID, INDEX_TWO_SENTENCES_TYPE);

								}
								if (firstSentence !=null){
									String threeSentencesText = firstSentence + " " + secondSentence + " " + thirdSentence;
									index(docNo, targetQID,originalDocNop, threeSentencesText, relevance, senID, INDEX_THREE_SENTENCES_TYPE);

								}

								senID++;
							}
						}
					}
				}
				index(docNo, targetQID,originalDocNop, completeDocumentText, "", 0, INDEX_DOCUMENT_TYPE);
				commitWriters();

				System.out.println("End Of Indexing document: " + docNo);
				String endTimeDoc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				System.out.println(docNo + " Doc index started at " + startTimeDoc + "and ended at: " + endTimeDoc);
				System.out.println("  ");
				System.out.println("---------------------------------------------------------------");
			}
		}
		String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Indexing of WebAP dataset started at " + startTime);
		System.out.println("Indexing of WebAP dataset ended at " + endTime);
	}

	public void openWriters(){
		try {
			FSDirectory directory1S = FSDirectory.open(Paths.get(INDEX_ONE_SENTENCE_PATH));
			Analyzer analyzer1S = new CustomAnalyzer();
			IndexWriterConfig config1S = new IndexWriterConfig(analyzer1S);
			config1S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.writer1S = new IndexWriter(directory1S, config1S);

			Directory directory2S = FSDirectory.open(Paths.get(INDEX_TWO_SENTENCES_PATH));
			Analyzer analyzer2S = new CustomAnalyzer();
			IndexWriterConfig config2S = new IndexWriterConfig(analyzer2S);
			config2S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.writer2S = new IndexWriter(directory2S, config2S);

			Directory directory3S = FSDirectory.open(Paths.get(INDEX_THREE_SENTENCES_PATH));
			Analyzer analyzer3S = new CustomAnalyzer();
			IndexWriterConfig config3S = new IndexWriterConfig(analyzer3S);
			config3S.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.writer3S = new IndexWriter(directory3S, config3S);

			Directory directoryD = FSDirectory.open(Paths.get(INDEX_DOCUMENT_PATH));
			Analyzer analyzerD = new CustomAnalyzer();
			IndexWriterConfig configD = new IndexWriterConfig(analyzerD);
			configD.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.writerD = new IndexWriter(directoryD, configD);

			Directory directoryAS = FSDirectory.open(Paths.get(INDEX_ALL_SEGMENTATIONS_PATH));
			Analyzer analyzerAS = new CustomAnalyzer();
			IndexWriterConfig configAS = new IndexWriterConfig(analyzerAS);
			configAS.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			this.writerAS = new IndexWriter(directoryAS, configAS);

		} catch (IOException e) {
			System.out.println("KIndex :: IndexWebAP.openWriters()  Could NOT open Index Writers");
			e.printStackTrace();
		}

	}

	/**
	 * Store the parameters in a document and index it to the appropriate index.
	 * Furthermore, all documents are indexed in the {@value #INDEX_ALL_SEGMENTATIONS_TYPE}
	 *
	 * @param docNo
	 * @param targetQID
	 * @param originalDocNop
	 * @param content
	 * @param senID
	 * @param indexType
	 */
	private  void index(String docNo, String targetQID, String originalDocNop, String content, String relevance, int senID, String indexType) {
		org.apache.lucene.document.Document document = new org.apache.lucene.document.Document(); //doc to add in index

		document.add(new TextField(DOCUMENT_FIELD_DOC_NO, docNo, Field.Store.YES));
		document.add(new TextField(DOCUMENT_FIELD_TARGET_QID, targetQID, Field.Store.YES));
		document.add(new TextField(DOCUMENT_FIELD_ORIGINAL_DOC_NO, originalDocNop, Field.Store.YES));
		//For non One Sentence Documents, the sentence ID will have the ID from the last sentence of the text segment
		document.add(new TextField(DOCUMENT_FIELD_CONTENT, content, Field.Store.YES));
		//For non One Sentence Documents, the relevance willbe the relevance of the last sentence of the text segment
		document.add(new TextField(DOCUMENT_FIELD_RELEVANCE_WITH_QUERY, relevance, Field.Store.YES));
		document.add(new TextField(DOCUMENT_FIELD_SENTENCE_ID, Integer.toString(senID), Field.Store.YES));
		document.add(new TextField(DOCUMENT_FIELD_INDEX_TYPE, indexType, Field.Store.YES));

		try {
			switch(indexType){
				case INDEX_ONE_SENTENCE_TYPE:
					writer1S.addDocument(document);
					break;
				case INDEX_TWO_SENTENCES_TYPE:
					writer2S.addDocument(document);
					break;
				case INDEX_THREE_SENTENCES_TYPE:
					writer3S.addDocument(document);
					break;
				case INDEX_DOCUMENT_TYPE:
					writerD.addDocument(document);
					break;
			}
			//index the document in All segments index.
			writerAS.addDocument(document);

		} catch (IOException e) {
			System.out.println("KIndex :: IndexWebAP.index()  Could NOT write to " + indexType + " Index ");
			e.printStackTrace();
		}
	}

	private void commitWriters(){
		try {
			writer1S.commit();
			writer2S.commit();
			writer3S.commit();
			writerD.commit();
			writerAS.commit();
		} catch (IOException e) {
			System.out.println("KIndex :: IndexWebAP.index()  Could NOT commit Index Writers");
			e.printStackTrace();
		}
	}

	public void closeWriters(){

		try {
			writer1S.close();
			writer2S.close();
			writer3S.close();
			writerD.close();
			writerAS.close();

		} catch (IOException e) {
			System.out.println("KIndex :: IndexWebAP.index()  Could NOT close Index Writers");
			e.printStackTrace();
		}
	}
}
