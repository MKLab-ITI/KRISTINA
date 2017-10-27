package gr.iti.kristina.parsers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.io.FileUtils;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.openjena.atlas.io.PrintUtils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import gr.iti.kristina.model.KeyEntityWrapper;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.parsers.DULParser.Result;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class LAParser
 */
@Stateless
@LocalBean
public class LAParser {

	// final String _folder =
	// "file:///C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/"
	// + "stamatia/examples1/";
	final String[] _imports = { "action.ttl", "context.ttl", "onto.ttl", "dialogue_actions.ttl" };

	// public static final String ACTION_NS =
	// "http://kristina-project.eu/ontologies/la/action#";
	// public static final String LA_CONTEXT_NS =
	// "http://kristina-project.eu/ontologies/la/context#";
	// public static final String ONTO_NS =
	// "http://kristina-project.eu/ontologies/la/onto#";

	// public static final String REQUEST = ACTION_NS + "Request";
	// public static final String INFORM = ACTION_NS + "Inform";
	// public static final String GREETING = ACTION_NS + "Greeting";

	public LAParser() {
	}

	@PostConstruct
	public void initialisation() {
		// model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
	}

	/*
	 * Loads LA results and creates a pellet model with all the imported
	 * ontologies needed to parse and reason on the inf model
	 */
	private OntModel createInfModel(String data) throws IOException {
		OntModel model = Utils.createDefaultModel(false);

		// load imports used by LA
		for (String i : _imports) {
			System.out.println("importing " + i);
			model.read(Namespaces.ONTOLOGY_FOLDER + i, "TURTLE");
		}

		// load LA string incoming data
		InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		try {
			model.read(stream, "http://base#", "TURTLE");
		} catch (Exception e) {
			File createTempFile = File.createTempFile("temp", "temp");
			FileUtils.writeStringToFile(createTempFile, data, "UTF-8");
			OntModel temp = Utils.createDefaultModel(false);
			temp.read(new FileInputStream(createTempFile), null);
			model.add(temp);
			createTempFile.delete();
		} finally {

		}

		// create inf model
		OntModel infModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		infModel.setDynamicImports(false);
		infModel.add(model);

		// return the inf model for later use
		return infModel;
	}

