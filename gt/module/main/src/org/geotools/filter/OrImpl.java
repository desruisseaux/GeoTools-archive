package org.geotools.filter;

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Or;

public class OrImpl extends LogicFilterImpl implements Or {
	
	protected OrImpl(FilterFactory factory, List/*<Filter>*/ children) {
		super(factory, children );
		
		//backwards compatability with old type system
		filterType = LOGIC_OR;
	}
	
	public boolean evaluate(Feature feature) {
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Filter filter = (Filter)itr.next();
			if( filter.evaluate( feature )) return true;
		}
		return false;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
	
}

