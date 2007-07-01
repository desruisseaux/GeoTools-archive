package org.geotools.caching.impl;

import org.geotools.caching.CacheEntry;
import org.geotools.caching.spatialindex.spatialindex.INode;

public class NodeCacheEntry implements CacheEntry {
	
	private final INode node ;
	private final Integer key ;
	private int hits ;
	private long creationTime ;
	private long lastAccessTime ;
	
	public NodeCacheEntry(INode node) {
		this.node = node ;
		key = new Integer(node.getIdentifier()) ;
		hits = 0 ;
		creationTime = System.currentTimeMillis() ;
		lastAccessTime = creationTime ;
	}

	public long getCost() {
		// TODO Auto-generated method stub
		return -1;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getExpirationTime() {
		// TODO Auto-generated method stub
		return -1;
	}

	public int getHits() {
		return hits ;
	}

	public long getLastAccessTime() {
		return lastAccessTime ;
	}

	public long getLastUpdateTime() {
		return -1;
	}

	public long getVersion() {
		// TODO Auto-generated method stub
		return -1;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return true ;
	}

	public Object getKey() {
		return key ;
	}

	public Object getValue() {
		return node;
	}

	public Object setValue(Object arg0) {
		throw new UnsupportedOperationException() ;
	}
	
	public void hit() {
		hits++ ;
		lastAccessTime = System.currentTimeMillis() ;
	}

}
