package org.geotools.maven.xmlcodegen;

/**
 * Bean for a constructor argument.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class BindingConstructorArgument {

	String name;
	
	String type;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
