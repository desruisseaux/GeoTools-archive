package org.geotools.event;

/**
 * Temporary hack for cloning (for use in DuplicatorStyleVisitor).
 * 
 * @author chorner
 * @deprecated
 */
public class GTCloneUtil {
	public static Object clone(GTComponent component) throws CloneNotSupportedException {
		return ((AbstractGTComponent) component).clone();
	}
}