package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.List;

import org.geotools.xml.Binding;
import org.geotools.xml.ComplexBinding;

/**
 * Gets properties from a parent object by visiting bindings in the hierachy.
 * The object properties are stored as name, object tuples.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GetPropertiesExecutor implements BindingWalker.Visitor {

	/** the parent object */
	Object parent;
	
	/** the properties */
	List properties;
	
	public GetPropertiesExecutor( Object parent ) {
		this.parent = parent;
		properties = new ArrayList();
	}

	public List getProperties() {
		return properties;
	}
	
	public void visit(Binding binding) {

		if (binding instanceof ComplexBinding) {
			ComplexBinding complex = (ComplexBinding)binding;
			
			try {
				List properties = complex.getProperties( parent );
				if ( properties != null ) {
					this.properties.addAll( properties );
				}
			} 
			catch (Exception e) {
				String msg = "Failed to get properties. Binding for " + complex.getTarget();
				throw new RuntimeException( msg ,  e );
			}
		}
		
	}

}
