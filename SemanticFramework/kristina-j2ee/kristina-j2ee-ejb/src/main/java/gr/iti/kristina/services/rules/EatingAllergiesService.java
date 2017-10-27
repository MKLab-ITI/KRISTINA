package gr.iti.kristina.services.rules;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;
import info.aduna.iteration.Iterations;

public class EatingAllergiesService extends ProfileService {
	
public EatingAllergiesService(boolean foo, String user, String scenario) throws RepositoryException, RepositoryConfigException, UsernameException {
		super(foo, user, scenario);
		// TODO Auto-generated constructor stub
	}


	//	RepositoryConnection kbConnection;
//	GraphDbRepositoryManager manager;
//	ValueFactory vf;
//	static public final String serverUrl = "http://160.40.50.196:8084/graphdb-workbench-free/";
//	Value person;
	private final Logger logger = Logger.getLogger(EatingAllergiesService.class);

//	public EatingAllergiesService(boolean foo) throws RepositoryException, RepositoryConfigException {
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
//	private Resource getPerson() throws RepositoryException{
//		URI Person = vf.createURI(Namespaces.USER_MNG + "Person");
//		RepositoryResult<Statement> people = kbConnection.getStatements(null, RDF.TYPE, Person, false);
//		if(people.hasNext()){
////			people.next().getSubject();
//			return people.next().getSubject();
//		}
//		return null;
//	}
	
	public boolean hasEatingAllergies() throws RepositoryException {
		logger.info("EatingAllergiesService for " + this.person);
		return !getEatingAllergies().isEmpty();
	}

	public List<Statement> getEatingAllergies() throws RepositoryException {
		URI hasAllergy = vf.createURI(Namespaces.USER_MNG + "hasAllergy");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasAllergy, null, false));
	}
	
	public List<Model> getPatterns() throws RepositoryException {
		ArrayList<Model> models = new ArrayList<>();
		// drink
		// food_1 -> lactose
		// food_2 -> gluten
		// food_3 -> nuts

		List<Statement> eatingAllergies = this.getEatingAllergies();
		for (Statement r : eatingAllergies) {
			if (r.getObject().stringValue().contains("food_1")) {
				logger.info("lactose");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//lactoseAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);
//				logger.info(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("food_2")) {
				logger.info("gluten");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//glutenAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("food_3")) {
				logger.info("nuts");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//nutsAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} 
		}

		return models;

	}


	public static void main(String[] args) throws RepositoryException, RepositoryConfigException, UsernameException {
		EatingAllergiesService s = new EatingAllergiesService(true, null, null);
		System.out.println(s.hasEatingAllergies());
	}

}
