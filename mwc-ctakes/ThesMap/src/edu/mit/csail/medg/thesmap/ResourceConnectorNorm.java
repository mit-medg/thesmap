/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.util.Vector;

import gov.nih.nlm.nls.lvg.Api.NormApi;
/**
 * @author psz
 *
 */
public class ResourceConnectorNorm extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorNorm> pool = 
			new ResourceConnectorPool<ResourceConnectorNorm>();
	static boolean broken = false;
	
	public NormApi norm = null;
	
	public ResourceConnectorNorm() {
		super("Norm");
		ThesProps prop = ThesMap.prop;
		String normYear = prop.getProperty(ThesProps.normYearName);
		String lvgHome = prop.getProperty(ThesProps.normLvgHomeName);
		String lvgProps = prop.getProperty(ThesProps.normLvgPropsName);
		norm = new NormApi(lvgHome + normYear + lvgProps);
		
		initialized = true;
	}
	
	public static ResourceConnectorNorm get() {
		if (broken) return null;
		ResourceConnectorNorm ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorNorm());
			ans = pool.getNext();
			if (!ans.initialized) {
				broken = true;
				ans = null;
			}
		}
		return ans;
	}
	
	public static void assurePoolSize(int n) {
		for (; n < pool.size(); n++) {
			pool.add(new ResourceConnectorNorm());
		}
	}
	
	public void close() {
		norm.CleanUp();
	}
	
	/**
	 * Converts an unnormalized phrase to a Vector of normalized ones, if any.
	 *  
	 * @param unnormalizedPhrase
	 * @return the normalized phrases, or null if none
	 */
	public Vector<String> lookup(String unnormalizedPhrase) {
		Vector<String> output = null;
		try {
			output = norm.Mutate(unnormalizedPhrase);
		}
		catch (Exception e) {}
		return (output == null || output.size() < 1) ? null: output;
	}

	
}
