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
package org.geotools.gce.arcgrid;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;


/**
 * A simple implementation of the Arc Grid Format.
 *
 * @author jeichar
 * @author simone giannecchini</a>
 * @source $URL$
 */
public class ArcGridFormat extends AbstractGridFormat implements Format {
    public static final DefaultParameterDescriptor CRS = new DefaultParameterDescriptor("crs",
            CoordinateReferenceSystem.class, //calss of the object we will pass
            null, //list of valid values not provided
            getDefaultCRS() //default value
        );

    /** Indicates whether the arcgrid data is compressed with GZIP */
    public static final DefaultParameterDescriptor COMPRESS = new DefaultParameterDescriptor("Compressed",
            "Indicates whether the arcgrid data is compressed with GZIP",
            Boolean.FALSE, true);

    /** Indicates whether the arcgrid is in GRASS format */
    public static final DefaultParameterDescriptor GRASS = new DefaultParameterDescriptor("GRASS",
            "Indicates whether arcgrid is in GRASS format", Boolean.FALSE, true);

    /**
     * Creates an instance and sets the metadata.
     */
    public ArcGridFormat() {
        setInfo();
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        HashMap info = new HashMap();

        info.put("name", "ArcGrid");
        info.put("description", "Arc Grid Coverage Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://gdal.velocet.ca/projects/aigrid/index.html");
        info.put("version", "1.0");
        mInfo = info;

        //reading parameters
        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { GRASS, COMPRESS, CRS }));

        //reading parameters
        writeParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { GRASS, COMPRESS }));
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object
     *      source)
     */
    public GridCoverageReader getReader(Object source) {
        return new ArcGridReader(source);
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
     *      destination)
     */
    public GridCoverageWriter getWriter(Object destination) {
        return new ArcGridWriter(destination);
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
     *      input)
     */
    public boolean accepts(Object input) {
        boolean compress = false;
        Reader fakeReader = null;
        IOExchange mExchange = IOExchange.getIOExchange();

        //getting the path of this object
        String pathname = null;

        if (input instanceof String) {
            pathname = (new File((String) input)).getName();
        } else if (input instanceof File) {
            pathname = ((File) input).getName();
        } else if (input instanceof URL) {
            URL url = (URL) input;
            pathname = url.getFile();
        } else  
//        	not acceptable!
            return false;

        //trying to check the header
        try {
            fakeReader = mExchange.getGZIPReader(input);
            //it is compressed
            compress = true;
        } catch (Exception e) {
            //if I get here I hope it is not compressed
            compress = false;
        } finally {
            fakeReader = null;
        }

        //GRASS, arcgrid or not acceptable?
        for (int i = 0; i < 3; i++) {
            try {
                if (i < 2) {
                    if (compress) {
                        fakeReader = mExchange.getGZIPReader(input);
                    } else {
                        fakeReader = mExchange.getReader(input);
                    }
                }

                switch (i) {
                case 0: //reading an arcgrid ascii grid

                    ArcGridRaster acgRaster = new ArcGridRaster(fakeReader,
                            compress);

                    //trying to parse the header
                    acgRaster.parseHeader();
                    fakeReader = null;

                    //ok it is an arcgrid ascii grid (well, it should be!)
                    return true;

                case 1:

                    GRASSArcGridRaster gAscgRaster = new GRASSArcGridRaster(fakeReader,
                            compress);

                    //trying to parse the header
                    gAscgRaster.parseHeader();
                    fakeReader = null;

                    //ok it is an arcgrid ascii grid (well, it should be!)
                    return true;

                default:
                    return false;
                }
            } catch (IOException e) {
                fakeReader = null;
            }
        }

        return false;
    }

    /**
     * @see org.opengis.coverage.grid.Format#getName()
     */
    public String getName() {
        return (String) this.mInfo.get("name");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getDescription()
     */
    public String getDescription() {
        return (String) this.mInfo.get("description");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getVendor()
     */
    public String getVendor() {
        return (String) this.mInfo.get("vendor");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getDocURL()
     */
    public String getDocURL() {
        return (String) this.mInfo.get("docURL");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getVersion()
     */
    public String getVersion() {
        return (String) this.mInfo.get("version");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getReadParameters()
     */
    public ParameterValueGroup getReadParameters() {
        return readParameters;
    }

    /**
     * @see org.opengis.coverage.grid.Format#getWriteParameters()
     */
    public ParameterValueGroup getWriteParameters() {
        return writeParameters;
    }
}
