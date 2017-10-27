package gr.iti.kristina.services.rules;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.QueryUtils;
import gr.iti.kristina.utils.Utils;
import info.aduna.iteration.Iterations;

public class DietRestrictionService extends ProfileService {

	public DietRestrictionService(boolean foo, String user, String scenario) throws RepositoryException, RepositoryConfigException, UsernameException {
		super(foo, user, scenario);
	}

	//RepositoryConnection kbConnection;
	//GraphDbRepositoryManager manager;
	//ValueFactory vf;
	//static public final String serverUrl = "http://160.40.50.196:8084/graphdb-workbench-free/";
	//Value person;
	private final Logger logger = Logger.getLogger(DietRestrictionService.class);

//	public DietRestrictionService(boolean foo) throws RepositoryException, RepositoryConfigException {
//		manager = new GraphDbRepositoryManager(serverUrl, AdminBean.username, AdminBean.password);
//		kbConnection = manager.getRepository("users").getConnection();
//		this.vf = kbConnection.getValueFactory();
//		this.person = this.getPerson();
//		System.out.println("person: " + this.person);
//	}

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

//	private Resource getPerson() throws RepositoryException {
//		URI Person = vf.createURI(Namespaces.USER_MNG + "Person");
//		RepositoryResult<Statement> people = kbConnection.getStatements(null, RDF.TYPE, Person, false);
//		if (people.hasNext()) {
//			// people.next().getSubject();
//			return people.next().getSubject();
//		}
//		return null;
//	}

	public boolean hasDietRestriction() throws RepositoryException {
		logger.info("DietRestrictionService for " + this.person);
		return !getDietRestrictions().isEmpty();
	}

	public List<Statement> getDietRestrictions() throws RepositoryException {
		URI hasDietRestriction = vf.createURI(Namespaces.USER_MNG + "hasDietRestriction");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasDietRestriction, null, false));
	}

	public boolean hasDietRestriction(String food)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		URI hasDietRestriction = vf.createURI(Namespaces.USER_MNG + "hasDietRestriction");
		// URI foodUri = vf.createURI(Namespaces.LA_ONTO + food);
		System.out.println("food" + food);

		// TODO need to see how to get the actual type from the user management
		// kb....

		String q = "ASK WHERE {?p ?hasDietRestriction [rdfs:label ?label]. FILTER (regex(?label, \"" + food
				+ "\", \"i\" )) }";

		boolean evaluateAskQuery = QueryUtils.evaluateAskQuery(kbConnection, q, new Binding[] {
				new BindingImpl("p", this.person), new BindingImpl("hasDietRestriction", hasDietRestriction) });
		System.out.println(evaluateAskQuery);

		return evaluateAskQuery;
	}

	public List<Model> getPatterns() throws RepositoryException {
		ArrayList<Model> models = new ArrayList<>();
		// drink
		// diet_restriction_1 -> diabetes
		// diet_restriction_2 -> no pork
		// diet_restriction_3 -> no meat
		// diet_restriction_4 -> no alcohol
		// diet_restriction_5 -> no animal products

		List<Statement> dietRestrictions = this.getDietRestrictions();
		for (Statement r : dietRestrictions) {
			if (r.getObject().stringValue().contains("diet_restriction_2")) {
				logger.info("no pork");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatPork.owl".replace("file:///", ""), "TTL");
				models.add(temp);
				// logger.info(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("diet_restriction_3")) {
				logger.info("no meat");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatMeat.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("diet_restriction_4")) {
				logger.info("no alcohol");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notDrinkAlcohol.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("diet_restriction_5")) {
				logger.info("no animal products");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatAnimalProduct.owl".replace("file:///", ""), "TTL");
				models.add(temp);
			}
		}

		return models;

	}

	public static void main(String[] args) throws RepositoryException, RepositoryConfigException, UsernameException {
		DietRestrictionService s = new DietRestrictionService(true, null, null);
		System.out.println(s.hasDietRestriction());
	}

}
