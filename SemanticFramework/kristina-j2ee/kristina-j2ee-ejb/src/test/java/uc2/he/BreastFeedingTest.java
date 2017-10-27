package uc2.he;

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

//@Listeners({ base.CustomReporter.class })
@Test(testName = "BreastFeedingTest")
public class BreastFeedingTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}

	@Test(priority = 1, description = "I'm wondering if you can provide information about breastfeeding")
	public void q1() throws Exception {
		Set<String> la = parseInput("Information, Breastfeeding");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 4);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("InformationOnBreastfeeding"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						parseInput(
								"TweetsForBreastfeeding, FrequencyOfBreastfeeding, UserGroupsForBreastfeeding, DurationOfBreastfeeding, BenefitsOfBreastfeeding"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						parseInput("Information_Clarification"))));
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL",
						parseInput("InformationOnBreastfeeding_URL"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "I would like to know about the benefits of breastfeeding")
	public void q2() throws Exception {
		Set<String> la = parseInput("Benefits, Breastfeeding");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("BenefitsOfBreastfeeding"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						parseInput("MoreBenefits_Clarification"))));
	}

	@Test(priority = 3, description = "Yes, please")
	public void q3() throws Exception {
		Set<String> la = parseInput("yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "MORE", Sets.newHashSet("MoreOnBenefitsOfBreastfeeding"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("MoreBenefits_AEP_Text"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 4, description = "Tell me something more about the benefits of breastfeeding")
	public void q4() throws Exception {
		// please see the spreadsheet, this is redundant since the same topic is
		// selected MoreOnBenefits, which is filtered out
		Set<String> la = parseInput("Benefits, More, Breastfeeding");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("BenefitsOfBreastfeeding"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						parseInput("MoreBenefits_Clarification"))));
	}

	@Test(priority = 5, description = "Could you please show reliable posts from social media?")
	public void q5() throws Exception {
		Set<String> la = parseInput("Post, SocialMedia");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		throw new SkipException("Need to implement acount filtering in social media service");

		// Set<Output> outputs = state.output;
		// assertTrue(outputs.size() == 1);
		//
		// assertTrue(outputs.stream()
		// .anyMatch(out -> outputTypeTopics(out, "SOCIAL",
		// Sets.newHashSet("TweetsForBreastfeeding"))));
		// addLog(this.getClass(),
		// Thread.currentThread().getStackTrace()[1].getMethodName(), la,
		// outputs,
		// state.responseModel);
	}

	@Test(priority = 6, description = "I guess breastfeeding aslo create a bond between mother and child... right?")
	public void q6() throws Exception {
		Set<String> la = parseInput("Bond, Mother, Child");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BondMotherChild"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 7, description = "Can you please tell me something about the frequency?")
	public void q7() throws Exception {
		Set<String> la = parseInput("Frequency");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("FrequencyOfBreastfeeding"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 8, description = "How long should I breastfeed my baby? ")
	public void q8() throws Exception {
		Set<String> la = parseInput("Duration, Breastfeeding");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("DurationOfBreastfeeding"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 9, description = "My baby usually fall asleep when I breastfeed")
	public void q9() throws Exception {
		Set<String> la = parseInput("FallsAsleep, Baby, Breastfeeding");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("FallsAsleepDuringBreastfeeding"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 10, description = "What should I do if the baby refuses to be suckled?")
	public void q10() throws Exception {
		Set<String> la = parseInput("Baby, Refuses");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("RefusesToBreastfeed"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 11, description = "Ok. And what happens if I cannot breastfeed my baby?")
	public void q11() throws Exception {
		throw new SkipException("How to determine alternatives?");

	}

	@Test(priority = 12, description = "I'd prefer if you could show me how to get an appointment with my doctor.")
	public void q12() throws Exception {
		Set<String> la = parseInput("Appointment, Doctor");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("InfoOnDoctorAppointment"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
}
