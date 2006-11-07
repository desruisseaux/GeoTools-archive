/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.visitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FilterVisitor2;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.IncludeFilter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Determines what queries can be processed server side and which can be processed client side.
 * 
 * @author dzwiers
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/module/main/src/org/geotools/data/wfs/WFSFilterVisitor.java $
 */
public class PostPreProcessFilterSplittingVisitor implements FilterVisitor, FilterVisitor2 {
		private static final Logger logger=Logger.getLogger("org.geotools.filter");
	    private Stack postStack = new Stack();
	    private Stack preStack = new Stack();
	    /** 
	     * Operates similar to postStack.  When a update is determined to affect an attribute expression the update
	     * filter is pushed on to the stack, then ored with the filter that contains the expression.
	     */
	    private Set changedStack=new HashSet();
	    private FilterCapabilities fcs = null;
	    private FeatureType parent = null;
		private Filter original = null;
		private ClientTransactionAccessor transactionAccessor;
	
	    private PostPreProcessFilterSplittingVisitor() {
	    	// do nothing
	    }
	
	    /**
	     * Create a new instance.
	     * @param fcs The FilterCapabilties that describes what Filters/Expressions the server can process.
	     * @param parent 
	     * @param transactionAccessor If the transaction is handled on the client and not the server then different filters
	     * must be sent to the server.  This class provides a generic way of obtaining the information from the transaction.
	     */
	    public PostPreProcessFilterSplittingVisitor(FilterCapabilities fcs, FeatureType parent, ClientTransactionAccessor transactionAccessor) {
	        this.fcs = fcs;
	        this.parent = parent;
	        this.transactionAccessor = transactionAccessor;
	    }
	    
	    /**
	     * Gets the filter that cannot be sent to the server and must be post-processed on the client by geotools.
	     * 
	     * @return the filter that cannot be sent to the server and must be post-processed on the client by geotools.
	     */
	    public org.opengis.filter.Filter getFilterPost() {
			if (!changedStack.isEmpty())
				// Return the original filter to ensure that
				// correct features are filtered
				return original ;
	
	        if (postStack.size() > 1) {
	            logger.warning("Too many post stack items after run: "
	                + postStack.size());
	        }
	        
	        // JE:  Changed to peek because get implies that the value can be retrieved multiple times
            org.opengis.filter.Filter f = postStack.isEmpty() ? Filter.INCLUDE : (org.opengis.filter.Filter) postStack.peek();
	        return f;
	    }
		
	    /**
	     * Gets the filter that can be sent to the server for pre-processing.
	     * 
	     * @return the filter that can be sent to the server for pre-processing.
	     */
	    public org.opengis.filter.Filter getFilterPre() {
	        if (preStack.isEmpty()) {
	            return Filter.INCLUDE;
	        }
	
	        if (preStack.size() > 1) {
	            logger.warning("Too many pre stack items after run: "
	                + preStack.size());
	        }

	
	        // JE:  Changed to peek because get implies that the value can be retrieved multiple times
            org.opengis.filter.Filter f = preStack.isEmpty() ? Filter.INCLUDE : (org.opengis.filter.Filter) preStack.peek();
			// deal with deletes here !!!
            if(transactionAccessor != null){
            	if(f != null && f!=Filter.EXCLUDE){
            		Filter deleteFilter = (org.geotools.filter.Filter) transactionAccessor.getDeleteFilter();
	            	if( deleteFilter!=null ){
                        if( deleteFilter==org.geotools.filter.Filter.ALL )
                            f=Filter.EXCLUDE;
                        else
                            f=((Filter)f).and(deleteFilter.not());
                    }
            	}
            }

	        if( changedStack.isEmpty())
	        	return f;
	        
	        Iterator iter=changedStack.iterator();
	        Filter updateFilter=(Filter) iter.next();
	        while( iter.hasNext() ){
                Filter next=(Filter) iter.next();
                if( next==org.geotools.filter.Filter.NONE){
                    updateFilter=next;
                    break;
                }else{
                    updateFilter=(Filter) updateFilter.or(next);
                }
            }
            if( updateFilter == org.geotools.filter.Filter.NONE || f==Filter.INCLUDE )
                return Filter.INCLUDE;
	        return ((Filter)f).or(updateFilter);
	    }

