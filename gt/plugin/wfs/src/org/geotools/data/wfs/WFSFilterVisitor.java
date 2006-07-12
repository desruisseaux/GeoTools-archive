
package org.geotools.data.wfs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.geotools.data.wfs.Action.UpdateAction;
import org.geotools.feature.FeatureType;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilitiesMask;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.NullFilter;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.ExpressionType;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.expression.MathExpression;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Determines what queries can be processed server side and which can be processed client side.
 * 
 * @author dzwiers
 * @source $URL$
 */
public class WFSFilterVisitor implements FilterVisitor {
	    private Stack postStack = new Stack();
	    private Stack preStack = new Stack();
	    /** 
	     * Operates similar to postStack.  When a update is determined to affect an attribute expression the update
	     * filter is pushed on to the stack, then ored with the filter that contains the expression.
	     */
	    private Set changedStack=new HashSet();
	    private FilterCapabilitiesMask fcs = null;
	    private FeatureType parent = null;
	    private WFSTransactionState state = null;
		private Filter original = null;
	
	    private WFSFilterVisitor() {
	    	// do nothing
	    }
	
	    WFSFilterVisitor(FilterCapabilitiesMask fcs, FeatureType parent, WFSTransactionState state) {
	        this.fcs = fcs;
	        this.parent = parent;
	        this.state = state;
	    }
		
	    Filter getFilterPost() {
			if (!changedStack.isEmpty())
				// Return the original filter to ensure that
				// correct features are filtered
				return original ;
	
	        if (postStack.size() > 1) {
	            WFSDataStoreFactory.logger.warning("Too many post stack items after run: "
	                + postStack.size());
	        }
	        
	        // JE:  Changed to peek because get implies that the value can be retrieved multiple times
	        Filter f = postStack.isEmpty() ? Filter.NONE : (Filter) postStack.peek();
	        return f;
	    }
		
	    Filter getFilterPre() {
	        if (preStack.isEmpty()) {
	            return Filter.NONE;
	        }
	
	        if (preStack.size() > 1) {
	            WFSDataStoreFactory.logger.warning("Too many pre stack items after run: "
	                + preStack.size());
	        }

	
	        // JE:  Changed to peek because get implies that the value can be retrieved multiple times
	        Filter f = preStack.isEmpty() ? Filter.NONE : (Filter) preStack.peek();
	        if( changedStack.isEmpty())
	        	return f;
	        
	        Iterator iter=changedStack.iterator();
	        Filter updateFilter=(Filter) iter.next();
	        while( iter.hasNext() )
	        	updateFilter=updateFilter.or((Filter) iter.next());
	        return f.or(updateFilter);
	    }
	
