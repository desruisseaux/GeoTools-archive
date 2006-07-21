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
/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given.
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff.crs_adapters;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffException;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.GeoTiffIIOMetadataDecoder;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.GeoTiffConstants;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.codes.GeoTiffCoordinateTransformationsCodes;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.codes.GeoTiffGCSCodes;
import org.geotools.gce.geotiff.IIOMetadataAdpaters.utils.codes.GeoTiffPCSCodes;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

/**
 * The <code>GeoTiffMetadata2CRSAdapter</code> is responsible for interpreting
 * the metadata provided by the <code>GeoTiffIIOMetadataDecoder</code> for the
 * purposes of constructing a CoordinateSystem object representative of the
 * information found in the tags.
 * 
 * <p>
 * This class implements the flow indicated by the following diagram:
 * </p>
 * 
 * <p align="center">
 * <img src="doc-files/GeoTiffFlow.jpg">
 * </p>
 * 
 * <p>
 * To use this class, the <CODE>GeoTiffReader</CODE> should create an instance
 * with the <code>CoordinateSystemAuthorityFactory</code> specified by the
 * <CODE>GeoTiffFormat</CODE> instance which created the reader. The image
 * specific metadata should then be set with the appropriate accessor methods.
 * Finally, the <code>createCoordinateSystem()</code> method is called to
 * produce the <code>CoordinateReferenceSystem</code> object specified by the
 * metadata.
 * </p>
 * 
 * @author Bryce Nordgren / USDA Forest Service
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/src/org/geotools/gce/geotiff/crs_adapters/GeoTiffMetadata2CRSAdapter.java $
 */
public class GeoTiffMetadata2CRSAdapter {
	// code from GeoTIFFWritingUtilities spec section 6.3.2.4
	private static final String PM_Greenwich = "8901";

	private CSAuthorityFactory csAuthorityFactory = null;

	private DatumAuthorityFactory datumFactory = null;

	// factories to construct CRS/CS/Datum objects directly.
	private DatumFactory datumObjFactory = null;

	private CRSFactory crsFactory = null;

	/** Holds value of property metadata. */
	private GeoTiffIIOMetadataDecoder metadata = null;

	// Default values of GeoTiff angular and linear units.
	// these will be modified if the appropriate GeoKeys are
	// set in the file
	private static Unit linearUnit = SI.METER;

	private static Unit angularUnit = NonSI.DEGREE_ANGLE;

	private double semiMajorAxis;

	private double inverseFlattening;

	private Hints hints;

	private MathTransformFactory mtFactory = null;

	private FactoryGroup factories = null;

	private CoordinateOperationAuthorityFactory opFactory = null;

	private int rasterType;

	private CSFactory csFactory;

	/**
	 * Creates a new instance of GeoTiffMetadata2CRSAdapter
	 * 
	 * @param hints
	 *            a map of hints to locate the authority and object factories.
	 *            (can be null)
	 */
	public GeoTiffMetadata2CRSAdapter(Hints hints) {

		this.hints = hints;

	}

	private synchronized final void initOperationFactory() {
		if (opFactory == null)
			opFactory = FactoryFinder.getCoordinateOperationAuthorityFactory(
					"EPSG", hints);
	}

	private synchronized final void initCSAuthorityFactory() {
		if (csAuthorityFactory == null)
			csAuthorityFactory = FactoryFinder.getCSAuthorityFactory("EPSG",
					hints);
	}

	private synchronized final void initdatumFactory() {
		if (datumFactory == null)
			datumFactory = FactoryFinder
					.getDatumAuthorityFactory("EPSG", hints);
		if (datumObjFactory == null)
			datumObjFactory = FactoryFinder.getDatumFactory(hints);
	}

	private synchronized final void initCRSFactory() {
		if (crsFactory == null)
			crsFactory = FactoryFinder.getCRSFactory(hints);
	}

	private synchronized final void initCSFactory() {
		if (csFactory == null)
			csFactory = FactoryFinder.getCSFactory(hints);
	}

	/**
	 * This method creates a <code>CoordinateReferenceSystem</code> object
	 * from the metadata which has been set earlier. If it cannot create the
	 * <code>CoordinateReferenceSystem</code>, then one of three exceptions
	 * is thrown to indicate the error.
	 * 
	 * @return the <code>CoordinateReferenceSystem</code> object representing
	 *         the file data
	 * 
	 * @throws IOException
	 *             if there is unexpected data in the GeoKey tags.
	 * @throws FactoryException
	 * @throws NullPointerException
	 *             if the <code>csAuthorityFactory</code>,
	 *             <code>datumFactory</code>, <code>crsFactory</code> or
	 *             <code>metadata</code> are uninitialized
	 * @throws UnsupportedOperationException
	 *             if the coordinate system specified by the GeoTiff file is not
	 *             supported.
	 */
	public CoordinateReferenceSystem createCoordinateSystem()
			throws IOException, FactoryException {

		// the first thing to check is the Model Type.
		// is it "Projected" or is it "Geographic"?
		// "Geocentric" is not supported.
		switch (getGeoKeyAsInt(GeoTiffConstants.GTModelTypeGeoKey)) {
		case GeoTiffPCSCodes.ModelTypeProjected:
			return createProjectedCoordinateSystem();

		case GeoTiffGCSCodes.ModelTypeGeographic:
			return createGeographicCoordinateSystem();

		default:
			throw new UnsupportedOperationException(
					"GeoTiffMetadata2CRSAdapter::createCoordinateSystem:Only Geographic & Projected Systems are supported.  ");
		}

	}

