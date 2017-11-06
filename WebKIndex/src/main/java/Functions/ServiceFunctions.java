package Functions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by spyridons on 11/8/2016.
 */
public class ServiceFunctions {

    /**
     * This function makes a POST request and returns its response.
     * Parameters are written into the request body in this method.
     * @param url
     * @param parameters, a map containing the request parameters
     * @param encode, whether to encode the parameters
     * @return
     * @throws IOException
     */
    public static String sendPostRequest(String url, HashMap<String,String> parameters, boolean encode)
    {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("content-type","application/x-www-form-urlencoded");

            String urlParameters = "";
            String prefix = "";
            for (Map.Entry<String, String> entry : parameters.entrySet())
            {
                String key = entry.getKey();
                String value = "";
                if(encode){
                    value = URLEncoder.encode(entry.getValue() , "utf-8");
                }
                else{
                    value = entry.getValue();
                }
                urlParameters+= prefix + key + "=" + value;
                prefix = "&";
            }

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            System.out.println("Sending 'POST' request to URL : " + url);
            //		System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            if(responseCode!=200)
                return "Error code returned: "+responseCode;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch(IOException io){
            System.out.println(io.getMessage());
            return "IO Exception: " + io.getMessage();
        }
    }

    /**
     * This function makes a POST request and returns its response.
     * Raw text is given in the body, so any other parameter must be already given in the query part of the url
     * @param url
     * @param body, the request body raw text
     * @param format, the request body text format
     * @return
     * @throws IOException
     */
    public static String sendPostRequestRaw(String url, String body, String format)
    {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("content-type",format);

            // Send post request
            System.out.println("Sending 'POST' request to URL : " + url);
            con.setDoOutput(true);


            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
            bw.write(body);
            bw.flush();
            bw.close();

            int responseCode = con.getResponseCode();

            //		System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            if(responseCode!=200)
                return "Error code returned: "+responseCode;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch(IOException io){
            System.out.println(io.getMessage());
            return "IO Exception: " + io.getMessage();
        }
    }

    public static String sendGetRequest(String url)
    {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();

            System.out.println("Sending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            if(responseCode!=200)
                return "Error code returned: "+responseCode;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
            }
            in.close();

            return response.toString();
        }
        catch(IOException io){
            System.out.println(io.getMessage());
            return "IO Exception: " + io.getMessage();
        }
    }

    public static void main(String[] args) {
        String s = sendGetRequest("http://localhost:9000/newspaper?title=Wohnmobil%20ausger%C3%A4umt");
        System.out.println(s);
    }
}
