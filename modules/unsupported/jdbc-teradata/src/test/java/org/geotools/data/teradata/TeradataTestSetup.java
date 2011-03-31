/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.teradata;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

import java.util.Properties;

public class TeradataTestSetup extends JDBCTestSetup {

    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);

        // the unit tests assume a non loose behaviour
        ((TeradataGISDialect) dataStore.getSQLDialect()).setLooseBBOXEnabled(false);

        // the tests assume non estimated extents
        ((TeradataGISDialect) dataStore.getSQLDialect()).setEstimatedExtentsEnabled(false);

        // let's work with the most common schema please
        dataStore.setDatabaseSchema(fixture.getProperty("schema"));
    }

    @Override
    protected Properties createExampleFixture() {
        Properties fixture = new Properties();
        fixture.put("driver", "com.teradata.jdbc.TeraDriver");
        fixture.put("url", "jdbc:teradata://localhost/DATABASE=geotools,PORT=1025,TMODE=ANSI,CHARSET=UTF8");
        fixture.put("host", "localhost");
        fixture.put("database", "geotools");
        fixture.put("schema", "geotools");
        fixture.put("port", "1025");
        fixture.put("user", "dbc");
        fixture.put("password", "dbc");
        return fixture;
    }

    @Override
    protected void setUpData() throws Exception {

        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ft1'");
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ft2'");
        runSafe("DROP TABLE \"ft1\"");
        runSafe("DROP TABLE \"ft2\"");

        run("CREATE TABLE \"ft1\"(" //
                + "\"id\" PRIMARY KEY not null generated always as identity (start with 0) integer, " //
                + "\"geometry\" ST_GEOMETRY, " //
                + "\"intProperty\" int," //
                + "\"doubleProperty\" double precision, " //
                + "\"stringProperty\" varchar(200) casespecific)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('" + fixture.getProperty("database") + "', '" + fixture.getProperty("schema") + "', 'ft1', 'geometry', 2, 1619, 'POINT')");
//        run("CREATE INDEX FT1_GEOMETRY_INDEX ON \"ft1\" USING GIST (\"geometry\") ");

        run("INSERT INTO \"ft1\" VALUES(0, 'POINT(0 0)', 0, 0.0, 'zero')");
        run("INSERT INTO \"ft1\" VALUES(1, 'POINT(1 1)', 1, 1.1, 'one')");
        run("INSERT INTO \"ft1\" VALUES(2, 'POINT(2 2)', 2, 2.2, 'two')");


    }

    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return new TeradataDataStoreFactory();
    }


}
