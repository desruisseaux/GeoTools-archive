package org.geotools.metadata;

import java.io.IOException;

import org.geotools.expr.Expr;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;

/**
 * Represents a query against metadata.
 * <p>
 * This class represents a strong extention of the existing geotools Filter and Expression
 * construct. I have chosen the name Query to agree with the origional meaning of the data.Query
 * which represented a metadata query matching the metadata typeName and a Feature Filter on the
 * resulting FeatureCollection. 
 * </p>
 * <p>
 * Another way of thinking about this is that it offers a walk of the Expr construct
 * from the persepective of Metadata. I noticed that although Expression and Filter
 * are defined as interfaces I have only ever seen one implementation. The *magic*
 * of data specific behaviour seems to occur only when walking the tree, rather than
 * implementing a custom FilterFactory (& thus custom tree mnodes).
 * </p>
 * <p>
 * So this class provides a tree construct for a QueryWalker to navigate through,
 * I am folloing the direct example of Filter and Expression here (even though
 * I think there may be a better way).
 * </p>
 * <p>
 * Q: Should this class turn itself into a data.Query? Or should I turn data.Query into a subclass.
 * </p>
 * @author Jody Garnett, Refractions Research
 * @version 2.1
 */
public class Query {
	Expr expr;
	public Query( Expr expr ){
		this.expr = expr;
	}
	public boolean accepts( Feature feature ) throws IOException{
		Metadata meta = null;
		
		// Get metdata for feature
		// meta = geature.getMetadata();
		Expr query = null;
		if( meta != null ){
			query = expr.resolve( meta );
		}
		else {
			query = expr; 
		}		
		Filter filter = query.filter( feature.getFeatureType() );
		
		return filter.contains( feature );
	}
	public boolean accepts( Metadata meta ){
		Expr query = expr.resolve( meta );
		Filter filter = query.filter( null );
		return filter.contains( );
	}
}
