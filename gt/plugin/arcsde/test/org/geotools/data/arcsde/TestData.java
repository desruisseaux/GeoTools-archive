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
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Provides access to the ArcSDEDataStore test data configuration.
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class TestData {
    /** folder used to load test filters */
    private String dataFolder = "/testData/";

    /** the set of test parameters loaded from /testData/testparams.properties */
    private Properties conProps = null;

    /** the name of the table holding the point test features */
    private String point_table;

    /** the name of the table holding the linestring test features */
    private String line_table;

    /** the name of the table holding the polygon test features */
    private String polygon_table;


    /**
     * Creates a new TestData object.
     *
     * @throws IOException DOCUMENT ME!
     */
    public TestData() throws IOException {
        URL folderUrl = getClass().getResource("/testData");
        dataFolder = folderUrl.toExternalForm() + "/";
        conProps = new Properties();

        String propsFile = "/testData/testparams.properties";
        InputStream in = getClass().getResourceAsStream(propsFile);

        if (in == null) {
            throw new IOException("cannot find test params: " + propsFile);
        }

        conProps.load(in);
        point_table = conProps.getProperty("point_table");
        line_table = conProps.getProperty("line_table");
        polygon_table = conProps.getProperty("polygon_table");

        if (point_table == null) {
            throw new IOException("point_table not defined in " + propsFile);
        }

        if (line_table == null) {
            throw new IOException("line_table not defined in " + propsFile);
        }

        if (polygon_table == null) {
            throw new IOException("polygon_table not defined in " + propsFile);
        }
    }

    /**
     * creates an ArcSDEDataStore using /testData/testparams.properties as
     * holder of datastore parameters
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public ArcSDEDataStore getDataStore() throws IOException {
        ConnectionPoolFactory pfac = ConnectionPoolFactory.getInstance();
        //clear all previously created connection pools, since it is possible
        //that a failed test leaves in use connections and affect the next tests
        pfac.clear();
        ConnectionConfig config = new ConnectionConfig(conProps);
        ArcSDEConnectionPool pool = pfac.createPool(config);
        ArcSDEDataStore ds = new ArcSDEDataStore(pool);

        return ds;
    }
	/**
	 * @return Returns the conProps.
	 */
	public Properties getConProps() {
		return conProps;
	}
	/**
	 * @param conProps The conProps to set.
	 */
	public void setConProps(Properties conProps) {
		this.conProps = conProps;
	}
	/**
	 * @return Returns the dataFolder.
	 */
	public String getDataFolder() {
		return dataFolder;
	}
	/**
	 * @param dataFolder The dataFolder to set.
	 */
	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}
	/**
	 * @return Returns the line_table.
	 */
	public String getLine_table() {
		return line_table;
	}
	/**
	 * @param line_table The line_table to set.
	 */
	public void setLine_table(String line_table) {
		this.line_table = line_table;
	}
	/**
	 * @return Returns the point_table.
	 */
	public String getPoint_table() {
		return point_table;
	}
	/**
	 * @param point_table The point_table to set.
	 */
	public void setPoint_table(String point_table) {
		this.point_table = point_table;
	}
	/**
	 * @return Returns the polygon_table.
	 */
	public String getPolygon_table() {
		return polygon_table;
	}
	/**
	 * @param polygon_table The polygon_table to set.
	 */
	public void setPolygon_table(String polygon_table) {
		this.polygon_table = polygon_table;
	}
}
