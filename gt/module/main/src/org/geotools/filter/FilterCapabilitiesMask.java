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
import java.util.Map;


/**
 * <p>
 * This class should be merged with FilterCapabilities at some point
 * </p>
 *
 * @author dzwiers
 * @source $URL$
 */
public class FilterCapabilitiesMask extends FilterCapabilities{
	/**
	 * Mask for no operation
	 */
    public static final int NO_OP = 0;

    // spatial masks
	/**
	 * Spatial Mask for bbox operation
	 */
    public static final int BBOX = 1;
	/**
	 * Spatial Mask for equals operation
	 */
    public static final int EQUALS = 2;
	/**
	 * Spatial Mask for disjoint operation
	 */
    public static final int DISJOINT = 4;
	/**
	 * Spatial Mask for intersect operation
	 */
    public static final int INTERSECT = 8;
	/**
	 * Spatial Mask for touches operation
	 */
    public static final int TOUCHES = 16;
	/**
	 * Spatial Mask for crosses operation
	 */
    public static final int CROSSES = 32;
	/**
	 * Spatial Mask for within operation
	 */
    public static final int WITHIN = 64;
	/**
	 * Spatial Mask for contains operation
	 */
    public static final int CONTAINS = 128;
	/**
	 * Spatial Mask for overlaps operation
	 */
    public static final int OVERLAPS = 256;
	/**
	 * Spatial Mask for beyond operation
	 */
    public static final int BEYOND = 512;
	/**
	 * Spatial Mask for dwithin operation
	 */
    public static final int DWITHIN = 1024;

    //scalar masks
	/**
	 * Scalar Mask for logical operation
	 */
    public static final int LOGICAL = 2048;
	/**
	 * Scalar Mask for simple comparison operations
	 */
    public static final int SIMPLE_COMPARISONS = 4096;
	/**
	 * Scalar Mask for like operation
	 */
    public static final int LIKE = 8192;
	/**
	 * Scalar Mask for between operation
	 */
    public static final int BETWEEN = 16384;
	/**
	 * Scalar Mask for null check operation
	 */
    public static final int NULL_CHECK = 32768;
	/**
	 * Scalar Mask for simple arithmetic operations
	 */
    public static final int SIMPLE_ARITHMETIC = 65536;
	/**
	 * Scalar Mask for function operations
	 */
    public static final int FUNCTIONS = 131072;
    
    private static Map smap = loadSMap();
    private static Map cmap = loadCMap();
    private int ops = NO_OP;

    private static Map loadSMap() {
        smap = new HashMap();
        smap.put("", new Integer(NO_OP));
        smap.put("BBOX", new Integer(BBOX));
        smap.put("Equals", new Integer(EQUALS));
        smap.put("Disjoint", new Integer(DISJOINT));
        smap.put("Intersect", new Integer(INTERSECT));
        smap.put("Touches", new Integer(TOUCHES));
        smap.put("Crosses", new Integer(CROSSES));
        smap.put("Within", new Integer(WITHIN));
        smap.put("Contains", new Integer(CONTAINS));
        smap.put("Overlaps", new Integer(OVERLAPS));
        smap.put("Beyond", new Integer(BEYOND));
        smap.put("DWithin", new Integer(DWITHIN));

        return smap;
    }

    private static Map loadCMap() {
        cmap = new HashMap();
        cmap.put("", new Integer(NO_OP));
        cmap.put("Logical", new Integer(LOGICAL));
        cmap.put("Simple_Comparisons", new Integer(SIMPLE_COMPARISONS));
        cmap.put("Like", new Integer(LIKE));
        cmap.put("Between", new Integer(BETWEEN));
        cmap.put("NullCheck", new Integer(NULL_CHECK));
        cmap.put("Simple_Arithmetic", new Integer(SIMPLE_ARITHMETIC));
        cmap.put("Functions", new Integer(FUNCTIONS));

        return cmap;
    }

    /**
     * Translates a String into an int mask for the operation
     * 
     * @param s String, operation name
     * @return one of the filter constants
     */
    public static int findOperation(String s) {
        if (smap.containsKey(s)) {
            return ((Integer) smap.get(s)).intValue();
        }

        if (cmap.containsKey(s)) {
            return ((Integer) cmap.get(s)).intValue();
        }

        return NO_OP;
    }

    /**
     * Converts a singular mask to the appropriate string as a Spatial Op
     * 
     * @param i The int constant
     * @return The String representation of the int as a FilterType
     */
    public static String writeSpatialOperation(int i) {
        switch (i) {
        case BBOX:
            return "BBOX";

        case EQUALS:
            return "Equals";

        case DISJOINT:
            return "Disjoint";

        case INTERSECT:
            return "Intersect";

        case TOUCHES:
            return "Touches";

        case CROSSES:
            return "Crosses";

        case WITHIN:
            return "Within";

        case CONTAINS:
            return "Contains";

        case OVERLAPS:
            return "Overlaps";

        case BEYOND:
            return "Beyond";

        case DWITHIN:
            return "DWithin";
        }

        return "";
    }

    /**
     * Converts a singular mask to the appropriate string as a Scalar Op
     * 
     * @param i The int constant
     * @return The String representation of the int as a FilterType
     */
    public static String writeScalarOperation(int i) {
        switch (i) {
        case LOGICAL:
            return "Logical";

        case SIMPLE_COMPARISONS:
            return "Simple_Comparisons";

        case LIKE:
            return "Like";

        case BETWEEN:
            return "Between";

        case NULL_CHECK:
            return "NullCheck";

        case SIMPLE_ARITHMETIC:
            return "Simple_Arithmetic";

        case FUNCTIONS:
            return "Functions";
        }

        return "";
    }
    
    
    /*
     * @see org.geotools.filter.FilterCapabilities#addType(short)
     */
    public void addType( short type ) {
        ops = ops | type;
    }
    /*
     * @see org.geotools.filter.FilterCapabilities#fullySupports(org.geotools.filter.Filter)
     */
    public boolean fullySupports( Filter filter ) {
        return super.fullySupports(filter);
    }
    /*
     * @see org.geotools.filter.FilterCapabilities#supports(org.geotools.filter.Filter)
     */
    public boolean supports( Filter filter ) {
        return super.supports(filter);
    }
    /*
     * @see org.geotools.filter.FilterCapabilities#supports(short)
     */
    public boolean supports( short type ) {
        return (ops & type) == type;
    }
    
// OLD METHODS
//    
//    /**
//     * DOCUMENT ME!
//     *
//     * @return Returns the scalar_ops.
//     */
    public int getScalarOps() {
        return ops;
    }
//
//    /**
//     * DOCUMENT ME!
//     *
//     * @param scalar_ops The scalar_ops to set.
//     */
//    public void setScalarOps(int scalar_ops) {
//        this.scalar_ops = scalar_ops;
//    }
//
//    /**
//     * DOCUMENT ME!
//     *
//     * @return Returns the spatial_ops.
//     */
    public int getSpatialOps() {
        return ops;
    }
//
//    /**
//     * DOCUMENT ME!
//     *
//     * @param spatial_ops The spatial_ops to set.
//     */
//    public void setSpatialOps(int spatial_ops) {
//        this.spatial_ops = spatial_ops;
//    }
}
