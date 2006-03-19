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
package org.geotools.data.oracle;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.filter.SQLEncoderOracle;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Subclasses JDBCTextFeatureWriter to issue Oracle transactions  directly as
 * sql text statements.  The super class takes care of all the nasty details,
 * this just returns the encoded geometry. To get some speed increases Jody
 * maintains that this class should not be used, that the updatable result
 * sets of JDBCFeatureWriter will work better.  But I couldn't get those to
 * work at all, whereas this works great for me.  We could also consider
 * putting the option for this or jdbc in the factory for OracleDataStore.
 * Should also consider using prepared statements for inserts, as they should
 * work faster - this should probably be done in the superclass.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public class OracleFeatureWriter extends JDBCTextFeatureWriter {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
	
    public OracleFeatureWriter(FeatureReader fReader, QueryData queryData)
        throws IOException {
        super(fReader, queryData);
    }

    protected String getGeometryInsertText(Geometry geom, int srid)
        throws IOException {
    	//return "?"; // Please use a prepaired statement to insert your geometry
    	
        String geomText = SQLEncoderOracle.toSDOGeom(geom, srid);
        return geomText;
    }
}