	/**
	 * This code is responsible for creating a projected coordinate reference
	 * system as specified in the GeoTiff specification. User defined values are
	 * supported throughout the evolution of this specification with except of
	 * the coordinate transformation which must be one of the supported types.
	 * 
	 * @return
	 * @throws IOException
	 * @throws FactoryException
	 */
	private ProjectedCRS createProjectedCoordinateSystem() throws IOException,
			FactoryException {

		// get the projection reference system code in case we have one
		String tempCode = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjectedCSTypeGeoKey);
		if (tempCode == null)
			tempCode = "unnamed".intern();
		final StringBuffer projCode = new StringBuffer(tempCode.toString()
				.trim().intern());
		// if it's user defined, there's a lot of work to do
		if (tempCode.equals("unnamed")
				|| tempCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			return createUserDefinedPCS();

			// if it's not user defined, just use the EPSG factory to create the
			// coordinate system
		}
		try {
			if (!tempCode.startsWith("EPSG") && !tempCode.startsWith("epsg")) {
				projCode.insert(0, "EPSG:");
			}
			// it is an EPSG crs let's create it.
			return (ProjectedCRS) CRS.decode(projCode.toString());
		} catch (FactoryException fe) {
			final IOException ex = new GeoTiffException(metadata, fe
					.getLocalizedMessage());
			ex.initCause(fe);
			throw ex;
		}
	}

	/**
	 * Creation of a geographic coordinate reference system as specified in the
	 * GeoTiff specification. User defined values are supported for all the
	 * possible levels of the above mentioned specification.
	 * 
	 * @return
	 * @throws IOException
	 */
	private GeographicCRS createGeographicCoordinateSystem() throws IOException {
		GeographicCRS gcs = null;

		// get the projection code
		final String tempCode = metadata
				.getGeoKey(GeoTiffGCSCodes.GeographicTypeGeoKey);

		// geogCode.toString().trim().intern();
		// if it's user defined, there's a lot of work to do
		if (tempCode == null
				|| tempCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			gcs = createUserDefinedGCS();

			// if it's not user defined, just use the EPSG factory to create the
			// coordinate system
		} else {
			try {
				final StringBuffer geogCode = new StringBuffer(tempCode);
				if (!tempCode.startsWith("EPSG")
						&& !tempCode.startsWith("epsg")) {
					geogCode.insert(0, "EPSG:");
				}
				gcs = (GeographicCRS) CRS.decode(geogCode.toString());

			} catch (FactoryException fe) {
				final IOException ex = new GeoTiffException(metadata, fe
						.getLocalizedMessage());
				ex.initCause(fe);
				throw ex;
			}
		}
		// set the angular unit specified by this GeoTIFFWritingUtilities file
		angularUnit = gcs.getCoordinateSystem().getAxis(0).getUnit();
		final GeodeticDatum tempDatum = ((GeodeticDatum) gcs.getDatum());
		final DefaultEllipsoid tempEll = (DefaultEllipsoid) tempDatum
				.getEllipsoid();
		inverseFlattening = tempEll.getInverseFlattening();
		semiMajorAxis = tempEll.getSemiMajorAxis();
		return gcs;
	}

	/**
	 * Getting a specified geotiff geo key as a int. It is somehow tolerant in
	 * the sense that in case such a key does not exist it retrieves 0.
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private final int getGeoKeyAsInt(final int key) {

		try {
			return Integer.parseInt(metadata.getGeoKey(key));
		} catch (NumberFormatException ne) {
			return 0;
		}

	}

	/**
	 * Create the grid to world (or raster to model) transformation for this
	 * source.
	 * 
	 * @see http://lists.maptools.org/pipermail/geotiff/2006-January/000213.html
	 * @task TODO add support for transformation
	 * @param crs
	 * @return
	 * @throws GeoTiffException
	 */
	public final MathTransform getRasterToModel(
			final CoordinateReferenceSystem crs) throws GeoTiffException {
		// /////////////////////////////////////////////////////////////////////
		//
		// Load initials
		//
		// /////////////////////////////////////////////////////////////////////
		final double[] tiePoints = metadata.getModelTiePoints();
		final double[] pixScales = metadata.getModelPixelScales();
		final int numTiePoints = tiePoints.length / 6;
		MathTransform xform = null;
		rasterType = getGeoKeyAsInt(GeoTiffConstants.RasterPixelIsArea);
		// @task TODO I do not know if this is correct but it works fine
		if (rasterType == GeoTiffConstants.UNDEFINED)
			rasterType = GeoTiffConstants.RasterPixelIsArea;// geotiff spec says
		// that PixelIsArea
		// is the default

		// /////////////////////////////////////////////////////////////////////
		//
		// checking the directions of the axes.
		// we need to understand how the axes of this gridcoverage are
		// specified.
		// trying to understand the direction of the first axis in order to
		//
		// /////////////////////////////////////////////////////////////////////
		final CoordinateSystem cs = crs.getCoordinateSystem();
		boolean lonFirst = !GridGeometry2D.swapXY(cs);
		// latitude index
		final int latIndex = lonFirst ? 1 : 0;

		// axis reversion
		/**
		 * The GridCoverage2D is backed by a PlanarImage whose intrinsic crs2D
		 * has first axis going east and second axis going south, therefore we
		 * need to reverse and possibly swap axes accordingly
		 */

		final boolean[] reverse = GridGeometry2D.reverse(cs);

		if (numTiePoints == 1) {

			final GeneralMatrix gm = new GeneralMatrix(3); // identity
			final double scaleRaster2ModelLongitude = (reverse[(latIndex + 1) % 2]) ? -pixScales[0]
					: pixScales[0];
			final double scaleRaster2ModelLatitude = (reverse[latIndex]) ? -pixScales[1]
					: pixScales[1];
			final double tiePointColumn = tiePoints[0]
					+ (rasterType == GeoTiffConstants.RasterPixelIsArea ? 0.5
							: 0); // "raster" space
			// coordinates
			// (indicies)
			final double tiePointRow = tiePoints[1]
					+ (rasterType == GeoTiffConstants.RasterPixelIsArea ? 0.5
							: 0);

			// compute an "offset and scale" matrix
			gm.setElement(0, 0, (lonFirst) ? scaleRaster2ModelLongitude : 0);
			gm.setElement(1, 1, (lonFirst) ? scaleRaster2ModelLatitude : 0);
			gm.setElement(0, 1, (lonFirst) ? 0 : scaleRaster2ModelLatitude);
			gm.setElement(1, 0, (lonFirst) ? 0 : scaleRaster2ModelLongitude);

			gm.setElement(0, 2, (lonFirst) ? tiePoints[3]
					+ (scaleRaster2ModelLongitude * tiePointColumn)
					: tiePoints[4] + (scaleRaster2ModelLatitude * tiePointRow));
			gm.setElement(1, 2, (lonFirst) ? tiePoints[4]
					+ (scaleRaster2ModelLatitude * tiePointRow) : tiePoints[3]
					+ (scaleRaster2ModelLongitude * tiePointColumn));

			// make it a LinearTransform
			xform = ProjectiveTransform.create(gm);
		} else {
			throw new GeoTiffException(metadata,
					"Unknown Raster to Model configuration.");
		}

		return xform;
	}

	/**
	 * Getting a specified geotiff geo key as a double. It is somehow tolerant
	 * in the sense that in case such a key does not exist it retrieves 0.
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private final double getGeoKeyAsDouble(final int key) {

		try {
			return Double.parseDouble(metadata.getGeoKey(key));
		} catch (NumberFormatException ne) {
			return 0;
		}

	}

	/**
	 * Getter for property metadata.
	 * 
	 * @return Value of property metadata.
	 */
	public final GeoTiffIIOMetadataDecoder getMetadata() {
		return this.metadata;
	}

	/**
	 * Setter for property metadata.
	 * 
	 * @param metadata
	 *            New value of property metadata.
	 */
	public final void setMetadata(final GeoTiffIIOMetadataDecoder metadata) {
		this.metadata = metadata;
	}

	/**
	 * We have a user defined PCRS, let's try to parse it.
	 * 
	 * @return
	 * @throws IOException
	 * @throws FactoryException
	 */
	private ProjectedCRS createUserDefinedPCS() throws IOException,
			FactoryException {

		/**
		 * 
		 * At the top level a user-defined PCRS is made by
		 * <ol>
		 * <li>NAME
		 * <li>PROJECTION geo key
		 * <li>GEOGRAPHIC CRS geo key
		 * </ol>
		 * 
		 * 
		 */
		// NAME of the user defined projected coordinate reference system.
		String projectedCrsName = metadata
				.getGeoKey(GeoTiffPCSCodes.PCSCitationGeoKey);
		if (projectedCrsName == null)
			projectedCrsName = "unnamed".intern();

		// PROJECTION geo key for this projected coordinate reference system.
		// get the projection code for this PCRS to build it from the GCS
		final String projCode = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjectionGeoKey);

		// is it user defined?
		Conversion projection = null;
		boolean projUserDefined = false;
		final ParameterValueGroup parameters;
		if (projCode == null
				|| projCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			projUserDefined = true;
			/**
			 * A user defined projection is made up by
			 * <ol>
			 * <li>NAME
			 * <li>PROJECTION COORDINATE TRANSFORMATION geo key
			 * <li>PROJECTION LINEAR UNITS CRS geo key
			 * </ol>
			 */
			// NAME of this projection coordinate transformation
			// getting user defined parameters
			String projectionName = metadata
					.getGeoKey(GeoTiffPCSCodes.PCSCitationGeoKey);
			if (projectionName == null)
				projectionName = "Unnamed";

			// getting default parameters for this projection and filling them
			// with the values found
			// inside the geokeys list.
			parameters = createUserDefinedProjectionParameter(projectionName);

			if (parameters == null)
				throw new GeoTiffException(metadata,
						"GeoTiffMetadata2CRSAdapter::createUserDefinedPCS:Projection is not supported.");

			// getting the linear unit
			linearUnit = createUnit(GeoTiffPCSCodes.ProjLinearUnitsGeoKey,
					GeoTiffPCSCodes.ProjLinearUnitSizeGeoKey, SI.METER,
					SI.METER);

		} else {
			parameters = null;
			initOperationFactory();
			projection = (Conversion) this.opFactory
					.createCoordinateOperation(new StringBuffer("EPSG:")
							.append(projCode).toString());
		}

		// GEOGRAPHIC CRS
		final GeographicCRS gcs = createGeographicCoordinateSystem();

		// was the prjection user defined?
		// in such case we need to set the remaining parameters.
		if (projUserDefined) {
			// setting missing parameters
			parameters.parameter("semi_minor").setValue(
					semiMajorAxis * (1 - (1 / inverseFlattening)));
			parameters.parameter("semi_major").setValue(semiMajorAxis);

		}
		// PROJECTED CRS
		initFactoryGroup();
		if (projUserDefined)
			return factories.createProjectedCRS(Collections.singletonMap(
					"name", projectedCrsName), gcs, null, parameters,
					DefaultCartesianCS.PROJECTED);

		return factories.createProjectedCRS(Collections.singletonMap("name",
				projectedCrsName), gcs, projection,
				DefaultCartesianCS.PROJECTED);

	}

	/**
	 * 
	 */
	private synchronized final void initFactoryGroup() {
		if (factories == null) {
			initCSFactory();
			initdatumFactory();
			initMTFactory();
			initCRSFactory();
			factories = new FactoryGroup(datumObjFactory, FactoryFinder
					.getCSFactory(hints), crsFactory, mtFactory);
		}

	}

	/**
	 * 
	 */
	private final void initMTFactory() {
		if (mtFactory == null)
			mtFactory = new DefaultMathTransformFactory();

	}

	/**
	 * Creating a prime meridian for the gcs we are creating at an higher level.
	 * As usua this method tries to follow tthe geotiff specification.
	 * 
	 * @return
	 * @throws IOException
	 */
	private PrimeMeridian createPrimeMeridian() throws IOException {
		// look up the prime meridian:
		// + could be an EPSG code
		// + could be user defined
		// + not defined = greenwich
		final String pmCode = metadata
				.getGeoKey(GeoTiffGCSCodes.GeogPrimeMeridianGeoKey);
		PrimeMeridian pm = null;

		try {
			if (pmCode != null) {
				if (pmCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
					try {
						final String name = metadata
								.getGeoKey(GeoTiffGCSCodes.GeogCitationGeoKey);
						final String pmValue = metadata
								.getGeoKey(GeoTiffGCSCodes.GeogPrimeMeridianLongGeoKey);
						final double pmNumeric = Double.parseDouble(pmValue);
						// is it greewich?
						if (pmNumeric == 0)
							return DefaultPrimeMeridian.GREENWICH;
						final Map props = new HashMap();
						props.put("name", (name != null) ? name
								: "User Defined GEOTIFF Prime Meridian");
						pm = datumObjFactory.createPrimeMeridian(props,
								pmNumeric, angularUnit);
					} catch (NumberFormatException nfe) {
						final IOException io = new GeoTiffException(metadata,
								"Invalid user-defined prime meridian spec.");
						io.initCause(nfe);
						throw io;
					}
				} else {
					pm = datumFactory.createPrimeMeridian(pmCode);
				}
			} else {
				pm = datumFactory.createPrimeMeridian(PM_Greenwich);
			}
		} catch (FactoryException fe) {
			final IOException io = new GeoTiffException(metadata, fe
					.getLocalizedMessage());
			io.initCause(fe);
			throw io;
		}

		return pm;
	}

	/**
	 * Looks up the Geodetic Datum as specified in the GeoTIFFWritingUtilities
	 * file. The geotools definition of the geodetic datum includes both an
	 * ellipsoid and a prime meridian, but the code in the
	 * GeoTIFFWritingUtilities file does NOT include the prime meridian, as it
	 * is specified separately. This code currently does not support user
	 * defined datum.
	 * 
	 * @param unit
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws GeoTiffException
	 *             DOCUMENT ME!
	 */
	private GeodeticDatum createGeodeticDatum(final Unit unit)
			throws IOException {
		// lookup the datum (w/o PrimeMeridian), error if "user defined"
		GeodeticDatum datum = null;
		final String datumCode = metadata
				.getGeoKey(GeoTiffGCSCodes.GeogGeodeticDatumGeoKey);

		if (datumCode == null) {
			throw new GeoTiffException(
					metadata,
					"GeoTiffMetadata2CRSAdapter::createGeodeticDatum(Unit unit):A user defined Geographic Coordinate system must include a predefined datum!");
		}

		if (datumCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			/**
			 * 
			 * 
			 * USER DEFINED DATUM
			 * 
			 * 
			 * 
			 */
			// datum name
			final String datumName = metadata
					.getGeoKey(GeoTiffGCSCodes.GeogCitationGeoKey);

			// is it WGS84?
			if (datumName.trim().equalsIgnoreCase("WGS84"))
				return DefaultGeodeticDatum.WGS84;

			initdatumFactory();

			// ELLIPSOID
			final Ellipsoid ellipsoid = createEllipsoid(unit);

			// PRIME MERIDIAN
			// lookup the Prime Meridian.
			final PrimeMeridian primeMeridian = createPrimeMeridian();

			// DATUM
			datum = new DefaultGeodeticDatum(datumName, ellipsoid,
					primeMeridian);
		} else {
			/**
			 * NOT USER DEFINED DATUM
			 */

			// we are going to use the provided EPSG code
			try {
				initdatumFactory();
				datum = (GeodeticDatum) (datumFactory.createDatum(datumCode));
			} catch (FactoryException fe) {
				final GeoTiffException ex = new GeoTiffException(metadata, fe
						.getLocalizedMessage());
				ex.initCause(fe);
				throw ex;
			} catch (ClassCastException cce) {
				final GeoTiffException ex = new GeoTiffException(metadata, cce
						.getLocalizedMessage());
				ex.initCause(cce);
				throw ex;
			}
		}

		return datum;
	}

	/**
	 * Creating an ellipsoid following the GeoTiff spec.
	 * 
	 * @param unit
	 * 
	 * @return
	 * 
	 * @throws GeoTiffException
	 */
	private Ellipsoid createEllipsoid(final Unit unit) throws GeoTiffException {
		// ellipsoid key
		final String ellipsoidKey = metadata
				.getGeoKey(GeoTiffGCSCodes.GeogEllipsoidGeoKey);
		String temp = null;
		// is the ellipsoid user defined?
		if (ellipsoidKey
				.equalsIgnoreCase(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			String nameEllipsoid = metadata
					.getGeoKey(GeoTiffGCSCodes.GeogCitationGeoKey);
			if (nameEllipsoid == null)
				nameEllipsoid = "unnamed";
			// is it the default for WGS84?
			if (nameEllipsoid.trim().equalsIgnoreCase("WGS84"))
				return DefaultEllipsoid.WGS84;
			// getting temporary parameters
			temp = metadata.getGeoKey(GeoTiffGCSCodes.GeogSemiMajorAxisGeoKey);
			semiMajorAxis = Double.parseDouble(temp);
			temp = metadata.getGeoKey(GeoTiffGCSCodes.GeogInvFlatteningGeoKey);
			inverseFlattening = Double.parseDouble(temp);
			// look for the ellypsoid first then build the datum
			final HashMap map = new HashMap();
			map.put("name", nameEllipsoid);

			return DefaultEllipsoid.createFlattenedSphere(map, semiMajorAxis,
					inverseFlattening, unit);
		}

		try {
			initdatumFactory();
			return datumFactory.createEllipsoid(new StringBuffer("EPSG:")
					.append(ellipsoidKey).toString());
		} catch (FactoryException fe) {
			final GeoTiffException ex = new GeoTiffException(metadata, fe
					.getLocalizedMessage());
			ex.initCause(fe);
			throw ex;
		}
	}

	/**
	 * The GeoTIFFWritingUtilities spec requires that a user defined GCS be
	 * comprised of the following:
	 * 
	 * <ul>
	 * <li> a citation </li>
	 * <li> a datum definition </li>
	 * <li> a prime meridian definition (if not Greenwich) </li>
	 * <li> an angular unit definition (if not degrees) </li>
	 * </ul>
	 * 
	 * 
	 * @return spi
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	private GeographicCRS createUserDefinedGCS() throws IOException {
		// coordinate reference system name
		String name = metadata.getGeoKey(GeoTiffGCSCodes.GeogCitationGeoKey);
		if (name == null)
			name = "unnamed";

		// lookup the angular units used in this file
		angularUnit = createUnit(GeoTiffGCSCodes.GeogAngularUnitsGeoKey,
				GeoTiffGCSCodes.GeogAngularUnitSizeGeoKey, SI.RADIAN,
				NonSI.DEGREE_ANGLE);

		// linear unit
		linearUnit = createUnit(GeoTiffGCSCodes.GeogLinearUnitsGeoKey,
				GeoTiffGCSCodes.GeogLinearUnitSizeGeoKey, SI.METER, SI.METER);

		// lookup the Geodetic datum
		final GeodeticDatum datum = createGeodeticDatum(linearUnit);

		// coordinate reference system
		GeographicCRS gcs = null;

		try {
			// property map is reused
			final Map props = new HashMap();

			// make the user defined GCS from all the components...
			props.put("name", name);
			initCRSFactory();
			gcs = crsFactory.createGeographicCRS(props, datum,
					DefaultEllipsoidalCS.GEODETIC_2D);
		} catch (FactoryException fe) {
			final GeoTiffException io = new GeoTiffException(metadata, fe
					.getLocalizedMessage());
			io.initCause(fe);
			throw io;
		}

		return gcs;
	}

	/**
	 * 
	 * @todo we should somehow try to to support user defined coordinate
	 *       transformation even if for the moment is not so cler to me how we
	 *       could achieve that since if we have no clue about the coordinate
	 *       transform what we are supposed to do in order to build a
	 *       conversion, guess it? How could we pick up the parameters, should
	 *       look for all and then guess the right transformation?
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws FactoryException
	 */
	private ParameterValueGroup createUserDefinedProjectionParameter(String name)
			throws IOException, FactoryException {
		final String coordTrans = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjCoordTransGeoKey);

		// throw descriptive exception if ProjCoordTransGeoKey not defined
		if ((coordTrans == null)
				|| coordTrans
						.equalsIgnoreCase(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			throw new GeoTiffException(
					metadata,
					"GeoTiffMetadata2CRSAdapter::createUserDefinedProjectionParameter(String name):User defined projections must specify"
							+ " coordinate transformation code in ProjCoordTransGeoKey");
		}

		// getting math transform factory
		initMTFactory();
		return setParametersForProjection(name, coordTrans);
	}

	/**
	 * Set the projection parameters basing its decision on the projection name.
	 * I found a complete list of projections on the geotiff website at address
	 * http://www.remotesensing.org/geotiff/proj_list.
	 * 
	 * I had no time to implement support for all of them therefore you will not
	 * find all of them. If you want go ahead and add support for the missing
	 * ones. I have tested this code against some geotiff files you can find on
	 * the geotiff website under the ftp sample directory but I can say that
	 * they are a real mess! I am respecting the specification strictly while
	 * many of those fiels do not! I could make this method trickier and use
	 * workarounds in order to be less strict but I will not do this, since I
	 * believe it is may lead us just on a very dangerous path.
	 * 
	 * 
	 * @param name
	 * @param coordTrans
	 * 
	 * @return
	 * @throws GeoTiffException
	 */
	private ParameterValueGroup setParametersForProjection(String name,
			final String coordTransCode) throws GeoTiffException {
		ParameterValueGroup parameters = null;
		try {
			int code = 0;
			if (coordTransCode != null)
				code = Integer.parseInt(coordTransCode);
			if (name == null)
				name = "unnamed";
			/**
			 * 
			 * Transverse Mercator
			 * 
			 */
			if (name.equalsIgnoreCase("transverse_mercator")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_TransverseMercator) {
				parameters = mtFactory
						.getDefaultParameters("transverse_mercator");
				parameters.parameter("central_meridian").setValue(
						getOriginLong());
				parameters.parameter("latitude_of_origin").setValue(
						getOriginLat());
				parameters
						.parameter("scale_factor")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			/**
			 * 
			 * Mercator_1SP
			 * 
			 */
			if (name.equalsIgnoreCase("mercator_1SP")
					|| name.equalsIgnoreCase("Mercator_2SP")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_Mercator) {
				parameters = mtFactory.getDefaultParameters("Mercator_1SP");
				parameters.parameter("central_meridian").setValue(
						getOriginLong());
				// parameters
				// .parameter("latitude_of_origin")
				// .setValue(
				// this
				// .getGeoKeyAsDouble(GeoTiffPCSCodes.ProjNatOriginLatGeoKey));
				parameters
						.parameter("scale_factor")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			/**
			 * 
			 * Mercator_2Sp
			 * 
			 */
			if (name.equalsIgnoreCase("lambert_conformal_conic_1SP")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_LambertConfConic_Helmert) {
				parameters = mtFactory
						.getDefaultParameters("lambert_conformal_conic_1SP");
				parameters.parameter("central_meridian").setValue(
						getOriginLong());
				parameters.parameter("latitude_of_origin").setValue(
						getOriginLat());
				parameters
						.parameter("scale_factor")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			/**
			 * 
			 * LAMBERT_CONFORMAT_CONIC_2SP
			 * 
			 */
			if (name.equalsIgnoreCase("lambert_conformal_conic_2SP")
					|| name
							.equalsIgnoreCase("lambert_conformal_conic_2SP_Belgium")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_LambertConfConic_2SP) {
				parameters = mtFactory
						.getDefaultParameters("lambert_conformal_conic_2SP");
				parameters.parameter("central_meridian").setValue(
						getOriginLong());
				parameters.parameter("latitude_of_origin").setValue(
						getOriginLat());
				parameters
						.parameter("standard_parallel_1")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjStdParallel1GeoKey));
				parameters
						.parameter("standard_parallel_2")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjStdParallel2GeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			// if (name.equalsIgnoreCase("equidistant_conic")
			// || code == GeoTiffMetadata2CRSAdapter.CT_EquidistantConic) {
			// parameters = mtFactory
			// .getDefaultParameters("equidistant_conic");
			// parameters.parameter("central_meridian").setValue(
			// getOriginLong());
			// parameters.parameter("latitude_of_origin").setValue(
			// getOriginLat());
			// parameters
			// .parameter("standard_parallel_1")
			// .setValue(
			// this
			// .getGeoKeyAsDouble(GeoTiffIIOMetadataDecoder.ProjStdParallel1GeoKey));
			// parameters
			// .parameter("standard_parallel_2")
			// .setValue(
			// this
			// .getGeoKeyAsDouble(GeoTiffIIOMetadataDecoder.ProjStdParallel2GeoKey));
			// parameters.parameter("false_easting").setValue(
			// getFalseEasting());
			// parameters.parameter("false_northing").setValue(
			// getFalseNorthing());
			//
			// return parameters;
			// }

			/**
			 * 
			 * STEREOGRAPHIC
			 * 
			 */
			if (name.equalsIgnoreCase("stereographic")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_Stereographic) {
				parameters = mtFactory.getDefaultParameters("stereographic");
				parameters.parameter("central_meridian").setValue(
						this.getOriginLong());

				parameters.parameter("latitude_of_origin").setValue(

				this.getOriginLat());
				parameters
						.parameter("scale_factor")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			/**
			 * 
			 * POLAR_STEREOGRAPHIC.
			 * 
			 */
			if (name.equalsIgnoreCase("polar_stereographic")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_PolarStereographic) {
				parameters = mtFactory
						.getDefaultParameters("polar_stereographic");

				parameters.parameter("latitude_of_origin").setValue(
						this.getOriginLat());
				parameters
						.parameter("scale_factor")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());
				parameters.parameter("central_meridian").setValue(
						getOriginLong());

				return parameters;
			}

			/**
			 * 
			 * OBLIQUE_MERCATOR.
			 * 
			 */
			if (name.equalsIgnoreCase("oblique_mercator")
					|| name.equalsIgnoreCase("hotine_oblique_mercator")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_ObliqueMercator) {
				parameters = mtFactory.getDefaultParameters("oblique_mercator");

				parameters.parameter("scale_factor").setValue(getScaleFactor());
				parameters
						.parameter("azimuth")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjAzimuthAngleGeoKey));
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());
				parameters.parameter("longitude_of_center").setValue(
						getOriginLong());
				parameters.parameter("latitude_of_center").setValue(
						getOriginLat());
				return parameters;
			}

			/**
			 * 
			 * albers_Conic_Equal_Area
			 * 
			 */
			if (name.equalsIgnoreCase("albers_Conic_Equal_Area")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_AlbersEqualArea) {
				parameters = mtFactory
						.getDefaultParameters("Albers_Conic_Equal_Area");
				parameters
						.parameter("standard_parallel_1")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjStdParallel1GeoKey));
				parameters
						.parameter("standard_parallel_2")
						.setValue(
								this
										.getGeoKeyAsDouble(GeoTiffPCSCodes.ProjStdParallel2GeoKey));
				parameters.parameter("latitude_of_center").setValue(
						getOriginLat());
				parameters.parameter("longitude_of_center").setValue(
						getOriginLong());
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}

			/**
			 * 
			 * Orthographic
			 * 
			 */
			if (name.equalsIgnoreCase("Orthographic")
					|| code == GeoTiffCoordinateTransformationsCodes.CT_Orthographic) {
				parameters = mtFactory.getDefaultParameters("orthographic");

				parameters.parameter("latitude_of_origin").setValue(
						getOriginLat());
				parameters.parameter("longitude_of_origin").setValue(
						getOriginLong());
				parameters.parameter("false_easting").setValue(
						getFalseEasting());
				parameters.parameter("false_northing").setValue(
						getFalseNorthing());

				return parameters;
			}
		} catch (Exception e) {
			throw new GeoTiffException(metadata, e.getMessage());
		}

		return parameters;
	}

	private double getScaleFactor() {
		String scale = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjScaleAtCenterGeoKey);
		if (scale == null)
			scale = metadata
					.getGeoKey(GeoTiffPCSCodes.ProjScaleAtNatOriginGeoKey);
		if (scale == null)
			return 0.0;
		return Double.parseDouble(scale);
	}

	/**
	 * Getting the false easting with a minimum of tolerance with respect to the
	 * parameters name. I saw that ofetn people use the wrong geokey to store
	 * the false eassting, we cannot be too picky we need to get going pretty
	 * smoouthly.
	 * 
	 * @return double False easting.
	 */
	private double getFalseEasting() {
		String easting = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjFalseEastingGeoKey);
		if (easting == null)
			easting = metadata
					.getGeoKey(GeoTiffPCSCodes.ProjFalseOriginEastingGeoKey);
		if (easting == null)
			return 0.0;
		return Double.parseDouble(easting);

	}

	/**
	 * Getting the false northing with a minimum of tolerance with respect to
	 * the parameters name. I saw that ofetn people use the wrong geokey to
	 * store the false eassting, we cannot be too picky we need to get going
	 * pretty smoouthly.
	 * 
	 * @return double False northing.
	 */
	private double getFalseNorthing() {
		String northing = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjFalseNorthingGeoKey);
		if (northing == null)
			northing = metadata
					.getGeoKey(GeoTiffPCSCodes.ProjFalseOriginNorthingGeoKey);
		if (northing == null)
			return 0.0;
		return Double.parseDouble(northing);

	}

	/**
	 * Getting the origin long with a minimum of tolerance with respect to the
	 * parameters name. I saw that ofetn people use the wrong geokey to store
	 * the false eassting, we cannot be too picky we need to get going pretty
	 * smoouthly.
	 * 
	 * @return double origin longitude.
	 */
	private double getOriginLong() {
		String origin = metadata
				.getGeoKey(GeoTiffPCSCodes.ProjCenterLongGeoKey);
		if (origin == null)
			origin = metadata
					.getGeoKey(GeoTiffPCSCodes.ProjNatOriginLongGeoKey);
		if (origin == null)
			origin = metadata
					.getGeoKey(GeoTiffPCSCodes.ProjFalseNorthingGeoKey);
		if (origin == null)
			return 0.0;
		return Double.parseDouble(origin);
	}

	/**
	 * Getting the origin lat with a minimum of tolerance with respect to the
	 * parameters name. I saw that ofetn people use the wrong geokey to store
	 * the false eassting, we cannot be too picky we need to get going pretty
	 * smoouthly.
	 * 
	 * @return double origin latitude.
	 */
	private double getOriginLat() {
		String origin = metadata.getGeoKey(GeoTiffPCSCodes.ProjCenterLatGeoKey);
		if (origin == null)
			origin = metadata.getGeoKey(GeoTiffPCSCodes.ProjNatOriginLatGeoKey);
		if (origin == null)
			return 0.0;

		return Double.parseDouble(origin);
	}

	/**
	 * This code creates an <code>javax.Units.Unit</code> object out of the
	 * <code>ProjLinearUnitsGeoKey</code> and the
	 * <code>ProjLinearUnitSizeGeoKey</code>. The unit may either be
	 * specified as a standard EPSG recognized unit, or may be user defined.
	 * 
	 * @param key
	 *            DOCUMENT ME!
	 * @param userDefinedKey
	 *            DOCUMENT ME!
	 * @param base
	 *            DOCUMENT ME!
	 * @param def
	 *            DOCUMENT ME!
	 * 
	 * @return <code>Unit</code> object representative of the tags in the
	 *         file.
	 * 
	 * @throws IOException
	 *             if the<code>ProjLinearUnitsGeoKey</code> is not specified
	 *             or if unit is user defined and
	 *             <code>ProjLinearUnitSizeGeoKey</code> is either not defined
	 *             or does not contain a number.
	 */
	private Unit createUnit(int key, int userDefinedKey, Unit base, Unit def)
			throws IOException {
		final String unitCode = metadata.getGeoKey(key);

		// if not defined, return the default
		if (unitCode == null) {
			return def;
		}

		// if specified, retrieve the appropriate unit code.
		if (unitCode.equals(GeoTiffConstants.GTUserDefinedGeoKey_String)) {
			try {
				final String unitSize = metadata.getGeoKey(userDefinedKey);

				// throw descriptive exception if required key is not there.
				if (unitSize == null) {
					throw new GeoTiffException(
							metadata,
							new StringBuffer(
									"GeoTiffMetadata2CRSAdapter::createUnit:Must define unit length when using a user ")
									.append("defined unit").toString());
				}

				double sz = Double.parseDouble(unitSize);
				return base.multiply(sz);
			} catch (NumberFormatException nfe) {
				final IOException ioe = new GeoTiffException(metadata, nfe
						.getLocalizedMessage());
				ioe.initCause(nfe);
				throw ioe;
			}
		} else {
			try {
				// using epsg code for this unit
				initCSAuthorityFactory();
				return csAuthorityFactory.createUnit(unitCode);
			} catch (FactoryException fe) {
				final IOException io = new GeoTiffException(metadata, fe
						.getLocalizedMessage());
				io.initCause(fe);
				throw io;
			}
		}

	}

	public int getRasterType() {
		return rasterType;
	}
}
