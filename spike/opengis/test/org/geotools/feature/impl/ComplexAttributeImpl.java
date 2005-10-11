package org.geotools.feature.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

public class ComplexAttributeImpl implements ComplexAttribute {
	private transient int HASHCODE = -1;

	protected final ComplexType TYPE;

	protected final String ID;

	protected List<Attribute> attribtues;

	private List<AttributeType> types = null;

	private List<Object> values = null;

	public ComplexAttributeImpl(ComplexType type) {
		this(null, type);
	}

	public ComplexAttributeImpl(String id, ComplexType<?> type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		if (type.isAbstract()) {
			throw new UnsupportedOperationException(type.getName()
					+ " is abstract");
		}
		ID = id;
		TYPE = type;
		attribtues = new ArrayList<Attribute>();
	}

	public ComplexType<?> getType() {
		return TYPE;
	}

	public String getID() {
		return ID;
	}

	/* public List<Attribute> getAttributes() { */
	public List<Attribute> get() {
		//return Collections.unmodifiableList(this.attribtues);
		/**
		 * GR: Really we should return a defensive copy, allowing external 
		 * modification to be used in set(List<Attribute>)
		 */
		return new ArrayList<Attribute>(this.attribtues);
	}

	public void set(List<Attribute> newValue) throws IllegalArgumentException {
		// assume the Attributes contents are valid,
		// since they must have been established through
		// Attribute.set(Object) which performs content
		// validation, so we have to perform schema validation here
		getType().getDescriptor().validate(newValue);
		this.attribtues = new ArrayList<Attribute>(newValue);
		this.values = null;
		this.types = null;
	}

	/**
	 * Inserts the specified Attribute at the specified position in this
	 * atttribute's contents list. Shifts the element currently at that position
	 * (if any) and any subsequent elements to the right (adds one to their
	 * indices).
	 * 
	 * @param index
	 *            index at which the specified element is to be inserted.
	 * @param value
	 *            Attribute to be inserted.
	 * 
	 * @throws NullPointerException
	 *             if the specified value is null.
	 * @throws IllegalArgumentException
	 *             if some aspect of the specified element prevents it from
	 *             being added to this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index &lt; 0 || index &gt;
	 *             size()).
	 */
	public void add(int index, Attribute value) {
		List<Attribute> content = new ArrayList<Attribute>(this.attribtues);
		content.add(index, value);
		set(content);
	}

	/**
	 * Removes the element at the specified position in this attribute's
	 * contents list. Shifts any subsequent attributes to the left (subtracts
	 * one from their indices), and revalidates the contents against the schema.
	 * <p>
	 * If the resulting content structure does not validates against the schema,
	 * an IllegalArgumentException is thrown and this Attribute's contents are
	 * not modified.
	 * </p>
	 * 
	 * @param index
	 *            the index of the element to removed.
	 * @return the element previously at the specified position.
	 * 
	 * @throws UnsupportedOperationException
	 *             if the <tt>remove</tt> method is not supported by this
	 *             list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index &lt; 0 || index &gt;=
	 *             size()).
	 */
	public Attribute remove(int index) {
		List<Attribute> content = new ArrayList<Attribute>(this.attribtues);
		Attribute oldValue = content.remove(index);
		getType().getDescriptor().validate(content);
		this.attribtues = content;
		return oldValue;
	}

	/**
	 * Represents just enough info to convey the idea of this being a "view"
	 * into getAttribtues.
	 */
	public synchronized List<AttributeType> types() {
		if (types == null) {
			types = createTypesView(get());
		}
		return types;
	}

	/** Factory method so subclasses can optimize */
	protected List<AttributeType> createTypesView(final List<Attribute> source) {
		return new AbstractList<AttributeType>() {
			@Override
			public AttributeType get(int index) {
				return source.get(index).getType();
			}

			@Override
			public int size() {
				return source.size();
			}

			@Override
			public AttributeType remove(int index) {
				Attribute removed = source.remove(index);
				if (removed != null) {
					return removed.getType();
				}
				return null;
			}

			/**
			 * Unsupported.
			 * <p>
			 * We may be able to do this for nilable types, or types that have a
			 * default value.
			 * </p>
			 * 
			 * @param index
			 * @param type
			 */
			@Override
			public void add(int arg0, AttributeType type) {
				throw new UnsupportedOperationException(
						"Cannot add directly to types");
			}
		};
	}

