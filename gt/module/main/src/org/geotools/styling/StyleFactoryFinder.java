package org.geotools.styling;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;

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
