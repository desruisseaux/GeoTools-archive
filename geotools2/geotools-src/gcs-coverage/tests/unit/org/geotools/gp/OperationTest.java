/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;

// JAI dependencies
import javax.media.jai.*;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.gc.*;
import org.geotools.gp.*;
import org.geotools.resources.Arguments;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link OperationJAI} implementation.
 *
 * @version $Id: OperationTest.java,v 1.4 2002/08/09 18:41:23 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class OperationTest extends GridCoverageTest {
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(OperationTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public OperationTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Basic test of {@link GridCoverage}.
     */
    public void testGridCoverage() {
        // No test to do. It was already done by {@link GridCoverageTest}.
        // We don't want to reload all tested image...
    }

    /**
     * Test a simple {@link OpenrationJAI}.
     */
    public void testOperationJAI() {
        final OperationJAI operation = new OperationJAI("addConst");
        final PrintWriter out = new PrintWriter(Arguments.getWriter(System.out));
        if (true) try {
            operation.print(out, null);
        } catch (IOException exception) {
            exception.printStackTrace(out);
            fail();
        }
        assertEquals("numSources",    1, operation.getNumSources());
        assertEquals("numParameters", 2, operation.getNumParameters());
    }

    /**
     * Test the "Colormap" operation.
     */
    public void testColormap() {
        final Operation   operation = new ColormapOperation();
        final GridCoverage coverage = getRandomCoverage();
        final ParameterList   param = operation.getParameterList().setParameter("Source", coverage);
        final GridCoverage   result = operation.doOperation(param, null);
        assertTrue(!Arrays.equals(getARGB(coverage), getARGB(result)));
        assertTrue(!coverage.geophysics(true) .equals(result.geophysics(true )));
        assertTrue(!coverage.geophysics(false).equals(result.geophysics(false)));
    }

    /**
     * Returns the ARGB code for the specified coverage.
     */
    private static int[] getARGB(final GridCoverage coverage) {
        IndexColorModel colors = (IndexColorModel) coverage.getRenderedImage().getColorModel();
        final int[] ARGB = new int[colors.getMapSize()];
        colors.getRGBs(ARGB);
        return ARGB;
    }
}
