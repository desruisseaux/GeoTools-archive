package iso.operations;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.aggregate.MultiCurveImpl;
import org.geotools.geometry.iso.aggregate.MultiPointImpl;
import org.geotools.geometry.iso.aggregate.MultiSurfaceImpl;
import org.geotools.geometry.iso.io.wkt.ParseException;
import org.geotools.geometry.iso.io.wkt.WKTReader;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Point;

public class CentroidTest extends TestCase {
	
	private FeatGeomFactoryImpl factory = null;

	public void testMain() {
		
		// === 2D ===
		this.factory = FeatGeomFactoryImpl.getDefault2D();
		
		// Test Points and MultiPoints
		this._testPoints2D();

		// Test Curves, MultiCurves and CurveBoundaries
		this._testCurves2D();

		// Test Surfaces, MultiSurfaces, SurfaceBoundaries and Rings
		this._testSurfaces2D();

		
		// === 3D ===
		this.factory = FeatGeomFactoryImpl.getDefault3D();
		
		// Test Points and MultiPoints
		this._testPoints3D();

		// Test Curves, MultiCurves and CurveBoundaries
		this._testCurves3D();

		
	}
	
	private void _testPoints2D() {
		
		double res[] = null;

		// Point

		res = this.createPointA().getCentroid().getCoordinates();
		assertTrue(res[0] == 30.0);
		assertTrue(res[1] == 50.0);

		res = this.createPointB().getCentroid().getCoordinates();
		assertTrue(res[0] == 100.0);
		assertTrue(res[1] == 120.0);

		// MultiPoint

		res = this.createMultiPointA().getCentroid().getCoordinates();
		assertTrue(res[0] == 67.5);
		assertTrue(res[1] == 50.0);

	}
	
	private void _testPoints3D() {

		double res[] = null;

		// Point

		res = this.createPointA3D().getCentroid().getCoordinates();
		assertTrue(res[0] == 30.0);
		assertTrue(res[1] == 50.0);
		assertTrue(res[2] == 10.0);

		res = this.createPointB3D().getCentroid().getCoordinates();
		assertTrue(res[0] == 100.0);
		assertTrue(res[1] == 120.0);
		assertTrue(res[2] == 20.0);

		// MultiPoint

		res = this.createMultiPointA3D().getCentroid().getCoordinates();
		System.out.println(this.createMultiPointA3D().getCentroid());
		assertTrue(Math.round(res[0] * 100) == 6667);
		assertTrue(Math.round(res[1] * 100) == 6333);
		assertTrue(Math.round(res[2] * 100) == 1833);
		
		
	}


	private void _testCurves2D() {
		
		double res[] = null;

		// Curve

		res = this.createCurveA().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 58146);
		assertTrue(Math.round(res[1] * 100) == 8811);

		res = this.createCurveB().getCentroid().getCoordinates();
		assertTrue(res[0] == 55);
		assertTrue(res[1] == 20);

		res = this.createCurveC().getCentroid().getCoordinates();
		assertTrue(res[0] == 40);
		assertTrue(res[1] == 35);

		res = this.createCurveD().getCentroid().getCoordinates();
		assertTrue(res[0] == 92.5);
		assertTrue(res[1] == 27.5);

