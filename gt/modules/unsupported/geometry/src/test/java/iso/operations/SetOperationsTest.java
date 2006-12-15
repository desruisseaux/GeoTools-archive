package iso.operations;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.io.wkt.ParseException;
import org.geotools.geometry.iso.io.wkt.WKTReader;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;

public class SetOperationsTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		// Primitive / Primitive - Tests
		this._testCurvePolygon(tGeomFactory);
		this._testPolygonPolygon(tGeomFactory);
		this._testCurveCurve(tGeomFactory);

		// Primitive / Complex - Tests

		// Complex / Complex - Tests

		
		// TODO Testing all combinations of surface / curve / point intersections
		
	}
	
	
	private void _testPolygonPolygon(FeatGeomFactoryImpl aGeomFactory) {

		SurfaceImpl surfaceAwithoutHole = this.createSurfaceAwithoutHole(aGeomFactory);
		SurfaceImpl surfaceAwithHole = this.createSurfaceAwithHole(aGeomFactory);
		SurfaceImpl surfaceAwithTwoHoles = this.createSurfaceAwithTwoHoles(aGeomFactory);
		SurfaceImpl surfaceBwithoutHole = this.createSurfaceBwithoutHole(aGeomFactory);
		SurfaceImpl surfaceBwithHole = this.createSurfaceBwithHole(aGeomFactory);
		SurfaceImpl surfaceC = this.createSurfaceC(aGeomFactory);
		
		SurfaceImpl s1 = null;
		SurfaceImpl s2 = null;
		
		// (S1)
		// A WITHOUT HOLES - B WITHOUT HOLES:
		// INTERSECTION SHELL-A/SHELL-B
		s1 = surfaceAwithoutHole;
		s2 = surfaceBwithoutHole;
		System.out.println("(S1)");
		this.testAndPrintTest("S1", s1, s2);

		// (S2)
		// A WITH ONE HOLE - B WITHOUT HOLES
		// INTERSECTION SHELL-A/SHELL-B
		s1 = surfaceAwithHole;
		s2 = surfaceBwithoutHole;
		System.out.println("(S2)");
		this.testAndPrintTest("S2", s1, s2);

		// (S3)
		// A WITHOUT HOLES - B WITH ONE HOLE
		// INTERSECTION SHELL-A/SHELL-B AND SHELL-A/HOLE-B
		s1 = surfaceAwithoutHole;
		s2 = surfaceBwithHole;
		System.out.println("(S3)");
		this.testAndPrintTest("S3", s1, s2);
		
		// (S4)
		// A WITH ONE HOLE - B WITH ONE HOLE
		// INTERSECTION SHELL-A/SHELL-B AND SHELL-A/HOLE-B
		s1 = surfaceAwithHole;
		s2 = surfaceBwithHole;
		System.out.println("(S4)");
		this.testAndPrintTest("S4", s1, s2);
		
		// (S5)
		// A WITH TWO HOLES (one in and one outside B) - B WITHOUT HOLES
		// INTERSECTION SHELL-A/SHELL-B
		s1 = surfaceAwithTwoHoles;
		s2 = surfaceBwithoutHole;
		System.out.println("(S5)");
		this.testAndPrintTest("S5", s1, s2);

		// (S6)
		// A WITH TWO HOLES (one in and one outside B) - B WITH ONE HOLES
		// INTERSECTION SHELL-A/SHELL-B AND SHELL-A/HOLE-B
		s1 = surfaceAwithTwoHoles;
		s2 = surfaceBwithHole;
		System.out.println("(S6)");
		this.testAndPrintTest("S6", s1, s2);

		// (S7)
		// B WITHOUT HOLES - C withour holes
		// DISJOINT
		s1 = surfaceBwithoutHole;
		s2 = surfaceC;
		System.out.println("(S7)");
		this.testAndPrintTest("S7", s1, s2);

	}

	private void _testCurvePolygon(FeatGeomFactoryImpl aGeomFactory) {

		SurfaceImpl surfaceAwithoutHole = this.createSurfaceAwithoutHole(aGeomFactory);
		SurfaceImpl surfaceAwithHole = this.createSurfaceAwithHole(aGeomFactory);
		SurfaceImpl surfaceAwithTwoHoles = this.createSurfaceAwithTwoHoles(aGeomFactory);
		SurfaceImpl surfaceBwithoutHole = this.createSurfaceBwithoutHole(aGeomFactory);
		SurfaceImpl surfaceBwithHole = this.createSurfaceBwithHole(aGeomFactory);
		SurfaceImpl surfaceC = this.createSurfaceC(aGeomFactory);
		CurveImpl curveA = this.createCurveA(aGeomFactory);
		CurveImpl curveF = this.createCurveF(aGeomFactory);

		GeometryImpl g1 = null;
		GeometryImpl g2 = null;
		
		g1 = surfaceBwithoutHole;
		g2 = curveA;
		System.out.println("(CS1)");
		this.testAndPrintTest("CS1", g1, g2);

		g1 = surfaceAwithoutHole;
		g2 = curveF;
		System.out.println("(CS2)");
		this.testAndPrintTest("CS2", g1, g2);

		//GeometryImpl g = this._testIntersection(surface1, curve1);
		//System.out.println("\nIntersection Surface/Curve - Expected result geometry: CURVE(100 105.71, 100 70, 83.22 83.05)");
		//System.out.println("Intersection Surface/Curve - result geometry: " + g);

	}
	
	private void _testCurveCurve(FeatGeomFactoryImpl aGeomFactory) {

		CurveImpl curveA = this.createCurveA(aGeomFactory);
		CurveImpl curveB = this.createCurveB(aGeomFactory);
		CurveImpl curveC = this.createCurveC(aGeomFactory);
		CurveImpl curveD = this.createCurveD(aGeomFactory);
		CurveImpl curveE = this.createCurveE(aGeomFactory);
		
		CurveImpl c1 = null;
		CurveImpl c2 = null;
		
		// (C1) - Touch
		c1 = curveA;
		c2 = curveB;
		System.out.println("(C1)");
		this.testAndPrintTest("C1", c1, c2);

		// (C2) - Cross
		c1 = curveB;
		c2 = curveC;
		System.out.println("(C2)");
		this.testAndPrintTest("C2", c1, c2);

		// (C3) - Overlap
		c1 = curveB;
		c2 = curveD;
		System.out.println("(C3)");
		this.testAndPrintTest("C3", c1, c2);

		// (C4) - Equal
		c1 = curveB;
		c2 = curveB;
		System.out.println("(C4)");
		this.testAndPrintTest("C4", c1, c2);

		
	}
	
	
	
	

	private void testAndPrintTest(String testCaseID, GeometryImpl g1, GeometryImpl g2) {
		
		System.out.print("\nTestcase : (" + testCaseID + ")");
		System.out.print("\nGeometry 1 :" + g1);
		System.out.print("\nGeometry 2 :" + g2 + "\n");

		GeometryImpl g = null;
		
		// INTERSECTION
		g = this._testIntersection(g1, g2);
		System.out.println("Intersection - result geometry: " + g);

		// UNION
		g = this._testUnion(g1, g2);
		System.out.println("Union - result geometry: " + g);

		// DIFFERENCE S1-S2
		g = this._testDifference(g1, g2);
		System.out.println("Difference - result geometry: " + g);

		// DIFFERENCE S2-S1
		g = this._testDifference(g2, g1);
		System.out.println("Difference - result geometry: " + g);

		// SYMMETRIC DIFFERENCE
		g = this._testSymmetricDifference(g1, g2);
		System.out.println("SymmetricDifference - result geometry: " + g);
		
	}

	
	
	private CurveImpl createCurveFromWKT(FeatGeomFactoryImpl aGeomFactory, String aWKTcurve) {
		CurveImpl rCurve = null;
		WKTReader wktReader = new WKTReader(aGeomFactory.getPrimitiveFactory(), aGeomFactory.getCoordinateFactory());
		try {
			rCurve = (CurveImpl) wktReader.read(aWKTcurve);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return rCurve;
	}

	private SurfaceImpl createSurfaceFromWKT(FeatGeomFactoryImpl aGeomFactory, String aWKTsurface) {
		SurfaceImpl rSurface = null;
		WKTReader wktReader = new WKTReader(aGeomFactory.getPrimitiveFactory(), aGeomFactory.getCoordinateFactory());
		try {
			rSurface = (SurfaceImpl) wktReader.read(aWKTsurface);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return rSurface;
	}
	
	private GeometryImpl _testIntersection(GeometryImpl g1, GeometryImpl g2) {
		return (GeometryImpl) g1.intersection(g2);
	}
	
	private GeometryImpl _testUnion(GeometryImpl g1, GeometryImpl g2) {
		return (GeometryImpl) g1.union(g2);
	}
	
	private GeometryImpl _testDifference(GeometryImpl g1, GeometryImpl g2) {
		return (GeometryImpl) g1.difference(g2);
	}
	
	private GeometryImpl _testSymmetricDifference(GeometryImpl g1, GeometryImpl g2) {
		return (GeometryImpl) g1.symmetricDifference(g2);
	}

	
	private SurfaceImpl createSurfaceAwithoutHole(FeatGeomFactoryImpl aGeomFactory) {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}
	
	private SurfaceImpl createSurfaceAwithHole(FeatGeomFactoryImpl aGeomFactory) {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceAwithTwoHoles(FeatGeomFactoryImpl aGeomFactory) {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60), (30 100, 30 120, 50 120, 50 100, 30 100))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceBwithoutHole(FeatGeomFactoryImpl aGeomFactory) {
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((100 10, 70 50, 90 100, 160 140, 200 90, 170 20, 100 10))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceBwithHole(FeatGeomFactoryImpl aGeomFactory) {
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((100 10, 70 50, 90 100, 160 140, 200 90, 170 20, 100 10), (120 30, 110 50, 120 80, 170 80, 160 40, 120 30))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}
	
	private SurfaceImpl createSurfaceC(FeatGeomFactoryImpl aGeomFactory) {
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((0 50, 50 50, 50 150, 20 140, 0 50))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}
	
	
	private CurveImpl createCurveA(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(30 20, 10 50, 100 120, 100 70, 10 140)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}
	
	private CurveImpl createCurveB(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(30 20, 50 20, 80 20)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}
	
	private CurveImpl createCurveC(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(40 60, 40 30, 40 10)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}

	private CurveImpl createCurveD(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(70 20, 100 20)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}
	
	private CurveImpl createCurveE(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(40 40, 40 50)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}

	private CurveImpl createCurveF(FeatGeomFactoryImpl aGeomFactory) {
		String wktCurve1 = "CURVE(80 200, 80 -100)";
		return this.createCurveFromWKT(aGeomFactory, wktCurve1);
	}

	

}
