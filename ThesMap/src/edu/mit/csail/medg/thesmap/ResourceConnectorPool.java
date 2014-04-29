package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;

public class ResourceConnectorPool<T extends ResourceConnector> {

	ArrayList<T> pool = new ArrayList<T>();
	int next = 0;
	
	public void add(T rc) {
		pool.add(rc);
	}
	
	public T getNext() {
		T ans = null;
		if (pool.size() > 0) {
			ans = pool.get(next);
			next = (next + 1) % pool.size();
		}
		return ans;
	}
	
	public int size() {
		return pool.size();
	}
	
	public void closeAll() {
		for (T connector: pool) {
			connector.close();
		}
	}
}
