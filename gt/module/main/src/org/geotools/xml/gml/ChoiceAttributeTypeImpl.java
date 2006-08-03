package org.geotools.xml.gml;

import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Created for GML generated FeatureTypes.  Represents a Choice type.  
 *  
 *  
 * This is temporary and only for use by the parser.  It should never be public or in common use.
 * 
 * @author Jesse
 */
class ChoiceAttributeTypeImpl extends DefaultAttributeType implements ChoiceAttributeType {

    private Class[] types;

	public ChoiceAttributeTypeImpl(String name, Class[] types, Class defaultType, boolean nillable, int min, int max,
            Object defaultValue, Filter f) {
           super(name,defaultType,nillable,min,max,defaultValue, f);
           this.types=types;
    }

	public Class[] getChoices() {
		return types;
	}
	public Object convert(Object obj) {
		return obj;
	}
	
	static class Geometry extends GeometricAttributeType implements ChoiceAttributeType.Geometry{

		private Class[] types;

		public Geometry(String name, Class[] types, Class defaultType, boolean nillable, 
				int min, int max, Object defaultValue, 
				CoordinateReferenceSystem cs, Filter filter) {
			super(name, defaultType, nillable, min, max, defaultValue, cs, filter);
			this.types=types;
		}

		public Class[] getChoices() {
			return types;
		}
		
		public Object convert(Object obj) {
			GeometryFactory fac=new GeometryFactory();
			if (getType()==MultiPolygon.class && obj instanceof Polygon ){
				return fac.createMultiPolygon(new Polygon[]{(Polygon) obj});
			}
			if (getType()==MultiPoint.class && obj instanceof Point ){
				return fac.createMultiPoint(new Point[]{(Point) obj});
			}
			if (getType()==MultiLineString.class && obj instanceof LineString ){
				return fac.createMultiLineString(new LineString[]{(LineString) obj});
			}
			if (getType()==GeometryCollection.class && obj instanceof Geometry ){
				return fac.createGeometryCollection(new com.vividsolutions.jts.geom.Geometry[]{(com.vividsolutions.jts.geom.Geometry) obj});
			}
			
			return obj;
		}
		
	}


}
