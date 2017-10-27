package uc1.na_he;

import static org.testng.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import base.Base;
import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.Console;
import gr.iti.kristina.topicflow.State;

//@Listeners({ base.CustomReporter.class })
//@Test(testName="SleepingRoutineTest")
public class SleepingRoutineTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc1_na_he");
	}

	@Test(priority = 1, description = "Does Hans have any sleeping problems?")
	public void q1() throws Exception {
		Set<String> la = parseInput("CareRecipient, Sleep, Problem");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "CLARIFICATION", Sets.newHashSet(
						"Clarification_SleepProblems"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						Sets.newHashSet("FallAsleepProblem", "NightProblems"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "Does he have problems falling asleep?")
	public void q2() throws Exception {
		Set<String> la = Sets.newHashSet("CareRecipient", "FallsAsleep", "Problem");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("FallAsleepProblem"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 3, description = "And does he have any problems during the night?")
	public void q3() throws Exception {
		Set<String> la = Sets.newHashSet("CareRecipient", "Problem", "Night");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("NightProblems"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 4, description = "And how long does he sleep a night?")
	public void q4() throws Exception {
		Set<String> la = parseInput("CareRecipient, Sleep, Duration");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 3);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("SleepDuration"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("WakeUpTime"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "ADDITIONAL", Sets.newHashSet("SleepTime"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 5, description = "And how often does he wake up at night?")
	public void q5() throws Exception {
		Set<String> la = parseInput("CareRecipient, Night, WakeUp, Frequency");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("WakeUpFrequency"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "ADDITIONAL", Sets.newHashSet("WakeUpTime"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 6, description = "Is there anything else important that I should know about his sleep")
	public void q6() throws RepositoryException, RepositoryConfigException,
			UnsupportedEncodingException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, UsernameException {

		throw new SkipException("not yet implemented");
	}

	@Test(priority = 7, description = "What can I do to help him falling asleep?", alwaysRun = true)
	public void q7() throws Exception {
		Set<String> la = parseInput("CareRecipient, FallsAsleep, Help");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("HelpFallingAsleep"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
}
