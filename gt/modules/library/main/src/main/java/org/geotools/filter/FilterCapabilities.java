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
package org.geotools.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opengis.filter.And;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;

/**
 * Represents the Filter capabilities that are supported by a SQLEncoder.  Each
 * SQLEncoder class should have one static FilterCapabilities, representing
 * the filter encoding operations that it can successfully perform.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * 
 * @deprecated use {@link org.opengis.filter.capability.FilterCapabilities}.
 */
public class FilterCapabilities {
	/**
	 * Mask for no operation
	 */
    public static final long NO_OP = 0;

    /**
     * Mask for Filter.INCLUDE
     */
    public static final long NONE = 0x01<<30;

    /**
     * Mask for Filter.EXCLUDE
     */
    public static final long ALL = 0x01<<31;
    
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
    
    public static final FilterCapabilities SIMPLE_COMPARISONS_OPENGIS;
    
    public static final FilterCapabilities LOGICAL_OPENGIS;
    
    static {
        SIMPLE_COMPARISONS_OPENGIS = new FilterCapabilities();
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsEqualTo.class);
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsGreaterThan.class);
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsGreaterThanOrEqualTo.class);
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsLessThanOrEqualTo.class);
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsLessThan.class);
        SIMPLE_COMPARISONS_OPENGIS.addType(PropertyIsNotEqualTo.class);
        
        LOGICAL_OPENGIS = new FilterCapabilities();
        LOGICAL_OPENGIS.addType(And.class);
        LOGICAL_OPENGIS.addType(Not.class);
        LOGICAL_OPENGIS.addType(Or.class);
    }
    
    private long ops = NO_OP;

	private Set functions=new HashSet();

    public FilterCapabilities(long filterCapabilitiesType) {
		addType(filterCapabilitiesType);
	}

	public FilterCapabilities() {
		this(NO_OP);
	}

    public FilterCapabilities(Class type) {
    	addType(type); 
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
     * Adds a new support type to capabilities.  For 2.2 only function expression support is added this way.
     * As of geotools 2.3 this will be the supported way of adding to Filtercapabilities.
     * 
     * @param type the Class that indicates the new support.
     */
    public void addType( Class type ){
    	if( org.opengis.filter.Filter.class.isAssignableFrom(type)
                ||
            org.opengis.filter.expression.Expression.class.isAssignableFrom(type)){
			addType(FUNCTIONS);
    		functions.add(type);
    	}
    }
    
    /**
     * Add all the capabilities in the provided FilterCapabilities to this capabilities.
     * 
     * @param capabilities capabilities to add.
     */
    public void addAll( FilterCapabilities capabilities){
    	ops= capabilities.ops|ops;
    	functions.addAll(capabilities.functions);
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
        addAll(convertFilterTypeToMask(type));
    }

    /**
     * Returns the mask that is equivalent to the FilterType constant.
     * 
     * @param type a constant from {@link FilterType}
     * @return the mask that is equivalent to the FilterType constant.
     */
	public FilterCapabilities convertFilterTypeToMask(short type) {
		if( type==FilterType.ALL )
			return FilterNameTypeMapping.NO_OP_CAPS;
		if( type==FilterType.NONE )
			return FilterNameTypeMapping.ALL_CAPS;
		Object object = FilterNameTypeMapping.filterTypeToFilterCapabilitiesMap.get(new Short(type));
		return (FilterCapabilities)object;
		
	}
    
 
    /**
     * Determines if the filter passed in is supported.
     *
     * @param filter The Filter to be tested.
     *
     * @return true if supported, false otherwise.
     */
    public boolean supports(org.opengis.filter.Filter filter) {
        for (Iterator ifunc = functions.iterator(); ifunc.hasNext();) {
            if (((Class)ifunc.next()).isAssignableFrom(filter.getClass()))
                return true;
        }
        
        if (functions.contains(filter.getClass()))
            return true;
        
        short filterType = Filters.getFilterType( filter );

        return supports(filterType);
    }

    /**
     * Determines if the filter and all its sub filters are supported.  Is most
     * important for logic filters, as they are the only ones with subFilters.
     * Null filters should not be used here, if nothing should be filtered
     * than Filter.INCLUDE can be used.  Embedded nulls can be a particular
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
    public boolean fullySupports(org.opengis.filter.Filter filter) {
        boolean supports = true;

        if (filter == null) {
            throw new IllegalArgumentException("Null filters can not be "
                + "unpacked, did you mean " + "Filter.INCLUDE?");
        }

        short filterType = Filters.getFilterType(filter);

        if (AbstractFilter.isLogicFilter(filterType)) {
            Iterator filters = ((LogicFilter) filter).getFilterIterator();
            org.opengis.filter.Filter testFilter = null;

            //short testFtype = 0;
            while (filters.hasNext()) {
                testFilter = (org.opengis.filter.Filter) filters.next();

                if (!(this.fullySupports(testFilter))) {
                    supports = false;
                    break;
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
    
    public boolean supports(FilterCapabilities type) {
    	return (ops & type.ops) == type.ops && functions.containsAll(type.functions);
	}
    
    public boolean supports(Class type){
    	return functions.contains(type);
    }
    
    public long getScalarOps() {
        return ops & (SIMPLE_ARITHMETIC|SIMPLE_COMPARISONS|FID|FUNCTIONS|LIKE|LOGICAL|NULL_CHECK|BETWEEN);
    }
    public long getSpatialOps() {
        return ops & (SPATIAL_BBOX|SPATIAL_BEYOND|SPATIAL_CONTAINS|SPATIAL_CROSSES
        		|SPATIAL_DISJOINT|SPATIAL_DWITHIN|SPATIAL_EQUALS|SPATIAL_INTERSECT
        		|SPATIAL_OVERLAPS|SPATIAL_TOUCHES|SPATIAL_WITHIN);
    }
    /**
     * Translates a String into an object that represents the operation
     * 
     * @param name String, operation name
     * 
     * @return one of the {@link FilterCapabilities} constants
     */
	public static FilterCapabilities findOperation(String name) {
		return FilterNameTypeMapping.findOperation(name);
	}
    /**
     * Translates a String into  an object that represents function expression
     * 
     * @param name String, expression name
     * 
     * @return one of the {@link FilterCapabilities} constants
     */
	public static FilterCapabilities findFunction(String name) {
		return FilterNameTypeMapping.findFunction(name);
	}

}
