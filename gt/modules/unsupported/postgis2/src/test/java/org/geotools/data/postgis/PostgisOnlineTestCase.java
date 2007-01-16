/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.postgis;

import org.geotools.data.DataStore;
import org.geotools.data.store.ContentDataStore;
import org.geotools.test.OnlineTestCase;

/**
 * Abstract class for PostGIS online test cases.
 * <p>
 * This test case is currently set up using:
 * <ol>
 * <li>Postgres installed with "postgres" password :-)
 * <li>PostGIS installed into a "geotools" database :-)
 * </ul>
 * # located in C:\Documents and Settings\USER\.geotools\postgis\typical.properties
 * As indicated by the following example fixture:<pre><code>
 * namespace=http://www.geotools.org/data/postgis/
 * host=localhost
 * port=5432
 * database=geotools
 * user=postgres
 * password=postgres
 * schema=public
 * </code></pre>
 * 
 * @since 2.4
 * @author Cory Horner, Refractions Research
 */
public abstract class PostgisOnlineTestCase extends OnlineTestCase {

    protected ContentDataStore dataStore; //today only, you are a contentdatastore
    protected PostGISContent content;
    
    protected abstract String getFixtureId();

    protected void connect() throws Exception {
        try {
            content = new PostGISContent(fixture);
            dataStore = new PostgisDataStore(content);
        } catch (Exception t ){
            t.printStackTrace();
            throw (Exception)t;
        }
    }

    protected void disconnect() throws Exception {
        content = null;
        dataStore = null;
    }

}
