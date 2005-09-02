/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.model;

import java.util.Vector;

import org.opengis.referencing.cs.AxisDirection;

/** A model for the axis direction dropdown.
 * Capabilities: <ul>
 * <li> provide list of available axis directions
 * <li> create axis direction from its name or index on the list.
 * </ul>
 * The contract is: All axis directions returned by
 * <code>getSupportedAxisDirections()</code> must be assured to work. 
 * So the other functions shall not throw any error when invoked with a
 * parameter (name or index) from this list.
 * @author Matthias Basler
 */
public interface IAxisDirectionModel {
    /** @return an ordered vector of available axis directions. */
    public Vector<String> getSupportedAxisDirections();

    /** @return the axis direction from its position in the vector as
     * returned by <code>getSupportedAxisDirections()</code>.
     * @param index the index in the said vector */
    public AxisDirection getAxisDirection(int index);

    /** @return a new (custom) axis direction.
     * @param name a name for the new direction. Be a little inventive:
     * Things like north and east and up are already allocated. */
    public AxisDirection createAxisDirection(String name);
}
