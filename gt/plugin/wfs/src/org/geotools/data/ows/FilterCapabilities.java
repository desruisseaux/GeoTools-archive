
package org.geotools.data.ows;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class FilterCapabilities {

    // spatial
    public static final int NO_OP = 0;
    public static final int BBOX = 1;
    public static final int EQUALS = 2;
    public static final int DISJOINT = 4;
    public static final int INTERSECT = 8;
    public static final int TOUCHES = 16;
    public static final int CROSSES = 32;
    public static final int WITHIN = 64;
    public static final int CONTAINS = 128;
    public static final int OVERLAPS = 256;
    public static final int BEYOND = 512;
    public static final int DWITHIN = 1024;
    
    //scalar
    public static final int LOGICAL = 1;
    public static final int SIMPLE_COMPARISONS = 2;
    public static final int LIKE = 4;
    public static final int BETWEEN = 8;
    public static final int NULL_CHECK = 16;
    public static final int SIMPLE_ARITHMETIC = 32;
    public static final int FUNCTIONS = 64;

    private static Map smap = {
            smap = new HashMap();
            smap.put("",new Integer(NO_OP));
            smap.put("BBOX",new Integer(BBOX));
            smap.put("Equals",new Integer(EQUALS));
            smap.put("Disjoint",new Integer(DISJOINT));
            smap.put("Intersect",new Integer(INTERSECT));
            smap.put("Touches",new Integer(TOUCHES));
            smap.put("Crosses",new Integer(CROSSES));
            smap.put("Within",new Integer(WITHIN));
            smap.put("Contains",new Integer(CONTAINS));
            smap.put("Overlaps",new Integer(OVERLAPS));
            smap.put("Beyond",new Integer(BEYOND));
            smap.put("DWithin",new Integer(DWITHIN));
    };
    private static Map cmap = {
            cmap = new HashMap();
            cmap.put("",new Integer(NO_OP));
            cmap.put("Logical",new Integer(LOGICAL));
            cmap.put("Simple_Comparisons",new Integer(SIMPLE_COMPARISONS));
            cmap.put("Like",new Integer(LIKE));
            cmap.put("Between",new Integer(BETWEEN));
            cmap.put("NullCheck",new Integer(NULL_CHECK));
            cmap.put("Simple_Arithmetic",new Integer(SIMPLE_ARITHMETIC));
            cmap.put("Functions",new Integer(FUNCTIONS));
    };
    public static int findOperation(String s){
        if(smap.containsKey(s))
            return ((Integer)smap.get(s)).intValue();
        if(cmap.containsKey(s))
            return ((Integer)cmap.get(s)).intValue();
        return NO_OP;
    }
    public static String writeSpatialOperation(int i){
        switch(i){
        case BBOX: return "BBOX";
        case EQUALS: return "Equals";
        case DISJOINT: return "Disjoint";
        case INTERSECT: return "Intersect";
        case TOUCHES: return "Touches";
        case CROSSES: return "Crosses";
        case WITHIN: return "Within";
        case CONTAINS: return "Contains";
        case OVERLAPS: return "Overlaps";
        case BEYOND: return "Beyond";
        case DWITHIN: return "DWithin";
        }
        return "";
    }
    public static String writeScalarOperation(int i){
        switch(i){
        case LOGICAL: return "Logical";
        case SIMPLE_COMPARISONS: return "Simple_Comparisons";
        case LIKE: return "Like";
        case BETWEEN: return "Between";
        case NULL_CHECK: return "NullCheck";
        case SIMPLE_ARITHMETIC: return "Simple_Arithmetic";
        case FUNCTIONS: return "Functions";
        }
        return "";
    }
    
    private int spatial_ops = NO_OP;
    private int scalar_ops = NO_OP;

    /**
     * @return Returns the scalar_ops.
     */
    public int getScalarOps() {
        return scalar_ops;
    }
    /**
     * @param scalar_ops The scalar_ops to set.
     */
    public void setScalarOps(int scalar_ops) {
        this.scalar_ops = scalar_ops;
    }
    /**
     * @return Returns the spatial_ops.
     */
    public int getSpatialOps() {
        return spatial_ops;
    }
    /**
     * @param spatial_ops The spatial_ops to set.
     */
    public void setSpatialOps(int spatial_ops) {
        this.spatial_ops = spatial_ops;
    }
}
