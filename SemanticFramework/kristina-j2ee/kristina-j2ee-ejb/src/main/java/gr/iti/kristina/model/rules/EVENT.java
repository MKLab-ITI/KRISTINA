package gr.iti.kristina.model.rules;

public class EVENT {
	public String value;
	public Object payload;
	
	public EVENT (){
		payload = "{}";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
