package gr.iti.kristina.topicflow.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.Binding;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

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

	private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

	String user, scenario, lang;

	public ProfileService(boolean foo, String user, String scenario, String lang)
			throws RepositoryException, RepositoryConfigException, UsernameException {
		manager = new GraphDbRepositoryManager(serverUrl, AdminBean.username, AdminBean.password);
		// manager = new GraphDbRepositoryManager(serverUrl);
		kbConnection = manager.getRepository("users").getConnection();
		this.vf = kbConnection.getValueFactory();
		this.user = user.toLowerCase();
		this.scenario = scenario.toLowerCase();
		this.person = this.getPerson();
		this.lang = lang;
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
		} else if (user.equals("claudia") && (scenario.equals("eat") || scenario.equals("sleep") || scenario.equals(
				"eat_sleep"))) {
			name = "stefan";
		} else if (user.equals("jana") && (scenario.equals("eat") || scenario.equals("sleep") || scenario.equals(
				"eat_sleep"))) {
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

	private Statement _getLocation() throws RepositoryException {
		URI hasLocation = vf.createURI(Namespaces.USER_MNG + "hasLocation");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasLocation, null, false)).get(0);
	}

	public String getLocation() throws RepositoryException, RepositoryConfigException, UsernameException {
		org.openrdf.model.Statement locationKB = _getLocation();
		if (locationKB != null) {
			return locationKB.getObject().stringValue();
		}
		return "";
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

	public List<Model> getWakeUpTimePattern() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
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
			models.add(temp);
		}
		statements.close();
		return models;
	}

	public boolean hasWakeUpTimePattern() throws RepositoryException, IOException {
		return !getWakeUpTimePattern().isEmpty();
	}

	public List<Model> getWakeUpFrequencyPattern() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
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
			models.add(temp);
		}
		statements.close();
		return models;

	}

	public boolean hasWakeUpFrequencyPattern() throws RepositoryException, IOException {
		return !getWakeUpFrequencyPattern().isEmpty();
	}

	public List<Model> getSleepDurationPattern() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
		URI getsHoursOfSleep = vf.createURI(Namespaces.USER_MNG + "getsHoursOfSleep");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, getsHoursOfSleep, null,
				false);
		if (statements.hasNext()) {
			String hours = statements.next().getObject().stringValue();

			LocalTime dt = new LocalTime(hours);

			// int hoursValue = Integer.parseInt(hours);
			//
			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$DURATION$$", dt.toString("HH:mm"));
			OntModel temp = updatePattern("sleepDuration.owl", mappings);
			statements.close();
			models.add(temp);
		}
		statements.close();
		return models;
	}

	public boolean hasSleepDurationPattern() throws RepositoryException, IOException {
		return !getSleepDurationPattern().isEmpty();
	}

	public List<Model> getToiletFrequencyModel() throws NumberFormatException, RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
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
			models.add(temp);
		}
		statements.close();
		return models;
	}

	public boolean hasToiletFrequencyModel() throws NumberFormatException, RepositoryException, IOException {
		return !getToiletFrequencyModel().isEmpty();
	}

	// public OntModel needsAssistanceModel() throws RepositoryException,
	// IOException {
	// return needsToiletAssistanceModel();
	// }

	public List<Model> needsToiletAssistanceModel() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
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
				models.add(temp);
			} else {
				statements.close();
				return models;
			}
		}
		statements.close();
		return models;
	}

	public boolean hasNeedsToiletAssistanceModel() throws RepositoryException, IOException {
		return !needsToiletAssistanceModel().isEmpty();
	}

	public List<Model> getSleepTimePattern() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
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
			models.add(temp);
		}
		statements.close();
		return models;
	}

	public boolean hasSleepTime() throws RepositoryException, IOException {
		return !getSleepTimePattern().isEmpty();
	}

	// activity_6
	public List<Model> getProblemFallAsleepPattern() throws NumberFormatException, RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
		URI hasSleepDisturbanceActivity = vf.createURI(Namespaces.USER_MNG + "hasSleepDisturbanceActivity");
		URI fallsAsleep = vf.createURI(Namespaces.USER_MNG + "activity_6");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasSleepDisturbanceActivity,
				fallsAsleep, false);
		if (statements.hasNext()) {
			HashMap<String, String> mappings = new HashMap<>();
			mappings.put("$$FREQ$$", "");
			models.add(updatePattern("problemFallingAsleep.owl", mappings));
		}
		statements.close();
		return models;
	}

	public boolean hasProblemFallAsleepPattern() throws NumberFormatException, RepositoryException, IOException {
		return !getProblemFallAsleepPattern().isEmpty();
	}

	// public List<OntModel> getProblems() throws RepositoryException,
	// IOException {
	// ArrayList<OntModel> models = new ArrayList<OntModel>();
	// models.addAll(getSleepProblems());
	// models.addAll(getMemoryProblem());
	// return models;
	// }

	public List<Model> getSleepProblems() throws RepositoryException, IOException {
		List<Model> models = new ArrayList<Model>();
		models.addAll(this.getProblemFallAsleepPattern());
		models.addAll(this.getWakeUpFrequencyPattern());
		models.addAll(this.getToiletFrequencyModel());
		return models;
	}

	public List<Model> getNightProblems() throws RepositoryException, IOException {
		ArrayList<Model> models = new ArrayList<Model>();
		models.addAll(this.getWakeUpFrequencyPattern());
		models.addAll(this.getToiletFrequencyModel());
		return models;
	}

	public boolean hasNightProblems() throws RepositoryException, IOException {
		return !getNightProblems().isEmpty();
	}

	public boolean hasSleepProblems() throws RepositoryException, IOException {
		return !getSleepProblems().isEmpty();
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

	public boolean hasActivitiesBeforeSleep() throws RepositoryException, IOException {
		return !getActivitiesBeforeSleep().isEmpty();
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

	public boolean hasDiseases() throws RepositoryException, IOException {
		return !this.getDiseases().isEmpty();
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

	public boolean hasDiabetesPattern() throws RepositoryException, IOException {
		return !this.getDiabetesPattern().isEmpty();
	}

	public List<OntModel> getDiabetesPattern() throws RepositoryException, IOException {
		ArrayList<OntModel> models = new ArrayList<OntModel>();
		URI hasDiseaseProperty = vf.createURI(Namespaces.USER_MNG + "hasDisease");
		RepositoryResult<Statement> statements = kbConnection.getStatements((URI) person, hasDiseaseProperty, null,
				false);
		while (statements.hasNext()) {
			String object = statements.next().getObject().stringValue();
			if (object.contains("disease_2")) {
				// models.add(this.getSleepTimePattern());
				models.add(updatePattern("diabetesType2Disease.owl", new HashMap<String, String>()));
			}
		}
		statements.close();
		return models;
	}

	public boolean hasMemoryProblemPattern() throws RepositoryException, IOException {
		return !this.getMemoryProblemPattern().isEmpty();
	}

	public List<OntModel> getMemoryProblemPattern() throws RepositoryException, IOException {
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

	/*
	 * Allergies
	 */
	public boolean hasAllergyPatterns() throws RepositoryException {
		LOG.debug("EatingAllergiesService for " + this.person);
		return !getEatingAllergies().isEmpty();
	}

	private List<Statement> getEatingAllergies() throws RepositoryException {
		URI hasAllergy = vf.createURI(Namespaces.USER_MNG + "hasAllergy");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasAllergy, null, false));
	}

	public List<Model> getAllergyPatterns() throws RepositoryException {
		LOG.debug("getAllergyPatterns for " + this.person);
		ArrayList<Model> models = new ArrayList<>();
		// drink
		// food_1 -> lactose
		// food_2 -> gluten
		// food_3 -> nuts

		List<Statement> eatingAllergies = this.getEatingAllergies();
		for (Statement r : eatingAllergies) {
			if (r.getObject().stringValue().contains("food_1")) {
				LOG.debug("lactose");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//lactoseAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);
				// LOG.debug(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("food_2")) {
				LOG.debug("gluten");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//glutenAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("food_3")) {
				LOG.debug("nuts");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//nutsAllergy.owl".replace("file:///", ""), "TTL");
				models.add(temp);
			}
		}
		return models;
	}

	/*
	 * Favourite Food
	 */

	public boolean hasFavouriteFood() throws RepositoryException {
		LOG.debug("FavouriteFoodService for " + this.person);
		return !getFavouriteFoods().isEmpty();
	}

	public List<String> getFavouriteFoodsString() throws RepositoryException {
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		List<Statement> asList = Iterations.asList(kbConnection.getStatements((URI) person, hasFavouriteDish, null,
				false));
		List<Statement> labels = new ArrayList<>();
		for (Statement s : asList) {
			labels.addAll(Iterations.asList(kbConnection.getStatements(vf.createURI(s.getObject().stringValue()),
					RDFS.LABEL, null,
					false)));
		}
		return labels.stream().map(x -> x.getObject().stringValue()).collect(Collectors.toList());

	}

	private List<Statement> getFavouriteFoods() throws RepositoryException {
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasFavouriteDish, null, false));
	}

	public List<Model> getFavouriteFoodPatterns() throws RepositoryException {
		LOG.debug("getFavouriteFoodPatterns for " + this.person);
		ArrayList<Model> models = new ArrayList<>();
		// drink
		// dish_1 -> lentis
		// dish_2 -> swab
		// dish_3 -> cheese

		List<Statement> favouriteFoods = this.getFavouriteFoods();
		for (Statement r : favouriteFoods) {
			if (r.getObject().stringValue().contains("dish_1")) {
				LOG.debug("lentis");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeLentilsWithSpaetzle.owl".replace("file:///", ""), "TTL");
				models.add(temp);
				// logger.info(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("dish_2")) {
				LOG.debug("swab");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeSwabianPockets.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("dish_3")) {
				LOG.debug("cheese");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//likeCheeseNoodles.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			}
		}

		return models;

	}

	/*
	 * Eating preferences
	 */

	public boolean hasEatingPreferences() throws RepositoryException {
		LOG.debug("EatingPreferencesService for " + this.person);
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return kbConnection.hasStatement((URI) person, hasFavouriteDish, null, false);
	}

	public boolean hasEatingPreferences(String food) throws RepositoryException {
		LOG.debug("EatingPreferencesService for " + food + " " + this.person);
		URI hasFavouriteDish = vf.createURI(Namespaces.USER_MNG + "hasFavouriteDish");
		return kbConnection.hasStatement((URI) person, hasFavouriteDish, null, false);
	}

	/*
	 * Diet restrictions
	 */

	public boolean hasDietRestrictions() throws RepositoryException {
		LOG.debug("hasDietRestrictions for " + this.person);
		return !getDietRestrictions().isEmpty();
	}

	private List<Statement> getDietRestrictions() throws RepositoryException {
		URI hasDietRestriction = vf.createURI(Namespaces.USER_MNG + "hasDietRestriction");
		return Iterations.asList(kbConnection.getStatements((URI) person, hasDietRestriction, null, false));
	}

	public boolean hasDietRestrictions(String food)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		LOG.debug("hasDietRestrictions for " + this.person);
		URI hasDietRestriction = vf.createURI(Namespaces.USER_MNG + "hasDietRestriction");
		// URI foodUri = vf.createURI(Namespaces.LA_ONTO + food);
		LOG.debug("food" + food);

		// TODO need to see how to get the actual type from the user management
		// kb....

		String q = "ASK WHERE {?p ?hasDietRestriction [rdfs:label ?label]. FILTER (regex(?label, \"" + food
				+ "\", \"i\" )) }";

		boolean evaluateAskQuery = QueryUtils.evaluateAskQuery(kbConnection, q, new Binding[] {
				new BindingImpl("p", this.person), new BindingImpl("hasDietRestriction", hasDietRestriction) });
		LOG.debug("query evaluation: " + evaluateAskQuery);

		return evaluateAskQuery;
	}

	public List<Model> getDietRestrictionPatterns(String food) throws RepositoryException {
		ArrayList<Model> models = new ArrayList<>();
		if (food.equals("pork")) {
			LOG.debug("getting  pork pattern");
			OntModel temp = Utils.createDefaultModel(false);
			temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatPork.owl".replace("file:///", ""), "TTL");
			models.add(temp);
			// logger.info(Utils.modelToString(temp, "N-TRIPLE"));
		} else if (food.equals("met")) {
			LOG.debug("getting meat pattern");
			OntModel temp = Utils.createDefaultModel(false);
			temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatMeat.owl".replace("file:///", ""), "TTL");
			models.add(temp);

		} else if (food.equals("alcohol")) {
			LOG.debug("getting alcohol pattern");
			OntModel temp = Utils.createDefaultModel(false);
			temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notDrinkAlcohol.owl".replace("file:///", ""), "TTL");
			models.add(temp);

		} else if (food.equals("animal products")) {
			LOG.debug("getting animal products pattern");
			OntModel temp = Utils.createDefaultModel(false);
			temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatAnimalProduct.owl".replace("file:///", ""), "TTL");
			models.add(temp);
		} else if (food.equals("sweet")) {
			LOG.debug("getting sweet pattern");
			LOG.debug("not implemented yet");
		}
		return models;
	}

	public List<Model> getDietRestrictionPatterns() throws RepositoryException {
		LOG.debug("getDietRestrictionPatterns for " + this.person);
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
				LOG.debug("no pork");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatPork.owl".replace("file:///", ""), "TTL");
				models.add(temp);
				// logger.info(Utils.modelToString(temp, "N-TRIPLE"));
			} else if (r.getObject().stringValue().contains("diet_restriction_3")) {
				LOG.debug("no meat");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatMeat.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("diet_restriction_4")) {
				LOG.debug("no alcohol");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notDrinkAlcohol.owl".replace("file:///", ""), "TTL");
				models.add(temp);

			} else if (r.getObject().stringValue().contains("diet_restriction_5")) {
				LOG.debug("no animal products");
				OntModel temp = Utils.createDefaultModel(false);
				temp.read(Namespaces.ONTOLOGY_FOLDER2 + "//notEatAnimalProduct.owl".replace("file:///", ""), "TTL");
				models.add(temp);
			}
		}
		return models;
	}

	private double getAvgWeight() throws RepositoryException {
		int age = getBabyAge(); // months
		double weight = getBabyWeight(); // kgs
		double height = getBabyHeight(); // cms
		boolean male = getBabyGender().equals("male");

		System.out.println(age + ", " + weight + ", " + height + ", " + male);

		LinkedListMultimap<Integer, Double> boys = LinkedListMultimap.create();
		boys.put(0, 3.4);
		boys.put(0, 50.3);
		boys.put(3, 6.2);
		boys.put(3, 60.00);
		boys.put(6, 8.00);
		boys.put(6, 67.00);
		boys.put(9, 9.2);
		boys.put(9, 72.00);
		boys.put(12, 10.2);
		boys.put(12, 76.00);
		boys.put(15, 11.1);
		boys.put(15, 79.00);
		boys.put(18, 11.8);
		boys.put(18, 82.5);
		boys.put(24, 12.9);
		boys.put(24, 88.00);
		boys.put(36, 15.1);
		boys.put(36, 96.5);
		boys.put(48, 16.07);
		boys.put(48, 100.13);
		boys.put(60, 18.03);
		boys.put(60, 106.40);
		boys.put(72, 19.91);
		boys.put(72, 112.77);
		boys.put(84, 22.00);
		boys.put(84, 118.50);
		boys.put(96, 23.56);
		boys.put(96, 122.86);

		LinkedListMultimap<Integer, Double> girls = LinkedListMultimap.create();
		// weight / height
		girls.put(0, 3.4);
		girls.put(0, 50.3);
		girls.put(3, 5.6);
		girls.put(3, 59.00);
		girls.put(6, 7.3);
		girls.put(6, 65.00);
		girls.put(9, 8.6);
		girls.put(9, 70.00);
		girls.put(12, 9.5);
		girls.put(12, 74.00);
		girls.put(15, 11.00);
		girls.put(15, 77.00);
		girls.put(18, 11.5);
		girls.put(18, 80.5);
		girls.put(24, 12.4);
		girls.put(24, 86.00);
		girls.put(36, 14.4);
		girls.put(36, 95.00);
		girls.put(48, 15.5);
		girls.put(48, 99.14);
		girls.put(60, 17.4);
		girls.put(60, 105.95);
		girls.put(72, 19.6);
		girls.put(72, 112.22);
		girls.put(84, 21.2);
		girls.put(84, 117.27);
		girls.put(96, 23.5);
		girls.put(96, 122.62);

		int[] ageValues = { 0, 3, 6, 9, 12, 15, 18, 24, 36, 48, 60, 72, 84, 96 };
		LinkedListMultimap<Integer, Double> data = male ? boys : girls;

		double avgWeight = 0.0, avgHeight = 0.0;
		int index = Arrays.binarySearch(ageValues, age);
		if (index >= 0) {
			System.out.println("index: " + index);
			avgWeight = data.get(ageValues[index]).get(0);
			avgHeight = data.get(ageValues[index]).get(1);
		} else {
			int low = 0, max = 0;
			for (int i = 0; i < ageValues.length; i++) {
				if (age <= ageValues[i]) {
					max = ageValues[i];
					low = ageValues[i - 1];
					break;
				}
			}
			System.out.println("low: " + low + " , max: " + max);

			double leftWeight = (age - low) * 1.0d / (max - low) * 1.0d;
			double rightWeight = (max - age) * 1.0d / (max - low) * 1.0d;
			System.out.println("leftWeight: " + leftWeight + " , rigthWeight: " + rightWeight);

			// weight
			avgWeight = (1 - leftWeight) * (data.get(low).get(0)) + (1 - rightWeight) * (data.get(max).get(0));

			// height
			avgHeight = (1 - leftWeight) * (data.get(low).get(1)) + (1 - rightWeight) * (data.get(max).get(1));
		}
		System.err.println("avgWeight: " + avgWeight + ", avgHeight: " + avgHeight);
		return avgWeight;
	}

	public String handleGainResponse()
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		return String.format("Según los datos que has introducido el bebé debería pesar %2.2f Kg", this.getAvgWeight());
	}

	public String handleGainResponseClarification()
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		return String.format(
				"[Need to change the text] Según los datos que has introducido el bebé debería pesar %2.2f Kg", this
						.getAvgWeight());
	}

	public String handleLocalEvent() throws Exception {
		String locationKB = getLocation();
		LocalEventBean localEventService = new LocalEventBean();
		String url = localEventService.start(locationKB, lang);
		return url;
	}

	public String getDoctorAppointmentURL() throws RepositoryException, RepositoryConfigException, UsernameException {
		String location = getLocation();
		if (location != null) {
			if (location.toLowerCase().equals("madrid")) {
				return "https://www.citaprevia.sanidadmadrid.org/Forms/Acceso.aspx";
			} else {
				return "https://ws1.ics.gencat.cat/VisitesIServeis/programacio_visites/Visites.aspx";
			}
		} else {
			return "unknown location";
		}
	}
	
	public String handleNearestPlaces(String type) throws Exception {
		String location = getLocation(); 
		if(StringUtils.isBlank(location)){
			location = "Tubingen"; 
		}
		return  "160.40.51.32:9000/places?address=" + location + "&placeType=" + type;
	}

	public boolean getBabyStimulateResponse() throws RepositoryException, RepositoryConfigException, UsernameException {
		return true;
	}

	public String getUrl(String args) throws RepositoryException, RepositoryConfigException, UsernameException {
		return String.format("160.40.50.196:8084/PDFs/%s/%s/document.pdf", args, lang);
	}

	// public String handleSocialMediaTopicsResponse(OntClass topic, String lng,
	// Set<OntClass> keyEntities) throws Exception {
	//
	// String _topic = "";
	// for (OntClass r : keyEntities) {
	// System.out.println(r);
	// if (isSubClassOf(Namespaces.LA_ONTO + "SocialMediaTopic", r)) {
	// _topic = r.getLocalName();
	// if (lang.equals("de")) {
	// _topic = trans_de.get(_topic);
	// } else if (lang.equals("tr")) {
	// _topic = trans_tr.get(_topic);
	// } else if (lang.equals("es")) {
	// _topic = trans_es.get(_topic);
	// }
	// }
	// }
	// System.out.println(_topic);
	// _topic = java.net.URLEncoder.encode(_topic, "UTF-8");
	// System.out.println(_topic);
	//
	// return "160.40.51.32:9000/topicDetectionHTML?language=" + lang +
	// "&keyword=" + _topic;
	//
	// }
	//
	// public String handleSocialMediaKeywordResponse(Topic topic, String
	// keyword) throws Exception {
	// String lang = topic.getLanguage().toLowerCase();
	// System.out.println(keyword);
	// // keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
	// System.out.println(keyword);
	// return "http://160.40.51.32:9000/tweetSearch?keyword=" + keyword +
	// "&language=" + lang + "&maxResults=15";
	//
	// }

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

	// public static void main(String[] args) {
	// LocalTime dt = new LocalTime("6");
	// System.err.println(dt.toString("HH:mm"));
	// }

}
