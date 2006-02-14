package org.geotools.filter;


import org.geotools.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Not;

public class NotImpl extends LogicFilterImpl implements Not {
	
	protected NotImpl(FilterFactory factory) {
		super(factory);
		
		//backwards compatability with old type system
		filterType = LOGIC_NOT;
	}
	
	protected NotImpl(FilterFactory factory, Filter filter) {
		super(factory);
		this.children.add(filter);
		
		//backwards compatability with old type system
		filterType = LOGIC_NOT;
	}

	public Filter getFilter() {
		return (Filter)children.get(0);
	}

	public void setFilter(Filter filter) {
		if (children.isEmpty()) {
			children.add(filter);
		}
		else {
			children.set(0,filter);
		}
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		return !getFilter().evaluate(feature);
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}

}
