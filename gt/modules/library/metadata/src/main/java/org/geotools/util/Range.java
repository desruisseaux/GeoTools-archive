/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008 GeoTools Project Managment Committee (PMC)
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
package org.geotools.util;

import java.io.Serializable;


/**
 * A range between a minimum and maximum comparable, the minimum/maximum may be
 * included or excluded or unbounded.
 * <p>
 * This class is a method compatible replacement for the javax.media.jai.util
 * Range class.
 * 
 * @author Jody Garnett
 */
public class Range<T extends Comparable<? super T>> implements Serializable  {
    private static final long serialVersionUID = -5393896130562660517L;

    private Class<T> elementClass;
    private T minValue;
    boolean isMinIncluded;
    private T maxValue;
    boolean isMaxIncluded;

    public Range(Class<T> elementClass ){
        this.elementClass = elementClass;
        this.minValue = null;
        this.isMinIncluded = false;
        this.maxValue = null;
        this.isMaxIncluded = false;
    }
    public Range(Class<T> elementClass, T value) {
        this(elementClass, value, true, value, true);
    }

    public Range(Class<T> elementClass, T minValue, T maxValue) {
        this(elementClass, minValue, true, maxValue, true);
    }

    public Range(Class<T> elementClass, T minValue,
            boolean isMinIncluded, T maxValue, boolean isMaxIncluded) {
        this.elementClass = elementClass;
        this.minValue = minValue;
        this.isMinIncluded = isMinIncluded || minValue == null;
        this.maxValue = maxValue;
        this.isMaxIncluded = isMaxIncluded || maxValue == null;
    }

    /**
     * Type of elements in this range.
     * 
     * @return Type of elements in this range, getElementClass will extend
     *         Comparable
     */
    public Class<T> getElementClass() {
        return elementClass;
    }

    /**
     * Minimal value, please refer to isMinIncluded to check if this value is
     * inclusive.
     * 
     * @return minimal value or null if unbounded, if isMinIncluded is true the
     *         value is considered included in the set
     */
    public T getMinValue() {
        return minValue;
    }

    /**
     * Indicates if getMinValue is included in the Range.
     * 
     * @return true if getMinValue is included in the Range
     */
    public boolean isMinIncluded() {
        return isMinIncluded;
    }

    /**
     * Indicates if getMinValue is unbounded.
     * 
     * @return true if getMinValue is null
     */
    public boolean isMinUnbounded() {
        return minValue == null;
    }

    /**
     * Maximal value, please refer to isMaxIncluded to check if this value is
     * inclusive.
     * 
     * @return maximal value or null if unbounded, if isMaxIncluded is true the
     *         value is considered included in the set
     */
    public T getMaxValue() {
        return maxValue;
    }

    /**
     * Indicates if getMaxValue is included in the Range.
     * 
     * @return true if getMaxValue is included in the Range
     */
    public boolean isMaxIncluded() {
        return isMaxIncluded;
    }

    /**
     * Indicates if getMaxValue is unbounded.
     * 
     * @return true if getMaxValue is null
     */
    public boolean isMaxUnbounded() {
        return maxValue == null;
    }

    public boolean isEmpty(){
        if( isMinUnbounded() && !isMinIncluded()) {
            return true; // ie empty
        }
        if( isMaxUnbounded() && !isMaxIncluded()) {
            return true; // ie empty
        }
        if( isMinUnbounded() || isMaxUnbounded() ) {
            return false; // complete range of valid values
        }
        
        // Optimisation by inlining compareMin( max )
        if( isMinUnbounded() ){
            return false; // max cannot be less that unbound min
        }
        else if (isMaxUnbounded() ){
            return true; // now way min can be greater than unbound max
        }
        else {
            int minCompare = minValue.compareTo( maxValue );
            if( minCompare == 0 ){
                if( isMinIncluded && isMaxIncluded){
                    return false; // example 0 compared 0
                }
                else if (isMinIncluded && !isMaxIncluded){
                    return true; // example 0 compared +0
                }
                else if(!isMinIncluded && isMaxIncluded){
                    return true; // example -0 compared 0                   
                }
                else {
                    return true; // example -0 compared +0
                }
            }
            return minCompare > 0;
        }
    }    
    
