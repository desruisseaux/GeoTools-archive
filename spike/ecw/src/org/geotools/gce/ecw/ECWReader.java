/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gce.ecw;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.plugins.ecw.ECWImageReader;
import javax.imageio.plugins.ecw.ECWImageReaderSpi;
import javax.imageio.plugins.gdalframework.GDALImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.units.Unit;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.util.NumberRange;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

/**
 * This class can read an ECW grid data source and create a grid coverage from
 * the data.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public class ECWReader extends AbstractGridCoverage2DReader implements
		GridCoverageReader {
	/**
	 * Logger.
	 * 
	 */
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.arcgrid");

	/** Caches and ImageReaderSpi for an AsciiGridsImageReader. */
	private final static ImageReaderSpi readerSPI = new ECWImageReaderSpi();

	private String parentPath;

	private double inNoData = Double.NaN;

	/**
	 * Creates a new instance of an ArcGridReader basing the decision on whether
	 * the file is compressed or not. I assume nothing about file extension.
	 * 
	 * @param aSource
	 *            Source object for which we want to build an ArcGridReader.
	 * @throws DataSourceException
	 */
	public ECWReader(Object input) throws DataSourceException {
		this(input, null);

	}

	/**
	 * Creates a new instance of an ArcGridReader basing the decision on whether
	 * the file is compressed or not. I assume nothing about file extension.
	 * 
	 * @param aSource
	 *            Source object for which we want to build an ArcGridReader.
	 * @param hints
	 *            Hints to be used by this read throughout his life.
	 * @throws DataSourceException
	 */
	public ECWReader(Object input, final Hints hints)
			throws DataSourceException {
		if (hints != null)
			this.hints.add(hints);
		inStream = null;
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input
		//
		// /////////////////////////////////////////////////////////////////////
		if (input == null) {

			final IOException ex = new IOException(
					"ECW:No source set to read this coverage.");
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw new DataSourceException(ex);
		}
		this.source = input;
		format = new ECWFormat();
		coverageName = "AsciiGrid";
		try {
			boolean closeMe = true;

			// /////////////////////////////////////////////////////////////////////
			//
			// Source management
			//
			// /////////////////////////////////////////////////////////////////////
			if (input instanceof URL) {
				// URL that point to a file
				final URL sourceURL = ((URL) input);
				if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
					this.source = input = new File(URLDecoder.decode(sourceURL
							.getFile(), "UTF-8"));
					inStream = ImageIO.createImageInputStream(source);
				} else {
					final IOException ex = new IOException(
							"ECW:The input provided does not point to a file.");
					if (LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					throw new DataSourceException(ex);
				}
			} else
			// //
			//
			// Name, path, etc...
			//
			// //
			if (input instanceof File) {
				final File sourceFile = (File) input;
				this.source = sourceFile;
				this.parentPath = sourceFile.getParent();
				this.coverageName = sourceFile.getName();
				final int dotIndex = coverageName.indexOf(".");
				coverageName = (dotIndex == -1) ? coverageName : coverageName
						.substring(0, dotIndex);
				inStream = ImageIO.createImageInputStream(sourceFile);
			} else
			// //
			//
			// Get a stream in order to read from it for getting the basic
			// information for this coverage
			//
			// //
			if (input instanceof FileImageInputStreamExt) {
				this.source = ((FileImageInputStreamExt) input).getFile();
				closeMe = false;
			} else {
				final IOException ex = new IOException(
						"ECW:The input provided does not point to a file.");
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				throw new DataSourceException(ex);
			}

			if (inStream == null)
				throw new IllegalArgumentException(
						"Unrecoverable error for the provided source");

			// /////////////////////////////////////////////////////////////////////
			//
			// Reader and metadata
			//
			// /////////////////////////////////////////////////////////////////////
			// //
			//
			// Get a reader for this format
			//
			// //
			final ECWImageReader reader = (ECWImageReader) readerSPI
					.createReaderInstance();
			reader.setInput(inStream);

			// //
			//
			// CRS
			//
			// //
			try {
				crs = CRS.parseWKT(reader.getProjection());
			} catch (FactoryException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
			if (crs == null) {
				LOGGER.info("crs not found proceeding with EPSG:4326");
				crs = ECWFormat.getDefaultCRS();
			}

			// //
			//
			// Getting metadata
			//
			// //

			// /////////////////////////////////////////////////////////////////////
			//
			// Informations about multiple levels and such
			//
			// /////////////////////////////////////////////////////////////////////
			getResolutionInfo(reader);

			// release the stream
			if (closeMe)
				inStream.close();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		}

	}

	/**
	 * Gets resolution information about the coverage itself.
	 * 
	 * @param reader
	 * @throws IOException
	 * @throws TransformException
	 */
	private void getResolutionInfo(GDALImageReader reader) throws IOException,
			TransformException {

		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		// //
		numOverviews = 0;
		overViewResolutions = null;

		int hrWidth = reader.getWidth(0);
		int hrHeight = reader.getHeight(0);
		final Rectangle actualDim = new Rectangle(0, 0, hrWidth, hrHeight);
		originalGridRange = new GeneralGridRange(actualDim);

		final double geoTransform[] = reader.getGeoTransform();
		if (geoTransform != null) {
			this.raster2Model = ProjectiveTransform.create(new AffineTransform(
					geoTransform[1], 0.0,  0.0,geoTransform[5],
					geoTransform[0], geoTransform[3]));
		}

		final AffineTransform tempTransform = new AffineTransform(
				(AffineTransform) raster2Model);
		tempTransform.translate(-0.5, -0.5);
		originalEnvelope = CRSUtilities.transform(ProjectiveTransform
				.create(tempTransform), new GeneralEnvelope(actualDim));
		originalEnvelope.setCoordinateReferenceSystem(crs);

		// ///
		// 
		// setting the higher resolution avalaible for this coverage
		//
		// ///
		highestRes = getResolution(originalEnvelope, actualDim, crs);

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
	 */
	public String[] getMetadataNames() throws IOException {
		// Metadata has not been handled at this point ie there is not spec on
		// where it should be obtained
		return null;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
	 */
	public String getMetadataValue(String name) throws IOException,
			MetadataNameNotFoundException {
		throw new MetadataNameNotFoundException(new StringBuffer(name).append(
				" is not a valid metadata name for ArcGridReader").toString());
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
	 */
	public String[] listSubNames() throws IOException {
		return null;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
	 */
	public String getCurrentSubname() throws IOException {
		return null;
	}

	/**
	 * Note: The geotools GridCoverage does not implement the geoapi
	 * GridCoverage Interface so this method shows an error. All other methods
	 * are using the geotools GridCoverage class
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {
		GeneralEnvelope readEnvelope = null;
		Rectangle requestedDim = null;
		if (params != null) {

			final int length = params.length;
			Parameter param;
			for (int i = 0; i < length; i++) {
				param = (Parameter) params[i];

				if (param.getDescriptor().getName().getCode().equals(
						AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
								.toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
					readEnvelope = new GeneralEnvelope((Envelope) gg
							.getEnvelope2D());
					requestedDim = gg.getGridRange2D().getBounds();
				}

			}
		}
		
		
		// /////////////////////////////////////////////////////////////////////
		//
		// set params
		//
		// /////////////////////////////////////////////////////////////////////
		Integer imageChoice = new Integer(0);
		final ImageReadParam readP = new ImageReadParam();
		try {
			imageChoice = setReadParams(readP, readEnvelope, requestedDim);
		} catch (TransformException e) {
			new DataSourceException(e);
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// IMAGE READ OPERATION
		//
		// /////////////////////////////////////////////////////////////////////
		final ImageReader reader = readerSPI.createReaderInstance();
		final ImageInputStream inStream = ImageIO
				.createImageInputStream(source);
		reader.setInput(inStream);
		final Hints newHints = (Hints) hints.clone();
		inStream.mark();
		if (!reader.isImageTiled(imageChoice.intValue())) {
			final Dimension tileSize = ImageUtilities.toTileSize(new Dimension(
					reader.getWidth(imageChoice.intValue()), reader
							.getHeight(imageChoice.intValue())));
			final ImageLayout layout = new ImageLayout();
			layout.setTileGridXOffset(0);
			layout.setTileGridYOffset(0);
			layout.setTileHeight(tileSize.height);
			layout.setTileWidth(tileSize.width);
			newHints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
		}
		inStream.reset();
		final ParameterBlock pbjRead = new ParameterBlock();
		pbjRead.add(inStream);
		pbjRead.add(imageChoice);
		pbjRead.add(Boolean.FALSE);
		pbjRead.add(Boolean.FALSE);
		pbjRead.add(Boolean.FALSE);
		pbjRead.add(null);
		pbjRead.add(null);
		pbjRead.add(readP);
		pbjRead.add(reader);

		// /////////////////////////////////////////////////////////////////////
		//
		// BUILDING COVERAGE
		//
		// /////////////////////////////////////////////////////////////////////
		// get the raster -> model transformation and
		// create the coverage
		return createImageCoverage(JAI.create("ImageRead", pbjRead,
				(RenderingHints) newHints));
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
	 */
	public void dispose() throws IOException {
		
		if(inStream!=null){
			try{
			inStream.close();
			}
			catch(IOException e){
				if(LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING,e.getLocalizedMessage(),e);
			}
		}
		
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getGridCoverageCount()
	 */
	public int getGridCoverageCount() {
		return 1;
	}


	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
	 */
	public boolean hasMoreGridCoverages() throws IOException {
		return false;
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#skip()
	 */
	public void skip() throws IOException {
	}
}
