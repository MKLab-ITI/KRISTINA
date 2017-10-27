package gr.iti.kristina.topicflow;

import java.util.Arrays;
import java.util.HashSet;

import gr.iti.kristina.model.Namespaces;

public class Main {

	public static void main(String[] args) throws Exception {

		// Set<OntClass> classesWithKey =
		// m.getClassesWithKey(m.infModel.getOntClass(Namespaces.LA_ONTO +
		// "Benefits"));
		// System.out.println(Utils.flattenCollection(classesWithKey));
		//
		// System.out.println(m.getAbstractTopics(m.infModel.getOntClass(Namespaces.CORE
		// + "More")));

		HashSet<String> laConncepts = new HashSet<>();
		laConncepts.addAll(Arrays.asList(
				 Namespaces.LA_ONTO + "Breastfeeding",
				 Namespaces.LA_ONTO + "More"
				// Namespaces.LA_ONTO + "SocialMedia",
//				Namespaces.LA_ONTO + "Benefits"
				));

		HashSet<String> themesHistory = new HashSet<>();
		// themesHistory.addAll(Arrays.asList(
		// "http://kristina-project.eu/ontologies/models/uc2_breastfeeding#BreastFeeding"));

		HashSet<String> topicsHistory = new HashSet<>();
		HashSet<String> laConceptsHistory = new HashSet<>();

//		Workflow wf = new Workflow(laConncepts, themesHistory, topicsHistory, laConceptsHistory);
//		wf.start(new HashSet<String>());
	}

}
