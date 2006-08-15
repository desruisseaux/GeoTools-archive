package org.geotools.xml.impl;

import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;


public abstract class InstanceComponentImpl implements InstanceComponent {

	/** namespace **/
	String namespace;
	/** name **/
	String name;
	/** text **/
	StringBuffer text;
	
	public XSDSchemaContent getDeclaration() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getText() {
		return text != null ? text.toString() : "";
	}

	public void setText(String text) {
		this.text = text != null ? new StringBuffer(text) : new StringBuffer();
	}

	public void addText(String text) {
		if (this.text != null) {
			this.text.append(text);
		}
		else {
			this.text = new StringBuffer(text);
		}
	}
	
	public void addText(char[] ch, int start, int length) {
		if (text == null) {
			text = new StringBuffer();
		}
		
		text.append(ch,start,length);
	}
}
