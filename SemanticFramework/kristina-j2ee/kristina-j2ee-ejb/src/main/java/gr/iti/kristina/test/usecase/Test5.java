package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test5 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_5.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test5 t = new Test5();
		t.call();
	}

}
