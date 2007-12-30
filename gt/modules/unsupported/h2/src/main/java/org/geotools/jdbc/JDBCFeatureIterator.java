/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.opengis.feature.Association;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.util.Converters;


/**
 * Iterator for read only access to a dataset.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class JDBCFeatureIterator extends JDBCFeatureIteratorSupport {
    /**
     * flag indicating if the iterator has another feature
     */
    Boolean next;

    /**
     * geometry factory used to create geometry objects
     */
    GeometryFactory geometryFactory;

    /**
     * feature builder
     */
    SimpleFeatureBuilder builder;

    public JDBCFeatureIterator(Statement st, SimpleFeatureType featureType,
        JDBCFeatureCollection collection) {
        super(st, featureType, collection);

        try {
            rs.beforeFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // set a geometry factory, use the hints on the collection first
        geometryFactory = (GeometryFactory) collection.getHints().get(Hints.JTS_GEOMETRY_FACTORY);

        if (geometryFactory == null) {
            // look for a coordinate sequence factory
            CoordinateSequenceFactory csFactory = (CoordinateSequenceFactory) collection.getHints()
                                                                                        .get(Hints.JTS_COORDINATE_SEQUENCE_FACTORY);

            if (csFactory != null) {
                geometryFactory = new GeometryFactory(csFactory);
            }
        }

        if (geometryFactory == null) {
            // use the datastore provided one
            geometryFactory = dataStore.getGeometryFactory();
        }

        builder = new SimpleFeatureBuilder(featureType);
    }

    public boolean hasNext() {
        if (next == null) {
            try {
                next = Boolean.valueOf(rs.next());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return next.booleanValue();
    }

    public SimpleFeature next() throws NoSuchElementException {
        if (next == null) {
            throw new IllegalStateException("Must call hasNext before calling next");
        }

        // find the primary key
        PrimaryKey pkey;

        try {
            pkey = dataStore.getPrimaryKey(featureType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // figure out the fid
        String fid;

        try {
            fid = pkey.encode(rs);

            // wrap the fid in the type name
            fid = featureType.getTypeName() + "." + fid;
        } catch (Exception e) {
            throw new RuntimeException("Could not determine fid from primary key", e);
        }

        // check for the association traversal depth hint, if not > 0 dont
        // resolve the associated feature or geometry
        Integer depth = (Integer) collection.getHints().get(Hints.ASSOCIATION_TRAVERSAL_DEPTH);

        if (depth == null) {
            depth = new Integer(0);
        }

        PropertyName associationPropertyName = (PropertyName) collection.getHints()
                                                                        .get(Hints.ASSOCIATION_PROPERTY);

        // round up attributes
        // List attributes = new ArrayList();
        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor type = featureType.getAttribute(i);

            //figure out if any referenced attributes should be resolved
            boolean resolve = depth.intValue() > 0;

            if (resolve && (associationPropertyName != null)) {
                AttributeDescriptor associationProperty = (AttributeDescriptor) associationPropertyName
                    .evaluate(featureType);
                resolve = (associationProperty != null)
                    && associationProperty.getLocalName().equals(type.getLocalName());
            }

            try {
                Object value = rs.getObject(type.getLocalName());

                // is this a geometry?
                if (type instanceof GeometryDescriptor) {
                    GeometryDescriptor gatt = (GeometryDescriptor) type;

                    if (value != null) {
                        try {
                            value = dataStore.getSQLDialect()
                                             .decodeGeometryValue(gatt, rs, type.getLocalName(),
                                    geometryFactory);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // check case where this is an associated geometry
                        if (dataStore.isAssociations()) {
                            try {
                                dataStore.ensureAssociationTablesExist(st.getConnection());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            String sql = dataStore.selectGeometryAssociationSQL(fid, null,
                                    gatt.getLocalName());
                            dataStore.LOGGER.fine(sql);

                            Statement select = st.getConnection().createStatement();

                            try {
                                ResultSet gas = select.executeQuery(sql.toString());

                                try {
                                    if (gas.next()) {
                                        String gid = gas.getString("gid");
                                        boolean ref = gas.getBoolean("ref");

                                        Geometry g = null;

                                        // if this is a "referenced" geometry,
                                        // do not
                                        // read it if the depth is <= 0
                                        if (ref && !resolve) {
                                            // use a stub
                                            g = geometryFactory.createPoint(new CoordinateArraySequence(
                                                        new Coordinate[] {  }));
                                            //g = new NullGeometry();
                                            setGmlProperties(g, gid, null, null);
                                        } else {
                                            // read the geometry
                                            sql = dataStore.selectGeometrySQL(gid);
                                            dataStore.LOGGER.fine(sql);

                                            ResultSet grs = select.executeQuery(sql);

                                            try {
                                                // should always be one
                                                if (!grs.next()) {
                                                    throw new SQLException("no entry for: " + gid
                                                        + " in " + JDBCDataStore.GEOMETRY_TABLE);
                                                }

                                                String name = grs.getString("name");
                                                String desc = grs.getString("description");

                                                if (grs.getObject("geometry") != null) {
                                                    //read the geometry
                                                    g = dataStore.getSQLDialect()
                                                                 .decodeGeometryValue(gatt, grs,
                                                            "geometry", geometryFactory);
                                                } else {
                                                    //multi geometry?
                                                    String gtype = grs.getString("type");

                                                    if ("MULTIPOINT".equals(gtype)
                                                            || "MULTILINESTRING".equals(gtype)
                                                            || "MULTIPOLYGON".equals(gtype)) {
                                                        sql = dataStore.selectMultiGeometrySQL(gid);
                                                        dataStore.LOGGER.fine(sql);

                                                        ResultSet mg = select.executeQuery(sql);

                                                        try {
                                                            ArrayList members = new ArrayList();

                                                            while (mg.next()) {
                                                                String mgid = mg.getString("mgid");
                                                                sql = dataStore.selectGeometrySQL(mgid);
                                                                dataStore.LOGGER.fine(sql);

                                                                Statement select2 = st.getConnection()
                                                                                      .createStatement();
                                                                ResultSet mgg = select2.executeQuery(sql);

                                                                try {
                                                                    mgg.next();

                                                                    String mname = mgg.getString(
                                                                            "name");
                                                                    String mdesc = mgg.getString(
                                                                            "description");

                                                                    Geometry member = dataStore.getSQLDialect()
                                                                                               .decodeGeometryValue(gatt,
                                                                            mgg, "geometry",
                                                                            geometryFactory);

                                                                    setGmlProperties(member, mgid,
                                                                        mname, mdesc);
                                                                    members.add(member);
                                                                } finally {
                                                                    JDBCDataStore.closeSafe(mgg);
                                                                    JDBCDataStore.closeSafe(select2);
                                                                }
                                                            }

                                                            if ("MULTIPOINT".equals(gtype)) {
                                                                g = geometryFactory.createMultiPoint((Point[]) members
                                                                        .toArray(new Point[members
                                                                            .size()]));
                                                            } else if ("MULTILINESTRING".equals(
                                                                        gtype)) {
                                                                g = geometryFactory
                                                                    .createMultiLineString((LineString[]) members
                                                                        .toArray(new LineString[members
                                                                            .size()]));
                                                            } else if ("MULTIPOLYGON".equals(gtype)) {
                                                                g = geometryFactory
                                                                    .createMultiPolygon((Polygon[]) members
                                                                        .toArray(new Polygon[members
                                                                            .size()]));
                                                            } else {
                                                                g = geometryFactory
                                                                    .createGeometryCollection((Geometry[]) members
                                                                        .toArray(new Geometry[members
                                                                            .size()]));
                                                            }
                                                        } finally {
                                                            JDBCDataStore.closeSafe(mg);
                                                        }
                                                    }
                                                }

                                                setGmlProperties(g, gid, name, desc);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            } finally {
                                                JDBCDataStore.closeSafe(grs);
                                            }
                                        }

                                        value = g;
                                    }
                                } finally {
                                    JDBCDataStore.closeSafe(gas);
                                }
                            } finally {
                                JDBCDataStore.closeSafe(select);
                            }
                        }
                    }
                }

                // is this an association?
                if (collection.getDataStore().isAssociations()
                        && Association.class.equals(type.getType().getBinding()) && (value != null)) {
                    Statement select = st.getConnection().createStatement();

                    try {
                        String sql = dataStore.selectAssociationSQL(fid);
                        JDBCDataStore.LOGGER.fine(sql);

                        ResultSet associations = select.executeQuery(sql);

                        try {
                            if (associations.next()) {
                                String rtable = associations.getString("rtable");
                                String rfid = associations.getString("rfid");

                                SimpleFeatureType associatedType = null;

                                try {
                                    associatedType = dataStore.getSchema(rtable);
                                } catch (IOException e) {
                                    //only log here, this means that the association
                                    // is probably bad... which we still want to 
                                    // handle, and fail only when and if we actually
                                    // resolve the link
                                    String msg = "Could not load schema: " + rtable;
                                    JDBCDataStore.LOGGER.log(Level.WARNING, msg, e);
                                }

                                // set the referenced id + typeName as user data
                                builder.userData("gml:id", rfid);
                                builder.userData("gml:featureTypeName", rtable);

                                FeatureTypeFactory tf = dataStore.getFeatureTypeFactory();

                                if (associatedType == null) {
                                    //means there was a problem with the link, 
                                    // create a dummy type
                                    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder(tf);
                                    tb.setName(rtable);
                                    associatedType = tb.buildFeatureType();
                                }

                                //create an association
                                AssociationType associationType = tf.createAssociationType(type
                                        .getName(), associatedType, false, Collections.EMPTY_LIST,
                                        null, null);
                                AssociationDescriptor associationDescriptor = tf
                                    .createAssociationDescriptor(associationType, type.getName(),
                                        1, 1, true);

                                FeatureFactory f = dataStore.getFeatureFactory();
                                Association association = f.createAssociation(null,
                                        associationDescriptor);
                                association.getUserData().put("gml:id", rfid);

                                if (resolve) {
                                    // use the value as an the identifier in a query against
                                    // the
                                    // referenced type
                                    DefaultQuery query = new DefaultQuery(rtable);

                                    Hints hints = new Hints(Hints.ASSOCIATION_TRAVERSAL_DEPTH,
                                            new Integer(depth.intValue() - 1));
                                    query.setHints(hints);

                                    FilterFactory ff = collection.getDataStore().getFilterFactory();
                                    Id filter = ff.id(Collections.singleton(ff.featureId(
                                                    value.toString())));
                                    query.setFilter(filter);

                                    try {
                                        // grab a reader and get the feature, there should
                                        // only
                                        // be one
                                        FeatureReader r = collection.getDataStore()
                                                                    .getFeatureReader(query,
                                                collection.getState().getTransaction());

                                        try {
                                            r.hasNext();

                                            SimpleFeature associated = r.next();
                                            association.setValue(associated);
                                        } finally {
                                            r.close();
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                // set the actual value to be the association
                                value = association;
                            }
                        } finally {
                            JDBCDataStore.closeSafe(associations);
                        }
                    } finally {
                        JDBCDataStore.closeSafe(select);
                    }
                }

                // if the value is not of the type of the binding, try to
                // convert
                Class binding = type.getType().getBinding();

                if ((value != null) && !(type.getType().getBinding().isAssignableFrom(binding))) {
                    if (JDBCDataStore.LOGGER.isLoggable(Level.FINER)) {
                        String msg = value + " is not of type " + binding.getName()
                            + ", attempting conversion";
                        JDBCDataStore.LOGGER.finer(msg);
                    }

                    Object converted = Converters.convert(value, binding);

                    if (converted != null) {
                        value = converted;
                    }
                }

                builder.add(value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // reset the next flag
        next = null;

        // create the feature
        try {
            return builder.buildFeature(fid);
        } catch (IllegalAttributeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for setting the gml:id of ageometry as user data.
     */
    private void setGmlProperties(Geometry g, String gid, String name, String description) {
        // set up the user data
        Map userData = null;

        if (g.getUserData() != null) {
            if (g.getUserData() instanceof Map) {
                userData = (Map) g.getUserData();
            } else {
                userData = new HashMap();
                userData.put(g.getUserData().getClass(), g.getUserData());
            }
        } else {
            userData = new HashMap();
        }

        if (gid != null) {
            userData.put("gml:id", gid);
        }

        if (name != null) {
            userData.put("gml:name", name);
        }

        if (description != null) {
            userData.put("gml:description", description);
        }

        g.setUserData(userData);
    }

    public void remove() {
        try {
            rs.deleteRow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void checkForMultiGEometry() {
        // if ( depth.intValue() > 0 ) {
        // try {
        // //check case where this is a multi geometry
        // // which is composed of associations
        //                
        // if ( gtype.startsWith( "MULTI" )
        // && gas.getObject("geometry") == null ) {
        //                
        // //look for the associated mapping
        // sql = dataStore.selectMultiGeometrySQL(gid);
        // dataStore.LOGGER.fine( sql );
        // ResultSet mg = select.executeQuery( sql );
        // try {
        // List geometries = new ArrayList();
        // while( mg.next() ) {
        // //get the referenced geometry id
        // String rgid = mg.getString( "rgid" );
        //                            
        // sql = dataStore.selectGeometryAssociationSQL(null, rgid, null);
        // dataStore.LOGGER.fine( sql );
        //                            
        // ResultSet rgas = st.executeQuery( sql );
        // try {
        // //read the geometry
        // rgas.next();
        //                                
        // Geometry geometry =
        // dataStore.getSQLDialect().decodeGeometryValue(gatt, rgas, "geometry",
        // geometryFactory);
        // setGmlID(geometry, rgid);
        // geometries.add( geometry );
        // }
        // finally {
        // JDBCDataStore.closeSafe( rgas );
        // }
        // }
        //                        
        // if ( "MULTIPOINT".equals( gtype ) ) {
        // Point[] points = (Point[]) geometries.toArray( new Point[
        // geometries.size() ] );
        // g = geometryFactory.createMultiPoint(points);
        // }
        // else if ( "MULTILINESTRING".equals( gtype ) ) {
        // LineString[] lines = (LineString[]) geometries.toArray( new
        // LineString[ geometries.size() ] );
        // g = geometryFactory.createMultiLineString(lines);
        // }
        // else if ( "MULTIPOLYGON".equals( gtype ) ) {
        // Polygon[] polygons = (Polygon[]) geometries.toArray( new Polygon[
        // geometries.size() ] );
        // g = geometryFactory.createMultiPolygon(polygons);
        // }
        // else {
        // Geometry[] geoms = (Geometry[]) geometries.toArray( new Geometry[
        // geometries.size() ]);
        // g = geometryFactory.createGeometryCollection( geoms );
        // }
        // }
        // finally {
        // JDBCDataStore.closeSafe( mg );
        // }
        // }
        // else {
        // //read the geometry normal
        // g = dataStore.getSQLDialect()
        // .decodeGeometryValue(gatt, gas, "geometry", geometryFactory);
        // }
        //                 
        // }
        // catch (IOException e) {
        // throw new RuntimeException( e );
        // }
        // }
    }
}
