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

package org.geotools.gui.swing.contexttree.renderer;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author johann sorel
 */
public abstract class RendererAndEditorComponent extends JPanel {

    public RendererAndEditorComponent(){
        super();        
        setBorder(new EmptyBorder(1,1,1,1));
    }
    
    public abstract void parse(Object obj);
    
    public abstract Object getValue();
    
}
