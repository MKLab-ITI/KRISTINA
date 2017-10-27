package gr.iti.kristina.model;

import org.apache.commons.lang3.SystemUtils;

public class Namespaces {

	public static final String KB_CONTEXT = "http://kristina-project.eu/ontologies/context-light#";
	public static final String LA_CONTEXT = "http://kristina-project.eu/ontologies/la/context#";
	public static final String LA_ACTION = "http://kristina-project.eu/ontologies/la/action#";
	public static final String LA_DIALOGUE = "http://kristina-project.eu/ontologies/dialogue_actions#";
	public static final String LA_ONTO = "http://kristina-project.eu/ontologies/la/onto#";
	public static final String RESPONSE = "http://kristina-project.eu/ontologies/responses#";
	public static final String TEMP_KRISTINA = "http://kristina#";
	public static final String TEMP = "http://temp#";
	public static final String WEATHER = "http://kristina-project.eu/ontologies/weather#";
	public static final String USER_MNG = "http://kristina-project.eu/user_management#";
	public static final String CORE = "http://kristina-project.eu/ontologies/models/core#";
	

	public static final String ONTOLOGY_FOLDER;
	static {
		if(SystemUtils.IS_OS_LINUX){
			ONTOLOGY_FOLDER = "file:////home/gmeditsk/Dropbox/kristina_prototype1/";
		} else {
			ONTOLOGY_FOLDER = "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/";
		}
	}
	
	public static final String ONTOLOGY_FOLDER2;
	static {
		if(SystemUtils.IS_OS_LINUX){
			ONTOLOGY_FOLDER2 = "file:////home/gmeditsk/Dropbox/kristina_prototype1/prototype2/";
		} else {
			ONTOLOGY_FOLDER2 = "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/prototype2/";
		}
	}

	public static final String KB_CONTEXT_FILE = ONTOLOGY_FOLDER + "context-light_v4.ttl";

	public static final String TOPICS_NAMESPACES_FILE = ONTOLOGY_FOLDER + "topics-namespaces.ttl";
	public static final String LA_ACTION_FILE = ONTOLOGY_FOLDER + "action.ttl";
	public static final String LA_CONTEXT_FILE = ONTOLOGY_FOLDER + "context.ttl";
	public static final String LA_ONTO_FILE = ONTOLOGY_FOLDER + "onto.ttl";
	
	
}
