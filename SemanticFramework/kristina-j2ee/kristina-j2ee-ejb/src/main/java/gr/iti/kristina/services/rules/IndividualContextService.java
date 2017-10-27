package gr.iti.kristina.services.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.repository.GraphDbRepositoryManager;
import gr.iti.kristina.utils.Utils;
import info.aduna.iteration.Iterations;

public class IndividualContextService extends ProfileService {

public IndividualContextService(boolean foo, String user, String scenario) throws RepositoryException, RepositoryConfigException, UsernameException {
	super(foo, user, scenario);
		// TODO Auto-generated constructor stub
	}

//	RepositoryConnection kbConnection;
//	ValueFactory vf;
//	GraphDbRepositoryManager manager;
//	static public final String serverUrl = "http://160.40.50.196:8084/graphdb-workbench-free/";
//	Value person;
	private final Logger logger = Logger.getLogger(IndividualContextService.class);

//	public IndividualContextService(boolean foo) throws RepositoryException, RepositoryConfigException {
//		manager = new GraphDbRepositoryManager(serverUrl, AdminBean.username,
//				AdminBean.password);
//		kbConnection = manager.getRepository("users").getConnection();
//		this.vf = kbConnection.getValueFactory();
//		this.person = this.getPerson();
//	}
//	
//	public void shutdown() {
//		manager.shutDown("shutdown");
//		if (kbConnection != null) {
//			try {
//				kbConnection.close();
//			} catch (RepositoryException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	private Resource getPerson() throws RepositoryException {
//		URI Person = vf.createURI(Namespaces.USER_MNG + "Person");
//		RepositoryResult<Statement> people = kbConnection.getStatements(null, RDF.TYPE, Person, false);
//		if (people.hasNext()) {
//			// people.next().getSubject();
//			return people.next().getSubject();
//		}
//		return null;
//	}

	public List<Model> getPatterns(String path) throws RepositoryException {
		System.out.println(path);
		ArrayList<Model> models = new ArrayList<>();
		OntModel temp = Utils.createDefaultModel(false);
		System.out.println(Namespaces.ONTOLOGY_FOLDER + path.replace("file:///", ""));
		temp.read(Namespaces.ONTOLOGY_FOLDER + path, "TTL");
		temp.add(temp);
		models.add(temp);
		return models;

	}

	public List<Model> getPatterns(String[] paths) throws RepositoryException {
		ArrayList<Model> models = new ArrayList<>();
		for (String path : paths) {
			OntModel temp = Utils.createDefaultModel(false);
			System.out.println(Namespaces.ONTOLOGY_FOLDER + path.replace("file:///", ""));
			temp.read(Namespaces.ONTOLOGY_FOLDER + path, "TTL");
			temp.add(temp);
			models.add(temp);
		}
		return models;
	}

	public static void main(String[] args) throws RepositoryException, RepositoryConfigException {
		// IndividualContextService s = new IndividualContextService();
		// s.getPatterns("prototype1/sleeping_habits_system/ki-gts/gt-sleepDuration_Response.ttl");
		System.out.println(Arrays.toString(
				"prototype1/sleeping_habits_system/ki-gts/gt-sleepDuration_Response.ttl $$ ".split("\\$\\$")));
	}

}
