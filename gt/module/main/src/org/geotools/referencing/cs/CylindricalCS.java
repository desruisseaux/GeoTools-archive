/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.cs;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.PolarCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * A three-dimensional coordinate system consisting of a
 * {@linkplain org.geotools.referencing.cs.PolarCS polar coordinate system} extended by a straight
 * coordinate axis perpendicular to the plane spanned by the polar coordinate system.
 * A <code>CylindricalCS</code> shall have three {@linkplain #getAxis axis}.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CRS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.crs.EngineeringCRS Engineering}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see PolarCS
 */
public class CylindricalCS extends org.geotools.referencing.cs.CoordinateSystem
                        implements org.opengis.referencing.cs.CylindricalCS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8290402732390917909L;

    /**
     * The polar coordinate system.
     */
    private final PolarCS polarCS;

    /**
     * Construct a three-dimensional coordinate system from a name.
     *
     * @param name    The coordinate system name.
     * @param polarCS The polar coordinate system.
     * @param axis    The perpendicular axis.
     */
    public CylindricalCS(final String               name,
                         final PolarCS           polarCS,
                         final CoordinateSystemAxis axis)
    {
        super(name, new CoordinateSystemAxis[] {polarCS.getAxis(0), polarCS.getAxis(1), axis});
        this.polarCS = polarCS;
    }

    /**
     * Construct a three-dimensional coordinate system from a set of properties. The properties map is
     * given unchanged to the {@linkplain CoordinateSystem#CoordinateSystem(Map,CoordinateSystemAxis[])
     * super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param polarCS    The polar coordinate system.
     * @param axis       The perpendicular axis.
     */
    public CylindricalCS(final Map            properties,
                         final PolarCS           polarCS,
                         final CoordinateSystemAxis axis)
    {
        super(properties, new CoordinateSystemAxis[] {polarCS.getAxis(0), polarCS.getAxis(1), axis});
        this.polarCS = polarCS;
    }

    /**
     * Returns <code>true</code> if the specified axis direction is allowed for this coordinate
     * system. The default implementation accepts all directions except temporal ones (i.e.
     * {@link AxisDirection#FUTURE FUTURE} and {@link AxisDirection#PAST PAST}).
     */
    protected boolean isCompatibleDirection(final AxisDirection direction) {
        return !AxisDirection.FUTURE.equals(direction.absolute());
    }
}