		res = this.createCurveX().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 100) == 4263);
		assertTrue(Math.round(res[1] * 100) == 13986);

		// MultiCurve

		res = this.createMultiCurveA().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 100) == 6025);
		assertTrue(Math.round(res[1] * 100) == 6766);

		// CurveBoundary

		res = this.createCurveA().getBoundary().getCentroid().getCoordinates();
		assertTrue(res[0] == 20);
		assertTrue(res[1] == 80);

		
	}
	
	private void _testCurves3D() {
		
	}

	
	
	private void _testSurfaces2D() {
		
		double res[] = null;

		// Surface
		
		res = this.createSurfaceX().getCentroid().getCoordinates();
		assertTrue(res[0] == 50);
		assertTrue(res[1] == 50);

		res = this.createSurfaceAwithoutHole().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 82161);
		assertTrue(Math.round(res[1] * 100) == 9122);

		res = this.createSurfaceAwithHole().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 81309);
		assertTrue(Math.round(res[1] * 100) == 9167);
		
		res = this.createSurfaceAwithTwoHoles().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 82692);
		assertTrue(Math.round(res[1] * 100) == 9106);

		res = this.createSurfaceBwithoutHole().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 135734);
		assertTrue(Math.round(res[1] * 100) == 6977);

		// MultiSurface

		res = this.createMultiSurfaceA().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 10) == 1068);
		assertTrue(Math.round(res[1] * 1000) == 81357);

		// SurfaceBoundary

		res = this.createSurfaceAwithoutHole().getBoundary().getCentroid().getCoordinates();
		assertTrue(Math.round(res[0] * 1000) == 82178);
		assertTrue(Math.round(res[1] * 1000) == 91658);

		// Ring
		
		res = this.createSurfaceAwithoutHole().getBoundary().getExterior().getCentroid().getCoordinates();
		System.out.println(this.createSurfaceAwithoutHole().getBoundary().getExterior().getCentroid());
		assertTrue(Math.round(res[0] * 1000) == 82178);
		assertTrue(Math.round(res[1] * 1000) == 91658);

	}


	
	private PointImpl createPointFromWKT(String aWKTpoint) {
		PointImpl rPoint = null;
		WKTReader wktReader = new WKTReader(this.factory.getPrimitiveFactory(), this.factory.getCoordinateFactory());
		try {
			rPoint = (PointImpl) wktReader.read(aWKTpoint);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return rPoint;
	}
	
	private CurveImpl createCurveFromWKT(String aWKTcurve) {
		CurveImpl rCurve = null;
		WKTReader wktReader = new WKTReader(this.factory.getPrimitiveFactory(), this.factory.getCoordinateFactory());
		try {
			rCurve = (CurveImpl) wktReader.read(aWKTcurve);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return rCurve;
	}
	
	private SurfaceImpl createSurfaceFromWKT(String aWKTsurface) {
		SurfaceImpl rSurface = null;
		WKTReader wktReader = new WKTReader(this.factory.getPrimitiveFactory(), this.factory.getCoordinateFactory());
		try {
			rSurface = (SurfaceImpl) wktReader.read(aWKTsurface);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return rSurface;
	}
	
	private PointImpl createPointA() {
		String wktPoint = "POINT(30 50)";
		return this.createPointFromWKT(wktPoint);
	}

	private PointImpl createPointA3D() {
		String wktPoint = "POINT(30 50 10)";
		return this.createPointFromWKT(wktPoint);
	}

	
	private PointImpl createPointB() {
		String wktPoint = "POINT(100 120)";
		return this.createPointFromWKT(wktPoint);
	}

	private PointImpl createPointB3D() {
		String wktPoint = "POINT(100 120 20)";
		return this.createPointFromWKT(wktPoint);
	}

	private PointImpl createPointC() {
		String wktPoint = "POINT(70 20)";
		return this.createPointFromWKT(wktPoint);
	}

	private PointImpl createPointC3D() {
		String wktPoint = "POINT(70 20 25)";
		return this.createPointFromWKT(wktPoint);
	}

	private PointImpl createPointD() {
		String wktPoint = "POINT(70 10)";
		return this.createPointFromWKT(wktPoint);
	}
	
	private MultiPointImpl createMultiPointA() {
		Set<Point> points = new HashSet<Point>();
		points.add(this.createPointA());
		points.add(this.createPointB());
		points.add(this.createPointC());
		points.add(this.createPointD());
		return (MultiPointImpl) this.factory.getAggregateFactory().createMultiPoint(points);
	}
	
	private MultiPointImpl createMultiPointA3D() {
		Set<Point> points = new HashSet<Point>();
		points.add(this.createPointA3D());
		points.add(this.createPointB3D());
		points.add(this.createPointC3D());
		return (MultiPointImpl) this.factory.getAggregateFactory().createMultiPoint(points);
	}
	
	
	private CurveImpl createCurveA() {
		String wktCurve1 = "CURVE(30 20, 10 50, 100 120, 100 70, 10 140)";
		return this.createCurveFromWKT(wktCurve1);
	}
	
	private CurveImpl createCurveB() {
		String wktCurve1 = "CURVE(30 20, 50 20, 80 20)";
		return this.createCurveFromWKT(wktCurve1);
	}
	
	private CurveImpl createCurveC() {
		String wktCurve1 = "CURVE(40 60, 40 30, 40 10)";
		return this.createCurveFromWKT(wktCurve1);
	}

	private CurveImpl createCurveD() {
		String wktCurve1 = "CURVE(70 20, 100 20, 100 50)";
		return this.createCurveFromWKT(wktCurve1);
	}

	private CurveImpl createCurveX() {
		String wktCurve1 = "CURVE(70 20, 100 20, 100 50, 120 60, 130 80, 120 90, 130 100, 140 110, 200 220, -50 220, -60 210, -120 100, 0 0)";
		return this.createCurveFromWKT(wktCurve1);
	}
	
	private MultiCurveImpl createMultiCurveA() {
		Set<OrientableCurve> curves = new HashSet<OrientableCurve>();
		curves.add(this.createCurveA());
		curves.add(this.createCurveB());
		curves.add(this.createCurveC());
		curves.add(this.createCurveD());
		return (MultiCurveImpl) this.factory.getAggregateFactory().createMultiCurve(curves);
	}

	private SurfaceImpl createSurfaceAwithoutHole() {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90))";
		return this.createSurfaceFromWKT(wktSurface1);
	}
	
	private SurfaceImpl createSurfaceAwithHole() {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60))";
		return this.createSurfaceFromWKT(wktSurface1);
	}

	private SurfaceImpl createSurfaceAwithTwoHoles() {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60), (30 100, 30 120, 50 120, 50 100, 30 100))";
		return this.createSurfaceFromWKT(wktSurface1);
	}

	private SurfaceImpl createSurfaceBwithoutHole() {
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((100 10, 70 50, 90 100, 160 140, 200 90, 170 20, 100 10))";
		return this.createSurfaceFromWKT(wktSurface1);
	}

	private SurfaceImpl createSurfaceX() {
		String wktSurface1 = "SURFACE ((0 0, 100 0, 100 100, 0 100, 0 0))";
		return this.createSurfaceFromWKT(wktSurface1);
	}
	
	private MultiSurfaceImpl createMultiSurfaceA() {
		Set<OrientableSurface> surfaces = new HashSet<OrientableSurface>();
		surfaces.add(this.createSurfaceAwithoutHole());
		surfaces.add(this.createSurfaceBwithoutHole());
		return (MultiSurfaceImpl) this.factory.getAggregateFactory().createMultiSurface(surfaces);
	}


	
	
	
	
	
}
