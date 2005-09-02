/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package test.crs;

import java.util.List;

import junit.framework.TestCase;

import org.geowidgets.crs.model.GeoTools_AxisDirectionModel;
import org.geowidgets.crs.model.IAxisDirectionModel;
import org.opengis.referencing.cs.AxisDirection;

/** Tests the implementation of IAxisDirectionModel. */
public class JUnit_AxisDirectionModel extends TestCase {
    IAxisDirectionModel model;

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("************************************");
        model = GeoTools_AxisDirectionModel.getDefault();
    }

    /** Test method for 'GeoTools_AxisDirectionModel.getDefault()'
     */
    public void testGetDefault() {
        assertNotNull("Axis direction model is null.", model);
    }

    /** Test method for 'GeoTools_AxisDirectionModel.getSupportedAxisDirections()'
     */
    public void testGetSupportedAxisDirections() {
        List<String> list = model.getSupportedAxisDirections();
        String err = "List of supported axis directions cannot be null or empty.";
        assertTrue(err, list != null & list.size() != 0);
        System.out.println("Supported objects returned :" + list.size());
    }

    /** Test method for 'GeoTools_AxisDirectionModel.getAxisDirection(int)'
     */
    public void testGetAxisDirection() {
        AxisDirection ad = model.getAxisDirection(1);
        assertNotNull("Returned second axis direction must not be null.", ad);
        System.out.println("First axis direction: " + ad.name());
    }

    /** Test method for 'GeoTools_AxisDirectionModel.createAxisDirection(String)'
     */
    public void testCreateAxisDirection() {
        AxisDirection ad = model.createAxisDirection("Custom");
        assertNotNull("Created custom axis direction must not be null.", ad);
        System.out.println("Custom axis direction: " + ad.name());
        
    }

}
