package gr.iti.kristina.startup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

//@Startup
@Singleton
@LocalBean
public class ContextBean {

	// static final String ONTO_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/onto.ttl";
	// static final String LA_CTX_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/context.ttl";

	public static OntModel infModel;

	public static Multimap<OntClass, Object> context;

	public ContextBean() {
	}

	@PostConstruct
	public void initialisation() {
		System.out.println("Context bean initialisation");
		context = HashMultimap.create();
		OntModel tempModel = Utils.createDefaultModel(false);
		tempModel.read(Namespaces.KB_CONTEXT_FILE, "TURTLE");
		tempModel.read(Namespaces.LA_ONTO_FILE, "TURTLE");
		infModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		infModel.setDynamicImports(false);
		infModel.add(tempModel);
		infModel.prepare();

		// System.out.println(Utils.modelToString(infModel, "N-TRIPLE"));

		List<OntClass> classes = infModel.listNamedClasses().toList();
		for (OntClass namedClass : classes) {
			if (!namedClass.getURI().contains("context-light") || namedClass.getLocalName().equals("IndividualContext"))
				continue;

			System.out.println("------------------------------------------------------------------");
			System.out.println("starting class: " + namedClass.getLocalName());
			List<OntClass> equivalentClasses = namedClass.listEquivalentClasses().toList();

			LinkedList<OntClass> localAgenda = new LinkedList<OntClass>();
			localAgenda.addAll(equivalentClasses);

			while (!localAgenda.isEmpty()) {
				OntClass equiv = localAgenda.pop();
				if (namedClass.equals(equiv))
					continue;
				// System.out.println("popped: " + equiv.getLocalName());
				if (equiv.canAs(IntersectionClass.class)) {
					// System.out.println("IntersectionClass");
					IntersectionClass intersectionClass = equiv.asIntersectionClass();
					RDFList _operands = intersectionClass.getOperands();
					List<RDFNode> operands = _operands.asJavaList();
					for (RDFNode _op : operands) {
						OntClass op = _op.as(OntClass.class);
						if (op.isRestriction()) {
							Resource someValuesFrom = op.as(SomeValuesFromRestriction.class).getSomeValuesFrom();
							if (someValuesFrom.canAs(OntClass.class)) {
								OntClass someValuesFromOntClass = someValuesFrom.as(OntClass.class);
								if (!someValuesFromOntClass.isAnon()) {
									System.out.println(someValuesFromOntClass.getLocalName());
									context.put(namedClass, someValuesFromOntClass);
								} else {
									print(someValuesFromOntClass.asUnionClass().listOperands().toList());
									context.put(namedClass,
											someValuesFromOntClass.asUnionClass().listOperands().toList());
								}
							}
						} else /* if (!op.isAnon()) */ {
							if (op.isAnon()) {
								// see sleephiegene context
								// System.out.println(op.canAs(IntersectionClass.class));
								List<Resource> collect = op.asUnionClass().getOperands().asJavaList().stream()
										.map(p -> p.as(SomeValuesFromRestriction.class).getSomeValuesFrom())
										.collect(Collectors.toList());
								print(collect);
								context.put(namedClass, collect);

							} else if (op.getURI().contains("context-light")) {
								// System.out.println("readded: " + op );
								localAgenda.addAll(op.listEquivalentClasses().toList());
							} else {
								System.out.println("class found: " + op.getLocalName());
								context.put(namedClass, op);
							}

						}
					}
				} else if (equiv.canAs(SomeValuesFromRestriction.class)) {
					// System.out.println("SomeValuesFromRestriction");
					Resource someValuesFrom = equiv.as(SomeValuesFromRestriction.class).getSomeValuesFrom();
					if (someValuesFrom.canAs(OntClass.class)) {
						OntClass someValuesFromOntClass = someValuesFrom.as(OntClass.class);
						if (!someValuesFromOntClass.isAnon()) {
							System.out.println(someValuesFromOntClass.getLocalName());
							context.put(namedClass, someValuesFromOntClass);
						} else {
							print(someValuesFromOntClass.asUnionClass().listOperands().toList());
							context.put(namedClass, someValuesFromOntClass.asUnionClass().listOperands().toList());
						}

					}
				}
			}
		}
	}

	private Set<OntClass> getTopicsByKeyEntity(OntClass keyEntity) {
		Set<OntClass> keys = context.keySet();
		Set<OntClass> topics = new HashSet<>();
		keyEntity = infModel.getOntClass(keyEntity.getURI());
		if (keyEntity == null)
			return topics;
		for (OntClass t : keys) {
			t = infModel.getOntClass(t.getURI());
			Collection<Object> values = context.get(t);
			for (Object v : values) {
				if (v instanceof OntClass) {
					OntClass v2 = infModel.getOntClass(((OntClass) v).getURI());
					if (v2 == null)
						continue;
					if (v2.hasSubClass(keyEntity) || v2.hasEquivalentClass(keyEntity)) {
						topics.add(t);
					}
				} else {
					for (OntClass c2 : (List<OntClass>) v) {
						c2 = infModel.getOntClass(c2.getURI());
						if (c2.hasEquivalentClass(keyEntity) || c2.hasSubClass(keyEntity)) {
							topics.add(t);
							break;
						}
					}
					// if (((List<OntClass>) v).contains(keyEntity)) {
					// topics.add(t);
					// }
				}
			}
		}
		// System.out.println(keyEntity);
		// System.out.println(topics.size());
		return topics;
	}

