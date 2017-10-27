package gr.iti.kristina.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class SimpleXmlRpcClient {


	public static void main(String[] args) {

		// Input
		String server = "http://160.40.51.32:8282/pescado/sos";
		String requestMethod = "POST";
		/*String xmlRequest = "<?xml version='1.0' encoding='UTF-8'?>"+
							"<GetFeatureOfInterest xmlns='http://www.opengis.net/sos/1.0' service='SOS' version='1.0.0' xmlns:ows='http://www.opengeospatial.net/ows' xmlns:gml='http://www.opengis.net/gml' xmlns:ogc='http://www.opengis.net/ogc'   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosGetFeatureOfInterest.xsd'>"+
							"<FeatureOfInterestId>foi_0000</FeatureOfInterestId></GetFeatureOfInterest>";*/
		
		String placeCode = "foi_0206"; //Tubingen code
		String startDate="2016-07-25T00:00:00+00:00";
		String endDate="2016-07-26T05:00:00+00:00";
		
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
				"<observedProperty>temperature#forecasted#exact</observedProperty>"+
				"<observedProperty>windSpeed#forecasted#exact</observedProperty>"+
				"<observedProperty>rain#forecasted#exact</observedProperty>"+
				"<observedProperty>humidity#forecasted#exact</observedProperty>"+
				"<observedProperty>snow#forecasted#exact</observedProperty>"+
				"<observedProperty>skyCondition#forecasted#exact</observedProperty>"+
				"<observedProperty>pressure#forecasted#exact</observedProperty>"+
				"<observedProperty>windDirection#forecasted#exact</observedProperty>"+
				//sky/pressure conditions
				"<featureOfInterest>"+
				  "<ObjectID>"+placeCode+"</ObjectID>"+
				"</featureOfInterest>"+
				"<responseFormat>text/xml;subtype=&quot;om/1.0.0&quot;</responseFormat>"+
				"</GetObservation>";
					
		// Create class instances
		SimpleXmlRpcClient xml_rpc = new SimpleXmlRpcClient();

		try
		{

			String xmlResponse = xml_rpc.runXmlRpcClient(server, requestMethod, xmlRequest);

			System.out.println("XML-RPC Response\n\n"+xmlResponse);
			
		}
		catch (IOException e) {
			System.err.println(e); 
			e.printStackTrace();
		}

	}


	String runXmlRpcClient(String server, String requestMethod, String xmlRequest) throws IOException{

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


}