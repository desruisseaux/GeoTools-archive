package org.geotools.filter;

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.opengis.filter.And;
import org.opengis.filter.FilterVisitor;

public class AndImpl extends LogicFilterImpl implements And {
	
	protected AndImpl(FilterFactory factory, List/*<Filter>*/ children) {
		super(factory, children );
		
		//backwards compatability with old type system
		this.filterType = LOGIC_AND;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Filter filter = (Filter)itr.next();
			if( !filter.evaluate( feature )) return false;
		}
		return true;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
}

