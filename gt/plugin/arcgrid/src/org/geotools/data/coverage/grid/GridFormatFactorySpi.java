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
package org.geotools.data.coverage.grid;

import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.opengis.coverage.grid.Format;
import java.io.IOException;
import java.net.URL;


/**
 * Constructs a live GridCoverageFormat.
 *
 * <p>
 * In addition to implementing
 * this interface datastores should have a services file:
 * </p>
 *
 * <p>
 * <code>META-INF/services/org.geotools.data.GridCoverageFormatFactorySpi</code>
 * </p>
 *
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 *
 * <p>
 * example:<br/><code>e.g.
 * org.geotools.data.arcgrid.ArcGridFormatFactory</code>
 * </p>
 *
 * <p>
 * The factories are never called directly by users, instead the
 * GridFormatFinder class is used.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public interface GridFormatFactorySpi extends org.geotools.factory.Factory {
    /**
     * Construct a live grid format using the params specifed.
     *
     * @param params The full set of information needed to construct a live
     *        data store. Typical key values for the map include: url -
     *        location of a resource, used by file reading datasources. dbtype
     *        - the type of the database to connect to, e.g. postgis, mysql
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws IOException if there were any problems creating or connecting
     *         the datasource.
     */
    Format createFormat();

    /**
     * @todo javadoc
     */
    GridCoverageReader createReader(Object source);

    /**
     * @todo javadoc
     */
    GridCoverageWriter createWriter(Object destination);

    boolean accepts(URL input);

    /**
     * Test to see if this format is available, if it has all the
     * appropriate libraries to construct a format.
     * <p>
     * Most datastores should
     * return true, because geotools will distribute the appropriate
     * libraries.  Though it's not a bad idea for DataStoreFactories to check
     * to make sure that the  libraries are there.
     * </p>
     * @return <tt>true</tt> if and only if this factory has all the
     *         appropriate jars on the classpath to handle a Format.
     */
    boolean isAvailable();
}
