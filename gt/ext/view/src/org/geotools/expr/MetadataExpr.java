package org.geotools.expr;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.metadata.Metadata;

/**
 * Retrive attribute from Feature.
 * <p>
 * This is a start of an Expr chain - I would rather setup
 * Expr.attribute( path ) if I can figure out how.
 * </p>
 */
public class MetadataExpr extends AbstractExpr {
	String xpath;	
	public MetadataExpr( String xpath ){
		this.xpath = xpath;
	}	
	/* (non-Javadoc)
	 * @see org.geotools.expr.AbstractExpr#resolve(org.geotools.metadata.Metadata)
	 */
	public Expr resolve(Metadata metadata) {
		if( metadata == null ) return null;
		Object value = metadata.getElement( xpath );
		return Exprs.literal( value );		
	}
	public Expression expression(FeatureType schema) {
		// Metadata is not currently supported at the Expression
		// level. Please resolve your Metadata for this Expr prior to use.
		//
		return null;		
	}
	public Expr resolve(String bind, Feature feature) {
		if( !xpath.startsWith( bind+"/" )){
			return this;
		}
		String newPath = xpath.substring( bind.length()+2 );
		Metadata meta;
		if( feature instanceof Metadata ){
			meta =(Metadata) feature;				
		}
		else {
			// aquire metadata from feature?
			meta = null;
		}
		if( meta == null ){
			return null;
		}
		Object value = meta.getElement( newPath);
		return Exprs.literal( value );		
	}
	public Expr reduce(String bind) {
		if( !xpath.startsWith( bind+"/" )){
			return this;
		}
		String newPath = xpath.substring( bind.length()+2 );
		return new MetadataExpr( newPath );
	}
}