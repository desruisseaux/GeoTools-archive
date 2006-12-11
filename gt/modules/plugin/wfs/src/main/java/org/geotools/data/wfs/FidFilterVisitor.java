/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FilterVisitor2;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.IncludeFilter;

/**
 * Changes all the Fids in FidFilters to be translated to the concrete fid.
 * <p>
 *  WFS has a difficult situation where FIDs are not assigned until after a commit.
 *  So in order to make it easier for the end programmer this visitor will convert any fids
 *  that were returned to client programs before the commit to the final FID after the commit
 *  was made.
 *  </p>
 *  
 * @author Jesse
 */
public class FidFilterVisitor implements FilterVisitor, FilterVisitor2 {

	private Stack current=new Stack();
	private Map fidMap;

	public FidFilterVisitor(Map fidMap) {
		this.fidMap=fidMap;
	}

    public void visit( ExcludeFilter filter ) {
        current.push(filter);
    }
    public void visit( IncludeFilter filter ) {        
        current.push(filter);
    }
	public void visit(Filter filter) {
		if (filter instanceof FidFilter) {
			FidFilter ff = (FidFilter) filter;
			visit(ff);
		}else if (filter instanceof LogicFilter) {
			LogicFilter lf = (LogicFilter) filter;
			visit(lf);
		}else
			current.push(filter);
	}

	public void visit(LogicFilter filter) {
		int stop=current.size();
		for (Iterator iter = filter.getFilterIterator(); iter.hasNext(); ){
			((Filter) iter.next()).accept(this);
		}
		Filter newFilter;
		switch( filter.getFilterType() ){
		case FilterType.LOGIC_AND:{
			newFilter=(Filter) current.pop();
			while ( current.size()>stop ){
				newFilter=newFilter.and((Filter) current.pop());
			}
			break;
		}
		case FilterType.LOGIC_OR:{
			newFilter=(Filter) current.pop();
			while ( current.size()>stop ){
				newFilter=newFilter.or((Filter) current.pop());
			}
			break;
		}
		case FilterType.LOGIC_NOT:{
			assert current.size()-stop==1;
			newFilter=(Filter) current.pop();
			newFilter=newFilter.not();
			break;
		}
		default:
			throw new IllegalArgumentException("Bug in FidFilterVisitor.  "+filter+" was not correctly handled by visitor");
		}
		
		current.push(newFilter);
	}

	public void visit(FidFilter filter) {
		String[] fids=filter.getFids();
		FidFilter newFilter=FilterFactoryFinder.createFilterFactory().createFidFilter();
		for (int i = 0; i < fids.length; i++) {
			String target=getFinalFid(fids[i]);
			newFilter.addFid(target);
		}
		current.push(newFilter);
	}

    /**
     * Returns the final version of the FID.  If a commit has changed the FID the new fid will be returned otherwise the same fid will be returned.  
     * 
     * @return the final version of the FID
     */
	public synchronized String getFinalFid(String fid) {
		String finalFid=(String) fidMap.get(fid);
		if( finalFid==null )
			return fid;
		return finalFid;
	}
	
	public void visit(BetweenFilter filter) {
		current.push(filter);
	}

	public void visit(CompareFilter filter) {
		current.push(filter);
	}

	public void visit(GeometryFilter filter) {
		current.push(filter);
	}

	public void visit(LikeFilter filter) {
		current.push(filter);
	}

	public void visit(NullFilter filter) {
		current.push(filter);
	}

	public void visit(LiteralExpression expression) {
		// nothing todo
	}

	public void visit(AttributeExpression expression) {
		// nothing todo
	}

	public void visit(Expression expression) {
		// nothing todo
	}

	public void visit(MathExpression expression) {
		// nothing todo
	}

	public void visit(FunctionExpression expression) {
		// nothing todo
	}

	public org.opengis.filter.Filter getProcessedFilter() {
		assert current.size()==1;
		return (org.opengis.filter.Filter) current.peek();
	}
}
