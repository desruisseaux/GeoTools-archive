package org.geotools.feature.type;

import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Geometry;

public class GeometryTypeImpl extends AttributeTypeImpl implements GeometryType {

	/**
	 * CoordianteSystem used by this GeometryAttributeType
	 * NOT used yet, needs to incorporate the functionality from the old
	 * GeometricAttributeType
	 */
	private CoordinateReferenceSystem CRS;
	
	public <T extends Geometry> GeometryTypeImpl(QName name, Class/*<T>*/ binding, boolean identified,
			boolean nillable, Set<Filter>restrictions, AttributeType superType, boolean isAbstract,
			CoordinateReferenceSystem crs){
		super(name, binding, identified, nillable, restrictions, superType, isAbstract);
		CRS = crs;
	}
}
