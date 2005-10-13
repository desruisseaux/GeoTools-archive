package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * Helper methods for dealing with Descriptor.
 * <p>
 * This methods opperate directly on the interfaces provided by geoapi, no
 * actual classes were harmed in the making of these utility methods.
 * </p>
 * 
 * @author Jody Garnett
 */
public class Descriptors {
	DescriptorFactory factory;

	/**
	 * Assume plugin system will hook us up with an appropriate
	 * DescriptorFactory
	 * <p>
	 * Note: we are forcing applications to explicitly specify a default such as
	 * SchemaFactoryImpl.
	 * 
	 * @param factory
	 */
	public Descriptors(DescriptorFactory factory) {
		this.factory = factory;
	}

	/**
	 * Handle subtyping in a "sensible" manner.
	 * <p>
	 * We explored using the XMLSchema of extention and restriction, and have
	 * instead opted for the traditional Java lanaguage notion of an override.
	 * <p>
	 * The concept of an overrided allows both:
	 * <ul>
	 * <li>extention - completly new attribtues are tacked on the "end" of the
	 * list
	 * <li>restriction - attribute with the same qname are used to specify
	 * additional (or replace) information provided by the parent.
	 * </ol>
	 * Note - even <b>removal</b> ( a complicated (and silly) use of
	 * restriction in XMLSchema) is supported. To remove simply override an
	 * attribute mentioned by the parent with multiplicity 0:0.
	 * </p>
	 * 
	 * @param schema
	 * @param subtype
	 * @return Descriptor resulting by extending the provided schema (collisions
	 *         on qname are treated as overrides)
	 */
	public Descriptor subtype(Descriptor schema, Descriptor extend) {
		/*
		 * if( schema instanceof AllDescriptor && subtype instanceof
		 * AllDescriptor ){ return subtype( (AllDescriptor) schema,
		 * (AllDescriptor) extend); } else if( schema instanceof
		 * ChoiceDescriptor && extend instanceof ChoiceDescriptor ){ return
		 * subtype( (ChoiceDescriptor) schema, (ChoiceDescriptor) extend); }
		 * else if( schema instanceof OrderedDescriptor && extend instanceof
		 * OrderedDescriptor ){ return subtype( (OrderedDescriptor) schema,
		 * (OrderedDescriptor) extend); } else { List<Descriptor> all = new
		 * ArrayList<Descriptor>(); all.add( schema ); all.add( extend );
		 * return factory.ordered( all, 1, 1 ); }
		 */
		try {
			return restriction(schema, extend);
		} catch (IllegalStateException couldNotRestrict) {
			return extention(schema, extend);
		}
	}

	/**
	 * Extending a schema.
	 * <p>
	 * Since we will be creating a new Descriptor we need the factory.
	 */
	public Descriptor extention(Descriptor schema, Descriptor extend) {
		if (schema instanceof AllDescriptor && extend instanceof AllDescriptor) {
			return extension((AllDescriptor) schema, (AllDescriptor) extend);
		} else if (schema instanceof ChoiceDescriptor
				&& extend instanceof ChoiceDescriptor) {
			return extension((ChoiceDescriptor) schema,
					(ChoiceDescriptor) extend);
		} else if (schema instanceof OrderedDescriptor
				&& extend instanceof OrderedDescriptor) {
			return extension((OrderedDescriptor) schema,
					(OrderedDescriptor) extend);
		} else {
			List<Descriptor> all = new ArrayList<Descriptor>();
			all.add(schema);
			all.add(extend);
			return factory.ordered(all, 1, 1);
		}
	}

	private AllDescriptor extension(AllDescriptor schema, AllDescriptor extend) {
		Set<AttributeDescriptor> all = new HashSet<AttributeDescriptor>();
		all.addAll(schema.all());
		all.addAll(extend.all());
		return factory.all(all, extend.getMinOccurs(), extend.getMaxOccurs());
	}

	private OrderedDescriptor extension(OrderedDescriptor schema,
			OrderedDescriptor extend) {
		List<Descriptor> ordered = new ArrayList<Descriptor>();
		ordered.addAll(schema.sequence());
		ordered.addAll(extend.sequence());
		return factory.ordered(ordered, extend.getMinOccurs(), extend
				.getMaxOccurs());
	}

	private ChoiceDescriptor extension(ChoiceDescriptor schema,
			ChoiceDescriptor extend) {
		Set<Descriptor> choice = new HashSet<Descriptor>();
		choice.addAll(schema.options());
		choice.addAll(extend.options());
		return factory.choice(choice, extend.getMinOccurs(), extend
				.getMaxOccurs());
	}

