package gr.iti.kristina.services.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.joda.time.LocalTime;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;

import com.hp.hpl.jena.ontology.OntModel;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.repository.GraphDbRepositoryManager;
import gr.iti.kristina.utils.QueryUtils;
import gr.iti.kristina.utils.Utils;
import info.aduna.iteration.Iterations;

public class ProfileService {

	RepositoryConnection kbConnection;
	GraphDbRepositoryManager manager;
	ValueFactory vf;
	static public final String serverUrl = "http://160.40.51.145:8082";
	Value person;
	private final Logger logger = Logger.getLogger(ProfileService.class);

	String user, scenario;

	public ProfileService(boolean foo, String user, String scenario)
			throws RepositoryException, RepositoryConfigException, UsernameException {
		manager = new GraphDbRepositoryManager(serverUrl, AdminBean.username, AdminBean.password);
//		manager = new GraphDbRepositoryManager(serverUrl);
		kbConnection = manager.getRepository("users").getConnection();
		this.vf = kbConnection.getValueFactory();
		this.user = user.toLowerCase();
		this.scenario = scenario.toLowerCase();
		this.person = this.getPerson();
		System.out.println("person: " + this.person);
	}

	public void shutdown() {
		manager.shutDown("shutdown");
		if (kbConnection != null) {
			try {
				kbConnection.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Resource getPerson() throws RepositoryException, UsernameException {
		String name = "";

		if (user.equals("hans") || user.equals("mustafa")) {
			name = this.user;
		} else if (user.equals("claudia") && (scenario.equals("eat") || scenario.equals("sleep") || scenario.equals("eat_sleep"))) {
			name = "stefan";
		} else if (user.equals("jana") && (scenario.equals("eat") || scenario.equals("sleep") || scenario.equals("eat_sleep"))) {
			name = "stefan";
		} else if (user.equals("claudia")) {
			name = "claudia";
		} else if (user.equals("jana")) {
			name = "jana";
		} else if (user.equals("carmen")) {
			name = user;
		} else if (user.equals("carlos")) {
			name = user;
		} else if (user.equals("test")) {
			name = "stefan";
		} else {
			throw new UsernameException("Cannot deternine user/scenario: user->" + user + ", scenario: " + scenario);
		}

		System.out.println("Final username: " + name + ", scenario: " + scenario);

		// URI Person = vf.createURI(Namespaces.USER_MNG + "Person");
		String q = "PREFIX : <http://kristina-project.eu/user_management#>\r\n" + "select ?p\r\n" + "where{\r\n"
				+ "    ?p a :Person;\r\n" + "        :hasName ?n.\r\n" + "    FILTER (regex(?n, \"" + name
				+ "\", \"i\" ))\r\n" + "}";
		Resource p = null;
		TupleQueryResult evaluateSelectQuery = null;
		try {
			evaluateSelectQuery = QueryUtils.evaluateSelectQuery(kbConnection, q);
			while (evaluateSelectQuery.hasNext()) {
				p = (Resource) evaluateSelectQuery.next().getValue("p");
			}
			evaluateSelectQuery.close();
		} catch (MalformedQueryException | QueryEvaluationException e) {
			throw new UsernameException(e.getMessage());
		} finally {
		}

		return p;
	}

	public Statement getLocation() throws RepositoryException {
		URI hasLocation = vf.createURI(Namespaces.USER_MNG + "hasLocation");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasLocation, null, false)).get(0);
	}

	public Integer getBabyAge() throws RepositoryException {
		URI hasAge = vf.createURI(Namespaces.USER_MNG + "hasChildAge");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasAge, null, false);
		if (statements.hasNext()) {
			int parseInt = Integer.parseInt(statements.next().getObject().stringValue());
			statements.close();
			return parseInt;
		}
		return null;
	}

