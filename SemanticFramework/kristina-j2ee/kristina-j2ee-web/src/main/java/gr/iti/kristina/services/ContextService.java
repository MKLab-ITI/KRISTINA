package gr.iti.kristina.services;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import gr.iti.kristina.admin.AdminBean;
import gr.iti.kristina.context.CurrentContextBean;
import gr.iti.kristina.context.NonVerbalContextBean;
import gr.iti.kristina.context.TopicUnderstandingBean;
import gr.iti.kristina.errors.ErrorHandling;
import gr.iti.kristina.model.ContextCluster;
import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.parsers.LAParser;
import gr.iti.kristina.qa.QuestionAnswerBean;
import gr.iti.kristina.qa.QuestionAnswerBean2;
import gr.iti.kristina.repository.TopicsNamespacesRepository;
import gr.iti.kristina.responses.AdditionalResponseGenerator;
import gr.iti.kristina.responses.StatementResponseGenerator;
import gr.iti.kristina.responses.UnknownResponseGenerator;
import gr.iti.kristina.responses.WeatherResponseGenerator;
import gr.iti.kristina.startup.DataSourceBean;
import gr.iti.kristina.startup.HierarchyBean;
import gr.iti.kristina.startup.SituationsBean;
import gr.iti.kristina.startup.SituationsBean.Result;
import gr.iti.kristina.utils.Utils;

@RequestScoped
@Path("context")
public class ContextService {

	@EJB
	CurrentContextBean currentContextBean;

	@EJB
	NonVerbalContextBean nonVerbalContextBean;

	@EJB
	TopicUnderstandingBean topicBean;

	@EJB
	LAParser laParserBean;

	@EJB
	QuestionAnswerBean qaBean;

	@EJB
	QuestionAnswerBean2 qaBean2;

	@EJB
	HierarchyBean hierarchy;

	@EJB
	HierarchyBean hb;

	private Response successResponse(String data) {
		return Response.ok() // 200
				.entity(data).build();
	}

