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
package org.geotools.data.wms.gce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.FactoryFinder;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;


public class WMSReader implements GridCoverageReader {
    private Object source;
    private boolean hasNext = true;
    private WMSFormat format;
    private WebMapServer wms;

    /**
     * Source must be a WebMapServer object
     *
     * @param source
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public WMSReader(Object source) {
        this.source = source;

        if (source instanceof WebMapServer) {
            wms = (WebMapServer) source;
        } else {
            throw new RuntimeException("source is not of type WebMapServer");
        }
    }

    public Object getSource() {
        return source;
    }

    public String[] getMetadataNames() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMetadataValue(String arg0)
        throws IOException, MetadataNameNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] listSubNames() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    public String getCurrentSubname() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    public boolean hasMoreGridCoverages() throws IOException {
        return hasNext;
    }

    public void skip() throws IOException {
        // TODO Auto-generated method stub
    }

    public void dispose() throws IOException {
        // TODO Auto-generated method stub
    }

    public void setFormat(WMSFormat format) {
        this.format = format;
    }

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        GetMapRequest request = wms.createGetMapRequest();
        String minx = "";
        String miny = "";
        String maxx = "";
        String maxy = "";
        
        CoordinateReferenceSystem crs = null;


        for (int i = 0; i < parameters.length; i++) {
            GeneralParameterValue generalValue = parameters[i];

            String paramName = generalValue.getDescriptor().getName().getCode();

            if ((generalValue == null)) {
                continue;
            }

            if (paramName.equals("LAYERS")) {
                ParameterGroup groupValue = (ParameterGroup) generalValue;
                String layers = "";
                String styles = "";

                List layerList = (List) groupValue.values();

                for (int j = 0; j < layerList.size(); j++) {
                    Parameter parameter = (Parameter) layerList.get(j);

                    layers = layers
                        + parameter.getDescriptor().getName().getCode();
                    styles = styles + (String) parameter.getValue();

                    if (j < (layerList.size() - 1)) {
                        layers = layers + ",";
                        styles = styles + ",";
                    }
                }

                request.setProperty("LAYERS", layers);
                request.setProperty("STYLES", styles);

                continue;
            }

            ParameterValue value = (ParameterValue) generalValue;

            if (paramName.equals("BBOX_MINX")) {
                minx = ((Double) value.getValue()).toString();

                continue;
            }

            if (paramName.equals("BBOX_MINY")) {
                miny = ((Double) value.getValue()).toString();

                continue;
            }

            if (paramName.equals("BBOX_MAXX")) {
                maxx = ((Double) value.getValue()).toString();

                continue;
            }

            if (paramName.equals("BBOX_MAXY")) {
                maxy = ((Double) value.getValue()).toString();

                continue;
            }

            if (paramName.equals("HEIGHT")) {
                request.setProperty("HEIGHT",
                    ((Integer) value.getValue()).toString());

                continue;
            }

            if (paramName.equals("WIDTH")) {
                request.setProperty("WIDTH",
                    ((Integer) value.getValue()).toString());

                continue;
            }

            if (paramName.equals("TRANSPARENT")) {
                if (value.booleanValue()) {
                    request.setProperty("TRANSPARENT", "TRUE");
                } else {
                    request.setProperty("TRANSPARENT", "FALSE");
                }

                continue;
            }

            if (value.getValue() == null) {
                continue;
            }
            
            if (paramName.equals("SRS")) {
            	String srs = value.stringValue();

            	try {
					crs = FactoryFinder.decode(srs);
				} catch (NoSuchAuthorityCodeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }

            request.setProperty(value.getDescriptor().getName().getCode(),
                value.stringValue());
        }

        String bbox = minx + "," + miny + "," + maxx + "," + maxy;
        request.setProperty("BBOX", bbox);

        GetMapResponse response;
        try {
            response = (GetMapResponse) wms.issueRequest(request);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new IOException(e.getLocalizedMessage());
        }
        BufferedImage image = ImageIO.read(response.getInputStream());

        if (image == null) {
            throw new IOException("Image cannot be read from:" + response);
        }

//        Envelope envelope = new Envelope(new double[] { 366800, 2170400 },
//                new double[] { 816000, 2460400 });
//        
//        CoordinateSystem cs;
//        if (crs != null) {
//        	cs = (CoordinateSystem) crs;
//        } else {
//        	cs = GeographicCoordinateSystem.WGS84;
//        }
        

        hasNext = false;

//        GridCoverage coverage = new GridCoverage("wmsMap", image, cs, envelope);

//        return coverage;
        return null;
	}
}
