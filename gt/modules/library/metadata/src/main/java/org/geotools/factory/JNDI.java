/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Provides {@linkplain InitialContext initial context} for <cite>Java Naming and Directory
 * Interfaces</cite> (JNDI) in Geotools. This classes provides a central place where initial
 * context can been found for the Geotools library. This context is used for example by the
 * {@linkplain org.geotools.referencing.factory.epsg.DefaultFactory EPSG factory} in order to
 * find connection parameters to an EPSG database. Using JNDI, such connection parameters can
 * be set in a J2EE environment.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Current version just returns the default context. Future version may performs
 *       a more elaborated choice, for example using similar plugin mechanism than
 *       other factories.
 */
public final class JNDI {
    /**
     * The initial context. Will be created only when first needed.
     */
    private static InitialContext context;

    /**
     * Do not allows instantiation of this class.
     */
    private JNDI() {
    }
    
    /**
     * Used for force the initial context for test cases ... or as needed.
     */
    public static void init( InitialContext context ){
        JNDI.context = context;
    }

    /**
     * Returns the default initial context.
     *
     * @param  hints An optional set of hints, or {@code null} if none.
     * @return The initial context (never {@code null}).
     * @throws NamingException if the initial context can't be created.
     */
    public static synchronized InitialContext getInitialContext(final Hints hints)
            throws NamingException
    {
        if (context == null) {
            context = new InitialContext();
        }
        return context;
    }
}
