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

import java.util.EventListener;

/**
 * @author johann sorel
 */
public interface ProcessListener extends EventListener{

    public void processChanged(int val, int max, String desc);
    
    public void objectCreated(Object obj);
    
    public void processEnded(String desc);
    
    public void processInterrupted(Exception e);
    
}
