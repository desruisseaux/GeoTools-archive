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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.epsg;

// J2SE dependencies and extensions
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.spi.ServiceRegistry;
import javax.units.NonSI;
import javax.units.Unit;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.nature.Units;
import org.geotools.metadata.extent.GeographicBoundingBox;
import org.geotools.referencing.AuthorityFactory;
import org.geotools.referencing.FactoryGroup;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.MonolineFormatter;
import org.geotools.util.LocalName;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.ScopedName;


/**
 * Default implementation for a coordinate system factory backed by the EPSG database. The EPSG
 * database is freely available at <A HREF="http://www.epsg.org">http://www.epsg.org</a>. Current
 * version of this class requires EPSG database version 6.
 * <br>
 * <h2>EPSG database installation</h2>
 * The EPSG database is available in MS Access format. The <code>EPSG_v6.mdb</code> file can be
 * stored anywhere on yours system under any name, at your convenience. The database must be
 * declared as an ODBC data source. Steps to follow:
 *
 * <ul>
 *   <li>Open the <cite>ODBC data sources</cite> dialog box from the Windows's Control Panel.</li>
 *   <li>Click the "Add..." button an select <cite>Microsoft Access Driver (*.mdb)</cite>.</li>
 *   <li>Data source name should be "EPSG". Filename can be anything; click on the "Select..."
 *       button to select it.</li>
 * </ul>
 *
 * @version $Id$
 * @author Yann Cézard
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
// TODO: vérifier noSuchAuthorityCode, new FactoryException
public class DefaultFactory extends AuthorityFactory
        implements DatumAuthorityFactory, CSAuthorityFactory, CRSAuthorityFactory
{
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                            ////////
    ////////      H A R D   C O D E D   V A L U E S    (other than SQL statements)      ////////
    ////////                                                                            ////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Preference node for the JDBC driver class name, and its default value.
     */
    private static final String DRIVER = "JDBC driver",
                        DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

    /**
     * Preference node for the EPSG database connection string, and its default value.
     */
    private static final String CONNECTION = "EPSG connection",
                        DEFAULT_CONNECTION = "jdbc:odbc:EPSG";

    /**
     * Returns a hard-coded unit from an EPSG code. We do not need to provide all units here,
     * but we must at least provide all base units declared in the [TARGET_UOM_CODE] column
     * of table [Unit of Measure]. Other units will be derived automatically if they are not
     * listed here.
     *
     * @param  code The code.
     * @return The unit, or <code>null</code> if the code is unrecognized.
     *
     * @todo Sexagesimal units not yet ported from legacy CTS code.
     *       If we supports them, investigate the potential impact on WKT formatting.
     */
    private static Unit getUnit(final int code) {
        switch (code) {
            case 9001: return    SI.METER;
            case 9002: return NonSI.FOOT;
            case 9030: return NonSI.NAUTICAL_MILE;
            case 9036: return    SI.KILO(SI.METER);
            case 9101: return    SI.RADIAN;
            case 9102: return NonSI.DEGREE_ANGLE;
            case 9103: return NonSI.MINUTE_ANGLE;
            case 9104: return NonSI.SECOND_ANGLE;
            case 9105: return NonSI.GRADE;
            case 9107: return Units.DEGREE_MINUTE_SECOND;
            case 9108: return Units.DEGREE_MINUTE_SECOND;
            case 9109: return    SI.MICRO(SI.RADIAN);
            case 9110: return Units.SEXAGESIMAL_DMS;
//TODO      case 9111: return NonSI.SEXAGESIMAL_DM;
            case 9201: return  Unit.ONE;
            case 9202: return Units.PPM;
            default  : return null;
        }
    }

    /**
     * Set a Bursa-Wolf parameter from an EPSG parameter.
     *
     * @param  parameters The Bursa-Wolf parameters to modify.
     * @param  code       The EPSG code for a parameter   from [PARAMETER_CODE]  column.
     * @param  value      The value of the parameter      from [PARAMETER_VALUE] column.
     * @param  unit       The unit of the parameter value from [UOM_CODE]        column.
     * @throws FactoryException if the code is unrecognized.
     */
    private static void setBursaWolfParameter(final BursaWolfParameters parameters,
                                              final int code, double value, final Unit unit)
            throws FactoryException
    {
        Unit target = unit;
        if (code >= 8605) {
            if      (code <= 8607) target = SI   .METER;
            else if (code <= 8710) target = NonSI.SECOND_ANGLE;
            else if (code == 8611) target = Units.PPM;
        }
        if (target != unit) {
            value = unit.getConverterTo(target).convert(value);
        }
        switch (code) {
            case 8605: parameters.dx  = value; break;
            case 8606: parameters.dy  = value; break;
            case 8607: parameters.dz  = value; break;
            case 8608: parameters.ex  = value; break;
            case 8609: parameters.ey  = value; break;
            case 8610: parameters.ez  = value; break;
            case 8611: parameters.ppm = value; break;
            default:   throw new FactoryException("Unexpected parameter code: "+code);
                       // TODO: localize.
        }
        /*
         * NOTE: THERE IS A FEW MORE HARD-CODED CONSTANTS IN createBursaWolfParameters(...).
         *       Namely: minimum and maximum operation method code (9603 and 9607 respectively)
         *       and coordinate rotation frame operation method code (9607).
         */
    }

    /**
     * List of tables and columns to test for codes values.
     * Elements at even index are table name.
     * Elements at odd index are column name.
     *
     * This table is used by the {@link #createObject} method in order to detect
     * which of the following methods should be invoked for a given code:
     *
     * {@link #createCoordinateReferenceSystem}
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
        "[Coordinate Reference System]", "COORD_REF_SYS_CODE",   // [ 0]: createCoordinateReferenceSystem
        "[Coordinate System]",           "COORD_SYS_CODE",       // [ 2]: createCoordinateSystem
        "[Coordinate Axis]",             "COORD_AXIS_CODE",      // [ 4]: createCoordinateSystemAxis
        "[Datum]",                       "DATUM_CODE",           // [ 6]: createDatum
        "[Ellipsoid]",                   "ELLIPSOID_CODE",       // [ 8]: createEllipsoid
        "[Prime Meridian]",              "PRIME_MERIDIAN_CODE"   // [10]: createPrimeMeridian
    };

    ////////////////////////////////////////////////////
    ////////                                    ////////
    ////////      END OF HARD CODED VALUES      ////////
    ////////                                    ////////
    ////////////////////////////////////////////////////
    /**
     * Last object type returned by {@link #createObject}, or -2 if none.
     * This type is an index in the {@link #OBJECT_TABLES} array and is
     * strictly for {@link #createObject} internal use.
     */
    private int lastObjectType = -2;

    /**
     * <code>true</code> if this factory has been registered as a service provider.
     * This this case, this factory will never be fully {@linkplain #dispose disposed}
     * except at the JVM shutdown.
     */
    private boolean isService;

    /**
     * The calendar instance for creating {@link java.util.Date} objects from a year
     * (the "epoch" in datum definition). We use the local timezone, which may not be
     * quite accurate. But there is no obvious timezone for "epoch", and the "epoch"
     * is approximative anyway.
     */
    private final Calendar calendar = Calendar.getInstance();

    /**
     * A pool of prepared statements. Key are {@link String} object related to their
     * originating method name (for example "Ellipsoid" for {@link #createEllipsoid},
     * while values are {@link PreparedStatement} objects.
     */
    private final Map statements = new IdentityHashMap();

    /**
     * Pool of naming systems, used for caching.
     * There is usually few of them (about 15).
     */
    private final Map scopes = new HashMap();

    /**
     * The properties to be given the objects to construct.
     * Reused every time {@link #createProperties} is invoked.
     */
    private final Map properties = new HashMap();

    /**
     * The set of alias for an object to construct.
     * Reused every time {@link #createProperties} is invoked.
     */
    private final List alias = new ArrayList();

    /**
     * A safety guard for preventing never-ending loops in recursive calls to
     * {@link #createDatum}. This is used by {@link #createBursaWolfParameters},
     * which need to create a target datum. The target datum could have its own
     * Bursa-Wolf parameters, with one of them pointing again to the source datum.
     */
    private final Set safetyGuard = new HashSet();

    /**
     * The connection to the EPSG database.
     */
    protected final Connection connection;

    /**
     * Constructs an authority factory using the default set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories} and the
     * default connection parameters to the EPSG database.
     *
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public DefaultFactory() throws SQLException {
        this(new FactoryGroup());
    }

    /**
     * Constructs an authority factory using the specified set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories} and the
     * default connection parameters to the EPSG database.
     * <br><br>
     * By default, this constructor loads the <code>"sun.jdbc.odbc.JdbcOdbcDriver"</code> and
     * ask for a connection to the <code>"jdbc:odbc:EPSG"</code> database. This default
     * behavior can be changed by invoking the {@link #main} method from the command line.
     * For example:
     * <blockquote><pre>
     * java org.geotools.referencing.espg.DefaultFactory -driver=[my driver] -connection=[my url]
     * </pre></blockquote>
     *
     * @param factories The set of object factories to use.
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public DefaultFactory(final FactoryGroup factories) throws SQLException {
        this(factories, Preferences.systemNodeForPackage(DefaultFactory.class));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database.
     */
    private DefaultFactory(final FactoryGroup factories,
                           final Preferences preferences) throws SQLException
    {
        this(factories,
             preferences.get(CONNECTION, DEFAULT_CONNECTION),
             preferences.get(DRIVER,     DEFAULT_DRIVER));
    }

    /**
     * Constructs an authority factory using the specified URL to an EPSG database.
     *
     * @param  factories The underlying factory used for objects creation.
     * @param  url       The url to the EPSG database. For example, a connection
     *                   using the ODBC-JDBC bridge may have an URL likes
     *                   <code>"jdbc:odbc:EPSG"</code>.
     * @param  driver    An optional driver to load, or <code>null</code> if none.
     *                   This is a convenience argument for the following pseudo-code:
     *                   <blockquote><code>
     *                   Class.forName(driver).newInstance();
     *                   </code></blockquote>
     *                   A message is logged to <code>"org.geotools.referencing"</code> wether
     *                   the loading sucseeds of fails. For JDBC-ODBC bridge, a typical value
     *                   for this argument is <code>"sun.jdbc.odbc.JdbcOdbcDriver"</code>.
     *                   This argument needs to be non-null only once for a specific driver.
     *
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public DefaultFactory(final FactoryGroup factories, final String url, final String driver)
            throws SQLException
    {
        this(factories, getConnection(url, driver));
    }

    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param factories  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     */
    public DefaultFactory(final FactoryGroup factories, final Connection connection) {
        super(factories, MAX_PRIORITY);
        this.connection = connection;
        ensureNonNull("connection", connection);
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
        ensureNonNull("url", url);
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
            record.setSourceClassName("DefaultFactory");
            record.setSourceMethodName("<init>");
            Logger.getLogger("org.geotools.referencing").log(record);
        }
        return DriverManager.getConnection(url);
    }

    /**
     * Returns the authority, which is {@link org.geotools.metadata.citation.Citation#EPSG EPSG}.
     */
    public Citation getAuthority() {
        return org.geotools.metadata.citation.Citation.EPSG;
    }

    /**
     * Returns the set of authority codes of the given type. The <code>type</code>
     * argument specify the base class. For example:
     * <ul>
     *   <li><strong><code>{@linkplain CoordinateReferenceSystem}.class&nbsp;</code></strong>
     *       asks for all authority codes accepted by one of
     *       {@link #createGeographicCRS createGeographicCRS},
     *       {@link #createProjectedCRS createProjectedCRS},
     *       {@link #createVerticalCRS createVerticalCRS},
     *       {@link #createTemporalCRS createTemporalCRS}
     *       and their friends.</li>
     *   <li><strong><code>{@linkplain ProjectedCRS}.class&nbsp;</code></strong>
     *       asks only for authority codes accepted by
     *       {@link #createProjectedCRS createProjectedCRS}.</li>
     * </ul>
     *
     * @param  type The spatial reference objects type (may be <code>Object.class</code>).
     * @return The set of authority codes for spatial reference objects of the given type.
     *         If this factory doesn't contains any object of the given type, then this method
     *         returns an {@linkplain java.util.Collections#EMPTY_SET empty set}.
     * @throws FactoryException if access to the underlying database failed.
     */
    public Set/*<String>*/ getAuthorityCodes(final Class type) throws FactoryException {
        throw new UnsupportedOperationException("Not yet implemented."); // TODO
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or <code>null</code> if the object
     *         corresponding to the specified <code>code</code> has no description.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the query failed for some other reason.
     */
    public InternationalString getDescriptionText(String code) throws FactoryException {
        throw new UnsupportedOperationException("Not yet implemented."); // TODO
    }

    /**
     * Returns a prepared statement for the specified name.
     *
     * @param  key A key uniquely identifying the caller
     *         (e.g. <code>"Ellipsoid"</code> for {@link #createEllipsoid}).
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
     * @param  code The identifier of the record where the string was found.
     * @return The string at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static String getString(final ResultSet result, final int columnIndex, final String code)
            throws SQLException, FactoryException
    {
        final String str = result.getString(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        code, column));
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
     * @param  code The identifier of the record where the string was found.
     * @return The double at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static double getDouble(final ResultSet result, final int columnIndex, final String code)
            throws SQLException, FactoryException
    {
        final double value = result.getDouble(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        code, column));
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
     * @param  code The identifier of the record where the string was found.
     * @return The integer at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static int getInt(final ResultSet result, final int columnIndex, final String code)
            throws SQLException, FactoryException
    {
        final int value = result.getInt(columnIndex);
        if (result.wasNull()) {
            final String column = result.getMetaData().getColumnName(columnIndex);
            result.close();
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$2,
                                                        code, column));
        }
        return value;
    }

    /**
     * Make sure that an object constructed from the database is not duplicated.
     *
     * @param  newValue The newly constructed object.
     * @param  oldValue The object previously constructed, or <code>null</code> if none.
     * @param  code The EPSG code (for formatting error message).
     * @throws FactoryException if a duplication has been detected.
     */
    private static Object ensureSingleton(final Object newValue, final Object oldValue, final String code)
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
     * Returns the name for the {@link IdentifiedObject} to construct.
     * This method also search for alias.
     *
     * @param  name The name for the {@link IndentifiedObject} to construct.
     * @param  code The EPSG code of the object to construct.
     * @param  remarks Remarks, or <code>null</code> if none.
     * @return The name together with a set of properties.
     */
    private Map createProperties(final String name, final String code, String remarks)
            throws SQLException, FactoryException
    {
        final Citation   authority  = getAuthority();
        final Identifier title      = new Identifier(authority, name.trim());
        final Identifier identifier = new Identifier(authority, code.trim());
        properties.clear();
        properties.put(org.geotools.referencing.IdentifiedObject.NAME_PROPERTY,        title);
        properties.put(org.geotools.referencing.IdentifiedObject.IDENTIFIERS_PROPERTY, identifier);
        if (remarks!=null && (remarks=remarks.trim()).length()!=0) {
            properties.put(org.geotools.referencing.IdentifiedObject.REMARKS_PROPERTY, remarks);
        }
        /*
         * Search for alias.
         */
        alias.clear();
        final PreparedStatement stmt;
        stmt = prepareStatement("Alias", "SELECT NAMING_SYSTEM_NAME,"
                                       +       " ALIAS"
                                       + " FROM [Alias] INNER JOIN [Naming System]"
                                       +   " ON [Alias].NAMING_SYSTEM_CODE ="
                                       +      " [Naming System].NAMING_SYSTEM_CODE"
                                       + " WHERE OBJECT_CODE = ?");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final String scope = getString(result, 1, code);
            LocalName cached = (LocalName) scopes.get(scope);
            if (cached == null) {
                cached = new LocalName(scope);
                scopes.put(scope, cached);
            }
            alias.add(new ScopedName(cached, getString(result, 2, code)));
        }
        result.close();
        if (!alias.isEmpty()) {
            properties.put(org.geotools.referencing.IdentifiedObject.ALIAS_PROPERTY,
                           (GenericName[]) alias.toArray(new GenericName[alias.size()]));
        }
        return properties;
    }

    /**
     * Returns an arbitrary object from a code.
     * The default implementation invokes one of {@link #createCoordinateReferenceSystem},
     * {@link #createCoordinateSystem}, {@link #createDatum}, {@link #createEllipsoid}, or
     * {@link #createUnit} methods according the object type.
     *
     * @param  code The EPSG value.
     * @return The object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized IdentifiedObject createObject(final String code) throws FactoryException {
        final String       KEY = "IdentifiedObject";
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
                        query = new StringBuffer("SELECT ");
                    }
                    query.setLength(7); // 7 is the length of "SELECT " in the line above.
                    query.append(column);
                    query.append(" FROM ");
                    query.append(table);
                    query.append(" WHERE ");
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
                        case  0:  return createCoordinateReferenceSystem(code);
                        case  2:  return createCoordinateSystem         (code);
                        case  4:  return createCoordinateSystemAxis     (code);
                        case  6:  return createDatum                    (code);
                        case  8:  return createEllipsoid                (code);
                        case 10:  return createPrimeMeridian            (code);
                        default: throw new AssertionError(i); // Should not happen
                    }
                }
                statements.remove(KEY);
                stmt.close();
            } catch (SQLException exception) {
                throw new FactoryException(exception);
            }
        }
        return super.createObject(code);
    }

    /**
     * Returns an unit from a code.
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
            stmt = prepareStatement("Unit", "SELECT UOM_CODE,"
                                          +       " FACTOR_B,"
                                          +       " FACTOR_C,"
                                          +       " TARGET_UOM_CODE"
                                          + " FROM [Unit of Measure]"
                                          + " WHERE UOM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int source = getInt(result,   1, code);
                final double   b = result.getDouble(2);
                final double   c = result.getDouble(3);
                final int target = getInt(result,   4, code);
                final Unit  base = getUnit(target);
                if (base == null) {
                    throw noSuchAuthorityCode(Unit.class, String.valueOf(target));
                }
                Unit unit = getUnit(source);
                if (unit != null) {
                    // TODO: check unit consistency here.
                } else if (b!=0 && c!=0) {
                    unit = (b==c) ? base : base.multiply(b/c);
                } else {
                    // TODO: provide a localized message.
                    throw new FactoryException("Unsupported unit: " + code);
                }
                returnValue = (Unit) ensureSingleton(unit, returnValue, code);
            }
            result.close();
        }
        catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        if (returnValue == null) {
            throw noSuchAuthorityCode(Unit.class, code);
        }
        return returnValue;
    }

    /**
     * Returns an ellipsoid from a code.
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
            stmt = prepareStatement("Ellipsoid", "SELECT ELLIPSOID_NAME,"
                                               +       " SEMI_MAJOR_AXIS,"
                                               +       " INV_FLATTENING,"
                                               +       " SEMI_MINOR_AXIS,"
                                               +       " UOM_CODE,"
                                               +       " REMARKS"
                                               + " FROM [Ellipsoid]"
                                               + " WHERE ELLIPSOID_CODE = ?");
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
                final Map    properties        = createProperties(name, code, remarks);
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
                        ellipsoid = factories.getDatumFactory().createEllipsoid(
                                    properties, semiMajorAxis, semiMinorAxis, unit);
                    }
                } else {
                    if (semiMinorAxis != 0) {
                        // Both 'inverseFlattening' and 'semiMinorAxis' are defined.
                        // Log a warning and create the ellipsoid using the inverse flattening.
                        Logger.getLogger("org.geotools.referencing").warning(Resources.format(
                                            ResourceKeys.WARNING_AMBIGUOUS_ELLIPSOID));
                    }
                    ellipsoid = factories.getDatumFactory().createFlattenedSphere(
                                properties, semiMajorAxis, inverseFlattening, unit);
                }
                /*
                 * Now that we have built an ellipsoid, compare
                 * it with the previous one (if any).
                 */
                returnValue = (Ellipsoid) ensureSingleton(ellipsoid, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        if (returnValue == null) {
             throw noSuchAuthorityCode(Ellipsoid.class, code);
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
            stmt = prepareStatement("PrimeMeridian", "SELECT PRIME_MERIDIAN_NAME,"
                                                   +       " GREENWICH_LONGITUDE,"
                                                   +       " UOM_CODE,"
                                                   +       " REMARKS"
                                                   + " FROM [Prime Meridian]"
                                                   + " WHERE PRIME_MERIDIAN_CODE = ?");
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
                final Map properties   = createProperties(name, code, remarks);
                PrimeMeridian primeMeridian = factories.getDatumFactory().createPrimeMeridian(
                                              properties, longitude, unit);
                returnValue = (PrimeMeridian) ensureSingleton(primeMeridian, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        if (returnValue == null) {
            throw noSuchAuthorityCode(PrimeMeridian.class, code);
        }
        return returnValue;
    }

    /**
     * Returns an area of use.
     *
     * @param  code Value allocated by authority.
     * @return The area of use.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Extent createExtent(final String code)
            throws FactoryException
    {
        Extent returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Area", "SELECT AREA_OF_USE,"
                                          +       " AREA_SOUTH_BOUND_LAT,"
                                          +       " AREA_NORTH_BOUND_LAT,"
                                          +       " AREA_WEST_BOUND_LON,"
                                          +       " AREA_EAST_BOUND_LON"
                                          + " FROM [Area]"
                                          + " WHERE AREA_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */ 
            while (result.next()) {
                final String description = getString(result, 1, code);
                final org.geotools.metadata.extent.Extent extent =
                      new org.geotools.metadata.extent.Extent();
                extent.setDescription(new SimpleInternationalString(description));
                final double ymin = result.getDouble(2);
                if (!result.wasNull()) {
                    final double ymax = result.getDouble(3);
                    if (!result.wasNull()) {
                        final double xmin = result.getDouble(4);
                        if (!result.wasNull()) {
                            final double xmax = result.getDouble(5);
                            if (!result.wasNull()) {
                                extent.setGeographicElement(
                                        new GeographicBoundingBox(xmin, xmax, ymin, ymax));
                            }
                        }
                    }
                }
                returnValue = (Extent) ensureSingleton(extent, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        if (returnValue == null) {
            throw noSuchAuthorityCode(Extent.class, code);
        }
        return returnValue;
    }

    /**
     * Private usage for {@link #createBursaWolfParameters}.
     */
    private static final class Info {
        /** CO.COORD_OP_CODE        */ final String operation;
        /** CO.COORD_OP_METHOD_CODE */ final int    method;
        /** CRS1.DATUM_CODE         */ final String target;
        Info(final String operation, final int method, final String target) {
            this.operation = operation;
            this.method    = method;
            this.target    = target;
        }
    }

    /** 
     * Returns Bursa-Wolf parameters for a geodetic datum. If the specified datum has
     * no conversion informations, then this method will returns <code>null</code>.
     *  
     * @param  code The EPSG code of the {@link GeodeticDatum}.
     * @param  toClose The result set to close if this method is going to invokes
     *         {@link #createDatum} recursively. This hack is necessary because many
     *         JDBC drivers do not support multiple result sets for the same statement.
     *         The result set is closed if an only if this method returns a non-null value.
     * @return an array of Bursa-Wolf parameters (in which case <code>toClose</code> has
     *         been closed), or <code>null</code> (in which case <code>toClose</code> has
     *         <strong>not</strong> been closed).
     */
    private BursaWolfParameters[] createBursaWolfParameters(final String    code,
                                                            final ResultSet toClose)
            throws SQLException, FactoryException
    {
        if (safetyGuard.contains(code)) {
            /*
             * Do not try to create Bursa-Wolf parameters if the datum is already
             * in process of being created. This check avoid never-ending loops in
             * recursive call to 'createDatum'.
             */
            return null;
        }
        List list = null;
        PreparedStatement stmt;
        stmt = prepareStatement("BursaWolfParametersSet",
                                         "SELECT FIRST(CO.COORD_OP_CODE),"
                                 +             " FIRST(CO.COORD_OP_METHOD_CODE),"
                                 +             " CRS2.DATUM_CODE"
                                 +      " FROM ([Coordinate_Operation] AS CO"
                                 + " INNER JOIN [Coordinate Reference System] AS CRS1"
                                 +          " ON CO.SOURCE_CRS_CODE = CRS1.COORD_REF_SYS_CODE)"
                                 + " INNER JOIN [Coordinate Reference System] AS CRS2"
                                 +          " ON CO.TARGET_CRS_CODE = CRS2.COORD_REF_SYS_CODE"
                                 +       " WHERE CO.COORD_OP_METHOD_CODE >= 9603"
                                 +         " AND CO.COORD_OP_METHOD_CODE <= 9607"
                                 +         " AND CRS1.DATUM_CODE = ?"
                                 +    " GROUP BY CRS2.DATUM_CODE");
        stmt.setString(1, code);
        ResultSet result = stmt.executeQuery();
        while (result.next()) {
            if (list == null) {
                list = new ArrayList();
            }
            list.add(new Info(getString(result, 1, code),
                              getInt   (result, 2, code),
                              getString(result, 3, code)));
        }
        result.close();
        if (list == null) {
            return null;
        }
        toClose.close();
        /*
         * We got all the needed informations before to built Bursa-Wolf parameters because the
         * 'createDatum(...)' call below may invokes 'createBursaWolfParameters(...)' recursively,
         * and not all JDBC drivers supported multi-result set for the same statement. Now, iterate
         * throw the results and fetch the parameter values for each BursaWolfParameters object.
         */
        stmt = prepareStatement("BursaWolfParameters", "SELECT PARAMETER_CODE,"
                                                     +       " PARAMETER_VALUE,"
                                                     +       " UOM_CODE"
                                                     + " FROM [Coordinate_Operation Parameter Value]"
                                                     + " WHERE COORD_OP_CODE = ?"
                                                     +   " AND COORD_OP_METHOD_CODE = ?");
        final int size = list.size();
        for (int i=0; i<size; i++) {
            final Info info = (Info) list.get(i);
            final GeodeticDatum datum;
            try {
                safetyGuard.add(code);
                datum = createGeodeticDatum(info.target);
            } finally {
                safetyGuard.remove(code);
            }
            final BursaWolfParameters parameters = new BursaWolfParameters(datum);
            stmt.setString(1, info.operation);
            stmt.setInt   (2, info.method);
            result = stmt.executeQuery();
            while (result.next()) {
                setBursaWolfParameter(parameters,
                                      getInt   (result, 1, info.operation),
                                      getDouble(result, 2, info.operation),
                           createUnit(getString(result, 3, info.operation)));
            }
            result.close();
            if (info.method == 9607) {
                // Coordinate frame rotation: same as 9606,
                // except for the sign of rotation parameters.
                parameters.ex = -parameters.ex;
                parameters.ey = -parameters.ey;
                parameters.ey = -parameters.ey;
            }
            list.set(i, parameters);
        }            
        return (BursaWolfParameters[]) list.toArray(new BursaWolfParameters[size]);
    }

    /**
     * Returns a datum from a code.
     *
     * @param  code Value allocated by authority.
     * @return The datum object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     *
     * @todo Current implementation maps all "vertical" datum to
     *       {@link VerticalDatumType#ELLIPSOIDAL}. We don't know yet how
     *       to maps the exact vertical datum type from the EPSG database.
     */
    public synchronized Datum createDatum(final String code) throws FactoryException {
        Datum returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Datum", "SELECT DATUM_NAME,"
                                           +       " DATUM_TYPE,"
                                           +       " ORIGIN_DESCRIPTION,"
                                           +       " REALIZATION_EPOCH,"
                                           +       " AREA_OF_USE_CODE,"
                                           +       " DATUM_SCOPE,"
                                           +       " REMARKS,"
                                           +       " ELLIPSOID_CODE,"     // Only for geodetic type
                                           +       " PRIME_MERIDIAN_CODE" // Only for geodetic type
                                           + " FROM [Datum]"
                                           + " WHERE DATUM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            boolean stop = false;
            while (result.next()) {
                final String name    = getString(result, 1, code);
                final String type    = getString(result, 2, code);
                final String anchor  = result.getString( 3);
                final int    epoch   = result.getInt   ( 4);
                final String area    = result.getString( 5);
                final String scope   = result.getString( 6);
                final String remarks = result.getString( 7);
                Map properties = createProperties(name, code, remarks);
                if (anchor != null) {
                    properties.put(org.geotools.referencing.datum.Datum.
                                   ANCHOR_POINT_PROPERTY, anchor);
                }
                if (epoch != 0) {
                    calendar.clear();
                    calendar.set(epoch, 0, 1);
                    properties.put(org.geotools.referencing.datum.Datum.
                                   REALIZATION_EPOCH_PROPERTY, calendar.getTime());
                }
                if (area != null) {
                    properties.put(org.geotools.referencing.datum.Datum.
                                   VALID_AREA_PROPERTY, createExtent(area));
                }
                if (scope != null) {
                    properties.put(org.geotools.referencing.datum.Datum.
                                   SCOPE_PROPERTY, scope);
                }
                final DatumFactory factory = factories.getDatumFactory();
                final Datum datum;
                /*
                 * Now build datum according their datum type. Constructions are straightforward,
                 * except for the "geodetic" datum type which need some special processing:
                 *
                 *   - Because it invokes again 'createProperties' indirectly (through calls to
                 *     'createEllipsoid' and 'createPrimeMeridian'), it must protect 'properties'
                 *     from changes.
                 *
                 *   - Because 'createBursaWolfParameters' may invokes 'createDatum' recursively,
                 *     we must close the result set if Bursa-Wolf parameters are found. In this
                 *     case, we lost our paranoiac check for duplication.
                 */
                if (type.equalsIgnoreCase("engineering")) {
                    datum = factory.createEngineeringDatum(properties);
                } else if (type.equalsIgnoreCase("vertical")) {
                    // TODO: Find the right datum type.
                    datum = factory.createVerticalDatum(properties, VerticalDatumType.ELLIPSOIDAL);
                } else if (type.equalsIgnoreCase("geodetic")) {
                    properties = new HashMap(properties); // Protect from changes
                    final Ellipsoid         ellipsoid = createEllipsoid    (getString(result, 8, code));
                    final PrimeMeridian      meridian = createPrimeMeridian(getString(result, 9, code));
                    final BursaWolfParameters[] param = createBursaWolfParameters(code, result);
                    if (param != null) {
                        properties.put(org.geotools.referencing.datum.GeodeticDatum.
                                       BURSA_WOLF_PROPERTY, param);
                        stop = true;
                    }
                    datum = factory.createGeodeticDatum(properties, ellipsoid, meridian);
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                returnValue = (Datum) ensureSingleton(datum, returnValue, code);
                if (stop) {
                    // Bypass the 'result.close()' line below:
                    // the ResultSet has already been closed.
                    return returnValue;
                }
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        if (returnValue == null) {
            throw noSuchAuthorityCode(Datum.class, code);
        }
        return returnValue;
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @todo Not yet implemented.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        throw new FactoryException("Not yet implemented.");
    }

    /**
     * Returns the coordinate system axis from an EPSG code for a {@link CoordinateSystem}.
     *
     * @param  code the EPSG code for coordinate system owner.
     * @param  dimension of the coordinate system, which is also the size of the returned array.
     * @return An array of coordinate system axis.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if the code has not been found.
     *
     * @todo WARNING!! The EPSG database use "ORDER" as a column name.
     *       This is tolerated by Access, but MySQL doesn't accept this name.
     */
    private CoordinateSystemAxis[] createCoordinateSystemAxis(String code, final int dimension)
            throws SQLException, FactoryException
    {
        final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[dimension];
        final PreparedStatement stmt;
        stmt = prepareStatement("Axis", "SELECT CA.COORD_AXIS_CODE,"
                                +             " CAN.COORD_AXIS_NAME,"
                                +             " CA.COORD_AXIS_ORIENTATION,"
                                +             " CA.COORD_AXIS_ABBREVIATION,"
                                +             " CA.UOM_CODE,"
                                +             " CAN.DESCRIPTION,"
                                +             " CAN.REMARKS"
                                +       " FROM [Coordinate Axis] AS CA"
                                + " INNER JOIN [Coordinate Axis Name] AS CAN"
                                +          " ON CA.COORD_AXIS_NAME_CODE = CAN.COORD_AXIS_NAME_CODE"
                                +       " WHERE CA.COORD_SYS_CODE = ?"
                                +    " ORDER BY [CA.ORDER]");
                                // WARNING: Be careful about the column name :
                                //          MySQL rejects ORDER as a column name !!!
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        final CSFactory factory = factories.getCSFactory();
        int i = 0;
        while (result.next()) {
            if (i >= axis.length) {
                // An exception will be thrown after the loop.
                continue;
            }
                         code         = getString(result, 1, code);
            final String name         = getString(result, 2, code);
            final String orientation  = getString(result, 3, code);
            final String abbreviation = getString(result, 4, code);
            final String unit         = getString(result, 5, code);
                  String description  = result.getString( 6);
            final String remarks      = result.getString( 7);
            final AxisDirection direction;
            try {
                direction = org.geotools.referencing.cs.CoordinateSystemAxis.getDirection(orientation);
            } catch (NoSuchElementException exception) {
                throw new FactoryException(Resources.format(
                                           ResourceKeys.ERROR_UNKNOW_TYPE_$1, name), exception);
            }
            if (description == null) {
                description = remarks;
            } else if (remarks != null) {
                description += System.getProperty("line.separator", "\n") + remarks;
            }
            final Map properties = createProperties(name, code, description);
            axis[i++] = factory.createCoordinateSystemAxis(properties, abbreviation, direction,
                                                           createUnit(unit));
        }
        result.close();
        if (i != axis.length) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                       new Integer(axis.length), new Integer(i)));
        }
        return axis;
    }

//    /**
//     * Returns a coordinate system from a code.
//     *
//     * @param  code Value allocated by authority.
//     * @return The coordinate system object.
//     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
//     * @throws FactoryException if some other kind of failure occured in the backing
//     *         store. This exception usually have {@link SQLException} as its cause.
//     */
//    public synchronized CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
//            throws FactoryException
//    {
//        String type = null;
//        try {
//            final PreparedStatement stmt;
//            stmt = prepareStatement("CoordinateReferenceSystem", "select COORD_REF_SYS_KIND"
//                                                        + " from [Coordinate Reference System]"
//                                                        + " where COORD_REF_SYS_CODE = ?");
//            stmt.setString(1, code);
//            final ResultSet result = stmt.executeQuery();
//            while (result.next()) {
//                final String candidate = getString(result, 1, code);
//                type = (String) ensureSingleton(candidate, type, code);
//            }
//            result.close();
//        } catch (SQLException exception) {
//            throw new FactoryException(code, exception);
//        }
//        if (type == null) {
//             throw new NoSuchAuthorityCodeException(code);
//        }
//        if (type.equalsIgnoreCase("compound")) {
//            return createCompoundCoordinateSystem(code);
//        }
//        if (type.equalsIgnoreCase("vertical")) {
//            return createVerticalCoordinateSystem(code);
//        }
//        if (type.equalsIgnoreCase("geographic 2D")) {
//            return createGeographicCoordinateSystem(code);
//        }
//        if (type.equalsIgnoreCase("projected")) {
//            return createProjectedCoordinateSystem(code);
//        }
//        throw new FactoryException(Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
//    }
//    
//    /**
//     * Returns a geographic coordinate system from an EPSG code.
//     *
//     * @param  code Value allocated by authority.
//     * @return The geographic coordinate system object.
//     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
//     * @throws FactoryException if some other kind of failure occured in the backing
//     *         store. This exception usually have {@link SQLException} as its cause.
//     *
//     */
//    public synchronized GeographicCoordinateSystem createGeographicCoordinateSystem(final String code)
//            throws FactoryException
//    {
//        GeographicCoordinateSystem returnValue = null;
//        try {
//            final PreparedStatement stmt;
//            stmt = prepareStatement("GeographicCoordinateSystem", "select DIMENSION,"
//                                               + " CS.COORD_SYS_CODE,"
//                                               + " COORD_REF_SYS_NAME,"
//                                               + " PRIME_MERIDIAN_CODE,"
//                                               + " D.DATUM_CODE,"
//                                               + " CRS.REMARKS"
//                                               + " from [Coordinate Reference System] as CRS,"
//                                               + " [Coordinate System] as CS,"
//                                               + " [Datum] as D"
//                                               + " where COORD_REF_SYS_CODE = ?"
//                                               + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE"
//                                               + " and D.DATUM_CODE = CRS.DATUM_CODE");
//            stmt.setString(1, code);
//            final ResultSet result = stmt.executeQuery();
//            /*
//             * If the supplied code exists in the database, then we
//             * should find only one record.   However, we will do a
//             * paranoiac check and verify if there is more records.
//             */
//            while (result.next()) {
//                final int        dimension = getInt   (result, 1, code);
//                final String  coordSysCode = getString(result, 2, code);
//                final String          name = getString(result, 3, code);
//                final String primeMeridian = getString(result, 4, code);
//                final String         datum = getString(result, 5, code);
//                final String       remarks = result.getString( 6);
//                final AxisInfo[] axisInfos = createAxisInfos(coordSysCode, dimension);
//                final Unit            unit = createUnitCS(coordSysCode);
//                final CharSequence     prp = createProperties(name, code, remarks);
//                final CoordinateSystem coordSys;
//                coordSys = factory.createGeographicCoordinateSystem(prp, unit,
//                                            createHorizontalDatum(datum),
//                                            createPrimeMeridian(primeMeridian),
//                                            axisInfos[0], axisInfos[1]);
//                returnValue = (GeographicCoordinateSystem) ensureSingleton(coordSys, returnValue, code);
//            }
//            result.close();
//        } catch (SQLException exception) {
//            throw new FactoryException(code, exception);
//        }
//        if (returnValue == null) {
//            throw new NoSuchAuthorityCodeException(code);
//        }
//        return returnValue;
//    }
//    
//    /**
//     * Returns a projected coordinate system from an EPSG code.
//     *
//     * @param  code Value allocated by authority.
//     * @return The projected coordinate system object.
//     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
//     * @throws FactoryException if some other kind of failure occured in the backing
//     *         store. This exception usually have {@link SQLException} as its cause.
//     */
//    public synchronized ProjectedCoordinateSystem createProjectedCoordinateSystem(final String code)
//            throws FactoryException
//    {
//        CoordinateSystem returnValue = null;
//        try {
//            final PreparedStatement stmt;
//            stmt = prepareStatement("ProjectedCoordinateSystem", "select DIMENSION,"
//                                       + " CS.COORD_SYS_CODE,"
//                                       + " CRS.COORD_REF_SYS_NAME,"
//                                       + " CRS.SOURCE_GEOGCRS_CODE,"
//                                       + " CO.COORD_OP_NAME,"
//                                       + " COM.COORD_OP_METHOD_NAME,"
//                                       + " CRS.PROJECTION_CONV_CODE,"
//                                       + " CRS.REMARKS"
//                                       + " from [Coordinate Reference System] as CRS,"
//                                       + " [Coordinate System] as CS,"
//                                       + " [Coordinate_Operation] as CO,"
//                                       + " [Coordinate_Operation Method] as COM"
//                                       + " where CRS.COORD_REF_SYS_CODE = ?"
//                                       + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE"
//                                       + " and CO.COORD_OP_CODE = CRS.PROJECTION_CONV_CODE"
//                                       + " and COM.COORD_OP_METHOD_CODE = CO.COORD_OP_METHOD_CODE");
//            stmt.setString(1, code);
//            final ResultSet result = stmt.executeQuery();
//            /*
//             * If the supplied code exists in the database, then we
//             * should find only one record.   However, we will do a
//             * paranoiac check and verify if there is more records.
//             */
//            while (result.next()) {
//                final int          dimension =                  getInt   (result, 1, code);
//                final String    coordSysCode =                  getString(result, 2, code);
//                final String            name =                  getString(result, 3, code);
//                final String     geoCoordSys =                  getString(result, 4, code);
//                final String   operationName =                  getString(result, 5, code);
//                final String  classification = fromEPSGtoOGC   (getString(result, 6, code));
//                final Parameter[] parameters = createParameters(getString(result, 7, code));
//                final String         remarks = result.getString(8);
//                final AxisInfo[]   axisInfos = createAxisInfos(coordSysCode, dimension);
//                final CharSequence       prp = createProperties(name, code, remarks);
//                final ParameterList list = factory.createProjectionParameterList(classification);
//                for (int i=0; i<parameters.length; i++) {
//                    parameters[i].setParameter(list);
//                }
//                final GeographicCoordinateSystem gcs;
//                gcs = createGeographicCoordinateSystem(geoCoordSys);
//                final Ellipsoid e = gcs.getHorizontalDatum().getEllipsoid();
//                if (e != null) {
//                    final Unit unit = e.getAxisUnit();
//                    list.setParameter("semi_major", Unit.METRE.convert(e.getSemiMajorAxis(), unit));
//                    list.setParameter("semi_minor", Unit.METRE.convert(e.getSemiMinorAxis(), unit));
//                }
//                final Projection projection = factory.createProjection(operationName,
//                                                                       classification, list);
//                final Unit unit = createUnitCS(coordSysCode);
//                final CoordinateSystem coordSys;
//                coordSys = factory.createProjectedCoordinateSystem(prp, gcs, projection, unit,
//                                                                   axisInfos[0], axisInfos[1]);
//                returnValue = (CoordinateSystem) ensureSingleton(coordSys, returnValue, code);
//            }
//            result.close();
//        } catch (SQLException exception) {
//            throw new FactoryException(code, exception);
//        }
//        if (returnValue == null) {
//            throw new NoSuchAuthorityCodeException(code);
//        }
//        return (ProjectedCoordinateSystem) returnValue;
//    }

//    /**
//     * Returns the parameter values for an operation method code.
//     *
//     * @param  code The operation code.
//     * @return The parameters.
//     * @throws FactoryException if an access to the database failed.
//     */
//    private ParameterValueGroup createParameters(final String code) throws FactoryException {
//        final List list = new ArrayList();
//        final PreparedStatement stmt;
//        stmt = prepareStatement("Parameters", "SELECT "
//                                       + " COP.PARAMETER_NAME,"
//                                       + " COPV.PARAMETER_VALUE,"
//                                       + " COPV.UOM_CODE"
//                                       + " FROM [Coordinate_Operation Parameter Usage] AS COPU,"
//                                       + " [Coordinate_Operation] AS CO,"
//                                       + " [Coordinate_Operation Parameter] AS COP,"
//                                       + " [Coordinate_Operation Parameter Value] AS COPV"
//                                       + " WHERE CO.COORD_OP_CODE = ?"
//                                       + " AND CO.COORD_OP_METHOD_CODE = COPU.COORD_OP_METHOD_CODE"
//                                       + " AND COP.PARAMETER_CODE = COPU.PARAMETER_CODE"
//                                       + " AND COPV.PARAMETER_CODE = COPU.PARAMETER_CODE"
//                                       + " AND COPV.COORD_OP_CODE = ?"
//                                       + " ORDER BY COPU.SORT_ORDER");
//        stmt.setString(1, code);
//        stmt.setString(2, code);
//        final ResultSet result = stmt.executeQuery();
//        while (result.next()) {
//            final String  name = getString(result, 1, code);
//            final double value = result.getDouble(2);
//            if (result.wasNull()) {
//                /*
//                 * This a temporary hack because sometimes PARAMETER_VALUE is
//                 * not defined, it is replaced by PARAMETER_VALUE_FILE_RE.
//                 */
//                result.close();
//                throw new UnsupportedOperationException("Not yet implemented");
//            }
//            final String  unit = getString(result, 3, code);
//            list.add(new Parameter(name, value, createUnit(unit)));
//        }
//        result.close();
//        return (Parameter[]) list.toArray(new Parameter[list.size()]);
//    }
//    
//    /**
//     * Returns a vertical coordinate system from an EPSG code.
//     *
//     * @param  code Value allocated by authority.
//     * @return The vertical coordinate system object.
//     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
//     * @throws FactoryException if some other kind of failure occured in the backing
//     *         store. This exception usually have {@link SQLException} as its cause.
//     */
//    public synchronized VerticalCoordinateSystem createVerticalCoordinateSystem(final String code)
//            throws FactoryException
//    {
//        VerticalCoordinateSystem returnValue = null;
//        try {
//            final PreparedStatement stmt;
//            stmt = prepareStatement("VerticalCoordinateSystem", "select DIMENSION,"
//                                               + " CS.COORD_SYS_CODE,"
//                                               + " COORD_REF_SYS_NAME,"
//                                               + " DATUM_CODE,"
//                                               + " CRS.REMARKS"
//                                               + " from [Coordinate Reference System] as CRS,"
//                                               + " [Coordinate System] as CS"
//                                               + " where COORD_REF_SYS_CODE = ?"
//                                               + " and CS.COORD_SYS_CODE = CRS.COORD_SYS_CODE");
//            stmt.setString(1, code);
//            final ResultSet result = stmt.executeQuery();
//            /*
//             * If the supplied code exists in the database, then we
//             * should find only one record.   However, we will do a
//             * paranoiac check and verify if there is more records.
//             */
//            while (result.next()) {
//                final int        dimension = getInt   (result, 1, code);
//                final String  coordSysCode = getString(result, 2, code);
//                final String          name = getString(result, 3, code);
//                final String         datum = getString(result, 4, code);
//                final String       remarks = result.getString( 5);
//                final AxisInfo[] axisInfos = createAxisInfos(coordSysCode, dimension);
//                final CharSequence     prp = createProperties(name, code, remarks);
//                final CoordinateSystem  coordSys;
//                coordSys = factory.createVerticalCoordinateSystem(prp,
//                                        createVerticalDatum(datum),
//                                        createUnitCS(coordSysCode), axisInfos[0]);
//                returnValue = (VerticalCoordinateSystem)ensureSingleton(coordSys,returnValue,code);
//            }
//            result.close();
//        } catch (SQLException exception) {
//            throw new FactoryException(code, exception);
//        }
//        if (returnValue == null) {
//            throw new NoSuchAuthorityCodeException(code);
//        }
//        return returnValue;
//    }
//    
//    /**
//     * Create a compound coordinate system from the EPSG code.
//     *
//     * @param code the EPSG code for the CS.
//     * @return the compound CS which value was given.
//     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
//     * @throws FactoryException if some other kind of failure occured in the backing
//     *         store. This exception usually have {@link SQLException} as its cause.
//     */
//    public synchronized CompoundCoordinateSystem createCompoundCoordinateSystem(final String code)
//            throws FactoryException
//    {
//        CompoundCoordinateSystem returnValue = null;
//        try {
//            final PreparedStatement stmt;
//            stmt = prepareStatement("CompoundCoordinateSystem", "select COORD_REF_SYS_NAME,"
//                                               + " COORD_REF_SYS_KIND,"
//                                               + " CMPD_HORIZCRS_CODE,"
//                                               + " CMPD_VERTCRS_CODE,"
//                                               + " REMARKS"
//                                               + " from [Coordinate Reference System]"
//                                               + " where COORD_REF_SYS_CODE = ?");
//            stmt.setString(1, code);
//            final ResultSet result = stmt.executeQuery();
//            while (result.next()) {
//                final String name = getString(result, 1, code);
//                final String type = getString(result, 2, code);
//                if (!type.equalsIgnoreCase("compound")) {
//                    throw new FactoryException(Resources.format(
//                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
//                }
//                final CoordinateSystem  cs1 = createCoordinateReferenceSystem(getString(result, 3, code));
//                final CoordinateSystem  cs2 = createCoordinateReferenceSystem(getString(result, 4, code));
//                final CharSequence      prp = createProperties(name, code, result.getString(5));
//                CompoundCoordinateSystem cs = factory.createCompoundCoordinateSystem(prp, cs1,cs2);
//                returnValue = (CompoundCoordinateSystem) ensureSingleton(cs, returnValue, code);
//            }
//            result.close();
//        }
//        catch (SQLException exception) {
//            throw new FactoryException(code, exception);
//        }
//        if (returnValue == null) {
//            throw new NoSuchAuthorityCodeException(code);
//        }
//        return returnValue; 
//    }

//    /**
//     * Returns the Unit for 1D and 2D coordinate system. This method scan the unit of
//     * all axis for the specified coordinate system. All axis must use the same units.
//     *
//     * @param  code The coordinate system code.
//     * @return The unit.
//     * @throws SQLException if an error occured during database access.
//     * @throws FactoryException if some other errors has occured.
//     */
//    private Unit createUnitCS(final String code) throws SQLException, FactoryException
//    {
//        Unit returnValue = null;
//        final PreparedStatement stmt;
//        // Note: can't use "Unit" key, because it is already used by "createUnit".
//        stmt = prepareStatement("UnitCS", "select UOM_CODE"
//                                          + " from [Coordinate Axis]"
//                                          + " where COORD_SYS_CODE = ?");
//        stmt.setString(1, code);
//        final ResultSet result = stmt.executeQuery();
//        while (result.next()) {
//            final Unit unit = createUnit(getString(result, 1, code));
//            returnValue = (Unit) ensureSingleton(unit, returnValue, code);
//        }
//        result.close();
//        if (returnValue == null) {
//            throw new NoSuchAuthorityCodeException(code);
//        }
//        return replaceAxisUnit(returnValue);
//    }

    /**
     * Invoked when a new {@link PreparedStatement} is about to be created from a SQL string.
     * Since the <A HREF="http://www.epsg.org">EPSG database</A> is available mainly in MS-Access
     * format, SQL statements are formatted using some syntax specific to this particular database
     * software (for example "<code>SELECT * FROM [Coordinate Reference System]</code>"). If the
     * EPSG database is ported to an other software, then this method should be overriden in order
     * to adapt the SQL syntax. For example a subclass connecting to a
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
     * Called when this factory is added to the given <code>category</code> of the given
     * <code>registry</code>  The object may already be registered under another category
     * or categories.
     * <br><br>
     * This method is invoked automatically when this factory is registered as a plugin,
     * and should not be invoked directly by the user.
     *
     * @param registry a <code>ServiceRegistry</code> where this factory has been registered.
     * @param category a <code>Class</code> object indicating the registry category under which
     *                 this object has been registered.
     *
     * @see org.geotools.referencing.FactoryFinder
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        super.onRegistration(registry, category);
        isService = true;
        Runtime.getRuntime().addShutdownHook(new Thread("EPSG factory shutdown") {
            public void run() {
                isService = false; // Instructs 'dispose()' to close the connection.
                try {
                    dispose();
                } catch (FactoryException exception) {
                    // Too late to log the error, since we are exiting the JVM. Ignore...
                }
            }
        });
    }

    /**
     * Dispose any resources hold by this object.
     *
     * @throws FactoryException if an error occured while closing the connection.
     */
    public synchronized void dispose() throws FactoryException {
        try {
            for (final Iterator it=statements.values().iterator(); it.hasNext();) {
                ((PreparedStatement) it.next()).close();
                it.remove();
            }
            if (!isService) {
                connection.close();
            }
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        super.dispose();
    }

    /**
     * Invokes {@link #dispose} when this factory is garbage collected.
     *
     * @throws Throwable if an error occured while closing the connection.
     */
    protected final void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * Constructs an object from the EPSG database and print its WKT (Well Know Text) to
     * the standard output. This method can be invoked from the command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.referencing.epsg.DefaultFactory 4181
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
     *       and will become the default URL every time an <code>DefaultFactory</code>
     *       is created without explicit URL. The "<code>default</code>" string reset the default
     *       URL.
     *       <br><br>
     *
     *   <strong><code>-driver</code></strong><br>
     *       Set the driver class. The default value is <code>sun.jdbc.odbc.JdbcOdbcDriver</code>.
     *       The specified classname is stored in system preferences and will become the default
     *       driver every time an <code>DefaultFactory</code> is created without explicit
     *       driver. The "<code>default</code>" string reset the default driver.
     *       <br><br>
     *
     *   <strong><code>-encoding</code></strong><br>
     *       Set the console encoding for this application output.
     *       This value has no impact on <code>DefaultFactory</code> behavior.
     * </blockquote>
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of code can be specified on the command line.
     */
    public static void main(String [] args) {
        MonolineFormatter.initGeotools(); // Use custom logger.
        final Arguments arguments = new Arguments(args);
        final PrintWriter     out = arguments.out;
        final String       driver = arguments.getOptionalString("-driver");
        final String   connection = arguments.getOptionalString("-connection");
        final Preferences   prefs = Preferences.systemNodeForPackage(DefaultFactory.class);
        if (driver != null) {
            if (driver.equalsIgnoreCase("default")) {
                prefs.remove(DRIVER);
            } else {
                prefs.put(DRIVER, driver);
            }
        }
        if (connection != null) {
            if (connection.equalsIgnoreCase("default")) {
                prefs.remove(CONNECTION);
            } else {
                prefs.put(CONNECTION, connection);
            }
        }
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        try {
            DefaultFactory factory = null;
            try {
                factory = new DefaultFactory();
                for (int i=0; i<args.length; i++) {
                    out.println(factory.createObject(args[i]));
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
