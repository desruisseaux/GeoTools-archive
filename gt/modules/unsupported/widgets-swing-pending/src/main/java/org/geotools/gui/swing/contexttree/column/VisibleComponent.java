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

package org.geotools.gui.swing.contexttree.column;

import org.geotools.gui.swing.contexttree.renderer.*;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource.EmptyBorderUIResource;

/**
 *
 * @author johann sorel
 */
public class VisibleComponent extends RendererAndEditorComponent{

    private JCheckBox check = new JCheckBox();
    
    public VisibleComponent(){
        super();
        setLayout(new GridLayout(1,1));
        check.setOpaque(false);
    }
    
    
    @Override
    public void parse(Object obj) {
       
        removeAll();
        if(obj instanceof Boolean){
            check.setSelected((Boolean)obj);
            add(check);
        }
        
    }

    @Override
    public Object getValue() {
        return check.isSelected();
    }

}
