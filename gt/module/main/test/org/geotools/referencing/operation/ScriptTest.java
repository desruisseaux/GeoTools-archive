/*
 * A first attempt to port script test to ...referencing
 *
 * To do:
 *   Uncomment MathTransform wkt check in addDefinition()
 *      - reqires fix / clarification to AbstractMathTransform formatWKT()
 *   Uncomment MT_Molodensky_TestScript - needs MolodenskyTransform implemented
 *   Uncomment MT_Proj_TestScript - need some projections implemented
 *   Uncomment <projection>_TestScripts in main (to test CRS parsing)
 *      - requires a CRS parser
 *   Uncomment FactoryFinder.getCoordinateOperationFactory() in SetUp()
 *      and test calculations in runInstruction()
 *      - requires a CoordinateOperationFactory
 *
 * Would be nice to do:
 *   Add tests for CONCAT_MT, INVERSE_MT, PASSTHROUGH_MT
 *   Find a more extensible way to add new tests (don't hardcode them)
 *
 * Done:
 *   Use new interfaces
 *   Get MT_*Tests running - need a wkt parser for math transforms
 */


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
package org.geotools.referencing.operation;

// Text parsing and formating
import java.text.ParsePosition;
import java.text.ParseException;
import java.util.StringTokenizer;

// Input/output
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

// Collections
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Collections;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.AssertionFailedError;

// Geotools dependencies
import org.geotools.referencing.BasicTest;
import org.geotools.resources.Arguments;
import org.geotools.resources.TestData;
import org.geotools.referencing.FactoryFinder;
import org.geotools.geometry.GeneralDirectPosition;

// OpenGIS dependencies
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.FactoryException;

