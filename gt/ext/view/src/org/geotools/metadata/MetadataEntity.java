package org.geotools.metadata;

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
public interface MetadataEntity {
    /**
     * Copy all the MetadataElements of this Entity into the given array. If the
     * argument array is null, a new one will be created. Gets all MetadataElements
     * from this Entity, returned as a complex object array.  
     * NOTE:  MetadataEntities will be included in the array if the MetadataEntity contains other 
     * MetadataEntities 
     *
     * @param attributes An array to copy elements into. May be null.
     *
     * @return The array passed in, or a new one if null.
     */
    Object[] getElements(Object[] elements);
    /**
     * Gets an element by the given zero-based index.
     *
     * @param index The requested index. Must be 0 &lt;= idx &lt;
     *        getNumberOfElements().
     *
     * @return A copy of the requested element, or NULL_ELEMENT.
     */    
    Object getElement(int index);    
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
     * Get the number of elements this feature has. 
     * 
     * @return The total number of elements this Feature contains.
     */
    int getNumElements();
}
