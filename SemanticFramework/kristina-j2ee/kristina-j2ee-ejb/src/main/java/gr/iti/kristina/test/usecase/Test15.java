package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test15 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_15.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test15 t = new Test15();
		t.call();
	}

}
