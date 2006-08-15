package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.xml.InstanceComponent;
import org.geotools.xml.Node;

public class NodeImpl implements Node {

	private InstanceComponent component;
	private Object value;

	List children;
	List attributes;
	
	public NodeImpl(InstanceComponent component) {
		this.component = component;
		children = new ArrayList();
		attributes = new ArrayList();
	}
	
	public NodeImpl(InstanceComponent component, Object value) {
		this(component);
		this.value = value;
	}
	
	public InstanceComponent getComponent() {
		return component;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Contents of this node.
	 * <p>
	 * XXX: either return unmodifeable Collection, or return the collection directly.
	 * Client code should make the copy iff they need it. Going to try changing it and see
	 * what breaks.
	 * </p>
	 * @see org.geotools.xml.Node#getChildren()
	 */
	public List getChildren() {
		return Collections.unmodifiableList(children);
	}

	public int getNChildren() {
		return children.size();
	}
	
	public List getChildren(String name) {
		ArrayList matches = new ArrayList();
		
		if (name == null)
			return matches;
		
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Node child = (Node)itr.next();
			if (name.equals(child.getComponent().getName()))
				matches.add(child);
		}
		
		return matches;
	}

	public List getChildren(Class clazz) {
		ArrayList matches = new ArrayList();
		
		if (clazz == null)
			return matches;
		
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Node child = (Node)itr.next();
			if ( child.getValue() == null)
				continue;
			
			if ( clazz.isAssignableFrom( child.getValue().getClass() ) )
				matches.add( child );
		}
		
		return matches;
	}
	
	public Node getChild(String name) {
		if (name == null)
			return null;
		
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Node child = (Node)itr.next();
			if (name.equals(child.getComponent().getName()))
				return child;
		}
		
		return null;
	}

	public Node getChild(Class clazz) {
		if ( clazz == null )
			return null;
		
		for ( Iterator itr = children.iterator(); itr.hasNext(); ) {
			Node child = (Node) itr.next();
			if ( child.getValue() == null )
				continue;
			
			if ( clazz.isAssignableFrom( child.getValue().getClass() ) )
				return child;
		}
		
		return null;
	}
	
	public List getAttributes() {
		return new ArrayList(attributes);
	}

	public List getAttributes(Class clazz) {
		ArrayList matches = new ArrayList();
		
		if ( clazz == null ) 
			return matches;
		
		for ( Iterator a = attributes.iterator(); a.hasNext(); ) {
			Node att = (Node) a.next();
			if ( att.getValue() == null ) 
				continue;
			
			if ( clazz.isAssignableFrom( att.getValue().getClass() ) ) {
				matches.add( att );
			}
		}
	
		return matches;
		
	}
	
	public int getNAttributes() {
		return attributes.size();
	}
	
	public Node getAttribute(String name) {
		if (name == null)
			return null;
		
		for (Iterator itr = attributes.iterator(); itr.hasNext();) {
			Node att = (Node)itr.next();
			if (name.equals(att.getComponent().getName()))
				return att;
		}
		
		return null;
	}
	
	public Node getAttribute(Class clazz) {
		if ( clazz == null )
			return null;
		
		for (Iterator itr = attributes.iterator(); itr.hasNext();) {
			Node att = (Node)itr.next();
			if ( att.getValue() == null ) 
				continue;
			
			if ( clazz.isAssignableFrom( att.getValue().getClass() ) ) 
				return att;
		}
		
		return null;
	}
	
	public Object getAttributeValue(String name) {
		Node node = getAttribute(name);
		if (node != null)
			return node.getValue();
		
		return null;
	}

	public Object getAttributeValue(Class clazz) {
		
		if ( clazz == null )
			return null;
		
		for ( Iterator a = attributes.iterator(); a.hasNext(); ) {
			Node att = (Node) a.next();
			if ( att.getValue() == null )
				continue;
			
			if ( clazz.isAssignableFrom( att.getValue().getClass() ) )
				return att.getValue();
		}
		
		return null;
	}
	
	public List getAttributeValues(Class clazz) {
		ArrayList matches = new ArrayList();
		
		if ( clazz == null )
			return matches;
		
		for ( Iterator a = attributes.iterator(); a.hasNext(); ) {
			Node att = (Node) a.next();
			if ( att.getValue() == null )
				continue;
			
			if ( clazz.isAssignableFrom( att.getValue().getClass() ) )
				matches.add( att.getValue() );
		}
		
		return matches;
	}
	
	
	//additional methods
	public void addChild(Node child) {
		children.add(child);
	}
	
	public void addAttribute(Node attribute) {
		attributes.add(attribute);
	}
	
	public String toString() {
		return getComponent().getName() + "=" + getValue();
	}

	public Object getChildValue(int index) {
		return ((Node)children.get( index )).getValue();
	}

	public Object getChildValue(String name) {
		Node node = getChild( name );
		if ( node != null ) 
			return node.getValue();
		
		return null;
	}
	
	public Object getChildValue(Class clazz) {
		Node node = getChild( clazz );
		if ( node != null ) 
			return node.getValue();
		
		return null;
	}
	
	public List getChildValues(String name) {
		ArrayList matches = new ArrayList();
		
		if (name == null)
			return matches;
		
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Node child = (Node)itr.next();
			if (name.equals(child.getComponent().getName()))
				matches.add(child.getValue());
		}
		
		return matches;
	}
	
	public List getChildValues(Class clazz) {
		ArrayList matches = new ArrayList();
		
		if (clazz == null)
			return matches;
		
		for (Iterator itr = children.iterator(); itr.hasNext();) {
			Node child = (Node)itr.next();
			Object parsed = child.getValue();
			if ( parsed == null ) 
				continue;
			
			if  ( clazz.isAssignableFrom( parsed.getClass() ) )
				matches.add( parsed );
		}
		
		return matches;
	}
	
	
}
