
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public interface Facet {
    public static int ENUMERATION = 1;
    public static int FRACTIONDIGITS = 2;
    public static int LENGTH = 3;
    public static int MAXEXCLUSIVE = 4;
    public static int MAXINCLUSIVE = 5;
    public static int MAXLENGTH = 6;
    public static int MINEXCLUSIVE = 7;
    public static int MININCLUSIVE = 8;
    public static int MINLENGTH = 9;
    public static int PATTERN = 10;
    public static int TOTALDIGITS = 11;
    public static int WHITESPACE = 12;
    
    /**
     * The Facet Type -- selected from one of the above constant values
     * @return
     */
    public int getFacetType();
    
    /**
     * The facet's constraint
     * @return
     */
    public String getValue();
}
