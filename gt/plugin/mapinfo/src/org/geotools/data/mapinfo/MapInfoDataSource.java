/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.data.mapinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;


/**
 * Parses MIF and MID file pair into features.
 * <p>
 * DataSource is no longer with us - but this MapInfoDataSource lives on in zombie
 * form - it serves as a reference point for when james has a run at making a DataStore.
 * </p>
 * @version $Revision: 1.7 $
 * @author Ian Turton
 * @author James Macgill
 */
public class MapInfoDataSource {
    private static Logger LOGGER = Logger.getLogger("org.geotools.mifmid");
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_NONE = "none";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_POINT = "point";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_LINE = "line";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_PLINE = "pline";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_REGION = "region";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_ARC = "arc";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_TEXT = "text";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_RECT = "rectangle";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_ROUNDRECT = "rounded rectangle";
    
    
    /** Geometry type identifier constant */
    public static final String TYPE_ELLIPSE = "ellipse";
    
    
    /** String Constant */
    public static final String CLAUSE_SYMBOL = "SYMBOL";
    
    
    /** String Constant */
    public static final String CLAUSE_PEN = "PEN";
    
    
    /** String Constant */
    public static final String CLAUSE_SMOOTH = "SMOOTH";
    
    
    /** String Constant */
    public static final String CLAUSE_CENTER = "CENTER";
    
    
    /** String Constant */
    public static final String CLAUSE_BRUSH = "BRUSH";
    
    
    /** String Constant */
    public static final String CLAUSE_VERSION = "Version";
    
    
    /** String Constant */
    public static final String CLAUSE_CHARSET = "Charset";
    
    
    /** String Constant */
    public static final String CLAUSE_DELIMETER = "DELIMITER";
    
    
    /** String Constant */
    public static final String CLAUSE_UNIQUE = "UNIQUE";
    
    
    /** String Constant */
    public static final String CLAUSE_INDEX = "INDEX";
    
    
    /** String Constant */
    public static final String CLAUSE_COLUMNS = "COLUMNS";
    
    // Header information
    String hVersion;
    String hCharset;
    String hDelimeter = "\t";
    ArrayList hColumnsNames;
    ArrayList hColumnsTypes;
    Vector hUnique;
    Vector hIndex;
    
    // CoordsSys not supported
    // Transform not supported
    // Global variables (for the initial read)
    private String line; // The current Line of the MIF file.
    private Vector pointFeatures; // The point Features read
    private Vector lineFeatures; // The line Features read
    private Vector polygonFeatures; // The polygon Features read
    
    // FeatureTypes for each supported Feature Type (POINT, LINE, POLYGON) - used to build features
    private FeatureType pointFeatureType;
    private FeatureType lineFeatureType;
    private FeatureType polygonFeatureType;
    
    // Factories to use to build Features
    //    private FlatFeatureFactory pointFactory;
    //    private FlatFeatureFactory lineFactory;
    //    private FlatFeatureFactory polygonFactory;
    
    private FeatureType pointType;
    private FeatureType lineType;
    private FeatureType polygonType;
    
    // Factory to use to build Geometries
    private GeometryFactory geomFactory;
    
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private static final StyleFactory styleFactory = StyleFactory.createStyleFactory();
    private Stroke stroke = styleFactory.getDefaultStroke();
    private Fill fill = styleFactory.getDefaultFill();
    
    private String filename;
    
    /**
     * Construct MapInfoDataSource and attach it to the specified file
     * <p>
     * Package visisbility - we should only really play with this
     * via the DataStore api.
     * </p>
     * @param url location of the mif file to read
     * @throws MalformedURLException invalid URL was used
     * @throws UnsupportedEncodingException 
     */
    MapInfoDataSource(URL url) throws UnsupportedEncodingException {
        filename = java.net.URLDecoder.decode( url.getFile(),null );
        geomFactory = new GeometryFactory();
    }
    
