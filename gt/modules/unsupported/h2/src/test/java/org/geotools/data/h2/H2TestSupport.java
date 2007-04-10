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
package org.geotools.data.h2;

import junit.framework.TestCase;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.type.TypeName;
import org.geotools.filter.FilterFactoryImpl;


/**
 * Test support class for tests which run of a live h2 instance.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2TestSupport extends TestCase {
    /**
     * Embedded server instance, created statically to live over the life
     * of many test cases, with a shutdown hook to cleanup
     */
    static Server server;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    if (server != null) {
                        //stop the server
                        server.stop();

                        //kill the files
                        String dir = System.getProperty("user.dir");

                        try {
                            DeleteDbFiles.execute(dir, "geotools", true);
                        } catch (SQLException e) {
                        }
                    }
                }
            }));
    }

    /**
     * types created for tests
     */

    //two run of the mill feature types
    static TypeName ft1 = new TypeName("featureType1");
    static TypeName ft2 = new TypeName("featureType2");

    //a feature type with no geometry
    static TypeName nogeom = new TypeName("noGeometry");

    /**
     * The datastore
     */
    H2DataStore dataStore;

    /**
     * Runs an sql script against the database.
     */
    static void run(String script) throws Exception {
        //load the script
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    H2TestSupport.class.getResourceAsStream(script)));

        H2ConnectionPoolDataSource cpDataSource = new H2ConnectionPoolDataSource("jdbc:h2:geotools");

        //connect
        Connection conn = cpDataSource.getPooledConnection().getConnection();

        try {
            Statement st = conn.createStatement();

            String line = null;

            while ((line = reader.readLine()) != null) {
                st.execute(line);
            }

            reader.close();

            st.close();
        } finally {
            conn.close();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        if (server == null) {
            //create the server instance
            server = Server.createTcpServer(new String[] {  });
            server.start();

            //spatialy enable it
            run("h2.sql");
        }

        //set up the data
        run("featureTypes.sql");

        H2ConnectionPoolDataSource cpDataSource = new H2ConnectionPoolDataSource("jdbc:h2:geotools");

        //create the datastore
        H2Content content = new H2Content(cpDataSource);
        content.setDatabaseSchema("geotools");

        dataStore = new H2DataStore(content);
        dataStore.setNamespaceURI("http://www.geotools.org/test");
        dataStore.setTypeFactory(new SimpleTypeFactoryImpl());
        dataStore.setFeatureFactory(new SimpleFeatureFactoryImpl());
        dataStore.setFilterFactory(new FilterFactoryImpl());
        dataStore.setGeometryFactory(new GeometryFactory());
    }

    protected void tearDown() throws Exception {
        run("featureTypes-cleanup.sql");
    }
}
