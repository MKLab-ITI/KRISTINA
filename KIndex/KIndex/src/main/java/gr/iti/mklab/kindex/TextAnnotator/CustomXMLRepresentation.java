package gr.iti.mklab.kindex.TextAnnotator;

import java.util.regex.Pattern;
/**
 * Created by Thodoris Tsompanidis on 1/4/2016.
 */
public class CustomXMLRepresentation {

	private String xml;

	public CustomXMLRepresentation() {
	}

	public CustomXMLRepresentation(String xml) {
		this.xml = xml;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public void removeElement(String element){
		/*if (element.contains("[")) {
			element = element.replaceAll("[\\[]", "[\\\\\\[]");
		}
		else if (element.contains("]")) {
			element = element.replaceAll("[]]", "[]]");
		}*/
		//element = element.replaceAll(">&<",">&amp;<");
		//element = element.replaceAll("\"&\"","\"&amp;\"");
		element = element.replaceAll("&(?!amp)","&amp;");
		//element = element.replaceAll("[(]","[(]");
		//element = element.replaceAll("[)]","[)]");
		element = element.replaceAll("><<",">&lt;<");
		element = element.replaceAll("\"<\"","\"&lt;\"");
		element = element.replaceAll(">><",">&gt;<");
		element = element.replaceAll("\">\"","\"&gt;\"");
		//element = element.replaceAll("\\*","\\\\*");
		//element = element.replaceAll("\\|","\\\\|");
		//element = element.replaceAll(">&<",">&amp;<");
		//element = element.replaceAll("[\\$\\^\\?\\+]","\\\\$0");
		//element = element.replaceAll("[\\[\\](){}]","[$0]");
		xml=xml.replaceFirst(Pattern.quote(element),"");
		xml = xml.replaceAll("   ","");
	}

	public void escapeXMLCharacters() {
		//xml = xml.replaceAll(">&<",">&amp;<");
		//xml = xml.replaceAll("\"&\"","\"&amp;\"");

		//replace all "&" characters not  followed by "amp"
		xml = xml.replaceAll("&(?!amp)","&amp;");
	}
}
