package gr.iti.kristina.model;

public class IRResponse {

	String domain;
	String content;
	
	public IRResponse(String domain, String content) {
		this.domain = domain;
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

	@Override
	public String toString() {
		return "IRResponse [domain=" + domain + ", content=" + content + "]";
	}
	
	

}
