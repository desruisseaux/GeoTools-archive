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
package org.geotools.data.arcgrid;

import org.geotools.data.gridcoverage.GenericGridFormat;
import org.geotools.gc.exchange.GridCoverageReader;
import org.geotools.gc.exchange.GridCoverageWriter;
import org.opengis.parameter.GeneralOperationParameter;
import java.util.HashMap;

/**
 * A simple implementation of the Arc Grid Format Metadata 
 *
 * @author jeichar
 */
public class ArcGridFormat extends GenericGridFormat {
    /**
     * creates an instance and sets the metadata
     */
    public ArcGridFormat() {
        setInfo();
    }

    /**
     * sets the metadata information
     *
     */
    private void setInfo() {
        HashMap info = new HashMap();
        info.put("name", "ArcGrid");
        info.put("description", "Arc Grid Coverage Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://gdal.velocet.ca/projects/aigrid/index.html");
        info.put("version", "1.0");
        mInfo = info;

        readParameters = new GeneralOperationParameter[2];
        readParameters[0] = ArcGridOperationParameter.getGRASSReadParam();
        readParameters[0] = ArcGridOperationParameter.getCompressReadParam();

        writeParameters = new GeneralOperationParameter[2];
        writeParameters[0] = ArcGridOperationParameter.getGRASSWriteParam();
        writeParameters[0] = ArcGridOperationParameter.getCompressWriteParam();
    }

}
