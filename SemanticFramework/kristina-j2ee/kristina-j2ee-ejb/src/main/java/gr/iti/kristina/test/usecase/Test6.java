package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test6 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_6.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test6 t = new Test6();
		t.call();
	}

}
