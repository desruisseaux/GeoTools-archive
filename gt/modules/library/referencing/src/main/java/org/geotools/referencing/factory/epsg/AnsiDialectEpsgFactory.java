/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// Geotools dependencies
import org.geotools.factory.Hints;


/**
 * An EPSG factory for the database generated by SQL scripts rather than the MS-Access one.
 * This class overrides {@link #adaptSQL} in order to translate SQL statements from
 * MS-Access syntax to ANSI syntax. By default, the translated SQL statements use the
 * table and field names in the Data Description Language (DDL) scripts provided by
 * EPSG to create the schema for the database. Subclasses can changes this default
 * behavior by modifying the {@link #map}.
 * <p>
 * <strong>References:</strong><ul>
 *   <li>EPSG geodecy parameters database readme at
 *       <A HREF="http://www.ihsenergy.com/epsg/geodetic2.html">www.epsg.org</A>
 *   </li>
 * </ul>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 * @author Didier Richard
 * @author John Grange
 */
public class AnsiDialectEpsgFactory extends DirectEpsgFactory {
    /**
     * The default map using ANSI names.
     */
    private static final String[] ANSI = {
        "[Alias]",                                  "epsg_alias",
        "[Area]",                                   "epsg_area",
        "[Coordinate Axis]",                        "epsg_coordinateaxis",
        "[Coordinate Axis Name]",                   "epsg_coordinateaxisname",
        "[Coordinate_Operation]",                   "epsg_coordoperation",
        "[Coordinate_Operation Method]",            "epsg_coordoperationmethod",
        "[Coordinate_Operation Parameter]",         "epsg_coordoperationparam",
        "[Coordinate_Operation Parameter Usage]",   "epsg_coordoperationparamusage",
        "[Coordinate_Operation Parameter Value]",   "epsg_coordoperationparamvalue",
        "[Coordinate_Operation Path]",              "epsg_coordoperationpath",
        "[Coordinate Reference System]",            "epsg_coordinatereferencesystem",
        "[Coordinate System]",                      "epsg_coordinatesystem",
        "[Datum]",                                  "epsg_datum",
        "[Ellipsoid]",                              "epsg_ellipsoid",
        "[Naming System]",                          "epsg_namingsystem",
        "[Prime Meridian]",                         "epsg_primemeridian",
        "[Supersession]",                           "epsg_supersession",
        "[Unit of Measure]",                        "epsg_unitofmeasure",
        "[Version History]",                        "epsg_versionhistory",
        "[ORDER]",                                  "coord_axis_order" // a field in epsg_coordinateaxis
    };

    /**
     * Maps the MS-Access names to ANSI names. Keys are MS-Access names including bracket.
     * Values are ANSI names. Keys and values are case-sensitive. The default content of
     * this map is:
     *
     * <pre><table>
     *   <tr><th align="center">MS-Access name</th>            <th align="center">ANSI name</th></tr>
     *   <tr><td>[Alias]</td>                                  <td>epsg_alias</td></tr>
     *   <tr><td>[Area]</td>                                   <td>epsg_area</td></tr>
     *   <tr><td>[Coordinate Axis]</td>                        <td>epsg_coordinateaxis</td></tr>
     *   <tr><td>[Coordinate Axis Name]</td>                   <td>epsg_coordinateaxisname</td></tr>
     *   <tr><td>[Coordinate_Operation]</td>                   <td>epsg_coordoperation</td></tr>
     *   <tr><td>[Coordinate_Operation Method]</td>            <td>epsg_coordoperationmethod</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter]</td>         <td>epsg_coordoperationparam</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter Usage]</td>   <td>epsg_coordoperationparamusage</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter Value]</td>   <td>epsg_coordoperationparamvalue</td></tr>
     *   <tr><td>[Coordinate_Operation Path]</td>              <td>epsg_coordoperationpath</td></tr>
     *   <tr><td>[Coordinate Reference System]</td>            <td>epsg_coordinatereferencesystem</td></tr>
     *   <tr><td>[Coordinate System]</td>                      <td>epsg_coordinatesystem</td></tr>
     *   <tr><td>[Datum]</td>                                  <td>epsg_datum</td></tr>
     *   <tr><td>[Naming System]</td>                          <td>epsg_namingsystem</td></tr>
     *   <tr><td>[Ellipsoid]</td>                              <td>epsg_ellipsoid</td></tr>
     *   <tr><td>[Prime Meridian]</td>                         <td>epsg_primemeridian</td></tr>
     *   <tr><td>[Supersession]</td>                           <td>epsg_supersession</td></tr>
     *   <tr><td>[Unit of Measure]</td>                        <td>epsg_unitofmeasure</td></tr>
     *   <tr><td>[CA.ORDER]</td>                               <td>coord_axis_order</td></tr>
     * </table></pre>
     *
     * Subclasses can modify this map in their constructor in order to provide a different
     * mapping.
     */
    protected final Map map = new LinkedHashMap();

    /**
     * The prefix before any table name. May be replaced by a schema if {@link #setSchema}
     * is invoked.
     */
    private String prefix = "epsg_";

    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param userHints  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     *
     * @since 2.2
     */
    public AnsiDialectEpsgFactory(final Hints      userHints,
                               final Connection connection)
    {
        super(userHints, connection);
        for (int i=0; i<ANSI.length; i++) {
            map.put(ANSI[i], ANSI[++i]);
        }
    }

    /**
     * Replaces the {@code "epsg_"} prefix by the specified schema name. If the removal
     * of the {@code "epsg_"} prefix is not wanted, append it to the schema name
     * (e.g. {@code "myschema.epsg_"}). This method should be invoked at construction
     * time only.
     *
     * @param schema The database schema in which the epsg tables are stored.
     *
     * @since 2.2
     */
    protected void setSchema(String schema) {
        schema = schema.trim();
        final int length = schema.length();
        if (length == 0) {
            throw new IllegalArgumentException(schema);
        }
        final char separator = schema.charAt(length-1);
        if (separator!='.' && separator!='_') {
            schema += '.';
        } else if (length == 1) {
            throw new IllegalArgumentException(schema);
        }
        for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
            final Map.Entry  entry = (Map.Entry) it.next();
            final String tableName = (String) entry.getValue();
            /**
             * Update the map, prepending the schema name to the table name
             * so long as the value is a table name and not a field. This
             * algorithm assumes that all old table names start with "epsg_".
             */
            if (tableName.startsWith(prefix)) {
                entry.setValue(schema + tableName.substring(prefix.length()));
            }
        }
        prefix = schema;
    }

    /**
     * Modifies the given SQL string to be suitable for non MS-Access databases.
     * This replaces table and field names in the SQL with the new names 
     * in the SQL DDL scripts provided with EPSG database.
     *
     * @param  statement The statement in MS-Access syntax.
     * @return The SQL statement in ANSI syntax.
     */
    protected String adaptSQL(final String statement) {
        final StringBuffer modified = new StringBuffer(statement);
        for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final String  oldName = (String) entry.getKey();
            final String  newName = (String) entry.getValue();
            /*
             * Replaces all occurences of 'oldName' by 'newName'.
             */
            int start = 0;
            while ((start=modified.indexOf(oldName, start)) >= 0) {
                modified.replace(start, start+oldName.length(), newName);
                start += newName.length();
            }
        }
        return modified.toString();
    }
}
