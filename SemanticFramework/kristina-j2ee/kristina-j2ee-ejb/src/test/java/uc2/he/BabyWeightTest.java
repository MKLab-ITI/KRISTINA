package uc2.he;

import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import base.Base;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.Console;
import gr.iti.kristina.topicflow.State;

//@Listeners({ base.CustomReporter.class })
@Test(testName = "BabyWeightTest")
public class BabyWeightTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}

	@Test(priority = 1, description = "I'm just wondering if my baby is gaining weight as expected.")
	public void q1() throws Exception {
		Set<String> la = parseInput("Baby, Gain, Weight");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 3);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyWeightDoubts"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT",
						parseInput("BabyGainsWeight"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("WHOReferenceTables"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "Yes.")
	public void q2() throws Exception {
		Set<String> la = parseInput("Yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("WHOReferenceTables"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 3, description = "I cannot understand the table")
	public void q3() throws Exception {
		Set<String> la = parseInput("NotUnderstand");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("DoNotUnderstandTable"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 4, description = "Could you please provide further/additional reliable sources?")
	public void q4() throws Exception {
		Set<String> la = parseInput("More");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "MORE", Sets.newHashSet("FurtherInfoOnBabyWeight"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 5, description = "I have doubts concerning the baby's weight.")
	public void q5() throws Exception {
		Set<String> la = parseInput("Baby, Weight, Doubts");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 3);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyWeightDoubts"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT",
						parseInput("BabyGainsWeight"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("WHOReferenceTables"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 6, description = "Do you have any advice to feed the baby?")
	public void q6() throws Exception {
		Set<String> la = parseInput("Advice, Feed, Baby");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION", parseInput(
						"RefusesToBreastfeed, FrequencyOfBreastfeeding"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 7, description = "My baby is not gaining weight ")
	public void q7() throws Exception {
		Set<String> la = parseInput("NotGain");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", parseInput(
						"BabyNotGainsWeight"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 8, description = "Thanks for this information. Is there anything else I should know?")
	public void q8() throws Exception {
		Set<String> la = parseInput("AnythingElse");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", parseInput(
						"BabyNotGainsWeight_WebSite"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", parseInput(
						"BabyNotGainsWeight_Leaflet"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}


}
