package gr.iti.kristina.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionClock;

import gr.iti.kristina.model.fusion.Observation;

/**
 * Session Bean implementation class FusionService
 */
@Singleton
@LocalBean
//@Startup
public class FusionServiceBean {

	KieServices ks;
	KieContainer kContainer;
	KieSession kSession;

	HashSet<Observation> latest = new HashSet<>();
	HashSet<Observation> offlineLatest = new HashSet<>();

	private final List<Observation> inferred = new ArrayList<Observation>();

	boolean pseudo = false;

	public final String[] painContext = { "Pain", "Hurt" };
	public final String[] valueContext = { "Valence", "Arousal" };

	private final Logger logger = Logger.getLogger(FusionServiceBean.class);

	public FusionServiceBean() {
	}

	@PostConstruct
	public void initialisation() {
		KieSessionConfiguration config = KieServices.Factory.get().newKieSessionConfiguration();
		if (pseudo) {
			config.setOption(ClockTypeOption.get("pseudo"));
		} else {
			config.setOption(ClockTypeOption.get("realtime"));
		}
		ks = KieServices.Factory.get();
		kContainer = ks.getKieClasspathContainer();
		// kSession = kContainer.newKieSession("fusionKSession");
		kSession = kContainer.newKieSession("fusionKSession", config);

		setGlobals();

		new Thread(new Runnable() {
			public void run() {
				kSession.fireUntilHalt();
			}
		}).start();

		System.out.println("Fusion service sucessfully initialised.");
	}

	private void setGlobals() {
		kSession.setGlobal("logger", logger);
		kSession.setGlobal("service", this);
		kSession.setGlobal("painContext", painContext);
		kSession.setGlobal("valueContext", valueContext);

		// TODO add more contexts
	}

	public void log(String text) {
		System.out.println(text);
	}

	public EntryPoint getEntryPoint(String entryPoint) {
		return kSession.getEntryPoint(entryPoint);
	}

	public KieSession getkSession() {
		return kSession;
	}

	public SessionClock getClock() {
		return kSession.getSessionClock();
	}

	public void stop() {
		System.out.println("halting...");
		kSession.halt();
		System.out.println("Done.");
	}

	public void insert(Observation[] observations) {
		EntryPoint ep = getEntryPoint("context");
		for (Observation o : observations) {
			// o.setStart(new Date());
			System.out.println("inserting observation: " + o.getStart().getTime() + " " + o.getEnd().getTime());
			ep.insert(o);
		}
	}

	public void setLatest(HashSet<Observation> observations) {
		System.out.println("setLatest");
		latest = observations;
		System.out.println(latest.size());
	}

	public void addLatestOffline(Observation observation) {
		System.out.println("offline setLatest");
		// System.out.println(
		// "inserting observation: " + observation.getStart().getTime() + " " +
		// observation.getEnd().getTime());
		offlineLatest.add(observation);
		System.out.println("offline size: " + offlineLatest.size());
	}

	public HashSet<Observation> getLatest(int seconds) {

		// System.out.println("in");
		// List<Observation> observations = new ArrayList<>();
		// QueryResults results = kSession.getQueryResults("latest");
		// kSession.fireAllRules();
		// System.out.println("ok");

		return latest;
	}

	public static void main(String[] args) throws InterruptedException {
		FusionServiceBean b = new FusionServiceBean();
		b.initialisation();
		// TimeUnit.SECONDS.sleep(3);
		Observation vi1 = new Observation();
		EntryPoint ep = b.getEntryPoint("context");
		ep.insert(vi1);
		// TimeUnit.SECONDS.sleep(3);
		// b.getLatest(2);
		System.out.println(b.latest.size());

	}

	public HashSet<Observation> getOfflineLatest(int interval) {
		return offlineLatest;
	}

	public void clear() {
		EntryPoint ep = getEntryPoint("context");
		Collection<FactHandle> factHandles = ep.getFactHandles();
		// System.out.println("delete: " + factHandles.size());
		for (FactHandle factHandle : factHandles) {
			ep.delete(factHandle);
		}

		factHandles = kSession.getFactHandles();
		// System.out.println("delete: " + factHandles.size());
		for (FactHandle factHandle : factHandles) {
			kSession.delete(factHandle);
		}
	}

	public void clearOffline() {
		offlineLatest.clear();
		clear();
	}
	
	public Observation getFirst(ArrayList<Observation> obs){
		Collections.sort(obs);
		return obs.get(0);
	}
	
	public Observation getLast(ArrayList<Observation> obs){
		Collections.sort(obs);
		return obs.get(obs.size() - 1);
	}
	
	public int getId(ArrayList<Observation> obs){
		int id = 0;
		for (Observation m : obs) {
			id += m.hashCode();
		}
		return id;
	}

	public long getDurationInMillis(Date start, Date end) {
		Duration i1 = new Duration(new DateTime(start), new DateTime(end));
		return i1.getMillis();
	}

	public void logHighInterval(ArrayList<Observation> observations) {
		Collections.sort(observations);
		Observation first = observations.get(0);
		Observation last = observations.get(observations.size() - 1);
		double avg = 0;
		int id = 0;
		String type = "";
		for (Observation m : observations) {
			type = m.getType();
			// avg += Double.parseDouble(m.getValue());
			id += m.hashCode();
			avg += m.getValue();

		}
		avg = avg / (double) observations.size();
		Observation m = new Observation();
		m.setId(id + "");
		m.setValue(avg);
		m.setState("High-" + type + " (" + String.format("%.2f", avg) + ")x" + observations.size());
		m.setType("Fusion");
		m.setCLASS(type);
		m.setStart(first.getStart());
		m.setEnd(last.getEnd());
		m.setContext(observations);
		// m.setDuration(getDurationInMillis(m.getStart(), m.getEnd()));
		inferred.add(m);
		offlineLatest.add(m);
		System.out.println("High Moving Intensity Interval Added");
//		getEntryPoint("context").insert(m);
	}

	public void logLowInterval(ArrayList<Observation> observations) {
		Collections.sort(observations);
		Observation first = observations.get(0);
		Observation last = observations.get(observations.size() - 1);
		double avg = 0;
		int id = 0;
		String type = "";
		for (Observation m : observations) {
			// avg += Double.parseDouble(m.getValue());
			avg += m.getValue();
			id += m.hashCode();
			type = m.getType();
		}
		avg = avg / (double) observations.size();
		Observation m = new Observation();
		m.setId(id + "");
		m.setCLASS(type);
		m.setValue(avg);
		m.setState("Low-" + type + " (" + String.format("%.2f", avg) + ")x" + observations.size());
		m.setType("Fusion");
		m.setStart(first.getStart());
		m.setEnd(last.getEnd());
		m.setContext(observations);
		inferred.add(m);
		offlineLatest.add(m);
		System.out.println("Low Moving Intensity Interval Added");
//		getEntryPoint("context").insert(m);
	}

}
