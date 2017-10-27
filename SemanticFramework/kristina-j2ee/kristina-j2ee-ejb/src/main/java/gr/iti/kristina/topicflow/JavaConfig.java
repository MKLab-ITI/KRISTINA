package gr.iti.kristina.topicflow;

import java.util.List;

public class JavaConfig {

	public String method;
	public String hasResults;
	public List<String> args;
	public String type;
	public String lang;

	@Override
	public String toString() {
		return "JavaConfig [method=" + method + ", hasResults="
				+ hasResults + ", args=" + args + ", type=" + type + ", lang=" + lang + "]";
	}

}
