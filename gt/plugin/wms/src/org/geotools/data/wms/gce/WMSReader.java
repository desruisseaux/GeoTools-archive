/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms.gce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSReader implements GridCoverageReader {
    private Object source;
    private boolean hasNext = true;
    private WMSFormat format;
    private WebMapServer wms;

    /**
     * Source must be a WebMapServer object
     *
     * @param source
     */
    public WMSReader(Object source) {
        this.source = source;

        if (source instanceof WebMapServer) {
            wms = (WebMapServer) source;
        } else {
            throw new RuntimeException("source is not of type WebMapServer");
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return format;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getSource()
     */
    public Object getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataNames()
     */
    public String[] getMetadataNames() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
     */
    public String getMetadataValue(String arg0)
        throws IOException, MetadataNameNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#listSubNames()
     */
    public String[] listSubNames() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getCurrentSubname()
     */
    public String getCurrentSubname() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
     */
    public boolean hasMoreGridCoverages() throws IOException {
        return hasNext;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */
    public GridCoverage read(ParameterValueGroup parameters)
        throws IllegalArgumentException, IOException {
        GetMapRequest request = wms.createGetMapRequest();

        String minx = "";
        String miny = "";
        String maxx = "";
        String maxy = "";

        List values = parameters.values();
        for (int i = 0; i < values.size(); i++) {
            ParameterValue value = (ParameterValue) values.get(i);

            if ((value == null) || (value.getValue() == null)) {
//                System.out.println( "parameter "+i+" "+(value == null ? "null" : value.getDescriptor().getName(null) ));
                continue;
            }
//            System.out.println( "parameter "+i+" "+value.getDescriptor().getName(null)+" : "+value.getValue() );
            if (value.getDescriptor().getName().equals("LAYERS")) {
                String layers = "";
                String styles = "";

                List layerList = (List) value.getValue();

                for (int j = 0; j < layerList.size(); j++) {
                    SimpleLayer simpleLayer = (SimpleLayer) layerList.get(j);
                    layers = layers + simpleLayer.getName();
                    styles = styles + simpleLayer.getStyle();

                    if (j < (layerList.size() - 1)) {
                        layers = layers + ",";
                        styles = styles + ",";
                    }
                }

                request.setProperty("LAYERS", layers);
                request.setProperty("STYLES", styles);

                continue;
            }

            if (value.getDescriptor().getName().equals("BBOX_MINX")) {
                minx = (String) value.getValue();

                continue;
            }

            if (value.getDescriptor().getName().equals("BBOX_MINY")) {
                miny = (String) value.getValue();

                continue;
            }

            if (value.getDescriptor().getName().equals("BBOX_MAXX")) {
                maxx = (String) value.getValue();

                continue;
            }

            if (value.getDescriptor().getName().equals("BBOX_MAXY")) {
                maxy = (String) value.getValue();

                continue;
            }

            if (value.getDescriptor().getName().equals("TRANSPARENT")) {
                if (value.booleanValue()) {
                    request.setProperty("TRANSPARENT", "TRUE");
                } else {
                    request.setProperty("TRANSPARENT", "FALSE");
                }

                continue;
            }

            request.setProperty(value.getDescriptor().getName().toString(),
                value.stringValue());
        }

        String bbox = minx + "," + miny + "," + maxx + "," + maxy;
        request.setProperty("BBOX", bbox);

        GetMapResponse response = (GetMapResponse) wms.issueRequest(request,
                false);

        BufferedImage image = ImageIO.read(response.getInputStream());
        if (image == null ){
            throw new IOException("Image cannot be read from:"+response );
        }
        Envelope envelope = new Envelope(new double[] { 366800, 2170400 },
                new double[] { 816000, 2460400 });
        CoordinateSystem cs = GeographicCoordinateSystem.WGS84;

        hasNext = false;        
        GridCoverage coverage = new GridCoverage("wmsMap", image, cs, envelope);

        return coverage;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#skip()
     */
    public void skip() throws IOException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#dispose()
     */
    public void dispose() throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @param format
     */
    public void setFormat(WMSFormat format) {
        this.format = format;
    }
}
