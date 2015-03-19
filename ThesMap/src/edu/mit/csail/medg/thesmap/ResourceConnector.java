/**
 * An (abstract) connector to a resource to be used in performing annotations.
 * For now, we contemplate three, corresponding to the three types of supported
 * annotations:
 *  1. UMLS Lookup
 *  2. MetaMap
 *  3. cTakes
 * but others may be added. 
 */
package edu.mit.csail.medg.thesmap;

/**
 * @author psz
 *
 */
public abstract class ResourceConnector {
	
	String name;
	ResourceConnectorPool<ResourceConnector> pool;
	boolean initialized;
	
	public ResourceConnector(String name) {
		this.name = name;
		initialized = false;
	}
	
//	public void closeAll() {
//		pool.closeAll();
//	}

	public ResourceConnector getResourceConnector() {
		return (ResourceConnector)pool.getNext();
	}
	
	public static void assurePoolSize(int n) {
	}
	
	public abstract void close();
	
}
