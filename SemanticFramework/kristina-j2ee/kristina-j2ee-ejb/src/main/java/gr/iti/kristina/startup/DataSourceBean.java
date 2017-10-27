package gr.iti.kristina.startup;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.google.common.collect.HashMultimap;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import gr.iti.kristina.model.Namespaces;
import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class DataSource
 */
//@Startup
@Singleton
@LocalBean
public class DataSourceBean {

	public static HashMultimap<Resource, Resource> datasource;
	public static OntModel model;

	public DataSourceBean() {
	}

	@PostConstruct
	public void initialisation() {
		System.err.println("datasource bean initialisation");
		datasource = HashMultimap.create();
		model = Utils.createDefaultModel(false, PelletReasonerFactory.THE_SPEC);
		model.read(Namespaces.LA_ONTO_FILE, "TTL");
		model.read(Namespaces.KB_CONTEXT_FILE, "TTL");
		model.prepare();

		OntClass Context = model.getOntClass(Namespaces.KB_CONTEXT + "Context");
		List<OntClass> list = Context.listSubClasses(false).toList();
		list.remove(OWL.Nothing);
		ObjectProperty datasourceProp = model.getObjectProperty(Namespaces.KB_CONTEXT + "datasource");
		for (OntClass c : list) {
			Individual temp = model.createIndividual("http://temp#" + Utils.randomString(), c);
			model.prepare();
			List<RDFNode> datasources = temp.listPropertyValues(datasourceProp).toList();
			for (RDFNode rdfNode : datasources) {
				datasource.put(c, rdfNode.asResource());
			}
		}

		Utils.printMap(datasource);
	}

	public static Set<Resource> getDataSources(Resource topic) {
		return datasource.get(topic);
	}

	public static Set<String> getDataSources_str(Optional<Resource> topic) {
		if (!topic.isPresent()) {
			return Collections.singleton("http://kristina-project.eu/ontologies/context-light#ir");
		}
		Set<Resource> set = datasource.get(topic.get());
		return set.stream().map(c -> c.toString()).collect(Collectors.toSet());
	}

}
