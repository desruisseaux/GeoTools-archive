package org.geotools.data.oracle.attributeio;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;
import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.AdapterSDO;
import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;
import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;
import oracle.sdoapi.geom.GeometryFactory;
import oracle.sdoapi.geom.InvalidGeometryException;
import oracle.sdoapi.sref.SRException;
import oracle.sdoapi.sref.SRManager;
import oracle.sdoapi.sref.SpatialReference;
import oracle.sql.STRUCT;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.feature.AttributeType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An attribute IO that uses the Oracle SDO API to read/write geometries
 * 
 * @author Andrea Aime
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 *  
 */
public class SDOAttributeIO implements AttributeIO {

	private static final Logger LOGGER = Logger
			.getLogger("org.geotools.data.oracle");

	// geometry adpaters
	private AdapterJTS adapterJTS;
	private AdapterSDO adapterSDO;
	private QueryData queryData;

	public SDOAttributeIO(AttributeType attributeType, QueryData queryData)
			throws DataSourceException {
		this.queryData = queryData;

		try {
			String tableName = queryData.getFeatureTypeInfo()
					.getFeatureTypeName();
			String columnName = attributeType.getName();
			LOGGER.fine("About to create Geometry convertor for " + tableName
					+ "." + columnName);

			// TODO should check that it is an OracleConnection
			OracleConnection conn = (OracleConnection) queryData
					.getConnection();

			GeometryFactory gFact = null;

			int srid = queryData.getFeatureTypeInfo().getSRID(columnName);

			if (srid != -1) {
				SRManager srManager = OraSpatialManager
						.getSpatialReferenceManager(conn);
				SpatialReference sr = srManager.retrieve(srid);
				gFact = OraSpatialManager.getGeometryFactory(sr);
			} else {
				gFact = OraSpatialManager.getGeometryFactory();
			}

			adapterSDO = new AdapterSDO(gFact, conn);
			adapterJTS = new AdapterJTS(gFact);
		} catch (SQLException e) {
			String msg = "Error setting up SDO Geometry convertor";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg + ":" + e.getMessage(), e);
		} catch (SRException e) {
			throw new DataSourceException(
					"Error setting up SDO Geometry convertor", e);
		}
	}

	/**
	 * @see org.geotools.data.jdbc.attributeio.AttributeIO#read(java.sql.ResultSet,
	 *      int)
	 */
	public Object read(ResultSet rs, int position) throws IOException {
		try {
			Object struct = rs.getObject(position);
			oracle.sdoapi.geom.Geometry sdoGeom = adapterSDO
					.importGeometry(struct);
			return adapterJTS.exportGeometry(Geometry.class, sdoGeom);
		} catch (SQLException e) {
			String msg = "SQL Exception reading geometry column";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		} catch (InvalidGeometryException e) {
			String msg = "Problem with the geometry";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		} catch (GeometryInputTypeNotSupportedException e) {
			String msg = "Geometry Conversion type error";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		} catch (GeometryOutputTypeNotSupportedException e) {
			String msg = "Geometry Conversion type error";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		}
	}

	/**
	 * 
	 * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.ResultSet,
	 *      int, java.lang.Object)
	 */
	public void write(ResultSet rs, int position, Object value)
			throws IOException {
		try {
			oracle.sdoapi.geom.Geometry sdoGeom = adapterJTS
					.importGeometry(value);
			Object o = adapterSDO.exportGeometry(STRUCT.class, sdoGeom);
			rs.updateObject(position, o);
		} catch (SQLException sqlException) {
			String msg = "SQL Exception writing geometry column";
			LOGGER.log(Level.SEVERE, msg, sqlException);
			throw new DataSourceException(msg, sqlException);
		} catch (InvalidGeometryException e) {
			String msg = "Problem with the geometry";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		} catch (GeometryInputTypeNotSupportedException e) {
			String msg = "Geometry Conversion type error";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		} catch (GeometryOutputTypeNotSupportedException e) {
			String msg = "Geometry Conversion type error";
			LOGGER.log(Level.SEVERE, msg, e);
			throw new DataSourceException(msg, e);
		}

	}

}