/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.model;

import java.util.Vector;

import org.opengis.referencing.cs.AxisDirection;

/** Factory that uses GeoTools to care about axis directions.
 * @author Matthias Basler
 */
public class GeoTools_AxisDirectionModel implements IAxisDirectionModel {
    protected static IAxisDirectionModel me = new GeoTools_AxisDirectionModel();

    /** @return the default GeoTools-based solution for an IAxisDirectionModel.*/
    public static IAxisDirectionModel getDefault() {
        return me;
    }

    protected GeoTools_AxisDirectionModel() {
    }

//******************************************************************************    
    //The actual work
    public Vector<String> getSupportedAxisDirections() {
        AxisDirection[] ad = AxisDirection.values();
        Vector<String> v = new Vector<String>();
        for (int i = 0; i < ad.length; ++i)
            v.addElement(ad[i].name());
        return v;
    }

    public AxisDirection getAxisDirection(int index) {
        return AxisDirection.values()[index];
    }

    public AxisDirection createAxisDirection(String name) {
        name = name.toUpperCase();
        for (AxisDirection ad : AxisDirection.values()) {
            if (ad.name().equals(name)) return ad;
        }
        return new AxisDirection(name);
    }
}