	private Response errorResponse(String data) {
		return Response.serverError() // 200
				.entity(data).build();
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response getText(@FormParam("emotions") String emotions, @FormParam("frames") String frames,
			@FormParam("username") String username, @FormParam("scenario") String scenario,
			@FormParam("file") String file) {
		return QA(emotions, frames, username, scenario, file, true);

	}

	private Response QA(String emotions, String frames, String username, String scenario, String file, boolean update) {
		System.out.println("frames:" + frames);
		System.out.println("emotions:" + emotions);
		// System.out.println("username:" + username);
		System.out.println("scenario:" + scenario);
		System.out.println("file:" + file);

		try {
			nonVerbalContextBean.update(emotions);
		} catch (Exception e) {
			return errorResponse(ErrorHandling.error("Non verbal management", e));
		}

		if (Strings.isNullOrEmpty(frames)) {
			// throw new UnexpectedException("empty frames");
		}
		if (Strings.isNullOrEmpty(scenario)) {
			return errorResponse(ErrorHandling.error("Scenario cannot be empty", null));
		}

		try {
			AdminBean.setScenario(scenario);
		} catch (Exception e) {
			return errorResponse(ErrorHandling.error("error setting scenario", e));
		}

		Stopwatch timer = Stopwatch.createStarted();

		String la_decode;
		try {
			la_decode = java.net.URLDecoder.decode(frames, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return errorResponse(ErrorHandling.error("Error decoding LA", e));
		}

		System.out.println("decoded frames: " + la_decode);
		// System.out.println("SERVER:: " + la_decode);

		// TODO: may need to collect more resources in addition to classified
		// elements...
		// HashSet<KeyEntityWrapper> keyEntityResources =
		// laParserBean.extractKeyEntityTypes(la_decode);
		LAParser.LAParserResult laResult;
		HashMap<OntResource, Collection<Resource>> topics = new HashMap<>();
		try {
			laResult = laParserBean.extractKeyEntityTypes(la_decode);
			System.out.println("LA Parser result:");
			laResult.print();
			System.out.println("extracted entities: " + laResult.getDomainKeyEntities());

			System.out.println("Recognizing topic...");
			OntModel topicModel = topicBean.loadExtractedEntityResources(laResult.getDomainKeyEntities());
			topics = topicBean.getTopics(topicModel);

			System.out.println(topics.toString());
		} catch (Exception e) {
			return errorResponse(ErrorHandling.error("Error parsing LA", e));
		}

		// the model that holds the responses
		OntModel responseModel = Utils.createDefaultModel(false);

		/**
		 * need here to handle the case where a topic instance belongs to more
		 * than one topics... because we need somehow to end up with a single
		 * topic
		 * 
		 */

		// update current context
		Collection<Resource> topicSet = new HashSet<Resource>();
		OntResource topicInstance = null;
		Iterator<Entry<OntResource, Collection<Resource>>> iterator = topics.entrySet().iterator();
		TopicsNamespacesRepository r = new TopicsNamespacesRepository();
		if (iterator.hasNext()) {
			Entry<OntResource, Collection<Resource>> topic_entry = iterator.next();
			topicSet = topic_entry.getValue();
			topicInstance = topic_entry.getKey();
			System.out.println("topics detected: " + topicSet);
			for (Resource t : topicSet) {
				try {
					System.out.println("Namespaces:::: " + r.getDirectNamespaces(t));
				} catch (MalformedQueryException | RepositoryException | QueryEvaluationException e) {
					return errorResponse(ErrorHandling.error("Error extracting namespaces", e));
				}
			}
		}
		r.shutDown("Namespaces:::");

		Set<String> datasources = DataSourceBean.getDataSources_str(topicSet.stream().findFirst());
		System.out.println("Datasources:::: " + datasources);

		/*
		 * Logic starts here
		 */

		try {

			// newspaper
			if (laResult.speechActType.toString().equals(Namespaces.LA_DIALOGUE + "RequestNewspaper")) {
				InitialContext jndi = new InitialContext();
				NewspaperBean newspaperService = (NewspaperBean) jndi
						.lookup("java:app/kristina-j2ee-ejb/NewspaperBean");
				String content = newspaperService.start(laResult.userText, null);

				// need to add the topic if it has not been detected
				Resource newspaperResource = ResourceFactory.createResource(Namespaces.KB_CONTEXT + "NewspaperContext");
				if (!topicSet.contains(newspaperResource)) {
					topicSet.add(newspaperResource);
				}

				currentContextBean.update(topicInstance, topicSet, la_decode, laResult.speechActType);
				StatementResponseGenerator g = new StatementResponseGenerator(currentContextBean,
						new ArrayList<Result>(), content, "free_text");
				responseModel.add(g.generate());

				// weather
			} else if (laResult.speechActType.toString().equals(Namespaces.LA_DIALOGUE + "RequestWeather")) {
				System.out.println("Weather context has been detected");

				InitialContext jndi = new InitialContext();
				WeatherServiceBean weatherService = (WeatherServiceBean) jndi
						.lookup("java:app/kristina-j2ee-ejb/WeatherServiceBean");
				LinkedHashMultimap<String, String> weatherData = weatherService.start();

				// need to add the topic if it has not been detected
				Resource weatherResource = ResourceFactory.createResource(Namespaces.KB_CONTEXT + "WeatherContext");
				if (!topicSet.contains(weatherResource)) {
					topicSet.add(weatherResource);
				}

				currentContextBean.update(topicInstance, topicSet, la_decode, laResult.speechActType);

				WeatherResponseGenerator g = new WeatherResponseGenerator(currentContextBean, weatherData);
				responseModel.add(g.generate());
			} else if (AdminBean.getScenario().equals("pain") || AdminBean.getScenario().equals("babycare")) {
				System.out.println("Call IR module..." + AdminBean.getScenario());
				System.out.println("userText: " + laResult.userText);

				InitialContext jndi = new InitialContext();
				PassageRetrievalBean PassageRetrievalBean = (PassageRetrievalBean) jndi
						.lookup("java:app/kristina-j2ee-ejb/PassageRetrievalBean");

				String content = PassageRetrievalBean.start(laResult.userText, null).getContent();
				currentContextBean.update(topicInstance, topicSet, la_decode, laResult.speechActType);
				if (content != null) {
					StatementResponseGenerator g = new StatementResponseGenerator(currentContextBean,
							new ArrayList<Result>(), content, "free_text");
					responseModel.add(g.generate());
				} else {
					UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean, "structured");
					responseModel.add(g.generate());
				}
			}

			else {// no special case detected, process with generic logic...
				currentContextBean.update(topicInstance, topicSet, la_decode, laResult.speechActType);
				if (topicSet.isEmpty()) {
					System.err.println("no topic detected!!");
//					AdditionalResponseGenerator g = new AdditionalResponseGenerator(currentContextBean, "structured");
//					responseModel.add(g.generate());
					
					UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean, "structured");
					responseModel.add(g.generate());
					
					// System.out.println("Call IR module...");
					// System.out.println("userText: " + laResult.userText);
					//
					// InitialContext jndi = new InitialContext();
					// PassageRetrievalBean PassageRetrievalBean =
					// (PassageRetrievalBean) jndi
					// .lookup("java:app/kristina-j2ee-ejb/PassageRetrievalBean");
					//
					// String content =
					// PassageRetrievalBean.start(laResult.userText);
					// if (content != null) {
					// StatementResponseGenerator g = new
					// StatementResponseGenerator(currentContextBean,
					// new ArrayList<Result>(), content, "free_text");
					// responseModel = g.generate();
					// } else {
					// UnknownResponseGenerator g = new
					// UnknownResponseGenerator(currentContextBean,
					// "structured");
					// responseModel.add(g.generate());
					// }
				} else {
					// Special case: Newspaper reading -> just return the topic
					if (topicSet.contains(ResourceFactory.createResource(Namespaces.KB_CONTEXT + "NewspaperContext"))) {
						System.out.println("newpaper context has been detected without dialogue");
						UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean, "structured");
						responseModel.add(g.generate());
					}
					// Special case: weather -> just return the topic
					else if (topicSet
							.contains(ResourceFactory.createResource(Namespaces.KB_CONTEXT + "WeatherContext"))) {
						System.out.println("Weather context has been detected without dialogue");
						UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean, "structured");
						responseModel.add(g.generate());
					} else { // call QA
						if (datasources.contains(Namespaces.KB_CONTEXT + "kb")) {
							if (update) {
								List<SituationsBean.Result> result = new ArrayList<>();
								result.addAll(qaBean2.start(laResult.getDomainKeyEntities(), topicSet, hb));

								if (result.isEmpty()) {
									UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean,
											"structured");
									responseModel.add(g.generate());
								} else {
									if (!Strings.isNullOrEmpty(file)) {
										File f = new File("@test/" + file);
										System.out.println(f);
										FileUtils.writeStringToFile(f, result.toString());
									}
									System.out.println(result);
									StatementResponseGenerator g = new StatementResponseGenerator(currentContextBean,
											result, null, "structured");
									responseModel.add(g.generate());
								}
							} else {
								TreeSet<ContextCluster> result = new TreeSet<>();
								result.addAll(qaBean.start(laResult.getDomainKeyEntities(), topicSet, hb));

								if (result.isEmpty()) {
									UnknownResponseGenerator g = new UnknownResponseGenerator(currentContextBean,
											"structured");
									responseModel.add(g.generate());
								} else {
									ContextCluster last = result.last();
									// System.out.println("filtered: " + last);
									double score = last.getScore();
									Collection<ContextCluster> tailSet = Collections2.filter(result,
											new Predicate<ContextCluster>() {

												@Override
												public boolean apply(ContextCluster input) {
													return input.getScore() == score;
												}
											});

									// handle equal scores
									Ordering<ContextCluster> o = new Ordering<ContextCluster>() {
										@Override
										public int compare(ContextCluster left, ContextCluster right) {
											return Doubles.compare(left.getRank(), right.getRank());
										}
									};

									double min = o.min(tailSet).getRank();
									double max = o.max(tailSet).getRank();
									System.err.println("min: " + min + ", max: " + max);
									double a = 0.7, b = 1.0;

									for (ContextCluster cc : tailSet) {
										if (max == min) {
											cc.setRank(1.0);
										} else
											cc.setRank(a + ((cc.getRank() - min) * (b - a) / (max - min)));
									}
									System.out.println("normalised: " + Utils.flattenCollection(tailSet));
									StatementResponseGenerator g = new StatementResponseGenerator(currentContextBean,
											new TreeSet<ContextCluster>(tailSet), null, "structured");
									responseModel.add(g.generate());
								}
							}

						} else {
							System.err.println("Cannot provide an answer...");
							AdditionalResponseGenerator g = new AdditionalResponseGenerator(currentContextBean,
									"structured");
							responseModel.add(g.generate());

							// the following has been commented in order to
							// prevent calling IR in other
							// scenarios than babycare and pain.

							// call IR
							// System.out.println("Call IR module...");
							// System.out.println("userText: " +
							// laResult.userText);
							//
							// InitialContext jndi = new InitialContext();
							// PassageRetrievalBean PassageRetrievalBean =
							// (PassageRetrievalBean) jndi
							// .lookup("java:app/kristina-j2ee-ejb/PassageRetrievalBean");
							//
							// String content =
							// PassageRetrievalBean.start(laResult.userText);
							// if (content != null) {
							// StatementResponseGenerator g = new
							// StatementResponseGenerator(currentContextBean,
							// new ArrayList<Result>(), content, "free_text");
							// responseModel.add(g.generate());
							// } else {
							// UnknownResponseGenerator g = new
							// UnknownResponseGenerator(currentContextBean,
							// "structured");
							// responseModel.add(g.generate());
							// }
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return errorResponse(ErrorHandling.error("Error in KI task", e));
		}
		currentContextBean.getLastContext().setResponseModel(responseModel);

		timer.stop();
		System.out.println("TIMER:::" + timer);
		return successResponse(Utils.modelToString(responseModel, "TTL"));
	}

	@POST
	@Path("/v2/update")
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response getText2(@FormParam("emotions") String emotions, @FormParam("frames") String frames,
			@FormParam("username") String username, @FormParam("scenario") String scenario,
			@FormParam("file") String file) {

		return QA(emotions, frames, username, scenario, file, true);
	}

	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "ok";
	}

	@POST
	@Path("/user")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public boolean setUser(@FormParam("user") String name) {
		String[] names = { "eugene", "maria", "george", "anna", "elisabeth" };
		name = name.toLowerCase();
		if (!Arrays.asList(names).contains(name)) {
			return false;
		}

		File f = new File("kristina-user.txt");
		try {
			f.createNewFile();
			FileUtils.writeStringToFile(f, name, "UTF-8");
			currentContextBean.clear();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	@GET
	@Path("/terms")
	@Produces(MediaType.TEXT_PLAIN)
	public Set<String> terms() {
		HashSet<String> list = new HashSet<>();
		OntModel m = topicBean.loadExtractedEntityResources(null);
		String q = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "Select ?c where { \r\n"
				+ " {?c rdfs:subClassOf <http://kristina-project.eu/ontologies/la/onto#DomainEntity> .} UNION\r\n"
				+ " {?c rdfs:subClassOf <http://kristina-project.eu/ontologies/context-light#GenericContext> .}}";

		Query query = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource r = soln.getResource("c");
			list.add(r.getLocalName());
		}
		qexec.close();

		return list;
	}
}
