package org.psu.geovista.geotools;


import java.util.EventListener;

/**
 *
 * @author jamesm
 */
public interface SelectionFilterChangeListener extends EventListener{
    
    public void selectionChanged(SelectionFilterChangeEvent sce);
    
}
