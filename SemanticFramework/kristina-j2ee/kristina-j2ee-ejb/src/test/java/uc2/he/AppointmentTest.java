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
public class AppointmentTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}


	@Test(priority = 1, description = "Should I get an appointment with a gyneacologist?")
	public void q1() throws Exception {
		Set<String> la = parseInput("Appointment, Gyneacologist");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("GyneacologistAppointment"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("NearestHealthCareCenter"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 2, description = "Why it so important to get an appointment with the Family Doctor?")
	public void q2() throws Exception {
		Set<String> la = parseInput("Appointment, FamilyDoctor, Important");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("AppointmentFamilyDoctorImportance"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 3, description = "Not sure, is there a web page or a telephone number?")
	public void q3() throws Exception {
		Set<String> la = parseInput("WebPage, PhoneNumber");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("InfoOnDoctorAppointment"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 4, description = "I found a lot of information on the web and I am worried")
	public void q4() throws Exception {
		Set<String> la = parseInput("Worried");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("Worried"))));

		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}

	
		
}
