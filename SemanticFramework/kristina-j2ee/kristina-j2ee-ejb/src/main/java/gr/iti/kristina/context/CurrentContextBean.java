package gr.iti.kristina.context;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;

import gr.iti.kristina.model.CurrentContextItem;

/**
 * Session Bean implementation class ContextBean
 */
@Singleton
@LocalBean
public class CurrentContextBean {

	LinkedList<CurrentContextItem> currentContextItems;
	private double version;

	public CurrentContextBean() {
		if (currentContextItems == null)
			currentContextItems = new LinkedList<>();
	}

	@PostConstruct
	public void initialisation() {
		System.out.println("---> current context bean initialisation ...");
		currentContextItems = new LinkedList<>();
		version = Math.random() * 50 + 1;
	}

	public void update(Set<Resource> context) {
		this.currentContextItems.add(new CurrentContextItem(context));
	}

	public void update(OntResource topicInstance, Collection<Resource> topicClasses, String la_decode,
			OntResource speechActType) {
		CurrentContextItem item = new CurrentContextItem(topicInstance, topicClasses, la_decode, speechActType);
		currentContextItems.add(item);
	}

	public void clear() {
		currentContextItems.clear();
	}

	public LinkedList<CurrentContextItem> getCurrentContextItems() {
		return currentContextItems;
	}

	public CurrentContextItem getLastContext() {
		try {
			return currentContextItems.getLast();
		} catch (Exception e) {
			return null;
		}
	}

	// TODO
	public void save() {
		throw new UnsupportedOperationException();
	}

	public double getVersion() {
		return version;
	}

}
