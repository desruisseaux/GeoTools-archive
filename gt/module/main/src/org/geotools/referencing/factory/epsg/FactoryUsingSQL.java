/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.factory.epsg;

// J2SE dependencies and extensions
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import javax.units.NonSI;
import javax.units.Unit;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.metadata.Identifier;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.measure.Units;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.projection.MapProjection;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.util.LocalName;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.ScopedName;


/**
 * Default implementation for a coordinate reference system factory backed by the EPSG database.
 * The EPSG database is freely available at <A HREF="http://www.epsg.org">http://www.epsg.org</a>.
 * Current version of this class requires EPSG database version 6.6 or above.
 * <p>
 * This factory doesn't cache any result. Any call to a {@code createFoo} method will send a new
 * query to the EPSG database. For caching, this factory should be wrapped in some buffered factory
 * like {@link DefaultFactory}.
 * <p>
 * This factory accepts names as well as numerical identifiers. For example
 * "<cite>NTF (Paris) / France I</cite>" and {@code "27581"} both fetchs the same object.
 * However, names may be ambiguous since the same name may be used for more than one object.
 * This is the case of "WGS 84" for example. If such an ambiguity is found, an exception
 * will be thrown. If names are not wanted as a legal EPSG code, subclasses can override the
 * {@link #isPrimaryKey} method.
 * <p>
 * This factory uses the MS-Access dialect of SQL, because the primary distribution format for the
 * EPSG database is MS-Access. For translating this SQL dialect into an other one, subclasses
 * should override the {@link #adaptSQL} method.
 *
 * @version $Id$
 * @author Yann Cézard
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 * @author Matthias Basler
 *
 * @since 2.1
 */
