package gr.iti.mklab.kindex.Babelfy;


import com.google.gson.Gson;
import gr.iti.mklab.kindex.RelExt.RelExt;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Class for handling Babelfy annotation.
 * Babelfy associates terms in a text with BabelNet and DBPedia resources
 *
 * Created by Thodoris Tsompanidis on 12/1/2016.
 */
public final class BabelfyHandler {

	/**
	 * Returns Babelfy annotated text in json
	 *
	 * ex.
	 * [{
	 *	 "tokenFragment": {
	 *	     "start": 32,
	 *  	 "end": 34
	 *	 },
	 *	 "charFragment": {
	 *	     "start": 158,
	 *	     "end": 173
	 *	 }
	 *	 "babelSynsetID": "bn:00004425n",
	 *	 "DBpediaURL": "http://dbpedia.org/resource/Anorexia_(symptom)",
	 *	 "BabelNetURL": "http://babelnet.org/rdf/s00004425n",
	 *	 "score": 1.0,
	 *	 "coherenceScore": 0.4444444444444444,
	 *	 "globalScore": 0.021761954758041425,
	 *	 "source": "BABELFY"
	 *   },...
	 * ]
	 *
	 * @param text to be annotated
	 * @return Babelfy annotated text in JSON format
	 */
	public static String annotateText(String text){


		Babelfy bfy = new Babelfy();
		List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(text, Language.EN);

		if (RelExt.MODE.equals("debug")) {
			for (SemanticAnnotation annotation : bfyAnnotations) {
				//splitting the input text using the CharOffsetFragment start and end anchors
				String frag = text.substring(annotation.getCharOffsetFragment().getStart(),
						annotation.getCharOffsetFragment().getEnd() + 1);
				System.out.println(frag + "\t" + annotation.getBabelSynsetID());
				System.out.println("\t" + annotation.getBabelNetURL());
				System.out.println("\t" + annotation.getDBpediaURL());
				System.out.println("\t" + annotation.getSource());
			}
		}

		//results may contain one token more than once. once as a single token and once as phrase
		//ex. "Yellow Fever". One Anottation is "Yellow", other one is "Fever" and other one is "Yellow Fever".
		//the annotation with most words wins. In this example we would only keep "Yellow Fever" as output.
		bfyAnnotations = cleanDuplicates(bfyAnnotations);

		Gson json = new Gson();
		return json.toJson(bfyAnnotations);

	}

	/**
	 * Gets a MetaMap annotates xml and babelfy the concept tags
	 * Ex. input "<?xml version="1.0"?><content><text>The main requirement is progressive cognitive decline of sufficient magnitude to interfere with normal social or occupational function. Prominent or persistent </text><concept MetaMap="Finding">memory</concept><text> impairment may not necessarily occur in the early stages but is usually evident with progression.</text></content>"
	 * Ex. output "<content><text>The main requirement is progressive cognitive decline of sufficient magnitude to interfere with normal social or occupational function. Prominent or persistent </text><concept BabelNet="http://babelnet.org/rdf/s00054299n" DBPedia="" MetaMap="Finding">memory</concept><text> impairment may not necessarily occur in the early stages but is usually evident with progression.</text></content>"
	 *
	 * @param text
	 * @return string
	 */
	public static String babelFyConceptTags(String text) {

		Document document = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
			document = docBuilder.parse(stream);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: MetaMapHandler.babelFyConceptTags() Could not create a document text.");
			e.printStackTrace();
		}

		Babelfy bfy = new Babelfy();
		NodeList concepts = document.getElementsByTagName("concept");
		for (int i = 0; i < concepts.getLength(); i++) {
			Element concept = (Element) concepts.item(i);

			//System.out.println("Concept Found: " + concept.getTextContent());
			List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(concept.getTextContent(), Language.EN);
			bfyAnnotations = cleanDuplicates(bfyAnnotations);

			String babelnet = "";
			String dbpedia = "";
			if (bfyAnnotations.size()>0){
				babelnet = bfyAnnotations.get(0).getBabelNetURL();
				dbpedia = bfyAnnotations.get(0).getDBpediaURL();
			}
			concept.setAttribute("BabelNet", babelnet);
			concept.setAttribute("DBPedia", dbpedia);
		}

