package gr.iti.kristina.model;

import java.io.Serializable;

public class Dependency implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String key1, key2;

	public Dependency(String key1, String key2) {
		this.key1 = key1;
		this.key2 = key2;
	}

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

}
