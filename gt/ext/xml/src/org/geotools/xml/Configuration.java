/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.resources.Utilities;
import org.geotools.xs.XSConfiguration;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Responsible for configuring a parser runtime environment.
 *
 * <p>
 * Implementations have the following responsibilites:
 *
 * <ul>
 *  <li>Configuration of bindings.
 *  <li>Configuration of context used by bindings.
 *  <li>Supplying specialized handlers for looking up schemas.
 *  <li>Supplying specialized handlers for parsing schemas.
 *  <li>Declaring dependencies on other configurations
 * </ul>
 * </p>
 * <h3>Dependencies</h3>
 * <p>
 * Configurations have dependencies on one another, that result from teh fact that
 * one schema imports another. Each configuration should declare all dependencies in
 * the constructor using the {@link #addDependency(Configuration)} method.
 * <code>
 * 	<pre>
 * 	class MyConfiguration extends Configuration {
 *     public MyConfiguration() {
 *       super();
 *       
 *       addDependency( new FooConfiguration() );
 *       addDependency( new BarConfiguration() );
 *     }
 *     ...
 *  }
 * 	</pre>
 * </code>
 * </p>
 * <h3>Binding Configuration</h3>
 * <p>
 *  In able for a particular binding to be found during a parse, the
 *  configuration must first populate a container with said binding. This 
 *  can be done by returning the appropriate instance of 
 *  {@link  org.geotools.xml.BindingConfiguration} in {@link #getBindingConfiguration()}:
 *  <pre>
 *          <code>
 *  BindingConfiguration getBindingConfiguration() {
 *      return new MyBindingConfiguration();
 *  }
 *          </code>
 *  </pre>
 *  
 *  Instances of type {@link org.geotools.xml.BindingConfiguration} are used to
 *  populate a container with all the bindings from a particular schema.
 * </p>
 *
 * <h3>Context Configuration</h3>
 * <p>
 * Many bindings have dependencies on other types of objects. The pattern used
 * to satisfy these dependencies is known as <b>Constructor Injection</b>. Which
 * means that any dependencies a binding has is passed to it in its constructor.
 * For instance, the following binding has a dependency on java.util.List.
 *
 * <pre>
 *         <code>
 * class MyBinding implements SimpleBinding {
 *
 *                List list;
 *
 *                 public MyBinding(List list) {
 *                         this.list = list;
 *                 }
 * }
 *         </code>
 * </pre>
 *
 * Before a binding can be created, the container in which it is housed in must
 * be able to satisfy all of its dependencies. It is the responsibility of the
 * configuration to statisfy this criteria. This is known as configuring the
 * binding context. The following is a suitable configuration for the above
 * binding.
 *
 * <pre>
 *         <code>
 * class MyConfiguration extends Configuration {
 *	....
 *                void configureContext(MutablePicoContainer container) {
 *                        container.registerComponentImplementation(ArrayList.class);
 *                }
 * }
 *         </code>
 * </pre>
 *
 * 
 * <h3>Schema Resolution</h3>
 * <p>
 * XML instance documents often contain schema uri references that are invalid 
 * with respect to the parser, or non-existant. A configuration can supply 
 * specialized look up classes to prevent the parser from following an 
 * invalid uri and prevent any errors that may occur as a result. 
 * </p>
 * <p>
 * An instance of {@link org.eclipse.xsd.util.XSDSchemaLocationResolver} can be
 * used to override a schemaLocation referencing another schema. This can be useful 
 * when the entity parsing an instance document stores schemas in a location 
 * unkown to the entity providing hte instance document.
 * </p>
 * 
 * <p>
 * An instance of {@link org.eclipse.xsd.util.XSDSchemaLocator} can be used 
 * to provide an pre-parsed schema and prevent the parser from parsing a 
 * schemaLocation manually. This can be useful when an instance document does 
 * not supply a schemaLocation for the targetNamespace of the document.
 * <pre>
 *         <code>
 * class MyConfiguration implements Configuration {
 *
 *                XSDSchemaLocationResolver getSchemaLocationResolver() {
 *                  return new MySchemaLocationResolver();
 *                }
 *                
 *                XSDSchemaLocator getSchemaLocator() {
 *                  return new MySchemaLocator();
 *                }
 * }
 *         </code>
 * </pre>
 *
 * </p>
 * <p>
 * The XSDSchemaLocator and XSDSchemaLocationResolver implementations are used
 * in a couple of scenarios. The first is when the <b>schemaLocation</b>
 * attribute of the root element of the instance document is being parsed.
 * The schemaLocation attribute has the form:
 *
 * <pre>
 * <code>
 *         schemaLocation="namespace location namespace location ..."
 * </code>
 * </pre>
 *
 * In which (namespace,location) tuples are listed. For each each namespace
 * encountered when parsing the schemaLocation attribute, an appropriate
 * resolver / locator is looked up. If an override is not aviable, the framework
 * attempts to resolve the location part of the tuple into a schema.
 *
 * The second scenario occurs when the parsing of a schema encounters an
 * <b>import</b> or an <b>include<b> element. These elements have the form:
 *
 *  <pre>
 *  <code>
 *      &lt;import namespace="" schemaLocation=""/&gt;
 *        </code>
 *  </pre>
 *
 *  and:
 *
 *  <pre>
 *  <code>
 *      &lt;include schemaLocation=""&gt;
 *  </code>
 *        </pre>
 *
 *        respectivley. Similar to above, the schemaLocation (and namespace in the
 *        case of an import) are used to find an override. If not found they are
 *        resolved directly.
 * </p>
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 * @see org.geotools.xml.BindingConfiguration
 */
public abstract class Configuration {
	
	/**
	 * List of configurations depended on.
	 */
	private List dependencies;
	
    /**
     * Holds the schema locator instance for this configuration, which
     * in turn caches the parsed XSDSchema
     */
    private SchemaLocator schemaLocator;
    
	/**
	 * Creates a new configuration. 
	 * <p>
	 * Any dependent schemas should be added in sublcass constructor. The xml schema
	 * dependency does not have to be added.
	 * </p>
	 *
	 */
	public Configuration() {
		dependencies = new ArrayList();
		
		//bootstrap check
		if ( !( this instanceof XSConfiguration ) ) {
			dependencies.add( new XSConfiguration() );
		}
	}
	
	/**
	 * 	@return a list of direct dependencies of the configuration.
	 * 
	 */
	public final List/*<Configuration>*/ getDependencies() {
		return dependencies;
	}

	/**
	 * Returns all dependencies in the configuration dependency tree.
	 * <p>
	 * The return list contains no duplicates.
	 * </p>
	 * @return All dependencies in teh configuration dependency tree.
	 */
	public final List allDependencies() {
	
		LinkedList unpacked = new LinkedList();
		
		Stack stack = new Stack();
		stack.push( this );
		
		while( !stack.isEmpty() ) {
			Configuration c = (Configuration) stack.pop();
			if ( !unpacked.contains( c ) ) {
				unpacked.addFirst( c );
				stack.addAll( c.getDependencies() );
			}
		}
		
		return unpacked;
	}
	
	/**
	 * Adds a dependent configuration.
	 * <p>
	 * This method should only be called from the constructor.
	 * </p>
	 * @param dependency
	 */
	protected void addDependency( Configuration dependency ) {
		if ( dependencies.contains( dependency ) )
			return;
		
		dependencies.add( dependency );
	}

	
	/**
	 * @return The namespace of the configuration schema.
	 */
	abstract public String getNamespaceURI();
	
	/**
	 * Returns the url to the file definiing hte schema.
	 * <p>
	 * For schema which are defined by multiple files, this method should return the base schema 
	 * which includes all other files that define the schema.
	 * </p>
	 */
	abstract public URL getSchemaFileURL() throws MalformedURLException;
	
	/**
	 * @return The binding set for types, elements, attributes of the configuration schema.
	 */
	abstract public BindingConfiguration getBindingConfiguration();
	
	/**
	 * Returns a schema location resolver instance used to override schema location
	 * uri's encountered in an instance document.
	 * <p>
	 * This method should be overridden to return such an instance. The default 
	 * implemntation returns <code>null</code>
	 * </p>
	 * @return The schema location resolver, or <code>null</code>
	 */
	abstract public XSDSchemaLocationResolver getSchemaLocationResolver();
	
	/**
	 * Returns a schema locator, used to create imported and included schemas
	 * when parsing an instance document.
	 * <p>
	 * This method may be overriden to return such an instance. The default 
	 * implementations returns an instanceof {@link SchemaLocator}. This method 
	 * may return <code>null</code> to indicate that no such locator should be used.
	 * </p>
	 * @return The schema locator, or <code>null</code>
	 */
	public XSDSchemaLocator getSchemaLocator() {
        if(schemaLocator == null){
            schemaLocator = new SchemaLocator( this );
        }
        return schemaLocator;
	}
	
	/**
     * Configures a container which houses all the bindings used during a parse.
     *
     * @param container The container housing the binding objects.
     */
    public final MutablePicoContainer setupBindings(MutablePicoContainer container) {
    	
    	//configure bindings of all dependencies
    	for ( Iterator d = allDependencies().iterator(); d.hasNext(); ) {
    		Configuration dependency = (Configuration) d.next();
    		
    		BindingConfiguration bindings = dependency.getBindingConfiguration();
    		bindings.configure( container );
    	}
    
    	//call template method, create a new container to allow subclass to override bindings
    	MutablePicoContainer override = new DefaultPicoContainer( container );
    	configureBindings( override );
    	
    	return override;
    }
    
    /**
     * Template method allowing subclass to override any bindings.
     * 
     * @param container Container containing all bindings, keyed by {@link QName}.
     */
    protected void configureBindings( MutablePicoContainer container ) {
    	//do nothing
    }
    
    /**
     * Configures the root context to be used when parsing elements.
     *
     * @param container The container representing the context.
     */
    public final MutablePicoContainer setupContext(MutablePicoContainer container) {
    	//configure bindings of all dependencies
    	List dependencies = allDependencies();
    	for ( Iterator d = dependencies.iterator(); d.hasNext(); ) {
    		Configuration dependency = (Configuration) d.next();
    		
    		//throw locator and location resolver into context
        	XSDSchemaLocationResolver resolver = dependency.getSchemaLocationResolver() ;
        	if ( resolver != null ) {
        		QName key = new QName( dependency.getNamespaceURI(), "schemaLocationResolver" );
        		container.registerComponentInstance( key, resolver );
        	}
        	XSDSchemaLocator locator = dependency.getSchemaLocator();
        	if ( locator != null ) {
        		QName key = new QName( dependency.getNamespaceURI(), "schemaLocator" );
        		container.registerComponentInstance( key, locator );	
        	}

        	//add any additional configuration, factories and such
        	// create a new container to allow configurations to override factories in dependant
        	// configurations
        	container = new DefaultPicoContainer( container );
            dependency.configureContext( container );
    	}
    	
    	return container;
    	
    }
    
    /**
     * Configures the root context to be used when parsing elements.
     * <p>
     * The context satisifies any depenencencies needed by a binding. This is 
     * often a factory used to create something. 
     * </p>
     * <p>
     * This method should be overriden. The default implementation does nothing.
     * </p>
     *
     * @param container The container representing the context.
     */
    protected void configureContext(MutablePicoContainer container) {
    	
    }
    
    /**
     * Equals override, equality is based soley on {@link #getNamespaceURI()}.
     */
    public final boolean equals(Object obj) {
    	if ( obj instanceof Configuration ) {
    		Configuration other = (Configuration) obj;
    		return Utilities.equals( getNamespaceURI(), other.getNamespaceURI() );
    	}
    	
    	return false;
    }
    
    public final int hashCode() {
    	if ( getNamespaceURI() != null ) {
    		return getNamespaceURI().hashCode();
    	}
    	
    	return 0;
    }
}
