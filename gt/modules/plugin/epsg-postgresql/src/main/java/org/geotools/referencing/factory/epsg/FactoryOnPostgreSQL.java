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
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

// Geotools dependencies
import org.geotools.util.Logging;
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

// PostgreSQL dependencies
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.jdbc3.Jdbc3SimpleDataSource;


/**
 * Connection to the EPSG database in PostgreSQL database engine using JDBC
 *
 * @deprecated Please use ThreadedPostgreSQLEpsgFactory
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Didier Richard
 * @author Martin Desruisseaux
 *
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/How+to+install+the+EPSG+database+in+PostgreSQL
 */
public class FactoryOnPostgreSQL extends ThreadedPostgreSQLEpsgFactory {
    public static final String CONFIGURATION_FILE = "EPSG-DataSource.properties";
    /**
     * Creates a new instance of this factory.
     */
    public FactoryOnPostgreSQL() {
        this(null);
    }

    /**
     * Creates a new instance of this factory with the specified hints.
     * The priority is set to a lower value than the {@linkplain FactoryOnAccess}'s one
     * in order to give the priority to any "official" database installed locally by the
     * user, when available.
     */
    public FactoryOnPostgreSQL(final Hints hints) {
        super(hints);
    }

}
