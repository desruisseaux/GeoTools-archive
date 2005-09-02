/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework;

/** Contains general helper functions
 *
 * @author  Matthias Basler
 */
public class Util {
    /** @return true, if the object is not <code>null</code>.
     * @param o the object to check
     * @throws NullPointerException if the object is <code>null</code>.*/
    public static boolean ensureNonNull(Object o) {
        if (o == null) throw new NullPointerException(o.toString());
        return true;
    }

    /** @return true, if the String is <b>not</b> <code>null</code> or "" 
     * @param str the String to check */
    public static boolean ensureNotEmpty(String str) {
        return (!(str == null || str.equals(""))); //$NON-NLS-1$
    }

    /** @return true, if the String is <code>null</code> or "" 
     * @param str the String to check */
    public static boolean isEmpty(String str) {
        return (str == null || str.equals("")); //$NON-NLS-1$
    }
}
