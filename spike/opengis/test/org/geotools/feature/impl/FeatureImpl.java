package org.geotools.feature.impl;

import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.spatialschema.geometry.Geometry;

public class FeatureImpl extends ComplexAttributeImpl implements Feature {
	final FeatureType TYPE;
	
	public FeatureImpl( String id, FeatureType type ){
		super( id, type );
		TYPE = type;
	}
	public FeatureType getType() {
		return TYPE;
	}
	public Object getCRS() {
		return getDefault().getCRS();
	}
	public Object getBounds() {
		Object bounds = null;
		for( Attribute attribute : attribtues ){
			if( attribute instanceof GeometryAttribute ){
				GeometryAttribute geom = (GeometryAttribute) attribute;
				// e.expandToInclude( geom.get().getBounds() );
				bounds = geom.get().getBounds();
			}
		}
		return bounds;
	}
	public Geometry getDefault() {
		return (Geometry) get( TYPE.getDefaultGeometry() );
	}
}
