package org.geotools.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * TODO type description
 * 
 * @author jeichar
 * @since 2.1
 */
public abstract class AbstractMetadata implements Metadata {

    EntityImpl type;
    
    public AbstractMetadata(){
        type=new EntityImpl(getClass());
    }
    
    /** 
     * @see org.geotools.metadata.Metadata#getElements(java.lang.Object[])
     */
    public final List getElements(List elements) {        
        if(elements==null)
            elements=new ArrayList();
        
        List methods=type.getGetMethods();
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            try{
                elements.add(method.invoke(this,null));
            }catch (Exception e) {
                throw new RuntimeException( "There must be a bug in the EntityImpl class during the introspection.", e );
            }
        }
            
        return elements;
    }


    /** 
     * @see org.geotools.metadata.Metadata#getElement(java.lang.String)
     */
    public final Object getElement(String xPath) {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.geotools.metadata.Metadata#getElement(org.geotools.metadata.ElementType)
     */
    public Object getElement(Element element) {

        ElementImpl elemImpl;
        
        if (element instanceof ElementImpl) {
            elemImpl = (ElementImpl) element;
         }else{
             elemImpl = (ElementImpl) type.getElement(element.getName());
         }
        return invoke(elemImpl.getMethod());
    }
    
    private Object invoke(Method m){
        try{
            return m.invoke(this,null);
        }catch (Exception e) {
            throw new RuntimeException( "There must be a bug in the EntityImpl class during the introspection.", e );
        }        
    }
    
    /** 
     * @see org.geotools.metadata.Metadata#getEntity()
     */
    public Entity getEntity() {
        return type;
    }
    
    private class EntityImpl implements Entity{
        
        ArrayList elemList=new ArrayList();
        
        HashMap elemMap=new HashMap();
        
        List getMethods;
        
        public EntityImpl( Class clazz ){
            init(clazz);
        }
        
        private void init(Class clazz){
            getMethods=new ArrayList();
            
            ArrayList ifaces=new ArrayList(); 
            getInterfaces(clazz,ifaces);
            
            /*
             * Inspects each interface.  If the interface is not the MetadataEntity
             * interface then its getXXX() methods are used to identify the MetadataElements
             * of the Metadata
             */
            for (Iterator iter = ifaces.iterator(); iter.hasNext();) {
                Class iface = (Class) iter.next();

                Method[] newmethods= iface.getDeclaredMethods();
                
                /*
                 * locate and add field that the getXXX() indicates
                 */
                for (int j = 0; j < newmethods.length; j++) {
                    Method method=newmethods[j];
                    if( method.getName().startsWith("get") ){
                        getMethods.add(method);
                        Class elementClass=method.getReturnType();
                        if( Metadata.class.isAssignableFrom(elementClass) ){
                            EntityImpl entity=new EntityImpl(elementClass);
                            elemMap.put(method.getName().substring(3), entity);
                            elemList.add(entity);
                        }
                        else{
                            ElementImpl element=new ElementImpl(elementClass);
                            elemMap.put(method.getName().substring(3), element);
                            elemList.add(element);
                        }
                    }
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

        List getGetMethods(){
            return getMethods;
        }
        
        /** 
         * @see org.geotools.metadata.Metadata.Entity#getElement(java.lang.String)
         */
        public Element getElement(String xpath) {
            return (Element)elemMap.get(xpath);
            
            //TODO implement for more complicated xpaths
        }

        /** 
         * @see org.geotools.metadata.Metadata.Entity#getElements()
         */
        public List getElements() {
            
            return elemList;
        }

    }

    private class ElementImpl implements Element{


        /**
         * @param elementClass
         */
        public ElementImpl(Class elementClass) {
            
            // TODO Auto-generated constructor stub
        }

        /**
         * @return
         */
        public Method getMethod() {
            // TODO Auto-generated method stub
            return null;
        }

        /** 
         * @see org.geotools.metadata.Metadata.Element#getType()
         */
        public Class getType() {
            // TODO Auto-generated method stub
            return null;
        }

        /** 
         * @see org.geotools.metadata.Metadata.Element#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        /** 
         * @see org.geotools.metadata.Metadata.Element#isNillable()
         */
        public boolean isNillable() {
            // TODO Auto-generated method stub
            return false;
        }

        /** 
         * @see org.geotools.metadata.Metadata.Element#isMetadataEntity()
         */
        public boolean isMetadataEntity() {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
}
