package gr.iti.kristina.topicflow;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.utils.Utils;

public class State {

	public HashSet<OntClass> clarificationTopics = new HashSet<>();
	public HashSet<OntClass> clarificationThemes = new HashSet<>();
	public OntClass theme = null;
	public OntClass topic = null;
	public Set<Output> output = new HashSet<>();
	public Model responseModel;

	public HashSet<OntClass> laConcepts = new HashSet<>();

	public State clone() {
		State n = new State();
		n.clarificationThemes.addAll(this.clarificationThemes);
		n.clarificationTopics.addAll(this.clarificationTopics);
		n.theme = this.theme;
		n.topic = this.topic;
		n.laConcepts.addAll(this.laConcepts);
		return n;
	}

	@Override
	public String toString() {

		return "****************************************************** \n " +
				new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
						.append("clarificationThemes", Utils.getLocalNames(clarificationThemes))
						.append("clarificationTopics", Utils.getLocalNames(clarificationTopics))
						.append("theme", Utils.getLocalName(theme))
						.append("topic", Utils.getLocalName(topic))
						.append("laConcepts", Utils.getLocalNames(laConcepts))
						.append("output", output).toString()
				+ "\n****************************************************** \n";

		// return "\n\n ******************************************************
		// \n"
		// + "State [\n"
		// + " clarificationTopics=" + Utils.getLocalNames(clarificationTopics)
		// + ", \n"
		// // + " clarificationTheme=" + clarificationThemes + ", \n"
		// + " theme=" + theme.getLocalName() + ", \n"
		// + " topic=" + topic.getLocalName() + ", \n"
		// + " laConcepts=" + Utils.getLocalNames(laConcepts)
		// + "] \n ****************************************************** \n\n";

	}

}
