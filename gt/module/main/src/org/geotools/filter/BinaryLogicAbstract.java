package org.geotools.filter;

import java.util.List;

import org.opengis.filter.BinaryLogicOperator;


public class BinaryLogicAbstract extends AbstractFilter implements BinaryLogicOperator {
	protected List/*<Filter>*/ children;
	
	protected BinaryLogicAbstract(FilterFactory factory, List/*<Filter>*/ children ) {
		super(factory);
		this.children = children;
	}
	
	public List/*<Filter>*/ getChildren() {
		return children;
	}
	
	public void setChildren(List children) {
		this.children = children;
	}

	public Filter and(Filter filter) {
		return factory.and(this,filter);
	}

	public Filter or(Filter filter) {
		return factory.or(this,filter);
	}

	public Filter not() {
		return factory.not(this);
	}
}
