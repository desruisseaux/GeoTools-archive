package org.opengis.feature.type;

import org.opengis.spatialschema.geometry.Geometry;

public interface GeometryType extends Type {	
	public <T extends Geometry> Class<T> getBinding();	
}