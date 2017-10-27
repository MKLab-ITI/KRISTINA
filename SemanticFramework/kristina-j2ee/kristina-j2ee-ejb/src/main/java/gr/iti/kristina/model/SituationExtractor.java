package gr.iti.kristina.model;

import java.util.HashSet;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import gr.iti.kristina.utils.QueryUtils;

public class SituationExtractor {

	RepositoryConnection kb;

	public SituationExtractor(RepositoryConnection kbConnection) {
		this.kb = kbConnection;
	}

	public HashSet<String> getRelevantSituations(HashSet<String> keyConcepts)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		System.out.println("Situation extractor key entities: " + keyConcepts);
		HashSet<String> situations = new HashSet<>();
		String getSituationsQuery = "" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX dul: <http://www.loa-cnr.it/ontologies/DUL.owl#>\r\n"
				+ "PREFIX context: <http://kristina-project.eu/ontologies/la/context#>\r\n"
				+ "select distinct ?situation\r\n" + "where{\r\n" + "    ?x rdf:type ?keyentity. \r\n" + "    {\r\n"
				+ "		?concept context:classifies ?x.\r\n" + "        ?description context:defines ?concept.\r\n"
				+ "        ?situation context:satisfies ?description .\r\n" + "    }\r\n" + "    UNION \r\n"
				+ "    {\r\n" + "		?description context:defines ?x.\r\n"
				+ "        ?situation context:satisfies ?description .\r\n" + "    }\r\n" + "    \r\n" + "}";

		for (String keyConcept : keyConcepts) {
			if (keyConcept.equals("http://kristina-project.eu/ontologies/la/onto#CareRecipient"))
				continue;
			TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb, getSituationsQuery,
					new BindingImpl("keyentity", kb.getValueFactory().createURI(keyConcept)));

			while (result.hasNext()) {
				BindingSet next = result.next();
				Value situationValue = next.getBinding("situation").getValue();
				situations.add(situationValue.stringValue());
			}
		}

		return situations;

	}

}
