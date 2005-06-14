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
package org.geotools.data.mif;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.AttributeTypes;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO use java.sql.Date?
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


/**
 * <p>
 * MIFFile class allows sequential reading and writing of Features in MapInfo
 * MIF/MID text file format with a FeatureReader and FeatureWriter.
 * </p>
 * 
 * <p>
 * This class has been developed starting from MapInfoDataSource.
 * </p>
 * 
 * <p>
 * Open issues:
 * </p>
 * 
 * <ul>
 * <li>
 * CoordSys clause parsing is still not supported
 * </li>
 * </ul>
 * 
 *
 * @author Luca S. Percich, AMA-MI
 * @author Paolo Rizzi, AMA-MI
 *
 */
public class MIFFile {
    // Geometry type identifier constants 
    private static final String TYPE_NONE = "none";
    private static final String TYPE_POINT = "point";
    private static final String TYPE_LINE = "line";
    private static final String TYPE_PLINE = "pline";
    private static final String TYPE_REGION = "region";

    // The following object types are still not supported
    private static final String TYPE_ARC = "arc";
    private static final String TYPE_TEXT = "text";
    private static final String TYPE_RECT = "rect"; // could be converted to polygon
    private static final String TYPE_ROUNDRECT = "roundrect";
    private static final String TYPE_ELLIPSE = "ellipse";

    // New types introduced after version 6.0, still not supported
    private static final String TYPE_MULTIPOINT = "multipoint";
    private static final String TYPE_COLLECTION = "collection";

    // String Style Constants 
    private static final String CLAUSE_SYMBOL = "symbol";
    private static final String CLAUSE_PEN = "pen";
    private static final String CLAUSE_SMOOTH = "smooth";
    private static final String CLAUSE_CENTER = "center";
    private static final String CLAUSE_BRUSH = "brush";

    // Header parse Constants (& parameter names) 
    private static final String CLAUSE_COLUMNS = "columns";
    public static final int MAX_STRING_LEN = 255; // Max length for MapInfo Char() fields

    // Some (by now useless) default values
    private static final String DEFAULT_PEN = "Pen (1,2,0)";
    private static final String DEFAULT_BRUSH = "Brush (2,16777215,16777215)";
    private static final String DEFAULT_SYMBOL = "Symbol (34,0,12)";
    private static Logger LOGGER = Logger.getLogger(
            "org.geotools.data.mif.MIFFile");

    // Header information
    private HashMap header = new HashMap();

    // File IO Variables
    private File mifFile = null;

    // File IO Variables
    private File midFile = null;

    // File IO Variables
    private File mifFileOut = null;

    // File IO Variables
    private File midFileOut = null;
    private Object[] featureDefaults = null;
    private char chDelimiter = '\t'; // TAB is the default delimiter if not specified in header
    private Geometry nullGeometry = null;

    // Schema variables
    private FeatureType featureType = null;
    private int numAttribs = 0;
    private int geomFieldIndex = -1;

    // Parameters for coordinate transformation during file i/o
    private boolean useTransform = false;
    private float multX = 1;
    private float multY = 1;
    private float sumX = 0;
    private float sumY = 0;

    // Options & parameters
    private GeometryFactory geomFactory = null;
    private String fieldNameCase;
    private String geometryName;
    private String geometryClass;

    /**
     * <p>
     * This constructor opens an existing MIF/MID file, and creates the
     * corresponding schema from the file header
     * </p>
     * 
     * <p>
     * Allowed parameters in params Map:
     * </p>
     * 
     * <ul>
     * <li>
     * PARAM_GEOMFACTORY = GeometryFactory object to be used for creating
     * geometries;
     * </li>
     * <li>
     * PARAM_FIELDCASE = field names tranformation: "upper" to uppercase |
     * "lower" to lowercase | "" none;
     * </li>
     * <li>
     * PARAM_GEOMNAME = &lt;String&gt, name of the geometry field (defaults to
     * "the_geom");
     * </li>
     * <li>
     * PARAM_GEOMTYPE = geometry type handling: "untyped" uses Geometry class |
     * "typed" force geometry to the type of the first valid geometry found in
     * file | "multi" like typed, but forces LineString to MultilineString and
     * Polygon to MultiPolygon; <br>
     * (This option is not supported yet)
     * </li>
     * </ul>
     * 
     * <p>
     * Header clauses values can also be set in the params Map, but they might
     * be overridden by values read from MIF header.
     * </p>
     * 
     * <p>
     * Basic usage:
     * </p>
     * <pre><code>
     *   HashMap params = new HashMap();
     *   params.put(MIFFile.PARAM_GEOMFACTORY, new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING_SINGLE), SRID));
     *   params.put(MIFFile.PARAM_FIELDCASE, "upper");
     *   params.put(MIFFile.PARAM_GEOMNAME, "GEOM");
     *   params.put(MIFFile.PARAM_GEOMTYPE, "typed");
     *   MIFFile mf = new MIFFile("c:/some_path/file.mif",params);
     *   FeatureType ft = mf.getSchema();
     *   FeatureReader fr = mf.getFeatureReader();	
     *   while (fr.hasNext()) {
     *   	Feature in = fr.next();
     *   	doSomethingWithFeature(in);
     *   }
     *   fr.close(); // closes file resources
     * </code></pre>
     *
     * @param path Full pathName
     * @param params DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public MIFFile(String path, Map params) throws IOException {
        super();

        parseParams(params);

        initFiles(path, true);

        MIFFileTokenizer mifTokenizer = new MIFFileTokenizer(new BufferedReader(
                    new FileReader(mifFile)));

        try {
            readMifHeader(false, mifTokenizer);
        } catch (Exception e) {
            throw new IOException("Can't read MIF header: " + e.toString());
        } finally {
            try {
                mifTokenizer.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * <p>
     * This constructor creates a a new MIF/MID file given schema and path.  If
     * a .mif/.mid file pair already exists, it will be overwritten.
     * </p>
     * 
     * <p>
     * Basic usage:
     * </p>
     * <pre><code>
     *   HashMap params = new HashMap();
     *   params.put(MIFFile.MIFDataStore.HCLAUSE_COORDSYS, "Nonearth \"m\"");
     * 
     *   MIFFile mf = new MIFFile("c:/some_path/", ft, params);
     * 
     * 
     *   FeatureWriter fw = mf.getFeatureWriter();
     * 
     *   while(...) {
     * 	    Feature f = fw.next();
     * 			f.setAttribute(...,...);
     * 			fw.write();
     * 	 }
     * 
     *   fw.close();
     * </code></pre>
     *
     * @param path Full path & file name of the MIF file to create
     * @param featureType
     * @param params Parameter map
     *
     * @throws IOException DOCUMENT ME!
     * @throws SchemaException DOCUMENT ME!
     */
    public MIFFile(String path, FeatureType featureType, HashMap params)
        throws IOException, SchemaException {
        super();

        parseParams(params);

        setSchema(featureType);
        initFiles(path, false);

        PrintStream outMif = new PrintStream(new FileOutputStream(mifFile, false));
        PrintStream outMid = new PrintStream(new FileOutputStream(midFile, false));

        // writes out header
        outMif.println(exportHeader());

        outMif.close();
        outMid.close();
    }

