package org.geotools.feature.impl;

import java.util.List;

import org.geotools.feature.Descriptors;
import org.geotools.feature.Types;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureImpl extends ComplexAttributeImpl implements Feature {

	public FeatureImpl(String id, FeatureType type) {
		super(id, type);
		// super takes care of id since type is always
		// identified
	}

	public FeatureType<?> getType() {
		return (FeatureType<?>) super.TYPE;
	}

	/**
	 * Returns the Coordinate Reference System of this Feature instance, based
	 * on the CRS of the feature's default geometry.
	 * <p>
	 * This CRS can be actually obtained from two different sources: the
	 * GeometryType has the default CRS of geometries for that type, though that
	 * CRS can be 'overriden' by the actual Geometry instance, in which case,
	 * the Geometry should hold it on the Geometry metadata. Think of this as a
	 * transition state until we adopt GeoAPI geometries.
	 * </p>
	 * <p>
	 * So, if the information is available through the Geometry instance, it
	 * takes precedence over the CRS declared on the default geometry type.
	 * </p>
	 * 
	 * @retrun the CRS of this Feature's default geometry, or null.
	 */
	public CoordinateReferenceSystem getCRS() {
		CoordinateReferenceSystem crs = null;
		GeometryType defaultGeomType = getType().getDefaultGeometry();
		if (defaultGeomType != null) {
			// use the value of the Attribute, if found. Else
			// the one of the default type
			Object instanceMetadata = getDefaultGeometry().getUserData();
			if (instanceMetadata instanceof CoordinateReferenceSystem) {
				crs = (CoordinateReferenceSystem) instanceMetadata;
			} else {
				crs = defaultGeomType.getCRS();
			}
		}
		return crs;
	}

	public Envelope getBounds() {
		if (getType().getDefaultGeometry() == null)
			return null;

		Envelope bounds = null;
		for (Attribute attribute : attribtues) {
			if (attribute instanceof GeometryAttribute) {
				Geometry geom = ((GeometryAttribute) attribute).get();
				if (geom != null) {
					if (bounds == null)
						bounds = geom.getEnvelopeInternal();
					else
						bounds.expandToInclude(geom.getEnvelopeInternal());
				}
			}
		}
		return bounds;
	}

	public Geometry getDefaultGeometry() {
		GeometryType geometryType = getType().getDefaultGeometry();
		Geometry geom = geometryType == null ? null
				: (Geometry) get(geometryType);
		return geom;
	}

	public void setDefaultGeometry(Geometry g) {
		GeometryType geometryType = getType().getDefaultGeometry();
		if(geometryType == null){
			throw new IllegalArgumentException("FeatureType has no default geometry attribute");
		}
		List<Attribute>geoms = getAttributes(geometryType.getName());
		if(geoms.size() > 0){
			Attribute att = geoms.get(0);
			att.set(g);
		}
	}

}
