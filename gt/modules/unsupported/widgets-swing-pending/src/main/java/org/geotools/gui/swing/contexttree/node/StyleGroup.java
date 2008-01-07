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
package org.geotools.gui.swing.contexttree.node;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.geotools.gui.swing.contexttree.ContextTreeModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author johann sorel
 */
public class StyleGroup implements SubNodeGroup {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private static final Icon ICON_STYLE = IconBundle.getResource().getIcon("16_style");
    private static final Icon ICON_FTS = IconBundle.getResource().getIcon("16_style_fts");
    private static final Icon ICON_RULE = IconBundle.getResource().getIcon("16_style_rule");
    
    public boolean isValid(Object target) {
        return (target instanceof MapLayer);
    }

    public ContextTreeNode[] createNodes(final ContextTreeModel model, Object target) {
        final MapLayer layer = (MapLayer) target;
        Style style = layer.getStyle();
        
        ContextTreeNode root = new PackStyleNode(model, "Style",style);
                
        FeatureTypeStyle[] ftss = style.getFeatureTypeStyles();
        
        for(FeatureTypeStyle fts : ftss){
            ContextTreeNode ftsnode = new FeatureTypeStyleNode(model,fts);
            root.add(ftsnode);
            
            Rule[] rules = fts.getRules();
            for(Rule rule : rules){
                ContextTreeNode rulenode = new RuleNode(model,rule);
                Symbolizer[] symbs = rule.getSymbolizers();
                for(Symbolizer symb : symbs){
                    Icon ico = new ImageIcon(RANDOM_STYLE_FACTORY.createGlyph(symb));
                    SymbolizerNode symbnode = new SymbolizerNode(model, ico, symb);
                    rulenode.add(symbnode);
                }   
                ftsnode.add(rulenode);
            }                    
        }
       
        
        return new ContextTreeNode[]{root};
    }
    
    private class PackStyleNode extends ContextTreeNode{
        
        private String name;
        
        PackStyleNode(ContextTreeModel model,String name,Style target){
            super(model);
            this.name = name;
            setUserObject(target);
        }
        
       
        @Override
        public Icon getIcon() {
            return ICON_STYLE;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getValue() {
            return name;
        }

        @Override
        public void setValue(Object obj) {
        }
        
    }
    
    
    private class FeatureTypeStyleNode extends ContextTreeNode{
        
        
        
        FeatureTypeStyleNode(ContextTreeModel model,FeatureTypeStyle target){
            super(model);
            setUserObject(target);
        }
        
       
        @Override
        public Icon getIcon() {
            return ICON_FTS;
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Object getValue() {
            return ((FeatureTypeStyle)userObject).getTitle();
        }

        @Override
        public void setValue(Object obj) {
            ((FeatureTypeStyle)userObject).setTitle(obj.toString());
        }
        
    }
    
    private class RuleNode extends ContextTreeNode{
        
        RuleNode(ContextTreeModel model,Rule target){
            super(model);
            setUserObject(target);
        }
        
       
        @Override
        public Icon getIcon() {
            return ICON_RULE;
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public Object getValue() {
            return ((Rule)userObject).getTitle();
        }

        @Override
        public void setValue(Object obj) {
            ((Rule)userObject).setTitle(obj.toString());
        }
        
    }
    
    private class SymbolizerNode extends ContextTreeNode{

        private Icon icon;
        
        SymbolizerNode(ContextTreeModel model, Icon icon, Object target){
            super(model);
            this.icon = icon;
            setUserObject(target);
        }
        
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getValue() {
            return "";
        }

        @Override
        public void setValue(Object obj) {
        }
        
    }
    
    
}
