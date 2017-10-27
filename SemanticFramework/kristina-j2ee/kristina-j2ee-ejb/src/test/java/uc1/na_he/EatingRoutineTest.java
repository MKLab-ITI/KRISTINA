package uc1.na_he;

import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import base.Base;
import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.Console;
import gr.iti.kristina.topicflow.State;
import uc2.he.BreastFeedingTest;

//@Listeners({ base.CustomReporter.class })
//@Test(testName="EatingRoutineTest")
public class EatingRoutineTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc1_na_he");
	}

	@Test (priority=1, description="Can you give me some information on the eating habits of Paul?")
	public void q1() throws Exception {
		Set<String> la = Sets.newHashSet("CareRecipient", "EatingHabit");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SPECIFYINGMOREINFORMATION",
						Sets.newHashSet("Allergies", "Preferences", "Restrictions"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}

	@Test(priority=2, description="What is his favourite food?")
	public void q2() throws Exception {
		Set<String> la = Sets.newHashSet("CareRecipient", "Favourite", "Food");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("FavouriteFood"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("Recipe_fromProfile"))));
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}

	@Test(priority=3, description="No. Please let me know about his restrictions")
	public void q3() throws Exception {
		Set<String> la = Sets.newHashSet("CareRecipient", "Restriction", "No");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "STATEMENT", Sets.newHashSet("Restrictions"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}

	@Test(priority=4, description="Can he eat sweets?")
	public void q4() throws Exception {
		Set<String> la = parseInput("CareRecipient, Sweet, Eat");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTopics(out, Sets.newHashSet("CheckSweets"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}
	
	@Test(priority=5, description="Do you know what he likes to eat?")
	public void q5() throws Exception {
		Set<String> la = parseInput("CareRecipient, Like, Eat");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTopics(out, Sets.newHashSet("FavouriteFood"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("Recipe_fromProfile"))));
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}
	
	@Test(priority=6, description="Yes")
	public void q6() throws Exception {
		Set<String> la = parseInput("Yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "RECIPE", Sets.newHashSet("Recipe_fromProfile"))));
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs, state.responseModel);
	}

}
