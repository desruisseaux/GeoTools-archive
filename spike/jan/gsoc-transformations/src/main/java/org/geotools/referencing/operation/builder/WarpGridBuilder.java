/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.operation.builder;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.media.jai.RasterFactory;
import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.algorithm.AbstractInterpolation;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.operation.transform.WarpGridTransform2D;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

/**
 * Provides a basic implementation for
 * {@linkplain WarpGridTransform2D warp grid math transform} builders.
 * 
 * @see <A
 *      HREF="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/WarpGrid.html">WarpGrid
 *      at JAI </A>
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/spike/jan/gsoc-transformations/src/main/java/org/geotools/referencing/operation/builder/WarpGridBuilder.java $
 * @version $Id$
 * @author jezekjan
 * 
 */
public abstract class WarpGridBuilder extends MathTransformBuilder {
	/**
	 * Grid width
	 */
	private int width;

	/**
	 * Grid height
	 */
	private int height;

	/**
	 * Envelope for generated Grid
	 */
	private Envelope envelope;

	/**
	 * List of Mapped Positions in local coordinates (transformed by worldToGrid
	 * transformation)
	 */
	List<MappedPosition> localpositions = new ArrayList<MappedPosition>();

	// List /*<MappedPosition>*/ worldpositions ;

	/** GridValues like maxx maxt dx dy etc.. */
	GridParamValues globalValues;

	/** RealToGrid Math Transform */
	MathTransform worldToGrid;

	/** Grid of x shifts */
	private float[][] dxgrid;

	/** Grid of y shifts */
	private float[][] dygrid;

	/** Warp positions - target position of each point (not delta) */
	private float[] warpPositions;

	/** Raster of interpolated values */
	private WritableRaster xRaster;

	/** Raster of interpolated values */
	private WritableRaster yRaster;

	/**
	 * Constructs Builder
	 * 
	 * @param vectors
	 *            Mapped positions
	 * @param dx
	 *            The horizontal spacing between grid cells.
	 * @param dy
	 *            The vertical spacing between grid cells.
	 * @param envelope
	 *            Envelope of generated grid.
	 * @throws MismatchedSizeException
	 * @throws MismatchedDimensionException
	 * @throws MismatchedReferenceSystemException
	 * @throws NoSuchIdentifierException
	 */
	public WarpGridBuilder(List<MappedPosition> vectors, double dx, double dy,
			Envelope envelope, MathTransform worldToGrid)
			throws MismatchedSizeException, MismatchedDimensionException,
			MismatchedReferenceSystemException, TransformException,
			NoSuchIdentifierException {
		this.worldToGrid = worldToGrid;

		this.globalValues = new GridParamValues(envelope, worldToGrid, dx, dy);

		super.setMappedPositions(vectors);

		// super.setMappedPositions(transformMPToGrid(vectors, realToGrid));
		localpositions = transformMPToGrid(vectors, worldToGrid);
		this.envelope = envelope;
	}

	public List<MappedPosition> getGridMappedPositions()
			throws MismatchedDimensionException, TransformException {
		if (localpositions == null) {
			localpositions = transformMPToGrid(getMappedPositions(),
					worldToGrid);
		}

		return localpositions;
	}

	/**
	 * Converts MappedPosition to HashMap where Source Points are key and delta
	 * in proper dimension is value
	 * 
	 * @param dim
	 *            delta dimension (0 - dx, 1 - dy)
	 * @return Map
	 * @throws TransformException
	 */
	protected HashMap<DirectPosition, Double> buildPositionsMap(int dim)
			throws TransformException {
		HashMap<DirectPosition, Double> poitnsToDeltas = new HashMap<DirectPosition, Double>();

		for (Iterator<MappedPosition> i = this.getGridMappedPositions()
				.iterator(); i.hasNext();) {
			MappedPosition mp = ((MappedPosition) i.next());
			poitnsToDeltas.put(mp.getSource(),
					mp.getSource().getCoordinates()[dim]
							- mp.getTarget().getCoordinates()[dim]);
		}

		return poitnsToDeltas;
	}

