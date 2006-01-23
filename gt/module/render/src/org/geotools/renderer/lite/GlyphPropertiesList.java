/*
 * GlyphPropertiesList.java
 *
 * Created on April 6, 2004, 4:11 PM
 */

package org.geotools.renderer.lite;

import java.util.Vector;

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

/**
 *
 * @author  jfc173
 * @source $URL$
 */
public class GlyphPropertiesList {
    
    private Vector list = new Vector();
    private Vector names = new Vector();
    private FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        
    /** Creates a new instance of GlyphPropertiesList */
    public GlyphPropertiesList() {
    }
    
    public void addProperty(String name, Class type, Object value){
        if (type.isAssignableFrom(value.getClass())){  
            list.add(new GlyphProperty(name, type, value));
            names.add(name);
        } else {
            throw new RuntimeException("Wrong class for setting variable " + name + ".  Expected a " + type + " but received a " + value.getClass() + ".");
        }
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public String getPropertyName(int i){
        return ((GlyphProperty) list.get(i)).getName();
    }
    
    public int getPropertyIndex(String name){
        return names.indexOf(name);
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public Class getPropertyType(int i){
        return ((GlyphProperty) list.get(i)).getType();
    }
    
    public Class getPropertyType(String name){
        int index = names.indexOf(name); 
        if (index != -1){
            return getPropertyType(index);
        } else {
            throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
        }
    }
    
    public boolean hasProperty(String name){
        return names.contains(name);
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public Object getPropertyValue(int i){
        return ((GlyphProperty) list.get(i)).getValue();
    }
 
    public Object getPropertyValue(String name){
        int index = names.indexOf(name); 
        if (index != -1){
            return getPropertyValue(index);
        } else {
            throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
        }
    }    
    
    private Expression stringToLiteral(String s){
        return factory.createLiteralExpression(s);
    }   
    
    private Expression numberToLiteral(Double d){
        return factory.createLiteralExpression(d.doubleValue());
    }
    
    private Expression numberToLiteral(Integer i){
        return factory.createLiteralExpression(i.intValue());
    }
    
    public void setPropertyValue(String name, int value){
        setPropertyValue(name, new Integer(value));        
    }
    
    public void setPropertyValue(String name, double value){
        setPropertyValue(name, new Double(value));
    }
    
    public void setPropertyValue(String name, Object value){
        int index = names.indexOf(name); 
        if (index != -1){
            GlyphProperty prop = (GlyphProperty) list.get(index);
            if (value instanceof String){
                value = stringToLiteral((String) value);
            }
            if (value instanceof Integer){                
                value = numberToLiteral((Integer) value);
            }
            if (value instanceof Double){
                value = numberToLiteral((Double) value);
            }
            if (prop.getType().isAssignableFrom(value.getClass())){
                prop.setValue(value);                
            } else {
                throw new RuntimeException("Wrong class for setting variable " + name + ".  Expected a " + prop.getType() + " but received a " + value.getClass() + ".");
            }
        } else {
            throw new RuntimeException("Tried to set the value of a non-existent property: " + name);
        }
    }
}
