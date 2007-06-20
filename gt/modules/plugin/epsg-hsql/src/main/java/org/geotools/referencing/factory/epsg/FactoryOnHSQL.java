/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.util.Version;
import org.geotools.factory.Hints;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

// HSQL dependencies
import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Connection to the EPSG database in HSQL database engine format using JDBC.
 * @deprecated Please use ThreadedHsqlEpsgFactory instead, this class is marked since 2.4 so perhaps it can be removed?
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Didier Richard
 */
public class FactoryOnHSQL extends ThreadedHsqlEpsgFactory {

    /**
     * Creates a new instance of this factory. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains
     * the name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise, a
     * temporary directory will be used.
     */
    public FactoryOnHSQL() {
        this(null);
    }

    /**
     * Creates a new instance of this data source using the specified hints. The priority
     * is set to a lower value than the {@linkplain FactoryOnAccess}'s one in order to give
     * precedence to the Access-backed database, if presents. Priorities are set that way
     * because:
     * <ul>
     *   <li>The MS-Access format is the primary EPSG database format.</li>
     *   <li>If a user downloads the MS-Access database himself, he probably wants to use it.</li>
     * </ul>
     */
    public FactoryOnHSQL(final Hints hints) {
        super(hints );
    }

}
