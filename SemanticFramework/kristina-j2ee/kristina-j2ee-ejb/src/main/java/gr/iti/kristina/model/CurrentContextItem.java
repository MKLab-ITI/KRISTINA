package gr.iti.kristina.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;

public class CurrentContextItem {

	private DateTime timestamp;

	private String speechActType;

	private String la_encode;
	private String topicInstance;
	private Collection<Resource> topics = new ArrayList<Resource>();
	private OntModel responseModel;

	public CurrentContextItem(Set<Resource> data) {

		this.timestamp = new DateTime();
	}

	public CurrentContextItem() {
		this.timestamp = new DateTime();
	}

	public CurrentContextItem(OntResource topicInstance, Collection<Resource> topics, String la_decode,
			OntResource speechActType) {
		if (topicInstance != null)
			this.setTopicInstance(topicInstance.toString());
		if (topics != null)
			this.setTopics(topics);
		this.la_encode = la_decode;
		this.setSpeechActType(speechActType.toString());
	}

	public void setResponseModel(OntModel responseModel) {
		this.responseModel = responseModel;
	}

	public OntModel getResponseModel() {
		return responseModel;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public String getText() {
		return la_encode;
	}

	public void setText(String la_encode) {
		this.la_encode = la_encode;
	}

	public static void main(String[] args) {
		CurrentContextItem cw = new CurrentContextItem(null);
		System.out.println(cw.getTimestamp());
	}

	public String getSpeechActType() {
		return speechActType;
	}

	public void setSpeechActType(String speechActType) {
		this.speechActType = speechActType;
	}

	public String getTopicInstance() {
		return topicInstance;
	}

	public void setTopicInstance(String topicInstance) {
		this.topicInstance = topicInstance;
	}

	public Collection<Resource> getTopics() {
		return topics;
	}

	public void setTopics(Collection<Resource> topics) {
		this.topics = topics;
	}

}
