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
package org.geotools.coverage.grid.io;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.IHSColorSpace;
import javax.media.jai.PlanarImage;
import javax.units.Unit;
import javax.units.UnitFormat;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DataSourceException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.resources.CRSUtilities;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
 * @author Simone Giannecchini, GeoSolutions
 * @since 2.3
 * @version 0.2
 */
public abstract class AbstractGridCoverage2DReader implements
		GridCoverageReader {
    
	/** The {@link Logger} for this {@link AbstractGridCoverage2DReader}. */
	private final static Logger LOGGER = Logging
			.getLogger("org.geotools.data.coverage.grid");

	protected static final double EPS = 1E-6;

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

	/** Hints used by the {@link AbstractGridCoverage2DReader} subclasses. */
	protected Hints hints = GeoTools.getDefaultHints();

	/**
	 * Highest resolution availaible for this reader.
	 */
	protected double[] highestRes = null;

	/** Temp variable used in many readers. */
	protected boolean closeMe;

	/**
	 * In case we are trying to read from a GZipped file this will be set to
	 * true.
	 */
	protected boolean gzipped;

	/**
	 * The original {@link GridRange} for the {@link GridCoverage2D} of this
	 * reader.
	 */
	protected GeneralGridRange originalGridRange = null;

	/**
	 * Input stream that can be used to initialize subclasses of
	 * {@link AbstractGridCoverage2DReader}.
	 */
	protected ImageInputStream inStream = null;

	/** Resolutions avialaible through an overviews based mechanism. */
	protected double[][] overViewResolutions = null;

	/**
	 * {@link GridCoverageFactory} instance.
	 */
	protected GridCoverageFactory coverageFactory;

	// -------------------------------------------------------------------------
	//
	// old support methods
	//
	// -------------------------------------------------------------------------
	/**
	 * This method is responsible for preparing the read param for doing an
	 * {@link ImageReader#read(int, ImageReadParam)}.
	 * 
	 * 
	 * <p>
	 * This method is responsible for preparing the read param for doing an
	 * {@link ImageReader#read(int, ImageReadParam)}. It sets the passed
	 * {@link ImageReadParam} in terms of decimation on reading using the
	 * provided requestedEnvelope and requestedDim to evaluate the needed
	 * resolution. It also returns and {@link Integer} representing the index of
	 * the raster to be read when dealing with multipage raster.
	 * 
	 * @param readP
	 *            an instance of {@link ImageReadParam} for setting the
	 *            subsampling factors.
	 * @param requestedEnvelope
	 *            the {@link GeneralEnvelope} we are requesting.
	 * @param requestedDim
	 *            the requested dimensions.
	 * @return the index of the raster to read in the underlying data source.
	 * @throws IOException
	 * @throws TransformException
	 */
	protected Integer setReadParams(ImageReadParam readP,
			GeneralEnvelope requestedEnvelope, Rectangle requestedDim)
			throws IOException, TransformException {

		readP.setSourceSubsampling(1, 1, 0, 0);// default values for
		// subsampling
		// //
		//
		// Default image index 0
		//
		// //
		Integer imageChoice = new Integer(0);

		// we are able to handle overviews properly only if the transformation
		// is
		// an affine transform with pure scale and translation, no rotational
		// components
		if (raster2Model != null && !isScaleTranslate(raster2Model))
			return imageChoice;

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
		double[] requestedRes = getResolution(requestedEnvelope, requestedDim,
				crs);
		if (requestedRes == null)
			return imageChoice;

		// //
		//
		// overviews or decimation
		//
		// //
		if (!decimate) {
			return getOverviewImage(null, requestedRes);
		} else {
    		// /////////////////////////////////////////////////////////////////////
    		// DECIMATION ON READING
    		// /////////////////////////////////////////////////////////////////////
    		decimationOnReadingControl(imageChoice, readP, requestedRes);
    		return imageChoice;
		}
	}

	private Integer getOverviewImage(String policy, double[] requestedRes) {
	    // setup policy
        if(policy == null) {
            policy = (String) hints.get(Hints.OVERVIEW_POLICY);
            if(policy == null)
                policy = Hints.VALUE_OVERVIEW_POLICY_NEAREST;
        }
        
        // sort resolutions from smallest pixels (higher res) to biggest pixels (higher res)
        // keeping a reference to the original image choice
        List resolutions = new ArrayList();
        for (int i = 0; i < overViewResolutions.length; i++) {
            resolutions.add(new Resolution(overViewResolutions[i][0], overViewResolutions[i][1], i));
        }
        Collections.sort(resolutions);

        // Now search for the best matching resolution. 
        // Check also for the "perfect match"... unlikely in practice unless someone
        // tunes the clients to request exactly the resolution embedded in
        // the overviews, something a perf sensitive person might do in fact)
        
        // the requested resolutions
        final double reqx = requestedRes[0];
        final double reqy = requestedRes[1];
        
        // are we looking for a resolution even higher than the native one?
        Resolution max = (Resolution) resolutions.get(0);
        if(reqx < max.x && reqy < max.y ||
               (Math.abs(reqx - max.x) < EPS && Math.abs(reqy - max.y) < EPS))
            return max.imageChoice;
        // are we looking for a resolution even lower than the smallest overview?
        Resolution min = (Resolution) resolutions.get(resolutions.size() - 1);
        if(reqx > min.x && reqy > min.y ||
                (Math.abs(reqx - min.x) < EPS && Math.abs(reqy - min.y) < EPS))
            return min.imageChoice;
        // Ok, so we know the overview is between min and max, skip the first
        // and search for an overview with a resolution lower than the one requested,
        // that one and the one from the previous step will bound the searched resolution
        Resolution prev = max;
        for (int i = 1; i < resolutions.size(); i++) {
            Resolution curr = (Resolution) resolutions.get(i);
            // perfect match check
            if((Math.abs(reqx - curr.x) < EPS && Math.abs(reqy - curr.y) < EPS)) {
                return curr.imageChoice;
            }
            
            // middle check. The first part of the condition should be sufficient, but
            // there are cases where the x resolution is satisfied by the lowest resolution, 
            // the y by the one before the lowest (so the aspect ratio of the request is 
            // different than the one of the overviews), and we would end up going out of the loop
            // since not even the lowest can "top" the request for one axis 
            if(curr.x > reqx && curr.y > reqy || i == resolutions.size() - 1) {
                if(policy == Hints.VALUE_OVERVIEW_POLICY_QUALITY)
                    return prev.imageChoice;
                else if(policy == Hints.VALUE_OVERVIEW_POLICY_SPEED)
                    return curr.imageChoice;
                else if(reqx - prev.x < curr.x - reqx)
                    return prev.imageChoice;
                else
                    return curr.imageChoice;
            }
            prev = curr;
        }
        throw new RuntimeException("There is an error in the resolution lookup code");
    }
	
	/**
	 * Simple support class for sorting orerview resolutions
	 */
	private static class Resolution implements Comparable {
	    double x;
	    double y;
	    int imageChoice;
	    
        public Resolution(double x, double y, int imageChoice) {
            super();
            this.x = x;
            this.y = y;
            this.imageChoice = imageChoice;
        }
	    
	    public int compareTo(Object o) {
	        Resolution other = (Resolution) o;
	        if(x > other.x)
	            return 1;
	        else if(x < other.x)
	            return -1;
	        else if(y > other.y)
                return 1;
            else
                return -1;
	    }
	    
	    public String toString() {
	        return "Resolution[Choice=" + imageChoice + ",x=" + x + ",y=" + y + "]";
	    }
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
						: subSamplingFactorY;

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
	 * Creates a {@link GridCoverage} for the provided {@link PlanarImage} using
	 * the {@link #originalEnvelope} that was provided for this coverage.
	 * 
	 * @param image
	 *            contains the data for the coverage to create.
	 * @return a {@link GridCoverage}
	 * @throws IOException
	 */
	protected final GridCoverage createImageCoverage(PlanarImage image)
			throws IOException {
		return createImageCoverage(image, null);

	}

	/**
	 * Creates a {@link GridCoverage} for the provided {@link PlanarImage} using
	 * the {@link #raster2Model} that was provided for this coverage.
	 * 
	 * <p>
	 * This method is vital when working with coverages that have a raster to
	 * model transformation that is not a simple scale and translate.
	 * 
	 * @param image
	 *            contains the data for the coverage to create.
	 * @param raster2Model
	 *            is the {@link MathTransform} that maps from the raster space
	 *            to the model space.
	 * @return a {@link GridCoverage}
	 * @throws IOException
	 */
	protected final GridCoverage createImageCoverage(PlanarImage image,
			MathTransform raster2Model) throws IOException {

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
			break;
		case DataBuffer.TYPE_SHORT:
			geophysicRange = new NumberRange(Short.MIN_VALUE, Short.MAX_VALUE);
			break;
		case DataBuffer.TYPE_DOUBLE:

			geophysicRange = new NumberRange(Double.MIN_VALUE, Double.MAX_VALUE);
			break;
		case DataBuffer.TYPE_FLOAT:
			geophysicRange = new NumberRange(Float.MIN_VALUE, Float.MAX_VALUE);
			return createDEMCoverage(image);
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
		if (raster2Model != null) {
			return coverageFactory.create(coverageName, image, crs,
					raster2Model, bands, null, null);
		}
		return coverageFactory.create(coverageName, image, new GeneralEnvelope(
				originalEnvelope), bands, null, null);

	}

	/**
	 * Creates a {@link GridCoverage} for a coverage that is not a simple image
	 * but that contains complex dadta from measurements.
	 * 
	 * 
	 * <p>
	 * This usually means that the original {@link DataBuffer#getDataType()} is
	 * of one of the following types:
	 * 
	 * <ul>
	 * <li>{@link DataBuffer#TYPE_FLOAT}</li>
	 * <li>{@link DataBuffer#TYPE_DOUBLE}</li>
	 * <li>{@link DataBuffer#TYPE_INT}</li>
	 * <li>{@link DataBuffer#TYPE_SHORT}</li>
	 * </ul>
	 * 
	 * and it implies that we have to prepare a transformation from geophysics
	 * values to non-geophysics values.
	 * 
	 * @param coverage
	 *            a {@link PlanarImage} containing the source coverage.
	 * @return a {@link GridCoverage}.
	 */
	private GridCoverage createDEMCoverage(PlanarImage coverage) {
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

		return coverageFactory.create(coverageName, coverage, originalEnvelope,
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
	 * @param envelope
	 *            the GeneralEnvelope
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

				if (crs != null && !CRS.equalsIgnoreMetadata(crs, crs2D)) {
					final MathTransform tr = CRS.findMathTransform(crs2D, crs);
					if (!tr.isIdentity())
						envelope = CRS.transform(tr, envelope);
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

	/**
	 * Retrieves the {@link CoordinateReferenceSystem} for dataset pointed by
	 * this {@link AbstractGridCoverage2DReader}.
	 * 
	 * @return the {@link CoordinateReferenceSystem} for dataset pointed by this
	 *         {@link AbstractGridCoverage2DReader}.
	 */
	public final CoordinateReferenceSystem getCrs() {
		return crs;
	}

	/**
	 * Retrieves the {@link GeneralGridRange} that represents the raster grid
	 * dimensions of the highest resolution level in this dataset.
	 * 
	 * @return the {@link GeneralGridRange} that represents the raster grid
	 *         dimensions of the highest resolution level in this dataset.
	 */
	public final GeneralGridRange getOriginalGridRange() {
		return originalGridRange;
	}

	/**
	 * Retrieves the {@link GeneralEnvelope} for this
	 * {@link AbstractGridCoverage2DReader}.
	 * 
	 * @return the {@link GeneralEnvelope} for this
	 *         {@link AbstractGridCoverage2DReader}.
	 */
	public final GeneralEnvelope getOriginalEnvelope() {
		return originalEnvelope;
	}

	/**
	 * Retrieves the source for this {@link AbstractGridCoverage2DReader}.
	 * 
	 * @return the source for this {@link AbstractGridCoverage2DReader}.
	 */
	public final Object getSource() {
		return source;
	}

	/**
	 * Disposes this reader.
	 * 
	 * <p>
	 * This method just tries to close the underlying {@link ImageInputStream}.
	 */
	public void dispose() {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
		}

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#skip()
	 */
	public void skip() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
	 */
	public boolean hasMoreGridCoverages() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
	 */
	public String[] listSubNames() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
	 */
	public String getCurrentSubname() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
	 */
	public String getMetadataValue(final String name)
			throws MetadataNameNotFoundException {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getGridCoverageCount()
	 */
	public int getGridCoverageCount() {
		throw new UnsupportedOperationException("Unsupported opertion.");
	}

	/**
	 * Checks the transformation is a pure scale/translate instance (using a
	 * tolerance)
	 * 
	 * @param transform
	 * @return
	 */
	protected final static boolean isScaleTranslate(MathTransform transform) {
		if (!(transform instanceof AffineTransform))
			return false;
		AffineTransform at = (AffineTransform) transform;
		return at.getShearX() < EPS && at.getShearY() < EPS;
	}
}
