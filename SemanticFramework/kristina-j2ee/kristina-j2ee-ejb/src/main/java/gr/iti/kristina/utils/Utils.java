package gr.iti.kristina.utils;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.URI;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import gr.iti.kristina.model.Namespaces;

public class Utils {

	public final static List<String> UPPER_CONCEPTS = Arrays.asList("http://www.w3.org/2002/07/owl#Thing",
			"http://www.w3.org/2002/07/owl#NamedIndividual", Namespaces.LA_CONTEXT + "Context",
			Namespaces.KB_CONTEXT + "Context");
	public final static List<Resource> UPPER_CONCEPTS_RES;

	static {
		Model model = ModelFactory.createDefaultModel();
		UPPER_CONCEPTS_RES = Arrays.asList(model.createResource("http://www.w3.org/2002/07/owl#Thing"),
				model.createResource("http://www.w3.org/2002/07/owl#NamedIndividual"),
				model.createResource(Namespaces.LA_CONTEXT + "Context"),
				model.createResource(Namespaces.KB_CONTEXT + "Context"),
				model.createResource(Namespaces.KB_CONTEXT + "ActivityRoutineContext"));
	}

	public static Collection<Resource> removeUpperClasses(List<Resource> types) {
		// System.out.println("Collection to clean: " + types);
		types.removeAll(UPPER_CONCEPTS_RES);
		return types;
		// return Collections2.filter(types, new Predicate<Resource>() {
		//
		// @Override
		// public boolean apply(Resource input) {
		// // System.out.println(input.getLocalName());
		// if (UPPER_CONCEPTS.contains(input.toString())) {
		// return false;
		// }
		// return true;
		// }
		// });
	}

	// public static HashSet<String> removeSuperClasses(Collection<String>
	// types, RepositoryConnection kbConnection)
	// throws RepositoryException {
	// ValueFactory vf = kbConnection.getValueFactory();
	// HashSet<String> temp = new HashSet<>();
	// temp.addAll(types);
	// for (String t1 : types) {
	// URI p1 = vf.createURI(t1);
	// for (String t2 : types) {
	// if (t1.equals(t2))
	// continue;
	// URI p2 = vf.createURI(t2);
	// if (kbConnection.hasStatement(p2, RDFS.SUBCLASSOF, p1, true)) {
	// temp.remove(t1);
	// // System.err.println("triple removed: " + t1);
	// // break;
	// }
	// if (kbConnection.hasStatement(p1, RDFS.SUBCLASSOF, p2, true)) {
	// temp.remove(t2);
	// // System.err.println("triple removed: " + t2);
	// // break;
	// }
	//
	// }
	// }
	// return temp;
	//
	// }

	public static Collection<String> removeUpperClasses(Set<String> types) {
		// System.out.println("Collection to clean: " + types);
		types.removeAll(UPPER_CONCEPTS);
		return types;
		// return Collections2.filter(types, new Predicate<String>() {
		//
		// @Override
		// public boolean apply(String input) {
		// // System.out.println(input.getLocalName());
		// if (UPPER_CONCEPTS.contains(input)) {
		// return false;
		// }
		// return true;
		// }
		// });
	}

	public static OntModel createDefaultModel(boolean processImports) {
		OntDocumentManager manager = new OntDocumentManager();
		manager.setProcessImports(processImports);
		OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);
		s.setDocumentManager(manager);
		return ModelFactory.createOntologyModel(s);
	}

	public static OntModel createDefaultModel(boolean processImports, OntModelSpec spec) {
		OntDocumentManager manager = new OntDocumentManager();
		manager.setProcessImports(processImports);
		OntModelSpec s = new OntModelSpec(spec);
		s.setDocumentManager(manager);
		return ModelFactory.createOntologyModel(s);
	}

	public static String randomString() {
		return new BigInteger(130, new SecureRandom()).toString(32);
	}

	public static String tempURI() {
		return Namespaces.TEMP;
	}

	public static String tempFullURI() {
		return Namespaces.TEMP + Utils.randomString();
	}

	public static String modelToString(Model model, String format) {
		StringWriter out = new StringWriter();
		model.write(out, format);
		return out.toString();
	}

	public static <K, V> void printMap(Multimap<K, V> source) {
		Set<K> keys = source.keySet();
		for (K key : keys) {
			Collection<V> value = source.get(key);
			System.out.printf(" - %-30s %s \n", key instanceof URI ? ((URI) key).getLocalName() : key,
					flattenCollection(value));
		}
	}
	
	public static <K, V> void printMapJena(Map<K, V> source) {
		Set<K> keys = source.keySet();
		for (K key : keys) {
			V value = source.get(key);
			System.out.printf(" - %-30s %s \n", key instanceof OntClass ? Utils.getLocalName((OntClass) key) : key,
					value);
		}
	}

	public static <K, V> String printMapString(Multimap<K, V> source) {
		String r = "";
		Set<K> keys = source.keySet();
		for (K key : keys) {
			Collection<V> value = source.get(key);
			r += String.format(" - %-30s %s \n", key instanceof URI ? ((URI) key).getLocalName() : key,
					flattenCollection(value));
		}
		return r;
	}

	public static String flattenCollection(Collection col) {
		StringBuilder s = new StringBuilder();
		s.append("[\n");
		for (Object object : col) {
			if (object instanceof URI) {
				s.append(((URI) object).getLocalName() + ", \n ");
			} else {
				s.append(object + ", \n");
			}
		}
		s.append("]");
		return s.toString();
	}

	public static String substringAfter(String text, String c) {
		String t = StringUtils.substringAfter(text, c);
		if (t == null || t.isEmpty())
			return text;
		else
			return t;
	}

	public static String getLocalNames(Set<? extends OntClass> set) {
		return set.stream().map(t -> t.getLocalName()).collect(Collectors.joining(", "));
	}
	public static String getLocalNames(List< ? extends OntClass> set) {
		return getLocalNames(Sets.newHashSet(set));
	}

	public static String getLocalName(OntClass clazz) {
		return clazz != null ? clazz.getLocalName() : null;
	}

}
