package org.geotools.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.geotools.feature.FeatureType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;

public class FidsExpr extends AbstractFilterExpr {
	Set fids = new HashSet();
	public FidsExpr( Collection fidCollection ){
		fids.addAll( fidCollection );
	}
	public FidsExpr( String fid ){
		fids.add( fid );
	}
	public Filter filter(FeatureType schema) {
		FidFilter filter = factory.createFidFilter();
		filter.addAllFids( fids );
		return filter;
	}
	public Expr or( Expr expr ){
		if( expr instanceof FidsExpr ){
			return or( (FidsExpr) expr );
		}
		else {
			return super.or( expr );
		}
	}
	public Expr or(FidsExpr expr ) {
		Set allFids = new HashSet();
		allFids.addAll( fids );
		allFids.addAll( expr.fids );
		return new FidsExpr( allFids );
	}	
}