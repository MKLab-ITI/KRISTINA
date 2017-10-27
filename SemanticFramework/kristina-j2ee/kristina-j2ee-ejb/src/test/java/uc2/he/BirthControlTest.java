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
public class BirthControlTest extends Base {

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

	@Test(priority = 1, description = "Could you please give me information about birth control?")
	public void q1() throws Exception {
		Set<String> la = Sets.newHashSet("Information", "BirthControl");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("BirthControlInformation"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	
	@Test(priority = 2, description = "Are there any additional sources where I can get more information about birth control?")
	public void q2() throws Exception {
		Set<String> la = parseInput("Information, BirthControl, Additional, More");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 3);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "MORE", Sets.newHashSet("BirthControlMoreInformation"))));

		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BirthControl_LeafletBarrier"))));

		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BirthControlHormonalContraceptives"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 3, description = "A friend of mine told me about the existance of emergency contraception")
	public void q3() throws Exception {
		Set<String> la = parseInput("Contraception, Emergency");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("EmergencyContraception"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 4, description = "Could you please provide more information?")
	public void q4() throws Exception {
		Set<String> la = parseInput("More, Information");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "MORE", Sets.newHashSet("BirthControlEmergencyMoreInformation"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
}
