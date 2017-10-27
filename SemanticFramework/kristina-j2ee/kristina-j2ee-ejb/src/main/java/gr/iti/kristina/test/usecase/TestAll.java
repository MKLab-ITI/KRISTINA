package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

public class TestAll {
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test4 t4 = new Test4();
		Test5 t5 = new Test5();
		Test6 t6 = new Test6();
		Test7 t7 = new Test7();
		Test8 t8 = new Test8();
		Test11 t11 = new Test11();
		Test12 t12 = new Test12();
		Test15 t15 = new Test15();
		Test16 t16 = new Test16();
		Test17 t17 = new Test17();
		Test18 t18 = new Test18();
		Test20 t20 = new Test20();
		Test24 t24 = new Test24();
		Test26 t26 = new Test26();
		Test28 t28 = new Test28();
		t4.call();
		t5.call();
		t6.call();
		t7.call();
		t8.call();
		t11.call();
		t12.call();
		t15.call();
		t16.call();
		t17.call();
		t18.call();
		t20.call();
		t24.call();
		t26.call();
//		t28.call();

	}
}
