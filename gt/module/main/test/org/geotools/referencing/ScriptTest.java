/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Management Committee (PMC)
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
 */
package org.geotools.referencing;

// J2SE dependencies
import java.net.URL;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.resources.TestData;
import org.geotools.referencing.operation.TestConsole;


/**
 * Run a test scripts. Scripts include a test suite provided by OpenGIS.
 * Each script contains a list of source and target coordinates reference systems (in WKT),
 * source coordinate points and expected coordinate points after the transformation from
 * source CRS to target CRS.
 *
 * This is probably the most important test case for the whole CRS module.
 *
 * @version $Id$
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class ScriptTest extends TestCase {
    /**
     * Run all tests from the command line.
     */
    public static void main(final String[] args) throws Exception {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScriptTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public ScriptTest(final String name) {
        super(name);
    }

    /**
     * Run the specified test script.
     *
     * @throws Exception If a test failed.
     */
    private void runScript(final String filename) throws Exception {
        final URL url = TestData.getResource(this, filename);
        if (url == null) {
            throw new FileNotFoundException(filename);
        }
        final LineNumberReader in = new LineNumberReader(new InputStreamReader(url.openStream()));
        final TestConsole test = new TestConsole(in);
        test.executeAll();
        in.close();
    }
    
    /**
     * Run "AbridgedMolodensky.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testAbridgedMolodesky() throws Exception {
        runScript("scripts/AbridgedMolodensky.txt");
    }
    
    /**
     * Run "Molodensky.txt".
     *
     * @throws IOException If {@link #MT_MOLODENSKY_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    public void testMolodesky() throws Exception {
        runScript("scripts/Molodensky.txt");
    }
    
    /**
     * Run "Projections.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testProjections() throws Exception {
        runScript("scripts/Projections.txt");
    }
    
    /**
     * Run the {@link #SIMPLE_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testSimple() throws Exception {
//        runScript(SIMPLE_SCRIPT);
//    }
    
    /**
     * Run the {@link #MERCATOR_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testMercator() throws Exception {
//        runScript(MERCATOR_SCRIPT);
//    }
    
    /**
     * Run the "ObliqueMercator.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testObliqueMercator() throws Exception {
        runScript("scripts/ObliqueMercator.txt");
    }
    
    /**
     * Run the {@link #TRANSVERSE_MERCATOR_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testTransverseMercator() throws Exception {
//        runScript(TRANSVERSE_MERCATOR_SCRIPT);
//    }   
    
    /**
     * Run the {@link #STEREOGRAPHIC_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testStereographic() throws Exception {
//        runScript(STEREOGRAPHIC_SCRIPT);
//    }
    
    /**
     * Run the {@link #ORTHOGRAPHIC_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testOrthographic() throws Exception {
//        runScript(ORTHOGRAPHIC_SCRIPT);
//    }
    
    /**
     * Run the {@link #ALBERS_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testAlbersEqualArea() throws Exception {
//        runScript(ALBERS_SCRIPT);
//    }
    
    /**
     * Run the {@link #LAMBERT_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testLambertConic() throws Exception {
//        runScript(LAMBERT_SCRIPT);
//    }
    
    /**
     * Run the {@link #OPENGIS_SCRIPT}.
     *
     * @throws Exception If a test failed.
     */
//    public void testOpenGIS() throws Exception {
//        runScript(OPENGIS_SCRIPT);
//    }
}
