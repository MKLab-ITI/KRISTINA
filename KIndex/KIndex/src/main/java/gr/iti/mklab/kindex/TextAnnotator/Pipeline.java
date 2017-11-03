package gr.iti.mklab.kindex.TextAnnotator;

import gr.iti.mklab.kindex.CoreNLP.CoreNLPHandler;
import gr.iti.mklab.kindex.MetaMap.LocalMetaMapHandler;
import gr.iti.mklab.kindex.MetaMap.MetaMapHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static gr.iti.mklab.kindex.Babelfy.BabelfyHandler.babelFyConceptTags;

/**
 * Pipeline class represents a container for hosting pipeline processes for Concept Extraction, Named Entities Extraction, Relation Extraction
 * Components can be used:
 * 		- MetaMap
 * 		- Stanford	CoreNLP
 * 		- Babelfy
 * 		- Unitex
 *
 * Created by Thodoris Tsompanidis on 16/3/2016.
 */
public class Pipeline {

	/**
	 * Pass the provided text through the pipeline.
	 * Pipelines:
	 * 		- MRC+POS+NER: Medical Related Concepts + Part Of Speech + Named Entity Recognition.
	 * 			~ Pass text in MetaMap to annotate Concepts [Disease or Syndrome, Sign or Syndrome, Finding]
	 * 			~ Pass text in Babelfy: Link the MetaMap concepts to BabelFy concepts.
	 * 			~ Pass the text in Stanford Core NLP for Named Entity Recognition and Part Of Speech extraction
	 * 			~ Merge the outputs and return the text with xml tags
	 *
	 * @param pipeline
	 * @param text
	 * @param debugMode
	 * @return The annotated text in xml tags. NULL if there is no pipeline provided, or if something goes wrong
	 */
	public static String execute(String pipeline, String text, boolean debugMode) {

		if (debugMode) System.out.println("Pipeline: " + pipeline);

		//first handle the special characters which cause troubles
		text = text.replaceAll("\r\n","\r\n ");
		//text = text.replaceAll("\\p{Punct} "," $0");
		//remove all no ascii characters
		text = text.replaceAll("[^\\x00-\\x7F]", "");
		text = StringEscapeUtils.unescapeXml(text);
		text = StringEscapeUtils.escapeXml(text);


		if (pipeline.equals("MRC+POS+NER")){
			return pipelineMRC_POS_NER(text, debugMode);
		}
		else if (pipeline.equals("MRC")){
			return pipelineMRC(text, debugMode, false);
		}
		else if (pipeline.equals("MRC_OCT")){
			return pipelineMRC(text, debugMode, true);
		}
		else if (pipeline.equals("POS+NER")){
			return pipelinePOS_NER(text, debugMode);
		}
		else{
			return null;
		}
	}

	/**
	 * Execute Part Of Speech and Named Entities Extraction
	 *
	 * @param text
	 * @param debugMode
	 * @return
	 */
	private static String pipelinePOS_NER(String text, boolean debugMode) {
		if (debugMode) System.out.println("Starting coreNLP analysis");

		//POS & NER extraction, Stanford CoreNLP
		String NePosText = CoreNLPHandler.getXMLwithNERandPOS(text);

		return NePosText;
	}

	/**
	 * execute MRC pipeline. MetaMap concept extraction
	 *
	 * @param text
	 * @param debugMode
	 * @return
	 */
	private static String pipelineMRC(String text, boolean debugMode, boolean onlyConceptTag) {
		if (debugMode) System.out.println("pipelineMRC_POS_NER function ");
		text = text.replaceAll("&apos;","\'");
		if (debugMode) System.out.println("MetaMap extracting concepts");
		//String MetaMapText = MetaMapHandler.getAnnotatedXMLTags(text, debugMode);
		String LocalMetaMapText = LocalMetaMapHandler.getAnnotatedXMLTags(text);

		//Debugging
		//System.out.println("----------------------------------------------------");
		//System.out.println(MetaMapText);
		//System.out.println(LocalMetaMapText);
		//System.out.println("----------------------------------------------------");
		//End of Debugging

		if (debugMode) System.out.println("MetaMap concepts extracted");

		if(!onlyConceptTag) {
			LocalMetaMapText = "<content><text>" + LocalMetaMapText;
			LocalMetaMapText = LocalMetaMapText.replaceAll(Pattern.quote("<concept"), "</text><concept");
			LocalMetaMapText = LocalMetaMapText.replaceAll(Pattern.quote("</concept>"), "</concept><text>");
			LocalMetaMapText = LocalMetaMapText + "</text></content>";
		}

		return LocalMetaMapText;
	}

