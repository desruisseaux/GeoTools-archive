/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;


/**
 *
 * @source $URL$
 */
public class StyleFactoryFinder {
    private static StyleFactory factory = null;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of the Factory, or null if the Factory could not be
     *         created.
     *
     * @throws FactoryConfigurationError DOCUMENT ME!
     */
    public static StyleFactory createStyleFactory()
        throws FactoryConfigurationError {
        if (factory == null) {
            factory = (StyleFactory) FactoryFinder.findFactory("org.geotools.styling.StyleFactory",
                    "org.geotools.styling.StyleFactoryImpl");
        }

        return factory;
    }
}
