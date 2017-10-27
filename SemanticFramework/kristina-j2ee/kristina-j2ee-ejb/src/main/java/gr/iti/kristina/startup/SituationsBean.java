package gr.iti.kristina.startup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.model.Triple;
import gr.iti.kristina.repository.GraphDbRepositoryManager;
import gr.iti.kristina.utils.QueryUtils;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class HierarchyBean
 */
//@Startup
@Singleton
@LocalBean
public class SituationsBean {

	public static SetMultimap<String, Triple> cache_patterns;
	public static SetMultimap<String, String> cache_key_entities;
	public static SetMultimap<String, String> cache_key_entities_superclasses;

	public SituationsBean() {

	}

//	@PostConstruct
	public void initialisation()
			throws RepositoryConfigException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		System.err.println("Situations bean initialisation");
		cache_patterns = HashMultimap.create();
		cache_key_entities = HashMultimap.create();
		cache_key_entities_superclasses = HashMultimap.create();

		GraphDbRepositoryManager manager = null;
		RepositoryConnection kb = null;

		manager = new GraphDbRepositoryManager(AdminBean.serverUrl, AdminBean.username, AdminBean.password);
		Repository repository = manager.getRepository("sleep");
		if (repository == null) {
			throw new RuntimeException("Cannot find a repository with name: " + AdminBean.getUsername());
		}
		kb = repository.getConnection();

		// >>> find all situations
		HashSet<String> situations = new HashSet<>();
		String q1 = "" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX dul: <http://www.loa-cnr.it/ontologies/DUL.owl#>\r\n"
				+ "PREFIX context: <http://kristina-project.eu/ontologies/la/context#>" + ""
				+ "select ?s where {?s a context:Situation .}";

		TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb, q1);
		while (result.hasNext()) {
			String s = result.next().getValue("s").stringValue();
			System.out.println(s);
			situations.add(s);
		}

		// >>> collect all relevant triples for a situation
		String q2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX context: <http://kristina-project.eu/ontologies/la/context#>\r\n"
				+ "PREFIX onto: <http://kristina-project.eu/ontologies/la/onto#>\r\n" + "construct {\r\n"
				+ "    ?node ?p ?o .\r\n" + "}\r\n" + "\r\n" + "where {\r\n" + "    ?node ?p ?o .\r\n"
				+ "    FILTER (contains(str(?p), str(context:)) ||  contains(str(?p), str(onto:)))  \r\n" + "}";

		for (String s : situations) {
			LinkedList<Value> agenda = new LinkedList<>();
			HashSet<Value> visited = new HashSet<>();
			visited.add(kb.getValueFactory().createURI(s));
			GraphQueryResult g = QueryUtils.evaluateConstructQuery(kb, q2,
					new BindingImpl("node", kb.getValueFactory().createURI(s)));
			while (g.hasNext()) {
				Statement next = g.next();
				cache_patterns.put(s, new Triple(next.getSubject().stringValue(), next.getPredicate().stringValue(),
						next.getObject().stringValue()));
				addToAgenda(next.getObject(), agenda);
			}

			while (!agenda.isEmpty()) {
				Value pop = agenda.pop();
				if (visited.contains(pop))
					continue;
				else
					visited.add(pop);
				GraphQueryResult g2 = QueryUtils.evaluateConstructQuery(kb, q2, new BindingImpl("node", pop));
				while (g2.hasNext()) {
					Statement next = g2.next();
					cache_patterns.put(s, new Triple(next.getSubject().stringValue(), next.getPredicate().stringValue(),
							next.getObject().stringValue()));
					addToAgenda(next.getObject(), agenda);
				}
			}
		}

		// remove redundant cache entries
		Set<String> keys = cache_patterns.keySet();
		Set<String> toRemove = new HashSet<>();
		for (String s1 : keys) {
			for (String s2 : keys) {
				if (s1.equals(s2))
					continue;
				Set<Triple> triples = cache_patterns.get(s2);
				for (Triple t : triples) {
					if (t.getS().equals(s1) || t.getO().equals(s1)) {
						toRemove.add(s1);
						break;
					}
				}
			}

		}
		System.err.println("to remove");
		System.out.println(Utils.flattenCollection(toRemove));
		for (String s : toRemove) {
			cache_patterns.removeAll(s);
		}

		// >>> add rdf:types
		for (String s : situations) {
			Set<Triple> triples = cache_patterns.get(s);
			Set<Triple> types = new HashSet<>();
			for (Triple t : triples) {
				// subject
				URI subject = kb.getValueFactory().createURI(t.getS());
				String directType = getDirectType(kb, subject);
				if (directType != null) {
					types.add(new Triple(subject.stringValue(), RDF.TYPE.stringValue(), directType));
				}

				// object
				try {
					URI object = kb.getValueFactory().createURI(t.getO());
					directType = getDirectType(kb, object);
					if (directType != null) {
						types.add(new Triple(object.stringValue(), RDF.TYPE.stringValue(), directType));
					}
				} catch (Exception e) {
				}
			}
			triples.addAll(types);
		}

		System.out.println("cache_patterns");
		Utils.printMap(cache_patterns);

		// collect key entities
		for (String s : cache_patterns.keySet()) {
			Set<Triple> triples = cache_patterns.get(s);
			for (Triple t : triples) {
				if (t.getP().equals(RDF.TYPE.stringValue())) {
					String obj = t.getO();
					if (obj.contains(Namespaces.LA_ONTO)) {
						cache_key_entities.put(s, obj);
						cache_key_entities_superclasses.putAll(s, HierarchyBean.hierarchy.get(obj));
					}
				}
			}

		}
		System.out.println("cache_key_entities");
		Utils.printMap(cache_key_entities);
	}

	// public static HashSet<String> getRelevantSituation(String keyEntity) {
	// HashSet<String> situations = new HashSet<>();
	// if (keyEntity.equals(Namespaces.LA_ONTO + "CareRecipient"))
	// return situations;
	// for (String s : cache_key_entities.keySet()) {
	// if (cache_key_entities.get(s).contains(keyEntity)
	// || cache_key_entities_superclasses.get(s).contains(keyEntity)) {
	// situations.add(s);
	// }
	// }
	// return situations;
	// }

	public static List<Result> getMatchedSituations(Set<String> keyEntities) {
		List<Result> result = new ArrayList<>();
		for (String s : cache_key_entities.keySet()) {
			Set<String> _keyEntities = cache_key_entities.get(s);
			_keyEntities.addAll(cache_key_entities_superclasses.get(s));
			HashSet<String> common = new HashSet<>(keyEntities);
			common.retainAll(_keyEntities);
			if (!common.isEmpty() && !(common.contains(Namespaces.LA_ONTO + "CareRecipient") && common.size() ==1)) {
				Result r = new SituationsBean.Result(s, common, keyEntities, cache_key_entities.get(s).size(),
						cache_key_entities_superclasses.get(s).size());
				result.add(r);
			}

		}

		Collections.sort(result, new Comparator<Result>() {

			public int compare(Result o1, Result o2) {

				Double x1 = o1.score;
				Double x2 = o2.score;
				int sComp = x1.compareTo(x2);

				if (sComp != 0) {
					return sComp;
				} else {
					x1 = o1.rank;
					x2 = o2.rank;
					return x1.compareTo(x2);
				}
			}
		});
		Collections.reverse(result);
		for (int i = 0; i < result.size(); i++) {
			result.get(i).order = i + 1;
		}
		return result;
	}

	private String getDirectType(RepositoryConnection kb, Value resource)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		String direct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + //
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + //
				"SELECT DISTINCT ?directClass\r\n" + //
				"WHERE {\r\n" + //
				"    ?arg sesame:directType ?directClass . "//
				+ "	 FILTER(?directClass != owl:NamedIndividual) " + //
				"}";

		TupleQuery directQuery = kb.prepareTupleQuery(QueryLanguage.SPARQL, direct);

		directQuery.setBinding("arg", resource);
		TupleQueryResult result = directQuery.evaluate();
		if (result.hasNext()) {
			return result.next().getBinding("directClass").getValue().stringValue();
		}

		return null;
	}

	private void addToAgenda(Value v, LinkedList<Value> agenda) throws RepositoryException {
		if (!"LiteralImpl".equals(v.getClass().getSimpleName())
		/*
		 * &&
		 * !kbConnection.hasStatement(vf.createURI(v.getValue().stringValue()),
		 * RDF.TYPE, OWL.CLASS, true)
		 */) {
			if (!agenda.contains(v))
				agenda.add(v);
		}
	}

	// public OntClass getDirectSuperClass (String c){
	// OntClass ontClass = model.getOntClass(c);
	// return ontClass.listSuperClasses(false).toList();
	// }

	public static class Result {
		public String situation;
		public Set<String> matchedKeyEntities;
		public Set<String> queryKeyEntities;
		public int totalKeyEntities;
		public int totalKeyEntitiesSuperclasses;
		public double score; // matched entities / query entities
		public double rank; // matched entities / total entities
		public int order;
		// public Set<Triple> triples;

		public Result(String situation, Set<String> matchedKeyEntities, Set<String> queryKeyEntities,
				int totalKeyEntities, int totalKeyEntitiesSuperclasses) {
			super();
			this.situation = situation;
			this.matchedKeyEntities = matchedKeyEntities;
			this.queryKeyEntities = queryKeyEntities;
			this.totalKeyEntities = totalKeyEntities;
			this.totalKeyEntitiesSuperclasses = totalKeyEntitiesSuperclasses;
			// this.triples = cache_patterns.get(this.situation);

			this.score = (double) this.matchedKeyEntities.size() / (double) this.queryKeyEntities.size();
			this.rank = (double) this.matchedKeyEntities.size() / (double) this.totalKeyEntities;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			Gson g = new GsonBuilder().setPrettyPrinting().create();
			return "Result: " + g.toJson(this) + Utils.flattenCollection(cache_patterns.get(this.situation));
		}

	}

}
