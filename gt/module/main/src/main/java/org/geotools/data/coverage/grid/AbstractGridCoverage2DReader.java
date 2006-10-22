/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.coverage.grid;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.IHSColorSpace;
import javax.media.jai.PlanarImage;
import javax.units.Unit;
import javax.units.UnitFormat;

import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.BufferedCoordinateOperationFactory;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.resources.CRSUtilities;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * This class is a first attempt for providing a way to get more informations
 * out of a single 2D raster datasets (x,y). It is worth to remark that for the
 * moment this is thought for 2D rasters not for 3D or 4D rasters (x,y,z,t).
 * 
 * <p>
 * The main drawback I see with the current GeoApi GridCoverageReader interface
 * is that there is no way to get real information about a raster source unless
 * you instantiate a GridCoverage. As an instance it is impossible to know the
 * envelope, the number of overviews, the tile size. This information is needed
 * in order to perform decimation on reading or to use built-in overviews<br>
 * This really impacts the ability to exploit raster datasets in a desktop
 * environment where caching is crucial.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * @version 0.2
 */
public abstract class AbstractGridCoverage2DReader implements
		GridCoverageReader {

	/** Caches a default GridCoverageFactory for usage in plugins. */
	protected final static GridCoverageFactory coverageFactory = FactoryFinder
			.getGridCoverageFactory(null);

	protected static final double EPS = 1E-6;

	/** Buffere factory for coordinate operations. */
	protected final static CoordinateOperationFactory operationFactory = new BufferedCoordinateOperationFactory(
			new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));

	/**
	 * Default color ramp. Preset colors used to generate an Image from the raw
	 * data
	 */
	protected final static Color[] demColors = new Color[] {
			new Color(5, 90, 5), new Color(150, 200, 150),
			new Color(190, 150, 20), new Color(100, 100, 50),
			new Color(200, 210, 220), Color.WHITE, Color.WHITE, Color.WHITE,
			Color.WHITE };

	/**
	 * This contains the maximum number of grid coverages in the file/stream.
	 * Until multi-image files are supported, this is going to be 0 or 1.
	 */
	protected volatile int numOverviews = 0;

	/** 2DGridToWorld math transform. */
	protected MathTransform raster2Model = null;

	/** crs for this coverage */
	protected CoordinateReferenceSystem crs = null;

	/** Envelope read from file */
	protected GeneralEnvelope originalEnvelope = null;

	/** Coverage name */
	protected String coverageName = "geotools_coverage";


	/** Source to read from */
	protected Object source = null;

	protected Hints hints = new Hints(new HashMap(5));

	protected double[] highestRes = null;

	protected GeneralGridRange originalGridRange = null;

	protected ImageInputStream inStream = null;

	protected double[][] overViewResolutions = null;

	// -------------------------------------------------------------------------
	//
	// Proposed new methods
	//
	// -------------------------------------------------------------------------
	// /**
	// * Allows any resources held by this object to be released.
	// */
	// public abstract void dispose();
	//
	// /**
	// * It tells me if the underlying data set is able to generate overvies at
	// * all resolutions (e.g. wavelet compressed raster formats).
	// *
	// * @return
	// */
	// public abstract boolean hasArbitraryOverviews();
	//
	// /**
	// * Number of predetermined overviews for the grid.
	// *
	// * @return The number of predetermined overviews for the grid.
	// */
	// public int getNumOverviews() {
	// return numOverviews;
	// }
	//
	// /**
	// * Returns the grid geometry for an overview.
	// *
	// * @param overviewIndex
	// * Overview index for which to retrieve grid geometry. Indices
	// * start at 0.
	// * @return The grid geometry for an overview.
	// * @throws IndexOutOfBoundsException
	// * if {@code overviewIndex} is out of bounds.
	// */
	//
	// public abstract GridGeometry getOverviewGridGeometry(int overviewIndex)
	// throws IndexOutOfBoundsException;
	//
	// /**
	// * Returns a pre-calculated overview for a grid coverage. The overview
	// * indices are numbered from 0 to
	// * <code>{@linkplain #getNumOverviews numberOverviews}-1</code>. The
	// * overviews are ordered from highest (index 0) to lowest
	// (<code>{@linkplain #getNumOverviews numberOverviews}-1</code>)
	// * resolution. Overview grid coverages will have overviews which are the
	// * overviews for the grid coverage with lower resolution than the
	// overview.
	// * For example, a 1 meter grid coverage with 3, 9, and 27 meter overviews
	// * will be ordered as in the left side below. The 3 meter overview will
	// have
	// * 2 overviews as in the right side below:
	// *
	// * <blockquote><table border=0>
	// * <tr>
	// * <th align="center">1 meter GC</th>
	// * <th>&nbsp;</th>
	// * <th align="center">3 meter overview</th>
	// * </tr>
	// * <tr>
	// * <td valign="top"><table border=0 align="center">
	// * <tr>
	// * <th>Index&nbsp;</th>
	// * <th>&nbsp;resolution</th>
	// * </tr>
	// * <tr>
	// * <td align="center">0</td>
	// * <td align="center"> 3</td>
	// * </tr>
	// * <tr>
	// * <td align="center">1</td>
	// * <td align="center"> 9</td>
	// * </tr>
	// * <tr>
	// * <td align="center">2</td>
	// * <td align="center">27</td>
	// * </tr>
	// * </table></td>
	// *
	// *
	// <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
	// * <td valign="top"><table border=0 align="center">
	// * <tr>
	// * <th>Index&nbsp;</th>
	// * <th>&nbsp;resolution</th>
	// * </tr>
	// * <tr>
	// * <td align="center">0</td>
	// * <td align="center"> 9</td>
	// * </tr>
	// * <tr>
	// * <td align="center">1</td>
	// * <td align="center">27</td>
	// * </tr>
	// * </table></td>
	// * </table></blockquote>
	// *
	// * @param overviewIndex
	// * Index of grid coverage overview to retrieve. Indexes start at
	// * 0.
	// * @return a pre-calculated overview for a grid coverage.
	// * @throws IndexOutOfBoundsException
	// * if {@code overviewIndex} is out of bounds.
	// */
	// public abstract GridCoverage getOverview(int overviewIndex)
	// throws IndexOutOfBoundsException;
	//
	// /**
	// * Returns the width of a tile in the given image.
	// *
	// * <p>
	// * The default implementation simply returns
	// * <code>getWidth(imageIndex)</code>, which is correct for non-tiled
	// * images. Readers that support tiling should override this method.
	// *
	// * @return the width of a tile.
	// *
	// * @param imageIndex
	// * the index of the image to be queried.
	// *
	// * @exception IllegalStateException
	// * if the input source has not been set.
	// * @exception IndexOutOfBoundsException
	// * if the supplied index is out of bounds.
	// * @exception IOException
	// * if an error occurs during reading.
	// */
	// public abstract int getTileHeight(int imageIndex) throws IOException;
	//
	// /**
	// * Returns the X coordinate of the upper-left corner of tile (0, 0) in the
	// * given image.
	// *
	// * <p>
	// * A reader for which the tile grid X offset always has the same value
	// * (usually 0), may return the value without accessing any image data. In
	// * such cases, it is not necessary to throw an exception even if no input
	// * source has been set or the image index is out of bounds.
	// *
	// * <p>
	// * The default implementation simply returns 0, which is correct for
	// * non-tiled images and tiled images in most formats. Readers that support
	// * tiling with non-(0, 0) offsets should override this method.
	// *
	// * @return the X offset of the tile grid.
	// *
	// * @param imageIndex
	// * the index of the image to be queried.
	// *
	// * @exception IllegalStateException
	// * if an input source is required to determine the return
	// * value, but none has been set.
	// * @exception IndexOutOfBoundsException
	// * if an image must be accessed to determine the return
	// * value, but the supplied index is out of bounds.
	// * @exception IOException
	// * if an error occurs during reading.
	// */
	// public abstract int getTileGridXOffset(int imageIndex) throws
	// IOException;
	//
	// /**
	// * Returns the Y coordinate of the upper-left corner of tile (0, 0) in the
	// * given image.
	// *
	// * <p>
	// * A reader for which the tile grid Y offset always has the same value
	// * (usually 0), may return the value without accessing any image data. In
	// * such cases, it is not necessary to throw an exception even if no input
	// * source has been set or the image index is out of bounds.
	// *
	// * <p>
	// * The default implementation simply returns 0, which is correct for
	// * non-tiled images and tiled images in most formats. Readers that support
	// * tiling with non-(0, 0) offsets should override this method.
	// *
	// * @return the Y offset of the tile grid.
	// *
	// * @param imageIndex
	// * the index of the image to be queried.
	// *
	// * @exception IllegalStateException
	// * if an input source is required to determine the return
	// * value, but none has been set.
	// * @exception IndexOutOfBoundsException
	// * if an image must be accessed to determine the return
	// * value, but the supplied index is out of bounds.
	// * @exception IOException
	// * if an error occurs during reading.
	// */
	// public abstract int getTileGridYOffset(int imageIndex) throws
	// IOException;
	//
	// /**
	// * Returns the width of a tile in the given image.
	// *
	// * <p>
	// * The default implementation simply returns
	// * <code>getWidth(imageIndex)</code>, which is correct for non-tiled
	// * images. Readers that support tiling should override this method.
	// *
	// * @return the width of a tile.
	// *
	// * @param imageIndex
	// * the index of the image to be queried.
	// *
	// * @exception IllegalStateException
	// * if the input source has not been set.
	// * @exception IndexOutOfBoundsException
	// * if the supplied index is out of bounds.
	// * @exception IOException
	// * if an error occurs during reading.
	// */
	// public abstract int getTileWidth(int imageIndex) throws IOException;
	//
	// /**
	// * It tells
	// *
	// * @param imageindex
	// * @return
	// */
	// public abstract boolean isTiled(int imageindex);
	//
	// /**
	// * Returns the number of tiles in the X direction.
	// *
	// * @return the number of tiles in the X direction.
	// */
	// public abstract int getNumXTiles();
	//	
	// /**
	// * Returns the number of tiles in the Y direction.
	// *
	// * @return the number of tiles in the Y direction.
	// */
	// public abstract int getNumYTiles();
	//
	// /**
	// *
	// * @param interpolation
	// * @param nOverviews
	// * @param extender
	// * @throws UnsupportedOperationException
	// */
	// public abstract void buildOverviews(Interpolation interpolation,
	// int nOverviews, BorderExtender extender)
	// throws UnsupportedOperationException;

	// -------------------------------------------------------------------------
	//
	// old support methods
	//
	// -------------------------------------------------------------------------
	/**
	 * This method is responsible for preparing the read param
	 * 
	 * @param readP
	 * @param requestedEnvelope
	 * @param dim
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	protected Integer setReadParams(ImageReadParam readP,
			GeneralEnvelope requestedEnvelope, Rectangle dim)
			throws IOException, TransformException {

		readP.setSourceSubsampling(1, 1, 0, 0);// default values for
		// subsampling
		// //
		//
		// Default image index 0
		//
		// //
		Integer imageChoice = new Integer(0);

		// //
		//
		// Check Hint to ignore overviews
		//
		// //
		Object o = hints.get(Hints.IGNORE_COVERAGE_OVERVIEW);
		if (o != null && ((Boolean) o).booleanValue()) {
			return imageChoice;

		}

		// //
		//
		// Am I going to decimate or to use overviews? If this geotiff has only
		// one page we use decimation, otherwise we use the best page avalabile.
		// Future versions should use both.
		//
		// //
		final boolean decimate = (numOverviews <= 0) ? true : false;

		// //
		//
		// Resolution requested. I am here computing the resolution required by
		// the user.
		//
		// //
		double[] requestedRes = getResolution(requestedEnvelope, dim, crs);
		if (requestedRes == null)
			return imageChoice;

		// //
		//
		// overviews or decimation
		//
		// //
		if (!decimate) {
			// /////////////////////////////////////////////////////////////////////
			// OVERVIEWS
			// /////////////////////////////////////////////////////////////////////
			// Should we leave now? In case the resolution of the first level is
			// already lower than the requested one we should use the first
			// level and leave.
			if (highestRes[0] - requestedRes[0] > EPS
					&& highestRes[1] - requestedRes[1] > EPS)
				return imageChoice;

			// Should we leave now? In case the resolution of the first level is
			// already lower than the requested one we should use the first
			// level and leave.
			int axis = 0;
			if (requestedRes[0] - requestedRes[1] > EPS)
				axis = 1;

			// //
			//
			// looking for the owerview with the highest lower resolution
			// compared
			// to the requested one.
			// This ensure more speed but less quality. In the future we should
			// provide a hint to control this behaviour.
			//
			// //
			double actRes;
			int i = 0;
			for (; i < numOverviews; i++) {
				actRes = (axis == 0) ? overViewResolutions[i][0]
						: overViewResolutions[i][1];
				// is actual resolution lower than the requeste resolution?
				if (actRes - requestedRes[axis] > EPS)
					break;
			}
			// checking that we did not exceeded the maximum number of pages.
			if (i == numOverviews) {
				// int subsamplingFactor=
				imageChoice = new Integer(numOverviews);
			} else
				// keeping the first image at highest resolution into account in
				// order to get the overview wit
				imageChoice = new Integer(i + 1);
		}
		// /////////////////////////////////////////////////////////////////////
		// DECIMATION ON READING
		// /////////////////////////////////////////////////////////////////////
		decimationOnReadingControl(imageChoice, readP, requestedRes);
		return imageChoice;
	}

	/**
	 * This method is responsible for evaluating possible subsampling factors
	 * once the best resolution level has been found, in case we have support
	 * for overviews, or starting from the original coverage in case there are
	 * no overviews availaible.
	 * 
	 * Anyhow this methof should not be called directly but subclasses should
	 * make use of the setReadParams method instead in order to transparently
	 * look for overviews.
	 * 
	 * @param imageChoice
	 * @param readP
	 * @param requestedRes
	 * 
	 */
	protected final void decimationOnReadingControl(Integer imageChoice,
			ImageReadParam readP, double[] requestedRes) {
		{

			int w, h;
			double selectedRes[] = new double[2];
			final int choice = imageChoice.intValue();
			if (choice == 0) {
				// highest resolution
				w = originalGridRange.getLength(0);
				h = originalGridRange.getLength(1);
				selectedRes[0] = highestRes[0];
				selectedRes[1] = highestRes[1];
			} else {
				// some overview
				selectedRes[0] = overViewResolutions[choice - 1][0];
				selectedRes[1] = overViewResolutions[choice - 1][1];
				w = (int) Math.round(originalEnvelope.getLength(0)
						/ selectedRes[0]);
				h = (int) Math.round(originalEnvelope.getLength(1)
						/ selectedRes[1]);

			}
			// /////////////////////////////////////////////////////////////////////
			// DECIMATION ON READING
			// Setting subsampling factors with some checkings
			// 1) the subsampling factors cannot be zero
			// 2) the subsampling factors cannot be such that the w or h are
			// zero
			// /////////////////////////////////////////////////////////////////////
			if (requestedRes == null) {
				readP.setSourceSubsampling(1, 1, 0, 0);

			} else {
				int subSamplingFactorX = (int) Math.floor(requestedRes[0]
						/ selectedRes[0]);
				subSamplingFactorX = subSamplingFactorX == 0 ? 1
						: subSamplingFactorX;

				while (w / subSamplingFactorX <= 0 && subSamplingFactorX >= 0)
					subSamplingFactorX--;
				subSamplingFactorX = subSamplingFactorX == 0 ? 1
						: subSamplingFactorX;
				int subSamplingFactorY = (int) Math.floor(requestedRes[1]
						/ selectedRes[1]);
				subSamplingFactorY = subSamplingFactorY == 0 ? 1
						: subSamplingFactorX;

				while (h / subSamplingFactorY <= 0 && subSamplingFactorY >= 0)
					subSamplingFactorY--;
				subSamplingFactorY = subSamplingFactorY == 0 ? 1
						: subSamplingFactorY;
				readP.setSourceSubsampling(subSamplingFactorX,
						subSamplingFactorY, 0, 0);
			}

		}
	}

	/**
	 * @param image
	 * 
	 * 
	 * @throws IOException
	 */
	protected final GridCoverage createImageCoverage(PlanarImage image)
			throws IOException {

		// deciding the number range
		NumberRange geophysicRange = null;

		switch (image.getSampleModel().getTransferType()) {
		case DataBuffer.TYPE_BYTE:
			geophysicRange = new NumberRange(0, 255);

			break;

		case DataBuffer.TYPE_USHORT:
			geophysicRange = new NumberRange(0, 65535);

			break;
		// going to treat following cases as DEM
		case DataBuffer.TYPE_INT:
			geophysicRange = new NumberRange(Integer.MIN_VALUE,
					Integer.MAX_VALUE);
		case DataBuffer.TYPE_SHORT:
			geophysicRange = new NumberRange(Short.MIN_VALUE, Short.MAX_VALUE);
		case DataBuffer.TYPE_DOUBLE:
			geophysicRange = new NumberRange(Double.MIN_VALUE, Double.MAX_VALUE);
		case DataBuffer.TYPE_FLOAT:
			geophysicRange = new NumberRange(Float.MIN_VALUE, Float.MAX_VALUE);
			return prepareDEMCoverage(image, new GeneralEnvelope(
					originalEnvelope));
		default:
			throw new DataSourceException(
					"createImageCoverage:Data buffer type not supported by this world image reader! Use byte, ushort or int");
		}

		/**
		 * Now we shuld be able to create the sample dimensions and the
		 * categories for this coverage in a much better and meaningful way
		 * using the color model and the color space type.
		 * 
		 * @todo How do we handle the NoData when it is 0?
		 * 
		 */
		// convenieience category in order to
		final Category values = new Category("values",
				new Color[] { Color.BLACK }, geophysicRange,
				LinearTransform1D.IDENTITY);

		// creating bands
		final int numBands = image.getSampleModel().getNumBands();
		final GridSampleDimension[] bands = new GridSampleDimension[numBands];
		// checking the names
		final ColorModel cm = image.getColorModel();
		final String names[] = new String[numBands];
		// in case of index color model we are already done.
		if (cm instanceof IndexColorModel) {
			names[0] = "index band";
		} else {
			// in case of multiband image we are not done yet.
			final ColorSpace cs = cm.getColorSpace();

			if (cs instanceof IHSColorSpace) {
				names[0] = "Intensity band";
				names[1] = "Hue band";
				names[2] = "Saturation band";

			} else {
				/**
				 * 
				 * 
				 * @TODO we need to support more types than the ones we have
				 *       here.
				 * 
				 * 
				 */
				// not IHS, let's take the type
				final int type = cs.getType();
				switch (type) {
				case ColorSpace.CS_GRAY:
				case ColorSpace.TYPE_GRAY:
					names[0] = "grayscale band";
					break;
				case ColorSpace.CS_sRGB:
				case ColorSpace.CS_LINEAR_RGB:
				case ColorSpace.TYPE_RGB:
					names[0] = "Red band";
					names[1] = "Green band";
					names[2] = "Blue band";
					break;
				case ColorSpace.TYPE_CMY:
					names[0] = "Cyan band";
					names[1] = "Magenta band";
					names[2] = "Yellow band";
					break;
				case ColorSpace.TYPE_CMYK:
					names[0] = "Cyan band";
					names[1] = "Magenta band";
					names[2] = "Yellow band";
					names[3] = "K band";
					break;

				}
			}
		}
		// setting bands names.
		for (int i = 0; i < numBands; i++) {

			bands[i] = new GridSampleDimension(names[i],
					new Category[] { values }, null).geophysics(true);
		}

		// creating coverage
		// if (raster2Model != null)
		// return FactoryFinder.getGridCoverageFactory(null).create(
		// coverageName, image, crs, raster2Model, bands, null, null);
		return coverageFactory.create(coverageName, image, new GeneralEnvelope(
				originalEnvelope), bands, null, null);

	}

	protected GridCoverage prepareDEMCoverage(PlanarImage image,
			GeneralEnvelope envelope) {
		// Create the SampleDimension, with colors and byte transformation
		// needed for visualization
		final UnitFormat unitFormat = UnitFormat.getStandardInstance();
		Unit uom = null;

		// unit of measure is meter usually, is this a good guess?
		try {
			uom = unitFormat.parseUnit("m");
		} catch (ParseException e) {
			uom = null;
		}

		final Category values = new Category("elevation", demColors,
				new NumberRange(2, 10), new NumberRange(-1, 8849));

		final GridSampleDimension band = new GridSampleDimension(
				"digital elevation", new Category[] { values }, uom)
				.geophysics(true);

		return FactoryFinder.getGridCoverageFactory(null)
				.create(coverageName, image, envelope,
						new GridSampleDimension[] { band }, null, null);

	}

	/**
	 * This method is responsible for computing the resolutions in for the
	 * provided grid geometry in the provided crs.
	 * 
	 * <P>
	 * It is worth to note that the returned resolution array is of length of 2
	 * and it always is lon, lat for the moment.<br>
	 * It might be worth to remove the axes reordering code when we are
	 * confident enough with the code to handle the north-up crs.
	 * <p>
	 * TODO use orthodromic distance?
	 * 
	 * @param envelope the GeneralEnvelope
	 * @param dim
	 * @param crs
	 * @throws DataSourceException
	 */
	protected final double[] getResolution(GeneralEnvelope envelope,
			Rectangle2D dim, CoordinateReferenceSystem crs)
			throws DataSourceException {
		double[] requestedRes = null;
		try {
			if (dim != null && envelope != null) {
				// do we need to transform the originalEnvelope?
				final CoordinateReferenceSystem crs2D = CRSUtilities
						.getCRS2D(envelope.getCoordinateReferenceSystem());

				if (crs != null
						&& !CRSUtilities.equalsIgnoreMetadata(crs, crs2D)) {
					final MathTransform tr = operationFactory.createOperation(
							crs2D, crs).getMathTransform();
					if (!tr.isIdentity())
						envelope = CRSUtilities.transform(tr, envelope);
				}
				requestedRes = new double[2];
				requestedRes[0] = envelope.getLength(0) / dim.getWidth();
				requestedRes[1] = envelope.getLength(1) / dim.getHeight();
			}
			return requestedRes;
		} catch (TransformException e) {
			throw new DataSourceException("Unable to get the resolution", e);
		} catch (FactoryException e) {
			throw new DataSourceException("Unable to get the resolution", e);
		}
	}

	public final CoordinateReferenceSystem getCrs() {
		return crs;
	}

	public final GeneralGridRange getOriginalGridRange() {
		return originalGridRange;
	}

	public final GeneralEnvelope getOriginalEnvelope() {
		return originalEnvelope;
	}

	public final Object getSource() {
		return source;
	}

}