    @Override    
    public String toString() {
        if( isEmpty() ){
            return "empty";
        }
        StringBuffer buf = new StringBuffer();
        if( isMinIncluded ){
            buf.append("[");    
        }
        else {
            buf.append("(");
        }
        if( minValue == null ){
            buf.append("unbounded");
        }
        else {
            buf.append( minValue );
        }
        buf.append(", ");
        if( maxValue == null ){
            buf.append("unbounded");
        }
        else {
            buf.append( maxValue );
        }
        if( isMaxIncluded ){
            buf.append("]");    
        }
        else {
            buf.append(")");
        }
        return buf.toString();        
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if( isEmpty() ) return 0;
        result = prime * result
                + ((elementClass == null) ? 0 : elementClass.hashCode());
        result = prime * result + (isMaxIncluded ? 1231 : 1237);
        result = prime * result + (isMinIncluded ? 1231 : 1237);
        result = prime * result
                + ((maxValue == null) ? 0 : maxValue.hashCode());
        result = prime * result
                + ((minValue == null) ? 0 : minValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Range other = (Range) obj;
        if (elementClass == null) {
            if (other.elementClass != null)
                return false;
        } else if (!elementClass.equals(other.elementClass))
            return false;
        if( isEmpty() && other.isEmpty())
            return true;

        if (isMaxIncluded != other.isMaxIncluded)
            return false;
        if (isMinIncluded != other.isMinIncluded)
            return false;
        if (maxValue == null) {
            if (other.maxValue != null)
                return false;
        } else if (!maxValue.equals(other.maxValue))
            return false;
        if (minValue == null) {
            if (other.minValue != null)
                return false;
        } else if (!minValue.equals(other.minValue))
            return false;
        return true;
    }
    /**
     * Intersection with provided Range.
     * @param range range t intersect  
     * @return intersection of provided range
     */
    Range<T> intersect(Range<T> range){
        if( range == null ){
            throw new IllegalArgumentException("Cannot intersect with null range");
        }
        if( !elementClass.equals( range.getElementClass() )){
            throw new IllegalArgumentException("Range element "+range.getElementClass()+" cannot be used with "+elementClass );
        }   
        if( isEmpty() || range.isEmpty() ){
            return new Range( elementClass ); // empty!            
        }
        int minCompare = compareMin( range.minValue, range.isMinIncluded );
        T min = minCompare < 0 ? range.getMinValue() : minValue; 
        boolean includeMin = minCompare < 0 ? range.isMinIncluded() : isMinIncluded;
        
        int maxCompare = compareMax( range.maxValue, range.isMaxIncluded );        
        T max = maxCompare > 0 ? range.getMaxValue() : maxValue;
        boolean includeMax = minCompare > 0 ? range.isMaxIncluded() : isMaxIncluded;
        return new Range<T>( elementClass, min, includeMin, max, includeMax );        
    }

    /**
     * @param range
     * @return @true if range intersects
     */
    public boolean intersects( Range range ){
        if( range == null ){
            throw new NullPointerException("Unable to intersects null");
        }
        return !intersect(range).isEmpty();
    }
    public Range[] subtract( Range range ){        
        throw new UnsupportedOperationException();
    }
    Range<T> union( Range<T> range ){
        if( range == null ){
            throw new IllegalArgumentException("Cannot intersect with null range");
        }
        if( !elementClass.equals( range.getElementClass() )){
            throw new IllegalArgumentException("Range element "+range.getElementClass()+" cannot be used with "+elementClass );
        }
        
        int minCompare = compareMin( range.minValue, range.isMinIncluded );
        int maxCompare = compareMax( range.maxValue, range.isMaxIncluded );
        
        T min = minCompare < 0 ? minValue : range.getMinValue();        
        boolean isMin = minCompare < 0 ? isMinIncluded : range.isMinIncluded;
        
        T max = maxCompare > 0 ? maxValue : range.getMaxValue();
        boolean isMax = maxCompare > 0 ? isMaxIncluded : range.isMaxIncluded;

        return new Range<T>( elementClass, min, isMin, max, isMax);
    }
    boolean contains( T value ){
        if( value == null || isEmpty()) {
            return false;
        }
        if( !isMinUnbounded() ){
            int compare = minValue.compareTo(value);
            if( compare == 0 && !isMinIncluded) return false;
            if( compare > 0 ) return false;
        }
        if( !isMaxUnbounded() ){
            int compare = maxValue.compareTo(value);
            if( compare == 0 && !isMaxIncluded ) return false;
            if( compare < 0) return false;
        }
        return true;
    }
    /**
     * Compare the provided value with minValue.
     * <p>
     * This implementation takes into account isMinUnbounded and isMinIncluded.
     * @param value
     * @param true if value is isIncluded in the comparison 
     * @return 0 if value is equal to minValue and both are included, negative if minValue is lower, positive if minValue is higher
     */
    int compareMin( T value, boolean isIncluded ){
        if( isMinUnbounded() ){
            return -1;
        }
        else if (value == null ){
            return 1;
        }
        else {
            int minCompare = minValue.compareTo( value );
            if( minCompare == 0 ){
                if( isMinIncluded && isIncluded){
                    return 0; // example 0 compared 0
                }
                else if (isMinIncluded && !isIncluded){
                    return -1; // example 0 compared +0
                }
                else if(!isMinIncluded && isIncluded){
                    return 1; // example -0 compared 0                   
                }
                else {
                    return -1; // example -0 compared +0
                }
            }
            return minCompare;
        }
    }
    /**
     * Compare the provided value with maxValue.
     * <p>
     * This implementation takes into account isMaxUnbounded and isMaxIncluded.
     * @param value
     * @param true if value is isIncluded in the comparison 
     * @return 0 if value is equal to maxValue and both are included, negative if maxValue is lower, positive if maxValue is higher
     */
    int compareMax( T value, boolean isIncluded ){
        if( isMaxUnbounded() ){
            return 1;
        }
        else if( value == null ){
            return -1;
        }
        else {
            int maxCompare = maxValue.compareTo( value );
            if( maxCompare == 0 ){
                if( isMaxIncluded && isIncluded){
                    return 0; // example 0 compared 0
                }
                else if (isMaxIncluded && !isIncluded){
                    return -1; // example 0 compared +0
                }
                else if(!isMaxIncluded && isIncluded){
                    return 1; // example -0 compared 0                   
                }
                else {
                    return -1; // example -0 compared +0
                }
            }
            return maxCompare;
        }
    }
    
    boolean contains( Range<T> range ){
        if( range == null || isEmpty() || range.isEmpty() )
            return false;

        return compareMin( range.getMaxValue(), range.isMaxIncluded ) < 0 &&
               compareMax( range.getMinValue(), range.isMinIncluded ) > 0;
   }
}