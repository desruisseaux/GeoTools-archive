package org.geotools.filter.visitor;

import java.util.Stack;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;

/**
 * Used to duplicate a Filter & or Expression
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 */
public class DuplicatorFilterVisitor extends AbstractFilterVisitor {
	Stack pages; // need a Stack as Filter structure is recursive
	FilterFactory ff;
	
	public DuplicatorFilterVisitor( FilterFactory factory ){
		ff = factory;
	}
	public void setFilterFactory( FilterFactory factory ){
		ff = factory;
	}
	public void visit(Filter filter) {
		// Should not happen?
	}
	public void visit(BetweenFilter filter) {
		BetweenFilter copy = null;					
		try {
			Expression leftCopy = null;			
			if (filter.getLeftValue() != null) {			
				filter.getLeftValue().accept( this );
				leftCopy = (Expression) pages.pop();				
			}
			
			filter.getMiddleValue().accept( this );			
			Expression middleCopy = (Expression) pages.pop();
			
			filter.getRightValue().accept( this );			
			Expression rightCopy = (Expression) pages.pop();			
		
			copy = ff.createBetweenFilter();
			copy.addLeftValue( leftCopy );
			copy.addMiddleValue( middleCopy );
			copy.addRightValue( rightCopy );
		} catch (IllegalFilterException erp){
			throw new RuntimeException( erp );
		}
		pages.push( copy );		
	}
	public void visit(CompareFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(GeometryFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(LikeFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(LogicFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(NullFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(FidFilter filter) {
		// TODO Auto-generated method stub
		
	}
	public void visit(AttributeExpression expression) {
		// TODO Auto-generated method stub
		
	}
	public void visit(Expression expression) {
		// TODO Auto-generated method stub
		
	}
	public void visit(LiteralExpression expression) {
		// TODO Auto-generated method stub
		
	}
	public void visit(MathExpression expression) {
		// TODO Auto-generated method stub
		
	}
	public void visit(FunctionExpression expression) {
		// TODO Auto-generated method stub
		
	}	
	public Object getCopy() {
		return pages.firstElement();
	}

}
