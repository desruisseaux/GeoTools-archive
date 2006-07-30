package org.geotools.gui.swing;

import java.awt.AWTEvent;
import java.util.EventObject;

import org.geotools.filter.Filter;


public class HighlightChangedEvent extends EventObject{
    Object source;
    
    Filter filter;
    public HighlightChangedEvent(Object source,Filter filter){
        super(source);
        this.source = source;
        
        this.filter = filter;
    }
    public Filter getFilter() {
        return filter;
    }
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    public Object getSource() {
        return source;
    }
    public void setSource(Object source) {
        this.source = source;
    }
}
