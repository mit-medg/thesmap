/**
 * 
 */
package edu.mit.csail.medg.thesmap;

/**
 * @author psz
 *
 */
/**
 * @author psz
 *
 */
public class ResourceConnectorcTakes extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorcTakes> pool = 
			new ResourceConnectorPool<ResourceConnectorcTakes>();
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorcTakes() {
		super("cTakes");
		
		initialized = false;
	}
	
	public static ResourceConnectorcTakes get() {
		if (broken) return null;
		ResourceConnectorcTakes ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorcTakes());
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
			pool.add(new ResourceConnectorcTakes());
		}
	}
	
	@Override
	public void close() {
		
	}
	
}
