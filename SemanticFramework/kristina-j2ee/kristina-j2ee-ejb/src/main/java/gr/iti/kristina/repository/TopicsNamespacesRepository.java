package gr.iti.kristina.repository;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;

import com.hp.hpl.jena.rdf.model.Resource;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.utils.QueryUtils;

/**
 * Session Bean implementation class TopicsNamespaces
 */

public class TopicsNamespacesRepository {

	private RemoteRepositoryManager _manager;
	private Repository repository;

	static public final String id = "topics-namespaces";
	public static RepositoryConnection kbConnection;

	public TopicsNamespacesRepository() {
		try {
			_manager = new RemoteRepositoryManager(AdminBean.serverUrl);
			_manager.setUsernameAndPassword(AdminBean.username, AdminBean.password);
			_manager.initialize();
			repository = _manager.getRepository(id);
			kbConnection = repository.getConnection();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<String> getDirectNamespaces(Resource topic)
			throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		return getDirectNamespaces(topic.getURI());
	}

	public Set<String> getDirectNamespaces(String topic)
			throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		Set<String> result = new HashSet<String>();
		String q = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + //
				"PREFIX context: <http://kristina-project.eu/ontologies/context-light#>\r\n" + //
				"PREFIX : <http://kristina-project.eu/ontologies/topics-namespaces#> \r\n" + //
				"select distinct ?i where { \r\n" + //
				"    ?i sesame:directType ?x .\r\n" + //
				"    ?i sesame:directType :Namespace .\r\n" + //
				"    FILTER (!isBlank(?x))\r\n" + //
				"}";

		TupleQueryResult evaluateSelectQuery = QueryUtils.evaluateSelectQuery(kbConnection, q,
				new BindingImpl("x", kbConnection.getValueFactory().createURI(topic)));
		while (evaluateSelectQuery.hasNext()) {
			BindingSet next = evaluateSelectQuery.next();
			Binding binding = next.getBinding("i");
			result.add(binding.getValue().stringValue());
		}
		evaluateSelectQuery.close();

		return result;
	}

	public HashSet<String> getAllNamespaces(String topic) {
		return null;
	}

	public void shutDown(String CONTEXT) {
		System.out.println("closing GraphDb manager [" + CONTEXT + "]");
		if (_manager != null) {
			try {
				_manager.shutDown();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
