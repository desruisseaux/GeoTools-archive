/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.gui.swing.toolbox.process;

import javax.swing.event.EventListenerList;

/**
 *
 * @author johann sorel
 */
public abstract class AbstractProcess implements Process{

    protected final EventListenerList LISTENERS = new EventListenerList();
    
    
    
    protected void fireProcessChanged(int val, int max, String desc){
        ProcessListener[] listeners = getProcessListeners();
        
        for(ProcessListener listener : listeners){
            listener.processChanged(val, max, desc);
        }
    }
    
    protected void fireObjectCreated(Object obj){
        ProcessListener[] listeners = getProcessListeners();
        
        for(ProcessListener listener : listeners){
            listener.objectCreated(obj);
        }
    }
    
    protected void fireProcessEnded(String desc){
        ProcessListener[] listeners = getProcessListeners();
        
        for(ProcessListener listener : listeners){
            listener.processEnded(desc);
        }
    }
    
    protected void fireProcessInterrupted(Exception e){
        ProcessListener[] listeners = getProcessListeners();
        
        for(ProcessListener listener : listeners){
            listener.processInterrupted(e);
        }
    }
    
    
    public void addProcessListener(ProcessListener listener) {
        LISTENERS.add(ProcessListener.class,listener);
    }

    public void removeProcessListener(ProcessListener listener) {
        LISTENERS.remove(ProcessListener.class,listener);
    }

    public ProcessListener[] getProcessListeners() {
        return LISTENERS.getListeners(ProcessListener.class);
    }


}
