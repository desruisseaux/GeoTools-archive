package org.geotools.xml;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;

/**
 * Base class for complex bindings which map to an EMF model class.
 * <p>
 * Provides implementations for:
 * <ul>
 * 	<li>{@link ComplexBinding#getProperty(Object, QName)}.
 * </ul>
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractComplexEMFBinding extends AbstractComplexBinding {

	/**
	 * Factory used to create model objects
	 */
	EFactory factory;
	
	/**
	 * Default constructor.
	 * <p>
	 * Creatign the binding with this constructor will force it to perform a 
	 * noop in the {@link #parse(ElementInstance, Node, Object)} method.
	 * </p>
	 */
	public AbstractComplexEMFBinding() {}
	
	/**
	 * Constructs the binding with an efactory.
	 * 
	 * @param factory Factory used to create model objects.
	 */
	public AbstractComplexEMFBinding( EFactory factory ) {
		this.factory = factory;
	}
	
	/**
	 * Uses EMF reflection to create an instance of the EMF model object this
	 * binding maps to. The properties of the resulting object are set using
	 * the the contents of <param>node</param>  
	 */
	public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
		//does this binding actually map to an eObject?
		if ( EObject.class.isAssignableFrom( getType() ) && factory != null ) {
			//yes, try and use the factory to dynamically create a new instance

			//get the classname
			String className = getType().getName();
			int index = className.lastIndexOf( '.' );
			if ( index != -1 ) {
				className = className.substring( index + 1 );
			}
			
			//find the proper create method
			Method create = 
				factory.getClass().getMethod( "create" + className, null );
			if ( create == null ) {
				//no dice
				return value;
			}
			
			//create the instance
			EObject eObject = (EObject) create.invoke( factory, null );
			
			//reflectivley set the properties of it
			for ( Iterator c = node.getChildren().iterator(); c.hasNext(); ) {
				Node child = (Node) c.next();
				String property = child.getComponent().getName();
				
				if ( EMFUtils.has( eObject, property ) ) {
					if ( EMFUtils.isCollection( eObject, property ) ) {
						EMFUtils.add( eObject, property, child.getValue() );
					}
					else {
						EMFUtils.set( eObject, property, child.getValue() );
					}
				}
			}
			
			return eObject;
		}
		
		//could not do it, just return whatever was passed in
		return value;
	}
	
	/**
	 * Uses EMF reflection dynamically return the property with the specified 
	 * name.
	 */
	public Object getProperty(Object object, QName name) throws Exception {
		
		if ( object instanceof EObject ) {
			EObject eObject = (EObject) object;
			if ( EMFUtils.has( eObject, name.getLocalPart() ) ) {
				return EMFUtils.get( eObject, name.getLocalPart() );
			}	
		}
		
		return null;
	}

}
