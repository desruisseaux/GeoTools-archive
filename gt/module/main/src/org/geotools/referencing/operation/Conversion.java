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

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;


/**
 * An operation on coordinates that does not include any change of Datum. The best-known
 * example of a coordinate conversion is a map projection. The parameters describing
 * coordinate conversions are defined rather than empirically derived. Note that some
 * conversions have no parameters. 
 *  
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Transformation
 */
public class Conversion extends Operation implements org.opengis.referencing.operation.Conversion {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2148164324805562793L;

    /**
     * Construct a conversion from a set of properties. The properties given in argument
     * follow the same rules than for the {@link CoordinateOperation} constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source coordinate
     *                  reference system} to positions in the {@linkplain #getTargetCRS target
     *                  coordinate reference system}.
     * @param method    The operation method.
     */
    public Conversion(final Map                       properties,
                      final CoordinateReferenceSystem sourceCRS,
                      final CoordinateReferenceSystem targetCRS,
                      final MathTransform             transform,
                      final OperationMethod           method)
    {
        super(properties, sourceCRS, targetCRS, transform, method);
    }

    /**
     * Version of the coordinate transformation.
     *
     * @deprecated This attribute is declared in {@link CoordinateOperation}
     *             but is not used in a conversion.
     */
    public String getOperationVersion() {
        return super.getOperationVersion();
    }
}
