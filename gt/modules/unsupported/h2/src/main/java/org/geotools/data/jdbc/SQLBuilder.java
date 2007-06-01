package org.geotools.data.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
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

public class SQLBuilder implements ExpressionVisitor, FilterVisitor {
	/**
	 * The data store
	 */
	JDBCDataStore dataStore;
	/**
     * The feature source which sql is being encoded against. 
     */
    JDBCFeatureSource featureSource;
    
	/**
     * Filter factory used to create filters
     */
    FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory( null );

    /**
     * SQL statement buffer
     */
    StringBuffer sql;
    
    /**
     * Post-processing filter
     */
    Filter postFilter;
    
    public SQLBuilder( JDBCDataStore dataStore ) {
    	this( dataStore, null );
    }
    
    public SQLBuilder( JDBCDataStore dataStore, JDBCFeatureSource featureSource ) {
    	this.dataStore = dataStore;
    	this.featureSource = featureSource;
    }
	
    public void setFeatureSource(JDBCFeatureSource featureSource) {
		this.featureSource = featureSource;
	}
    
    public JDBCFeatureSource getFeatureSource() {
		return featureSource;
	}
    
    public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}
    
    public FilterFactory getFilterFactory() {
		return filterFactory;
	}
    
    /**
     * Helper method for getting the feature type being worked against.
     */
	protected FeatureType featureType() {
		return featureSource.getSchema();
	}
    
	/**
     * Initializes the state of the builder.
     */
    protected void init() {
        sql = new StringBuffer();
        postFilter = null;
    }
    
    //
    // Methods for encoding full statements
    /**
     * Builds a "CREATE TABLE" statement.
     * <p>
     * 
     * </p>
     * @param featureType The feature type to create the table from.
     *
     * @return A statement of the form "CREATE TABLE ...".
     */
	public String createTable( FeatureType featureType ) {
		init();

	    sql.append("CREATE TABLE ");

	    //table name
	    table( featureType.getTypeName() );

	    //table definition
	    String[] sqlTypeNames = null;

	    try {
	        sqlTypeNames = JDBCUtils.sqlTypeNames( featureType, dataStore );
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }

	    sql.append(" ( ");

	    for (int i = 0; i < sqlTypeNames.length; i++) {
	        AttributeType type = featureType.getAttributeType(i);

	        //the column name
	        name(type.getName());
	        sql.append(" ");

	        //sql type name
	        sql.append(sqlTypeNames[i]);

	        if (i < (sqlTypeNames.length - 1)) {
	            sql.append(", ");
	        }
	    }

	    sql.append(" )");
	    return sql.toString();
	}
	
	/**
     * Builds a "DROP TABLE" statement.
     * <p>
     * 
     * </p>
     * @param featureType The feature type to create the table from.
     *
     * @return A statement of the form "DROP TABLE ...".
     */
	public String dropTable( FeatureType featureType ) {
		init();
		
		sql.append("DROP TABLE ");

	    //table name
	    table( featureType.getTypeName() );
	    
	    return sql.toString();
	}
	
	/**
     * Builds a query which selects the envelope or bounding box of every 
     * feature / row in a table. 
     *
     * @return A statement of the form "SELECT envelope(...) FROM ..."
     */
	public String bounds() {
		return bounds( null );
	}
	
	/**
     * Builds a query which selects the envelope or bounding box of a subset of 
     * features / rows in a table. 
     * <p>
     * The <tt>filter</tt> argument can be used to filter those rows / features
     * returned or can be <code>null</code> to return all rows.
     * </p>
     *
     * @return A statement of the form "SELECT envelope(...) FROM ... WHERE ..."
     */
	public String bounds( Filter filter ) {
		init();
		
		//select
		sql.append("SELECT envelope(");
		geometry();
		sql.append( ")");
		
		//from
		from();
		
		//where
		if ( filter != null && filter != Filter.INCLUDE ) {
			where( filter );
		}
		
		return sql.toString();
	}
	
	/**
     * Builds a query which selects the bound of all rows / features in a table.
     *
     * @return A statement of the form "SELECT count(*) FROM ..."
     */
	public String count() {
		return count( null );
	}
	
	/**
     * Builds a query which selects the count of a subset of rows / features in 
     * a table.
     * <p>
     * The <tt>filter</tt> argument can be used to filter those rows / features
     * included in the count, or <code>null</code> to include all rows.
     * </p>
     *
     * @return A statement of the form "SELECT count(*) FROM ... WHERE ..."
     */
	public String count( Filter filter ) {
		init();
		
		//select
		sql.append("SELECT count(*)");
		
		//from
		from();
		
		//where
		if ( filter != null && filter != Filter.INCLUDE ) {
			where( filter );
		}
		
		return sql.toString();
	}
	
	/**
	 * Builds a statement of the form "SELECT att1,att2,...,attN FROM ... WHERE ...";
	 * <p>
	 * The <tt>filter</tt> parameter may be null to omit the where clause.
	 * </p>
	 * 
	 */
	public String select( Filter filter ) {
		init();
		
		FeatureType featureType = featureType();
		PrimaryKey pkey = featureSource.getPrimaryKey();
		
		//select
		int n = pkey.columns.length;
		String[] propertyNames = 
			new String[ n + featureType.getAttributeTypes().length ];
		
		//primary key columns
		for ( int i = 0; i < n; i++ ) {
			propertyNames[i] = pkey.columns[i].name;
		}
		//attribute columns
		for ( int i = n; i < propertyNames.length; i++ ) {
			propertyNames[i] = featureType.getAttributeType( i - n ).getName();
		}
		select( propertyNames );
		
		//from
		from();
		
		//where
		if ( filter != null && filter != Filter.INCLUDE ) {
			where( filter );
		}
		
		return sql.toString();
	}
	
	/**
	 * Builds a statement of the form "DELETE FROM ... WHERE ...";
	 * <p>
	 * The <tt>filter</tt> parameter may be null to omit the where clause.
	 * </p>
	 * 
	 */
	public void delete( Filter filter ) {
		init();
		
		//delete
		sql.append( "DELETE ");
		
		//from
		from();
		
		//where
		if ( filter != null && filter != Filter.INCLUDE ) {
			where( filter );
		}
		
	}
	
	
