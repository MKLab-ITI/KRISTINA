package MetaMap;

import gov.nih.nlm.nls.skr.GenericObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to handle conection and extraction for MetaMap
 *
 * MetaMap MetaMap is a highly configurable program developed by Dr. Alan (Lan) Aronson at the National Library of Medicine (NLM)
 * to map biomedical text to the UMLS Metathesaurus or, equivalently, to discover Metathesaurus concepts referred to in text
 *
 * Created by Thodoris Tsompanidis on 11/12/2015.
 */
public class MetaMapHandler {

	public static final String INPUT_FILE="MetaMap/INPUT.TXT";
	public static final String OUTPUT_FILE="MetaMap/MetaMapOutput.html";
	public static final String TEMP_FILE="MetaMap/tmp.txt";

	/**
	 * Gets as input the text to be processed and returns the output of MetaMap calling
	 * To handle the MetaMap output format, you can use a custom parser (Class CustomMetaMapParser)
	 *
	 * @param text String. The text to be processed. (To get concepts)
	 * @return String. The MetaMap output
	 */
	public static String getMetaMapOutput(String text) {


		//In MetaMap Web API calling, a file with the text is uploaded and not just the text as string
		//Because of that, the incoming text is stored first in a temp file.
		//At the end of this function, file's content is removed

		String output=null;
		try {
			//Write the text in temp file
			PrintWriter inputWriter = null;
			inputWriter = new PrintWriter(TEMP_FILE, "UTF-8");
			inputWriter.print(text);
			inputWriter.close();

			GenericObject myGenericObj = new GenericObject(MetaMapCONSTANTS.USERNAME, MetaMapCONSTANTS.PASSWORD);
			myGenericObj.setField("Email_Address", MetaMapCONSTANTS.EMAIL);
			//Restrict concept extraction to semantic types: [Disease or Syndrome], [Finding], [Sign or Symptom]
			myGenericObj.setField("Batch_Command", "metamap -E -J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs"); //
			myGenericObj.setField("SilentEmail", true);
			myGenericObj.setFileField("UpLoad_File", TEMP_FILE);
			//myGenericObj.setFileField("UpLoad_File", "MetaMap/tmp.txt");
			//myGenericObj.setFileField("UpLoad_File", "C:/tmp.txt");
			output = myGenericObj.handleSubmission();

			//Delete content of temp input file
			//inputWriter = new PrintWriter(TEMP_FILE, "UTF-8");
			//inputWriter.print("");
			//inputWriter.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("KIndex :: MetaMapHandler.getMetaMapOutput() Could not open temp input file");
			e.printStackTrace();
			return null;
		}

		return output;
	}

