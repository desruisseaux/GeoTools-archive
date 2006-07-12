/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Represents the Filter capabilities that are supported by a SQLEncoder.  Each
 * SQLEncoder class should have one static FilterCapabilities, representing
 * the filter encoding operations that it can successfully perform.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
public class FilterCapabilities {
	/**
	 * Mask for no operation
	 */
    public static final long NO_OP = 0;

    // spatial masks
	/**
	 * Spatial Mask for bbox operation
	 */
    public static final long SPATIAL_BBOX = 0x01;
	/**
	 * Spatial Mask for equals operation
	 */
    public static final long SPATIAL_EQUALS = 0x01<<1;
	/**
	 * Spatial Mask for disjoint operation
	 */
    public static final long SPATIAL_DISJOINT = 0x01<<2;
	/**
	 * Spatial Mask for intersect operation
	 */
    public static final long SPATIAL_INTERSECT = 0x01<<3;
	/**
	 * Spatial Mask for touches operation
	 */
    public static final long SPATIAL_TOUCHES = 0x01<<4;
	/**
	 * Spatial Mask for crosses operation
	 */
    public static final long SPATIAL_CROSSES = 0x01<<5;
	/**
	 * Spatial Mask for within operation
	 */
    public static final long SPATIAL_WITHIN = 0x01<<6;
	/**
	 * Spatial Mask for contains operation
	 */
    public static final long SPATIAL_CONTAINS = 0x01<<7;
	/**
	 * Spatial Mask for overlaps operation
	 */
    public static final long SPATIAL_OVERLAPS = 0x01<<8;
	/**
	 * Spatial Mask for beyond operation
	 */
    public static final long SPATIAL_BEYOND = 0x01<<9;
	/**
	 * Spatial Mask for dwithin operation
	 */
    public static final long SPATIAL_DWITHIN = 0x01<<10;

    //scalar masks
	/**
	 * Scalar Mask for like operation
	 */
    public static final long LIKE = 0x01<<11;
	/**
	 * Scalar Mask for between opelongion
	 */
    public static final long BETWEEN = 0x01<<12;
	/**
	 * Scalar Mask for null check operation
	 */
    public static final long NULL_CHECK = 0x01<<13;
	/**
	 * Scalar Mask for simple arithmetic operations
	 */
    public static final long SIMPLE_ARITHMETIC = 0x01<<14;
	/**
	 * Scalar Mask for function operations
	 */
    public static final long FUNCTIONS = 0x01<<15;

    // masks for different comparison filters
	public static final long COMPARE_EQUALS = 0x01<<16;

	public static final long COMPARE_GREATER_THAN = 0x01<<17;
	
	public static final long COMPARE_GREATER_THAN_EQUAL = 0x01<<18;

	public static final long COMPARE_LESS_THAN = 0x01<<19;

	public static final long COMPARE_LESS_THAN_EQUAL = 0x01<<20;

	public static final long COMPARE_NOT_EQUALS = 0x01<<21;

	public static final long FID = 0x01<<22;

	// masks for different logic filters
	public static final long LOGIC_AND = 0x01<<23;

	public static final long LOGIC_NOT = 0x01<<24;

	public static final long LOGIC_OR = 0x01<<25;

	/**
	 * Scalar Mask for logical operation
	 */
    public static final long LOGICAL = (LOGIC_AND|LOGIC_OR|LOGIC_NOT);
	/**
	 * Scalar Mask for simple comparison operations
	 */
    public static final long SIMPLE_COMPARISONS = COMPARE_EQUALS|COMPARE_GREATER_THAN|COMPARE_GREATER_THAN_EQUAL|COMPARE_LESS_THAN|COMPARE_LESS_THAN_EQUAL|COMPARE_NOT_EQUALS;    
    private static Map spatialFiltersMap = loadSpatialFiltersMap();
    private static Map comparisonsMap = loadComparisonFilterMap();
    private static Map filterTypeToFilterCapabilitiesMap = loadFilterTypeToFilterCapabilitiesMap();
    private long ops = NO_OP;

