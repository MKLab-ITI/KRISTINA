package gr.iti.kristina.model.fusion.scenarios;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.kie.api.runtime.rule.EntryPoint;

import gr.iti.kristina.model.fusion.NonVerbalItem;
import gr.iti.kristina.services.FusionServiceBean;

public class ScenarioTest {
	
	
	public static void main(String[] args) throws InterruptedException {
		FusionServiceBean f = new FusionServiceBean();
		f.initialisation();
		new Thread(new Runnable() {
			public void run() {
				f.getkSession().fireUntilHalt();
			}
		}).start();

		SessionPseudoClock clock = (SessionPseudoClock) f.getClock();

		NonVerbalItem nvi = new NonVerbalItem();
		//nvi.setType("gesture");
		
		EntryPoint ep = f.getEntryPoint("context");
		ep.insert(nvi);

		clock.advanceTime(1, TimeUnit.SECONDS);

		ep.insert(new NonVerbalItem());
		TimeUnit.SECONDS.sleep(1);
		f.stop();
	}

}


