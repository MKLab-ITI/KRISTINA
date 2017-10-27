package gr.iti.kristina.topicflow;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.ontology.OntClass;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.topicflow.services.LanguageGeneration;
import gr.iti.kristina.utils.Utils;

public class Workflow {

	private static final Logger LOG = LoggerFactory.getLogger(Workflow.class);

	static boolean HISTORY;

	ThemeKB themesKB;
	LinkedList<State> historyOfStates = new LinkedList<>();
	State currentState = null;
	ResponseGenerator g;

	boolean LG;

	public final static List<String> rejection = Arrays.asList(Namespaces.LA_ONTO + "no");
	public final static List<String> confirmation = Arrays.asList(Namespaces.LA_ONTO + "yes", Namespaces.LA_ONTO
			+ "ok");

	public Workflow(Set<String> themeModels, boolean history, boolean LG) {
		themesKB = new ThemeKB(themeModels);
		HISTORY = history;
		this.LG = LG;
		LOG.debug("Workflow service sucessfully initialised.");
	}

	public State start(Set<String> laConcepts, String name, String scenario, String lang, String userText)
			throws Exception {

		System.out.println("previous state: \n" + getLastState());
		g = new ResponseGenerator(themesKB, name, scenario, lang, userText);
		g.initialise();
		_start(laConcepts, name, scenario);
		currentState.output.addAll(g.outputs);
		// System.out.println(Utils.modelToString(g.flashModel(), "TTL"));
		currentState.responseModel = g.flashModel(currentState);
		g.p.shutdown();
		System.out.println("current state: \n" + currentState);
		if (LG) {
			LanguageGeneration lg = new LanguageGeneration(g.outputs, themesKB);
			lg.play();
		}
		return currentState;
	}

