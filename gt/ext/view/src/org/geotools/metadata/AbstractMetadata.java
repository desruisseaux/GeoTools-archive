package org.geotools.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO type description
 * 
 * @author jeichar
 * @since 2.1
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
        
        elementlist=new ArrayList();
        
        ArrayList ifaces=new ArrayList(); 
        getInterfaces(getClass(),ifaces);
        
        
        /*
         * Inspects each interface.  If the interface is not the MetadataEntity
         * interface then its getXXX() methods are used to identify the MetadataElements
         * of the Metadata
         */
        for (Iterator iter = ifaces.iterator(); iter.hasNext();) {
            Class iface = (Class) iter.next();

            Method[] methods= iface.getDeclaredMethods();
            
            /*
             * locate and add field that the getXXX() indicates
             */
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                String name = method.getName();
                if(name.startsWith("get")){
                    try{
                        elementlist.add(method.invoke(this,null));
                    }catch(IllegalAccessException iae){
                        throw new RuntimeException(iae);
                    }catch(InvocationTargetException ite){
                        throw new RuntimeException(ite);
                    }//try
                }//if
            }//for
        }//for
    }

    private void getInterfaces(Class class1, List list){

         Class[] ifaces=class1.getInterfaces();
        
        for (int i = 0; i < ifaces.length; i++) {
            Class iface = ifaces[i];
            getInterfaces(iface, list);
        }//for
        
        if( !class1.isInterface() )
            return;
        
        if (class1==Metadata.class )
            return;
        
        if(!Metadata.class.isAssignableFrom(class1))
            return;
        if(list.contains(class1)){
            return;
        }   
        list.add(class1);
    }
    
    /** 
     * @see org.geotools.metadata.Metadata#getElement(int)
     */
    public final Object getElement(int index) {
        if( elementlist==null )
            initElementList();
        
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
        if( elementlist==null )
            initElementList();
        return elementlist.size();
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