	/**
	 * Restriction only works on exact structure match.
	 * <p>
	 * This is the way XMLSchema handles it ...
	 * </p>
	 * 
	 * @param schema
	 * @param sub
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Descriptor restriction(Descriptor schema, Descriptor restrict) {
		Descriptor descriptor;
		if (schema instanceof AllDescriptor
				&& restrict instanceof AllDescriptor) {
			Set<AttributeDescriptor> all = restriction(((AllDescriptor) schema)
					.all(), ((AllDescriptor) restrict).all());
			descriptor = factory.all(all, restrict.getMinOccurs(), restrict
					.getMaxOccurs());
			// return null;//restriction( (AllDescriptor) schema,
			// (AllDescriptor) restict);
		} else if (schema instanceof ChoiceDescriptor
				&& restrict instanceof ChoiceDescriptor) {
			Set<Descriptor> options = restriction(((ChoiceDescriptor) schema)
					.options(), ((ChoiceDescriptor) restrict).options());
			descriptor = factory.choice(options, restrict.getMinOccurs(),
					restrict.getMaxOccurs());
			// return restriction( (ChoiceDescriptor) schema, (ChoiceDescriptor)
			// restrict);
		} else if (schema instanceof OrderedDescriptor
				&& restrict instanceof OrderedDescriptor) {
			List<Descriptor> sequence = restriction(
					((OrderedDescriptor) schema).sequence(),
					((OrderedDescriptor) restrict).sequence());
			descriptor = factory.ordered(sequence, restrict.getMinOccurs(),
					restrict.getMaxOccurs());
			// return restriction( (OrderedDescriptor) schema,
			// (OrderedDescriptor) restrict);
		} else if (schema instanceof AttributeDescriptor
				&& restrict instanceof AttributeDescriptor) {
			descriptor = restriction((AttributeDescriptor) schema,
					(AttributeDescriptor) restrict);
		} else {
			throw new IllegalStateException("Cannot restrict provided schema");
		}
		return descriptor;
	}

	/**
	 * We can only restrict node if the restricftion is a subtype that used by
	 * node.
	 * 
	 * @param node
	 * @param restrict
	 * @return restrict, iff restrict.getType() ISA node.getType()
	 */
	AttributeDescriptor restriction(AttributeDescriptor node,
			AttributeDescriptor restrict) {
		if (node.getType() == restrict.getType()) {
			return restrict;
		}
		for (AttributeType<?> type = restrict.getType(); type != null; type = type
				.getSuper()) {
			if (node.getType() == type) {
				return restrict;
			}
		}
		throw new IllegalStateException("Cannot restrict provided schema");
	}

	/*
	 * AllDescriptor restriction( AllDescriptor schema, AllDescriptor restrict ){
	 * Set<AttributeDescriptor> all = restriction( schema.all(), restrict.all() );
	 * return factory.all( all, restrict.getMinOccurs(), restrict.getMaxOccurs() ); }
	 * 
	 * OrderedDescriptor restriction( OrderedDescriptor schema,
	 * OrderedDescriptor restrict ){ List<Descriptor> sequence = restriction(
	 * schema.sequence(), restrict.sequence() ); return factory.ordered(
	 * sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() ); }
	 * 
	 * ChoiceDescriptor restriction( ChoiceDescriptor schema, ChoiceDescriptor
	 * restrict ){ Set<Descriptor> sequence = restriction( schema.options(),
	 * restrict.options() ); return factory.choice( sequence,
	 * restrict.getMinOccurs(), restrict.getMaxOccurs() ); }
	 */

	<T extends Descriptor> Set<T> restriction(Set<T> schema, Set<T> restrict) {
		Set<T> restriction = new HashSet<T>();

		Iterator<T> i = schema.iterator();
		Iterator<T> j = restrict.iterator();
		while (i.hasNext() && j.hasNext()) {
			restriction.add((T) restriction(i.next(), j.next()));
		}
		return restriction;
	}

	List<Descriptor> restriction(List<? extends Descriptor> schema,
			List<? extends Descriptor> restrict) {
		List<Descriptor> restriction = new ArrayList<Descriptor>();

		Iterator<? extends Descriptor> i = schema.iterator();
		Iterator<? extends Descriptor> j = restrict.iterator();
		while (i.hasNext() && j.hasNext()) {
			restriction.add(restriction(i.next(), j.next()));
		}
		return restriction;
	}

	/**
	 * Returns Map of prefix/namespace URI, with the namespaces found on a
	 * schema allowable content nodes. If prefixes has not been assigned to the
	 * QNames, a prefix is automattically assigned.
	 * 
	 * @param schema
	 * @return
	 */
	static public Map<String, String> namespaces(Descriptor schema) {
		List<AttributeType> types = types(schema);
		Map<String, String> namespaces = new HashMap<String, String>();
		int curr = 0;
		for (AttributeType<?> t : types) {
			QName name = t.getName();
			String ns = name.getNamespaceURI();
			String prefix = name.getPrefix();
			if (!namespaces.containsValue(ns)) {
				if ("".equals(prefix)) {
					prefix = "p" + curr++;
				}
				namespaces.put(prefix, ns);
			}
		}
		return namespaces;
	}

