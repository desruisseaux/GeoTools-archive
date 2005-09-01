package org.opengis.feature;

import org.opengis.feature.type.GeometryType;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * Represent a Geometry as complex content.
 * <p>
 * List of information to make available through Complex api:
 * <ul>
 * <li>srs
 * <li>bounds
 * </ul>
 * </p>
 * <p>
 * This class is cotten candy and does not add any new ability
 * to our modeling.
 * </p>
 */
public interface GeometryAttribute extends Attribute  {
   // May not be needed if Geometry has this information?
   // CoordinateReferenceSystem getCRS();
   // May not be needed if Geometry has this information?
   // Object getBounds();
   
   /**
    * Type should be configued with a Geometry for getJavaType.
    * <p>
    * Q: If needed a set of well-known GeometryType can be constructed,
    * may be needed to report CRS and Bounds constraints on data?
    * A: It was needed when we switched over to Attribute
    */
   GeometryType getType();
   Geometry get();
   void set( Geometry geom );
}