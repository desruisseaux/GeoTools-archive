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
package org.geotools.util;

// J2SE dependencies
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link KeySortedList}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class KeySortedListTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(KeySortedListTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public KeySortedListTest(final String name) {
        super(name);
    }

    /**
     * Inserts random floating point numbers into the list. The key is the integer part of the
     * floating point number. This means that the number should be sorted in such a way that
     * their integer part are in increasing order, while the fractional part remains in random
     * order.
     */
    public void testAdd() {
        final Random      random = new Random(6969483179756527012L);
        final KeySortedList list = new KeySortedList();
        final Collection   check = new ArrayList();
        final int    maxElements = 1000;
        for (int i=0; i<maxElements; i++) {
            final double  x     = random.nextDouble() * (maxElements/10);
            final Integer key   = new Integer((int) x);
            final Double  value = new Double(x);
            list.add(key, value);
            check.add(value);
        }
        /*
         * Checks the content.
         */
        assertEquals(maxElements, check.size());
        assertEquals(maxElements, list .size());
        assertEquals(new HashSet(check), new HashSet(list));
        /*
         * Checks the iteration.
         */
        int count=0, lastKey=0;
        for (final ListIterator it=list.listIterator(); it.hasNext(); count++) {
            assertEquals(count, it.nextIndex());
            final Double element = (Double) it.next();
            assertEquals(count, it.previousIndex());
            final double value = element.doubleValue();
            final int    key   = (int) value;
            assertTrue(key >= lastKey);
            lastKey = key;
            assertSame(element, list.get(count));
        }
        assertEquals(maxElements, count);
        /*
         * Checks the iteration from a middle point.
         */
        final Integer     midKey = new Integer(maxElements/10 / 2);
        final KeySortedList head = list.headList(midKey);
        final KeySortedList tail = list.tailList(midKey);
        final Collection rebuild = new ArrayList(head);
        rebuild.addAll(tail);
        assertEquals(list.size(), head.size() + tail.size());
        assertEquals(list, rebuild);
        assertSame(list.listIterator(midKey).next(), tail.listIterator().next());
    }
}