/**
 * Run the suite of OpenGIS tests. A text file ({@link #OPENGIS_SCRIPT}) is provided.
 * This file contains a list of source and target coordinates systems (in WKT), source
 * coordinate points and expected coordinate points after the transformation from
 * source CS to target CS. Running this test really test all the following classes:
 *
 * <ul>
 *   <li>{@link CoordinateSystemFactory} (especially the WKT parser)</li>
 *   <li>{@link CoordinateSystemAuthorityFactory} (especially the implementation for EPSG codes)</li>
 *   <li>{@link CoordinateTransformationFactory}</li>
 *   <li>Many {@link MathTransform} implementations.</li>
 * </ul>
 *
 * This is probably the most important test case for the whole CTS module.
 *
 * @version $Id$
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class ScriptTest extends TestCase {
    /**
     * A simple test file to parse and execute.
     */
    private static final String SIMPLE_SCRIPT = "Simple_TestScript.txt";
    
    /**
     * A test file to parse and execute for stereographic projection.
     */
    private static final String MERCATOR_SCRIPT = "Mercator_TestScript.txt";
    
    /**
     * A test file to parse and execute for stereographic projection.
     */
    private static final String TRANSVERSE_MERCATOR_SCRIPT = "TransverseMercator_TestScript.txt";
    
    /**
     * A test file to parse and execute for stereographic projection.
     */
    private static final String STEREOGRAPHIC_SCRIPT = "Stereographic_TestScript.txt";
    
    /**
     * A test file to parse and execute for stereographic projection.
     */
    private static final String ORTHOGRAPHIC_SCRIPT = "Orthographic_TestScript.txt";
    
    /**
     * A test file to parse and execute for Albers equals area projection.
     */
    private static final String ALBERS_SCRIPT = "AlbersEqualArea_TestScript.txt";
    
    /**
     * A test file to parse and execute for Lambert conic projection.
     */
    private static final String LAMBERT_SCRIPT = "LambertConic_TestScript.txt";
    
    /**
     * The OpenGIS test file to parse and execute.
     */
    private static final String OPENGIS_SCRIPT = "OpenGIS_TestScript.txt";
    
    /**
     * A test file to parse and execute for projections as math transforms.
     */
    private static final String MT_PROJ_SCRIPT = "MT_Projection_TestScript.txt";
    
    /**
     * A test file to parse and execute for Abridged Molodensky math transforms.
     */
    private static final String MT_ABRIDGED_MOL_SCRIPT = "MT_AbridgedMolodensky_TestScript.txt";
    
    /**
     * A test file to parse and execute for Molodensky math transforms.
     */
    private static final String MT_MOLODENSKY_SCRIPT = "MT_Molodensky_TestScript.txt";
 
    /**
     * The coordinate system factory to use for the test.
     * This is also the class used for parsing WKT texts.
     */
    private CRSFactory crsFactory;
    
    /**
     * The coordinate transformation factory to use for the test.
     */
    private CoordinateOperationFactory coFactory;
    
    /**
     * The math transformation factory to use for the test.
     */
    private MathTransformFactory mtFactory;
    
    /**
     * The list of object defined in the {@link #OPENGIS_SCRIPT} file. Keys are
     * {@link String} objects, while values are {@link CoordinateSystem} or
     * {@link MathTransform} objects.
     */
    private Map definitions;
    
    /**
     * Source and target coordinate systems for the test currently executed.
     * Those fields are updated many times by {@link #runInstruction}.
     */
    private CoordinateReferenceSystem sourceCRS, targetCRS;
    
    /** 
     * The Math Transform for the current test. This is only used for tests 
     * that do not use a source and target coordinate reference system.
     * This field is updated many times by {@link #runInstruction}.
     */
    private MathTransform transform;
    
    /*
     * <code>true</code> if tests are using a math transform instead of a 
     * source and target coordinate reference system. This field is updated 
     * many times by {@link #runInstruction}.
     */
    private boolean usingMathTransform = false;
    
    /**
     * Source and target coordinate points for the test currently executed.
     * Those fields are updated many times by {@link #runInstruction}.
     */
    private DirectPosition sourcePosition, targetPosition;

    /**
     * Tolerance numbers for the test currently executed.
     * Thise field is updated many times by {@link #runInstruction}.
     */
    private double[] tolerance;
    
    /**
     * Number of test run and passed. Used for displaying
     * a report after once the test is finished.
     */
    private int testRun, testPassed;
    
    /**
     * If non-null display error messages to this writer instead of throwing
     * {@link AssertionFailedError}. This is used for debugging only.
     */
    private PrintWriter out;
    
    /**
     * Constructs a test case with the given name.
     */
    public ScriptTest(final String name) {
        super(name);
    }
    
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScriptTest.class);
    }
    
    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        crsFactory    = FactoryFinder.getCRSFactory();
