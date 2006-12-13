package iso.operations;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.io.wkt.ParseException;
import org.geotools.geometry.iso.io.wkt.WKTReader;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;

import GeometryVisualization.PaintGMObject;


public class DisplayGeometry {
	
	public static void main(String[] args) {
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		DisplayGeometry d = new DisplayGeometry();
		
		SurfaceImpl s1 = null;
		SurfaceImpl s2 = null;
		
		s1= d.createSurfaceAwithTwoHoles(tGeomFactory);
		draw(s1);

		s2= d.createSurfaceBwithHole(tGeomFactory);
		draw(s2);

		GeometryImpl g = null;
		g =	(GeometryImpl) s1.difference(s2);
		draw(g);

		g = (GeometryImpl) s1.symmetricDifference(s2);
		draw(g);
		
		g = (GeometryImpl) s1.union(s2);
		draw(g);
		

	}
	
	public static void draw(GeometryImpl g) {
		PaintGMObject.paint(g);
	}

	
	
	private SurfaceImpl createSurfaceAwithoutHole(FeatGeomFactoryImpl aGeomFactory) {
		SurfaceImpl rSurface = null;
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}
	
	private SurfaceImpl createSurfaceAwithHole(FeatGeomFactoryImpl aGeomFactory) {
		SurfaceImpl rSurface = null;
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceAwithTwoHoles(FeatGeomFactoryImpl aGeomFactory) {
		SurfaceImpl rSurface = null;
		String wktSurface1 = "SURFACE ((10 90, 30 50, 70 30, 120 40, 150 70, 150 120, 100 150, 30 140, 10 90), (90 60, 110 100, 120 90, 100 60, 90 60), (30 100, 30 120, 50 120, 50 100, 30 100))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceBwithoutHole(FeatGeomFactoryImpl aGeomFactory) {
		SurfaceImpl rSurface = null;
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((100 10, 70 50, 90 100, 160 140, 200 90, 170 20, 100 10))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
	}

	private SurfaceImpl createSurfaceBwithHole(FeatGeomFactoryImpl aGeomFactory) {
		SurfaceImpl rSurface = null;
		// Clockwise oriented
		String wktSurface1 = "SURFACE ((100 10, 70 50, 90 100, 160 140, 200 90, 170 20, 100 10), (120 30, 110 50, 120 80, 170 80, 160 40, 120 30))";
		return this.createSurfaceFromWKT(aGeomFactory, wktSurface1);
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
	
	
}
