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
package org.geotools.data.property;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * Simple AttributeReader that works against Java properties files.
 * 
 * <p>
 * This AttributeReader is part of the geotools2 DataStore tutorial, and should
 * be considered a Toy.
 * </p>
 * 
 * <p>
 * The content of this file should start with a the property "_" with the value
 * being the typeSpec describing the featureType. Thereafter each line will
 * should have a FeatureID as the property and the attribtues as the value
 * separated by | characters.
 * </p>
 * <pre><code>
 * _=id:Integer|name:String|geom:Geometry
 * fid1=1|Jody|<i>well known text</i>
 * fid2=2|Brent|<i>well known text</i>
 * fid3=3|Dave|<i>well known text</i>
 * </code></pre>
 *
 * @author jgarnett
 */
public class PropertyAttributeReader implements AttributeReader {
    /** DOCUMENT ME!  */
    private static final WKTReader wktReader = new WKTReader(new GeometryFactory());

    /** DOCUMENT ME! */
    BufferedReader reader;

    /** DOCUMENT ME! */
    FeatureType type;

    /** DOCUMENT ME! */
    String line;

    /** DOCUMENT ME! */
    String next;

	String[] text;
	
	String fid;
	
    /**
     * Creates a new PropertyAttributeReader object.
     *
     * @param file DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public PropertyAttributeReader(File file) throws IOException {
        String typeName = typeName(file);
        String namespace = namespace(file);
        reader = new BufferedReader(new FileReader(file));
        line = reader.readLine();

        if ((line == null) || !line.startsWith("_=")) {
            throw new IOException(typeName + " schema not available");
        }

        String typeSpec = line.substring(2);

        try {
            type = DataUtilities.createType(namespace + typeName, typeSpec);
        } catch (SchemaException e) {
            throw new DataSourceException(typeName + " schema not available", e);
        }

        line = null;
        next = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String typeName(File file) {
        String name = file.getName();
        int split = name.lastIndexOf('.');

        return (split == -1) ? name : name.substring(0, split);
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String namespace(File file) {
        File parent = file.getParentFile();

        return (parent == null) ? "" : (parent.getName() + ".");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getAttributeCount() {
        return type.getAttributeCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     */
    public AttributeType getAttributeType(int index)
        throws ArrayIndexOutOfBoundsException {
        return type.getAttributeType(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {
        reader.close();
        reader = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        }

        next = reader.readLine();

        return next != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public void next() throws IOException {
        if (hasNext()) {
            line = next;
            next = null;

	        int split = line.indexOf('=');
	        fid = line.substring(0, split);
			text = line.substring(split + 1).split("\\|");
			if(type.getAttributeCount() != text.length)
				throw new DataSourceException("format error: expected " +
						type.getAttributeCount() + " attributes, but found " +
						text.length + ". [" + line + "]");
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFeatureID() {
        if (line == null) {
            return null;
        }

		return fid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public Object read(int index)
        throws IOException, ArrayIndexOutOfBoundsException {
        if (line == null) {
            throw new IOException(
                "No content available - did you remeber to call next?");
        }

        AttributeType attType = type.getAttributeType(index);

        String stringValue = null;
		try {
			stringValue = text[index];
		} catch (RuntimeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			stringValue = null;
		}

        Object value = null;

        if (attType.isGeometry()) {
            try {
                value = wktReader.read(stringValue);
            } catch (ParseException e) {
                throw new DataSourceException("Can't parse WKT for fid#" + fid,
                    e);
            }
        } else {
            value = attType.parse(stringValue);
        }

        return value;
    }
}

/*
POINT(529264 4801784)|
038-54|
48|
041|
106|
Caser?o Zaldegietxebarrierdikoa (Txopenebenta)|
Zaldegietxebarrierdikoa baserria (Txopebenta)|
Zaldegietxebarrierdikoa (Txopebenta)|
Zaldegietxebarrierdikoa, Caser?o (Txopebenta)|
Zaldegietxebarrierdikoa bas
*/