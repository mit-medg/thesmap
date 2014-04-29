/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author psz
 *
 */
public class ResourceConnectorWjl extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorWjl> pool = 
			new ResourceConnectorPool<ResourceConnectorWjl>();
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
	
	ThesProps props;
		
	public ResourceConnectorWjl() {
		super("WJL");
		props = ThesMap.prop;
		initialized = false;
		Document test = lookup("MI");
		if (test != null) {
//			U.log("WJL Test \"MI\":" + test);
			initialized = true;
		}
		U.log("ResourceConnectorWjl initialized = " + initialized);
	}
	
	public static ResourceConnectorWjl get() {
		if (broken) return null;
		ResourceConnectorWjl ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorWjl());
			ans = pool.getNext();
		}
		if (!ans.initialized) {
			broken = true;
			ans = null;
		}
		return ans;
	}
	
	public static void assurePoolSize(int n) {
		for (; n < pool.size(); n++) {
			pool.add(new ResourceConnectorWjl());
		}
	}
	
	@Override
	public void close() {
		
	}
	
	/**
	 * Handles the interface to WJL's text analysis program, which runs on a server
	 * and is accessed by a URL defined in props.
	 * @param text The text to be analyzed
	 * @return a String containing the HTML interpretation of the text
	 */
	public org.w3c.dom.Document lookup(String text) {
		org.w3c.dom.Document doc = null;
		try {
			String stringUrl = props.getProperty(ThesProps.wjlUrl);
			URL wjlUrl = new URL(stringUrl);
			HttpURLConnection conn = (HttpURLConnection) wjlUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
	        conn.setRequestProperty("Accept", "text/*");
	        conn.setRequestProperty("Accept-Charset", "UTF-8");
	        conn.setAllowUserInteraction(false);
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write("text=" + URLEncoder.encode(text, "UTF-8"));
			out.close();
			conn.connect();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				InputStream stream = conn.getInputStream();
				doc = dBuilder.parse(stream);
			} else {
				U.log("HTTP Error: " + conn.getResponseMessage());
			}
		} catch(IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			doc = null;
		} 
		return doc;
	}
	
}
