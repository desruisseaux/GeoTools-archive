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
package org.geotools.gui.swing.toolbox;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JDialog;
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.toolbox.tools.svg2mif.ToolSVG2MIF;
import org.geotools.gui.swing.toolbox.tools.vdem2csv.ToolVdem2csv;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;



/**
 *
 * @author johann sorel
 */
final class TreeTable extends JXTreeTable implements MouseListener{

    private final ToolPackTreeNode root;

    /**
     * Tree widget to manage MapContexts and MapLayers
     * 
     */
    TreeTable(JToolTree frame) {
        super(new DefaultTreeTableModel(new ToolPackTreeNode("Root")));
                        
        setTreeCellRenderer(new DefaultTreeRenderer(new ToolTreeNodeProvider(frame)));
                
        String name = TextBundle.getResource().getString("col_tree");     
        name = "Tools";
        getColumnModel().getColumn(0).setHeaderValue( name );
        
        root = (ToolPackTreeNode) getTreeTableModel().getRoot();
        
        addTool(new ToolSVG2MIF());
        addTool(new ToolVdem2csv());
                
        expandAll();
        
        addMouseListener(this);
    }

    
    void addTool(Tool tool){
        
        ToolTreeNode node = new ToolTreeNode(tool.getTitle());
        node.setUserObject(tool);
        
        String[] path = tool.getPath();
        
        ToolPackTreeNode origine = root;
        for(String str : path){
            boolean found = false;
            for(int i=0, max=origine.getChildCount(); (i<max && !found) ; i++){
                TreeTableNode n = origine.getChildAt(i);
                
                if(n instanceof ToolPackTreeNode){
                    if(  ((ToolPackTreeNode)n).getTitle().equals(str)){
                        origine = (ToolPackTreeNode)n;
                        found = true;
                    }
                }
            }
            
            if(!found){
                ToolPackTreeNode n = new ToolPackTreeNode(str);
                origine.add(n);
                origine = n;
            }
        }
        
        origine.add(node);
        
    }

    public void mouseClicked(MouseEvent e) {
        int nb = e.getClickCount();
        if(nb == 2){
            TreePath path = getTreeSelectionModel().getSelectionPath();
            Object node = path.getLastPathComponent();
            if(node instanceof ToolTreeNode){
                JDialog dialog = new JDialog();
                Tool tool = (Tool) ((ToolTreeNode)node).getUserObject();
                dialog.setTitle(tool.getTitle());
                dialog.setContentPane(tool.getComponent());
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setModal(true);
                dialog.setVisible(true);                
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    


    
}

