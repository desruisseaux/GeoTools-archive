/*
 * NamedLayer.java
 *
 * Created on November 3, 2003, 10:10 AM
 */

package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  jamesm
 */
public class NamedLayerImpl extends StyledLayerImpl implements NamedLayer {
    List styles = new ArrayList();
    
    public FeatureTypeConstraint[] getLayerFeatureConstrains(){
        return null;
    }
    
    public Style[] getStyles(){
        return (Style[])styles.toArray(new Style[0]);
    }
    
    /**
     * 
     * @param sl may be a StyleImpl or a NamedStyle
     */
    public void addStyle(Style sl){
        styles.add(sl);
    }

}
