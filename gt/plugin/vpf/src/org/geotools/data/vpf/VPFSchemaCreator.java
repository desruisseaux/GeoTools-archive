/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.data.vpf;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import java.util.HashMap;
import java.util.Vector;

import org.geotools.data.DataSourceException;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;

/*
 * VPFSchemaCreator.java
 *
 * Created on 13. april 2004, 14:26
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFSchemaCreator {
    private static HashMap featureTypes = startSchema();

    public static FeatureType getSchema(String featuretype)
                                 throws DataSourceException {
        try {
            /* Should build a hashmap of the attributes which has been read from
             * the featuretypeconfiguration
             *
             * Or should it be  built upon the header files of the featuretypes?
             * This is probably the best solution
             */
            /*
            AttributeType attributes[] = null;
            if ( featuretype.equals( "roadl") ) {
                attributes = new AttributeType[12];
                attributes[0] = AttributeTypeFactory.newAttributeType( "START_NODE", Integer.class );
                attributes[1] = AttributeTypeFactory.newAttributeType( "END_NODE", Integer.class );
                attributes[2] = AttributeTypeFactory.newAttributeType( "RIGHT_EDGE", String.class );
                attributes[3] = AttributeTypeFactory.newAttributeType( "LEFT_EDGE", String.class );
                attributes[4] = AttributeTypeFactory.newAttributeType( "COORDINATES", LineString.class  );
                attributes[5] = AttributeTypeFactory.newAttributeType( "FACC_FEATURE_CODE", String.class );
                attributes[6] = AttributeTypeFactory.newAttributeType( "ACCURACY_CATEGORY", Integer.class );            
                attributes[7] = AttributeTypeFactory.newAttributeType( "EXISTENCE_CATEGORY", Integer.class );
                attributes[8] = AttributeTypeFactory.newAttributeType( "MEDIAN_CATEGORY", Integer.class );
                attributes[9] = AttributeTypeFactory.newAttributeType( "TILE_REFERENCE_ID", Integer.class );
                attributes[10] = AttributeTypeFactory.newAttributeType( "EDGE_PRIMITIVE_ID", Integer.class );
                attributes[11] = AttributeTypeFactory.newAttributeType( "ROUTE_INTENDED_USE", Integer.class );                                        
            } else if ( featuretype.equals( "landicea")) {
                attributes = new AttributeType[2];
                attributes[0] = AttributeTypeFactory.newAttributeType( "FEATURE_NUMBER", Integer.class );
                attributes[1] = AttributeTypeFactory.newAttributeType( "COORDINATES", Polygon.class  );
            } else if ( featuretype.equals( "builtupa")) {
                attributes = new AttributeType[2];
                attributes[0] = AttributeTypeFactory.newAttributeType( "FEATURE_NUMBER", Integer.class );
                attributes[1] = AttributeTypeFactory.newAttributeType( "COORDINATES", Polygon.class  );
            } else if ( featuretype.equals( "grassa")) {
                attributes = new AttributeType[2];
                attributes[0] = AttributeTypeFactory.newAttributeType( "FEATURE_NUMBER", Integer.class );
                attributes[1] = AttributeTypeFactory.newAttributeType( "COORDINATES", Polygon.class  );
            } else if ( featuretype.equals( "edgetype") )  {
                attributes = new AttributeType[1];
                attributes[0] = AttributeTypeFactory.newAttributeType( "COORDINATES", LineString.class  );
            } 
             */
            Object type = featureTypes.get(featuretype);

            if (type == null) {
                throw new SchemaException("Schema not found");
            }

            return (FeatureType) type;
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException(featuretype + 
                                          " schema not available", e);
        }
    }

    public static String[] getTypeNames() {
        Vector v = new Vector(featureTypes.keySet());
        String[] tmp = new String[v.size()];

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (String) v.elementAt(i);
        }

        return tmp;
    }

    private static HashMap startSchema() {
        HashMap tmp = new HashMap();

        try {
            AttributeType[] attributes = new AttributeType[1];
            attributes[0] = AttributeTypeFactory.newAttributeType("COORDINATES", 
                                                                  LineString.class);
            tmp.put("edgetype", 
                    FeatureTypeFactory.newFeatureType(attributes, "edgetype"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp;
    }

    public static void addSchema(FeatureType type, String featurename) {
        featureTypes.put(featurename, type);
    }
}