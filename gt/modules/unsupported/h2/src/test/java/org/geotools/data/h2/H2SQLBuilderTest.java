package org.geotools.data.h2;

import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.TypeName;
import org.opengis.filter.FilterFactory2;

public class H2SQLBuilderTest extends H2TestSupport {

	/**
	 * Factory for creating filters
	 */
	FilterFactory2 ff;
	
	/**
	 * The dsql encoder
	 */
	H2SQLBuilder encoder;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory( null );
	
		ContentEntry entry = 
			dataStore.getContent().entry( dataStore, new TypeName( "featureType1" ) );
		
		H2ContentState state = (H2ContentState) entry.getState( Transaction.AUTO_COMMIT );
		
		encoder = new H2SQLBuilder( state , ff );
	}
	
	public void testCreate() throws Exception {
		String sql = encoder.create();
		assertEquals( 
			"CREATE TABLE \"geotools\".\"featureType1\" (" + 
				" \"intProperty\" INTEGER, \"doubleProperty\" DOUBLE, \"stringProperty\" VARCHAR_IGNORECASE " +
			");",  sql );
	}
	
//	public void testEncodePropertyNameSimple() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Object data = encoder.visit( name, null );
//		
//		assertEquals( "\"intProperty\"", encoder.getSQL().toString() );
//		assertEquals( Integer.class, data );
//	}
//	
//	public void testEncodePropertyNameXpath1() throws Exception {
//		PropertyName name = ff.property( "//intProperty" );
//		Object data = encoder.visit( name, null );
//		
//		assertEquals( "\"intProperty\"", encoder.getSQL().toString() );
//		assertEquals( Integer.class, data );
//	}
//	
//	public void testEncodeLiteralInt1() throws Exception {
//		Literal literal = ff.literal( 2 );
//		encoder.visit( literal, Integer.class );
//		
//		assertEquals( "2", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralInt2() throws Exception {
//		Literal literal = ff.literal( 2 );
//		encoder.visit( literal, null );
//		
//		assertEquals( "2", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralDouble1() throws Exception {
//		Literal literal = ff.literal( 2.5 );
//		encoder.visit( literal, Double.class );
//		
//		assertEquals( "2.5", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralDouble2() throws Exception {
//		Literal literal = ff.literal( 2.0 );
//		encoder.visit( literal, null );
//		
//		assertEquals( "2.0", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralDouble3() throws Exception {
//		Literal literal = ff.literal( 2.5 );
//		encoder.visit( literal, Double.class );
//		
//		assertEquals( "2.5", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralDouble4() throws Exception {
//		Literal literal = ff.literal( 2 );
//		encoder.visit( literal, null );
//		
//		assertEquals( "2", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralBoolean1() throws Exception {
//		Literal literal = ff.literal( true );
//		encoder.visit( literal, Boolean.class );
//		
//		assertEquals( "true", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralBoolean2() throws Exception {
//		Literal literal = ff.literal( true );
//		encoder.visit( literal, null );
//		
//		assertEquals( "true", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralBoolean3() throws Exception {
//		Literal literal = ff.literal( 1 );
//		encoder.visit( literal, Boolean.class );
//		
//		assertEquals( "true", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralBoolean4() throws Exception {
//		Literal literal = ff.literal( 0 );
//		encoder.visit( literal, Boolean.class );
//		
//		assertEquals( "false", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLiteralString() throws Exception {
//		Literal literal = ff.literal( 2 );
//		encoder.visit( literal, String.class );
//		
//		assertEquals( "'2'", encoder.getSQL().toString() );
//	}
//
//	
//	public void testEncodePropertyIsEqualTo1() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal literal = ff.literal( "2" );
//		
//		PropertyIsEqualTo equalTo = ff.equals( name, literal );
//		encoder.visit( equalTo, null );
//
//		assertEquals( "( \"intProperty\" ) = ( 2 )" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeAnd1() throws Exception {
//		Filter left = ff.equals(
//			ff.property( "intProperty" ), ff.literal( "2" ) 
//		);
//		Filter right = ff.equals(
//			ff.property( "stringProperty" ), ff.literal( "hello" ) 
//		);
//		And and = ff.and( left, right );
//		encoder.visit( and, null );
//		
//		assertEquals( 
//			"( ( \"intProperty\" ) = ( 2 ) ) AND ( ( \"stringProperty\" ) = ( 'hello' ) )", encoder.getSQL().toString() 
//		);
//	}
//	
//	public void testEncodeAnd2() throws Exception {
//		Filter single = ff.equals(
//			ff.property( "intProperty" ), ff.literal( "2" ) 
//		);
//		List children = new ArrayList();
//		children.add( single );
//		
//		And and = ff.and( children );
//		encoder.visit( and, null );
//		
//		assertEquals( "( \"intProperty\" ) = ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeOr() throws Exception {
//		Filter left = ff.equals(
//			ff.property( "intProperty" ), ff.literal( "2" ) 
//		);
//		Filter right = ff.equals(
//			ff.property( "stringProperty" ), ff.literal( "hello" ) 
//		);
//		Or or = ff.or( left, right );
//		encoder.visit( or, null );
//		
//		assertEquals( 
//			"( ( \"intProperty\" ) = ( 2 ) ) OR ( ( \"stringProperty\" ) = ( 'hello' ) )", encoder.getSQL().toString() 
//		);
//	}
//	
//	public void testEncodeId1() throws Exception {
//		HashSet fids 
//			= new HashSet( Arrays.asList( new Object[] { ff.featureId( "fid" ) } ) );
//		
//		Id id = ff.id( fids );
//		
//		encoder.visit( id , null);
//		
//		assertEquals( "( \"fid\" ) = ( 'fid' )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeId2() throws Exception {
//		//set a multi fid mapper
//		/*
//		FIDMapper mapper =  new MultiColumnFIDMapper( 
//			"public", "test", new String[]{"fid1","fid2"},new int[]{Types.VARCHAR,Types.INTEGER},
//			new int[]{ 100, 100 }, new int[]{0,0}, new boolean[]{false,false}
//		);
//		dataStore.setFIDMapper( "test", mapper );
//		*/
//		HashSet fids 
//		= new HashSet( Arrays.asList( new Object[] { ff.featureId( "fid&2" ) } ) );
//	
//		Id id = ff.id( fids );
//		
//		encoder.visit( id , null);
//		
//		assertEquals( "( ( \"fid1\" ) = ( 'fid' ) ) AND ( ( \"fid2\" ) = ( 2 ) )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeNot() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal literal = ff.literal( "2" );
//		
//		PropertyIsEqualTo equalTo = ff.equals( name, literal );
//		Not not = ff.not( equalTo );
//		
//		encoder.visit( not, null );
//
//		assertEquals( "NOT ( ( \"intProperty\" ) = ( 2 ) )" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeBetween1() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal lower = ff.literal( "1" );
//		Literal upper = ff.literal( "2" );
//		
//		PropertyIsBetween between = ff.between( name, lower, upper );
//		encoder.visit( between, null );
//
//		assertEquals( "( \"intProperty\" ) BETWEEN ( 1 ) AND ( 2 )" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeBetween2() throws Exception {
//		PropertyName name = ff.property( "stringProperty" );
//		Literal lower = ff.literal( "1" );
//		Literal upper = ff.literal( "2" );
//		
//		PropertyIsBetween between = ff.between( name, lower, upper );
//		encoder.visit( between, null );
//
//		assertEquals( "( \"stringProperty\" ) BETWEEN ( '1' ) AND ( '2' )" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLike1() throws Exception {
//		PropertyName name = ff.property( "stringProperty" );
//		PropertyIsLike like = ff.like( name, "h?el*lo"  );
//		
//		encoder.visit( like, null );
//
//		assertEquals( "\"stringProperty\" LIKE 'h_el%lo'" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLike2() throws Exception {
//		PropertyName name = ff.property( "stringProperty" );
//		PropertyIsLike like = ff.like( name, "h\\?el*lo");
//		
//		encoder.visit( like, null );
//
//		assertEquals( "\"stringProperty\" LIKE 'h?el%lo'" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeLike3() throws Exception {
//		PropertyName name = ff.property( "stringProperty" );
//		PropertyIsLike like = ff.like( name, "hSelMlo", "M","S", "E");
//		
//		encoder.visit( like, null );
//
//		assertEquals( "\"stringProperty\" LIKE 'h_el%lo'" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeNull() throws Exception {
//		PropertyName name = ff.property( "stringProperty" );
//		PropertyIsNull isNull = ff.isNull( name );
//		
//		encoder.visit( isNull, null );
//
//		assertEquals( "\"stringProperty\" IS NULL" , encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeBBOX1() throws Exception {
//		BBOX bbox = ff.bbox( "pointProperty", 0, 0, 1, 1, "EPSG:4326" );
//		encoder.visit( bbox, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))', 4326 )", 
//			encoder.getSQL().toString() 
//		);
//	}
//	
//	public void testEncodeBBOX2() throws Exception {
//		BBOX bbox = ff.bbox( "pointProperty", 0, 0, 1, 1, null );
//		encoder.visit( bbox, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))', 0 )", 
//			encoder.getSQL().toString() 
//		);
//	}
//	
//	public void testEncodeIntersects() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Intersects intersects = ff.intersects( name, literal );
//		encoder.visit( intersects, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) AND intersects( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeBeyond() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Beyond beyond = ff.beyond( name, literal, 10, null );
//		encoder.visit( beyond, null );
//		
//		assertEquals( 
//			"distance( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) ) > 10.0", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeDWithin() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		DWithin dwithin = ff.dwithin( name, literal, 10, null );
//		encoder.visit( dwithin, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND distance( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) ) <= 10.0", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeCrosses() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Crosses crosses = ff.crosses( name, literal );
//		encoder.visit( crosses, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND crosses( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeDisjoint() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Disjoint disjoint = ff.disjoint( name, literal );
//		encoder.visit( disjoint, null );
//		
//		assertEquals( 
//			"disjoint( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeEquals() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Equals equals = ff.equal( name, literal );
//		encoder.visit( equals, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND equals( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeOverlaps() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Overlaps overlaps = ff.overlaps( name, literal );
//		encoder.visit( overlaps, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND overlaps( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeTouches() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Touches touches = ff.touches( name, literal );
//		encoder.visit( touches, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND touches( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeWithin() throws Exception {
//		PropertyName name = ff.property( "pointProperty" );
//		Literal literal = 
//			ff.literal( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
//		
//		Within within = ff.within( name, literal );
//		encoder.visit( within, null );
//		
//		assertEquals( 
//			"\"pointProperty\" && GeometryFromText( 'POINT (0 0)', 0 ) " +
//			"AND within( ( \"pointProperty\" ) , ( GeometryFromText( 'POINT (0 0)', 0 ) ) )", 
//			encoder.getSQL().toString()
//		);
//	}
//	
//	public void testEncodeAdd1() throws Exception {
//		Literal l1 = ff.literal( 1 );
//		Literal l2 = ff.literal( 2 );
//		
//		Add add = ff.add( l1, l2 );
//		encoder.visit( add, null );
//		
//		assertEquals( "( 1 ) + ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeAdd2() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal literal = ff.literal( 2 );
//		
//		Add add = ff.add( name, literal );
//		encoder.visit( add, null );
//		
//		assertEquals( "( \"intProperty\" ) + ( 2 )", encoder.getSQL().toString() );
//		
//	}
//	
//	public void testEncodeAdd3() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal literal = ff.literal( "2" );
//		
//		Add add = ff.add( name, literal );
//		encoder.visit( add, null );
//		
//		assertEquals( "( \"intProperty\" ) + ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeAdd4() throws Exception {
//		Literal l1 = ff.literal( 1 );
//		Literal l2 = ff.literal( 2 );
//		Literal l3 = ff.literal( 3 );
//		
//		Add add1 = ff.add( l1, l2 );
//		Add add2 = ff.add( add1, l3 );
//		
//		encoder.visit( add2, null );
//		
//		assertEquals( "( ( 1 ) + ( 2 ) ) + ( 3 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeAdd5() throws Exception {
//		PropertyName name = ff.property( "intProperty" );
//		Literal literal = ff.literal( "2.5" );
//		
//		Add add = ff.add( name, literal );
//		encoder.visit( add, null );
//		
//		assertEquals( "( \"intProperty\" ) + ( 2.5 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeSubtract() throws Exception {
//		Literal l1 = ff.literal( 1 );
//		Literal l2 = ff.literal( 2 );
//		
//		Subtract sub = ff.subtract( l1, l2 );
//		encoder.visit( sub, null );
//		
//		assertEquals( "( 1 ) - ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeDivide() throws Exception {
//		Literal l1 = ff.literal( 1 );
//		Literal l2 = ff.literal( 2 );
//		
//		Divide div = ff.divide( l1, l2 );
//		encoder.visit( div, null );
//		
//		assertEquals( "( 1 ) / ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeMultiply() throws Exception {
//		Literal l1 = ff.literal( 1 );
//		Literal l2 = ff.literal( 2 );
//		
//		Multiply mul = ff.multiply( l1, l2 );
//		encoder.visit( mul, null );
//		
//		assertEquals( "( 1 ) * ( 2 )", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeIncludeFilter() throws Exception {
//		encoder.visit( Filter.INCLUDE, null );
//		assertEquals( "1 = 1", encoder.getSQL().toString() );
//	}
//	
//	public void testEncodeExcludeFilter() throws Exception {
//		encoder.visit( Filter.EXCLUDE, null );
//		assertEquals( "1 = 0", encoder.getSQL().toString() );
//	}
//	
//	public void testSelect() throws Exception {
//		Filter filter = ff.and( 
//			ff.equals( 
//				ff.property( "intProperty" ), ff.add( ff.literal( 1 ), ff.literal( 2 ) )
//			), 
//			ff.less( 
//				ff.property( "doubleProperty"), 
//				ff.subtract( ff.property( "intProperty"), ff.literal( 5 ) )
//			)
//		);
//		DefaultQuery query = 
//			new DefaultQuery( "test", filter, new String[]{ "pointProperty","stringProperty" } );
//		String sql = encoder.select( query );
//		assertEquals( 
//			"SELECT astext( force_2d( \"pointProperty\" ) ), \"stringProperty\" " + 
//			"FROM \"test\".\"test\" " + 
//			"WHERE ( ( \"intProperty\" ) = ( ( 1 ) + ( 2 ) ) ) " +
//			"AND ( ( \"doubleProperty\" ) < ( ( \"intProperty\" ) - ( 5 ) ) )", sql	
//		);
//	}
//	
//	public void testDelete() throws Exception {
//		Filter filter = ff.equals( ff.property( "intProperty" ), ff.literal( 2 ) );
//		String sql = encoder.delete( filter );
//		
//		assertEquals( "DELETE FROM \"test\".\"test\" WHERE ( \"intProperty\" ) = ( 2 )", sql );
//	}
//	
//	public void testUpdate() throws Exception {
//		String[] names = new String[]{ "pointProperty", "intProperty", "stringProperty" };
//		Object[] values = new Object[] {
//			new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) , new Integer( 2 ), "foo" 
//		};
//		Filter filter = ff.equals( ff.property( "intProperty" ), ff.literal( 2 ) );
//		DefaultQuery query = new DefaultQuery( "test", filter, names );
//		
//		String sql = encoder.update( query, values );
//		assertEquals( 
//			"UPDATE \"test\".\"test\" " +
//			"SET \"pointProperty\" = GeometryFromText( 'POINT (0 0)', 0 ), " +
//			"\"intProperty\" = 2, \"stringProperty\" = 'foo' " +
//			"WHERE ( \"intProperty\" ) = ( 2 )", sql
//		);
//	}
//	
//	public void testInsert() throws Exception {
//		String[] names = new String[]{ "pointProperty", "intProperty", "stringProperty" };
//		Object[] values = new Object[] {
//			new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) , new Integer( 2 ), "foo" 
//		};
//		
//		String sql = encoder.insert( names, values );
//		assertEquals( 
//			"INSERT INTO \"test\".\"test\" ( \"pointProperty\", \"intProperty\", \"stringProperty\" ) " +
//			"VALUES ( GeometryFromText( 'POINT (0 0)', 0 ), 2, 'foo' )", sql 
//		);
//	}
}
