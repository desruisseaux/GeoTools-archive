package org.geotools.feature;

import java.util.*;
import org.apache.log4j.Category;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;

/** 
 * <p>A GeoTools representation of a simple feature.
 * A flat feature type enforces the following properties:<ul>
 * <li>Attribute types are restricted to Java primitives and Strings. 
 * <li>Attributes may only have one occurrence.
 * <li>Each feature has a single, non-changing geometry attribute.</ul></p>
 *
 * <p>Flat feature types define features types that may be thought of as 'layers' in 
 * traditional GIS parlance.  They are called flat because they do not
 * allow any nested elements, but they also restrict the attribute objects
 * to be very simple data types.</p>
 */
public class FeatureFactory {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(FeatureFactory.class.getName());
    
    FeatureType schema; 

    /**
     * Constructor with geometry.
     *
     * @param geometry The geometry for this feature type.
     */
    public FeatureFactory (FeatureType schema) {
        this.schema = schema;
    }


    /* ********************************************************************************************
     * Handles all attribute interface implementation.                                            *
     * ********************************************************************************************/
    /**
     * Creates a new feature.
     *
     * @return Whether or not this represents a feature type (over a 'flat' attribute).
     */
    public Feature create(Object[] attributes)
        throws IllegalFeatureException {

        if(schema instanceof FeatureTypeFlat) {
            return new FeatureFlat((FeatureTypeFlat) schema, attributes);
        }
        else {
            return null;
        }
    }


}