	/**
	 * Execute the MRC+POS+NER Pipeline. More for MRC+POS+NER Pipeline in {@link #execute(String, String, boolean)}
	 * Return a String with the text in xml tags OR null if something goes wrong
	 *
	 * @param text
	 * @param debugMode
	 * @return The text in xml tags or NULL if something goes wrong
	 */
	private static String pipelineMRC_POS_NER(String text, boolean debugMode) {

		if (debugMode) System.out.println("pipelineMRC_POS_NER function ");
		text = text.replaceAll("&apos;","\'");
		if (debugMode) System.out.println("MetaMap extracting concepts");
		//String MetaMapText = MetaMapHandler.getAnnotatedXMLTags(text, debugMode);
		String LocalMetaMapText = LocalMetaMapHandler.getAnnotatedXMLTags(text);

		//Debugging
		//System.out.println("----------------------------------------------------");
		//System.out.println(MetaMapText);
		//System.out.println(LocalMetaMapText);
		//System.out.println("----------------------------------------------------");
		//End of Debugging

		if (debugMode) System.out.println("MetaMap concepts extracted");

		/*
		String MetaMapText = "The main requirement is progressive cognitive decline of sufficient magnitude to interfere with normal social or occupational function. Prominent or persistent <concept MetaMap=\"Finding\">memory</concept> impairment may not necessarily occur in the early stages but is usually evident with progression. <concept MetaMap=\"DiseaseOrSyndrome\">Deficits</concept> on <concept MetaMap=\"Finding\">tests</concept> of attention and frontal-sub-cortical skills and visuospatial <concept MetaMap=\"Finding\">ability</concept> may be especially prominent.\n" +
				"The main <concept MetaMap=\"Finding\">diagnostic issue</concept> is to distinguish <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> from the commoner <concept MetaMap=\"DiseaseOrSyndrome\">Alzheimer's disease</concept>. A common <concept MetaMap=\"Finding\">diagnostic</concept> error is to attribute the clinical features of <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> to <concept MetaMap=\"DiseaseOrSyndrome\">cerebral vascular disease</concept> such as <concept MetaMap=\"DiseaseOrSyndrome\">multi-infarct dementia</concept> or <concept MetaMap=\"DiseaseOrSyndrome\">Binswanger's disease</concept>.\n" +
				"There are <concept MetaMap=\"Finding\">no</concept> specific <concept MetaMap=\"Finding\">diagnostic</concept> tests for <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept>. CT and MRI imaging can assist in the process (Ince, P et al Copyright Brain Pathology 1998).\n" +
				"Care and treatment\n" +
				"For people with <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> neuroleptics may be particularly dangerous. This class of drugs induce Parkinson-like side effects, including <concept MetaMap=\"SignOrSymptom\">rigidity</concept> and an inability to perform tasks or to <concept MetaMap=\"Finding\">communicate</concept>. Studies have shown that when prescribed for people with <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> it may cause <concept MetaMap=\"Finding\">sudden death</concept>. If a person with <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> must be prescribed a neuroleptic it should be done with the utmost care and under constant supervision with regular monitoring. In certain cases some people with <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept> are <concept MetaMap=\"Finding\">able</concept> to tolerate such treatment so that their hallucinations are reduced. There is now some evidence to suggest that the more recently developed '<concept MetaMap=\"Finding\">atypical</concept>' anti-psychotic dugs like olanzapine (Zyprexa), quetiapine (Seroquel) or respiridone (Risperdal), may be safe to use. It is still reasonable to try to simplify anti-parkinsonian medication as a first step, particularly withdrawing drugs of lower potency (and particular tendency to cause <concept MetaMap=\"SignOrSymptom\">confusion</concept>) such as anti-cholinergics and selegeline; where possible dopamine agonists should also be <concept MetaMap=\"Finding\">withdrawn</concept>, leaving most patients on levodopa <concept MetaMap=\"Finding\">alone</concept>. (Ince, P et al Copyright Brain Pathology 1998) At present there is no cure for <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept>. Recent research has suggested that the cholinesterase drugs <concept MetaMap=\"Finding\">used</concept> to treat <concept MetaMap=\"DiseaseOrSyndrome\">Alzheimer's disease</concept> may also be useful in treating <concept MetaMap=\"DiseaseOrSyndrome\">DLB</concept>, although they are not yet licensed for this use (<concept MetaMap=\"DiseaseOrSyndrome\">Alzheimer</concept> Scotland-Action on Dementia 2002).\n" +
				"Ongoing research / Clinical trials";
		*/


		//At this point concept tags are added to text
		//Ex. For people with <concept MetaMap="DiseaseOrSyndrome">DLB</concept> neuroleptics...

		LocalMetaMapText = "<?xml version=\"1.0\"?><content><text>" + LocalMetaMapText;
		LocalMetaMapText = LocalMetaMapText.replaceAll(Pattern.quote("<concept"),"</text><concept");
		LocalMetaMapText = LocalMetaMapText.replaceAll(Pattern.quote("</concept>"),"</concept><text>");
		LocalMetaMapText =  LocalMetaMapText + "</text></content>";
		//if (debugMode) System.out.println("Babelfying MetaMap output");
		//
		//BabelFy the concepts
		//String ConText = babelFyConceptTags(MetaMapText);

		//if (debugMode) System.out.println("Babelfied MetaMap output");

		//At this point babelfy attributes are added to text
		//Ex. <text>Prominent or persistent </text><concept BabelNet="http://babelnet.org/rdf/s00054299n" DBPedia="" MetaMap="Finding">memory</concept><text> impairment may not necessarily occur in the early stages but is usually evident with progression.</text>

		if (debugMode) System.out.println("Starting coreNLP analysis");

		//POS & NER extraction, Stanford CoreNLP
		String NePosText = CoreNLPHandler.getXMLwithNERandPOS(text);

		// the initial text is now with <pos> tags and <ne> tags only for NE

		/*System.out.println("Concept: ");
		System.out.println(BabelFyMetaMaptext);
		System.out.println("NER & POS: ");
		System.out.println(NER_POSText);*/

		//String ConText = "<content><text>The main requirement is progressive cognitive decline of sufficient magnitude to interfere with normal social or occupational function. Prominent or persistent </text><concept BabelNet=\"http://babelnet.org/rdf/s00054299n\" DBPedia=\"\" MetaMap=\"Finding\">memory</concept><text> impairment may not necessarily occur in the early stages but is usually evident with progression. </text><concept BabelNet=\"http://babelnet.org/rdf/s00025917n\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">Deficits</concept><text> on </text><concept BabelNet=\"http://babelnet.org/rdf/s00076647n\" DBPedia=\"\" MetaMap=\"Finding\">tests</concept><text> of attention and frontal-sub-cortical skills and visuospatial </text><concept BabelNet=\"http://babelnet.org/rdf/s00000317n\" DBPedia=\"\" MetaMap=\"Finding\">ability</concept><text> may be especially prominent. The main </text><concept BabelNet=\"http://babelnet.org/rdf/s00101313a\" DBPedia=\"\" MetaMap=\"Finding\">diagnostic issue</concept><text> is to distinguish </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> from the commoner </text><concept BabelNet=\"http://babelnet.org/rdf/s00003193n\" DBPedia=\"http://dbpedia.org/resource/Alzheimer's_disease\" MetaMap=\"DiseaseOrSyndrome\">Alzheimer's disease</concept><text>. A common </text><concept BabelNet=\"http://babelnet.org/rdf/s00101313a\" DBPedia=\"\" MetaMap=\"Finding\">diagnostic</concept><text> error is to attribute the clinical features of </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> to </text><concept BabelNet=\"http://babelnet.org/rdf/s01844666n\" DBPedia=\"http://dbpedia.org/resource/Cerebrovascular_disease\" MetaMap=\"DiseaseOrSyndrome\">cerebral vascular disease</concept><text> such as </text><concept BabelNet=\"http://babelnet.org/rdf/s14421488n\" DBPedia=\"http://dbpedia.org/resource/Vascular_dementia\" MetaMap=\"DiseaseOrSyndrome\">multi-infarct dementia</concept><text> or </text><concept BabelNet=\"http://babelnet.org/rdf/s17368190n\" DBPedia=\"http://dbpedia.org/resource/Binswanger\" MetaMap=\"DiseaseOrSyndrome\">Binswanger's disease</concept><text>. There are </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"Finding\">no</concept><text> specific </text><concept BabelNet=\"http://babelnet.org/rdf/s00101313a\" DBPedia=\"\" MetaMap=\"Finding\">diagnostic</concept><text> tests for </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text>. CT and MRI imaging can assist in the process (Ince, P et al Copyright Brain Pathology 1998). Care and treatment For people with </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> neuroleptics may be particularly dangerous. This class of drugs induce Parkinson-like side effects, including </text><concept BabelNet=\"http://babelnet.org/rdf/s00067854n\" DBPedia=\"http://dbpedia.org/resource/Structural_rigidity\" MetaMap=\"SignOrSymptom\">rigidity</concept><text> and an inability to perform tasks or to </text><concept BabelNet=\"http://babelnet.org/rdf/s00085435v\" DBPedia=\"\" MetaMap=\"Finding\">communicate</concept><text>. Studies have shown that when prescribed for people with </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> it may cause </text><concept BabelNet=\"http://babelnet.org/rdf/s00075057n\" DBPedia=\"http://dbpedia.org/resource/Sudden_death_(sport)\" MetaMap=\"Finding\">sudden death</concept><text>. If a person with </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> must be prescribed a neuroleptic it should be done with the utmost care and under constant supervision with regular monitoring. In certain cases some people with </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text> are </text><concept BabelNet=\"http://babelnet.org/rdf/s00096151a\" DBPedia=\"\" MetaMap=\"Finding\">able</concept><text> to tolerate such treatment so that their hallucinations are reduced. There is now some evidence to suggest that the more recently developed '</text><concept BabelNet=\"http://babelnet.org/rdf/s00097741a\" DBPedia=\"\" MetaMap=\"Finding\">atypical</concept><text>' anti-psychotic dugs like olanzapine (Zyprexa), quetiapine (Seroquel) or respiridone (Risperdal), may be safe to use. It is still reasonable to try to simplify anti-parkinsonian medication as a first step, particularly withdrawing drugs of lower potency (and particular tendency to cause </text><concept BabelNet=\"http://babelnet.org/rdf/s00021789n\" DBPedia=\"\" MetaMap=\"SignOrSymptom\">confusion</concept><text>) such as anti-cholinergics and selegeline; where possible dopamine agonists should also be </text><concept BabelNet=\"http://babelnet.org/rdf/s00092941v\" DBPedia=\"\" MetaMap=\"Finding\">withdrawn</concept><text>, leaving most patients on levodopa </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"Finding\">alone</concept><text>. (Ince, P et al Copyright Brain Pathology 1998) At present there is no cure for </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text>. Recent research has suggested that the cholinesterase drugs </text><concept BabelNet=\"http://babelnet.org/rdf/s13783090v\" DBPedia=\"\" MetaMap=\"Finding\">used</concept><text> to treat </text><concept BabelNet=\"http://babelnet.org/rdf/s00003193n\" DBPedia=\"http://dbpedia.org/resource/Alzheimer's_disease\" MetaMap=\"DiseaseOrSyndrome\">Alzheimer's disease</concept><text> may also be useful in treating </text><concept BabelNet=\"\" DBPedia=\"\" MetaMap=\"DiseaseOrSyndrome\">DLB</concept><text>, although they are not yet licensed for this use (</text><concept BabelNet=\"http://babelnet.org/rdf/s00003193n\" DBPedia=\"http://dbpedia.org/resource/Alzheimer's_disease\" MetaMap=\"DiseaseOrSyndrome\">Alzheimer</concept><text> Scotland-Action on Dementia 2002). Ongoing research / Clinical trials</text></content>";
		//String NePosText = "<content> <DT>The</DT>  <JJ>main</JJ>  <NN>requirement</NN>  <VBZ>is</VBZ>  <JJ>progressive</JJ>  <JJ>cognitive</JJ>  <NN>decline</NN>  <IN>of</IN>  <JJ>sufficient</JJ>  <NN>magnitude</NN>  <NoPOS>to</NoPOS>  <VB>interfere</VB>  <IN>with</IN>  <JJ>normal</JJ>  <JJ>social</JJ>  <CC>or</CC>  <JJ>occupational</JJ>  <NN>function</NN>  <Punctuation>.</Punctuation>  <JJ>Prominent</JJ>  <CC>or</CC>  <JJ>persistent</JJ>  <NN>memory</NN>  <NN>impairment</NN>  <MD>may</MD>  <RB>not</RB>  <RB>necessarily</RB>  <VB>occur</VB>  <NoPOS>in</NoPOS>  <DT>the</DT>  <JJ>early</JJ>  <NNS>stages</NNS>  <CC>but</CC>  <VBZ>is</VBZ>  <RB>usually</RB>  <JJ>evident</JJ>  <IN>with</IN>  <NN>progression</NN>  <Punctuation>.</Punctuation>  <NNS>Deficits</NNS>  <IN>on</IN>  <NNS>tests</NNS>  <IN>of</IN>  <NN>attention</NN>  <CC>and</CC>  <JJ>frontal-sub-cortical</JJ>  <NNS>skills</NNS>  <CC>and</CC>  <JJ>visuospatial</JJ>  <NN>ability</NN>  <MD>may</MD>  <VB>be</VB>  <RB>especially</RB>  <JJ>prominent</JJ>  <Punctuation>.</Punctuation>  <DT>The</DT>  <JJ>main</JJ>  <JJ>diagnostic</JJ>  <NN>issue</NN>  <VBZ>is</VBZ>  <NoPOS>to</NoPOS>  <VB>distinguish</VB>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <IN>from</IN>  <DT>the</DT>  <NN>commoner</NN>  <NN>Alzheimer</NN>  <POS>'s</POS>  <NN>disease</NN>  <Punctuation>.</Punctuation>  <DT>A</DT>  <JJ>common</JJ>  <JJ>diagnostic</JJ>  <NN>error</NN>  <VBZ>is</VBZ>  <NoPOS>to</NoPOS>  <VB>attribute</VB>  <DT>the</DT>  <JJ>clinical</JJ>  <NNS>features</NNS>  <IN>of</IN>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <NoPOS>to</NoPOS>  <JJ>cerebral</JJ>  <JJ>vascular</JJ>  <NN>disease</NN>  <JJ>such</JJ>  <IN>as</IN>  <JJ>multi-infarct</JJ>  <NN>dementia</NN>  <CC>or</CC>  <ne type=\"PERSON\"><NNP>Binswanger</NNP> </ne>  <POS>'s</POS>  <NN>disease</NN>  <Punctuation>.</Punctuation>  <EX>There</EX>  <VBP>are</VBP>  <DT>no</DT>  <JJ>specific</JJ>  <JJ>diagnostic</JJ>  <NNS>tests</NNS>  <IN>for</IN>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <Punctuation>.</Punctuation>  <NN>CT</NN>  <CC>and</CC>  <NNP>MRI</NNP>  <NN>imaging</NN>  <MD>can</MD>  <VB>assist</VB>  <NoPOS>in</NoPOS>  <DT>the</DT>  <NN>process</NN>  <Punctuation>(</Punctuation>  <ne type=\"PERSON\"><NNP>Ince</NNP> </ne>  <Punctuation>,</Punctuation>  <NN>P</NN>  <FW>et</FW>  <FW>al</FW>  <NN>Copyright</NN>  <NN>Brain</NN>  <NN>Pathology</NN>  <ne type=\"DATE\"><CD>1998</CD> </ne>  <Punctuation>)</Punctuation>  <Punctuation>.</Punctuation>  <NNP>Care</NNP>  <CC>and</CC>  <NN>treatment</NN>  <IN>For</IN>  <NNS>people</NNS>  <IN>with</IN>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <NNS>neuroleptics</NNS>  <MD>may</MD>  <VB>be</VB>  <RB>particularly</RB>  <JJ>dangerous</JJ>  <Punctuation>.</Punctuation>  <DT>This</DT>  <NN>class</NN>  <IN>of</IN>  <NNS>drugs</NNS>  <VBP>induce</VBP>  <ne type=\"MISC\"><JJ>Parkinson-like</JJ> </ne>  <JJ>side</JJ>  <NNS>effects</NNS>  <Punctuation>,</Punctuation>  <VBG>including</VBG>  <NN>rigidity</NN>  <CC>and</CC>  <DT>an</DT>  <NN>inability</NN>  <NoPOS>to</NoPOS>  <VB>perform</VB>  <NNS>tasks</NNS>  <CC>or</CC>  <NoPOS>to</NoPOS>  <VB>communicate</VB>  <Punctuation>.</Punctuation>  <NNS>Studies</NNS>  <VBP>have</VBP>  <VBN>shown</VBN>  <IN>that</IN>  <WRB>when</WRB>  <VBN>prescribed</VBN>  <IN>for</IN>  <NNS>people</NNS>  <IN>with</IN>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <PRP>it</PRP>  <MD>may</MD>  <VB>cause</VB>  <JJ>sudden</JJ>  <NN>death</NN>  <Punctuation>.</Punctuation>  <IN>If</IN>  <DT>a</DT>  <NN>person</NN>  <IN>with</IN>  <ne type=\"ORGANIZATION\"><NN>DLB</NN> </ne>  <MD>must</MD>  <VB>be</VB>  <VBN>prescribed</VBN>  <DT>a</DT>  <JJ>neuroleptic</JJ>  <PRP>it</PRP>  <MD>should</MD>  <VB>be</VB>  <VBN>done</VBN>  <IN>with</IN>  <DT>the</DT>  <JJ>utmost</JJ>  <NN>care</NN>  <CC>and</CC>  <IN>under</IN>  <JJ>constant</JJ>  <NN>supervision</NN>  <IN>with</IN>  <JJ>regular</JJ>  <NN>monitoring</NN>  <Punctuation>.</Punctuation>  <IN>In</IN>  <JJ>certain</JJ>  <NNS>cases</NNS>  <DT>some</DT>  <NNS>people</NNS>  <IN>with</IN>  <ne type=\"ORGANIZATION\"><NN>DLB</NN> </ne>  <VBP>are</VBP>  <JJ>able</JJ>  <NoPOS>to</NoPOS>  <VB>tolerate</VB>  <JJ>such</JJ>  <NN>treatment</NN>  <IN>so</IN>  <IN>that</IN>  <PRP>their</PRP>  <NNS>hallucinations</NNS>  <VBP>are</VBP>  <VBN>reduced</VBN>  <Punctuation>.</Punctuation>  <EX>There</EX>  <VBZ>is</VBZ>  <ne type=\"DATE\"><RB>now</RB> </ne>  <DT>some</DT>  <NN>evidence</NN>  <NoPOS>to</NoPOS>  <VB>suggest</VB>  <IN>that</IN>  <DT>the</DT>  <RBR>more</RBR>  <ne type=\"DATE\"><RB>recently</RB> </ne>  <VBN>developed</VBN>  <Punctuation>`</Punctuation>  <JJ>atypical</JJ>  <Punctuation>'</Punctuation>  <JJ>anti-psychotic</JJ>  <NNS>dugs</NNS>  <IN>like</IN>  <NN>olanzapine</NN>  <Punctuation>(</Punctuation>  <NN>Zyprexa</NN>  <Punctuation>)</Punctuation>  <Punctuation>,</Punctuation>  <NN>quetiapine</NN>  <Punctuation>(</Punctuation>  <ne type=\"MISC\"><NN>Seroquel</NN> </ne>  <Punctuation>)</Punctuation>  <CC>or</CC>  <NN>respiridone</NN>  <Punctuation>(</Punctuation>  <ne type=\"MISC\"><NN>Risperdal</NN> </ne>  <Punctuation>)</Punctuation>  <Punctuation>,</Punctuation>  <MD>may</MD>  <VB>be</VB>  <JJ>safe</JJ>  <NoPOS>to</NoPOS>  <VB>use</VB>  <Punctuation>.</Punctuation>  <PRP>It</PRP>  <VBZ>is</VBZ>  <RB>still</RB>  <JJ>reasonable</JJ>  <NoPOS>to</NoPOS>  <VB>try</VB>  <NoPOS>to</NoPOS>  <VB>simplify</VB>  <JJ>anti-parkinsonian</JJ>  <NN>medication</NN>  <IN>as</IN>  <DT>a</DT>  <ne type=\"ORDINAL\"><JJ>first</JJ> </ne>  <NN>step</NN>  <Punctuation>,</Punctuation>  <RB>particularly</RB>  <VBG>withdrawing</VBG>  <NNS>drugs</NNS>  <IN>of</IN>  <JJR>lower</JJR>  <NN>potency</NN>  <Punctuation>(</Punctuation>  <CC>and</CC>  <JJ>particular</JJ>  <NN>tendency</NN>  <NoPOS>to</NoPOS>  <VB>cause</VB>  <NN>confusion</NN>  <Punctuation>)</Punctuation>  <JJ>such</JJ>  <IN>as</IN>  <NNS>anti-cholinergics</NNS>  <CC>and</CC>  <NN>selegeline</NN>  <Punctuation>;</Punctuation>  <WRB>where</WRB>  <JJ>possible</JJ>  <NN>dopamine</NN>  <NNS>agonists</NNS>  <MD>should</MD>  <RB>also</RB>  <VB>be</VB>  <VBN>withdrawn</VBN>  <Punctuation>,</Punctuation>  <VBG>leaving</VBG>  <RBS>most</RBS>  <NNS>patients</NNS>  <IN>on</IN>  <NN>levodopa</NN>  <RB>alone</RB>  <Punctuation>.</Punctuation>  <Punctuation>(</Punctuation>  <ne type=\"PERSON\"><NNP>Ince</NNP> </ne>  <Punctuation>,</Punctuation>  <NN>P</NN>  <FW>et</FW>  <FW>al</FW>  <NN>Copyright</NN>  <NN>Brain</NN>  <NN>Pathology</NN>  <ne type=\"DATE\"><CD>1998</CD> </ne>  <Punctuation>)</Punctuation>  <IN>At</IN>  <ne type=\"DATE\"><JJ>present</JJ> </ne>  <EX>there</EX>  <VBZ>is</VBZ>  <DT>no</DT>  <NN>cure</NN>  <IN>for</IN>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <Punctuation>.</Punctuation>  <JJ>Recent</JJ>  <NN>research</NN>  <VBZ>has</VBZ>  <VBN>suggested</VBN>  <IN>that</IN>  <DT>the</DT>  <NN>cholinesterase</NN>  <NNS>drugs</NNS>  <VBN>used</VBN>  <NoPOS>to</NoPOS>  <VB>treat</VB>  <NNP>Alzheimer</NNP>  <POS>'s</POS>  <NN>disease</NN>  <MD>may</MD>  <RB>also</RB>  <VB>be</VB>  <JJ>useful</JJ>  <NoPOS>in</NoPOS>  <VBG>treating</VBG>  <ne type=\"ORGANIZATION\"><NNP>DLB</NNP> </ne>  <Punctuation>,</Punctuation>  <IN>although</IN>  <PRP>they</PRP>  <VBP>are</VBP>  <RB>not</RB>  <RB>yet</RB>  <VBN>licensed</VBN>  <IN>for</IN>  <DT>this</DT>  <NN>use</NN>  <Punctuation>(</Punctuation>  <NN>Alzheimer</NN>  <NN>Scotland-Action</NN>  <IN>on</IN>  <NNP>Dementia</NNP>  <ne type=\"DATE\"><CD>2002</CD> </ne>  <Punctuation>)</Punctuation>  <Punctuation>.</Punctuation>  <JJ>Ongoing</JJ>  <NN>research</NN>  <Punctuation>/</Punctuation>  <JJ>Clinical</JJ>  <NNS>trials</NNS>  </content>";

		if (debugMode) System.out.println("Merging Concept text with coreNLP output");

		//String mergedText = merge_ConceptXML_with_NerPosXML(ConText, NePosText);
		String mergedText = merge_ConceptXML_with_NerPosXML(LocalMetaMapText, NePosText);

		//System.out.println("Final text");
		//System.out.println(mergedText);

		return mergedText;
	}