		//print updated document
		String output = null;
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		} catch (TransformerException e) {
			System.err.print("KIndex :: MetaMapHandler.babelFyConceptTags() Could not transform the xml document to string.");
			e.printStackTrace();
		}

		//System.out.println(output);
		return output;
	}

	public static String extractNE(String text){
		BabelfyParameters bp = new BabelfyParameters();
		bp.setAnnotationType(BabelfyParameters.SemanticAnnotationType.NAMED_ENTITIES);
		Babelfy bfy = new Babelfy(bp);
		List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(text, Language.EN);

		if (RelExt.MODE.equals("debug")) {
			for (SemanticAnnotation annotation : bfyAnnotations) {
				//splitting the input text using the CharOffsetFragment start and end anchors
				String frag = text.substring(annotation.getCharOffsetFragment().getStart(),
						annotation.getCharOffsetFragment().getEnd() + 1);
				System.out.println(frag + "\t" + annotation.getBabelSynsetID());
				System.out.println("\t" + annotation.getBabelNetURL());
				System.out.println("\t" + annotation.getDBpediaURL());
				System.out.println("\t" + annotation.getSource());
			}
		}

		//results may contain one token more than once. once as a single token and once as phrase
		//ex. "Yellow Fever". One Anottation is "Yellow", other one is "Fever" and other one is "Yellow Fever".
		//the annotation with most words wins. In this example we would only keep "Yellow Fever" as output.
		bfyAnnotations = cleanDuplicates(bfyAnnotations);

		Gson json = new Gson();
		return json.toJson(bfyAnnotations);
	}

	/**
	 * Returns a SemanticAnnotation list clean of duplicates
	 *
	 * results may contain one token more than once. once as a single token and once as phrase
	 * ex. "Yellow Fever". One Anottation is "Yellow", other one is "Fever" and other one is "Yellow Fever".
	 * the annotation with most words wins. In this example we would only keep "Yellow Fever" as output.
	 *
	 * @param annotations to be cleaned
	 * @return a duplicate free list of SemanticAnnotation
	 */
	private static List<SemanticAnnotation> cleanDuplicates(List<SemanticAnnotation> annotations) {

		List<SemanticAnnotation> newAnnotations = new ArrayList<SemanticAnnotation>();
		HashSet<Integer> tokensAlreadyIn = new HashSet<Integer>();

		//first find the annotation with most words in it
		int max = 0;
		for (SemanticAnnotation annotation : annotations) {
			int diff = annotation.getTokenOffsetFragment().getEnd()-annotation.getTokenOffsetFragment().getStart();
			if (diff > max)
				max=diff;
		}

		//for each count of terms
		for (int i=max; i >= 0; i--){
			//for every remaining annotation
			Iterator<SemanticAnnotation> anIter = annotations.iterator();
			while(anIter.hasNext()) {
				SemanticAnnotation annotation = anIter.next();
				//only annotations containing i terms
				if (annotation.getTokenOffsetFragment().getEnd()-annotation.getTokenOffsetFragment().getStart() == i) {
					//if none of tokens (single terms) is already in newAnnotation list, add annotation to new Annotation, and tokens ids to hashSet
					if (noneOfTokensIsAlreadyIn(annotation.getTokenOffsetFragment().getStart(), annotation.getTokenOffsetFragment().getEnd(), tokensAlreadyIn)) {
						newAnnotations.add(annotation);
						anIter.remove();
						//add token ids to hashSet
						for (int j = annotation.getTokenOffsetFragment().getStart(); j <= annotation.getTokenOffsetFragment().getEnd(); j++) {
							tokensAlreadyIn.add(j);
						}
					}
					//if at least one token is already in newAnnotation List, don't add it but remove it.
					else {
						anIter.remove();
					}
				}
			}
		}


		return newAnnotations;
	}

	/**
	 * Returns true only if no integer between start and end is contained in tokensAlreadyIn
	 * If at least one int is contained, returns false
	 *
	 * @param start
	 * @param end
	 * @param tokensAlreadyIn
	 * @return true only if noneOfTokensIsAlreadyIn
	 */
	private static boolean noneOfTokensIsAlreadyIn(int start, int end, HashSet<Integer> tokensAlreadyIn) {
		for(int i = start; i <= end;i++){
			if (tokensAlreadyIn.contains(i))
				return false;
		}
		return true;
	}
}






