    private static Map loadSpatialFiltersMap() {
        spatialFiltersMap = new HashMap();
        spatialFiltersMap.put("", new Long(NO_OP));
        spatialFiltersMap.put("BBOX", new Long(SPATIAL_BBOX));
        spatialFiltersMap.put("Equals", new Long(SPATIAL_EQUALS));
        spatialFiltersMap.put("Disjoint", new Long(SPATIAL_DISJOINT));
        spatialFiltersMap.put("Intersect", new Long(SPATIAL_INTERSECT));
        spatialFiltersMap.put("Touches", new Long(SPATIAL_TOUCHES));
        spatialFiltersMap.put("Crosses", new Long(SPATIAL_CROSSES));
        spatialFiltersMap.put("Within", new Long(SPATIAL_WITHIN));
        spatialFiltersMap.put("Contains", new Long(SPATIAL_CONTAINS));
        spatialFiltersMap.put("Overlaps", new Long(SPATIAL_OVERLAPS));
        spatialFiltersMap.put("Beyond", new Long(SPATIAL_BEYOND));
        spatialFiltersMap.put("DWithin", new Long(SPATIAL_DWITHIN));

        return spatialFiltersMap;
    }

    private static Map loadComparisonFilterMap() {
        comparisonsMap = new HashMap();
        comparisonsMap.put("", new Long(NO_OP));
        comparisonsMap.put("Logical", new Long(LOGICAL));
        comparisonsMap.put("Simple_Comparisons", new Long(SIMPLE_COMPARISONS));
        comparisonsMap.put("Like", new Long(LIKE));
        comparisonsMap.put("Between", new Long(BETWEEN));
        comparisonsMap.put("NullCheck", new Long(NULL_CHECK));
        comparisonsMap.put("Simple_Arithmetic", new Long(SIMPLE_ARITHMETIC));
        comparisonsMap.put("Functions", new Long(FUNCTIONS));

        return comparisonsMap;
    }
    
    private static Map loadFilterTypeToFilterCapabilitiesMap(){
    	Map conversionMap=new HashMap();
    	conversionMap.put(new Short(FilterType.BETWEEN), new Long(BETWEEN));
    	conversionMap.put(new Short(FilterType.COMPARE_EQUALS), new Long(COMPARE_EQUALS));
    	conversionMap.put(new Short(FilterType.COMPARE_GREATER_THAN), new Long(COMPARE_GREATER_THAN));
    	conversionMap.put(new Short(FilterType.COMPARE_GREATER_THAN_EQUAL), new Long(COMPARE_GREATER_THAN_EQUAL));
    	conversionMap.put(new Short(FilterType.COMPARE_LESS_THAN), new Long(COMPARE_LESS_THAN));
    	conversionMap.put(new Short(FilterType.COMPARE_LESS_THAN_EQUAL), new Long(COMPARE_LESS_THAN_EQUAL));
    	conversionMap.put(new Short(FilterType.COMPARE_NOT_EQUALS), new Long(COMPARE_NOT_EQUALS));
    	conversionMap.put(new Short(FilterType.FID), new Long(FID));
    	conversionMap.put(new Short(FilterType.GEOMETRY_BBOX), new Long(SPATIAL_BBOX));
    	conversionMap.put(new Short(FilterType.GEOMETRY_BEYOND), new Long(SPATIAL_BEYOND));
    	conversionMap.put(new Short(FilterType.GEOMETRY_CONTAINS), new Long(SPATIAL_CONTAINS));
    	conversionMap.put(new Short(FilterType.GEOMETRY_CROSSES), new Long(SPATIAL_CROSSES));
    	conversionMap.put(new Short(FilterType.GEOMETRY_DISJOINT), new Long(SPATIAL_DISJOINT));
    	conversionMap.put(new Short(FilterType.GEOMETRY_DWITHIN), new Long(SPATIAL_DWITHIN));
    	conversionMap.put(new Short(FilterType.GEOMETRY_EQUALS), new Long(SPATIAL_EQUALS));
    	conversionMap.put(new Short(FilterType.GEOMETRY_INTERSECTS), new Long(SPATIAL_INTERSECT));
    	conversionMap.put(new Short(FilterType.GEOMETRY_OVERLAPS), new Long(SPATIAL_OVERLAPS));
    	conversionMap.put(new Short(FilterType.GEOMETRY_TOUCHES), new Long(SPATIAL_TOUCHES));
    	conversionMap.put(new Short(FilterType.GEOMETRY_WITHIN), new Long(SPATIAL_WITHIN));
    	conversionMap.put(new Short(FilterType.LIKE), new Long(LIKE));
    	conversionMap.put(new Short(FilterType.LOGIC_AND), new Long(LOGIC_AND));
    	conversionMap.put(new Short(FilterType.LOGIC_NOT), new Long(LOGIC_NOT));
    	conversionMap.put(new Short(FilterType.LOGIC_OR), new Long(LOGIC_OR));
    	conversionMap.put(new Short(FilterType.NULL), new Long(NULL_CHECK));
    	return conversionMap;
    }

