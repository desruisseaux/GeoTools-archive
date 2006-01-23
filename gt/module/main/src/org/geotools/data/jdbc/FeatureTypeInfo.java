/*
 * Created on 31-mar-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.FeatureType;



/**
 * Stores information about known FeatureTypes.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 */
public class FeatureTypeInfo {
	private String featureTypeName;
	private FeatureType schema;
	private Map sridMap = new HashMap();
	private FIDMapper mapper;

	public FeatureTypeInfo(String typeName, FeatureType schema, FIDMapper mapper) {
		this.featureTypeName = typeName;
		this.schema = schema;
		this.mapper = mapper;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getFeatureTypeName() {
		return featureTypeName;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public FeatureType getSchema() {
		return schema;
	}

	/**
	 * Get the DataStore specific SRID for a geometry column
	 *
	 * @param geometryAttributeName The name of the Geometry column to get the srid for.
	 *
	 * @return The srid of the geometry column.  This will only be present if
	 *         determineSRID(String) of JDBCDataStore has been overridden.  If there is no
	 *         SRID registered -1 will be returned.
	 */
	public int getSRID(String geometryAttributeName) {
		int srid = -1;

		Integer integer = (Integer) sridMap.get(geometryAttributeName);

		if (integer != null) {
			srid = integer.intValue();
		}

		return srid;
	}

	public Map getSRIDs() {
		return Collections.unmodifiableMap(sridMap);
	}

	/**
	 * Puts the srid for a geometry column in the internal map.
	 *
	 * @param geometryColumnName The geometry column name.
	 * @param srid The SRID of the geometry column.
	 */
	void putSRID(String geometryColumnName, int srid) {
		sridMap.put(geometryColumnName, new Integer(srid));
	}

	public String toString() {
		return "typeName = " + featureTypeName + " schema: " +
		schema + "srids: " + sridMap;
	}
    /**
     * @return
     */
    public FIDMapper getFIDMapper() {
        return mapper;
    }

    /**
     * @param mapper
     */
    public void setFIDMapper(FIDMapper mapper) {
        this.mapper = mapper;
    }

}