        public void visit( IncludeFilter filter ) {
            return;
        }
        public void visit( ExcludeFilter filter ) {
            if (fcs.supports(FilterType.ALL)) {
                preStack.push(filter);
            } else {
                postStack.push(filter);
            }
        }
	    /**
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
	     */
	    public void visit(Filter filter) {	        
	        if( original==null )
	        	original=filter;
	        if (!postStack.isEmpty()) {
	        	postStack.push(filter);
                logger.fine(
                    		
                		
	                "@see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)"+filter.toString());
	        } else {
	            switch (filter.getFilterType()) {
	            case FilterType.BETWEEN:
	                visit((BetweenFilter) filter);
	
	                break;
	
	            case FilterType.COMPARE_EQUALS:
	            case FilterType.COMPARE_GREATER_THAN:
	            case FilterType.COMPARE_GREATER_THAN_EQUAL:
	            case FilterType.COMPARE_LESS_THAN:
	            case FilterType.COMPARE_LESS_THAN_EQUAL:
	            case FilterType.COMPARE_NOT_EQUALS:
	                visit((BetweenFilter) filter);
	
	                break;
	
	            case FilterType.FID:
	                visit((BetweenFilter) filter);
	
	                break;
	
	            case FilterType.GEOMETRY_BBOX:
	            case FilterType.GEOMETRY_BEYOND:
	            case FilterType.GEOMETRY_CONTAINS:
	            case FilterType.GEOMETRY_CROSSES:
	            case FilterType.GEOMETRY_DISJOINT:
	            case FilterType.GEOMETRY_DWITHIN:
	            case FilterType.GEOMETRY_EQUALS:
	            case FilterType.GEOMETRY_INTERSECTS:
	            case FilterType.GEOMETRY_OVERLAPS:
	            case FilterType.GEOMETRY_TOUCHES:
	            case FilterType.GEOMETRY_WITHIN:
	                visit((GeometryFilter) filter);
	
	                break;
	
	            case FilterType.LIKE:
	                visit((LikeFilter) filter);
	
	                break;
	
	            case FilterType.LOGIC_AND:
	            case FilterType.LOGIC_NOT:
	            case FilterType.LOGIC_OR:
	                visit((LogicFilter) filter);
	
	                break;
	
	            case FilterType.NULL:
	                visit((NullFilter) filter);
	
	                break;
	
	            default:
	                postStack.push(filter);
	                logger.warning(filter.toString()
                        + " marked for post-processing in PostPreProcessFilterSplittingVisitor");
                    
	                break;
	            }
	            
	        }
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
	     */
	    public void visit(BetweenFilter filter) {
	        if( original==null )
	        	original=filter;

	        if (fcs.supports(FilterCapabilities.BETWEEN)) {
	            int i = postStack.size();
	            Expression leftValue = filter.getLeftValue();
	            Expression middleValue = filter.getMiddleValue();
	            Expression rightValue = filter.getRightValue();
	            if( leftValue==null 
	            		|| rightValue==null 
	            		|| middleValue==null ){
	            	postStack.push(filter);
	            	return; 
	            	
	            }
				leftValue.accept(this);
	
	            if (i < postStack.size()) {
	            	// post process it
	            	postStack.pop();
	            	postStack.push(filter);
	
	                return;
	            }
	
				middleValue.accept(this);
	
	            if (i < postStack.size()) {
	            	// post process it
	            	preStack.pop(); // left side
	            	postStack.pop();
	            	postStack.push(filter);
	
	                return;
	            }
	
				rightValue.accept(this);

	            if (i < postStack.size()) {
	            	// post process it
	            	preStack.pop(); // left side
	            	preStack.pop(); // middle
	            	postStack.pop();
	            	postStack.push(filter);
	            	
	            	return;
	            }
	            
            	preStack.pop(); // left side
            	preStack.pop(); // middle
            	preStack.pop(); // right side
	            
	            preStack.push(filter);
	        } else {
            	postStack.push(filter);
	        }
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
	     */
	    public void visit(CompareFilter filter) {
	        if( original==null )
	        	original=filter;

	        // supports it as a group -- no need to check the type
	        if (!fcs.supports(FilterCapabilities.SIMPLE_COMPARISONS) ) {
	            postStack.push(filter);
	            return;
	        }
	
	        int i = postStack.size();
	        Expression leftValue = filter.getLeftValue();
	        Expression rightValue = filter.getRightValue();
	        if( leftValue==null || rightValue==null ){
	        	postStack.push(filter);
	        	return;
	        }

	        leftValue.accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
	
			rightValue.accept(this);
	
	        if (i < postStack.size()) {
	        	preStack.pop(); // left
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
            
        	preStack.pop(); // left side
        	preStack.pop(); // right side
	        preStack.push(filter);
	        
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
	     */
	    public void visit(GeometryFilter filter) {
	        if( original==null )
	        	original=filter;

	        switch (filter.getFilterType()) {
	        case FilterType.GEOMETRY_BBOX:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_BBOX) ) {
	
	            	// JE:  This is not documented and I can not figure out why if Filter is not supported that there is
	            	// any reason that the filter should still be sent to server.
	            	// Only thing I can think of is that BBox is ALWAYS supported but in that case an exception should
	            	// be thrown instead.
//	                if (filter.getLeftGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
//	                    LiteralExpression le = (LiteralExpression) filter
//	                        .getLeftGeometry();
//	
//	                    if ((le == null) || (le.getLiteral() == null)
//	                            || !(le.getLiteral() instanceof Geometry)) {
//	                    	postStack.push(filter);
//	
//	                        return;
//	                    }
//	
//	                } else {
//	                    if (filter.getRightGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
//	                        LiteralExpression le = (LiteralExpression) filter
//	                            .getRightGeometry();
//	
//	                        if ((le == null) || (le.getLiteral() == null)
//	                                || !(le.getLiteral() instanceof Geometry)) {
//	                        	postStack.push(filter);
//	
//	                            return;
//	                        }
//	                    } else {
//	                    	postStack.push(filter);
//	
//	                        return;
//	                    }
//	                }
	            	
	            	postStack.push(filter);
	            	return;
	            	
	            }
	
            	break;
	        case FilterType.GEOMETRY_BEYOND:
	
	            if (!fcs.supports(FilterCapabilities.SPATIAL_BEYOND) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_CONTAINS:
	
	            if (!fcs.supports(FilterCapabilities.SPATIAL_CONTAINS) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_CROSSES:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_CROSSES) ) {
		        	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_DISJOINT:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_DISJOINT) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_DWITHIN:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_DWITHIN) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_EQUALS:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_EQUALS) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	            
	            // JE: this was missing intentional?  I'm adding it since it make the test work as expected
	            // see WFSFilterVisitorGeometryTest#testVisitGeometryFilterEquals()
	            break;
	        case FilterType.GEOMETRY_INTERSECTS:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_INTERSECT) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_OVERLAPS:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_OVERLAPS) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_TOUCHES:
	
