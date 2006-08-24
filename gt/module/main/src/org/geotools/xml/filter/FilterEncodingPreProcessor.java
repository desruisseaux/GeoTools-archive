/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xml.filter;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
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
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.expression.MathExpression;
import org.geotools.xml.XMLHandlerHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 * Prepares a filter for xml encoded for interoperability with another system.  It will behave differently depeding on
 * the compliance level chosen.  A new request will have to be made and the features will have
 * to be tested again on the client side if there are any FidFilters in the filter.  Consider the following to understand why:
 *
 * and {
 *                 nullFilter
 *                 or{
 *                         fidFilter
 *                         nullFilter
 *                 }
 * }
 *
 * for strict it would throw an exception, for low it would be left alone, but for Medium it would end up as:
 *
 *         and{
 *                 nullFilter
 *                 nullFilter
 *         }
 *
 * and getFids() would return the fids in the fidFilter.
 *
 * So the final filter would (this is not standard but a common implementation) return the results of the and filter as well as
 * all the features that match the fids.  Which is more than the original filter would accept.
 *
 * The XML Document writer can operate at different levels of compliance. The
 * geotools level is extremely flexible and forgiving.
 *
 * <p>
 * All NOT(FidFilter) are changed to Filter.NONE.  So make sure that the filter is processed again on the client with the original
 * filter
 * </p>
 *
 * For a description of the difference Compliance levels that can be used see
 * <ul>
 * <li>{@link XMLHandlerHints#VALUE_FILTER_COMPLIANCE_LOW}</li>
 * <li>{@link XMLHandlerHints#VALUE_FILTER_COMPLIANCE_MEDIUM}</li>
 * <li>{@link XMLHandlerHints#VALUE_FILTER_COMPLIANCE_HIGH}</li>
 * </ul>
 *
 * @author Jesse
 */
public class FilterEncodingPreProcessor implements FilterVisitor {
    private static final int LOW = 0;
    private static final int MEDIUM = 1;
    private static final int HIGH = 2;
    private int complianceInt;
    private Stack current = new Stack();
    FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();
    private boolean requiresPostProcessing=false;

    public FilterEncodingPreProcessor(Integer complianceLevel) {
        if ((complianceLevel != XMLHandlerHints.VALUE_FILTER_COMPLIANCE_LOW)
                && (complianceLevel != XMLHandlerHints.VALUE_FILTER_COMPLIANCE_MEDIUM)
                && (complianceLevel != XMLHandlerHints.VALUE_FILTER_COMPLIANCE_HIGH)) {
            throw new IllegalArgumentException(
                "compliance level must be one of: XMLHandlerHints.VALUE_FILTER_COMPLIANCE_LOOSE "
                + "XMLHandlerHints.VALUE_FILTER_COMPLIANCE_MEDIUM or "
                + "XMLHandlerHints.VALUE_FILTER_COMPLIANCE_MAXIMUM");
        }

        this.complianceInt = complianceLevel.intValue();
    }

    /**
     * Gets the fid filter that contains all the fids.
     *
     * @return the fid filter that contains all the fids.
     */
    public FidFilter getFidFilter() {
        FidFilter filter = filterFac.createFidFilter();

        if (current.isEmpty()) {
            return filter;
        }

        Data data = (Data) current.peek();

        if (data.fids.size() > 0) {
            filter.addAllFids(data.fids);
        }

        return filter;
    }

    /**
     * Returns the filter that can be encoded.
     *
     * @return the filter that can be encoded.
     */
    public Filter getFilter() {
        if( current.isEmpty() )
            return Filter.ALL;
        return ((Data) this.current.peek()).filter;
    }

    public void visit(Filter filter) {
        if (filter instanceof BetweenFilter || filter instanceof BetweenFilter
                || filter instanceof CompareFilter
                || filter instanceof GeometryFilter
                || filter instanceof LikeFilter
                || filter instanceof LogicFilter
                || filter instanceof NullFilter || filter instanceof FidFilter) {
            filter.accept(this);
        } else {
            current.push(new Data(filter));
        }
    }

    public void visit(BetweenFilter filter) {
        current.push(new Data(filter));
    }

    public void visit(CompareFilter filter) {
        current.push(new Data(filter));
    }

    public void visit(GeometryFilter filter) {
        current.push(new Data(filter));
    }

    public void visit(LikeFilter filter) {
        current.push(new Data(filter));
    }

    public void visit(LogicFilter filter) {
        int startSize = current.size();

        try {
            switch (this.complianceInt) {
            case LOW:
                current.push(new Data(filter));

                break;

            case MEDIUM:

                for (Iterator iter = filter.getFilterIterator();
                        iter.hasNext();) {
                    Filter component = (Filter) iter.next();
                    component.accept(this);
                }

                current.push(createMediumLevelLogicFilter(
                        filter.getFilterType(), startSize));

                break;

            case HIGH:

                for (Iterator iter = filter.getFilterIterator();
                        iter.hasNext();) {
                    Filter component = (Filter) iter.next();
                    component.accept(this);
                }

                current.push(createHighLevelLogicFilter(
                        filter.getFilterType(), startSize));

                break;

            default:
                break;
            }
        } catch (Exception e) {
            if (e instanceof UnsupportedFilterException) {
                throw (UnsupportedFilterException) e;
            }

            throw new UnsupportedFilterException("Exception creating filter", e);
        }
    }

    private Data createMediumLevelLogicFilter(short filterType,
        int startOfFilterStack) throws IllegalFilterException {
        Data resultingFilter;

        switch (filterType) {
        case FilterType.LOGIC_AND: {
            Set fids = andFids(startOfFilterStack);
            resultingFilter = buildFilter(filterType, startOfFilterStack);
            resultingFilter.fids.addAll(fids);
            
            if( resultingFilter.filter!=Filter.ALL && !fids.isEmpty() )
                requiresPostProcessing=true;
            break;
        }

        case FilterType.LOGIC_OR: {
            Set fids = orFids(startOfFilterStack);
            resultingFilter = buildFilter(filterType, startOfFilterStack);
            resultingFilter.fids.addAll(fids);
            break;
        }

        case FilterType.LOGIC_NOT:
            resultingFilter = buildFilter(filterType, startOfFilterStack);
            break;

        default:
            resultingFilter = buildFilter(filterType, startOfFilterStack);

            break;
        }

        return resultingFilter;
    }

    private Set orFids(int startOfFilterStack) {
        Set set = new HashSet();

        for (int i = startOfFilterStack; i < current.size(); i++) {
            Data data = (Data) current.get(i);

            if (!data.fids.isEmpty()) {
                set.addAll(data.fids);
            }
        }

        return set;
    }

    private Set andFids(int startOfFilterStack) {
        if (!hasFidFilter(startOfFilterStack)) {
            return Collections.EMPTY_SET;
        }

        Set toRemove = new HashSet();
        List fidSet = new ArrayList();
        boolean doRemove = true;

        for (int i = startOfFilterStack; i < current.size(); i++) {
            Data data = (Data) current.get(i);

            if (data.fids.isEmpty()) {
                toRemove.add(data);
            } else {
                fidSet.add(data.fids);

                if (data.filter != Filter.ALL) {
                    doRemove = false;
                }
            }
        }

        if (doRemove) {
            current.removeAll(toRemove);
        }

        if (fidSet.size() == 0) {
            return Collections.EMPTY_SET;
        }

        if (fidSet.size() == 1) {
            return (Set) fidSet.get(0);
        }

        HashSet set = new HashSet();

        for (int i = 0; i < fidSet.size(); i++) {
            Set tmp = (Set) fidSet.get(i);

            for (Iterator iter = tmp.iterator(); iter.hasNext();) {
                String fid = (String) iter.next();

                if (allContain(fid, fidSet)) {
                    set.add(fid);
                }
            }
        }

        return set;
    }

    private boolean allContain(String fid, List fidSets) {
        for (int i = 0; i < fidSets.size(); i++) {
            Set tmp = (Set) fidSets.get(i);

            if (!tmp.contains(fid)) {
                return false;
            }
        }

        return true;
    }

    private Data buildFilter(short filterType, int startOfFilterStack)
        throws IllegalFilterException {
        if (current.isEmpty()) {
            return Data.ALL;
        }

        if (filterType == FilterType.LOGIC_NOT) {
            return buildNotFilter(startOfFilterStack);
        }

        if (current.size() == (startOfFilterStack + 1)) {
            return (Data) current.pop();
        }

        LogicFilter f = filterFac.createLogicFilter(filterType);

        while (current.size() > startOfFilterStack) {
            Data data = (Data) current.pop();

            if (data.filter != Filter.ALL) {
                f.addFilter(data.filter);
            }
        }

        return new Data(compressFilter(filterType, f));
    }

    private Filter compressFilter(short filterType, LogicFilter f)
        throws IllegalFilterException {
        LogicFilter result;
        int added = 0;

        switch (filterType) {
        case FilterType.LOGIC_AND:

            if (contains(f, Filter.ALL)) {
                return Filter.ALL;
            }

            result = filterFac.createLogicFilter(filterType);

            for (Iterator iter = f.getFilterIterator(); iter.hasNext();) {
                Filter filter = (Filter) iter.next();

                if (filter == Filter.NONE) {
                    continue;
                }

                added++;
                result.addFilter(filter);
            }

            if (!result.getFilterIterator().hasNext()) {
                return Filter.ALL;
            }

            break;

        case FilterType.LOGIC_OR:

            if (contains(f, Filter.NONE)) {
                return Filter.NONE;
            }

            result = filterFac.createLogicFilter(filterType);

            for (Iterator iter = f.getFilterIterator(); iter.hasNext();) {
                Filter filter = (Filter) iter.next();

                if (filter == Filter.ALL) {
                    continue;
                }

                added++;
                result.addFilter(filter);
            }

            if (!result.getFilterIterator().hasNext()) {
                return Filter.ALL;
            }

            break;

        default:
            return Filter.ALL;
        }

        switch (added) {
        case 0:
            return Filter.ALL;

        case 1:
            return (Filter) result.getFilterIterator().next();

        default:
            return result;
        }
    }

    private boolean contains(LogicFilter f, Filter toFind) {
        for (Iterator iter = f.getFilterIterator(); iter.hasNext();) {
            Filter filter = (Filter) iter.next();

            if (filter.equals(toFind)) {
                return true;
            }
        }

        return false;
    }

    private Data buildNotFilter(int startOfFilterStack) {
        if (current.size() > (startOfFilterStack + 1)) {
            throw new UnsupportedFilterException(
                "A not filter cannot have more than one filter");
        } else {
            Data tmp = (Data) current.pop();

            Data data = new Data(tmp.filter.not());

            if (!tmp.fids.isEmpty()) {
                data.filter = Filter.NONE;
                data.fids.clear();
                requiresPostProcessing=true;
            }

            return data;
        }
    }

    private Data createHighLevelLogicFilter(short filterType,
        int startOfFilterStack) throws IllegalFilterException {
        if (hasFidFilter(startOfFilterStack)) {
            Set fids;

            switch (filterType) {
            case FilterType.LOGIC_AND:
                fids = andFids(startOfFilterStack);

                Data filter = buildFilter(filterType, startOfFilterStack);
                filter.fids.addAll(fids);

                return filter;

            case FilterType.LOGIC_OR: {
                if (hasNonFidFilter(startOfFilterStack)) {
                    throw new UnsupportedFilterException(
                        "Maximum compliance does not allow Logic filters to contain FidFilters");
                }

                fids = orFids(startOfFilterStack);
                
                pop(startOfFilterStack);

                Data data = new Data();
                data.fids.addAll(fids);

                return data;
            }

            case FilterType.LOGIC_NOT:
                return buildFilter(filterType, startOfFilterStack);

            default:
                return Data.ALL;
            }
        } else {
            return buildFilter(filterType, startOfFilterStack);
        }
    }

    private void pop(int startOfFilterStack) {
        while (current.size() > startOfFilterStack)
            current.pop();
    }

    private boolean hasNonFidFilter(int startOfFilterStack) {
        for (int i = startOfFilterStack; i < current.size(); i++) {
            Data data = (Data) current.get(i);

            if (data.filter != Filter.ALL) {
                return true;
            }
        }

        return false;
    }

    private boolean hasFidFilter(int startOfFilterStack) {
        for (int i = startOfFilterStack; i < current.size(); i++) {
            Data data = (Data) current.get(i);

            if (!data.fids.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void visit(NullFilter filter) {
        current.push(new Data(filter));
    }

    public void visit(FidFilter filter) {
        Data data = new Data();
        data.fids.addAll(Arrays.asList(filter.getFids()));
        current.push(data);
    }

    public void visit(AttributeExpression expression) {
        // nothing todo
    }

    public void visit(Expression expression) {
        // nothing todo
    }

    public void visit(LiteralExpression expression) {
        // nothing todo
    }

    public void visit(MathExpression expression) {
        // nothing todo
    }

    public void visit(FunctionExpression expression) {
        // nothing todo
    }

    private static class Data {
        final public static Data NONE = new Data(Filter.NONE);
        final public static Data ALL = new Data(Filter.ALL);
        final Set fids = new HashSet();
        Filter filter = Filter.ALL;

        public Data() {
        }

        public Data(Filter f) {
            filter = f;
        }

        public String toString() {
            return filter + ":" + fids;
        }
    }

    /**
     * Returns true if the filter was one where the request to the server is more general than the actual filter.  
     * See {@link XMLHandlerHints#VALUE_FILTER_COMPLIANCE_MEDIUM} and example of when this can happen.
     * 
     * @return true if the filter was one where the request to the server is more general than the actual filter.
     */
    public boolean requiresPostProcessing() {
        return requiresPostProcessing;
    }
}