	    /**
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)
	     */
	    public void visit(Filter filter) {
	        if (Filter.NONE == filter) {
	            return;
	        }
	        if( original==null )
	        	original=filter;
	        if (!postStack.isEmpty()) {
	        	postStack.push(filter);
                WFSDataStoreFactory.logger.warning(
	                "@see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)");
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
	            WFSDataStoreFactory.logger.warning(
	                    "@see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Filter)");
	
	                break;
	            }
	            
	            // deal with deletes here !!!
	            if(state != null){
	            	List l = state.getActions();
	            	Iterator i = l.iterator();
	            	Filter pre = (Filter)preStack.pop();
	            	if(pre != null){
	            	while(i.hasNext()){
	            		Action a = (Action)i.next();
	            		if(a.getType() == Action.DELETE){
	            			pre = pre.and(a.getFilter());
	            		}
	            	}
	            	preStack.push(pre);
	            	}
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

	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.BETWEEN) == FilterCapabilitiesMask.BETWEEN) {
	            int i = postStack.size();
	            filter.getLeftValue().accept(this);
	
	            if (i < postStack.size()) {
	            	// post process it
	            	postStack.pop();
	            	postStack.push(filter);
	
	                return;
	            }
	
	            filter.getMiddleValue().accept(this);
	
	            if (i < postStack.size()) {
	            	// post process it
	            	preStack.pop(); // left side
	            	postStack.pop();
	            	postStack.push(filter);
	
	                return;
	            }
	
	            filter.getRightValue().accept(this);

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
	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.SIMPLE_COMPARISONS) != FilterCapabilitiesMask.SIMPLE_COMPARISONS) {
	            postStack.push(filter);
	            return;
	        }
	
	        int i = postStack.size();
	        filter.getLeftValue().accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
	
	        filter.getRightValue().accept(this);
	
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
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.BBOX) != FilterCapabilitiesMask.BBOX) {
	
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
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.BEYOND) != FilterCapabilitiesMask.BEYOND) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_CONTAINS:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.CONTAINS) != FilterCapabilitiesMask.CONTAINS) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_CROSSES:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.CROSSES) != FilterCapabilitiesMask.CROSSES) {
		        	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_DISJOINT:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.DISJOINT) != FilterCapabilitiesMask.DISJOINT) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_DWITHIN:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.DWITHIN) != FilterCapabilitiesMask.DWITHIN) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_EQUALS:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.EQUALS) != FilterCapabilitiesMask.EQUALS) {
	            	postStack.push(filter);
	
	                return;
	            }
	            
	            // JE: this was missing intentional?  I'm adding it since it make the test work as expected
	            // see WFSFilterVisitorGeometryTest#testVisitGeometryFilterEquals()
	            break;
	        case FilterType.GEOMETRY_INTERSECTS:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.INTERSECT) != FilterCapabilitiesMask.INTERSECT) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_OVERLAPS:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.OVERLAPS) != FilterCapabilitiesMask.OVERLAPS) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_TOUCHES:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.TOUCHES) != FilterCapabilitiesMask.TOUCHES) {
	            	postStack.push(filter);
	
	                return;
	            }
	
	            break;
	
	        case FilterType.GEOMETRY_WITHIN:
	
	            if ((fcs.getSpatialOps() & FilterCapabilitiesMask.WITHIN) != FilterCapabilitiesMask.WITHIN) {
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
	        filter.getLeftGeometry().accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(filter);
	
	            return;
	        }
	
	        filter.getRightGeometry().accept(this);
	
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

	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.LIKE) != FilterCapabilitiesMask.LIKE) {
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

	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.LOGICAL) != FilterCapabilitiesMask.LOGICAL) {
	        	postStack.push(filter);
	
	            return;
	        }

            int i = postStack.size();
            int j = preStack.size();
	        if (filter.getFilterType() == FilterType.LOGIC_NOT) {
	            // should only have one child
	            Iterator it = filter.getFilterIterator();
	
	            if (it.hasNext()) {
	                ((Filter) it.next()).accept(this);
	                
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
	                    ((Filter) it.next()).accept(this);
	                }
	
	                //combine the unsupported and add to the top
	                if (i < postStack.size()) {
	                    if (filter.getFilterType() == FilterType.LOGIC_AND) {
	                        Filter f = (Filter) postStack.pop();
	
	                        while (postStack.size() > i)
	                            f=f.and((Filter) postStack.pop());
	
	                        postStack.push(f);
	                        
	                        if(j<preStack.size()){
	                        	f = (Filter)preStack.pop();
	                        	
		                        while (preStack.size() > j)
		                            f=f.and((Filter) preStack.pop());
		                        preStack.push(f);
	                        }
	                    } else {
                            WFSDataStoreFactory.logger.warning(
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

	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.NULL_CHECK) != FilterCapabilitiesMask.NULL_CHECK) {
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
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.AttributeExpression)
	     */
	    public void visit(AttributeExpression expression) {
	    	// JE: removed deprecated code
	        if ( parent.getAttributeType(expression.getAttributePath())==null ) {
	        	postStack.push(expression);
	        }
	        if(state!=null){
	        // TODO must check transaction here !!!
	        List actions = state.getActions();
	        Iterator i = actions.iterator();
	        while(i.hasNext()){
	        	Action a = (Action)i.next();
	        	if(a.getType() == Action.UPDATE){
	        		UpdateAction ua = (UpdateAction)a;
	        		if(ua.getProperty(expression.getAttributePath())!=null){
	        			changedStack.add(a.getFilter());
	        			preStack.push(a.getFilter());
	        			return;
	        		}
	        	}
	        }}
	        
	        preStack.push(expression);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.Expression)
	     */
	    public void visit(Expression expression) {
	    	postStack.push(expression);
            WFSDataStoreFactory.logger.warning(
	            "@see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)");
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.LiteralExpression)
	     */
	    public void visit(LiteralExpression expression) {
	        if (expression.getLiteral() == null) {
	        	postStack.push(expression);
	        }
	        preStack.push(expression);
	    }
	
	    /**
	     * 
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.MathExpression)
	     */
	    public void visit(MathExpression expression) {
	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.SIMPLE_ARITHMETIC) != FilterCapabilitiesMask.SIMPLE_ARITHMETIC) {
	        	postStack.push(expression);
	
	            return;
	        }
	
	        int i = postStack.size();
	        expression.getLeftValue().accept(this);
	
	        if (i < postStack.size()) {
	        	postStack.pop();
	        	postStack.push(expression);
	
	            return;
	        }
	
	        expression.getRightValue().accept(this);
	
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
	     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.expression.FunctionExpression)
	     */
	    public void visit(FunctionExpression expression) {
	        if ((fcs.getScalarOps() & FilterCapabilitiesMask.FUNCTIONS) != FilterCapabilitiesMask.FUNCTIONS) {
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
	                and.addFilter((Filter) ((LogicFilter) f).getFilterIterator()
	                                        .next());
	            } else {
	                and.addFilter(f.not());
	            }
	        }
	
	        return and.not();
	    }

    public static class WFSBBoxFilterVisitor implements FilterVisitor{
        Envelope maxbbox;
        public WFSBBoxFilterVisitor(Envelope fsd){
            maxbbox = fsd;
        }public void visit(Filter filter) {
            if (Filter.NONE == filter) {
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
                switch (filter.getFilterType()) {
                
                            case FilterType.GEOMETRY_BBOX:
                                // find literal side and deal ...
                                Envelope bbox = null;
                                LiteralExpression le = null;
                                if (filter.getLeftGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
                                    le = (LiteralExpression) filter.getLeftGeometry();
                                    if(le != null &&  le.getLiteral() != null && le.getLiteral() instanceof Geometry){                
                                        bbox = ((Geometry) le.getLiteral()).getEnvelopeInternal();
                                    }
                                } else {
                                    if (filter.getRightGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
                                        le = (LiteralExpression) filter.getRightGeometry();
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
                                            WFSDataStoreFactory.logger.warning(e.toString());
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
                                if(filter.getLeftGeometry()!=null)
                                    filter.getLeftGeometry().accept(this);
                                if(filter.getRightGeometry()!=null)
                                    filter.getRightGeometry().accept(this);
                                
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
