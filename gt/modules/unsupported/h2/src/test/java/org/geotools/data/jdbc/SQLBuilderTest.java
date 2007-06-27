package org.geotools.data.jdbc;

import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;


/**
 * Test case for {@link SQLBuilder}.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class SQLBuilderTest extends JDBCTestSupport {

	SQLBuilder builder;
	FeatureType source;
	Filter filter;
	
	protected void setUp() throws Exception {
		super.setUp();

		source = dataStore.getSchema("ft1");
		builder = new SQLBuilder( dataStore );
		
		FilterFactory ff = dataStore.getFilterFactory();
		filter = ff.equal( ff.property( "intProperty"), ff.literal( 1 ), false );
	}
	
	public void testCreateTable() throws Exception {
		String sql = builder.createTable( source );
		
		assertEquals("CREATE TABLE \"geotools\".\"ft1\" ( \"geometry\" OTHER, \"intProperty\" INTEGER, \"doubleProperty\" DOUBLE, \"stringProperty\" VARCHAR_IGNORECASE )", sql );
	}
	
	public void testBounds() throws Exception {
		String sql = builder.bounds( source );
		
		assertEquals( "SELECT envelope(\"geometry\") FROM \"geotools\".\"ft1\"", sql );
	}
	
	public void testBoundsWithFilter() {
		String sql = builder.bounds( source, filter );
		
		assertEquals( "SELECT envelope(\"geometry\") FROM \"geotools\".\"ft1\"" + 
			" WHERE ( \"intProperty\" ) = ( 1 )", sql );
	}
	
	public void testCount() throws Exception {
		String sql = builder.count(source);
		
		assertEquals( "SELECT count(*) FROM \"geotools\".\"ft1\"", sql );
	}
	
	public void testCountWithFilter() throws Exception {
		String sql = builder.count( source,filter );
		
		assertEquals( "SELECT count(*) FROM \"geotools\".\"ft1\"" + 
			" WHERE ( \"intProperty\" ) = ( 1 )", sql );
	}
	
	public void testSelect() throws Exception {
		String sql = builder.select( source,(Filter) null );
		
		System.out.println( sql );
		assertEquals( "SELECT \"id\", \"geometry\", \"intProperty\", \"doubleProperty\", " +
			"\"stringProperty\" FROM \"geotools\".\"ft1\"", sql );
	}
}
