package gr.iti.kristina.topicflow;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.inference.DefaultSPINRuleComparator;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.inference.SPINRuleComparator;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SPIN;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

public class ThemeKB {

	private static final Logger LOG = LoggerFactory.getLogger(ThemeKB.class);

	public OntModel infModel;
	String prefixes = "" +
			"PREFIX core:   <" + Namespaces.CORE + ">" +
			"PREFIX rdfs:   <" + RDFS.getURI() + ">"
			+ "PREFIX owl:   <" + OWL.getURI() + ">";

	public ThemeKB(Set<String> themeModels) {
		OntModel model = Utils.createDefaultModel(false);
		model.read(Namespaces.ONTOLOGY_FOLDER + "prototype3/3rd_prototype/models/core.ttl", "TURTLE");

		themeModels.forEach(m -> {
			LOG.debug("loading: " + m);
			model.read(Namespaces.ONTOLOGY_FOLDER
					+ String.format("prototype3/3rd_prototype/models/%s.ttl", m), "TURTLE");
		});

		model.read(Namespaces.ONTOLOGY_FOLDER + "onto.ttl", "TURTLE");

		infModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		infModel.setDynamicImports(false);
		// infModel.add(model);
		// infModel.prepare();
		// ((PelletInfGraph) infModel.getGraph()).getKB().classify();
		// ((PelletInfGraph) infModel.getGraph()).getKB().realize();
		infModel = startReasoning(model);

	}

	private static OntModel loadModelWithImports(String url) {
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(url);
		return JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
	}

	private OntModel startReasoning(OntModel queryModel) {
		SPINModuleRegistry.get().init();

		OntModel newTriples = ModelFactory.createOntologyModel();
		queryModel.addSubModel(newTriples);

		System.out.println("Loading OWL RL ontology...");
		OntModel owlrlModel = loadModelWithImports("http://topbraid.org/spin/owlrl-all");

		SPINModuleRegistry.get().registerAll(owlrlModel, null);

		MultiUnion multiUnion = JenaUtil.createMultiUnion(new Graph[] {
				queryModel.getGraph(),
				owlrlModel.getGraph()
		});
		Model unionModel = ModelFactory.createModelForGraph(multiUnion);

		Map<Resource, List<CommandWrapper>> cls2Query = SPINQueryFinder.getClass2QueryMap(unionModel, queryModel,
				SPIN.rule, true, false);
		Map<Resource, List<CommandWrapper>> cls2Constructor = SPINQueryFinder.getClass2QueryMap(queryModel, queryModel,
				SPIN.constructor, true, false);
		SPINRuleComparator comparator = new DefaultSPINRuleComparator(queryModel);

		System.out.println("Running SPIN inferences...");
		SPINInferences.run(queryModel, newTriples, cls2Query, cls2Constructor,
				null, null, false, SPIN.rule, comparator,
				null);
		// System.out.println("Inferred triples: " +
		// Utils.modelToString(newTriples, "TURTLE"));
		return (OntModel) queryModel.add(newTriples);
	}

