package Functions;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

import java.io.StringWriter;

/**
 * Created by spyridons on 9/21/2016.
 */
public class XmlFunctions {
    /***
     * clear all xml tags from a text
     * @param text
     * @return
     */
    public static String clearXML(String text) {
        String xmlRegex = "<" // an opening '<' sign (xml tag start)
                + "[^><]+" // followed by one or more characters that are not '>' or '<'
                // the '<' in the negation covers the case that the first matched '<' is a simple lower than symbol
                + ">"; // followed by a '>' sign (xml tag end)
        String textWithoutXML = text.replaceAll(xmlRegex, "");
        return textWithoutXML;
    }

    /**
     * Print contents of node
     * @param node
     * @return
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static String node2String(Node node) {
        // you may prefer to use single instances of Transformer, and
        // StringWriter rather than create each time. That would be up to your
        // judgement and whether your app is single threaded etc
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerConfigurationException e) {
            System.out.println("TransformerConfigurationException caught!!!");
            return "TransformerConfigurationException caught!!!";
        } catch (TransformerException e) {
            System.out.println("TransformerException caught!!!");
            return "TransformerException caught!!!";
        }

    }
}
