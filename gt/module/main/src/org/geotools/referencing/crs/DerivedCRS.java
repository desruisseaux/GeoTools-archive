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
package org.geotools.referencing.crs;

import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A coordinate reference system that is defined by its coordinate conversion from another
 * coordinate reference system but is not a projected coordinate reference system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultDerivedCRS}. 
 */
public class DerivedCRS extends DefaultDerivedCRS {
    /**
     * Constructs a derived CRS from a name.
     */
    public DerivedCRS(final String                    name,
                      final OperationMethod         method,
                      final CoordinateReferenceSystem base,
                      final MathTransform    baseToDerived,
                      final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(name, method, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a derived CRS from a set of properties.
     */
    public DerivedCRS(final Map                 properties,
                      final OperationMethod         method,
                      final CoordinateReferenceSystem base,
                      final MathTransform    baseToDerived,
                      final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, method, base, baseToDerived, derivedCS);
    }

    /**
     * Constructs a derived CRS from a defining conversion.
     */
    public DerivedCRS(final Map                 properties,
                      final Conversion  conversionFromBase,
                      final CoordinateReferenceSystem base,
                      final MathTransform    baseToDerived,
                      final CoordinateSystem     derivedCS)
            throws MismatchedDimensionException
    {
        super(properties, conversionFromBase, base, baseToDerived, derivedCS);
    }
}
