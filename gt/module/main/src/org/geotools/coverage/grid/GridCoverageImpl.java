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

import java.util.List;
import java.util.Map;

import javax.media.jai.PropertySource;

import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridNotEditableException;
import org.opengis.coverage.grid.GridPacking;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.util.InternationalString;

public class GridCoverageImpl extends org.geotools.coverage.grid.GridCoverage {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param coverage
	 */
	public GridCoverageImpl(org.geotools.coverage.grid.GridCoverage coverage) {
		super(coverage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param crs
	 * @param source
	 * @param properties
	 */
	public GridCoverageImpl(String name, CoordinateReferenceSystem crs,
			PropertySource source, Map properties) {
		super(name, crs, source, properties);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverage#isDataEditable()
	 */
	public boolean isDataEditable() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
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
	public boolean[] getDataBlock(GridRange arg0, boolean[] arg1) throws InvalidRangeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getNumSampleDimensions()
	 */
	public int getNumSampleDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getSampleDimension(int)
	 */
	public SampleDimension getSampleDimension(int arg0) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#getSources()
	 */
	public List getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.Coverage#evaluate(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public Object evaluate(DirectPosition arg0) throws CannotEvaluateException {
		// TODO Auto-generated method stub
		return null;
	}
}