	/**
	 * Locate type associated with provided name, or null if not found.
	 * <p>
	 * Namespaces are not taken in count, so if two properties share the
	 * same local name, the first one that matches will be returned.
	 * </p>
	 * 
	 * @param schema
	 * @param name
	 * @return
	 */
	static public AttributeType type(Descriptor schema, String name) {
		AttributeDescriptor node = node(schema, name);
		if (node != null)
			return node.getType();
		return null;
	}

	/**
	 * Locate type associated with provided name, or null if not found.
	 * 
	 * @param schema
	 * @param name
	 * @return
	 */
	static public AttributeType type(Descriptor schema, QName name) {
		AttributeDescriptor node = node(schema, name);
		if (node != null)
			return node.getType();
		return null;
	}

	/**
	 * Finds the first node associated with the provided name disregarding
	 * namespaces
	 * 
	 * @param schema
	 * @param name
	 * @return
	 */
	static public AttributeDescriptor node(Descriptor schema, String name) {
		for (Descriptor child : list(schema)) {
			if (child instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) child;
				if (node.getType().name().equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the node associated with the provided name.
	 * 
	 * @param schema
	 * @param name
	 * @return AttributeDescriptor assoicated with provided name, or null if not
	 *         found.
	 */
	static public AttributeDescriptor node(Descriptor schema, QName name) {
		for (Descriptor child : list(schema)) {
			if (child instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) child;
				if (node.getType().getName().equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the node associated with the provided type.
	 * <p>
	 * Note a type may be included in more then one node, in which case this
	 * will only find the first one.
	 * </p>
	 * 
	 * @param schema
	 * @param type
	 * @return AttributeDescriptor assoicated with provided name, or null if not
	 *         found.
	 */
	static public AttributeDescriptor node(Descriptor schema, AttributeType type) {
		for (Descriptor child : list(schema)) {
			if (child instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) child;
				if (node.getType() == type) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * List of nodes matching AttributeType.
	 * 
	 * @param schema
	 * @param type
	 * @return List of nodes for the provided type, or empty.
	 */
	static public List<AttributeDescriptor> nodes(Descriptor schema,
			AttributeType type) {
		List<AttributeDescriptor> nodes = new ArrayList<AttributeDescriptor>();
		for (Descriptor child : list(schema)) {
			if (child instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) child;
				if (node.getType() == type) {
					nodes.add(node);
				}
			}
		}
		return nodes;
	}

	/**
	 * List of types described by this schema.
	 * <p>
	 * On the cases where order matters, the returned list preserves the order
	 * of descriptors declared in <code>schema</code>
	 * </p>
	 * 
	 * @param schema
	 * @param testType
	 * @return List of nodes for the provided type, or empty.
	 */
	static public List<AttributeType> types(Descriptor schema) {
		List<AttributeType> types = new ArrayList<AttributeType>();
		for (Descriptor child : list(schema)) {
			if (child instanceof AttributeDescriptor) {
				AttributeDescriptor node = (AttributeDescriptor) child;
				types.add(node.getType());
			}
		}
		return types;
	}

	/**
	 * True if there may be more then one AttributeType in the schema.
	 * <p>
	 * This may happen if:
	 * <ul>
	 * <li>The AttributeType is referenced by more then one node.
	 * <li>The node referencing the type has multiplicy greater then 1
	 * </ul>
	 * 
	 * @param schema
	 * @param type
	 * @return
	 */
	public static boolean multiple(Descriptor schema, AttributeType type) {
		// return maxOccurs( schema, type ) != 1;
		return maxOccurs(schema, type) > 1;
	}

	public static int maxOccurs(Descriptor schema, AttributeType type) {
		List<AttributeDescriptor> nodes = nodes(schema, type);
		if (nodes.isEmpty())
			return 0;

		int max = 0;
		for (AttributeDescriptor node : nodes) {
			if (max == Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
			max += node.getMaxOccurs();
		}
		return max;
	}

	/**
	 * Returns the list of descriptors defined in the provided schema,
	 * preserving declaration order when relevant.
	 * 
	 * @param schema
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static public List<? extends Descriptor> list(Descriptor schema) {
		// ok, if SimpleDescriptor actually extends OrderedDescriptor
		// this first comparison would bot be needed, though I couldn't
		// get SimpleDescriptor extending OrderedDescriptor and still
		// returning List<AttributeDescriptor> with my current/almost void,
		// knowledge of Java5
		/*
		 * if( schema instanceof SimpleDescriptor ){ return
		 * ((SimpleDescriptor)schema).sequence(); } else
		 */
		if (schema instanceof OrderedDescriptor) {
			return ((OrderedDescriptor) schema).sequence();
		} else if (schema instanceof AllDescriptor) {
			return new ArrayList<AttributeDescriptor>(((AllDescriptor) schema)
					.all());
		} else if (schema instanceof ChoiceDescriptor) {
			return new ArrayList<Descriptor>(((ChoiceDescriptor) schema)
					.options());
		}
		return Collections.EMPTY_LIST;
	}
}
