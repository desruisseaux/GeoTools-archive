/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.Filter;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeSqlConstruct;
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
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEAdapter.java,v 1.4 2004/06/22 20:13:15 jfear Exp $
 */
public class ArcSDEAdapter {
	/** Logger for ths class' package */
	private static final Logger LOGGER = Logger.getLogger(ArcSDEAdapter.class
			.getPackage().getName());

	/** mappings of SDE attribute's types to Java ones */
	private static final Map sde2JavaTypes = new HashMap();

	/** inverse of sdeTypes, maps Java types to SDE ones */
	private static final Map java2SDETypes = new HashMap();

	static {
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_STRING),
				String.class);
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INT16),
				Short.class);
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_INT32),
				Integer.class);
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT32),
				Float.class);
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_FLOAT64),
				Double.class);
		sde2JavaTypes
				.put(new Integer(SeColumnDefinition.TYPE_DATE), Date.class);
		sde2JavaTypes.put(new Integer(SeColumnDefinition.TYPE_BLOB),
				byte[].class);

		/**
		 * By now keep using the deprecated constants (TYPE_INTEGER, etc.),
		 * switching directly to the new ones gives problems with ArcSDE
		 * instances prior to version 9.0.
		 */

		// SeColumnDefinition.TYPE_RASTER is not supported...
		java2SDETypes.put(String.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_STRING, 255, 0));
		java2SDETypes.put(Short.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_SMALLINT, 4, 0));
		java2SDETypes.put(Integer.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_INTEGER, 10, 0));
		java2SDETypes.put(Float.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_FLOAT, 5, 2));
		java2SDETypes.put(Double.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_DOUBLE, 15, 4));
		java2SDETypes.put(Date.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_DATE, 1, 0));
		java2SDETypes.put(byte[].class, new SdeTypeDef(
				SeColumnDefinition.TYPE_BLOB, 1, 0));
		java2SDETypes.put(Number.class, new SdeTypeDef(
				SeColumnDefinition.TYPE_DOUBLE, 15, 4));
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
			throw new NullPointerException(
					"a GeometryAttributeType must be provided, got null");
		}

		Class geometryClass = attribute.getType();

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
				throw new IllegalArgumentException(
						"no SDE geometry mapping for " + geometryClass);
			}
		} else {
			if (geometryClass == Point.class) {
				shapeTypes |= SeLayer.SE_POINT_TYPE_MASK;
			} else if (geometryClass == LineString.class) {
				shapeTypes |= SeLayer.SE_LINE_TYPE_MASK;
			} else if (geometryClass == Polygon.class) {
				shapeTypes |= SeLayer.SE_AREA_TYPE_MASK;
			} else if (geometryClass == Geometry.class) {
				LOGGER
						.info("Creating SeShape types for all types of geometries.");
				shapeTypes |= (SeLayer.SE_MULTIPART_TYPE_MASK
						| SeLayer.SE_POINT_TYPE_MASK
						| SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_AREA_TYPE_MASK);
			} else {
				throw new IllegalArgumentException(
						"no SDE geometry mapping for " + geometryClass);
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
	public static SeColumnDefinition createSeColumnDefinition(AttributeType type)
			throws SeException {
		SeColumnDefinition colDef = null;
		String colName = type.getName();
		int fieldLength;
		int fieldScale;
		boolean nillable = type.isNillable();

		SdeTypeDef def = getSdeType(type.getType());

		LOGGER.info("def.type=" + def.colDefType + ", string type="
				+ SeColumnDefinition.TYPE_STRING);

		if (type.getType() == String.class) {
//			fieldLength = (type.getFieldLength() == 0) ? def.size : type
//					.getFieldLength();
			fieldLength = def.size;
			fieldScale = def.scale;
		} else {
			fieldLength = def.size;
//			fieldScale = (type.getFieldLength() == 0) ? def.scale : type
//					.getFieldLength();
			fieldScale = def.scale;
		}

		colDef = new SeColumnDefinition(colName, def.colDefType, fieldLength,
				fieldScale, nillable);

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
	private static SdeTypeDef getSdeType(Class attClass)
			throws IllegalArgumentException {
		SdeTypeDef sdeType = (SdeTypeDef) java2SDETypes.get(attClass);

		if (sdeType == null) {
			throw new IllegalArgumentException("No SDE type mapping for "
					+ attClass.getName());
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
	public static FeatureType fetchSchema(ArcSDEConnectionPool connPool,
			String typeName) throws IOException {
		SeLayer sdeLayer = connPool.getSdeLayer(typeName);
		SeTable sdeTable = connPool.getSdeTable(typeName);
		AttributeType[] types = createAttributeTypes(sdeLayer, sdeTable);
		FeatureType type = null;

		try {
			type = FeatureTypeFactory.newFeatureType(types, sdeLayer
					.getQualifiedName());
		} catch (SeException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		} catch (SchemaException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		} catch (FactoryConfigurationError ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}

		return type;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param sdeLayer
	 *            DOCUMENT ME!
	 * @param table
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	private static AttributeType[] createAttributeTypes(SeLayer sdeLayer,
			SeTable table) throws DataSourceException {
		boolean isNilable;
		int fieldLen;
		Object defValue;

		SeColumnDefinition[] seColumns = null;

		try {
			seColumns = table.describe();
		} catch (SeException ex) {
			LOGGER.log(Level.WARNING, ex.getSeError().getErrDesc(), ex);
			throw new DataSourceException("Error obtaining table schema from "
					+ table.getQualifiedName());
		}

		int nCols = seColumns.length;
		AttributeType[] attTypes = new AttributeType[nCols];
		AttributeType attribute = null;
		Class typeClass = null;

		for (int i = 0; i < nCols; i++) {
			// well, once again, the "great" ArcSDE Java API seems to not
			// provide
			// us as many information as we want. In fact, SeColumnDefinition
			// has a constructor with an argument to specify if an attribute
			// accepts null values, but DOES NOT HAVE a method to retrieve
			// such property... very very usefull, ESRI. (I really hope to
			// someone open my eyes and tell me how it can be obtained)
			isNilable = true;
			defValue = null;

			Integer sdeType = new Integer(seColumns[i].getType());
			fieldLen = seColumns[i].getSize();

			if (sdeType.intValue() == SeColumnDefinition.TYPE_SHAPE) {
				CoordinateReferenceSystem crs = null;

				crs = parseCRS(sdeLayer);

				int seShapeType = sdeLayer.getShapeTypes();
				typeClass = getGeometryType(seShapeType);
				isNilable = (seShapeType & SeLayer.SE_NIL_TYPE_MASK) == SeLayer.SE_NIL_TYPE_MASK;
				defValue = GeometryBuilder.defaultValueFor(typeClass);
				attribute = new GeometricAttributeType(seColumns[i]
						.getName(), typeClass, isNilable,1,1,defValue, crs,Filter.NONE);
			} else if (sdeType.intValue() == SeColumnDefinition.TYPE_RASTER) {
				throw new DataSourceException(
						"Raster columns are not supported yet");
			} else {
				typeClass = (Class) sde2JavaTypes.get(sdeType);
				attribute = AttributeTypeFactory.newAttributeType(seColumns[i]
						.getName(), typeClass, isNilable, fieldLen, defValue);
			}

			attTypes[i] = attribute;
		}

		return attTypes;
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
	private static CoordinateReferenceSystem parseCRS(SeLayer sdeLayer)
			throws DataSourceException {
		CoordinateReferenceSystem crs = null;
		SeCoordinateReference seCRS = sdeLayer.getCoordRef();
		String WKT = seCRS.getProjectionDescription();
		LOGGER.info("About to parse CRS for layer " + sdeLayer.getName() + ": "
				+ WKT);

		try {
			LOGGER.info("Se CRS envelope: " + seCRS.getXYEnvelope());
		} catch (SeException e1) {
			// intentionally blank
		}

		if ("UNKNOWN".equalsIgnoreCase(WKT)) {
			LOGGER.warning("ArcSDE layer " + sdeLayer.getName()
					+ " does not provides a Coordinate Reference System");
		} else {
			try {
				CRSFactory crsFactory = FactoryFinder.getCRSFactory();
				crs = crsFactory.createFromWKT(WKT);
				LOGGER.fine("ArcSDE CRS correctly parsed from layer "
						+ sdeLayer.getName());
			} catch (FactoryException e) {
				String msg = "CRS factory does not knows how to parse the "
						+ "CRS for layer " + sdeLayer.getName() + ": " + WKT;
				LOGGER.log(Level.SEVERE, msg, e);
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
	public static Class getGeometryType(int seShapeType) {
		Class clazz = com.vividsolutions.jts.geom.Geometry.class;

		// in all this assignments, 1 means true and 0 false
		final int isCollection = ((seShapeType & SeLayer.SE_MULTIPART_TYPE_MASK) == SeLayer.SE_MULTIPART_TYPE_MASK) ? 1
				: 0;

		final int isPoint = ((seShapeType & SeLayer.SE_POINT_TYPE_MASK) == SeLayer.SE_POINT_TYPE_MASK) ? 1
				: 0;

		final int isLineString = (((seShapeType & SeLayer.SE_SIMPLE_LINE_TYPE_MASK) == SeLayer.SE_SIMPLE_LINE_TYPE_MASK) || ((seShapeType & SeLayer.SE_LINE_TYPE_MASK) == SeLayer.SE_LINE_TYPE_MASK)) ? 1
				: 0;

		final int isPolygon = ((seShapeType & SeLayer.SE_AREA_TYPE_MASK) == SeLayer.SE_AREA_TYPE_MASK) ? 1
				: 0;

		boolean isError = false;

		// first check if the shape type supports more than one geometry type.
		// In that case, it is *highly* recomended that it support all the
		// geometry types, so we can safely return Geometry.class. If this is
		// not
		// the case and the shape type supports just a few geometry types, then
		// we give it a chance and return Geometry.class anyway, but be aware
		// that transactions over that layer could fail if a Geometry that is
		// not supported is tried for insertion.
		if ((isPoint + isLineString + isPolygon) > 1) {
			clazz = Geometry.class;

			if (4 < (isCollection + isPoint + isLineString + isPolygon)) {
				LOGGER
						.warning("Be careful!! we're mapping an ArcSDE Shape type "
								+ "to the generic Geometry class, but the shape type "
								+ "does not really allows all geometry types!: "
								+ "isCollection="
								+ isCollection
								+ ", isPoint="
								+ isPoint
								+ ", isLineString="
								+ isLineString
								+ ", isPolygon=" + isPolygon);
			} else {
				LOGGER.info("safely mapping SeShapeType to abstract Geometry");
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

		if (isError) {
			throw new IllegalArgumentException("Cannot map the shape type to "
					+ "a Geometry class: isCollection=" + isCollection
					+ ", isPoint=" + isPoint + ", isLineString=" + isLineString
					+ ", isPolygon=" + isPolygon);
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
	public static long getNumericFid(String fid)
			throws IllegalArgumentException {
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
	 * @param stringFids
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	public static long[] getNumericFids(String[] stringFids)
			throws IllegalArgumentException {
		int nfids = stringFids.length;
		long[] fids = new long[nfids];

		for (int i = 0; i < nfids; i++) {
			fids[i] = ArcSDEAdapter.getNumericFid(stringFids[i]);
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
			return "SdeTypeDef[colDefType=" + this.colDefType + ", size="
					+ this.size + ", scale=" + this.scale + "]";
		}
	}
}