public class FactoryUsingSQL extends AbstractAuthorityFactory {
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                            ////////
    ////////      H A R D   C O D E D   V A L U E S    (other than SQL statements)      ////////
    ////////                                                                            ////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a hard-coded unit from an EPSG code. We do not need to provide all units here,
     * but we must at least provide all base units declared in the [TARGET_UOM_CODE] column
     * of table [Unit of Measure]. Other units will be derived automatically if they are not
     * listed here.
     *
     * @param  code The code.
     * @return The unit, or {@code null} if the code is unrecognized.
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
     * This table is used by the {@link #createObject} method in order to detect
     * which of the following methods should be invoked for a given code:
     *
     * {@link #createCoordinateReferenceSystem}
     * {@link #createCoordinateSystem}
     * {@link #createDatum}
     * {@link #createEllipsoid}
     * {@link #createUnit}
     *
     * The order is significant: it is the key for a {@code switch} statement.
     *
     * @see #createObject
     * @see #lastObjectType
     */
    private static final TableInfo[] TABLES_INFO = {
        new TableInfo(CoordinateReferenceSystem.class,
                      "[Coordinate Reference System]",
                      "COORD_REF_SYS_CODE",
                      "COORD_REF_SYS_NAME",
                      "COORD_REF_SYS_KIND",
         new Class[] { ProjectedCRS.class, GeographicCRS.class, GeocentricCRS.class},
         new String[] {"projected",       "geographic",        "geocentric"}),

        new TableInfo(CoordinateSystem.class,
                      "[Coordinate System]",
                      "COORD_SYS_CODE",
                      "COORD_SYS_NAME",
                      "COORD_SYS_TYPE",
         new Class[]  { CartesianCS.class, EllipsoidalCS.class, SphericalCS.class, VerticalCS.class},
         new String[] {"Cartesian",       "ellipsoidal",       "spherical",       "gravity-related"}),

        new TableInfo(CoordinateSystemAxis.class,
                      "[Coordinate Axis]",
                      "COORD_AXIS_CODE",
                      null, null, null, null),

        new TableInfo(Datum.class,
                      "[Datum]",
                      "DATUM_CODE",
                      "DATUM_NAME",
                      "DATUM_TYPE",
         new Class[] { GeodeticDatum.class, VerticalDatum.class, EngineeringDatum.class},
         new String[]{"geodetic",          "vertical",          "engineering"}),

        new TableInfo(Ellipsoid.class,
                      "[Ellipsoid]",
                      "ELLIPSOID_CODE",
                      "ELLIPSOID_NAME",
                      null, null, null),
        new TableInfo(PrimeMeridian.class,
                      "[Prime Meridian]",
                      "PRIME_MERIDIAN_CODE",
                      "PRIME_MERIDIAN_NAME",
                      null, null, null),

        new TableInfo(CoordinateOperation.class,
                      "[Coordinate_Operation]",
                      "COORD_OP_CODE",
                      "COORD_OP_NAME",
                      "COORD_OP_TYPE",
         new Class[] { Conversion.class, Transformation.class},
         new String[]{"conversion",     "transformation"}),

        new TableInfo(OperationMethod.class,
                      "[Coordinate_Operation Method]",
                      "COORD_OP_METHOD_CODE",
                      "COORD_OP_METHOD_NAME",
                      null, null, null)
    };

    ///////////////////////////////////////////////////////////////////////////////
    ////////                                                               ////////
    ////////        E N D   O F   H A R D   C O D E D   V A L U E S        ////////
    ////////                                                               ////////
    ////////    NOTE: 'createFoo(...)' methods may still have hard-coded   ////////
    ////////    values (others than SQL statements) in 'equalsIgnoreCase'  ////////
    ////////    expressions.                                               ////////
    ///////////////////////////////////////////////////////////////////////////////
    /**
     * The name of the thread to execute at JVM shutdown. This thread will be created
     * by {@link DefaultFactory} on registration. It will be checked by {@link #dispose}
     * in order to determine if we are in the process for shutting down the database engine.
     */
    static final String SHUTDOWN_THREAD = "EPSG factory shutdown";

    /**
     * The authority for this database. Will be created only when first needed.
     * This authority will contains the database version in the {@linkplain Citation#getEdition
     * edition} attribute, together with the {@linkplain Citation#getEditionDate edition date}.
     */
    private transient Citation authority;

    /**
     * Last object type returned by {@link #createObject}, or -1 if none.
     * This type is an index in the {@link #TABLES_INFO} array and is
     * strictly for {@link #createObject} internal use.
     */
    private int lastObjectType = -1;

    /**
     * The last table in which object name were looked for. This is for internal use
     * by {@link #getNumericalIdentifier} only.
     */
    private transient String lastTableForName;

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
     * The set of authority codes for different types. Keys are {@link Class} or
     * {@link String} objects. This map is used by the {@link #getAuthorityCodes}
     * method. Note that this factory can't be disposed as long as some cached sets
     * are in use (i.e. as long as this map is not empty). This is why a weak value
     * map is mandatory here. The {@link AuthorityCodes#finalize} methods take care
     * of closing the stamenents used by the sets.
     */
    private final Map/*<Object,Reference<AuthorityCodes>>*/ authorityCodes = new HashMap();

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
     * The prefix to prepend to each properties, or an empty buffer if none.
     * This is used by {@code createProperties} internal methods.
     */
    private final StringBuffer prefix = new StringBuffer();

    /**
     * A safety guard for preventing never-ending loops in recursive calls to
     * {@link #createDatum}. This is used by {@link #createBursaWolfParameters},
     * which need to create a target datum. The target datum could have its own
     * Bursa-Wolf parameters, with one of them pointing again to the source datum.
     */
    private final Set safetyGuard = new HashSet();

    /**
     * The buffered authority factory, or {@code null} if none. This field is set
     * to a different value by {@link DefaultFactory} only, which will point toward a
     * buffered factory wrapping this {@code FactoryUsingSQL} for efficienty.
     */
    AbstractAuthorityFactory buffered = this;

    /**
     * The connection to the EPSG database.
     */
    protected final Connection connection;

    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param factories  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     */
    public FactoryUsingSQL(final FactoryGroup factories, final Connection connection) {
        super(factories, MAXIMUM_PRIORITY-20);
        this.connection = connection;
        ensureNonNull("connection", connection);
    }

    /**
     * Returns the authority for this EPSG database.
     * This authority will contains the database version in the {@linkplain Citation#getEdition
     * edition} attribute, together with the {@linkplain Citation#getEditionDate edition date}.
     *
     * @todo Localize the alternate title.
     */
    public synchronized Citation getAuthority() {
        if (authority == null) try {
            final String query = adaptSQL("SELECT VERSION_NUMBER, VERSION_DATE FROM [Version History]" +
                                          " ORDER BY VERSION_DATE DESC");
            final DatabaseMetaData metadata  = connection.getMetaData();
            final Statement        statement = connection.createStatement();
            final ResultSet        result    = statement.executeQuery(query);
            if (result.next()) {
                final String version = result.getString(1);
                final Date   date    = result.getDate  (2);
                final String engine  = metadata.getDatabaseProductName();
                final CitationImpl c = new CitationImpl(CitationImpl.EPSG);
                c.getAlternateTitles().add(new SimpleInternationalString(
                        "EPSG database version "+version+" on "+engine));
                c.setEdition(new SimpleInternationalString(version));
                c.setEditionDate(date);
                authority = c;
            } else {
                authority = CitationImpl.EPSG;
            }
            result.close();
            statement.close();
        } catch (SQLException exception) {
            Utilities.unexpectedException(LOGGER.getName(), "FactoryUsingSQL", "getAuthority", exception);
            return CitationImpl.EPSG;
        }
        return authority;
    }

    /**
     * Returns a description of the database engine.
     *
     * @throws FactoryException if the database's metadata can't be fetched.
     *
     * @todo localize
     */
    public synchronized String getBackingStoreDescription() throws FactoryException {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer  buffer = new StringBuffer();
        final Citation   authority = getAuthority();
        CharSequence s;
        if ((s=authority.getEdition()) != null) {
            buffer.append("EPSG version:    ");
            buffer.append(s);
            buffer.append(lineSeparator);
        }
        try {
            final DatabaseMetaData metadata = connection.getMetaData();
            if ((s=metadata.getDatabaseProductName()) != null) {
                buffer.append("Database engine: ");
                buffer.append(s);
                if ((s=metadata.getDatabaseProductVersion()) != null) {
                    buffer.append(" version ");
                    buffer.append(s);
                }
                buffer.append(lineSeparator);
            }
            if ((s=metadata.getURL()) != null) {
                buffer.append("Database URL:    ");
                buffer.append(s);
                buffer.append(lineSeparator);
            }
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        return buffer.toString();
    }

    /**
     * Returns the set of authority codes of the given type.
     * <p>
     * <strong>NOTE:</strong> This method returns a living connection to the underlying database.
     * This means that the returned set can executes efficiently idioms like the following one:
     *
     * <blockquote>
     * <pre>getAuthorityCodes(<var>type</var).containsAll(<var>others</var>)</pre>
     * </blockquote>
     *
     * But do not keep the returned reference for a long time. The returned set should stay valid
     * even if retained for a long time (as long as this factory has not been {@linkplain #dispose
     * disposed}), but the existence of those long-living connections may prevent this factory to
     * release some resources. If the set of codes is needed for a long time, copy their values in
     * an other collection object.
     *
     * @param  type The spatial reference objects type (may be {@code Object.class}).
     * @return The set of authority codes for spatial reference objects of the given type.
     *         If this factory doesn't contains any object of the given type, then this method
     *         returns an {@linkplain java.util.Collections#EMPTY_SET empty set}.
     * @throws FactoryException if access to the underlying database failed.
     *
     * @since 2.2
     */
    public Set/*<String>*/ getAuthorityCodes(final Class type) throws FactoryException {
        return getAuthorityCodes0(type);
    }

    /**
     * Implementation of {@link #getAuthorityCodes0} as a private method, for protecting
     * {@link #getDescriptionText} for user method overriding.
     */
    private synchronized Set/*<String>*/ getAuthorityCodes0(final Class type) throws FactoryException {
        Reference reference;
        AuthorityCodes candidate;
        /*
         * If the set were already requested previously for the given type, returns it.
         * Otherwise, a new one will be created (but will not use the database connection yet).
         */
        reference = (Reference) authorityCodes.get(type);
        candidate = (reference!=null) ? (AuthorityCodes) reference.get() : null;
        if (candidate != null) {
            return candidate;
        }
        for (int i=0; i<TABLES_INFO.length; i++) {
            final TableInfo table = TABLES_INFO[i];
            if (table.type.isAssignableFrom(type)) {
                /*
                 * Maybe an instance already existed but was not found above because the user
                 * specified some implementation class instead of an interface class. Before
                 * to return the newly created set, check again in the cached sets using the
                 * SQL statement as a key instead of the type. Two sets with identical SQL
                 * statements are actually identical collections even if the user-specified
                 * type is different.
                 */
                final AuthorityCodes codes;
                codes = new AuthorityCodes(connection, TABLES_INFO[i], type, this);
                reference = (Reference) authorityCodes.get(codes.sqlAll);
                candidate = (reference!=null) ? (AuthorityCodes) reference.get() : null;
                if (candidate == null) {
                    candidate = codes;
                } else if (reference instanceof SoftReference) {
                    // No need to update the reference.
                    return candidate;
                }
                reference = new SoftReference(candidate);
                authorityCodes.put(codes.type,   reference);
                authorityCodes.put(codes.sqlAll, reference);
                return candidate;
            }
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @since 2.2
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or {@code null} if the object
     *         corresponding to the specified {@code code} has no description.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the query failed for some other reason.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        for (int i=0; i<TABLES_INFO.length; i++) {
            final Set codes = getAuthorityCodes0(TABLES_INFO[i].type);
            if (codes instanceof AuthorityCodes) {
                final String text = (String) ((AuthorityCodes) codes).asMap().get(code);
                if (text != null) {
                    return new SimpleInternationalString(text);
                }
            }
        }
        /*
         * Maybe the user overrided some object creation
         * methods with a value for the supplied code.
         */
        final Identifier identifier = createObject(code).getName();
        if (identifier instanceof GenericName) {
            return ((GenericName) identifier).toInternationalString();
        }
        return new SimpleInternationalString(identifier.getCode());
    }

    /**
     * Returns a prepared statement for the specified name. Most {@link PreparedStatement}
     * creations are performed through this method, except {@link #getNumericalIdentifier}
     * and {@link #createObject}.
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
     * Converts a code from an arbitrary name to the numerical identifier (the primary key).
     * If the supplied code is already a numerical value, then it is returned unchanged.
     * If the code is not found in the name column, it is returned unchanged as well so that
     * the caller will produces an appropriate "Code not found" error message. If the code
     * is found more than once, then an exception is thrown.
     * <p>
     * Note that this method includes a call to {@link #trimAuthority}, so there is no need to
     * call it before or after this method.
     *
     * @param  code       The code to check.
     * @param  table      The table where the code should appears.
     * @param  codeColumn The column name for the code.
     * @param  nameColumn The column name for the name.
     * @return The numerical identifier (i.e. the table primary key value).
     * @throws SQLException if an error occured while reading the database.
     */
    private String toPrimaryKey(final String code,
                                final String table,
                                final String codeColumn,
                                final String nameColumn)
            throws SQLException, FactoryException
    {
        assert Thread.holdsLock(this);
        final String epsg = trimAuthority(code);
        if (!isPrimaryKey(epsg)) {
            /*
             * The character is not the numerical code. Search the value in the database.
             * If a prepared statement is already available, reuse it providing that it was
             * created for the current table. Otherwise, we will create a new statement.
             */
            final String KEY = "NumericalIdentifier";
            PreparedStatement statement = (PreparedStatement) statements.get(KEY);
            if (statement != null) {
                if (!table.equals(lastTableForName)) {
                    statements.remove(KEY);
                    statement.close();
                    statement        = null;
                    lastTableForName = null;
                }
            }
            if (statement == null) {
                final String query = "SELECT " + codeColumn + " FROM " + table +
                                     " WHERE " + nameColumn + " = ?";
                statement = connection.prepareStatement(adaptSQL(query));
                statements.put(KEY, statement);
            }
            statement.setString(1, epsg);
            String identifier = null;
            final ResultSet result = statement.executeQuery();
            while (result.next()) {
                identifier = (String) ensureSingleton(result.getString(1), identifier, code);
            }
            result.close();
            if (identifier != null) {
                return identifier;
            }
        }
        return epsg;
    }

    /**
     * Make sure that an object constructed from the database is not incoherent.
     * If the code supplied to a {@code createFoo} method exists in the database,
     * then we should find only one record. However, we will do a paranoiac check and
     * verify if there is more records, using a {@code while (results.next())}
     * loop instead of {@code if (results.next())}. This method is invoked in
     * the loop for making sure that, if there is more than one record (which should
     * never happen), at least they have identical contents.
     *
     * @param  newValue The newly constructed object.
     * @param  oldValue The object previously constructed, or {@code null} if none.
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
     * Prepend the prefix to the specified key, if needed.
     * This is used by {@code createProperties} internal methods.
     */
    private String prepend(String key) {
        final int base = prefix.length();
        if (base != 0) {
            prefix.append(key);
            key = prefix.toString();
            prefix.setLength(base);
        }
        return key;
    }

    /**
     * Returns the name for the {@link IdentifiedObject} to construct.
     * This method also search for alias.
     *
     * @param  name The name for the {@link IndentifiedObject} to construct.
     * @param  code The EPSG code of the object to construct.
     * @param  remarks Remarks, or {@code null} if none.
     * @return The name together with a set of properties.
     */
    private Map createProperties(final String name, final String code, String remarks)
            throws SQLException, FactoryException
    {
        final Citation authority  = getAuthority();
        if (prefix.length() == 0) {
            // Do not clear if we are adding "conversion.XXX"
            // properties to the existing set.
            properties.clear();
        }
        if (name != null) {
            properties.put(prepend(IdentifiedObject.NAME_KEY),
                           new NamedIdentifier(authority, name.trim()));
        }
        if (code != null) {
            properties.put(prepend(IdentifiedObject.IDENTIFIERS_KEY),
                           new NamedIdentifier(authority, code.trim()));
        }
        if (remarks!=null && (remarks=remarks.trim()).length()!=0) {
            properties.put(prepend(IdentifiedObject.REMARKS_KEY), remarks);
        }
        /*
         * Search for alias.
         */
        List alias = null;
        final PreparedStatement stmt;
        stmt = prepareStatement("Alias", "SELECT NAMING_SYSTEM_NAME, ALIAS"
                                       + " FROM [Alias] INNER JOIN [Naming System]"
                                       +   " ON [Alias].NAMING_SYSTEM_CODE ="
                                       +      " [Naming System].NAMING_SYSTEM_CODE"
                                       + " WHERE OBJECT_CODE = ?");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final String scope = result.getString(1);
            final String local = getString(result, 2, code);
            final GenericName generic;
            if (scope == null) {
                generic = new LocalName(local);
            } else {
                LocalName cached = (LocalName) scopes.get(scope);
                if (cached == null) {
                    cached = new LocalName(scope);
                    scopes.put(scope, cached);
                }
                generic = new ScopedName(cached, local);
            }
            if (alias == null) {
                alias = new ArrayList();
            }
            alias.add(generic);
        }
        result.close();
        if (alias != null) {
            properties.put(prepend(IdentifiedObject.ALIAS_KEY),
                           (GenericName[]) alias.toArray(new GenericName[alias.size()]));
        }
        return properties;
    }

    /**
     * Returns the name for the {@link IdentifiedObject} to construct.
     * This method also search for alias.
     *
     * @param  name  The name for the {@link IndentifiedObject} to construct.
     * @param  code  The EPSG code of the object to construct.
     * @param  area  The area of use, or {@code null} if none.
     * @param  scope The scope, or {@code null} if none.
     * @param  remarks Remarks, or {@code null} if none.
     * @return The name together with a set of properties.
     */
    private Map createProperties(final String name, final String code,
                                 String area, String scope, String remarks)
            throws SQLException, FactoryException
    {
        final Map properties = createProperties(name, code, remarks);
        if (area != null  &&  (area=area.trim()).length() != 0) {
            final Extent extent = buffered.createExtent(area);
            properties.put(prepend(Datum.VALID_AREA_KEY), extent);
        }
        if (scope != null &&  (scope=scope.trim()).length() != 0) {
            properties.put(prepend(Datum.SCOPE_KEY), scope);
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
        ensureNonNull("code", code);
        final String       KEY = "IdentifiedObject";
        PreparedStatement stmt = (PreparedStatement) statements.get(KEY); // Null allowed.
        StringBuffer     query = null; // Will be created only if the last statement doesn't suit.
        /*
         * Iterates through all tables listed in TABLES_INFO, starting with the table used during
         * the last call to 'createObject(code)'.  This approach assumes that two consecutive calls
         * will often return the same type of object.  If the object type changed, then this method
         * will have to discard the old prepared statement and prepare a new one, which may be a
         * costly operation. Only the last successful prepared statement is cached, in order to keep
         * the amount of statements low. Unsuccessful statements are immediately disposed.
         */
        final String  epsg         = trimAuthority(code);
        final boolean isPrimaryKey = isPrimaryKey(epsg);
        final int     tupleToSkip  = isPrimaryKey ? lastObjectType : -1;
        int index = -1;
        for (int i=-1; i<TABLES_INFO.length; i++) {
            if (i == tupleToSkip) {
                // Avoid to test the same table twice.  Note that this test also avoid a
                // NullPointerException if 'stmt' is null, since 'lastObjectType' should
                // be -2 in this case.
                continue;
            }
            try {
                if (i >= 0) {
                    final TableInfo table = TABLES_INFO[i];
                    final String column = isPrimaryKey ? table.codeColumn : table.nameColumn;
                    if (column == null) {
                        continue;
                    }
                    if (query == null) {
                        query = new StringBuffer("SELECT ");
                    }
                    query.setLength(7); // 7 is the length of "SELECT " in the line above.
                    query.append(table.codeColumn);
                    query.append(" FROM ");
                    query.append(table.table);
                    query.append(" WHERE ");
                    query.append(column);
                    query.append(" = ?");
                    if (isPrimaryKey) {
                        assert !statements.containsKey(KEY) : table;
                        stmt = prepareStatement(KEY, query.toString());
                    } else {
                        // Do not cache the statement for names.
                        stmt = connection.prepareStatement(adaptSQL(query.toString()));
                    }
                }
                /*
                 * Checks if at least one record if found for the code. If the code is the primary
                 * key, then we will stop at the first table found since a well-formed EPSG database
                 * should not contains any duplicate identifiers. In the code is a name, then search
                 * in all tables since duplicate names exist.
                 */
                stmt.setString(1, epsg);
                final ResultSet result = stmt.executeQuery();
                final boolean  present = result.next();
                result.close();
                if (present) {
                    if (index >= 0) {
                        throw new FactoryException(Resources.format(
                                ResourceKeys.ERROR_DUPLICATED_VALUES_$1, code));
                    }
                    index = (i < 0) ? lastObjectType : i;
                    if (isPrimaryKey) {
                        lastObjectType = index;
                        break;
                    }
                }
                if (isPrimaryKey) {
                    statements.remove(KEY);
                }
                stmt.close();
            } catch (SQLException exception) {
                throw databaseFailure(IdentifiedObject.class, code, exception);
            }
        }
        /*
         * If a record has been found in one table, then delegate to the appripriate method.
         */
        if (index >= 0) {
            switch (index) {
                case 0:  return buffered.createCoordinateReferenceSystem(code);
                case 1:  return buffered.createCoordinateSystem         (code);
                case 2:  return buffered.createCoordinateSystemAxis     (code);
                case 3:  return buffered.createDatum                    (code);
                case 4:  return buffered.createEllipsoid                (code);
                case 5:  return buffered.createPrimeMeridian            (code);
                case 6:  break; // TODO return buffered.createCoordinateOperation      (code);
                case 7:  break; // TODO return buffered.createOperationMethod          (code);
                default: throw new AssertionError(index); // Should not happen
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
        ensureNonNull("code", code);
        Unit returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Unit of Measure]", "UOM_CODE", "UNIT_OF_MEAS_NAME");
            final PreparedStatement stmt;
            stmt = prepareStatement("Unit", "SELECT UOM_CODE,"
                                          +       " FACTOR_B,"
                                          +       " FACTOR_C,"
                                          +       " TARGET_UOM_CODE"
                                          + " FROM [Unit of Measure]"
                                          + " WHERE UOM_CODE = ?");
            stmt.setString(1, primaryKey);
            final ResultSet result = stmt.executeQuery();
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
            throw databaseFailure(Unit.class, code, exception);
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
        ensureNonNull("code", code);
        Ellipsoid returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Ellipsoid]", "ELLIPSOID_CODE", "ELLIPSOID_NAME");
            final PreparedStatement stmt;
            stmt = prepareStatement("Ellipsoid", "SELECT ELLIPSOID_CODE,"
                                               +       " ELLIPSOID_NAME,"
                                               +       " SEMI_MAJOR_AXIS,"
                                               +       " INV_FLATTENING,"
                                               +       " SEMI_MINOR_AXIS,"
                                               +       " UOM_CODE,"
                                               +       " REMARKS"
                                               + " FROM [Ellipsoid]"
                                               + " WHERE ELLIPSOID_CODE = ?");
            stmt.setString(1, primaryKey);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                /*
                 * One of 'semiMinorAxis' and 'inverseFlattening' values can be NULL in
                 * the database. Consequently, we don't use 'getString(ResultSet, int)'
                 * because we don't want to thrown an exception if a NULL value is found.
                 */
                final String epsg              = getString(result, 1, code);
                final String name              = getString(result, 2, code);
                final double semiMajorAxis     = getDouble(result, 3, code);
                final double inverseFlattening = result.getDouble( 4);
                final double semiMinorAxis     = result.getDouble( 5);
                final String unitCode          = getString(result, 6, code);
                final String remarks           = result.getString( 7);
                final Unit   unit              = buffered.createUnit(unitCode);
                final Map    properties        = createProperties(name, epsg, remarks);
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
                        LOGGER.warning(Resources.format(ResourceKeys.WARNING_AMBIGUOUS_ELLIPSOID));
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
            throw databaseFailure(Ellipsoid.class, code, exception);
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
        ensureNonNull("code", code);
        PrimeMeridian returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Prime Meridian]", "PRIME_MERIDIAN_CODE", "PRIME_MERIDIAN_NAME");
            final PreparedStatement stmt;
            stmt = prepareStatement("PrimeMeridian", "SELECT PRIME_MERIDIAN_CODE,"
                                                   +       " PRIME_MERIDIAN_NAME,"
                                                   +       " GREENWICH_LONGITUDE,"
                                                   +       " UOM_CODE,"
                                                   +       " REMARKS"
                                                   + " FROM [Prime Meridian]"
                                                   + " WHERE PRIME_MERIDIAN_CODE = ?");
            stmt.setString(1, primaryKey);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String epsg      = getString(result, 1, code);
                final String name      = getString(result, 2, code);
                final double longitude = getDouble(result, 3, code);
                final String unit_code = getString(result, 4, code);
                final String remarks   = result.getString( 5);
                final Unit unit        = buffered.createUnit(unit_code);
                final Map properties   = createProperties(name, epsg, remarks);
                PrimeMeridian primeMeridian = factories.getDatumFactory().createPrimeMeridian(
                                              properties, longitude, unit);
                returnValue = (PrimeMeridian) ensureSingleton(primeMeridian, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw databaseFailure(PrimeMeridian.class, code, exception);
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
        ensureNonNull("code", code);
        Extent returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Area]", "AREA_CODE", "AREA_NAME");
            final PreparedStatement stmt;
            stmt = prepareStatement("Area", "SELECT AREA_OF_USE,"
                                          +       " AREA_SOUTH_BOUND_LAT,"
                                          +       " AREA_NORTH_BOUND_LAT,"
                                          +       " AREA_WEST_BOUND_LON,"
                                          +       " AREA_EAST_BOUND_LON"
                                          + " FROM [Area]"
                                          + " WHERE AREA_CODE = ?");
            stmt.setString(1, primaryKey);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                ExtentImpl extent = null;
                final String description = result.getString(1);
                if (description != null) {
                    extent = new ExtentImpl();
                    extent.setDescription(new SimpleInternationalString(description));
                }
                final double ymin = result.getDouble(2);
                if (!result.wasNull()) {
                    final double ymax = result.getDouble(3);
                    if (!result.wasNull()) {
                        final double xmin = result.getDouble(4);
                        if (!result.wasNull()) {
                            final double xmax = result.getDouble(5);
                            if (!result.wasNull()) {
                                if (extent == null) {
                                    extent = new ExtentImpl();
                                }
                                extent.setGeographicElements(Collections.singleton(
                                        new GeographicBoundingBoxImpl(xmin, xmax, ymin, ymax)));
                            }
                        }
                    }
                }
                if (extent != null) {
                    returnValue = (Extent) ensureSingleton(extent.unmodifiable(), returnValue, code);
                }
            }
            result.close();
        } catch (SQLException exception) {
            throw databaseFailure(Extent.class, code, exception);
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
     * no conversion informations, then this method will returns {@code null}.
     *  
     * @param  code The EPSG code of the {@link GeodeticDatum}.
     * @param  toClose The result set to close if this method is going to invokes
     *         {@link #createDatum} recursively. This hack is necessary because many
     *         JDBC drivers do not support multiple result sets for the same statement.
     *         The result set is closed if an only if this method returns a non-null value.
     * @return an array of Bursa-Wolf parameters (in which case {@code toClose} has
     *         been closed), or {@code null} (in which case {@code toClose} has
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
                                         "SELECT MIN(CO.COORD_OP_CODE),"
                                 +             " MIN(CO.COORD_OP_METHOD_CODE),"
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
                datum = buffered.createGeodeticDatum(info.target);
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
                  buffered.createUnit(getString(result, 3, info.operation)));
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
     *       {@link VerticalDatumType#GEOIDAL}. We don't know yet how
     *       to maps the exact vertical datum type from the EPSG database.
     */
    public synchronized Datum createDatum(final String code) throws FactoryException {
        ensureNonNull("code", code);
        Datum returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Datum]", "DATUM_CODE", "DATUM_NAME");
            final PreparedStatement stmt;
            stmt = prepareStatement("Datum", "SELECT DATUM_CODE,"
                                           +       " DATUM_NAME,"
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
            stmt.setString(1, primaryKey);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String epsg    = getString(result, 1, code);
                final String name    = getString(result, 2, code);
                final String type    = getString(result, 3, code);
                final String anchor  = result.getString( 4);
                final int    epoch   = result.getInt   ( 5);
                final String area    = result.getString( 6);
                final String scope   = result.getString( 7);
                final String remarks = result.getString( 8);
                Map properties = createProperties(name, epsg, area, scope, remarks);
                if (anchor != null) {
                    properties.put(Datum.ANCHOR_POINT_KEY, anchor);
                }
                if (epoch != 0) {
                    calendar.clear();
                    calendar.set(epoch, 0, 1);
                    properties.put(Datum.REALIZATION_EPOCH_KEY, calendar.getTime());
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
                if (type.equalsIgnoreCase("geodetic")) {
                    properties = new HashMap(properties); // Protect from changes
                    final Ellipsoid         ellipsoid = buffered.createEllipsoid    (getString(result,  9, code));
                    final PrimeMeridian      meridian = buffered.createPrimeMeridian(getString(result, 10, code));
                    final BursaWolfParameters[] param = createBursaWolfParameters(code, result);
                    if (param != null) {
                        result = null; // Already closed by createBursaWolfParameters
                        properties.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, param);
                    }
                    datum = factory.createGeodeticDatum(properties, ellipsoid, meridian);
                } else if (type.equalsIgnoreCase("vertical")) {
                    // TODO: Find the right datum type.
                    datum = factory.createVerticalDatum(properties, VerticalDatumType.GEOIDAL);
                } else if (type.equalsIgnoreCase("engineering")) {
                    datum = factory.createEngineeringDatum(properties);
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                returnValue = (Datum) ensureSingleton(datum, returnValue, code);
                if (result == null) {
                    // Bypass the 'result.close()' line below:
                    // the ResultSet has already been closed.
                    return returnValue;
                }
            }
            result.close();
        } catch (SQLException exception) {
            throw databaseFailure(Datum.class, code, exception);
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
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @todo Not yet implemented.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        ensureNonNull("code", code);
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
            AxisDirection direction;
            try {
                direction = DefaultCoordinateSystemAxis.getDirection(orientation);
            } catch (NoSuchElementException exception) {
                if (orientation.equalsIgnoreCase("Geocentre > equator/PM")) {
                    direction = AxisDirection.OTHER; // TODO: can we choose a more accurate direction?
                } else if (orientation.equalsIgnoreCase("Geocentre > equator/90dE")) {
                    direction = AxisDirection.EAST;
                } else if (orientation.equalsIgnoreCase("Geocentre > north pole")) {
                    direction = AxisDirection.NORTH;
                } else {
                    throw new FactoryException(Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1,
                                                                orientation), exception);
                }
            }
            if (description == null) {
                description = remarks;
            } else if (remarks != null) {
                description += System.getProperty("line.separator", "\n") + remarks;
            }
            final Map properties = createProperties(name, code, description);
            axis[i++] = factory.createCoordinateSystemAxis(properties, abbreviation, direction,
                                                           buffered.createUnit(unit));
        }
        result.close();
        if (i != axis.length) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                       new Integer(axis.length), new Integer(i)));
        }
        return axis;
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
        ensureNonNull("code", code);
        CoordinateSystem returnValue = null;
        final PreparedStatement stmt;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Coordinate System]", "COORD_SYS_CODE", "COORD_SYS_NAME");
            stmt = prepareStatement("CoordinateSystem", "SELECT COORD_SYS_CODE,"
                                                      +       " COORD_SYS_NAME,"
                                                      +       " COORD_SYS_TYPE,"
                                                      +       " DIMENSION,"
                                                      +       " REMARKS"
                                                      + " FROM [Coordinate System]"
                                                      + " WHERE COORD_SYS_CODE = ?");
            stmt.setString(1, primaryKey);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String    epsg = getString(result, 1, code);
                final String    name = getString(result, 2, code);
                final String    type = getString(result, 3, code);
                final int  dimension = getInt   (result, 4, code);
                final String remarks = result.getString( 5);
                final CoordinateSystemAxis[] axis = createCoordinateSystemAxis(code, dimension);
                final Map properties = createProperties(name, epsg, remarks); // Must be after axis
                final CSFactory factory = factories.getCSFactory();
                CoordinateSystem cs = null;
                if (type.equalsIgnoreCase("ellipsoidal")) {
                    switch (dimension) {
                        case 2: cs=factory.createEllipsoidalCS(properties, axis[0], axis[1]); break;
                        case 3: cs=factory.createEllipsoidalCS(properties, axis[0], axis[1], axis[2]); break;
                    }
                } else if (type.equalsIgnoreCase("cartesian")) {
                    switch (dimension) {
                        case 2: cs=factory.createCartesianCS(properties, axis[0], axis[1]); break;
                        case 3: cs=factory.createCartesianCS(properties, axis[0], axis[1], axis[2]); break;
                    }
                } else if (type.equalsIgnoreCase("spherical")) {
                    switch (dimension) {
                        case 3: cs=factory.createSphericalCS(properties, axis[0], axis[1], axis[2]); break;
                    }
                } else if (type.equalsIgnoreCase("gravity-related")) {
                    switch (dimension) {
                        case 1: cs=factory.createVerticalCS(properties, axis[0]); break;
                    }
                } else if (type.equalsIgnoreCase("linear")) {
                    switch (dimension) {
                        case 1: cs=factory.createLinearCS(properties, axis[0]); break;
                    }
                } else if (type.equalsIgnoreCase("polar")) {
                    switch (dimension) {
                        case 2: cs=factory.createPolarCS(properties, axis[0], axis[1]); break;
                    }
                } else if (type.equalsIgnoreCase("cylindrical")) {
                    switch (dimension) {
                        case 3: cs=factory.createCylindricalCS(properties, axis[0], axis[1], axis[2]); break;
                    }
                } else if (type.equalsIgnoreCase("affine")) {
                    switch (dimension) {
                        case 2: cs=factory.createAffineCS(properties, axis[0], axis[1]); break;
                        case 3: cs=factory.createAffineCS(properties, axis[0], axis[1], axis[2]); break;
                    }
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                if (cs == null) {
                    result.close();
                    throw new FactoryException("Unexpected dimensions."); // TODO: localize.
                }
                returnValue = (CoordinateSystem) ensureSingleton(cs, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw databaseFailure(CoordinateSystem.class, code, exception);
        }
        if (returnValue == null) {
            throw noSuchAuthorityCode(CoordinateSystem.class, code);
        }
        return returnValue;
        
    }

    /**
     * Returns a coordinate reference system from a code.
     *
     * @param  code Value allocated by authority.
     * @return The coordinate reference system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        ensureNonNull("code", code);
        CoordinateReferenceSystem returnValue = null;
        try {
            final String primaryKey = toPrimaryKey(code,
                    "[Coordinate Reference System]", "COORD_REF_SYS_CODE", "COORD_REF_SYS_NAME");
            PreparedStatement stmt;
            stmt = prepareStatement("CoordinateReferenceSystem",
                                            "SELECT COORD_REF_SYS_CODE,"
                                          +       " COORD_REF_SYS_NAME,"
                                          +       " AREA_OF_USE_CODE,"
                                          +       " CRS_SCOPE,"
                                          +       " REMARKS,"
                                          +       " COORD_REF_SYS_KIND,"
                                          +       " COORD_SYS_CODE,"       // Null for CompoundCRS
                                          +       " DATUM_CODE,"           // Null for ProjectedCRS
                                          +       " SOURCE_GEOGCRS_CODE,"  // For ProjectedCRS only
                                          +       " PROJECTION_CONV_CODE," // For ProjectedCRS only
                                          +       " CMPD_HORIZCRS_CODE,"   // For CompoundCRS only
                                          +       " CMPD_VERTCRS_CODE"     // For CompoundCRS only
                                          + " FROM [Coordinate Reference System]"
                                          + " WHERE COORD_REF_SYS_CODE = ?");
            stmt.setString(1, primaryKey);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String epsg    = getString(result, 1, code);
                final String name    = getString(result, 2, code);
                final String area    = result.getString( 3);
                final String scope   = result.getString( 4);
                final String remarks = result.getString( 5);
                final String type    = getString(result, 6, code);
                final CRSFactory factory = factories.getCRSFactory();
                final CoordinateReferenceSystem crs;
                /* ----------------------------------------------------------------------
                 *   GEOGRAPHIC CRS
                 *
                 *   NOTE: 'createProperties' MUST be invoked after any call to an other
                 *         'createFoo' method. Consequently, do not factor out.
                 * ---------------------------------------------------------------------- */
                if (type.equalsIgnoreCase("geographic 2D") ||
                    type.equalsIgnoreCase("geographic 3D"))
                {
                    final String csCode       = getString(result, 7, code);
                    final String dmCode       = getString(result, 8, code);
                    final EllipsoidalCS cs    = buffered.createEllipsoidalCS(csCode);
                    final GeodeticDatum datum = buffered.createGeodeticDatum(dmCode);
                    crs = factory.createGeographicCRS(
                          createProperties(name, epsg, area, scope, remarks), datum, cs);
                }
                /* ----------------------------------------------------------------------
                 *   PROJECTED CRS
                 *
                 *   NOTE: This method invokes itself indirectly, through createGeographicCRS.
                 *         Concequently, we can't use 'result' anymore. We must close it here.
                 * ---------------------------------------------------------------------- */
                else if (type.equalsIgnoreCase("projected")) {
                    final String csCode     = getString(result,  7, code);
                    final String geoCode    = getString(result,  9, code);
                    final String conversion = getString(result, 10, code);
                    result.close(); // Must be close before createGeographicCRS
                    final CartesianCS   cs     = buffered.createCartesianCS(csCode);
                    final GeographicCRS geoCRS = buffered.createGeographicCRS(geoCode);
                    stmt = prepareStatement("Projection", "SELECT COORD_OP_NAME,"
                                                        +       " AREA_OF_USE_CODE,"
                                                        +       " COORD_OP_SCOPE,"
                                                        +       " COORD_OP_METHOD_CODE,"
                                                        +       " REMARKS"
                                                        + " FROM [Coordinate_Operation]"
                                                        + " WHERE COORD_OP_CODE = ?");
                    stmt.setString(1, conversion);
                    result = stmt.executeQuery();
                    OperationMethod method = null;
                    Map properties = null;
                    while (result.next()) {
                        final String opName    = getString(result, 1, conversion);
                        final String opArea    = result.getString( 2);
                        final String opScope   = result.getString( 3);
                        final String opCode    = getString(result, 4, conversion);
                        final String opRemarks = result.getString( 5);
                        method = (OperationMethod) ensureSingleton(createOperationMethod(
                                                   opCode, conversion, 2, 2, true), method, code);
                        properties = createProperties(name, epsg, area, scope, remarks);
                        assert prefix.length() == 0 : prefix;
                        try {
                            prefix.append("conversion.");
                            properties = createProperties(opName, conversion, opArea, opScope, opRemarks);
                        } finally {
                            prefix.setLength(0);
                        }
                    }
                    if (method == null) {
                         throw noSuchAuthorityCode(Projection.class, conversion);
                    }
                    result.close();
                    result = null;
                    // TODO: remove cast below when we will be allowed to compile for J2SE 1.5.
                    final ParameterValueGroup parameters = (ParameterValueGroup) method.getParameters().createValue();
                    final Ellipsoid ellipsoid = ((GeodeticDatum) geoCRS.getDatum()).getEllipsoid();
                    final Unit axisUnit = ellipsoid.getAxisUnit();
                    parameters.parameter("semi-major axis").setValue(ellipsoid.getSemiMajorAxis(), axisUnit);
                    parameters.parameter("semi-minor axis").setValue(ellipsoid.getSemiMinorAxis(), axisUnit);
                    crs = factories.createProjectedCRS(properties, geoCRS, method, parameters, cs);
                }
                /* ----------------------------------------------------------------------
                 *   VERTICAL CRS
                 * ---------------------------------------------------------------------- */
                else if (type.equalsIgnoreCase("vertical")) {
                    final String        csCode = getString(result, 7, code);
                    final String        dmCode = getString(result, 8, code);
                    final VerticalCS    cs     = buffered.createVerticalCS   (csCode);
                    final VerticalDatum datum  = buffered.createVerticalDatum(dmCode);
                    crs = factory.createVerticalCRS(
                          createProperties(name, epsg, area, scope, remarks), datum, cs);
                }
                /* ----------------------------------------------------------------------
                 *   COMPOUND CRS
                 *
                 *   NOTE: This method invokes itself recursively.
                 *         Concequently, we can't use 'result' anymore.
                 * ---------------------------------------------------------------------- */
                else if (type.equalsIgnoreCase("compound")) {
                    final String code1 = getString(result, 11, code);
                    final String code2 = getString(result, 12, code);
                    result.close();
                    result = null;
                    final CoordinateReferenceSystem crs1, crs2;
                    crs1 = buffered.createCoordinateReferenceSystem(code1);
                    crs2 = buffered.createCoordinateReferenceSystem(code2);
                    crs  = factory.createCompoundCRS(
                           createProperties(name, epsg, area, scope, remarks),
                           new CoordinateReferenceSystem[] {crs1, crs2});
                }
                /* ----------------------------------------------------------------------
                 *   GEOCENTRIC CRS
                 * ---------------------------------------------------------------------- */
                else if (type.equalsIgnoreCase("geocentric")) {
                    final String           csCode = getString(result, 7, code);
                    final String           dmCode = getString(result, 8, code);
                    final CoordinateSystem cs     = buffered.createCoordinateSystem(csCode);
                    final GeodeticDatum    datum  = buffered.createGeodeticDatum   (dmCode);
                    final Map properties = createProperties(name, epsg, area, scope, remarks);
                    if (cs instanceof CartesianCS) {
                        crs = factory.createGeocentricCRS(properties, datum, (CartesianCS) cs);
                    } else if (cs instanceof SphericalCS) {
                        crs = factory.createGeocentricCRS(properties, datum, (SphericalCS) cs);
                    } else {
                        result.close();
                        throw new FactoryException("Incompatible CS type.");
                        // TODO: localize and provide more details.
                    }
                }
                /* ----------------------------------------------------------------------
                 *   ENGINEERING CRS
                 * ---------------------------------------------------------------------- */
                else if (type.equalsIgnoreCase("engineering")) {
                    final String           csCode = getString(result, 7, code);
                    final String           dmCode = getString(result, 8, code);
                    final CoordinateSystem cs     = buffered.createCoordinateSystem(csCode);
                    final EngineeringDatum datum  = buffered.createEngineeringDatum(dmCode);
                    crs = factory.createEngineeringCRS(
                          createProperties(name, epsg, area, scope, remarks), datum, cs);
                }
                /* ----------------------------------------------------------------------
                 *   UNKNOW CRS
                 * ---------------------------------------------------------------------- */
                else {
                    result.close();
                    throw new FactoryException(Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
                }
                returnValue = (CoordinateReferenceSystem) ensureSingleton(crs, returnValue, code);
                if (result == null) {
                    // Bypass the 'result.close()' line below:
                    // the ResultSet has already been closed.
                    return returnValue;
                }
            }
            result.close();
        } catch (SQLException exception) {
            throw databaseFailure(CoordinateReferenceSystem.class, code, exception);
        }
        if (returnValue == null) {
             throw noSuchAuthorityCode(CoordinateReferenceSystem.class, code);
        }
        return returnValue;
    }

    /**
     * Returns parameter descriptors from a code. The default value is
     * set to the actual value declared in the parameter value table.
     *
     * @param  operation The operation code.
     * @param  method The method code.
     * @return The parameter descriptors.
     */
    private ParameterDescriptor[] createParameters(final String  method,
                                                   final String  operation,
                                                   final boolean isProjection)
            throws SQLException, FactoryException
    {
        final List descriptors = new ArrayList();
        if (isProjection) {
            descriptors.add(MapProjection.AbstractProvider.SEMI_MAJOR);
            descriptors.add(MapProjection.AbstractProvider.SEMI_MINOR);
        }
        final PreparedStatement stmt;
        stmt = prepareStatement("Parameters",
                                        "SELECT CP.PARAMETER_NAME,"
                                +             " CP.DESCRIPTION,"
                                +             " CV.PARAMETER_VALUE,"
                                +             " CV.PARAM_VALUE_FILE_REF,"
                                +             " CV.UOM_CODE"
                                +      " FROM ([Coordinate_Operation Parameter] AS CP"
                                + " INNER JOIN [Coordinate_Operation Parameter Value] AS CV"
                                +          " ON CV.PARAMETER_CODE = CP.PARAMETER_CODE)"
                                + " INNER JOIN [Coordinate_Operation Parameter Usage] AS CU"
                                +         " ON (CP.PARAMETER_CODE = CU.PARAMETER_CODE)"
                                +        " AND (CV.COORD_OP_METHOD_CODE = CU.COORD_OP_METHOD_CODE)"
                                +       " WHERE CV.COORD_OP_METHOD_CODE = ?"
                                +         " AND CV.COORD_OP_CODE = ?"
                                +    " ORDER BY CU.SORT_ORDER");
        stmt.setString(1, method);
        stmt.setString(2, operation);
        ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final ParameterDescriptor parameter;
            final String name    = getString(result, 1, operation);
            final String remarks = result.getString( 2);
            final double value   = result.getDouble( 3);
            if (!result.wasNull()) {
                final Unit unit = buffered.createUnit(getString(result, 5, operation));
                final Map properties = createProperties(name, null, remarks);
                parameter = new DefaultParameterDescriptor(properties,
                            value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, unit, true);
            } else {
                Object ref = getString(result, 4, operation);
                try {
                    ref = new URI((String) ref);
                } catch (URISyntaxException exception) {
                    // Ignore: we will stores the reference as a file.
                    ref = new File((String) ref);
                }
                parameter = new DefaultParameterDescriptor(name, remarks, ref, true);
            }
            descriptors.add(parameter);
        }
        result.close();
        return (ParameterDescriptor[]) descriptors.toArray(new ParameterDescriptor[descriptors.size()]);
    }

    /**
     * Returns an operation method from a code.
     *
     * @param  code Value allocated by authority.
     * @return The operation method code.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     */
    private OperationMethod createOperationMethod(final String code, final String operation,
                                                  final int sourceDimensions,
                                                  final int targetDimensions,
                                                  final boolean isProjection)
            throws SQLException, FactoryException
    {
        OperationMethod returnValue = null;
        final PreparedStatement stmt;
        stmt = prepareStatement("OperationMethod",
                                           "SELECT COORD_OP_METHOD_NAME,"
                                         +       " FORMULA,"
                                         +       " REMARKS"
                                         +  " FROM [Coordinate_Operation Method]"
                                         + " WHERE COORD_OP_METHOD_CODE = ?");
        stmt.setString(1, code);
        ResultSet result = stmt.executeQuery();
        ParameterDescriptor[] descriptors = null;
        OperationMethod method = null;
        while (result.next()) {
            final String name    = getString(result, 1, code);
            final String formula = result.getString( 2);
            final String remarks = result.getString( 3);
            if (descriptors == null) {
                descriptors = createParameters(code, operation, isProjection);
            }
            final Map properties = createProperties(name, code, remarks);
            if (formula != null) {
                properties.put(OperationMethod.FORMULA_KEY, formula);
            }
            method = new DefaultOperationMethod(properties, sourceDimensions, targetDimensions,
                     new DefaultParameterDescriptorGroup(properties, descriptors));
            returnValue = (OperationMethod) ensureSingleton(method, returnValue, code);
        }
        if (returnValue == null) {
             throw noSuchAuthorityCode(OperationMethod.class, code);
        }
        return returnValue;
    }

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
     * Returns {@code true} if the specified code may be a primary key in some table. This method
     * do not needs to checks any entry in the database. It should just checks from the syntax if
     * the code looks like a valid EPSG identifier. The default implementation returns {@code true}
     * if all non-space characters are {@linkplain Character#isDigit(char) digits}.
     * <p>
     * When this method returns {@code false}, some {@code createFoo(...)} methods look for the
     * code in the name column instead of the primary key column. This allows to accept the
     * "<cite>NTF (Paris) / France I</cite>" string (for example) in addition to the {@code "27581"}
     * primary key. Both should fetch the same object.
     * <p>
     * If this method returns {@code true} in all cases, then this factory never search for matching
     * names. In such case, an appropriate exception will be thrown in {@code createFoo(...)}
     * methods if the code is not found in the primary key column. Subclasses can overrides this
     * method that way if this is the intended behavior.
     *
     * @param  code The code the inspect.
     * @return {@code true} if the code is probably a primary key.
     * @throws FactoryException if an unexpected error occured while inspecting the code.
     *
     * @since 2.2
     */
    protected boolean isPrimaryKey(final String code) throws FactoryException {
        final int length = code.length();
        for (int i=0; i<length; i++) {
            final char c = code.charAt(i);
            if (!Character.isDigit(c) && !Character.isSpaceChar(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if it is safe to dispose this factory. This method is invoked indirectly
     * by {@link DefaultFactory} after some timeout in order to release resources. This method will
     * block the disposal if some {@linkplain #getAuthorityCodes set of authority codes} are still
     * in use.
     */
    final synchronized boolean canDispose() {
        boolean can = true;
        Map pool/*<SoftReference,WeakReference>*/ = null;
        for (final Iterator it=authorityCodes.entrySet().iterator(); it.hasNext();) {
            final Map.Entry  entry     = (Map.Entry)      it.next();
            final Reference  reference = (Reference)      entry.getValue();
            final AuthorityCodes codes = (AuthorityCodes) reference.get();
            if (codes == null) {
                it.remove();
                continue;
            }
            /*
             * A set of authority codes is still in use. We can't dispose this factory.
             * But maybe the set was retained only by soft references... So we continue
             * the iteration anyway and replace all soft references by weak ones, in order
             * to get more chances to be garbage-collected before the next disposal cycle.
             */
            can = false;
            if (reference instanceof SoftReference) {
                // Each reference appears twice (once with the type key, and once under the SQL
                // statement as key). So we need to manage a pool of references for avoiding
                // duplication.
                if (pool == null) {
                    pool = new IdentityHashMap();
                }
                WeakReference weak = (WeakReference) pool.get(reference);
                if (weak == null) {
                    weak = new WeakReference(codes);
                    pool.put(reference, weak);
                }
                entry.setValue(weak);
            }
        }
        return can;
    }

    /**
     * Disposes any resources hold by this object.
     *
     * @throws FactoryException if an error occured while closing the connection.
     */
    public synchronized void dispose() throws FactoryException {
        final boolean shutdown = SHUTDOWN_THREAD.equals(Thread.currentThread().getName());
        try {
            for (final Iterator it=authorityCodes.values().iterator(); it.hasNext();) {
                final AuthorityCodes set = (AuthorityCodes) ((Reference) it.next()).get();
                if (set != null) {
                    set.finalize();
                }
                it.remove();
            }
            for (final Iterator it=statements.values().iterator(); it.hasNext();) {
                ((PreparedStatement) it.next()).close();
                it.remove();
            }
            if (shutdown) {
                shutdown(true);
            }
            connection.close();
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        super.dispose();
        if (shutdown) try {
            shutdown(false);
        } catch (SQLException exception) {
            throw new FactoryException(exception);
        }
        LOGGER.fine("EPSG connection closed."); // TODO: localize
    }

    /**
     * Shutdown the database engine. This method is invoked twice by {@link DefaultFactory}
     * at JVM shutdown: one time before the {@linkplain #connection} is closed, and a second
     * time after. This shutdown hook is usefull for <cite>embedded</cite> database engine
     * starting a server process in addition to the client process. Just closing the connection
     * is not enough for them. Example:
     * <P>
     * <UL>
     *   <LI>HSQL database engine needs to execute a {@code "SHUTDOWN"} statement using the
     *      {@linkplain #connection} before it is closed.</LI>
     *   <LI>Derby database engine needs to instruct the {@linkplain java.sql.DriverManager driver
     *       manager} after all connections have been closed.</LI>
     * </UL>
     * <P>
     * The default implementation does nothing, which is suffisient for implementations
     * connecting to a distant server (i.e. non-embedded database engine), for example
     * {@linkplain AccessDataSource MS-Access} or {@linkplain PostgreDataSource PostgreSQL}.
     *
     * @param active {@code true} if the {@linkplain #connection} is alive, or {@code false}
     *        otherwise. This method is invoked first with {@code active} set to {@code true},
     *        then a second time with {@code active} set to {@code false}.
     * @throws SQLException if this method failed to shutdown the database engine.
     */
    protected void shutdown(final boolean active) throws SQLException {
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
     * Constructs an exception for a database failure.
     *
     * @todo localize
     */
    private static FactoryException databaseFailure(final Class type, final String code,
                                                    final SQLException cause)
    {
        return new FactoryException("Database failure will constructing a " +
                Utilities.getShortName(type) + " for code \""+code+"\".", cause);
    }
}
