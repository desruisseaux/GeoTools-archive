package org.geotools.coverage.processing;

import java.awt.Polygon;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;

import org.geotools.coverage.processing.operation.Histogram;
import org.geotools.parameter.ImagingParameterDescriptors;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 * @deprecated Users should avoid this class, since it is likely to be merged with
 *             {@link OperationJAI} later.
 */
public abstract class AbstractOperationJAIDecorator extends OperationJAI {

	/**
	 * Constructor for an {@link AbstractStatisticsOperationJAI}.
	 * 
	 * <p>
	 * This contructor internally calls the
	 * {@link #prepareParameters(org.opengis.parameter.ParameterValueGroup)}
	 * method in order to prepare the parameters that need to be processed
	 * before being feed to the corresponding JAI operation.
	 * 
	 * <p>
	 * Tipically this facility is important for make JAI operations GeoSpatial
	 * aware. An example could be the {@link Histogram} operation. In
	 * {@link JAI} we have one defined but we have decorated it here in order to
	 * be able to specify ROIs as {@link Polygon} instead of simple Java2D
	 * shapes.
	 * 
	 * @param operation
	 *            provides the {@link OperationDescriptor} for the {@link JAI}
	 *            operation that we want to decorate with geospatial behavioru.
	 */
	protected AbstractOperationJAIDecorator(OperationDescriptor operation) {
		super(operation, prepareParams(operation.getName()));
	}

	/**
	 * 
	 * @param operationDescriptor
	 * @param decorator
	 */
	protected AbstractOperationJAIDecorator(
			OperationDescriptor operationDescriptor,
			ImagingParameterDescriptors decorator) {
		super(operationDescriptor, decorator);
		
	}

	public AbstractOperationJAIDecorator(String name) {
		super(getOperationDescriptor(name), prepareParams(name));
	}

	protected static Map REPLACED_DESCRIPTORS;

	protected static ImagingParameterDescriptors prepareParams(
			String name) {
		return new ImagingParameterDescriptors(
				getOperationDescriptor(name), REPLACED_DESCRIPTORS.values());
	}

}