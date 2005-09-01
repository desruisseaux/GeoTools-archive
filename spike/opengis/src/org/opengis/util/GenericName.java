package org.opengis.util;


/**
 * Note: I understand that this is different from what opengis uses right now!
 * <p>
 * OpenGIS uses:
 * <ul>
 * <li>GenericName - common interface for type and attribtue name in context of a namespace
 * <li>LocalName - Identifier within a namespace, scope is a GenericName and there is a List LocalNames
 * <li>ScopedName - Fully qualified identifier for an object, uses a LocalName as head and a GenericName as its tail
 * <li>Identifier - comment says "Value uniquely identifying an object within an namespace, problem
 *     is there is nothing in the interface called namespace (it appears to all be about CRS factory).
 * </ul>
 * In short the above seems at odds with what is expected, I am changing this to make the code clear.
 * The GeoAPI list can explain the error of my understanding at a later time.
 * </p>
 */
public class GenericName {
	GenericName namespace = null;
	String name = null;

	public GenericName( String name ){
		this( null, name );
	}
	public GenericName( GenericName namespace, String name ){
		this.namespace = namespace;
		this.name = name;
	}
	/**
	 * Context in which this name is to be understood.
	 * @return Context, or null for root.
	 */
    public GenericName getNamespace(){
    	return namespace;
    }
    public String getName(){
    	return name;
    }
    public String toString() {
    	if( namespace == null ) return name;
    	return namespace +"/"+name;
    }
}
