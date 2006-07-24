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
package org.geotools.data.coverage.grid;

import java.util.Map;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
 * @author <a href="mailto:simboss1@gmail.com">Simone Giannecchini (simboss)</a>
 * @see AbstractFormatFactory
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/data/coverage/grid/AbstractGridFormat.java $
 */
public abstract class AbstractGridFormat
    implements Format {

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
			crs = CRS.decode("EPSG:4326",true);
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
	 * @todo javadoc
	 */
  abstract public org.opengis.coverage.grid.GridCoverageReader getReader(Object
      source);

  /**
	 * @todo javadoc
	 */
  abstract public org.opengis.coverage.grid.GridCoverageWriter getWriter(Object
      destination);

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
