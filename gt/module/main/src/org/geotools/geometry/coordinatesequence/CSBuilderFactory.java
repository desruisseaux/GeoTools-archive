/*
 * Created on 31-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.geometry.coordinatesequence;

/**
 * @author wolf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CSBuilderFactory {
	private static Class defaultBuilderClass;
	
	public static CSBuilder getDefaultBuilder() {
		try {
			return (CSBuilder) getDefaultBuilderClass().newInstance();
		} catch (Exception e) {
			// TODO: should we throw a better exception here? It's a fatal error anyway...
			throw new RuntimeException("Could not create a coordinate sequence builder", e);
		}
	}
	
	private static Class getDefaultBuilderClass() {
		if(defaultBuilderClass == null) {
			defaultBuilderClass = DefaultCSBuilder.class;
		}
		return defaultBuilderClass;
	}

	/**
	 * @param class1
	 */
	public static void setDefaultBuilderClass(Class builderClass) {
		if(!CSBuilder.class.isAssignableFrom(builderClass))
			throw new RuntimeException(builderClass.getName() + " does not implement the CSBuilder interface");
		defaultBuilderClass = builderClass;
		
	}
	
	
}
