/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.sql;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

// OpenGIS dependencies
import org.opengis.metadata.MetaData;
import org.opengis.util.CodeList;


/**
 * A connection to a metadata database. The metadata database can be created
 * using one of the scripts suggested in GeoAPI, for example
 * <code><A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/metadata/doc-files/postgre/create.sql">create.sql</A></CODE>.
 * Then, in order to get for example a telephone number, the following code
 * may be used.
 *
 * <BLOCKQUOTE><PRE>
 * import org.opengis.metadata.citation.{@linkplain org.opengis.metadata.citation.Telephone Telephone};
 * ...
 * Connection     connection = ...
 * MetadataSource source     = new MetadataSource(connection);
 * Telephone      telephone  = (Telephone) source.getEntry(Telephone.class, id);
 * </PRE></BLOCKQUOTE>
 *
 * where <code>id</code> is the primary key value for the desired record in the
 * <code>CI_Telephone</code> table.
 *
 * @author Touraïvane
 * @author Olivier Kartotaroeno
 * @author Martin Desruisseaux
 */
public class MetadataSource {
    /**
     * The package for metadata <strong>interfaces</strong> (not the implementation).
     */
    final String metadataPackage = "org.opengis.metadata.";

    /**
     * The connection to the database.
     */
    private final Connection connection;

    /**
     * The SQL query to use for fetching the attribute in a specific row.
     * The first question mark is the table name to search into; the second
     * one is the primary key of the record to search.
     */
    private final String query = "SELECT * FROM metadata.\"?\" WHERE identifier=?";

    /**
     * The SQL query to use for fetching a code list element.
     * The first question mark is the table name to search into;
     * the second one is the primary key of the element to search.
     */
    private final String codeQuery = "SELECT name FROM metadata.\"?\" WHERE code=?";

    /**
     * The prepared statements created is previous call to {@link #getValue}.
     * Those statements are encapsulated into {@link MetadataResult} objects.
     */
    private final Map statements = new HashMap();

    /**
     * The map from GeoAPI names to ISO names. For example the GeoAPI
     * {@link org.opengis.metadata.citation.Citation} interface maps
     * to the ISO 19115 <code>CI_Citation</code> name.
     */
    private final Properties geoApiToIso = new Properties();

    /**
     * The class loader to use for proxy creation.
     */
    private final ClassLoader loader;
    
    /**
     * Creates a new metadata source.
     *
     * @param connection The connection to the database.
     */
    public MetadataSource(final Connection connection) {
        this.connection = connection;
        try {
            final InputStream in = MetaData.class.getClassLoader()
                  .getResourceAsStream("org/opengis/metadata/GeoAPI_to_ISO.properties");
            geoApiToIso.load(in);
            in.close();
        } catch (IOException exception) {
            /*
             * Note: we do not expose the checked IOException because in a future
             *       version (when we will be allowed to use J2SE 1.5), it should
             *       disaspear. This is because a J2SE 1.5 enabled version should
             *       use method's annotations instead.
             */
            throw new MetadataException(exception);
        }
        loader = getClass().getClassLoader();
    }

    /**
     * Returns an implementation of the specified metadata interface filled
     * with the data referenced by the specified identifier. Alternatively,
     * this method can also returns a {@link CodeList} element.
     *
     * @param  type The interface to implement (e.g.
     *         {@link org.opengis.metadata.citation.Citation}), or
     *         the {@link CodeList}.
     * @param  identifier The identifier used in order to locate the record for
     *         the metadata entity to be created. This is usually the primary key
     *         of the record to search for.
     * @return An implementation of the required interface, or the code list element.
     * @throws SQLException if a SQL query failed.
     */
    public synchronized Object getEntry(final Class type, final int identifier)
            throws SQLException
    {
        if (CodeList.class.isAssignableFrom(type)) {
            return getCodeList(type, identifier);
        }
        return Proxy.newProxyInstance(loader, new Class[] {type},
                                      new MetadataEntity(identifier, this));
    }

