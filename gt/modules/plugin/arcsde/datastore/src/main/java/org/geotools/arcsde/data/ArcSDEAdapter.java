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
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRegistration;
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
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEAdapter.class.getPackage().getName());

    /** mappings of SDE attribute's types to Java ones */
    private static final Map sde2JavaTypes = new HashMap();

    /** inverse of sdeTypes, maps Java types to SDE ones */
    private static final Map java2SDETypes = new HashMap();

    static {
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_NSTRING), String.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_STRING), String.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_INT16), Short.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_INT32), Integer.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_INT64), Long.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_FLOAT32), Float.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_FLOAT64), Double.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_DATE), Date.class);
        // @TODO: not at all, only for capable open table with GeoServer
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_BLOB), byte[].class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_CLOB),
        // String.class);
        // @Tested for view
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_UUID), String.class);
        // @TODO sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_XML),
        // org.w3c.dom.Document.class);

        // deprecated codes as for ArcSDE 9.0+. Adding them to maintain < 9.0
        // compatibility
        // though the assigned int codes matched their new counterparts, I let
        // them here as a reminder
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_SMALLINT), Short.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_INTEGER), Integer.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_FLOAT), Float.class);
        sde2JavaTypes.put(Integer.valueOf(SeColumnDefinition.TYPE_DOUBLE), Double.class);

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
    public static int guessShapeTypes(GeometryDescriptor attribute) {
        if (attribute == null) {
            throw new NullPointerException("a GeometryAttributeType must be provided, got null");
        }

        Class geometryClass = attribute.getType().getBinding();

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
                shapeTypes |= (SeLayer.SE_MULTIPART_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK
                        | SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_AREA_TYPE_MASK);
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
    public static SeColumnDefinition createSeColumnDefinition(AttributeDescriptor type)
            throws SeException {
        SeColumnDefinition colDef = null;
        String colName = type.getLocalName();
        boolean nillable = type.isNillable();

        SdeTypeDef def = getSdeType(type.getType().getBinding());

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
     * @param sdeLayer
     *            layer containing sde spatial attributes to create schema from
     * @param sdeTable
     *            layer containing sde non spatial attributes to create schema
     *            from. The table qualified name is gonna be used as the feature
     *            type name.
     * @return the feature type representing the ArcSDE feature class given by
     *         the layer and table.
     * 
     * @throws IOException
     *             if an exception is caught accessing the sde feature class
     *             metadata.
     */
    public static SimpleFeatureType fetchSchema(final SeLayer sdeLayer, final SeTable sdeTable,
            final String namespace) throws IOException {
        final String typeName = sdeTable.getQualifiedName();
        List<AttributeDescriptor> properties = createAttributeDescriptors(sdeLayer, sdeTable,
                namespace);
        SimpleFeatureType type = createSchema(typeName, namespace, properties);
        return type;
    }

    /**
     * Fetchs the schema for the "SQL SELECT" like view definition
     */
    public static SimpleFeatureType fetchSchema(ArcSDEConnectionPool connPool, String typeName,
            String namespace, SeQueryInfo queryInfo) throws IOException {

        List<AttributeDescriptor> attributeDescriptors;

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
        SimpleFeatureType type = createSchema(typeName, namespace, attributeDescriptors);
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
    private static List<AttributeDescriptor> createAttributeDescriptors(SeLayer sdeLayer,
            SeTable table, String namespace) throws DataSourceException {
        SeColumnDefinition[] seColumns = null;
        try {
            seColumns = table.describe();
        } catch (SeException e) {
            throw new DataSourceException(e);
        }

        return createAttributeDescriptors(sdeLayer, namespace, seColumns);
    }

    private static List<AttributeDescriptor> createAttributeDescriptors(SeLayer sdeLayer,
            String namespace, SeColumnDefinition[] seColumns) throws DataSourceException {
        String attName;
        boolean isNilable;
        int fieldLen;
        Object defValue;
        CoordinateReferenceSystem metadata = null;

        final int nCols = seColumns.length;
        List<AttributeDescriptor> attDescriptors = new ArrayList<AttributeDescriptor>(nCols);

        Class typeClass = null;

        for (int i = 0; i < nCols; i++) {
            SeColumnDefinition colDef = seColumns[i];

            // didn't found in the ArcSDE Java API the way of knowing
            // if an SeColumnDefinition is nillable
            attName = seColumns[i].getName();
            isNilable = true;
            defValue = null;
            fieldLen = seColumns[i].getSize();

            final Integer sdeType = Integer.valueOf(colDef.getType());

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
                Object obj = sde2JavaTypes.get(sdeType);
                if (obj == null) {
                    // interesting question: Do we throw an exception here, or
                    // do we allow un-handle-able columns
                    // to just 'disappear' when serving those tables?
                    throw new DataSourceException("Unsupported column type (" + sdeType.intValue()
                            + ") for " + attName);
                }
                typeClass = (Class) obj;
                // @TODO: add restrictions once the Restrictions utility methods
                // are implemented
                // Set restrictions = Restrictions.createLength(name, typeClass,
                // fieldLen);
            }

            AttributeTypeBuilder b = new AttributeTypeBuilder();
            b.setBinding(typeClass);
            b.setName(attName);
            b.setNillable(isNilable);
            b.setDefaultValue(defValue);
            if (fieldLen > 0) {
                b.setLength(fieldLen);
            }
            b.setCRS(metadata);

            attDescriptors.add(b.buildDescriptor(attName));
        }

        return attDescriptors;
    }

    private static SimpleFeatureType createSchema(String typeName, String namespace, List properties) {
        // TODO: use factory lookup mechanism once its in place
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

        builder.setName(typeName);
        builder.setNamespaceURI(namespace);

        for (Iterator it = properties.iterator(); it.hasNext();) {
            AttributeDescriptor attType = (AttributeDescriptor) it.next();
            builder.add(attType);
        }

        return builder.buildFeatureType();
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
            LOGGER.fine("ArcSDE layer " + sdeLayer.getName()
                    + " does not provides a Coordinate Reference System");
        } else {
            try {
                CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                crs = crsFactory.createFromWKT(WKT);
                LOGGER.fine("ArcSDE CRS correctly parsed from layer " + sdeLayer.getName());
            } catch (FactoryException e) {
                String msg = "CRS factory does not knows how to parse the " + "CRS for layer "
                        + sdeLayer.getName() + ": " + WKT;
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

        // if (seShapeType == SeLayer.TYPE_NIL) {
        // // do nothing
        // } else if (seShapeType == SeLayer.TYPE_MULTI_MASK) {
        // clazz = GeometryCollection.class;
        // } else if (seShapeType == SeLayer.TYPE_LINE || seShapeType ==
        // SeLayer.TYPE_SIMPLE_LINE) {
        // clazz = LineString.class;
        // } else if (seShapeType == SeLayer.TYPE_MULTI_LINE
        // || seShapeType == SeLayer.TYPE_MULTI_SIMPLE_LINE) {
        // clazz = MultiLineString.class;
        // } else if (seShapeType == SeLayer.TYPE_MULTI_POINT) {
        // clazz = MultiPoint.class;
        // } else if (seShapeType == SeLayer.TYPE_MULTI_POLYGON) {
        // clazz = MultiPolygon.class;
        // } else if (seShapeType == SeLayer.TYPE_POINT) {
        // clazz = Point.class;
        // } else if (seShapeType == SeLayer.TYPE_POLYGON) {
        // clazz = Polygon.class;
        // } else {
        // in all this assignments, 1 means true and 0 false
        final int isCollection = ((seShapeType & MULTIPART_MASK) == MULTIPART_MASK) ? 1 : 0;

        final int isPoint = ((seShapeType & POINT_MASK) == POINT_MASK) ? 1 : 0;

        final int isLineString = (((seShapeType & SIMPLE_LINE_MASK) == SIMPLE_LINE_MASK) || ((seShapeType & LINESTRING_MASK) == LINESTRING_MASK)) ? 1
                : 0;

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
                LOGGER.warning("Be careful!! we're mapping an ArcSDE Shape type "
                        + "to the generic Geometry class, but the shape type "
                        + "does not really allows all geometry types!: " + "isCollection="
                        + isCollection + ", isPoint=" + isPoint + ", isLineString=" + isLineString
                        + ", isPolygon=" + isPolygon);
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

        // }
        return clazz;
    }

    /**
     * Returns the most appropriate {@link Geometry} class that matches the
     * shape's type.
     * 
     * @param shape
     *            SeShape instance for which to infer the matching geometry
     *            class, may be null
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

        final int seShapeType = shape == null ? SeShape.TYPE_NIL : shape.getType();

        if (seShapeType == SeShape.TYPE_NIL) {
            clazz = null;
        } else if (seShapeType == SeShape.TYPE_LINE || seShapeType == SeShape.TYPE_SIMPLE_LINE) {
            clazz = LineString.class;
        } else if (seShapeType == SeShape.TYPE_MULTI_LINE
                || seShapeType == SeShape.TYPE_MULTI_SIMPLE_LINE) {
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
            throw new IllegalArgumentException("Cannot map the shape type '" + seShapeType
                    + "' to any known SeShape.TYPE_*");
        }
        return clazz;
    }

    /**
     * Returns the numeric identifier of a FeatureId, given by the full
     * qualified name of the featureclass prepended to the ArcSDE feature id.
     * ej: SDE.SDE.SOME_LAYER.1
     * 
     * @param id
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
            throw new IllegalArgumentException(
                    "Only FeatureIds are supported when encoding id filters to SDE.  Not "
                            + id.getClass());

        String fid = ((FeatureId) id).getID();
        int dotIndex = fid.lastIndexOf('.');
        try {
            return Long.decode(fid.substring(++dotIndex)).longValue();
        } catch (Exception ex) {
            throw new IllegalArgumentException("FeatureID " + fid
                    + " does not seems as a valid ArcSDE FID");
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param identifiers
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
     * @author Gabriel Roldan, Axios Engineering
     * @version $Revision: 1.4 $
     */
    private static class SdeTypeDef {
        /**
         * A magic number provided by SeColumnDefinition (example
         * SeColumnDefinition.TYPE_STRING)
         */
        final int colDefType;

        /** size (probably field size?) */
        final int size;

        /** scale */
        final int scale;

        /**
         * Creates a new SdeTypeDef object.
         * 
         * @param colDefType
         *            Constant provided by SeColumnDefinition
         * @param size
         *            Field size
         * @param scale
         *            Field scale
         */
        public SdeTypeDef(int colDefType, int size, int scale) {
            this.colDefType = colDefType;
            this.size = size;
            this.scale = scale;
        }

        /**
         * Text representation
         * 
         * @return Text represenation for debugging
         */
        public String toString() {
            return "SdeTypeDef[colDefType=" + this.colDefType + ", size=" + this.size + ", scale="
                    + this.scale + "]";
        }
    }

    /**
     * Creates the given featuretype in the underlying ArcSDE database.
     * 
     * <p>
     * The common use case to create an ArcSDE layer is to setup the SeTable
     * object with all the non-geometry attributes first, then create the
     * SeLayer and set the geometry column name and its properties. This
     * approach brings a nice problem, since we need to create the attributes in
     * exactly the same order as specified in the passed FeatureType, which
     * means that the geometry attribute needs not to be the last one.
     * </p>
     * 
     * <p>
     * To avoid this, the following workaround is performed: instead of creating
     * the schema as described above, we will first create the SeTable with a
     * single, temporary column, since it is not possible to create a table
     * without columns. The, we will iterate over the AttributeTypes and add
     * them as they appear using
     * <code>SeTable.addColumn(SeColumnDefinition)</code>. But if we found
     * that the current AttributeType is geometric, instead of adding the column
     * we just create the SeLayer object. This way, the geometric attribute is
     * inserted at the end, and then we keep iterating and adding the rest of
     * the columns. Finally, the first column is removed, since it was temporary
     * (note that I advertise it, it is a _workaround_).
     * </p>
     * 
     * <p>
     * Sometimes some 'extra' information is required to correctly create the
     * underlying ArcSDE SeLayer. For instance, a specific configuration keyword
     * might be required to be used (instead of "DEFAULTS"), or a particular
     * column might need to be marked as the rowid column for the featuretype.
     * 
     * A non-null <code>hints</code> parameter contains a mapping from a list
     * of well-known {@link java.lang.String} keys to values. The possible keys
     * are listed in the table below. keys with any other values are ignored.
     * 
     * <table>
     * <tr>
     * <td>key name</td>
     * <td>key value type</td>
     * <td>default value (if applicable)</td>
     * </tr>
     * 
     * <tr>
     * <td>configuration.keyword</td>
     * <td>{@link java.lang.String}</td>
     * <td>"DEFAULTS"</td>
     * </tr>
     * 
     * <tr>
     * <td>rowid.column.type</td>
     * <td>{@link java.lang.String} - "NONE", "USER" and "SDE" are the only
     * valid values</td>
     * <td>"NONE"</td>
     * </tr>
     * 
     * <tr>
     * <td>rowid.column.name</td>
     * <td>{@link java.lang.String}</td>
     * <td>null</td>
     * </tr>
     * 
     * </p>
     * 
     * @param featureType
     *            the feature type containing the name, attributes and
     *            coordinate reference system of the new ArcSDE layer.
     * 
     * @param hints
     *            A map containing extra ArcSDE-specific hints about how to
     *            create the underlying ArcSDE SeLayer and SeTable objects from
     *            this FeatureType.
     * 
     * @throws IOException
     *             see <code>throws DataSourceException</code> bellow
     * @throws IllegalArgumentException
     *             if the passed feature type does not contains at least one
     *             geometric attribute, or if the type name contains '.' (dots).
     * @throws NullPointerException
     *             if <code>featureType</code> is <code>null</code>
     * @throws DataSourceException
     *             if there is <b>not an available (free) connection </b> to the
     *             ArcSDE instance(in that case maybe you need to increase the
     *             maximun number of connections for the connection pool), or an
     *             SeException exception is catched while creating the feature
     *             type at the ArcSDE instance (e.g. a table with that name
     *             already exists).
     */
    public static void createSchema(SimpleFeatureType featureType, Map hints,
            ArcSDEConnectionPool connectionPool) throws IOException, IllegalArgumentException {
        if (featureType == null) {
            throw new NullPointerException("You have to provide a FeatureType instance");
        }

        if (featureType.getDefaultGeometry() == null) {
            throw new IllegalArgumentException(
                    "FeatureType must have at least one geometry attribute");
        }

        final String[] typeNameParts = featureType.getTypeName().split("\\.");
        final String unqualifiedTypeName = typeNameParts[typeNameParts.length - 1];

        // Create a new SeTable/SeLayer with the specified attributes....
        ArcSDEPooledConnection connection = null;
        SeTable table = null;
        SeLayer layer = null;

        // flag to know if the table was created by us when catching an
        // exception.
        boolean tableCreated = false;

        // table/layer creation hints information
        int rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
        String rowIdColumn = null;
        String configKeyword = "DEFAULTS";
        if (hints != null) {
            if (hints.get("configuration.keyword") instanceof String) {
                configKeyword = (String) hints.get("configuration.keyword");
            }
            if (hints.get("rowid.column.type") instanceof String) {
                String rowIdStr = (String) hints.get("rowid.column.type");
                if (rowIdStr.equalsIgnoreCase("NONE")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
                } else if (rowIdStr.equalsIgnoreCase("USER")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER;
                } else if (rowIdStr.equalsIgnoreCase("SDE")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
                } else {
                    throw new DataSourceException(
                            "createSchema hint 'rowid.column.type' must be one of 'NONE', 'USER' or 'SDE'");
                }
            }
            if (hints.get("rowid.column.name") instanceof String) {
                rowIdColumn = (String) hints.get("rowid.column.name");
            }
        }

        // placeholder to a catched exception to know in the finally block
        // if we should cleanup the crap we left in the database
        Exception error = null;

        try {
            connection = connectionPool.getConnection();

            // create a table with provided username
            String qualifiedName = null;

            if (unqualifiedTypeName.indexOf('.') == -1) {
                qualifiedName = connection.getUser() + "." + featureType.getTypeName();
                LOGGER.finer("new full qualified type name: " + qualifiedName);
            } else {
                qualifiedName = unqualifiedTypeName;
                LOGGER.finer("full qualified type name provided by user: " + qualifiedName);
            }

            layer = new SeLayer(connection);
            layer.setTableName(qualifiedName);
            layer.setCreationKeyword(configKeyword);

            final String HACK_COL_NAME = "gt_workaround_col_";

            table = createSeTable(connection, qualifiedName, HACK_COL_NAME, configKeyword);
            tableCreated = true;

            final List<AttributeDescriptor> atts = featureType.getAttributes();
            AttributeDescriptor currAtt;

            for (Iterator<AttributeDescriptor> it = atts.iterator(); it.hasNext();) {
                currAtt = it.next();

                if (currAtt instanceof GeometryDescriptor) {
                    GeometryDescriptor geometryAtt = (GeometryDescriptor) currAtt;
                    createSeLayer(layer, qualifiedName, geometryAtt);
                } else {
                    LOGGER.fine("Creating column definition for " + currAtt);

                    SeColumnDefinition newCol = ArcSDEAdapter.createSeColumnDefinition(currAtt);

                    // /////////////////////////////////////////////////////////////
                    // HACK!!!!: this hack is just to avoid the error that
                    // occurs //
                    // when adding a column wich is not nillable. Need to fix
                    // this//
                    // but by now it conflicts with the requirement of creating
                    // //
                    // the schema with the correct attribute order. //
                    // /////////////////////////////////////////////////////////////
                    newCol = new SeColumnDefinition(newCol.getName(), newCol.getType(), newCol
                            .getSize(), newCol.getScale(), true);

                    // /////////////////////////////////////////////////////////////
                    // END of horrible HACK //
                    // /////////////////////////////////////////////////////////////
                    LOGGER.fine("Adding column " + newCol.getName() + " to the actual table.");
                    table.addColumn(newCol);
                }
            }

            LOGGER.fine("deleting the 'workaround' column...");
            table.dropColumn(HACK_COL_NAME);

            LOGGER.fine("setting up table registration with ArcSDE...");
            SeRegistration reg = new SeRegistration(connection, table.getName());
            if (rowIdColumn != null) {
                LOGGER.fine("setting rowIdColumnName to " + rowIdColumn + " in table "
                        + reg.getTableName());
                reg.setRowIdColumnName(rowIdColumn);
                reg.setRowIdColumnType(rowIdType);
                reg.alter();
                reg = null;
            }

            LOGGER.fine("Schema correctly created: " + featureType);

        } catch (SeException e) {
            LOGGER.log(Level.WARNING, e.getSeError().getErrDesc(), e);
            throw new DataSourceException(e.getMessage(), e);
        } catch (DataSourceException dse) {
            LOGGER.log(Level.WARNING, dse.getMessage(), dse);
            throw dse;
        } finally {
            if ((error != null) && tableCreated) {
                // TODO: remove table if created and then failed
            }
            connection.close();
        }
    }

    private static SeTable createSeTable(ArcSDEPooledConnection connection, String qualifiedName,
            String hackColName, String configKeyword) throws SeException {
        SeTable table;
        final SeColumnDefinition[] tmpCol = { new SeColumnDefinition(hackColName,
                SeColumnDefinition.TYPE_STRING, 4, 0, true) };
        table = new SeTable(connection, qualifiedName);

        try {
            LOGGER.warning("Remove the line 'table.delete()' for production use!!!");
            table.delete();
        } catch (SeException e) {
            // intentionally do nothing
        }

        LOGGER.info("creating table " + qualifiedName);

        // create the table using DBMS default configuration keyword.
        // valid keywords are defined in the dbtune table.
        table.create(tmpCol, configKeyword);
        LOGGER.info("table " + qualifiedName + " created...");

        return table;
    }

    private static void createSeLayer(SeLayer layer, String qualifiedName,
            GeometryDescriptor geometryAtt) throws SeException {
        String spatialColName = geometryAtt.getLocalName();
        LOGGER.info("setting spatial column name: " + spatialColName);
        layer.setSpatialColumnName(spatialColName);

        // Set the shape types that can be inserted into this layer
        int seShapeTypes = ArcSDEAdapter.guessShapeTypes(geometryAtt);
        layer.setShapeTypes(seShapeTypes);
        layer.setGridSizes(1100, 0, 0);
        layer.setDescription("Created with GeoTools");

        // Define the layer's Coordinate Reference
        CoordinateReferenceSystem crs = geometryAtt.getCRS();
        SeCoordinateReference coordref = getGenericCoordRef();
        String WKT = null;

        if (crs == null) {
            LOGGER.warning("Creating feature type " + qualifiedName
                    + ": the geometry attribute does not supply a coordinate reference system");
        } else {
            LOGGER.info("Creating the SeCoordRef object for CRS " + crs);
            WKT = crs.toWKT();
            coordref.setCoordSysByDescription(WKT);
        }

        SeExtent validCoordRange = null;

        if ((WKT != null) && (WKT.indexOf("GEOGCS") != -1)) {
            validCoordRange = new SeExtent(-180, -90, 180, 90);
        } else {
            validCoordRange = coordref.getXYEnvelope();
        }

        layer.setExtent(validCoordRange);

        LOGGER.info("Applying CRS " + coordref.getCoordSysDescription());
        layer.setCoordRef(coordref);
        LOGGER.info("CRS applyed to the new layer.");

        // /////////////////////////
        // this param is used by ArcSDE for database initialization purposes
        int estInitFeatCount = 4;

        // this param is used by ArcSDE as an estimation of the average number
        // of points the layer's geometries will have, one never will know what
        // for
        int estAvgPointsPerFeature = 4;
        LOGGER.info("Creating the layer...");
        layer.create(estInitFeatCount, estAvgPointsPerFeature);
        LOGGER.info("ArcSDE layer created.");
    }

    /**
     * Creates and returns a <code>SeCoordinateReference</code> CRS, though
     * based on an UNKNOWN CRS, is inclusive enough (in terms of valid
     * coordinate range and presicion) to deal with most coordintates.
     * 
     * <p>
     * Actually tested to deal with coordinates with 0.0002 units of separation
     * as well as with large coordinates such as UTM (values greater than
     * 500,000.00)
     * </p>
     * 
     * <p>
     * This method is driven by the equally named method in TestData.java
     * </p>
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SeException
     *             DOCUMENT ME!
     */
    private static SeCoordinateReference getGenericCoordRef() throws SeException {
        // create a sde CRS with a huge value range and 5 digits of presission
        SeCoordinateReference seCRS = new SeCoordinateReference();
        int shift = 600000;
        SeExtent validRange = new SeExtent(-shift, -shift, shift, shift);
        seCRS.setXYByEnvelope(validRange);
        LOGGER.info("CRS: " + seCRS.getXYEnvelope());

        return seCRS;
    }
}
