package gr.iti.kristina.responses;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.LinkedHashMultimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;

import gr.iti.kristina.context.CurrentContextBean;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

public class WeatherResponseGenerator extends ResponseGenerator {

	OntModel model;
	LinkedHashMultimap<String, String> result;

	String[] fields = { "temperature", "windSpeed", "windDirection", "skyCondition", "pressure", "humidity" };
	XSDDatatype[] datatypes = { XSDDatatype.XSDinteger, XSDDatatype.XSDinteger, XSDDatatype.XSDstring,
			XSDDatatype.XSDstring, XSDDatatype.XSDinteger, XSDDatatype.XSDinteger };

	public WeatherResponseGenerator(CurrentContextBean currentContextBean, LinkedHashMultimap<String, String> result) {
		super(currentContextBean, "free_text");
		this.result = result;
		model = Utils.createDefaultModel(false);
		model.addSubModel(baseModel);
		model.setDynamicImports(false);

		Ontology ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

	public OntModel generate() throws UnsupportedEncodingException {

		OntClass WeatherResponse = _m.createClass(Namespaces.RESPONSE + "WeatherResponse");
		OntProperty plausibility = _m.createDatatypeProperty(Namespaces.RESPONSE + "plausibility");
		OntProperty responseType = _m.createObjectProperty(Namespaces.RESPONSE + "responseType");
		OntProperty text = _m.createDatatypeProperty(Namespaces.RESPONSE + "text");
		OntResource responseTypeValue = _m.createOntResource(Namespaces.RESPONSE + super.responseType);

		List<Individual> responses = new ArrayList<>();

		OntModel temp = Utils.createDefaultModel(false, OntModelSpec.OWL_DL_MEM);
		Ontology ont = temp.createOntology(base);
		ont.addImport(temp.createResource(StringUtils.removeEnd(Namespaces.WEATHER, "#")));

		Individual weatherResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(), WeatherResponse);
		weatherResponseInd.addProperty(plausibility, model.createTypedLiteral(1.0));
		weatherResponseInd.addProperty(responseType, responseTypeValue);

		OntClass Forecast = _m.createClass(Namespaces.WEATHER + "Forecast");
		Individual forecastInd = temp.createIndividual(Utils.tempURI() + Utils.randomString(), Forecast);
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			Set<String> values = result.get(field);
			if (!values.isEmpty()) {
				DatatypeProperty p = _m.createDatatypeProperty(Namespaces.WEATHER + field);
				Literal v = temp.createTypedLiteral(values.toArray()[0], datatypes[i]);
				forecastInd.addProperty(p, v);
			}
		}

		// statementResponseInd.addProperty(text,
		// URLEncoder.encode(responseText, "UTF-8"));
		weatherResponseInd.addProperty(text, Utils.modelToString(temp, "TTL"));
		responses.add(weatherResponseInd);

		super.generateResponseContainer(model, responses);
		return model;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		// WeatherResponseGenerator g = new WeatherResponseGenerator(null, null,
		// null, null);
		// OntModel model2 = g.generate();
		// System.out.println(Utils.modelToString(model2, "TTL"));
	}

}
