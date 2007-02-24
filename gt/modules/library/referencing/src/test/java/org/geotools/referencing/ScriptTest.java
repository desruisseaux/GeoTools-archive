/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE dependencies
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.test.TestData;


/**
 * Run a test scripts. Scripts include a test suite provided by OpenGIS.
 * Each script contains a list of source and target coordinates reference systems (in WKT),
 * source coordinate points and expected coordinate points after the transformation from
 * source CRS to target CRS.
 * <p>
 * This is probably the most important test case for the whole CRS module.
 *
 * @source $URL$
 * @version $Id$
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class ScriptTest extends TestCase {
    /**
     * Run all tests from the command line.
     */
    public static void main(final String[] args) throws Exception {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
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
        final LineNumberReader in = TestData.openReader(this, filename);
        final TestScript test = new TestScript(in);
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
     * Run "Simple.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testSimple() throws Exception {
        runScript("scripts/Simple.txt");
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
     * Run "Mercator.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testMercator() throws Exception {
        runScript("scripts/Mercator.txt");
    }
    
    /**
     * Run the "ObliqueMercator.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testObliqueMercator() throws Exception {
        runScript("scripts/ObliqueMercator.txt");
    }
    
    /**
     * Run "TransverseMercator.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testTransverseMercator() throws Exception {
        runScript("scripts/TransverseMercator.txt");
    }   
    
    /**
     * Run "AlbersEqualArea.txt"
     *
     * @throws Exception If a test failed.
     */
    public void testAlbersEqualArea() throws Exception {
        runScript("scripts/AlbersEqualArea.txt");
    }
    
    /**
     * Run "LambertAzimuthalEqualArea.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testLambertAzimuthalEqualArea() throws Exception {
        runScript("scripts/LambertAzimuthalEqualArea.txt");
    }
    
    /**
     * Run "LambertConic.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testLambertConic() throws Exception {
        runScript("scripts/LambertConic.txt");
    }
    
    /**
     * Run "Stereographic.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testStereographic() throws Exception {
        runScript("scripts/Stereographic.txt");
    }
    
    /**
     * Run "Orthographic.txt".
     *
     * @throws Exception If a test failed.
     */
    public void testOrthographic() throws Exception {
        runScript("scripts/Orthographic.txt");
    }
    
    /**
     * Run "NZMG.txt"
     * 
     * @throws Exception If a test failed.
     */
    public void testNZMG() throws Exception {
    	runScript("scripts/NZMG.txt");
    }
    
    /**
     * Run "Krovak.txt"
     * 
     * @throws Exception If a test failed.
     */
    public void testKrovak() throws Exception {
    	runScript("scripts/Krovak.txt");
    }
    
    /**
     * Run "OpenGIS.txt".
     *
     * @throws Exception If a test failed.
     */
//    public void testOpenGIS() throws Exception {
//        runScript("scripts/OpenGIS.txt");
//    }
    
    /**
     * Run "NADCON.txt"
     *
     * @throws Exception If a test failed.
     */
//    public void testNADCON() throws Exception {
//        runScript("scripts/NADCON.txt");
//    }
}
