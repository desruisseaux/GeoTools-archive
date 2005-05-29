/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

import org.opengis.referencing.operation.MathTransformFactory;
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.FactoryGroup;



/**
 * @deprecated Renamed as {@link DefaultCoordinateOperationFactory}.
 */
public class CoordinateOperationFactory extends DefaultCoordinateOperationFactory {
    public CoordinateOperationFactory() {
        super();
    }

    public CoordinateOperationFactory(final Hints hints) {
        super(hints);
    }

    public CoordinateOperationFactory(final MathTransformFactory mtFactory) {
        super(mtFactory);
    }

    public CoordinateOperationFactory(final FactoryGroup factories) {
        super(factories);
    }
}
