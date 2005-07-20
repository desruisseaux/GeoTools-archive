package org.geotools.expr;

import java.util.List;

import org.geotools.api.filter.BinaryLogicOperator;
import org.geotools.api.filter.Filter;

public class BinaryLogicAbstract extends FilterAbstract implements BinaryLogicOperator {
	protected List<Filter> children;
	protected BinaryLogicAbstract(Expr factory, List<Filter> children ) {
		super(factory);
		this.children = children;
	}
	public List<Filter> getChildren() {
		return children;
	}
}
