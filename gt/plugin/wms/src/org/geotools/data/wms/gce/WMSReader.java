/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
import org.geotools.data.wms.GetMapRequest;
import org.geotools.data.wms.GetMapResponse;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.getCapabilities.DCPType;
import org.geotools.data.wms.getCapabilities.Get;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.parameter.GeneralParameterValue;

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
	private WMT_MS_Capabilities capabilities;
	
	/**
	 * Source must be a WMT_MS_Capabilities object
	 * 
	 * @param source 
	 */
	public WMSReader(Object source) {
		this.source = source;
		if (source instanceof WMT_MS_Capabilities) {
			capabilities = (WMT_MS_Capabilities) source;
		} else {
			throw new RuntimeException("source is not of type WMT_MS_Capabilities");
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
	public String getMetadataValue(String arg0) throws IOException,
			MetadataNameNotFoundException {
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
	public GridCoverage read(GeneralParameterValue[] parameters)
			throws IllegalArgumentException, IOException {
		
			DCPType dcp = (DCPType) capabilities.getCapability().getRequest().getGetMap().getDcpTypes().get(0);
			Get get = (Get) dcp.getHttp().getGets().get(0);
			
			GetMapRequest request = new GetMapRequest(get.getOnlineResource());
		    
			String minx = "",miny = "",maxx = "",maxy = "";
			
		    for (int i = 0; i < parameters.length; i++) {
				WMSParameterValue value = (WMSParameterValue) parameters[i];
				if (value == null || value.getValue() == null)
					continue;
				
				if (value.getDescriptor().getName(null).equals("LAYERS")) {
					String layers = "";
					String styles = "";
					
					List layerList = (List) value.getValue();
					for (int j = 0; j < layerList.size(); j++) {
						SimpleLayer simpleLayer = (SimpleLayer) layerList.get(j);
						layers = layers + simpleLayer.getName();
						styles = styles + simpleLayer.getStyle();
						if (j < layerList.size()-1) {
							layers = layers + ",";
							styles = styles + ",";
						}
					}
					request.setProperty("LAYERS", layers);
					request.setProperty("STYLES", styles);
					continue;
				}
				
				if (value.getDescriptor().getName(null).equals("BBOX_MINX")) {
					minx = (String) value.getValue();
					continue;
				}
				if (value.getDescriptor().getName(null).equals("BBOX_MINY")) {
					miny = (String) value.getValue();
					continue;
				}
				if (value.getDescriptor().getName(null).equals("BBOX_MAXX")) {
					maxx = (String) value.getValue();
					continue;
				}
				if (value.getDescriptor().getName(null).equals("BBOX_MAXY")) {
					maxy = (String) value.getValue();
					continue;
				}
				
				if (value.getDescriptor().getName(null).equals("TRANSPARENT")) {
					if (value.booleanValue()) {
						request.setProperty("TRANSPARENT", "TRUE");
					} else {
						request.setProperty("TRANSPARENT", "FALSE");
					}
					continue;
				}
								
				request.setProperty(value.getDescriptor().getName(null), value.stringValue());					
			}

		    String bbox = minx+","+miny+","+maxx+","+maxy;
		    request.setProperty("BBOX", bbox);
		    
		    GetMapResponse response = WebMapServer.issueGetMapRequest(request);

		    BufferedImage image = ImageIO.read(response.getResponse());
		    Envelope envelope = new Envelope(new double[] {366800, 2170400}, new double[] {816000, 2460400});
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
