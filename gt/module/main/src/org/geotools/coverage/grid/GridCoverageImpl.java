/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.media.jai.PropertySource;

import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.EngineeringCRS;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridNotEditableException;
import org.opengis.coverage.grid.GridPacking;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.util.InternationalString;

public class GridCoverageImpl extends org.geotools.coverage.grid.GridCoverage {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	private MathTransform transform;
	
	/**
	 * @param coverage
	 * @throws FactoryException 
	 * @throws NoSuchElementException 
	 * @throws OperationNotFoundException 
	 */
	public GridCoverageImpl(org.geotools.coverage.grid.GridCoverage coverage) throws OperationNotFoundException, NoSuchElementException, FactoryException {
		super(coverage);

		transform = createTransform(crs);
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
			PropertySource source, Map properties, BufferedImage image) throws OperationNotFoundException, NoSuchElementException, FactoryException {
		super(name, crs, source, properties);
		this.image = image;
		
		transform = createTransform(crs);		
	}
	
	protected MathTransform createTransform(CoordinateReferenceSystem crs) throws OperationNotFoundException, NoSuchElementException, FactoryException {
		return FactoryFinder.getCoordinateOperationFactory().createOperation(crs, EngineeringCRS.CARTESIAN_2D).getMathTransform();
	}
	
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
		// TODO Auto-generated method stub
		return null;
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
	public byte[] getDataBlock(GridRange arg0, byte[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, short[])
	 */
	public short[] getDataBlock(GridRange arg0, short[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, int[])
	 */
	public int[] getDataBlock(GridRange arg0, int[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, float[])
	 */
	public float[] getDataBlock(GridRange arg0, float[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#getDataBlock(org.opengis.coverage.grid.GridRange, double[])
	 */
	public double[] getDataBlock(GridRange arg0, double[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
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
	public void setDataBlock(GridRange arg0, boolean[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, byte[])
	 */
	public void setDataBlock(GridRange arg0, byte[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, short[])
	 */
	public void setDataBlock(GridRange arg0, short[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, int[])
	 */
	public void setDataBlock(GridRange arg0, int[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, float[])
	 */
	public void setDataBlock(GridRange arg0, float[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#setDataBlock(org.opengis.coverage.grid.GridRange, double[])
	 */
	public void setDataBlock(GridRange arg0, double[] arg1) throws InvalidRangeException, GridNotEditableException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
//		return crs.getCoordinateSystem().getAxis(0).getI;
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
				createTransform(point.getCoordinateReferenceSystem()).transform(point, transformedPoint);
			} else {
				transform.transform(point, transformedPoint);
			}
		} catch (Exception e) {
			throw new CannotEvaluateException("Exception occured while transforming.", e);
		} 
		image.getRaster().getPixel( (int) point.getCoordinates()[0], (int) point.getCoordinates()[1], results);
		
		return results;
	}
}