	public synchronized List<Object> values() {
		if (values == null) {
			values = createValuesView(get());
		}
		return values;
	}

	/** Factory method so subclasses can optimize */
	protected List<Object> createValuesView(final List<Attribute> source) {
		return new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return source.get(index).get();
			}

			@Override
			public Object set(int index, Object value) {
				Object replaced = source.get(index).get();
				source.get(index).set(value);
				return replaced;
			}

			@Override
			public int size() {
				return source.size();
			}

			@Override
			public AttributeType remove(int index) {
				Attribute removed = source.remove(index);
				if (removed != null) {
					return removed.getType();
				}
				return null;
			}

			/**
			 * Unsupported, we can support this for flat schema.
			 * <p>
			 * We may be able to do this after walking the schema and figuring
			 * out that there is only one binding for the provided object.
			 * </p>
			 * 
			 * @param index
			 * @param testType
			 */
			@Override
			public void add(int index, Object value) {
				throw new UnsupportedOperationException(
						"Cannot add directly to types");
			}
		};
	}

	/**
	 * There is no perscribed default binding for complex content.
	 * <p>
	 * Tempting to support a couple things according to assoiated type:
	 * <ul>
	 * <li>List.class - return ununmodifiable values()
	 * <li>Array - return array of values()
	 * </ul>
	 * But really we should just return null and force client code to document
	 * what it is doing.
	 * </p>
	 */
	/*
	 * public Object get() { Class binding = getType().getBinding(); if
	 * (binding.isArray() && binding.getComponentType() == Object.class) {
	 * return values().toArray(); } else if
	 * (List.class.isAssignableFrom(binding)) { return
	 * Collections.unmodifiableList(values()); } return null; }
	 */

	/**
	 * GR: note for consistency the default binding for complex types was
	 * established to List.class, so any complex type's content is a List<Attribute>,
	 * at the difference of simple types where the binding is an atomic class. I
	 * guess the idea of not having a prescribed type for complex content was
	 * driven by the initial thought of Geometry being complex, which is in GML
	 * land, but in Java world we have a primitive type for geometries, as it
	 * may be the case with any other concrete class. So it is far more
	 * consistent to have List<Attribute>.class as the prescribed type of
	 * Complex, since it is actually the only content a complex may have.
	 * 
	 * Accordingly, this method has moved to set(List<Attribute>).
	 * 
	 * No default binding perscribed by interface.
	 * 
	 * @see ComplexAttributeImpl.get()
	 */
	@SuppressWarnings("unchecked")
	public void set(Object newValue) {
		Class binding = getType().getBinding();
		if (binding.isArray() && binding.getComponentType() == Object.class
				&& newValue instanceof Object[]) {
			Object values[] = (Object[]) newValue;
			if (values().size() == values.length) {
				List<Object> glomp = values();
				for (int i = 0; i < values.length; i++) {
					glomp.set(i, values[i]);
				}
			}
		} else if (List.class.isAssignableFrom(binding)) {
			List<Object> values = (List<Object>) newValue;
			if (values().size() == values.size()) {
				List<Object> glomp = values();
				for (int i = 0; i < values.size(); i++) {
					glomp.set(i, values.get(i));
				}
			}
		}
		throw new UnsupportedOperationException(
				"No modification with out implementation!");
	}

	public Object get(String name) {
		return get(TYPE.type(name));
	}

	public Object get(AttributeType type) {
		if (Descriptors.multiple(TYPE.getDescriptor(), type)) {
			List<Object> got = new ArrayList<Object>();
			for (Attribute attribute : attribtues) {
				if (attribute.getType() == type) {
					got.add(attribute.get());
				}
			}
			return got;
		} else {
			for (Attribute attribute : attribtues) {
				if (attribute.getType() == type) {
					return attribute.get();
				}
			}
			return null;
		}
	}

	public int hashCode() {
		if (HASHCODE == -1) {
			HASHCODE = 23 + (getType().hashCode() * attribtues.hashCode() * (ID == null ? 1
					: ID.hashCode()));
		}
		return HASHCODE;
	}
}