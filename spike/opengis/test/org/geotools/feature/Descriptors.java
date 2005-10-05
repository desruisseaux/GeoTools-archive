package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * This methods opperate directly on the interfaces provided by geoapi,
 * no actual classes were harmed in the making of these utility methods.
 * </p>
 * @author Jody Garnett
 */
public class Descriptors {
	DescriptorFactory factory;	
	/**
	 * Assume plugin system will hook us up with an appropriate DescriptorFactory
	 * <p>
	 * Note: we are forcing applications to explicitly specify a default such as
	 * SchemaFactoryImpl.
	 * @param factory
	 */
	public Descriptors( DescriptorFactory factory ){
		this.factory = factory;
	}
	/**
	 * Handle subtyping in a "sensible" manner.
	 * <p>
	 * If the subtype is an exact match (excepting multiplicity) it can serve
	 * as a restriction. If not it is used as an extention.
	 * </p>
	 * @param schema
	 * @param subtype
	 * @return
	 */
	public Descriptor subtype( Descriptor schema, Descriptor subtype ){
		try {
			return restriction( schema, subtype );
		}
		catch( IllegalStateException couldNotRestrict ){
			return extention( schema, subtype );
		}
	}
	/**
	 * Extending a schema.
	 * <p>
	 * Since we will be creating a new Descriptor we need the factory.
	 */
	public Descriptor extention( Descriptor schema, Descriptor extend ){
		if( schema instanceof AllDescriptor && extend instanceof AllDescriptor ){
			return extension( (AllDescriptor) schema, (AllDescriptor) extend);
		}
		else if( schema instanceof ChoiceDescriptor && extend instanceof ChoiceDescriptor ){
			return extension( (ChoiceDescriptor) schema, (ChoiceDescriptor) extend);
		}
		else if( schema instanceof OrderedDescriptor && extend instanceof OrderedDescriptor ){
			return extension( (OrderedDescriptor) schema, (OrderedDescriptor) extend);
		}
		else {
			List<Descriptor> all = new ArrayList<Descriptor>();
			all.add( schema );
			all.add( extend );
			return factory.ordered( all, 1, 1 );
		}
	}
	private AllDescriptor extension( AllDescriptor schema, AllDescriptor extend ){
		List<Descriptor> all = new ArrayList<Descriptor>();
		all.addAll( schema.all() );
		all.addAll( extend.all() );		
		return factory.all( all, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private OrderedDescriptor extension( OrderedDescriptor schema, OrderedDescriptor extend ){
		List<Descriptor> ordered = new ArrayList<Descriptor>();
		ordered.addAll( schema.sequence() );
		ordered.addAll( extend.sequence() );
		return factory.ordered( ordered, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private ChoiceDescriptor extension( ChoiceDescriptor schema, ChoiceDescriptor extend ){
		List<Descriptor> choice = new ArrayList<Descriptor>();
		choice.addAll( schema.options() );
		choice.addAll( extend.options() );
		return factory.choice( choice, extend.getMinOccurs(), extend.getMaxOccurs() );
	}	
	/**
	 * Restriction only works on exact structure match.
	 * <p>
	 * This is the way XMLSchema handles it ...
	 * </p>
	 * @param schema
	 * @param sub
	 * @return
	 */	
	public Descriptor restriction( Descriptor schema, Descriptor restrict ){
		if( schema instanceof AllDescriptor && restrict instanceof AllDescriptor ){
			return restriction( (AllDescriptor) schema, (AllDescriptor) restrict);
		}
		else if( schema instanceof ChoiceDescriptor && restrict instanceof ChoiceDescriptor ){
			return restriction( (ChoiceDescriptor) schema, (ChoiceDescriptor) restrict);
		}
		else if( schema instanceof OrderedDescriptor && restrict instanceof OrderedDescriptor ){
			return restriction( (OrderedDescriptor) schema, (OrderedDescriptor) restrict);
		}
		else if( schema instanceof AttributeDescriptor && restrict instanceof AttributeDescriptor ){
			return restriction( (AttributeDescriptor) schema, (AttributeDescriptor) restrict);
		}
		throw new IllegalStateException( "Cannot restrict provided schema" );
	}
	
	/**
	 * We can only restrict node if the restricftion is a subtype that used by node.
	 * 
	 * @param node
	 * @param restrict
	 * @return restrict, iff restrict.getType() ISA node.getType()
	 */
	AttributeDescriptor restriction( AttributeDescriptor node, AttributeDescriptor restrict ){
		if( node.getType() == restrict.getType() ){
			return restrict;
		}
		for( AttributeType type = restrict.getType(); type != null; type = type.getSuper() ){
			if( node.getType() == type ){
				return restrict; 
			}
		}
		throw new IllegalStateException( "Cannot restrict provided schema" );
	}
	AllDescriptor restriction( AllDescriptor schema, AllDescriptor restrict ){
		Collection<Descriptor> all = restriction( schema.all(), restrict.all() );		
		return factory.all( all, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	OrderedDescriptor restriction( OrderedDescriptor schema, OrderedDescriptor restrict ){
		List<Descriptor> sequence = restriction( schema.sequence(), restrict.sequence() );
		return factory.ordered( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	ChoiceDescriptor restriction( ChoiceDescriptor schema, ChoiceDescriptor restrict ){
		Collection<Descriptor> sequence = restriction( schema.options(), restrict.options() );
		return factory.choice( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	Collection<Descriptor> restriction( Collection<Descriptor> schema, Collection<Descriptor> restrict ){
		List<Descriptor> restriction = new ArrayList<Descriptor>();
		
		Iterator<Descriptor> i = schema.iterator();
		Iterator<Descriptor> j = restrict.iterator();
		while( i.hasNext() && j.hasNext() ){
			restriction.add( restriction( i.next(), j.next() ) );
		}
		return restriction;
	}
	List<Descriptor> restriction( List<Descriptor> schema, List<Descriptor> restrict ){
		List<Descriptor> restriction = new ArrayList<Descriptor>();
		
		Iterator<Descriptor> i = schema.iterator();
		Iterator<Descriptor> j = restrict.iterator();
		while( i.hasNext() && j.hasNext() ){
			restriction.add( restriction( i.next(), j.next() ) );
		}
		return restriction;
	}	
	/**
	 * Locate type associated with provided name, or null if not found.
	 * 
	 * @param schema
	 * @param name
	 * @return
	 */
	static public AttributeType type( Descriptor schema, String name ){
		return type(schema, new QName(name));
	}

	/**
	 * Locate type associated with provided name, or null if not found.
	 * 
	 * @param schema
	 * @param name
	 * @return
	 */
	static public AttributeType type( Descriptor schema, QName name ){
		AttributeDescriptor node = node( schema, name );
		if( node != null ) return node.getType();
		return null;
	}

	static public AttributeDescriptor node( Descriptor schema, String name ){
		return node(schema, new QName(name));
	}
		
	/**
	 * Finds the node associated with the provided name.
	 * 
	 * @param schema
	 * @param name
	 * @return AttributeDescriptor assoicated with provided name, or null if not found.
	 */
	static public AttributeDescriptor node( Descriptor schema, QName name ){
		for( Descriptor child : list( schema ) ){
			if( child instanceof AttributeDescriptor ){
				AttributeDescriptor node = (AttributeDescriptor) child;
				if( node.getType().getName().equals( name )){
					return node;
				}
			}
		}
		return null;
	}
	/**
	 * Finds the node associated with the provided type.
	 * <p>
	 * Note a type may be included in more then one node, in which
	 * case this will only find the first one.
	 * </p>
	 * @param schema
	 * @param type
	 * @return AttributeDescriptor assoicated with provided name, or null if not found.
	 */
	static public AttributeDescriptor node( Descriptor schema, AttributeType type){
		for( Descriptor child : list( schema ) ){
			if( child instanceof AttributeDescriptor ){
				AttributeDescriptor node = (AttributeDescriptor) child;
				if( node.getType() == type ){
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
	static public List<AttributeDescriptor> nodes( Descriptor schema, AttributeType type ){
		List<AttributeDescriptor> nodes = new ArrayList<AttributeDescriptor>();
		for( Descriptor child : list( schema ) ){
			if( child instanceof AttributeDescriptor ){
				AttributeDescriptor node = (AttributeDescriptor) child;
				if( node.getType() == type ){
					nodes.add( node );
				}
			}
		}
		return nodes;
	}
	/**
	 * Set of types described by this schema.
	 * 
	 * @param schema
	 * @param type
	 * @return List of nodes for the provided type, or empty.
	 */
	static public Set<AttributeType> types( Descriptor schema ){
		Set<AttributeType> types = new HashSet<AttributeType>();
		for( Descriptor child : list( schema ) ){
			if( child instanceof AttributeDescriptor ){
				AttributeDescriptor node = (AttributeDescriptor) child;
				types.add( node.getType() );
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
	public static boolean multiple( Descriptor schema, AttributeType type ){
		return maxOccurs( schema, type ) != 1;
	}
	public static int maxOccurs( Descriptor schema, AttributeType type ){
		List<AttributeDescriptor> nodes = nodes( schema, type );
		if( nodes.isEmpty() ) return 0;
		
		int max = 0;
		for( AttributeDescriptor node : nodes ){
			if( max == Integer.MAX_VALUE ){
				return Integer.MAX_VALUE;
			}
			max += node.getMaxOccurs();
		}
		return max;
	}
	@SuppressWarnings("unchecked")
	static public Collection<Descriptor> list( Descriptor schema ){
		if( schema instanceof OrderedDescriptor ){
			return ((OrderedDescriptor)schema).sequence();
		}
		else if( schema instanceof AllDescriptor ){
			return ((AllDescriptor)schema).all();
		}
		else if( schema instanceof ChoiceDescriptor ){
			return ((ChoiceDescriptor)schema).options();
		}
		return Collections.EMPTY_LIST;
	}	
}
