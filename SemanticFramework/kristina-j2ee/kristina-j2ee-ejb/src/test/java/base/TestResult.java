package base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.hp.hpl.jena.rdf.model.Model;

import gr.iti.kristina.test.testcases.Output;

public class TestResult {

	String scenario;
	String methodName;
	String description;
	Set<String> las;
	Set<Output> outputs;
	Model responseModel;

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("scenario", scenario)
				.append("methodName", methodName)
				.append("description", description)
				.append("LAs", las)
				.append("outputs", outputs)
				.append("responseModel", !this.responseModel.isEmpty()).toString();
	}

	public void save() {
		Path pathToRDF = Paths.get(String.format(
				"test-output/rdf/%s/%s.ttl", scenario, methodName
						+ "." + description.replaceAll(" ", "_").replaceAll("\\W+", "")));
		Path pathInfo = Paths.get(String.format(
				"test-output/rdf/%s/%s.info", scenario, methodName
						+ "." + description.replaceAll(" ", "_").replaceAll("\\W+", "")));
		try {
			Files.createDirectories(pathToRDF.getParent());
			responseModel.write(Files.newOutputStream(pathToRDF), "TTL");
			IOUtils.write(this.toString().getBytes(), Files.newOutputStream(pathInfo));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {
		// File f = new File("test-output/sample.todelete");
		// System.out.println(f.createNewFile());

		Path pathToFile = Paths.get("test-output/sample.todelete");
		Files.createDirectories(pathToFile.getParent());
		Files.createFile(pathToFile);

	}

}
