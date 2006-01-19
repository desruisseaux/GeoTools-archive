/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.factory;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;

// JUnit dependencies
import junit.framework.Assert;


/**
 * An internal dummy factory for testing factory dependencies.
 * It doesn't matter if this factory is registered or not. We
 * just need a {@code InternalFactory.class} value different
 * than {@code DummyFactory.class}.
 * @source $URL$
 */
interface InternalFactory extends Factory {
}

/**
 * Dummy factory interface for {@link FactoryRegistryTest}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DummyFactory extends InternalFactory {
    /**
     * A hint key for a {@code DummyFactory} instance.
     */
    Hints.Key DUMMY_FACTORY = new Hints.Key(DummyFactory.class.getName());

    /**
     * A hint key for a {@code DummyFactory2} instance.
     */
    Hints.Key INTERNAL_FACTORY = new Hints.Key(InternalFactory.class.getName());

    /**
     * Dummy factory implementation #1.
     * This factory doesn't use any other factory.
     */
    final class Example1 implements DummyFactory {
        public String toString() {
            return "#1";
        }

        public Map getImplementationHints() {
            return Collections.singletonMap(Hints.KEY_INTERPOLATION,
                                            Hints.VALUE_INTERPOLATION_BILINEAR);
        }
    }

    /**
     * Dummy factory implementation #2.
     * This factory uses factory #1.
     */
    final class Example2 implements DummyFactory {
        public String toString() {
            return "#2";
        }

        public Map getImplementationHints() {
            return Collections.singletonMap(INTERNAL_FACTORY, new Example1());
        }
    }

    /**
     * Dummy factory implementation #3.
     * This factory uses factory #2, which uses itself factory #1.
     */
    final class Example3 implements DummyFactory {
        public String toString() {
            return "#3";
        }

        public Map getImplementationHints() {
            return Collections.singletonMap(INTERNAL_FACTORY, new Example2());
        }
    }

    /**
     * Dummy factory implementation #4.
     * {@link FactoryRegistryTest} will not register this factory in same time than other ones.
     */
    final class Example4 implements DummyFactory {
        public String toString() {
            return "#4";
        }

        public Map getImplementationHints() {
            return Collections.singletonMap(Hints.KEY_INTERPOLATION,
                                            Hints.VALUE_INTERPOLATION_BICUBIC);
        }
    }

    /**
     * Dummy factory implementation #5.
     * {@link FactoryRegistryTest} will not register this factory in same time than other ones.
     * This factory is the only one to accept hints.
     */
    final class Example5 implements DummyFactory {
        public Example5() {
            Assert.fail("The constructor with Hints argument should have been used.");
        }

        public Example5(Hints hints) {
        }

        public String toString() {
            return "#5";
        }

        public Map getImplementationHints() {
            return Collections.singletonMap(Hints.KEY_INTERPOLATION,
                                            Hints.VALUE_INTERPOLATION_BILINEAR);
        }
    }
}
