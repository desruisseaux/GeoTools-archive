/*
 * Created on Aug 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.getCapabilities.Layer;
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
        param.remarks = "Value contains the width, in pixels, of the requested map";
        param.defaultValue = "1.1.1";
        param.validValues = new TreeSet();
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
        param.remarks = "Value contains the width, in pixels, of the requested map";
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
        param.remarks = "Value contains the desired SRS";
        
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

	public static GeneralOperationParameter createStylesReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "STYLES";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains a comma delimited String of layers";

        Identifier id = null;

        return param;
	}	
	
	public static GeneralOperationParameter createLayersReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "LAYERS";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains a comma delimited String of layers";

        Identifier id = null;

        return param;
	}	
	
	public static GeneralOperationParameter createBBoxReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BBOX";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains a BoundingBox in the form: \"minx,miny,maxx,maxy\"";

        Identifier id = null;

        return param;
	}	
	
	public static GeneralOperationParameter createTransparentReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "TRANSPARENT";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates map transparency";
        param.defaultValue = "FALSE";
        param.validValues = new TreeSet();
        param.validValues.add("TRUE");
        param.validValues.add("FALSE");

        Identifier id = null;

        return param;
	}
	
	public static GeneralOperationParameter createBGColorReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "BGCOLOR";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates map background colour in hex format (0xRRGGBB)";
        param.defaultValue = "0xFFFFFF";

        Identifier id = null;

        return param;
	}
	
	public static GeneralOperationParameter createExceptionsReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "EXCEPTIONS";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates the format in which exceptions are returned";
        param.defaultValue = "application/vnd.ogc.se_xml";
        param.validValues = new TreeSet();
        param.validValues.add("application/vnd.ogc.se_xml");
        param.validValues.add("application/vnd.ogc.se_inimage");
        param.validValues.add("application/vnd.ogc.se_blank");

        Identifier id = null;

        return param;
	}	

	public static GeneralOperationParameter createTimeReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "TIME";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value indicates the time value desired";

        Identifier id = null;

        return param;
	}	
	
	public static GeneralOperationParameter createElevationReadParam() {
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
