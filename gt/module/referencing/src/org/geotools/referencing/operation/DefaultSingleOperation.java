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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.SingleOperation;


/**
 * A single (not {@linkplain DefaultConcatenatedOperation concatenated}) coordinate operation.
 *  
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultSingleOperation extends AbstractCoordinateOperation implements SingleOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 672935231344137185L;

    /**
     * Constructs a new single operation with the same values than the specified defining
     * conversion, together with the specified source and target CRS. This constructor
     * is used by {@link DefaultConversion} only.
     */
    DefaultSingleOperation(final Conversion               definition,
                           final CoordinateReferenceSystem sourceCRS,
                           final CoordinateReferenceSystem targetCRS,
                           final MathTransform             transform)
    {
        super(definition, sourceCRS, targetCRS, transform);
    }

    /**
     * Constructs a single operation from a set of properties. The properties given in argument
     * follow the same rules than for the {@link AbstractCoordinateOperation} constructor.
     *
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source CRS}
     *                  to positions in the {@linkplain #getTargetCRS target CRS}.
     */
    public DefaultSingleOperation(final Map                      properties,
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final MathTransform             transform)
    {
        super(properties, sourceCRS, targetCRS, transform);
    }
}
