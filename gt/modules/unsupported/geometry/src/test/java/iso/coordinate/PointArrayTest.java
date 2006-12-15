package iso.coordinate;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.PointArrayImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.PointArray;

public class PointArrayTest extends TestCase {

	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._test1(tGeomFactory);
		
	}
	
	
	private void _test1(FeatGeomFactoryImpl aGeomFactory) {
		
		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		
		PositionImpl p1 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-50,  0}));
		PositionImpl p2 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-30,  30}));
		PositionImpl p3 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{0,  50}));
		PositionImpl p4 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{30,  30}));
		PositionImpl p5 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{50,  0}));

		List<PositionImpl> posList = new ArrayList<PositionImpl>();
		
		PointArray pa = null;
		
		// Testing Illegal Constructor call
		try {
			pa = tCoordFactory.createPointArray(posList);
		} catch (IllegalArgumentException e) {
			//
		}
		assertTrue(pa==null);
		
		posList.add(p1);
		posList.add(p2);
		posList.add(p3);
		posList.add(p4);
		posList.add(p5);
		
		// Legal Constructor call
		pa = tCoordFactory.createPointArray(posList);
				
		// PointArray.length()
		assertTrue(pa.length() == 5);
		// PointArray.positions()
		assertTrue(pa.positions().size() == 5);

		// get-method creates new DP instance
		DirectPosition dp = pa.get(0, null);
		System.out.println(dp);
		assertTrue(dp.getOrdinate(0) == -50);
		assertTrue(dp.getOrdinate(1) == 0);
		
		DirectPosition newDp = pa.get(4, dp);
		System.out.println(dp);
		assertTrue(dp.getOrdinate(0) == 50);
		assertTrue(dp.getOrdinate(1) == 0);
		// get-method uses the same DirectPosition without creating new instance
		assertTrue(newDp == dp);
		
		DirectPosition dp2 = tCoordFactory.createDirectPosition(new double[]{5, 5});
		pa.set(4, dp2);
		newDp = pa.get(4, dp);
		System.out.println(dp);
		assertTrue(dp.getOrdinate(0) == 5);
		assertTrue(dp.getOrdinate(1) == 5);
		// Check if the values were copied and not referenced (by modifying the ordinates)
		dp2.setOrdinate(0, 2);
		newDp = pa.get(4, dp);
		System.out.println(dp);
		assertTrue(dp.getOrdinate(0) == 5);
		
		double[] coord = ((PointArrayImpl)pa).getCoordinate(0);
		System.out.print(coord[0] + "|" + coord[1]);
		
		// .isEmpty() and remove(int)
		assertTrue(!((PointArrayImpl)pa).isEmpty());
		((PointArrayImpl)pa).removePosition(((PointArrayImpl)pa).get(3));
		((PointArrayImpl)pa).remove(0);
		((PointArrayImpl)pa).remove(0);
		((PointArrayImpl)pa).remove(0);
		((PointArrayImpl)pa).remove(0);
		assertTrue(((PointArrayImpl)pa).isEmpty());

	}
	
}
