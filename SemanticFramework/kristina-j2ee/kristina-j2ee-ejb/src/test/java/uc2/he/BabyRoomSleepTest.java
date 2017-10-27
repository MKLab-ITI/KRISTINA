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
@Test(testName = "BabyRoomSleepTest")
public class BabyRoomSleepTest extends Base {

	@BeforeClass
	public void initialize() {
		super.initialize("uc2_he");
	}
	
	@Test(priority = 1, description = "Could you please give me generic information about baby care?")
	public void q1() throws Exception {
		Set<String> la = parseInput("Newborn, Information, BabyCare");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BabyCare"))));

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyRoomSleepFaceUp"))));
		
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	
	

	@Test(priority = 2, description = "I see that it is forbidden to smoke in the baby's room")
	public void q2() throws Exception {
		Set<String> la = parseInput("Baby, Smoke, Room");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyRoomSmoke"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 3, description = "I see, but my housband smokes")
	public void q3() throws Exception {
		Set<String> la = parseInput("Housband, Smoke");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyRoomSmokeHusband"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 4, description = "Oh yes and the babys shall also sleep without a pillow")
	public void q4() throws Exception {
		Set<String> la = parseInput("Baby, Sleep, Pillow");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyRoomSleepPillow"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 5, description = "Thanks KRISTINA. Tell me something about the baby's bath")
	public void q5() throws Exception {
		Set<String> la = parseInput("Baby, Bath");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("BabyCareBath"))));
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "PROACTIVE", Sets.newHashSet("BabyCareBath_Video"))));

		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 6, description = "Yes")
	public void q6() throws Exception {
		Set<String> la = parseInput("Yes");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 2);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "URL", Sets.newHashSet("BabyCareBath_VideoPlay"))));		
		
		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "TEXT", Sets.newHashSet("BabyCareBath_Video_37"))));
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	@Test(priority = 7, description = "Ok and how should I check the temperature?")
	public void q7() throws Exception {
		Set<String> la = parseInput("Check, Temperature");
		State state = workflow.start(injectNamespace(la), Console.name, Console.scenario, Console.lang, null);

		Set<Output> outputs = state.output;
		assertTrue(outputs.size() == 1);

		assertTrue(outputs.stream()
				.anyMatch(out -> outputTypeTopics(out, "IR", Sets.newHashSet("BabyCareBath_CheckTemprature"))));		
		
		addLog(this.getClass(), Thread.currentThread().getStackTrace()[1].getMethodName(), la, outputs,
				state.responseModel);
	}
	
	

	
}