	private void _start(Set<String> laConceptsStrings, String name, String scenario) throws Exception {

		if (debugCommand(laConceptsStrings)) {
			return;
		}

		currentState = new State();

		if (getLastState().output.stream().anyMatch(o -> o.type.equals("UNKNOWN"))
				&& !isRejection(getLastState().laConcepts.stream()
						.map(t -> Utils.getLocalName(t)).collect(Collectors.toSet()))) {
			LOG.debug("*********propagating theme and topic since last turn was UNKNOWN**********");
			currentState = getLastNLinked(historyOfStates, 2).getLast().clone();
		}

		// handle classification of single resource
		if (getLastState().clarificationThemes.size() == 1) {
			OntClass _theme = getLastState().clarificationThemes.stream().findFirst().get();
			if (this.isConfirmation(laConceptsStrings)) {
				LOG.debug("Clarification theme resolved: " + Utils.getLocalName(_theme));
				currentState.theme = _theme;
				// if specifying mode information and user answers yes, then no
				// extra onto are added!!
				currentState.laConcepts.addAll(themesKB.getKeysOfTopic(_theme));
			} else if (!this.isConfirmation(laConceptsStrings)) {
				LOG.debug("Clarification rejected");
				State clone = getLastState().clone();
				clone.clarificationThemes.clear();
				clone.clarificationTopics.clear();
				clone.laConcepts.clear();
				historyOfStates.add(clone);
				// currentState.theme = getLastState().theme;
				// currentState.topic = getLastState().topic;
				// historyOfStates.add(currentState);
				// g.unknown();
				// return;
				LOG.debug("adding hiddenOnRejection LAs: " + Utils.getLocalNames(themesKB
						.getHiddenKeysOnRejectionOfTopic(_theme)));
				currentState.laConcepts.addAll(themesKB.getHiddenKeysOnRejectionOfTopic(_theme));

			}
			// copy last LA concepts
			currentState.laConcepts.addAll(getLastState().laConcepts);
		}

		if (getLastState().clarificationTopics.size() == 1) {
			OntClass first = getLastState().clarificationTopics.stream().findFirst().get();
			if (this.isConfirmation(laConceptsStrings)) {
				LOG.debug("Clarification topic resolved: " + Utils.getLocalName(first));
				currentState.topic = first;
				currentState.theme = themesKB.getThemesOfTopics(first).stream().findFirst().get();

				// if specifying mode information and user answers yes, then no
				// extra onto are added!!
				LOG.debug("adding LAs: " + Utils.getLocalNames(themesKB.getKeysOfTopic(first)));
				currentState.laConcepts.addAll(themesKB.getKeysOfTopic(first));

				LOG.debug("adding hidden LAs: " + Utils.getLocalNames(themesKB.getHiddenKeysOfTopic(first)));
				currentState.laConcepts.addAll(themesKB.getHiddenKeysOfTopic(first));
			} else if (!this.isConfirmation(laConceptsStrings)) {
				LOG.debug("Clarification rejected");
				State clone = getLastState().clone();
				clone.clarificationThemes.clear();
				clone.clarificationTopics.clear();
				clone.laConcepts.clear();
				historyOfStates.add(clone);
				// currentState.theme = getLastState().theme;
				// currentState.topic = getLastState().topic;
				// historyOfStates.add(currentState);
				// g.unknown();
				// return;
				LOG.debug("adding hiddenOnRejection LAs: " + Utils.getLocalNames(themesKB
						.getHiddenKeysOnRejectionOfTopic(first)));
				currentState.laConcepts.addAll(themesKB.getHiddenKeysOnRejectionOfTopic(first));

			}
			// copy last LA concepts
			currentState.laConcepts.addAll(getLastState().laConcepts);
		}

		// laConceptsStrings.removeIf(x -> rejection.contains(x.toLowerCase()));

		// Either the user has not answered yes/no, or we have multiple
		// clarifications
		if (isClarificationTurn() && currentState.theme == null) {
			Set<OntClass> newLAs = this.themesKB.getOntClasses(laConceptsStrings);
			Set<OntClass> newTopics = themesKB.getTopicsWithKeys(newLAs);
			Set<OntClass> interThemes = new HashSet<OntClass>(), interTopics = new HashSet<OntClass>();
			if (!getLastState().clarificationThemes.isEmpty()) {
				LOG.debug("computing LAs for multiple clarifications themes");
				interThemes.addAll(themesKB.semanticTopicIntersection(newTopics, getLastState().clarificationThemes));
				LOG.debug(Utils.getLocalNames(interThemes));
			}
			if (!getLastState().clarificationTopics.isEmpty()) {
				LOG.debug("computing LAs for multiple clarifications topics");
				interTopics.addAll(themesKB.semanticTopicIntersection(newTopics, getLastState().clarificationTopics));
				LOG.debug(Utils.getLocalNames(interTopics));
			}
			if (interTopics.isEmpty() && interThemes.isEmpty()) {
				LOG.debug("no relevant answer provided, assuming changing topics");
				LOG.debug("no LA copied from last state.");
				LOG.debug("clear clarification lists.");
				// historyOfStates.add(new State());
				State clone = getLastState().clone();
				clone.clarificationThemes.clear();
				clone.clarificationTopics.clear();
				clone.laConcepts.clear();
				historyOfStates.add(clone);
			} else {
				Set<OntClass> inter = new HashSet<OntClass>();
				if (!interTopics.isEmpty()) {
					Sets.intersection(interThemes, interTopics).copyInto(inter);
				}
				LOG.debug("adding LAs of: " + Utils.getLocalNames(inter));
				inter.stream().forEach(t -> {
					Set<OntClass> keysOfTopic = themesKB.getKeysOfTopic(t);
					currentState.laConcepts.addAll(keysOfTopic);
				});
				// copy last LA concepts
				if (getLastState().clarificationThemes.size() == 1 || getLastState().clarificationTopics.size() == 1) {
					LOG.debug("propagating previous LAs: " + Utils.getLocalNames(getLastState().laConcepts));
					currentState.laConcepts.addAll(getLastState().laConcepts);
				}
			}
		}

		addLAConceptsToCurrentState(laConceptsStrings);

		// get topics that contain some of the input la onto
		Set<OntClass> topicsWithKeys = themesKB.getTopicsWithKeys(currentState.laConcepts);

		// Remove last selected topic
		SetView<OntClass> i = Sets.intersection(Sets.newHashSet(getLastState().topic), topicsWithKeys);
		if (!i.isEmpty()) {
			LOG.debug("removing " + Utils.getLocalNames(i) + " since it has been selected in the previous turn.");
			topicsWithKeys.removeIf(t -> t.equals(getLastState().topic));
		}

		// find the relevant themes that match the topics
		HashMultimap<OntClass, OntClass> relevantThemes = HashMultimap.create();
		for (OntClass c : topicsWithKeys) {
			Set<OntClass> _themes = themesKB.getThemesOfTopics(c);
			for (OntClass abs : _themes) {
				relevantThemes.put(abs, c);
			}
		}

		// inject current theme info from clarification, if not present
		// This handles only single classification results!!!!!
		if (currentState.theme != null) {
			LOG.debug("Injecting theme: " + Utils.getLocalName(currentState.theme));
			LOG.debug("Injecting topic: " + Utils.getLocalName(currentState.topic));
			// relevantThemes.clear();
			// since it is from yes clarification,
			// ignore anything else
			relevantThemes.entries().removeIf(k -> !k.getKey().equals(currentState.theme));
			relevantThemes.put(currentState.theme, currentState.theme);
			if (currentState.topic != null)
				relevantThemes.put(currentState.theme, currentState.topic);
		}

		List<Theme> themes = new ArrayList<>();
		Set<OntClass> keys = relevantThemes.keySet();
		for (OntClass c : keys) {
			themes.add(new Theme(c, relevantThemes.get(c)));
		}

		if (themes.isEmpty()) {
			LOG.debug("No theme found");
			g.unknown(null);
			return;
		}

		LOG.debug("Unfiltered themes: " + themes);
		themes = this.filterThemes(themes);
		LOG.debug("filtered themes: " + themes);

		if (themes.isEmpty()) {
			LOG.debug("No theme found");
			g.unknown(null);
			return;
		}

		// clarify themes
		if (themes.size() > 1 /*
								 * || (!themes.isEmpty() &&
								 * !themes.get(0).isActive() &&
								 * currentState.theme == null)
								 */) {
			LOG.warn("themes > 1 -> sending theme clarification");
			Set<OntClass> _themes = themes.stream()
					.map(t -> t.theme).collect(Collectors.toSet());
			Set<OntClass> _topics = themes.stream()
					.map(t -> t.getMostGenericTopicsExcludingThemes())
					.flatMap(t -> t.stream()).collect(Collectors.toSet());
			g.clarificationResponse("theme", _themes);
			currentState.clarificationThemes.addAll(_themes);
			currentState.clarificationTopics.addAll(_topics);
			historyOfStates.add(currentState);
			return;
		}

		Theme selectedTheme = themes.get(0);

		Set<OntClass> mostGenericTopics = selectedTheme.getMostGenericTopics();
		LOG.debug("mostGenericTopics :" + Utils.getLocalNames(mostGenericTopics));

		// if (currentState.theme == null && !selectedTheme.isActive()
		// && !selectedTheme.theme.equals(getLastState().theme)) {
		// LOG.debug("currentState.theme == null&&
		// !selectedTheme.theme.equals(getLastTalkedTheme())");
		// Set<OntClass> subTopics = mostGenericTopics;
		// LOG.debug("subtopics: " + Utils.getLocalNames(subTopics));
		// if (!subTopics.isEmpty()) {
		// g.clarificationResponse("topic", subTopics);
		// }
		// currentState.clarificationTopics.addAll(subTopics);
		// historyOfStates.add(currentState);
		// return;
		// }

		for (OntClass t : mostGenericTopics) {
			// if topic discussed
			if (isDiscussed(t)) {
				LOG.debug("topic discussed: " + Utils.getLocalName(t));
				Set<OntClass> subTopics = getNonDiscussedSubTopicsWithResponses(t);
				if (subTopics.isEmpty()) {
					LOG.debug("topic does not have free subtopics, so remove it: " + Utils.getLocalName(t));
					mostGenericTopics.remove(t);
				} else {
					LOG.debug("topic have subtopics, so keep it: " + t + ", " + Utils.getLocalNames(subTopics));
				}
			}
		}

		// keep the topics in the response. I do not need to check this
		// previously, since this does not apply on clarifications, only on
		// moreSpecifyingInformation responses

		mostGenericTopics = this.keepTopicsInResponse(mostGenericTopics);

		if (mostGenericTopics.isEmpty()) {
			// System.out.println(">>> [not found] ");
			// g.notFound();
			g.unknown(null);
			currentState.theme = selectedTheme.theme;
			historyOfStates.add(currentState);
			// TODO proactive ???
		} else if (mostGenericTopics.size() > 1) {
			LOG.debug("Multiple topics found");

			// TODO try to find the most probable topic!!!

			// RESPONSE: do you want to tell you about [leafX, leafY, leafZ,
			// ...]
			g.clarificationResponse("topic", mostGenericTopics);
			currentState.clarificationTopics.addAll(mostGenericTopics);
			currentState.theme = selectedTheme.theme;
			historyOfStates.add(currentState);

		} else {
			// RESPONSE: Response for [leafX]
			LOG.debug("One theme found: " + selectedTheme);
			OntClass topic = (OntClass) mostGenericTopics.toArray()[0];

			if (themesKB.isKBOrIR(topic) && !themesKB.isAbstract(topic)) {
				LOG.debug("have response: " + Utils.getLocalName(topic));
				if (!isDiscussed(topic)) {
					// TODO check if supertopic is active?
					g.direct(topic, false);
				}

				HashSet<OntClass> proactiveTopics = new HashSet<>();

				if (StringUtils.isBlank(themesKB.getJavaConfigProperty(topic).method)) {
					// if a method exists, then the proactive responses would
					// fetch again the same info

					// proactive
					LOG.debug("check for proactive subtopics of: " + Utils.getLocalName(topic));
					Set<OntClass> subTopics = getNonDiscussedSubTopicsWithResponses(topic);
					if (subTopics.isEmpty()) {
						LOG.debug("no proactive subclasses found for: " + Utils.getLocalName(topic));
					} else {
						LOG.debug("proactive subclasses found: " + Utils.getLocalNames(subTopics));
						proactiveTopics.addAll(subTopics);
						if (proactiveTopics.size() == 1) {
							currentState.clarificationTopics.addAll(proactiveTopics);
						}
					}

				} else {
					// get only non-kb topics, e.g. URL, TEXT, etc.
					LOG.debug("check for non-kb proactive subtopics of: " + Utils.getLocalName(topic));
					Set<OntClass> subTopics = getNonDiscussedSubTopicsWithResponses(topic)
							.stream().filter(t -> !themesKB.getResponseTypes(t).contains("KB")).collect(Collectors
									.toSet());
					if (subTopics.isEmpty()) {
						LOG.debug("no proactive subclasses found for: " + Utils.getLocalName(topic));
					} else {
						LOG.debug("proactive subclasses found: " + Utils.getLocalNames(subTopics));
						proactiveTopics.addAll(subTopics);
						if (proactiveTopics.size() == 1) {
							currentState.clarificationTopics.addAll(proactiveTopics);
						}
					}
				}
				// custom proactiveness through ontology
				LOG.debug("check for custom proactive of: " + Utils.getLocalName(topic));
				proactiveTopics.addAll(themesKB.getCustomProactiveTopics(topic));
				LOG.debug("proactive topics: " + Utils.getLocalNames(themesKB.getCustomProactiveTopics(topic)));

				if (!proactiveTopics.isEmpty()) {
					g.proactiveResponse(proactiveTopics);
					if (proactiveTopics.size() == 1) {
						currentState.clarificationTopics.addAll(proactiveTopics);
					}
				}

			} else {
				LOG.debug("No direct response (KB or IR) for: " + Utils.getLocalName(topic));
				LOG.debug("Searching for specifyingMoreInformation response");
				// specifyingMoreInformation responses
				Set<OntClass> subTopics = getNonDiscussedSubTopicsWithResponses(topic);
				LOG.debug("subtopics of " + Utils.getLocalName(topic) + ": " + Utils.getLocalNames(subTopics));
				if (!subTopics.isEmpty()) {
					g.specifyingMoreInformationResponse(subTopics);
					// if (subTopics.size() == 1){
					currentState.clarificationTopics.addAll(subTopics);
					// }
				}

				//
			}

			// check for extra clarifications.
			// Important!!! Do not add again clarification that exists in the
			// last state's clarification topic list
			LOG.debug("check for extra clarifications (CLARIFICATION responseType)");
			Set<OntClass> clarifications = Sets.difference(getClarifications(topic),
					getLastState().clarificationTopics);
			if (!clarifications.isEmpty()) {
				LOG.debug("found: " + Utils.getLocalNames(clarifications));
				g.clarificationResponse("topic", clarifications);
				// if (clarifications.size() == 1) {
				currentState.clarificationTopics.addAll(clarifications);
				// }
			}

			LOG.debug("check for custom more specific of: " + Utils.getLocalName(topic));
			HashSet<OntClass> specifics = new HashSet<>();
			specifics.addAll(themesKB.getCustomMoreSpecific(topic));
			LOG.debug("specific topics: " + Utils.getLocalNames(themesKB.getCustomProactiveTopics(topic)));
			if (!specifics.isEmpty()) {
				g.specifyingMoreInformationResponse(specifics);
				currentState.clarificationTopics.addAll(specifics);
			}

			// additional responses
			HashSet<OntClass> additionalTopics = new HashSet<>();
			LOG.debug("check for additional responsesof: " + Utils.getLocalName(topic));
			additionalTopics.addAll(themesKB.getAdditionalResponseTopics(topic));
			LOG.debug("additional topics: " + Utils.getLocalNames(additionalTopics));

			if (!additionalTopics.isEmpty()) {
				for (OntClass t : additionalTopics) {
					if (g.hasKBResults(t))
						g.direct(t, true);
				}
			}

			// need to clear running context!
			currentState.topic = topic;
			currentState.theme = selectedTheme.theme;
			historyOfStates.add(currentState);
		}

		// } else {
		// // no active theme exists... continue with partial themes
		// LOG.debug("No full path exists");
		// this.filterThemes(themes);
		//
		// // need to clarify the theme
		// ResponseGenerator g = new ResponseGenerator(this.themesKB);
		// currentState.clarificationThemes.addAll(themes.stream()
		// .map(t -> t.theme).collect(Collectors.toSet()));
		// g.iCanTellYouAboutTheseThemes(themes.stream()
		// .map(t -> t.theme).collect(Collectors.toSet()));
		// historyOfStates.add(currentState);
		// }

	}

