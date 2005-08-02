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
package org.geotools.data.hsql;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Creates a HsqlDataStoreFactory based on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Amr Alam, Refractions Research
 */
public class HsqlDataStoreFactory {
    private static final Logger LOGGER = Logger.getLogger(HsqlDataStoreFactory.class
            .getName());

    /** Creates Hsql JDBC driver class. */
    private static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";

    /** Param, package visibiity for JUnit tests */
    static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'hsql'", true, "hsql");

    /** Param, package visibiity for JUnit tests */
    static final Param HOST = new Param("host", String.class,
            "hsql host machine", true, "localhost");

    /** Param, package visibiity for JUnit tests */
    static final Param PORT = new Param("port", String.class,
            "hsql connection port", true, "9001");

    /** Param, package visibiity for JUnit tests */
    static final Param DATABASE = new Param("database", String.class,
            "hsql database");

    /** Param, package visibiity for JUnit tests */
    static final Param DBFILENAME = new Param("filename", String.class,
            "hsql database filename");

    /** Param, package visibiity for JUnit tests */
    static final Param USER = new Param("user", String.class,
            "user name to login as", false);

    /** Param, package visibiity for JUnit tests */
    static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);

    /** Param, package visibiity for JUnit tests */
    static final Param NAMESPACE = new Param("namespace", String.class,
            "namespace prefix used", false);

    /** Array with all of the params */
    static final Param[] arrayParameters = {
            DBTYPE, DBFILENAME, USER, PASSWD, NAMESPACE
        };

    /**
     * Creates a new instance of HsqlDataStoreFactory
     */
    public HsqlDataStoreFactory() {
    }

    /**
     * Checks to see if all the hsql params are there.
     * 
     * <p>
     * Should have:
     * </p>
     * 
     * <ul>
     * <li>
     * dbtype: equal to hsql
     * </li>
     * <li>
     * user
     * </li>
     * <li>
     * passwd
     * </li>
     * <li>
     * dbfilename
     * </li>
     * </ul>
     * 
     *
     * @param params Set of parameters needed for a hsql data store.
     *
     * @return <code>true</code> if dbtype equals hsql, and contains keys for
     *         host, user, passwd, and database.
     */
    public boolean canProcess(Map params) {
        Object value;

        if (params != null) {
            for (int i = 0; i < arrayParameters.length; i++) {
                if (!(((value = params.get(arrayParameters[i].key)) != null)
                        && (arrayParameters[i].type.isInstance(value)))) {
                    if (arrayParameters[i].required) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.warning("Failed on : "
                                + arrayParameters[i].key);
                            LOGGER.fine(params.toString());
                        }

                        return (false);
                    }
                }
            }
        } else {
            return (false);
        }

        if ((((String) params.get("dbtype")).equalsIgnoreCase("hsql"))) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Construct a hsql data store using the params.
     *
     * @param params The full set of information needed to construct a live
     *        data source.  Should have  dbtype equal to 'hsql', as well as
     *        dbfilename, user, passwd, and namespace (optional).
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws IOException See DataSourceException
     */
    public HsqlDataStore createDataStore(Map params) throws IOException {
        // lookup will throw nice exceptions back to the client code
        //        String host = (String) HOST.lookUp(params);
        String filename = (String) DBFILENAME.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);

        //        String port = (String) PORT.lookUp(params);
        //        String database = (String) DATABASE.lookUp(params);
        String namespace = (String) NAMESPACE.lookUp(params);

        if (!canProcess(params)) {
            // Do this as a last sanity check.
            LOGGER.warning("Can not process : " + params);
            throw new IOException("The parameteres map isn't correct!!");
        }

        HsqlConnectionFactory connFact = new HsqlConnectionFactory(filename,
                user, passwd);

        if (namespace != null) {
            return new HsqlDataStore(connFact, namespace);
        } else {
            return new HsqlDataStore(connFact);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param params
     *
     * @return
     *
     * @throws IOException See UnsupportedOperationException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        return createDataStore(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @return "HSQL"
     */
    public String getDisplayName() {
        return "HSQL";
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.  Currently uses the string "HSQL
     *         Database"
     */
    public String getDescription() {
        return "HSQL Database";
    }

    //    /**
    //     *
    //     */
    //    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
    //        String host = (String) HOST.lookUp(params);
    //        String user = (String) USER.lookUp(params);
    //        String port = (String) PORT.lookUp(params);
    //        String database = (String) DATABASE.lookUp(params);
    //        return new DataSourceMetadataEnity( host+"port", database, "HSQL connection to "+host+" as "+user );
    //    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.  This method is used for gui apps, so as to not
     * advertise data store capabilities they don't actually have.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     */
    public boolean isAvailable() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.warning("HSQL data sources are not available: "
                + cnfe.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { DBTYPE, DBFILENAME, USER, PASSWD, NAMESPACE };
    }

    /**
     * @return Returns the implementation hints. The default implementation returns en
     * empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Data class used to capture Parameter requirements.
     * 
     * <p>
     * Subclasses may provide specific setAsText()/getAsText() requirements
     * </p>
     * 
     * <p>
     * Warning: We would like to start moving towards a common paraemters
     * framework with GridCoverageExchnage. Param will be maintained as a
     * wrapper for one point release (at which time it will be deprecated).
     * </p>
     */
    static class Param {
        /** True if Param is required */
        final public boolean required;

        /** Key used in Parameter map */
        final public String key;

        /** Type of information required */
        final public Class type;

        /** Short description (less then 40 characters) */
        final public String description;

        /**
         * Sampel value provided as an example for user input.
         * 
         * <p>
         * May be passed to getAsText( sample ) for inital text based user
         * interface default.
         * </p>
         */
        final public Object sample;

        /**
         * Provides support for text representations
         * 
         * <p>
         * The parameter type of String is assumed.
         * </p>
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         */
        public Param(String key) {
            this(key, String.class, null);
        }

        /**
         * Provides support for text representations.
         * 
         * <p>
         * You may specify a <code>type</code> for this Param.
         * </p>
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         */
        public Param(String key, Class type) {
            this(key, type, null);
        }

        /**
         * Provides support for text representations
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         * @param description User description of Param (40 chars or less)
         */
        public Param(String key, Class type, String description) {
            this(key, type, description, true);
        }

        /**
         * Provides support for text representations
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         * @param description User description of Param (40 chars or less)
         * @param required <code>true</code> is param is required
         */
        public Param(String key, Class type, String description,
            boolean required) {
            this(key, type, description, required, null);
        }

        /**
         * Provides support for text representations
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         * @param description User description of Param (40 chars or less)
         * @param required <code>true</code> is param is required
         * @param sample Sample value as an example for user input
         */
        public Param(String key, Class type, String description,
            boolean required, Object sample) {
            this.key = key;
            this.type = type;
            this.description = description;
            this.required = required;
            this.sample = sample;
        }

        /**
         * Lookup Param in a user supplied map.
         * 
         * <p>
         * Type conversion will occur if required, this may result in an
         * IOException. An IOException will be throw in the Param is required
         * and the Map does not contain the Map.
         * </p>
         * 
         * <p>
         * The handle method is used to process the user's value.
         * </p>
         *
         * @param map Map of user input
         *
         * @return Parameter as specified in map
         *
         * @throws IOException if parse could not handle value
         */
        public Object lookUp(Map map) throws IOException {
            if (!map.containsKey(key)) {
                if (required) {
                    throw new IOException("Parameter " + key + " is required:"
                        + description);
                } else {
                    return null;
                }
            }

            Object value = map.get(key);

            if (value == null) {
                return null;
            }

            if (value instanceof String && (type != String.class)) {
                value = handle((String) value);
            }

            if (value == null) {
                return null;
            }

            if (!type.isInstance(value)) {
                throw new IOException(type.getName()
                    + " required for parameter " + key + ": not "
                    + value.getClass().getName());
            }

            return value;
        }

        /**
         * Convert value to text representation for this Parameter
         *
         * @param value DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String text(Object value) {
            return value.toString();
        }

        /**
         * Handle text in a sensible manner.
         * 
         * <p>
         * Performs the most common way of handling text value:
         * </p>
         * 
         * <ul>
         * <li>
         * null: If text is null
         * </li>
         * <li>
         * origional text: if type == String.class
         * </li>
         * <li>
         * null: if type != String.class and text.getLength == 0
         * </li>
         * <li>
         * parse( text ): if type != String.class
         * </li>
         * </ul>
         * 
         *
         * @param text
         *
         * @return Value as processed by text
         *
         * @throws IOException If text could not be parsed
         * @throws DataSourceException DOCUMENT ME!
         */
        public Object handle(String text) throws IOException {
            if (text == null) {
                return null;
            }

            if (type == String.class) {
                return text;
            }

            if (text.length() == 0) {
                return null;
            }

            try {
                return parse(text);
            } catch (IOException ioException) {
                throw ioException;
            } catch (Throwable throwable) {
                throw new DataSourceException("Problem creating "
                    + type.getName() + " from '" + text + "'", throwable);
            }
        }

        /**
         * Provides support for text representations
         * 
         * <p>
         * Provides basic support for common types using reflection.
         * </p>
         * 
         * <p>
         * If needed you may extend this class to handle your own custome
         * types.
         * </p>
         *
         * @param text Text representation of type should not be null or empty
         *
         * @return Object converted from text representation
         *
         * @throws Throwable DOCUMENT ME!
         * @throws IOException If text could not be parsed
         * @throws DataSourceException DOCUMENT ME!
         */
        public Object parse(String text) throws Throwable {
            Constructor constructor;

            try {
                constructor = type.getConstructor(new Class[] { String.class });
            } catch (SecurityException e) {
                //  type( String ) constructor is not public
                throw new IOException("Could not create " + type.getName()
                    + " from text");
            } catch (NoSuchMethodException e) {
                // No type( String ) constructor
                throw new IOException("Could not create " + type.getName()
                    + " from text");
            }

            try {
                return constructor.newInstance(new Object[] { text, });
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new DataSourceException("Could not create "
                    + type.getName() + ": from '" + text + "'",
                    illegalArgumentException);
            } catch (InstantiationException instantiaionException) {
                throw new DataSourceException("Could not create "
                    + type.getName() + ": from '" + text + "'",
                    instantiaionException);
            } catch (IllegalAccessException illegalAccessException) {
                throw new DataSourceException("Could not create "
                    + type.getName() + ": from '" + text + "'",
                    illegalAccessException);
            } catch (InvocationTargetException targetException) {
                throw targetException.getCause();
            }
        }
    }
}