    /**
     * Translates a String into an long mask for the operation
     * 
     * @param s String, operation name
     * @return one of the filter constants
     */
    public static long findOperation(String s) {
        if (spatialFiltersMap.containsKey(s)) {
            return ((Long) spatialFiltersMap.get(s)).intValue();
        }

        if (comparisonsMap.containsKey(s)) {
            return ((Long) comparisonsMap.get(s)).intValue();
        }

        return NO_OP;
    }

    /**
     * Converts a singular mask to the appropriate string as a Spatial Op
     * 
     * @param i The long constant
     * @return The String representation of the long as a FilterType
     */
    public static String writeSpatialOperation(long i) {
        if ( i == SPATIAL_BBOX )
            return "BBOX";

        if ( i == SPATIAL_EQUALS )
            return "Equals";

        if ( i == SPATIAL_DISJOINT )
            return "Disjoint";

        if ( i == SPATIAL_INTERSECT )
            return "Intersect";

        if ( i == SPATIAL_TOUCHES )
            return "Touches";

        if ( i == SPATIAL_CROSSES )
            return "Crosses";

        if ( i == SPATIAL_WITHIN )
            return "Within";

        if ( i == SPATIAL_CONTAINS )
            return "Contains";

        if ( i == SPATIAL_OVERLAPS )
            return "Overlaps";

        if ( i == SPATIAL_BEYOND )
            return "Beyond";

        if ( i == SPATIAL_DWITHIN )
            return "DWithin";

        return "";
    }

    /**
     * Converts a singular mask to the appropriate string as a Scalar Op
     * 
     * @param i The long constant
     * @return The String representation of the long as a FilterType
     */
    public static String writeScalarOperation(long i) {
        if( i == LOGICAL) 
            return "Logical";

        if( i == SIMPLE_COMPARISONS)
            return "Simple_Comparisons";

        if( i ==LIKE )
            return "Like";

        if( i == BETWEEN )
            return "Between";

        if( i == NULL_CHECK )
            return "NullCheck";

        if( i == SIMPLE_ARITHMETIC )
            return "Simple_Arithmetic";

        if( i == FUNCTIONS )
            return "Functions";

        if( i == FID )
            return "FeatureID";
        
        if( i == COMPARE_EQUALS )
            return "Compare_Equals";
        
        if( i == COMPARE_GREATER_THAN )
            return "Compare_Greater_Than";
        
        if( i == COMPARE_GREATER_THAN_EQUAL )
            return "Compare_Greater_Than_Equal";
        
        if( i == COMPARE_LESS_THAN )
            return "Compare_Less_Than";
        
        if( i == COMPARE_LESS_THAN_EQUAL )
            return "Compare_Less_Than_Equal";
        
        if( i == COMPARE_NOT_EQUALS )
            return "Compare_Not_Equals";
        
        return "";
    }
    
    
    /**
     * Adds a new support type to capabilities.
     *
     * @param type The one of the masks enumerated in this class
     */
    public void addType( long type ) {
        ops = ops | type;
    }    
    
