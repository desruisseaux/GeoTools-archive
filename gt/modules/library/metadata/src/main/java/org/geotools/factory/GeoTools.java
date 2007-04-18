/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.awt.RenderingHints;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Version;


/**
 * Static methods relative to the global Geotools configuration.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class GeoTools {
    /**
     * The current GeoTools version. The separator character must be the dot.
     */
    private static final Version VERSION = new Version("2.4.SNAPSHOT");

    /**
     * The bindings between {@linkplain System#getProperties system properties} and
     * a hint key. This field must be declared before any call to the {@link #bind}
     * method.
     */
    private static final Map/*<String, RenderingHints.Key>*/ BINDINGS = new HashMap();

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default value to be
     * assigned to the {@link Hints#CRS_AUTHORITY_EXTRA_DIRECTORY CRS_AUTHORITY_EXTRA_DIRECTORY}
     * hint.
     */
    public static final String CRS_AUTHORITY_EXTRA_DIRECTORY =
            "org.geotools.referencing.crs-directory";
    static {
        bind(CRS_AUTHORITY_EXTRA_DIRECTORY, Hints.CRS_AUTHORITY_EXTRA_DIRECTORY);
    }

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#EPSG_DATA_SOURCE EPSG_DATA_SOURCE} hint.
     */
    public static final String EPSG_DATA_SOURCE = "org.geotools.referencing.epsg-datasource";
    static {
        bind(EPSG_DATA_SOURCE, Hints.EPSG_DATA_SOURCE);
    }

    // TODO: add more propery keys. But should we really enumerate them, or should
    //       we allow property keys for every Hints constants using some automatic
    //       registration?

    /**
     * System-wide default hints.
     */
    private static Hints hints;
    static {
        try {
           init(null); // try for default initialization 
        }
        catch (Exception t) {
            // applet cannot access system properties
            // assume they will set GeoTools hints themselves
            hints = null;
        }
    }

    /**
     * Do not allow instantiation of this class.
     */
    private GeoTools() {
    }

    /**
     * Binds the specified {@linkplain System#getProperty(String) system property}
     * to the specified key. Only one key can be binded to a given system property.
     * However the same key can be binded to more than one system property names,
     * in which case the extra system property names are aliases.
     *
     * @param  property The system property.
     * @param  key The key to bind to the system property.
     * @throws IllegalArgumentException if an other key is already bounds
     *         to the given system property.
     */
    private static void bind(final String property, final RenderingHints.Key key) {
        synchronized (BINDINGS) {
            final RenderingHints.Key old = (RenderingHints.Key) BINDINGS.put(property, key);
            if (old == null) {
                return;
            }
            // Roll back
            BINDINGS.put(property, old);
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                "property", property));
    }

    /**
     * Reports back the version of GeoTools being used.
     */
    public static Version getVersion(){
         return VERSION;
    }

    /**
     * This is the default set of Hints used for the various utility classes.
     * <p>
     * We woulld like to transition the utility classes to being injected with their
     * required factories, either by taking Hints as part of their constructor, or
     * otherwise. Making this change would be a three step process 1) create instance
     * methods for each static final class method 2) create an singleton instance of the
     * class 3) change each static final class method into a call to the singleton. With
     * this in place we could then encourage client code to make use of utility class
     * instances before eventually retiring the static final methods. 
     * </p> 
     * @return Instance returned is a copy, you can add to it if you wish;
     */
    public static Hints getDefaultHints() {
        // return null;
        if( hints == null ){
            throw new IllegalStateException( "Please call GeoTools.init( Hints )" );
        }
        return hints;
    }
    
    /** Initialize GeoTools for use */
    public static void init( Hints myHints ){
        if( myHints == null ){
            hints = getSystemPropertyHints();
        }
        else {
            hints = myHints;
        }
    }

    /**
     * This method is hard to implement as we do not have an open ended system :-(
     * <p>
     * For now we will churn through the available keys and see what we can find.
     * The difficult part is is that Keys are strongly typed, while properties
     * are not.
     * </p>
     * @return Hints Default hints as provided by system properties
     */
    private static Hints getSystemPropertyHints() {
        Hints hints = new Hints( null );
        
        Field[] keys = Hints.class.getFields();
        for( int i=0; i<keys.length; i++){
            // should ask Hint providers to register themselves here?
            Class keyType = keys[i].getType();
            if( Hints.Key.class.isAssignableFrom( keyType ) ){
                try {
                    Hints.Key key = (Hints.Key) keys[i].get( null );
                    Object systemProperty = key.getSystemProperty();
                    if( systemProperty != null ){
                        hints.put( key, systemProperty );
                    }
                }
                catch (Exception t ){
                    t.printStackTrace();
                }
            }
        }                
        return hints;
    }

    /**
     * Reports the GeoTools {@linkplain #getVersion version} number to the
     * {@linkplain System#out standard output stream}.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        arguments.out.print("GeoTools version ");
        arguments.out.println(getVersion());
    }
}