	/**
	 * Transforms MappedPostions to grid system
	 * 
	 */
	private List<MappedPosition> transformMPToGrid(
			List<MappedPosition> MappedPositions, MathTransform trans)
			throws MismatchedDimensionException, TransformException {
		List<MappedPosition> gridmp = new ArrayList<MappedPosition>();

		for (Iterator<MappedPosition> i = MappedPositions.iterator(); i
				.hasNext();) {
			MappedPosition mp = (MappedPosition) i.next();

			DirectPosition2D gridSource = new DirectPosition2D();
			DirectPosition2D gridTarget = new DirectPosition2D();
			trans.transform(mp.getSource(), gridSource);
			gridSource
					.setCoordinateReferenceSystem(DefaultEngineeringCRS.CARTESIAN_2D);

			trans.transform(mp.getTarget(), gridTarget);
			gridTarget
					.setCoordinateReferenceSystem(DefaultEngineeringCRS.CARTESIAN_2D);

			gridmp.add(new MappedPosition(gridSource, gridTarget));
		}

		return gridmp;
	}

	private void ensureVectorsInsideEnvelope() {
		/* @TODO - ensure that source MappedPositions are inside the envelope */
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.referencing.operation.builder.MathTransformBuilder#computeMathTransform()
	 */
	protected MathTransform computeMathTransform() throws FactoryException {
		warpPositions = getWarpPositions();

		/** Add the last parameter - warp positions * */
		globalValues.WarpGridParameters.parameter("warpPositions").setValue(
				warpPositions);

		/** Use provider and values to get proper transformation * */
		WarpGridTransform2D wt = (WarpGridTransform2D) (new WarpGridTransform2D.Provider())
				.createMathTransform(globalValues.getWarpGridParameters());

		wt.setWorldtoGridTransform(this.worldToGrid);

		MathTransform trans = null;

		try {
			trans = ConcatenatedTransform.create(ConcatenatedTransform.create(
					this.worldToGrid, wt), this.worldToGrid.inverse());
		} catch (TransformException e) {
			throw new FactoryException(Errors
					.format(ErrorKeys.NONINVERTIBLE_TRANSFORM), e);
		}
	
		return wt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.referencing.operation.builder.MathTransformBuilder#getMinimumPointCount()
	 */
	public int getMinimumPointCount() {
		return 1;
	}

	/**
	 * Computes WarpGrid Positions.
	 * 
	 * @throws TransformException
	 *             when problems in world to grid system occurs
	 * 
	 */
	abstract protected float[] computeWarpGrid(ParameterValueGroup values)
			throws TransformException, FactoryException;

	/**
	 * Convenience method for generating the grid by interpolation. This method
	 * should be called from implementation of computeWarpGrid.
	 * 
	 * @param WarpParams
	 * @param dxInterp
	 * @param dyInterp
	 * @return
	 * @throws TransformException
	 */
	protected float[] interpolateWarpGrid(ParameterValueGroup WarpParams,
			AbstractInterpolation dxInterp, AbstractInterpolation dyInterp)
			throws TransformException {
		float[] warpPositions = (float[]) WarpParams.parameter("warpPositions")
				.getValue();

		for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
			for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
				DirectPosition2D dp = new DirectPosition2D(
						WarpParams.parameter("xStart").intValue()
								+ (j * WarpParams.parameter("xStep").intValue()),
						WarpParams.parameter("yStart").intValue()
								+ (i * WarpParams.parameter("yStep").intValue()));

				double x = -dxInterp.getValue(dp)
						+ (j * WarpParams.parameter("xStep").intValue())
						+ WarpParams.parameter("xStart").intValue();
				double y = -dyInterp.getValue(dp)
						+ (i * WarpParams.parameter("yStep").intValue())
						+ WarpParams.parameter("yStart").intValue();

				warpPositions[(i * ((1 + WarpParams.parameter("xNumCells")
						.intValue()) * 2))
						+ (2 * j)] = (float) x;

				warpPositions[(i * ((1 + WarpParams.parameter("xNumCells")
						.intValue()) * 2))
						+ (2 * j) + 1] = (float) y;
			}
		}

		return warpPositions;
	}