    /**
     * Returns an attribute from a table.
     *
     * @param  type       The interface class. This is mapped to the table name in the database.
     * @param  method     The method invoked. This is mapped to the column name in the database.
     * @param  identifier The primary key of the record to search for.
     * @return The value of the requested attribute.
     * @throws SQLException if the SQL query failed.
     */
    final Object getValue(final Class type, final Method method, final int identifier)
            throws SQLException
    {
        assert Thread.holdsLock(this);
        final String className = getClassName(type);
        MetadataResult result = (MetadataResult) statements.get(type);
        if (result == null) {
            result = new MetadataResult(connection, query, getTableName(className));
            statements.put(type, result);
        }
        final String columnName = getColumnName(className, method);
        final Class valueType = method.getReturnType();
        if (valueType.isInterface() && valueType.getName().startsWith(metadataPackage)) {
            return getEntry(valueType, result.getInt(identifier, columnName));
        } else if (CodeList.class.isAssignableFrom(valueType)) {
            return getCodeList(valueType, result.getInt(identifier, columnName));
        } else {
            return result.getObject(identifier, columnName);
        }
    }

    /**
     * Returns a code list of the given type.
     *
     * @param  type The type, as a subclass of {@link CodeList}.
     * @param  code The code to search for (usually the primary key).
     * @return The code list element.
     * @throws SQLException if a SQL query failed.
     */
    private CodeList getCodeList(final Class type, final int code) throws SQLException {
        assert Thread.holdsLock(this);
        final String className = getClassName(type);
        MetadataResult result = (MetadataResult) statements.get(type);
        if (result == null) {
            result = new MetadataResult(connection, query, getTableName(className));
            statements.put(type, result);
        }
        final String name = result.getString(code);
        /*
         * Search a code list with the same name than the one declared
         * in the database. We will use name instead of code numerical
         * value, since the later is more bug prone.
         */
        final CodeList[] values;
        try {
            values = (CodeList[]) type.getMethod("values", (Class []) null)
                                      .invoke   (null,     (Object[]) null);
        } catch (NoSuchMethodException exception) {
            throw new MetadataException(exception);
        } catch (IllegalAccessException exception) {
            throw new MetadataException(exception);
        } catch (InvocationTargetException exception) {
            throw new MetadataException(exception);
        }
        CodeList candidate;
        final StringBuffer candidateName = new StringBuffer(className);
        candidateName.append('.');
        final int base = candidateName.length();
        if (code>=1 && code<values.length) {
            candidate = values[code-1];
            candidateName.append(candidate.name());
            if (name.equals(geoApiToIso.getProperty(candidateName.toString()))) {
                return candidate;
            }
        }
        /*
         * The previous code was an optimization which checked directly the code list
         * for the same code than the one used in the database. Most of the time, the
         * name matches and this loop is never executed. If we reach this point, then
         * maybe the numerical code are not the same in the database than in the Java
         * CodeList implementation. Check each code list element by name.
         */
        for (int i=0; i<values.length; i++) {
            candidate = values[i];
            candidateName.setLength(base);
            candidateName.append(candidate.name());
            if (name.equals(geoApiToIso.getProperty(candidateName.toString()))) {
                return candidate;
            }
        }
        // TODO: localize
        throw new SQLException("Unknow code list: "+name+" in table \"" + 
                               getTableName(className)+'"');
    }

    /**
     * Returns the unqualified Java interface name for the specified type.
     * This is usually the GeoAPI name.
     */
    private static String getClassName(final Class type) {
        final String className = type.getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Returns the table name for the specified class.
     * This is usually the ISO 19115 name.
     */
    private String getTableName(final String className) {
        final String tableName = geoApiToIso.getProperty(className);
        return (tableName != null) ? tableName : className;
    }

    /**
     * Returns the column name for the specified method.
     */
    private String getColumnName(final String className, final Method method) {
        final String methodName = method.getName();
        final String columnName = geoApiToIso.getProperty(className+'.'+methodName);
        return (columnName != null) ? columnName : methodName;
    }

    /**
     * Close all connections used in this object.
     */
    public synchronized void close() throws SQLException {
        for (final Iterator it=statements.values().iterator(); it.hasNext();) {
            ((MetadataResult)it.next()).close();
            it.remove();
        }
        connection.close();
    }    
}
