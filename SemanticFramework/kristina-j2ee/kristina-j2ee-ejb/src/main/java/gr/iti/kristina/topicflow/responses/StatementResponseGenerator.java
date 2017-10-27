package gr.iti.kristina.topicflow.responses;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import gr.iti.kristina.context.CurrentContextBean;
import gr.iti.kristina.model.ContextCluster;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.model.Triple;
import gr.iti.kristina.startup.SituationsBean;
import gr.iti.kristina.startup.SituationsBean.Result;
import gr.iti.kristina.utils.Utils;

public class StatementResponseGenerator extends ResponseGenerator {
	OntModel model;

	private List<Result> result;
	private TreeSet<ContextCluster> result2;
	private String responseText;

	// TODO we may have more than one result...
	// public StatementResponseGenerator(CurrentContextBean currentContextBean,
	// TreeSet<ContextCluster> result,
	// String text_response, String responseType) {
	// super(currentContextBean, responseType);
	// this.result = result;
	// this.responseText = text_response;
	// model = Utils.createDefaultModel(false);
	// model.addSubModel(baseModel);
	// model.setDynamicImports(false);
	//
	// Ontology ont = model.createOntology(base);
	// ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE,
	// "#")));
	// }

	public StatementResponseGenerator(CurrentContextBean currentContextBean, List<Result> result, String text_response,
			String responseType) {
		super(currentContextBean, responseType);
		this.result = result;
		this.responseText = text_response;
		model = Utils.createDefaultModel(false);
		model.addSubModel(baseModel);
		model.setDynamicImports(false);

		Ontology ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

	public StatementResponseGenerator(CurrentContextBean currentContextBean, TreeSet<ContextCluster> result,
			String text_response, String responseType) {
		super(currentContextBean, responseType);
		this.result2 = result;
		this.responseText = text_response;
		model = Utils.createDefaultModel(false);
		model.addSubModel(baseModel);
		model.setDynamicImports(false);

		Ontology ont = model.createOntology(base);
		ont.addImport(model.createResource(StringUtils.removeEnd(Namespaces.RESPONSE, "#")));
	}

	public OntModel generate() throws UnsupportedEncodingException {
		OntClass StatementResponse = _m.createClass(Namespaces.RESPONSE + "StatementResponse");
		OntProperty plausibility = _m.createDatatypeProperty(Namespaces.RESPONSE + "plausibility");
		OntProperty rank = _m.createDatatypeProperty(Namespaces.RESPONSE + "rank");
		OntProperty responseType = _m.createObjectProperty(Namespaces.RESPONSE + "responseType");
		Property rdf = _m.createProperty(Namespaces.RESPONSE + "rdf");
		OntProperty text = _m.createDatatypeProperty(Namespaces.RESPONSE + "text");
		OntResource responseTypeValue = _m.createOntResource(Namespaces.RESPONSE + super.responseType);

		List<Individual> responses = new ArrayList<>();

		if (super.responseType.equals("structured")) {
			if (result2 != null) {
				for (ContextCluster cc : result2) {
					Individual statementResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
							StatementResponse);
					statementResponseInd.addProperty(plausibility, model.createTypedLiteral(cc.getRank()));
					statementResponseInd.addProperty(responseType, responseTypeValue);

					List<ReifiedStatement> statements = createRDFStatements(cc.getTriples());
					for (ReifiedStatement s : statements) {
						statementResponseInd.addProperty(rdf, s);
					}
					responses.add(statementResponseInd);
				}
			} else {
				for (Result cc : result) {
					Individual statementResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
							StatementResponse);
					statementResponseInd.addProperty(plausibility, model.createTypedLiteral(cc.score));
					statementResponseInd.addProperty(rank, model.createTypedLiteral(cc.order));
					statementResponseInd.addProperty(responseType, responseTypeValue);

					List<ReifiedStatement> statements = createRDFStatements(
							SituationsBean.cache_patterns.get(cc.situation));
					for (ReifiedStatement s : statements) {
						statementResponseInd.addProperty(rdf, s);
					}
					responses.add(statementResponseInd);
				}
			}
		} else if (super.responseType.equals("free_text")) {
			Individual statementResponseInd = model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			statementResponseInd.addProperty(plausibility, model.createTypedLiteral(1.0));
			statementResponseInd.addProperty(responseType, responseTypeValue);
			// statementResponseInd.addProperty(text,
			// URLEncoder.encode(responseText, "UTF-8"));
			statementResponseInd.addProperty(text, responseText);
			responses.add(statementResponseInd);
		} else {
			throw new UnsupportedOperationException("No valid response type has been provided");
		}

		super.generateResponseContainer(model, responses);
		return model;
	}

	private List<ReifiedStatement> createRDFStatements(Set<Triple> triples) {
		List<ReifiedStatement> results = new ArrayList<>();
		for (Triple t : triples) {
			Resource s;
			Property p;
			RDFNode o;
			if (t.s.startsWith("http")) {
				s = model.createResource(t.s);
			} else {
				throw new UnsupportedOperationException();
			}
			p = model.createProperty(t.p);
			if (t.o.startsWith("http")) {
				o = model.createResource(t.o);
			} else {
				o = model.createLiteral(t.o);
			}

			Statement statement = model.createStatement(s, p, o);
			ReifiedStatement reifiedStatement = statement.createReifiedStatement(Utils.tempFullURI());
			results.add(reifiedStatement);

		}

		return results;

	}

	// public static void main(String[] args) throws
	// UnsupportedEncodingException {
	// StatementResponseGenerator g = new StatementResponseGenerator(null, null,
	// null, null);
	// OntModel model2 = g.generate();
	// System.out.println(Utils.modelToString(model2, "TTL"));
	// }

}