	// TODO: need to support multiple situation instances
	public LAParserResult extractKeyEntityTypes(String data) throws IOException {

		LAParserResult result = new LAParserResult();

		OntModel laModel = createInfModel(data);

		// get speech act instance
		// OntClass SpeechAct = laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "SpeechAct");
		// List<? extends OntResource> speechacts =
		// SpeechAct.listInstances(false).toList();
		// if (speechacts.size() > 1) {
		// System.err.println("More than one speech acts detected");
		// }
		// System.out.println(speechacts.size());

		// get speech act type
		// OntResource speechAct = speechacts.get(0);
		OntResource speechActType = null;

		// TODO may need to add more speech act types here!
		// if (speechAct.hasRDFType(laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Request"), true)) {
		// speechActType = laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Request");
		// }
		// if (speechAct.hasRDFType(laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Inform"), true)) {
		// speechActType = laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Inform");
		// }
		// if (speechAct.hasRDFType(laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Greeting"), true)) {
		// speechActType = laModel.getOntClass(Namespaces.LA_DIALOGUE +
		// "Greeting");
		// }

		// speechActType = speechAct.getRDFType(true).as(OntResource.class);
		// System.out.println(speechAct.getRDFType(true));

		// System.out.println(speechActType);
		// **** store speech act type
		// result.speechActType = speechActType;

		// get the textual content
		OntClass UserAction = laModel.getOntClass(Namespaces.LA_DIALOGUE + "UserAction");
		List<? extends OntResource> userActions = UserAction.listInstances().toList();
		if (!userActions.isEmpty()) {
			OntResource userAction = userActions.get(0);
			// userText
			RDFNode textualContent = userAction
					.getPropertyValue(laModel.getProperty(Namespaces.LA_ACTION + "textualContent"));
			if (textualContent != null) {
				result.userText = textualContent.asLiteral().getString();
			}
			// confidence
			RDFNode confidence = userAction
					.getPropertyValue(laModel.getProperty(Namespaces.LA_ACTION + "sentenceASRconfidence"));
			if (confidence != null) {
				result.asrConfidence = confidence.asLiteral().getDouble();
			}

			// speech act
			OntResource speechActInstance = userAction
					.getPropertyResourceValue(laModel.getProperty(Namespaces.LA_ACTION + "contains"))
					.as(OntResource.class);
			speechActType = speechActInstance.getRDFType(true).as(OntResource.class);
			result.speechActType = speechActType;
		}

		// get situation instance
		// OntProperty semanticContent = laModel.getOntProperty(ACTION_NS +
		// "semanticContent");
		// OntResource situation = (OntResource)
		// speechAct.getPropertyResourceValue(semanticContent).as(OntResource.class);

		// need to directly get the situations, since not all situations are
		// references by speech acts
		OntClass ContextSituation = laModel.getOntClass(Namespaces.LA_CONTEXT + "Situation");
		List<? extends OntResource> situations = ContextSituation.listInstances(false).toList();
		System.err.println(situations.size() + " situations found");

		// get includedEvents...
		OntProperty includesEvent = laModel.getOntProperty(Namespaces.LA_CONTEXT + "includesEvent");
		for (OntResource situation : situations) {
			List<RDFNode> includedEvents = situation.listPropertyValues(includesEvent).toList();
			for (RDFNode e : includedEvents) {
				Resource rdfType = e.as(OntResource.class).getRDFType(true);
				// System.err.println("-------------------");
				// System.err.println(rdfType);
				// System.err.println(e);
				KeyEntityWrapper w = new KeyEntityWrapper(e.asResource(), rdfType, null);
				result.keyEntitiesFromClassifiedTypes.add(w);

				// get subclasses
				// List<OntClass> list =
				// e.as(OntClass.class).listSubClasses().toList();
				// for (OntClass ontClass : list) {
				// KeyEntityWrapper w1 = new
				// KeyEntityWrapper(ontClass.asResource(), ontClass, null);
				// result.keyEntitiesFromClassifiedTypes.add(w1);
				// }
			}

			// get request description
			List<Statement> descriptions = situation
					.listProperties(laModel.getOntProperty(Namespaces.LA_CONTEXT + "satisfies")).toList();
			DULParser p = new DULParser();
			for (Statement desc : descriptions) {
				OntResource description = desc.getObject().as(OntResource.class);
				Result defines_classifies_path = p.defines_classifies_path_jena(description, laModel);
				// defines_classifies_path.print();
				result.keyEntitiesFromClassifiedTypes.addAll(defines_classifies_path.classificationTypes);
				result.keyEntitiesFromConceptType.addAll(defines_classifies_path.conceptTypes);
				// if it is a request description instance, then hold the query
				// type
				if (description.hasRDFType(laModel.getOntClass(Namespaces.LA_CONTEXT + "RequestDescription"), true)) {
					// ********//
					result.queryConceptType = defines_classifies_path.classificationTypes.get(0);
					// break;
				} else {
					// if it is a normal description, then extract concept types
					// iteratively...

				}

			}
		}

		//HashSet<KeyEntityWrapper> domainKeyEntities = result.getDomainKeyEntities();
//		for (KeyEntityWrapper keyEntityWrapper : domainKeyEntities) {
//			Resource type = keyEntityWrapper.getType();
//			List<OntClass> subclasses = type.as(OntClass.class).listSubClasses().toList();
//			for (OntClass ontClass : subclasses) {
//				KeyEntityWrapper w1 = new KeyEntityWrapper(ontClass.asResource(), ontClass, null);
//				result.keyEntitiesFromClassifiedTypes.add(w1);
//			}
//
//		}
		System.err.println("domain entities" + Utils.flattenCollection(result.getDomainKeyEntities()));

		// System.out.println(semanticContentValue);

		// String queryString = "" + "Select ?x ?type" + " where {"
		// + " [] <http://www.loa-cnr.it/ontologies/DUL.owl#classifies> ?x . ?x
		// a ?type . " + "}";
		// Query query = QueryFactory.create(queryString);
		//
		// QueryExecution qexec = QueryExecutionFactory.create(query, laModel);
		// ResultSet results = qexec.execSelect();
		// List<KeyEntityWrapper> keyEntities = new ArrayList<>();
		// for (; results.hasNext();) {
		// QuerySolution soln = results.nextSolution();
		// Resource x = soln.getResource("x");
		// Resource type = soln.getResource("type");
		// if (!Utils.UPPER_CONCEPTS.contains(type.getLocalName())) {
		// keyEntities.add(new KeyEntityWrapper(x, type, null));
		// }
		// }
		// return keyEntities;
		return result;
	}
	
