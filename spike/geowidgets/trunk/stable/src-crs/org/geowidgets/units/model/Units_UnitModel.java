/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.units.model;

import javax.units.*;

import org.geowidgets.framework.Res;

/** A class the builds on the Units framework only. (EPSG codes are used
 * in the .properies file only to support localization.) The number of supported
 * units is limited to those manually defined in this class and .properties file. <p/>
 * This class keeps all units in memory, returning the same object each time
 * the same unit is requested. The class is a singleton itself.
 * 
 * @author Matthias Basler
 */
public class Units_UnitModel extends _UnitCombiBoxModel {
    protected static Units_UnitModel me = new Units_UnitModel();

    /** @return the default instance of this implementation. This is a singleton. */
    public static IUnitModel getDefault() {
        return me;
    }

    protected Units_UnitModel() {
        fillUnits();
    }

    protected void fillUnits() {
        try {
            insertL(9001, SI.METER);
            insertL(9002, NonSI.FOOT);
            insertL(9005, NonSI.FOOT);
            insertL(9030, NonSI.NAUTICAL_MILE);
            insertL(9035, NonSI.MILE);
            insertL(9036, SI.KILO(SI.METER));
            insertL(9037, NonSI.YARD);

            insertA(9101, SI.RADIAN);
            insertA(9102, NonSI.DEGREE_ANGLE);
            insertA(9105, NonSI.GRADE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void insertL(int code, Unit u) {
        String name = Res.get(Res.UNITS, Integer.toString(code));
        lUnits.put(name, u);
    }

    protected void insertA(int code, Unit u) {
        String name = Res.get(Res.UNITS, Integer.toString(code));
        aUnits.put(name, u);
    }

    //--------------------------------------------------------------------------
    public String getDefaultUnit(int unitType) {
        return (unitType == UNIT_ANGULAR) ? Res.get(Res.UNITS, Integer
                .toString(9101)) : Res.get(Res.UNITS, Integer.toString(9001));
    }

}