	/**
	 * Returns array of warp grid positions. The array contains target positions
	 * to original grid. The cells are enumerated in row-major order, that is,
	 * all the grid points along a row are enumerated first, then the grid
	 * points for the next row are enumerated, and so on. As an example, suppose
	 * xNumCells is equal to 2 and yNumCells is equal 1. Then the order of the
	 * data in table would be: x00, y00, x10, y10, x20, y20, x01, y01, x11, y11,
	 * x21, y21
	 * 
	 * @see <A
	 *      HREF="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/WarpGrid.html">warpPositions
	 *      at WarpGrid constructor at JAI </A>
	 * @return warp positions
	 * 
	 */
	private float[] getWarpPositions() throws FactoryException {
		if (warpPositions == null) {
			try {
				warpPositions = computeWarpGrid(globalValues.WarpGridParameters);
			} catch (TransformException e) {
				throw new FactoryException(Errors
						.format(ErrorKeys.CANT_TRANSFORM_VALID_POINTS), e);
			}

			return warpPositions;
		} else {
			return warpPositions;
		}
	}

	/**
	 * Returns array of Shifts. This method is useful to create Coverage2D
	 * object.
	 * 
	 * @return array of Shifts
	 */
	public float[][] getDxGrid() throws FactoryException {
		if ((dxgrid == null) || (dxgrid.length == 0)) {
			ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
			final int xNumCells = WarpParams.parameter("xNumCells").intValue();
			final int yNumCells = WarpParams.parameter("yNumCells").intValue();
			final int xStep = WarpParams.parameter("xStep").intValue();
			final int yStep = WarpParams.parameter("yStep").intValue();
			final int xStart = WarpParams.parameter("xStart").intValue();
			final int yStart = WarpParams.parameter("yStart").intValue();

			final float[] warpPositions;

			warpPositions = getWarpPositions();

			dxgrid = new float[yNumCells + 1][xNumCells + 1];

			for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
				for (int j = 0; j <= WarpParams.parameter("xNumCells")
						.intValue(); j++) {
					dxgrid[WarpParams.parameter("yNumCells").intValue() - i][j] = (float) warpPositions[(int) ((i
							* (1 + xNumCells) * 2) + (2 * j))]
							- (j * xStep) - xStart;
				}
			}
		}

