/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.geowidgets.framework.Res;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;

/** This class is responsible to deliver UI elements and their attributes
 * such as colors specifically for the CRS selection widget. <p/>
 * General UI elements and their attributes are found in the GeneralSwingUIFactory.
 * 
 * @author Matthias Basler
 */
public class CRS_SwingUIFactory extends GeneralSwingUIFactory {
    /** The default <code>CRS_SwingUIFactory</code>.*/
    protected static CRS_SwingUIFactory me = new CRS_SwingUIFactory();

    /** Creates the instance of this singleton class. */
    protected CRS_SwingUIFactory() {
        initColors();
    }

    /** Returns the default look and feel for the CRS widgets. */
    public static CRS_SwingUIFactory getDefault() {
        return me;
    }
    //******************************************************************************
    /** Contains the default colors for the panels in the CRS assembly widgets. */
    protected Map<Class, Color> colors = new HashMap<Class, Color>();

    /** Fills the map of colors for the CRS assembly widgets.*/
    protected void initColors() {
        colors.put(Ellipsoid.class, new Color(204, 255, 153));
        colors.put(PrimeMeridian.class, new Color(204, 255, 153));
        colors.put(GeodeticDatum.class, new Color(153, 204, 255));
        colors.put(CoordinateSystemAxis.class, new Color(249, 140, 255));
        colors.put(EllipsoidalCS.class, new Color(209, 153, 255));
        colors.put(CartesianCS.class, new Color(209, 153, 255));
        colors.put(Conversion.class, new Color(209, 183, 225));
        colors.put(Projection.class, new Color(209, 183, 225));//Same as Conversion
        colors.put(GeographicCRS.class, new Color(153, 153, 255));
        colors.put(ProjectedCRS.class, new Color(170, 170, 235));

    }

    //Colors
    /** @return the color to use for each of the "CustomPanel"s of the
     * CRS assembly widget. 
     * @param cl the class, on which the color does depend. */
    public Color getCustomPanelColor(Class cl) {
        return colors.get(cl);
    }

    //UI elements
    /** Nearly each CRS assembly widget's element has a "CustomPanel" attached that
     * allows to assemble the object from its parameters or sub objects.
     * This panel is created and configured here.
     * @return the pre-configured "CustomPanel"
     * @param titleResource the key for the localized String. 
     * @param backgroundColor the panel's background color */
    public JPanel createCustomPanel(String titleResource, Color backgroundColor) {
        JPanel panel = super.createJPanel(Res.CRS, titleResource);
        if (backgroundColor != null) panel.setBackground(backgroundColor);
        return panel;
    }
}
