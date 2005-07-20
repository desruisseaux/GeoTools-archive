package org.geotools.expr;

import org.geotools.api.filter.Filter;
import org.geotools.api.filter.Not;
import org.geotools.feature.Feature;

public class NotImpl extends FilterAbstract implements Not {
	private Filter filter;

	protected NotImpl(Expr factory, Filter filter) {
		super(factory);
		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

	@Override
	public Boolean evaluate(Feature feature) {
		return !filter.evaluate(feature);
	}

}
