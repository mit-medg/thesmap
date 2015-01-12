/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
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
		Document test = null;
		test = lookup("MI");
		if (test != null) {
			U.log("WJL Test \"MI\":" + test);
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
	 * @return a Document containing the HTML interpretation of the text
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public org.w3c.dom.Document lookup(String text) //throws IOException, ParserConfigurationException, SAXException 
	{
		Document doc = null;
		String stringUrl = props.getProperty(ThesProps.wjlUrl);
		URL wjlUrl;
		HttpURLConnection conn = null;
		try {
			wjlUrl = new URL(stringUrl);
			conn = (HttpURLConnection) wjlUrl.openConnection();
			conn.setRequestMethod("POST");
		} catch (IOException e) {
			U.log("***IOException trying to make HTTP connection:");
			U.logException(e);
			return null;
		}
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Accept", "text/*");
		conn.setRequestProperty("Accept-Charset", "UTF-8");
		conn.setAllowUserInteraction(false);
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(conn.getOutputStream());
			out.write("text=" + URLEncoder.encode(text, "UTF-8"));
		} catch (IOException e) {
			U.log("***IOException trying to write to HTTP output stream:");
			U.logException(e);
			return null;
		} 
		String html = null;
		try {
			out.close();
			conn.connect();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				/* I tried the code commented out here, but was getting errors parsing the HTML,
				 * as if some part of its beginning was not properly read.  I switched to using 
				 * readHttpResponse and now those errors are gone.
				 */
//				InputStream stream = conn.getInputStream();	
//				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//				doc = dBuilder.parse(stream);
				html = readHttpResponse(conn);
//				System.out.println("WJL Response: " + html);
			    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			    factory.setValidating(false); // does not avoid malformed xhtml!
			    DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
			    	InputSource is = new InputSource(new StringReader(html));
			    	doc = builder.parse(is);
				} catch (ParserConfigurationException | SAXException e) {
			    	U.logException(e);
				}
			} else {
				U.log("HTTP Error: " + conn.getResponseMessage());
			}
		} catch (IOException e) {
			U.log("\n****************\nError in parsing.\n****************\n\n"+ text + 
					"\n\n<**************\n" + html + "\n\n*************>\n\n");
		}
		return doc;
	}
	
	String readHttpResponse(HttpURLConnection conn) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			U.log("***IOException trying to retrieve HTTP Response: " + e.toString());
			return null;
		}
		StringBuilder sb=new StringBuilder();
		int cp;
		try {
			while((cp=in.read())!=-1) {
			    sb.append((char)cp);
			}
		} catch (IOException e) {
			U.log("***IOException reading from HTTP Response: " + e.toString());
		}
		return (sb.length() == 0) ? null : sb.toString();
	}
}
