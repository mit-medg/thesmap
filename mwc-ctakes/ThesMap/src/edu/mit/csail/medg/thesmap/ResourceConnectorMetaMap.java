/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;
import java.util.List;

import se.sics.prologbeans.PrologSession;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Result;

/**
 * @author psz
 *
 */
public class ResourceConnectorMetaMap extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorMetaMap> pool = 
			new ResourceConnectorPool<ResourceConnectorMetaMap>();
	static boolean broken = false;
	
	public MetaMapApi mm = null;
	
	public ResourceConnectorMetaMap() {
		super("MetaMap");
		ThesProps prop = ThesMap.prop;
		String metaMapServer = prop.getProperty(ThesProps.serverProp, MetaMapApi.DEFAULT_SERVER_HOST);
		String metaMapPort = prop.getProperty(ThesProps.portProp);
		int mmPort = MetaMapApi.DEFAULT_SERVER_PORT;
		if (metaMapPort != null) mmPort = Integer.parseInt(metaMapPort);
		String metaMapTimeout = prop.getProperty(ThesProps.timeoutProp);
		int mmTimeout = MetaMapApi.DEFAULT_TIMEOUT;
		if (metaMapTimeout != null) mmTimeout = Integer.parseInt(metaMapTimeout);
//		U.log("Metamap server: " + metaMapServer + "; port: " + mmPort + "; timeout: " + mmTimeout);
		
		try{
			mm = new MetaMapApiImpl(metaMapServer, mmPort, mmTimeout);
			PrologSession sess = mm.getSession();
			sess.connect();
			boolean connd = sess.isConnected();
			if (connd) {
				List<String> metaMapOptions = new ArrayList<String>();
				metaMapOptions.add("--blanklines");
				metaMapOptions.add(defeatBlanklinesSplit);
				metaMapOptions.add("--negex");
				if (metaMapOptions.size() > 0) mm.setOptions(metaMapOptions);
				initialized = true;
			}
		}
		catch (Exception e) {
			U.pe("Unable to connect to MetaMap on " + metaMapServer + ":" + mmPort
					+ ";\n   " + e.getMessage());
//			e.printStackTrace(System.err);
			initialized = false;
		}
		if (initialized) pool.add(this);
	}
	
	public static ResourceConnectorMetaMap get() {
		if (broken) return null;
		ResourceConnectorMetaMap ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorMetaMap());
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
			pool.add(new ResourceConnectorMetaMap());
		}
	}
	
	public void close() {
		mm.disconnect();
	}
	
	static final String defeatBlanklinesSplit = "1000000";	// will become 0 in a future version of MetaMap

	public List<Result> process(String text) {
		if (broken || mm == null) {
			U.pe("Cannot process by MetaMap because there is no connection.");
			return null;
		}
		return mm.processCitationsFromString(text);
	}
	
}
