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

// J2SE dependencies and extensions
import java.io.*;
import java.util.*;
import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;
import junit.framework.*;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.parameter.*;
import org.geotools.referencing.operation.GeneralMatrix;


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
     * Tests integer and floating point values in a wide range of values. Some on those
     * values are cached (e.g. 0, 90, 360) because frequently used. It should be transparent
     * to the user. Test also unit conversions (degrees to radians in this case).
     */
    public void testSequence() {
        for (int i=-1000; i<=1000; i++) {
            assertEquals("Integer", i, new Parameter("Integer", i          ).intValue());
            assertEquals("Double",  i, new Parameter("Double",  i, null    ).doubleValue(), 0.0);
            assertEquals("Double",  i, new Parameter("Double",  i, Unit.ONE).doubleValue(), 0.0);
            assertEquals("Double",  Math.toRadians(i),
                new Parameter("Double",  i, NonSI.DEGREE_ANGLE).doubleValue(SI.RADIAN), 1E-6);
        }
    }

    /**
     * Creates a parameter bounded by some range of integer numbers, and tests values
     * inside and outside this range. Tests also the uses of values of the wrong type.
     */
    public void testRangeIntegers() {
        Parameter param;
        param = new Parameter(new ParameterDescriptor("Range", 15, -30, +40));
        assertEquals(   "intValue", 15, param.intValue());
        assertEquals("doubleValue", 15, param.doubleValue(), 0.0);
        param.setValue(12);
        assertEquals(   "intValue", 12, param.intValue());
        assertEquals("doubleValue", 12, param.doubleValue(), 0.0);
        try {
            param.setValue(50);
            fail("setValue(> max)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(-40);
            fail("setValue(< min)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(10.0);
            fail("setValue(double)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        assertEquals("equals", param, param.clone());
    }

    /**
     * Creates a parameter bounded by some range of floating point numbers, and tests values
     * inside and outside this range. Tests also the uses of values of the wrong types.
     */
    public void testRangeDoubles() {
        Parameter param;
        param = new Parameter(new ParameterDescriptor("Range", 15.0, -30.0, +40.0, null));
        assertEquals(   "intValue", 15, param.intValue());
        assertEquals("doubleValue", 15, param.doubleValue(), 0.0);
        param.setValue(12.0);
        assertEquals(   "intValue", 12, param.intValue());
        assertEquals("doubleValue", 12, param.doubleValue(), 0.0);
        try {
            param.setValue(50.0);
            fail("setValue(> max)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue(-40.0);
            fail("setValue(< min)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        try {
            param.setValue("12");
            fail("setValue(String)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        assertEquals("equals", param, param.clone());
    }

    /**
     * Tests parameter for a code list. Try to inserts invalid values. Try also to insert a
     * new code list. This operation should fails if the new code list is created after the
     * parameter.
     */
    public void testCodeList() {
        Parameter param = new Parameter("Test", AxisDirection.DISPLAY_UP);
        ParameterDescriptor op = (ParameterDescriptor) param.getDescriptor();
        assertEquals("equals", new HashSet(Arrays.asList(AxisDirection.values())), op.getValidValues());
        assertNull("defaultValue", op.getDefaultValue());
        param.setValue(AxisDirection.DOWN);
        try {
            param.setValue(VerticalDatumType.ELLIPSOIDAL);
            fail("setValue(VerticalDatumType)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        AxisDirection dummy = new AxisDirection("Dummy");
        try {
            param.setValue(dummy);
            fail("setValue(AxisDirection)");
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception.
        }
        param = new Parameter("Test", AxisDirection.DISPLAY_UP);
        param.setValue(dummy); // Should not fails.
        assertEquals("equals", param, param.clone());
    }

    /**
     * Test {@link ParameterDescriptor} construction.
     */
    public void testParameterDescriptor() {
        ParameterDescriptor descriptor;
        Parameter           parameter;

        descriptor = new ParameterDescriptor("Test", 12, 4, 20, SI.METER);
        parameter  = (Parameter) descriptor.createValue();
        assertEquals("name",         "Test",          descriptor.getName().getCode());
        assertEquals("unit",         SI.METER,        descriptor.getUnit());
        assertEquals("class",        Double.class,    descriptor.getValueClass());
        assertEquals("defaultValue", new Double(12),  descriptor.getDefaultValue());
        assertEquals("minimum",      new Double( 4),  descriptor.getMinimumValue());
        assertEquals("maximum",      new Double(20),  descriptor.getMaximumValue());
        assertEquals("value",        12,               parameter.intValue());
        assertEquals("unit",         SI.METER,         parameter.getUnit());
        for (int i=4; i<=20; i++) {
            parameter.setValue(i);
            assertEquals("value", new Double(i), parameter.getValue());
            assertEquals("unit",  SI.METER,      parameter.getUnit());
            assertEquals("value", i,             parameter.doubleValue(SI.METER), 0);
        }
        try {
            parameter.setValue(3.0);
            fail("setValue(< min)");
        } catch (InvalidParameterValueException e) {
            // This is the expected exception.
        }
        try {
            parameter.setValue("12");
            fail("setValue(Sring)");
        } catch (InvalidParameterValueException e) {
            // This is the expected exception.
        }
        for (int i=400; i<=2000; i+=100) {
            parameter.setValue(i, SI.CENTI(SI.METER));
            assertEquals("value", new Double(i),      parameter.getValue());
            assertEquals("unit",  SI.CENTI(SI.METER), parameter.getUnit());
            assertEquals("value", i/100,              parameter.doubleValue(SI.METER), 0);
        }
        try {
            descriptor = new ParameterDescriptor("Test", 3, 4, 20);
            fail("setValue(< min)");
        } catch (InvalidParameterValueException e) {
            // This is the expected exception.
        }
        try {
            descriptor = new ParameterDescriptor("Test", 12, 20, 4);
            fail("ParameterDescriptor(min > max)");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
    }

    /**
     * Test {@link Parameter} construction.
     */
    public void testParameterValue() throws IOException, ClassNotFoundException {
        Parameter           parameter;
        ParameterDescriptor descriptor;
        Set                 validValues;

        parameter  = new Parameter("Test", 14);
        descriptor = (ParameterDescriptor) parameter.getDescriptor();
        assertNull  ("unit",                          parameter.getUnit());
        assertEquals("intValue",     14,              parameter.intValue());
        assertEquals("doubleValue",  14,              parameter.doubleValue(), 0);
        assertEquals("type",         Integer.class,  descriptor.getValueClass());
        assertEquals("name",         "Test",         descriptor.getName().getCode());
        assertEquals("defaultValue", new Integer(0), descriptor.getDefaultValue());
        assertNull  ("minimum",                      descriptor.getMinimumValue());
        assertNull  ("maximum",                      descriptor.getMaximumValue());
        assertNull  ("unit",                         descriptor.getUnit());
        assertNull  ("validValues",                  descriptor.getValidValues());
        try {
            parameter.doubleValue(SI.METER);
            fail("doubleValue(METER)");
        } catch (IllegalStateException e) {
            // This is the expected exception.
        }
        try {
            parameter.stringValue();
            fail("stringValue()");
        } catch (InvalidParameterTypeException e) {
            // This is the expected exception.
        }
        serialize(parameter);

        parameter  = new Parameter("Test", 3, SI.METER);
        descriptor = (ParameterDescriptor)       parameter.getDescriptor();
        assertEquals("intValue",       3,        parameter.intValue());
        assertEquals("doubleValue",    3,        parameter.doubleValue(), 0);
        assertEquals("doubleValue",  300,        parameter.doubleValue(SI.CENTI(SI.METER)), 0);
        assertEquals("name",         "Test",    descriptor.getName().getCode());
        assertEquals("unit",         SI.METER,  descriptor.getUnit());
        assertNull  ("defaultValue",            descriptor.getDefaultValue());
        assertNull  ("minimum",                 descriptor.getMinimumValue());
        assertNull  ("maximum",                 descriptor.getMaximumValue());
        assertNull  ("validValues",             descriptor.getValidValues());
        try {
            parameter.stringValue();
            fail("stringValue()");
        } catch (InvalidParameterTypeException e) {
            // This is the expected exception.
        }
        serialize(parameter);

        parameter   = new Parameter("Test", AxisDirection.NORTH);
        descriptor  = (ParameterDescriptor) parameter.getDescriptor();
        validValues = descriptor.getValidValues();
        assertEquals("value",  AxisDirection.NORTH, parameter.getValue());
        assertEquals("name",   "Test",             descriptor.getName().getCode());
        assertNull  ("unit",                       descriptor.getUnit());
        assertNull  ("defaultValue",               descriptor.getDefaultValue());
        assertNull  ("minimum",                    descriptor.getMinimumValue());
        assertNull  ("maximum",                    descriptor.getMaximumValue());
        assertTrue  ("validValues", validValues.contains(AxisDirection.NORTH));
        assertTrue  ("validValues", validValues.contains(AxisDirection.SOUTH));
        assertTrue  ("validValues", validValues.contains(AxisDirection.DISPLAY_LEFT));
        assertTrue  ("validValues", validValues.contains(AxisDirection.PAST));
        assertEquals("validValues", new HashSet(Arrays.asList(AxisDirection.values())), validValues);
        try {
            parameter.doubleValue();
            fail("doubleValue()");
        } catch (InvalidParameterTypeException e) {
            // This is the expected exception.
        }
        serialize(parameter);
    }

    /**
     * Test parameter values group.
     */
    public void testGroup() {
        final Integer ONE = new Integer(1);
        final ParameterDescriptor p1, p2, p3;
        p1 = new ParameterDescriptor(Collections.singletonMap("name", "1"), true,  Integer.class, null, ONE, null, null, null);
        p2 = new ParameterDescriptor(Collections.singletonMap("name", "2"), true,  Integer.class, null, ONE, null, null, null);
        p3 = new ParameterDescriptor(Collections.singletonMap("name", "3"), false, Integer.class, null, ONE, null, null, null);

        final Parameter v1, v2, v3, v1b, v2b, v3b, v1t, v2t, v3t;
        v1  = new Parameter(p1); v1 .setValue( 10);
        v2  = new Parameter(p2); v2 .setValue( 20);
        v3  = new Parameter(p3); v3 .setValue( 30);
        v1b = new Parameter(p1); v1b.setValue(-10);
        v2b = new Parameter(p2); v2b.setValue(-20);
        v3b = new Parameter(p3); v3b.setValue(-30);

        ParameterDescriptorGroup descriptor;
        ParameterGroup           group;
        Collection               content;
        Map                      properties;
        Parameter                automatic;

        /* --------------------------------------------- *
         * Case (v1, v2, v3) where:
         *    - v1   is mandatory
         *    - v2   is mandatory
         *    - v3   is optional
         * --------------------------------------------- */
        properties = Collections.singletonMap("name", "group");
        group      = new ParameterGroup(properties, new Parameter[] {v1, v2, v3});
        descriptor = (ParameterDescriptorGroup) group.getDescriptor();
        content    = descriptor.descriptors();
        assertEquals("name", "group", descriptor.getName().getCode());
        assertEquals("descriptors", 3, content.size());
        assertTrue  ("p1",  content.contains(p1));
        assertTrue  ("p2",  content.contains(p2));
        assertTrue  ("p3",  content.contains(p3));
        assertSame  ("p1",  p1, descriptor.descriptor("1"));
        assertSame  ("p2",  p2, descriptor.descriptor("2"));
        assertSame  ("p3",  p3, descriptor.descriptor("3"));

        // Checks default values
        content = group.values();
        assertEquals("values", 3, content.size());
        assertTrue  ("v1",  content.contains(v1 ));
        assertTrue  ("v2",  content.contains(v2 ));
        assertTrue  ("v3",  content.contains(v3 ));
        assertFalse ("v1b", content.contains(v1b));
        assertFalse ("v2b", content.contains(v2b));
        assertFalse ("v3b", content.contains(v3b));
        assertSame  ("v1",  v1, group.parameter("1"));
        assertSame  ("v2",  v2, group.parameter("2"));
        assertSame  ("v3",  v3, group.parameter("3"));
        assertEquals("v1",  10, group.parameter("1").intValue());
        assertEquals("v2",  20, group.parameter("2").intValue());
        assertEquals("v3",  30, group.parameter("3").intValue());

        // Tests the replacement of some values
        assertFalse("v1b", group.values().remove(v1b));
//        try {
//            assertTrue(group.values().remove(v1));
//            fail("v1 is a mandatory parameter; it should not be removeable.");
//        } catch (InvalidParameterCardinalityException e) {
//            // This is the expected exception.
//        }
        assertTrue  ("v1b", group.values().add(v1b));
        assertTrue  ("v2b", group.values().add(v2b));
        assertTrue  ("v3b", group.values().add(v3b));
        assertFalse ("v1b", group.values().add(v1b)); // Already present
        assertFalse ("v2b", group.values().add(v2b)); // Already present
        assertFalse ("v3b", group.values().add(v3b)); // Already present
        assertEquals("v1b", -10, group.parameter("1").intValue());
        assertEquals("v2b", -20, group.parameter("2").intValue());
        assertEquals("v3b", -30, group.parameter("3").intValue());
        assertEquals("values", 3, content.size());

        // Tests equality
        assertEquals("new", group, group=new ParameterGroup(descriptor, new Parameter[] {v1b, v2b, v3b}));

        /* --------------------------------------------- *
         * Case (v1, v2) where:
         *    - v1   is mandatory
         *    - v2   is mandatory
         *    - v3   is optional and initially omitted
         * --------------------------------------------- */
        group      = new ParameterGroup(descriptor, new Parameter[] {v1, v2});
        descriptor = (ParameterDescriptorGroup) group.getDescriptor();
        content    = group.values();
        automatic  = (Parameter) v3.getDescriptor().createValue(); // Remove cast with J2SE 1.5
        assertEquals   ("values", 2,  content.size());
        assertTrue     ("v1",         content.contains(v1 ));
        assertTrue     ("v2",         content.contains(v2 ));
        assertFalse    ("v3",         content.contains(v3 ));
        assertFalse    ("v1b",        content.contains(v1b));
        assertFalse    ("v2b",        content.contains(v2b));
        assertFalse    ("v3b",        content.contains(v3b));
        assertSame     ("v1",   v1,   group.parameter ("1"));
        assertSame     ("v2",   v2,   group.parameter ("2"));
        assertFalse    ("automatic",  content.contains(automatic));
        assertNotEquals("v3",   v3,   group.parameter ("3")); // Should have automatically created.
        assertTrue     ("automatic",  content.contains(automatic));

        // Tests the replacement of some values
        assertTrue  ("v1b", group.values().add(v1b));
        assertTrue  ("v2b", group.values().add(v2b));
        assertTrue  ("v3b", group.values().add(v3b));
        assertFalse ("v1b", group.values().add(v1b)); // Already present
        assertFalse ("v2b", group.values().add(v2b)); // Already present
        assertFalse ("v3b", group.values().add(v3b)); // Already present
        assertEquals("v1b", -10, group.parameter("1").intValue());
        assertEquals("v2b", -20, group.parameter("2").intValue());
        assertEquals("v3b", -30, group.parameter("3").intValue());
        assertEquals("values", 3, content.size());
if (true) return;

        group      = new ParameterGroup(descriptor, new Parameter[] {v1, v2, v3, v2b});
        descriptor = (ParameterDescriptorGroup) group.getDescriptor();
        content    = group.values();
        automatic  = (Parameter) v3.getDescriptor().createValue(); // Remove cast with J2SE 1.5
        assertEquals   ("values", 4,  content.size());
        assertTrue     ("v1",         content.contains(v1 ));
        assertTrue     ("v2",         content.contains(v2 ));
        assertTrue     ("v3",         content.contains(v3 ));
        assertFalse    ("v1b",        content.contains(v1b));
        assertTrue     ("v2b",        content.contains(v2b));
        assertFalse    ("v3b",        content.contains(v3b));
        assertSame     ("v1",   v1,   group.parameter ("1"));
        assertSame     ("v2",   v2,   group.parameter ("2"));
        assertSame     ("v3",   v3,   group.parameter ("3"));
        assertSame     ("v2b",  v2b,   group.parameter("2"));
        assertFalse    ("automatic",  content.contains(automatic));
        assertNotEquals("v3",   v3,   group.parameter ("3")); // Should have automatically created.
        assertTrue     ("automatic",  content.contains(automatic));

        try {
            new ParameterGroup(descriptor, new Parameter[] {v1, v3});
            fail("Parameter 2 was mandatory.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        try {
            new ParameterGroup(descriptor, new Parameter[] {v1, v2, v3, v3b});
            fail("Parameter 3 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }
        try {
            new ParameterGroup(descriptor, new Parameter[] {v1, v3, v1b});
            fail("Parameter 1 was not allowed to be inserted twice.");
        } catch (IllegalArgumentException exception) {
            // This is the expected exception.
        }

        //
        // Case (v1, v2)
        //
        group      = new ParameterGroup(properties, new Parameter[] {v1, v2});
        descriptor = (ParameterDescriptorGroup) group.getDescriptor();
        content    = descriptor.descriptors();
        assertEquals("name", "group", descriptor.getName().getCode());
        assertEquals("descriptors", 2, content.size());
        assertTrue  ("p1",  content.contains(p1));
        assertTrue  ("p2",  content.contains(p2));
        assertFalse ("p3",  content.contains(p3));
        assertSame  ("p1",  p1, descriptor.descriptor("1"));
        assertSame  ("p2",  p2, descriptor.descriptor("2"));
        try {
            assertSame("p3", p3, descriptor.descriptor("3"));
            fail("p3 should not exists");
        } catch (ParameterNotFoundException e) {
            // This is the expected exception
        }

        content = group.values();
        assertEquals("values", 2, content.size());
        assertTrue  ("v1",  content.contains(v1 ));
        assertTrue  ("v2",  content.contains(v2 ));
        assertFalse ("v3",  content.contains(v3 ));
        assertFalse ("v1b", content.contains(v1b));
        assertFalse ("v2b", content.contains(v2b));
        assertFalse ("v3b", content.contains(v3b));
        assertSame  ("v1",  v1, group.parameter("1"));
        assertSame  ("v2",  v2, group.parameter("2"));
        try {
            assertSame("v3", v3, group.parameter("3"));
            fail("v3 should not exists");
        } catch (ParameterNotFoundException e) {
            // This is the expected exception
        }

        //
        // Case (v1, v3)
        //
        group      = new ParameterGroup(properties, new Parameter[] {v1, v3});
        descriptor = (ParameterDescriptorGroup) group.getDescriptor();
        content    = descriptor.descriptors();
        assertEquals("name", "group", descriptor.getName().getCode());
        assertEquals("descriptors", 2, content.size());
        assertTrue  ("p1",  content.contains(p1));
        assertFalse ("p2",  content.contains(p2));
        assertTrue  ("p3",  content.contains(p3));
        assertSame  ("p1",  p1, descriptor.descriptor("1"));
        assertSame  ("p3",  p3, descriptor.descriptor("3"));
        try {
            assertSame("p2", p2, descriptor.descriptor("2"));
            fail("p2 should not exists");
        } catch (ParameterNotFoundException e) {
            // This is the expected exception
        }

        content = group.values();
        assertEquals("values", 2, content.size());
        assertTrue  ("v1",  content.contains(v1 ));
        assertFalse ("v2",  content.contains(v2 ));
        assertTrue  ("v3",  content.contains(v3 ));
        assertFalse ("v1b", content.contains(v1b));
        assertFalse ("v2b", content.contains(v2b));
        assertFalse ("v3b", content.contains(v3b));
        assertSame  ("v1",  v1, group.parameter("1"));
        assertSame  ("v3",  v3, group.parameter("3"));
        try {
            assertSame("v2", v2, group.parameter("2"));
            fail("v2 should not exists");
        } catch (ParameterNotFoundException e) {
            // This is the expected exception
        }

        new ParameterGroup(properties, new Parameter[] {v1, v2, v3, v2b});
//        try {
//            new ParameterGroup(properties, new Parameter[] {v1, v2, v3, v3b});
//            fail("Parameter 3 was not allowed to be inserted twice.");
//        } catch (IllegalArgumentException exception) {
//            // This is the expected exception.
//        }
//        try {
//            new ParameterGroup(properties, new Parameter[] {v1, v3, v1b});
//            fail("Parameter 1 was not allowed to be inserted twice.");
//        } catch (IllegalArgumentException exception) {
//            // This is the expected exception.
//        }
    }

    /**
     * Test WKT formatting of transforms backed by matrix.
     */
    public void testMatrix() {
//        final Formatter  formatter = new Formatter(null);
//        final GeneralMatrix matrix = new GeneralMatrix(4);
//        matrix.setElement(0,2,  4);
//        matrix.setElement(1,0, -2);
//        matrix.setElement(2,3,  7);
//        MathTransform transform = ProjectiveTransform.create(matrix);
//        assertFalse(transform instanceof AffineTransform);
//        formatter.append(transform);
//        assertEquals("PARAM_MT[\"Affine\", "          +
//                     "PARAMETER[\"num_row\", 4], "    +
//                     "PARAMETER[\"num_col\", 4], "    +
//                     "PARAMETER[\"elt_0_2\", 4.0], "  +
//                     "PARAMETER[\"elt_1_0\", -2.0], " +
//                     "PARAMETER[\"elt_2_3\", 7.0]]", formatter.toString());
//        matrix.setSize(3,3);
//        transform = ProjectiveTransform.create(matrix);
//        assertTrue(transform instanceof AffineTransform);
//        formatter.clear();
//        formatter.append(transform);
//        assertEquals("PARAM_MT[\"Affine\", "          +
//                     "PARAMETER[\"num_row\", 3], "    +
//                     "PARAMETER[\"num_col\", 3], "    +
//                     "PARAMETER[\"elt_0_2\", 4.0], "  +
//                     "PARAMETER[\"elt_1_0\", -2.0]]", formatter.toString());
    }

    /**
     * Tests the storage of matrix parameters.
     */
    public void textMatrixEdit() {
//        final int size = 8;
//        final Random random = new Random(47821365);
//        final GeneralMatrix matrix = new GeneralMatrix(size);
//        for (int j=0; j<size; j++) {
//            for (int i=0; i<size; i++) {
//                matrix.setElement(j, i, 200*random.nextDouble()-100);
//            }
//        }
//        MatrixParameters descriptor = new MatrixParameters(Collections.singletonMap("name", "Test"));
//        for (int height=2; height<=size; height++) {
//            for (int width=2; width<=size; width++) {
//                MatrixParameterValues parameters = (MatrixParameterValues) descriptor.createValue();
//                GeneralMatrix copy = (GeneralMatrix) matrix.clone();
//                copy.setSize(height, width);
//                parameters.setMatrix(copy);
//                assertEquals("height", height, ((Parameter) parameters.parameter("num_row")).intValue());
//                assertEquals("width",  width,  ((Parameter) parameters.parameter("num_col")).intValue());
//                assertEquals("equals", copy,   parameters.getMatrix());
//                assertEquals("equals", parameters, parameters.clone());
//            }        
//        }
    }

    /**
     * Test the serialization of the given object.
     */
    private static void serialize(final Object object) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream out  = new ByteArrayOutputStream();
        final ObjectOutputStream    outs = new ObjectOutputStream(out);
        outs.writeObject(object);
        outs.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final Object test = in.readObject();
        in.close();

        assertNotSame("Serialization", object, test);
        assertEquals ("Serialization", object, test);
        assertEquals ("Serialization", object.hashCode(), test.hashCode());
    }

    /**
     * Ensure that the specified objects are not equals.
     */
    private static void assertNotEquals(final String message, final Object o1, final Object o2) {
        assertNotNull(message, o1);
        assertNotNull(message, o2);
        assertNotSame(message, o1, o2);
        assertFalse  (message, o1.equals(o2));
    }
}
