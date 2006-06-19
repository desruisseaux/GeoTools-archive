/*
 * FilterModel.java
 *
 * Created on May 9, 2005, 11:41 AM
 */

package edu.psu.geovista.geotools.filter.model;


import edu.psu.geovista.geotools.filter.FilterEvent;
import edu.psu.geovista.geotools.filter.FilterListener;
import javax.swing.event.EventListenerList;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;

/**
 *
 * @author jamesm
 */
public class FilterModel {
    private EventListenerList ell;
    private Filter filter;
    private Filter tempFilter;
    private boolean changing;
    
    /** Creates a new instance of FilterModel */
    public FilterModel() {
        ell = new EventListenerList();
    }
    
    public void expandFilter(Filter f,  boolean changing){
        setFilter(filter.or(f), changing);
    };
    
    public void subtractFilter(Filter f,  boolean changing){
        //  FilterFactory.createFilterFactory().createLogicFilter(filter, f.not(), LogicFilter.LOGIC_AND);
        try{
            setFilter(FilterFactoryFinder.createFilterFactory().createLogicFilter(filter, f.not(), LogicFilter.LOGIC_AND),changing);
        } catch(IllegalFilterException ife){
            //sol
        }
    }
    
    public void invert(){
        setFilter(filter.not());
        
    };
    
    public void setFilter(Filter f){
        setFilter(f,false);
    }
    
    public Filter getFilter(){
        return filter;
    }
    
    public void commit(){
        setFilter(tempFilter);
    }
    
    public boolean isChanging(){
        return changing;
    }
    
    public void setFilter(Filter f, boolean changing) {
        this.changing = changing;
        if(!changing){
            filter = f;
        } else{
            tempFilter = f;
        }
        notifyListeners(f);
    };
    
    public void addFilterListener(FilterListener l){
        ell.add(FilterListener.class, l);
    }
    
   public void removeFilterListener(FilterListener l){
        ell.remove(FilterListener.class, l);
    }
    
    protected void notifyListeners(Filter f){
        FilterEvent e = new FilterEvent(this,f,changing);
        Object listnerList[] = ell.getListenerList();
        for(int i=0;i<listnerList.length;i+=2){
            if(listnerList[i] == FilterListener.class){
                ((FilterListener)listnerList[i+1]).filterChange(e);
            }
        }
       
    }
}