	public LAParserResult extractKeyEntityTypesFAKE(String data, String userText, String lang, String speechActType) throws IOException {
		//  [read1 - Read, article2 - Article] 
		OntModel temp  = Utils.createDefaultModel(false);
		LAParserResult result = new LAParserResult();
		
		data = data.substring(1, data.length()-2);
		String[] splited = data.split(",");
		List<String> keyConcepts = new ArrayList<String>();
		for (String string : splited) {
			string = string.trim();
			String[] splitted2 = string.split("-");
			keyConcepts.add(splitted2[1].trim());
			System.out.println(splitted2[1].trim());
			KeyEntityWrapper w = new KeyEntityWrapper(temp.createResource(Utils.tempFullURI()), temp.createClass(Namespaces.LA_ONTO + splitted2[1].trim()), null);
			result.keyEntitiesFromConceptType.add(w);
		}

		result.userText = userText;
		result.asrConfidence = 1.0;
		result.language = lang;
		result.speechActType = temp.createOntResource(Namespaces.LA_DIALOGUE + speechActType);		
		return result;
	}

	// public List<KeyEntityWrapper> extractKeyEntityResources(OntModel model) {
	// String queryString = "" + "Select ?x ?type" + " where {"
	// + " [] <http://www.loa-cnr.it/ontologies/DUL.owl#classifies> ?x . " +
	// "}";
	// Query query = QueryFactory.create(queryString);
	//
	// QueryExecution qexec = QueryExecutionFactory.create(query, model);
	// ResultSet results = qexec.execSelect();
	// List<KeyEntityWrapper> keyEntities = new ArrayList<>();
	// for (; results.hasNext();) {
	// QuerySolution soln = results.nextSolution();
	// Resource x = soln.getResource("x");
	// keyEntities.add(new KeyEntityWrapper(null, x, null));
	// }
	// return keyEntities;
	// }

	// TODO: in next version, the return value should be list for multiple
	// context instances
	// public CurrentContextItem createGenericContext(List<KeyEntityWrapper>
	// extractedKeyEntities) {
	// List<Resource> transform = Lists.transform(extractedKeyEntities, new
	// Function<KeyEntityWrapper, Resource>() {
	//
	// @Override
	// public Resource apply(KeyEntityWrapper input) {
	//
	// return input.getType();
	// }
	// });
	//
	// return new CurrentContextItem(Sets.newHashSet(transform));
	// }
	//
	// public CurrentContextItem createGenericContext() {
	// List<KeyEntityWrapper> extractedKeyEntities = this.extractKeyEntities();
	// List<Resource> transform = Lists.transform(extractedKeyEntities, new
	// Function<KeyEntityWrapper, Resource>() {
	//
	// @Override
	// public Resource apply(KeyEntityWrapper input) {
	//
	// return input.getType();
	// }
	// });
	//
	// return new CurrentContextItem(Sets.newHashSet(transform));
	// }

