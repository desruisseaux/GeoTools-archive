/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Center for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 *
 * @author jamesm
 */

package org.geotools.renderer;

import org.geotools.featuretable.*;
import org.geotools.datasource.*;
import org.geotools.map.Map;
import org.geotools.styling.*;

import com.vividsolutions.jts.geom.*;

import java.awt.*;

public class AWTRenderer implements org.geotools.renderer.Renderer {
    private Graphics g;
    private double scaleDenominator;
    /** Creates a new instance of AWTRenderer */
    public AWTRenderer() {
    }
    
    public void setOutput(Graphics graphics){
        g = graphics;
    }
    
    public void render(Feature features[], Extent e,Style s){
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        for(int i=0;i<featureStylers.length;i++){
            FeatureTypeStyle fts = featureStylers[i];
            for(int j=0;j<features.length;j++){
                Feature feature = features[j];
                if(feature.getTypeName().equalsIgnoreCase(fts.getFeatureTypeName())){
                    //this styler is for this type of feature
                    //now find which rule applies
                    Rule[] rules = fts.getRules();
                    for(int k=0;k<rules.length;k++){
                        //does this rule apply?
                        if(rules[k].getMinScaleDenominator()<scaleDenominator && rules[k].getMaxScaleDenominator()>scaleDenominator){
                            //yes it does
                            //this gives us a list of symbolizers
                            Symbolizer[] symbolizers = rules[k].getSymbolizers();
                            //HACK: now this gets a little tricky...
                            //HACK: each symbolizer could be a point, line, text, raster or polygon symboliser
                            //HACK: but, if need be, a line symboliser can symbolise a polygon
                            //HACK: this code ingores this potential problem for the moment
                            for(int m =0;m<symbolizers.length;m++){
                                System.out.println("Using symbolizer "+symbolizers[m]);
                                if (symbolizers[m] instanceof PolygonSymbolizer){
                                    renderPolygon(feature,(PolygonSymbolizer)symbolizers[m]);
                                }
                                else if(symbolizers[m] instanceof LineSymbolizer){
                                    renderLine(feature,(LineSymbolizer)symbolizers[m]);
                                }
                                //else if...
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer){
        org.geotools.styling.Stroke stroke = symbolizer.getStroke();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom;
        if(geomName==null){
            geom = feature.getGeometry();
        }
        else{
            String names[] =  feature.getAttributeNames();
            for(int i=0;i<names.length;i++){
                if(names[i].equalsIgnoreCase(geomName)){
                    geom=(Geometry)feature.getAttributes()[i];
                }
            }
        }
        Fill fill = symbolizer.getFill();
        System.out.println("Rendering a polygon with an outline colour of "+stroke.getColor()+
            "and a fill colour of "+fill.getColor());
    }
    
    private void renderLine(Feature feature, LineSymbolizer symbolizer){
        org.geotools.styling.Stroke stroke = symbolizer.getStroke();
        System.out.println("Rendering a line with a colour of "+stroke.getColor());
    }
}