	private Set<OntClass> keepTopicsInResponse(Set<OntClass> mostGenericTopics) {
		return mostGenericTopics.stream().filter(t -> {
			try {
				return g.hasKBResults(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}).collect(Collectors.toSet());
	}

	private void addLAConceptsToCurrentState(Set<String> laConcepts) {
		currentState.laConcepts.addAll(this.themesKB.getOntClasses(laConcepts));
		LOG.debug("current state's LA Concepts: " + Utils.getLocalNames(currentState.laConcepts));
	}

	private boolean isConfirmation(Set<String> laConcepts) {
		return laConcepts.stream()
				.map(x -> x.toLowerCase())
				.anyMatch(x -> confirmation.contains(x));
	}

	private boolean isRejection(Set<String> laConcepts) {
		return laConcepts.stream()
				.map(x -> x.toLowerCase())
				.anyMatch(x -> rejection.contains(x));
	}

	// private boolean hasConfirmation(Set<String> laConcepts) {
	// return laConcepts.stream()
	// .map(x -> x.toLowerCase())
	// .anyMatch(x -> confirmation.contains(x));
	// }

	private boolean debugCommand(Set<String> laConcepts) {
		if (laConcepts.size() == 1 && laConcepts.stream()
				.map(x -> x.toLowerCase())
				.anyMatch(x -> x.endsWith("print state"))) {
			System.out.println(getLastState());
			return true;
		}
		if (laConcepts.size() == 1 && laConcepts.stream()
				.map(x -> x.toLowerCase())
				.anyMatch(x -> x.endsWith("print themes"))) {

			System.out.println(this.historyOfStates.stream()
					.filter(state -> state.theme != null)
					.map(state -> state.theme.getLocalName()).collect(Collectors.joining("\n")));

			return true;
		}

		if (laConcepts.size() == 1 && laConcepts.stream()
				.map(x -> x.toLowerCase())
				.anyMatch(x -> x.endsWith("print topics"))) {

			System.out.println(this.historyOfStates.stream()
					.filter(state -> state.topic != null)
					.map(state -> state.topic.getLocalName()).collect(Collectors.joining("\n")));

			return true;
		}

		return false;

	}

	private boolean isDiscussed(OntClass topic) {
		if (!HISTORY)
			return false;

		return this.getLastN(historyOfStates, null).stream()
				.filter(state -> state.topic != null)
				.anyMatch(state -> state.topic.equals(topic));
	}

	private Set<OntClass> getNonDiscussedSubTopicsWithResponses(OntClass topic) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Set<OntClass> collect = themesKB.getSubTopics(topic, true).stream()
				.filter(t1 -> !this.themesKB.hasResponseType(t1, "CLARIFICATION")
						&& !this.themesKB.hasResponseType(t1, "PROACTIVE")
						&& !this.themesKB.hasResponseType(t1, "EXCLUDE_FROM_SUBTOPICS"))
				.filter(t3 -> {
					try {
						return g.hasKBResults(t3);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				})
				.filter(t2 -> !isDiscussed(t2))
				.collect(Collectors.toSet());

		// LOG.debug("getNonDiscussedSubTopics size:" + collect.size());
		// LOG.debug("getNonDiscussedSubTopics", Utils.getLocalNames(collect));
		return collect;
	}

	private Set<OntClass> getClarifications(OntClass topic) {
		return themesKB.getSubTopics(topic, true).stream()
				.filter(t -> this.themesKB.hasResponseType(t, "CLARIFICATION"))
				.collect(Collectors.toSet());
	}

	private <E> Set<E> getLastN(LinkedList<E> list, Integer n) {
		if (n == null) {
			return Sets.newHashSet(list);
		}

		return Sets.newHashSet(list.subList(Math.max(list.size() - n, 0), list.size()));
	}

	private <E> LinkedList<E> getLastNLinked(LinkedList<E> list, Integer n) {
		if (n == null) {
			return new LinkedList<E>();
		}

		return new LinkedList<E>(list.subList(Math.max(list.size() - n, 0), list.size()));
	}

	private State getLastState() {
		if (this.historyOfStates.isEmpty()) {
			return new State();
		}
		return this.historyOfStates.getLast();
	}

	private OntClass getLastTalkedThemeRecursive() {
		Iterator<State> descendingIterator = historyOfStates.descendingIterator();
		while (descendingIterator.hasNext()) {
			State state = descendingIterator.next();
			if (state.theme != null) {
				return state.theme;
			}
		}
		return null;
	}

	private OntClass getLastTalkedTopicRecursive() {
		Iterator<State> descendingIterator = historyOfStates.descendingIterator();
		while (descendingIterator.hasNext()) {
			State state = descendingIterator.next();
			if (state.topic != null) {
				return state.topic;
			}
		}
		return null;
	}

	private List<Theme> filterThemes(List<Theme> themes) {
		// // keep only active ones
		// // get first the active themes [their keys satisfied -> high
		// probability
		// // to talk about this theme]
		// List<Theme> activeThemes = themes.stream()
		// .filter(x -> x.isActive())
		// .collect(Collectors.toList());
		//
		// // if active themes exists
		// if (!activeThemes.isEmpty()) {
		// LOG.debug("active themes found: " + activeThemes);
		// themes = activeThemes;
		// }

		// TODO I do not check here anything about the currentState.theme??

		if (isClarificationTurn()) {
			HashSet<OntClass> clarificationThemes = getLastState().clarificationThemes;
			HashSet<OntClass> clarificationTopics = getLastState().clarificationTopics;

			// need to promote themes that have relation with clarification
			// topics
			if (!clarificationThemes.isEmpty()) {
				LOG.debug("promote themes relevant to clarification themes: " + Utils.getLocalNames(
						clarificationThemes));
				themes = themes.stream()
						.filter(t -> clarificationThemes.contains(t.theme) || t.isActive())
						// restrictions or
						// allergies and user
						// said Breastfeeding
						// (active!). We should
						// not remove the active
						// from here
						.collect(Collectors.toList());
			}
			if (!clarificationTopics.isEmpty()) {
				LOG.debug("promote themes relevant to clarification topics: " + Utils.getLocalNames(
						clarificationTopics));
				themes = themes.stream()
						.filter(t -> t.isActive() || !Sets.intersection(clarificationTopics, t.topics).isEmpty())
						.collect(Collectors.toList());
			}
			LOG.debug("themes after clarification filtering: " + themes);
		}
		LOG.debug(themes.toString());

		// first rank topics and take into account partial themes. If themes are
		// checked first,
		// partial themes are removed (test: Breastfeeding, q10)

		// rank topics and keep the highest
		LOG.debug("LA concepts:" + Utils.getLocalNames(currentState.laConcepts));
		LOG.debug("rank topics and keep the highest");
		HashMap<OntClass, Integer> _scores2 = new HashMap<>();
		themes.stream().forEach(x -> {
			x.topics.forEach(topic -> {
				int number = themesKB.getMatchedKeys(topic, currentState.laConcepts);
				_scores2.put(topic, number);
			});
		});
		LOG.debug("scores: ");
		Utils.printMapJena(_scores2);
		TreeSet<Integer> scores2 = Sets.newTreeSet(_scores2.values());
		// LOG.debug("scores:" + scores2);
		int max2 = scores2.last();
		// LOG.debug("max score: " + max2);
		themes.stream().forEach(t -> {
			t.topics.removeIf(topic -> /* !themesKB.isTheme(topic) && */ _scores2.get(topic) != max2);
		});

		LOG.debug(themes.toString());

		LOG.debug("keeping only the most generic topics");
		for (Theme t : themes) {
			t.topics = t.getMostGenericTopics();
		}

		LOG.debug(themes.toString());

		// themes with no topics
		LOG.debug("removing themes with no topics: " + themes.stream()
				.filter(t -> t.topics.isEmpty())
				.peek(t2 -> LOG.debug("removing: " + t2)).collect(Collectors.toSet()));
		themes.removeIf(t -> t.topics.isEmpty());

		// rank themes and keep the highest
		LOG.debug("rank themes and keep the highest");
		HashMap<OntClass, Integer> _scores1 = new HashMap<>();
		themes.stream().forEach(t -> {
			int number = themesKB.getMatchedKeys(t.theme, currentState.laConcepts);
			_scores1.put(t.theme, number);
		});
		LOG.debug("scores: ");
		Utils.printMapJena(_scores1);
		TreeSet<Integer> scores1 = Sets.newTreeSet(_scores1.values());
		// LOG.debug("scores:" + scores1);
		int max1 = scores1.last();
		// LOG.debug("max score: " + max1);
		themes.removeIf(theme -> _scores1.get(theme.theme) != max1);

		LOG.debug(themes.toString());

		// themes with no topics
		LOG.debug("removing themes with no topics: " + themes.stream()
				.filter(t -> t.topics.isEmpty())
				.peek(t2 -> LOG.debug("removing: " + t2)).collect(Collectors.toSet()));
		themes.removeIf(t -> t.topics.isEmpty());

		// active || contained in last themes / contained in last topic
		OntClass lastTalkedTheme = getLastTalkedThemeRecursive();
		LOG.debug("last talked theme: " + Utils.getLocalName(lastTalkedTheme));
		boolean sameAsLastTheme = themes.stream().anyMatch(t -> t.theme.equals(lastTalkedTheme));
		if (sameAsLastTheme && lastTalkedTheme != null) {
			LOG.debug("same theme as previous one: " + Utils.getLocalName(lastTalkedTheme));
			themes = themes.stream()
					.filter(t -> t.theme.equals(lastTalkedTheme) || t.isActive())
					.collect(Collectors.toList());
		}

		// first rank then remove topics: otherwise when we are in a certain
		// branch, we cannot go out. For example from Nausea to Pregnancy diet,
		// when Pregnancy is provided as input. Always Nausea is promoted, if
		// last talked topic
		OntClass lastTalkedTopic = getLastTalkedTopicRecursive();
		LOG.debug("last talked topic: " + Utils.getLocalName(lastTalkedTopic));
		Set<OntClass> sameAsLastTopic = themes.stream().map(t -> themesKB.semanticTopicIntersection(t.topics, Sets
				.newHashSet(lastTalkedTopic))).flatMap(t -> t.stream()).collect(Collectors.toSet());
		if (!sameAsLastTopic.isEmpty() && lastTalkedTopic != null) {
			LOG.debug("same/sub topic: " + Utils.getLocalNames(sameAsLastTopic) + " as previous one: " + Utils
					.getLocalName(lastTalkedTopic));
			themes.stream().forEach(t -> {
				t.topics.removeIf(topic -> !sameAsLastTopic.contains(topic));
			});
			// themes = themes.stream().filter(t ->
			// t.isActive()).collect(Collectors.toList());
		}
		// themes.stream().forEach(t -> {
		// boolean r = t.topics.removeIf(topic ->
		// getLastState().clarificationTopics.contains(topic)
		// && themesKB.getResponseTypes(topic).contains("CLARIFICATION"));
		// if (r)
		// LOG.debug(
		// "topic removed since it is a custom clarification and belongs to the
		// last state clarification list ");
		// });

		return themes;
	}

	// public Set<OntClass> getMostSpecificTopics(Set<OntClass> topics) {
	// Set<OntClass> toRemove = new HashSet<>();
	// for (OntClass c1 : topics) {
	// for (OntClass c2 : topics) {
	// if (c1.equals(c2)) {
	// continue;
	// }
	// if (c1.hasSuperClass(c2, false)) {
	// toRemove.add(c2);
	// }
	// }
	// }
	// topics.removeAll(toRemove);
	// return topics;
	// }

	private boolean isClarificationTurn() {
		return !getLastState().clarificationTopics.isEmpty()
				|| !getLastState().clarificationThemes.isEmpty();
	}

}
