package gr.iti.kristina.services.rules;

import java.util.ArrayList;
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

public class FavouriteFoodService extends ProfileService {
	
public FavouriteFoodService(boolean foo, String user, String scenario) throws RepositoryException, RepositoryConfigException, UsernameException {
	super(foo, user, scenario);
		// TODO Auto-generated constructor stub
	}


	//	RepositoryConnection kbConnection;
//	GraphDbRepositoryManager manager;
//	ValueFactory vf;
//	static public final String serverUrl = "http://160.40.50.196:8084/graphdb-workbench-free/";
//	Value person;
	private final Logger logger = Logger.getLogger(FavouriteFoodService.class);

//	public FavouriteFoodService(boolean foo) throws RepositoryException, RepositoryConfigException {
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
		logger.info("FavouriteFoodService for " + this.person);
		return !getFavouriteFoods().isEmpty();
	}

	public List<Statement> getFavouriteFoods() throws RepositoryException {
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasFavouriteDish, null, false));
	}
	
	public List<Model> getPatterns() throws RepositoryException {
		ArrayList<Model> models = new ArrayList<>();
		// drink
		// dish_1 -> lentis
		// dish_2 -> swab
		// dish_3 -> cheese

		List<Statement> favouriteFoods = this.getFavouriteFoods();
		for (Statement r : favouriteFoods) {
			if (r.getObject().stringValue().contains("dish_1")) {
				logger.info("lentis");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeLentilsWithSpaetzle.owl".replace("file:///", ""), "TTL");
				models.add(temp);
//				logger.info(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("dish_2")) {
				logger.info("swab");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeSwabianPockets.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("dish_3")) {
				logger.info("cheese");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeCheeseNoodles.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} 
		}

		return models;

	}


	public static void main(String[] args) throws RepositoryException, RepositoryConfigException, UsernameException {
		FavouriteFoodService s = new FavouriteFoodService(true, null, null);
		System.out.println(s.hasEatingAllergies());
	}

}
