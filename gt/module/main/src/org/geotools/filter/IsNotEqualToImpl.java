package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;

public class IsNotEqualToImpl extends CompareFilterImpl {

	protected IsNotEqualToImpl(FilterFactory factory) {
		this(factory,null,null);
	}
	
	protected IsNotEqualToImpl(FilterFactory factory, Expression e1, Expression e2) {
		super(factory, e1, e2);
		
		//backwards compat with old type system
		this.filterType = COMPARE_NOT_EQUALS;
	}

	//@Override
	public boolean evaluate(Feature feature) {
		Object value1 = eval( expression1, feature );	
		Object value2 = eval( expression2, feature );	
		
		if (value1 instanceof Long) {
            value1 = new Integer(((Long)value1).intValue());
        }

        if (value2 instanceof Long) {
            value2 = new Integer(((Long)value2).intValue());
        } 
        
		return (value1 == null && value2 != null) ||
			(value1 != null && value2 == null) ||
		    (value1 != null && !value1.equals( value2 ));
	}
	
	public Object accept(FilterVisitor visitor, Object extraData) {
		//TODO: JD: do we need a PropertyIsNotEqualTo interface?
		throw new UnsupportedOperationException();
	}

}
