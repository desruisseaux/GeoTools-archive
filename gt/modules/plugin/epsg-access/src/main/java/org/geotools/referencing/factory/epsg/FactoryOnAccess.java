/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
import java.sql.SQLException;
import javax.sql.DataSource;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Connection to the EPSG database in MS-Access format using JDBC-ODBC bridge.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Please use {@link ThreadedAccessEpsgFactory}.
 */
public class FactoryOnAccess extends ThreadedAccessEpsgFactory {
    /**
     * Creates a new instance of this factory.
     */
    public FactoryOnAccess() {
        this(null);
    }

    /**
     * Creates a new instance of this factory using the specified set of hints.
     */
    public FactoryOnAccess(final Hints hints) {
        super(hints );
    }
}
