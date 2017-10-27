package gr.iti.kristina.qa;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Resource;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.model.ContextBuilder;
import gr.iti.kristina.model.ContextCluster;
import gr.iti.kristina.model.KeyEntityWrapper;
import gr.iti.kristina.model.Signature;
import gr.iti.kristina.model.SituationExtractor;
import gr.iti.kristina.repository.GraphDbRepositoryManager;
import gr.iti.kristina.startup.HierarchyBean;
import gr.iti.kristina.startup.SituationsBean;
import gr.iti.kristina.startup.SituationsBean.Result;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class QuestionAnswerBean
 */
@Stateless
@LocalBean
public class QuestionAnswerBean {

	// final String serverUrl = "http://localhost:8084/graphdb-workbench-free",
	// username = "kristina",
	// password = "samiam#2";
	// final String repositoryId = "kb";

	//

	public TreeSet<ContextCluster> start(HashSet<KeyEntityWrapper> keyEntities, Collection<Resource> topics,
			HierarchyBean hb) throws RepositoryConfigException, RepositoryException, IOException, NamingException {

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

			// test
//			SituationExtractor s = new SituationExtractor(kbConnection);
//			HashSet<String> relevantSituations = s.getRelevantSituations(new HashSet<>(mappings.values()));
//			System.err.println("relevant situations");
//			System.out.println(Utils.flattenCollection(relevantSituations));

			//
			System.err.println("from cache");
			List<Result> relevantSituations2 = SituationsBean.getMatchedSituations(new HashSet<>(mappings.values()));
			System.out.println(relevantSituations2);
			// end test

			System.out.println("build contexts");
			TreeSet<ContextCluster> contextClusters = buildContexts(mappings, kbConnection, hb);
			return contextClusters;// rankContextClusters(contextClusters);
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

	private TreeSet<ContextCluster> buildContexts(Multimap<Signature, String> mappings,
			RepositoryConnection kbConnection, HierarchyBean hb)
			throws MalformedQueryException, RepositoryException, QueryEvaluationException, NamingException {
		ContextBuilder ctx = new ContextBuilder(mappings, kbConnection);
		ctx.start();
		TreeSet<ContextCluster> contextClusters = ctx.getContextClusters();
		System.out.println("size:" + contextClusters.size());
		for (ContextCluster cc : contextClusters) {
			cc.calculateContextClusterScore(mappings, kbConnection, hb);
		}
		System.out.println("Question answer");

		// need to do that because score updates are not taken into account
		TreeSet<ContextCluster> orderedContextClusters = new TreeSet<>();
		for (ContextCluster cc : contextClusters) {
			orderedContextClusters.add(cc);
		}
		// System.out.println(orderedContextClusters);
		return orderedContextClusters;
	}

	@SuppressWarnings("unused")
	private HashSet<Signature> fakeConceptsFrequency() {
		HashSet<Signature> result = new HashSet<>();

		Signature s1 = new Signature();
		s1.label = "drink";
		s1.uri = "http://kristina-project.eu/ontologies/entities#Drink";
		s1.localName = "drink";

		Signature s2 = new Signature();
		s2.label = "Water";
		s2.uri = "http://kristina-project.eu/ontologies/entities#Water";
		s2.localName = "Water";

		Signature s3 = new Signature();
		s3.label = "Glass";
		s3.uri = "http://purl.oclc.org/NET/muo/ucum/unit/volume/glass";
		s3.localName = "glass";
		//
		Signature s4 = new Signature();
		s4.label = "person";
		s4.uri = "http://kristina-project.eu/ontologies/profile#Person";
		s4.localName = "person";
		//
		result.add(s4);
		result.add(s3);
		result.add(s2);
		result.add(s1);

		return result;
	}

	public void clearKb() throws RepositoryConfigException, RepositoryException, IOException, RDFParseException {
		GraphDbRepositoryManager manager = new GraphDbRepositoryManager(AdminBean.serverUrl, AdminBean.username,
				AdminBean.password);
		RepositoryConnection kbConnection = manager.getRepository("kb").getConnection();
		kbConnection.begin();
		kbConnection.clear();
		kbConnection.commit();
		System.out.println(kbConnection.size() + "");

		kbConnection.begin();
		String context = "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/patterns/sleep_habits_data.ttl";
		String common = "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/common-entities.ttl";
		@SuppressWarnings("unused")
		String dul = "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/imports/DUL.rdf";
		// String schema =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/review2016-demo/schema.ttl";
		// String temperature =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/review2016-demo/best_temperature.ttl";
		// String sleep =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/review2016-demo/sleep.ttl";
		kbConnection.add(new FileInputStream(context), "http://kristina", RDFFormat.TURTLE);
		kbConnection.add(new FileInputStream(common), "http://kristina", RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(dul), "http://kristina",
		// RDFFormat.RDFXML);
		// kbConnection.add(new FileInputStream(temperature), "http://kristina",
		// RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(sleep), "http://kristina",
		// RDFFormat.TURTLE);
		kbConnection.commit();
		System.out.println(kbConnection.size() + "");

	}

}
