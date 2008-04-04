/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal.jp2mrsid;

import java.util.logging.Logger;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.geotools.coverageio.gdal.AbstractGDALBasedTestCase;


/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Base JP2K testing class.
 */
public abstract class AbstractJP2MrSIDTestCase extends AbstractGDALBasedTestCase {
	//TODO: Change these temp settings.
	//Define the GDAL_SKIP env variable to enable the proper plugin.
	static{
		gdal.AllRegister();
		final Driver driverkak = gdal.GetDriverByName("JP2KAK");
		final Driver driverecw = gdal.GetDriverByName("JP2ECW");
		if (driverkak!=null || driverecw!=null){
			final StringBuffer skipDriver = new StringBuffer("");
			if (driverkak!=null)
				skipDriver.append("JP2KAK ");
			if (driverecw!=null)
				skipDriver.append("JP2ECW");
			gdal.SetConfigOption("GDAL_SKIP", skipDriver.toString());
		}
	}
	
    protected final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.jp2mrsid");

    public AbstractJP2MrSIDTestCase(String name) {
        super(name, "JP2K", new JP2MrSIDFormatFactory());
    }
}