	// @PreDestroy
	// public void shutdown() {
	// model.close();
	// }

	public class LAParserResult {
		public Double asrConfidence;
		public HashSet<KeyEntityWrapper> keyEntitiesFromConceptType;
		public HashSet<KeyEntityWrapper> keyEntitiesFromClassifiedTypes;
		public OntResource speechActType;
		public KeyEntityWrapper queryConceptType;
		public String userText;
		public String language;

		public LAParserResult() {
			keyEntitiesFromClassifiedTypes = new HashSet<>();
			keyEntitiesFromConceptType = new HashSet<>();
		}

		public HashSet<KeyEntityWrapper> getDomainKeyEntities() {
			HashSet<KeyEntityWrapper> result = new HashSet<>();

			for (KeyEntityWrapper ke : keyEntitiesFromConceptType) {
				// System.out.println(ke.getType().getNameSpace());
				if (ke.getType().getNameSpace().contains(Namespaces.LA_ONTO)) {
					result.add(ke);
				}
			}
			for (KeyEntityWrapper ke : keyEntitiesFromClassifiedTypes) {
				if (ke.getType().getNameSpace().contains(Namespaces.LA_ONTO)) {
					result.add(ke);
				}
			}
			return result;
		}

		public void print() {
			System.out.println("speechActType:" + speechActType);
			System.out.println("userText:" + userText);
			System.out.println("confidence:" + asrConfidence);
			System.out.println("language:" + language);
		}
	}

