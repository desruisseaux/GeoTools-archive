package org.geotools.metadata;

import java.util.List;

/**
 * The MetadataEntity is a set of metadata elements describing the same aspect of data
 * 
 * May contain one or more metadata entities
 * Equivalent to a clas in UML terminology.
 *
 * The MetadataEntity interface is similar to that of the Feature Interface 
 * The subinterfaces of MetadataEntity are used to specify which MetadataElements
 * are required, or possible.
 * 
 * Any getXXX() method declared in a MetadataEntity
 * <i>subinterfaces</i> indicate the MetadataElements
 * that may be found as a elemtent of the particular Metadata Type.
 * 
 * This contract is required because MetadataEntity does not have  
 * type classes that describe the elements that can be expected
 * for a particular metadata.  Features have a FeatureType that
 * describe the FeatureAttributes.  In Metadata the getXXX() methods
 * declare the MetadataElement structure.
 * 
 * @see Metadata interface as an example
 * 
 * @author jeichar
 */
public interface Metadata {
    /**
     * Copy all the MetadataElements of this Entity into the given list. If the
     * argument list is null, a new one will be created. Gets all MetadataElements
     * from this Entity, returned as a complex object list.  
     *
     * @param attributes A list to copy elements into. May be null.
     *
     * @return The list passed in, or a new one if null.
     */
    List getElements(List list);
    
     /**
      * Gets an MetadataElements for this Entity at the location specified by xPath.
      * Due to the complex nature of xpath, the return Object may be a single
      * MetadataElement/Entity or a java.util.Collection containing a mix of MetadataEntities and/or
      * MetadataElements
      *
      * @param xPath XPath representation of element location.
      *
      * @return A copy of the requested element, null if the requested xpath
      *         is not found, or NULL_ELEMENT.
      */
    Object getElement(String xPath);

    
    /**
     * 
     * @param element
     * @return
     */
    Object getElement(Element element);
    
    /**
     * 
     * @return
     */
    Entity getEntity();
    
    
    /**
     * 
     * TODO type description
     * 
     * @author jeichar
     *
     */
    public static interface Entity {
        Object getElement( String xpath );
        List getElements( );
    }

    /**
     * TODO type description
     * 
     * @author jeichar
     *
     */
    public static interface Element {
        
        /**
         * Gets the type of this element.
         *
         * @return Type.
         */
        Class getType();
        /**
         * Gets the name of this element.
         *
         * @return Name.
         */
        String getName();
        /**
         * Returns whether nulls are allowed for this element.
         *
         * @return true if nulls are permitted, false otherwise.
         */
        boolean isNillable();
        /**
         * Whether or not this element is complex in any way.  If it is
         * not nested then the code can just do the default processing, such
         * as printing the element directly, for example.  If it is nested then
         * that indicates there is more to be done, and the actual ElementType
         * should be determined and processed accordingly.
         *  
         * @return <code>true</code> if Any  
         *
         */
        boolean isMetadataEntity();
        
        Entity getEntity();
    }    
    
}
