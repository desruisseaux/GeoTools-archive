/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.transform;

// JUnit dependencies
import junit.framework.TestCase;


/**
 * Tests the {@link EarthGravitationalModel} class.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EarthGravitationalModelTest extends TestCase {
    /**
     * Tests the {@link EarthGravitationalModel#heightOffset} method.
     */
    public void testHeightOffset() throws Exception {
        final EarthGravitationalModel gh = new EarthGravitationalModel();
        gh.load("EGM180.nor");
        assertEquals( 1.505, gh.heightOffset(45, 45,    0), 0.001);
        assertEquals( 1.515, gh.heightOffset(45, 45, 1000), 0.001);
        assertEquals(46.908, gh.heightOffset( 0, 45,    0), 0.001);
    }
}