	/**
	 * TODO write javadoc
	 * @param ConText
	 * @param NePosText
	 * @return
	 */
	private static String merge_ConceptXML_with_NerPosXML(String ConText, String NePosText){

		CustomXMLRepresentation NePosXML = new CustomXMLRepresentation(NePosText);

		String output = "";

		Document docConcept = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream streamConcept = new ByteArrayInputStream(ConText.getBytes(StandardCharsets.UTF_8));
			docConcept = docBuilder.parse(streamConcept);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: Pipeline.merge_ConceptXML_with_NerPosXML() Could not create a document text.");
			e.printStackTrace();
		}

		NodeList ctSegments = docConcept.getElementsByTagName("content").item(0).getChildNodes(); // concept or text segmets (children of content)
		for (int i = 0; i < ctSegments.getLength(); i++) {
			Element segment = (Element) ctSegments.item(i);

			boolean isConcept = (segment.getNodeName().equals("concept")) ? true : false;
			if (isConcept){
				output += "<concept";
				NamedNodeMap attributes = segment.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					output += " " + attributes.item(j).getNodeName() + "=\"" + attributes.item(j).getNodeValue() + "\"";
				}
				output += ">";
			}

			String textSeg= getCorrespondingNEPosSegment(segment.getTextContent(), NePosXML, isConcept);
			if (textSeg.startsWith("--ERROR--")){
				return textSeg;
			}
			else{
				output +=textSeg;
			}

