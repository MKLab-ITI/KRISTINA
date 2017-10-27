package gr.iti.kristina.test.testcases;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

public class Test {

	List<Item> readTestFile(String file) throws IOException {
		InputStream stream = Test.class.getResourceAsStream("/testcases/" + file);
		String theString = IOUtils.toString(stream, Charset.defaultCharset());

		Gson g = new Gson();
		Item[] items = g.fromJson(theString, Item[].class);
		return Arrays.asList(items);

		// System.out.println(Utils.flattenCollection(Arrays.asList(items)));

	}

	public static void main(String[] args) throws IOException {
		Test test = new Test();
		test.readTestFile("pregnancy.test");
	}
}
