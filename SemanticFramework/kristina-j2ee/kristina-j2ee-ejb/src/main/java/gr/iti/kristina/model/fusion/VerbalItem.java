package gr.iti.kristina.model.fusion;

import java.util.HashSet;

public class VerbalItem extends FusionItem {

	private HashSet<String> keyConcepts;

	public VerbalItem(HashSet<String> keyConcepts) {
		this.keyConcepts = keyConcepts;
	}

	public HashSet<String> getKeyConcepts() {
		return keyConcepts;
	}

}