	private QueryExecution createSelectQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		return QueryExecutionFactory.create(query, infModel);
	}

	public Set<OntClass> getClasses() {
		Set<OntClass> classes = new HashSet<>();
		String queryString = this.prefixes + " 	select ?c where {?c a core:Core} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("c");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		return classes;
	}

	public Set<OntClass> getThemesOfTopics(OntClass c) {
		Set<OntClass> classes = new HashSet<>();
		String queryString = this.prefixes
				+ " select ?c where {"
				+ "		?c core:responseType ?responseType. "
				+ "		?node rdfs:subClassOf ?c. "
				+ "		FILTER regex(?responseType, \"THEME\", \"i\" ) } ";
		// LOG.debug(queryString);
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("node", c);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("c");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		return classes;
	}

	public Set<OntClass> getKeysOfTopic(OntClass c) {
		List<RDFNode> keys = c.listPropertyValues(infModel.getOntProperty(Namespaces.CORE + "key")).toList();
		return keys.stream().map(x -> x.as(OntClass.class)).collect(Collectors.toSet());
	}

	public Set<OntClass> getHiddenKeysOfTopic(OntClass c) {
		List<RDFNode> keys = c.listPropertyValues(infModel.getOntProperty(Namespaces.CORE + "hiddenKey")).toList();
		return keys.stream().map(x -> x.as(OntClass.class)).collect(Collectors.toSet());
	}
	
	public Set<OntClass> getHiddenKeysOnRejectionOfTopic(OntClass c) {
		List<RDFNode> keys = c.listPropertyValues(infModel.getOntProperty(Namespaces.CORE + "hiddenKeyOnRejection")).toList();
		return keys.stream().map(x -> x.as(OntClass.class)).collect(Collectors.toSet());
	}

	public Set<OntClass> getTopicsWithKey(OntClass key) {
		Set<OntClass> classes = new HashSet<>();
		if (key == null)
			return classes;
		String queryString = this.prefixes + " 	select ?c where {"
				+ "	?c core:key ?key. "
				+ "	FILTER(?c != owl:Nothing) . "
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("key", key);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("c");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		// LOG.debug("key: " + key + ", termClasses: " + classes);
		return classes;

	}

	public Set<OntClass> getTopicsWithKeys(Set<OntClass> keys) {
		Set<OntClass> classes = new HashSet<>();
		for (OntClass c : keys) {
			Set<OntClass> classesWithKey = getTopicsWithKey(c);
			classes.addAll(classesWithKey);
		}
		// LOG.debug("getTermClassesWithKeys: " + classes);
		return classes;
	}

	public Set<OntClass> getSubTopics(OntClass topic, boolean direct) {
		return topic.listSubClasses(true).toSet().stream()
				.filter(t -> !t.equals(OWL.Nothing)).collect(Collectors.toSet());
		// Set<OntClass> classes = new HashSet<>();
		// String queryString = this.prefixes + " "
		// + "select ?c where {"
		// + " ?c rdfs:subClassOf ?topic."
		// + " FILTER EXISTS {?c a core:Core. }"
		// + " FILTER(?c != ?topic). "
		// + " FILTER(?c != owl:Nothing). "
		// + " FILTER NOT EXISTS { ?otherSub rdfs:subClassOf ?topic. \n" +
		// " ?c rdfs:subClassOf ?otherSub . \n" +
		// " FILTER (?otherSub != ?c). \n" +
		// " }"
		// + "} ";
		// QueryExecution qexec = this.createSelectQuery(queryString);
		// QuerySolutionMap initialBindings = new QuerySolutionMap();
		// initialBindings.add("topic", topic);
		// qexec.setInitialBinding(initialBindings);
		// LOG.debug(queryString);
		// ResultSet results = qexec.execSelect();
		// for (; results.hasNext();) {
		// QuerySolution soln = results.nextSolution();
		// Resource r = soln.getResource("c");
		// classes.add(r.as(OntClass.class));
		// }
		// qexec.close();
		// return classes;
	}

	public Set<OntClass> getCustomProactiveTopics(OntClass topic) {
		Set<OntClass> classes = new HashSet<>();
		String queryString = this.prefixes
				+ " select ?proactive where {"
				+ "		?topic core:proactive ?proactive. "
				+ "}";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("proactive");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		return classes;
	}

	public Set<OntClass> getAdditionalResponseTopics(OntClass topic) {
		Set<OntClass> classes = new HashSet<>();
		String queryString = this.prefixes
				+ " select ?additional where {"
				+ "		?topic core:additional ?additional. "
				+ "}";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("additional");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		return classes;
	}
	
	public Set<OntClass> getCustomMoreSpecific(OntClass topic) {
		Set<OntClass> classes = new HashSet<>();
		String queryString = this.prefixes
				+ " select ?additional where {"
				+ "		?topic core:more_specific ?additional. "
				+ "}";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("additional");
			classes.add(r.as(OntClass.class));
		}
		qexec.close();
		return classes;
	}

	public String getSpokenText(OntClass topic) {
		String queryString = this.prefixes + " 	"
				+ "select ?text where {"
				+ "		?topic core:spokenText ?text."
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		String text = "";
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			text = soln.getLiteral("text").getString();
		}
		qexec.close();
		if(StringUtils.isBlank(text)){
			LOG.warn("no spoken text found for: " + Utils.getLocalName(topic));
		}
		return text;
	}

	public String getText(OntClass topic) {
		String queryString = this.prefixes + " 	"
				+ "select ?text where {"
				+ "		?topic core:text ?text."
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		String text = "";
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			text = soln.getLiteral("text").getString();
		}
		qexec.close();
		return text;
	}

	public String getURL(OntClass topic) {
		String queryString = this.prefixes + " 	"
				+ "select ?text where {"
				+ "		?topic core:url ?text."
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		String text = "";
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			text = soln.getLiteral("text").getString();
		}
		qexec.close();
		return text;
	}

	public Set<String> getResponseTypes(OntClass topic) {
		Set<String> types = new HashSet<>();
		String queryString = this.prefixes + " 	"
				+ "select ?type where {"
				+ "		?topic core:responseType ?type."
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			types.add(soln.getLiteral("type").getString());
		}
		qexec.close();
		return types;
	}

	public JavaConfig getJavaConfigProperty(OntClass topic) {
		JavaConfig javaConfig = new JavaConfig();
		String queryString = this.prefixes + " 	"
				+ "select ?type where {"
				+ "		?topic core:javaConfig ?type."
				+ "} ";
		QueryExecution qexec = this.createSelectQuery(queryString);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("topic", topic);
		qexec.setInitialBinding(initialBindings);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			String _t = soln.getLiteral("type").getString();
			Gson g = new Gson();
			javaConfig = g.fromJson(_t, JavaConfig.class);
		}
		qexec.close();

		LOG.debug(javaConfig.toString());

		return javaConfig;
	}

	public OntClass getOntoClass(String uri) {
		OntClass ontClass = this.infModel.getOntClass(uri);
		if (ontClass == null) {
			LOG.debug("Cannot be found: " + uri);
		}
		return ontClass;
	}

	public Set<OntClass> getOntClasses(Set<String> laConcepts) {
		return laConcepts.stream()
				.map(la -> this.getOntoClass(la)).filter(la -> la != null).collect(Collectors.toSet());
	}

	public boolean isLeafTopic(OntClass topic) {
		return this.getSubTopics(topic, true).isEmpty();
	}

	public void printClasses() {
		for (OntClass c : getClasses()) {
			LOG.debug(c.toString());
			LOG.debug("Subclasses: " + Utils.flattenCollection(c.listSuperClasses(false).toList()));
			LOG.debug("Keys: " + this.getKeysOfTopic(c));
			LOG.debug("");
		}
	}

	public boolean isTheme(OntClass topic) {
		return this.getResponseTypes(topic).contains("THEME");
	}

	public boolean isAbstract(OntClass topic) {
		return this.getResponseTypes(topic).contains("ABSTRACT");
	}

	public boolean hasResponseType(OntClass topic, String responseType) {
		return this.getResponseTypes(topic).contains(responseType);
	}

	public int getMatchedKeys(OntClass topic, HashSet<OntClass> ontos) {
		Set<OntClass> keysOfTopic = Sets.newHashSet(getKeysOfTopic(topic));
		int size = Sets.intersection(keysOfTopic, ontos).size();
		// LOG.debug("getMatchedKeys: " + size);
		return size;
	}

	public int getSemanticMatchedKeys(OntClass topic, HashSet<OntClass> ontos) {
		Set<OntClass> keysOfTopic = Sets.newHashSet(getKeysOfTopic(topic));
		keysOfTopic.addAll(topic.listSubClasses().toList()
				.stream().map(sub -> getKeysOfTopic(sub)).flatMap(x -> x.stream()).collect(Collectors.toSet()));
		int size = semanticTopicIntersection(keysOfTopic, ontos).size();
		return size;
	}

	public Set<OntClass> semanticTopicIntersection(Set<OntClass> newTopics,
			Set<OntClass> clarifications) {
		return newTopics.stream().filter(t -> {
			return clarifications.stream().anyMatch(c -> {
				return t.hasSuperClass(c, false);
			});
		}).collect(Collectors.toSet());
	}

	public boolean isKBOrIR(OntClass topic) {
		return !Sets.intersection(this.getResponseTypes(topic),
				Sets.newHashSet("IR", "KB", "TEXT", "URL", "MORE", "SOCIAL", "RECIPE", "WEATHER")).isEmpty();
	}

}
