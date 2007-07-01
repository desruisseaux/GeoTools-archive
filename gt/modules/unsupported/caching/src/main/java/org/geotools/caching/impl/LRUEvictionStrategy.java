package org.geotools.caching.impl;

import org.geotools.caching.FeatureCache;

public class LRUEvictionStrategy {
	
	final private FeatureCache cache ;
	
	public LRUEvictionStrategy(FeatureCache cache) {
		this.cache = cache ;
	}

}
