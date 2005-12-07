package org.geotools.referencing.operation.transform;

import org.opengis.referencing.operation.TransformException;

import junit.framework.TestCase;

public class NZMGTransformTest extends TestCase {

	public void testInverseTransform() throws TransformException {
		double[] src = new double[]{
			2487100.638,6751049.719,2486533.395,6077263.661,2216746.425,5388508.765
		};
		double[] exp = new double[]{
			-34.444066,172.739194,-40.512409,172.723106,-46.651295,169.172062
		};
		double[] dst = new double[6];
		
		new NZMGTransform().inverseTransform(src,0,dst,0,3);
		
		for (int i = 0; i < exp.length; i++) {
			assertEquals(exp[i],dst[i],0.0001);
		}

		
	}
}
