package org.geotools.feature.impl;

import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureImpl extends ComplexAttributeImpl implements Feature {
	final FeatureType<?> TYPE;
	
	public FeatureImpl( String id, FeatureType type ){
		super( id, type );
		TYPE = type;
	}
	public FeatureType<?> getType() {
		return TYPE;
	}
	public CoordinateReferenceSystem getCRS() {
		GeometryAttribute att = (GeometryAttribute)get( TYPE.getDefaultGeometry() );
		return att == null? null : att.getCRS();
	}
	public Envelope getBounds() {
		if(TYPE.getDefaultGeometry() == null)
			return null;
		
		Envelope bounds = null;
		for( Attribute attribute : attribtues ){
			if( attribute instanceof GeometryAttribute ){
				Geometry geom = ((GeometryAttribute) attribute).get();
				if(geom != null){
					if(bounds == null)
						bounds = geom.getEnvelopeInternal();
					else
						bounds.expandToInclude( geom.getEnvelopeInternal() );
				}
			}
		}
		return bounds;
	}

	public Geometry getDefaultGeometry() {
		GeometryAttribute att = (GeometryAttribute)get( TYPE.getDefaultGeometry() );
		return att == null? null : att.get();
	}
	 
	public void setDefaultGeometry(Geometry g){
		GeometryAttribute att = (GeometryAttribute)get( TYPE.getDefaultGeometry() );
		if(att == null){
			throw new IllegalArgumentException("Type has no geometry attribute");
		}
		att.set(g);
	}

}
