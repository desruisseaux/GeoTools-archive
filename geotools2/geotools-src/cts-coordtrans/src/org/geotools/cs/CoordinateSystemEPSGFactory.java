/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// Database connection
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

// Preferences and reflection
import java.util.prefs.Preferences;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

// Logging
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Collections and iterators
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.util.CaselessStringKey;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.Geotools;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.pt.AngleFormat; // For Javadoc


/**
 * Default implementation for a coordinate system factory backed
 * by the EPSG database. The EPSG database is freely available at
 * <A HREF="http://www.epsg.org">http://www.epsg.org</a>. Current
 * version of this class requires EPSG database version 6.
 * <br>
 * <h2>EPSG database installation</h2>
 * The EPSG database is available in MS Access format only (not our fault! We take it as EPSG
 * publish it). The <code>EPSG_v6.mdb</code> file can be stored anywhere on your system under
 * any name, at your convenience. The database must be declared as an ODBC data source. Steps
 * to follow:
 *
 * <ul>
 *   <li>Open the <cite>ODBC data sources</cite> dialog box from the Windows's Control Panel.</li>
 *   <li>Click the "Add..." button an select <cite>Microsoft Access Driver (*.mdb)</cite>.</li>
 *   <li>Data source name should be "EPSG". Filename can be anything; click on the "Select..."
 *       button to select it.</li>
 * </ul>
 * <br>
 * <h2>Note about multi-radix units</h2>
 * The EPSG database express many angles in some multi-radix units. For example, a lot of EPSG's
 * angles are coded in the following format: <cite>sign - degrees - decimal point - minutes (two
 * digits) - integer seconds (two digits) - fraction of seconds (any precision)</cite>. According
 * this convention, the angle <code>40°30'N</code> would be coded as <code>40.30</code>
 * (sexagesimal degree) instead of <code>40.5</code> (fractional degree). Unfortunatly,
 * sexagesimal degrees have the following inconvenients:
 *
 * <ul>
 *   <li>They are not suitable for computation purpose. For example, we can't compute the
 *       difference between two angles using an ordinary substraction.</li>
 *   <li>They make coordinate transformations harder. For example, we can't scale them with
 *       an affine transform.</li>
 *   <li>Their unit can't be formatted correctly in a Well Know Text (WKT).</li>
 *   <li>What sexagesimal unit try to do is really the job of {@link AngleFormat}.</li>
 * </ul>
 *
 * Consequently, the default implementation of <code>CoordinateSystemEPSGFactory</code> will
 * <strong>not</strong> use sexagesimal degrees for coordinate systems. All axis will use
 * fractional degrees instead, which are way more convenient for computation purpose (radians
 * would be as good). If sexagesimal degrees are really wanted, subclasses should overrides
 * the {@link #replaceAxisUnit} method.
 *
 * @version $Id: CoordinateSystemEPSGFactory.java,v 1.22 2004/02/09 09:03:21 desruisseaux Exp $
 * @author Yann Cézard
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class CoordinateSystemEPSGFactory extends CoordinateSystemAuthorityFactory {
    /**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    private static CoordinateSystemEPSGFactory DEFAULT;

    /** Preference node for the JDBC driver class name, and its default value. */
    private static final String DRIVER = "JDBC driver",
                        DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

    /** Preference node for the EPSG database connection string, and its default value. */
    private static final String CONNECTION = "EPSG connection",
                        DEFAULT_CONNECTION = "jdbc:odbc:EPSG";

    /** Preference node for the EPSG factory implementation classname. */
    private static final String IMPLEMENTATION = "EPSG Factory";

    /**
     * List of tables and columns to test for codes values.
     * Elements at even index are table name.
     * Elements at odd index are column name.
     *
     * This table is used by the {@link #createObject} method in order to detect
     * which of the following methods should be invoked for a given code:
     *
     * {@link #createCoordinateSystem}
     * {@link #createDatum}
     * {@link #createEllipsoid}
     * {@link #createUnit}
     *
     * The order is significant: it is the key for a <code>switch</code> statement.
     *
     * @see #createObject
     * @see #lastObjectType
     */
    private static final String[] OBJECT_TABLES = {
        "[Coordinate Reference System]", "COORD_REF_SYS_CODE", // [0]: createCoordinateSystem
        "[Datum]",                       "DATUM_CODE",         // [1]: createDatum
        "[Ellipsoid]",                   "ELLIPSOID_CODE",     // [2]: createEllipsoid
        "[Unit of Measure]",             "UOM_CODE"            // [3]: createUnit
    };

    /**
     * Default mapping for {@link #namesMap}.
     *
     * @task REVISIT: How to know if "Geographic/geocentric conversions"
     *                is geographic to geocentric or geocentric to geographic?
     */
    private static String[] DEFAULT_PARAM_MAP = {
        // Parameters names
        "Latitude of natural origin",         "latitude_of_origin",
        "Latitude of false origin",           "latitude_of_origin",
        "Longitude of natural origin",        "central_meridian",
        "Longitude of false origin",          "central_meridian",
        "Scale factor at natural origin",     "scale_factor",
        "False easting",                      "false_easting",
        "False northing",                     "false_northing",
        "Easting at false origin",            "false_easting",
        "Northing at false origin",           "false_northing",
        "Latitude of 1st standard parallel",  "standard_parallel_1",
        "Latitude of 2nd standard parallel",  "standard_parallel_2",

        // Projection names
        "Lambert Conic Conformal (2SP)",      "Lambert_Conformal_Conic_2SP",
        "Albers Equal Area",                  "Albers_Conic_Equal_Area",
//      "Geographic/geocentric conversions",  "Geocentric_To_Ellipsoid"
//      TODO: How to know if it is "Geocentric_To_Ellipsoid" or "Ellipsoid_To_Geocentric"?
    };
    
    /**
     * Maps EPSG parameter names to OGC parameter names.
     * For example, "False easting" (the EPSG name) is mapped to "false_easting" (the OGC name).
     * For now, this map is shared among all instances of {@link CoordinateSystemEPSGFactory}.
     * A future version may keep on map for each factory instance, which would allow some
     * customization.
     */
    private static final Map namesMap = new HashMap();
    static {
        for (int i=0; i<DEFAULT_PARAM_MAP.length;) {
            namesMap.put(new CaselessStringKey(DEFAULT_PARAM_MAP[i++]), DEFAULT_PARAM_MAP[i++]);
        }
    }

    /**
     * A pool of prepared statements. Key are {@link String} object related to their
     * originating method name (for example "Ellipsoid" for {@link #createEllipsoid},
     * while values are {@link PreparedStatement} objects.
     */
    private final Map statements = new IdentityHashMap();

    /**
     * Last object type returned by {@link #createObject}, or -2 if none.
     * This type is an index in the {@link #OBJECT_TABLES} array and is
     * strictly for {@link #createObject} internal use.
     */
    private int lastObjectType = -2;

    /**
     * The connection to the EPSG database.
     */
    protected final Connection connection;

    /**
     * Construct an authority factory using
     * the specified connection.
     *
     * @param factory    The underlying factory used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     */
    public CoordinateSystemEPSGFactory(final CoordinateSystemFactory factory,
                                       final Connection           connection)
    {
        super(factory);
        this.connection = connection;
        Info.ensureNonNull("connection", connection);
    }
    
    /**
     * Construct an authority factory using
     * the specified URL to an EPSG database.
     *
     * @param  factory The underlying factory used for objects creation.
     * @param  url     The url to the EPSG database. For example, a connection
     *                 using the ODBC-JDBC bridge may have an URL likes
     *                 <code>"jdbc:odbc:EPSG"</code>.
     * @param  driver  An optional driver to load, or <code>null</code> if none.
     *                 This is a convenience argument for the following pseudo-code:
     *                 <blockquote><code>
     *                 Class.forName(driver).newInstance();
     *                 </code></blockquote>
     *                 A message is logged to <code>"org.geotools.cts"</code> wether
     *                 the loading sucseeds of fails. For JDBC-ODBC bridge, a typical value
     *                 for this argument is <code>"sun.jdbc.odbc.JdbcOdbcDriver"</code>.
     *                 This argument needs to be non-null only once for a specific driver.
     *
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public CoordinateSystemEPSGFactory(final CoordinateSystemFactory factory,
                                       final String url, final String driver) throws SQLException
    {
        this(factory, getConnection(url, driver));
    }

    /**
     * Returns a new instance of <code>CoordinateSystemEPSGFactory</code> using reflection API.
     * This method allow the use of an alternative implementation. The implementation must be
     * a subclass of <code>CoordinateSystemEPSGFactory</code> and must have a constructor with
     * the following arguments:
     * <ul>
     *   <li>The coordinate system factory as a {@link CoordinateSystemFactory}</li>
     *   <li>The EPSG database URL as a {@link String}</li>
     *   <li>The database driver as a {@link String}</li>
     * </ul>
     *
     * @param  factory        The underlying factory used for objects creation.
     * @param  implementation Fully qualified class name of the implementation to use.
     * @param  url            The url to the EPSG database.
     * @param  driver         An optional driver to load, or <code>null</code> if none.
     * @return The <code>CoordinateSystemEPSGFactory</code> instance.
     */
    private static CoordinateSystemEPSGFactory newInstance(final CoordinateSystemFactory factory,
                                                           final String implementation,
                                                           final String url,
                                                           final String driver) throws SQLException
    {
        final Class[] ARG_TYPES = new Class[] {
            CoordinateSystemFactory.class, String.class, String.class
        };
        try {
            return (CoordinateSystemEPSGFactory) Class.forName(implementation)
                    .getConstructor(ARG_TYPES).newInstance(new Object[] {factory, url, driver});
        } catch (InvocationTargetException exception) {
            /*
             * The class and the constructor have been found, but the
             * the creation failed because of an exception in user code.
             * Rethrow the user exception (not the reflection API exception).
             */
            Throwable cause = exception.getTargetException();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new UndeclaredThrowableException(cause);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception cause) {
            /*
             * A reflection API call failed.  If the cause was an unchecked exception
             * (for example a SecurityException), throws as-is. Otherwise, wrap it in
             * an unchecked IllegalStateException with the reflection exception as
             * its cause. This situation occurs when system properties contain the
             * classname of an invalid implementation,  which was itself specified
             * by the user usually from the command line with the "-implementation"
             * argument.
             */
            final IllegalStateException exception = new IllegalStateException(
                  Resources.format(ResourceKeys.ERROR_BAD_ARGUMENT_$2,
                                   "-implementation", implementation));
            exception.initCause(cause);
            throw exception;
        }
    }

    /**
     * Returns a default coordinate system factory backed by the EPSG database.
     * Invoking {@link #dispose} on this special factory will not close the database
     * connection, since it may be shared by many users. However, it is safe to invoke
     * {@link #dispose} anyway in order to release some resources used by this instance.
     * <br><br>
     * By default, this method loads the <code>"sun.jdbc.odbc.JdbcOdbcDriver"</code> and
     * ask for a connection to the <code>"jdbc:odbc:EPSG"</code> database. This default
     * behavior can be changed by invoking the {@link #main} method from the command line.
     * For example:
     * <blockquote><pre>
     * java org.geotools.cs.CoordinateSystemEPSGFactory -driver=[my driver] -connection=[my url]
     * </pre></blockquote>
     *
     * @return The default factory.
     * @throws SQLException if the connection to the database can't be etablished.
     */
    public synchronized static CoordinateSystemAuthorityFactory getDefault() throws SQLException {
        if (DEFAULT == null) {
            final Preferences prefs = Preferences.systemNodeForPackage(CoordinateSystemAuthorityFactory.class);
            final String     driver = prefs.get(DRIVER,         DEFAULT_DRIVER);
            final String    connect = prefs.get(CONNECTION,     DEFAULT_CONNECTION);
            final String  implement = prefs.get(IMPLEMENTATION, null);
            final CoordinateSystemFactory factory = CoordinateSystemFactory.getDefault();
            if (implement == null) {
                DEFAULT = new CoordinateSystemEPSGFactory(factory, connect, driver);
            } else {
                DEFAULT = newInstance(factory, implement, connect, driver);
            }
            Runtime.getRuntime().addShutdownHook(new Thread("EPSG factory shutdown") {
                public void run() {
                    try {
                        DEFAULT.close(true);
                    } catch (SQLException exception) {
                        // Too late to log the error, since we are exiting the JVM. Ignore...
                    }
                }
            });
        }
        return DEFAULT;
    }

    /**
     * Invoked when a new {@link PreparedStatement} is about to be created from a SQL string.
     * Since the <A HREF="http://www.epsg.org">EPSG database</A> is available only in MS-Access
     * format, SQL statements are formatted using some syntax specific to this particular database
     * software (for example "<code>SELECT * FROM [Coordinate Reference System]</code>").
     * If a port of EPSG database is to be used with an other software, then this method should be
     * overriden in order to adapt the SQL syntax. For example a subclass connecting to a
     * <cite>PostgreSQL</cite> database could replace all spaces ("&nbsp;") between
     * watching braces ("[" and "]") by underscore ("_").
     *
     * @param  statement The statement in MS-Access syntax.
     * @return The SQL statement to use. The default implementation returns the string unchanged.
     */
    protected String adaptSQL(final String statement) {
        return statement;
    }

    /**
     * Returns the authority name, which is <code>"EPSG"</code>.
     */
    public String getAuthority() {
        return "EPSG";
    }

    /**
     * Get the connection to an URL.
     *
     * @param  url     The url to the EPSG database.
     * @param  driver  The driver to load, or <code>null</code> if none.
     * @return The connection to the EPSG database.
     * @throws SQLException if the connection can't be etablished.
     */
    private static Connection getConnection(final String url, String driver) throws SQLException {
        Info.ensureNonNull("url", url);
        if (driver != null) {
            LogRecord record;
            try {
                final Driver drv = (Driver)Class.forName(driver).newInstance();
                record = Resources.getResources(null).getLogRecord(Level.CONFIG,
                                            ResourceKeys.LOADED_JDBC_DRIVER_$3);
                record.setParameters(new Object[] {
                    drv.getClass().getName(),
                    new Integer(drv.getMajorVersion()),
                    new Integer(drv.getMinorVersion())
                });
            } catch (Exception exception) {
                record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
                record.setThrown(exception);
                // Try to connect anyway. It is possible that
                // an other driver has already been loaded...
            }
            record.setSourceClassName("CoordinateSystemEPSGFactory");
            record.setSourceMethodName("<init>");
            Logger.getLogger("org.geotools.cs").log(record);
        }
        return DriverManager.getConnection(url);
    }

    /**
     * Returns a prepared statement for the specified name.
     *
     * @param  key A key uniquely identifying the caller
     *         (e.g. "Ellipsoid" for {@link #createEllipsoid}).
     * @param  sql The SQL statement to use if for creating the {@link PreparedStatement}
     *         object. Will be used only if no prepared statement was already created for
     *         the specified key.
     * @return The prepared statement.
     * @throws SQLException if the prepared statement can't be created.
     */
    private PreparedStatement prepareStatement(final String key, final String sql)
            throws SQLException
    {
        assert Thread.holdsLock(this);
        PreparedStatement stmt = (PreparedStatement) statements.get(key);
        if (stmt == null) {
            stmt = connection.prepareStatement(adaptSQL(sql));
            statements.put(key, stmt);
        }
        return stmt;
    }

    /**
     * Gets the string from the specified {@link ResultSet}.
     * The string is required to be non-null. A null string
     * will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The string at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static String getString(final ResultSet result, final int columnIndex, final String line)
            throws SQLException, FactoryException
    {
        final String str = result.getString(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        line, column));
        }
        return str.trim();
    }

    /**
     * Gets the value from the specified {@link ResultSet}.
     * The value is required to be non-null. A null value
     * (i.e. blank) will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The double at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static double getDouble(final ResultSet result, final int columnIndex, final String line)
            throws SQLException, FactoryException
    {
        final double value = result.getDouble(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        line, column));
        }
        return value;
    }

    /**
     * Gets the value from the specified {@link ResultSet}.
     * The value is required to be non-null. A null value
     * (i.e. blank) will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The integer at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static int getInt(final ResultSet result, final int columnIndex, final String line)
            throws SQLException, FactoryException
    {
        final int value = result.getInt(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        line, column));
        }
        return value;
    }

    /**
     * Make sure that an object constructed from the database
     * is not duplicated.
     *
     * @param  newValue The newly constructed object.
     * @param  oldValue The object previously constructed,
     *         or <code>null</code> if none.
     * @param  The EPSG code (for formatting error message).
     * @throws FactoryException if a duplication has been detected.
     */
    private static Object ensureSingleton(final Object newValue, final Object oldValue, String code)
            throws FactoryException
    {
        if (oldValue == null) {
            return newValue;
        }
        if (oldValue.equals(newValue)) {
            return oldValue;
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_DUPLICATED_VALUES_$1, code));
    }

    /**
     * Transform a string from the OGC format to the EPSG format.
     * Parenthesis are removed; spaces are replaced by underscore;
     * first letter is upper case and others are lower case.
     *
     * @param name the string at the EPSG format.
     * @return the String at OGC format.
     */
    private static String fromEPSGtoOGC(final String name) {
        final String ogcName = (String) namesMap.get(new CaselessStringKey(name));
        if (ogcName != null) {
            return ogcName;
        }
        StringBuffer buf   = new StringBuffer();
        StringTokenizer st = new StringTokenizer(name, " ()");
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            // First letter in upper case
            buf.append(Character.toUpperCase(word.charAt(0)));
            buf.append(word.substring(1));
            buf.append("_");
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Returns the name for the {@link Info} object to construct,
     * together with a set of properties. This {@link CharSequence}
     * object is to be given to the {@link Info} constructor.
     *
     * @param  name The name for the {@link Info} object to construct.
     * @param  code The EPSG code of the object to construct.
     * @param  remarks Remarks, or <code>null</code> if none.
     * @return The name together with a set of properties.
     */
    private CharSequence pack(String name, String code, String remarks) {
        name = name.trim();
        code = code.trim();
        final InfoProperties.Named props = new InfoProperties.Named(name);
        props.put("authority",     getAuthority());
        props.put("authorityCode", code);
        if (remarks!=null && (remarks=remarks.trim()).length()!=0) {
            props.put("remarks", remarks);
        }
        return props;
    }

    /**
     * Returns an arbitrary object from a code.
     * Default implementation will invokes one of {@link #createCoordinateSystem},
     * {@link #createDatum}, {@link #createEllipsoid}, or {@link #createUnit}
     * methods according the object type.
     *
     * @param  code The EPSG value.
     * @return The ellipsoid object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Object createObject(final String code) throws FactoryException {
        final String       KEY = "Object";
        PreparedStatement stmt = (PreparedStatement) statements.get(KEY); // Null allowed.
        StringBuffer     query = null; // Will be created only if the last statement doesn't suit.
        /*
         * Iterates through all tables listed in OBJECT_TABLES, starting with the table used during
         * the last call to 'createObject(code)'.  This approach assumes that two consecutive calls
         * will often return the same type of object.  If the object type changed, then this method
         * will have to discard the old prepared statement and prepare a new one, which may be a
         * costly operation. Only the last successful prepared statement is cached, in order to keep
         * the amount of statements low. Unsuccessful statements are immediately disposed.
         */
        for (int i=-2; i<OBJECT_TABLES.length; i+=2) {
            if (i == lastObjectType) {
                // Avoid to test the same table twice.  Note that this test also avoid a
                // NullPointerException if 'stmt' is null, since 'lastObjectType' should
                // be -2 in this case.
                continue;
            }
            try {
                if (i >= 0) {
                    final String table  = OBJECT_TABLES[i  ];
                    final String column = OBJECT_TABLES[i+1];
                    if (query == null) {
                        query = new StringBuffer("select ");
                    }
                    query.setLength(7); // 7 is the length of "select " in the line above.
                    query.append(column);
                    query.append(" from ");
                    query.append(table);
                    query.append(" where ");
                    query.append(column);
                    query.append(" = ?");
                    assert !statements.containsKey(KEY);
                    stmt = prepareStatement(KEY, query.toString());
                }
                stmt.setString(1, code);
                final ResultSet result = stmt.executeQuery();
                final boolean  present = result.next();
                result.close();
                if (present) {
                    if (i >= 0) {
                        lastObjectType = i;
                    }
                    switch (lastObjectType) {
                        case 0:  return createCoordinateSystem(code);
                        case 2:  return createDatum           (code);
                        case 4:  return createEllipsoid       (code);
                        case 6:  return createUnit            (code);
                        default: throw new AssertionError     (i); // Should not happen
                    }
                }
                statements.remove(KEY);
                stmt.close();
            } catch (SQLException exception) {
                throw new FactoryException(code, exception);
            }
        }
        throw new NoSuchAuthorityCodeException(code);
    }

    /**
     * Returns an {@link Ellipsoid} object from a code.
     *
     * @param  code The EPSG value.
     * @return The ellipsoid object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Ellipsoid createEllipsoid(final String code)
            throws FactoryException
    {
        Ellipsoid returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Ellipsoid", "select ELLIPSOID_NAME,"
                                                 + " SEMI_MAJOR_AXIS,"
                                                 + " INV_FLATTENING,"
                                                 + " SEMI_MINOR_AXIS,"
                                                 + " UOM_CODE,"
                                                 + " REMARKS"
                                                 + " from [Ellipsoid]"
                                                 + " where ELLIPSOID_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                /*
                 * One of 'semiMinorAxis' and 'inverseFlattening' values can be NULL in
                 * the database. Consequently, we don't use 'getString(ResultSet, int)'
                 * because we don't want to thrown an exception if a NULL value is found.
                 */
                final String name              = getString(result, 1, code);
                final double semiMajorAxis     = getDouble(result, 2, code);
                final double inverseFlattening = result.getDouble( 3);
                final double semiMinorAxis     = result.getDouble( 4);
                final String unitCode          = getString(result, 5, code);
                final String remarks           = result.getString( 6);
                final Unit   unit              = createUnit(unitCode);
                final CharSequence prp         = pack(name, code, remarks);
                final Ellipsoid ellipsoid;
                if (inverseFlattening == 0) {
                    if (semiMinorAxis == 0) {
                        // Both are null, which is not allowed.
                        final String column = result.getMetaData().getColumnName(3);
                        result.close();
                        throw new FactoryException(Resources.format(
                                                   ResourceKeys.ERROR_NULL_VALUE_$2, code, column));
                    } else {
                        // We only have semiMinorAxis defined -> it's OK
                        ellipsoid = factory.createEllipsoid(prp, semiMajorAxis, semiMinorAxis, unit);
                    }
                } else {
                    if (semiMinorAxis != 0) {
                        // Both 'inverseFlattening' and 'semiMinorAxis' are defined.
                        // Log a warning and create the ellipsoid using the inverse flattening.
                        Logger.getLogger("org.geotools.cs").warning(Resources.format(
                                            ResourceKeys.WARNING_AMBIGUOUS_ELLIPSOID));
                    }
                    ellipsoid = factory.createFlattenedSphere(prp, semiMajorAxis, inverseFlattening, unit);
                }
                /*
                 * Now that we have built an ellipsoid, compare
                 * it with the previous one (if any).
                 */
                returnValue = (Ellipsoid) ensureSingleton(ellipsoid, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a {@link Unit} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The unit object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Unit createUnit(final String code)
            throws FactoryException
    {
        Unit returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Unit", "select UNIT_OF_MEAS_TYPE,"
                                            + " FACTOR_B,"
                                            + " FACTOR_C"
                                            + " from [Unit of Measure]"
                                            + " where UOM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final String type = getString(result, 1, code);
                final double b    = result.getDouble( 2);
                final double c    = result.getDouble( 3);

                Unit baseUnit;
                Unit scaledUnit = null;
                if (type.equalsIgnoreCase("length")) {
                    // In the UOM table, all length are based on the metre.
                    baseUnit = Unit.METRE;
                } else if (type.equalsIgnoreCase("angle")) {
                    // In the UOM table, all angles are based on the radian.
                    baseUnit = Unit.RADIAN;
                    if (b==0 && c==0) {
                        switch (Integer.parseInt(code)) {
                            // Special cases: very unconvenient units...
                            case 9107: scaledUnit = Unit.DMS; break;
                            case 9108: scaledUnit = Unit.DMS; break; // HACK: Missing hemisphere!!
                            case 9110: scaledUnit = Unit.SEXAGESIMAL_DEGREE; break;
                            case 9111: throw new FactoryException("\"degree and decimal minute\" unit not yet supported");
                        }
                    }
                } else if (type.equalsIgnoreCase("scale")) {
                    baseUnit = Unit.DIMENSIONLESS;
                    if (b==0 && c==0) {
                        switch (Integer.parseInt(code)) {
                            // Special cases: Coefficient.
                            case 9203: scaledUnit = baseUnit; break;
                        }
                    }
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                /*
                 * Now that we have built an unit, scale it and
                 * compare it with the previous one (if any).
                 */
                if (scaledUnit == null) {
                    if (b==0 || c==0) {
                        final String column = result.getMetaData().getColumnName(b==0 ? 2 : 3);
                        result.close();
                        throw new FactoryException(Resources.format(
                                                   ResourceKeys.ERROR_NULL_VALUE_$2, code, column));
                    }
                    scaledUnit = baseUnit.scale(b/c);
                }
                returnValue = (Unit) ensureSingleton(scaledUnit, returnValue, code);
            }
            result.close();
        }
        catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a prime meridian, relative to Greenwich.
     *
     * @param  code Value allocated by authority.
     * @return The prime meridian object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized PrimeMeridian createPrimeMeridian(final String code)
            throws FactoryException
    {
        PrimeMeridian returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("PrimeMeridian", "select PRIME_MERIDIAN_NAME,"
                                                     + " GREENWICH_LONGITUDE,"
                                                     + " UOM_CODE,"
                                                     + " REMARKS"
                                                     + " from [Prime Meridian]"
                                                     + " where PRIME_MERIDIAN_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */ 
            while (result.next()) {
                final String name      = getString(result, 1, code);
                final double longitude = getDouble(result, 2, code);
                final String unit_code = getString(result, 3, code);
                final String remarks   = result.getString( 4);
                final Unit unit        = createUnit(unit_code);
                final CharSequence prp = pack(name, code, remarks);
                PrimeMeridian primeMeridian = factory.createPrimeMeridian(prp, unit, longitude);
                returnValue = (PrimeMeridian) ensureSingleton(primeMeridian, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a datum from a code. This method may
     * returns a vertical, horizontal or local datum.
     *
     * @param  code Value allocated by authority.
     * @return The datum object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     *
     * @task REVISIT: Current implementation maps all "vertical" datum to
     *                {@link DatumType#ELLIPSOIDAL} and all "horizontal"
     *                datum to {@link DatumType#GEOCENTRIC}. At the time
     *                of writting, it was not clear how to maps the exact
     *                datum type from the EPSG database.
     *
     * @task REVISIT: The creation of horizontal datum use only the first
     *                {@link WGS84ConversionInfo} object, because current
     *                version of {@link CoordinateSystemFactory} do not
     *                allows more than one conversion info. We should fix
     *                that.
     *
     * @task TODO:    Datum "engineering" is currently not supported.
     */
    public Datum createDatum(final String code) throws FactoryException {
        Datum returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Datum", "select DATUM_NAME,"
                                             + " DATUM_TYPE,"
                                             + " REMARKS,"
                                             + " ELLIPSOID_CODE"  // Only for horizontal type
                                             + " from [Datum]"
                                             + " where DATUM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final String name    = getString(result, 1, code);
                final String type    = getString(result, 2, code);
                final String remarks = result.getString( 3);
                final CharSequence prp = pack(name, code, remarks);
                final Datum datum;
                if (type.equalsIgnoreCase("vertical")) {
                    /*
                     * Vertical datum type. Maps to "ELLIPSOIDAL".
                     */
                    final DatumType.Vertical dtype = DatumType.Vertical.ELLIPSOIDAL; // TODO
                    datum = factory.createVerticalDatum(prp, dtype);
                } else if (type.equalsIgnoreCase("geodetic")) {
                    /*
                     * Horizontal datum type. Maps to "GEOCENTRIC".
                     */
                    final Ellipsoid         ellipsoid = createEllipsoid(getString(result, 4, code));
                    final WGS84ConversionInfo[] infos = createWGS84ConversionInfo(code);
                    final WGS84ConversionInfo mainInf = (infos.length!=0) ? infos[0] : null;
                    final DatumType.Horizontal  dtype = DatumType.Horizontal.GEOCENTRIC; // TODO
                    // TODO: on utilise la premiere info seulement pour le moment.
                    datum = factory.createHorizontalDatum(prp, dtype, ellipsoid, mainInf);
                } else if (type.equalsIgnoreCase("engineering")) {
                    /*
                     * Local datum type.
                     */
                    // TODO
                    //return factory.createLocalDatum(prp, new DatumType.Local("bidon",0,0));
                    result.close();
                    throw new UnsupportedOperationException("DatumType.Local not supported.");
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                returnValue = (Datum) ensureSingleton(datum, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /** 
     * Returns the differents WGS84 Conversion Informations
     * for a {@link HorizontalDatum}. If the specified datum
     * has no WGS84 conversion informations, then this method
     * will returns an empty array.
     *  
     * @param  code the EPSG code of the {@link HorizontalDatum}.
     * @return an array of {@link WGS84ConversionInfo}, which may
     *         be empty.
     */
    private WGS84ConversionInfo[] createWGS84ConversionInfo(final String code)
            throws FactoryException
    {
        final List list = new ArrayList();
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("WGS84ConversionInfo", "select CO.COORD_OP_CODE,"
                                               + " A.AREA_OF_USE,"
                                               + " CO.COORD_OP_METHOD_CODE"
                                               + " from [Coordinate_Operation] as CO,"
                                               + " [Coordinate Reference System] as CRS,"
                                               + " [Area] as A"
                                               + " where CRS.DATUM_CODE = ?"
                                               + " and CO.SOURCE_CRS_CODE = CRS.COORD_REF_SYS_CODE"
                                               + " and CO.TARGET_CRS_CODE = 4326"
                                               + " and A.AREA_CODE = CO.AREA_OF_USE_CODE"
                                               + " order by CO.COORD_OP_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final WGS84ConversionInfo info = new WGS84ConversionInfo();
                final Parameter[] param = createParameter(getString(result, 1, code));
                if ((param != null) && (param.length != 0)) {
                    final String areaOfUse    = result.getString(2); // Accept null.
                    final String methodOpCode = getString(result, 3, code);
                    // Value could be something else, but I don't know what to do when
                    // it is the case (for example 9618, with a radian Unit).
                    // So limiting to 9603 and 9606 cases for the moment.
                    if (methodOpCode.equals("9603") || methodOpCode.equals("9606")) {
                        // First we get the description of the area of use
                        info.areaOfUse = areaOfUse;

                        // Then we get the coordinates. For each one we convert the unit in meter
                        info.dx = Unit.METRE.convert(param[0].value, param[0].unit);
                        info.dy = Unit.METRE.convert(param[1].value, param[1].unit);
                        info.dz = Unit.METRE.convert(param[2].value, param[2].unit);

                        if (methodOpCode.equals("9606")) {
                            // Here we know that the database provides four more informations
                            // for WGS84 conversion : ex, ey, ez and ppm
                            info.ex  = Unit.ARC_SECOND.convert(param[3].value, param[3].unit);
                            info.ey  = Unit.ARC_SECOND.convert(param[4].value, param[4].unit);
                            info.ez  = Unit.ARC_SECOND.convert(param[5].value, param[5].unit);
                            info.ppm = param[6].value; // Parts per million, no conversion needed
                        }
                        list.add(info);
                    }
                }
            }            
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        return (WGS84ConversionInfo[]) list.toArray(new WGS84ConversionInfo[list.size()]);
    }
    
    /**
     * Returns a coordinate system from a code.
     *
     * @param  code Value allocated by authority.
     * @return The coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized CoordinateSystem createCoordinateSystem(final String code)
            throws FactoryException
    {
        String type = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("CoordinateSystem", "select COORD_REF_SYS_KIND"
                                                        + " from [Coordinate Reference System]"
                                                        + " where COORD_REF_SYS_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String candidate = getString(result, 1, code);
                type = (String) ensureSingleton(candidate, type, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (type == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        if (type.equalsIgnoreCase("compound")) {
            return createCompoundCoordinateSystem(code);
        }
        if (type.equalsIgnoreCase("vertical")) {
            return createVerticalCoordinateSystem(code);
        }
        if (type.equalsIgnoreCase("geographic 2D")) {
            return createGeographicCoordinateSystem(code);
        }
        if (type.equalsIgnoreCase("projected")) {
            return createProjectedCoordinateSystem(code);
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
    }
    
    /**
     * Returns a geographic coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The geographic coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     *
     */
    public synchronized GeographicCoordinateSystem createGeographicCoordinateSystem(final String code)
            throws FactoryException
    {
        GeographicCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("GeographicCoordinateSystem", "select DIMENSION,"
                                               + " CS.COORD_SYS_CODE,"
                                               + " COORD_REF_SYS_NAME,"
                                               + " PRIME_MERIDIAN_CODE,"
                                               + " D.DATUM_CODE,"
                                               + " CRS.REMARKS"
                                               + " from [Coordinate Reference System] as CRS,"
                                               + " [Coordinate System] as CS,"
                                               + " [Datum] as D"
                                               + " where COORD_REF_SYS_CODE = ?"
                                               + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE"
                                               + " and D.DATUM_CODE = CRS.DATUM_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int        dimension = getInt   (result, 1, code);
                final String  coordSysCode = getString(result, 2, code);
                final String          name = getString(result, 3, code);
                final String primeMeridian = getString(result, 4, code);
                final String         datum = getString(result, 5, code);
                final String       remarks = result.getString( 6);
                final AxisInfo[] axisInfos = createAxisInfos(coordSysCode, dimension);
                final Unit            unit = createUnitCS(coordSysCode);
                final CharSequence     prp = pack(name, code, remarks);
                final CoordinateSystem coordSys;
                coordSys = factory.createGeographicCoordinateSystem(prp, unit,
                                            createHorizontalDatum(datum),
                                            createPrimeMeridian(primeMeridian),
                                            axisInfos[0], axisInfos[1]);
                returnValue = (GeographicCoordinateSystem) ensureSingleton(coordSys, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }
    
    /**
     * Returns a projected coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The projected coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized ProjectedCoordinateSystem createProjectedCoordinateSystem(final String code)
            throws FactoryException
    {
        CoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("ProjectedCoordinateSystem", "select DIMENSION,"
                                       + " CS.COORD_SYS_CODE,"
                                       + " CRS.COORD_REF_SYS_NAME,"
                                       + " CRS.SOURCE_GEOGCRS_CODE,"
                                       + " CO.COORD_OP_NAME,"
                                       + " COM.COORD_OP_METHOD_NAME,"
                                       + " CRS.PROJECTION_CONV_CODE,"
                                       + " CRS.REMARKS"
                                       + " from [Coordinate Reference System] as CRS,"
                                       + " [Coordinate System] as CS,"
                                       + " [Coordinate_Operation] as CO,"
                                       + " [Coordinate_Operation Method] as COM"
                                       + " where CRS.COORD_REF_SYS_CODE = ?"
                                       + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE"
                                       + " and CO.COORD_OP_CODE = CRS.PROJECTION_CONV_CODE"
                                       + " and COM.COORD_OP_METHOD_CODE = CO.COORD_OP_METHOD_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int          dimension =                 getInt   (result, 1, code);
                final String    coordSysCode =                 getString(result, 2, code);
                final String            name =                 getString(result, 3, code);
                final String     geoCoordSys =                 getString(result, 4, code);
                final String   operationName =                 getString(result, 5, code);
                final String  classification = fromEPSGtoOGC  (getString(result, 6, code));
                final Parameter[] parameters = createParameter(getString(result, 7, code));
                final String         remarks = result.getString(8);
                final AxisInfo[]   axisInfos = createAxisInfos(coordSysCode, dimension);
                final CharSequence       prp = pack(name, code, remarks);
                final ParameterList list = factory.createProjectionParameterList(classification);
                for (int i=0; i<parameters.length; i++) {
                    parameters[i].setParameter(list);
                }
                final GeographicCoordinateSystem gcs;
                gcs = createGeographicCoordinateSystem(geoCoordSys);
                final Ellipsoid e = gcs.getHorizontalDatum().getEllipsoid();
                if (e != null) {
                    final Unit unit = e.getAxisUnit();
                    list.setParameter("semi_major", Unit.METRE.convert(e.getSemiMajorAxis(), unit));
                    list.setParameter("semi_minor", Unit.METRE.convert(e.getSemiMinorAxis(), unit));
                }
                final Projection projection = factory.createProjection(operationName,
                                                                       classification, list);
                final Unit unit = createUnitCS(coordSysCode);
                final CoordinateSystem coordSys;
                coordSys = factory.createProjectedCoordinateSystem(prp, gcs, projection, unit,
                                                                   axisInfos[0], axisInfos[1]);
                returnValue = (CoordinateSystem) ensureSingleton(coordSys, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return (ProjectedCoordinateSystem) returnValue;
    }
    
    /**
     * Returns a vertical coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The vertical coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized VerticalCoordinateSystem createVerticalCoordinateSystem(final String code)
            throws FactoryException
    {
        VerticalCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("VerticalCoordinateSystem", "select DIMENSION,"
                                               + " CS.COORD_SYS_CODE,"
                                               + " COORD_REF_SYS_NAME,"
                                               + " DATUM_CODE,"
                                               + " CRS.REMARKS"
                                               + " from [Coordinate Reference System] as CRS,"
                                               + " [Coordinate System] as CS"
                                               + " where COORD_REF_SYS_CODE = ?"
                                               + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int        dimension = getInt   (result, 1, code);
                final String  coordSysCode = getString(result, 2, code);
                final String          name = getString(result, 3, code);
                final String         datum = getString(result, 4, code);
                final String       remarks = result.getString( 5);
                final AxisInfo[] axisInfos = createAxisInfos(coordSysCode, dimension);
                final CharSequence     prp = pack(name, code, remarks);
                final CoordinateSystem  coordSys;
                coordSys = factory.createVerticalCoordinateSystem(prp,
                                        createVerticalDatum(datum),
                                        createUnitCS(coordSysCode), axisInfos[0]);
                returnValue = (VerticalCoordinateSystem)ensureSingleton(coordSys,returnValue,code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }
    
    /**
     * Create a compound coordinate system from the EPSG code.
     *
     * @param code the EPSG code for the CS.
     * @return the compound CS which value was given.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized CompoundCoordinateSystem createCompoundCoordinateSystem(final String code)
            throws FactoryException
    {
        CompoundCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("CompoundCoordinateSystem", "select COORD_REF_SYS_NAME,"
                                               + " COORD_REF_SYS_KIND,"
                                               + " CMPD_HORIZCRS_CODE,"
                                               + " CMPD_VERTCRS_CODE,"
                                               + " REMARKS"
                                               + " from [Coordinate Reference System]"
                                               + " where COORD_REF_SYS_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String name = getString(result, 1, code);
                final String type = getString(result, 2, code);
                if (!type.equalsIgnoreCase("compound")) {
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
                }
                final CoordinateSystem  cs1 = createCoordinateSystem(getString(result, 3, code));
                final CoordinateSystem  cs2 = createCoordinateSystem(getString(result, 4, code));
                final CharSequence      prp = pack(name, code, result.getString(5));
                CompoundCoordinateSystem cs = factory.createCompoundCoordinateSystem(prp, cs1,cs2);
                returnValue = (CompoundCoordinateSystem) ensureSingleton(cs, returnValue, code);
            }
            result.close();
        }
        catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue; 
    }
    
    /**
     * Returns the {@link AxisInfo}s from an
     * EPSG code for a {@link CoordinateSystem}.
     *
     * @param  code the EPSG code.
     * @param  dimension of the coordinate system, which is also the
     *         size of the returned Array.
     * @return an array of AxisInfo.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if the code has not been found.
     *
     * @task HACK: WARNING!! The EPSG database use "ORDER" as a column name.
     *             This is tolerated by Access, but MySQL doesn't accept this name.
     */
    private AxisInfo[] createAxisInfos(final String code, final int dimension)
            throws SQLException, FactoryException
    {
        final AxisInfo[] axis = new AxisInfo[dimension];
        final PreparedStatement stmt;
        stmt = prepareStatement("AxisInfo", "select COORD_AXIS_NAME,"
                                           + " COORD_AXIS_ORIENTATION"
                                           + " from [Coordinate Axis] as CA,"
                                           + " [Coordinate Axis Name] as CAN"
                                           + " where COORD_SYS_CODE = ?"
                                           + " and CA.COORD_AXIS_NAME_CODE = CAN.COORD_AXIS_NAME_CODE"
                                           // WARNING: Be careful about the table name :
                                           //          MySQL refuse ORDER as a column name !!!
                                           + " order by [ORDER]");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        int i = 0;
        while (result.next()) {
            final String name = getString(result, 1, code);
            final AxisOrientation orientation;
            try {
                orientation = AxisOrientation.getEnum(getString(result, 2, code));
            } catch (NoSuchElementException exception) {
                throw new FactoryException(Resources.format(
                                           ResourceKeys.ERROR_UNKNOW_TYPE_$1, name), exception);
            }
            if (i < axis.length) {
                axis[i++] = new AxisInfo(name, orientation);
            }
        }
        result.close();
        if (i != axis.length) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                       new Integer(axis.length), new Integer(i)));
        }
        return axis;
    }
    
    /**
     * Returns the Unit for 1D and 2D coordinate system. This method scan the unit of
     * all axis for the specified coordinate system. All axis must use the same units.
     *
     * @param  code The coordinate system code.
     * @return The unit.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if some other errors has occured.
     */
    private Unit createUnitCS(final String code) throws SQLException, FactoryException
    {
        Unit returnValue = null;
        final PreparedStatement stmt;
        // Note: can't use "Unit" key, because it is already used by "createUnit".
        stmt = prepareStatement("UnitCS", "select UOM_CODE"
                                          + " from [Coordinate Axis]"
                                          + " where COORD_SYS_CODE = ?");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final Unit unit = createUnit(getString(result, 1, code));
            returnValue = (Unit) ensureSingleton(unit, returnValue, code);
        }
        result.close();
        if (returnValue == null) {
            throw new NoSuchAuthorityCodeException(code);
        }
        return replaceAxisUnit(returnValue);
    }

    /**
     * Replace the axis's unit for a coordinate system. This method is invoked by all
     * <code>create...CoordinateSystem</code> methods,  but not by other methods like
     * <code>createPrimeMeridian</code>. The default implementation replace sexagesimal
     * degree units by the plain fractional degree units, which is much more suitable
     * to computations (see <cite>Note about multi-radix units</cite> in this class's
     * description).
     * <br><br>
     * If sexagesimal degrees are really wanted, subclasses can override this method
     * and just returns <code>unit</code> with no change.
     *
     * @param  unit The unit declared in the EPSG database for a coordinate system.
     * @return The unit to use: <code>unit</code> with no change, or a substitute
     *         if <code>unit</code> was a multi-radix unit (e.g. sexagesimal degree).
     */
    protected Unit replaceAxisUnit(final Unit unit) {
        if (Unit.DMS.equals(unit) || Unit.SEXAGESIMAL_DEGREE.equals(unit)) {
            return Unit.DEGREE;
        }
        return unit;
    }

    /**
     * Dispose any resources hold by this object. The database connection
     * will be closed as well, except if this method is invoked on the
     * default instance as returned by {@link #getDefault}.
     *
     * @throws FactoryException if an error occured while closing the connection.
     */
    public synchronized void dispose() throws FactoryException {
        try {
            close(this != DEFAULT);
        } catch (SQLException exception) {
            throw new FactoryException(null, exception);
        }
    }

    /**
     * Close all statements. If <code>definitive</code> is true, then the database connection
     * is closed as well and this {@link CoordinateSystemEPSGFactory} is no longer usable.
     * If <code>definitive</code> is false, then the connection still alive and this object
     * can be used again.
     *
     * @throws SQLException if an error occured while closing the statements or the connection.
     */
    private synchronized void close(final boolean definitive) throws SQLException {
        for (final Iterator it=statements.values().iterator(); it.hasNext();) {
            final PreparedStatement stmt = (PreparedStatement) it.next();
            stmt.close();
        }
        statements.clear();
        if (definitive) {
            connection.close();
        }
    }

    /**
     * Returns the parameter list for an operation method code.
     *
     * @param  code The operation code.
     * @return Parameters.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if some other errors has occured.
     *
     * @task HACK: This method has a temporary hack when PARAMETER_VALUE_FILE_RE
     *             is defined instead of PARAMETER_VALUE.
     */
    private Parameter[] createParameter(final String code) throws SQLException, FactoryException {
        final List list = new ArrayList();
        final PreparedStatement stmt;
        stmt = prepareStatement("Parameter", "select "
                                       + " COP.PARAMETER_NAME,"
                                       + " COPV.PARAMETER_VALUE,"
                                       + " COPV.UOM_CODE"
                                       + " from [Coordinate_Operation Parameter Usage] as COPU,"
                                       + " [Coordinate_Operation] as CO,"
                                       + " [Coordinate_Operation Parameter] as COP,"
                                       + " [Coordinate_Operation Parameter Value] as COPV"
                                       + " where CO.COORD_OP_CODE = ?"
                                       + " and CO.COORD_OP_METHOD_CODE = COPU.COORD_OP_METHOD_CODE"
                                       + " and COP.PARAMETER_CODE = COPU.PARAMETER_CODE"
                                       + " and COPV.PARAMETER_CODE = COPU.PARAMETER_CODE"
                                       + " and COPV.COORD_OP_CODE = ?"
                                       + " order by COPU.SORT_ORDER");
        stmt.setString(1, code);
        stmt.setString(2, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final String  name = getString(result, 1, code);
            final double value = result.getDouble(2);
            if (result.wasNull()) {
                /*
                 * This a temporary hack because sometimes PARAMETER_VALUE is
                 * not defined, it is replaced by PARAMETER_VALUE_FILE_RE.
                 */
                result.close();
                throw new UnsupportedOperationException("Not yet implemented");
            }
            final String  unit = getString(result, 3, code);
            list.add(new Parameter(name, value, createUnit(unit)));
        }
        result.close();
        return (Parameter[]) list.toArray(new Parameter[list.size()]);
    }
    
    /**
     * An internal class for Operations Parameters informations
     */
    private static final class Parameter {
        /**
         * The EPSG name for this Parameter
         */
        public final String name;

        /**
         * The value of the parameter.
         */
        public final double value;
        
        /**
         * The Unit for this parameter.
         */
        public final Unit unit;
        
        /**
         * Main class constructor.
         */
        private Parameter(final String name, final double value, final Unit unit)
        {
            this.name  = name;
            this.value = value;
            this.unit  = unit;
        }

        /**
         * Copy this parameter in the specified {@link ParameterList}. The EPSG parameter
         * name will be converted into OGC name, and the value into standard units. If the
         * parameter can't be set, a warning is logged but the process continue.
         *
         * @param list The parameter list in which to copy this parameter.
         */
        final void setParameter(final ParameterList list)
        {
            final String ogcName = fromEPSGtoOGC(name);
            double standardValue = value;
            if (Unit.METRE.canConvert(unit)) {
                standardValue = Unit.METRE.convert(standardValue, unit);
            }
            if (Unit.DEGREE.canConvert(unit)) {
                standardValue = Unit.DEGREE.convert(standardValue, unit);
            }
            try {
                list.setParameter(ogcName, standardValue);
            } catch (IllegalArgumentException exception) {
                final LogRecord record = Resources.getResources(null).getLogRecord(Level.WARNING,
                        ResourceKeys.WARNING_UNKNOW_PARAMETER_$3, name, new Double(value), unit);
                record.setSourceClassName("CoordinateSystemEPSGFactory");
                record.setSourceMethodName("createProjectedCoordinateSystem");
                record.setThrown(exception);
                Logger.getLogger("org.geotools.cs").log(record);
            }
        }

        /**
         * Returns a string representation for debugging purpose.
         */
        public String toString()
        {
            final StringBuffer buffer = new StringBuffer("Parameter[\"");
            buffer.append(name);
            buffer.append("\"=");
            buffer.append(value);
            buffer.append(' ');
            buffer.append(unit);
            buffer.append(']');
            return buffer.toString();
        }
    }

    /**
     * Construct an object from the EPSG database and print its WKT (Well Know Text) to
     * the standard output. This method can be invoked from the command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.cs.CoordinateSystemEPSGFactory 4181
     * </pre></blockquote>
     *
     * Should print:
     *
     * <blockquote><pre>
     * GEOGCS["Luxembourg 1930", DATUM["Luxembourg 1930", <FONT face="Arial">etc...</FONT>
     * </pre></blockquote>
     *
     * The following optional arguments are supported:
     * <blockquote>
     *   <strong><code>-connection</code></strong><br>
     *       Set the EPSG database URL. The URL must conform to
     *       {@link DriverManager#getConnection(String)} specification. The default value
     *       is <code>jdbc:odbc:EPSG</code>. The specified URL is stored in system preferences
     *       and will become the default URL for every calls to {@link #getDefault()}.
     *       The "<code>default</code>" string reset the default URL.
     *       <br><br>
     *
     *   <strong><code>-driver</code></strong><br>
     *       Set the driver class. The default value is <code>sun.jdbc.odbc.JdbcOdbcDriver</code>.
     *       The specified classname is stored in system preferences and will become the default
     *       driver for every calls to {@link #getDefault()}.
     *       The "<code>default</code>" string reset the default driver.
     *       <br><br>
     *
     *   <strong><code>-implementation</code></strong><br>
     *       Set an alternative implementation. The argument must be a fully qualified class name
     *       of a <code>CoordinateSystemEPSGFactory</code> subclass. The implementation must have
     *       a public constructor with the following arguments:
     *       <ul>
     *         <li>The coordinate system factory as a {@link CoordinateSystemFactory}</li>
     *         <li>The EPSG database URL as a {@link String}</li>
     *         <li>The database driver as a {@link String}</li>
     *       </ul>
     *       The specified classname is stored in system preferences and will become the default
     *       implementation for every calls to {@link #getDefault()}.
     *       The "<code>default</code>" string reset the default implementation.
     *       <br><br>
     *
     *   <strong><code>-encoding</code></strong><br>
     *       Set the console encoding for this application output.
     *       This value has no impact on <code>CoordinateSystemEPSGFactory</code> behavior.
     * </blockquote>
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of code can be specified on the command line.
     */
    public static void main(String [] args) {
        Geotools.init(); // Use custom logger.
        final Arguments  arguments = new Arguments(args);
        final PrintWriter      out = arguments.out;
        final String     newDriver = arguments.getOptionalString("-driver");
        final String newConnection = arguments.getOptionalString("-connection");
        final String  newImplement = arguments.getOptionalString("-implementation");
        final Preferences prefs = Preferences.systemNodeForPackage(CoordinateSystemAuthorityFactory.class);

        if (newDriver != null) {
            if (newDriver.equalsIgnoreCase("default")) {
                prefs.remove(DRIVER);
            } else {
                prefs.put(DRIVER, newDriver);
            }
        }
        if (newConnection != null) {
            if (newConnection.equalsIgnoreCase("default")) {
                prefs.remove(CONNECTION);
            } else {
                prefs.put(CONNECTION, newConnection);
            }
        }
        if (newImplement != null) {
            if (newImplement.equalsIgnoreCase("default")) {
                prefs.remove(IMPLEMENTATION);
            } else {
                prefs.put(IMPLEMENTATION, newImplement);
            }
        }
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        if (args.length == 0) {
            final String     driver = prefs.get(DRIVER, DEFAULT_DRIVER);
            final String    connect = prefs.get(CONNECTION, DEFAULT_CONNECTION);
            try {
                final Driver drv = (Driver)Class.forName(driver).newInstance();
                out.println(Resources.getResources(arguments.locale).getString(
                            ResourceKeys.LOADED_JDBC_DRIVER_$3, drv.getClass().getName(),
                                                    new Integer(drv.getMajorVersion()),
                                                    new Integer(drv.getMinorVersion())));
            } catch (Exception exception) {
                out.println(exception);
            }
            out.print("Connection: \"");
            out.print(connect);
            out.println('"');
            return;
        }
        CoordinateSystemAuthorityFactory factory = null;
        try {
            try {
                factory = CoordinateSystemEPSGFactory.getDefault();
                for (int i=0; i<args.length; i++) {
                    out.println();
                    out.println();
                    out.print("<=== EPSG ");
                    out.print(args[i]);
                    out.println(" ===>");
                    out.println(factory.createObject(args[i]));
                    out.flush();
                }
            } finally {
                if (factory != null) {
                    factory.dispose();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace(out);
        }
        out.flush();
    }
}
