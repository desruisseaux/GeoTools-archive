package org.geotools.geometry.iso.coordinate;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;

/**
 * @author sanjay
 *
 */
public class EnvelopeTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testEnvelope1(tGeomFactory);
		
	}	

	private void _testEnvelope1(FeatGeomFactoryImpl aGeomFactory) {
		
		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();

		
		// CoordinateFactory.createDirectPosition(double[])
		DirectPositionImpl dp1 = tCoordFactory.createDirectPosition(new double[] {0, 0});
		DirectPositionImpl dp2 = tCoordFactory.createDirectPosition(new double[] {100, 100});

		DirectPositionImpl dp0 = tCoordFactory.createDirectPosition(new double[] {100, 100});
		
		// DirectPosition.equals(DirectPosition)
		assertTrue(dp2.equals(dp0));

		// Envelope.getDimension()
		assertTrue(dp2.getDimension() == 2);
		System.out.println("Dimension of dp1: " + dp2.getDimension());
		
		EnvelopeImpl env1 = new EnvelopeImpl(dp1, dp2);
		
		// Envelope.getLowerCorner() + Envelope.equals(DP, tol)
		assertTrue(env1.getLowerCorner().equals(dp1, 0));
		System.out.println(env1.getLowerCorner());
		
		// Envelope.getUpperCorner() + Envelope.equals(DP, tol)
		assertTrue(env1.getUpperCorner().equals(dp2, 0));
		System.out.println(env1.getUpperCorner());
		System.out.println(env1);
		
		EnvelopeImpl env2 = new EnvelopeImpl(env1);
		System.out.println(env2);
		
		// Envelope.equals(Envelope)
		assertTrue(env1.equals(env2));
		
		
		DirectPositionImpl dp3 = tCoordFactory.createDirectPosition(new double[] {0,0});
		DirectPositionImpl dp4 = tCoordFactory.createDirectPosition(new double[] {100,50});
		DirectPositionImpl dp5 = tCoordFactory.createDirectPosition(new double[] {100.01,50});
		DirectPositionImpl dp6 = tCoordFactory.createDirectPosition(new double[] {50,100});
		DirectPositionImpl dp7 = tCoordFactory.createDirectPosition(new double[] {50,100.01});
		
		// Envelope.contains(DirectPosition)
		System.out.println("Contains Method for " + env1);
		assertTrue(env1.contains(dp3) == true);
		System.out.println(dp3 + " liegt im Envelope: " + env1.contains(dp3));
		assertTrue(env1.contains(dp4) == true);
		System.out.println(dp4 + " liegt im Envelope: " + env1.contains(dp4));
		assertTrue(env1.contains(dp5) == false);
		System.out.println(dp5 + " liegt im Envelope: " + env1.contains(dp5));
		assertTrue(env1.contains(dp6) == true);
		System.out.println(dp6 + " liegt im Envelope: " + env1.contains(dp6));
		assertTrue(env1.contains(dp7) == false);
		System.out.println(dp7 + " liegt im Envelope: " + env1.contains(dp7));

//		DirectPositionImpl dp8 = tCoordFactory.createDirectPosition(new double[] {200,200});
//		
//		EnvelopeImpl env2 = new EnvelopeImpl(dp6, dp8);
//		EnvelopeImpl env3 = new EnvelopeImpl(dp7, dp8);
//		
//		System.out.println(env1 + " intersects with " + env2 + " : " + env1.intersects(env2));
//		System.out.println(env1 + " intersects with " + env3 + " : " + env1.intersects(env3));
		
		System.out.println("TEST EXPAND");
		env1 = tCoordFactory.createEnvelope(dp1.getCoordinates());
		System.out.println(env1);
		env1.expand(dp2.getCoordinates());
		System.out.println(env1);
		env1.expand(dp5.getCoordinates());
		System.out.println(env1);
		
		// TODO Test Intersects
		
		
	}

	
}
