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

import java.util.Map;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A 2D coordinate reference system used to approximate the shape of the earth on a planar surface.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultProjectedCRS}.
 */
public class ProjectedCRS extends DefaultProjectedCRS {
    /**
     * Constructs a projected CRS from a name.
     */
    public ProjectedCRS(final String                 name,
                        final OperationMethod      method,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        super(name, method, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected CRS from a set of properties.
     */
    public ProjectedCRS(final Map              properties,
                        final OperationMethod      method,
                        final GeographicCRS          base,
                        final MathTransform baseToDerived,
                        final CartesianCS       derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, method, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a projected CRS from a defining conversion.
     */
    public ProjectedCRS(final Map                 properties,
                        final Conversion  conversionFromBase,
                        final GeographicCRS             base,
                        final MathTransform    baseToDerived,
                        final CartesianCS          derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, conversionFromBase, base, baseToDerived, derivedCS);
    }
}
