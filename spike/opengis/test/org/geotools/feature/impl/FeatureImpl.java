package org.geotools.feature.impl;

import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureImpl extends ComplexAttributeImpl implements Feature {
	final FeatureType TYPE;
	
	public FeatureImpl( String id, FeatureType type ){
		super( id, type );
		TYPE = type;
	}
	public FeatureType getType() {
		return TYPE;
	}
	public CoordinateReferenceSystem getCRS() {
		GeometryAttribute att = getDefaultGeometry();
		return att == null? null : att.getCRS();
	}
	public Envelope getBounds() {
		Envelope bounds = new Envelope();
		for( Attribute attribute : attribtues ){
			if( attribute instanceof GeometryAttribute ){
				GeometryAttribute geom = (GeometryAttribute) attribute;
				bounds.expandToInclude( geom.get().getEnvelopeInternal() );
			}
		}
		return bounds;
	}
	public GeometryAttribute getDefaultGeometry() {
		return (GeometryAttribute) get( TYPE.getDefaultGeometry() );
	}
	public Geometry defaultGeometry() {
		GeometryAttribute att = getDefaultGeometry();
		return att == null? null : att.get();
	}
	 
	public void setDefault(Geometry g){
		throw new UnsupportedOperationException("implement!");
	}

}
