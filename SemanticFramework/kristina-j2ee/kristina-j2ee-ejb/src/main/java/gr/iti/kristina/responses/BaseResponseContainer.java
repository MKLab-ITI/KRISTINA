package gr.iti.kristina.responses;

import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

public class BaseResponseContainer {

	public String base = "http://response-data";
	public OntModel model= Utils.createDefaultModel(false);
	
	public Ontology ont;
	
	public OntModel _m;
	public OntClass ResponseContainer;
	public OntProperty containsResponse;
	public OntProperty timestamp;
	public OntProperty conversationalContext;
	
	public OntProperty plausibility;
	public OntProperty rank;
	public OntProperty responseType;
	public OntProperty source;
	public Property rdf ;
	public OntProperty text ;
	public OntProperty url ;
	
	public OntClass Context;
	
	public Individual responseContainerInd;
	
	public BaseResponseContainer() {
		_m = Utils.createDefaultModel(false, OntModelSpec.OWL_DL_MEM);
		ResponseContainer = _m.createClass(Namespaces.RESPONSE + "ResponseContainer");
		containsResponse = _m.createObjectProperty(Namespaces.RESPONSE + "containsResponse");
		timestamp = _m.createDatatypeProperty(Namespaces.RESPONSE + "timestamp");
		conversationalContext = _m.createObjectProperty(Namespaces.RESPONSE + "conversationalContext");
		
		plausibility = _m.createDatatypeProperty(Namespaces.RESPONSE + "plausibility");
		rank = _m.createDatatypeProperty(Namespaces.RESPONSE + "rank");
		source = _m.createDatatypeProperty(Namespaces.RESPONSE + "source");
		responseType = _m.createObjectProperty(Namespaces.RESPONSE + "responseType");
		rdf = _m.createProperty(Namespaces.RESPONSE + "rdf");
		text = _m.createDatatypeProperty(Namespaces.RESPONSE + "text");
		url = _m.createDatatypeProperty(Namespaces.RESPONSE + "url");
		
		Context = _m.createClass(Namespaces.KB_CONTEXT + "Context");
		
		responseContainerInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
				ResponseContainer);
		
		model.setDynamicImports(false);

		ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

//	protected void generateResponseContainer(OntModel model, List<Individual> responses) {
//		Individual responseContainerInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
//				ResponseContainer);
//		for (Individual i : responses) {
//			responseContainerInd.addProperty(containsResponse, i);
//		}
//
//		responseContainerInd.addProperty(timestamp, model.createTypedLiteral(new DateTime().toGregorianCalendar()));
//		CurrentContextItem lastContext = currentContextBean.getLastContext();
//		if (lastContext != null) {
//			OntResource topicInstance = model.createOntResource(lastContext.getTopicInstance());
//			responseContainerInd.addProperty(conversationalContext, topicInstance);
//			Collection<Resource> topics = lastContext.getTopics();
//			for (Resource r : topics) {
//				model.add(topicInstance, RDF.type, r);
//			}
//		}
//	}
}
