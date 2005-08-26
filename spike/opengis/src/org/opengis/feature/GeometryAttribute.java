package org.opengis.feature;

import org.opengis.feature.type.Type;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * Represent a Geometry as complex content.
 * <p>
 * List of information to make available through Complex api:
 * <ul>
 * <li>srs
 * <li>bounds
 * </ul>
 */
interface GeometryAttribute {
   CoordinateReferenceSystem getCRS();
   Object getBounds();
   /**
    * Type should be configued with a Geometry for getJavaType.
    * <p>
    * If needed a set of well-known GeometryType can be constructed.
    */
   Type getType();
   Geometry get();
   void set( Geometry geom );
}

