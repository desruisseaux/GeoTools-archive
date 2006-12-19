package iso.operations;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.io.wkt.ParseException;
import org.geotools.geometry.iso.io.wkt.WKTReader;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.opengis.spatialschema.geometry.Boundary;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.complex.CompositePoint;
import org.opengis.spatialschema.geometry.complex.CompositeSurface;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;

public class ClosureTest extends TestCase {

	private FeatGeomFactoryImpl factory = null;

	public void testMain() {
		
		this.factory = FeatGeomFactoryImpl.getDefault2D();
		
		// Test Curves
		this._testAll();
	}
	
	private void _testAll() {
		
		
		// Point
		CompositePoint cp = (CompositePoint) this.createPoint().getClosure();
		//System.out.println(cp);
		
		// Curve
		CompositeCurve cc = (CompositeCurve) this.createCurve().getClosure();
		//System.out.println(cc);
		
		// Surface
		CompositeSurface cs = (CompositeSurface) this.createSurface().getClosure();
		//System.out.println(cs);

		
		// Complexes
		CompositePoint ncp = (CompositePoint) cp.getClosure();
		assertTrue(ncp == cp);
		//System.out.println(ncp);

		CompositeCurve ncc = (CompositeCurve) cc.getClosure();
		assertTrue(ncc == cc);
		//System.out.println(ncc);
		
		CompositeSurface ncs = (CompositeSurface) cs.getClosure();
		assertTrue(ncs == cs);
		//System.out.println(ncs);
		
		// Boundaries
		
		Complex c = null;
		Boundary b = null;

		b = this.createCurve().getBoundary();
		c = b.getClosure();
		assertTrue(b == c);

		b = this.createSurface().getBoundary();
		c = b.getClosure();
		assertTrue(b == c);

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
	
	
	private PointImpl createPoint() {
		String wktPoint = "POINT(30 50)";
		return this.createPointFromWKT(wktPoint);
	}
	
	
	private CurveImpl createCurve() {
		String wktCurve1 = "CURVE(150.0 100.0, 160.0 140.0, 180.0 100.0, 170.0 120.0)";
		return this.createCurveFromWKT(wktCurve1);
	}
	
	private SurfaceImpl createSurface() {
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90))";
		return this.createSurfaceFromWKT(wktSurface1);
	}
	
	

}
