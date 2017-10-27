package gr.iti.kristina.model.rules;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Resource;

public class Topic {
	ArrayList values;
	Resource requestType;
	boolean generic;
	boolean answered;
	String userText;
	String language;

	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	public ArrayList getValues() {
		return values;
	}

	public void setValues(ArrayList values) {
		this.values = values;
	}

	public Resource getRequestType() {
		return requestType;
	}

	public void setRequestType(Resource requestType) {
		this.requestType = requestType;
	}

	public boolean isGeneric() {
		return generic;
	}

	public void setGeneric(boolean generic) {
		this.generic = generic;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	
	public String getUserText() {
		return userText;
	}
	public void setUserText(String userText) {
		this.userText = userText;
	}

}