    protected File file( String filename, String ext ) throws FileNotFoundException{
    	File file = new File(filename);
    	//if( file.exists() ) return file;
    	
    	file = new File( setExtension( filename, ext.toUpperCase() ));
    	if( file.exists() ) return file;
    	
    	file = new File( setExtension( filename, ext.toLowerCase() ));
    	if( file.exists() ) return file;
    	
    	file = new File( setExtension( filename.toLowerCase(), ext.toLowerCase() ));
    	if( file.exists() ) return file;
    	
    	file = new File( setExtension( filename.toUpperCase(), ext.toUpperCase() ));
    	if( file.exists() ) return file;
    	
    	// that is it I'm out of guesses
    	throw new FileNotFoundException( "Could not locate '"+filename+"."+ext+"'");
    }
    /** Reads the MIF and MID files and returns a Vector of the Features they contain
     * @return a vector of features
     * @throws DataSourceException if file doesn't exist or is not readable etc
     */
    protected Vector readMifMid() throws DataSourceException {
        if (filename == null) {
            throw new DataSourceException("Invalid filename passed to readMifMid");
        }        
        // Read files
        try {
        	File mif = file( filename, "MIF" );
        	File mid = file( filename, "MID" );
                
            Vector features = readMifMid(
            		new BufferedReader(new FileReader(mif)),
					new BufferedReader(new FileReader(mid))
			);
            return features;
        } catch (FileNotFoundException fnfexp) {
            throw new DataSourceException("FileNotFoundException trying to read mif file : ",
            fnfexp);
        }
    }
    
    private String setExtension(String filename, String ext) {
        if (ext.indexOf(".") == -1) {
            ext = "." + ext;
        }
        
        if (filename.lastIndexOf(".") == -1) {
            return filename + ext;
        }
        
        return filename.substring(0, filename.lastIndexOf(".")) + ext;
    }
    
    /**
     * This private method constructs the factories used to create the Feature, and Geometries as
     * they are read It takes it's setup values from the value of the COLUMNS clause in the MIF
     * file
     *
     * @throws DataSourceException
     */
    private void setUpFactories() throws DataSourceException {
        // Go through each column name, and set up an attribute for each one
        ArrayList colAttribs = new ArrayList();
        
        // Add attributes for each column
        //Iterator it = hColumns.keySet().iterator();
        for (int i = 0; i < hColumnsNames.size(); i++) {
            String type = ((String) hColumnsTypes.get(i)).toLowerCase();
            Class typeClass = null;
            
            if (type.equals("float") || type.startsWith("decimal")) {
                typeClass = Double.class;
                hColumnsTypes.set(i, "Double");
            } else if (type.startsWith("char")) {
                typeClass = String.class;
                hColumnsTypes.set(i, "String");
            } else if (type.equals("integer") || type.equals("smallint")) {
                typeClass = Integer.class;
                hColumnsTypes.set(i, "Integer");
            } else {
                LOGGER.fine("unknown type in mif/mid read " + type + " storing as String");
                typeClass = String.class;
                hColumnsTypes.set(i, "String");
            }
            
            colAttribs.add(AttributeTypeFactory.newAttributeType((String) hColumnsNames.get(i), typeClass));
        }
        
        
        // Add default Geometry attribute type
        colAttribs.add(0, AttributeTypeFactory.newAttributeType("point", Geometry.class));
        
        // create point feature Type & factory
        try {
            pointType = FeatureTypeFactory.newFeatureType(
            (AttributeType[]) colAttribs.toArray(new AttributeType[0]),
            filename.toString() + "_point");
            
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up point factory : ", schexp);
        }
        
        
        // Set up Line factory
        // Add default attribute type
        colAttribs.set(0, AttributeTypeFactory.newAttributeType("line", Geometry.class));
        
        // create line feature Type & factory
        try {
            lineType = FeatureTypeFactory.newFeatureType(
            (AttributeType[]) colAttribs.toArray(new AttributeType[0]),
            filename.toString() + "_line");
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up line factory : ", schexp);
        }
        
        
        // Set up Polygon factory
        // Add default attribute type
        colAttribs.set(0, AttributeTypeFactory.newAttributeType("polygon", Geometry.class));
        
        // create polygon feature Type & factory
        try {
            polygonType = FeatureTypeFactory.newFeatureType(
            (AttributeType[]) colAttribs.toArray(new AttributeType[0]),
            filename.toString() + "_poly");
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up polygon factory : ", schexp);
        }
    }
    
