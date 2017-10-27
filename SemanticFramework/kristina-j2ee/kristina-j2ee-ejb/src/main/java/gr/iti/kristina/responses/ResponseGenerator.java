package gr.iti.kristina.responses;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import gr.iti.kristina.context.CurrentContextBean;
import gr.iti.kristina.model.CurrentContextItem;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

public class ResponseGenerator {
//	public static enum RESPONSE_TYPE {
//		REQUEST, STATEMENT, UNKNOWN
//	}
//	
	protected String base = "http://response-data";

	//public static final String RESPONSE_URI = "http://kristina-project.eu/ontologies/responses#";
	// public String RESPONSE_ONTOLOGY = "response.ttl";

	public OntModel baseModel;
	// protected HashMap<OntResource, Collection<Resource>> topic;
	protected String responseType;
	private CurrentContextBean currentContextBean;
	
	//for resource creation
	OntModel _m = Utils.createDefaultModel(false, OntModelSpec.OWL_DL_MEM);

	public ResponseGenerator(CurrentContextBean currentContextBean, String responseType) {
		// this.topic = topic;
		this.currentContextBean = currentContextBean;
		this.responseType = responseType;
		baseModel = Utils.createDefaultModel(false);
		// InputStream file =
		// UnknownResponseGenerator.class.getResourceAsStream("/" +
		// RESPONSE_ONTOLOGY);
		// baseModel.read(file, "http://base#", "TURTLE");

	}

	protected void generateResponseContainer(OntModel model, List<Individual> responses) {
		OntClass ResponseContainer = _m.createClass(Namespaces.RESPONSE + "ResponseContainer");
		OntProperty containsResponse = _m.createObjectProperty(Namespaces.RESPONSE + "containsResponse");
		OntProperty timestamp = _m.createDatatypeProperty(Namespaces.RESPONSE + "timestamp");
		OntProperty conversationalContext = _m.createObjectProperty(Namespaces.RESPONSE + "conversationalContext");

		Individual responseContainerInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
				ResponseContainer);
		for (Individual i : responses) {
			responseContainerInd.addProperty(containsResponse, i);
		}

		responseContainerInd.addProperty(timestamp, model.createTypedLiteral(new DateTime().toGregorianCalendar()));
		CurrentContextItem lastContext = currentContextBean.getLastContext();
		if (lastContext != null) {
			OntResource topicInstance = model.createOntResource(lastContext.getTopicInstance());
			responseContainerInd.addProperty(conversationalContext, topicInstance);
			Collection<Resource> topics = lastContext.getTopics();
			for (Resource r : topics) {
				model.add(topicInstance, RDF.type, r);
			}
		}
	}
}
