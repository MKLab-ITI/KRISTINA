package gr.iti.kristina.qa;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Resource;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.model.KeyEntityWrapper;
import gr.iti.kristina.model.Signature;
import gr.iti.kristina.repository.GraphDbRepositoryManager;
import gr.iti.kristina.startup.HierarchyBean;
import gr.iti.kristina.startup.SituationsBean;
import gr.iti.kristina.startup.SituationsBean.Result;

/**
 * Session Bean implementation class QuestionAnswerBean
 */
@Stateless
@LocalBean
public class QuestionAnswerBean2 {

	public List<Result> start(HashSet<KeyEntityWrapper> keyEntities, Collection<Resource> topics, HierarchyBean hb)
			throws RepositoryConfigException, RepositoryException, IOException, NamingException {

		// TODO topics are not used

		GraphDbRepositoryManager manager = null;
		RepositoryConnection kbConnection = null;
		try {
			manager = new GraphDbRepositoryManager(AdminBean.serverUrl, AdminBean.username, AdminBean.password);
			Repository repository = manager.getRepository(AdminBean.getUsername());
			if (repository == null) {
				throw new RuntimeException("Cannot find a repository with name: " + AdminBean.getUsername());
			}
			kbConnection = repository.getConnection();

			HashSet<Signature> concepts = Signature.createSignatures(keyEntities);
			Multimap<Signature, String> mappings = domainMapping(concepts, kbConnection);

			List<Result> relevantSituations2 = SituationsBean.getMatchedSituations(new HashSet<>(mappings.values()));

			return relevantSituations2;
		} catch (MalformedQueryException | QueryEvaluationException ex) {
			throw new RuntimeException("Cannot open repository: " + AdminBean.getUsername());
		} finally {
			if (kbConnection != null) {
				kbConnection.close();
				if (manager != null)
					manager.shutDown("QuestionAnswer:: shuding down...");
			}
		}
	}

	private Multimap<Signature, String> domainMapping(HashSet<Signature> concepts, RepositoryConnection kbConnection)
			throws RepositoryConfigException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		DomainMapping match = new DomainMapping(concepts, kbConnection);
		match.start();
		return match.getMappings();
	}

}
