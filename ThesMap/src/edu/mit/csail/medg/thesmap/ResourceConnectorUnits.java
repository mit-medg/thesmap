/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeSet;

/**
 * @author psz
 *
 */
public class ResourceConnectorUnits extends ResourceConnector {

//	static ResourceConnectorPool<ResourceConnectorNoise> pool = 
//			new ResourceConnectorPool<ResourceConnectorNoise>();
	static ResourceConnectorUnits singleton = null;
	
	static TreeSet<String> unitsWords = null;
	
	static final String unitsWordsFileName = "UnitsWords.txt";
	
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorUnits() {
		super("UnitsWords");
		if (unitsWords == null) {
			URL inJar = ClassLoader.getSystemClassLoader().getResource(unitsWordsFileName);
			if (inJar != null) {
				try {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(inJar.openStream()));
					unitsWords = new TreeSet<String>();
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						unitsWords.add(inputLine.trim().toLowerCase());
					in.close();
					broken = false;
				} catch (IOException e) {
					unitsWords = null;
				}
			}
			if (unitsWords == null) {
				System.err.println("Unable to find and load unitsWords.txt file;\n" +
						"It should be in jar.");
				initialized = false;
				broken = true;
			} else initialized = true;
		}
	}
	
	public static ResourceConnectorUnits get() {
		if (broken) return null;
		if (singleton != null) return singleton;
		singleton = new ResourceConnectorUnits();
		if (singleton.initialized) return singleton;
		broken = true;
		return null;
	}
	
	public static void assurePoolSize(int n) {
		// We need only one, so ignore this call.
	}
	
	@Override
	public void close() {
		
	}
	
	public boolean lookup(String word) {
		if (word == null) return false;
		return unitsWords.contains(word.toLowerCase());
	}
	
}
