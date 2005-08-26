package org.opengis.feature;

import java.util.List;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Type;

interface Complex extends Attribute {
   /**
    * Access the type of this construct.
    */
   ComplexType getType();

   /** Access to contents of this Feature */
   List<Attribute> attributes();

   /**
    * List view of attribtue types, in a manner similar Map.keys().
    * <p>
    * The content avalable through types() an values() are considered a view of attribtues().
    * Order is maintained, and removing content will result in a modification to all three lists.
    * in a manner simialr to Map.keysSet() and Map.values().
    */
   List<Type> types();

   /**
    * Value view of attribtue types, in a manner similar Map.keys().
    * <p>
    * The content avalable through types() an values() are considered a view of attribtues().
    * Order is maintained, and removing content will result in a modification to all three lists.
    * in a manner simialr to Map.keysSet() and Map.values().
    */
   List<Object> values();
   
   /** Access to validation constraints */
   List<Schema> schema();
}