    /**
     * Reads an entire MID/MIF file. (Two files, actually, separately opened)
     *
     * @param mifReader An opened BufferedReader to the MIF file.
     * @param midReader An opened BufferedReader to the MID file.
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Vector readMifMid(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        // Read the MIF header
        readMifHeader(mifReader);
        
        
        // Set up factories
        setUpFactories();
        
        Vector features = new Vector();
        
        // Start by reading first line
        try {
            line = readMifLine(mifReader);
        } catch (IOException ioexp) {
            throw new DataSourceException("No data at start of file", ioexp);
        }
        
        Feature feature;
        
        // Read each object in the MIF file
        while ((feature = readObject(mifReader, midReader)) != null) {
            // Figure out which type of feature it is
            // Add to relevent vector
            features.addElement(feature);
        }
        
        return features;
    }
    
    /**
     * Reads the header from the given MIF file stream
     *
     * @param mifReader
     *
     * @throws DataSourceException
     */
    private void readMifHeader(BufferedReader mifReader)
    throws DataSourceException {
        try {
            while ((readMifLine(mifReader) != null) && !line.trim().equalsIgnoreCase("DATA")) {
                if (clause(line).equalsIgnoreCase(CLAUSE_VERSION)) {
                    // Read Version clause
                    hVersion = line.trim().substring(line.trim().indexOf(' ')).trim();
                    LOGGER.info("version [" + hVersion + "]");
                }
                
                if (clause(line).equalsIgnoreCase(CLAUSE_CHARSET)) {
                    // Read Charset clause
                    //hCharset = line.replace('\"',' ').trim().substring(line.trim().indexOf(' ')).trim();
                    hCharset = remainder(line).replace('"', ' ').trim();
                    LOGGER.info("Charset [" + hCharset + "]");
                }
                
                if (clause(line).equalsIgnoreCase(CLAUSE_DELIMETER)) {
                    // Read Delimeter clause
                    hDelimeter = line.replace('\"', ' ').trim().substring(line.trim().indexOf(' '))
                    .trim();
                    
                    if(hDelimeter.length()==0) {
                        //triming removed everything so it must have been a space.
                        hDelimeter=" ";
                    }
                    LOGGER.info("delimiter [" + hDelimeter + "]");
                }
                
                if (clause(line).equalsIgnoreCase(CLAUSE_UNIQUE)) {
                    // Read Unique clause
                    StringTokenizer st = new StringTokenizer(line.trim()
                    .substring(line.trim()
                    .indexOf(' ')), ",");
                    hUnique = new Vector();
                    LOGGER.info("Unique cols ");
                    
                    while (st.hasMoreTokens()) {
                        String uniq = st.nextToken();
                        LOGGER.info("\t" + uniq);
                        hUnique.addElement(uniq);
                    }
                }
                
                if (clause(line).equalsIgnoreCase(CLAUSE_INDEX)) {
                    // Read Index clause
                    StringTokenizer st = new StringTokenizer(line.trim()
                    .substring(line.trim()
                    .indexOf(' ')), ",");
                    hIndex = new Vector();
                    LOGGER.info("Indexes");
                    
                    while (st.hasMoreTokens()) {
                        String index = st.nextToken();
                        LOGGER.info("\t" + index);
                        hIndex.addElement(index);
                    }
                }
                
                if (clause(line).equalsIgnoreCase(CLAUSE_COLUMNS)) {
                    // Read Columns clause
                    int cols = 0;
                    
                    try {
                        cols = Integer.parseInt(remainder(line));
                        
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.finest("Cols " + cols);
                        }
                    } catch (NumberFormatException nfexp) {
                        LOGGER.severe("bad number of colums " + nfexp);
                    }
                    
                    
                    // Read each of the columns
                    hColumnsNames = new ArrayList();
                    hColumnsTypes = new ArrayList();
                    
                    for (int i = 0; i < cols; i++) {
                        line = readMifLine(mifReader);
                        
                        //StringTokenizer st = new StringTokenizer(line.trim().substring(line.trim().indexOf(' ')), " ");
                        String name = clause(line);
                        String value = remainder(line);
                        
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.finest("column name " + name + " value " + value);
                        }
                        
                        hColumnsNames.add(name);
                        hColumnsTypes.add(value);
                    }
                }
            }
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading MIF header : " +
            ioexp.getMessage());
        }
    }
    
    /**
     * A 'Clause' is stored as a single string at the start of a line. This rips the clause name
     * out of the given line.
     *
     * @param line
     *
     * @return
     */
    private String clause(String line) {
        return clause(line, ' ');
    }
    
    private String clause(String line, char delimiter) {
        line = line.trim();
        
        int index = line.indexOf(delimiter);
        
        if (index == -1) {
            return line;
        } else {
            return line.substring(0, index).trim();
        }
    }
    
    /**
     * returns the last word of the string
     *
     * @param line
     *
     * @return
     */
    private String remainder(String line) {
        return remainder(line, ' ');
    }
    
    private String remainder(String line, char delimiter) {
        line = line.trim();
        
        int index = line.lastIndexOf(delimiter);
        
        if (index == -1) {
            return "";
        } else {
            return line.substring(index).trim();
        }
    }
    
    /**
     * Reads the next line in the reader, ignoring lines which are nothing but whitespace. Sets the
     * global 'line' variable to the currently read line
     *
     * @param reader
     *
     * @return
     *
     * @throws IOException
     * @throws DataSourceException
     */
    private String readMifLine(BufferedReader reader) throws IOException, DataSourceException {
        do {
            line = reader.readLine();
            
            if (line == null) {
                return null;
            }
            
            if (isShadingClause(line)) {
                LOGGER.finest("going to process shading");
                processShading(line);
                line = " ";
            }
        } while (line.trim().length() == 0);
        
        line = line.trim();
        
        //LOGGER.finest("returning line " + line);
        return line;
    }
    
    /**
     * Reads a single MIF Object (Point, Line, Region, etc.) as a Feature
     *
     * @param mifReader
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Feature readObject(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        Feature feature = null;
        
        //LOGGER.finest("line = " + line);
        // examine The current line
        if (line == null) {
            return null;
        }
        
        int index = line.indexOf(' ');
        
        if (index == -1) {
            index = line.length();
        }
        
        if (line.substring(0, index).equalsIgnoreCase(TYPE_POINT)) {
            // Read point data
            LOGGER.finest("Reading POINT");
            feature = readPointObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_LINE)) {
            // Read line data
            LOGGER.finest("Reading LINE");
            feature = readLineObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_PLINE)) {
            // Read pline data
            LOGGER.finest("Reading PLINE");
            feature = readPLineObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_REGION)) {
            // Read region data
            LOGGER.finest("Reading REGION");
            feature = readRegionObject(mifReader, midReader);
        } else {
            LOGGER.finest(line + " unknown object in mif reader");
        }
        
        return feature;
    }
    
    /**
     * Reads Point information from the MIF stream
     *
     * @param mifReader
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Feature readPointObject(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");
        
        try {
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            
            // Construct Geomtry
            Geometry pointGeom = geomFactory.createPoint(new Coordinate(x, y));
            
            
            // Read next line
            readMifLine(mifReader);
            
            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);
            
            
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(pointType, pointGeom, midValues);
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Built point feature : " + x + " " + y);
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : ", nfexp);
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : ", ioexp);
        }
        
        return feature;
    }
    
    /**
     * Reads Line information from the MIF stream
     *
     * @param mifReader
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Feature readLineObject(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");
        
        try {
            double x1 = Double.parseDouble(st.nextToken());
            double y1 = Double.parseDouble(st.nextToken());
            double x2 = Double.parseDouble(st.nextToken());
            double y2 = Double.parseDouble(st.nextToken());
            
            // Construct Geomtry
            Coordinate[] cPoints = new Coordinate[2];
            cPoints[0] = new Coordinate(x1, y1);
            cPoints[1] = new Coordinate(x2, y2);
            
            Geometry lineGeom = geomFactory.createLineString(cPoints);
            
            
            // Read next line
            readMifLine(mifReader);
            
            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);
            
            
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineType, lineGeom, midValues);
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Built line feature : " + x1 + " " + y1 + " - " + x2 + " " + y2);
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : " +
            nfexp.getMessage());
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : " +
            ioexp.getMessage());
        }
        
        return feature;
    }
    
    /**
     * Reads Multi-Line (PLine) information from the MIF stream
     *
     * @param mifReader
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Feature readPLineObject(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));
        
        try {
            int numsections = 1;
            
            if (st.hasMoreTokens() && st.nextToken().trim().equalsIgnoreCase("MULTIPLE")) {
                numsections = Integer.parseInt(st.nextToken());
            }
            
            // A vector of coordinates
            Vector coords = new Vector();
            
            // Read each polygon
            for (int i = 0; i < numsections; i++) {
                // Read line (number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));
                
                // Read each point
                for (int p = 0; p < numpoints; p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }
            }
            
            Geometry plineGeom = geomFactory.createLineString(
            (Coordinate[]) coords.toArray(
            new Coordinate[coords.size()]));
            
            
            // Read next line
            readMifLine(mifReader);
            
            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);
            
            
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineType, plineGeom, midValues);
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Read polyline (" + coords.size() + ")");
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : " +
            nfexp.getMessage());
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : " +
            ioexp.getMessage());
        }
        
        return feature;
    }
    
    /**
     * Reads Region (Polygon) information from the MIF stream
     *
     * @param mifReader
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private Feature readRegionObject(BufferedReader mifReader, BufferedReader midReader)
    throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));
        
        try {
            int numpolygons = Integer.parseInt(st.nextToken());
            
            // A vector of polygons
            Vector polys = new Vector();
            
            // Read each polygon
            for (int i = 0; i < numpolygons; i++) {
                Vector coords = new Vector();
                
                // Read number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));
                
                // Read each point
                for (int p = 0; p < numpoints; p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }
                
                
                // Create polygon from points
                coords.addElement(
                new Coordinate(((Coordinate) coords.get(0)).x,
                ((Coordinate) coords.get(0)).y));
                
                try {
                    Polygon pol = geomFactory.createPolygon(geomFactory.createLinearRing(
                    (Coordinate[]) coords.toArray(
                    new Coordinate[coords.size()])),
                    null);
                    
                    
                    // Add to vector
                    polys.addElement(pol);
                } catch (TopologyException topexp) {
                    throw new DataSourceException("TopologyException reading Region polygon : ",
                    topexp);
                }
            }
            
            Geometry polyGeom = geomFactory.createMultiPolygon(
            (Polygon[]) polys.toArray(new Polygon[polys.size()]));
            
            
            // Read next line
            readMifLine(mifReader);
            
            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);
            
            
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(polygonType, polyGeom, midValues);
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Read Region (" + polys.size() + ")");
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : ", nfexp);
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : ", ioexp);
        }
        
        return feature;
    }
    
    /**
     * Builds a complete Feature object using the given FeatureType, with the Geometry geom, and
     * the given attributes.
     *
     * @param featureType The FeatureType to use to constuct the Feature
     * @param factory
     * @param geom The Geometry to use as the default Geometry
     * @param attribs The attibutes to use as the Feature's attributes (Attributes must be set up
     *        in the FeatureType)
     *
     * @return A fully-formed Feature
     *
     * @throws DataSourceException
     */
    private Feature buildFeature(FeatureType featureType, Geometry geom,
    ArrayList attribs) throws DataSourceException {
        int numAttribs = featureType.getAttributeTypes().length;
        
        
        // add geometry to the attributes
        attribs.add(0, geom);
        
        if (numAttribs != attribs.size()) {
            LOGGER.severe("wrong number of attributes passed to buildFeature");
            throw new DataSourceException("wrong number of attributes passed to buildFeature.\n" +
            "expected " + numAttribs + " got " + attribs.size());
        }
        
        // Create Feature
        try {
            return featureType.create(attribs.toArray());
        } catch (IllegalAttributeException ifexp) {
            throw new DataSourceException("IllegalAttributeException creating feature : ", ifexp);
        }
    }
    
    /**
     * Reads a single line of the given MID file stream, and returns a hashtable of the data in it,
     * keyed byt he keys in the hColumns hash
     *
     * @param midReader
     *
     * @return
     *
     * @throws DataSourceException
     */
    private ArrayList readMid(BufferedReader midReader)
    throws DataSourceException {
        ArrayList midValues = new ArrayList();
        
        if (midReader == null) {
            return new ArrayList();
        }
        
        // The delimeter is a single delimiting character
        String midLine = "";
        
        try {
            midLine = midReader.readLine();
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Read MID " + midLine);
            }
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading MID file");
        }
        
        // read MID tokens
        int col = 0;
        //StringTokenizer delimeters = new StringTokenizer(midLine, hDelimeter + "\0");
        List tokens = parse(midLine, hDelimeter.charAt(0));
        Iterator values = tokens.iterator();
        while (values.hasNext()) {
            //String token = delimeters.nextToken();
            String value = (String)values.next();
            String type = (String) hColumnsTypes.get(col++);
            addAttribute(type, value.trim(), midValues);
        }
        return midValues;
    }
    
    private void addAttribute(String type, String token, ArrayList midValues) {
        if (type.equals("String")) {
            midValues.add(token);
        } else if (type.equals("Double")) {
            try {
                midValues.add(new Double(token));
            } catch (NumberFormatException nfe) {
                LOGGER.info("Bad double " + token);
                midValues.add(new Double(0.0));
            }
        } else if (type.equals("Integer")) {
            try {
                midValues.add(new Integer(token));
            } catch (NumberFormatException nfe) {
                LOGGER.info("Bad Integer value " + token);
                midValues.add(new Integer(0));
            }
        } else {
            LOGGER.info("Unknown type " + type);
        }
    }
    
    /**
     * Reads the shading information at the end of Object data
     *
     * @param line
     *
     * @throws DataSourceException
     */
    private void processShading(String line) throws DataSourceException {
        int color;
        int r;
        int g;
        int b;
        
        if (line == null) {
            return;
        }
        
        String shadeType = line.toLowerCase();
        String name = clause(shadeType, '(');
        String settings = remainder(shadeType, '(');
        StringTokenizer st = new StringTokenizer(settings, "(),");
        String[] values = new String[st.countTokens()];
        
        for (int i = 0; st.hasMoreTokens(); i++) {
            values[i] = st.nextToken();
        }
        
        if (name.equals("pen")) {
            try {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("setting new pen " + settings);
                    LOGGER.finest("width " + values[0]);
                }
                
                stroke.setWidth(filterFactory.createLiteralExpression(new Integer(values[0])));
                
                int pattern = Integer.parseInt(values[1]);
                
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("pattern = " + pattern);
                }
                
                stroke.setDashArray(MifStyles.getPenPattern(new Integer(pattern)));
                color = Integer.parseInt(values[2]);
                
                String rgb = Integer.toHexString(color);
                
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("color " + color + " -> " + rgb);
                }
                
                stroke.setColor(filterFactory.createLiteralExpression(rgb));
            } catch (Exception nfe) {
                throw new DataSourceException("Error setting up pen", nfe);
            }
            
            return;
        } else if (name.equals("brush")) {
            LOGGER.finest("setting new brush " + settings);
            
            int pattern = Integer.parseInt(values[0]);
            LOGGER.finest("pattern = " + pattern);
            
            Graphic dg = styleFactory.getDefaultGraphic();
            dg.addExternalGraphic(MifStyles.getBrushPattern(new Integer(pattern)));
            stroke.setGraphicFill(dg);
            color = Integer.parseInt(values[1]);
            
            String rgb = Integer.toHexString(color);
            LOGGER.finest("color " + color + " -> " + rgb);
            fill.setColor(filterFactory.createLiteralExpression(rgb)); // foreground
            
            if (values.length == 3) { // optional parameter
                color = Integer.parseInt(values[2]);
                rgb = Integer.toHexString(color);
                LOGGER.finest("color " + color + " -> " + rgb);
                
                fill.setBackgroundColor(filterFactory.createLiteralExpression(rgb)); // background
            } else {
                fill.setBackgroundColor((Expression) null);
            }
        } else if (name.equals("center")) {
            LOGGER.finest("setting center " + settings);
        } else if (name.equals("smooth")) {
            LOGGER.finest("setting smooth on");
        } else if (name.equals("symbol")) {
            LOGGER.finest("setting symbol " + settings);
            
            Mark symbol = null;
            ExternalGraphic eg = null;
            
            if (values.length == 3) { // version 3.0
                
                //symbol = symbols.get(new Integer(symNumb));
            } else if (values.length == 6) {}
            else if (values.length == 4) { // custom bitmap
                eg = styleFactory.createExternalGraphic("CustSymb/" + values[0],"image/unknown"); // hack!
                
            } else {
                LOGGER.info("unexpected symbol style " + name + settings);
            }
        } else if (name.equals("font")) {
            LOGGER.finest("setting font " + settings);
        } else {
            LOGGER.finest("unknown styling directive " + name + settings);
        }
        
        return;
    }
    
    /**
     * Test whether the given line contains a known shading clause keyword (PEN, STYLE, etc.)
     *
     * @param line
     *
     * @return
     */
    private boolean isShadingClause(String line) {
        line = line.toUpperCase();
        
        boolean ret = ((line.indexOf(CLAUSE_PEN) != -1) || (line.indexOf(CLAUSE_SYMBOL) != -1) ||
        (line.indexOf(CLAUSE_SMOOTH) != -1) ||
        (line.indexOf(CLAUSE_CENTER) != -1) || line.indexOf(CLAUSE_BRUSH) != -1);
        
        return ret;
    }
    
    /**
     * Loads features from the datasource into the passed collection, based on the passed filter.
     * Note that all data sources must support this method at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param query contains info about request of which features to retrieve.
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
        org.geotools.filter.Filter filter = null;
        if (query != null) {
            filter = query.getFilter();
        }
        Vector features = readMifMid();
        for(int i=0; i < features.size(); i++){
            if(filter == null || filter.contains((Feature)features.elementAt(i))){
                collection.add(features.elementAt(i));
            }
        }
    }
    
    
    /** Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks TODO: implement this method.
     * @return FeatureType of the features in the MIFMID file
     */
    public FeatureType getSchema(){
        return DefaultFeatureType.EMPTY;
    }
    
    private List parse(String s, char delim) {
        ArrayList tokens = new ArrayList();
        int index = 0;
        boolean inQuotes = false;
        boolean gettingToken = true;
        StringBuffer token = new StringBuffer("");
        
        while(true) {
            if(s.length() <= index) {
                if (!token.toString().equals(""))
                    tokens.add(token.toString());
                
                break;
            }
            
            char c = s.charAt(index);
            index++;
            if (c == '"') {
                if (inQuotes) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
                
                inQuotes = !inQuotes;
                gettingToken = inQuotes;
            }
            else if(c == delim || c == '\0') {
                if (inQuotes)
                    token.append(c);
                else if (gettingToken) {
                    // eat whitespaces
                    if (token.length() > 0) {
                        tokens.add(token.toString());
                        // initialize new token
                        token.setLength(0);
                        gettingToken = false;
                    }
                }
            }
            else {
                token.append(c);
                gettingToken = true;
            }
        }
        return tokens;
    }
    
    
    
}
