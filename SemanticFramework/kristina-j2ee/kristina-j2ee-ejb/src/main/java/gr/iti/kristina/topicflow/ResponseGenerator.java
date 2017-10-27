package gr.iti.kristina.topicflow;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.responses.BaseResponseContainer;
import gr.iti.kristina.topicflow.services.IRResponse;
import gr.iti.kristina.topicflow.services.PassageRetrieval;
import gr.iti.kristina.topicflow.services.ProfileService;
import gr.iti.kristina.topicflow.services.RecipeBean;
import gr.iti.kristina.topicflow.services.WeatherServiceBean;
import gr.iti.kristina.utils.Utils;

public class ResponseGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseGenerator.class);

	private ThemeKB themeKB;
	String scenario;
	String name;
	String lang;
	String userText;

	private OntModel responseModel;

	public BaseResponseContainer container;

	ProfileService p;

	Set<Output> outputs;

	public ResponseGenerator(ThemeKB themeKB, String name, String scenario, String lang, String userText) {
		this.themeKB = themeKB;
		this.name = name;
		this.scenario = scenario;
		this.lang = lang;
		this.userText = userText;
		responseModel = Utils.createDefaultModel(false);
	}

	public void initialise() throws RepositoryException, RepositoryConfigException, UsernameException {
		responseModel.removeAll();
		container = new BaseResponseContainer();
		container.responseContainerInd.addProperty(container.timestamp,
				container.model.createTypedLiteral(new DateTime().toGregorianCalendar()));
		if (p != null) {
			LOG.debug("shuting down profile service");
			p.shutdown();
		}
		p = new ProfileService(true, name, scenario, lang);
		outputs = Sets.newHashSet();
		LOG.debug("ResponseGenerator sucessfully initialised.");
	}

	public OntModel flashModel(State currentState) {
		// assign the topic/theme at the container level...
		OntClass _conversationalContext = currentState.topic != null ? currentState.topic : currentState.theme;
		if (_conversationalContext != null)
			container.responseContainerInd.addProperty(container.conversationalContext, _conversationalContext);
		responseModel.add(container.model);
		return responseModel;
	}

	// active theme + multiple topics
	// public void iCanTellYouAboutTheseTopics(Set<OntClass> topics) {
	// System.out.println(">>> [topic clarification] " + topics.stream()
	// .map(x -> themeKB.getSpokenText(x)).collect(Collectors.joining(" OR ")));
	// }

	// active theme + one topic
	public void direct(OntClass topic, boolean additional) throws Exception {
		Set<String> responseTypes = themeKB.getResponseTypes(topic);
		LOG.debug(String.format("responseTypes of %s: %s", topic, responseTypes));
		if (responseTypes.contains("KB")) {
			JavaConfig config = themeKB.getJavaConfigProperty(topic);
			if (config != null) {
				if (StringUtils.isBlank(config.type) || config.type.equals("model")) {
					List<Model> patterns = callMethodList(config.method, config.args);
					LOG.debug(patterns.size() + " patterns found for " + Utils.getLocalName(topic));
					if (patterns != null && patterns.isEmpty()) {
						String message = String.format("No '%s' found.", themeKB.getSpokenText(topic));
						this.generateNotFoundResponse(topic);
						outputs.add(new Output("NOTFOUND", topic));
						System.out.println(">>> [not found] " + message);
						return;
					}
					this.generateStatementResponse(topic, patterns);
					outputs.add(new Output(!additional ? "STATEMENT" : "ADDITIONAL", topic));
					System.out.println(">>> [KB response] " + themeKB.getSpokenText(topic));
				} else if (config.type.equals("text")) {
					String result = callMethodString(config.method, config.args);
					LOG.debug("method string result: " + result);
					if (StringUtils.isBlank(result)) {
						String message = String.format("No '%s' found.", themeKB.getSpokenText(topic));
						this.generateNotFoundResponse(topic);
						outputs.add(new Output("NOTFOUND", topic));
						System.out.println(">>> [not found] " + message);
						return;
					}
					this.generateTextResponse(topic, result);
					outputs.add(new Output("TEXT", topic));
					System.out.println(">>> [KB response] " + result);
				}

				else if (config.type.equals("boolean")) {
					boolean result = callMethodBoolean(config.method, config.args);
					LOG.debug("method boolean result: " + result);
					if (result) {
						this.generatePositiveResponse(topic, result + "");
					} else {
						this.generateNegativeResponse(topic, result + "");
					}
					outputs.add(new Output("KB", topic));
					System.out.println(">>> [KB boolean response] " + result);
				}

				else {
					LOG.error("response type not found");
					outputs.add(new Output("ERROR", topic));
				}
			} else {
				throw new IllegalArgumentException("topic does not have a java config snippet");
			}
		} else if (responseTypes.contains("IR")) {
			System.out.println(">>> [IR response] " + themeKB.getSpokenText(topic));
			JavaConfig java = themeKB.getJavaConfigProperty(topic);
			String _lang = lang;
			if (StringUtils.isNotBlank(java.lang)) {
				_lang = java.lang;
			}
			PassageRetrieval ir = new PassageRetrieval();
			String userInput = themeKB.getSpokenText(topic);
			IRResponse response = ir.start(userInput, _lang);
			if (response == null) {
				this.generateNotFoundResponse(topic);
				outputs.add(new Output("NOTFOUND", topic));
				return;
			}
			LOG.debug(response.toString());
			this.generateTextResponse(topic, response);
			LOG.warn("Need to enrich IR responses with domain concepts");
			outputs.add(new Output("IR", topic));
			// TODO call IR
		} else if (responseTypes.contains("TEXT")) {
			String text = themeKB.getText(topic);
			System.out.println(">>> [TEXT response] " + text);
			this.generateTextResponse(topic, text);
			outputs.add(new Output("TEXT", topic));
		} else if (responseTypes.contains("URL")) {
			JavaConfig java = themeKB.getJavaConfigProperty(topic);
			String url = "";
			if (!StringUtils.isBlank(java.method)) {
				url = callMethodString(java.method, java.args);
			} else {
				url = themeKB.getURL(topic);
			}
			String intro = themeKB.getText(topic);
			if (StringUtils.isBlank(intro)) {
				intro = themeKB.getSpokenText(topic);
			}
			System.out.println(">>> [URL response] " + intro + ": " + url);
			this.generateURLResponse(topic, url, intro);
			outputs.add(new Output("URL", topic));
		} else if (responseTypes.contains("MORE")) {
			String text = themeKB.getURL(topic);
			System.out.println(">>> [URL response] " + text);
			this.generateURLResponse(topic, text, null);
			outputs.add(new Output("MORE", topic));
		} else if (responseTypes.contains("SOCIAL")) {
			JavaConfig javaConfigProperty = themeKB.getJavaConfigProperty(topic);
			String keywords = "";
			if (javaConfigProperty != null) {
				List<String> args = javaConfigProperty.args;
				keywords = Translation.get(Sets.newHashSet(args), lang);
			}

			String link = "160.40.51.32:9000/topicDetectionHTML?language=" + lang + "&keyword=" + keywords;
			System.out.println(String.format(">>> [Social URL response %s] %s", keywords, link));
			this.generateURLResponse(topic, link, "Here is a link with the currently discussed topics.");
			outputs.add(new Output("SOCIAL", topic));
		}

		else if (responseTypes.contains("RECIPE")) {
			JavaConfig java = themeKB.getJavaConfigProperty(topic);
			if (java.args != null) {
				List<String> args = java.args;
				if (args.contains("profile")) {
					List<String> foodsString = p.getFavouriteFoodsString();
					this.generateRecipeResponse(topic, foodsString.get(0), userText);
					outputs.add(new Output("RECIPE", topic));
					System.out.println(String.format(">>> [RECIPE] %s", foodsString.get(0)));
					return;
				}
			}
			this.generateRecipeResponse(topic, null, userText);
			outputs.add(new Output("RECIPE", topic));
			System.out.println(String.format(">>> [RECIPE] %s", userText));
		}

		else if (responseTypes.contains("WEATHER")) {
			LinkedHashMultimap<String, String> weatherData = new WeatherServiceBean().start();
			generateWeatherResponse(topic, weatherData);
			outputs.add(new Output("WEATHER", topic));
			System.out.println(String.format(">>> [WEATHER] %s", weatherData));
		}

		else {
			LOG.warn(">>> [!caution: skip response / no_IR_KB] " + themeKB.getSpokenText(topic));
		}

	}

	// partial (no active theme detected)
	// public void iCanTellYouAboutTheseThemes(Set<OntClass> themes) {
	// System.out.println(">>> [theme clarification] " + themes.stream()
	// .map(x -> themeKB.getSpokenText(x)).collect(Collectors.joining(" OR ")));
	// }

	// active theme + no topics
	// public void noFurtherInfo() {
	// System.out.println(">>> [response] I have no further information on this
	// sub-topic. ");
	// generateNotFoundResponse();
	// }

	// do not understand
	public void unknown(OntClass topic) {
		System.out.println(">>> [unknown] I am sorry, I didn't understand you");
		this.generateUnknownResponse(topic);
		outputs.add(new Output("UNKNOWN", (OntClass) null));
	}

	public void notFound(OntClass topic) {
		System.out.println(">>> [notFound]");
		this.generateNotFoundResponse(topic);
		outputs.add(new Output("NOTFOUND", (OntClass) null));
	}

	// public void howCanIHelpYou() {
	// System.out.println(">>> [Poking] How can I help you?");
	// }

	public void proactiveResponse(Set<OntClass> topics) throws UnsupportedEncodingException {
		String message = topics.stream()
				.map(x -> themeKB.getSpokenText(x)).collect(Collectors.joining(" AND "));
		System.out.println(">>> [proactive] " + message);
		this.generateProactiveResponse(topics, message);
		outputs.add(new Output("PROACTIVE", topics));
	}

	public void clarificationResponse(String extraText, Set<OntClass> topics) {
		String text = topics.stream()
				.map(x -> themeKB.getSpokenText(x)).collect(Collectors.joining(" OR "));
		this.generateClarificationResponse(topics, text);

		System.out.println(String.format(">>> [%s clarification] ", extraText) + text);
		outputs.add(new Output("CLARIFICATION", topics));
	}

	public void specifyingMoreInformationResponse(Set<OntClass> topics) throws UnsupportedEncodingException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		for (OntClass topic : topics) {
			if (themeKB.getResponseTypes(topic).contains("KB")) {
				JavaConfig config = themeKB.getJavaConfigProperty(topic);
				if (StringUtils.isNotBlank(config.hasResults)) {
					boolean hasResults = callMethodBoolean(config.hasResults, config.args);
					if (!hasResults) {
						LOG.debug(String.format(
								"removing topic %s from specifyingMoreInformationResponse because does not have results",
								Utils.getLocalName(topic)));
						topics.remove(topic);
					}
				}
			}
		}

		if (topics.isEmpty()) {
			this.generateNotFoundResponse(OWL.Nothing.as(OntClass.class));
			System.out.println(">>> [notFound] no information found.");
			outputs.add(new Output("NOTFOUND", topics));
		} else {
			String text = topics.stream()
					.map(x -> themeKB.getSpokenText(x)).collect(Collectors.joining(" OR "));
			this.generateSpecifyingInformationResponse(topics, text);
			System.out.println(">>> [specifyingMoreInformation] " + text);
			outputs.add(new Output("SPECIFYINGMOREINFORMATION", topics));
		}

	}

	private void generateSpecifyingInformationResponse(Set<OntClass> topics, String spokenText)
			throws UnsupportedEncodingException {
		OntClass SpecifyingInformationRequest = container._m
				.createClass(Namespaces.RESPONSE + "SpecifyingInformationResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				SpecifyingInformationRequest);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		for (OntClass t : topics) {
			responseInd.addProperty(container.topic, t);
		}

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);

	}

	private void generateNegativeResponse(OntClass topic, String spokenText) throws UnsupportedEncodingException {
		LOG.debug("generating negative response: " + spokenText);
		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "NegativeResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generatePositiveResponse(OntClass topic, String spokenText) throws UnsupportedEncodingException {
		LOG.debug("generating negative response: " + spokenText);
		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "PositiveResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	private void generateClarificationResponse(Set<OntClass> topics, String spokenText) {
		OntClass ProactiveResponse = container._m.createClass(Namespaces.RESPONSE + "ClarificationResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				ProactiveResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		for (OntClass t : topics) {
			responseInd.addProperty(container.topic, t);
		}

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateProactiveResponse(Set<OntClass> topics, String spokenText) throws UnsupportedEncodingException {
		OntClass ProactiveResponse = container._m.createClass(Namespaces.RESPONSE + "ProactiveResponse");
		OntResource freetext = container._m.createOntResource(Namespaces.RESPONSE + "free_text");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				ProactiveResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, freetext);
		responseInd.addProperty(container.text, spokenText);
		for (OntClass t : topics) {
			responseInd.addProperty(container.topic, t);
		}

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);

	}

	private void generateStatementResponse(OntClass topic, List<Model> models) {
		LOG.debug(String.format("generating statement response for %d models", models.size()));
		for (Model m : models) {
			OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");
			OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");
			// response
			Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
					StatementResponse);
			responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
			responseInd.addProperty(container.responseType, structured);
			if (topic != null)
				responseInd.addProperty(container.topic, topic);
			List<ReifiedStatement> reifiedStatements = createReifiedStatements(m);
			for (ReifiedStatement rs : reifiedStatements) {
				responseInd.addProperty(container.rdf, rs);
			}
			container.model.add(m);
			// container
			container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		}
		LOG.debug("generateStatementResponse finished");
	}

	public void generateUnknownResponse(OntClass topic) {
		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "UnknownResponse");
		OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, structured);
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateTextResponse(OntClass topic, String text) throws UnsupportedEncodingException {
		// BaseResponseContainer container = new BaseResponseContainer();
		LOG.warn("Generating text response: " + text);
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
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateTextResponse(OntClass topic, IRResponse response) throws UnsupportedEncodingException {
		OntClass StatementResponse = container._m.createClass(Namespaces.RESPONSE + "StatementResponse");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				StatementResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));

		OntResource responseTypeValue = container.model.createOntResource(Namespaces.RESPONSE + "free_text");
		responseInd.addProperty(container.responseType, responseTypeValue);
		responseInd.addProperty(container.source, response.getDomain());
		responseInd.addProperty(container.text, response.getContent());
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	private void generateNotFoundResponse(OntClass topic) {

		OntClass NoResponse = container._m.createClass(Namespaces.RESPONSE + "NotFoundResponse");
		OntResource structured = container._m.createOntResource(Namespaces.RESPONSE + "structured");

		// response
		Individual responseInd = container.model.createIndividual(Utils.tempURI() + Utils.randomString(), NoResponse);
		responseInd.addProperty(container.plausibility, container.model.createTypedLiteral(plausibility()));
		responseInd.addProperty(container.responseType, structured);
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateURLResponse(OntClass topic, String url, String spokenText) throws UnsupportedEncodingException {

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
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
	}

	public void generateRecipeResponse(OntClass topic, String food, String userText) throws Exception {

		if (topic.getLocalName().contains("Diabetes")) {
			if (lang.toLowerCase().equals("de")) {
				this.generateURLResponse(topic, "http://diabsite.de/ernaehrung/rezepte/index.html",
						"Here is a link with recipes.");
			} else {
				this.generateURLResponse(topic, "http://menudiabetyka.pl/", "Here is a link with recipes.");
			}

			return;
		}

		RecipeBean recipeService = new RecipeBean();
		String text = "";
		if (StringUtils.isNotBlank(food)) {
			text = recipeService.start(food, lang);
		} else {
			text = recipeService.start(userText, lang);
		}

		if (text == null) {
			// this.generateNegativeResponse("Sorry, I cannot find any
			// recipe.");
			this.generateNotFoundResponse(topic);
			return;
		}

		this.generateURLResponse(topic, text, "Here is a link with a recipe.");
	}

	public void generateWeatherResponse(OntClass topic, LinkedHashMultimap<String, String> weatherData)
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
						this.generateProactiveResponse(Sets.newHashSet(topic),
								"The weather is rather cold today. Would you like me to read the newspaper for you?");
					} else if (t > 15) {
						forecastInd.addProperty(weatherClassification, "hot");
						responseInd.addProperty(weatherClassification, "hot");
						this.generateProactiveResponse(Sets.newHashSet(topic),
								"Nice weather today! Would you like me to check for events in the city?");
						this.generateProactiveResponse(Sets.newHashSet(topic),
								"Nice weather today! Do you want me to tell you where the nearest park is?");
						this.generateProactiveResponse(Sets.newHashSet(topic),
								"Nice weather today! Do you want me to tell you activities in the city where you can go with your baby?");
					}
				}
			}
		}
		responseInd.addProperty(container.text, Utils.modelToString(temp, "TTL"));
		if (topic != null)
			responseInd.addProperty(container.topic, topic);

		// container
		container.responseContainerInd.addProperty(container.containsResponse, responseInd);
		// container.responseContainerInd.addProperty(container.topic, topic);

		Individual topicInstance = container.model.createIndividual(Utils.tempURI() + Utils.randomString(),
				container._m.createClass(Namespaces.KB_CONTEXT + "WeatherContext"));

		container.responseContainerInd.addProperty(container.conversationalContext, topicInstance);
		// responseModel.add(container.model);
	}

	private List<ReifiedStatement> createReifiedStatements(Model m) {
		List<ReifiedStatement> results = new ArrayList<>();
		List<Statement> listStatements = m.listStatements().toList();
		for (Statement statement : listStatements) {
			if (statement.getPredicate().equals(OWL.imports) || statement.getObject().equals(OWL.Ontology)) {
				continue;
			}
			ReifiedStatement reifiedStatement = statement.createReifiedStatement(Utils.tempFullURI());
			results.add(reifiedStatement);
		}
		return results;
	}

	/**
	 * Actually check if it is KB and has results, otherwise return true
	 * 
	 * @param topic
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public boolean hasKBResults(OntClass topic) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Set<String> responseTypes = themeKB.getResponseTypes(topic);

		if (responseTypes.contains("KB")) {
			JavaConfig config = themeKB.getJavaConfigProperty(topic);
			if (StringUtils.isNotBlank(config.hasResults)) {
				Method method = p.getClass().getMethod(config.hasResults);
				boolean hasResponse = (boolean) method.invoke(p);
				LOG.debug(String.format("should [%s] participate in response? %b", Utils.getLocalName(topic),
						hasResponse));
				return hasResponse;
			} else {
				LOG.debug(String.format("should [%s] participate in response? %b", Utils.getLocalName(topic),
						true));
				return true;
			}
		}

		LOG.debug(String.format("should [%s] participate in response? %b", Utils.getLocalName(topic), true));
		return true;
	}

	public List<Model> callMethodList(String methodString, List<String> args) throws NoSuchMethodException,
			SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		LOG.debug("calling method: " + methodString + ", args: " + args);
		if (args == null) {
			Method method = p.getClass().getMethod(methodString);
			return (List<Model>) method.invoke(p);
		} else {
			Method method = p.getClass().getMethod(methodString, String.class);
			return (List<Model>) method.invoke(p, args.stream().toArray(String[]::new));
		}

	}

	public boolean callMethodBoolean(String methodString, List<String> args) throws NoSuchMethodException,
			SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		LOG.debug("calling method: " + methodString + ", args: " + args);
		if (args == null) {
			Method method = p.getClass().getMethod(methodString);
			return (boolean) method.invoke(p);
		} else {
			Method method = p.getClass().getMethod(methodString, String.class);
			return (boolean) method.invoke(p, args.stream().toArray(String[]::new));
		}
	}

	public String callMethodString(String methodString, List<String> args) throws NoSuchMethodException,
			SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		LOG.debug("calling method: " + methodString + ", args: " + args);
		if (args == null) {
			Method method = p.getClass().getMethod(methodString);
			return (String) method.invoke(p);
		} else {
			Method method = p.getClass().getMethod(methodString, String.class);
			return (String) method.invoke(p, args.stream().toArray(String[]::new));
		}
	}

	private Double plausibility() {
		return 1.0;
	}

	// public void doYouMean(Set<OntClass> topics) {
	// System.out.println(">>> Do you mean " + topics.stream()
	// .map(x -> x.getLocalName()).collect(Collectors.joining(" or ")));
	// }

}
