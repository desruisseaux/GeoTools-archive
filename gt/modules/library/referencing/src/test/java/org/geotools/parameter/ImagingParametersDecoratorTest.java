/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.parameter;

import java.util.HashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RenderedRegistryMode;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.metadata.iso.citation.Citations;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;

/**
 * Testing {@link ImagingParameterDescriptors} decorators.
 * @author Simone Giannecchini
 * @since 2.4.x
 * 
 */
public class ImagingParametersDecoratorTest extends TestCase {

    /**
     * The parameter descriptor for the coordinate reference system.
     */
    public static final ParameterDescriptor SPATIAL_SUBSAMPLING_X =
            new DefaultParameterDescriptor(Citations.OGC, "xPeriod",
                Double.class,    // Value class (mandatory)
                null,                               // Array of valid values
                null,                               // Default value
                null,                               // Minimal value
                null,                               // Maximal value
                null,                               // Unit of measure
                true);                             // Parameter is optional
	/**
	 * 
	 */
	public ImagingParametersDecoratorTest() {
		super("ImagingParametersDecoratorTest");
	}

	public void testClone() {
		////
		//
		// get the descriptors for extrema  JAI operation
		//
		////
		final OperationRegistry registry = JAI.getDefaultInstance()
				.getOperationRegistry();
		final OperationDescriptor operation = (OperationDescriptor) registry
				.getDescriptor(RenderedRegistryMode.MODE_NAME, "Extrema");
		
		////
		//
		// get the OverrideableImagingParameterDescriptors
		// to replace xPeriod
		//
		////
		final Map replacingDescriptors= new HashMap(1);
		replacingDescriptors.put("xPeriod", SPATIAL_SUBSAMPLING_X);
		final ImagingParameterDescriptorsDecorator ripd= 
				new ImagingParameterDescriptorsDecorator(operation,replacingDescriptors);
		////
		//
		// Set the parameter we want to override
		//
		////
		final Parameter  p= (Parameter) SPATIAL_SUBSAMPLING_X.createValue();
		//note that we are supposed to use spatial coordinates for this value we are seeting here. 
		p.setValue(new Double(2.3));
		final Map replacingParameters= new HashMap();
		replacingParameters.put("xPeriod", p);
		final ImagingParametersDecorator rip= new ImagingParametersDecorator(ripd, 
				replacingParameters);
		assertTrue(rip.parameter("xPeriod").toString().startsWith("xPeriod = 2.3"));
		if(TestData.isInteractiveTest())
			System.out.println(rip.parameter("xPeriod").toString());
		
		////
		//		
		//get the descriptor and create a value using the descriptor
		//
		////
		final GeneralParameterDescriptor descriptor = rip.getDescriptor();
		assert descriptor instanceof ImagingParameterDescriptorsDecorator;
		final GeneralParameterValue value = descriptor.createValue();
		assert value instanceof ImagingParametersDecorator;
		
		//print me out the wkt
		if(TestData.isInteractiveTest())
			System.out.println(rip.toWKT());
		
		//clone
		if(TestData.isInteractiveTest())
			System.out.println(((ImagingParametersDecorator)rip.clone()).parameter("xPeriod").toString());

		//clone and equals
		if(TestData.isInteractiveTest())
			System.out.println(((ImagingParametersDecorator)rip.clone()).equals(rip));
	}
}
