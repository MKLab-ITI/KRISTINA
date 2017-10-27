package gr.iti.kristina.services.rules;

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

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.repository.GraphDbRepositoryManager;

public class EatingPreferencesService extends ProfileService {
	
public EatingPreferencesService(boolean foo, String user, String scenario) throws RepositoryException, RepositoryConfigException, UsernameException {
	super(foo, user, scenario);
		// TODO Auto-generated constructor stub
	}

	//	RepositoryConnection kbConnection;
//	ValueFactory vf;
//	GraphDbRepositoryManager manager;
//	static public final String serverUrl = "http://160.40.50.196:8084/graphdb-workbench-free/";
//	Value person;
	private final Logger logger = Logger.getLogger(EatingPreferencesService.class);

//	public EatingPreferencesService(boolean foo) throws RepositoryException, RepositoryConfigException {
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

	public boolean hasEatingPreferences() throws RepositoryException {
		logger.info("EatingPreferencesService for " + this.person);
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return kbConnection.hasStatement((URI)person, hasFavouriteDish, null, false);
	}
	
	public boolean hasEatingPreferences(String food) throws RepositoryException {
		logger.info("EatingPreferencesService for " + food + " " + this.person);
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return kbConnection.hasStatement((URI)person, hasFavouriteDish, null, false);
	}
	
	public static void main(String[] args) throws RepositoryException, RepositoryConfigException, UsernameException {
		EatingPreferencesService s = new EatingPreferencesService(true, null, null);
		System.out.println(s.hasEatingPreferences());
	}

}
