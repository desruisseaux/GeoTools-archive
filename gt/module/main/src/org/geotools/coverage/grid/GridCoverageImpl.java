/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.grid;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.media.jai.PropertySource;

import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridNotEditableException;
import org.opengis.coverage.grid.GridPacking;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.util.InternationalString;

public class GridCoverageImpl extends org.geotools.coverage.grid.GridCoverage {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	private MathTransform transform;
	
	private Envelope envelope;
	
	/**
	 * @param coverage
	 * @throws FactoryException 
	 * @throws NoSuchElementException 
	 * @throws OperationNotFoundException 
	 */
	public GridCoverageImpl(org.geotools.coverage.grid.GridCoverage coverage) throws OperationNotFoundException, NoSuchElementException, FactoryException {
		super(coverage);

//		transform = createTransform(crs);
	}

	/**
	 * @param name
	 * @param crs
	 * @param source
	 * @param properties
	 * @throws FactoryException 
	 * @throws NoSuchElementException 
	 * @throws OperationNotFoundException 
	 */
	public GridCoverageImpl(String name, CoordinateReferenceSystem crs,
			PropertySource source, Map properties, BufferedImage image, Envelope envelope) throws OperationNotFoundException, NoSuchElementException, FactoryException {
		super(name, crs, source, properties);
		this.image = image;
		this.envelope = envelope;
		
//		transform = createTransform(crs);		
	}
	
//	protected MathTransform createTransform(CoordinateReferenceSystem crs) throws OperationNotFoundException, NoSuchElementException, FactoryException {
//		return FactoryFinder.getCoordinateOperationFactory().createOperation(crs, EngineeringCRS.CARTESIAN_2D).getMathTransform();
//	}
	
	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#isDataEditable()
	 */
	public boolean isDataEditable() {
		return image.isTileWritable(0,0);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getGridPacking()
	 */
	public GridPacking getGridPacking() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getGridGeometry()
	 */
	public GridGeometry getGridGeometry() {

		GridRange range = new org.geotools.coverage.grid.GridRange(image);
		boolean[] inverse = { false, false };
		
		return new org.geotools.coverage.grid.GridGeometry(range, envelope, inverse);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getOptimalDataBlockSizes()
	 */
	public int[] getOptimalDataBlockSizes() {
		return image.getRaster().getSampleModel().getSampleSize();
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getNumOverviews()
	 */
	public int getNumOverviews() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getOverviewGridGeometry(int)
	 */
	public GridGeometry getOverviewGridGeometry(int arg0) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getOverview(int)
	 */
	public GridCoverage getOverview(int arg0) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, boolean[])
	 */
	public boolean[] getDataBlock(GridRange range, boolean[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new boolean[range.getLength(0) + range.getLength(1)];
		}
		int[] temp = new int[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = (temp[i] == 0);
		}
		
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, byte[])
	 */
	public byte[] getDataBlock(GridRange range, byte[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new byte[range.getLength(0) + range.getLength(1)];
		}
		int[] temp = new int[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = (byte) temp[i];
		}
		
		return destination;	
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, short[])
	 */
	public short[] getDataBlock(GridRange range, short[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new short[range.getLength(0) + range.getLength(1)];
		}
		int[] temp = new int[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = (short) temp[i];
		}
		
		return destination;	
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, int[])
	 */
	public int[] getDataBlock(GridRange range, int[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new int[range.getLength(0) + range.getLength(1)];
		}
		int[] temp = new int[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = temp[i];
		}
		
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, float[])
	 */
	public float[] getDataBlock(GridRange range, float[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new float[range.getLength(0) + range.getLength(1)];
		}
		float[] temp = new float[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = temp[i];
		}
		
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, double[])
	 */
	public double[] getDataBlock(GridRange range, double[] destination) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		if (destination == null) {
			destination = new double[range.getLength(0) + range.getLength(1)];
		}
		double[] temp = new double[range.getLength(0) + range.getLength(1)];

		image.getRaster().getPixels(range.getLower(0), range.getLower(1),
				range.getLength(0), range.getLength(1), temp);
		
		for (int i = 0; i < temp.length; i++) {
			destination[i] = temp[i];
		}
		
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getPackedDataBlock(org.opengis.coverage.grid.GridRange)
	 */
	public byte[] getPackedDataBlock(GridRange arg0) throws InvalidRangeException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, boolean[])
	 */
	public void setDataBlock(GridRange range, boolean[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		int[] temp = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			temp[i] = values[i] ? 1 : 0; 
		}
		
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), temp);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, byte[])
	 */
	public void setDataBlock(GridRange range, byte[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		int[] temp = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			temp[i] = (int) values[i]; 
		}
		
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), temp);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, short[])
	 */
	public void setDataBlock(GridRange range, short[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		int[] temp = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			temp[i] = (int) values[i]; 
		}
		
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), temp);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, int[])
	 */
	public void setDataBlock(GridRange range, int[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), values);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, float[])
	 */
	public void setDataBlock(GridRange range, float[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), values);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, double[])
	 */
	public void setDataBlock(GridRange range, double[] values) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		image.getRaster().setPixels(range.getLower(0), range.getLower(1), 
				range.getLength(0), range.getLength(1), values);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setPackedDataBlock(org.opengis.coverage.grid.GridRange, byte[])
	 */
	public void setPackedDataBlock(GridRange arg0, byte[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getDimensionNames()
	 */
	public InternationalString[] getDimensionNames() {
		List results = new ArrayList();
		
		results.addAll(Arrays.asList(crs.getCoordinateSystem().getAxis(0).getIdentifiers()));
		results.addAll(Arrays.asList(crs.getCoordinateSystem().getAxis(1).getIdentifiers()));
		
		InternationalString[] strings = new InternationalString[results.size()];
		
		for (int i = 0; i < results.size(); i++) {
			Identifier identifier = (Identifier) results.get(i);
			strings[i] = new SimpleInternationalString(identifier.getCode());
		}

		return strings;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getNumSampleDimensions()
	 */
	public int getNumSampleDimensions() {
		return image.getRaster().getNumBands();
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getSampleDimension(int)
	 */
	public SampleDimension getSampleDimension(int index) throws IndexOutOfBoundsException {
		//TODO this is not right.
		return new org.geotools.coverage.SampleDimension(new String[] { "Alpha", "Red", "Green", "Blue" },
				new Color[] { new Color(0,0,0,0), Color.RED, Color.GREEN, Color.BLUE });
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getSources()
	 */
	public List getSources() {
		return Collections.singletonList(this);
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#evaluate(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public Object evaluate(DirectPosition point) throws CannotEvaluateException {
		double[] results = new double[2];
		
		DirectPosition transformedPoint = (DirectPosition) point.clone();
		
		try {
			if (!point.getCoordinateReferenceSystem().equals(crs)) {
//				createTransform(point.getCoordinateReferenceSystem()).transform(point, transformedPoint);
			} else {
				transform.transform(point, transformedPoint);
			}
		} catch (Exception e) {
			throw new CannotEvaluateException("Exception occured while transforming.", e);
		} 
		image.getRaster().getPixel( (int) point.getCoordinates()[0], (int) point.getCoordinates()[1], results);
		
		return results;
	}
	
	public RenderedImage getRenderedImage() {
		return image;
	}
	public Envelope getEnvelope() {
		return envelope;
	}
	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}
}