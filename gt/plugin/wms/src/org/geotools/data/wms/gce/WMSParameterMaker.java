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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.Utils;
import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.Style;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralOperationParameter;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSParameterMaker {

	private WMT_MS_Capabilities capabilities;
	
	public WMSParameterMaker(WMT_MS_Capabilities capabilities) {
		this.capabilities = capabilities;
	}
	
	public GeneralOperationParameter createVersionReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "VERSION";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the version of the WMS server to be used";
        param.defaultValue = "1.1.1";
        param.validValues = new TreeSet();
        
        //param.validValues.add("1.0.0");
        //param.validValues.add("1.1.0");
        param.validValues.add("1.1.1"); //TODO version support here

        Identifier id = null;

        return param;
	}	
	
	public GeneralOperationParameter createFormatReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "FORMAT";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the desired format";
        param.validValues = new TreeSet();
        List formats = capabilities.getCapability().getRequest().getGetMap().getFormats();
        param.validValues.addAll(formats);

        Identifier id = null;

        return param;
	}
	
	public GeneralOperationParameter createRequestReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "REQUEST";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the the type of the request";
        param.defaultValue = "GetMap";
        param.validValues = new TreeSet();
        param.validValues.add("GetMap");

        Identifier id = null;

        return param;
	}
	
	public GeneralOperationParameter createSRSReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "SRS";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the desired SRS for the entire map";
        
        Set srs = new TreeSet();
        retrieveSRSs(capabilities.getCapability().getLayer(), srs);
        param.validValues = srs;
        
        Identifier id = null;

        return param;
	}	
	
	private void retrieveSRSs(Layer layer, Set srsSet) {
		List layerSRS = layer.getSrs();
		srsSet.addAll(layerSRS);
		
		List subLayers = layer.getSubLayers();
		Iterator iter = subLayers.iterator();
		while (iter.hasNext()) {
			Layer subLayer = (Layer) iter.next();
			retrieveSRSs(subLayer, srsSet);
		}
	}
	
	public GeneralOperationParameter createWidthReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "WIDTH";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the width, in pixels, of the requested map";

        Identifier id = null;

        return param;
	}	
	
	public GeneralOperationParameter createHeightReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "HEIGHT";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the height, in pixels, of the requested map";
        
        Identifier id = null;

        return param;
	}		

	public GeneralOperationParameter createLayersReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "LAYERS";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains a list containing multiple SimpleLayer instances, " 
        	           +"representing a layer to be drawn and its style. The Style value "
					   +"can be empty.";
        param.availableLayers = Utils.findDrawableLayers(capabilities.getCapability().getLayer());

        Identifier id = null;

        return param;
	}	
	


	public GeneralOperationParameter createBBoxMinXReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BBOX_MINX";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the minX value for the bounding box";

        Identifier id = null;

        return param;
	}

	public GeneralOperationParameter createBBoxMinYReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BBOX_MINY";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the minY value for the bounding box";

        Identifier id = null;

        return param;
	}

	public GeneralOperationParameter createBBoxMaxXReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BBOX_MAXX";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the maxX value for the bounding box";

        Identifier id = null;

        return param;
	}

	public GeneralOperationParameter createBBoxMaxYReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BBOX_MAXY";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the maxY value for the bounding box";

        Identifier id = null;

        return param;
	}
	
	public GeneralOperationParameter createTransparentReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "TRANSPARENT";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates map transparency";
        param.defaultValue = new Boolean(false);
        param.valueClass = Boolean.class;

        Identifier id = null;

        return param;
	}
	
	public GeneralOperationParameter createBGColorReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BGCOLOR";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates map background colour in hex format (0xRRGGBB)";
        param.defaultValue = "0xFFFFFF";

        Identifier id = null;

        return param;
	}
	
	public GeneralOperationParameter createExceptionsReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "EXCEPTIONS";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates the format in which exceptions are returned";
        param.defaultValue = "application/vnd.ogc.se_xml";
        param.validValues = new TreeSet(capabilities.getCapability().getException().getFormats());
        
        Identifier id = null;

        return param;
	}	

	public GeneralOperationParameter createTimeReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "TIME";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates the time value desired";

        Identifier id = null;

        return param;
	}	
	
	public GeneralOperationParameter createElevationReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "ELEVATION";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates the elevation value desired";

        Identifier id = null;

        return param;
	}		
	
	//TODO support Sample dimensions
	//TODO support VendorSpecific Parameters.
	
	
}
