/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;

import org.geotools.feature.type.DefaultFeatureTypeBuilder;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class to deal with SDE specifics such as creating SeQuery objects
 * from geotool's Query's, mapping SDE types to Java ones and JTS Geometries,
 * etc.
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSDEAdapter.java $
 * @version $Id$
 */
public class ArcSDEAdapter {
    /** Logger for ths class' package */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEAdapter.class.getPackage().getName());

    /** mappings of SDE attribute's types to Java ones */
    private static final Map sde2JavaTypes = new HashMap();

    /** inverse of sdeTypes, maps Java types to SDE ones */
    private static final Map java2SDETypes = new HashMap();

    static {
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_NSTRING), String.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_STRING), String.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INT16), Short.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INT32), Integer.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INT64), Long.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT32), Float.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT64), Double.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_DATE), Date.class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_BLOB),
        // byte[].class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_CLOB),
        // String.class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_UUID),
        // String.class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_XML),
        // org.w3c.dom.Document.class);

        // deprecated codes as for ArcSDE 9.0+. Adding them to maintain < 9.0
        // compatibility
        // though the assigned int codes matched their new counterparts, I let
        // them here as a reminder
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_SMALLINT), Short.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INTEGER), Integer.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT), Float.class);
        sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_DOUBLE), Double.class);

        /**
         * By now keep using the deprecated constants (TYPE_INTEGER, etc.),
         * switching directly to the new ones gives problems with ArcSDE
         * instances prior to version 9.0.
         */
        // SeColumnDefinition.TYPE_RASTER is not supported...
        java2SDETypes.put(String.class, new SdeTypeDef(SeColumnDefinition.TYPE_STRING, 255, 0));
        java2SDETypes.put(Short.class, new SdeTypeDef(SeColumnDefinition.TYPE_SMALLINT, 4, 0));
        java2SDETypes.put(Integer.class, new SdeTypeDef(SeColumnDefinition.TYPE_INTEGER, 10, 0));
        java2SDETypes.put(Float.class, new SdeTypeDef(SeColumnDefinition.TYPE_FLOAT, 5, 2));
        java2SDETypes.put(Double.class, new SdeTypeDef(SeColumnDefinition.TYPE_DOUBLE, 15, 4));
        java2SDETypes.put(Date.class, new SdeTypeDef(SeColumnDefinition.TYPE_DATE, 1, 0));
        java2SDETypes.put(Long.class, new SdeTypeDef(SeColumnDefinition.TYPE_INTEGER, 10, 0));
        java2SDETypes.put(byte[].class, new SdeTypeDef(SeColumnDefinition.TYPE_BLOB, 1, 0));
        java2SDETypes.put(Number.class, new SdeTypeDef(SeColumnDefinition.TYPE_DOUBLE, 15, 4));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param attribute
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws NullPointerException
     *             DOCUMENT ME!
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    public static int guessShapeTypes(GeometryAttributeType attribute) {
        if (attribute == null) {
            throw new NullPointerException("a GeometryAttributeType must be provided, got null");
        }

        Class geometryClass = attribute.getBinding();

        int shapeTypes = 0;

        if (attribute.isNillable()) {
            shapeTypes |= SeLayer.SE_NIL_TYPE_MASK;
        }

        if (GeometryCollection.class.isAssignableFrom(geometryClass)) {
            shapeTypes |= SeLayer.SE_MULTIPART_TYPE_MASK;

            if (geometryClass == MultiPoint.class) {
                shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
            } else if (geometryClass == MultiLineString.class) {
                shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
            } else if (geometryClass == MultiPolygon.class) {
                shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
            } else {
                throw new IllegalArgumentException("no SDE geometry mapping for " + geometryClass);
            }
        } else {
            if (geometryClass == Point.class) {
                shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
            } else if (geometryClass == LineString.class) {
                shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
            } else if (geometryClass == Polygon.class) {
                shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
            } else if (geometryClass == Geometry.class) {
                LOGGER.fine("Creating SeShape types for all types of geometries.");
                shapeTypes |= (SeLayer.SE_MULTIPART_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK | SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_AREA_TYPE_MASK);
            } else {
                throw new IllegalArgumentException("no SDE geometry mapping for " + geometryClass);
            }
        }

        return shapeTypes;
    }

    /**
     * Creates the column definition as used by the ArcSDE Java API, for the
     * given AttributeType.
     * 
     * @param type
     *            the source attribute definition.
     * 
     * @return an <code>SeColumnDefinition</code> object matching the
     *         properties of the source AttributeType.
     * 
     * @throws SeException
     *             if the SeColumnDefinition constructor throws it due to some
     *             invalid parameter
     */
    public static SeColumnDefinition createSeColumnDefinition(AttributeType type) throws SeException {
        SeColumnDefinition colDef = null;
        String colName = type.getLocalName();
        boolean nillable = type.isNillable();

        SdeTypeDef def = getSdeType(type.getBinding());

        int sdeColType = def.colDefType;
        int fieldLength = def.size;
        int fieldScale = def.scale;

        colDef = new SeColumnDefinition(colName, sdeColType, fieldLength, fieldScale, nillable);

        return colDef;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param attClass
     * 
     * @return an SdeTypeDef instance with default values for the given class
     * 
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    private static SdeTypeDef getSdeType(Class attClass) throws IllegalArgumentException {
        SdeTypeDef sdeType = (SdeTypeDef) java2SDETypes.get(attClass);

        if (sdeType == null) {
            throw new IllegalArgumentException("No SDE type mapping for " + attClass.getName());
        }

        return sdeType;
    }

    /**
     * Fetches the schema of a given ArcSDE featureclass and creates its
     * corresponding Geotools FeatureType
     * 
     * @param connPool
     *            DOCUMENT ME!
     * @param typeName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public static FeatureType fetchSchema(ArcSDEConnectionPool connPool, String typeName, String namespace) throws IOException {
        SeLayer sdeLayer = connPool.getSdeLayer(typeName);
        SeTable sdeTable = connPool.getSdeTable(typeName);
        // List/*<AttributeDescriptor>*/properties =
        // createAttributeDescriptors(connPool, sdeLayer, sdeTable, namespace);
        List/* <AttributeType> */properties = createAttributeDescriptors(connPool, sdeLayer, sdeTable, namespace);
        FeatureType type = createSchema(typeName, namespace, properties);
        return type;
    }

    /**
     * Fetchs the schema for the "SQL SELECT" like view definition
     * 
     * @param connPool
     * @param typeName
     * @param viewDef
     * @return
     * @throws IOException
     */
    public static FeatureType fetchSchema(ArcSDEConnectionPool connPool, String typeName, String namespace, SeQueryInfo queryInfo) throws IOException {

        List attributeDescriptors;

        ArcSDEPooledConnection conn = connPool.getConnection();

        SeQuery testQuery = null;
        try {
            // is the first table is a layer, we'll get it to obtain CRS info
            // from
            String mainTable = queryInfo.getConstruct().getTables()[0];
            SeLayer layer = null;
            try {
                layer = connPool.getSdeLayer(conn, mainTable);
            } catch (NoSuchElementException e) {
                LOGGER.info(mainTable + " is not an SeLayer, so no CRS info will be parsed");
            }
            LOGGER.fine("testing query");
            testQuery = new SeQuery(conn);
            testQuery.prepareQueryInfo(queryInfo);
            testQuery.execute();
            LOGGER.fine("definition query executed successfully");

            LOGGER.fine("fetching row to obtain view's types");

            SeRow testRow = testQuery.fetch();
            SeColumnDefinition[] colDefs = testRow.getColumns();

            attributeDescriptors = createAttributeDescriptors(layer, namespace, colDefs);

        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        } finally {
            if (testQuery != null) {
                try {
                    testQuery.close();
                } catch (SeException e) {
                }
            }
            conn.close();
        }
        FeatureType type = createSchema(typeName, namespace, attributeDescriptors);
        return type;
    }

    /**
     * Creates the FeatureType content for a given ArcSDE layer in the form of a
     * list of AttributeDescriptors
     * 
     * @param sdeLayer
     *            sde layer
     * @param table
     *            sde business table associated to <code>layer</code>
     * 
     * @return List&lt;AttributeDescriptor&gt;
     * 
     * @throws DataSourceException
     *             if any problem is found wroking with arcsde to fetch layer
     *             metadata
     * 
     */
    private static List createAttributeDescriptors(ArcSDEConnectionPool connPool, SeLayer sdeLayer, SeTable table, String namespace) throws DataSourceException {
        SeColumnDefinition[] seColumns = null;
        try {
            seColumns = table.describe();
        } catch (SeException e) {
            throw new DataSourceException(e);
        }

        return createAttributeDescriptors(sdeLayer, namespace, seColumns);
    }

    private static List createAttributeDescriptors(SeLayer sdeLayer, String namespace, SeColumnDefinition[] seColumns) throws DataSourceException {
        String attName;
        boolean isNilable;
        int fieldLen;
        Object defValue;
        Object metadata = null;

        int nCols = seColumns.length;
        List attDescriptors = new ArrayList(nCols);

        AttributeType attributeType = null;
        Class typeClass = null;

        for (int i = 0; i < nCols; i++) {
            SeColumnDefinition colDef = seColumns[i];

            // didn't found in the ArcSDE Java API the way of knowing
            // if an SeColumnDefinition is nillable
            attName = seColumns[i].getName();
            isNilable = true;
            defValue = null;
            fieldLen = seColumns[i].getSize();

            final Integer sdeType = new Integer(colDef.getType());

            if (sdeType.intValue() == SeColumnDefinition.TYPE_SHAPE) {
                CoordinateReferenceSystem crs = null;

                crs = parseCRS(sdeLayer);
                metadata = crs;

                int seShapeType = sdeLayer.getShapeTypes();
                typeClass = getGeometryTypeFromLayerMask(seShapeType);
                isNilable = (seShapeType & SeLayer.SE_NIL_TYPE_MASK) == SeLayer.SE_NIL_TYPE_MASK;
                defValue = ArcSDEGeometryBuilder.defaultValueFor(typeClass);

            } else if (sdeType.intValue() == SeColumnDefinition.TYPE_RASTER) {
                throw new DataSourceException("Raster columns are not supported yet");
            } else {
                typeClass = (Class) sde2JavaTypes.get(sdeType);
                // @TODO: add restrictions once the Restrictions utility methods
                // are implemented
                // Set restrictions = Restrictions.createLength(name, typeClass,
                // fieldLen);
            }
            attributeType = AttributeTypeFactory.newAttributeType(attName, typeClass, isNilable, fieldLen, defValue, metadata);
            attDescriptors.add(attributeType);
        }

        return attDescriptors;
    }

    private static FeatureType createSchema(String typeName, String namespace, List properties) {
        // TODO: use factory lookup mechanism once its in place
        FeatureTypeBuilder builder = CommonFactoryFinder.getFeatureTypeFactory(null);
        
        builder.setName(typeName);
        try {
            builder.setNamespace(new URI(namespace));
        } 
        catch (URISyntaxException e) {
            LOGGER.warning("Illegal namespace uri: " + namespace );
        }
        
        for (Iterator it = properties.iterator(); it.hasNext();) {
            AttributeType attType = (AttributeType) it.next();
            builder.addType(attType);
        }

        FeatureType type;
        try {
            type = builder.getFeatureType();
        } 
        catch (SchemaException e) {
            throw new RuntimeException(e);
        }

        return type;
    }

    /**
     * Obtains the <code>SeCoordinateReference</code> of the given
     * <code>SeLayer</code> and tries to create a
     * <code>org.opengis.referencing.crs.CoordinateReferenceSystem</code> from
     * its WKT.
     * 
     * @param sdeLayer
     *            the SeLayer from which to query the CRS in ArcSDE form.
     * 
     * @return the actual CRS or null if <code>sdeLayer</code> does not
     *         defines its coordinate system.
     * 
     * @throws DataSourceException
     *             if the WKT can't be parsed to an opengis CRS using the
     *             CRSFactory
     */
    private static CoordinateReferenceSystem parseCRS(SeLayer sdeLayer) throws DataSourceException {
        CoordinateReferenceSystem crs = null;
        SeCoordinateReference seCRS = sdeLayer.getCoordRef();
        String WKT = seCRS.getProjectionDescription();
        LOGGER.finer("About to parse CRS for layer " + sdeLayer.getName() + ": " + WKT);
        
        try {
            LOGGER.fine(sdeLayer.getName() + " has CRS envelope: " + seCRS.getXYEnvelope());
        } catch (SeException e1) {
            // intentionally blank
        }

        if ("UNKNOWN".equalsIgnoreCase(WKT)) {
            LOGGER.fine("ArcSDE layer " + sdeLayer.getName() + " does not provides a Coordinate Reference System");
        } else {
            try {
                CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                crs = crsFactory.createFromWKT(WKT);
                LOGGER.fine("ArcSDE CRS correctly parsed from layer " + sdeLayer.getName());
            } catch (FactoryException e) {
                String msg = "CRS factory does not knows how to parse the " + "CRS for layer " + sdeLayer.getName() + ": " + WKT;
                LOGGER.log(Level.CONFIG, msg, e);
                // throw new DataSourceException(msg, e);
            }

        }

        return crs;
    }

    /**
     * Returns the mapping JTS geometry type for the ArcSDE Shape type given by
     * the bitmask <code>seShapeType</code>
     * 
     * <p>
     * This bitmask is composed of a combination of the following shape types,
     * as defined in the ArcSDE Java API:
     * 
     * <pre>
     * SE_NIL_TYPE_MASK = 1;
     * SE_POINT_TYPE_MASK = 2;
     * SE_LINE_TYPE_MASK = 4;
     * SE_AREA_TYPE_MASK = 16;
     * SE_MULTIPART_TYPE_MASK = 262144;
     * </pre>
     * 
     * (Note that the type SE_SIMPLE_LINE_TYPE_MASK is not used)
     * </p>
     * 
     * @param seShapeType
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    public static Class getGeometryTypeFromLayerMask(int seShapeType) {
        Class clazz = com.vividsolutions.jts.geom.Geometry.class;
        final int MULTIPART_MASK = SeLayer.SE_MULTIPART_TYPE_MASK;
        final int POINT_MASK = SeLayer.SE_POINT_TYPE_MASK;
        final int SIMPLE_LINE_MASK = SeLayer.SE_SIMPLE_LINE_TYPE_MASK;
        final int LINESTRING_MASK = SeLayer.SE_LINE_TYPE_MASK;
        final int AREA_MASK = SeLayer.SE_AREA_TYPE_MASK;

//        if (seShapeType == SeLayer.TYPE_NIL) {
//            // do nothing
//        } else if (seShapeType == SeLayer.TYPE_MULTI_MASK) {
//            clazz = GeometryCollection.class;
//        } else if (seShapeType == SeLayer.TYPE_LINE || seShapeType == SeLayer.TYPE_SIMPLE_LINE) {
//            clazz = LineString.class;
//        } else if (seShapeType == SeLayer.TYPE_MULTI_LINE
//                || seShapeType == SeLayer.TYPE_MULTI_SIMPLE_LINE) {
//            clazz = MultiLineString.class;
//        } else if (seShapeType == SeLayer.TYPE_MULTI_POINT) {
//            clazz = MultiPoint.class;
//        } else if (seShapeType == SeLayer.TYPE_MULTI_POLYGON) {
//            clazz = MultiPolygon.class;
//        } else if (seShapeType == SeLayer.TYPE_POINT) {
//            clazz = Point.class;
//        } else if (seShapeType == SeLayer.TYPE_POLYGON) {
//            clazz = Polygon.class;
//        } else {
        // in all this assignments, 1 means true and 0 false
        final int isCollection = ((seShapeType & MULTIPART_MASK) == MULTIPART_MASK) ? 1 : 0;

        final int isPoint = ((seShapeType & POINT_MASK) == POINT_MASK) ? 1 : 0;

        final int isLineString = (((seShapeType & SIMPLE_LINE_MASK) == SIMPLE_LINE_MASK) || ((seShapeType & LINESTRING_MASK) == LINESTRING_MASK)) ? 1 : 0;

        final int isPolygon = ((seShapeType & AREA_MASK) == AREA_MASK) ? 1 : 0;

        boolean isError = false;

        // first check if the shape type supports more than one geometry
        // type.
        // In that case, it is *highly* recomended that it support all the
        // geometry types, so we can safely return Geometry.class. If this
        // is
        // not
        // the case and the shape type supports just a few geometry types,
        // then
        // we give it a chance and return Geometry.class anyway, but be
        // aware
        // that transactions over that layer could fail if a Geometry that
        // is
        // not supported is tried for insertion.
        if ((isPoint + isLineString + isPolygon) > 1) {
            clazz = Geometry.class;

            if (4 < (isCollection + isPoint + isLineString + isPolygon)) {
                LOGGER.warning("Be careful!! we're mapping an ArcSDE Shape type " + "to the generic Geometry class, but the shape type " + "does not really allows all geometry types!: " + "isCollection=" + isCollection + ", isPoint=" + isPoint + ", isLineString=" + isLineString + ", isPolygon=" + isPolygon);
            } else {
                LOGGER.fine("safely mapping SeShapeType to abstract Geometry");
            }
        } else if (isCollection == 1) {
            if (isPoint == 1) {
                clazz = MultiPoint.class;
            } else if (isLineString == 1) {
                clazz = MultiLineString.class;
            } else if (isPolygon == 1) {
                clazz = MultiPolygon.class;
            } else {
                isError = true;
            }
        } else {
            if (isPoint == 1) {
                clazz = Point.class;
            } else if (isLineString == 1) {
                clazz = LineString.class;
            } else if (isPolygon == 1) {
                clazz = Polygon.class;
            } else {
                isError = true;
            }
        }

//        }
        return clazz;
    }
    
    /**
     * Returns the most appropriate {@link Geometry} class that matches the
     * shape's type.
     * 
     * @param shape
     *            non <code>null</code> SeShape instance for which to infer
     *            the matching geometry class
     * @return the Geometry subclass corresponding to the shape type
     * @throws SeException
     *             propagated if thrown by {@link SeShape#getType()}
     * @throws IllegalArgumentException
     *             if none of the JTS geometry classes can be matched to the
     *             shape type (shouldnt happen as for the
     *             {@link SeShape#getType() types} defined in the esri arcsde
     *             java api 9.0)
     */
    public static Class getGeometryTypeFromSeShape(SeShape shape) throws SeException {
        Class clazz = com.vividsolutions.jts.geom.Geometry.class;
        
        int seShapeType = shape.getType();
        
        if (seShapeType == SeShape.TYPE_NIL) {
            // do nothing
        } else if (seShapeType == SeShape.TYPE_LINE || seShapeType == SeShape.TYPE_SIMPLE_LINE) {
            clazz = LineString.class;
        } else if (seShapeType == SeShape.TYPE_MULTI_LINE || seShapeType == SeShape.TYPE_MULTI_SIMPLE_LINE) {
            clazz = MultiLineString.class;
        } else if (seShapeType == SeShape.TYPE_MULTI_POINT) {
            clazz = MultiPoint.class;
        } else if (seShapeType == SeShape.TYPE_MULTI_POLYGON) {
            clazz = MultiPolygon.class;
        } else if (seShapeType == SeShape.TYPE_POINT) {
            clazz = Point.class;
        } else if (seShapeType == SeShape.TYPE_POLYGON) {
            clazz = Polygon.class;
        } else {
            throw new IllegalArgumentException("Cannot map the shape type '" + seShapeType + "' to any known SeShape.TYPE_*");
        }
        return clazz;
    }

    /**
     * Returns the numeric identifier of a FeatureId, given by the full
     * qualified name of the featureclass prepended to the ArcSDE feature id.
     * ej: SDE.SDE.SOME_LAYER.1
     * 
     * @param fid
     *            a geotools FeatureID
     * 
     * @return an ArcSDE feature ID
     * 
     * @throws IllegalArgumentException
     *             If the given string is not properly formatted
     *             [anystring].[long value]
     */
    public static long getNumericFid(Identifier id) throws IllegalArgumentException {
        if (!(id instanceof FeatureId))
            throw new IllegalArgumentException("Only FeatureIds are supported when encoding id filters to SDE.  Not " + id.getClass());

        String fid = ((FeatureId) id).getID();
        int dotIndex = fid.lastIndexOf('.');
        try {
            return Long.decode(fid.substring(++dotIndex)).longValue();
        } catch (Exception ex) {
            throw new IllegalArgumentException("FeatureID " + fid + " does not seems as a valid ArcSDE FID");
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param stringFids
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    public static long[] getNumericFids(Set identifiers) throws IllegalArgumentException {
        int nfids = identifiers.size();
        long[] fids = new long[nfids];

        Iterator ids = identifiers.iterator();
        for (int i = 0; i < nfids; i++) {
            fids[i] = ArcSDEAdapter.getNumericFid((Identifier) ids.next());
        }

        return fids;
    }

    /**
     * Holds default values for the properties (size and scale) of a
     * SeColumnDefinition, given by its column type
     * (SeColumnDefinition.SE_STRING, etc).
     * 
     * <p>
     * </p>
     * 
     * @author Gabriel Roldan, Axios Engineering
     * @version $Revision: 1.4 $
     */
    private static class SdeTypeDef {
        /** DOCUMENT ME! */
        final int colDefType;

        /** DOCUMENT ME! */
        final int size;

        /** DOCUMENT ME! */
        final int scale;

        /**
         * Creates a new SdeTypeDef object.
         * 
         * @param colDefType
         *            DOCUMENT ME!
         * @param size
         *            DOCUMENT ME!
         * @param scale
         *            DOCUMENT ME!
         */
        public SdeTypeDef(int colDefType, int size, int scale) {
            this.colDefType = colDefType;
            this.size = size;
            this.scale = scale;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return DOCUMENT ME!
         */
        public String toString() {
            return "SdeTypeDef[colDefType=" + this.colDefType + ", size=" + this.size + ", scale=" + this.scale + "]";
        }
    }
}
