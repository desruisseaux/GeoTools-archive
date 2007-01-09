package org.geotools.data.postgis;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.geotools.data.Query;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.LikeFilterImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.BinaryExpression;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Encodes a PostGIS sql statement.
 * <p>
 * <br>
 * The sql encoder <b>requires</b> the feature type it is working against and 
 * the datastore using it to encode. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class PostgisSQLEncoder implements FilterVisitor, ExpressionVisitor {

	/**
	 * Filter factory used to create filters
	 */
	FilterFactory filterFactory;
	/** 
	 * The featuretype being encoded against.
	 */
	FeatureType featureType;
	/**
	 * The datastore
	 */
	PostgisDataStore dataStore;
	/**
	 * SQl Statement buffer
	 */
	StringBuffer sql;
	/**
	 * flag controlling wether  ByteA function is used to transfer WKB data 
	 */
	boolean usingByteA = false;
	/** 
	 * flag controlling wether WKB format is used instead of WKT 
	 */
	boolean usingWKB = false;
	/**
	 * Flag controlling wether GEOS is being used for encoding operations.
	 */
	boolean usingGEOS = true;
	
	/**
	 * Creates the sql encoder.
	 * 
	 * @param featureType The featureType the encoder works against.
	 * @param dataStore The datastore using the encoder.
	 * @param filterFactory A factory used to create filter objects.
	 */
	public PostgisSQLEncoder( FeatureType featureType, PostgisDataStore dataStore, FilterFactory filterFactory ) {
		this.featureType = featureType;
		this.dataStore = dataStore;
		this.filterFactory = filterFactory;
		
		sql = new StringBuffer();
	}
	
	/**
	 * @param flag controlling wether  ByteA function is used to transfer WKB data 
	 */
	public void setUsingByteA(boolean usingByteA) {
		this.usingByteA = usingByteA;
	}
	
	/**
	 * @return flag controlling wether  ByteA function is used to transfer WKB data
	 */
	public boolean isUsingByteA() {
		return usingByteA;
	}
	
	/**
	 * @param usingWKB flag controlling wether WKB format is used instead of WKT	
	 */
	public void setUsingWKB(boolean usingWKB) {
		this.usingWKB = usingWKB;
	}
	
	/**
	 * @return flag controlling wether WKB format is used instead of WKT
	 */
	public boolean isUsingWKB() {
		return usingWKB;
	}
	
	/**
	 * @return The filter capabilities of the encoder.
	 */
	public FilterCapabilities filterCapabilities() {
		FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addType(FilterCapabilities.NONE);
        capabilities.addType(FilterCapabilities.ALL);
        capabilities.addType(FilterCapabilities.FID);
        capabilities.addType(FilterCapabilities.NULL_CHECK);
        capabilities.addType(FilterCapabilities.BETWEEN);
        capabilities.addType(FilterCapabilities.LOGICAL);
        capabilities.addType(FilterCapabilities.SIMPLE_ARITHMETIC);
        capabilities.addType(FilterCapabilities.SIMPLE_COMPARISONS);
        capabilities.addType(FilterCapabilities.SPATIAL_BBOX);
        capabilities.addType(FilterCapabilities.LIKE);

        if (usingGEOS) {
        	capabilities.addType(FilterCapabilities.SPATIAL_BEYOND);
            capabilities.addType(FilterCapabilities.SPATIAL_CONTAINS);
            capabilities.addType(FilterCapabilities.SPATIAL_CROSSES);
            capabilities.addType(FilterCapabilities.SPATIAL_DISJOINT);
            capabilities.addType(FilterCapabilities.SPATIAL_DWITHIN);
            capabilities.addType(FilterCapabilities.SPATIAL_EQUALS);
            capabilities.addType(FilterCapabilities.SPATIAL_INTERSECT);
            capabilities.addType(FilterCapabilities.SPATIAL_OVERLAPS);
            capabilities.addType(FilterCapabilities.SPATIAL_TOUCHES);
            capabilities.addType(FilterCapabilities.SPATIAL_WITHIN);
        }
        
        return capabilities;
	}
	
	/**
	 * Encodes an sql SELECT statement from a query. 
	 * 
	 * @param query The query.
	 * 
	 * @return The encoded sql statement.
	 */
	public String select( Query query ) {
		sql = new StringBuffer();
		
		//select
		sql.append( "SELECT ");
		if ( query.getPropertyNames() == null || query.getPropertyNames().length == 0 ) {
			sql.append( "*" );
		}
		else {
			for ( int i = 0; i < query.getPropertyNames().length; i++ ) {
				PropertyName propertyName = filterFactory.property( query.getPropertyNames()[ i ] );
				
				//get the attribute type
				AttributeType attributeType = (AttributeType) propertyName.evaluate( featureType );
				if ( attributeType != null ) {
					//check for geometry, because it is encided differnently
					if ( Geometry.class.isAssignableFrom( attributeType.getType() ) ) {
						 if ( usingWKB ) {
							 //if using byta, wrap in 'encode'
							 if ( usingByteA ) {
								 sql.append("encode( " );	 
							 }
							 
							 sql.append( "asbinary( force_2d( ");
							 propertyName.accept( this, null );
							 sql.append( " ), 'XDR' )" );
							 
							 if ( usingByteA ) {
								sql.append( ", 'base64' )"); 
							 }
						 }
		                 else {
		                    sql.append( "astext( force_2d( " ); 
		                    propertyName.accept( this, null );
		                    sql.append( " ) )" );
		                }
					}
					else {
						//normal property , nothign special
						propertyName.accept( this, null );		
					}
				}
				
				if ( i < query.getPropertyNames().length - 1 ) {
					sql.append( ", " );
				}
			}
		}
		
		//from
		sql.append( " FROM " );
		name( dataStore.getDataStoreConfig().getDatabaseSchemaName(), sql );
		sql.append( "." );
		name( featureType.getTypeName(), sql );
		
		//where
		if ( query.getFilter() != null ) {
			sql.append( " WHERE " );
			query.getFilter().accept( this, null );
		}
		
		//order by
		if ( query.getSortBy() != null && query.getSortBy().length > 0 ) {
			sql.append( " ORDER BY " );
			for ( int i = 0; i < query.getSortBy().length; i++ ) {
				SortBy sortBy = query.getSortBy()[ i ];
				sortBy.getPropertyName().accept( this, null );
				if ( sortBy.getSortOrder() == SortOrder.DESCENDING ) {
					sql.append( " DESC" );
				}
				
				if ( i < query.getSortBy().length - 1 ) {
					sql.append( ", " );
				}
			}
		}
		
		return sql.toString();
	}
	
	/**
	 * Encodes an sql DELETE statement from a filter.
	 * 
	 * @param filter The filter dtermining what is to be deleted.
	 * 
	 * @return The encoded DELETE statement.
	 */
	public String delete( Filter filter ) {
		sql = new StringBuffer();
		sql.append( "DELETE FROM " );
		
		name( dataStore.getDataStoreConfig().getDatabaseSchemaName(), sql );
		sql.append( "." );
		name( featureType.getTypeName(), sql );
		
		sql.append( " WHERE " );
		filter.accept( this, null );
		
		return sql.toString();
	}
	
	/**
	 * Encodes an sql UPDATE statement from a query and a set of values.
	 * 
	 * @param query The query defining the properties to change and filter.
	 * @param values The new values.
	 * 
	 * @return The encoded UPDATE statement.
	 */
	public String update( Query query, Object[] values ) {
		sql = new StringBuffer();
		sql.append( "UPDATE " );
		
		//table name
		name( dataStore.getDataStoreConfig().getDatabaseSchemaName(), sql );
		sql.append( "." );
		name( featureType.getTypeName(), sql );
		
		sql.append( " SET " );
		for (int i = 0; i < values.length; i++) {
			//encode the name
			PropertyName propertyName = filterFactory.property( query.getPropertyNames()[ i ] );
			Class type = (Class) propertyName.accept( this, null );
			
			sql.append( " = " );
			
			//encode the value
			Literal literal = filterFactory.literal( values[ i ] );
            literal.accept( this, type );
           
            if ( i < values.length - 1 ) {
            	sql.append( ", " );
            }
         }

		//filter
		if ( query.getFilter() != null ) {
			sql.append( " WHERE " );	
			query.getFilter().accept( this, null );
		}
		
		return sql.toString();
	}
	
	/**
	 * Encodes an sql INSERT statement.
	 * 
	 * @param names The names of attributes ( columns )
	 * @param values The values to insert
	 * 
	 * @return The encoded INSERT statement.
	 */
	public String insert( String[] names, Object[] values ) {
		sql = new StringBuffer();
		sql.append( "INSERT INTO " );
		
		//table name
		name( dataStore.getDataStoreConfig().getDatabaseSchemaName(), sql );
		sql.append( "." );
		name( featureType.getTypeName(), sql );
		
		//column names
		sql.append( " ( " );
		if ( names == null ) {
			//assume all, 
			names = new String[ featureType.getAttributeCount() ];
			for ( int i = 0; i < names.length; i++ ) {
				names[ i ] = featureType.getAttributeType( i ).getName();
			}
		}
		if ( names.length != values.length ) {
			String msg = "Specified " + names.length + " names, but " + values.length + 
				" values";
			throw new IllegalArgumentException( msg );
		}
		
		AttributeType[] types = new AttributeType[ names.length ]; 
		for ( int i = 0; i < names.length; i++ ) {
			PropertyName propertyName = filterFactory.property( names[ i ] );
			propertyName.accept( this, null );
			
			if ( i < names.length - 1 ) {
				sql.append( ", " );
			}
			
			//evaluate to get attribute, used for encoding values
			types[ i ] = (AttributeType) propertyName.evaluate( featureType );
		}
		sql.append( " ) VALUES ( " );
		
		for ( int i = 0; i < values.length; i++ ) {
			Literal literal = filterFactory.literal( values[ i ] );
			AttributeType type = types[ i ];
			
			literal.accept( this, type.getType() );
			if ( i < values.length - 1 ) {
				sql.append( ", " );
			}
		}
		sql.append( " )" );
		
		return sql.toString();
	}
	
	/**
	 * @return The String buffer being used to encode the sql statement.
	 */
	public StringBuffer getSQL() {
		return sql;
	}
	
	//
	// Start of non-public api
	//
	
	//
	//  visitor methods
	//
	public Object visitNullFilter(Object data) {
		return null;
	}

	public Object visit(ExcludeFilter exclude, Object data) {
		sql.append( "1 = 0" );
		return data;
	}

	public Object visit(IncludeFilter include, Object data) {
		sql.append( "1 = 1" );
		return data;
	}

	//
	// logical operators
	//
	public Object visit(Not not, Object data) {
		sql.append( "NOT ( " );
		data = not.getFilter().accept( this, data );
		sql.append( " )" );
		
		return data;
	}

	public Object visit(And and, Object data) {
		return visit( and, data, "AND" );
	}

	public Object visit(Or or, Object data) {
		return visit( or, data, "OR" );
	}
	
	protected Object visit( BinaryLogicOperator logic, Object data, String operator ) {
		
		boolean parentheses = logic.getChildren().size() > 1;
		
		//encode each child
		for ( Iterator f = logic.getChildren().iterator(); f.hasNext(); ) {
			Filter filter = (Filter) f.next();
			
			if ( parentheses )
				sql.append( "( " );
			
			data = filter.accept( this, data );
			
			if ( parentheses )
				sql.append( " )" );
			
			if ( f.hasNext() ) {
				sql.append( " " + operator + " " );
			}
		}
		
		return data;
	}

	
	
	//
	//id filter
	//
	public Object visit(Id id, Object data) {
		//get a fid mapper from the datastore
		FIDMapper mapper;
		try {
			mapper = dataStore.getFIDMapper( featureType.getTypeName() );
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
		
		//prepare each column as a property name
		PropertyName[] columnNames = new PropertyName[ mapper.getColumnCount() ];
		for ( int i = 0; i < columnNames.length; i++ ) {
			columnNames[ i ] = filterFactory.property( mapper.getColumnName( i ) );
		}
		
		//process each fid, creating an OR filter
		List or = new ArrayList();
		for ( Iterator f = id.getIDs().iterator(); f.hasNext(); ) {
			String fid = (String) f.next();
			
			//map to values
			Object[] values;
			try {
				values = mapper.getPKAttributes( fid );
			} 
			catch (IOException e) {
				throw new RuntimeException( e );
			}
			
			//create an "AND" filter for each property name
			List and = new ArrayList();
			for ( int i = 0; i < values.length; i++ ) {
				//create an equalTo filter
				PropertyName name = columnNames[ i ];
				Literal literal = filterFactory.literal( values[ i ] );
				and.add( filterFactory.equals( name, literal ) );
			}
			
			//add to or
			or.add( filterFactory.and( and ) );
		}
		
		//encode it
		return filterFactory.or( or ).accept( this, data );
	}

	//
	// binary comparison operators
	//
	public Object visit(PropertyIsBetween between, Object data) {
		sql.append( "( ");
		
		data = between.getExpression().accept( this, data );
		sql.append( " ) BETWEEN ( " );
		
		data = between.getLowerBoundary().accept( this, data );
		sql.append( " ) AND ( " );
		        
        data = between.getUpperBoundary().accept(this, data );
        sql.append( " )" );
        
        return data;
	}

	public Object visit(PropertyIsEqualTo equalTo, Object data) {
		return visit( equalTo, data, "=" );
	}

	public Object visit(PropertyIsNotEqualTo notEqualTo, Object data) {
		return visit( notEqualTo, data, "!=" );
	}

	public Object visit(PropertyIsGreaterThan greaterThan, Object data) {
		return visit( greaterThan, data, ">" );
	}

	public Object visit(PropertyIsGreaterThanOrEqualTo greaterThanOrEqualTo, Object data) {
		return visit( greaterThanOrEqualTo, data, ">=" );
	}

	public Object visit(PropertyIsLessThan lessThan, Object data) {
		return visit( lessThan, data, "<" );
	}

	public Object visit(PropertyIsLessThanOrEqualTo lessThanOrEqualTo, Object data) {
		return visit( lessThanOrEqualTo, data, "<=" );
	}

	protected Object visit( BinaryComparisonOperator comparison, Object data, String operator ) {
		
		sql.append( "( " );
		
		//left
		data = comparison.getExpression1().accept( this, data );
		
		//operator
		sql.append( " ) " + operator + " ( " );
		
		//right
		data = comparison.getExpression2().accept( this, data );
		
		sql.append( " )" );
		
		return data;
	}
	
	public Object visit(PropertyIsLike like, Object data) {
		String pattern = LikeFilterImpl.convertToSQL92( 
			like.getEscape().charAt( 0 ), like.getWildCard().charAt( 0 ), 
			like.getSingleChar().charAt( 0 ), like.getLiteral()	
		);
		
		//evaluate left hande
    	data = like.getExpression().accept( this, data );
    	
    	sql.append( " LIKE " );
    	
    	//encode pattern as string
    	string( pattern, sql );
    	
    	return data;
	}

	public Object visit(PropertyIsNull isNull, Object data) {
		data = isNull.getExpression().accept( this, data );
		sql.append( " IS " );
		nil( sql );
		
		return data;
	}

	//
	// spatial operators
	//
	public Object visit(BBOX bbox, Object data) {
		//create a property name + geometry
		PropertyName name = filterFactory.property( bbox.getPropertyName() );
		Envelope e = new Envelope( bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY() );
		
		if ( bbox.getSRS() != null ) {
			CoordinateReferenceSystem crs;
			try {
				crs = CRS.decode( bbox.getSRS() );
				data = crs;
			} 
			catch( Exception ex ) {
				PostgisDataStore.LOGGER.log( Level.WARNING, ex.getLocalizedMessage(), ex );
			}
			
		}
		
		return visit( name, JTS.toGeometry( e ), data );
	}

	protected Object visit( PropertyName name, Geometry geometry , Object data ) {
		//encode the property ( dont carry back data )
		name.accept( this, data );
		
		//bbox operator
		sql.append( " && " );
		
		//encode the geometry
		Literal literal = filterFactory.literal( geometry );
		return literal.accept( this, data );
	}
	
	public Object visit(Beyond beyond, Object data) {
		return visit( beyond, data, ">", false );
	}

	public Object visit(DWithin dwithin, Object data) {
		return visit( dwithin, data, "<=" , true );
	}
	
	protected Object visit( DistanceBufferOperator distance, Object data, String operator, boolean index ) {
		data = visit( (BinarySpatialOperator) distance, data, "distance", index );
		sql.append( " " + operator + " " + distance.getDistance() );
		
		return data;
	}
	
	public Object visit(Disjoint disjoint, Object data) {
		return visit( disjoint, data, "disjoint", false );	
	}
	
	public Object visit(Contains contains, Object data) {
		return visit( contains, data, "contains", true );
	}

	public Object visit(Crosses crosses, Object data) {
		return visit( crosses, data, "crosses" , true );
	}

	public Object visit(Equals equals, Object data) {
		return visit( equals, data, "equals", true );
	}

	public Object visit(Intersects intersects, Object data) {
		return visit( intersects, data, "intersects", true );
	}

	public Object visit(Overlaps overlaps, Object data) {
		return visit( overlaps, data, "overlaps" , true );
	}

	public Object visit(Touches touches, Object data) {
		return visit( touches, data, "touches", true  );
	}

	public Object visit(Within within, Object data) {
		return visit( within, data, "within", true  );
	}

	protected Object visit( BinarySpatialOperator comparison, Object data, String operator, boolean index ) {
		
		//use the index for this comparison?
		if ( index ) {
			//figure out the property name, and geometry
			PropertyName propertyName = null;
			Geometry geometry = null;
			
			if ( comparison.getExpression1() instanceof PropertyName ) {
				propertyName = (PropertyName) comparison.getExpression1();
				geometry = (Geometry) comparison.getExpression2().evaluate( null, Geometry.class );
			}
			else if ( comparison.getExpression2() instanceof PropertyName ) {
				propertyName = (PropertyName) comparison.getExpression2(); 
				geometry = (Geometry) comparison.getExpression1().evaluate( null, Geometry.class );
			}
			
			if ( propertyName != null && geometry != null ) {
				data = visit( propertyName , geometry, data );
				sql.append( " AND " );
			}
		}
		
		//operator
		sql.append( operator + "( ( " );
		
		//left
		data = comparison.getExpression1().accept( this, data );
		
		sql.append( " ) , ( " );
		
		//right
		data = comparison.getExpression2().accept( this, data );
	
		sql.append( " ) )" );
		
		return data;
	}
	
	public Object visit(NilExpression nil, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	//
	// Arithmetic operators
	//
	public Object visit(Add add, Object data) {
		return visit( add, data, "+" );
	}
	
	public Object visit(Subtract subtract, Object data) {
		return visit( subtract, data, "-" );
	}

	public Object visit(Multiply multiply, Object data) {
		return visit( multiply, data, "*" );
	}
	
	public Object visit(Divide divide, Object data) {
		return visit( divide, data, "/" );
	}

	protected Object visit( BinaryExpression expression, Object data, String operator ) {
		sql.append( "( " );
		data = expression.getExpression1().accept( this, data );
		
		sql.append( " ) " + operator + " ( " );
		data = expression.getExpression2().accept( this, data );
		
		sql.append( " )" );
		return data;
	}
	
	public Object visit(Function function, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Encodes a literal. 
	 * <p>
	 * 
	 * </p>
	 */
	public Object visit(Literal literal, Object data) {
		Object value = null;
		
		//was the type passed in as a hint?
		if ( data instanceof Class ) {
			//special case for numerics, we want to avoid truncation, so we 
			// convert to largest possible value space, and let the database
			// do the conversions it needs to
			if ( Number.class.isAssignableFrom( (Class) data ) ) {
				value = literal.evaluate( null, BigDecimal.class );
			}
			else {
				//non numeric, just convert
				value = literal.evaluate( null, (Class) data );	
			}
			
		}
		else {
			//just evaluate
			value = literal.evaluate( null );
		}
		
		//check for string
		if ( value instanceof String ) {
			string( (String) value, sql );
			return data;
		}
		
		//check for geometry
		if ( value instanceof Geometry ) {
			//figure out srid
			CoordinateReferenceSystem crs = null;
			
			//passed in?
			if ( data instanceof CoordinateReferenceSystem ) {
				crs = (CoordinateReferenceSystem) data;
			}
			else {
				//check the feature type
				if ( featureType.getDefaultGeometry() != null ) {
					crs = featureType.getDefaultGeometry().getCoordinateSystem();
				}
			}

			//convert to a postgis srid
			String srid = null;
			if ( crs != null ) {
				srid = encode( crs );
			}
			
			geometry( (Geometry) value, srid, sql );
			return data;
		}
		
		//convert to string
		String encoded = (String) Converters.convert( value, String.class );
		if ( encoded == null ) {
			//fall back to toString()
			encoded = value.toString();
		}
		
		sql.append( encoded );
		return data;
	}

	/**
	 * Helper method to encode a crs. Can we have this on CRS?
	 */
	String encode(CoordinateReferenceSystem crs) {
	    	if (crs == null) {
	            return null;
	        }

	    	for (Iterator i = crs.getIdentifiers().iterator(); i.hasNext();) {
	            Identifier id = (Identifier) i.next();
	            
	            return id.toString();
	    	}    

	        return null;	
	    }

	/**
	 * Encodes a {@link PropertyName} in an sql statement.
	 * <p>
	 * If the AttributeType can be infered from {@link #featureType} then its name 
	 * is used directly.
	 * </p>
	 * 
	 * @return The class binding of the attribute type, or <code>null</code> if unknown.
	 */
	public Object visit(PropertyName propertyName, Object data) {
		//1. evaluate against the type to get the AttributeType
		AttributeType attributeType = (AttributeType) propertyName.evaluate( featureType );
		if ( attributeType != null ) {
			//encode the name of the attribute, not the property name itself
			name( attributeType.getName(), sql );
			
			//return the type as the return 
			return attributeType.getType();
		}
		
		//2. not in type, could it be a primary key?
		FIDMapper mapper = null;
		try {
			mapper = dataStore.getFIDMapper( featureType.getTypeName() );
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
		
		//serach for the property in the primary key, encode it and return its type
		for ( int i = 0; i < mapper.getColumnCount(); i++ ) {
			String name = mapper.getColumnName( i );
			if ( propertyName.getPropertyName().equals( name ) ) {
				
				//encode it
				name( propertyName.getPropertyName(), sql );
				
				//return the type
				Integer type = new Integer( mapper.getColumnType( i ) );
				return JDBC1DataStore.TYPE_MAPPINGS.get( type );
			}
		}
		
		//3. give up, just encode directly
		name( propertyName.getPropertyName(), sql );
		
		//return nothing
		return null;
	}


	/**
	 * Encodes a name to be used in an sql statement.
	 * <p>
	 * This implementation wraps the name in double quotes.
	 * <p>
	 * 
	 * @param raw The raw name.
	 * @param sql The sql statement buffer
	 * 
	 */
	protected void name( String raw, StringBuffer sql ) {
		sql.append( "\"" + raw + "\"" );
	}
	
	/**
	 * Encodes a string to be used in an sql statement.
	 * 
	 * @param raw The raw string.
	 * @param sql The sql statement buffer
	 * 
	 */
	protected void string( String raw, StringBuffer sql ) {
		sql.append( "'" + raw + "'" );
	}
	
	/**
	 * Encodes a geometry with a known srid to be used in an sql statement.
	 * 
	 * @param geometry The geometry to encode.
	 * @param srid The spatial reference id of the geometry
	 * @param sql HTe sql statement buffer
	 */
	protected void geometry( Geometry geometry, String srid, StringBuffer sql ) {
		String wkt = new WKTWriter().write( geometry );
		sql.append( "GeometryFromText( '" + wkt + "', " ); 
		srid( srid, sql );
		sql.append( " )" );
	}
	
	/**
	 * Encodes a srid to be used in an sql statement
	 * 
	 * @param srid The raw spatial reference id of the geometry
	 * @param sql HTe sql statement buffer
	 */
	protected void srid( String raw, StringBuffer sql ) {
		if ( raw == null ) {
			sql.append( "0" );
		}
		else if ( raw.matches( "[0-9]+" ) ) {
			sql.append( raw );
		}
		else if ( raw.matches( ".*:[0-9]+" ) ) {
			raw = raw.replaceAll( ".*:([0-9]+)", "$1");
			sql.append( raw );
		}
		
	}
	
	/**
	 * Encodes <code>nulL</code> to be used in an sql statement.
	 * 
	 * @param sql The sql statement buffer
	 */
	protected void nil( StringBuffer sql ) {
		sql.append( "NULL" );
	}
}
