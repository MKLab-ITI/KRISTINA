package gr.iti.kristina.test.testcases;

import java.util.List;

public class Item {

	public int id;
	public List<String> input;
	public List<Output> output;

	@Override
	public String toString() {
		return "Item [id=" + id + ", input=" + input + ", output=" + output + "]";
	}

}
