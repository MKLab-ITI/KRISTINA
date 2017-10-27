package gr.iti.kristina.parsers;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Statement;

import gr.iti.kristina.model.KeyEntityWrapper;
import gr.iti.kristina.model.Namespaces;

public class DULParser {

	public DULParser.Result defines_classifies_path_jena(OntResource description, OntModel model) {
		OntProperty defines = model.getOntProperty(Namespaces.LA_CONTEXT + "defines");
		OntProperty classifies = model.getOntProperty(Namespaces.LA_CONTEXT + "classifies");

		Result result = new Result();
		// get all the concepts defined by the description
		List<Statement> dulConcepts = description.listProperties(defines).toList();
		// for each concept...
		for (Statement dc : dulConcepts) {
			OntResource dulConcept = dc.getObject().as(OntResource.class);
			// System.out.println("swswswswswswswswsswswswswsw: " + dulConcept);
			// get the concept type
			OntResource dulConceptType = dulConcept.getRDFType(true).as(OntResource.class);
			//System.out.println(dulConceptType);
			KeyEntityWrapper e1 = new KeyEntityWrapper(dulConcept, dulConceptType, null);

			// get the classified resource type
			OntResource classifiedResource = dulConcept.getPropertyResourceValue(classifies).as(OntResource.class);
			OntResource classifiedResourceType = classifiedResource.getRDFType(true).as(OntResource.class);
			//System.err.println(classifiedResourceType);
			KeyEntityWrapper e2 = new KeyEntityWrapper(classifiedResource, classifiedResourceType, null);

			result.conceptTypes.add(e1);
			result.classificationTypes.add(e2);
		}
		return result;

	}

	final protected class Result {
		List<KeyEntityWrapper> conceptTypes;
		List<KeyEntityWrapper> classificationTypes;

		public Result() {
			conceptTypes = new ArrayList<>();
			classificationTypes = new ArrayList<>();
		}

		void print() {
			System.err.println("concept types: ");
			System.err.println(conceptTypes);
			System.err.println("classification types: ");
			System.err.println(classificationTypes);
		}

	}

}
