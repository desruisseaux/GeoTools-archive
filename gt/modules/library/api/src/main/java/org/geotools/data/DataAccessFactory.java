/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import org.opengis.util.InternationalString;
import org.geotools.factory.Factory;


/**
 * Constructs a live DataAccess connection from a set of parameters.
 * <p>
 * Parameters are specified using a Java Bean; the DataAccesFinder utility class will take care of
 * allowing you to work with Map<String,Serializable> as a trasfer object of bean properties.
 * </p>
 *
 * @author Jody Garnett
 */
public interface DataAccessFactory extends Factory {
    /**
     * Display name for this DataAccess in the current locale.
     *
     * @return human readable display name
     */
    public InternationalString getName();

    /**
     * Test to ensure the correct environment is available for this Factory to function.
     * <p>
     * Implementations usually check such things as availablity of required JDBC drivers,
     * or Java Advanced Imaging formats that they intended to use.
     * </p>
     * @return true if needed environment is found
     */
    public boolean isAvailable();

    /**
     * A java bean (with default properties values) describing connection parameters.
     *
     * @return Java Bean describing parameters required for data access
     */
    Object createAccessBean();

    /**
     * Test to see if this factory is suitable for processing this connectionParams
     * <p>
     * This method is often an instance of check followed by ensuring required bean properties (ie
     * connection parameters) are non null.
     * </p>
     *
     * @param connectionPrametersBean
     * @return <code>true</code> if bean has valid parameters to attempt a connection
     */
    boolean canAccess(Object connectionPrametersBean);

    /**
     * Connect to a physical data storage location and provide DataAccess class for interaction.
     * <p>
     * A new DataAccess class is created on each call; end-users should either store this instance
     * as a Singleton (gasp!) or make use of the GeoTools catalog facilities to manage connections.
     *
     * @param bean Bean capturing connection paramters, should be of the same type as provided by
     *        createConnectionBean
     * @return The created DataAccess
     * @throws If there were any problems setting up the connection
     */
    DataAccess createAccess(Object bean) throws IOException;

    /**
     * Please note that creating a new physical storage location
     * may require additional parameters beyond that needed for
     * simple connection.
     *
     * @return Java Bean describing parameters required for creation
     */
    Object createContentBean();

    /**
     * Confirm that this factory is suitable for creating the physical storage
     * location described by the provided bean.
     * <p>
     * Implementations may also chose to check security concerns (such as the ability
     * to write to disk) as part of this method.
     *
     * @param bean
     * @return <code>true</code> if bean has valid parameters to attempt a connection
     */
    boolean canCreateContent(Object bean);

    /**
     * Set up a new physical storage location, and supply a DataAccess class for interaction.
     *
     * @param bean
     * @return
     */
    DataAccess createContent(Object bean);
}
