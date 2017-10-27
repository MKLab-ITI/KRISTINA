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
//@Test(testName="PregnancyTest")
public class PregnancyTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}

	// @Test
	// public void noInput() throws Exception {
	// Set<String> la = Sets.newHashSet();
	// State state = workflow.start(injectNamespace(la), Console.name,
	// Console.scenario, Console.lang, null);
	// Optional<Output> first = state.output.stream().findFirst();
	// assertTrue(first.isPresent());
	// assertTrue(first.get().type.equals("UNKNOWN"));
	// }

	@Test(priority = 1, description = "What are the symptoms of pregnancy? ")
	public void q1() throws Exception {
		Set<String> la = Sets.newHashSet("Symptom", "Pregnancy");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("PregnancySymptoms"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("Constipation", "MoodSwings", "Nauseas", "SkinChanges", "PregnancySocialMedia",
								"Tiredness"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 2, description = "Well, I cannot sleep and I'm always tired?")
	public void q2() throws Exception {
		Set<String> la = Sets.newHashSet("Insomnia", "Tired");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("Tiredness"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("Constipation", "MoodSwings", "Nauseas", "SkinChanges"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 3, description = "What should I do?")
	public void q3() throws Exception {
		Set<String> la = Sets.newHashSet("Advice");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("AdviceOnTiredness"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 4, description = "Well, I also  feel nausea especially when I wake up")
	public void q4() throws Exception {
		Set<String> la = Sets.newHashSet("Feel", "Nausea", "WakeUp");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("Nauseas"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("Constipation", "MoodSwings", "Tiredness", "SkinChanges", "ReduceNauseas"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 5, description = "Is there something I can do to reduce nauseas?")
	public void q5() throws Exception {
		Set<String> la = Sets.newHashSet("Reduce", "Nausea");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("ReduceNauseas"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 6, description = "Should I be careful with the diet during the pregnancy?")
	public void q6() throws Exception {
		Set<String> la = Sets.newHashSet("Diet", "Pregnancy");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("PregnancyDiet"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 7, description = "I am also constipated")
	public void q7() throws Exception {
		Set<String> la = Sets.newHashSet("Constipation");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("Constipation"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("MoodSwings", "Tiredness", "SkinChanges", "Nauseas"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 8, description = "Could you give me information on the quantity of food and drinks?")
	public void q8() throws Exception {
		Set<String> la = Sets.newHashSet("Quantity", "Food", "Drink");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("Quantity"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputType(out, "TEXT")));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 9, description = "Sometimes I am nervous and then I am happy")
	public void q9() throws Exception {
		Set<String> la = Sets.newHashSet("Nervous", "Happy");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("MoodSwings"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("Constipation", "Tiredness", "SkinChanges", "Nauseas"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 10, description = "My skin also looks difference")
	public void q10() throws Exception {
		Set<String> la = Sets.newHashSet("Skin", "Different");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("SkinChanges"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE",
						Sets.newHashSet("Constipation", "MoodSwings", "SkinChanges", "Nauseas"))));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	@Test(priority = 11, description = "Is there any phone number to get official support on pregnancy?")
	public void q11() throws Exception {
		Set<String> la = Sets.newHashSet("Support", "Phone", "Pregnancy");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputType(out, "TEXT")));
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 12, description = "Any additional reliable web source I can use to get more information about pregnancy?")
	public void q12() throws Exception {
		Set<String> la = parseInput("Additional, Information, Pregnancy");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "MORE", Sets.newHashSet("MoreInfoOnPregnancy"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "SOCIAL", Sets.newHashSet("PregnancySocialMedia"))));
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

}
