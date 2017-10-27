package base;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.Console;
import gr.iti.kristina.topicflow.Workflow;
import gr.iti.kristina.utils.Utils;

public abstract class Base {

	public Workflow workflow = null;

	public abstract void initialize();

	public String scenario;

	public boolean LG = false;

	public static LinkedList<TestResult> testResults = new LinkedList<>();

	public void addLog(Class<?> c, String methodName, Set<String> la, Set<Output> outputs, Model responseModel) {
		List<Method> methods = Arrays.asList(c.getDeclaredMethods());
		Optional<Method> method = methods.stream().filter(m -> m.getName().equals(methodName)).findFirst();
		if (method.isPresent()) {
			Test annotation = method.get().getDeclaredAnnotation(Test.class);
			TestResult t = new TestResult();
			t.methodName = methodName;
			t.description = annotation.description();
			t.las = la;
			t.outputs = outputs;
			t.scenario = scenario;
			t.responseModel = responseModel;
			this.testResults.addLast(t);
			t.save();
		}
		System.out.println("------------------------------");
		System.out.println(methodName + " finished");
		System.out.println("------------------------------");
	}

	@BeforeMethod
	public void beforeTest() {
		// System.out.println("Executing test: " +
		// Arrays.toString(Thread.currentThread().getStackTrace()));
	}

	@AfterSuite
	public void printResults() {
		System.out.println("Print results");
		for (TestResult testResult : testResults) {
			System.out.println(testResult + " \n ");
		}
	}

	public void initialize(String ontologyName) {
		Set<String> themeModels = Console.setUp(ontologyName);
		this.scenario = ontologyName;

		assertNotNull(themeModels);
		assertTrue(themeModels.size() > 0);

		workflow = new Workflow(themeModels, false, LG);
		assertNotNull(workflow);
	}

	public Set<String> injectNamespace(Set<String> classes) {
		return Sets.newHashSet(classes.stream()
				.map(x -> Namespaces.LA_ONTO + x.trim()).collect(Collectors.toSet()));
	}

	public Set<String> parseInput(String input) {
		return Arrays.asList(input.split(",")).stream()
				.map(x -> x.trim()).collect(Collectors.toSet());
	}

	public Set<String> convertToString(Set<OntClass> classes) {
		return classes.stream().map(c -> Utils.getLocalName(c)).collect(Collectors.toSet());
	}

	public boolean matchSets(Set<String> classes1, Set<OntClass> classes2) {
		Set<String> set = convertToString(classes2);
		return !Sets.intersection(classes1, set).isEmpty() && classes2.size() == classes1.size();
	}

	public boolean outputType(Output out, String type) {
		return out.type.equals(type);
	}

	public boolean outputTopics(Output out, Set<String> topics) {
		return matchSets(topics, out.topics);
	}

	public boolean outputTypeTopics(Output out, String type, Set<String> topics) {
		return outputType(out, type) && outputTopics(out, topics);
	}

}
