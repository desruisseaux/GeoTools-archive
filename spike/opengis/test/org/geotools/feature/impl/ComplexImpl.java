package org.geotools.feature.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.Schemas;
import org.opengis.feature.Attribute;
import org.opengis.feature.Complex;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Type;

public class ComplexImpl implements Complex {
	protected final String ID;
	protected final ComplexType TYPE;
	
	protected List<Attribute> attribtues;
	
	private List<Type> types = null;	
	private List<Object> values = null;
	
	public ComplexImpl( ComplexType type ){
		this( null, type );
	}
	public ComplexImpl( String id, ComplexType type ){
		ID = id;
		TYPE = type;
		attribtues = new ArrayList<Attribute>();
	}
	public ComplexType getType() {
		return TYPE;
	}
	public String getID() {
		return ID;
	}
	public List<Attribute> getAttributes() {
		return attribtues;
	}
	/**
	 * Represents just enough info to convey the idea of this being a "view" into getAttribtues.
	 */
	public synchronized List<Type> types() {
		if( types == null ){
			types = createTypesView( attribtues );
		}
		return types;
	}
	/** Factory method so subclasses can optimize */
	protected List<Type> createTypesView( final List<Attribute> source ){
		return new AbstractList<Type>(){
			@Override
			public Type get(int index) {
				return attribtues.get(index).getType();
			}
			@Override
			public int size() {
				return attribtues.size();
			}
			@Override
			public Type remove(int index) {
				Attribute removed = attribtues.remove(index);
				if( removed != null ){
					return removed.getType();
				}
				return null;
			}
			/** Unsupported.
			 * <p>
			 * We may be able to do this for nilable types, or types that have a
			 * default value.
			 * </p>
			 * @param index
			 * @param type
			 */
			@Override
			public void add(int arg0, Type type) {
				throw new UnsupportedOperationException("Cannot add directly to types");
			}
		};
	}

	public synchronized List<Object> values() {
		if( values == null ){
			values = createValuesView( attribtues );
		}
		return values;
	}
	/** Factory method so subclasses can optimize */
	protected List<Object> createValuesView( final List<Attribute> source ){
		return new AbstractList<Object>(){
			@Override
			public Object get(int index) {
				return attribtues.get(index).get();
			}
			@Override
			public Object set(int index, Object value) {
				Object replaced = attribtues.get(index).get();				
				attribtues.get(index).set( value );
				return replaced;
			}

			@Override
			public int size() {
				return attribtues.size();
			}
			@Override
			public Type remove(int index) {
				Attribute removed = attribtues.remove(index);
				if( removed != null ){
					return removed.getType();
				}
				return null;
			}
			/**
			 * Unsupported, we can support this for flat schema.
			 * <p>
			 * We may be able to do this after walking the schema
			 * and figuring out that there is only one binding for the
			 * provided object.
			 * </p>
			 * @param index
			 * @param type
			 */
			@Override
			public void add(int index, Object value) {
				throw new UnsupportedOperationException("Cannot add directly to types");
			}
		};
	}

	public String name() {
		return getType().getName().toString();
	}

	/**
	 * There is no perscribed default binding for complex content.
	 * <p>
	 * Tempting to support a couple things according to assoiated type:
	 * <ul>
	 * <li>List.class - return ununmodifiable vaqlues()
	 * <li>Array - return array of values()
	 * </ul>
	 * But really we should just return null and force client code to document
	 * what it is doing.
	 * </p>
	 */
	public Object get() {
		Class binding = getType().getBinding();
		if( binding.isArray() && binding.getComponentType() == Object.class ){
			return values().toArray();
		}
		else if ( List.class.isAssignableFrom( binding )){
			return Collections.unmodifiableList( values() );
		}
		return null;
	}
	/**
	 * No default binding perscribed by interface.
	 * @see ComplexImpl.get()
	 */
	@SuppressWarnings("unchecked")
	public void set(Object newValue) {
		Class binding = getType().getBinding();
		if( binding.isArray() && binding.getComponentType() == Object.class
				&& newValue instanceof Object[]){ 
			Object values[] = (Object[]) newValue;
			if( values().size() == values.length ){
				List<Object> glomp = values();
				for( int i = 0; i<values.length; i++ ){
					glomp.set( i, values[i] );
				}
			}
		}
		else if ( List.class.isAssignableFrom( binding )){
			List<Object> values = (List<Object>) newValue;
			if( values().size() == values.size() ){
				List<Object> glomp = values();
				for( int i = 0; i<values.size(); i++ ){
					glomp.set( i, values.get(i) );
				}
			}
		}
		throw new UnsupportedOperationException("No modification with out implementation!");
	}
	public Object get(String name) {
		return get( TYPE.type( name ));
	}
	public Object get(Type type) {
		if( Schemas.multiple( TYPE.getSchema(),type ) ){
			for( Attribute attribute : attribtues ){
				if( attribute.getType() == type ){
					return attribute.get();
				}
			}
			return null;
		}
		else {
			List<Object> got = new ArrayList<Object>();
			for( Attribute attribute : attribtues ){
				if( attribute.getType() == type ){
					got.add( attribute.get() );
				}
			}
			return got;
		}
	}
}