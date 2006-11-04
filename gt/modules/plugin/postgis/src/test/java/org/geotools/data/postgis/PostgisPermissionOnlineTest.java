/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.Transaction;
import org.geotools.filter.Filter;

import junit.framework.TestCase;

/**
 * 
 * @author Justin
 */
public class PostgisPermissionOnlineTest extends TestCase {

	DataStore dataStore;
	
	protected void setUp() throws Exception {
		PostgisTests.Fixture f = 
			PostgisTests.newFixture("restricted.properties");
		
		Map params = new HashMap();

		params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
		params.put(PostgisDataStoreFactory.HOST.key, f.host);
		params.put(PostgisDataStoreFactory.PORT.key, f.port);
		params.put(PostgisDataStoreFactory.DATABASE.key, f.database);
		params.put(PostgisDataStoreFactory.USER.key, f.user);
		params.put(PostgisDataStoreFactory.PASSWD.key, f.password);
		params.put(PostgisDataStoreFactory.SCHEMA.key,f.schema);
		
		dataStore = new PostgisDataStoreFactory().createDataStore(params);
	}
	
	public void testGetFeatureSource() throws IOException {
		try {
			dataStore.getFeatureSource("restricted");
			fail("user should not have been able to create featureSource to restricted table");		} 
		catch (DataSourceException e) {}
	}
	
	public void testGetFeatureWriter() throws IOException {
		try {
			dataStore.getFeatureWriter("restricted", Filter.EXCLUDE, Transaction.AUTO_COMMIT);
			fail("user should not have been able to create featureWriter to restricted table");		
		} 
		catch (DataSourceException e) {}
		
		try {
			dataStore.getFeatureWriter("restricted", Transaction.AUTO_COMMIT);
			fail("user should not have been able to create featureWriter to restricted table");		
		} 
		catch (DataSourceException e) {}
		
		try {
			dataStore.getFeatureWriterAppend("restricted", Transaction.AUTO_COMMIT);
			fail("user should not have been able to create featureWriter to restricted table");		
		} 
		catch (DataSourceException e) {}
	}
	
	public void testGetSchema() throws IOException {
		try {
			dataStore.getSchema("restricted");
			fail("user should not have been able to create featureWriter to restricted table");		
		} 
		catch (DataSourceException e) {}
	}
}
