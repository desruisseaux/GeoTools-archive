package org.geotools.gml2;

import java.util.HashMap;

import org.geotools.feature.FeatureType;

public class FeatureTypeCache {

	HashMap map = new HashMap();
	
	public FeatureType get(String name) {
		synchronized (this) {
			return (FeatureType) map.get(name);
		}
	}
	
	public void put(FeatureType type) {
		if (type.getTypeName() == null) {
			throw new IllegalArgumentException("Type name must be non null");
		}
		
		synchronized (this) {
			if (map.get(type.getTypeName()) != null) {
				
				FeatureType other = (FeatureType)map.get(type.getTypeName());
				if (!other.equals(type)) {
					String msg = "Type with same name already exists in cache.";
					throw new IllegalArgumentException(msg);	
				}
				
				return;
			}
			
			map.put(type.getTypeName(),type);
		}
	}
}