//	public String select(Query query) {
//        init();
//
//        //select
//        select(query.getPropertyNames());
//
//        //from
//        from();
//
//        //where
//        if ((query.getFilter() != null) && (query.getFilter() != Filter.INCLUDE)) {
//            where(query.getFilter());
//        }
//
//        //order by
//        if ((query.getSortBy() != null) && (query.getSortBy().length > 0)) {
//            sortBy(query.getSortBy());
//        }
//
//        return sql.toString();
//    }
//	
    //
    // Methods for encoding partial statements
    //
    /**
     * Encodes the select clause of a query.
     * <p>
     * If <param>propertyNames</param> is null or empty then "*" is used.
     * </p>
     * @param propertyNames The array of properties / columns in the select.
     */
    protected void select(String[] propertyNames) {
        sql.append("SELECT ");

        if ((propertyNames == null) || (propertyNames.length == 0)) {
            sql.append("*");
        } else {
        	//get the feature type
            FeatureType featureType = featureType();
            
            for (int i = 0; i < propertyNames.length; i++) {
                PropertyName propertyName = filterFactory.property(propertyNames[i]);
                
                //get the attribute type
                AttributeType attributeType = (AttributeType) propertyName.evaluate(featureType);
                
                if (attributeType != null) {
                    //check for geometry, because it is encided differnently
                    if (Geometry.class.isAssignableFrom(attributeType.getType())) {
                    	
                    }
                }
                
                //encode it
                propertyName.accept( this, null );

                if (i < (propertyNames.length - 1)) {
                    sql.append(", ");
                }
            }
        }
    }
    
    /**
     * Encodes "FROM <table>".
     *
     */
    protected void from() {
    	sql.append( " FROM " );
    	table();
    }
    
    /**
     * Encodes the table name in a statment qualifying it with the database schema
     * name if set.
     * <p>
     * The table name is derived from the feature source of the builder.
     * </p>
     */
    protected void table() {
    	table( featureSource.getSchema().getTypeName() );
    }
    
    /**
     * Encodes the table name of a query qualifying it with the database schema
     * name if set.
     * 
     * @param name The name of the table.
     * 
     */
    protected void table( String name ) {
    	//qualify with schema
    	if ( dataStore.getDatabaseSchema() != null ) {
    		name( dataStore.getDatabaseSchema() );
            sql.append(".");	
    	}
        
    	//local part
    	name( name );
    }
    
    /**
     * Encodes the WHERE clause of a query.
     *
     * @param filter The filter defining the where clause, non-null.
     *
     */
    protected void where(Filter filter) {
        sql.append(" WHERE ");
        filter.accept(this, null);
    }

    /**
     * Encodes the name of the default geometry in a statement.
     * <p>
     * The default geometry is derived from the feautre source of the builder.
     * </p>
     *
     */
    protected void geometry() {
    	if ( featureSource.getSchema().getDefaultGeometry() == null ) {
    		String msg = "No geometry column to encode";
    		throw new IllegalStateException( msg );
    	}
    	
    	name( featureSource.getSchema().getDefaultGeometry().getName() );
    }
    
    /**
     * Encodes a name to be used in an sql statement.
     * <p>
     * This implementation wraps the name in double quotes.
     * <p>
     *
     * @param raw The raw name.
     *
     */
    protected void name(String raw) {
        sql.append("\"" + raw + "\"");
    }

    /**
     * Encodes a string to be used in an sql statement.
     *
     * @param raw The raw string.
     * @param sql The sql statement buffer
     *
     */
    protected void string(String raw, StringBuffer sql) {
        sql.append("'" + raw + "'");
    }
    
    /**
     * Encodes a geometry with a known srid to be used in an sql statement.
     *
     * @param geometry The geometry to encode.
     * @param srid The spatial reference id of the geometry
     * @param sql HTe sql statement buffer
     */
    protected void geometry(Geometry geometry, String srid, StringBuffer sql) {
        String wkt = new WKTWriter().write(geometry);
        sql.append("GeometryFromText( '" + wkt + "', ");
        srid(srid, sql);
        sql.append(" )");
    }
    
    /**
     * Encodes a srid to be used in an sql statement
     *
     * @param srid The raw spatial reference id of the geometry
     * @param sql HTe sql statement buffer
     */
    protected void srid(String raw, StringBuffer sql) {
        if (raw == null) {
            sql.append("0");
        } else if (raw.matches("[0-9]+")) {
            sql.append(raw);
        } else if (raw.matches(".*:[0-9]+")) {
            raw = raw.replaceAll(".*:([0-9]+)", "$1");
            sql.append(raw);
        }
    }

    /**
     * Encodes <code>nulL</code> to be used in an sql statement.
     *
     * @param sql The sql statement buffer
     */
    protected void nil(StringBuffer sql) {
        sql.append("NULL");
    }
    
    /**
     * @return The current sql buffer.
     */
    protected StringBuffer getSQL() {
    	return sql;
    }
    
	//
	// ExpressionVisitor methods
	//
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
        try {
            //1. evaluate against the type to get the AttributeType
            FeatureType featureType = featureType();

            AttributeType attributeType = (AttributeType) propertyName.evaluate(featureType);

            if (attributeType != null) {
                //encode the name of the attribute, not the property name itself
                name(attributeType.getName());

                //return the type as the return 
                return attributeType.getType();
            }

            //2. not in type, could it be a primary key?
            PrimaryKey key = featureSource.getPrimaryKey();

            //serach for the property in the primary key, encode it and return its type
            for (int i = 0; i < key.columns.length; i++) {
                String name = key.columns[i].name;

                if (propertyName.getPropertyName().equals(name)) {
                    //encode it
                    name(propertyName.getPropertyName());

                    //return the type
                    return key.columns[i].type;
                }
            }

            //3. give up, just encode directly
            name(propertyName.getPropertyName());

            //return nothing
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    //
    // Arithmetic operators
    //
    public Object visit(Add add, Object data) {
        return visit(add, data, "+");
    }

    public Object visit(Subtract subtract, Object data) {
        return visit(subtract, data, "-");
    }

    public Object visit(Multiply multiply, Object data) {
        return visit(multiply, data, "*");
    }

    public Object visit(Divide divide, Object data) {
        return visit(divide, data, "/");
    }

    protected Object visit(BinaryExpression expression, Object data, String operator) {
        sql.append("( ");
        data = expression.getExpression1().accept(this, data);

        sql.append(" ) " + operator + " ( ");
        data = expression.getExpression2().accept(this, data);

        sql.append(" )");

        return data;
    }
    
    public Object visit(Literal literal, Object data) {
        Object value = null;

        //was the type passed in as a hint?
        if (data instanceof Class) {
            //special case for numerics, let the database do the conversions
            // it needs to
            if (Number.class.isAssignableFrom((Class) data)) {
                //value = literal.evaluate( null, BigDecimal.class );
                value = literal.evaluate(null, Number.class);
            } else {
                //non numeric, just convert
                value = literal.evaluate(null, (Class) data);
            }
        } else {
            //just evaluate
            value = literal.evaluate(null);
        }

        //check for string
        if (value instanceof String) {
            string((String) value, sql);

            return data;
        }

        FeatureType featureType = featureType();

        //check for geometry
        if (value instanceof Geometry) {
            //figure out srid
            CoordinateReferenceSystem crs = null;

            //passed in?
            if (data instanceof CoordinateReferenceSystem) {
                crs = (CoordinateReferenceSystem) data;
            } else {
                //check the feature type
                if (featureType.getDefaultGeometry() != null) {
                    crs = featureType.getDefaultGeometry().getCoordinateSystem();
                }
            }

            //convert to a postgis srid
            String srid = null;

            if (crs != null) {
                srid = encode(crs);
            }

            geometry((Geometry) value, srid, sql);

            return data;
        }

        //convert to string
        String encoded = (String) Converters.convert(value, String.class);

        if (encoded == null) {
            //fall back to toString()
            encoded = value.toString();
        }

        sql.append(encoded);

        return data;
    }
    
    public Object visit(NilExpression expression, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(Function expression, Object extraData) {
		// TODO Auto-generated method stub
		return null;
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

	
	//
	// FilterVisitor api
	//
    public Object visitNullFilter(Object data) {
        return null;
    }

    public Object visit(ExcludeFilter exclude, Object data) {
        sql.append("1 = 0");

        return data;
    }

    public Object visit(IncludeFilter include, Object data) {
        sql.append("1 = 1");

        return data;
    }

    //
    // logical operators
    //
    public Object visit(Not not, Object data) {
        sql.append("NOT ( ");
        data = not.getFilter().accept(this, data);
        sql.append(" )");

        return data;
    }

    public Object visit(And and, Object data) {
        return visit(and, data, "AND");
    }

    public Object visit(Or or, Object data) {
        return visit(or, data, "OR");
    }

    protected Object visit(BinaryLogicOperator logic, Object data, String operator) {
        boolean parentheses = logic.getChildren().size() > 1;

        //encode each child
        for (Iterator f = logic.getChildren().iterator(); f.hasNext();) {
            Filter filter = (Filter) f.next();

            if (parentheses) {
                sql.append("( ");
            }

            data = filter.accept(this, data);

            if (parentheses) {
                sql.append(" )");
            }

            if (f.hasNext()) {
                sql.append(" " + operator + " ");
            }
        }

        return data;
    }
    
    //
    //id filter
    //
    public Object visit(Id id, Object data) {
        try {
            //get the primary key
            PrimaryKey key = featureSource.getPrimaryKey();

            //prepare each column as a property name
            PropertyName[] columnNames = new PropertyName[key.columns.length];

            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = filterFactory.property(key.columns[i].name);
            }

            //process each fid, creating an OR filter
            List or = new ArrayList();

            for (Iterator f = id.getIDs().iterator(); f.hasNext();) {
                String fid = (String) f.next();

                //map to values
                Object[] values = key.decode(fid);

                //create an "AND" filter for each property name
                List and = new ArrayList();

                for (int i = 0; i < values.length; i++) {
                    //create an equalTo filter
                    PropertyName name = columnNames[i];
                    Literal literal = filterFactory.literal(values[i]);
                    and.add(filterFactory.equals(name, literal));
                }

                //add to or
                or.add(filterFactory.and(and));
            }

            //encode it
            return filterFactory.or(or).accept(this, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //
    // binary comparison operators
    //
    public Object visit(PropertyIsBetween between, Object data) {
        sql.append("( ");

        data = between.getExpression().accept(this, data);
        sql.append(" ) BETWEEN ( ");

        data = between.getLowerBoundary().accept(this, data);
        sql.append(" ) AND ( ");

        data = between.getUpperBoundary().accept(this, data);
        sql.append(" )");

        return data;
    }

    public Object visit(PropertyIsEqualTo equalTo, Object data) {
        return visit(equalTo, data, "=");
    }

    public Object visit(PropertyIsNotEqualTo notEqualTo, Object data) {
        return visit(notEqualTo, data, "!=");
    }

    public Object visit(PropertyIsGreaterThan greaterThan, Object data) {
        return visit(greaterThan, data, ">");
    }

    public Object visit(PropertyIsGreaterThanOrEqualTo greaterThanOrEqualTo, Object data) {
        return visit(greaterThanOrEqualTo, data, ">=");
    }

    public Object visit(PropertyIsLessThan lessThan, Object data) {
        return visit(lessThan, data, "<");
    }

    public Object visit(PropertyIsLessThanOrEqualTo lessThanOrEqualTo, Object data) {
        return visit(lessThanOrEqualTo, data, "<=");
    }

    protected Object visit(BinaryComparisonOperator comparison, Object data, String operator) {
        sql.append("( ");

        //left
        data = comparison.getExpression1().accept(this, data);

        //operator
        sql.append(" ) " + operator + " ( ");

        //right
        data = comparison.getExpression2().accept(this, data);

        sql.append(" )");

        return data;
    }

    public Object visit(PropertyIsLike like, Object data) {
        String pattern = LikeFilterImpl.convertToSQL92(like.getEscape().charAt(0),
                like.getWildCard().charAt(0), like.getSingleChar().charAt(0), like.getLiteral());

        //evaluate left hande
        data = like.getExpression().accept(this, data);

        sql.append(" LIKE ");

        //encode pattern as string
        string(pattern, sql);

        return data;
    }

    public Object visit(PropertyIsNull isNull, Object data) {
        data = isNull.getExpression().accept(this, data);
        sql.append(" IS ");
        nil(sql);

        return data;
    }

    //
    // spatial operators
    //
    public Object visit(BBOX bbox, Object data) {
        //create a property name + geometry
        PropertyName name = filterFactory.property(bbox.getPropertyName());
        Envelope e = new Envelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY());

        if (bbox.getSRS() != null) {
            CoordinateReferenceSystem crs;

            try {
                crs = CRS.decode(bbox.getSRS());
                data = crs;
            } catch (Exception ex) {
                featureSource.getLogger().log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }

        return visit(name, JTS.toGeometry(e), data);
    }

    protected Object visit(PropertyName name, Geometry geometry, Object data) {
        //encode the property ( dont carry back data )
        name.accept(this, data);

        //bbox operator
        sql.append(" && ");

        //encode the geometry
        Literal literal = filterFactory.literal(geometry);

        return literal.accept(this, data);
    }

    public Object visit(Beyond beyond, Object data) {
        return visit(beyond, data, ">", false);
    }

    public Object visit(DWithin dwithin, Object data) {
        return visit(dwithin, data, "<=", true);
    }

    protected Object visit(DistanceBufferOperator distance, Object data, String operator,
        boolean index) {
        data = visit((BinarySpatialOperator) distance, data, "distance", index);
        sql.append(" " + operator + " " + distance.getDistance());

        return data;
    }

    public Object visit(Disjoint disjoint, Object data) {
        return visit(disjoint, data, "disjoint", false);
    }

    public Object visit(Contains contains, Object data) {
        return visit(contains, data, "contains", true);
    }

    public Object visit(Crosses crosses, Object data) {
        return visit(crosses, data, "crosses", true);
    }

    public Object visit(Equals equals, Object data) {
        return visit(equals, data, "equals", true);
    }

    public Object visit(Intersects intersects, Object data) {
        return visit(intersects, data, "intersects", true);
    }

    public Object visit(Overlaps overlaps, Object data) {
        return visit(overlaps, data, "overlaps", true);
    }

    public Object visit(Touches touches, Object data) {
        return visit(touches, data, "touches", true);
    }

    public Object visit(Within within, Object data) {
        return visit(within, data, "within", true);
    }

    protected Object visit(BinarySpatialOperator comparison, Object data, String operator,
        boolean index) {
        //use the index for this comparison?
        if (index) {
            //figure out the property name, and geometry
            PropertyName propertyName = null;
            Geometry geometry = null;

            if (comparison.getExpression1() instanceof PropertyName) {
                propertyName = (PropertyName) comparison.getExpression1();
                geometry = (Geometry) comparison.getExpression2().evaluate(null, Geometry.class);
            } else if (comparison.getExpression2() instanceof PropertyName) {
                propertyName = (PropertyName) comparison.getExpression2();
                geometry = (Geometry) comparison.getExpression1().evaluate(null, Geometry.class);
            }

            if ((propertyName != null) && (geometry != null)) {
                data = visit(propertyName, geometry, data);
                sql.append(" AND ");
            }
        }

        //operator
        sql.append(operator + "( ( ");

        //left
        data = comparison.getExpression1().accept(this, data);

        sql.append(" ) , ( ");

        //right
        data = comparison.getExpression2().accept(this, data);

        sql.append(" ) )");

        return data;
    }

}
