//package base;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.testng.IReporter;
//import org.testng.ISuite;
//import org.testng.ISuiteResult;
//import org.testng.ITestContext;
//import org.testng.ITestNGMethod;
//import org.testng.ITestResult;
//import org.testng.xml.XmlSuite;
//
//public class CustomReporter implements IReporter {
//
//		
//	@Override
//	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
//		// Iterating over each suite included in the test
//		for (ISuite suite : suites) {
//			// Following code gets the suite name
//			String suiteName = suite.getName();
//			// Getting the results for the said suite
//			Map<String, ISuiteResult> suiteResults = suite.getResults();
//			for (ISuiteResult sr : suiteResults.values()) {
//				ITestContext tc = sr.getTestContext();
//				List<ITestNGMethod> methods = Arrays.asList(tc.getAllTestMethods());
//				String testName = methods.stream().findFirst().get().getTestClass().getTestName();
//
//				System.out.println("*****************************************************");
//				System.out.println(testName);
//				System.out.println("Passed tests for suite '" + suiteName +
//						"' is:" + tc.getPassedTests().getAllResults().size());
//				System.out.println("Failed tests for suite '" + suiteName +
//						"' is:" +
//						tc.getFailedTests().getAllResults().size());
//				System.out.println("Skipped tests for suite '" + suiteName +
//						"' is:" +
//						tc.getSkippedTests().getAllResults().size());
//
//				for (ITestNGMethod tm : methods) {
//					Set<ITestResult> r = tc.getPassedTests().getResults(tm);
//					Set<ITestResult> r2 = tc.getFailedTests().getResults(tm);
//					System.out.println(tm);
//					System.out.print(tm.getDescription() + " ");
//					System.out.println(tm.getMethodName());
//					tm.getTestClass().getTestName();
//
//				}
//			}
//		}
//	}
//
//}
