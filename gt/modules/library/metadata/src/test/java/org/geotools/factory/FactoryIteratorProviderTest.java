/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.factory;

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link FactoryIteratorProvider} addition in {@link Factories}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FactoryIteratorProviderTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FactoryIteratorProviderTest.class);
    }

    /**
     * Constructs a test case.
     */
    public FactoryIteratorProviderTest(final String testName) {
        super(testName);
    }

    /**
     * Creates a new, initially empty, factory registry.
     */
    private static FactoryRegistry createRegistry() {
        return new FactoryRegistry(Arrays.asList(new Class<?>[] {
            DummyFactory.class
        }));
    }

    /**
     * Returns a factory from the specified registry.
     *
     * @throws FactoryNotFoundException if no factory was found.
     * @throws FactoryRegistryException if a factory can't be returned for some other reason.
     */
    private static DummyFactory getFactory(final FactoryRegistry registry,
                                           final Class<? extends DummyFactory> type)
            throws FactoryRegistryException
    {
        Hints hints = null;
        if (type != null) {
            hints = new Hints(DummyFactory.DUMMY_FACTORY, type);
        }
        return registry.getServiceProvider(DummyFactory.class,
                null, hints, DummyFactory.DUMMY_FACTORY);
    }

    /**
     * Tests the registration of {@link DummyFactory} instances from
     * {@link DummyFactoryIteratorProvider}.
     */
    public void testRegistration() {
        FactoryRegistry registry = createRegistry();
        /*
         * Tests the initially empty factory.
         */
        try {
            assertNotNull(getFactory(registry, null));
            fail("No factory should have been found.");
        } catch (FactoryNotFoundException e) {
            // This is the expected exception.
        }
        /*
         * Tests after the addition of a new provider.
         */
        final FactoryIteratorProvider provider1 = new DummyFactoryIteratorProvider(true);
        try {
            Factories.addFactoryIteratorProvider(provider1);
            assertNotNull(getFactory(registry, null));
            DummyFactory factory;

            factory = getFactory(registry, DummyFactory.Example1.class);
            assertEquals(DummyFactory.Example1.class, factory.getClass());
            factory = getFactory(registry, DummyFactory.Example2.class);
            assertEquals(DummyFactory.Example2.class, factory.getClass());
            try {
                assertNotNull(getFactory(registry, DummyFactory.Example3.class));
                fail("Example #3 should not be registered.");
            } catch (FactoryNotFoundException e) {
                // This is the expected exception.
            }
            try {
                assertNotNull(getFactory(registry, DummyFactory.Example4.class));
                fail("Example #4 should not be registered.");
            } catch (FactoryNotFoundException e) {
                // This is the expected exception.
            }
            /*
             * Add yet an other provider, and test again.
             */
            final FactoryIteratorProvider provider2 = new DummyFactoryIteratorProvider(false);
            try {
                Factories.addFactoryIteratorProvider(provider2);
                factory = getFactory(registry, DummyFactory.Example1.class);
                assertEquals(DummyFactory.Example1.class, factory.getClass());
                factory = getFactory(registry, DummyFactory.Example2.class);
                assertEquals(DummyFactory.Example2.class, factory.getClass());
                factory = getFactory(registry, DummyFactory.Example3.class);
                assertEquals(DummyFactory.Example3.class, factory.getClass());
                factory = getFactory(registry, DummyFactory.Example4.class);
                assertEquals(DummyFactory.Example4.class, factory.getClass());
            } finally {
                Factories.removeFactoryIteratorProvider(provider2);
            }
        } finally {
            Factories.removeFactoryIteratorProvider(provider1);
        }
        /*
         * Tests with a new registry, which should be empty.
         */
        registry = createRegistry();
        try {
            assertNotNull(getFactory(registry, null));
            fail("The factory should be empty again.");
        } catch (FactoryNotFoundException e) {
            // This is the expected exception.
        }
    }
}
