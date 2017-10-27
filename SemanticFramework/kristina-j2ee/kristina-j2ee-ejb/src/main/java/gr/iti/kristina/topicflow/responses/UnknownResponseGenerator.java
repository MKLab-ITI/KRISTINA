package gr.iti.kristina.topicflow.responses;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;

import gr.iti.kristina.context.CurrentContextBean;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

public class UnknownResponseGenerator extends ResponseGenerator {
	OntModel model;

	String base = "http://response-data";

	public UnknownResponseGenerator(CurrentContextBean currentContextBean, String responseType) {
		super(currentContextBean, responseType);
		model = Utils.createDefaultModel(false);
		model.addSubModel(baseModel);
		model.setDynamicImports(false);

		Ontology ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

	public OntModel generate() {
		OntClass NoResponse = _m.createClass(Namespaces.RESPONSE + "UnknownResponse");
		OntProperty plausibility = _m.createDatatypeProperty(Namespaces.RESPONSE + "plausibility");
		OntProperty responseType = _m.createObjectProperty(Namespaces.RESPONSE + "responseType");
		OntResource structured = _m.createOntResource(Namespaces.RESPONSE + "structured");

		Individual noResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		noResponseInd.addProperty(plausibility, model.createTypedLiteral(1.0));
		noResponseInd.addProperty(responseType, structured);
		super.generateResponseContainer(model, Collections.singletonList(noResponseInd));
		return model;
	}

	public static void main(String[] args) {
		CurrentContextBean c = new CurrentContextBean();
		UnknownResponseGenerator g = new UnknownResponseGenerator(c, "structured");
		System.out.println(Utils.modelToString(g.generate(), "TTL"));
	}

}
