package gr.iti.kristina.model;

import java.util.HashSet;

public class Signature {

	public String localName, uri, label;
	HashSet<Signature> superClasses;
	HashSet<String> synsets;
	// public String type; //class or instance

	public Signature() {
	}

	public static HashSet<Signature> createSignatures(HashSet<KeyEntityWrapper> entities) {
		HashSet<Signature> signatures = new HashSet<>();
		for (KeyEntityWrapper kew : entities) {
			Signature s = new Signature();

			s.localName = kew.type.getLocalName();
			s.uri = kew.type.toString();
			s.label = s.localName;

			signatures.add(s);

		}

		return signatures;
	}

	@Override
	public String toString() {
		return "Signature{" + "localName=" + localName + ", uri=" + uri + ", label=" + label + ", superClasses="
				+ superClasses + ", synsets=" + synsets + '}';
	}

}
