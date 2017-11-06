package Unitex;

import com.google.gson.Gson;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Not useful at 07/10/2016
 * Created by spyridons on 8/18/2016.
 */
public class UnitexJniParser {

    public static void main(String [] args) throws IOException, ParserConfigurationException, SAXException {
        ArrayList<String> toRemove = new ArrayList<String>( Arrays.asList("<manual>","<TREAT>","<DRUG>","<manual_drug>"
                ,"<Cad_drug>","<conj_no>","<GENEU>","<manual_DIS>","<Disease>","<SignORSymptom>","<TEST>","</manual>"
                ,"</TREAT>","</DRUG>","</manual_drug>","</Cad_drug>","</conj_no>","</GENEU>","</manual_DIS>"
                ,"</Disease>","</SignORSymptom>","</TEST>"));

        System.out.println(System.getProperty("user.dir"));
        String baseWorkDir = "./unitexFiles/baseWork";
        String resourceDir = "./unitexFiles/resources";

        // call unitex for input file
//        UnitexJniHandler.main(null);

        // parse result
        String outputFilePath = baseWorkDir + "/test_output2.txt";
        String line = "";
        BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(outputFilePath), "UTF-16LE"));
        JSONArray finalResult = new JSONArray();
        while((line = file.readLine())!=null)
        {
            // form as an xml string
            String xml = "<xml>" + line + "</xml>";

            // replace non-closing and redundant tags
            if(xml.contains("<Negation>")) {
                xml = xml.replaceAll("\\<Negation\\>", "");
            }
            for(String rm: toRemove) {
                if (xml.contains(rm))
                    xml = xml.replaceAll(rm, "");
            }

            // track xml tags
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();
            JSONObject ob = new JSONObject();
            boolean hasChildElements = false;
            if(hasChildElements(root)) {
                ob.put("E1", new JSONArray());
                ob.put("E2", new JSONArray());
                hasChildElements = true;
            }
            for(Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if(child instanceof Element){
                    // form result
                    String jsonResult = formKBResult(child.getTextContent().trim());
                    String tag = ((Element) child).getTagName();
                    if(tag.equals("E1") || tag.equals("E2"))
                        ((JSONArray) ob.get(tag)).put(new JSONObject(jsonResult));
                    else
                        ob.put(tag,new JSONObject(jsonResult));
                }
            }
            finalResult.put(ob);
        }
        System.out.println(finalResult);

    }

    public static String formKBResult(String text)
    {
        UnitexJniResult result = new UnitexJniResult();
        Babelfy bfy = new Babelfy();
        List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(text, Language.EN);

//        result.setConcept(tag);
        result.setTerm(text);
        for (SemanticAnnotation annotation : bfyAnnotations) {
            String babelNetURL = annotation.getBabelNetURL();
            result.addToBabelNet(babelNetURL);
            String dbPediaURL = annotation.getDBpediaURL();
            result.addToDBPedia(dbPediaURL);
        }

        return new Gson().toJson(result);
    }

    public static boolean hasChildElements(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0;i < children.getLength();i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                return true;
        }
    return false;
    }

}