    /**
     * Parses the parameters map into fields:
     *
     * @param params
     *
     * @throws IOException DOCUMENT ME!
     */
    private void parseParams(Map params) throws IOException {
        if (params == null) {
            params = new HashMap();
        }

        // Sets defaults for header
        setHeaderClause(MIFDataStore.HCLAUSE_VERSION,
            (String) getParam(MIFDataStore.HCLAUSE_VERSION, "300", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_CHARSET,
            (String) getParam(MIFDataStore.HCLAUSE_CHARSET, "WindowsLatin1",
                false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_DELIMITER,
            (String) getParam(MIFDataStore.HCLAUSE_DELIMITER,
                String.valueOf(chDelimiter), false, params));
        chDelimiter = getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER).charAt(0);

        setHeaderClause(MIFDataStore.HCLAUSE_UNIQUE,
            (String) getParam(MIFDataStore.HCLAUSE_UNIQUE, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_INDEX,
            (String) getParam(MIFDataStore.HCLAUSE_INDEX, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_COORDSYS,
            (String) getParam(MIFDataStore.HCLAUSE_COORDSYS, "", false, params));
        setHeaderClause(MIFDataStore.HCLAUSE_TRANSFORM,
            (String) getParam(MIFDataStore.HCLAUSE_TRANSFORM, "", false, params));

        geomFactory = (GeometryFactory) getParam(MIFDataStore.PARAM_GEOMFACTORY,
                null, false, params);

        if (geomFactory == null) {
            geomFactory = new GeometryFactory(new PrecisionModel(
                        PrecisionModel.FLOATING), 0);
        }

        nullGeometry = geomFactory.createPoint(new Coordinate(0, 0));

        geometryName = (String) getParam(MIFDataStore.PARAM_GEOMNAME,
                "the_geom", false, params);
        fieldNameCase = ((String) getParam(MIFDataStore.PARAM_FIELDCASE, "",
                false, params)).toLowerCase();

        geometryClass = ((String) getParam(MIFDataStore.PARAM_GEOMTYPE,
                "untyped", false, params)).toLowerCase();
    }

    /**
     * Returns a parameter value from the parameters map
     *
     * @param name
     * @param defa
     * @param required
     * @param params DOCUMENT ME!
     *
     * @return
     *
     * @throws IOException if required parameter is missing
     */
    private Object getParam(String name, Object defa, boolean required,
        Map params) throws IOException {
        Object result;

        try {
            result = params.get(name);
        } catch (Exception e) {
            result = null;
        }

        if (result == null) {
            if (required) {
                throw new IOException("MIFFile: parameter " + name
                    + " is required");
            }

            result = defa;
        }

        return result;
    }

    /**
     * <p>
     * Sets the value for a Header Clause. Possible values are:
     * </p>
     * 
     * <ul>
     * <li>
     * MIFDataStore.HCLAUSE_VERSION = Version number ("310")
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_CHARSET = Charset name ("WindowsLatin1")
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_UNIQUE = Comma-separated list of field indexes
     * (1..numFields) corresponding to unique values (i.e. street names for
     * street segments)
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_INDEX = Comma-separated list of field indexes
     * (1..numFields) indicating which fields have to be indexed in MapInfo
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_COORDSYS = MapInfo CoordSys clause
     * </li>
     * <li>
     * MIFDataStore.HCLAUSE_TRANSFORM = Comma-separated list of four
     * transformation parameters ("1000, 1000, 0, 0")
     * </li>
     * </ul>
     * 
     *
     * @param clause Name of the Header Clause
     * @param value Value for the Header Clause
     *
     * @throws IOException DOCUMENT ME!
     */
    private void setHeaderClause(String clause, String value)
        throws IOException {
        if (value == null) {
            value = "";
        }

        if (clause.equals(MIFDataStore.HCLAUSE_DELIMITER)
                && (value.equals("") || value.equals("\""))) {
            throw new IOException("Bad delimiter specified");
        }

        header.put(clause, value);
    }

    /**
     * Gets the value for an header clause
     *
     * @param clause
     *
     * @return
     */
    public String getHeaderClause(String clause) {
        try {
            return (String) getParam(clause, "", false, header);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * <p>
     * Opens the MIF file for input and returns a FeatureReader for accessing
     * the features.
     * </p>
     * 
     * <p>
     * TODO Concurrent file access is still not handled. MUST LOCK FILE and
     * return an error if another FeatureReader is open - Handle concurrent
     * access with synchronized(mif) / or Filesystem locking is enough?
     * </p>
     *
     * @return A FeatureReader for reading features from MIF/MID file
     *
     * @throws IOException
     */
    public FeatureReader getFeatureReader() throws IOException {
        MIFFileTokenizer mifTokenizer = null;
        MIFFileTokenizer midTokenizer = null;

        // if exists outMIF throw new IOException("File is being accessed in write mode");
        try {
            mifTokenizer = new MIFFileTokenizer(new BufferedReader(
                        new FileReader(mifFile)));
            midTokenizer = new MIFFileTokenizer(new BufferedReader(
                        new FileReader(midFile)));
            readMifHeader(true, mifTokenizer); // skips header

            return new Reader(mifTokenizer, midTokenizer);
        } catch (Exception e) {
            if (mifTokenizer != null) {
                mifTokenizer.close();
            }

            if (midTokenizer != null) {
                midTokenizer.close();
            }

            throw new IOException("Error initializing reader: " + e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return A featureWriter for this file
     *
     * @throws IOException
     */
    public FeatureWriter getFeatureWriter() throws IOException {
        return getFeatureWriter(false);
    }

    /**
     * <p>
     * Private FeatureWriter in append mode, could be called by
     * DataStore.getFeatureWriterAppend(); not implemented yet
     * </p>
     *
     * @param append
     *
     * @return
     *
     * @throws IOException
     */
    private FeatureWriter getFeatureWriter(boolean append)
        throws IOException {
        if (append) {
            // copy inMif to OutMIf
        } else {
            // WriteHeader
        }

        PrintStream outMif = new PrintStream(new FileOutputStream(mifFileOut,
                    append));
        PrintStream outMid = new PrintStream(new FileOutputStream(midFileOut,
                    append));

        return new Writer(outMif, outMid, append);
    }

    /**
     * Creates the MIF file header
     *
     * @return the Header as a String
     *
     * @throws SchemaException DOCUMENT ME!
     */
    private String exportHeader() throws SchemaException {
        // Header tags passed in parameters are overridden by the tags read from mif file 
        String header = exportClause(MIFDataStore.HCLAUSE_VERSION, true, false)
            + exportClause(MIFDataStore.HCLAUSE_CHARSET, true, true) // TODO Charset clause support should imply character conversion????
            + exportClause(MIFDataStore.HCLAUSE_DELIMITER, true, true)
            + exportClause(MIFDataStore.HCLAUSE_UNIQUE, false, false)
            + exportClause(MIFDataStore.HCLAUSE_INDEX, false, false)
            + exportClause(MIFDataStore.HCLAUSE_COORDSYS, false, false)
            + exportClause(MIFDataStore.HCLAUSE_TRANSFORM, false, false);

        header += ("Columns " + (numAttribs - 1) + "\n");

        for (int i = 1; i < numAttribs; i++) {
            AttributeType at = featureType.getAttributeType(i);
            header += ("  " + at.getName() + " " + getMapInfoAttrType(at)
            + "\n");
        }

        header += "Data\n";

        return header;
    }

    private String exportClause(String clause, boolean required, boolean quote)
        throws SchemaException {
        String result = getHeaderClause(clause);

        if (!result.equals("")) {
            if (quote) {
                result = MIFStringTokenizer.strQuote(result);
            }

            return clause + " " + result + "\n";
        }

        if (required) {
            throw new SchemaException("Header clause " + clause
                + " is required.");
        }

        return "";
    }

    /**
     * Maps an AttributeType to a MapInfo field type
     *
     * @param at Attribute Type
     *
     * @return the String description of the MapInfo Type
     */
    private String getMapInfoAttrType(AttributeType at) {
        if (at.getType() == String.class) {
            int l = AttributeTypes.getFieldLength(at, MAX_STRING_LEN);

            if (l <= 0) {
                l = MAX_STRING_LEN;
            }

            return "Char(" + l + ")";
        } else if (at.getType() == Integer.class) {
            return "Integer";
        } else if (at.getType() == Double.class) {
            return "Float";
        } else if (at.getType() == Boolean.class) {
            return "Logical";
        } else if (at.getType() == Date.class) {
            return "Date";
        } else {
            return "Char(" + MAX_STRING_LEN + ")"; // TODO Should it raise an exception here (UnsupportedSchema) ?
        }
    }

    /**
     * Sets the location name of the MIFMID file
     *
     * @param path The full path of the MIF file
     * @param mustExist True if opening file for reading
     *
     * @throws FileNotFoundException DOCUMENT ME!
     */
    private void initFiles(String path, boolean mustExist)
        throws FileNotFoundException {
        File file = new File(path);

        if (file.isDirectory()) {
            throw new FileNotFoundException(path + " is a directory");
        }

        String fName = file.getName();
        int ext = fName.lastIndexOf(".");

        if (ext > 0) {
            String theExt = fName.substring(ext + 1);

            if (!(theExt.equalsIgnoreCase("MIF")
                    || theExt.equalsIgnoreCase("MID"))) {
                throw new FileNotFoundException(
                    "Please specify a MIF or MID file extension.");
            }

            fName = fName.substring(0, ext);
        }

        file = file.getParentFile();

        mifFile = getFileHandler(file, fName, ".MIF", mustExist);
        midFile = getFileHandler(file, fName, ".MID", mustExist);

        mifFileOut = getFileHandler(file, fName, ".MIF.out", false);
        midFileOut = getFileHandler(file, fName, ".MID.out", false);
    }

    /**
     * Utility function for initFiles
     *
     * @param path DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     * @param ext DOCUMENT ME!
     * @param mustExist DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     */
    private File getFileHandler(File path, String fileName, String ext,
        boolean mustExist) throws FileNotFoundException {
        File file = new File(path, fileName + ext.toUpperCase());

        if (file.exists() || !mustExist) {
            return file;
        }

        file = new File(path, fileName + ext.toLowerCase());

        if (file.exists()) {
            return file;
        }

        throw new FileNotFoundException("Can't find file: " + file.getName());
    }

    /**
     * Reads the header from the given MIF file stream
     *
     * @param skipRead Skip the header, just to get to the data section
     * @param mif DOCUMENT ME!
     *
     * @throws IOException
     * @throws SchemaException DOCUMENT ME!
     */
    private void readMifHeader(boolean skipRead, MIFFileTokenizer mif)
        throws IOException, SchemaException {
        try {
            String tok;

            while (mif.readLine()) {
                tok = mif.getToken().toLowerCase();

                // "data" might be a field name, in this case the type name would follow on the same line 
                if (tok.equals("data") && mif.getLine().equals("")) {
                    break;
                }

                if (skipRead) {
                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_VERSION)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_VERSION, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_CHARSET)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_CHARSET,
                        mif.getToken(' ', false, true));

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_DELIMITER)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_DELIMITER,
                        mif.getToken(' ', false, true));
                    chDelimiter = getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER)
                                      .charAt(0);

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_UNIQUE)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_UNIQUE, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_COORDSYS)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_COORDSYS, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_INDEX)) {
                    setHeaderClause(MIFDataStore.HCLAUSE_INDEX, mif.getLine());

                    continue;
                }

                if (tok.equals(MIFDataStore.HCLAUSE_TRANSFORM)) {
                    useTransform = true;
                    multX = Float.parseFloat("0" + mif.getToken(','));
                    multY = Float.parseFloat("0" + mif.getToken(','));
                    sumX = Float.parseFloat("0" + mif.getToken(','));
                    sumY = Float.parseFloat("0" + mif.getToken(','));

                    if (multX == 0) {
                        multX = 1;
                    }

                    if (multY == 0) {
                        multY = 1;
                    }

                    continue;
                }

                if (tok.equals(CLAUSE_COLUMNS)) {
                    int cols;

                    try {
                        cols = Integer.parseInt(mif.getLine());
                    } catch (NumberFormatException nfexp) {
                        throw new IOException("bad number of colums: "
                            + mif.getLine());
                    }

                    // Columns <n> does not take into account the geometry column, so we increment
                    AttributeType[] columns = new AttributeType[++cols];

                    String name;
                    String type;
                    Object defa;
                    Class typeClass;
                    int size;

                    for (int i = 1; i < cols; i++) {
                        if (!mif.readLine()) {
                            throw new IOException("Expected column definition");
                        }

                        name = mif.getToken();

                        if (fieldNameCase.equalsIgnoreCase("upper")) {
                            name = name.toUpperCase();
                        } else if (fieldNameCase.equalsIgnoreCase("lower")) {
                            name = name.toLowerCase();
                        }

                        type = mif.getToken('(').toLowerCase();
                        defa = null;
                        typeClass = null;
                        size = 4;

                        if (type.equals("float") || type.equals("decimal")) {
                            typeClass = Double.class;
                            size = 8;
                            defa = new Double(0.0);

                            // TODO: check precision?
                        } else if (type.startsWith("char")) {
                            typeClass = String.class;
                            size = Integer.parseInt(mif.getToken(')'));
                            defa = "";
                        } else if (type.equals("integer")
                                || type.equals("smallint")) {
                            typeClass = Integer.class;
                            defa = new Integer(0);

                            // TODO: apply a restriction for Smallint (value between -32768 and +32767)
                        } else if (type.equals("logical")) {
                            typeClass = Boolean.class;
                            size = 2; // ???
                            defa = new Boolean(false);
                        } else if (type.equals("date")) {
                            typeClass = Date.class; // MapInfo format: yyyymmdd
                            size = 4; // ???
                            defa = null; // Dates are "nillable" (like Strings can be empty)
                        } else {
                            LOGGER.fine("unknown type in mif/mid read " + type
                                + " storing as String");
                            typeClass = String.class;
                            size = 254;
                            defa = "";
                        }

                        // Apart from Geometry, MapInfo table fields cannot be null, so Nillable is always false and default value must always be provided!
                        columns[i] = AttributeTypeFactory.newAttributeType(name,
                                typeClass, (defa == null), size, defa);
                    }

                    // If Table_is_mappable()
                    Class geomType = Geometry.class;

                    if (!geometryClass.equalsIgnoreCase("untyped")) {
                        // TODO look for the first valid geometry to determine exact Geometry subtype for FT
                        if (geometryClass.equalsIgnoreCase("multi")) {
                            // TODO Switch a flag to be used in readRegionOject and ReadPlineObject forcing conversion to MultiLineString and MultiPolygon
                        }
                    }

                    columns[0] = AttributeTypeFactory.newAttributeType(geometryName,
                            geomType, true);

                    try {
                        String typeName = mifFile.getName();
                        typeName = typeName.substring(0, typeName.indexOf("."));

                        // TODO switch to FeatureTypeBuilder
                        setSchema(FeatureTypeFactory.newFeatureType(columns,
                                typeName));
                    } catch (SchemaException schexp) {
                        throw new SchemaException(
                            "Exception creating feature type from MIF header: "
                            + schexp.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("IOException reading MIF header, line "
                + mif.getLineNumber() + ": " + e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current FeatureType associated with the MIF file
     */
    public FeatureType getSchema() {
        return featureType;
    }

    /**
     * Sets the schema (FeatureType) and creates value setters and IO object
     * buffer
     *
     * @param ft
     *
     * @throws SchemaException DOCUMENT ME!
     */
    private void setSchema(FeatureType ft) throws SchemaException {
        featureType = ft;

        numAttribs = featureType.getAttributeCount();
        geomFieldIndex = -1;

        // Creates the input buffer for reading MID file
        featureDefaults = new Object[numAttribs];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeType at = featureType.getAttributeType(i);

            Class atc = at.getType();

            if (Geometry.class.isAssignableFrom(atc)) {
                if (geomFieldIndex >= 0) {
                    throw new SchemaException(
                        "Feature Types with more than one geometric attribute are not supported.");
                }

                if (i > 0) {
                    throw new SchemaException(
                        "Geometry must be the first attribute in schema.");
                }

                geomFieldIndex = i; // = 0
            }
        }

        MIFValueSetter[] tmp = getValueSetters();

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            if (i != geomFieldIndex) {
                tmp[i].setString("");
                featureDefaults[i] = tmp[i].getValue();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return An array of valueSetters to be used for IO operations
     */
    private MIFValueSetter[] getValueSetters() {
        MIFValueSetter[] fieldValueSetters = new MIFValueSetter[numAttribs];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeType at = featureType.getAttributeType(i);
            Class atc = at.getType();

            if (i == geomFieldIndex) {
                fieldValueSetters[i] = null;
            } else if (atc == Integer.class) {
                fieldValueSetters[i] = new MIFValueSetter("0") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Integer(strValue);
                            }
                        };
            } else if (atc == Double.class) {
                fieldValueSetters[i] = new MIFValueSetter("0") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Double(strValue);
                            }

                            protected void valueToString() {
                                // TODO use DecimalFormat class!!!
                                super.valueToString();
                            }
                        };
            } else if (atc == Boolean.class) {
                fieldValueSetters[i] = new MIFValueSetter("false") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new Boolean("T".equalsIgnoreCase(
                                            strValue) ? "true"
                                                      : ("F".equalsIgnoreCase(
                                            strValue) ? "false" : strValue));
                            }

                            protected void valueToString() {
                                if ((objValue == null)
                                        || (((Boolean) objValue).booleanValue() == false)) {
                                    strValue = "F";
                                } else {
                                    strValue = "T";
                                }
                            }
                        };
            } else if (atc == Date.class) {
                // TODO Check conversion of date values - switch to java.sql.Date
                fieldValueSetters[i] = new MIFValueSetter("") {
                            protected SimpleDateFormat dateFormat = new SimpleDateFormat(
                                    "yyyyMMdd");

                            protected void stringToValue()
                                throws Exception {
                                if ((strValue != null) && !strValue.equals("")) {
                                    objValue = dateFormat.parse(strValue);
                                } else {
                                    objValue = null;
                                }

                                // Date.valueOf(strValue.substring(0, 4) + "-" + strValue.substring(4, 6) + "-" + strValue.substring(6));
                            }

                            protected void valueToString() {
                                if (objValue == null) {
                                    strValue = "";
                                } else {
                                    strValue = dateFormat.format(objValue);

                                    // strValue = ((Date) objValue).getYear() + "" + ((Date) objValue).getMonth() + "" + ((Date) objValue).getDay();
                                }
                            }
                        };
            } else {
                fieldValueSetters[i] = new MIFValueSetter("") {
                            protected void stringToValue()
                                throws Exception {
                                objValue = new String(strValue);
                            }

                            // Quotes the string
                            protected void valueToString() {
                                strValue = new String("\""
                                        + objValue.toString().replaceAll("\"",
                                            "\"\"") + "\"");
                            }
                        };
            }
        }

        return fieldValueSetters;
    }

