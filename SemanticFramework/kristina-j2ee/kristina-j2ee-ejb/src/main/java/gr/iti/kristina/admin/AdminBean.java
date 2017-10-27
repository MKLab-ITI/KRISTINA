package gr.iti.kristina.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.repository.GraphDbRepositoryManager;

/**
 * Session Bean implementation class AdminBean
 */
@Stateless
@LocalBean
public class AdminBean {

	static public final String serverUrl = "http://localhost:8084/graphdb-workbench-free/";
	static public final String username = "kristina";
	static public final String password = "samiam#2";
	// static private final String repositoryId = "kb";

	// static public final String[] USERNAMES = { "eugene" };

	private static String current_username;
	private static String scenario;

	/**
	 * Default constructor.
	 */
	public AdminBean() {
		// TODO Auto-generated constructor stub
	}

	public static boolean clearKb() throws RepositoryException, RepositoryConfigException, RDFParseException,
			FileNotFoundException, IOException {
		GraphDbRepositoryManager manager = new GraphDbRepositoryManager(AdminBean.serverUrl, AdminBean.username,
				AdminBean.password);
		RepositoryConnection kbConnection = manager.getRepository(AdminBean.getUsername()).getConnection();
		kbConnection.begin();
		kbConnection.clear();
		kbConnection.commit();
		System.out.println(kbConnection.size() + "");

		kbConnection.begin();
		// String data =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/kb-data/sleep_onset.ttl";
		// String common =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/common-entities.ttl";
		// String context =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/1stPrototype/context-light_v4.ttl";
		// String dul =
		// "C:/Users/gmeditsk/Dropbox/iti.private/Kristina/ontologies/imports/DUL.rdf";
		// String folder = "C:/Users/gmeditsk/Dropbox/kristina_prototype1/";

		File f = new File(Namespaces.ONTOLOGY_FOLDER.replace("file:///", "") + "sleeping_habits_system/ki-gts");
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			System.err.println(file);
			if (!file.getName().endsWith("ttl"))
				continue;
			kbConnection.add(new FileInputStream(file), Namespaces.TEMP_KRISTINA, RDFFormat.TURTLE);
		}

		// String sleepTime = folder +
		// "sleeping_habits_responses/example-sleepTime_Response.ttl";
		// String wakeupFrequency =
		// "sleeping_habits_responses/example-wakeUp-frequency_Response.ttl";
		// String wakeupTime =
		// "sleeping_habits_responses/example-wakeUpTime_Response.ttl";
		// String beforeSleep =
		// "sleeping_habits_responses/example-beforeSleep_Response.ttl";
		// String toilet_frequency =
		// "sleeping_habits_responses/example-toilet-frequency_Response.ttl";
		// String fav_board =
		// "sleeping_habits_responses/example-favouriteBoardGame_Response.ttl";
		// String like_after_tv =
		// "sleeping_habits_responses/example-likeAfterTV_Response.ttl";
		//
		kbConnection.add(new FileInputStream(Namespaces.KB_CONTEXT_FILE.replace("file:///", "")),
				Namespaces.TEMP_KRISTINA, RDFFormat.TURTLE);
		kbConnection.add(new FileInputStream(Namespaces.LA_ACTION_FILE.replace("file:///", "")),
				Namespaces.TEMP_KRISTINA, RDFFormat.TURTLE);
		kbConnection.add(new FileInputStream(Namespaces.LA_ONTO_FILE.replace("file:///", "")), Namespaces.TEMP_KRISTINA,
				RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(sleepTime), "http://kristina",
		// RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(wakeupFrequency),
		// "http://kristina", RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(wakeupTime), "http://kristina",
		// RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(beforeSleep), "http://kristina",
		// RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(toilet_frequency),
		// "http://kristina", RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(fav_board), "http://kristina",
		// RDFFormat.TURTLE);
		// kbConnection.add(new FileInputStream(like_after_tv),
		// "http://kristina", RDFFormat.TURTLE);

		kbConnection.commit();
		System.out.println(kbConnection.size() + " done");

		/*
		 * initialize topics-namespaces repository
		 * 
		 */

		RepositoryConnection kbConnection2 = manager.getRepository("topics-namespaces").getConnection();
		kbConnection2.begin();
		kbConnection2.clear();
		kbConnection2.commit();

		kbConnection2.begin();

		kbConnection2.add(new FileInputStream(Namespaces.KB_CONTEXT_FILE.replace("file:///", "")),
				Namespaces.TEMP_KRISTINA, RDFFormat.TURTLE);
		kbConnection2.add(new FileInputStream(Namespaces.TOPICS_NAMESPACES_FILE.replace("file:///", "")),
				Namespaces.TEMP_KRISTINA, RDFFormat.TURTLE);
		kbConnection2.commit();
		System.out.println(kbConnection2.size() + " done");

		return true;
	}

	public static void main(String[] args) throws RepositoryException, RepositoryConfigException, RDFParseException,
			FileNotFoundException, IOException {
		AdminBean.setScenario("sleep"); // sleep
		AdminBean.clearKb();
		// System.out.println(AdminBean.getRepositoryId());
	}

	// public static String getRepositoryId() throws IOException {
	// File f = new
	// File("C:/Users/gmeditsk/jboss-eap-6.4/bin/kristina-user.txt");
	// if (!f.exists()) {
	// f = new File("C:/Users/gmeditsk/EAP-7.0.0/bin/kristina-user.txt");
	// }
	// List<String> readLines = FileUtils.readLines(f, "UTF-8");
	// if (readLines.isEmpty()) {
	// throw new IOException("there is no active care-recipient");
	// }
	// return readLines.get(0).toLowerCase();
	// }

	public static String getUsername() {
		return current_username.toLowerCase();
	}

	public static String getScenario() {
		return scenario.toLowerCase();
	}

	public static void setScenario(String scenario) throws RuntimeException {
		AdminBean.scenario = scenario.toLowerCase();
		switch (AdminBean.scenario) {
		case "newspaper": // newspaper
			current_username = "newspaper";
			break;
		case "weather": // weather
			current_username = "weather";
			break;
		case "sleep": // sleep
			current_username = "sleep";
			break;
		case "pain": // backpain
			current_username = "backpain";
			break;
		case "babycare": // babycare
			current_username = "babycare";
			break;
		default:
			throw new RuntimeException("Scenario " + scenario + " not valid.");
		}
	}

	public static void setUsername(String username) throws RuntimeException {
		username = username.toLowerCase();
		switch (username) {
		case "elisabeth": // newspaper
			current_username = "newspaper";
			break;
		case "hans": // weather
			current_username = "weather";
			break;
		case "iwona": // sleep
			current_username = "sleep";
			break;
		case "juan": // backpain
			current_username = "backpain";
			break;
		case "maria": // babycare
			current_username = "babycare";
			break;
		default:
			throw new RuntimeException("Username " + username + " not valid.");
		}
	}

}
