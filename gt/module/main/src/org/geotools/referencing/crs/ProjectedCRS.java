/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;
import javax.units.Unit;

// OpenGIS direct dependencies
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;

// Geotools dependencies
import org.geotools.referencing.wkt.Formatter;


/**
 * A 2D coordinate reference system used to approximate the shape of the earth on a planar surface.
 * It is done in such a way that the distortion that is inherent to the approximation is carefully
 * controlled and known. Distortion correction is commonly applied to calculated bearings and
 * distances to produce values that are a close match to actual field values.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.cs.CartesianCS Cartesian}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProjectedCRS extends GeneralDerivedCRS
                       implements org.opengis.referencing.crs.ProjectedCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4502680112031773028L;

    /**
     * Constructs a projected CRS from a name.
     *
     * @param  name The name.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public ProjectedCRS(final String                 name,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        this(Collections.singletonMap("name", name), base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected CRS from a set of properties.
     * The properties are given unchanged to the super-class constructor.
     *
     * @param  properties Name and other properties to give to the new object.
     *         Available properties are {@linkplain org.geotools.referencing.Factory listed there}.
     *         Properties for the {@link Projection} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws MismatchedDimensionException if the source and target dimension of
     *         <code>baseToDeviced</code> don't match the dimension of <code>base</code>
     *         and <code>derivedCS</code> respectively.
     */
    public ProjectedCRS(final Map              properties,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected coordinate reference system from a projection name.
     * 
     * @param  properties Name and other properties to give to the new object.
     *         Properties for the {@link Conversion} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  projectionName The classification name for the projection to be created
     *         (e.g. "Transverse_Mercator", "Mercator_1SP", "Oblique_Stereographic", etc.).
     * @param  parameterValues The parameter value to give to the projection. Should includes
     *         "central_meridian", "latitude_of_origin", "scale_factor", "false_easting",
     *         "false_northing" and any other parameters specific to the projection.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @todo Not yet implemented.
     */
    public ProjectedCRS(final Map                          properties,
                        final GeographicCRS                    geoCRS,
                        final String                   projectionName,
                        final GeneralParameterValue[] parameterValues,
                        final CartesianCS                          cs)
    {
        super(properties, geoCRS, null, cs);
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns a hash value for this projected CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "PROJCS"
     */
    protected String formatWKT(final Formatter formatter) {
        final Unit unit = getUnit();
        final Unit oldUnit = formatter.getContextualUnit();
        formatter.append(baseCRS);
        formatter.append(conversionFromBase.getMethod());
        formatter.setContextualUnit(unit);
        formatter.append(conversionFromBase);
        formatter.setContextualUnit(oldUnit);
        final int dimension = coordinateSystem.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        if (unit == null) {
            formatter.setInvalidWKT();
        }
        return "PROJCS";
    }
}
