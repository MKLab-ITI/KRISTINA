package gr.iti.kristina.topicflow.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IRResponse {

	String domain;
	String url;
	String content;

	public IRResponse(String domain, String url, String content) {
		this.domain = domain;
		this.url = url;
		this.content = content;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
//		return "IRResponse [domain=" + domain + ", content=" + content + "]";
	}

}