			if (isConcept){
				output += "</concept>";
			}

			/*System.out.println("----------------------------------");
			System.out.println(output);*/
		}

		output = output.replaceAll("([a-zA-Z])&([a-zA-Z])","$1&amp;$2");

		return output;
	}

	/**
	 * Gets a segment of text and an XML with NE and POS tags, and extracts the segment of XML corresponding to the provided text.
	 * Also, removes the extracted XML segment from nePosText.
	 * ??? Text segment must be in the beginning of the XML. ???
	 *
	 * @param textConcept - Segment of text
	 * @param nePosText - NE and POS XML
	 * @param isConcept
	 * @return
	 */
	private static String getCorrespondingNEPosSegment(String textConcept, CustomXMLRepresentation nePosText, boolean isConcept) {

		String output = "";

		//textConcept = textConcept.replaceAll("\\p{Punct} "," $0 ");
		/*textConcept = textConcept.replaceAll("[.]",". ").replaceAll("[,]",", ").replaceAll("[']","' ")
				.replaceAll("[\\[]"," [ " ).replaceAll("[\\]]"," ] ").replaceAll("[(]", " ( ").replaceAll("[)]"," ) ")
				.replaceAll("[<]", " < ").replaceAll("[>]", " > ");*/
		//textConcept = textConcept.replaceAll("[\\.,']","$0 ").replaceAll("[\\[\\](){}!@#$%^&*+=]"," $0 ");
		textConcept = textConcept.replaceAll("[']","$0 ").replaceAll("[\\[\\](){}!@#$%^&*+=]"," $0 ");
		String[] lemmas = textConcept.split(" ");
		ArrayList<String> wordList = new ArrayList(Arrays.asList(lemmas));

		nePosText.escapeXMLCharacters();

		boolean goOn = true;
		//while wordList is not empty, repeat
		while(wordList.size()>0 && goOn){

			Document docNePos = null;
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				InputStream streamNePos = new ByteArrayInputStream(nePosText.getXml().getBytes(StandardCharsets.UTF_8));
				docNePos = docBuilder.parse(streamNePos);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				System.err.print("KIndex :: Pipeline.getCorrespondingNEPosSegment() Could not create a document text.");
				e.printStackTrace();
			}

			NodeList npSegments = docNePos.getElementsByTagName("content").item(0).getChildNodes(); // concept or text segments (children of content)

			for (int i = 0; i < npSegments.getLength(); i++) {
				Node n = npSegments.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element segment = (Element) n;
					String tag = segment.getNodeName();
					String stLemma = (segment.hasAttribute("lemma")) ? segment.getAttribute("lemma") : "";
					String lemma = segment.getTextContent();

					//Debug
					//if (wordList.get(0).equals("take")){
					//	System.out.println("take!");
					//}

					if ((wordList.size() == 0) && (tag.equals("Punctuation"))){
						output += lemma + " " ;
						nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
						break;
					}

					int initSize = wordList.size();
					int initXMLSize = nePosText.getXml().length();
					if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").equals("")){
						wordList.remove(0);
						break;
					}
					if (tag.equals("ne")){
						String NEtype = segment.getAttribute("type");
						NodeList NEchildren = segment.getChildNodes();
						//if this text segment is concept, do not add the NamedEntity tag.
						String NEstring = (isConcept) ? "" : "<ne type=\"" + NEtype + "\">";
						for (int c = 0; c < NEchildren.getLength(); c++) {
							Node child = NEchildren.item(c);
							if (child.getNodeType() == Node.ELEMENT_NODE) {
								Element Echild = (Element) child;
								String Ctag = Echild.getNodeName();
								String OrigLemma = Echild.getAttribute("lemma");
								String Clemma = Echild.getTextContent();
								if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().equals(Clemma.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","").toLowerCase())) {
									NEstring += "<" + Ctag + " lemma=\""+OrigLemma+"\">" + Clemma + "</" + Ctag + "> ";
									wordList.remove(0);
								}
								else if (!Clemma.equals("")){
									NEstring += "<" + Ctag + " lemma=\""+OrigLemma+"\">" + Clemma + "</" + Ctag + "> ";
									if (wordList.get(0).contains(Clemma)){
										wordList.set(0,wordList.get(0).replace(Clemma,""));
									}
									else if (Clemma.contains(wordList.get(0))){
										String ClemmaRemaining = Clemma.replaceAll("[^\\x00-\\x7F]", ""); //replace all non-ascii characters
										while (Clemma.contains(wordList.get(0))){
											ClemmaRemaining = ClemmaRemaining.replace(wordList.get(0)," ");
											wordList.remove(0);
											if (Clemma.endsWith(wordList.get(0))){
												break;
											}
										}
										ClemmaRemaining = (ClemmaRemaining.startsWith(" ")) ? ClemmaRemaining.replace(" ","") : ClemmaRemaining;
										if ((!ClemmaRemaining.equals("")) && wordList.get(0).startsWith(ClemmaRemaining)){
											wordList.set(0, wordList.get(0).replace(ClemmaRemaining,""));
										}
									}
								}
								/*else if (Clemma.contains(wordList.get(0))){
									wordList.remove(0);
									if (Clemma.endsWith(wordList.get(0))) {
										NEstring += "<" + Ctag + " lemma=\"" + OrigLemma + "\">" + Clemma + "</" + Ctag + "> ";
									}
								}*/
								/*else if ((!Clemma.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","").toLowerCase().equals("")) &&
										wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().contains(Clemma.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","").toLowerCase())){
									NEstring += "<" + Ctag + " lemma=\""+OrigLemma+"\">" + Clemma + "</" + Ctag + "> ";
									wordList.set(0,wordList.get(0).replace(OrigLemma,""));
								}*/
							}
						}
						NEstring += (isConcept) ? "" : "</ne>";
						output += NEstring;
						NEstring = (isConcept) ? "<ne type=\"" + NEtype + "\">" + NEstring + "</ne>" : NEstring;
						nePosText.removeElement(NEstring);

						//avoid infinite loop
						if (wordList.size() == initSize && NEstring.length() == initXMLSize){
							return "--ERROR-- \r\n Error: 1200 \r\n In word:" + wordList.get(0) + ", lemma:"+lemma;
						}
						break;
					}
					else if(tag.equals("NoPOS") || tag.equals("Punctuation")){
						//output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
						output += lemma + " " ;
						nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
						if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().equals(lemma.toLowerCase())){
							wordList.remove(0);
						}

						//avoid infinite loop
						else if (nePosText.getXml().length() == initXMLSize){
							return "--ERROR-- \r\n Error: 1201 \r\n In word:" + wordList.get(0) + ", lemma:"+lemma +"\r\n nePosText: "+ nePosText.getXml();
						}
						break;
					}
					else if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().equals(lemma.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase())) {
						output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
						wordList.remove(0);
						nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
						break;
					}
					//we have the example of "Cannot" -> <MD>Can</MD><RB>not</RB>
					//in that case, in the first loop the first lemma will be added
					//in second loop the lemma will be added and wordList.get(0) will be removed
					else if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().contains(lemma.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase())){
						if (wordList.get(0).startsWith(lemma)){
							output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
							wordList.set(0,wordList.get(0).replaceFirst(Pattern.quote(lemma),""));
							nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
							break;
						}
						else if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().startsWith(lemma.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase())){
							output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
							wordList.set(0,wordList.get(0).replaceFirst(lemma.replaceAll("[^a-zA-Z0-9 ]", ""),""));
							nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
							break;
						}
						else if (wordList.get(0).endsWith(lemma)){
							output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
							wordList.remove(0);
							nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
							break;
						}
						else if (wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().endsWith(lemma.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase())){
							output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
							wordList.remove(0);
							nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
							break;
						}
					}
					else if (lemma.contains(wordList.get(0))){
						while(lemma.contains(wordList.get(0))){
							if (lemma.endsWith(wordList.get(0))) {
								wordList.remove(0);
								break;
							}
							else{
								wordList.remove(0);
							}

						}
						output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
						nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
						if (wordList.size() == 0){
							break;
						}
					}
					else if (lemma.contains(wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", ""))){
						while(lemma.contains(wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", ""))){
							if (lemma.endsWith(wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", ""))) {
								wordList.remove(0);
								break;
							}
							else{
								wordList.remove(0);
							}

						}
						output += "<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + "> ";
						nePosText.removeElement("<" + tag + " lemma=\""+stLemma+"\">" + lemma + "</" + tag + ">");
						if (wordList.size() == 0){
							break;
						}
					}

					//avoid infinite loop
					if (wordList.size() == initSize){
						return "--ERROR-- \r\n Error: 1202 \r\n In word:" + wordList.get(0) + ", lemma:"+lemma;
					}
				}
				else{ //if n.getNodeType() != Node.ELEMENT_NODE
					if (npSegments.getLength() == 1){
						goOn = false;
						break;
					}
				}
			}

			if (wordList.size()>0 && wordList.get(0).replaceAll("[^a-zA-Z0-9 ]", "").equals("")){
				wordList.remove(0);
			}

			if(npSegments.getLength() == 0 && wordList.size() == 1){
				wordList.remove(0);
			}
		}

		//in case wordList is empty but the next element in nePosText is punctuation, this element has to be added in output
		Document docNePos = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream streamNePos = new ByteArrayInputStream(nePosText.getXml().getBytes(StandardCharsets.UTF_8));
			docNePos = docBuilder.parse(streamNePos);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.print("KIndex :: Pipeline.getCorrespondingNEPosSegment() Could not create a document text.");
			e.printStackTrace();
		}

		NodeList npSegments = docNePos.getElementsByTagName("content").item(0).getChildNodes(); // concept or text segments (children of content)
		for (int i = 0; i < npSegments.getLength(); i++) {
			Node n = npSegments.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element segment = (Element) n;
				String tag = segment.getNodeName();
				String lemma = segment.getTextContent();
				if (tag.equals("Punctuation")){
					output += lemma + " ";
					nePosText.removeElement("<" + tag + " lemma=\""+lemma+"\">" + lemma + "</" + tag + ">");
				}
				break; // it will break after the first time an element will be checked
			}
		}



		return output;
	}


}















