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
package org.geotools.factory;


/**
 * Deadly error. Usually if this is thrown, an application built upon geotools
 * will not be able to function. I make this an error so that the standard bad code<br>
 * <code> try { somthingRisky(); } catch (Exception e) {
 * logger.warning("something happened"); }</code> will be subverted and the
 * error will grind the application to a halt, as it should.
 *
 * @author Ian Schneider
 * @source $URL$
 * @version $Id$
 *
 * @see java.lang.Error
 *
 * @deprecated This error was used by {@link FactoryFinder}. The proposed replacement
 *       ({@link FactoryRegistry}) uses an exception ({@link FactoryRegistryException}) instead of
 *       an error. There is two reasons why we switched back to an exception instead of an error:
 *       <ul>
 *         <li>At the difference of {@link FactoryFinder}, {@link FactoryRegistry} do not expects
 *             a default implementation to be specified.</li>
 *         <li>{@link FactoryRegistry} can accepts an optional set of user-provided {@link Hints}.
 *             Those hints may reduces the set of acceptable factories.</li>
 *       </ul>
 *       Because of the above, a "factory not found" exception may not be caused by a JVM,
 *       compilation or packaging error. It is more similar to a
 *       {@link java.util.MissingResourceException}.
 */
public class FactoryConfigurationError extends Error {
    /**
     * Creates a new instance of FactoryConfigurationError
     *
     * @param message Informative statememt about what whent wrong
     */
    public FactoryConfigurationError(String message) {
        super(message);
    }

    /**
     * Creates a new instance of FactoryConfigurationError
     *
     * @param message Informative statememt about what whent wrong
     * @param cause The origional exception which caused the problem
     */
    public FactoryConfigurationError(String message, Throwable cause) {
        super(message, cause);
    }
}
