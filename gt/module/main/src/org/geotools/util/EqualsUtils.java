/*
 * Created on 29/08/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.geotools.util;

/**
 * Utility class that provides a method for checking equality
 * between two objects using the method defined by Joshua Bloch in
 * Effective Java.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation. 
 *
 * @deprecated This method was already provided in {@link org.geotools.resources.Utilities}.
 *             Note that this method is not part of the public API and may be removed in any
 *             future Geotools version (especially when a similar method will be available
 *             in J2SE code).
 */
public class EqualsUtils {

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 != null) {
                return false;
            }
        } else {
            if (!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }
}
