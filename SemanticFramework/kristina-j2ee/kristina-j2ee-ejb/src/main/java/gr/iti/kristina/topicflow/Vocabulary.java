package gr.iti.kristina.topicflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import gr.iti.kristina.model.Namespaces;
import info.debatty.java.stringsimilarity.Cosine;

public class Vocabulary {

	Set<String> ontoClassNames;

	public Vocabulary() {
		ontoClassNames = new HashSet<>();

		OntModel model = ModelFactory.createOntologyModel();
		model.read(Namespaces.ONTOLOGY_FOLDER + "onto.ttl", "TURTLE");
		model.listClasses().toList().forEach(c -> {
			if (!c.isAnon())
				ontoClassNames.add(c.getLocalName().toLowerCase());
		});

	}

	public Set<String> match(String text) {
		HashSet<String> tokens = Sets.newHashSet(Arrays.asList(text.split(" ")));
		Cosine lcs = new Cosine();

		HashMap<String, String> results = new HashMap<>();

		for (String t : tokens) {
			double distance = 2;
			for (String cl : ontoClassNames) {
				double _d = lcs.distance(t, cl);
				if (_d < distance) {
					distance = _d;
					results.put(t, cl);
				}
			}
		}
		System.out.println(results);
		return null;
	}

	public static void main(String[] args) {

		Vocabulary v = new Vocabulary();
		v.match("I want to know the sleeping habits of care recipient");

	}

}