		try
		{
			*//*File file = new File(INPUT_FILE);
			String contents = new Scanner(file).useDelimiter("\\Z").next();*//*
			String t = getMetaMapOutput(text);
			String results = CustomMetaMapParser.mergeResutlstoHTML(t);
			System.out.print("Object Submitted");

			//write results to file
			PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8");
			writer.print(results);
			writer.close();

		} catch (RuntimeException | FileNotFoundException | UnsupportedEncodingException ex) {
			System.err.print("KIndex :: MetaMapHandler.extractFromFileToHTMLFile() Could not open input or output file");
			ex.printStackTrace();
		}

	}*/

	
	/**
	 * Get the text annotated. MetaMap consepts will be included in tags.
	 * Tag labels will be named after the MetaMap concept
	 *
	 * @param text
	 * @param debugMode
	 * @return The annotated text
	 */
	public static String getAnnotatedXMLTags(String text, boolean debugMode) {

		//remove the empty lines from the text
		text = text.replaceAll("(?m)^[ \\t]*\\r?\\n", "");

		String outputXML = getFormattedXMLOutput(text, debugMode);

		Document document = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(outputXML.getBytes(StandardCharsets.UTF_8));
			document = docBuilder.parse(stream);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: MetaMapHandler.getAnnotatedXMLTags() Could not create a document from MetaMap XML output.");
			e.printStackTrace();
		}

		// "Candidate" tag contains concept.
		// extract the semantic type of the concept and the position/length

		HashSet<Integer> startPos = new HashSet<Integer>();
		int charOffset = 0;
		int nlChar = 0;
		int minPos = -1;
		NodeList candidates = document.getElementsByTagName("Candidate");
		for (int i = 0; i < candidates.getLength(); i++) {
			Node candidate = candidates.item(i);
			NodeList children = candidate.getChildNodes();

			String semType = "NoType";
			int start = 0, length = 0;
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (child.getNodeName().equals("SemTypes")) {
					Node semTypeNode = child.getChildNodes().item(1);
					semType = semTypeNode.getFirstChild().getNodeValue();
				} else if (child.getNodeName().equals("ConceptPIs")) {
					Node conceptPI = child.getChildNodes().item(1);
					NodeList cPIList = conceptPI.getChildNodes();
					for (int k = 0; k < cPIList.getLength(); k++) {
						Node cPIchild = cPIList.item(k);
						if (cPIchild.getNodeName().equals("StartPos")) {
							//System.out.println("Start: " + cPIchild.getFirstChild().getNodeValue());
							start = Integer.parseInt(cPIchild.getFirstChild().getNodeValue());
						} else if (cPIchild.getNodeName().equals("Length")) {
							//System.out.println("Length: " + cPIchild.getFirstChild().getNodeValue());
							length = Integer.parseInt(cPIchild.getFirstChild().getNodeValue());
						}
					}
				}
			}

			//if concept is found, wrap the concept in xml tags
			if (!semType.equals("NoType")) {

				int initialTextLength = text.length();
				String semanticType = getConceptLabel(semType);


				//check if the same term is already annotated
				if((!startPos.contains(start)) && (start > minPos)) {

					minPos = start;
					startPos.add(start);
					//charOffset: the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
					//nlChar: the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually

					 nlChar = text.substring(0, start + charOffset + nlChar).split(System.getProperty("line.separator")).length - 1;

					String prev = text.substring(0, start + charOffset + nlChar );
					String conceptTerms = text.substring(start + charOffset + nlChar , start + length + charOffset + nlChar );
					String next = text.substring(start + length + charOffset + nlChar, text.length());
					if (!conceptTerms.matches("[a-zA-Z0-9].*")) { //in case concept Term's first character is not a letter or number
						Pattern p = Pattern.compile("\\p{L}");
						Matcher m = p.matcher(conceptTerms);
						if (m.find()) {
							int letterPos = m.start();
							int offset = conceptTerms.substring(0, letterPos).length();
							prev = text.substring(0, start + charOffset + nlChar + offset);
							conceptTerms = text.substring(start + charOffset + nlChar + offset, start + length + charOffset + nlChar + offset);
							next = text.substring(start + length + charOffset + nlChar + offset, text.length());
						}
					}
					text = prev + "<concept MetaMap=\"" + semanticType + "\">" + conceptTerms + "</concept>" + next;
					charOffset += text.length() - initialTextLength;
					//System.out.println("Offset: " + charOffset);
					//System.out.println("New Text: ");
					//System.out.println(text);
				}
			}
			//System.out.println("Semantic Type: " + semType + ", Start: " + start + ", length: " + length);
		}

		return text;
	}

	private static String getFormattedXMLOutput(String text, boolean debugMode) {
		//In MetaMap Web API calling, a file with the text is uploaded and not just the text as string
		//Because of that, the incoming text is stored first in a temp file.
		//At the end of this function, file's content is removed

		String output=null;
		try {
			//Write the text in temp file
			PrintWriter inputWriter = null;
			inputWriter = new PrintWriter(TEMP_FILE, "UTF-8");
			inputWriter.print(text);
			inputWriter.close();

			GenericObject myGenericObj = new GenericObject(MetaMapCONSTANTS.USERNAME, MetaMapCONSTANTS.PASSWORD);
			myGenericObj.setField("Email_Address", MetaMapCONSTANTS.EMAIL);
			//Restrict concept extraction to semantic types: [Disease or Syndrome], [Finding], [Sign or Symptom]
			myGenericObj.setField("Batch_Command", "metamap -A -J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs --XMLf -V USAbase"); //-A -J dsyn,fndg,sosy --XMLf -V USAbase
			myGenericObj.setField("SilentEmail", true);
			myGenericObj.setFileField("UpLoad_File", TEMP_FILE);

			if (debugMode) System.out.println("MetaMap sending submission");
			output = myGenericObj.handleSubmission();
			if (debugMode) System.out.println("MetaMap Submission got results back");

		} catch ( Exception e) {
			System.out.println("KIndex :: MetaMapHandler.getFormattedXMLOutput() An error occurred");
			e.printStackTrace();
			return null;
		}

		return output;
	}

	public static String getAllConceptsInString(String content) {

		//remove the empty lines and non-ascii charactes from the text
		content = content.replaceAll("(?m)^[ \\t]*\\r?\\n", "").replaceAll("[^\\x00-\\x7F]", "");

		String outputXML = getFormattedXMLOutput(content, false);

		Document document = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(outputXML.getBytes(StandardCharsets.UTF_8));
			document = docBuilder.parse(stream);
			HashSet<String> concepts= new HashSet<String>();

			NodeList candidates = document.getElementsByTagName("SemType");
			for (int i = 0; i < candidates.getLength(); i++) {
				Node candidate = candidates.item(i);
				String concept = candidate.getTextContent();
				concepts.add(getConceptLabel(concept));
			}

			return concepts.toString().replaceAll("[\\[\\],]"," ");

		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("KIndex :: MetaMapHandler.getAnnotatedXMLTags() Could not create a document from MetaMap XML output.");
			System.err.println("MetaMam Output: ");
			System.err.println(outputXML);
			e.printStackTrace();
			return "";
		}
	}

	private static String getConceptLabel(String concept){
		switch (concept) {
			//acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs
			case "acab":
				return "AcquiredAbnormality";
			case "anab":
				return "AnatomicalAbnormality";
			case "antb":
				return "Antibiotic";
			case "bact":
				return "Bacterium";
			case "bodm":
				return "BiomedicalOrDentalMaterial";
			case "comd":
				return "CellOrMolecularDysfunction";
			case "clnd":
				return "ClinicalDrug";
			case "cgab":
				return "CongenitalAbnormality";
			case "diap":
				return "DiagnosticProcedure";
			case "dsyn":
				return "DiseaseOrSyndrome";
			case "drdd":
				return "DrugDeliveryDevice";
			case "food":
				return "Food";
			case "inpo":
				return "InjuryOrPoisoning";
			case "lbpr":
				return "LaboratoryProcedure";
			case "medd":
				return "MedicalDevice";
			case "mobd":
				return "MentalOrBehavioralDysfunction";
			case "neop":
				return "NeoplasticProcess";
			case "patf":
				return "PathologicFunction";
			case "podg":
				return "PatientOrDisabledGroup";
			case "phsu":
				return "PharmacologicSubstance";
			case "sosy":
				return "SignOrSymptom";
			case "topp":
				return "TherapeuticOrPreventiveProcedure";
			case "virs":
				return "Virus";
		}
		return "NoType";
	}

}
















