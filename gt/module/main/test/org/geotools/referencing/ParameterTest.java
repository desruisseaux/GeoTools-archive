/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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

import java.util.*;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;
import junit.framework.*;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.parameter.InvalidParameterValueException;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.parameter.*;


/**
 * Tests the <code>org.geotools.parameter</code> package.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ParameterTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ParameterTest.class);
    }

    /**
     * Construct a test case.
     */
    public ParameterTest(String testName) {
        super(testName);
    }

    /**
     * Tests integer and floating points values in a wide range of values. Some on those
     * values are cached (e.g. 0, 90, 360) because frequently used. It should be transparent
     * to the user. Test also unit conversions (degrees to radians in this case).
     */
    public void testSequence() {
        for (int i=-1000; i<=1000; i++) {
            assertEquals("Integer", i, new ParameterValue("Integer", i)          .intValue());
            assertEquals("Double",  i, new ParameterValue("Double",  i, null    ).doubleValue(), 0.0);
            assertEquals("Double",  i, new ParameterValue("Double",  i, Unit.ONE).doubleValue(), 0.0);
            assertEquals("Double",  Math.toRadians(i),
                         new ParameterValue("Double",  i, NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN), 1E-6);
        }
    }

    /**
     * Creates a parameter bounded by some range of integer numbers, and tests values
     * inside and outside this range. Tests also the uses of values of the wrong type.
     */
    public void testRangeIntegers() {
        ParameterValue param;
        param = new ParameterValue(new OperationParameter("Range", 15, -30, +40));
        assertEquals("Range", 15, param.intValue());
        assertEquals("Range", 15, param.doubleValue(), 0.0);
        param.setValue(12);
        assertEquals("Range", 12, param.intValue());
        assertEquals("Range", 12, param.doubleValue(), 0.0);
        try {
            param.setValue(50);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(-40);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(10.0);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        assertEquals("Equals", param, param.clone());
    }

    /**
     * Creates a parameter bounded by some range of floating point numbers, and tests values
     * inside and outside this range. Tests also the uses of values of the wrong types.
     */
    public void testRangeDoubles() {
        ParameterValue param;
        param = new ParameterValue(new OperationParameter("Range", 15.0, -30.0, +40.0, null));
        assertEquals("Range", 15, param.intValue());
        assertEquals("Range", 15, param.doubleValue(), 0.0);
        param.setValue(12.0);
        assertEquals("Range", 12, param.intValue());
        assertEquals("Range", 12, param.doubleValue(), 0.0);
        try {
            param.setValue(50.0);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(-40.0);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(10);
            fail("Range");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        assertEquals("Equals", param, param.clone());
    }

    /**
     * Test parameter values group.
     */
    public void testGroup() {
        final Integer ONE = new Integer(1);
        final OperationParameter p1, p2, p3;
        p1 = new OperationParameter(Collections.singletonMap("name", "1"), 1, 1, Integer.class, null, ONE, null, null, null);
        p2 = new OperationParameter(Collections.singletonMap("name", "2"), 1, 2, Integer.class, null, ONE, null, null, null);
        p3 = new OperationParameter(Collections.singletonMap("name", "3"), 0, 1, Integer.class, null, ONE, null, null, null);
        final ParameterValue v1, v2, v3, v1b, v2b, v3b, v1t, v2t, v3t;
        v1  = new ParameterValue(p1); v1.setValue( 10);
        v2  = new ParameterValue(p2); v2.setValue( 20);
        v3  = new ParameterValue(p3); v3.setValue( 30);
        v1b = new ParameterValue(p1); v1.setValue(-10);
        v2b = new ParameterValue(p2); v2.setValue(-20);
        v3b = new ParameterValue(p3); v3.setValue(-30);
        /*
         * Test creation without pre-defined parameter group.
         */
        final Map properties = Collections.singletonMap("name", "group");
        new ParameterValueGroup(properties, new ParameterValue[] {v1, v2, v3});
        new ParameterValueGroup(properties, new ParameterValue[] {v1, v2});
        new ParameterValueGroup(properties, new ParameterValue[] {v1, v3});
        new ParameterValueGroup(properties, new ParameterValue[] {v1, v2, v3, v2b});
        try {
            new ParameterValueGroup(properties, new ParameterValue[] {v1, v2, v3, v3b});
            fail("Parameter 3 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        try {
            new ParameterValueGroup(properties, new ParameterValue[] {v1, v3, v1b});
            fail("Parameter 1 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        /*
         * Test creation with a pre-defined parameter group.
         */
        final OperationParameterGroup group =
              new OperationParameterGroup(properties, new OperationParameter[] {p1, p2, p3});
        new ParameterValueGroup(group, new ParameterValue[] {v1, v2, v3});
        new ParameterValueGroup(group, new ParameterValue[] {v1, v2});
        new ParameterValueGroup(group, new ParameterValue[] {v1, v2, v3, v2b});
        try {
            new ParameterValueGroup(group, new ParameterValue[] {v1, v3});
            fail("Parameter 2 was mandatory.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        try {
            new ParameterValueGroup(group, new ParameterValue[] {v1, v2, v3, v3b});
            fail("Parameter 3 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        try {
            new ParameterValueGroup(group, new ParameterValue[] {v1, v3, v1b});
            fail("Parameter 1 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
    }

    /**
     * Tests parameter for a code list. Try to inserts invalid values. Try also to insert a
     * new code list. This operation should fails if the new code list is created after the
     * parameter.
     */
    public void testCodeList() {
        ParameterValue param = new ParameterValue("Test", AxisDirection.TOP);
        OperationParameter op = (OperationParameter) param.getDescriptor();
        assertEquals("CodeList", new HashSet(Arrays.asList(AxisDirection.values())), op.getValidValues());
        assertEquals("Default", AxisDirection.TOP, op.getDefaultValue());
        param.setValue(AxisDirection.DOWN);
        try {
            param.setValue(VerticalDatumType.ELLIPSOIDAL);
            fail("Ellipsoidal");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        AxisDirection dummy = new AxisDirection("Dummy");
        try {
            param.setValue(dummy);
            fail("Dummy");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        param = new ParameterValue("Test", AxisDirection.TOP);
        param.setValue(dummy); // Should not fails.
        assertEquals("Equals", param, param.clone());
    }

    /**
     * Tests the storage of matrix parameters.
     */
    public void textMatrix() {
        final int size = 8;
        final Random random = new Random(47821365);
        final GeneralMatrix matrix = new GeneralMatrix(size);
        for (int j=0; j<size; j++) {
            for (int i=0; i<size; i++) {
                matrix.setElement(j, i, 200*random.nextDouble()-100);
            }
        }
        MatrixParameters descriptor = new MatrixParameters(Collections.singletonMap("name", "Test"));
        for (int height=2; height<=size; height++) {
            for (int width=2; width<=size; width++) {
                MatrixParameterValues parameters = (MatrixParameterValues) descriptor.createValue();
                GeneralMatrix copy = (GeneralMatrix) matrix.clone();
                copy.setSize(height, width);
                parameters.setMatrix(copy);
                assertEquals("height", height, ((ParameterValue) parameters.getValue("num_row")).intValue());
                assertEquals("width",  width,  ((ParameterValue) parameters.getValue("num_col")).intValue());
                assertEquals("equals", copy,   parameters.getMatrix());
                assertEquals("Equals", parameters, parameters.clone());
            }        
        }
    }
}
