package iso.coordinate;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;

/**
 * @author Sanjay Jena
 *
 */
public class DirectPositionTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault3D();
		
		this._testDP(tGeomFactory.getCoordinateFactory());
		
	}	

	private void _testDP(GeometryFactory aCoordFactory) {
		
		
		double x1 = 10000.00;
		double y1 = 10015.50;
		double z1 = 10031.00;
		
		double coords1[] = new double[]{x1, y1, z1};
		double coords2[] = new double[]{x1, y1};
		double resultCoords[];
		
		// Creating a DP
		DirectPosition dp1 = aCoordFactory.createDirectPosition(coords1);
		
		// getCoordinates()
		resultCoords = dp1.getCoordinates();
		assertTrue(coords1[0] == resultCoords[0]);
		assertTrue(coords1[1] == resultCoords[1]);
		assertTrue(coords1[2] == resultCoords[2]);
		
		// getOrdinate(dim)
		assertTrue(coords1[0] == dp1.getOrdinate(0));
		assertTrue(coords1[1] == dp1.getOrdinate(1));
		assertTrue(coords1[2] == dp1.getOrdinate(2));
		
		// Cloning a DP
		DirectPosition dp2 = (DirectPosition) dp1.clone();
		
		// setOrdinate(dim, value)
		dp1.setOrdinate(0, 10.5);
		dp1.setOrdinate(1, 20.7);
		dp1.setOrdinate(2, -30.666);
		resultCoords = dp1.getCoordinates();
		assertTrue(resultCoords[0] == 10.5);
		assertTrue(resultCoords[1] == 20.7);
		assertTrue(resultCoords[2] == -30.666);
		
		// Test if clone() returned a copy, and not a reference
		// The values of dp2 should not be modified by the previous setOrdinate call in dp1
		resultCoords = dp2.getCoordinates();
		assertTrue(x1 == resultCoords[0]);
		assertTrue(y1 == resultCoords[1]);
		assertTrue(z1 == resultCoords[2]);

		//DirectPosition dp3 = aCoordFactory.createDirectPosition(coords2);
		
		assertTrue(dp1.getDimension() == 3);
		
		
	}

}
