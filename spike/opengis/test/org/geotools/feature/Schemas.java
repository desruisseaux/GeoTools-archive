package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opengis.feature.schema.AllSchema;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceSchema;
import org.opengis.feature.schema.OrderedSchema;
import org.opengis.feature.schema.Schema;
import org.opengis.feature.schema.SchemaFactory;
import org.opengis.feature.type.AttributeType;

/**
 * Helper methods for dealing with Schema.
 * <p>
 * This methods opperate directly on the interfaces provided by geoapi,
 * no actual classes were harmed in the making of these utility methods.
 * </p>
 * @author Jody Garnett
 */
public class Schemas {
	SchemaFactory factory;	
	/**
	 * Assume plugin system will hook us up with an appropriate SchemaFactory
	 * <p>
	 * Note: we are forcing applications to explicitly specify a default such as
	 * SchemaFactoryImpl.
	 * @param factory
	 */
	public Schemas( SchemaFactory factory ){
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
	public Schema subtype( Schema schema, Schema subtype ){
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
	 * Since we will be creating a new Schema we need the factory.
	 */
	public Schema extention( Schema schema, Schema extend ){
		if( schema instanceof AllSchema && extend instanceof AllSchema ){
			return extension( (AllSchema) schema, (AllSchema) extend);
		}
		else if( schema instanceof ChoiceSchema && extend instanceof ChoiceSchema ){
			return extension( (ChoiceSchema) schema, (ChoiceSchema) extend);
		}
		else if( schema instanceof OrderedSchema && extend instanceof OrderedSchema ){
			return extension( (OrderedSchema) schema, (OrderedSchema) extend);
		}
		else {
			List<Schema> all = new ArrayList<Schema>();
			all.add( schema );
			all.add( extend );
			return factory.ordered( all, 1, 1 );
		}
	}
	private AllSchema extension( AllSchema schema, AllSchema extend ){
		List<Schema> all = new ArrayList<Schema>();
		all.addAll( schema.all() );
		all.addAll( extend.all() );		
		return factory.all( all, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private OrderedSchema extension( OrderedSchema schema, OrderedSchema extend ){
		List<Schema> ordered = new ArrayList<Schema>();
		ordered.addAll( schema.sequence() );
		ordered.addAll( extend.sequence() );
		return factory.ordered( ordered, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private ChoiceSchema extension( ChoiceSchema schema, ChoiceSchema extend ){
		List<Schema> choice = new ArrayList<Schema>();
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
	public Schema restriction( Schema schema, Schema restrict ){
		if( schema instanceof AllSchema && restrict instanceof AllSchema ){
			return restriction( (AllSchema) schema, (AllSchema) restrict);
		}
		else if( schema instanceof ChoiceSchema && restrict instanceof ChoiceSchema ){
			return restriction( (ChoiceSchema) schema, (ChoiceSchema) restrict);
		}
		else if( schema instanceof OrderedSchema && restrict instanceof OrderedSchema ){
			return restriction( (OrderedSchema) schema, (OrderedSchema) restrict);
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
	AllSchema restriction( AllSchema schema, AllSchema restrict ){
		Collection<Schema> all = restriction( schema.all(), restrict.all() );		
		return factory.all( all, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	OrderedSchema restriction( OrderedSchema schema, OrderedSchema restrict ){
		List<Schema> sequence = restriction( schema.sequence(), restrict.sequence() );
		return factory.ordered( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	ChoiceSchema restriction( ChoiceSchema schema, ChoiceSchema restrict ){
		Collection<Schema> sequence = restriction( schema.options(), restrict.options() );
		return factory.choice( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	Collection<Schema> restriction( Collection<Schema> schema, Collection<Schema> restrict ){
		List<Schema> restriction = new ArrayList<Schema>();
		
		Iterator<Schema> i = schema.iterator();
		Iterator<Schema> j = restrict.iterator();
		while( i.hasNext() && j.hasNext() ){
			restriction.add( restriction( i.next(), j.next() ) );
		}
		return restriction;
	}
	List<Schema> restriction( List<Schema> schema, List<Schema> restrict ){
		List<Schema> restriction = new ArrayList<Schema>();
		
		Iterator<Schema> i = schema.iterator();
		Iterator<Schema> j = restrict.iterator();
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
	static public AttributeType type( Schema schema, String name ){
		AttributeDescriptor node = node( schema, name );
		if( node != null ) return node.getType();
		return null;
	}
	/**
	 * Finds the node associated with the provided name.
	 * 
	 * @param schema
	 * @param name
	 * @return AttributeDescriptor assoicated with provided name, or null if not found.
	 */
	static public AttributeDescriptor node( Schema schema, String name ){
		for( Schema child : list( schema ) ){
			if( child instanceof AttributeDescriptor ){
				AttributeDescriptor node = (AttributeDescriptor) child;
				if( node.getType().name().equals( name )){
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
	static public AttributeDescriptor node( Schema schema, AttributeType type){
		for( Schema child : list( schema ) ){
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
	static public List<AttributeDescriptor> nodes( Schema schema, AttributeType type ){
		List<AttributeDescriptor> nodes = new ArrayList<AttributeDescriptor>();
		for( Schema child : list( schema ) ){
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
	static public Set<AttributeType> types( Schema schema ){
		Set<AttributeType> types = new HashSet<AttributeType>();
		for( Schema child : list( schema ) ){
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
	public static boolean multiple( Schema schema, AttributeType type ){
		return maxOccurs( schema, type ) != 1;
	}
	public static int maxOccurs( Schema schema, AttributeType type ){
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
	static public Collection<Schema> list( Schema schema ){
		if( schema instanceof OrderedSchema ){
			return ((OrderedSchema)schema).sequence();
		}
		else if( schema instanceof AllSchema ){
			return ((AllSchema)schema).all();
		}
		else if( schema instanceof ChoiceSchema ){
			return ((ChoiceSchema)schema).options();
		}
		return Collections.EMPTY_LIST;
	}	
}
