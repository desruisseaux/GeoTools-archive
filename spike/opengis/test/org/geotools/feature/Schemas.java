package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opengis.feature.schema.Node;
import org.opengis.feature.schema.Schema;
import org.opengis.feature.schema.SchemaFactory;
import org.opengis.feature.type.Type;

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
		if( schema instanceof Schema.All && extend instanceof Schema.All ){
			return extension( (Schema.All) schema, (Schema.All) extend);
		}
		else if( schema instanceof Schema.Choice && extend instanceof Schema.Choice ){
			return extension( (Schema.Choice) schema, (Schema.Choice) extend);
		}
		else if( schema instanceof Schema.Ordered && extend instanceof Schema.Ordered ){
			return extension( (Schema.Ordered) schema, (Schema.Ordered) extend);
		}
		else {
			List<Schema> all = new ArrayList<Schema>();
			all.add( schema );
			all.add( extend );
			return factory.ordered( all, 1, 1 );
		}
	}
	private Schema.All extension( Schema.All schema, Schema.All extend ){
		List<Schema> all = new ArrayList<Schema>();
		all.addAll( schema.all() );
		all.addAll( extend.all() );		
		return factory.all( all, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private Schema.Ordered extension( Schema.Ordered schema, Schema.Ordered extend ){
		List<Schema> ordered = new ArrayList<Schema>();
		ordered.addAll( schema.sequence() );
		ordered.addAll( extend.sequence() );
		return factory.ordered( ordered, extend.getMinOccurs(), extend.getMaxOccurs() );
	}
	private Schema.Choice extension( Schema.Choice schema, Schema.Choice extend ){
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
		if( schema instanceof Schema.All && restrict instanceof Schema.All ){
			return restriction( (Schema.All) schema, (Schema.All) restrict);
		}
		else if( schema instanceof Schema.Choice && restrict instanceof Schema.Choice ){
			return restriction( (Schema.Choice) schema, (Schema.Choice) restrict);
		}
		else if( schema instanceof Schema.Ordered && restrict instanceof Schema.Ordered ){
			return restriction( (Schema.Ordered) schema, (Schema.Ordered) restrict);
		}
		else if( schema instanceof Node && restrict instanceof Node ){
			return restriction( (Node) schema, (Node) restrict);
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
	Node restriction( Node node, Node restrict ){
		if( node.getType() == restrict.getType() ){
			return restrict;
		}
		for( Type type = restrict.getType(); type != null; type = type.getSuper() ){
			if( node.getType() == type ){
				return restrict; 
			}
		}
		throw new IllegalStateException( "Cannot restrict provided schema" );
	}
	Schema.All restriction( Schema.All schema, Schema.All restrict ){
		List<Schema> all = restriction( schema.all(), restrict.all() );		
		return factory.all( all, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	Schema.Ordered restriction( Schema.Ordered schema, Schema.Ordered restrict ){
		List<Schema> sequence = restriction( schema.sequence(), restrict.sequence() );
		return factory.ordered( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
	}
	Schema.Choice restriction( Schema.Choice schema, Schema.Choice restrict ){
		List<Schema> sequence = restriction( schema.options(), restrict.options() );
		return factory.choice( sequence, restrict.getMinOccurs(), restrict.getMaxOccurs() );
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
	static public Type type( Schema schema, String name ){
		Node node = node( schema, name );
		if( node != null ) return node.getType();
		return null;
	}
	/**
	 * Finds the node associated with the provided name.
	 * 
	 * @param schema
	 * @param name
	 * @return Node assoicated with provided name, or null if not found.
	 */
	static public Node node( Schema schema, String name ){
		for( Schema child : list( schema ) ){
			if( child instanceof Node ){
				Node node = (Node) child;
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
	 * @return Node assoicated with provided name, or null if not found.
	 */
	static public Node node( Schema schema, Type type){
		for( Schema child : list( schema ) ){
			if( child instanceof Node ){
				Node node = (Node) child;
				if( node.getType() == type ){
					return node;
				}
			}
		}
		return null;
	}
	/**
	 * List of nodes matching Type.
	 * 
	 * @param schema
	 * @param type
	 * @return List of nodes for the provided type, or empty.
	 */
	static public List<Node> nodes( Schema schema, Type type ){
		List<Node> nodes = new ArrayList<Node>();
		for( Schema child : list( schema ) ){
			if( child instanceof Node ){
				Node node = (Node) child;
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
	static public Set<Type> types( Schema schema ){
		Set<Type> types = new HashSet<Type>();
		for( Schema child : list( schema ) ){
			if( child instanceof Node ){
				Node node = (Node) child;
				types.add( node.getType() );
			}
		}
		return types;
	}
	/**
	 * True if there may be more then one Type in the schema.
	 * <p>
	 * This may happen if:
	 * <ul>
	 * <li>The Type is referenced by more then one node.
	 * <li>The node referencing the type has multiplicy greater then 1
	 * </ul>
	 * 
	 * @param schema
	 * @param type
	 * @return
	 */
	public static boolean multiple( Schema schema, Type type ){
		return maxOccurs( schema, type ) != 1;
	}
	public static int maxOccurs( Schema schema, Type type ){
		List<Node> nodes = nodes( schema, type );
		if( nodes.isEmpty() ) return 0;
		
		int max = 0;
		for( Node node : nodes ){
			if( max == Integer.MAX_VALUE ){
				return Integer.MAX_VALUE;
			}
			max += node.getMaxOccurs();
		}
		return max;
	}
	@SuppressWarnings("unchecked")
	static public List<Schema> list( Schema schema ){
		if( schema instanceof Schema.Ordered ){
			return ((Schema.Ordered)schema).sequence();
		}
		else if( schema instanceof Schema.All ){
			return ((Schema.All)schema).all();
		}
		else if( schema instanceof Schema.Choice ){
			return ((Schema.Choice)schema).options();
		}
		return Collections.EMPTY_LIST;
	}	
}
