package org.geotools.data.postgis;

import java.io.IOException;
import java.util.PropertyResourceBundle;

/**
 * Convenience class for postgis testing.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class PostgisTests {

	public static Fixture newFixture() throws IOException {
		PropertyResourceBundle resource;
        resource = new PropertyResourceBundle(
    		PostgisTests.class.getResourceAsStream("fixture.properties")
		);

        Fixture f = new Fixture();
        
        f.namespace = resource.getString("namespace");
        f.host = resource.getString("host");
        f.port = Integer.valueOf(resource.getString("port"));
        f.database = resource.getString("database");
        f.user = resource.getString("user");
        f.password = resource.getString("password");	
        
        return f;
	}
	
	public static class Fixture {
		public String namespace;
		public String host;
		public String database;
		public Integer port;
		public String user;
		public String password;
		
	}
}
