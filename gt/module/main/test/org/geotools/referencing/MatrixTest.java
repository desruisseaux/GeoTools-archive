/*
   Copyright (C) 1997,1998,1999
   Kenji Hiranabe, Eiwa System Management, Inc.

   This program is free software.
   Implemented by Kenji Hiranabe(hiranabe@esm.co.jp),
   conforming to the Java(TM) 3D API specification by Sun Microsystems.

   Permission to use, copy, modify, distribute and sell this software
   and its documentation for any purpose is hereby granted without fee,
   provided that the above copyright notice appear in all copies and
   that both that copyright notice and this permission notice appear
   in supporting documentation. Kenji Hiranabe and Eiwa System Management,Inc.
   makes no representations about the suitability of this software for any
   purpose.  It is provided "AS IS" with NO WARRANTY.
*/
package org.geotools.referencing;

import javax.vecmath.*;
import junit.framework.*;


/**
 * Test the <code>java.vecmath</code> package.
 *
 * @version $Id$
 * @author Kenji Hiranabe
 */
public class MatrixTest extends TestCase {
    /**
     * <code>true</code> for testing discrepency between Java3D and Eiwa's implementations.
     * Some results expected by this test suite disagree with the result produced by Sun's
     * implementation. We have not sorted out which one is right.
     */
    private static final boolean DISCREPANCY = false;

    /**
     * Small numbers for comparaison purpose.
     */
    private static final float EPS = 1.0e-5f;

