
package org.geotools.metadata;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public abstract class AbstractMetadata implements Metadata {

    ArrayList elementlist; 
    Entity type;
    
    /** 
     * @see org.geotools.metadata.Metadata#getElements(java.lang.Object[])
     */
    public final Object[] getElements(Object[] elements) {
        if( elementlist==null){
            initElementList();
        }
        
        if(elements==null){
            elements=elementlist.toArray();
        }else{           
            elementlist.toArray(elements);
        }
        return elements;
    }

    /**
     * 
     */
    private void initElementList() {
        
        if( elementlist!=null )
            return;
        
        Class[] interfaces=getClass().getInterfaces();
        
        /*
         * Inspects each interface.  If the interface is not the MetadataEntity
         * interface then its getXXX() methods are used to identify the MetadataElements
         * of the Metadata
         */
        for (int i = 0; i < interfaces.length; i++) {
            Class intrface = interfaces[i];
            if( intrface.getName().equals("MetadataEntity") ) 
                continue;
            
            Method[] methods= intrface.getMethods();
            
            /*
             * locate and add field that the getXXX() indicates
             */
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                String name = method.getName();
                if(name.startsWith("get")){
                    //TODO
                }

            }
        }
    }

    /** 
     * @see org.geotools.metadata.Metadata#getElement(int)
     */
    public final Object getElement(int index) {
        // TODO Auto-generated method stub
        return elementlist.get(index);
    }

    /** 
     * @see org.geotools.metadata.Metadata#getElement(java.lang.String)
     */
    public final Object getElement(String xPath) {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.geotools.metadata.Metadata#getNumElements()
     */
    public final int getNumElements() {
        // TODO Auto-generated method stub
        return 0;
    }

    /** 
     * @see org.geotools.metadata.Metadata#getElement(org.geotools.metadata.ElementType)
     */
    public Object getElement(Element element) {
        // TODO Auto-generated method stub
        return null;
    }
    /** 
     * @see org.geotools.metadata.Metadata#getEntity()
     */
    public Entity getEntity() {
        // TODO Auto-generated method stub
        return null;
    }
}
