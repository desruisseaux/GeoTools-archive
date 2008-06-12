/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling2;

import org.opengis.style.Graphic;
import org.opengis.style.GraphicFill;

/**
 *
 * @author Johann Sorel
 */
class DefaultGraphicFill implements GraphicFill{

    private final Graphic graphic;
    
    DefaultGraphicFill(Graphic graphic){
        this.graphic = graphic;
    }
    
    public Graphic getGraphic() {
        return graphic;
    }


}
