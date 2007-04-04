package org.geotools.factory;

import java.lang.reflect.Field;

public class GeoTools {
    
    private static Hints hints;
    static {
        try {
           init( null ); // try for default initialization 
        }
        catch( Throwable t ){            
            // applet cannot access system properties
            // assume they will set GeoTools hints themselves
            hints = null;
        }
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
    public static Hints getDefaultHints(){
        return null;
//        if( hints == null ){
//            throw new IllegalStateException( "Please call GeoTools.init( Hints )" );
//        }
//        return new Hints( hints );
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
            if( Hints.FileKey.class.isAssignableFrom( keyType ) ){
                try {
                    Hints.FileKey fileKey = (Hints.FileKey) keys[i].get( null );
                    Object systemProperty = fileKey.getSystemProperty();
                    if( systemProperty != null ){
                        hints.put( fileKey, systemProperty );
                    }
                }
                catch (Throwable t ){
                    t.printStackTrace();
                }
            }
            if( Hints.Key.class.isAssignableFrom( keyType ) ){
                try {
                    Hints.Key key = (Hints.Key) keys[i].get( null );
                    Object systemProperty = key.getSystemProperty();
                    if( systemProperty != null ){
                        hints.put( key, systemProperty );
                    }
                }
                catch (Throwable t ){
                    t.printStackTrace();
                }
            }
        }                
        return hints;
    }

    /** Used to quickly report back the version of GeoTools being used */
    public static String getVersion(){
         return "2.4";
    }
}
