package org.geotools.brewer.color;

import java.io.IOException;
import java.util.PropertyResourceBundle;

/**
 * Convenience class for postgis testing.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/plugin/postgis/test/org/geotools/data/postgis/PostgisTests.java $
 */
public class PostgisTests {

	public static Fixture newFixture(String props) throws IOException {
		PropertyResourceBundle resource;
        resource = new PropertyResourceBundle(
    		PostgisTests.class.getResourceAsStream(props)
		);

        Fixture f = new Fixture();
        
        f.namespace = resource.getString("namespace");
        f.host = resource.getString("host");
        f.port = Integer.valueOf(resource.getString("port"));
        f.database = resource.getString("database");
        f.user = resource.getString("user");
        f.password = resource.getString("password");	
        f.schema = resource.getString("schema");
        
        if (f.schema == null || "".equals(f.schema.trim()))
        	f.schema = "public";
        return f;
	}
	
	public static Fixture newFixture() throws IOException {
		return newFixture("fixture.properties");
	}
	
	public static class Fixture {
		public String namespace;
		public String host;
		public String database;
		public Integer port;
		public String user;
		public String password;
		public String schema;
		
	}
}
