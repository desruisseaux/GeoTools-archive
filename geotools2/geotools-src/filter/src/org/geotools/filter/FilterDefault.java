/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

/**
 * Implements Filter interface, with constants and default behaviors for methods
 *
 * 
 * @author Rob Hranac, Vision for New York
 * @version 4/18/02
 */
public class FilterDefault implements Filter {

    /** Defines filter type (all valid types defined below). */
    protected short filterType;

    /** Sets the permissiveness of the filter construction handling. */
    protected boolean permissiveConstruction = true;

    /* *************************************************************************
     * This is a listing of all possible filter types, grouped by types that
     * are implemented by a single filter (ie. all logic types are implemented
     * by FilterLogic).
     * ************************************************************************/

    /* Types implemented by FilterLogic */
    /** Defines a logical 'OR' filter. */  
    public static final short LOGIC_OR = 1;
    /** Defines a logical 'AND' filter. */
    public static final short LOGIC_AND = 2;
    /** Defines a logical 'NOT' filter. */
    public static final short LOGIC_NOT = 3;

    /* Types implemented by FilterGeometry */
    /** Defines a geometric bounding box filter. */
    public static final short GEOMETRY_BBOX = 4;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_EQUALS = 5;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_DISJOINT = 6;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_INTERSECTS = 7;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_TOUCHES = 8;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_CROSSES = 9;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_WITHIN = 10;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_CONTAINS = 11;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_OVERLAPS = 12;
    /** Defines a logical 'NOT' operator. */
    public static final short GEOMETRY_BEYOND = 13;

    /* Types implemented by FilterCompare */
    /** Defines a comparative equals filter (may be a math filter). */
    public static final short COMPARE_EQUALS = 14;
    /** Defines a comparative less than filter (is a math filter). */
    public static final short COMPARE_LESS_THAN = 15;
    /** Defines a comparative greater than filter (is a math filter). */
    public static final short COMPARE_GREATER_THAN = 16;
    /** Defines a comparative less than/equals filter (is a math filter). */
    public static final short COMPARE_LESS_THAN_EQUAL = 17;
    /** Defines a comparative greater than/equals filter (is a math filter). */
    public static final short COMPARE_GREATER_THAN_EQUAL = 18;

    /** Defines a between filter, which is implemented by FilterBetween.
        Note that this filter is defined as a math filter. */
    public static final short BETWEEN = 19;

    /** Defines a null filter, which is implemented by FilterNull. */
    public static final short NULL = 21;

    /** Defines a like filter, which is implemented by FilterLike. */
    public static final short LIKE = 20;


    /**
     * Implements a 'contained by' check for a given feature, defaulting to true
     * 
     * @param feature Specified feature to examine.
     * @return Result of 'contains' test.
     */
    public boolean contains(Feature feature)
        throws MalformedFilterException {
        return true;
    }
    
    /**
     * Default implementation for OR - should be sufficient for most filters.
     *
     * @param feature Parent of the filter: must implement GMLHandlerGeometry.
     * @return ORed filter.
     */
    public Filter or(Filter filter) {
        try {
            return new FilterLogic(this, filter, LOGIC_OR);
        }
        catch(IllegalFilterException e) {
            return filter;
        }
    }
    
    /**
     * Default implementation for AND - should be sufficient for most filters.
     *
     * @param feature Parent of the filter: must implement GMLHandlerGeometry.
     * @return ANDed filter.
     */
    public Filter and(Filter filter) {
        try {
            return new FilterLogic(this, filter, LOGIC_AND);
        }
        catch(IllegalFilterException e) {
            return filter;
        }
    }
    
    /**
     * Default implementation for NOT - should be sufficient for most filters.
     *
     * @return NOTed filter.
     */
    public Filter not() {
        try {
            return new FilterLogic(this, LOGIC_NOT);
        }
        catch(IllegalFilterException e) {
            return this;
        }
    }

    /**
     * <p>Permissive construction allows expressions to contain attributes with
     * insecure types and filters to contain expressions with uninsured types.
     * In this context, 'uninsured' means that the actual type of the
     * expression does not have to conform to the declared type.</p>
     *
     * <p>All of the filter routines are guaranteed to handle errors correctly
     * as long as construction is set to be non-permissive.  This means that
     * the filter may still throw a MalformedFilterException when asked to
     * evaluate a feature, but it should never throw anything else.  However,
     * if permissiveness is set, then this is not true.  In this case, it
     * is entirely possible the filter will fail (during casting) without
     * a graceful exit.</p>
     *
     * @param permissiveConstruction Indicates permissive construction.
     */
    public void setPermissive(boolean permissiveConstruction) {
        permissiveConstruction = permissiveConstruction;
    }
    

    /* *************************************************************************
     * Following static methods check for certain aggregate types, based on 
     * (above) declared types.  Note that these aggregate types do not
     * necessarily map directly to the sub-classes of FilterDefault.  In most,
     * but not all, cases, a single class implements an aggregate type.
     * However, there are aggregate types that are implemented by multiple
     * classes (ie. the Math type is implemented by two seperate classes).
     * ************************************************************************/
    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a logic filter type.
     */
    protected static boolean isLogicFilter(short filterType) {
        if( (filterType == LOGIC_OR) ||
            (filterType == LOGIC_AND) ||
            (filterType == LOGIC_NOT) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is math.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a math filter type.
     */
    protected static boolean isMathFilter(short filterType) {

        if( (filterType == COMPARE_LESS_THAN) ||
            (filterType == COMPARE_GREATER_THAN) ||
            (filterType == COMPARE_LESS_THAN_EQUAL) ||
            (filterType == COMPARE_GREATER_THAN_EQUAL) ||
            (filterType == BETWEEN) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is compare.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a compare filter type.
     */
    protected static boolean isCompareFilter(short filterType) {

        if( (isMathFilter(filterType)) ||
            (filterType == COMPARE_EQUALS) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a geometry filter type.
     */
    protected static boolean isGeometryFilter(short filterType) {

        if( (filterType == GEOMETRY_BBOX) ||
            (filterType == GEOMETRY_EQUALS) ||
            (filterType == GEOMETRY_DISJOINT) ||
            (filterType == GEOMETRY_TOUCHES) ||
            (filterType == GEOMETRY_INTERSECTS) ||
            (filterType == GEOMETRY_CROSSES) ||
            (filterType == GEOMETRY_WITHIN) ||
            (filterType == GEOMETRY_CONTAINS) ||
            (filterType == GEOMETRY_OVERLAPS) ||
            (filterType == GEOMETRY_BEYOND) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a logic filter type.
     */
    protected static boolean isSimpleFilter(short filterType) {

        if( isCompareFilter(filterType) ||
            isGeometryFilter(filterType) ||
            (filterType == NULL) ||
            (filterType == LIKE) ) {
            return true;
        }
        else {
            return false;
        }
    }

    
}
