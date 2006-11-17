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
package org.geotools.gce.geotiff;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridCoverageWriter;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.GeoTiffIIOMetadataEncoder;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.GeoTiffConstants;
import org.geotools.gce.geotiff.crs_adapters.CRS2GeoTiffMetadataAdapter;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.CRSUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;

/*
 * GeoTools - OpenSource mapping toolkit http://geotools.org (C) 2005-2006,
 * GeoTools Project Managment Committee (PMC) This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; version
 * 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 */
/**
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/src/org/geotools/gce/geotiff/GeoTiffWriter.java $
 */
public final class GeoTiffWriter extends AbstractGridCoverageWriter implements
		GridCoverageWriter {
	
	/**The {@link ImageOutputStream} destination.*/
	private ImageOutputStream destination;

	/** factory for getting tiff writers. */
	private final static TIFFImageWriterSpi tiffWriterFactory = new TIFFImageWriterSpi();

	/**
	 * Constructor for a {@link GeoTiffWriter}.
	 * 
	 * @param destination
	 * @throws IOException
	 */
	public GeoTiffWriter(Object destination) throws IOException {
		this(destination, null);

	}

	/**
	 * Constructor for a {@link GeoTiffWriter}.
	 * 
	 * @param destination
	 * @param hints
	 * @throws IOException
	 */
	public GeoTiffWriter(Object destination, Hints hints) throws IOException {

		if (destination instanceof File)
			this.destination = ImageIO.createImageOutputStream(destination);
		else if (destination instanceof URL) {
			final URL dest = (URL) destination;
			if (dest.getProtocol().equalsIgnoreCase("file")) {
				final File destFile = new File(URLDecoder.decode(
						dest.getFile(), "UTF8"));
				this.destination = ImageIO.createImageOutputStream(destFile);
			}

		} else if (destination instanceof OutputStream) {

			this.destination = new FileCacheImageOutputStream(
					(OutputStream) destination, null);

		} else if (destination instanceof ImageOutputStream)
			this.destination = (ImageOutputStream) destination;
		else
			throw new IllegalArgumentException(
					"The provided destination canno be used!");
		// //
		//
		// managing hints
		//
		// //
		if (hints != null) {
			if (super.hints == null)
				this.hints = new Hints(null);
			hints.add(hints);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
	 */
	public Format getFormat() {
		return new GeoTiffFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
	 */
	public Object getDestination() {
		return destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	public String[] getMetadataNames() {
		throw new UnsupportedOperationException(
				"Method not currently supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMetadataValue(final String arg0, final String arg1)
			throws IOException, MetadataNameNotFoundException {
		throw new UnsupportedOperationException(
				"Method not currently supported");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	public void setCurrentSubname(final String arg0) throws IOException {
		throw new UnsupportedOperationException(
				"Method not currently supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage,
	 *      org.opengis.parameter.GeneralParameterValue[])
	 */
	public void write(final GridCoverage gc, final GeneralParameterValue[] arg1)
			throws IllegalArgumentException, IOException,
			IndexOutOfBoundsException {
		// /////////////////////////////////////////////////////////////////////
		//
		// getting the coordinate reference system
		//
		// /////////////////////////////////////////////////////////////////////
		final CoordinateReferenceSystem crs = gc.getCoordinateReferenceSystem();

		// /////////////////////////////////////////////////////////////////////
		//
		// we handle just projected andgeographic crs
		//
		// /////////////////////////////////////////////////////////////////////
		if (crs instanceof ProjectedCRS || crs instanceof GeographicCRS) {

			// creating geotiff metadata
			final CRS2GeoTiffMetadataAdapter adapter = (CRS2GeoTiffMetadataAdapter) CRS2GeoTiffMetadataAdapter
					.get(crs);
			final GeoTiffIIOMetadataEncoder metadata = adapter
					.parseCoordinateReferenceSystem();

			// setting tie points and scaley
			setTiePointAndScale(crs, metadata, (AffineTransform) gc
					.getGridGeometry().getGridToCoordinateSystem());

			// writing
			writeImage(((GridCoverage2D) gc).geophysics(false)
					.getRenderedImage(), this.destination, metadata);

		} else
			throw new GeoTiffException(
					null,
					"The supplied grid coverage uses an unsupported crs! You are allowed to use only projected and geographic coordinate reference systems");
	}

	/**
	 * This method is used to set the tie point and the scale parameters for the
	 * GeoTiff file we are writing. It does this regardles of the nature fo the
	 * crs without making any assumptions on the order or the direction of the
	 * axes, but checking them from the supplied CRS.
	 * 
	 * @see {@link http://lists.maptools.org/pipermail/geotiff/2006-January/000213.html}
	 * @param crs
	 *            The {@link CoordinateReferenceSystem} of the
	 *            {@link GridCoverage2D} to encode.
	 * @param metadata
	 * @param envelope
	 *            The {@link Envelope} of the {@link GridCoverage2D} to encode.
	 * @param W
	 *            The width of the {@link GridCoverage2D} to encode.
	 * @param H
	 *            The height of the {@link GridCoverage2D} to encode.
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 * @throws TransformException
	 */
	private void setTiePointAndScale(final CoordinateReferenceSystem crs,
			final GeoTiffIIOMetadataEncoder metadata,
			final AffineTransform gridToCoord)
			throws IndexOutOfBoundsException, IOException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Setting raster type to pixel centre since the ogc specifications
		// require so.
		//
		// /////////////////////////////////////////////////////////////////////
		metadata.addGeoShortParam(GeoTiffConstants.GTRasterTypeGeoKey,
				GeoTiffConstants.RasterPixelIsPoint);

		// /////////////////////////////////////////////////////////////////////
		//		
		// checking the directions of the axes.
		// we need to understand how the axes of this gridcoverage are
		// specified.
		// trying to understand the direction of the first axis in order to
		//
		// /////////////////////////////////////////////////////////////////////
		boolean lonFirst = (XAffineTransform.getSwapXY(gridToCoord) != -1);

		// /////////////////////////////////////////////////////////////////////
		//
		// Deciding how to structure the tie points with respect to the CRS.
		//
		// /////////////////////////////////////////////////////////////////////
		// tie points
		final double tiePointLongitude = (lonFirst) ? gridToCoord
				.getTranslateX() : gridToCoord.getTranslateY();
		final double tiePointLatitude = (lonFirst) ? gridToCoord
				.getTranslateY() : gridToCoord.getTranslateX();
		metadata.setModelTiePoint(0, 0, 0, tiePointLongitude, tiePointLatitude,
				0);
		// scale
		final double scaleModelToRasterLongitude = (lonFirst) ? Math
				.abs(gridToCoord.getScaleX()) : Math.abs(gridToCoord
				.getShearY());
		final double scaleModelToRasterLatitude = (lonFirst) ? Math
				.abs(gridToCoord.getScaleY()) : Math.abs(gridToCoord
				.getShearX());
		metadata.setModelPixelScale(scaleModelToRasterLongitude,
				scaleModelToRasterLatitude, 0);
		// Alternative code, not yet enabled in order to avoid breaking code.
        // The following code is insensitive to axis order and rotations in the
        // 'coord' space (not in the 'grid' space, otherwise we would not take
        // the inverse of the matrix).
/*
        final AffineTransform coordToGrid = gridToCoord.createInverse();
		final double scaleModelToRasterLongitude = 1 / XAffineTransform.getScaleX0(coordToGrid);
		final double scaleModelToRasterLatitude  = 1 / XAffineTransform.getScaleY0(coordToGrid);
 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
	 */
	public void dispose() throws IOException {
	}

	/**
	 * Writes the provided rendered image to the provided image output stream
	 * using the supplied geotiff metadata.
	 */
	private boolean writeImage(final RenderedImage image,
			final ImageOutputStream outputStream,
			final GeoTiffIIOMetadataEncoder geoTIFFMetadata) throws IOException {
		if (image == null || outputStream == null) {
			throw new IllegalArgumentException("some parameters are null");
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// GETTING READER AND METADATA
		//
		// /////////////////////////////////////////////////////////////////////
		final ImageWriter writer = tiffWriterFactory.createWriterInstance();
		final IIOMetadata metadata = createGeoTiffIIOMetadata(writer,
				ImageTypeSpecifier.createFromRenderedImage(image),
				geoTIFFMetadata);

		// /////////////////////////////////////////////////////////////////////
		//
		// IMAGEWRITE
		//
		// /////////////////////////////////////////////////////////////////////
		writer.setOutput(outputStream);
		writer.write(writer.getDefaultStreamMetadata(writer
				.getDefaultWriteParam()), new IIOImage(image, null, metadata),
				null);

		// /////////////////////////////////////////////////////////////////////
		//
		// release resources
		//
		// /////////////////////////////////////////////////////////////////////
		outputStream.flush();
		if (!(destination instanceof ImageOutputStream))
			outputStream.close();
		writer.dispose();

		return true;
	}

	/**
	 * Creates image metadata which complies to the GeoTIFFWritingUtilities
	 * specification for the given image writer, image type and
	 * GeoTIFFWritingUtilities metadata.
	 * 
	 * @param writer
	 *            the image writer, must not be null
	 * @param type
	 *            the image type, must not be null
	 * @param geoTIFFMetadata
	 *            the GeoTIFFWritingUtilities metadata, must not be null
	 * @return the image metadata, never null
	 * @throws IIOException
	 *             if the metadata cannot be created
	 */
	public final static IIOMetadata createGeoTiffIIOMetadata(
			ImageWriter writer, ImageTypeSpecifier type,
			GeoTiffIIOMetadataEncoder geoTIFFMetadata) throws IIOException {
		final IIOMetadata imageMetadata = writer.getDefaultImageMetadata(type,
				null);

		org.w3c.dom.Element w3cElement = (org.w3c.dom.Element) imageMetadata
				.getAsTree(GeoTiffConstants.GEOTIFF_IIO_METADATA_FORMAT_NAME);
		final Element element = new DOMBuilder().build(w3cElement);

		geoTIFFMetadata.assignTo(element);

		final Parent parent = element.getParent();
		parent.removeContent(element);

		final Document document = new Document(element);

		try {
			final org.w3c.dom.Document w3cDoc = new DOMOutputter()
					.output(document);
			imageMetadata.setFromTree(
					GeoTiffConstants.GEOTIFF_IIO_METADATA_FORMAT_NAME, w3cDoc
							.getDocumentElement());
		} catch (JDOMException e) {
			throw new IIOException(
					"Failed to set GeoTIFFWritingUtilities specific tags.", e);
		} catch (IIOInvalidTreeException e) {
			throw new IIOException(
					"Failed to set GeoTIFFWritingUtilities specific tags.", e);
		}

		return imageMetadata;
	}
}
