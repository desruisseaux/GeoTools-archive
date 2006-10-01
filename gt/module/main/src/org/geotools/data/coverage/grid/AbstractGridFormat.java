/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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

import java.util.Map;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import sun.misc.FormattedFloatingDecimal.Form;

/**
 * AbstractGridFormat is a convenience class so subclasses only need to populate
 * a Map class and set the read and write parameter fields.
 * 
 * 
 * 
 * For example the ArcGridFormat has the following method which sets up all the
 * required information: <code>private void setInfo(){ HashMap info=new
 * HashMap(); info.put("name", "ArcGrid"); info.put("description", "Arc Grid
 * Coverage Format"); info.put("vendor", "Geotools"); info.put("docURL",
 * "http://gdal.velocet.ca/projects/aigrid/index.html"); info.put("version",
 * "1.0");  mInfo=info;  readParameters=new GeneralParameterDescriptor[2];
 * readParameters[0]=ArcGridOperationParameter.getGRASSReadParam();
 * readParameters[0]=ArcGridOperationParameter.getCompressReadParam();
 * writeParameters=new GeneralParameterDescriptor[2];
 * writeParameters[0]=ArcGridOperationParameter.getGRASSWriteParam();
 * writeParameters[0]=ArcGridOperationParameter.getCompressWriteParam();
 * }</code>
 * 
 * @author jeichar
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/data/coverage/grid/AbstractGridFormat.java $
 */
public abstract class AbstractGridFormat implements Format {

	/**
	 * The Map object is used by the information methods(such as getName()) as a
	 * data source. The keys in the Map object (for the associated method) are
	 * as follows: getName() key = "name" value type=String getDescription() key =
	 * "description" value type=String getVendor() key = "vendor" value
	 * type=String getDocURL() key = "docURL" value type=String getVersion() key =
	 * "version" value type=String Naturally, any methods that are overridden
	 * need not have an entry in the Map
	 */
	protected Map mInfo;

	protected ParameterValueGroup readParameters;

	protected ParameterValueGroup writeParameters;

	private static CoordinateReferenceSystem crs;
	static {
		try {
			crs = CRS.decode("EPSG:4326", true);
		} catch (NoSuchAuthorityCodeException e) {
			crs = DefaultGeographicCRS.WGS84;
		} catch (FactoryException e) {
			crs = DefaultGeographicCRS.WGS84;
		}
	}

	/** Indicates the area to load from */
	public static final DefaultParameterDescriptor READ_GRIDGEOMETRY2D = new DefaultParameterDescriptor(
			"ReadGridGeometry2D", GridGeometry2D.class, null, null);

	/**
	 * @see org.opengis.coverage.grid.Format#getName()
	 */
	public String getName() {
		return (String) mInfo.get("name");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDescription()
	 */
	public String getDescription() {
		return (String) mInfo.get("description");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVendor()
	 */
	public String getVendor() {
		return (String) mInfo.get("vendor");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getDocURL()
	 */
	public String getDocURL() {
		return (String) mInfo.get("docURL");
	}

	/**
	 * @see org.opengis.coverage.grid.Format#getVersion()
	 */
	public String getVersion() {
		return (String) mInfo.get("version");
	}

	/**
	 * Gets a {@link GridCoverageReader} for this format able to create
	 * coverages out of the <code>source</code> object.
	 * 
	 * <p>
	 * In case this {@link Format} cannot reader the provided
	 * <code>source</code> object <code>null</code> is returned.
	 * 
	 * @param source
	 *            The source object to parse.
	 * @return A reader for this {@link Format} or null.
	 */
	abstract public GridCoverageReader getReader(Object source);

	/**
	 * 
	 * Gets a {@link GridCoverageReader} for this format able to create
	 * coverages out of the <code>source</code> object using the provided
	 * <code>hints</code>.
	 * 
	 * <p>
	 * In case this {@link Format} cannot reader the provided
	 * <code>source</code> object <code>null</code> is returned.
	 * 
	 * @param source
	 *            The source object to parse. *
	 * @param hints
	 *            The {@link Hints} to use when trying to instantiate this
	 *            reader.
	 * @return A reader for this {@link Format} or null.
	 */
	abstract public GridCoverageReader getReader(Object source, Hints hints);

	/**
	 * Retrieves a {@link GridCoverageWriter} suitable for writing to the
	 * provided <code>destination</code> with this format.
	 * 
	 * <p>
	 * In case no writers are availaible <code>null</code> is returned.
	 * 
	 * @param destination
	 *            The destinatin where to write.
	 * @return A {@link GridCoverageWriter} suitable for writing to the provided
	 *         <code>destination</code> with this format.
	 */
	abstract public GridCoverageWriter getWriter(Object destination);

	/**
	 * Tells me if this {@link Format} can read the provided <code>input</code>.
	 * 
	 * 
	 * @param input
	 *            The input object to test for suitablilty.
	 * @return True if this format can read this object, False otherwise.
	 */
	abstract public boolean accepts(Object input);

	/**
	 * @see org.geotools.data.coverage.grid.Format#equals(org.geotools.data.coverage.grid.Format)
	 */
	public boolean equals(Format f) {
		if (f.getClass() == getClass()) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.Format#getReadParameters()
	 */
	public ParameterValueGroup getReadParameters() {
		return this.readParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.Format#getWriteParameters()
	 */
	public ParameterValueGroup getWriteParameters() {
		return this.writeParameters;
	}

	/**
	 * getDefaultCRS
	 * 
	 * This method provides the user with a default crs WGS84
	 */
	static public CoordinateReferenceSystem getDefaultCRS() {
		return crs;
	}
}