    /**
     * Construct a test case.
     */
    public MatrixTest(String testName) {
        super(testName);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(MatrixTest.class);
    }

    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        final Package p = Package.getPackage("javax.vecmath");
        if (p != null) {
            System.out.println(p);
            System.out.println(p.getImplementationVendor());
        }
        junit.textui.TestRunner.run(suite());
    }

    private static boolean equals(final float m1, final float m2) {
	return Math.abs(m1 - m2) < EPS;
    }
    private static boolean equals(final double m1, final double m2) {
	return Math.abs(m1 - m2) < EPS;
    }

    private static void assertEquals(float    m1, float    m2) {assertEquals(m1, m2, EPS);}
    private static void assertEquals(double   m1, double   m2) {assertEquals(m1, m2, EPS);}
    private static void assertEquals(Matrix3d m1, Matrix3d m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Matrix4d m1, Matrix4d m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Tuple4d  m1, Tuple4d  m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Tuple3d  m1, Tuple3d  m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Matrix3f m1, Matrix3f m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Matrix4f m1, Matrix4f m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(GMatrix  m1, GMatrix  m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(GVector  v1, GVector  v2) {assertTrue(v1.epsilonEquals(v2, EPS));}
    private static void assertEquals(Tuple4f  m1, Tuple4f  m2) {assertTrue(m1.epsilonEquals(m2, EPS));}
    private static void assertEquals(Tuple3f  m1, Tuple3f  m2) {assertTrue(m1.epsilonEquals(m2, EPS));}

    /** Test two axis angle for equality. */
    private static void assertEquals(final AxisAngle4d a1, final AxisAngle4d a2) {
        if (0 < a1.x*a2.x + a1.y*a2.y + a1.z*a2.z) {  // same direction
            assertEquals(0, a1.y*a2.z - a1.z*a2.y);
            assertEquals(0, a1.z*a2.x - a1.x*a2.z);
            assertEquals(0, a1.x*a2.y - a1.y*a2.x);
            assertEquals(a1.angle, a2.angle, EPS);
        } else {
            assertEquals(0, a1.y*a2.z - a1.z*a2.y);
            assertEquals(0, a1.z*a2.x - a1.x*a2.z);
            assertEquals(0, a1.x*a2.y - a1.y*a2.x);
            assertTrue(equals(a1.angle, -a2.angle)             || 
                       equals(a1.angle + a2.angle,  2*Math.PI) || 
                       equals(a1.angle + a2.angle, -2*Math.PI));
        }
    }

    /** Test two axis angle for equality. */
    private static void assertEquals(final AxisAngle4f a1, final AxisAngle4f a2) {
        if (0 < a1.x*a2.x + a1.y*a2.y + a1.z*a2.z) {  // same direction
            assertEquals(0, a1.y*a2.z - a1.z*a2.y);
            assertEquals(0, a1.z*a2.x - a1.x*a2.z);
            assertEquals(0, a1.x*a2.y - a1.y*a2.x);
            assertEquals(a1.angle, a2.angle, EPS);
        } else {
            assertEquals(0, a1.y*a2.z - a1.z*a2.y);
            assertEquals(0, a1.z*a2.x - a1.x*a2.z);
            assertEquals(0, a1.x*a2.y - a1.y*a2.x);
            assertTrue(equals(a1.angle, -a2.angle) || 
                       equals(a1.angle + a2.angle,  2*Math.PI) || 
                       equals(a1.angle + a2.angle, -2*Math.PI));
        }
    }

    /////////////////////
    //  test  methods.
    /////////////////////

    /**
     * Tests {@link Vector3d}.
     */
    public void testVector3d() {
        Vector3d zeroVector = new Vector3d();
        Vector3d v1 = new Vector3d(2,3,4);
        Vector3d v2 = new Vector3d(2,5,-8);

        Vector3d v3 = new Vector3d();
        v3.cross(v1, v2);

        // check cross and dot.
        assertEquals(0, v3.dot(v1));
        assertEquals(0, v3.dot(v2));

        // check alias-safe
        v1.cross(v1, v2);
        assertEquals(new Vector3d(-44,24,4), v1);

        // check length
        assertEquals(93, v2.lengthSquared());
        assertEquals(Math.sqrt(93), v2.length());

        // check normalize
        v1.set(v2);
        v2.normalize();
        assertEquals(1, v2.length());
        v1.cross(v2,v1);
        assertEquals(zeroVector, v1);

        // check Angle
        v1.set(1,2,3);
        v2.set(-1,-6,-3);
        double ang = v1.angle(v2);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));

        // check Angle (0)
        v1.set(v2);
        ang = v1.angle(v2);
        assertEquals(0, ang);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));

        // check small Angle
        v1.set(1,2,3);
        v2.set(1,2,3.00001);
        ang = v1.angle(v2);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));

        // check large Angle
        v1.set(1,2,3);
        v2.set(-1,-2,-3.00001);
        ang = v1.angle(v2);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));
    }

    /**
     * Tests {@link Vector3f}.
     */
    public void testVector3f() {
        Vector3f zeroVector = new Vector3f();
        Vector3f v1 = new Vector3f(2,3,4);
        Vector3f v2 = new Vector3f(2,5,-8);
        Vector3f v3 = new Vector3f();
        v3.cross(v1, v2);

        assertEquals(0, v3.dot(v1));
        assertEquals(0, v3.dot(v2));

        // check alias-safe
        v1.cross(v1, v2);
        assertEquals(new Vector3f(-44,24,4), v1);

        // check length
        assertEquals(93, v2.lengthSquared());
        assertEquals(Math.sqrt(93), v2.length());

        // check normalize
        v1.set(v2);
        v2.normalize();
        assertEquals(1, v2.length());
        v1.cross(v2,v1);
        assertEquals(zeroVector, v1);

        // check Angle
        v1.set(1,2,3);
        v2.set(-1,-6,-3);
        double ang = v1.angle(v2);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));

        // check Angle (0)
        v1.set(v2);
        ang = v1.angle(v2);
        assertEquals(0, ang);
        assertEquals(v1.length()*v2.length()*Math.cos(ang), v1.dot(v2));
    }

    /**
     * Tests {@link Matrix3d}.
     */
    public void testMatrix3d() {
        Matrix3d O = new Matrix3d();
        Matrix3d I = new Matrix3d(); I.setIdentity();
        Matrix3d m1 = new Matrix3d();
        Matrix3d m2 = new Matrix3d();
        double [] v = { 2,1,4, 1,-2,3, -3,-1,1 };

        // check get/set
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++)
                m1.setElement(i, j, i*2*j + 3);
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++)
                assertEquals(i*2*j + 3, m1.getElement(i, j));
        }

        // check mul with O, I
        m1.set(v);
        m2 = new Matrix3d(m1);
        m2.mul(O);
        assertEquals(O, m2);
        m2.mul(m1, I);
        assertEquals(m2, m1);

        // check determinant
        assertEquals(-36, m1.determinant());

        // check negate, add
        m2.negate(m1);
        m2.add(m1);
        assertEquals(O, m2);

        // check mul, sub
        m2.negate(m1);
        Matrix3d m3 = new Matrix3d(m1);
        m3.sub(m2);
        m3.mul(0.5);
        assertEquals(m1, m3);

        // check invert
        m3.invert(m2);
        m3.mul(m2);
        assertEquals(I, m3);

        // translate
        Point3d p1 = new Point3d(1,2,3);
        Vector3d v1 = new Vector3d(2,-1,-4);

        // rotZ
        // rotate (1,0,0) 30degree abount z axis -> (cos 30,sin 30,0)
        p1.set(1,0,0);
        m1.rotZ(Math.PI/6);
        m1.transform(p1);
        assertEquals(new Point3d(Math.cos(Math.PI/6),
                                 Math.sin(Math.PI/6),
                                 0), p1);

        // rotY
        // rotate() (1,0,0) 60degree about y axis -> (cos 60,0,-sin 60)
        p1.set(1,0,0);
        m1.rotY(Math.PI/3);
        m1.transform(p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        // rot around arbitary axis
        // rotate() (1,0,0) 60degree about y axis -> (cos 60,0,-sin 60)
        AxisAngle4d a1 = new AxisAngle4d(0,1,0,Math.PI/3);
        p1.set(1,0,0);
        m1.set(a1);
        m1.transform(p1, p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        // use quat.
        Quat4d q1 = new Quat4d();
        p1.set(1,0,0);
        q1.set(a1);
        m2.set(q1);
        assertEquals(m1, m2);
        m2.transform(p1, p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        if (!DISCREPANCY) {
            /*
             * From this point, all remaining tests fails with Sun's implementation.
             */
            return;
        }
        // Mat <-> Quat <-> Axis
        a1.set(1, 2, -3, Math.PI/3);
        mat3dQuatAxisAngle(a1);

        // Mat <-> Quat <-> Axis (near PI case)
        a1.set(1, 2, 3, Math.PI);
        mat3dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, X major case )
        a1.set(1, .1, .1, Math.PI);
        mat3dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, Y major case )
        a1.set(.1, 1, .1, Math.PI);
        mat3dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, Z major case )
        a1.set(.1, .1, 1, Math.PI);
        mat3dQuatAxisAngle(a1);

        // isometric view 3 times 2/3 turn
        a1.set(1, 1, 1, 2*Math.PI/3);
        m1.set(a1);
        p1.set(1, 0, 0);
        m1.transform(p1);
        assertEquals(new Point3d(0,1,0), p1);
        m1.transform(p1);
        assertEquals(new Point3d(0,0,1), p1);
        m1.transform(p1);
        assertEquals(new Point3d(1,0,0), p1);

        // check normalize, normalizeCP
        m1.set(a1);
        assertEquals(1, m1.determinant());
        assertEquals(1, m1.getScale());
        m2.set(a1);
        m2.normalize();
        assertEquals(m1, m2);
        m2.set(a1);
        m2.normalizeCP();
        assertEquals(m1, m2);
        double scale = 3.0;
        m2.rotZ(-Math.PI/4);
        m2.mul(scale);
        assertEquals(scale*scale*scale, m2.determinant());
        assertEquals(scale, m2.getScale());
        m2.normalize();
        assertEquals(1, m2.determinant());
        assertEquals(1, m2.getScale());
        m2.rotX(Math.PI/3);
        m2.mul(scale);
        assertEquals(scale*scale*scale, m2.determinant());
        assertEquals(scale, m2.getScale());
        m2.normalizeCP();
        assertEquals(1, m2.determinant());
        assertEquals(1, m2.getScale());

        // transpose and inverse
        m1.set(a1);
        m2.invert(m1);
        m1.transpose();
        assertEquals(m1, m2);
    }

    private static void mat3dQuatAxisAngle(AxisAngle4d a1) {
        Matrix3d m1 = new Matrix3d();
        Matrix3d m2 = new Matrix3d();
        AxisAngle4d a2 = new AxisAngle4d();
        Quat4d q1 = new Quat4d();
        Quat4d q2 = new Quat4d();

        // Axis <-> Quat
        q1.set(a1);
        a2.set(q1);
        // a1.v parallels to a2.v 
        assertEquals(a1, a2);
        q2 = new Quat4d();
        q2.set(a2);
        assertEquals(q1, q2);

        // Quat <-> Mat
        q1.set(a1);
        m1.set(q1);
        q2.set(m1);
        assertEquals(q1, q2);
        m2.set(q2);
        assertEquals(m1, m2);

        // Mat <-> AxisAngle
        m1.set(a1);
        a2.set(m1);
        assertEquals(a1, a2);
        m2.set(a1);
        assertEquals(m1, m2);
        a1.x *= 2; a1.y *= 2; a1.z *= 2;
        m2.set(a1);
        a1.x = -a1.x; a1.y = -a1.y; a1.z = -a1.z; a1.angle = -a1.angle;
        m2.set(a1);
        assertEquals(m1, m2);
    }

    private static void mat4dQuatAxisAngle(AxisAngle4d a1) {
        Matrix4d m1 = new Matrix4d();
        Matrix4d m2 = new Matrix4d();
        AxisAngle4d a2 = new AxisAngle4d();
        Quat4d q1 = new Quat4d();
        Quat4d q2 = new Quat4d();

        // Axis <-> Quat
        q1.set(a1);
        a2.set(q1);
        // a1.v parallels to a2.v 
        assertEquals(a1, a2);
        q2 = new Quat4d();
        q2.set(a2);
        assertEquals(q1, q2);

        // Quat <-> Mat
        q1.set(a1);
        m1.set(q1);
        q2.set(m1);
        assertEquals(q1, q2);
        m2.set(q2);
        assertEquals(m1, m2);

        // Mat <-> AxisAngle
        m1.set(a1);
        a2.set(m1);
        assertEquals(a1, a2);
        m2.set(a1);
        assertEquals(m1, m2);
        a1.x *= 2; a1.y *= 2; a1.z *= 2;
        m2.set(a1);
        a1.x = -a1.x; a1.y = -a1.y; a1.z = -a1.z; a1.angle = -a1.angle;
        m2.set(a1);
        assertEquals(m1, m2);
    }

    /**
     * Tests {@link Matrix4d}.
     */
    public void testMatrix4d() {
        Matrix4d O = new Matrix4d();
        Matrix4d I = new Matrix4d(); I.setIdentity();
        Matrix4d m1 = new Matrix4d();
        Matrix4d m2 = new Matrix4d();

        // check get/set
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++)
                m1.setElement(i, j, i*2*j + 3);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++)
                assertEquals(i*2*j + 3, m1.getElement(i, j));
        }

        // check mul with O, I
        m1 = new Matrix4d( 2,  1,  4,  1,
                          -2,  3, -3,  1,
                          -1,  1,  2,  2,
                           0,  8,  1,-10);
        m2 = new Matrix4d(m1);
        m2.mul(O);
        assertEquals(O, m2);
        m2.mul(m1, I);
        assertEquals(m1, m2);

        // check negate, add
        m2.negate(m1);
        m2.add(m1);
        assertEquals(O, m2);

        // check mul, sub
        double v[] = { 5,  1,  4,  0,
                       2,  3, -4, -1,
                       2,  3, -4, -1,
                       1,  1,  1,  1};
        m2.set(v);
        m2.negate(m1);
        Matrix4d m3 = new Matrix4d(m1);
        m3.sub(m2);
        m3.mul(0.5);
        assertEquals(m1, m3);

        // check invert
        m2 = new Matrix4d(0.5, 1,   4,  1,
                         -2,   3,  -4, -1,
                          1,   9, 100,  2,
                        -20,   2,   1,  9);
        m3.invert(m2);
        m3.mul(m2);
        assertEquals(I, m3);

        // translate
        m1 = new Matrix4d(-1,  2,  0,  3,
                          -1,  1, -3, -1,
                           1,  2,  1,  1,
                           0,  0,  0,  1);
        Point3d  p1 = new Point3d(1,2,3);
        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d(1,2,3);
        Vector4d V2 = new Vector4d(2,-1,-4,1);

        m1.transform(p1);
        assertEquals(new Point3d(6,-9,9), p1);
        m1.transform(V2,V2);
        assertEquals(new Vector4d(-1,8,-3,1), V2);

        // rotZ
        // rotate (1,0,0) 30degree abount z axis -> (cos 30,sin 30,0)
        p1.set(1,0,0);
        m1.rotZ(Math.PI/6);
        m1.transform(p1);
        assertEquals(new Point3d(Math.cos(Math.PI/6),
                                 Math.sin(Math.PI/6),
                                 0), p1);

        // rotY
        // rotate() (1,0,0) 60degree about y axis -> (cos 60,0,-sin 60)
        p1.set(1,0,0);
        m1.rotY(Math.PI/3);
        m1.transform(p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        // rot around arbitary axis
        // rotate() (1,0,0) 60degree about y axis -> (cos 60,0,-sin 60)
        AxisAngle4d a1 = new AxisAngle4d(0,1,0,Math.PI/3);
        p1.set(1,0,0);
        m1.set(a1);
        m1.transform(p1, p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        // use quat.
        Quat4d q1 = new Quat4d();
        p1.set(1,0,0);
        q1.set(a1);
        m2.set(q1);
        assertEquals(m1, m2);
        m2.transform(p1, p1);
        assertEquals(new Point3d(Math.cos(Math.PI/3),
                                 0,
                                 -Math.sin(Math.PI/3)), p1);

        if (!DISCREPANCY) {
            /*
             * From this point, all remaining tests fails with Sun's implementation.
             */
            return;
        }
        // Mat <-> Quat <-> Axis
        a1.set(1,2,-3,Math.PI/3);
        mat4dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI case)
        a1.set(1,2,3,Math.PI);
        mat4dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, X major case )
        a1.set(1,.1,.1,Math.PI);
        mat4dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, Y major case )
        a1.set(.1,1,.1,Math.PI);
        mat4dQuatAxisAngle(a1);
        // Mat <-> Quat <-> Axis (near PI, Z major case )
        a1.set(.1,.1,1,Math.PI);
        mat4dQuatAxisAngle(a1);

        // isometric view 3 times 2/3 turn
        a1.set(1,1,1,2*Math.PI/3);
        m1.set(a1);
        p1.set(1,0,0);
        m1.transform(p1);
        assertEquals(new Point3d(0,1,0), p1);
        m1.transform(p1);
        assertEquals(new Point3d(0,0,1), p1);
        m1.transform(p1);
        assertEquals(new Point3d(1,0,0), p1);

        // check getScale
        m1.set(a1);
        assertEquals(1, m1.determinant());
        assertEquals(1, m1.getScale());
        m2.set(a1);

        // transpose and inverse
        m1.set(a1);
        m2.invert(m1);
        m1.transpose();
        assertEquals(m1, m2);

        // rot, scale, trans
        Matrix3d n1 = new Matrix3d();
        n1.set(a1);
        Matrix3d n2 = new Matrix3d();
        v1.set(2, -1, -1);
        m1.set(n1, v1, 0.4);
        m2.set(n1, v1, 0.4);
        Vector3d v2 = new Vector3d();
        double s = m1.get(n2, v2);
        assertEquals(n2, n1);
        assertEquals(0.4, s);
        assertEquals(v2, v1);
        assertEquals(m2, m1); // not modified

    }

    /**
     * Tests {@link GMatrix}.
     */
    public void testGMatrix() {
        GMatrix I44  = new GMatrix(4,4); // Identity 4x4
        GMatrix O44  = new GMatrix(4,4); O44.setZero(); // O 4x4
        GMatrix O34  = new GMatrix(3,4); O34.setZero(); // O 3x4
        GMatrix m1   = new GMatrix(3,4);
        GMatrix m2   = new GMatrix(3,4);
        Matrix3d mm1 = new Matrix3d();
        Matrix3d mm2 = new Matrix3d();

        // get/setElement
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 4; j++) {
                m1.setElement(i,j,(i+1)*(j+2));
                if (j < 3)
                    mm1.setElement(i,j,(i+1)*(j+2));
            }
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 4; j++) {
                assertEquals((i+1)*(j+2), m1.getElement(i,j));
            }

        m1.get(mm2);
        assertEquals(mm1, mm2);
        assertEquals(m1, (GMatrix) m1.clone());

        // mul with I,O
        m2.mul(m1, I44);
        assertEquals(m1, m2);
        m2.mul(m1, O44);
        assertEquals(O34, m2);

        // LUD
        Matrix4d mm3 = new Matrix4d( 1,  2,  3,  4,
                                    -2,  3, -1,  3,
                                    -1, -2, -4,  1,
                                     1,  1, -1, -2);
        Matrix4d mm4 = new Matrix4d();
        Matrix4d mm5 = new Matrix4d();
        mm5.set(mm3);

        // setSize, invert
        m1.setSize(4, 4);
        m2.setSize(4, 4);
        m1.set(mm3);
        m2.set(m1);
        m1.invert();
        mm3.invert();
        mm5.mul(mm3);
        assertEquals(new Matrix4d(1,0,0,0,
                                  0,1,0,0,
                                  0,0,1,0,
                                  0,0,0,1), mm5);

        m1.get(mm4);
        assertEquals(mm3, mm4);
        m1.mul(m2);
        assertEquals(m1, I44);

        // LUD
        Matrix4d mm6 = new Matrix4d(1,   2,  3,  4,
                                    -2,  3, -1,  3,
                                    -1, -2, -4,  1,
                                     1,  1, -1, -2);
        Vector4d vv1 = new Vector4d(1,-1,-1,2);
        Vector4d vv2 = new Vector4d();
        Vector4d vv3 = new Vector4d(4,2,7,-3);
        mm6.transform(vv1, vv2);
        assertEquals(vv2, vv3);

        m1.set(mm6);
        GVector x = new GVector(4);
        GVector v2 = new GVector(4);
        GVector b = new GVector(4);
        x.set(vv1); // (1,-1,-1,2)
        b.set(vv3); // (4,2,7,-3)
        GVector mx = new GVector(4);
        mx.mul(m1, x); // M*x = (4,2,7,-3)
        assertEquals(mx, b);

        GVector p = new GVector(4);
        m1.LUD(m2, p);
        if (!DISCREPANCY) {
            /*
             * From this point, all remaining tests fails with Sun's implementation.
             */
            return;
        }
        checkLUD(m1, m2, p);
        GVector xx = new GVector(4);
        xx.LUDBackSolve(m2, b, p);
        assertEquals(xx, x);

        GMatrix u = new GMatrix(m1.getNumRow(), m1.getNumRow());
        GMatrix w = new GMatrix(m1.getNumRow(), m1.getNumCol());
        GMatrix v = new GMatrix(m1.getNumCol(), m1.getNumCol());
        int rank = m1.SVD(u, w, v);
        assertEquals(4, rank);
        checkSVD(m1, u, w, v);
        xx.SVDBackSolve(u, w, v, b);
        assertEquals(xx, x);

        // overwrite m1 -LUD-> m1
        // m1.LUD(m1, p);
        // xx.LUDBackSolve(m2, b, p);
        // assertTrue(equals(xx, x));
    }

    private static void checkLUD(GMatrix m, GMatrix LU, GVector permutation) {
        int n = m.getNumCol();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double aij = 0.0;
                int min = i < j ? i : j;
                for (int k = 0; k <= min; k++) {
                    if (i != k)
                        aij += LU.getElement(i, k)*LU.getElement(k, j);
                    else
                        aij += LU.getElement(k, j);
                }
                if (Math.abs(aij - m.getElement((int)permutation.getElement(i),j)) > EPS) {
                    fail("a["+i+","+j+"] = "+aij+"(LU)ij ! = "+m.getElement((int)permutation.getElement(i),j));
                }
            }
        }
    }

    private static void checkSVD(GMatrix m, GMatrix u, GMatrix w, GMatrix v) {
        int wsize = w.getNumRow() < w.getNumRow() ? w.getNumRow() : w.getNumCol();
        for (int i = 0; i < m.getNumRow(); i++) {
            for (int j = 0; j < m.getNumCol(); j++) {
                double sum = 0.0;
                for (int k = 0; k < m.getNumCol(); k++) {
                    sum += u.getElement(i,k)*w.getElement(k,k)*v.getElement(j,k);
                }
                /* check if SVD is OK */
                if (EPS < Math.abs(m.getElement(i, j)-sum)) {
                    fail("(SVD)ij = "+sum +" != a["+i+","+j+"] = "+m.getElement(i,j));
                }
            }

        }
    }

    /**
     * Tests SVD on {@link GMatrix}.
     */
    public void testSVD() {
        double val[] = {1,2,3,4,
                        5,6,7,8,
                        9,0,8,7,
                        6,5,4,3,
                        2,1,0,1};
        int m = 5;
        int n = 4;
        GMatrix matA = new GMatrix(m,n,val);
        GMatrix matU = new GMatrix(m,m);
        GMatrix matW = new GMatrix(m,n);
        GMatrix matV = new GMatrix(n,n);

        //this = U*W*transpose(V)
        if (DISCREPANCY) {
            /*
             * The Sun's implementation fails here with an
             * ArrayIndexOutOfBoundsException: 4
             */
            int rank = matA.SVD(matU, matW, matV);
            GMatrix matTEMP = new GMatrix(m,n);
            matTEMP.mul(matU, matW);
            matV.transpose();
            matTEMP.mul(matV);
            assertEquals(matTEMP, matA);
        }
    }
}
