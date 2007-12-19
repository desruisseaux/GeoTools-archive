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

package org.geotools.gui.swing.toolbox.tools.svg2mif;

import javax.swing.JComponent;
import org.geotools.gui.swing.toolbox.Tool;

/**
 *
 * @author Laurent Jegou
 */
public class ToolSVG2MIF implements Tool{

        
    public String getTitle(){
        return "SVG > MIF";
    }

    public JComponent getComponent() {
        return new svg2mifPanel();
    }

    public String[] getPath() {
        return new String[]{"File utilities","Convert tools"};
    }

}
