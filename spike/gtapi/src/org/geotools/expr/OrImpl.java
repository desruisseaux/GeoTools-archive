package org.geotools.expr;

import java.util.List;

import org.geotools.api.filter.Filter;
import org.geotools.api.filter.Or;
import org.geotools.feature.Feature;

public class OrImpl extends BinaryLogicAbstract implements Or {
	protected OrImpl(Expr factory, List<Filter> children) {
		super(factory, children );
	}
	@Override
	public Boolean evaluate(Feature feature) {
		for( Filter filter : children ){
			if( filter.evaluate( feature )) return true;
		}
		return false;
	}
	/** Chain additional filter onto children */
	public OrImpl or(Filter filter) {
		children.add( filter );
		return this;
	}
}

