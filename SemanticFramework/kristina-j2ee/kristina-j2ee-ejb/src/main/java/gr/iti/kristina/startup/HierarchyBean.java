package gr.iti.kristina.startup;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class HierarchyBean
 */
//@Startup
@Singleton
@LocalBean
public class HierarchyBean {

	// static final String ONTO_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/onto.ttl";
	// static final String LA_CTX_FILE =
	// "file:///C:/Users/gmeditsk/Dropbox/kristina_prototype1/context.ttl";

	public static OntModel model;

	public static Multimap<String, String> hierarchy;

	public HierarchyBean() {

	}

	@PostConstruct
	public void initialisation() {
		System.err.println("Hierarchy bean initialisation");
		hierarchy = HashMultimap.create();
		model = Utils.createDefaultModel(false, OntModelSpec.RDFS_MEM_RDFS_INF);
		model.read(Namespaces.LA_ONTO_FILE, "TTL");
		model.read(Namespaces.LA_CONTEXT_FILE, "TTL");
		model.prepare();

		List<OntClass> list = model.listClasses().toList();

		for (OntClass c : list) {
			List<OntClass> superclasses = c.listSuperClasses(false).toList();
			for (OntClass cc : superclasses) {
				hierarchy.put(c.toString(), cc.toString());
			}
		}
		// Utils.printMap(hierarchy);
	}

	public List<OntClass> getSuperClasses(String c) {
		OntClass ontClass = model.getOntClass(c);
		return ontClass.listSuperClasses(false).toList();
	}

	// public OntClass getDirectSuperClass (String c){
	// OntClass ontClass = model.getOntClass(c);
	// return ontClass.listSuperClasses(false).toList();
	// }

}
