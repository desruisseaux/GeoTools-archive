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

package org.geotools.gui.swing.contexttree;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.contexttree.popup.PasteItem;
import org.geotools.map.MapContext;

/**
 *
 * @author johann sorel
 */
public class JContextTree extends JComponent{

    private final TreeTable treetable;
    
    public JContextTree(){
        treetable = new TreeTable(this);
        
        JScrollPane pane = new JScrollPane(treetable);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        setLayout(new BorderLayout());
        
        add(BorderLayout.CENTER,pane);
    }
    
    
    public JContextTreePopup getPopupMenu(){
        return treetable.getPopupMenu();
    }
    
    public SelectionData[] getSelection(){
        return treetable.getSelection();
    }
    
////////////////////////////////////////////////////////////////////////////////
// STATIC CONSTRUCTORS /////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * create a default TreeTable, with default columns
     * and default JContextTreePopup items
     * 
     * @param map
     * @return default TreeTable
     */
    public static JContextTree createDefaultTree(JMapPane map) {
        JContextTree tree = new JContextTree();

        tree.addColumnModel(new VisibleTreeTableColumn());
        tree.addColumnModel(new OpacityTreeTableColumn());
        tree.addColumnModel(new StyleTreeTableColumn());
        
        
        
        JContextTreePopup popup = tree.getPopupMenu();        
        popup.addPopControl(new LayerVisibilityItem());            //layer         
        popup.addSeparator();        
        popup.addPopControl(new LayerZoomItem(map));            //layer
        popup.addPopControl(new LayerFeatureItem());            //layer
        popup.addPopControl(new ContextActiveItem(tree));  //context
        popup.addSeparator();
        popup.addPopControl(new CutItem(tree));                 //all
        popup.addPopControl(new CopyItem(tree));                //all
        popup.addPopControl(new PasteItem(tree));               //all
        popup.addPopControl(new DuplicateItem(tree));           //all        
        popup.addSeparator();        
        popup.addPopControl(new DeleteItem(tree));              //all
        popup.addSeparator();
        popup.addPopControl(new LayerPropertyItem());           //layer
        popup.addPopControl(new ContextPropertyItem());         //context
                
        popup.setMapPane(map);
        
        tree.revalidate();

        return tree;
    }
        
////////////////////////////////////////////////////////////////////////////////
// CUT/COPY/PASTE/DUPLICATE/DELETE  ////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     *  prefix string used when pasting/duplicating datas
     * 
     * @param prefix if null, prefix will be an empty string
     */
    public void setPrefixString(String prefix){
        treetable.setPrefixString(prefix);
    }
        
    /**
     * prefix used when pasting/duplicating datas
     * 
     * @return String 
     */
    public String getPrefixString() {
        return treetable.getPrefixString();
    }

    /**
     * 
     * @return true if ther is something selected
     */
    public boolean hasSelection() {
        return treetable.hasSelection();
    }

    /**
     * Duplicate was is actually selected in the tree. nothing happens
     * if selection isn't composed of only 1 type of datas. (only layers or only contexts )
     * 
     * @return true if duplication succeed
     */
    public boolean duplicateSelection() {
        return treetable.duplicateSelection();
    }
    
    /**
     * 
     * @return true if tree buffer is empty
     */
    public boolean isBufferEmpty() {
        return treetable.isBufferEmpty();
    }

    /**
     * 
     * @return true is paste can succeed
     */
    public boolean canPasteBuffer() {
        return treetable.canPasteBuffer();
    }
    
    /**
     * 
     * @return true if duplication can succeed
     */
    public boolean canDuplicateSelection() {
        return treetable.canDuplicateSelection();
    }

    /**
     * 
     * @return true if delete can succeed
     */
    public boolean canDeleteSelection() {
        return treetable.canDeleteSelection();
    }

    /**
     * 
     * @return true if copy can succeed
     */
    public boolean canCopySelection() {
        return treetable.canCopySelection();
    }

    /**
     * 
     * @return true if cut can succeed
     */
    public boolean canCutSelection() {
        return treetable.canCutSelection();
    }

    /**
     * delete what is actually selected
     * 
     * @return true if delete suceed
     */
    public boolean deleteSelection() {
        return treetable.deleteSelection();
    }

    /**
     * copy what is actually selected in the tree buffer
     * 
     * @return true if copy succeed
     */
    public boolean copySelectionInBuffer() {
        return treetable.copySelectionInBuffer();
    }

