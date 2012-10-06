
package com.loc;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


public class Proximity {
	
	private static final String GEOCODE_REQUEST_URL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&region=uk";
	private static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

	private static final String GEOCODE_REQUEST_DIR_URL = "http://maps.googleapis.com/maps/api/directions/json?sensor=false&region=uk";

	public String Calcproximit (String from, String to) {
		String distance = "NA";
        try {
        	StringBuilder urlBuilder = new StringBuilder(GEOCODE_REQUEST_DIR_URL);
            if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
            	urlBuilder.append("&origin=").append(URLEncoder.encode(from, "UTF-8"));
            	urlBuilder.append("&destination=").append(URLEncoder.encode(to, "UTF-8"));
            }
            
            final GetMethod getMethod = new GetMethod(urlBuilder.toString());
            try {
            	httpClient.executeMethod(getMethod);
                Reader reader = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());
                
                int data = reader.read();
                char[] buffer = new char[1024];
                Writer writer = new StringWriter();
                while ((data = reader.read(buffer)) != -1) {
                    	writer.write(buffer, 0, data);
               	}

                String result = "{"+writer.toString();
                
                JSONObject json = (JSONObject) JSONSerializer.toJSON( result );
                
                JSONArray routes =json.getJSONArray("routes");
                
                String parse1 = routes.getString(0);
                
                JSONObject json2 = (JSONObject) JSONSerializer.toJSON(parse1);  
                
                JSONArray legs = json2.getJSONArray("legs");  
                
                String parse2 = legs.getString(0);
                
                JSONObject json3 = (JSONObject) JSONSerializer.toJSON(parse2);  
                
                String parse3 = json3.getString("distance");
                
                JSONObject json4 = (JSONObject) JSONSerializer.toJSON(parse3);             
                //System.out.println(json4.getString("text"));
                distance=json4.getString("text");
                
            } finally {
                getMethod.releaseConnection();
                
            }
            
        } catch (Exception e) {
             e.printStackTrace();
        }
        return distance;
		
	}

	public double proximitCalc_manual(String from, String to) {
		String[] loc1,loc2;

		String from_cor = getLongLat(from);
		String to_cor = getLongLat(to);

		loc1 = from_cor.split(":");
		loc2 = to_cor.split(":");
		
		return GetDistance(Double.valueOf(loc1[0]) ,Double.valueOf(loc1[1]),Double.valueOf(loc2[0]),Double.valueOf(loc1[1]));
		
	}
	
    private double GetDistance(double lat1, double lon1, double lat2, double lon2)
    {
        //code for Distance in Kilo Meter
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.abs(rad2deg(Math.acos(dist)) * 60 * 1.1515 * 1.609344 * 1000);
        dist = dist * 60 * 1.1515;
        
        // calculate in kilometers
        dist = dist * 1.609344;

        return (dist);
    }
    
    private double deg2rad(double deg) {
    	return (deg * Math.PI / 180.0);
    }

    
    private double rad2deg(double rad) {
    	return (rad * 180.0 / Math.PI);
    }
	
	public void getLoc(Double lat, Double lng) {
		// to be implemented
	}
	
    public String getLongLat(String address) {

        String strLatitude = "nf";
        String strLongtitude ="nf";
        
        try {
        	StringBuilder urlBuilder = new StringBuilder(GEOCODE_REQUEST_URL);
            if (StringUtils.isNotBlank(address)) {
            	urlBuilder.append("&address=").append(URLEncoder.encode(address, "UTF-8"));
            }
            
            final GetMethod getMethod = new GetMethod(urlBuilder.toString());
            try {
            	httpClient.executeMethod(getMethod);
                Reader reader = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());
                
                int data = reader.read();
                char[] buffer = new char[1024];
                Writer writer = new StringWriter();
                while ((data = reader.read(buffer)) != -1) {
                    	writer.write(buffer, 0, data);
               	}

                String result = writer.toString();
                //System.out.println(result.toString());

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db = dbf.newDocumentBuilder();
		        InputSource is = new InputSource();
		        is.setCharacterStream(new StringReader("<"+writer.toString().trim()));
		        Document doc = db.parse(is);
            
                strLatitude = getXpathValue(doc, "//GeocodeResponse/result/geometry/location/lat/text()");
                System.out.println("Latitude:" + strLatitude);
                
                strLongtitude = getXpathValue(doc,"//GeocodeResponse/result/geometry/location/lng/text()");
                System.out.println("Longitude:" + strLongtitude);
                
                
            } finally {
                getMethod.releaseConnection();
                
            }
            
        } catch (Exception e) {
             e.printStackTrace();
        }
        return (strLongtitude+":"+strLatitude).toString();
    }

    private String getXpathValue(Document doc, String strXpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xPath.compile(strXpath);
        String resultData = null;
        Object result4 = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result4;
        for (int i = 0; i < nodes.getLength(); i++) {
        	resultData = nodes.item(i).getNodeValue();
		}
        return resultData;
    }
    
}
