package Weather;

import com.google.gson.Gson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by spyridons on 7/27/2016.
 */
public class WeatherHandler {

    public String getWeatherResponse(String placeCode, String startDate, String endDate)
    {
        System.out.println("Weather request");
        // Input
        String server = "http://160.40.X.Y:Z/pescado/sos";
        String requestMethod = "POST";
        System.out.println("Place code: "+placeCode);
        System.out.println("Start date: "+startDate);
        System.out.println("End date: "+endDate);

        String xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<GetObservation xmlns=\"http://www.opengis.net/sos/1.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:om=\"http://www.opengis.net/om/1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/sos/1.0"+
                "http://schemas.opengis.net/sos/1.0.0/sosGetObservation.xsd\" service=\"SOS\" version=\"1.0.0\" srsName=\"urn:ogc:def:crs:EPSG:4326\">"+
                "<offering>WeatherDataType</offering>"+
                "<eventTime>"+
                "<ogc:TM_During>"+
                "<ogc:PropertyName>om:samplingTime</ogc:PropertyName>"+
                "<gml:TimePeriod>"+
                "<gml:beginPosition>"+startDate+"</gml:beginPosition>"+
                "<gml:endPosition>"+endDate+"</gml:endPosition>"+
                "</gml:TimePeriod>"+
                "</ogc:TM_During>"+
                "</eventTime>"+
//                "<observedProperty>temperature#forecasted#exact</observedProperty>"+
//                "<observedProperty>windSpeed#forecasted#exact</observedProperty>"+
////"<observedProperty>rain#forecasted#exact</observedProperty>"+
//                "<observedProperty>humidity#forecasted#exact</observedProperty>"+

                "<observedProperty>temperature#forecasted#exact</observedProperty>"+
                "<observedProperty>windSpeed#forecasted#exact</observedProperty>"+
                "<observedProperty>rain#forecasted#exact</observedProperty>"+
                "<observedProperty>humidity#forecasted#exact</observedProperty>"+
                "<observedProperty>snow#forecasted#exact</observedProperty>"+
                "<observedProperty>skyCondition#forecasted#exact</observedProperty>"+
                "<observedProperty>pressure#forecasted#exact</observedProperty>"+
                "<observedProperty>windDirection#forecasted#exact</observedProperty>"+
//"<observedProperty>snow#forecasted#exact</observedProperty>"+
                //sky/pressure conditions
                "<featureOfInterest>"+
                "<ObjectID>"+placeCode+"</ObjectID>"+
                "</featureOfInterest>"+
                "<responseFormat>text/xml;subtype=&quot;om/1.0.0&quot;</responseFormat>"+
                "</GetObservation>";


        try {

            String xmlResponse = runXmlRpcClient(server, requestMethod, xmlRequest);

//            System.out.println("XML Request:" + xmlRequest);
//            System.out.println("XML-RPC Response\n\n" + xmlResponse);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlResponse)));
            Element root = doc.getDocumentElement();
            String result = getValues(root);
            System.out.println(result);

            List<String> columns = getColumns(root);

            WeatherResponseObject w = new WeatherResponseObject();
            w.setFields(columns);
            String[] values = result.trim().split(";");
            for (int i = 0 ; i < values.length; i++) {
                values[i] = values[i].replaceAll(",", ";");
            }
            List<String> valuesList = Arrays.asList(values);
            w.setValues(valuesList);

            String response = new Gson().toJson(w);
            return response;

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
        return null;
    }

    private String runXmlRpcClient(String server, String requestMethod, String xmlRequest) throws IOException {

        System.out.println("Sending request to weather service...");

        // Open url connection to server
        URL u = new URL(server);
        URLConnection uc = u.openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;

        // Set connection parameters
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod(requestMethod);

        // Open outputstream to write the request
        OutputStream out = connection.getOutputStream();
        OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");
        wout.write(xmlRequest);

        wout.flush();
        out.close();

        System.out.println("Response code: " + connection.getResponseCode());

        // Get the response
        InputStream in = connection.getInputStream();
        StringBuffer response = new StringBuffer();

        int c;
        while ((c = in.read()) != -1){
            response.append((char) c);
        }


        // Close streams and url connection
        in.close();
        out.close();
        connection.disconnect();

        return response.toString();
    }

    private String getValues (Element parent) {

        String result = "";

        if(parent.getTagName().equals("swe:values"))
            result = parent.getTextContent();



        for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if(child instanceof Element)
                result = getValues((Element) child);
        }

        return result;
    }

    private List<String> getColumns (Element parent) {

        List<String> result = new ArrayList<>();

        if(parent.getTagName().equals("swe:field")){
            String fieldName = parent.getAttribute("name");
            if(fieldName!=null)
                result.add(parent.getAttribute("name"));
        }



        for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if(child instanceof Element)
                result.addAll(getColumns((Element) child));
        }

        return result;
    }
}
