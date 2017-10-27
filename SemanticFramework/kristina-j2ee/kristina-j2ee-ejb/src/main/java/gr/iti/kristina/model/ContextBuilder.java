package gr.iti.kristina.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gr.iti.kristina.startup.HierarchyBean;
import gr.iti.kristina.utils.QueryUtils;

public class ContextBuilder {

	private final Multimap<Signature, String> mappings;
	// private final State state;
	private final RepositoryConnection kbConnection;
	private final ValueFactory vf;

	// generated in this class
	private HashMultimap<String, Triple> contexts;
	// private Set<Dependency> contextDependencies;
	TreeSet<ContextCluster> contextClusters = new TreeSet<>();

	private final Logger logger = Logger.getLogger(ContextBuilder.class);

	public ContextBuilder(Multimap<Signature, String> mappings, RepositoryConnection connection) {
		this.mappings = mappings;
		// this.state = state;
		this.kbConnection = connection;
		vf = kbConnection.getValueFactory();
	}

	public void start() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		contexts = HashMultimap.create();
		LinkedList<Value> agenda = new LinkedList<>();

		Set<Signature> mappingsSet = mappings.keySet();

		String q1 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?node ?p ?y  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/2002/07/owl#\"))"
				+ "    FILTER (contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\") || !contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?y != ?node && ?y != owl:Thing "
				+ "     && ?y != rdfs:Class && ?y != owl:Class && ?y != rdf:Property && ?y != owl:ObjectProperty && ?y != owl:DatatypeProperty "
				+ "     && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment && !isBlank(?y))"
				+ "}  ";

		String q2 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?x ?p ?node  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/2002/07/owl#\"))"
				+ "    FILTER (contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\") || !contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?node != ?x && ?x != owl:Nothing && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment)"
				+ "}  ";

