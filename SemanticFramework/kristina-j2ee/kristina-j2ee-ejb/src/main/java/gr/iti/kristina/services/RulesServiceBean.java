package gr.iti.kristina.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import gr.iti.kristina.context.TopicUnderstandingBean;
import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.IRResponse;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.model.fusion.Observation;
import gr.iti.kristina.model.rules.EVENT;
import gr.iti.kristina.model.rules.Topic;
import gr.iti.kristina.parsers.LAParser;
import gr.iti.kristina.responses.BaseResponseContainer;
import gr.iti.kristina.services.rules.DietRestrictionService;
import gr.iti.kristina.services.rules.EatingAllergiesService;
import gr.iti.kristina.services.rules.EatingPreferencesService;
import gr.iti.kristina.services.rules.FavouriteFoodService;
import gr.iti.kristina.services.rules.IndividualContextService;
import gr.iti.kristina.services.rules.ProfileService;
import gr.iti.kristina.startup.ContextBean;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class FusionService
 */
@Singleton
@LocalBean
@Startup
public class RulesServiceBean {

//	@EJB
//	ContextBean contextBean;

	KieServices ks;
	KieContainer kContainer;
	KieSession kSession;

	public static OntModel responseModel;
	public static OntModel topicModel;
	public static BaseResponseContainer container;
	public static List<Resource> keyEntities;
	public static OntClass partialTopic = null;
	public static boolean partialTopicRequested;

	private final Logger logger = Logger.getLogger(RulesServiceBean.class);
	private String scenario;
	private String user;

	public RulesServiceBean() {
		// responseModel.removeAll();
	}

	@PostConstruct
	public void initialisation() {
		ks = KieServices.Factory.get();
		kContainer = ks.getKieClasspathContainer();
		kSession = kContainer.newKieSession("ruleContextKSession");
		setGlobals();
		responseModel = Utils.createDefaultModel(false);
		System.out.println("RulesServiceBean service sucessfully initialised.");
	}

	private void setGlobals() {
		kSession.setGlobal("logger", logger);
		kSession.setGlobal("service", this);
	}

	public void log(String text) {
		System.out.println(text);
	}

