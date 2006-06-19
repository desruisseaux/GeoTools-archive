/*
 * FeatureCollectionFilterer.java
 *
 * Created on November 4, 2003, 2:50 PM
 */

package edu.psu.geovista.geotools.filter;

/**
 *
 * @author  jfc173
 */

import org.geotools.feature.*;
import org.geotools.filter.Filter;
import java.util.ArrayList;
import java.io.Serializable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.EventListenerList;

public class FeatureCollectionFilterer implements Serializable, FilterListener{
    
    private FeatureCollection whole = null;
    private FeatureCollection part;
//    private ClassificationExpression expr;
    private Filter filter = null;
    private EventListenerList ell = new EventListenerList();
    
    /** Creates a new instance of FeatureCollectionFilterer */
    public FeatureCollectionFilterer() {
    }

    public void setFeatureCollection(FeatureCollection fc){
        whole = fc;
        if (filter != null){
            filterTheCollection();
        }
    }
    
    public void setFilter(Filter f){
        filter = f;
        //Generally, expressions are created through a factory, not a constructor.  There's probably a sufficiently good
        //reason for doing so that using the constructor here is bad form.  Oh well.  I'll worry about it later.
        //At any rate, this expression is never used!
//        expr = new ClassificationExpression();
//        expr.setFilter(f);
        
        if (whole != null){
            filterTheCollection();
        }
    }
        
    private void filterTheCollection(){    
        ArrayList list = new ArrayList();
        part = FeatureCollections.newCollection();
        
        FeatureIterator it = whole.features();
        while(it.hasNext()){
            Feature next = it.next();
//            System.out.println("Filter contains " + next.getID() + ": " + filter.contains(next));
            if (filter.contains(next)){
                list.add(next);
            }
        }
        part.addAll(list);

        fireAction();   
    }
    
    public FeatureCollection getFilteredCollection(){
        return part;
    }
    
  public void addActionListener(ActionListener sl){
    ell.add(ActionListener.class, sl);
  }

  public void removeActionListener(ActionListener sl){
    ell.remove(ActionListener.class, sl);
  }

  public void fireAction(){
    Object[] listeners = ell.getListenerList();
    int numListeners = listeners.length;
    ActionEvent se = new ActionEvent(this, 42, "Don't panic!");
    for (int i = 0; i < numListeners; i++){
      if (listeners[i]==ActionListener.class){
        // pass the event to the listeners event dispatch method
        ((ActionListener)listeners[i+1]).actionPerformed(se);
      }
    }
  }
    
  public void filterChange(FilterEvent fe) {
      filter = fe.getFilter();
  }  
    
}
