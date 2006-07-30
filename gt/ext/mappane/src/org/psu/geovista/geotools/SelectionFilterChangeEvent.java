package org.psu.geovista.geotools;


import java.util.EventObject;
import org.geotools.filter.Filter;

/**
 *
 * @author jamesm
 */
public class SelectionFilterChangeEvent extends EventObject {
    public static final int ADD = 1;
    public static final int REMOVE = 2;
    public static final int REPLACE = 4;
    public static final int INVERT = 8;
    private boolean inProgress;
    private int type;
    private Filter filter;
    
    public SelectionFilterChangeEvent(Filter newFilter, Object source,int mode, boolean inProgress){
        super(source);
        this.filter = newFilter;
        this.type = mode;
        this.inProgress = inProgress;
    }
    
    public Filter getFilter(){
        return filter;
    }
    
    public int getMode(){
        return type;
    }
    
    public boolean isInProgress(){
        return inProgress;
    }
    
    
}
