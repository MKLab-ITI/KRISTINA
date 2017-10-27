package gr.iti.kristina.model.fusion.scenarios;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.kie.api.runtime.rule.EntryPoint;

import gr.iti.kristina.model.fusion.NonVerbalItem;
import gr.iti.kristina.model.fusion.VerbalItem;
import gr.iti.kristina.services.FusionServiceBean;

public class ScenarioHurtsHere {

	public static void main(String[] args) throws InterruptedException {
		FusionServiceBean f = new FusionServiceBean();
		f.initialisation();
		new Thread(new Runnable() {
			public void run() {
				f.getkSession().fireUntilHalt();
			}
		}).start();

		//SessionPseudoClock clock = (SessionPseudoClock) f.getClock();

		HashSet<String> keyConcepts = new HashSet<>();
		keyConcepts.addAll(Arrays.asList("Hurt", "Pain"));

		// insert LA
		VerbalItem vi1 = new VerbalItem(keyConcepts);
		EntryPoint ep = f.getEntryPoint("context");
		ep.insert(vi1);
		//f.getkSession().fireAllRules();

		//clock.advanceTime(2, TimeUnit.SECONDS);

		TimeUnit.SECONDS.sleep(1);
		// insert fake non-verbal
		ep.insert(new NonVerbalItem());
		//f.getkSession().fireAllRules();
		
		TimeUnit.SECONDS.sleep(10);
		//clock.advanceTime(10, TimeUnit.SECONDS);
//		f.getkSession().fireAllRules();

//		TimeUnit.SECONDS.sleep(1);
//		f.stop();
	}

}