		return dxgrid;
	}

	/**
	 * Returns array of Shifts. This method is useful to create Coverage2D
	 * object.
	 * 
	 * @return array of Shifts
	 */
	public float[][] getDyGrid() throws FactoryException {
		if ((dygrid == null) || (dygrid.length == 0)) {
			ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
			final int xNumCells = WarpParams.parameter("xNumCells").intValue();
			final int yNumCells = WarpParams.parameter("yNumCells").intValue();
			final int xStep = WarpParams.parameter("xStep").intValue();
			final int yStep = WarpParams.parameter("yStep").intValue();
			final int xStart = WarpParams.parameter("xStart").intValue();
			final int yStart = WarpParams.parameter("yStart").intValue();

			final float[] warpPositions;

			warpPositions = getWarpPositions();

			dygrid = new float[yNumCells + 1][xNumCells + 1];

			for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
				for (int j = 0; j <= WarpParams.parameter("xNumCells")
						.intValue(); j++) {
					dygrid[WarpParams.parameter("yNumCells").intValue() - i][j] = (float) warpPositions[(int) ((i
							* (1 + xNumCells) * 2)
							+ (2 * j) + 1)]
							- (i * yStep) - yStart;
				}
			}
		}

		return dygrid;
	}

	/**
	 * 
	 * @param dim
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public File writeDeltaFile(int dim, String path) throws IOException,
			FactoryException {
		ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
		final float[] warpPositions = (float[]) WarpParams.parameter(
				"warpPositions").getValue();
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		/* Print the header */
		osw.write("Warp Grid transformation");
		osw.write("\n");

		/*
		 * Print first row (number of columns, number of rows, number of
		 * z–values (always one), minimum longitude, cell size, minimum
		 * latitude, cell size, and not used. )
		 */
		osw.write((WarpParams.parameter("xNumCells").intValue() + 1) + " "
				+ (WarpParams.parameter("yNumCells").intValue() + 1) + " "
				+ "1 " + WarpParams.parameter("xStart").doubleValue() + " "
				+ WarpParams.parameter("xStep").doubleValue() + " "
				+ WarpParams.parameter("yStart").doubleValue() + " "
				+ WarpParams.parameter("yStep").doubleValue() + " 0");

		// osw.write("\n");
		int ii = 0;

		if (dim == 0) {
			for (int i = getDxGrid().length - 1; i >= 0; i--) {
				osw.write(String.valueOf("\n"));

				for (int j = 0; j < getDxGrid()[i].length; j++) {
					osw.write(String.valueOf(getDxGrid()[i][j]) + " ");
				}
			}
		} else if (dim == 1) {
			for (int i = getDyGrid().length - 1; i >= 0; i--) {
				osw.write(String.valueOf("\n"));

				for (int j = 0; j < getDyGrid()[i].length; j++) {
					osw.write(String.valueOf(getDyGrid()[i][j]) + " ");
				}
			}
		} else {
			throw new IndexOutOfBoundsException(Double.toString(dim));
		}

		osw.close();

		return file;
	}

	/**
	 * Converts warp positions from float[] containing target positions to
	 * float[][] containing deltas.
	 * 
	 */
	/*
	 * public static void warpPosToDeltas(int xStart, int xStep, int xNumCells,
	 * int yStart, int yStep, int yNumCells, float[][] yDeltas, float[][]
	 * xDeltas) { /* @TODO
	 */

	/**
	 * Recalculates Deltas to Warp Positions (target position of each grid cell)
	 * 
	 * @param xStart
	 * @param xStep
	 * @param xNumCells
	 * @param yStart
	 * @param yStep
	 * @param yNumCells
	 * @param yDeltas
	 * @param xDeltas
	 * @return
	 */
	/*
	 * public static float[] deltasToWarpPos(int xStart, int xStep, int
	 * xNumCells, int yStart, int yStep, int yNumCells, float[][] yDeltas,
	 * float[][] xDeltas) { float[] warpPos = new float[(xNumCells + 1) *
	 * (yNumCells + 1) * 2];
	 * 
	 * for (int i = 0; i < yNumCells; i++) { for (int j = 0; j < xNumCells; j++) {
	 * warpPos[(2 * j) + (xNumCells * i * 2)] = xStart + (j * xStep) +
	 * xDeltas[i][j]; warpPos[(2 * j) + (xNumCells * i * 2) + 1] = yStart + (i *
	 * yStep) + yDeltas[i][j]; } }
	 * 
	 * return warpPos; }
	 */
	
	public WritableRaster getDxRaster() throws FactoryException {
		float[][] values;
		values = getDxGrid();
		WritableRaster raster = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_FLOAT, values[0].length, values.length, 1,
				new Point(0, 0));
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[0].length; j++) {

				raster.setSample(j, i, 0, values[i][j]);

			}
		}

		return raster;
	}

	public WritableRaster getDyRaster() throws FactoryException {
		float[][] values;
		values = getDyGrid();
		WritableRaster raster = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_FLOAT, values[0].length, values.length, 1,
				new Point(0, 0));
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[0].length; j++) {

				raster.setSample(j, i, 0, values[i][j]);

			}
		}

		return raster;
	}

	public WritableRaster getDxDyRaster() throws FactoryException {
		float[][] valuesDx;
		valuesDx = getDxGrid();
		float[][] valuesDy;
		valuesDy = getDyGrid();
		WritableRaster raster = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_FLOAT, valuesDx[0].length, valuesDx.length, 2,
				new Point(0, 0));
		for (int i = 0; i < valuesDx.length; i++) {
			for (int j = 0; j < valuesDx[0].length; j++) {

				raster.setSample(j, i, 0, valuesDx[i][j]);
				raster.setSample(j, i, 1, valuesDy[i][j]);

			}
		}

		return raster;
	}


	/**
	 * 
	 * @return Number of y Grid Cells
	 */
	public int getHeight() {
		return globalValues.getWarpGridParameters().parameter("yNumCells")
				.intValue();
	}

	/**
	 * Sets grid height
	 * 
	 * @param height
	 *            grid height
	 */
	public void setHeight(int height) {
		this.height = height;

		try {
			this.globalValues = new GridParamValues(envelope, worldToGrid,
					this.envelope.getLength(0) / width, this.envelope
							.getLength(1)
							/ height);
		} catch (NoSuchIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.dxgrid = null;
		this.dygrid = null;
		this.warpPositions = null;
	}

	/**
	 * 
	 * @return Number of grid cells width
	 */
	public int getWidth() {
		return globalValues.getWarpGridParameters().parameter("xNumCells")
				.intValue();
	}

	/**
	 * Sets grid width
	 * 
	 * @param width
	 *            width of grid
	 */
	public void setWidth(int width) {
		this.width = width;

		try {
			this.globalValues = new GridParamValues(envelope, worldToGrid,
					this.envelope.getLength(0) / width, this.envelope
							.getLength(1)
							/ height);
		} catch (NoSuchIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.dxgrid = null;
		this.dygrid = null;
		this.warpPositions = null;
	}

	/**
	 * 
	 * @return Envelope (in real world coordinates)
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	public MathTransform getWorldToGrid() {
		MathTransform mt = null;

		mt = this.globalValues.getGridToCRS();

		return mt;
	}

	public void setEnvelope(Envelope envelope) {
		cleanTransformationVars();
		this.envelope = envelope;
	}

	/**
	 * Cleans all variable to force the recalculation of grid.
	 */
	protected void cleanTransformationVars() {
		this.warpPositions = null;
		this.dxgrid = null;
		this.dygrid = null;
		this.localpositions = null;
	}

	public void setMappedPositions(List<MappedPosition> positions)
			throws MismatchedSizeException, MismatchedDimensionException,
			MismatchedReferenceSystemException {
		// TODO Auto-generated method stub
		cleanTransformationVars();

		super.setMappedPositions(positions);
	}

	/**
	 * Takes care about parameters
	 * 
	 * @author jezekjan
	 * 
	 */
	private static class GridParamValues {
		private ParameterValueGroup WarpGridParameters;

		private MathTransform trans;

		/**
		 * Constructs GridParamValues from such properties.
		 * 
		 * @param env
		 *            Envelope
		 * @param trans
		 *            Transformation to Grid CRS.
		 * @param dx
		 *            x step
		 * @param dy
		 *            y step
		 * @throws TransformException
		 */
		public GridParamValues(Envelope env, MathTransform trans, double dx,
				double dy) throws TransformException, NoSuchIdentifierException {
			Envelope dxdy = new Envelope2D(env.getCoordinateReferenceSystem(),
					env.getMinimum(0), env.getMinimum(1), dx, dy);

			/* Transforms dx, dy and envelope to grid system */
			dxdy = CRS.transform(trans, dxdy);

			Envelope gridEnv = CRS.transform(trans, env);

			WarpGridParameters = new ParameterGroup(
					WarpGridTransform2D.Provider.PARAMETERS);
			WarpGridParameters.parameter("xStart").setValue(
					(new Double(gridEnv.getMinimum(0))).intValue());
			WarpGridParameters.parameter("yStart").setValue(
					(new Double(gridEnv.getMinimum(1))).intValue());
			WarpGridParameters.parameter("xStep").setValue(
					new Double(Math.ceil(dxdy.getLength(0))).intValue());
			WarpGridParameters.parameter("yStep").setValue(
					new Double(Math.ceil(dxdy.getLength(1))).intValue());
			WarpGridParameters.parameter("xNumCells")
					.setValue(
							new Double(Math.ceil(gridEnv.getLength(0)
									/ WarpGridParameters.parameter("xStep")
											.intValue())).intValue());
			WarpGridParameters.parameter("yNumCells")
					.setValue(
							new Double(Math.ceil(gridEnv.getLength(1)
									/ WarpGridParameters.parameter("yStep")
											.intValue())).intValue());

			WarpGridParameters.parameter("warpPositions").setValue(
					new float[2
							* (WarpGridParameters.parameter("xNumCells")
									.intValue() + 1)
							* (WarpGridParameters.parameter("yNumCells")
									.intValue() + 1)]);

			this.trans = calculateWorldToGrid(WarpGridParameters, gridEnv, env,
					(AffineTransform2D) trans);
		}

		/**
		 * Calculates final world to grid transformation. This transformation is
		 * partly defined by {@link trans} and than by dividing the {@code env}
		 * by {@code params}
		 * 
		 * @param params
		 * @param env
		 * @param trans
		 * @return
		 */
		private MathTransform calculateWorldToGrid(ParameterValueGroup params,
				Envelope gridenv, Envelope wenv, AffineTransform2D trans) {
			GeneralMatrix M = new GeneralMatrix(3, 3);
			// double xScale =gridenv.getLength(0)/
			// (WarpGridParameters.parameter("xNumCells").intValue());
			// double yScale = gridenv.getLength(1)/
			// WarpGridParameters.parameter("yNumCells").intValue();

			double gridXLenght = WarpGridParameters.parameter("xStep")
					.doubleValue()
					* WarpGridParameters.parameter("xNumCells").doubleValue();
			double gridYLenght = WarpGridParameters.parameter("yStep")
					.doubleValue()
					* WarpGridParameters.parameter("yNumCells").doubleValue();

			double[] m0 = {
					WarpGridParameters.parameter("xStep").doubleValue(),
					0,
					WarpGridParameters.parameter("xStart").doubleValue()
							- WarpGridParameters.parameter("xStep")
									.doubleValue() / 2 };
			double[] m1 = {
					0,
					-WarpGridParameters.parameter("yStep").doubleValue(),
					WarpGridParameters.parameter("yStart").doubleValue()
							+ gridYLenght
							+ WarpGridParameters.parameter("yStep")
									.doubleValue() / 2 };
			double[] m2 = { 0, 0, 1 };
			M.setRow(0, m0);
			M.setRow(1, m1);
			M.setRow(2, m2);

			// ProjectiveTransform.create(M);
			MathTransform conTrans = null;
			AffineTransform2D at = (AffineTransform2D) ProjectiveTransform
					.create(M);
			try {

				conTrans = (ConcatenatedTransform.create(at, trans.inverse()));
			} catch (NoninvertibleTransformException e) {
			}
			return conTrans;
		}

		/**
		 * Sets the grid warp positions in
		 * 
		 * @param warpPos
		 *            array of grid warp positions
		 */
		public void setWarpGridPositions(float[] warpPos) {
			WarpGridParameters.parameter("warpPositions").setValue(warpPos);
		}

		/**
		 * Returns warp grid positions.
		 * 
		 * @return warp grid positions
		 */
		public ParameterValueGroup getWarpGridParameters() {
			return WarpGridParameters;
		}

		public MathTransform getGridToCRS() {
			return trans;
		}
	}

}
