package gr.iti.kristina.test.testcases;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntClass;

import gr.iti.kristina.utils.Utils;

public class Output {
	public String type;
	public Set<OntClass> topics;

	public Output(String type, Set<OntClass> topics) {
		super();
		this.topics = Sets.newHashSet();
		this.type = type;
		if (topics != null)
			this.topics.addAll(topics);
	}

	public Output(String type, OntClass topic) {
		super();
		this.topics = Sets.newHashSet();
		this.type = type;
		if (topic != null)
			this.topics.add(topic);
	}

	@Override
	public String toString() {
		return "Output [type=" + type + ", topics=" + Utils.getLocalNames(topics) + "]";
	}

}