/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.styling;

import org.geotools.filter.Expression;


/**
 * Abstract base class for implementing style factories.
 */
public interface StyleFactory2 extends StyleFactory {
    /**
     * Label Shield hack, non SLD 1.1
     *
     * @param fill DOCUMENT ME!
     * @param fonts DOCUMENT ME!
     * @param halo DOCUMENT ME!
     * @param label DOCUMENT ME!
     * @param labelPlacement DOCUMENT ME!
     * @param geometryPropertyName DOCUMENT ME!
     * @param graphic DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TextSymbolizer2 createTextSymbolizer(Fill fill, Font[] fonts,
        Halo halo, Expression label, LabelPlacement labelPlacement,
        String geometryPropertyName, Graphic graphic);
}