	            if (!fcs.supports(FilterCapabilities.SPATIAL_TOUCHES) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_WITHIN:
	
	            if (!fcs.supports( FilterCapabilities.SPATIAL_WITHIN) ) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        default:
	        	postStack.push(filter);
	
	            return;
	        }
            
            // TODO check against tranasaction ?
	
	        int i = postStack.size();
	        Expression leftGeometry = filter.getLeftGeometry();
	        Expression rightGeometry = filter.getRightGeometry();
	        if( leftGeometry==null || rightGeometry==null ){
	        	postStack.push(filter);
	        	return;
	        }
			leftGeometry.accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
	
			rightGeometry.accept(this);
	
	        if (i < postStack.size()) {
	        	preStack.pop(); // left
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
            
        	preStack.pop(); // left side
        	preStack.pop(); // right side
	        preStack.push(filter);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
	     */
	    public void visit(LikeFilter filter) {
	        if( original==null )
	        	original=filter;

	        if (!fcs.supports( FilterCapabilities.LIKE) ) {
	        	postStack.push(filter);
	
	            return;
	        }
	
	        int i = postStack.size();
	        filter.getValue().accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
            
        	preStack.pop(); // value
	        preStack.push(filter);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
	     */
	    public void visit(LogicFilter filter) {
	        if( original==null )
	        	original=filter;

	        if (!fcs.supports( FilterCapabilities.LOGICAL) ) {
	        	postStack.push(filter);
	
	            return;
	        }

            int i = postStack.size();
            int j = preStack.size();
	        if (filter.getFilterType() == FilterType.LOGIC_NOT) {
	            // should only have one child
	            Iterator it = filter.getFilterIterator();
	
	            if (it.hasNext()) {
	                Filter next = (Filter) it.next();
					(next).accept(this);
	                
	                if (i < postStack.size()) {
	                	// since and can split filter into both pre and post parts
	                	// the parts have to be combined since ~(A^B) == ~A | ~B
	                	// combining is easy since filter==combined result however both post and pre stacks
	                	// must be cleared since both may have components of the filter
	                	popToSize(postStack,i);
	                	popToSize(preStack, j);
	                	postStack.push(filter);
	                }else{ 
	                	popToSize(preStack,j);
                        preStack.push(filter);
	                }
	            }
	        } else {
	            if (filter.getFilterType() == FilterType.LOGIC_OR) {
	                Filter orReplacement;
	
	                try {
	                    orReplacement = translateOr(filter);
	                    orReplacement.accept(this);
	                } catch (IllegalFilterException e) {
	                	popToSize(preStack,j);
	                	postStack.push(filter);
	                    return;
	                }
	                if( postStack.size()>i ){
	                	popToSize(postStack,i);
	                	postStack.push(filter);

	                	return;
	                }

	                preStack.pop();
                	preStack.push(filter);
	            } else {
	                // more than one child
	                Iterator it = filter.getFilterIterator();
	
	                while (it.hasNext()) {
	                    Filter next = (Filter) it.next();
						(next).accept(this);
	                }
	
	                //combine the unsupported and add to the top
	                if (i < postStack.size()) {
	                    if (filter.getFilterType() == FilterType.LOGIC_AND) {
	                        Filter f = (Filter) postStack.pop();
	
	                        while (postStack.size() > i)
	                            f= (Filter) f.and((Filter) postStack.pop());
	
	                        postStack.push(f);
	                        
	                        if(j<preStack.size()){
	                        	f = (Filter)preStack.pop();
	                        	
		                        while (preStack.size() > j)
		                            f=(Filter) f.and((Filter) preStack.pop());
		                        preStack.push(f);
	                        }
	                    } else {
                            logger.warning(
	                            "LogicFilter found which is not 'and, or, not");

                            popToSize(postStack, i);
                            popToSize(preStack, j);
	
	                        postStack.push(filter);
	                    }
	                } else {
	                	popToSize(preStack,j);
                        preStack.push(filter);              
	                }
	            }
	        }
	    }

		private void popToSize(Stack stack, int j) {
			while( j<stack.size() ){
				stack.pop();
			}
		}
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
	     */
	    public void visit(NullFilter filter) {
	        if( original==null )
	        	original=filter;

	        if (!fcs.supports( FilterCapabilities.NULL_CHECK) ) {
	        	postStack.push(filter);
	
	            return;
	        }
	
	        int i = postStack.size();
	        filter.getNullCheckValue().accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	        }
            
        	preStack.pop(); // null
	        preStack.push(filter);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
	     */
	    public void visit(FidFilter filter) {
	        if( original==null )
	        	original=filter;

	        // figure out how to check that this is top level.
	        // otherwise this is fine
	        if (!postStack.isEmpty()) {
	        	postStack.push(filter);
	        }
	        preStack.push(filter);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
	     */
	    public void visit(AttributeExpression expression) {
	    	// JE: removed deprecated code
	        if (parent != null  && parent.getAttributeType(expression.getAttributePath()) == null) {
	        	postStack.push(expression);
	        	return;
	        }
	        if(transactionAccessor!=null){
	        	Filter updateFilter= (Filter) transactionAccessor.getUpdateFilter(expression.getAttributePath());
	        	if( updateFilter!=null ){
	        		changedStack.add(updateFilter);
	        		preStack.push(updateFilter);
	        	}else
	        		preStack.push(expression);
	        } else{
	        	preStack.push(expression);
	        }
	    }

		/**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
	     */
	    public void visit(Expression expression) {
	    	postStack.push(expression);
            logger.warning(
	            "@see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)");
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
	     */
	    public void visit(LiteralExpression expression) {
	        if (expression.getLiteral() == null) {
	        	postStack.push(expression);
	        }
	        preStack.push(expression);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
	     */
	    public void visit(MathExpression expression) {
	        if (!fcs.supports( FilterCapabilities.SIMPLE_ARITHMETIC) ) {
	        	postStack.push(expression);
	
	            return;
	        }
	
	        int i = postStack.size();
	        Expression leftValue = expression.getLeftValue();
	        Expression rightValue = expression.getRightValue();
	        if( leftValue==null || rightValue==null ){
	        	postStack.push(expression);
	        	return;
	        }
	        leftValue.accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(expression);
	
	            return;
	        }
	
	        rightValue.accept(this);
	
	        if (i < postStack.size()) {
	        	preStack.pop(); // left
	        	postStack.pop();
	        	postStack.push(expression);
	
	            return;
	        }
            
        	preStack.pop(); // left side
        	preStack.pop(); // right side
	        preStack.push(expression);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
	     */
	    public void visit(FunctionExpression expression) {
	        if (!fcs.supports( FilterCapabilities.FUNCTIONS) || !fcs.supports(expression.getClass()) ) {
	        	postStack.push(expression);
	
	            return;
	        }
	
	        if (expression.getName() == null) {
	        	postStack.push(expression);
	
	            return;
	        }

	        int i = postStack.size();
	        int j = preStack.size();
	
	        for (int k = 0; k < expression.getArgCount(); k++) {
	            expression.getArgs()[k].accept(this);
	
	            if (i < postStack.size()) {
	            	while(j<preStack.size())
	            		preStack.pop();
	            	postStack.pop();
	                postStack.push(expression);
	
	                return;
	            }
	        }
            	while(j<preStack.size())
            		preStack.pop();
	        preStack.push(expression);
	    }
	
	    /**
	     * 
	     * @param filter
	     * @return Or Filter
	     * @throws IllegalFilterException
	     */
	    public Filter translateOr(LogicFilter filter)
	        throws IllegalFilterException {
	        if (filter.getFilterType() != FilterType.LOGIC_OR) {
	            return filter;
	        }
	
	        // a|b == ~~(a|b) negative introduction
	        // ~(a|b) == (~a + ~b) modus ponens
	        // ~~(a|b) == ~(~a + ~b) substitution
	        // a|b == ~(~a + ~b) negative simpilification
	        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
	        LogicFilter and = ff.createLogicFilter(FilterType.LOGIC_AND);
	        Iterator i = filter.getFilterIterator();
	
	        while (i.hasNext()) {
	            Filter f = (Filter) i.next();
	
	            if (f.getFilterType() == FilterType.LOGIC_NOT) {
	                // simplify it 
                    LogicFilter logic = (LogicFilter) f;
                    Filter next = (Filter) logic.getFilterIterator().next();
	                and.addFilter((org.opengis.filter.Filter) next );
	            } else {
	                and.addFilter(f.not());
	            }
	        }
	
	        return (Filter) and.not();
	    }

    public static class WFSBBoxFilterVisitor implements FilterVisitor{
        Envelope maxbbox;
        public WFSBBoxFilterVisitor(Envelope fsd){
            maxbbox = fsd;
        }public void visit(Filter filter) {
            if (org.geotools.filter.Filter.NONE == filter) {
                return;
            }
                switch (filter.getFilterType()) {
                case FilterType.BETWEEN:
                    visit((BetweenFilter) filter);
    
                    break;
    
                case FilterType.COMPARE_EQUALS:
                case FilterType.COMPARE_GREATER_THAN:
                case FilterType.COMPARE_GREATER_THAN_EQUAL:
                case FilterType.COMPARE_LESS_THAN:
                case FilterType.COMPARE_LESS_THAN_EQUAL:
                case FilterType.COMPARE_NOT_EQUALS:
                    visit((BetweenFilter) filter);
    
                    break;
    
                case FilterType.FID:
                    visit((BetweenFilter) filter);
    
                    break;
    
                case FilterType.GEOMETRY_BBOX:
                case FilterType.GEOMETRY_BEYOND:
                case FilterType.GEOMETRY_CONTAINS:
                case FilterType.GEOMETRY_CROSSES:
                case FilterType.GEOMETRY_DISJOINT:
                case FilterType.GEOMETRY_DWITHIN:
                case FilterType.GEOMETRY_EQUALS:
                case FilterType.GEOMETRY_INTERSECTS:
                case FilterType.GEOMETRY_OVERLAPS:
                case FilterType.GEOMETRY_TOUCHES:
                case FilterType.GEOMETRY_WITHIN:
                    visit((GeometryFilter) filter);
    
                    break;
    
                case FilterType.LIKE:
                    visit((LikeFilter) filter);
    
                    break;
    
                case FilterType.LOGIC_AND:
                case FilterType.LOGIC_NOT:
                case FilterType.LOGIC_OR:
                    visit((LogicFilter) filter);
    
                    break;
    
                case FilterType.NULL:
                    visit((NullFilter) filter);
    
                    break;
    
                default:
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
         */
        public void visit( BetweenFilter filter ) {
            if(filter!=null){
                if(filter.getLeftValue()!=null)
                        filter.getLeftValue().accept(this);
                if(filter.getRightValue()!=null)
                    filter.getRightValue().accept(this);
                if(filter.getMiddleValue()!=null)
                    filter.getMiddleValue().accept(this);
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
         */
        public void visit( CompareFilter filter ) {
            if(filter!=null){
                if(filter.getLeftValue()!=null)
                        filter.getLeftValue().accept(this);
                if(filter.getRightValue()!=null)
                    filter.getRightValue().accept(this);
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
         */
        public void visit( GeometryFilter filter ) {
            if(filter!=null){
                Expression leftGeometry = filter.getLeftGeometry();
				Expression rightGeometry = filter.getRightGeometry();
				switch (filter.getFilterType()) {
                
                            case FilterType.GEOMETRY_BBOX:
                                // find literal side and deal ...
                                Envelope bbox = null;
                                LiteralExpression le = null;
                                if (leftGeometry!=null && leftGeometry.getType() == ExpressionType.LITERAL_GEOMETRY) {
                                    le = (LiteralExpression) leftGeometry;
                                    if(le != null &&  le.getLiteral() != null && le.getLiteral() instanceof Geometry){                
                                        bbox = ((Geometry) le.getLiteral()).getEnvelopeInternal();
                                    }
                                } else {
                                    if (rightGeometry!=null && rightGeometry.getType() == ExpressionType.LITERAL_GEOMETRY) {
                                        le = (LiteralExpression) rightGeometry;
                                        if(le != null &&  le.getLiteral() != null && le.getLiteral() instanceof Geometry){
                                            Geometry g = (Geometry) le.getLiteral();
                                            bbox = g.getEnvelopeInternal();
                                        }
                                    }
                                }
                                if(bbox!=null){
                                    boolean changed = false;
                                    double minx,miny,maxx,maxy;
                                    minx = bbox.getMinX();
                                    miny = bbox.getMinY();
                                    maxx = bbox.getMaxX();
                                    maxy = bbox.getMaxY();
                                    if(minx < maxbbox.getMinX()){
                                        minx = maxbbox.getMinX();
                                        changed = true;
                                    }
                                    if(maxx > maxbbox.getMaxX()){
                                        maxx = maxbbox.getMaxX();
                                        changed = true;
                                    }
                                    if(miny < maxbbox.getMinY()){
                                        miny = maxbbox.getMinY();
                                        changed = true;
                                    }
                                    if(maxy > maxbbox.getMaxY()){
                                        maxy = maxbbox.getMaxY();
                                        changed = true;
                                    }
                                    if(changed){
                                        Envelope tmp = new Envelope(minx,maxx,miny,maxy);
                                        try {
                                            le.setLiteral((new GeometryFactory()).toGeometry(tmp));
                                        } catch (IllegalFilterException e) {
                                            logger.warning(e.toString());
                                        }
                                    }
                                }
                                return;
                            case FilterType.GEOMETRY_BEYOND:
                            case FilterType.GEOMETRY_CONTAINS:
                            case FilterType.GEOMETRY_CROSSES:
                            case FilterType.GEOMETRY_DISJOINT:
                            case FilterType.GEOMETRY_DWITHIN:
                            case FilterType.GEOMETRY_EQUALS:
                            case FilterType.GEOMETRY_INTERSECTS:
                            case FilterType.GEOMETRY_OVERLAPS:
                            case FilterType.GEOMETRY_TOUCHES:
                            case FilterType.GEOMETRY_WITHIN:
                            default:
                                if(leftGeometry!=null)
                                    leftGeometry.accept(this);
                                if(rightGeometry!=null)
                                    rightGeometry.accept(this);
                                
                }
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
         */
        public void visit( LikeFilter filter ) {
            if(filter!=null){
                if(filter.getValue()!=null)
                    filter.getValue().accept(this);
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
         */
        public void visit( LogicFilter filter ) {
            if(filter!=null){
                Iterator i = filter.getFilterIterator();
                while(i.hasNext()){
                    Filter tmp = (Filter)i.next();
                    tmp.accept(this);
                }
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
         */
        public void visit( NullFilter filter ) {
            if(filter!=null){
                if(filter.getNullCheckValue()!=null)
                    filter.getNullCheckValue().accept(this);
            }
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
         */
        public void visit( FidFilter filter ) {
            // do nothing
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
         */
        public void visit( AttributeExpression expression ) {
            // do nothing
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
         */
        public void visit( Expression expression ) {
            // do nothing
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
         */
        public void visit( LiteralExpression expression ) {
            // do nothing
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
         */
        public void visit( MathExpression expression ) {
            // do nothing
        }
        /*
         * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
         */
        public void visit( FunctionExpression expression ) {
            // do nothing
        }
    }
}