	public Double getBabyWeight() throws RepositoryException {
		URI hasChildWeight = vf.createURI(Namespaces.USER_MNG + "hasChildWeight");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasChildWeight, null, false);
		if (statements.hasNext()) {
			Double parseDouble = Double.parseDouble(statements.next().getObject().stringValue());
			statements.close();
			return parseDouble;
		}
		return null;
	}

	public Double getBabyHeight() throws RepositoryException {
		URI hasChildHeight = vf.createURI(Namespaces.USER_MNG + "hasChildHeight");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasChildHeight, null, false);
		if (statements.hasNext()) {
			Double parseDouble = Double.parseDouble(statements.next().getObject().stringValue());
			statements.close();
			return parseDouble;
		}
		return null;
	}

	public String getBabyGender() throws RepositoryException {
		URI hasChildGender = vf.createURI(Namespaces.USER_MNG + "hasChildGender");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasChildGender, null, false);
		if (statements.hasNext()) {
			String gender = statements.next().getObject().stringValue().toLowerCase();
			statements.close();
			return gender;
		}
		return null;
	}

	public OntModel getWakeUpTimePattern() throws RepositoryException, IOException {
		URI getsUpAt = vf.createURI(Namespaces.USER_MNG + "getsUpAt");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, getsUpAt, null, false);
		if (statements.hasNext()) {
			String up = statements.next().getObject().stringValue();
			LocalTime dt = new LocalTime(up);
			String dt2 = dt.toString("HH:mm");

			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$TIME$$", dt2);
			OntModel temp = updatePattern("wakeUpTime.owl", mappings);
			statements.close();
			return temp;
		}
		statements.close();
		return null;
	}

	public OntModel getWakeUpFrequencyPattern() throws RepositoryException, IOException {
		URI hasSleepDisturbanceActivity = vf.createURI(Namespaces.USER_MNG + "hasSleepDisturbanceActivity");
		URI awakeningduringNight = vf.createURI(Namespaces.USER_MNG + "activity_7");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasSleepDisturbanceActivity,
				awakeningduringNight, false);
		if (statements.hasNext()) {
			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("00001", "1");
			mappings.put("00002", "4");
			OntModel temp = updatePattern("wakeUpFrequency.owl", mappings);
			statements.close();
			return temp;
		}
		statements.close();
		return null;

	}

	public OntModel getSleepDurationPattern() throws RepositoryException, IOException {
		URI getsHoursOfSleep = vf.createURI(Namespaces.USER_MNG + "getsHoursOfSleep");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, getsHoursOfSleep, null,
				false);
		if (statements.hasNext()) {
			String hours = statements.next().getObject().stringValue();
			
			LocalTime dt = new LocalTime(hours);
			
//			int hoursValue = Integer.parseInt(hours);
//
			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$DURATION$$", dt.toString("HH:mm"));
			OntModel temp = updatePattern("sleepDuration.owl", mappings);
			statements.close();
			return temp;
		}
		statements.close();
		return null;
	}

	public OntModel getToiletFrequencyModel() throws NumberFormatException, RepositoryException, IOException {
		URI visitsToToiletPerNight = vf.createURI(Namespaces.USER_MNG + "visitsToToiletPerNight");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, visitsToToiletPerNight, null,
				false);
		if (statements.hasNext()) {
			String freq = statements.next().getObject().stringValue();
			Integer freqValue = Integer.parseInt(freq);

			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$FREQ$$", freqValue + "");
			OntModel temp = updatePattern("toiletFrequency.owl", mappings);
			statements.close();
			return temp;
		}
		statements.close();
		return null;
	}
	
	public OntModel needsAssistanceModel() throws RepositoryException, IOException {
		return needsToiletAssistanceModel();
	}

	public OntModel needsToiletAssistanceModel() throws RepositoryException, IOException {
		URI needsAssistanceForToilet = vf.createURI(Namespaces.USER_MNG + "needsAssistanceForToilet");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, needsAssistanceForToilet,
				null, false);
		if (statements.hasNext()) {
			String hours = statements.next().getObject().stringValue();
			boolean needs = Boolean.parseBoolean(hours);

			if (needs) {
				HashMap<String, String> mappings = new HashMap<>();
				mappings.put("$$TIME$$", "");
				OntModel temp = updatePattern("assistanceToilet.owl", mappings);
				statements.close();
				return temp;
			} else {
				statements.close();
				return null;
			}
		}
		statements.close();
		return null;
	}

	public OntModel getSleepTimePattern() throws RepositoryException, IOException {
		URI sleepsAt = vf.createURI(Namespaces.USER_MNG + "sleepsAt");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, sleepsAt, null, false);
		if (statements.hasNext()) {
			String up = statements.next().getObject().stringValue();
			LocalTime dt = new LocalTime(up);
			String dt2 = dt.toString("HH:mm");

			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$TIME$$", dt2);
			OntModel temp = updatePattern("sleepTime.owl", mappings);
			statements.close();
			return temp;
		}
		statements.close();
		return null;
	}

	public OntModel getProblemFallAsleepPattern() throws NumberFormatException, RepositoryException, IOException {
		HashMap<String, String> mappings = new HashMap<>();
		mappings.put("$$FREQ$$", "");
		OntModel temp = updatePattern("problemFallingAsleep.owl", mappings);
		return temp;
	}
	
	public List<OntModel> getProblems() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		models.addAll(getSleepProblems());
		models.addAll(getMemoryProblem());
		return models;
	}

	public List<OntModel> getSleepProblems() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI hasSleepDisturbanceActivity = vf.createURI(Namespaces.USER_MNG + "hasSleepDisturbanceActivity");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasSleepDisturbanceActivity,
				null, false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("activity_6")) {
				// models.add(this.getSleepTimePattern());
				models.add(this.getProblemFallAsleepPattern());
			} else if (object.contains("activity_7")) {
				models.add(this.getWakeUpFrequencyPattern());
			} else if (object.contains("activity_8")) {
				models.add(this.getWakeUpTimePattern());
			}
		}
		statements.close();
		return models;
	}

	public List<OntModel> getActivitiesBeforeSleep() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI prefersActivityBeforeSleep = vf.createURI(Namespaces.USER_MNG + "prefersActivityBeforeSleep");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, prefersActivityBeforeSleep,
				null, false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("activity_1")) {
				// models.add(this.getSleepTimePattern());
				models.add(updatePattern("beforeSleepWatchTV.owl", new HashMap<String, String>()));
			} else if (object.contains("activity_4")) {
				models.add(updatePattern("beforeSleepHotWaterBottle.owl", new HashMap<String, String>()));
			} else if (object.contains("activity_5")) {
				models.add(updatePattern("beforeSleepDrinkMilk.owl", new HashMap<String, String>()));
			}
		}
		statements.close();
		return models;
	}

	public List<OntModel> getActivitiesAfterTV() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI performsActivityAfterWatchingTV = vf.createURI(Namespaces.USER_MNG + "performsActivityAfterWatchingTV");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person,
				performsActivityAfterWatchingTV, null, false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("activity_2")) {
				// models.add(this.getSleepTimePattern());
				models.add(updatePattern("afterTVLikePlayBoardGames.owl", new HashMap<String, String>()));
			} else if (object.contains("activity_3")) {
				models.add(updatePattern("afterTVHairCombed.owl", new HashMap<String, String>()));
			}
		}
		statements.close();
		return models;
	}

	public List<OntModel> getDiseases() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI hasDiseaseProperty = vf.createURI(Namespaces.USER_MNG + "hasDisease");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasDiseaseProperty, null,
				false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("disease_1")) {
				// models.add(this.getSleepTimePattern());
				models.add(updatePattern("dementiaDisease.owl", new HashMap<String, String>()));
			} else if (object.contains("disease_2")) {
				models.add(updatePattern("diabetesType2Disease.owl", new HashMap<String, String>()));
			}
		}
		statements.close();
		return models;
	}

	public List<OntModel> getMemoryProblem() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI hasDiseaseProperty = vf.createURI(Namespaces.USER_MNG + "hasDisease");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasDiseaseProperty, null,
				false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("disease_1")) {
				// models.add(this.getSleepTimePattern());
				models.add(updatePattern("dementiaDisease.owl", new HashMap<String, String>()));
			}
		}
		statements.close();
		return models;
	}

	public List<OntModel> getFavouriteBoardGame() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		models.add(updatePattern("likePlayLudo.owl", new HashMap<String, String>()));
		return models;
	}

	public OntModel updatePattern(String file, HashMap<String, String> mappings) throws IOException {
		OntModel temp = Utils.createDefaultModel(false);
		System.out.println(Namespaces.ONTOLOGY_FOLDER2 + file);
		temp.read(Namespaces.ONTOLOGY_FOLDER2 + file, "TTL");
		String modelToString = Utils.modelToString(temp, "TTL");

		Set<String> keySet = mappings.keySet();
		for (String k : keySet) {
			String value = mappings.get(k);
			modelToString = modelToString.replace(k, value);
		}
		temp.removeAll();
		temp.read(IOUtils.toInputStream(modelToString, "UTF-8"), "http://temp", "TTL");
		return temp;
	}

//	public static void main(String[] args) {
//		LocalTime dt = new LocalTime("6");
//		System.err.println(dt.toString("HH:mm"));
//	}
	
}
