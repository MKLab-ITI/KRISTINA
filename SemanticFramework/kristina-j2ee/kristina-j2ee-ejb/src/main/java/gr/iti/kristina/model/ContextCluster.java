package gr.iti.kristina.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;

import gr.iti.kristina.startup.HierarchyBean;
import gr.iti.kristina.utils.Utils;

public class ContextCluster implements Serializable, Comparable<ContextCluster> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashSet<Triple> triples;
	private double score;
	private double rank;
	private int numberOfConcepts;

	public ContextCluster() {
		triples = new HashSet<>();
	}

	public void add(Triple t) {
		triples.add(t);
	}

	public void addAll(HashSet<Triple> triples) {
		this.triples.addAll(triples);
	}

	public HashSet<Triple> getTriples() {
		return triples;
	}

	public void setTriples(HashSet<Triple> triples) {
		this.triples = triples;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((triples == null) ? 0 : triples.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContextCluster other = (ContextCluster) obj;
		if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
			return false;
		if (triples == null) {
			if (other.triples != null)
				return false;
		} else if (!triples.equals(other.triples))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ContextCluster{" + "triples=" + Utils.flattenCollection(triples) + ", score=" + score + ", rank=" + rank
				+ ", types=" + getNumberOfConcepts() + "}";
	}

	public void calculateContextClusterScore(Multimap<Signature, String> mappings, RepositoryConnection kbConnection,
			HierarchyBean hb)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException, NamingException {
		HashMap<String, String> directTypes = new HashMap<>();
		HashSet<String> agenda = new HashSet<>();
		ValueFactory vf = kbConnection.getValueFactory();

		// InitialContext jndi = new InitialContext();
		// HierarchyBean hb = (HierarchyBean)
		// jndi.lookup("java:app/kristina-j2ee-ejb/HierarchyBean");

		// System.err.println("HierarchyBean::::: " + hb.hierarchy);

		// String q = "" + "SELECT ?type " //
		// + "WHERE {" //
		// + " ?_node a ?type ." //
		// + " FILTER(!isBlank(?type))." //
		// + "" //
		// + "" //
		// + "}"; //
		// TupleQuery superClassesQuery =
		// kbConnection.prepareTupleQuery(QueryLanguage.SPARQL, q);

		// String direct = "PREFIX rdfs:
		// <http://www.w3.org/2000/01/rdf-schema#>\r\n" + //
		// "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + //
		// "SELECT DISTINCT ?directClass\r\n" + //
		// "WHERE {\r\n" + //
		// " ?arg a ?directClass .\r\n" + //
		// " FILTER (!isBlank(?directClass) && ?directClass !=
		// owl:NamedIndividual ) .\r\n" + //
		// " FILTER NOT EXISTS {\r\n" + //
		// " ?temp a owl:Class .\r\n" + //
		// " FILTER (((?temp != owl:NamedIndividual) && (?temp != ?directClass))
		// && (!isBlank(?temp))) .\r\n"
		// + //
		// " ?arg1 a ?temp .\r\n" + //
		// " ?temp rdfs:subClassOf ?directClass .\r\n" + //
		// " } .\r\n" + //
		// "}";

		String direct = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + //
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + //
				"SELECT DISTINCT ?directClass\r\n" + //
				"WHERE {\r\n" + //
				"    ?arg sesame:directType ?directClass . "//
				+ "	 FILTER(?directClass != owl:NamedIndividual) " + //
				"}";

		TupleQuery directQuery = kbConnection.prepareTupleQuery(QueryLanguage.SPARQL, direct);

		System.out.println("calculateContextClusterScore");
		Collection<String> mapping_values = mappings.values();
		HashSet<String> contextTypes = new HashSet<>();
		Stopwatch timer = Stopwatch.createStarted();
		// Stopwatch timer2 = Stopwatch.createStarted();

		for (Triple t : triples) {
			URI ts = vf.createURI(t.s);
			if (!agenda.contains(t.s)) { // in order not to query again for the
											// same resources...
				agenda.add(t.s);
				// superClassesQuery.setBinding("_node", ts);
				// TupleQueryResult result = superClassesQuery.evaluate();
				// while (result.hasNext()) {
				// BindingSet bindingSet = result.next();
				// String stringValue =
				// bindingSet.getBinding("type").getValue().stringValue();
				// // String stringValue = type.getValue().stringValue();
				// contextTypes.add(stringValue);
				// // resources.add(t.s);
				// }
				directQuery.setBinding("arg", ts);
				TupleQueryResult result = directQuery.evaluate();
				if (result.hasNext()) {
					String stringValue = result.next().getBinding("directClass").getValue().stringValue();
					directTypes.put(t.s, stringValue);
					Collection<String> collection = hb.hierarchy.get(stringValue);
					if (collection != null) {
						contextTypes.add(stringValue);
						contextTypes.addAll(hb.hierarchy.get(stringValue));
					}

				}
			}

			// System.err.println("a1: " + timer2);
			// timer2 = Stopwatch.createStarted();

			try {
				// result = QueryUtils.evaluateSelectQuery(kbConnection, q, new
				// BindingImpl("_node", vf.createURI(t.o)));
				// superClassesQuery.clearBindings();
				URI to = vf.createURI(t.o);
				if (!agenda.contains(t.o)) {
					agenda.add(t.o);
					// superClassesQuery.setBinding("_node", to);
					// TupleQueryResult result = superClassesQuery.evaluate();
					// while (result.hasNext()) {
					// BindingSet bindingSet = result.next();
					// String stringValue =
					// bindingSet.getBinding("type").getValue().stringValue();
					// contextTypes.add(stringValue);
					// // resources.add(t.o);
					// }
					directQuery.setBinding("arg", to);
					TupleQueryResult result = directQuery.evaluate();
					if (result.hasNext()) {
						// directTypes.put(t.o,
						// result.next().getBinding("directClass").getValue().stringValue());
						String stringValue = result.next().getBinding("directClass").getValue().stringValue();
						directTypes.put(t.o, stringValue);
						Collection<String> collection = hb.hierarchy.get(stringValue);
						if (collection != null) {
							contextTypes.add(stringValue);
							contextTypes.addAll(hb.hierarchy.get(stringValue));
						}
					}
				}

				// System.err.println("a2: " + timer2);

			} catch (IllegalArgumentException | QueryEvaluationException e) {
				System.err.println("error:::" + e.getMessage());
				continue;

			} finally {
				// if (result != null)
				// result.close();
			}
		}
		System.err.println("a: " + timer);
		// System.out.println("context types:");
		// System.out.println(Utils.flattenCollection(contextTypes));
		// System.out.println("resource types:");
		// Utils.printMap(resourceTypes);
		// System.out.println("direct types:");
		// System.out.println(directTypes);

		timer = Stopwatch.createStarted();

		int numberOfMatches = 0;
		for (String mapping_value : mapping_values) {
			// System.out.println("mapping value: " + mapping_value);
			if (contextTypes.contains(mapping_value)) {
				numberOfMatches++;
			}

		}
		this.score = numberOfMatches;
		this.setNumberOfConcepts(new HashSet<String>(directTypes.values()).size());
		this.rank = (double) this.score / (double) this.numberOfConcepts;
		System.err.println("b: " + timer);

		/*
		 * POST PROCESSING
		 */

		// insert type triples
		// Multiset<String> resources = resourceTypes.keys();
		timer = Stopwatch.createStarted();
		// for (String r : resources) {
		// // Set<String> types = resourceTypes.get(r);
		// // System.out.println(Utils.flattenCollection(types));
		// // Collection<String> clean =
		// // Utils.removeUpperClasses(Utils.removeSuperClasses(types,
		// // kbConnection));
		// String tt = directTypes.get(r);
		// if (tt != null) {
		// Triple triple = new Triple(r, RDF.TYPE.toString(), tt);
		// triples.add(triple);
		// }
		// }

		// insert missing types
		Set<String> resources = directTypes.keySet();
		for (String r : resources) {
			// System.out.println("---------------" + r);
			Triple triple = new Triple(r, RDF.TYPE.toString(), directTypes.get(r));
			triples.add(triple);
		}

		System.out.println("c: " + timer);

		// insert description defines assertions
		// URI defines = vf.createURI(LAParser.LA_CONTEXT_NS + "defines");
		// URI classifies = vf.createURI(LAParser.LA_CONTEXT_NS + "classifies");
		List<Triple> temp = new ArrayList<>();
		String q3 = "CONSTRUCT {\r\n" + //
				"    ?arg <http://kristina-project.eu/ontologies/la/context#defines> ?d .\r\n" + //
				"    ?d <http://kristina-project.eu/ontologies/la/context#classifies> ?c .\r\n" + //
				"}\r\n" + //
				"WHERE {\r\n" + //
				"	?arg <http://kristina-project.eu/ontologies/la/context#defines> ?d .\r\n" + //
				"	OPTIONAL {\r\n" + //
				"		?d <http://kristina-project.eu/ontologies/la/context#classifies> ?c .\r\n" + //
				"	}\r\n" + //
				"}";

		GraphQuery prepareGraphQuery = kbConnection.prepareGraphQuery(QueryLanguage.SPARQL, q3);

		Set<Value> extraResources = new HashSet<>();
		timer = Stopwatch.createStarted();
		for (Triple t : triples) {
			if (t.o.equals(Namespaces.LA_CONTEXT + "ContextDescription")) {
				// prepareGraphQuery.clearBindings();
				prepareGraphQuery.setBinding("arg", vf.createURI(t.s));
				GraphQueryResult evaluate = prepareGraphQuery.evaluate();

				while (evaluate.hasNext()) {
					Statement next = evaluate.next();
					Triple t1 = new Triple(next.getSubject().stringValue(), next.getPredicate().stringValue(),
							next.getObject().stringValue());
					temp.add(t1);
					extraResources.add(next.getSubject());
					extraResources.add(next.getObject());
					System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>in for extra");
				}
//				evaluate.close();
			}
		}
		triples.addAll(temp);
		System.err.println("d: " + timer);
		// System.out.println("TRIPLES ADDED: " + temp);

		for (Value v : extraResources) {
			System.out.println("extra:" + v);
			directQuery.setBinding("arg", v);
			TupleQueryResult result = directQuery.evaluate();

			if (result.hasNext()) {
				String stringValue = result.next().getBinding("directClass").getValue().stringValue();
				Triple triple = new Triple(v.stringValue(), RDF.TYPE.toString(), stringValue);
				triples.add(triple);
			}
//			result.close();
		}

	}

	// @Override
	// public int compare(ContextCluster o1, ContextCluster o2) {
	// if (o1.getScore() < o2.getScore())
	// return -1;
	// else if (o1.getScore() > o2.getScore())
	// return 1;
	// else if (o1.getScore() == o2.getScore())
	// return 0;
	// else
	// return 1;
	// }
	//
	@Override
	public int compareTo(ContextCluster o2) {
		// System.out.println(this.getScore() + " - " + o2.getScore());
		if (this.getScore() < o2.getScore())
			return -1;
		else if (this.getScore() > o2.getScore())
			return 1;
		else if (this.equals(o2))
			return 0;
		else
			return 1;
	}

	public int getNumberOfConcepts() {
		return numberOfConcepts;
	}

	public void setNumberOfConcepts(int numberOfConcepts) {
		this.numberOfConcepts = numberOfConcepts;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	// public static void main(String[] args) {
	// ContextCluster c1 = new ContextCluster();
	// c1.score = 2;
	//
	// ContextCluster c2 = new ContextCluster();
	// c2.score = 1;
	//
	// System.out.println(c2.compareTo(c1));
	//
	// TreeSet<ContextCluster> set = new TreeSet<>();
	// set.add(c1);
	// set.add(c2);
	//
	// System.out.println(set);
	// }

}
