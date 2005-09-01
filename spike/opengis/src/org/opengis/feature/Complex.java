package org.opengis.feature;

import java.util.List;

import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Type;

public interface Complex extends Attribute {
   /** 
    * Access the type of this construct.
    */
   public abstract ComplexType getType();

   /**
    * Unique, inmutable identification for domain object being modeled.
    * 
    * @return Unique ID, may not be null if getType().isIdentifiable() is true
    */
   String getID();
   
   /**
    * Access to contents of this Feature.
    * <p>
    * This represents the contents of this Complex value, the methods
    * types() and values() return "views" into this data structure in a manner
    * similar to Maps.keySet() and values().
    * </p>
    * <p>
    * Java Bean property conventions are used to indicate that this is
    * part of our data model.
    * </p>
    * @see types()
    * @see values()
    */
   List<Attribute> getAttributes();
   
   /**
    * List view of attribtue types, in a manner similar Map.keys().
    * <p>
    * The content avalable through types() an values() are considered a view of attribtues().
    * Order is maintained, and removing content will result in a modification to all three lists.
    * in a manner simialr to Map.keysSet() and Map.values().
    * <p>
    * Collections naming conventions are used to indicate this is a view into
    * our data model.
    */
   List<Type> types();

   /**
    * Value view of attribtue types, in a manner similar Map.keys().
    * <p>
    * The content avalable through types() an values() are considered a view of attribtues().
    * Order is maintained, and removing content will result in a modification to all three lists.
    * in a manner simialr to Map.keysSet() and Map.values().
    * <p>
    * Collections naming conventions are used to indicate this is a view into
    * our data model.
    */
   List<Object> values();
   
   /**
    * Access to value associated with name.
    * <p>
    * This method acts as a precanned search of getAttribtues() based on Type,
    * where type is determined by getType().type( name )
    * <ul>
    * <li>Type by Schema 1:1 - it will return an Object of the bound java class indicated by Type
    * <li>Type by Schema 0:* - it will return a possibly empty List of the bound java class indicated by Type
    * </p>
    * <p>
    * This method is not considered useful for accessing content with multiplicity
    * @param name
    * @return Object, or List based on type
    */
   Object get( String name );

   /**
    * Access to value associated with type.
    * @param type
    * @return Object, or List based on schema referencing Type
    */
   Object get( Type type );
}