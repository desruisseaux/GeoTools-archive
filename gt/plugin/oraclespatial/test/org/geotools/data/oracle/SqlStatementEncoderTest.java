package org.geotools.data.oracle;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureType;
import org.geotools.filter.SQLEncoderOracle;

public class SqlStatementEncoderTest extends TestCase {
	private SQLEncoderOracle encoder;
	private SqlStatementEncoder sql;

	protected void setUp() throws Exception {
	    encoder = new SQLEncoderOracle( 4326 );
		sql = new SqlStatementEncoder( encoder, "table", "fid" );
		super.setUp();
	}

	/*
	 * Test method for 'org.geotools.data.oracle.SqlStatementEncoder.makeCreateTableSQL(FeatureType)'
	 */
	public void testMakeCreateTableSQL() throws Exception {
		FeatureType schema = DataUtilities.createType( "ignore", "name:String, line:MultiLineString, measure:Integer");
		String create = sql.makeCreateTableSQL( schema );
		String expected =
			"CREATE TABLE table(fid NUMBER,name VARCHAR, line MDSYS.SDO_GEOMETRY, measure INTEGER);\n"+
		    "CREATE UNIQUE INDEX table_index ON (fid );\n"+
		    "CREATE INDEX table_sidx ON table( line) INDEXTYPE IS mdsys.spatial_index;";
		assertEquals( expected, create );	
	}

	/*
	 * Test method for 'org.geotools.data.oracle.SqlStatementEncoder.makeType(Class)'
	 */
	public void testMakeType() {

	}

	/*
	 * Test method for 'org.geotools.data.oracle.SqlStatementEncoder.makeCreateIndexSQL()'
	 */
	public void testMakeCreateIndexSQL() {

	}

}