	public static void main(String[] args) throws IOException {
//		LAParser parser = new LAParser();
//		parser.extractKeyEntityTypes("@prefix : <http://kristina-project.eu/ontologies/la/exampledataQuestion#> .\n"
//				+ "@prefix act: <http://kristina-project.eu/ontologies/la/action#> .\n"
//				+ "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
//				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
//				+ "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n"
//				+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
//				+ "@prefix onto: <http://kristina-project.eu/ontologies/la/onto#> .\n"
//				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
//				+ "@base <http://kristina-project.eu/ontologies/la/exampledataQuestion> .\n" + "\n"
//				+ "<http://kristina-project.eu/ontologies/la/exampledataQuestion> rdf:type owl:Ontology ;\n"
//				+ "                                                               \n"
//				+ "                                                               owl:imports <http://kristina-project.eu/ontologies/la/action> ,\n"
//				+ "                                                                           <http://kristina-project.eu/ontologies/la/onto> .\n"
//				+ "\n" + "\n" + "#################################################################\n" + "#\n"
//				+ "#    Individuals\n" + "#\n" + "#################################################################\n"
//				+ "\n" + "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#Eugene\n" + "\n"
//				+ ":Eugene rdf:type onto:CareRecipient ,\n" + "                 owl:NamedIndividual .\n" + "\n" + "\n"
//				+ "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#action1\n" + "\n"
//				+ ":action1 rdf:type act:UserAction ,\n" + "                  owl:NamedIndividual ;\n" + "         \n"
//				+ "         act:contains :speechAct1 .\n" + "\n" + "\n" + "\n"
//				+ "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#agent1\n" + "\n"
//				+ ":agent1 rdf:type <http://kristina-project.eu/ontologies/la/context#Agent> ,\n"
//				+ "                 owl:NamedIndividual ;\n" + "        \n"
//				+ "        <http://kristina-project.eu/ontologies/la/context#classifies> :Eugene .\n" + "\n" + "\n"
//				+ "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#context1\n" + "\n"
//				+ ":context1 rdf:type <http://kristina-project.eu/ontologies/la/context#Context> ,\n"
//				+ "                   owl:NamedIndividual ;\n" + "          \n"
//				+ "          <http://kristina-project.eu/ontologies/la/context#classifies> :wakeUp1 .\n" + "\n" + "\n"
//				+ "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#requestDesc1\n" + "\n"
//				+ ":requestDesc1 rdf:type <http://kristina-project.eu/ontologies/la/context#RequestDescription> ,\n"
//				+ "                       owl:NamedIndividual ;\n" + "              \n"
//				+ "              <http://kristina-project.eu/ontologies/la/context#defines> :requested1 .\n" + "\n"
//				+ "\n" + "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#requested1\n" + "\n"
//				+ ":requested1 rdf:type <http://kristina-project.eu/ontologies/la/context#RequestElement> ,\n"
//				+ "                     owl:NamedIndividual ;\n" + "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#classifies> :time1 .\n" + "\n" + "\n"
//				+ "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#speechAct1\n" + "\n"
//				+ ":speechAct1 rdf:type act:Request ,\n" + "                     owl:NamedIndividual ;\n"
//				+ "            \n"
//				+ "            act:textualContent \"At what time does Eugene wake up?\"^^xsd:string ;\n"
//				+ "            \n" + "            act:individualType \"true\"^^xsd:boolean ;\n" + "            \n"
//				+ "            act:semanticContent :wakeUpCtx1 .\n" + "\n" + "\n" + "\n"
//				+ "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#startTime1\n" + "\n"
//				+ ":startTime1 rdf:type <http://kristina-project.eu/ontologies/la/context#StartTime> ,\n"
//				+ "                     owl:NamedIndividual ;\n" + "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#classifies> :time1 .\n" + "\n" + "\n"
//				+ "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#time1\n" + "\n"
//				+ ":time1 rdf:type onto:Time ,\n" + "                owl:NamedIndividual .\n" + "\n" + "\n" + "\n"
//				+ "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#wakeUp1\n" + "\n"
//				+ ":wakeUp1 rdf:type onto:WakeUp ,\n" + "                  owl:NamedIndividual .\n" + "\n" + "\n" + "\n"
//				+ "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#wakeUpCtx1\n" + "\n"
//				+ ":wakeUpCtx1 rdf:type <http://kristina-project.eu/ontologies/la/context#ContextSituation> ,\n"
//				+ "                     owl:NamedIndividual ;\n" + "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#includes> :Eugene ;\n"
//				+ "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#satisfies> :requestDesc1 ;\n"
//				+ "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#includes> :time1 ,\n"
//				+ "                                                                        :wakeUp1 ;\n"
//				+ "            \n"
//				+ "            <http://kristina-project.eu/ontologies/la/context#satisfies> :wakeUpDesc1 .\n" + "\n"
//				+ "\n" + "\n" + "###  http://kristina-project.eu/ontologies/la/exampledataQuestion#wakeUpDesc1\n" + "\n"
//				+ ":wakeUpDesc1 rdf:type <http://kristina-project.eu/ontologies/la/context#ContextDescription> ,\n"
//				+ "                      owl:NamedIndividual ;\n" + "             \n"
//				+ "             <http://kristina-project.eu/ontologies/la/context#defines> :agent1 ,\n"
//				+ "                                                                        :context1 ,\n"
//				+ "                                                                        :startTime1 .\n" + "\n"
//				+ "\n" + "\n" + "\n" + "###  Generated by the OWL API (version 3.5.1) http://owlapi.sourceforge.net\n"
//				+ "\n" + "");

		// System.out.println(Utils.modelToString(model, "TTL"));
		// CurrentContextItem createGenericContext =
		// parser.createGenericContext(parser.extractKeyEntities());
		// System.out.println(createGenericContext);
		
		String data = "[read1 - Read, article2 - Article]";
		LAParserResult extractKeyEntityTypesFAKE = new LAParser().extractKeyEntityTypesFAKE(data, "userText", "pl", "Request");
		extractKeyEntityTypesFAKE.print();
		System.out.println(Utils.flattenCollection(extractKeyEntityTypesFAKE.getDomainKeyEntities()));

	}

}
