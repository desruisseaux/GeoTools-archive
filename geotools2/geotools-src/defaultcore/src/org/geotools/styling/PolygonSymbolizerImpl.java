/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.styling;

/**
 * @version $Id: PolygonSymbolizerImpl.java,v 1.6 2003/07/22 16:36:52 ianturton Exp $
 * @author James Macgill, CCG
 */
public class PolygonSymbolizerImpl
    implements org.geotools.styling.PolygonSymbolizer {
    private Fill fill = new FillImpl();
    private Stroke stroke = new StrokeImpl();
    private String geometryPropertyName = null;

    /** Creates a new instance of DefaultPolygonStyler */
    protected PolygonSymbolizerImpl() {
    }

    public int hashcode() {
        int key = 0;
        key = fill.hashCode();
        key = (key * 13) + stroke.hashCode();
        key = (key * 13) + geometryPropertyName.hashCode();

        return key;
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently area types can be used.
     * If a line is used then the line string is closed for filling (only)
     * by connecting its end point to its start point.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an
     * XPath reference to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }

    public String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }

    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry.
     *
     * @return The Fill style to use when rendering the area.
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * Sets the graphical-symbolization parameter to use to fill the area
     * of the geometry.
     *
     * @param fill The Fill style to use when rendering the area.
     */
    public void setFill(Fill fill) {
        this.fill = fill;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the outline
     * of the Polygon.
     *
     * @return The Stroke style to use when rendering lines.
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the graphical-symbolization parameter to use for the outline
     * of the Polygon.
     *
     * @param stroke The Stroke style to use when rendering lines.
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
}