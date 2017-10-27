package uc1.sc;

import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import base.Base;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.Console;
import gr.iti.kristina.topicflow.State;

public class SocialCompanionTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc1_sc");
	}

	@Test(priority = 1, description = "Mr. Müller lies on the floor and is not able to get up again by himself; "
			+ "Mr  Müller  slipped off his armchair")
	public void q1() throws Exception {
		Set<String> la = parseInput("Lie, Floor");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						Sets.newHashSet("InjuredOrPain"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "No, he's alright! ; No")
	public void q2() throws Exception {
		Set<String> la = parseInput("No");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT",
						Sets.newHashSet("NoPain"))));
		assertTrue(state.clarificationThemes.isEmpty());
		assertTrue(state.clarificationTopics.isEmpty());
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 3, description = "I just don't know how to lift him up.")
	public void q3() throws Exception {
		Set<String> la = parseInput("LiftUp");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						Sets.newHashSet("LiftHimUpVideo"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 4, description = "That would be great! Thank you!")
	public void q4() throws Exception {
		Set<String> la = parseInput("Yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						Sets.newHashSet("ShowVideo"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 5, description = "KRISTINA, could you play the video again? ; Could you replay the video please?")
	public void q5() throws Exception {
		Set<String> la = parseInput("Replay");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						Sets.newHashSet("ShowVideo_Replay"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 6, description = "I have to assist Mr. Müller getting out of the bed.")
	public void q6() throws Exception {
		Set<String> la = parseInput("OutOfBed, Assistance");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						Sets.newHashSet("OutOfBed_Video"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 7, description = "That would be great! I'd like to see it.")
	public void q7() throws Exception {
		Set<String> la = parseInput("Yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						Sets.newHashSet("OutOfBed_ShowVideo"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 8, description = "I heard that in 2017 there was a reform, concerning the care degrees and I lost the whole overview.")
	public void q8() throws Exception {
		Set<String> la = parseInput("Reform, Overview");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT",
						Sets.newHashSet("Overview"))));
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						parseInput("CareDegrees, FinancialSupport, Differences"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 9, description = "I've some doubts about financial suppport, when a person is in need of care.")
	public void q9() throws Exception {
		Set<String> la = parseInput("FinancialSupport");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						parseInput("ElderlyCare, HomeCare, OutpatientCare"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 10, description = "I'm interested in care at home.")
	public void q10() throws Exception {
		Set<String> la = parseInput("HomeCare");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						parseInput("HomeCare"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 11, description = "Yes, i'm going out with a friend. Could you check for events in the city tonight?")
	public void q11() throws Exception {
		Set<String> la = parseInput("LocalEvent");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						parseInput("LocalEvents"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 12, description = "It would be great to get information about care at an elderly house, since unfortunatley we need to give my mother into an elderly home")
	public void q12() throws Exception {
		Set<String> la = parseInput("ElderlyCare");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						parseInput("ElderlyCare"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 13, description = "My biggest concern is, that the reform is a disadvantage. ; I would like to know how the care degrees compare with the former levels of care.")
	public void q13() throws Exception {
		Set<String> la = parseInput("Compare");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR",
						parseInput("Differences"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 14, description = "Oh yes! I am planning to go for a hike tomorrow and I wonder how the whether gonna be.")
	public void q14() throws Exception {
		Set<String> la = parseInput("Weather, Show");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "WEATHER",
						parseInput("ShowWeather"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 15, description = "I would like to know more about what care degrees there are. ; It would be great to know what single degrees mean.")
	public void q15() throws Exception {
		Set<String> la = parseInput("CareDegrees");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("CareDegrees"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 16, description = "Yes please! Is there also Information that compare the care degrees with the former levels of care?")
	public void q16() throws Exception {
		Set<String> la = parseInput("Difference");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("Differences"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 17, description = "Is there any information about care degrees in Turkish? ; Could you show me any information about care degrees in Turkish?")
	public void q17() throws Exception {
		Set<String> la = parseInput("CareDegrees, Turkish");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		// throw new SkipException("not yet implemented");

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("CareDegreesTurkish"))));

		addLog(this.getClass(),
				Thread.currentThread().getStackTrace()[1].getMethodName(), la,
				outputs, state.responseModel);
	}

}
