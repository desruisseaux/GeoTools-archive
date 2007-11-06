package org.geotools.referencing.operation.builder;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.algorithm.TPSInterpolation;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class InterpolationTest extends TestCase {

	public InterpolationTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
    public void testInterpoaltion(){
    	
    	CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

    	// Define the Envelope for our work; this will be the bounds of the final interpolation
    	GeneralDirectPosition min = new GeneralDirectPosition( 0.0,   0.0 );
    	GeneralDirectPosition max = new GeneralDirectPosition( 100.0, 100.0 );
    	Envelope env = new GeneralEnvelope(min, max);
    

    	
    	// Generate some known points to root the interpolation
    	DirectPosition a = new DirectPosition2D(crs,13,85);
    	DirectPosition b = new DirectPosition2D(crs,14,15);
    	DirectPosition c = new DirectPosition2D(crs,45,78);
    	DirectPosition d = new DirectPosition2D(crs,95,28);

    	
    	// Define at each point the values to be interpolated; we do this in a HashMap
    	HashMap /*<DirectPosition2D, Float>*/ pointsAndValues = new HashMap();
    	pointsAndValues.put(a,  6.5);
    	pointsAndValues.put(b,  1.5);
    	pointsAndValues.put(c, -9.5);
    	pointsAndValues.put(d,  7.5);


    	//now we can construct the Interpolation Object
    	TPSInterpolation interp = new TPSInterpolation(pointsAndValues, 1000, 1000, env);


    	 // We can create and show a coverage image of the interpolation within the Envelope
    	GridCoverageFactory gcf = new GridCoverageFactory();
    	gcf.create("Intepolated Coverage",  interp.get2DGrid(), env).show();

    	
    	// We can also generate an interpolated value at any DirectPosition
    	float myValue = interp.getValue(new DirectPosition2D(crs,12.34,15.123));
    	
    	//TODO --- test case!
    	
    }
    
    public static Test suite() {
        return new TestSuite(InterpolationTest.class);
    }
    
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