    /**
     * Adds a new support type to capabilities.
     *
     * @param type The {@link FilterType} type that is supported
     * @deprecated
     * @see #convertFilterTypeToMask(short)
     * @see #addType(long)
     */
    public void addType( short type ) {
        addType(convertFilterTypeToMask(type));
    }

    /**
     * Returns the mask that is equivalent to the FilterType constant.
     * 
     * @param type a constant from {@link FilterType}
     * @return the mask that is equivalent to the FilterType constant.
     */
	public long convertFilterTypeToMask(short type) {
		Long capabilitiesType = (Long)filterTypeToFilterCapabilitiesMap.get(new Short(type));
		if( capabilitiesType==null )
			return NO_OP;
		return (capabilitiesType).intValue();
	}
    
 
    /**
     * Determines if the filter passed in is supported.
     *
     * @param filter The Filter to be tested.
     *
     * @return true if supported, false otherwise.
     */
    public boolean supports(Filter filter) {
        short filterType = filter.getFilterType();

        return supports(filterType);
    }

    /**
     * Determines if the filter and all its sub filters are supported.  Is most
     * important for logic filters, as they are the only ones with subFilters.
     * Null filters should not be used here, if nothing should be filtered
     * than Filter.NONE can be used.  Embedded nulls can be a particular
     * source of problems, buried in logic filters.
     *
     * @param filter the filter to be tested.
     *
     * @return true if all sub filters are supported, false otherwise.
     *
     * @throws IllegalArgumentException If a null filter is passed in.  As this
     *         function is recursive a null in a logic filter will also cause
     *         an error.
     */
    public boolean fullySupports(Filter filter) {
        boolean supports = true;

        if (filter == null) {
            throw new IllegalArgumentException("Null filters can not be "
                + "unpacked, did you mean " + "Filter.NONE?");
        }

        short filterType = filter.getFilterType();

        if (AbstractFilter.isLogicFilter(filterType)) {
            Iterator filters = ((LogicFilter) filter).getFilterIterator();
            Filter testFilter = null;

            //short testFtype = 0;
            while (filters.hasNext()) {
                testFilter = (Filter) filters.next();

                if (!(this.fullySupports(testFilter))) {
                    supports = false;
                }
            }
        } else {
            supports = this.supports(filter);
        }

        return supports;
    }
    

    /**
     * Determines if the filter type passed in is supported.
     *
     * @param type The AbstractFilter type to be tested
     *
     * @return true if supported, false otherwise.
     * @deprecated
     */
    public boolean supports( short type ) {
        return supports(convertFilterTypeToMask(type));
    }
    
    public boolean supports(long type) {
    	return (ops & type) == type;
	}
    
    public long getScalarOps() {
        return ops & (SIMPLE_ARITHMETIC|SIMPLE_COMPARISONS|FID|FUNCTIONS|LIKE|LOGICAL|NULL_CHECK|BETWEEN);
    }
    public long getSpatialOps() {
        return ops & (SPATIAL_BBOX|SPATIAL_BEYOND|SPATIAL_CONTAINS|SPATIAL_CROSSES
        		|SPATIAL_DISJOINT|SPATIAL_DWITHIN|SPATIAL_EQUALS|SPATIAL_INTERSECT
        		|SPATIAL_OVERLAPS|SPATIAL_TOUCHES|SPATIAL_WITHIN);
    }

}
