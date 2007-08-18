package org.geotools.feature;


import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.Utilities;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * TODO: rename to GeometricAttribute Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * <p>
 * </p>
 * <p>
 * Example Use:
 * 
 * <pre><code>
 *         GeometryAttributeType x = new GeometryAttributeType( ... );
 *         TODO code example
 * </code></pre>
 * 
 * </p>
 * 
 * @author Leprosy
 * @since 0.3 TODO: test wkt geometry parse.
 */
public class GeometryAttributeImpl extends AttributeImpl implements
		GeometryAttribute {
	/** CoordianteSystem used by this GeometryAttributeType */
	protected CoordinateReferenceSystem crs;
	private BoundingBox bounds;

	public GeometryAttributeImpl(
		Object content, AttributeDescriptor descriptor, String id, 
		CoordinateReferenceSystem crs
	) {
		super(content, descriptor, id);
		this.crs = crs;
		
		if (!(descriptor.getType() instanceof GeometryType)) {
			throw new IllegalArgumentException("Expected GeometryType, got "
					+ descriptor);
		}
	}

	public CoordinateReferenceSystem getCRS() {
		return crs;
	}
    
    public void setCRS(CoordinateReferenceSystem coordinateSystem) {
        this.crs = coordinateSystem;
    }
    

	/*
	 * public GeometryFactory getGeometryFactory() { return geometryFactory; }
	 */

//	protected Object parse(Object value) throws IllegalArgumentException {
//		if (value == null) {
//			return value;
//		}
//
//		if (value instanceof Geometry) {
//			return value;
//		}
//
//		if (value instanceof String) {
//			String wkt = (String) value;
//			WKTReader reader = new WKTReader();
//			try {
//				return reader.read(wkt);
//			} catch (com.vividsolutions.jts.io.ParseException pe) {
//				throw new IllegalArgumentException("Could not parse the "
//						+ "string: " + wkt + " to well known text");
//			}
//		}
//		// consider wkb/gml support?
//		throw new IllegalArgumentException(getClass().getName()
//				+ " cannot parse " + value);
//	}

	public Object /*Geometry*/ getValue() {
		return (Geometry) super.getValue();
	}

	public void setValue(Geometry geometry) {
		super.setValue(geometry);
	}

	/**
	 * Set the bounds for the contained geometry.
	 */
	public synchronized void setBounds( BoundingBox bbox ){
		bounds = bbox;
	}
	
	/**
	 * Returns the non null envelope of this attribute. If the attribute's
	 * geometry is <code>null</code> the returned Envelope
	 * <code>isNull()</code> is true.
	 * 
	 * @return 
	 */
	public synchronized BoundingBox getBounds() {
		if( bounds == null ){
			ReferencedEnvelope bbox = new ReferencedEnvelope(crs);
			Geometry geom = (Geometry) getValue();
			if (geom != null) {
				bbox.expandToInclude(geom.getEnvelopeInternal());
			}
			bounds =  bbox;
		}
		return bounds;
	}

	public boolean equals(Object o) {
		if (!(o instanceof GeometryAttributeImpl)) {
			return false;
		}
		GeometryAttributeImpl att = (GeometryAttributeImpl) o;

		//JD: since Geometry does not implement equals(Object) "properly",( ie
		// if you dont call equals(Geomtery) two geometries which are equal 
		// will not be equal) we dont call super.equals()
		//if ( !super.equals( att ) ) {
		//	return false;
		//}
		if (!Utilities.equals(DESCRIPTOR, att.DESCRIPTOR))
			return false;

		if (!Utilities.equals(TYPE, att.TYPE))
			return false;

		if (!Utilities.equals(ID, att.ID))
			return false;

		//if (!Utilities.equals(content, att.content))
		//	return false;	
		if ( content != null && att.content != null ) {
			if ( !((Geometry)content).equals((Geometry)att.content)) {
				return false;
			}
		}
		else {
			return Utilities.equals(content, this.content);
		}
		
		return Utilities.equals( getBounds(), att.getBounds() ) && 
			Utilities.equals( crs, att.crs );
		
	}
	
	public int hashCode() {
		int hash = super.hashCode(); 
		
		if ( bounds != null ) {
			hash ^= bounds.hashCode();
		}
		
		if ( crs != null ) {
			hash ^= crs.hashCode();
		}
		
		return hash;
	}
}
