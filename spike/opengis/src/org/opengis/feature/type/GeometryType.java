package org.opengis.feature.type;

import org.opengis.referencing.crs.CoordinateReferenceSystem;


public interface GeometryType extends AttributeType {	
	//public <T extends Geometry> Class<T> getBinding();
	
	/**
	 * The coordinate reference system of the Geometries
	 * contained by attributes of this type.
	 */
	public CoordinateReferenceSystem getCRS();
}