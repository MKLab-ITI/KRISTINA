package gr.iti.kristina.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import gr.iti.kristina.model.KeyEntityWrapper;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class TopicUnderstandingBean
 */
@Stateless
@LocalBean
public class TopicUnderstandingBean {

	// @EJB
	// PelletReasoner pelletBean;

	// public static final String CTX_URI =
	// "http://kristina-project.eu/ontologies/context-light#";
	// public static final String CTX_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/context-light_v4.ttl";
	// public static final String COMMON_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/onto.ttl";

	public TopicUnderstandingBean() {
	}

	@PostConstruct
	public void initialisation() {
		// pelletBean.getOntModel().read(
		// "file:///C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/context-light_v3.ttl",
		// "TURTLE");
		// System.out.println("initialisation pellet");
	}

	public OntModel loadExtractedEntityResources(HashSet<KeyEntityWrapper> keyEntities) {
		System.out.println("1");
		OntModel tempModel = Utils.createDefaultModel(false);
		tempModel.read(Namespaces.KB_CONTEXT_FILE, "TURTLE");
		tempModel.read(Namespaces.LA_ONTO_FILE, "TURTLE");
		System.out.println("2");
		OntModel infModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		infModel.setDynamicImports(false);
		infModel.add(tempModel);
		System.out.println("3");

		OntClass Context = infModel.getOntClass(Namespaces.KB_CONTEXT + "Context");
		OntProperty contains = infModel.getOntProperty(Namespaces.KB_CONTEXT + "contains");

		Individual context = infModel.createIndividual("http://temp#" + Utils.randomString(), Context);
		System.out.println("4");
		if (keyEntities != null) {
			for (KeyEntityWrapper ke : keyEntities) {
				Resource ind = infModel.createResource(ke.getInd().getURI());
				Resource type = infModel.createResource(ke.getType().getURI());
				context.addProperty(contains, ind);
				ind.addProperty(RDF.type, type);
			}
		}
		System.out.println("5");
		infModel.prepare();
		System.out.println("6");
		return infModel;
	}

	public HashMap<OntResource, Collection<Resource>> getTopics(OntModel infModel) {
		System.out.println("7");
		HashMap<OntResource, Collection<Resource>> result = new HashMap<>();
		List<? extends OntResource> contexts = getContextInstances(infModel);
		System.out.println("8");
		// System.out.println(contexts);
		for (OntResource context : contexts) {
			Collection<Resource> types = getContextTypes(context, true);
			// System.out.println(types.isEmpty());
			if (!types.isEmpty())
				result.put(context, types);
		}
		System.out.println("9");
		return result;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws OWLOntologyCreationException {
		TopicUnderstandingBean t = new TopicUnderstandingBean();
		String data = "@prefix : <http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#> .\r\n"
				+ "@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n"
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
				+ "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\r\n"
				+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + "\r\n"
				+ "<http://kristina-project.eu/ontologies/context-light/sleeping_habits_data> a owl:Ontology .\r\n"
				+ "	\r\n" + "# \r\n" + "# \r\n"
				+ "# #################################################################\r\n" + "# #\r\n"
				+ "# #    Individuals\r\n" + "# #\r\n"
				+ "# #################################################################\r\n" + "# \r\n" + "# \r\n"
				+ "# http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#When_does_Eugene_usually_go_to_bed\r\n"
				+ "\r\n"
				+ ":When_does_Eugene_usually_go_to_bed a owl:NamedIndividual , <http://kristina-project.eu/ontologies/context-light/v1#Context> ;\r\n"
				+ "	<http://kristina-project.eu/ontologies/context-light/v1#contains> :bed , :care_recipient , :sleep , :start_time .\r\n"
				+ "# \r\n" + "# http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#bed\r\n"
				+ "\r\n"
				+ ":bed a owl:NamedIndividual , <http://kristina-project.eu/ontologies/context-light/v1#Bed> .\r\n"
				+ "# \r\n"
				+ "# http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#care_recipient\r\n"
				+ "\r\n"
				+ ":care_recipient a owl:NamedIndividual , <http://kristina-project.eu/ontologies/context-light/v1#CareRecipient> .\r\n"
				+ "# \r\n" + "# http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#sleep\r\n"
				+ "\r\n"
				+ ":sleep a owl:NamedIndividual , <http://kristina-project.eu/ontologies/context-light/v1#Sleep> .\r\n"
				+ "# \r\n" + "# http://kristina-project.eu/ontologies/context-light/sleeping_habits_data#start_time\r\n"
				+ "\r\n"
				+ ":start_time a owl:NamedIndividual , <http://kristina-project.eu/ontologies/context-light/v1#StartTime> .\r\n"
				+ "# \r\n"
				+ "# Generated by the OWL API (version 4.2.3.20160319-0906) https://github.com/owlcs/owlapi\r\n";

		// t.loadVerbalResults(data);

		// HashMap<OntResource, Collection<Resource>> topic = t.getTopic();
		// Set<OntResource> keys = topic.keySet();
		// for (OntResource k : keys) {
		// System.out.println(k);
		// System.out.println(topic.get(k));
		// }
		// System.out.println(new BigInteger(130, new
		// SecureRandom()).toString(32));

	}

	private List<? extends OntResource> getContextInstances(OntModel model) {
		OntClass Context = model.getOntClass(Namespaces.KB_CONTEXT + "Context");
		return Context.listInstances(false).toList();
	}

	private Collection<Resource> getContextTypes(OntResource context, boolean direct) {
		System.out.println("11");
		List<Resource> types = context.listRDFTypes(direct).toList();
		System.out.println("12");
		return Utils.removeUpperClasses(types);

	}

	// private Collection<Resource> getRestrictionClasses(Collection<Resource>
	// types) {
	// Set<OntClass> relevantClasses = new HashSet<>();
	//
	// for (Resource type : types) {
	// relevantClasses.remove(type); // to remove classes which we expand,
	// // e.g. IndividualContext
	// Set<OntClass> equivalentclasses =
	// type.as(OntClass.class).listEquivalentClasses().toSet();
	// equivalentclasses.remove(type);
	// for (OntClass eq : equivalentclasses) {
	// if (eq.isIntersectionClass()) {
	// IntersectionClass inter = eq.asIntersectionClass();
	// List<RDFNode> operands = inter.getOperands().asJavaList();
	// for (RDFNode op : operands) {
	// if (!op.isAnon()) {
	// relevantClasses.add(op.as(OntClass.class));
	// } else {
	// SomeValuesFromRestriction restriction =
	// op.as(SomeValuesFromRestriction.class);
	// relevantClasses.add(restriction.getSomeValuesFrom().as(OntClass.class));
	// }
	// }
	// } else if (eq.isAnon()) {
	// SomeValuesFromRestriction restriction =
	// eq.as(SomeValuesFromRestriction.class);
	// relevantClasses.add(restriction.getSomeValuesFrom().as(OntClass.class));
	//
	// }
	// }
	// }
	// return Utils.removeUpperClasses(new
	// ArrayList<Resource>(relevantClasses));
	// }

}
