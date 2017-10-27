package gr.iti.kristina.topicflow;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.hpl.jena.ontology.OntClass;

import gr.iti.kristina.utils.Utils;

public class Theme {

	private static final Logger LOG = LoggerFactory.getLogger(Theme.class);

	public OntClass theme;
	public Set<OntClass> topics = new HashSet<>();
	public boolean active;

	public Theme(OntClass theme, Set<OntClass> topics) {
		this.theme = theme;
		this.topics = topics;
		this.active = this._isActive();
	}

	public Set<OntClass> getMostSpecificTopics() {
		Set<OntClass> toRemove = new HashSet<>();
		HashSet<OntClass> _set = new HashSet<OntClass>(this.topics);
		for (OntClass c1 : _set) {
			for (OntClass c2 : _set) {
				if (c1.equals(c2)) {
					continue;
				}
				if (c1.hasSuperClass(c2, false)) {
					toRemove.add(c2);
				}
			}
		}
		_set.removeAll(toRemove);
		return _set;
	}

	public Set<OntClass> getMostGenericTopics() {
//		throw new UnsupportedOperationException("need also to check if has responses");
		Set<OntClass> toRemove = new HashSet<>();
		HashSet<OntClass> _set = new HashSet<OntClass>(this.topics);
		// _set.remove(theme);
		for (OntClass c1 : _set) {
			for (OntClass c2 : _set) {
				if (c1.equals(c2)) {
					continue;
				}
				if (c1.hasSuperClass(c2, false)) {
					toRemove.add(c1);
				}
			}
		}
		_set.removeAll(toRemove);
		LOG.debug("getMostGenericTopics" + _set);
		return _set;
	}
	public Set<OntClass> getMostGenericTopicsExcludingThemes() {
		Set<OntClass> toRemove = new HashSet<>();
		HashSet<OntClass> _set = new HashSet<OntClass>(this.topics);
		_set.remove(theme);
		for (OntClass c1 : _set) {
			for (OntClass c2 : _set) {
				if (c1.equals(c2)) {
					continue;
				}
				if (c1.hasSuperClass(c2, false)) {
					toRemove.add(c1);
				}
			}
		}
		_set.removeAll(toRemove);
		LOG.debug("getMostGenericTopicsExcluingThemes" + _set);
		return _set;
	}


	private boolean _isActive() {
		return this.topics.stream().anyMatch(x -> x.equals(this.theme));
	}
	
	public boolean isActive() {
		return active;
	}

	@Override
	public String toString() {
		// ObjectMapper mapper = new ObjectMapper();
		// try {
		// return
		// mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		// } catch (JsonProcessingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return "";

		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("theme", Utils.getLocalName(theme) + String.format("[%b]", active))
				.append("topics", Utils.getLocalNames(topics)).toString();
		//
		// return "Theme [theme=" + theme.getLocalName() + ", topics=" +
		// Utils.getLocalNames(topics) + "]";
	}

	// public void removeLeafTopics(Set<OntClass> recentTopics) {
	// SetView<OntClass> intersection = Sets.intersection(this.topics,
	// recentTopics);
	// if (!intersection.isEmpty())
	// LOG.debug("leaf topics removed: " + intersection);
	// this.topics.removeIf(t -> recentTopics.contains(t));
	//
	// }

}
