package gr.iti.mklab.kindex.Functions;

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
}