	public OntClass match(List<Resource> keyConcepts) {
		ArrayListMultimap<OntClass, OntClass> votings = ArrayListMultimap.create();

		for (Resource keyConcept : keyConcepts) {
			Set<OntClass> topicsByKeyEntity = this.getTopicsByKeyEntity(keyConcept.as(OntClass.class));
			System.out.println(keyConcept + " - " + Utils.flattenCollection(topicsByKeyEntity));
			for (OntClass ontClass : topicsByKeyEntity) {
				if (!votings.containsEntry(ontClass, keyConcept.as(OntClass.class)))
					votings.put(ontClass, keyConcept.as(OntClass.class));
			}
		}
		System.out.println("votings");
		Utils.printMap(votings);

		// remove superclasses
		Set<OntClass> keySet = votings.keySet();
		HashSet<OntClass> toremove = new HashSet<>();
		for (OntClass c1 : keySet) {
			boolean found = false;
			c1 = infModel.getOntClass(c1.getURI());
			for (OntClass c2 : keySet) {
				c2 = infModel.getOntClass(c2.getURI());
				if (c1.hasEquivalentClass(c2))
					continue;
				if (c1.hasSubClass(c2)) {
					found = true;
				}
			}
			if (found) {
				toremove.add(c1);
			}
		}
		// System.out.println(Utils.flattenCollection(toremove));
		for (OntClass ontClass : toremove) {
			votings.removeAll(ontClass);
		}

		System.out.println("---------------------------- PARTIAL MATCHES ------------------------ ");
		// System.out.println(Utils.flattenCollection(fin));
		Multimap<OntClass, OntClass> sortedByDescendingFrequency = sortedByDescendingFrequency(votings);
		if (sortedByDescendingFrequency.isEmpty())
			return null;
		Utils.printMap(sortedByDescendingFrequency);

		/*
		 * Need to take first the more frequent ones and then compute the
		 * score!!!!
		 */

		OntClass next = sortedByDescendingFrequency.keySet().iterator().next();
		int max = (int) sortedByDescendingFrequency.get(next).stream().map(p -> p).count();
		System.out.println("MAX:" + max);
		if (max == 1 && keyConcepts
				.contains(Utils.createDefaultModel(false).createResource(Namespaces.LA_ONTO + "CareRecipient"))) {
			return null;
		}

		// divide by number of key concepts
		keySet = votings.keySet();
		HashMap<OntClass, Double> scores = new HashMap<>();
		for (OntClass ontClass : keySet) {
			List<OntClass> list = (List<OntClass>) votings.get(ontClass);
			int sum = (int) list.stream().map(p -> p).count();
			if (sum != max)
				continue;
			double s = (double) sum / (double) context.get(ontClass).size();
			if(s < 0.5)
				continue;
			scores.put(ontClass, s > 1.0 ? 1.0 : s);
		}
		// System.out.println(scores);
		Map<OntClass, Double> sortByValue = sortByValue(scores);
		System.out.println(sortByValue);

		return sortByValue.keySet().iterator().next();

		// Utils.printMap(sortedByDescendingFrequency.keySet().iterator().next());

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public Multimap<OntClass, OntClass> sortedByDescendingFrequency(Multimap<OntClass, OntClass> multimap) {
		return ImmutableMultimap.<OntClass, OntClass> builder().orderKeysBy(descendingCountOrdering(multimap.keys()))
				.putAll(multimap).build();
	}

	private static Ordering<OntClass> descendingCountOrdering(final Multiset<OntClass> multiset) {
		return new Ordering<OntClass>() {
			@Override
			public int compare(OntClass left, OntClass right) {
				return Ints.compare(multiset.count(left), multiset.count(right));
			}
		}.reverse();
	}

	public void print(List<? extends Resource> list) {
		System.out.print("[ ");
		for (Resource r : list) {
			System.out.print(r.getLocalName() + " , ");
		}
		System.out.println("] ");
	}

	// public List<OntClass> getSuperClasses(String c) {
	// OntClass ontClass = model.getOntClass(c);
	// return ontClass.listSuperClasses(false).toList();
	// }

	// public OntClass getDirectSuperClass (String c){
	// OntClass ontClass = model.getOntClass(c);
	// return ontClass.listSuperClasses(false).toList();
	// }

	public static void main(String[] args) {
		// ContextBean b = new ContextBean();
		// b.initialisation();
		// Utils.printMap(b.context);

		Multimap<String, Integer> votings = ArrayListMultimap.create();
		votings.put("sss", 1);
		votings.put("sss", 1);
		System.out.println(votings.toString());

	}

}
