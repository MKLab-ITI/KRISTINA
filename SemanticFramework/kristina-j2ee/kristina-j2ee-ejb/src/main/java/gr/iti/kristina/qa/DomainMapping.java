package gr.iti.kristina.qa;

import java.util.HashSet;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gr.iti.kristina.model.Signature;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class DomainMapping
 */

public class DomainMapping {

	private final HashSet<Signature> concepts;
	// private final State state;
	private Multimap<Signature, String> mappings;

	// GraphDbRepositoryManager manager;
	// Repository kbRepository;
	RepositoryConnection kbConnection;
	ValueFactory vf;

	// JenaWrapper jenaWrapper;

//	final String serverUrl = "http://localhost:8084/graphdb-workbench-free", username = "kristina",
//			password = "samiam#2";
//	final String repositoryId = "kb";

	// Logger logger = LoggerFactory.getLogger(DomainMapping.class);

	// no connection provided
	// DomainMapping(HashSet<Signature> concepts, State state) throws
	// RepositoryConfigException, RepositoryException {
	// this.concepts = concepts;
	// this.state = state;
	//
	// manager = new GraphDbRepositoryManager(serverUrl, username, password);
	// kbConnection = manager.getRepository(repositoryId).getConnection();
	// vf = kbConnection.getValueFactory();
	//
	// jenaWrapper = new JenaWrapper(kbConnection);
	// }
	// reuse existing connection
	DomainMapping(HashSet<Signature> concepts, RepositoryConnection connection)
			throws RepositoryConfigException, RepositoryException {
		this.concepts = concepts;
		kbConnection = connection;
		vf = kbConnection.getValueFactory();
	}

	public Multimap<Signature, String> start()
			throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		mappings = HashMultimap.create();

		// for labels
		// String q = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
		// + "PREFIX f: <http://www.kristina.eu/sparql/functions/> " + "select
		// ?x ?s where { "
		// + " ?x rdfs:label ?label. " + " FILTER (lang(?label) = 'en' ||
		// lang(?label) = '') "
		// + " BIND (f:stringMatch(?label, ?node) as ?s) " + "} ORDER BY
		// DESC(?s) LIMIT 1";
		//
		// for uris
		String q = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
				+ "PREFIX f: <http://www.kristina.eu/sparql/functions/> " + "select ?x ?s where { " + "    ?x ?y ?z . "
				+ "    BIND (f:stringMatch(str(?x), ?node) as ?s) " + "} ORDER BY DESC(?s) LIMIT 1";

		for (Signature s : concepts) {
			System.out.println(String.format("processing key concept %s", s));

			// TODO need to remove the limit and try to disambiguate when >1
			// results...
			// TupleQueryResult result =
			// QueryUtils.evaluateSelectQuery(kbConnection, q,
			// new BindingImpl("node", vf.createLiteral(s.uri)));
			// while (result.hasNext()) {
			// BindingSet bindingSet = result.next();
			// URI x = (URI) bindingSet.getBinding("x").getValue();
			// System.err.println(s.label + " " + x);
			// mappings.put(s, x.toString());
			// }
			// result.close();
			mappings.put(s, s.uri);
		}
		System.out.println("print mapping: ");
		Utils.printMap(mappings);
		return mappings;
	}

	// public void close() {
	// logger.debug("closing domain matching module");
	// try {
	// if (kbConnection != null) {
	// kbConnection.close();
	// jenaWrapper.close();
	// manager.shutDown("DomainMatchingModule::");
	// }
	// } catch (RepositoryException ex) {
	// logger.debug("", ex);
	// }
	// }
	public Multimap<Signature, String> getMappings() {
		return mappings;
	}

}