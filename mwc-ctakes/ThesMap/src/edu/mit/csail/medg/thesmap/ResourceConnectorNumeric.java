/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @author psz
 *
 */
public class ResourceConnectorNumeric extends ResourceConnector {

//	static ResourceConnectorPool<ResourceConnectorNoise> pool = 
//			new ResourceConnectorPool<ResourceConnectorNoise>();
	static ResourceConnectorNumeric singleton = null;
	
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorNumeric() {
		super("Numeric");
	}
	
	public static ResourceConnectorNumeric get() {
		if (broken) return null;
		if (singleton != null) return singleton;
		singleton = new ResourceConnectorNumeric();
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
	
}
