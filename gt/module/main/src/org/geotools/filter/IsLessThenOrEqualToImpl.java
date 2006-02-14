package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Expression;

public class IsLessThenOrEqualToImpl extends CompareFilterImpl implements
		PropertyIsLessThanOrEqualTo {
	
	protected IsLessThenOrEqualToImpl(FilterFactory factory) {
		this(factory, null, null);
	}
	
	protected IsLessThenOrEqualToImpl(FilterFactory factory, Expression expression1, Expression expression2) {
		super(factory, expression1, expression2);
		
		//backwards compat with old type system
		this.filterType = COMPARE_LESS_THAN_EQUAL;
	}

	//@Override
	public boolean evaluate(Feature feature) {
		Comparable value1 = comparable(expression1,feature);
		Comparable value2 = comparable(expression2,feature);
		
		return compare(value1,value2) <= 0;
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit(this,extraData);
	}
}
