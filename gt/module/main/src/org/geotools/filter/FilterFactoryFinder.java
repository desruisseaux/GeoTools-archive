/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

import java.util.Map;
import java.util.Collections;

import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Envelope;


/**
 * This specifies the interface to create filters.
 *
 * @version $Id$
 *
 * @task TODO: This needs to be massively overhauled.  This should be the
 *       source of immutability of filters.  See {@link FeatureTypeFactory},
 *       as that provides a good example of what this should look like.  The
 *       mutable factory to create immutable objects is a good model for this.
 *       The creation methods should only create fully formed filters.  This
 *       in turn means that all the set functions in the filters should be
 *       eliminated.  When rewriting this class/package, keep in mind
 *       FilterSAXParser in the filter module, as the factory should fit
 *       cleanly with that, and should handle sax parsing without too much
 *       memory overhead.
 * @task REVISIT: resolve errors, should all throw errors?
 */
public abstract class FilterFactoryFinder {
    /** A cached factory to create filters. */
    private static FilterFactory factory = null;

    /**
     * Creates an instance of a Filter factory.
     *
     * @return An instance of the Filter factory.
     *
     * @throws FactoryConfigurationError If a factory is not found.
     */
    public static FilterFactory createFilterFactory()
        throws FactoryConfigurationError {
        if (factory == null) {
            factory = (FilterFactory) FactoryFinder.findFactory("org.geotools.filter.FilterFactory",
                    "org.geotools.filter.FilterFactoryImpl");
        }

        return factory;
    }

}
