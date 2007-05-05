package org.geotools.geometry.iso.coordinate;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.GeometryFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;

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
		
		GeometryFactoryImpl tCoordFactory = aGeomFactory.getGeometryFactoryImpl();

		
		// CoordinateFactory.createDirectPosition(double[])
		DirectPositionImpl dp1 = tCoordFactory.createDirectPosition(new double[] {0, 0});
		DirectPositionImpl dp2 = tCoordFactory.createDirectPosition(new double[] {100, 100});

		DirectPositionImpl dp0 = tCoordFactory.createDirectPosition(new double[] {100, 100});
		
		// DirectPosition.equals(DirectPosition)
		assertTrue(dp2.equals(dp0));

		// Envelope.getDimension()
		assertTrue(dp2.getDimension() == 2);
		//System.outprintln("Dimension of dp1: " + dp2.getDimension());
		
		EnvelopeImpl env1 = new EnvelopeImpl(dp1, dp2);
		
		// Envelope.getLowerCorner() + Envelope.equals(DP, tol)
		assertTrue(env1.getLowerCorner().equals(dp1));
		//System.outprintln(env1.getLowerCorner());
		
		// Envelope.getUpperCorner() + Envelope.equals(DP, tol)
		assertTrue(env1.getUpperCorner().equals(dp2));
		//System.outprintln(env1.getUpperCorner());
		//System.outprintln(env1);
		
		EnvelopeImpl env2 = new EnvelopeImpl(env1);
		//System.outprintln(env2);
		
		// Envelope.equals(Envelope)
		assertTrue(env1.equals(env2));
		
		
		DirectPositionImpl dp3 = tCoordFactory.createDirectPosition(new double[] {0,0});
		DirectPositionImpl dp4 = tCoordFactory.createDirectPosition(new double[] {100,50});
		DirectPositionImpl dp5 = tCoordFactory.createDirectPosition(new double[] {100.01,50});
		DirectPositionImpl dp6 = tCoordFactory.createDirectPosition(new double[] {50,100});
		DirectPositionImpl dp7 = tCoordFactory.createDirectPosition(new double[] {50,100.01});
		
		// Envelope.contains(DirectPosition)
		//System.outprintln("Contains Method for " + env1);
		assertTrue(env1.contains(dp3) == true);
		//System.outprintln(dp3 + " liegt im Envelope: " + env1.contains(dp3));
		assertTrue(env1.contains(dp4) == true);
		//System.outprintln(dp4 + " liegt im Envelope: " + env1.contains(dp4));
		assertTrue(env1.contains(dp5) == false);
		//System.outprintln(dp5 + " liegt im Envelope: " + env1.contains(dp5));
		assertTrue(env1.contains(dp6) == true);
		//System.outprintln(dp6 + " liegt im Envelope: " + env1.contains(dp6));
		assertTrue(env1.contains(dp7) == false);
		//System.outprintln(dp7 + " liegt im Envelope: " + env1.contains(dp7));

//		DirectPositionImpl dp8 = tCoordFactory.createDirectPosition(new double[] {200,200});
//		
//		EnvelopeImpl env2 = new EnvelopeImpl(dp6, dp8);
//		EnvelopeImpl env3 = new EnvelopeImpl(dp7, dp8);
//		
//		//System.outprintln(env1 + " intersects with " + env2 + " : " + env1.intersects(env2));
//		//System.outprintln(env1 + " intersects with " + env3 + " : " + env1.intersects(env3));
		
		//System.outprintln("TEST EXPAND");
		env1 = tCoordFactory.createEnvelope(dp1.getCoordinates());
		//System.outprintln(env1);
		env1.expand(dp2.getCoordinates());
		//System.outprintln(env1);
		env1.expand(dp5.getCoordinates());
		//System.outprintln(env1);
		
		// TODO Test Intersects		
	}

	
}
