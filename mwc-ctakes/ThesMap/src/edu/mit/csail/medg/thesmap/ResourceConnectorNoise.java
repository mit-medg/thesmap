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
public class ResourceConnectorNoise extends ResourceConnector {

//	static ResourceConnectorPool<ResourceConnectorNoise> pool = 
//			new ResourceConnectorPool<ResourceConnectorNoise>();
	static ResourceConnectorNoise singleton = null;
	
	static TreeSet<String> noiseWords = null;
	
	static final String noiseWordsFileName = "NoiseWords.txt";
	
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorNoise() {
		super("NoiseWords");
		if (noiseWords == null) {
			URL inJar = ClassLoader.getSystemClassLoader().getResource(noiseWordsFileName);
			if (inJar != null) {
				try {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(inJar.openStream()));
					noiseWords = new TreeSet<String>();
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						noiseWords.add(inputLine.trim());
					in.close();
				} catch (IOException e) {
					noiseWords = null;
				}
			}
			if (noiseWords == null) {
				System.err.println("Unable to find and load NoiseWords.txt file;\n" +
						"It should be in jar.");
				initialized = false;
				broken = true;
			} else initialized = true;
		}
	}
	
	public static ResourceConnectorNoise get() {
		if (broken) return null;
		if (singleton != null) return singleton;
		singleton = new ResourceConnectorNoise();
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
		return noiseWords.contains(word);
	}
	
}
