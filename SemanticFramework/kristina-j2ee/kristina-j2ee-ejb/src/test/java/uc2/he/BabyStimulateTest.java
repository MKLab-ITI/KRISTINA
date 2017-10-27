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
@Test(testName = "BabyStimulateTest")
public class BabyStimulateTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}

	@Test(priority = 1, description = "Is it good to stimulate the baby during the first year ?")
	public void q1() throws Exception {
		Set<String> la = parseInput("Baby, Stimulate");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "KB", Sets.newHashSet("BabyStimulate"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", parseInput("BabyStimulateEyesight, BabyStimulateHearing, BabyStimulateTouch"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "Yes, can you please give me information on how to stimulate touch/hearing/eyesight?")
	public void q2() throws Exception {
		Set<String> la = parseInput("Yes, Stimulate, Touch");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BabyStimulateLeaflet"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("BabyStimulateTouch"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 3, description = "Yes, can you please give me information on how to stimulate touch/hearing/eyesight?")
	public void q3() throws Exception {
		Set<String> la = parseInput("Yes, Stimulate, Hear");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BabyStimulateLeaflet"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("BabyStimulateHearing"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 4, description = "Yes, can you please give me information on how to stimulate touch/hearing/eyesight?")
	public void q4() throws Exception {
		Set<String> la = parseInput("Yes, Stimulate, EyeSight");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BabyStimulateLeaflet"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", parseInput("BabyStimulateEyesight"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

}