    /**
     * Utility function for copying or moving files
     *
     * @param in Source file
     * @param out Destination file
     * @param deleteIn If true, source will be deleted upon successfull copy
     *
     * @throws IOException
     */
    protected static void copyFileAndDelete(File in, File out, boolean deleteIn)
        throws IOException {
        try {
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out)
                .getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
            sourceChannel.close();
            destinationChannel.close();

            if (deleteIn) {
                in.delete();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * <p>
     * Private FeatureReader inner class for reading Features from the MIF file
     * </p>
     */
    private class Reader implements FeatureReader {
        private MIFFileTokenizer mif = null;
        private MIFFileTokenizer mid = null;
        private boolean mifEOF = false;
        private Feature inputFeature = null;
        private Object[] inputBuffer = null;
        private MIFValueSetter[] fieldValueSetters;

        private Reader(MIFFileTokenizer mifTokenizer,
            MIFFileTokenizer midTokenizer) throws IOException {
            inputBuffer = new Object[numAttribs];
            fieldValueSetters = getValueSetters();
            mif = mifTokenizer;
            mid = midTokenizer;
            inputFeature = readFeature();
        }

        public boolean hasNext() {
            return (inputFeature != null);
        }

        // Reads the next feature and returns the last one
        public Feature next() throws NoSuchElementException {
            if (inputFeature == null) {
                throw new NoSuchElementException("Reached the end of MIF file");
            }

            Feature temp = inputFeature;

            try {
                inputFeature = readFeature();
            } catch (Exception e) {
                throw new NoSuchElementException(
                    "Error retrieving next feature: " + e.toString());
            }

            return temp;
        }

        public FeatureType getFeatureType() {
            return featureType;
        }

        public void close() {
            try {
                if (mif != null) {
                    mif.close();
                }

                if (mid != null) {
                    mid.close();
                }
            } finally {
                mif = null;
                mid = null;
            }
        }

        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        /**
         * Reads a single MIF Object (Point, Line, Region, etc.) as a Feature
         *
         * @return The feature, or null if the end of file was reached
         *
         * @throws IOException
         */
        private Feature readFeature() throws IOException {
            Feature feature = null;
            Geometry geom = readGeometry();

            if (mifEOF) {
                return null;
            }

            if (!mid.readLine()) {
                // TODO According to MapInfo spec., MID file is optional... in this case we should return the default values for the feature
                if (geom != null) {
                    throw new IOException("Unexpected end of MID file.");
                }

                return null;
            }

            if (geom == nullGeometry) {
                geom = null;
            }

            // Reads data from mid file
            // Assumes that geomFieldIndex == 0
            try {
                String tok = "";
                int col = 0;

                while (!mid.isEmpty()) {
                    tok = mid.getToken(chDelimiter, false, true);

                    if (!fieldValueSetters[++col].setString(tok)) {
                        LOGGER.severe("Bad value:"
                            + fieldValueSetters[col].getError());
                    }

                    inputBuffer[col] = fieldValueSetters[col].getValue();
                }

                if (col != (numAttribs - 1)) {
                    throw new Exception(
                        "Bad number of attributes read on MID row "
                        + mid.getLineNumber() + ": found " + col
                        + ", expecting " + numAttribs);
                }
            } catch (Exception e) {
                throw new IOException("Error reading MID file, line "
                    + mid.getLineNumber() + ": " + e.getMessage());
            }

            // Now add geometry and build the feature
            try {
                inputBuffer[0] = geom;
                feature = featureType.create(inputBuffer);
            } catch (Exception e) {
                throw new IOException("Exception building feature: "
                    + e.getMessage());
            }

            return feature;
        }

        /**
         * Reads one geometric object from the MIF file
         *
         * @return The geometry object
         *
         * @throws IOException
         */
        private Geometry readGeometry() throws IOException {
            if (!mif.readLine()) {
                mifEOF = true;

                return null;
            }

            Geometry geom = null;

            try {
                // First of all reads geometry
                String objType = mif.getToken().toLowerCase();

                if (objType.equals(TYPE_NONE)) {
                    geom = nullGeometry;
                } else if (objType.equals(TYPE_POINT)) {
                    geom = readPointObject();
                } else if (objType.equals(TYPE_LINE)) {
                    geom = readLineObject();
                } else if (objType.equals(TYPE_PLINE)) {
                    geom = readPLineObject();
                } else if (objType.equals(TYPE_REGION)) {
                    geom = readRegionObject();
                } else if (objType.equals(CLAUSE_PEN)
                        || objType.equals(CLAUSE_SYMBOL)
                        || objType.equals(CLAUSE_SMOOTH)
                        || objType.equals(CLAUSE_CENTER)
                        || objType.equals(CLAUSE_BRUSH)) {
                    // Symply ignores styling clauses, so let's read the next lines
                    geom = readGeometry();
                } else {
                    // TODO add MultiPoint & Collection!!!
                    throw new IOException(
                        "Unknown or unsupported object in mif file:" + objType);
                }
            } catch (Exception e) {
                throw new IOException("File " + mifFile.getName() + ", line "
                    + mif.getLineNumber() + ": " + e.getMessage());
            }

            return geom;
        }

        /**
         * Reads Multi-Line (PLine) information from the MIF stream
         *
         * @return
         *
         * @throws IOException
         */
        private Geometry readPLineObject() throws IOException {
            try {
                String tmp = mif.getToken(' ', true);
                int numsections = 1;
                int numpoints = 0;

                if (tmp.equalsIgnoreCase("MULTIPLE")) {
                    numsections = Integer.parseInt(mif.getToken(' ', true)); //read the number of sections
                    numpoints = Integer.parseInt(mif.getToken(' ', true)); //read the number of points
                } else {
                    // already got the number of points, simply parse it
                    numpoints = Integer.parseInt(tmp);
                }

                LineString[] lineStrings = new LineString[numsections];

                // Read each polyline
                for (int i = 0; i < lineStrings.length; i++) {
                    if (numpoints == 0) {
                        numpoints = Integer.parseInt(mif.getToken(' ', true));
                    }

                    Coordinate[] coords = new Coordinate[numpoints];

                    // Read each point
                    for (int p = 0; p < coords.length; p++) {
                        coords[p] = readMIFCoordinate();
                    }

                    numpoints = 0;

                    lineStrings[i] = geomFactory.createLineString(coords);
                }

                if (numsections == 1) {
                    LOGGER.finest("Read polyline()");

                    return (Geometry) lineStrings[0];
                }

                LOGGER.finest("Read MultiPolyline(" + lineStrings.length + ")");

                return (Geometry) geomFactory.createMultiLineString(lineStrings);
            } catch (Exception e) {
                throw new IOException(
                    "Exception reading PLine data from MIF file : "
                    + e.toString());
            }
        }

        /**
         * Reads Region (Polygon) information from the MIF stream
         *
         * @return
         *
         * @throws IOException
         */
        private Geometry readRegionObject() throws IOException {
            try {
                int numpolygons = Integer.parseInt(mif.getToken(' ', true));
                LinearRing[] rings = null;
                LinearRing theRing = null;

                if (numpolygons > 1) {
                    rings = new LinearRing[numpolygons - 1];
                }

                // Read each linearring; for now, assumes that all the other rings but the first one are interior holes
                for (int i = 0; i < numpolygons; i++) {
                    // TODO - Add support Multipolygons!!!!!!!
                    // Read coordinates
                    int numpoints = Integer.parseInt(mif.getToken(' ', true));
                    Coordinate[] coords = new Coordinate[numpoints + 1];

                    for (int p = 0; p < numpoints; p++) {
                        coords[p] = readMIFCoordinate();
                    }

                    coords[coords.length - 1] = coords[0];

                    LinearRing tmpRing = geomFactory.createLinearRing(coords);

                    if (theRing == null) {
                        theRing = tmpRing;
                    } else {
                        // if (theRing.contains(tmpRing)) {
                        rings[i - 1] = tmpRing;

                        // } else {
                        // Create Poly and add to multipolygon list
                        // theRing = tmpRing; // new element for Multipolygon
                    }
                }

                try {
                    Polygon pol = geomFactory.createPolygon(theRing, rings);

                    return pol;

                    // MultiPolygon polyGeom = geomFactory.createMultiPolygon(polys);
                    // return polyGeom;
                } catch (TopologyException topexp) {
                    throw new TopologyException(
                        "TopologyException reading Region polygon : "
                        + topexp.toString());
                }
            } catch (Exception e) {
                throw new IOException(
                    "Exception reading Region data from MIF file : "
                    + e.toString());
            }
        }

        /**
         * Reads a couple of coordinates (x,y) from input stream, applying the
         * transform factor if required.
         *
         * @return A Coordinate object, or null if error encountered
         *
         * @throws IOException if couldn't build a valid Coordinate object
         */
        private Coordinate readMIFCoordinate() throws IOException {
            String x;
            String y;

            try {
                x = mif.getToken(' ', true);
                y = mif.getToken();

                if (x.equals("") || y.equals("")) {
                    throw new IOException("End of file.");
                }

                Coordinate result = new Coordinate(Double.parseDouble(x),
                        Double.parseDouble(y));

                if (useTransform) {
                    result.x = (result.x * multX) + sumX;
                    result.y = (result.y * multY) + sumY;
                }

                return result;
            } catch (Exception e) {
                throw new IOException("Error getting coordinates: "
                    + e.toString());
            }
        }

        /**
         * Reads Point information from the MIF stream
         *
         * @return DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        private Geometry readPointObject() throws IOException {
            return geomFactory.createPoint(readMIFCoordinate());
        }

        /**
         * Reads Line information from the MIF stream
         *
         * @return DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        private Geometry readLineObject() throws IOException {
            Coordinate[] cPoints = new Coordinate[2];
            cPoints[0] = readMIFCoordinate();
            cPoints[1] = readMIFCoordinate();

            return geomFactory.createLineString(cPoints);
        }
    }

    /**
     * <p>
     * MIF Feature Writer
     * </p>
     *
     * @author sigfrido
     */
    private class Writer implements FeatureWriter {
        private PrintStream outMif = null;
        private PrintStream outMid = null;
        private FeatureReader innerReader = null;
        private MIFValueSetter[] fieldValueSetters;
        private Feature editFeature = null;
        private Feature originalFeature = null;

        private Writer(PrintStream mif, PrintStream mid, boolean append)
            throws IOException {
            innerReader = getFeatureReader();
            fieldValueSetters = getValueSetters();
            outMif = mif;
            outMid = mid;

            try {
                if (!append) {
                    outMif.println(exportHeader());
                }
            } catch (Exception e) {
                outMif = null;
                outMid = null;
                throw new IOException(e.getMessage());
            }
        }

        public FeatureType getFeatureType() {
            return featureType;
        }

        public Feature next() throws IOException {
            try {
                if (originalFeature != null) {
                    writeFeature(originalFeature); // keep the original
                }

                if (innerReader.hasNext()) {
                    originalFeature = innerReader.next(); // ;
                    editFeature = featureType.duplicate(originalFeature);
                } else {
                    originalFeature = null;
                    editFeature = featureType.create(featureDefaults);
                }

                return editFeature;
            } catch (Exception e) {
                throw new IOException(e.toString());
            }
        }

        public void remove() throws IOException {
            if (editFeature == null) {
                throw new IOException("Current feature is null");
            }

            editFeature = null;
            originalFeature = null;
        }

        public void write() throws IOException {
            if (editFeature == null) {
                throw new IOException("Current feature is null");
            }

            try {
                writeFeature(editFeature);
            } catch (Exception e) {
                editFeature = null;
                throw new IOException("Can't write feature: " + e.toString());
            }

            editFeature = null;
            originalFeature = null;
        }

        public boolean hasNext() throws IOException {
            return innerReader.hasNext();
        }

        public void close() throws IOException {
            while (hasNext())
                next();

            try {
                if (originalFeature != null) {
                    writeFeature(originalFeature); // keep the original
                }
            } catch (Exception e) {
            }

            innerReader.close();
            innerReader = null;

            try {
                if (outMif != null) {
                    outMif.close();
                }

                if (outMid != null) {
                    outMid.close();
                }

                copyFileAndDelete(mifFileOut, mifFile, true);
                copyFileAndDelete(midFileOut, midFile, true);
            } catch (IOException e) {
            } finally {
                outMid = null;
                outMif = null;
            }
        }

        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        /**
         * Writes the given Feature to file
         *
         * @param f The feature to write
         *
         * @throws IOException if cannot access file for reading
         * @throws SchemaException if given Feature is not compatible with
         *         MIFFile FeatureType. TODO: private
         */
        public void writeFeature(Feature f) throws IOException, SchemaException {
            if ((outMif == null) || (outMid == null)) {
                throw new IOException(
                    "Output stream has not been opened for writing.");
            }

            Geometry theGeom = (geomFieldIndex >= 0)
                ? (Geometry) f.getAttribute(geomFieldIndex) : null;
            String outGeom = exportGeometry(theGeom);

            if (outGeom.equals("")) {
                throw new SchemaException("Unsupported geometry type: "
                    + theGeom.getClass().getName());
            }

            outMif.println(outGeom);

            int col;
            String outBuf = "";

            try {
                for (col = 1; col < numAttribs; col++) {
                    fieldValueSetters[col].setValue(f.getAttribute(col));

                    if (col > 1) {
                        outBuf += chDelimiter;
                    }

                    outBuf += fieldValueSetters[col].getString();
                }
            } catch (Exception e) {
                throw new IOException("Error writing MID file: "
                    + e.getMessage());
            }

            outMid.println(outBuf);
        }

        private String exportGeometry(Geometry geom) {
            // Style information is optional, so we will not export the default styles
            if ((geom == null) || (geom == nullGeometry) || (geom.isEmpty())) {
                return TYPE_NONE.toUpperCase();
            }

            if (geom instanceof Point) {
                return TYPE_POINT + " "
                + exportCoord(((Point) geom).getCoordinate());
            }

            if (geom instanceof LineString) {
                Coordinate[] coords = geom.getCoordinates();

                return TYPE_PLINE + " " + exportCoords(coords, false);
            }

            // TODO Handle MultiPolygon
            if (geom instanceof Polygon) {
                Polygon poly = (Polygon) geom;
                int nRings = poly.getNumInteriorRing();
                String buf = TYPE_REGION + " " + (1 + nRings) + "\n";
                buf += exportCoords(poly.getExteriorRing().getCoordinates(),
                    true);

                for (int i = 0; i < nRings; i++) {
                    buf += ("\n"
                    + exportCoords(poly.getInteriorRingN(i).getCoordinates(),
                        true));
                }

                return buf;
            }

            if (geom instanceof MultiLineString) {
                MultiLineString multi = (MultiLineString) geom;
                String buf = TYPE_PLINE + " Multiple "
                    + multi.getNumGeometries();

                for (int i = 0; i < multi.getNumGeometries(); i++) {
                    buf += ("\n"
                    + exportCoords(((LineString) multi.getGeometryN(i))
                        .getCoordinates(), false));
                }

                return buf;
            }

            return "";
        }

        /**
         * Renders a single coordinate
         *
         * @param coord DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        private String exportCoord(Coordinate coord) {
            return coord.x + " " + coord.y;
        }

        /**
         * Renders a coordinate list, prefixing it with the number of points
         * SkipLast is used for Polygons (in Mapinfo the last vertex of a
         * polygon is not the clone of first one)
         *
         * @param coords DOCUMENT ME!
         * @param skipLast DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        private String exportCoords(Coordinate[] coords, boolean skipLast) {
            int len = (skipLast) ? (coords.length - 1) : coords.length;

            String buf = String.valueOf(len);

            for (int i = 0; i < len; i++) {
                buf += ("\n" + exportCoord(coords[i]));
            }

            return buf;
        }
    }
}
