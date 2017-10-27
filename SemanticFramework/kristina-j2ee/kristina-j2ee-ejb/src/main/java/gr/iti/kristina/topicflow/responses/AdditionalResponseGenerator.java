package gr.iti.kristina.topicflow.responses;

import java.util.Collections;

import javax.naming.NamingException;

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

public class AdditionalResponseGenerator extends ResponseGenerator {
	OntModel model;

	public AdditionalResponseGenerator(CurrentContextBean currentContextBean, String responseType) {
		super(currentContextBean, responseType);
		model = Utils.createDefaultModel(false);
		model.addSubModel(baseModel);
		model.setDynamicImports(false);

		Ontology ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

	public OntModel generate() {
		OntClass AdditionalResponse = _m.createClass(Namespaces.RESPONSE + "AdditionalInformationRequest");
		OntProperty plausibility = _m.createOntProperty(Namespaces.RESPONSE + "plausibility");
		OntProperty responseType = _m.createOntProperty(Namespaces.RESPONSE + "responseType");
		OntResource structured = _m.createOntResource(Namespaces.RESPONSE + "structured");

		Individual additionalResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
				AdditionalResponse);
		additionalResponseInd.addProperty(plausibility, model.createTypedLiteral(1.0));
		additionalResponseInd.addProperty(responseType, structured);
		super.generateResponseContainer(model, Collections.singletonList(additionalResponseInd));
		return model;
	}

	public static void main(String[] args) throws NamingException {
		CurrentContextBean c = new CurrentContextBean();
		AdditionalResponseGenerator g = new AdditionalResponseGenerator(c, null);
		System.out.println(Utils.modelToString(g.generate(), "TTL"));
	}

}
