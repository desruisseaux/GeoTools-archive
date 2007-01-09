package org.geotools.data.postgis;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.test.OnlineTestCase;

public abstract class PostgisOnlineTestCase extends OnlineTestCase {

	protected DataStore dataStore;
	
	protected abstract String getFixtureId();
	
	protected void connect() throws Exception {
		Map params = new HashMap();

		params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
		params.put(PostgisDataStoreFactory.HOST.key, fixture.getProperty("host"));
		params.put(PostgisDataStoreFactory.PORT.key, fixture.getProperty("port"));
		params.put(PostgisDataStoreFactory.SCHEMA.key, fixture.getProperty("schema"));
		params.put(PostgisDataStoreFactory.DATABASE.key, fixture.getProperty("database"));
		params.put(PostgisDataStoreFactory.USER.key, fixture.getProperty("user"));
		params.put(PostgisDataStoreFactory.PASSWD.key, fixture.getProperty("password"));
		
		dataStore = new PostgisDataStoreFactory().createDataStore(params);
	}
	
	protected void disconnect() throws Exception {
		//TODO: disconnect
	}

}