		String q3 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?x ?node ?y  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?node != ?x && ?x != owl:Nothing && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment && !isBlank(?y))"
				+ "}  ";

		// this is for the >1 iterations, where we do not need types...
		String q11 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?node ?p ?y  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/2002/07/owl#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?y != ?node && ?y != owl:Thing "
				+ "     && ?y != rdfs:Class && ?y != owl:Class && ?y != rdf:Property && ?y != owl:ObjectProperty && ?y != owl:DatatypeProperty "
				+ "     && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment && !isBlank(?y))"
				+ "}  ";

		TupleQuery tpq11 = kbConnection.prepareTupleQuery(QueryLanguage.SPARQL, q11);

		String q22 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?x ?p ?node  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/2002/07/owl#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?node != ?x && ?x != owl:Nothing && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment)"
				+ "}  ";
		TupleQuery tpq22 = kbConnection.prepareTupleQuery(QueryLanguage.SPARQL, q22);

		String q33 = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct * where {  "
				+ "    ?x ?node ?y  . " + "    FILTER (!contains(str(?p), \"http://www.w3.org/2000/01/rdf-schema#\"))"
				+ "    FILTER (!contains(str(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"))"
				+ "    FILTER(?node != ?x && ?x != owl:Nothing && ?p != rdfs:domain && ?p != rdfs:range && ?p != rdfs:label && ?p != rdfs:comment && !isBlank(?y))"
				+ "}  ";
		TupleQuery tpq33 = kbConnection.prepareTupleQuery(QueryLanguage.SPARQL, q33);

		int H = 3;

		for (Signature s : mappingsSet) {
			String _node = mappings.get(s).iterator().next();
			System.out.println("--------------------------------------------------" + _node);

			// if (_node.contains("CareRecipient"))
			// continue;

			// check here if node is an instance or class and make the necessary
			// distinctions...
			URI nodeUri = vf.createURI(_node);
			boolean isClass = kbConnection.hasStatement(nodeUri, RDF.TYPE, OWL.CLASS, true);
			logger.info("is class:" + isClass);

			System.err.println("_node=" + _node);
			System.out.println("_signature: " + s);
			TupleQueryResult result = QueryUtils.evaluateSelectQuery(kbConnection, q1,
					new BindingImpl("node", vf.createURI(_node)));
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Binding p = bindingSet.getBinding("p");
				Binding y = bindingSet.getBinding("y");
				if (!isClass) {
					// no need to create a context with class as core
					// resource...
					Triple t = new Triple(_node, p.getValue().stringValue(), y.getValue().stringValue());
					contexts.put(s.uri, t);
					// t.S_isUri = nodeUri instanceof URI;
					// t.O_isLiteral = nodeUri instanceof LiteralImpl;

				}
				// but we do need their instances for expansion!!!
				addToAgenda(p, agenda);
				System.err.println("+++++++++++++++++++++++++" + p);
			}
			result.close();
			result = QueryUtils.evaluateSelectQuery(kbConnection, q2, new BindingImpl("node", vf.createURI(_node)));
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Binding x = bindingSet.getBinding("x");
				Binding p = bindingSet.getBinding("p");
				if (!isClass) {
					contexts.put(s.uri, new Triple(x.getValue().stringValue(), p.getValue().stringValue(), _node));

				}
				addToAgenda(x, agenda);

				// agenda.add(x.getValue().stringValue());
			}
			result.close();

			result = QueryUtils.evaluateSelectQuery(kbConnection, q3, new BindingImpl("node", vf.createURI(_node)));
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Binding x = bindingSet.getBinding("x");
				Binding y = bindingSet.getBinding("y");
				if (!isClass) {
					contexts.put(s.uri, new Triple(x.getValue().stringValue(), _node, y.getValue().stringValue()));

				}
				addToAgenda(y, agenda);
				addToAgenda(x, agenda);
				// agenda.add(x.getValue().stringValue());
			}
			result.close();

		}

		// System.out.println("print agenda 1");
		// System.out.println(Utils.flattenCollection(agenda));

		// more
		System.out.println("linked");

		int temp = 2;
		LinkedList<Value> current = new LinkedList<>();
		HashSet<Value> visited = new HashSet<>();
		while (temp <= H) {
			while (!agenda.isEmpty()) {
				Value a = (Value) agenda.pop();
				String a_stringValue = a.stringValue();
				if (visited.contains(a)) {
					continue;
				}
				visited.add(a);
				// String _node = mappings.get(s).iterator().next();
				// TupleQueryResult result =
				// QueryUtils.evaluateSelectQuery(kbConnection, q11, new
				// BindingImpl("node", a));
				tpq11.setBinding("node", a);
				TupleQueryResult result = tpq11.evaluate();
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Binding p = bindingSet.getBinding("p");
					Binding y = bindingSet.getBinding("y");
					if (shouldBeReturned(new Binding[] { p, y })) {
						contexts.put(a_stringValue,
								new Triple(a_stringValue, p.getValue().stringValue(), y.getValue().stringValue()));
						addToAgenda(y, current);
					}
				}
				result.close();
				// result = QueryUtils.evaluateSelectQuery(kbConnection, q22,
				// new BindingImpl("node", a));
				tpq22.setBinding("node", a);
				result = tpq22.evaluate();
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Binding x = bindingSet.getBinding("x");
					Binding p = bindingSet.getBinding("p");
					if (shouldBeReturned(new Binding[] { p, x })) {
						contexts.put(a_stringValue,
								new Triple(x.getValue().stringValue(), p.getValue().stringValue(), a_stringValue));
						addToAgenda(x, current);
					}
				}
				result.close();

				// result = QueryUtils.evaluateSelectQuery(kbConnection, q33,
				// new BindingImpl("node", a));
				tpq33.setBinding("node", a);
				result = tpq33.evaluate();
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Binding x = bindingSet.getBinding("x");
					Binding y = bindingSet.getBinding("y");
					if (shouldBeReturned(new Binding[] { x, y })) {
						contexts.put(a_stringValue,
								new Triple(x.getValue().stringValue(), a_stringValue, y.getValue().stringValue()));
						addToAgenda(y, current);
						addToAgenda(x, current);
					}
				}
				result.close();
			}
			// System.out.println("print agenda " + temp);
			// System.out.println(Utils.flattenCollection(current));
			temp++;
			agenda.addAll(current);
			current.clear();
		}
		visited.clear();

		System.out.println("print content:");
		// cleanContext(contexts);
		// Utils.printMap(contexts);
		// System.out.println("print agenda");
		// System.out.println(Utils.flattenCollection(agenda));

		runRuleEngine();
		// mergeContexts();
		removeSubProperties();

	}

	private void removeSubProperties() throws RepositoryException {
		// System.err.println("removeSubProperties started");
		for (ContextCluster cc : contextClusters) {
			HashSet<Triple> temp = new HashSet<>();
			temp.addAll(cc.triples);
			HashSet<Triple> triples = cc.triples;
			// System.err.println("size: " + triples.size());
			for (Triple t1 : triples) {
				URI p1 = vf.createURI(t1.getP());
				// boolean found = false;
				for (Triple t2 : triples) {
					if (t1.getS().equals(t2.getS()) && t1.getO().equals(t2.getO()) && !t1.equals(t2)) {

						URI p2 = vf.createURI(t2.getP());
						// System.out.println(p1 + " " + p2);
						if (kbConnection.hasStatement(p2, RDFS.SUBPROPERTYOF, p1, true)) {
							temp.remove(t1);
							System.err.println("triple removed: " + t1);
							// break;
						}
						if (kbConnection.hasStatement(p1, RDFS.SUBPROPERTYOF, p2, true)) {
							temp.remove(t2);
							System.err.println("triple removed: " + t2);
							// break;
						}
					}
				}
			}
			cc.triples.clear();
			cc.triples.addAll(temp);
		}

	}

	// add for expansion only the resources that are not classes, literals and
	// not already contained in agenda.
	private void addToAgenda(Binding v, LinkedList<Value> agenda) throws RepositoryException {
		if (!"LiteralImpl".equals(v.getValue().getClass().getSimpleName())
		/*
		 * &&
		 * !kbConnection.hasStatement(vf.createURI(v.getValue().stringValue()),
		 * RDF.TYPE, OWL.CLASS, true)
		 */) {
			if (!agenda.contains(v.getValue()))
				agenda.add(v.getValue());
		}
	}

	// private void cleanContext(HashMultimap<String, Triple> contexts2) {
	// Set<Entry<String, Triple>> entries = contexts2.entries();
	// Iterator<Entry<String, Triple>> iterator = entries.iterator();
	// while (iterator.hasNext()) {
	// Map.Entry<String, Triple> entry = (Map.Entry<String, Triple>)
	// iterator.next();
	// if
	// (entry.getValue().p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
	// {
	// iterator.remove();
	// }
	//
	// }
	//
	// }

	public boolean shouldBeReturned(Binding... bindings) {
		// boolean r = true;
		// for (int i = 0; i < bindings.length; i++) {
		// Binding b = bindings[i];
		// r = r && pass(b.getValue());
		// System.out.println(r);
		//
		// }
		// return r;
		return true;
	}

	// private boolean pass(Value value) {
	// System.err.println(value.stringValue());
	// if (StringUtils.containsIgnoreCase(value.stringValue(), "dul.owl")) {
	// return false;
	// }
	// return true;
	// }

	protected void runRuleEngine() {
		System.out.println("running drools");
		// contextDependencies = new HashSet<>();

		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		KieSession kSession = kContainer.newKieSession("contextsKSession");
		kSession.setGlobal("logger", logger);
		kSession.setGlobal("service", this);

		Collection<Map.Entry<String, Triple>> entries = contexts.entries();

		for (Map.Entry<String, Triple> entry : entries) {
			kSession.insert(entry);
			// logger.info(entry.getValue().toString());
			// kSession.insert(entry.getValue());

		}

		kSession.fireAllRules();
		kSession.dispose();
	}

	public boolean match(Triple t1, Triple t2) {
		return t1.s.equals(t2.s) || t1.s.equals(t2.p) || t1.s.equals(t2.o) || t1.p.equals(t2.s) // ||
																								// t1.p.equals(t2.p)
				|| t1.p.equals(t2.o) || t1.o.equals(t2.s) || t1.o.equals(t2.p) || t1.o.equals(t2.o);
	}

	public boolean matchList(Triple t1, HashSet<Triple> triples) {
		for (Triple t : triples) {
			if (match(t1, t)) {
				return true;
			}
		}
		return false;
	}

	public void logContextCluster(ContextCluster cluster) {

		contextClusters.add(cluster);
		// System.out.println("context cluster added: " +
		// contextClusters.size());
	}

	// public void logDependency(Dependency d) {
	// contextDependencies.add(d);
	// }
	// private Set<Triple> mergeContexts() {
	// for (Dependency d : contextDependencies) {
	// String key1 = d.getKey1();
	// String key2 = d.getKey2();
	//
	// Set<Triple> c1 = contexts.get(key1);
	// Set<Triple> c2 = contexts.get(key2);
	//
	// c1.addAll(c2);
	// return c1;
	//
	// }
	// }
	public TreeSet<ContextCluster> getContextClusters() {
		return contextClusters;
	}

	public static void main(String[] args) {
		ContextBuilder b = new ContextBuilder(null, null);
		b.runRuleEngine();
	}

}
