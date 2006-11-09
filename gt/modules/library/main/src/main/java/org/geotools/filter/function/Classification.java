package org.geotools.filter.function;

/**
 * Proposed data struction for classifications.
 * 
 * Suggestions:
 * - follow bean naming conventions for xpath
 * 
 * @author jgarnett
 *
 */
class Classification {
    private int slots;

    public Classification( int slots ){
        this.slots = slots;
    }
    
    int getSlotCount(){
        return slots; 
    }
    public String getTilte( int slot ){
        return "Group "+(slot+1);
    }    
    public static class Range extends Classification {
        Object min[];
        Object max[];
        public Range( Object min[], Object max[] ){
            super( min.length );
            this.min = min;
            this.max = max;
        }
        Object getMin( int slot ){
            return min[slot];
        }
        Object getMax( int slot ){
            return max[slot];
        }    
        public String getTitle( int slot ){
            return "Range "+min[slot]+".."+max[slot];
        }
    }
}
