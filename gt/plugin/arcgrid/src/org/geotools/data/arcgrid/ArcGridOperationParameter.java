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

import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameter;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.units.Unit;


/**
 * The Arc Grid Format parameters are simple so there is only one
 * ArcGridOperationParameter class that descibes can describe all the
 * parameters
 *
 * @author jeichar
 */
public class ArcGridOperationParameter implements OperationParameter {

    /**
     * Creates a "Compress" Parameter.  Indicates whether the arcgrid data is
     * compressed with GZIP
     *
     * @return a "Compress" Parameter.
     */
    public static GeneralOperationParameter getCompressReadParam() {
        ArcGridOperationParameter param = new ArcGridOperationParameter();
        param.name = "Compressed";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value should be true if Data Source is compressed";

        Identifier id = null;

        return param;
    }

    /**
     * Creates a "Compress" Parameter.  Indicates whether the arcgrid data is
     * to be compressed with GZIP
     *
     * @return a "Compress" Parameter.
     */
    public static GeneralOperationParameter getCompressWriteParam() {
        ArcGridOperationParameter param = new ArcGridOperationParameter();
        param.name = "Compress";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value should be true written data should be compressed";

        Identifier id = null;

        return param;
    }

    /**
     * Creates a "GRASS" Parameter.  Indicates whether the arcgrid is in GRASS
     * format
     *
     * @return a "GRASS" Parameter.
     */
    public static GeneralOperationParameter getGRASSReadParam() {
        ArcGridOperationParameter param = new ArcGridOperationParameter();
        param.name = "GRASS";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value should be true if Data Source is in GRASS Format";

        Identifier id = null;

        return param;
    }

    /**
     * Creates a "GRASS" Parameter.  Indicates whether the arcgrid is to be
     * written in GRASS format
     *
     * @return a "GRASS" Parameter.
     */
    public static GeneralOperationParameter getGRASSWriteParam() {
        ArcGridOperationParameter param = new ArcGridOperationParameter();
        param.name = "GRASS";
        param.maxOccurs = 1;
        param.minOccurs = 0;
        param.remarks = "Value should be true grid is to be written in GRASS Format";

        Identifier id = null;

        return param;
    }
    Identifier[] identifiers;
    int maxOccurs;
    int minOccurs;
    String name;
    String remarks;
    Class valueClass = ArcGridParameterValue.class;

	/* (non-Javadoc)
	 * @see org.opengis.parameter.GeneralOperationParameter#createValue()
	 */
	public GeneralParameterValue createValue() {
		return new ArcGridParameterValue(false, this);
	}

    /**
     * @see org.opengis.parameter.OperationParameter#getDefaultValue()
     */
    public Object getDefaultValue() {
        return new Boolean("false");
    }

    /**
     * @see org.opengis.crs.Info#getIdentifiers()
     */
    public Identifier[] getIdentifiers() {
        return identifiers;
    }

    /**
     * @see org.opengis.parameter.GeneralOperationParameter#getMaximumOccurs()
     */
    public int getMaximumOccurs() {
        return maxOccurs;
    }

    /**
     * @see org.opengis.parameter.OperationParameter#getMaximumValue()
     */
    public Comparable getMaximumValue() {
        return null;
    }

    /**
     * @see org.opengis.parameter.GeneralOperationParameter#getMinimumOccurs()
     */
    public int getMinimumOccurs() {
        return minOccurs;
    }

    /**
     * @see org.opengis.parameter.OperationParameter#getMinimumValue()
     */
    public Comparable getMinimumValue() {
        return null;
    }

    /**
     * @see org.opengis.crs.Info#getName(java.util.Locale)
     */
    public String getName(Locale arg0) {
        return name;
    }

    /**
     * @see org.opengis.crs.Info#getRemarks(java.util.Locale)
     */
    public String getRemarks(Locale arg0) {
        return remarks;
    }

	/* (non-Javadoc)
	 * @see org.opengis.parameter.OperationParameter#getUnit()
	 */
	public Unit getUnit() {
		return null;
	}

    /**
     * @see org.opengis.parameter.OperationParameter#getValidValues()
     */
    public Set getValidValues() {
        Set set = new TreeSet();
        set.add(new Boolean("true"));
        set.add(new Boolean("false"));

        return set;
    }

    /**
     * @see org.opengis.parameter.OperationParameter#getValueClass()
     */
    public Class getValueClass() {
        return valueClass;
    }

	public String toWKT() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ArcGridOpperationParameter not relizable as WKT");
	}
}
