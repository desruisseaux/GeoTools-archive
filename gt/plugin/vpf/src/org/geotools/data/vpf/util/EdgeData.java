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

package org.geotools.data.vpf.util;

import com.vividsolutions.jts.geom.*;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.geotools.data.vpf.io.RowField;
import org.geotools.data.vpf.io.TripletId;

import org.geotools.feature.FeatureType;

/*
 * EdgeData.java
 *
 * Created on 6. april 2004, 14:54
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class EdgeData extends HashMap {
    public Object put(Object key, Object value) {
        if (key instanceof String) {
            GeometryFactory geofactory = new GeometryFactory();
            String key_s = (String) key;

            if (key_s.equals("coordinates")) {
                StringBuffer sb = new StringBuffer();
                StringTokenizer st = new StringTokenizer((String) value, "()");
                Coordinate[] c = new Coordinate[st.countTokens()];
                int i = 0;

                while (st.hasMoreTokens()) {
                    StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
                    c[i] = new Coordinate(Double.parseDouble(st2.nextToken()), 
                                          Double.parseDouble(st2.nextToken()));
                    i++;
                }

                //this.COORDINATES = (LineString) type.getAttributeType( 0 ).parse( geofactory.createLineString( c ) );
                return super.put(key_s, geofactory.createLineString(c));
            } else if (key_s.equals("right_face") || 
                           key_s.equals("left_face") || 
                           key_s.equals("right_edge") || 
                           key_s.equals("left_edge")) {
                if (value != null) {
                    Object tmp = ((RowField) value).getValue();

                    if (tmp instanceof TripletId) {
                        return super.put(key_s, (TripletId) tmp );
                    } else if ( tmp instanceof Integer ) {
                        return super.put(key_s, ((Integer) tmp) );
                    } else {
                        System.out.println( "DYNGE I TRIPLETGENERERING!!!" );
                    }
                } else {
                    return super.put(key_s, null );
                }
            }
        }

        return super.put(key, value);
    }
}