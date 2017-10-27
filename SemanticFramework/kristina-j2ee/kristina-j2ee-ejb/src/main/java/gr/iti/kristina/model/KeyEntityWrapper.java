package gr.iti.kristina.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class KeyEntityWrapper {

	Resource ind;
	Resource type;
	Resource annotation;

	public KeyEntityWrapper(Resource ind, Resource type, Resource annotation) {
		super();
		this.ind = ind;
		this.type = type;
		this.annotation = annotation;
	}

	public Resource getType() {
		return type;
	}

	public Resource getInd() {
		return ind;
	}

	public Resource getAnnotation() {
		return annotation;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ind.getLocalName() + " - " + type.getLocalName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((ind == null) ? 0 : ind.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyEntityWrapper other = (KeyEntityWrapper) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		if (ind == null) {
			if (other.ind != null)
				return false;
		} else if (!ind.equals(other.ind))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
