package org.geotools.xml.gml;

import java.util.Collections;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

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
class ChoiceAttributeTypeImpl extends AttributeTypeImpl implements ChoiceAttributeType {

    protected Class[] types;
    private boolean isNillable;
    private int maxOccurs;
    private int minOccurs;
    Object defaultValue;
    
	public ChoiceAttributeTypeImpl(Name name, Class<?>[] types, Class<?> defaultType, boolean nillable, int min, int max,
            Object defaultValue, Filter filter) {
	    super( name, defaultType, false, false, toRestrictions(filter), null, toDescription(types) );
	    if( defaultValue == null && !isNillable ){
	        defaultValue = DataUtilities.defaultValue( defaultType );
	    }
	    this.minOccurs = min;
	    this.maxOccurs = max;
    }

	public Class[] getChoices() {
		return types;
	}
	public Object convert(Object obj) {
		return obj;
	}

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getLocalName() {
        return getName().getLocalPart();
    }

    public AttributeType getType() {
        return this;
    }

    public int getMaxOccurs() {
        return minOccurs;
    }

    public int getMinOccurs() {
        return maxOccurs;
    }

    public boolean isNillable() {
        return isNillable;
    }

    static List<Filter> toRestrictions( Filter filter ){
        if( filter == null ){
            return (List<Filter>)Collections.EMPTY_LIST;
        }
        else {
            return Collections.singletonList( filter );
        }
    }
    static InternationalString toDescription( Class[] bindings ){
        StringBuffer buf = new StringBuffer();
        buf.append("Choice betwee ");
        for( Class bind : bindings ){
            buf.append( bind.getName() );
            buf.append( "," );
        }
        return new SimpleInternationalString( buf.toString() );
    }
}
