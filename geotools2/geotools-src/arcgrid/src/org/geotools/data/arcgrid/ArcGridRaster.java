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

import org.geotools.data.DataSourceException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class user for parsing an ArcGrid header (.arc, .asc) file
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ArcGridRaster {
    /** Column number tag in the header file */
    public static final String NCOLS = "NCOLS";

    /** Row number tag in the header file */
    public static final String NROWS = "NROWS";

    /** x corner coordinate tag in the header file */
    public static final String XLLCORNER = "XLLCORNER";

    /** y corner coordinate tag in the header file */
    public static final String YLLCORNER = "YLLCORNER";

    /** cell size tag in the header file */
    public static final String CELLSIZE = "CELLSIZE";

    /** no data tag in the header file */
    public static final String NODATA_VALUE = "NODATA_VALUE";

    /** header or data file url */
    private URL srcURL;

    /** max value found in the file */
    private int maxValue = Integer.MIN_VALUE;

    /** min value found in the file */
    private int minValue = Integer.MAX_VALUE;

    /** set of properties found in the header file */
    private Map propertyMap;

    /**
     * Creates a new instance of ArcGridRaster
     *
     * @param srcURL URL of a ArcGridRaster
     *
     * @throws FileNotFoundException if the header file does not exist
     * @throws IOException if some problem is encountered reading the file
     * @throws DataSourceException for problems related to the file content
     */
    public ArcGridRaster(URL srcURL) throws FileNotFoundException, IOException, DataSourceException {
        this.srcURL = srcURL;
        propertyMap = initMap();

        // parse file
        File srcFile = new File(srcURL.getFile());
        BufferedReader reader = new BufferedReader(new FileReader(srcFile));
        parseHeader(propertyMap, reader);

        if (!fullPropertySet(propertyMap)) {
            throw new DataSourceException("Needed properties missing in ArcGrid header file");
        }
    }

    /**
     * Max value
     *
     * @return the max value contained in the data file
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Min value
     *
     * @return the min value contained in the data file
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Returns a property value
     *
     * @param property use mnemonic constants
     *
     * @return the property value or null if the passed property is not recognized
     */
    public Object getProperty(String property) {
        return propertyMap.get(property);
    }

    /**
     * Returns the number of rows contained in the file
     *
     * @return number of rows
     */
    public int getNRows() {
        return ((Integer) propertyMap.get(NROWS)).intValue();
    }

    /**
     * Returns the number of columns contained in the file
     *
     * @return number of columns
     */
    public int getNCols() {
        return ((Integer) propertyMap.get(NCOLS)).intValue();
    }

    /**
     * Returns the x cordinate of the ... corner
     *
     * @return x cordinate of the ... corner
     */
    public double getXlCorner() {
        return ((Double) propertyMap.get(XLLCORNER)).intValue();
    }

    /**
     * Returns the y cordinate of the ... corner
     *
     * @return y cordinate of the ... corner
     */
    public double getYlCorner() {
        return ((Double) propertyMap.get(YLLCORNER)).doubleValue();
    }

    /**
     * Returns the cell size
     *
     * @return cell size
     */
    public double getCellSize() {
        return ((Double) propertyMap.get(CELLSIZE)).doubleValue();
    }

    /**
     * Returns the no data (null) value
     *
     * @return no data (null) value
     */
    public double getNoData() {
        return ((Double) propertyMap.get(NODATA_VALUE)).doubleValue();
    }

    /**
     * Initializes the map with the known properties, makes it easier to parse the file
     *
     * @return the initialized map
     */
    private Map initMap() {
        Map map = new HashMap();
        map.put(NCOLS, null);
        map.put(NROWS, null);
        map.put(XLLCORNER, null);
        map.put(YLLCORNER, null);
        map.put(CELLSIZE, null);
        map.put(NODATA_VALUE, null);

        return map;
    }

    /**
     * Parses the reader for the known properties
     *
     * @param properties the map to be filled in
     * @param reader the source data
     *
     * @throws IOException for reading errors
     * @throws DataSourceException for unrecoverable data format violations
     */
    private void parseHeader(Map properties, BufferedReader reader)
        throws IOException, DataSourceException {
        String currLine = reader.readLine();

        while (currLine != null) {
            // remove uneeded spaces
            currLine = currLine.trim();

            // get key and value            
            int firstSpaceIndex = currLine.indexOf(' ');

            if (firstSpaceIndex == -1) {
                throw new DataSourceException("Illegal line in ArcGrid header file");
            }

            String key = currLine.substring(0, firstSpaceIndex).toUpperCase();
            String value = currLine.substring(firstSpaceIndex).trim();

            // be tolerant about unknown keys, all we need is a subset of the
            // knows keys, the others will be discarded
            if (properties.containsKey(key)) {
                Class propClass = getPropertyClass(key);

                try {
                    if (propClass == String.class) {
                        properties.put(key, value);
                    } else if (propClass == Integer.class) {
                        properties.put(key, Integer.valueOf(value));
                    } else if (propClass == Double.class) {
                        properties.put(key, Double.valueOf(value));
                    }
                } catch (NumberFormatException nfe) {
                    throw new DataSourceException("Invalid property value in ArcGrid header file",
                        nfe);
                }
            }

            // read next line
            currLine = reader.readLine();

            if (key.equals(NODATA_VALUE)) {
                break;
            }
        }
    }

    /**
     * Checks wheter all of the properties in the map have been assigned
     *
     * @param properties the property map to be checked
     *
     * @return true if the map is filled in with values, false if at least one value is null
     */
    private boolean fullPropertySet(Map properties) {
        boolean full = true;
        Collection values = properties.values();

        for (Iterator it = values.iterator(); it.hasNext();) {
            if (it.next() == null) {
                full = false;

                break;
            }
        }

        return full;
    }

    /**
     * Returns the class of the value associated with a key
     *
     * @param key use one of the constants declared in this class
     *
     * @return the class of the value associated to the passed key
     */
    private Class getPropertyClass(String key) {
        Class propClass = null;

        if (key.equals(XLLCORNER) || key.equals(YLLCORNER) || key.equals(CELLSIZE)
                || key.equals(NODATA_VALUE)) {
            propClass = Double.class;
        } else {
            propClass = Integer.class;
        }

        return propClass;
    }

    /**
     * Returns the RenderedImage of the raster
     *
     * @return RenderedImage
     */
    public RenderedImage getImage() {
        BufferedImage image = new BufferedImage(getNCols(), getNRows(),
                BufferedImage.TYPE_BYTE_INDEXED);

        WritableRaster writableRaster = image.getRaster();

        try {
            int type = 0;

            InputStream in = srcURL.openStream();
            Reader r = new BufferedReader(new InputStreamReader(in));
            StreamTokenizer st = new StreamTokenizer(r);
            st.parseNumbers();
            st.wordChars('_', '_');
            st.eolIsSignificant(false);
            st.lowerCaseMode(true);

            // skip header
            for (int i = 0; i < 15; i++) {
                type = st.nextToken();
            }

            if (type == StreamTokenizer.TT_NUMBER) {
                st.pushBack(); // put it back if its a number - thats data
            } else {
                type = st.nextToken();
            }

            st.ordinaryChars('E', 'E');

            // Read and write values.
            int i1 = 0;
            double d1 = 0;

            //System.out.println("");
            for (int y = 0; y < getNRows(); y++) {
                for (int x = 0; x < getNCols(); x++) {
                    st.nextToken();
                    d1 = st.nval;
                    type = st.nextToken();

                    if ((type != StreamTokenizer.TT_NUMBER) && (type != StreamTokenizer.TT_EOF)) {
                        /* Either an exponent term number or end of file marker or something is wrong (eg. grid value is non-numeric)! */
                        st.nextToken();
                        d1 = d1 * Math.pow(10.0, st.nval);
                    } else {
                        st.pushBack();
                    }

                    i1 = new Double((255.0d * (d1 - minValue)) / (maxValue - minValue)).intValue();
                    writableRaster.setSample(x, y, 0, i1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }
}
