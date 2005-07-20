package org.geotools.expr;

import java.util.List;

import org.geotools.api.filter.And;
import org.geotools.api.filter.Filter;
import org.geotools.feature.Feature;

public class AndImpl extends BinaryLogicAbstract implements And {
	protected AndImpl(Expr factory, List<Filter> children) {
		super(factory, children );
	}
	@Override
	public Boolean evaluate(Feature feature) {
		for( Filter filter : children ){
			if( !filter.evaluate( feature )) return false;
		}
		return true;
	}
	/** Chain additional filter onto children */
	public AndImpl and(Filter filter) {
		children.add( filter );
		return this;
	}
}