//        coFactory    = FactoryFinder.getCoordinateOperationFactory();
        mtFactory    = FactoryFinder.getMathTransformFactory();
        definitions  = new HashMap();
        if(out == null){
            out = new PrintWriter(System.out);
        }
    }
    
    /**
     * Check if two coordinate points are equals, in the limits of the specified
     * tolerance vector.
     *
     * @param expected  The expected coordinate point.
     * @param actual    The actual coordinate point.
     * @param tolerance The tolerance vector. If this vector length is smaller than the number
     *                  of dimension of <code>actual</code>, then the last tolerance value will
     *                  be reused for all extra dimensions.
     * @throws AssertionFailedError If the actual point is not equals to the expected point.
     */
    private static void assertEquals(final DirectPosition expected,
        final DirectPosition actual,
        final double[]        tolerance)
    throws AssertionFailedError {
        final int dimension = actual.getDimension();
        final int lastToleranceIndex = tolerance.length-1;
        assertEquals("The coordinate point doesn't have the expected dimension",
        expected.getDimension(), dimension);
        for (int i=0; i<dimension; i++) {
            assertEquals("Mismatch for ordinate "+i+" (zero-based):",
            expected.getOrdinate(i), actual.getOrdinate(i),
            tolerance[Math.min(i, lastToleranceIndex)]);
        }
    }
    
    /**
     * Returns a coordinate system for the specified name. The coordinate system
     * must has been previously defined with a call to {@link #addDefinition}.
     */
    private CoordinateReferenceSystem getCoordinateReferenceSystem(final String name) throws FactoryException {
        final Object crs = definitions.get(name);
        if (crs instanceof CoordinateReferenceSystem) {
            return (CoordinateReferenceSystem) crs;
        }
        throw new FactoryException("No coordinate reference system defined for \""+name+"\".");
    }
    
    /**
     * Returns a math transform for the specified name. The math transform
     * must has been previously defined with a call to {@link #addDefinition}.
     */
    private MathTransform getMathTransform(final String name) throws FactoryException {
        final Object mt = definitions.get(name);
        if (mt instanceof MathTransform) {
            return (MathTransform) mt;
        }
        throw new FactoryException("No math transform defined for \""+name+"\".");
    }
    
    /**
     * Parse a vector of values. Vectors are used for coordinate points.
     * Example:
     * <pre>
     * (46.69439222, 13.91405611, 41.21)
     * </pre>
     *
     * @param  text The vector to parse.
     * @return The vector as floating point numbers.
     * @throws NumberFormatException if a number can't be parsed.
     */
    private static double[] parseVector(String text) throws NumberFormatException {
        text = removeDelimitors(text, '(', ')');
        final StringTokenizer st = new StringTokenizer(text, ",");
        final double[]    values = new double[st.countTokens()];
        for (int i=0; i<values.length; i++) {
            values[i] = Double.parseDouble(st.nextToken());
        }
        return values;
    }
    
    /**
     * Check if the specified string start and end with the specified delimitors,
     * and returns the string without the delimitors.
     *
     * @param text  The string to check.
     * @param start The delimitor required at the string begining.
     * @param end   The delimitor required at the string end.
     */
    private static String removeDelimitors(String text, final char start, final char end) {
        text = text.trim();
        final int endPos = text.length()-1;
        if (endPos >= 1) {
            if (text.charAt(0)==start && text.charAt(endPos)==end) {
                text = text.substring(1, endPos).trim();
            }
        }
        return text;
    }
    
    /**
     * If the specified string start with <code>"set"</code>, then add its
     * value to the {@link #definitions} map and returns <code>true</code>.
     * Otherwise, returns <code>false</code>.
     *
     * @param  text The string to parse.
     * @return <code>true</code> if it was a definition string,
     *         or <code>false</code> otherwise.
     * @throws FactoryException if the string can't be parsed.
     */
    private boolean addDefinition(String text) throws FactoryException {
        /*
         * List of keywords processed in a special ways by this method.
         */
        final String SET       = "set";
        final String PARAM_MT  = "PARAM_MT";
        final String COMPD_CRS  = "COMPD_CS";
        final String FITTED_CRS = "FITTED_CS";
        /*
         * If the string is in the form "set name = value",
         * then separate the name and the value parts.
         */
        if (!text.regionMatches(true, 0, SET, 0, SET.length())) {
            return false;
        }
        text = text.substring(SET.length());
        StringTokenizer st = new StringTokenizer(text, "=");
        if (st.countTokens() != 2) {
            throw new FactoryException("String must be in the form \"name = value\".");
        }
        String name  = st.nextToken().trim();
        String value = st.nextToken().trim();
        final Object crs;
        /*
         * Checks if the value is a Compound Coordinate Reference System.
         * Syntax: COMPD_CS["name", cs1name, cs2name]
         */
        if (value.regionMatches(true, 0, COMPD_CRS, 0, COMPD_CRS.length())) {
            value = removeDelimitors(value.substring(COMPD_CRS.length()), '[', ']');
            st = new StringTokenizer(value, ",");
            if (st.countTokens() != 3) {
                throw new FactoryException("COMPD_CS must be in the form "+
                "COMPD_CS[\"name\", crs1name, crs2name]");
            }
            String crsname = removeDelimitors(st.nextToken(), '"', '"');
            CoordinateReferenceSystem head = getCoordinateReferenceSystem(st.nextToken().trim());
            CoordinateReferenceSystem tail = getCoordinateReferenceSystem(st.nextToken().trim());
            crs = crsFactory.createCompoundCRS(Collections.singletonMap("name", crsname), new CoordinateReferenceSystem[] {head, tail});
        }   
        else if (value.regionMatches(true, 0, FITTED_CRS, 0, FITTED_CRS.length())) {
            System.out.println("FITTED_CS not yet implemented.");
            return true;
        }
        else if (value.regionMatches(true, 0, PARAM_MT, 0, PARAM_MT.length())) {
            crs = mtFactory.createFromWKT(value);
            if (true) {
                assertEquals("MathTransform.equals(...) failed", crs, crs);
//                final MathTransform check = mtFactory.createFromWKT(crs.toString());
//                assertEquals("WKT formating produces a different result.", check, crs);
            }
        }
        else {
            crs = crsFactory.createFromWKT(value);
            if (true) {
                assertEquals("CoordinateReferenceSystem.equals(...) failed", crs, crs);
                final CoordinateReferenceSystem check = crsFactory.createFromWKT(crs.toString());
                assertEquals("WKT formating produces a different result.", check, crs);
            }
        }
        if (definitions.put(name, crs) != null) {
            throw new FactoryException("A value is already defined for \""+name+"\".");
        }
        return true;
    }
    
    /**
     * Run an instruction. Instruction may be any of the following lines
     * (values listed here are just examples):
     * <pre>
     *   cs_source      = _Wgs84NE_
     *   cs_target      = _Wgs84SW_
     *   test_tolerance = 1e-6
     *   pt_source      = (1, 2)
     *   pt_target      = (-1, -2)
     * </pre>
     *
     * or
     * 
     * <pre>
     *   math_transform = _mt_merc1_
     *   test_tolerance = 1e-6
     *   pt_source      = (1, 2)
     *   pt_target      = (-1, -2)
     * </pre>
     *
     * The "<code>pt_target</code>" instruction triggers the computation.
     * <br><br>
     *
     * @param  text The instruction to parse.
     * @param  lineNumber The line number, for error output.
     * @throws FactoryException if the instruction can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    private void runInstruction(final String text, final int lineNumber)
    throws FactoryException, TransformException {
        final StringTokenizer st = new StringTokenizer(text, "=");
        if (st.countTokens() != 2) {
            throw new FactoryException("Illegal instruction: "+text);
        }
        final String name  = st.nextToken().trim().toLowerCase();
        final String value = st.nextToken().trim();
        if (name.equals("cs_source")) {
            sourceCRS = getCoordinateReferenceSystem(value);
            usingMathTransform = false;
            return;
        }
        if (name.equals("cs_target")) {
            targetCRS = getCoordinateReferenceSystem(value);
            usingMathTransform = false;
            return;
        }
        if (name.equals("math_transform")) {
            transform = getMathTransform(value);
            usingMathTransform = true;
            return;
        }
        if (name.equals("test_tolerance")) {
            tolerance = parseVector(value);
            return;
        }
        if (name.equals("pt_source")) {
            sourcePosition = new GeneralDirectPosition(parseVector(value));
            return;
        }
        if (!name.equals("pt_target")) {
            throw new FactoryException("Unknown instruction: "+name);
        }
        targetPosition = new GeneralDirectPosition(parseVector(value));
        /*
         * The "pt_target" instruction triggers the test.
         */
        DirectPosition    computed = null;
        CoordinateOperation op = null;
        try {
            testRun++;
            if (usingMathTransform) {
                computed = transform.transform(sourcePosition, computed);
            } else {
//                op = coFactory.createOperation(sourceCRS, targetCRS);
//                computed = op.getMathTransform().transform(sourcePosition, computed);
            }
            assertEquals(targetPosition, computed, tolerance);
            testPassed++;
        } catch (TransformException exception) {
            if (out == null) {
                throw exception;
            }
            out.print("----TRANSFORMATION FAILED AT LINE ");
            out.print(String.valueOf(lineNumber));
            out.println("-------------------------------------------------------");
            out.println(exception);
            out.println();
        } catch (AssertionFailedError error) {
            if (out == null) {
                throw error;
            }
            out.print("----TEST FAILED AT LINE ");
            out.print(String.valueOf(lineNumber));
            out.println("-------------------------------------------------------");
            if (usingMathTransform) {
                out.println("math_transform : " + transform);
            } else {
                out.println("cs_source : " + sourceCRS);
                out.println("cs_target : " + targetCRS);
            }
            out.println("pt_source = " + sourcePosition);
            out.println("pt_target = " + targetPosition);
            out.println("computed  = " + computed);
            out.println();
        } catch (AssertionError error) {
            out.print("----AssertionError AT LINE ");
            out.print(String.valueOf(lineNumber) + "\n");
            error.printStackTrace();
        }
    }
    
    /**
     * Run the specified script.
     *
     * @throws IOException If the script can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    private void runScript(final String script) throws IOException, FactoryException {
        definitions.clear();
        testRun    = 0;
        testPassed = 0;
        final LineNumberReader reader;
        if (true) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(script);
            if (in == null) {
                // Then we are being run by maven
                Reader scriptReader = TestData.getReader( BasicTest.class, script);
                if( scriptReader == null ){
                	throw new FileNotFoundException("Could not locate test-data "+script );
                }
                reader = new LineNumberReader( scriptReader );
            }
            else{
                reader = new LineNumberReader(new InputStreamReader(in));
            }
        }
        String line;
        while ((line=reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                // Ignore empty lines.
                continue;
            }
            if (line.startsWith("//")) {
                // Ignore comment lines.
                continue;
            }
            if (addDefinition(line)) {
                // Definition line are processed by 'addDefinition'.
                continue;
            }
            try {
                runInstruction(line, reader.getLineNumber());
            } catch (TransformException exception) {
                // TODO: We should throw the TransformException instead,
                //       but Maven doesn't seem to like it.
                throw new AssertionError(exception);
            }
        }
        reader.close();
        if (out != null) {
            out.print("Test run    : "); out.println(testRun);
            out.print("Test passed : "); out.println(testPassed);
            out.print("Success rate: "); out.print((int) (100*testPassed/testRun));
            out.println('%');
            out.flush();
        }
    }
    
    /**
     * Run the {@link #SIMPLE_SCRIPT}.
     *
     * @throws IOException If {@link #SIMPLE_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testSimple() throws IOException, FactoryException {
//        runScript(SIMPLE_SCRIPT);
//    }
    
    /**
     * Run the {@link #MERCATOR_SCRIPT}.
     *
     * @throws IOException If {@link #MERCATOR_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testMercator() throws IOException, FactoryException {
//        runScript(MERCATOR_SCRIPT);
//    }
    
    /**
     * Run the {@link #TRANSVERSE_MERCATOR_SCRIPT}.
     *
     * @throws IOException If {@link #TRANSVERSE_MERCATOR_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testTransverseMercator() throws IOException, FactoryException {
//        runScript(TRANSVERSE_MERCATOR_SCRIPT);
//    }   
    
    /**
     * Run the {@link #STEREOGRAPHIC_SCRIPT}.
     *
     * @throws IOException If {@link #STEREOGRAPHIC_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testStereographic() throws IOException, FactoryException {
//        runScript(STEREOGRAPHIC_SCRIPT);
//    }
    
    /**
     * Run the {@link #ORTHOGRAPHIC_SCRIPT}.
     *
     * @throws IOException If {@link #ORTHOGRAPHIC_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testOrthographic() throws IOException, FactoryException {
//        runScript(ORTHOGRAPHIC_SCRIPT);
//    }
    
    /**
     * Run the {@link #ALBERS_SCRIPT}.
     *
     * @throws IOException If {@link #ALBERS_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testAlbersEqualArea() throws IOException, FactoryException {
//        runScript(ALBERS_SCRIPT);
//    }
    
    /**
     * Run the {@link #LAMBERT_SCRIPT}.
     *
     * @throws IOException If {@link #LAMBERT_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testLambertConic() throws IOException, FactoryException {
//        runScript(LAMBERT_SCRIPT);
//    }
    
    /**
     * Run the {@link #OPENGIS_SCRIPT}.
     *
     * @throws IOException If {@link #OPENGIS_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testOpenGIS() throws IOException, FactoryException {
//        runScript(OPENGIS_SCRIPT);
//    }
    
    /**
     * Run the {@link #MT_PROJ_SCRIPT}.
     *
     * @throws IOException If {@link #MT_PROJ_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testMTproj() throws IOException, FactoryException {
//        runScript(MT_PROJ_SCRIPT);
//    }
    
    /**
     * Run the {@link #MT_ABRIDGED_MOL_SCRIPT}.
     *
     * @throws IOException If {@link #MT_ABRIDGED_MOL_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    public void testMTAbridgedMol() throws IOException, FactoryException {
        runScript(MT_ABRIDGED_MOL_SCRIPT);
    }
    
    /**
     * Run the {@link #MT_MOLODENSKY_SCRIPT}.
     *
     * @throws IOException If {@link #MT_MOLODENSKY_SCRIPT} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
//    public void testMTMolodesky() throws IOException, FactoryException {
//        runScript(MT_MOLODENSKY_SCRIPT);
//    }
    
    /**
     * Run the test from the command line. By default, this method run all tests. In order
     * to run only one test, use one of the following line:
     * <ul>
     *     <li>java -ea org.geotools.cs.ScriptTest -test=Simple</li>
     *     <li>java -ea org.geotools.cs.ScriptTest -test=Stereographic</li>
     *     <li>java -ea org.geotools.cs.ScriptTest -test=Orthographic</li>
     *     <li>java -ea org.geotools.cs.ScriptTest -test=AlbersEqualArea</li>
     *     <li>java -ea org.geotools.cs.ScriptTest -test=OpenGIS</li>
     * </ul>
     *
     * @param  args The command-line arguments.
     * @throws Exception if a test failed.
     */
    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        final String script = arguments.getOptionalString("-test");
        arguments.getRemainingArguments(0);
        final ScriptTest test = new ScriptTest(null);
        boolean done = false;
        test.out = arguments.out;
        test.setUp();
        if (script==null || script.equalsIgnoreCase("Simple")) {
//            test.testSimple();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("Mercator")) {
//            test.testMercator();
            done = true;
        } 
        if (script==null || script.equalsIgnoreCase("TransverseMercator")) {
//            test.testTransverseMercator();
            done = true;
        } 
        if (script==null || script.equalsIgnoreCase("Stereographic")) {
//            test.testStereographic();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("Orthographic")) {
//            test.testOrthographic();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("AlbersEqualArea")) {
//            test.testAlbersEqualArea();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("LambertConic")) {
//            test.testLambertConic();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("OpenGIS")) {
//            test.testOpenGIS();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("MT_Projection")) {
//            test.testMTproj();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("MT_AbridgedMolodensky")) {
            test.testMTAbridgedMol();
            done = true;
        }
        if (script==null || script.equalsIgnoreCase("MT_Molodensky")) {
//            test.testMTMolodesky();
            done = true;
        }
        if (script!=null && !done) {
            test.runScript(script+"_TestScript.txt");
        }
        test.tearDown();
    }
}