    /**
     * copy what is actually selected in the tree buffer and cut it from the tree.
     * 
     * @return true if cut succeed
     */
    public boolean cutSelectionInBuffer() {
        return treetable.cutSelectionInBuffer();
    }
 
    /**
     * paste at the selected node what is in the buffer
     * 
     * @return true if paste succeed
     */
    public boolean pasteBuffer() {
        return treetable.pasteBuffer();
    }

    /**
     * get a Array of the objects in the buffer
     * 
     * @return object array, can be MapLayers or MapContexts or empty array
     */
    public Object[] getBuffer() {
        return treetable.getBuffer();
    }

        
////////////////////////////////////////////////////////////////////////////////
// COLUMNS MANAGEMENT //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add a new column in the model and update the treetable
     * @param model the new column model
     */
    public void addColumnModel(TreeTableColumn model) {
        treetable.addColumnModel(model);
    }
    
    /**
     * remove column
     * @param model
     */
    public void removeColumnModel(TreeTableColumn model){
        treetable.removeColumnModel(model);
    }
    
    /**
     * remove column at index column
     * @param column
     */
    public void removeColumnModel(int column){
             treetable.removeColumnModel(column);
    }
    
    /**
     * get the list of column
     * @return list of column models
     */
    public TreeTableColumn[] getColumnModels() {
        return treetable.getColumnModels();
    }

////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT MANAGEMENT ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * get the active context
     * @return return the active MapContext, if none return null
     */
    public MapContext getActiveContext() {
        return treetable.getActiveContext();
    }
    
    /**
     * active the context if in the tree
     * @param context the mapcontext to active
     */
    public void setActiveContext(MapContext context) {
        treetable.setActiveContext(context);
    }
    
    /**
     * add context to the Tree if not allready in it
     * @param context the context to add
     */
    public void addMapContext(MapContext context) {
        treetable.addMapContext(context);
    }
    
    /**
     * remove context from the tree
     * @param context target mapcontext to remove
     */
    public void removeMapContext(MapContext context) {
        treetable.removeMapContext(context);
    }
    
    /**
     * count MapContext in the tree
     * @return number of mapcontext in the tree
     */
    public int getMapContextCount() {
        return treetable.getMapContextCount();
    }
    
    /**
     * return context at index i
     * @param i position of the mapcontext
     * @return the mapcontext a position i
     */
    public MapContext getMapContext(int i) {
        return treetable.getMapContext(i);
    }
    
    /**
     * get the index of a mapcontext in the tree
     * @param context the mapcontext to find
     * @return index of context
     */
    public int getMapContextIndex(MapContext context) {
        return treetable.getMapContextIndex(context);
    }
    
    /**
     * MapContext Array
     * @return empty Array if no mapcontexts in tree
     */
    public MapContext[] getMapContexts(){
        return treetable.getMapContexts();
    }
        
    /**
     * move a mapcontext
     * @param context the context to move
     * @param newplace new position of the child node
     */
    public void moveMapContext(MapContext context, int newplace) {
        treetable.moveMapContext(context, newplace);
    }

////////////////////////////////////////////////////////////////////////////////
// LISTENERS MANAGEMENT ////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add tree context Listener 
     * @param ker the new listener
     */
    public void addTreeContextListener(TreeContextListener ker) {
        treetable.addTreeContextListener(ker);
    }
    
    /**
     * remove tree context Listener 
     * @param ker the listner to remove
     */
    public void removeTreeContextListener(TreeContextListener ker) {
        treetable.removeTreeContextListener(ker);
    }
    
    /**
     * get tree context Listeners array
     * @return the listener's table
     */
    public TreeContextListener[] getTreeContextListeners() {
        return treetable.getTreeContextListeners();
    }
    
    /**
     * add tree Selection Listener 
     * @param ker the new listener
     */
    public void addTreeSelectionListener(TreeSelectionListener ker) {
        treetable.getSelectionManager().addTreeSelectionListener(ker);        
    }
    
    /**
     * remove tree Selection Listener 
     * @param ker the listner to remove
     */
    public void removeTreeSelectionListener(TreeSelectionListener ker) {
        treetable.getSelectionManager().removeTreeSelectionListener(ker);
    }
    
    /**
     * get tree Selection Listeners array
     * @return the listener's table
     */
    public TreeSelectionListener[] getTreeSelectionListeners() {
        return treetable.getSelectionManager().getTreeSelectionListeners();
    }
    
    
}
