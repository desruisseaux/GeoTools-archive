/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.units.Unit;

import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameter;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSOperationParameter implements OperationParameter {
	
	public static GeneralOperationParameter createVersionReadParam() {
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
	
	public static GeneralOperationParameter createRequestReadParam() {
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
	
	public static GeneralOperationParameter createLayersReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "LAYERS";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains a comma delimited String of layers";

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

	public static GeneralOperationParameter createSRSReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "SRS";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the desired SRS";

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
	
	public static GeneralOperationParameter createWidthReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "WIDTH";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the width, in pixels, of the requested map";

        Identifier id = null;

        return param;
	}
	
	
	public static GeneralOperationParameter createHeightReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "HEIGHT";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the height, in pixels, of the requested map";

        Identifier id = null;

        return param;
	}		
	
	public static GeneralOperationParameter createFormatReadParam() {
        WMSOperationParameter param = new WMSOperationParameter();
        param.name = "FORMAT";
        param.maxOccurs = 1;
        param.minOccurs = 1;
        param.remarks = "Value contains the desired format";

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
	
    Identifier[] identifiers;
    int maxOccurs;
    int minOccurs;
    String name;
    String remarks;
    String defaultValue;
    Set validValues;
    Class valueClass = WMSParameterValue.class;	
	
	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getValueClass()
	 */
	public Class getValueClass() {
		return valueClass;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getValidValues()
	 */
	public Set getValidValues() {
		return validValues;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getDefaultValue()
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getMinimumValue()
	 */
	public Comparable getMinimumValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getMaximumValue()
	 */
	public Comparable getMaximumValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getUnit()
	 */
	public Unit getUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.GeneralOperationParameter#createValue()
	 */
	public GeneralParameterValue createValue() {
		return new WMSParameterValue(null, this);
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.GeneralOperationParameter#getMinimumOccurs()
	 */
	public int getMinimumOccurs() {
		return minOccurs;
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.GeneralOperationParameter#getMaximumOccurs()
	 */
	public int getMaximumOccurs() {
		return maxOccurs;
	}

	/* (non-Javadoc)
	 * @see org.opengis.referencing.IdentifiedObject#getName(java.util.Locale)
	 */
	public String getName(Locale arg0) {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.opengis.referencing.IdentifiedObject#getIdentifiers()
	 */
	public Identifier[] getIdentifiers() {
		return identifiers;
	}

	/* (non-Javadoc)
	 * @see org.opengis.referencing.IdentifiedObject#getRemarks(java.util.Locale)
	 */
	public String getRemarks(Locale arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.referencing.IdentifiedObject#toWKT()
	 */
	public String toWKT() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
