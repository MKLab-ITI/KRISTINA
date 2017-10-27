package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test12 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_12.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test12 t = new Test12();
		t.call();
	}

}
