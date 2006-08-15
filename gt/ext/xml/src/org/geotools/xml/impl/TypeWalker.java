package org.geotools.xml.impl;

import org.eclipse.xsd.XSDTypeDefinition;

public class TypeWalker {

	/** bottom type in hierachy **/
	XSDTypeDefinition base;
	
	public TypeWalker(XSDTypeDefinition base) {
		this.base = base;
	}
	
	public void walk(Visitor visitor) {
		
		XSDTypeDefinition type = base; 
			
		while(type != null) {
			//do the visit, if visitor returns false, break out
			if (!visitor.visit(type))
				break;
			
			//get the next type
			if (type.equals(type.getBaseType()))
				break;
			type = type.getBaseType();
		}
	}
	
	public static interface Visitor {
		
		/**
		 * Supplies the current type to the visitor.
		 *  
		 * @param type The current type.
		 * 
		 * @return True to signal that the walk should continue, false to 
		 * signal the walk should stop. 
		 */
		boolean visit(XSDTypeDefinition type);
		
	}
}
 