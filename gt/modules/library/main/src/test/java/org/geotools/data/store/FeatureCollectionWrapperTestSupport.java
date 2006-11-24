package org.geotools.data.store;

import junit.framework.TestCase;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleSchema;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class FeatureCollectionWrapperTestSupport extends TestCase {

	protected CoordinateReferenceSystem crs;
	protected FeatureCollection delegate;
	
	protected void setUp() throws Exception {
		crs = CRS.parseWKT( 
			"GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]" 
		);
		SimpleTypeBuilder typeBuilder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		typeBuilder.load( new SimpleSchema() );
		
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "test" );
		typeBuilder.setCRS( crs );
		typeBuilder.addAttribute( "defaultGeom", Point.class );
		typeBuilder.addAttribute( "someAtt", Integer.class );
		typeBuilder.addAttribute( "otherGeom", LineString.class );
		typeBuilder.setGeometryName( "defaultGeom" );
		
		FeatureType featureType = typeBuilder.feature();
		
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		
		GeometryFactory gf = new GeometryFactory();
		delegate = new DefaultFeatureCollection( "test", featureType ){};
		
		double x = -140;
		double y = 45;
		for ( int i = 0; i < 5; i++ ) {
			builder.init();
			builder.setType( featureType );
			
			Point point = gf.createPoint( new Coordinate( x+i, y+i ) );
			point.setUserData( crs );
			
			builder.add( point );
			builder.add( new Integer( i ) );
			
			LineString line = gf.createLineString( new Coordinate[] { new Coordinate( x+i, y+i ), new Coordinate( x+i+1, y+i+1 ) } );
			line.setUserData( crs );
			builder.add( line );
			
			delegate.add( builder.feature( i + "" ) );
		}
	}
}