	public void start(LAParser.LAParserResult laResult, String json, String flatUsername, String flatScenario) {
		responseModel.removeAll();
		container = new BaseResponseContainer();
		container.responseContainerInd.addProperty(container.timestamp,
				container.model.createTypedLiteral(new DateTime().toGregorianCalendar()));
		// responseModel.add(container.model);
		keyEntities = laResult.getDomainKeyEntities().stream().map(t -> t.getType()).collect(Collectors.toList());

		/*
		 * Partial matches!!
		 * 
		 */
//		partialTopic = contextBean.match(keyEntities);
//		System.err.println("PARTIAL TOPIC = " + partialTopic);
		partialTopicRequested = false;

		Collection<? extends Object> objects = kSession.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return object.getClass().equals(EVENT.class);
			}
		});
		for (Object object : objects) {
			kSession.delete(kSession.getFactHandle(object));
		}

		if (Strings.isNullOrEmpty(json)) {
			this.user = flatUsername;
			this.scenario = flatScenario;
		} else {
			JSONObject o = new JSONObject(json);
			JSONObject metaObject = o.getJSONObject("meta");
			this.scenario = metaObject.getString("scenario");
			this.user = metaObject.getString("user");
		}
		kSession.insert(laResult);
		kSession.fireAllRules();
		System.out.println("user: " + this.user + ", scenario: " + this.scenario);
	}

	public ArrayList<Resource> findTopic(LAParser.LAParserResult laResult) throws NamingException {
		InitialContext jndi = new InitialContext();
		TopicUnderstandingBean topicBean = (TopicUnderstandingBean) jndi
				.lookup("java:app/kristina-j2ee-ejb/TopicUnderstandingBean");
		RulesServiceBean.topicModel = topicBean.loadExtractedEntityResources(laResult.getDomainKeyEntities());
		HashMap<OntResource, Collection<Resource>> topics = topicBean.getTopics(topicModel);
		if (topics.isEmpty()) {
			return new ArrayList<>();
		} else {
			return new ArrayList<Resource>(topics.entrySet().iterator().next().getValue());
		}

	}

	public String callNewspaperService(String text, Topic topic)
			throws NamingException, ClientProtocolException, IOException, URISyntaxException {
		InitialContext jndi = new InitialContext();
		NewspaperBean newspaperService = (NewspaperBean) jndi.lookup("java:app/kristina-j2ee-ejb/NewspaperBean");
		return newspaperService.start(text, topic.getLanguage());
	}

	public void generateNewspaperResponse(String text) throws UnsupportedEncodingException {
		// BaseResponseContainer container = new BaseResponseContainer();

		if (text == null || text.isEmpty()) {
			this.generateNotFoundResponse();
			return;
		}

		OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				StatementResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));

		OntResource responseTypeValue = container.model.createOntResource(Namespaces.RESPONSE + "free_text");
		responseInd.addProperty(container.responseType, responseTypeValue);
		responseInd.addProperty(container.text, text);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		Individual topicInstance = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				container._m.createClass(Namespaces.KB_CONTEXT + "NewspaperContext"));

		container.responseContainerInd.addProperty(container.conversationalContext, topicInstance);
		// responseModel.add(container.model);
	}

	private Double plausibility() {
		if (partialTopicRequested)
			return 0.85;
		return 1.0;
	}

	public void generateTextResponse(String text) throws UnsupportedEncodingException {
		// BaseResponseContainer container = new BaseResponseContainer();
		text = new String(text.getBytes("UTF-8"), StandardCharsets.UTF_8);
		System.out.println("generate text response: " + text);

		OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				StatementResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));

		OntResource responseTypeValue = container.model.createOntResource(Namespaces.RESPONSE + "free_text");
		responseInd.addProperty(container.responseType, responseTypeValue);
		responseInd.addProperty(container.text, text);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// Individual topicInstance =
		// container.model.createIndividual(Utils.tempURI() +
		// Utils.randomString(),
		// container._m.createClass(Namespaces.KB_CONTEXT +
		// "NewspaperContext"));

		// container.responseContainerInd.addProperty(container.conversationalContext,
		// topicInstance);
		// responseModel.add(container.model);
	}

	public void generateTextResponse(IRResponse response) throws UnsupportedEncodingException {
		// BaseResponseContainer container = new BaseResponseContainer();

		OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				StatementResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));

		OntResource responseTypeValue = container.model.createOntResource(Namespaces.RESPONSE + "free_text");
		responseInd.addProperty(container.responseType, responseTypeValue);
		responseInd.addProperty(container.source, response.getDomain());
		responseInd.addProperty(container.text, response.getContent());

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// Individual topicInstance =
		// container.model.createIndividual(Utils.tempURI() +
		// Utils.randomString(),
		// container._m.createClass(Namespaces.KB_CONTEXT +
		// "NewspaperContext"));

		// container.responseContainerInd.addProperty(container.conversationalContext,
		// topicInstance);
		// responseModel.add(container.model);
	}

	public LinkedHashMultimap<String, String> callWeatherService()
			throws NamingException, ClientProtocolException, IOException, URISyntaxException {
		InitialContext jndi = new InitialContext();
		WeatherServiceBean weatherService = (WeatherServiceBean) jndi
				.lookup("java:app/kristina-j2ee-ejb/WeatherServiceBean");
		return weatherService.start();
	}

	public void generateWeatherResponse(LinkedHashMultimap<String, String> weatherData)
			throws UnsupportedEncodingException {
		String[] fields = { "temperature", "windSpeed", "windDirection", "skyCondition", "pressure", "humidity" };
		XSDDatatype[] datatypes = { XSDDatatype.XSDinteger, XSDDatatype.XSDinteger, XSDDatatype.XSDstring,
				XSDDatatype.XSDstring, XSDDatatype.XSDinteger, XSDDatatype.XSDinteger };

		OntClass WeatherResponse = container._m.createClass(Namespaces.RESPONSE + "WeatherResponse");
		OntProperty weatherClassification = container._m
				.createDatatypeProperty(Namespaces.WEATHER + "weatherClassification");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				WeatherResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));

		OntResource responseTypeValue = container._m.createOntResource(Namespaces.RESPONSE + "free_text");
		responseInd.addProperty(container.responseType, responseTypeValue);

		OntModel temp = Utils.createDefaultModel(false, OntModelSpec.OWL_DL_MEM);
		Ontology ont = temp.createOntology(container.base);
		ont.addImport(temp.createResource(StringUtils.removeEnd(Namespaces.WEATHER, "#")));

		OntClass Forecast = container._m.createClass(Namespaces.WEATHER + "Forecast");
		Individual forecastInd = temp.createIndividual(Utils.tempURI() + Utils.randomString(), Forecast);
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			Set<String> values = weatherData.get(field);
			if (!values.isEmpty()) {
				DatatypeProperty p = container._m.createDatatypeProperty(Namespaces.WEATHER + field);
				Literal v = temp.createTypedLiteral(values.toArray()[0], datatypes[i]);
				forecastInd.addProperty(p, v);
				if (field.equals("temperature")) {
					int t = v.getInt();
					if (t <= 15) {
						forecastInd.addProperty(weatherClassification, "cold");
						responseInd.addProperty(weatherClassification, "cold");
						this.generateProactiveResponse(
								"The weather is rather cold today. Would you like me to read the newspaper for you?");
					} else if (t > 15) {
						forecastInd.addProperty(weatherClassification, "hot");
						responseInd.addProperty(weatherClassification, "hot");
						this.generateProactiveResponse(
								"Nice weather today! Would you like me to check for events in the city?");
						this.generateProactiveResponse(
								"Nice weather today! Do you want me to tell you where the nearest park is?");
						this.generateProactiveResponse(
								"Nice weather today! Do you want me to tell you activities in the city where you can go with your baby?");
					}
				}
			}
		}
		responseInd.addProperty(container.text, Utils.modelToString(temp, "TTL"));

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		Individual topicInstance = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				container._m.createClass(Namespaces.KB_CONTEXT + "WeatherContext"));

		container.responseContainerInd.addProperty(container.conversationalContext, topicInstance);
		// responseModel.add(container.model);
	}

	public void generateUnknownResponse() throws UnsupportedEncodingException {

		// BaseResponseContainer container = new BaseResponseContainer();

		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "UnknownResponse");
		OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, structured);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// responseModel.add(container.model);
	}

	public void generateNotFoundResponse() throws UnsupportedEncodingException {

		// BaseResponseContainer container = new BaseResponseContainer();

		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "NotFoundResponse");
		OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, structured);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// responseModel.add(container.model);
	}

	public void generateNegativeResponse(String spokenText) throws UnsupportedEncodingException {
		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "NegativeResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generatePositiveResponse(String spokenText) throws UnsupportedEncodingException {
		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "PositiveResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateURLResponse(String url, String spokenText) throws UnsupportedEncodingException {
		OntClass URLResponse = container._m.createClass(Namespaces.RESPONSE + "URLResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), URLResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		if (Strings.isNullOrEmpty(spokenText)) {
			responseInd.addProperty(container.text, "Here is a link with additional information.");
		} else {
			spokenText = new String(spokenText.getBytes(), StandardCharsets.UTF_8);
			responseInd.addProperty(container.text, spokenText);
		}

		responseInd.addProperty(container.url, url);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateSocialMediaTopicsResponse(String text, String spokenText) throws UnsupportedEncodingException {
		OntClass SocialMediaResponse = container._m.createClass(Namespaces.RESPONSE + "SocialMediaTopicsResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				SocialMediaResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		// responseInd.addProperty(container.text, spokenText);
		responseInd.addProperty(container.text, text);

		// container
		container.responseContainerInd.addProperty(container.text, spokenText);
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateRecipeResponse(String text, String spokenText) throws UnsupportedEncodingException {
		OntClass RecipeResponse = container._m.createClass(Namespaces.RESPONSE + "RecipeResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				RecipeResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		responseInd.addProperty(container.text, text);

		// container
		container.responseContainerInd.addProperty(container.text, spokenText);
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateMissingInformationResponse() throws UnsupportedEncodingException {
		OntClass MissingInformationResponse = container._m
				.createClass(Namespaces.RESPONSE + "MissingInformationResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				MissingInformationResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, "I cannot provide a response. You need to be more specific.");

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// responseModel.add(container.model);
	}

	public void generateSpecifyingInformationResponse(String spokenText) throws UnsupportedEncodingException {
		OntClass SpecifyingInformationRequest = container._m
				.createClass(Namespaces.RESPONSE + "SpecifyingInformationResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				SpecifyingInformationRequest);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// responseModel.add(container.model);
	}

	public void generateProactiveResponse(String spokenText) throws UnsupportedEncodingException {
		OntClass ProactiveResponse = container._m.createClass(Namespaces.RESPONSE + "ProactiveResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				ProactiveResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		// container.responseContainerInd.addProperty(container.timestamp,
		// container.model.createTypedLiteral(new
		// DateTime().toGregorianCalendar()));

		// responseModel.add(container.model);
	}

	public boolean handleGenericTopicResponse(Resource TopicClass) throws UnsupportedEncodingException {
		Property responseTypeProp = topicModel.getProperty(Namespaces.KB_CONTEXT + "responseType");
		RDFNode responseTypePropertyValue = TopicClass.as(OntClass.class).getPropertyValue(responseTypeProp);
		logger.info("PROEPRTY VALUES " + responseTypePropertyValue);
		if (responseTypePropertyValue == null) {
			this.generateMissingInformationResponse();
			return false;

		} else if (responseTypePropertyValue.toString().contains("SpecifyingInformationRequest")) {
			Property responseTypeValuesProp = topicModel.getProperty(Namespaces.KB_CONTEXT + "responseTypeValues");
			AnnotationProperty spokenProp = topicModel.getAnnotationProperty(Namespaces.KB_CONTEXT + "spoken");
			String spokenText = TopicClass.as(OntClass.class).getPropertyValue(spokenProp).asLiteral().getString();
			List<Statement> responseTypes = TopicClass.as(OntClass.class).listProperties(responseTypeValuesProp)
					.toList();
			List<String> joinValues = new ArrayList<String>();
			for (Statement statement : responseTypes) {
				OntClass r = statement.getObject().as(OntClass.class);
				joinValues.add(r.getPropertyValue(spokenProp).asLiteral().getString());
			}
			spokenText += String.join(", ", joinValues);
			logger.info(spokenText);
			this.generateSpecifyingInformationResponse(spokenText);
			EVENT e = new EVENT();
			e.setValue("DietRestrictions");
			this.kSession.insert(e);
			return true;
		}
		return false;

	}

	public ArrayList<Resource> handleMultipleTopics(Topic topic) {
		ArrayList values = topic.getValues();
		ArrayList<Resource> result = new ArrayList<>();
		for (Object r : values) {
			if (this.isSubClassOf(Namespaces.KB_CONTEXT + "IndividualContext", (Resource) r)) {
				result.add((Resource) r);
				return result;
			}

			if (this.isSpecial((Resource) r)) {
				result.add((Resource) r);
				return result;
			}
		}
		result.add((Resource) values.get(0));
		return result;

	}

	public boolean handleEatingHabitResponse(Topic topic)
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		// has diet restriction ?
		List<String> alternativeTopics = new ArrayList<>();
		List<String> alternativeText = new ArrayList<>();
		DietRestrictionService drs = new DietRestrictionService(true, user, scenario);
		boolean hasDietRestriction = drs.hasDietRestriction();
		if (hasDietRestriction) {
			alternativeText.add("dietary restrictions");
			alternativeTopics.add("DietRestrictions");
		}
		drs.shutdown();
		// has eating allergies ?
		EatingAllergiesService eas = new EatingAllergiesService(true, user, scenario);
		boolean hasEatingAllergies = eas.hasEatingAllergies();
		if (hasEatingAllergies) {
			alternativeText.add("eating allergies");
			alternativeTopics.add("EatingAllergies");
		}
		eas.shutdown();
		// has Eating preferences ?
		EatingPreferencesService epf = new EatingPreferencesService(true, user, scenario);
		boolean hasEatingPreferences = epf.hasEatingPreferences();
		if (hasEatingPreferences) {
			alternativeText.add("eating preferences");
			alternativeTopics.add("EatingPreferences");
		}
		epf.shutdown();

		if (alternativeTopics.isEmpty()) {
			this.generateNegativeResponse("No");
			return false;
		} else {
			container.responseContainerInd.addProperty(container.text, "Yes.");

			// select one to send as response
			String selected = alternativeTopics.remove(0);
			alternativeText.remove(0);
			if (!alternativeText.isEmpty()) {
				this.generateSpecifyingInformationResponse(
						"I can also give you information about " + String.join(", ", alternativeText));
			}

			EVENT e = new EVENT();
			e.setValue(selected);
			e.setPayload("{\"spokenText\": false}");
			this.kSession.insert(e);
			return true;

		}
	}

	public void handleDietRestrictionsResponse(Topic topic, EVENT e)
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		logger.info("handleDietRestrictionsResponse java code");
		DietRestrictionService s = new DietRestrictionService(true, user, scenario);
		List<Model> models = s.getPatterns();
		s.shutdown();
		String payload = (String) e.getPayload();
		JSONObject obj = new JSONObject(payload);
		boolean shouldGenerateText = obj.getBoolean("spokenText");
		String spokenText = "";
		if (models.isEmpty()) {
			if (topic.getRequestType().toString().equals(Namespaces.LA_DIALOGUE + "BooleanRequest")) {
				if (shouldGenerateText) {
					spokenText = "No.";
				}
			} else if (topic.getRequestType().toString().equals(Namespaces.LA_DIALOGUE + "Request")) {
				if (shouldGenerateText) {
					spokenText = "There are no diet restrictions.";
				}
			}
			this.generateNegativeResponse(spokenText);
		} else {
			// if
			// (topic.getRequestType().toString().equals(Namespaces.LA_DIALOGUE
			// + "BooleanRequest")) {
			// if (shouldGenerateText) {
			// spokenText = "Yes.";
			// // container.responseContainerInd.addProperty(container.text,
			// // spokenText);
			// this.generatePositiveResponse("Yes");
			// }
			//
			// } else {
			for (Model m : models) {
				OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
				OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

				// response
				Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
						StatementResponse);
				responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
				responseInd.addProperty(container.responseType, structured);

				List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
				for (ReifiedStatement rs : reifiedStatements) {
					responseInd.addProperty(container.rdf, rs);
				}
				container.model.add(m);

				// container
				container.responseContainerInd.addProperty(container.containsResponse, responseInd);
			}
			// }
		}

	}

	public void handleEatingAllergiesResponse(Topic topic)
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		logger.info("eating allergies in java code");
		EatingAllergiesService s = new EatingAllergiesService(true, user, scenario);
		List<Model> models = s.getPatterns();
		s.shutdown();
		if (models.isEmpty()) {
			this.generateNegativeResponse("No allergies found.");
			return;
		}

		// generate responses
		// if (topic.getRequestType().toString().equals(Namespaces.LA_DIALOGUE +
		// "Request")) {
		for (Model m : models) {
			OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		}
		// } else {
		// this.generatePositiveResponse("Yes");
		// }

	}

	public void handleSocialMediaTopicsResponse(Topic topic) throws Exception {
		// InitialContext jndi = new InitialContext();
		// SocialMediaBean socialMediaService = (SocialMediaBean) jndi
		// .lookup("java:app/kristina-j2ee-ejb/SocialMediaBean");
		// String result = socialMediaService.start();
		// if (result == null) {
		// this.generateNegativeResponse("There are no topics available.");
		// return;
		// }

		// TODO handle responses...
		// this.generateSocialMediaTopicsResponse(result, "Here is a link with
		// the currently discussed topics.");
		// PROACTIVE response
		// this.generateProactiveResponse("Do you want me to show tweets on a
		// specific topic?");
		HashMap<String, String> trans_es = new HashMap<>();
		trans_es.put("Cinema", "Cine");
		trans_es.put("Concert", "Concierto");
		trans_es.put("Movie", "Película");
		trans_es.put("Politics", "Política");
		trans_es.put("Sports", "Deportes");
		trans_es.put("Theater", "Teatro");

		HashMap<String, String> trans_de = new HashMap<>();
		trans_de.put("Cinema", "Kino");
		trans_de.put("Concert", "Konzert");
		trans_de.put("Movie", "Film");
		trans_de.put("Politics", "Politik");
		trans_de.put("Sports", "Sport");
		trans_de.put("Theater", "Theater");

		HashMap<String, String> trans_tr = new HashMap<>();
		trans_tr.put("Cinema", "Sinema");
		trans_tr.put("Concert", "Konser");
		trans_tr.put("Movie", "Film");
		trans_tr.put("Politics", "Siyaset");
		trans_tr.put("Sports", "Spor Dalları");
		trans_tr.put("Theater", "Tiyatro");

		String _topic = "";
		String lang = topic.getLanguage().toLowerCase();
		for (Resource r : keyEntities) {
			System.out.println(r);
			if (isSubClassOf(Namespaces.LA_ONTO + "SocialMediaTopic", r)) {
				_topic = r.getLocalName();
				if (lang.equals("de")) {
					_topic = trans_de.get(_topic);
				} else if (lang.equals("tr")) {
					_topic = trans_tr.get(_topic);
				} else if (lang.equals("es")) {
					_topic = trans_es.get(_topic);
				}
			}
		}
		System.out.println(_topic);
		_topic = java.net.URLEncoder.encode(_topic, "UTF-8");
		System.out.println(_topic);

		this.generateURLResponse("160.40.51.32:9000/topicDetectionHTML?language=" + lang + "&keyword=" + _topic,
				"Here is a link with the currently discussed topics.");

	}

	public void handleSocialMediaKeywordResponse(Topic topic, String keyword) throws Exception {
		// InitialContext jndi = new InitialContext();
		// SocialMediaBean socialMediaService = (SocialMediaBean) jndi
		// .lookup("java:app/kristina-j2ee-ejb/SocialMediaBean");
		// String result = socialMediaService.start();
		// if (result == null) {
		// this.generateNegativeResponse("There are no topics available.");
		// return;
		// }

		// TODO handle responses...
		// this.generateSocialMediaTopicsResponse(result, "Here is a link with
		// the currently discussed topics.");
		// PROACTIVE response
		// this.generateProactiveResponse("Do you want me to show tweets on a
		// specific topic?");

		// http://160.40.51.32:9000/tweetSearch?keyword=vacunaci%C3%B3n&language=es&maxResults=15

		String lang = topic.getLanguage().toLowerCase();
		System.out.println(keyword);
		// keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		System.out.println(keyword);
		this.generateURLResponse(
				"http://160.40.51.32:9000/tweetSearch?keyword=" + keyword + "&language=" + lang + "&maxResults=15",
				"Here is a link with the currently discussed topics.");

	}

	public String getLocation() throws RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);
		org.openrdf.model.Statement locationKB = s.getLocation();
		s.shutdown();
		if (locationKB != null) {
			return locationKB.getObject().stringValue();
		}

		return null;
	}

	public void handleLocalEventResponse(Topic topic) throws Exception {

		ProfileService s = new ProfileService(true, user, scenario);
		org.openrdf.model.Statement locationKB = s.getLocation();
		s.shutdown();
		String location = "";
		if (locationKB != null) {
			location = locationKB.getObject().stringValue();
		}

		InitialContext jndi = new InitialContext();
		LocalEventBean localEventService = (LocalEventBean) jndi.lookup("java:app/kristina-j2ee-ejb/LocalEventBean");
		String url = localEventService.start(location, topic.getLanguage());
		if (url == null) {
			// this.generateNegativeResponse("Sorry, I cannot find any event.");
			this.generateNotFoundResponse();
			return;
		}

		this.generateURLResponse(url, "Here is a link with some events that may interest you.");

		// TODO PROACTIVE response??
	}

	public String handleBabyContextCheckUpRecommended(Topic topic) throws Exception {
		ProfileService p = new ProfileService(true, user, scenario);
		Integer age = p.getBabyAge();
		p.shutdown();
		if (age == null) {
			return null;
		}
		if (age < 1) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 1);
			return "Revisión del recién nacido.";
		} else if (age >= 1 && age < 2) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 2);
			return "Revisión al mes.";
		} else if (age >= 2 && age < 4) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 3);
			return "Revisión de los 2 meses.";
		} else if (age >= 4 && age < 6) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 4);
			return "Revisión de los 4 meses.";
		} else if (age >= 6 && age < 9) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 5);
			return "Revisión de los 6 meses.";
		} else if (age >= 9) {
			System.out.println("handleBabyContextCheckUpRecommended: " + 6);
			return "Revisión de los 9 meses.";
		}
		return null;
	}

	public void handleBabyEventResponse(Topic topic) throws Exception {

		ProfileService s = new ProfileService(true, user, scenario);
		org.openrdf.model.Statement locationKB = s.getLocation();
		s.shutdown();
		String location = "";
		if (locationKB != null) {
			location = locationKB.getObject().stringValue();
		}

		InitialContext jndi = new InitialContext();
		BabyEventBean localEventService = (BabyEventBean) jndi.lookup("java:app/kristina-j2ee-ejb/BabyEventBean");
		String url = localEventService.start(location, topic.getLanguage());
		if (url == null) {
			// this.generateNegativeResponse("Sorry, I cannot find any event.");
			this.generateNotFoundResponse();
			return;
		}

		this.generateURLResponse(url, "Here is a link with some events that may interest you.");

		// TODO PROACTIVE response??
	}

	public void handleRecipeResponse(Topic topic) throws Exception {

		if (((Resource) topic.getValues().get(0)).getLocalName().contains("Diabetes")) {
			if (topic.getLanguage().toLowerCase().equals("de")) {
				this.generateURLResponse("http://diabsite.de/ernaehrung/rezepte/index.html",
						"Here is a link with recipes.");
			} else {
				this.generateURLResponse("http://menudiabetyka.pl/", "Here is a link with recipes.");
			}

			return;
		}

		InitialContext jndi = new InitialContext();
		RecipeBean recipeService = (RecipeBean) jndi.lookup("java:app/kristina-j2ee-ejb/RecipeBean");
		String text = recipeService.start(topic.getUserText(), topic.getLanguage());
		if (text == null) {
			// this.generateNegativeResponse("Sorry, I cannot find any
			// recipe.");
			this.generateNotFoundResponse();
			return;
		}

		this.generateURLResponse(text, "Here is a link with a recipe.");

		// TODO PROACTIVE response??
	}

	public void handleFavouriteFoodResponse(Topic topic)
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		logger.info("favourite food in java");
		FavouriteFoodService s = new FavouriteFoodService(true, user, scenario);
		List<Model> models = s.getPatterns();
		s.shutdown();

		if (models.isEmpty()) {
			this.generateNegativeResponse("No favourite found.");
			return;
		}

		// generate responses
		for (Model m : models) {
			OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		}

	}

	public void handleEatsAnimalProductResponse(Topic topic) throws UnsupportedEncodingException, RepositoryException,
			RepositoryConfigException, MalformedQueryException, QueryEvaluationException, UsernameException {
		logger.info("diet restrictions in java code");

		OntClass AnimalProduct = topicModel.getOntClass(Namespaces.LA_ONTO + "AnimalProduct");
		boolean hasRestriction = false;
		for (Resource c : keyEntities) {
			if (AnimalProduct.hasSubClass(c)) {
				System.out.println(c);
				DietRestrictionService s = new DietRestrictionService(true, user, scenario);
				hasRestriction = s.hasDietRestriction(c.getLocalName());
				s.shutdown();
				break;
			}
		}

		if (!hasRestriction) {
			this.generatePositiveResponse("Yes");
		} else {
			this.generateNegativeResponse("No");
		}

		// find the animal product

	}

	public void handleGainResponse(Topic topic)
			throws UnsupportedEncodingException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);
		int age = s.getBabyAge(); // months
		double weight = s.getBabyWeight(); // kgs
		double height = s.getBabyHeight(); // cms
		boolean male = s.getBabyGender().equals("male");

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

		String format = String.format("Según los datos que has introducido el bebé debería pesar %2.2f Kg", avgWeight);
		this.generateTextResponse(format);
		s.shutdown();
	}

	public void handleDrinkAlcoholResponse(Topic topic) throws UnsupportedEncodingException, RepositoryException,
			RepositoryConfigException, MalformedQueryException, QueryEvaluationException, UsernameException {
		logger.info("drink alcohol restrictions in java code");

		DietRestrictionService s = new DietRestrictionService(true, user, scenario);
		boolean hasRestriction = s.hasDietRestriction("alcohol");
		s.shutdown();
		if (!hasRestriction) {
			this.generatePositiveResponse("Yes");
		} else {
			this.generateNegativeResponse("No");
		}

		// find the animal product

	}

	public void callIR(Topic topic) throws Exception {
		InitialContext jndi = new InitialContext();
		PassageRetrievalBean PassageRetrievalBean = (PassageRetrievalBean) jndi
				.lookup("java:app/kristina-j2ee-ejb/PassageRetrievalBean");
		IRResponse content = PassageRetrievalBean.start(topic.getUserText(), topic.getLanguage());

		if (content == null) {
			// this.generateUnknownResponse();
			this.generateNotFoundResponse();
			return;
		}

		this.generateTextResponse(content);

	}

	public void callIR(Topic topic, String text) throws Exception {
		String l = topic.getLanguage();
		if (l == null || l.isEmpty()) {
			l = "de";
		}
		if (text.equals("RequestInfoSleep")) {
			if (l.equals("de")) {
				text = "Gibt es eine Anleitung zur Schlafhygiene?";
			} else {
				text = "Czy są jakieś wskazówki/wytyczne dotyczące lepszego/zdrowego snu?";
			}
		} else if (text.equals("RequestInfoSleepHygiene")) {
			if (l.equals("de")) {
				text = "Gibt es eine Anleitung zur Schlafhygiene?";
			} else {
				text = "Możesz mi podać / Czy są jakieś wskazówki/wytyczne dotyczące higieny snu";
			}
		} else if (text.equals("RequestInfoDementia")) {
			if (l.equals("de")) {
				text = "Kannst du mir (mehr/weitere) Information über Demenz";

			} else {
				text = "Czy możesz mi podać parę ogólnych/więcej/dalszych informacji na temat demencji (i jej kondycji)";
			}
		} else if (text.equals("RequestInfoDiabetes")) {
			if (l.equals("de")) {
				text = "Kannst du mir allgemeine / mehr / weitere Information zu/über Diabetes";
			} else {
				text = "Czy możesz mi podać parę ogólnych/więcej/dalszych informacji na temat diety cukrzyków";
			}
		} else if (text.equals("RequestInfoProtectionBaby")) {
			text = "¿Es seguro salir para ir a pasear aunque haga calor hoy? ¿Me puedes recomendar una precaución especial para el sol?";
		}

		InitialContext jndi = new InitialContext();
		PassageRetrievalBean PassageRetrievalBean = (PassageRetrievalBean) jndi
				.lookup("java:app/kristina-j2ee-ejb/PassageRetrievalBean");
		IRResponse content = PassageRetrievalBean.start(text, topic.getLanguage());

		if (content == null) {
			// this.generateUnknownResponse();
			this.generateNotFoundResponse();
			return;
		}

		this.generateTextResponse(content);

	}

	public void callIndividualContext(Topic topic, Object payload) throws Exception {
		IndividualContextService s = new IndividualContextService(true, user, scenario);
		String[] split = ((String) payload).split("\\$\\$");
		System.out.println(split);
		String filenamesString = split[0].trim();
		String[] filenames = filenamesString.split(",");
		System.out.println("Filenames: " + Arrays.toString(filenames));
		List<Model> models = s.getPatterns(filenames);

		int counter = 1;
		for (Model m : models) {
			OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);
			responseInd.addProperty(container.rank, container.model.createTypedLiteral(counter++));

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		}

		String additionalResponsesString = split[1].trim();
		System.out.println("AdditionalInformation: " + additionalResponsesString);
		String[] additionalResponses = additionalResponsesString.split(",");
		if (additionalResponses.length == 0)
			return;
		models = s.getPatterns(additionalResponses);
		for (Model m : models) {
			OntClass AdditionalInformation = container._m
					.createClass(Namespaces.RESPONSE + "AdditionalInformationRequest");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					AdditionalInformation);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);

		}
		s.shutdown();

	}

	public void generateStatementResponse(List<? extends Model> models) {
		int counter = 1;
		for (Model m : models) {
			OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);
			responseInd.addProperty(container.rank, container.model.createTypedLiteral(counter++));

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		}
	}

	public void generateAdditionalResponse(List<? extends Model> models) {
		for (Model m : models) {
			OntClass AdditionalInformation = container._m
					.createClass(Namespaces.RESPONSE + "AdditionalInformationRequest");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					AdditionalInformation);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);

			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);

			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		}
	}

	public void handleActivityDuration(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel sleepDurationModel = s.getSleepDurationPattern();
		if (sleepDurationModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(sleepDurationModel);
		generateStatementResponse(models);

		OntModel upTimeModel = s.getWakeUpTimePattern();
		if (upTimeModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(upTimeModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleToiletFrequency(Topic topic)
			throws RepositoryException, RepositoryConfigException, IOException, UsernameException {

		ProfileService s = new ProfileService(true, user, scenario);

		OntModel toiletFreqModel = s.getToiletFrequencyModel();
		if (toiletFreqModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(toiletFreqModel);
		generateStatementResponse(models);

		OntModel assistModel = s.needsToiletAssistanceModel();
		if (assistModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(assistModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleWakeUpFrequencyContext(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel wakeUpFrequencyPattern = s.getWakeUpFrequencyPattern();
		if (wakeUpFrequencyPattern == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(wakeUpFrequencyPattern);
		generateStatementResponse(models);

		OntModel upTimeModel = s.getWakeUpTimePattern();
		if (upTimeModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(upTimeModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleSleepTimeContext(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel sleepTimePatternModel = s.getSleepTimePattern();
		if (sleepTimePatternModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(sleepTimePatternModel);
		generateStatementResponse(models);

		OntModel upTimeModel = s.getWakeUpTimePattern();
		if (upTimeModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(upTimeModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleWakeUpContext(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel wakeUpTimePatternModel = s.getWakeUpTimePattern();
		if (wakeUpTimePatternModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(wakeUpTimePatternModel);
		generateStatementResponse(models);

		OntModel sleepDurationModel = s.getSleepDurationPattern();
		if (sleepDurationModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(sleepDurationModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleNeedsAssistance(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel needsAssistanceModel = s.needsAssistanceModel();
		if (needsAssistanceModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(needsAssistanceModel);
		generateStatementResponse(models);
		s.shutdown();
	}

	public void handleNeedsAssistanceForToilet(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		OntModel needsAssistanceForToiletPatternModel = s.needsToiletAssistanceModel();
		if (needsAssistanceForToiletPatternModel == null) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		List<Model> models = new ArrayList<Model>();
		models.add(needsAssistanceForToiletPatternModel);
		generateStatementResponse(models);

		OntModel toiletFrequencyModel = s.getToiletFrequencyModel();
		if (toiletFrequencyModel == null) {
			s.shutdown();
			return;
		}
		models = new ArrayList<Model>();
		models.add(toiletFrequencyModel);
		generateAdditionalResponse(models);
		s.shutdown();
	}

	public void handleProblems(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> problems = s.getProblems();
		if (problems.isEmpty()) {
			generateNegativeResponse("");
			s.shutdown();
			return;
		}
		generateStatementResponse(problems);
		s.shutdown();
	}

	public void handleSleepProblem(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> sleepProblems = s.getSleepProblems();
		if (sleepProblems.isEmpty()) {
			generateNegativeResponse("");
			s.shutdown();
			return;
		}
		generateStatementResponse(sleepProblems);
		s.shutdown();
	}

	public void handleDiseaseContext(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> diseasesModel = s.getDiseases();
		if (diseasesModel.isEmpty()) {
			generateNegativeResponse("");
			s.shutdown();
			return;
		}
		generateStatementResponse(diseasesModel);
		s.shutdown();
	}

	public void handleMemoryProblem(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> memoryProblemsModel = s.getMemoryProblem();
		if (memoryProblemsModel.isEmpty()) {
			generateNegativeResponse("");
			s.shutdown();
			return;
		}
		generateStatementResponse(memoryProblemsModel);
		s.shutdown();
	}

	public void handleFavouriteBoardGame(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> favouriteBoardGame = s.getFavouriteBoardGame();
		if (favouriteBoardGame.isEmpty()) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		generateStatementResponse(favouriteBoardGame);
		s.shutdown();
	}

	public void handleSleepHelp(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> activitiesBrforeSleepModels = s.getActivitiesBeforeSleep();
		if (activitiesBrforeSleepModels.isEmpty()) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		generateStatementResponse(activitiesBrforeSleepModels);
		s.shutdown();
	}

	public void handleActivitiesBeforeSleep(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> activitiesBrforeSleepModels = s.getActivitiesBeforeSleep();
		if (activitiesBrforeSleepModels.isEmpty()) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		generateStatementResponse(activitiesBrforeSleepModels);

		OntModel sleepTimeModel = s.getSleepTimePattern();
		if (sleepTimeModel == null) {
			s.shutdown();
			return;
		}
		List<OntModel> models = new ArrayList<OntModel>();
		models.add(sleepTimeModel);
		generateAdditionalResponse(models);
		s.shutdown();

	}

	public void handleLikesToDoAfterAnActivityContext(Topic topic)
			throws IOException, RepositoryException, RepositoryConfigException, UsernameException {
		ProfileService s = new ProfileService(true, user, scenario);

		List<OntModel> activitiesAfterTVModels = s.getActivitiesAfterTV();
		if (activitiesAfterTVModels.isEmpty()) {
			generateUnknownResponse();
			s.shutdown();
			return;
		}
		generateStatementResponse(activitiesAfterTVModels);

		OntModel sleepTimeModel = s.getSleepTimePattern();
		if (sleepTimeModel == null) {
			s.shutdown();
			return;
		}
		List<OntModel> models = new ArrayList<OntModel>();
		models.add(sleepTimeModel);
		generateAdditionalResponse(models);

		s.shutdown();
	}

	public void handleNearestPlaces(Topic topic, String type) throws Exception {
		ProfileService s = new ProfileService(true, user, scenario);
		org.openrdf.model.Statement locationKB = s.getLocation();
		String location = "Tubingen";
		s.shutdown();
		if (locationKB != null) {
			location = locationKB.getObject().stringValue();
		}

		this.generateURLResponse("160.40.51.32:9000/places?address=" + location + "&placeType=" + type,
				"Here is a link with nearest " + type + "s.");

	}

	// public EntryPoint getEntryPoint(String entryPoint) {
	// return kSession.getEntryPoint(entryPoint);
	// }
	//
	// public KieSession getkSession() {
	// return kSession;
	// }
	//
	// public SessionClock getClock() {
	// return kSession.getSessionClock();
	// }

	public List<ReifiedStatement> createReifiedStatements(Model m) {
		List<ReifiedStatement> results = new ArrayList<>();
		StmtIterator listStatements = m.listStatements();
		while (listStatements.hasNext()) {
			Statement statement = listStatements.next();
			if (statement.getPredicate().equals(OWL.imports) || statement.getObject().equals(OWL.Ontology)) {
				listStatements.remove();
				continue;
			}
			ReifiedStatement reifiedStatement = statement.createReifiedStatement(Utils.tempFullURI());
			results.add(reifiedStatement);
		}
		listStatements.close();
		return results;
	}

	public boolean isSubClassOf(String topclass, Object topicClass) {
		// System.out.println("dddddddddddddddddddddddd" + topicClass);
		// System.out.println("dddddddddddddddddddddddd" + topclass);
		OntClass TopClass = RulesServiceBean.topicModel.getOntClass(topclass);
		if (TopClass == null) {
			return false;
		}
		// OntClass TopicClass = topicModel.getOntClass(topicClass);
		return TopClass.hasSubClass((Resource) topicClass);
		// retu//rn true;

	}

	public boolean isSpecial(Object topicClass) {
		// System.out.println("dddddddddddddddddddddddd" + topicClass);
		// System.out.println("dddddddddddddddddddddddd" + topclass);
		AnnotationProperty p = RulesServiceBean.topicModel.getAnnotationProperty(Namespaces.KB_CONTEXT + "isSpecial");

		Statement property = RulesServiceBean.topicModel.getProperty((Resource) topicClass, p);
		return property != null;
	}

	public boolean isGenericTopic(Resource topic) {
		AnnotationProperty generic = topicModel.getAnnotationProperty(Namespaces.KB_CONTEXT + "generic");
		return topicModel.contains(topic, generic, topicModel.createTypedLiteral(true));
	}

	public void stop() {
		System.out.println("halting...");
		kSession.halt();
		System.out.println("Done.");
	}

	// public void insert(Observation[] observations) {
	// EntryPoint ep = getEntryPoint("context");
	// for (Observation o : observations) {
	// // o.setStart(new Date());
	// System.out.println("inserting observation: " + o.getStart().getTime() + "
	// " + o.getEnd().getTime());
	// ep.i nsert(o);
	// }
	// }
	//
	// public void setLatest(HashSet<Observation> observations) {
	// System.out.println("setLatest");
	// latest = observations;
	// System.out.println(latest.size());
	// }
	//
	// public void addLatestOffline(Observation observation) {
	// System.out.println("offline setLatest");
	// // System.out.println(
	// // "inserting observation: " + observation.getStart().getTime() + " " +
	// // observation.getEnd().getTime());
	// offlineLatest.add(observation);
	// System.out.println("offline size: " + offlineLatest.size());
	// }
	//
	// public HashSet<Observation> getLatest(int seconds) {
	//
	// // System.out.println("in");
	// // List<Observation> observations = new ArrayList<>();
	// // QueryResults results = kSession.getQueryResults("latest");
	// // kSession.fireAllRules();
	// // System.out.println("ok");
	//
	// return latest;
	// }

	// public static void main(String[] args) throws InterruptedException {
	// RulesServiceBean b = new RulesServiceBean();
	// b.initialisation();
	// // TimeUnit.SECONDS.sleep(3);
	// Observation vi1 = new Observation();
	// EntryPoint ep = b.getEntryPoint("context");
	// ep.insert(vi1);
	// // TimeUnit.SECONDS.sleep(3);
	// // b.getLatest(2);
	// // System.out.println(b.latest.size());
	//
	// }

	// public void clear() {
	// EntryPoint ep = getEntryPoint("context");
	// Collection<FactHandle> factHandles = ep.getFactHandles();
	// // System.out.println("delete: " + factHandles.size());
	// for (FactHandle factHandle : factHandles) {
	// ep.delete(factHandle);
	// }
	//
	// factHandles = kSession.getFactHandles();
	// // System.out.println("delete: " + factHandles.size());
	// for (FactHandle factHandle : factHandles) {
	// kSession.delete(factHandle);
	// }
	// }

	public void flashModel() {
		responseModel.add(container.model);
	}

	public int getId(ArrayList<Observation> obs) {
		int id = 0;
		for (Observation m : obs) {
			id += m.hashCode();
		}
		return id;
	}

	public long getDurationInMillis(Date start, Date end) {
		Duration i1 = new Duration(new DateTime(start), new DateTime(end));
		return i1.getMillis();
	}

	public static void main(String[] args)
			throws RepositoryException, RepositoryConfigException, IOException, UsernameException {
		RulesServiceBean r = new RulesServiceBean();
		r.handleGainResponse(null);
	}

}
