/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Defines a derived view of Feature information.
 * <p>
 * Well here we are the center of the Opperations API. Temped to call this
 * class Op or FeatureOp. If we ever have to split this idea into Request
 * / Response we can have Op / FeatureView. For now I am trying to run
 * with David Zwiers idea of combinging the two.
 * </p>
 * <p>
 * How do you get started? Talk to DataRepository it is willing to constuct
 * a FeatureView for you (based on wrapping up a registered
 * DataStore / FeatureStore.
 * </p>
 * <p>
 * But you probably want to know what is going on here ...
 * </p>
 * <p>
 * From Email:
 * </p>
 * <p><b>David Zwiers</b>
 * Where the Operation's constructor is smart enough to walk up the chain
 * and find out if it is directly linked to a datastore using the
 * isPostProcessing() method ... thus allowing for optimizations.  
 * </p>
 * <p>
 * We will need some form of backwards hints, such as FeatureReader
 * reader(Map hints) in FeatureView as well.
 * <pre><code>
 *  interface FeatureView{
 *      static final String FILTER_HINT // for an SQL Filter
 *      static final String QUERY_HINT // for an SQL Query
 *      static final String AS_HINT // for an SQL rename
 *      static final String JOIN_HINT // for an SQL join
 * 
 *      FeatureType getSchema();
 *      FeatureReader reader();
 * }
 * <code></pre>
 * </p>
 * <p>
 * An example chain would look like this:
 * <pre><code>
 * *roads* *river*
 *   |        |
 * _MyOp_  *filter*
 *     \    /
 *     _join_
 * </code></pre>
 * Where *opp* is pre-processed, and _opp_ is post-processed.
 * </p>
 * 
 * <p><b>Jody:</b>
 * A very interesting thing that you have done is to express all opperations
 * as downstream from a FeatureSource. That is even Filter is logically
 * placed after a FeatureSource. The fact that Filter may be handled specially
 * by the FeautreSource is *hidden* from the client code.
 * </p>
 * <p>
 * This puts me in mind of another API - the Java Collections API.
 * </p>
 * <p>
 * One thing that is Very nice about the Collections api is the ability to
 * construct chains very quickly, and easily using methods and Constructors.
 * The chains are in your face and look like normal java code. Not hidden
 * across Filter, typeName, forceCS and reporjectCS as is currently the
 * case with Query.
 * </p>
 * <p>
 * Now to pull this kind of thing off the Collections API has made use of
 * a convention (Construtor Chainging  - which you share), and a series
 * of short method names that produce serivitive chains.
 * </p>
 * <p>
 * Lets try with this with the explicit Collections API approach.
 * I have also mixed in the "common" Filters from the RandomAccess wiki page.
 * </p>
 * Notice that there is no extention point for User Defined Opperations -
 * they use the Consturctor Convention.
 * </p>
 * <p>
 * So we could do All the capabilities of Query with the following:<br>
 * <code><b>return</b> registry.view( "road" ).name( "myroad").prefix("topp").cs( LatLong ).reproject( bc_alberts );</code>
 * </p>
 * <p>
 * So lets try David's example>: <br>
 * <code><b>return</b> new MyOpp( ds.getFeatureSource( "roads") ).join( ds.getFeatureSource( "river" ).filter( Filter ) );<code>
 * </p>
 * Ideas:   
 * <p>
 * Now if we want to get a little bit more wild - we can get rid of the most common Filter expresions.
 * <pre><code>
 * interface FeatureView {
 *    FeatureView and( Filter, Filter )
 *    FeatureView or( Filter, Filter );
 *    FeatureView not( Filter );
 * 
 *    FeatureView beween( Expression, String xpath, Expression )
 *    FeatureView eq( String xpath, Expression )
 *    FeatureView gt( String xpath, Expression )
 *    FeatureView gte( String xpath, Expression )
 *    FeatureView lt( String xpath, Expression )
 *    FeatureView like( String xpath, String regex)
 *    FeatureView like( String xpath, Regex regex)
 * </code></pre>
 * The above starts to look very good in practice - although it is tempting to start letting literal
 * Objects in for Expressions:<br>
 * <code><b>return</b> DataStore.getFeatureSource( "road" ).bbox( Extent ).between( 47, "code", 50 );</code>
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public interface FeatureView {
	/** From FeatureResults - Schema of this FeatureView */
	FeatureType getSchema();	
	/** From FeatureResults - Access to feature information */
	FeatureReader  reader() throws IOException;
	/** From FeatureResults - Bounds query */
	Envelope bounds();
	/** From FeatureResults - Count query */
	int count();
	
	/** From Query - changes schema */
	FeatureView name( String typeName );
	/** From Query - changes schema */
	FeatureView prefix( String prefix );
	/** From Query - forces crs */
	FeatureView cs( CoordinateReferenceSystem crs );
	/** From Query - reprojection opperation */
	FeatureView reproject( CoordinateReferenceSystem crs );
	/** From Query - Schema order manipulation */
	FeatureView as( String attributes[] );
	
	/** Allow derived single attribue - for use with join?
	 * <p>
	 * Compare contrast with as( List asList ).
	 * </p>
	 * <p>
	 * This would indicate something like the following:
	 * <pre><code>
	 * FeatureView vw = registry.view( "road" );
	 * <b>return<b> vw.as( "Name", "USER_NAME" ).join( vw.as( "Address", "ADDRESS" ) ); 
	 * </code></pre>
	 * </p>
	 * <p>
	 * vs.:
	 * <pre><code>
	 * <b>return</b> registry.view( "road" ).as( new Array[]{new As(  "Name", "USER_NAME),new As("Address", "ADDRESS"),} ); 
	 * </code></pre>
	 * </p>
	 * Right now I don't really like either :-(
	 */
	FeatureView as( String attribute, Expression expr );
	/** Allow derived single attribtues - for use with join? */
	FeatureView as( String attribute, String xpath );
	/** Allow derived attribtues */
	FeatureView as( As as[] );
	/** Allow derived attribtues */
	FeatureView as( List asList );
	
	/** From Query - allow the limiting of content */
	FeatureView filter( Filter filter );
	/**
	 * Join the two views.
	 * <p>
	 * Several questions arise:
	 * <ul>
	 * <li>Intended use is a join of two FeatureTypes, where the resulting
	 * schema contains two Attribtues (one for each joined schema). Client
	 * code can then use a series of "as" opperations to extract just the
	 * information they are looking for - into one nice flat schema.
	 * </li>
	 * <li>
	 * The alternative is to dump everything into one flat schema and
	 * hope for a lack of conflicts. And use some renaming when conflicts
	 * occur. I have seen this before - it works perfectly for 80% of client
	 * needs, and hurts for the last 20%. Still that may be better then the
	 * xpath approach which looks like it will hurt all the time.
	 * </li>
	 * <li>if both views have the same typeName (that is derived from the same
	 * FeatureView) should we just dump all the attributes together
	 * (for use with the as( attribue, xpath )? Or should that be a separate
	 * define/combine opperation?
	 * </li>
	 * </p>
	 * @param view
	 * @return 
	 */
	FeatureView join( FeatureView view );
	/**
	 * Join two views with immediate Filtering.
	 * <p>
	 * I am going to be clear on this one. Filtering is against a Schema of
	 * two attributes (one for each view so the filter needs xpath).
	 * If they are both using the the same a number 2 will be stuck on the end
	 * of the second one. The results will be faltened into one
	 * Schema (no nested xpaths), any conflicting attribtues will have a number
	 * 2 stuck on the end.
	 * </p>
	 * <p>
	 * Clear may not be right - what do you thing?
	 * </p>
	 */
	FeatureView join( FeatureView view, Filter expression );
	
}

/**
 * Initial hack at declaring Schema transformations.
 * <p>
 * Note this is really lame - we have to know the FeatureType in order
 * to create a AttributeExpression against the FeatureType - why?
 * <p>
 */ 
class As {
	final public String name;
	final public Expression expr;
	
	public As( String attributeName ){
		this( attributeName, attributeName );
	}
	public As( String attributeName, String xpath ) {
		this( attributeName, expr( xpath ) );		
	}
	public As( String attributeName, Expression expression ){
		name = attributeName;
		expr = expression;
	}
	public static Expression expr( String xpath ){
		AttributeExpression expr;
		try {
			return FilterFactory.createFilterFactory().createAttributeExpression( null, xpath );			
		} catch (IllegalFilterException e) {
			return null;
		} catch (FactoryConfigurationError e) {
			return null;
		}		
	}
}