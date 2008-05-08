package org.geotools.filter.function;


/**
 * Classifies into ranges of minimum and maximum values.
 * 
 * @author Cory Horner, Refractions Research
 *
 */
public final class RangedClassifier extends Classifier {

    Comparable min[];
    Comparable max[];

    public RangedClassifier(Comparable min[], Comparable max[]) {
        this.min = min;
        this.max = max;
        //initialize titles
        this.titles = new String[min.length];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = truncateZeros(min[i].toString()) + ".." + truncateZeros(max[i].toString());
        }
    }
    
    private String truncateZeros(String str) {
        if (str.indexOf(".") > -1) {
            while(str.endsWith("0")) {
                str = str.substring(0, str.length() - 1);
            }
            if (str.endsWith(".")) {
                str = str.substring(0, str.length() - 1);
            }
        }
        return str;
    }
    
    public int getSize() {
        return Math.min(min.length, max.length);
    }
    
    public Object getMin(int slot) {
        return min[slot];
    }
    
    public Object getMax(int slot) {
        return max[slot];
    }
    
    public int classify(Object value) {
        return classify((Comparable) value); 
    }
    
    private int classify(Comparable val) {
        Comparable value = val;
        if (val instanceof Integer) { //convert to double as java is stupid
            value = new Double(((Integer) val).intValue());
        }
        //check each slot and see if: min <= value < max
        int last = min.length - 1;
        for (int i = 0; i <= last; i++) {
            Comparable localMin = (Comparable) this.min[i];
            Comparable localMax = (Comparable) this.max[i];
            
            if (localMin.compareTo(value) < 1 && localMax.compareTo(value) > 0) {
                return i;
            }
        }
        if (max[last].compareTo(value) == 0) { //if value = max, put it in the last slot
            return last;
        }
        return -1;
    }
    
}