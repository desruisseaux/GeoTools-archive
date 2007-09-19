package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.util.CommonsConverterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Abstract test suite for jdbc datastores.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class JDBCConformanceTestSupport extends TestCase {

	/**
	 * data
	 */
	DataSource dataSource;
	/**
	 * datastore
	 */
	JDBCDataStore dataStore;
	/**
	 * feature types 
	 */
	FeatureType point,line,polygon;
	FeatureCollection points,lines,polygons;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//create the datasource
		dataSource = createDataSource();
		
		//create the datastore
		dataStore = createDataStore();
		dataStore.setFeatureFactory( new SimpleFeatureFactoryImpl() );
		dataStore.setTypeFactory( new SimpleTypeFactoryImpl() );
		dataStore.setFilterFactory( CommonFactoryFinder.getFilterFactory( null ) );
		
		dataStore.setDataSource( dataSource );
		
		//create the data
		SimpleTypeBuilder tb = 
			new SimpleTypeBuilder( dataStore.getTypeFactory() );
		
		tb.setName( "point" );
		tb.setNamespaceURI( "http://geotools.org/jdbc" );
		
		tb.attribute( "geometry", Point.class );
		tb.attribute( "intProperty", Integer.class );
		point = tb.buildFeatureType();
		
		tb.init();
		tb.setName( "line" );
		tb.attribute( "geometry", LineString.class );
		tb.attribute( "doubleProperty", Double.class );
		line = tb.buildFeatureType();
		
		tb.init();
		tb.setName( "polygon" );
		tb.attribute( "geometry", Polygon.class );
		tb.attribute( "stringProperty", String.class );
		polygon = tb.buildFeatureType();
	
		SimpleFeatureBuilder fb = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		GeometryFactory gf = new GeometryFactory();

		points = new DefaultFeatureCollection(null,null){};

		fb.setType( point );
		fb.add( gf.createPoint( new Coordinate( 0, 0 ) )  );
		fb.add( new Integer( 0 ) );
		points.add( fb.buildFeature("fid.0"));
		
		fb.init();
		fb.add( gf.createPoint( new Coordinate( 1, 1 ) )  );
		fb.add( new Integer( 1 ) );
		points.add( fb.buildFeature("fid.1"));
		
		fb.init();
		fb.add( gf.createPoint( new Coordinate( 2,2 ) )  );
		fb.add( new Integer( 2 ) );
		points.add( fb.buildFeature("fid.2"));
		
		lines = new DefaultFeatureCollection(null,null){};
		
		fb.setType( line );
		
		fb.init();
		fb.add( gf.createLineString(new Coordinate[]{ new Coordinate(0,0),new Coordinate(1,1)}));
		fb.add( new Double( 0.0 ) );
		lines.add( fb.buildFeature("fid.0"));
		
		fb.init();
		fb.add( gf.createLineString(new Coordinate[]{ new Coordinate(1,1),new Coordinate(2,2)}));
		fb.add( new Double( 1.1 ) );
		lines.add( fb.buildFeature("fid.1"));
		
		fb.init();
		fb.add( gf.createLineString(new Coordinate[]{ new Coordinate(2,2),new Coordinate(3,3)}));
		fb.add( new Double( 2.2 ) );
		lines.add( fb.buildFeature("fid.2"));
		
		//create the tables
		SQLBuilder sql = dataStore.createSQLBuilder();
		Connection connection = dataSource.getConnection();
		Statement st = connection.createStatement();
		try {
			st.execute( sql.dropTable( point ) );
		}
		catch( SQLException e ) {}
		st.execute( sql.createTable( point ) );
		
		try {
			st.execute( sql.dropTable( line ) );
		}
		catch( SQLException e ) {}
		st.execute( sql.createTable( line ) );
		
		try {
			st.execute( sql.dropTable( polygon ) );
		}
		catch( SQLException e ) {}
		st.execute( sql.createTable( polygon ) );
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Creates the data source.
	 */
	protected abstract DataSource createDataSource();
	
	/**
	 * Creates an instance of the datastore.
	 */
	protected abstract JDBCDataStore createDataStore();
	
	public void test() {
		
	}
}
