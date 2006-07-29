package org.geotools.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Hint object with known parameters for XML parsing.
 * 
 * @author Jesse
 */
public class XMLHandlerHints implements Map {

    /** 
     * Declares the schemas to use for parsing.  
     * Value must be a java.util.Map of <String,URI> objects
     * where String is the Namespace and URI is the URL to use to load the schema. 
     */
    public static final String NAMESPACE_MAPPING = "NAMESPACE_MAPPING";
    /** Declares a FlowHandler for the parser to use */
    public final static String FLOW_HANDLER_HINT = "FLOW_HANDLER_HINT";
    /** Tells the parser to "Stream" */
    public static final String STREAM_HINT = "org.geotools.xml.gml.STREAM_HINT";

    private Map map=new HashMap();
    public void clear() {
        map.clear();
    }

    public boolean containsKey( Object key ) {
        return map.containsKey(key);
    }

    public boolean containsValue( Object value ) {
        return map.containsValue(value);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public boolean equals( Object o ) {
        return map.equals(o);
    }

    public Object get( Object key ) {
        return map.get(key);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Object put( Object arg0, Object arg1 ) {
        return map.put(arg0, arg1);
    }

    public void putAll( Map arg0 ) {
        map.putAll(arg0);
    }

    public Object remove( Object key ) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection values() {
        return map.values();
    }
    
    
    

}
