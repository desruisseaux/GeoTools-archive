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

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 * JXMapContextTreeModel for JXTreeTable
 * @author johann sorel
 */
public class ContextTreeModel extends DefaultTreeTableModel implements MapLayerListListener {

    private EventListenerList listeners = new EventListenerList();
    /**
     * number of the tree column
     */
    public static final int TREE = 0;
    private MapContext activeContext;
    private boolean treeedit = true;
    private ArrayList<TreeTableColumn> columns = new ArrayList<TreeTableColumn>();
    private Vector columnNames = new Vector();

    /**
     * Creates a new instance of JXMapContextTreeModel
     * 
     */
    ContextTreeModel() {
        super();
        ContextTreeNode node = new ContextTreeNode(this);
        setRoot(node);

        columnNames.add(TextBundle.getResource().getString("col_tree"));

        setColumnIdentifiers(columnNames);
    }

    /**
     * get the class of a specific column
     * @param column column number
     * @return Class of the column
     */
    @Override
    public Class getColumnClass(int column) {
        Class c = Object.class;


        if (column == TREE) {
            c = TreeTableModel.class;
        } else {
            if (column <= columns.size()) {
                c = columns.get(column - 1).getColumnClass();
            }
        }

        return c;
    }

    /**
     * get number of column
     * @return int
     */
    @Override
    public int getColumnCount() {
        return 1 + columns.size();
    }

    /**
     * set if the treecolumn (maplayer and mapcontext titles) can be edited
     * @param b new value
     */
    public void setTreeColumEditable(boolean b) {
        treeedit = b;
    }

    /**
     * know is the cell is editable
     * @param node specific node
     * @param column column number
     * @return editable state
     */
    @Override
    public boolean isCellEditable(Object node, int column) {

        ContextTreeNode treenode = (ContextTreeNode) node;

        if (column == TREE) {
            return treeedit;
        } else {
            if (column <= columns.size()) {
                return columns.get(column - 1).isCellEditable(((ContextTreeNode) node).getUserObject());
            } else {
                return false;
            }
        }
    }

    /**
     * insert a node at a specific node
     * @param newChild the new node
     * @param father the node who will contain the new node
     * @param index position of the new node
     */
    @Override
    public void insertNodeInto(MutableTreeTableNode newChild, MutableTreeTableNode father, int index) {
        super.insertNodeInto(newChild, father, index);
    }

    /**
     * remove a node from his parent
     * @param node the node to remove
     */
    @Override
    public void removeNodeFromParent(MutableTreeTableNode node) {
        super.removeNodeFromParent(node);
    }

    /**
     * move a node
     * @param newChild the moving node
     * @param father his new parent node
     * @param index position in the father node
     */
    public void moveNode(MutableTreeTableNode newChild, MutableTreeTableNode father, int index) {
        super.removeNodeFromParent(newChild);
        super.insertNodeInto(newChild, father, index);
    }
////////////////////////////////////////////////////////////////////////////////
// COLUMNS MANAGEMENT //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add a new column in the model
     * @param model the new column model
     */
    void addColumnModel(TreeTableColumn model) {
        columns.add(model);
        columnNames.add(model.getName());
        setColumnIdentifiers(columnNames);

        model.setModelIndex(columns.indexOf(model) + 1);
    }

    /**
     * get the list of column
     * @return list of column models
     */
    ArrayList<TreeTableColumn> getColumnModels() {
        return columns;
    }
////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT MANAGEMENT ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * get the active context
     * @return return the active MapContext, if none return null
     */
    public MapContext getActiveContext() {
        return activeContext;
    }

    /**
     * active the context if in the tree
     * @param context the mapcontext to active
     */
    public void setActiveContext(MapContext context) {

        if (getMapContextIndex(context) >= 0) {
            ContextTreeNode node;

            if (activeContext != null) {
                node = (ContextTreeNode) getMapContextNode(activeContext);
                modelSupport.fireChildChanged(new TreePath(getRoot()), getMapContextIndex(activeContext), node);
            }

            activeContext = context;
            node = (ContextTreeNode) getMapContextNode(activeContext);
            modelSupport.fireChildChanged(new TreePath(getRoot()), getMapContextIndex(activeContext), node);
        } else if (activeContext != null) {
            ContextTreeNode node = (ContextTreeNode) getMapContextNode(activeContext);
            modelSupport.fireChildChanged(new TreePath(getRoot()), getMapContextIndex(activeContext), node);
            activeContext = null;
        }

        fireContextActivated(context, getMapContextIndex(context));
    }

    /**
     * add context to the Tree if not allready in it
     * @param context the context to add
     */
    public void addMapContext(MapContext context) {

        if (getMapContextIndex(context) < 0) {
            context.addMapLayerListListener(this);

            ContextTreeNode node = new ContextTreeNode(this);
            node.setUserObject(context);

            insertNodeInto(node, (ContextTreeNode) getRoot(), getRoot().getChildCount());

            for (int i = context.getLayerCount() - 1; i >= 0; i--) {
                ContextTreeNode layer = new ContextTreeNode(this);
                layer.setUserObject(context.getLayer(i));
                insertNodeInto(layer, node, node.getChildCount());
            }

            fireContextAdded(context, getMapContextIndex(context));
            setActiveContext(context);
        }
    }

    /**
     * remove context from the tree
     * @param context target mapcontext to remove
     */
    public void removeMapContext(MapContext context) {

        for (int i = 0; i < getRoot().getChildCount(); i++) {
            ContextTreeNode jm = (ContextTreeNode) getRoot().getChildAt(i);

            if (jm.getUserObject().equals(context)) {
                removeNodeFromParent(jm);

                if (jm.getUserObject().equals(activeContext)) {
                    activeContext = null;
                    fireContextActivated(null, -1);
                }

                fireContextRemoved(context, i);
            }
        }
    }

    /**
     * count MapContext in the tree
     * @return number of mapcontext in the tree
     */
    public int getMapContextCount() {
        return getRoot().getChildCount();
    }

    /**
     * return context at index i
     * @param i position of the mapcontext
     * @return the mapcontext a position i
     */
    public MapContext getMapContext(int i) {
        return (MapContext) ((ContextTreeNode)getRoot().getChildAt(i)).getUserObject();
    }

    /**
     * get the index of a mapcontext in the tree
     * @param context the mapcontext to find
     * @return index of context
     */
    public int getMapContextIndex(MapContext context) {
        int ret = -1;

        if (context != null) {
            for (int i = 0; i < getRoot().getChildCount(); i++) {
                ContextTreeNode jm = (ContextTreeNode) getRoot().getChildAt(i);
                if (jm.getUserObject().equals(context)) {
                    ret = i;
                }
            }
        }

        return ret;
    }

    /**
     * return the node of context
     * <b>use with care!<b/>
     * @param context the context to find
     * @return the node contining the mapcontext
     */
    public TreeNode getMapContextNode(MapContext context) {
        TreeNode node = null;

        if (context != null) {
            for (int i = 0; i < getRoot().getChildCount(); i++) {
                ContextTreeNode jm = (ContextTreeNode) getRoot().getChildAt(i);
                if (jm.getUserObject().equals(context)) {
                    node = jm;
                }
            }
        }

        return node;
    }

    /**
     * moveContext depending on nodes
     * <b>use with care!<b/>
     * @param moveNode the node to move
     * @param father the new parent node
     * @param place new position of the child node
     */
    public void moveMapContext(ContextTreeNode moveNode, ContextTreeNode father, int place) {
        int depart = ((ContextTreeNode) getRoot()).getIndex(moveNode);

        removeNodeFromParent(moveNode);
        insertNodeInto(moveNode, father, place);

        fireContextMoved((MapContext) moveNode.getUserObject(), depart, place);
    }
////////////////////////////////////////////////////////////////////////////////
// LAYER MANAGEMENT - USE BY DRAG&DROP CLASSES /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add a maplayer into a node, used for drag and drop
     * @param newChild the new node
     * @param parent the father node
     * @param index new position of the child node
     */
    public void insertLayerInto(ContextTreeNode newChild, ContextTreeNode parent, int index) {


        if (newChild.getUserObject() instanceof MapLayer && parent.getUserObject() instanceof MapContext) {

            MapContext context = (MapContext) parent.getUserObject();
            MapLayer layer = (MapLayer) newChild.getUserObject();


            index = context.getLayerCount() - index;
            if (index < 0) {
                index = 0;
            }
            if (index > context.getLayerCount()) {
                index = context.getLayerCount();
            }
            if (index > parent.getChildCount()) {
                index = 0;
            }
            context.addLayer(index, layer);
        }
    }

    /**
     * remove a node maplayer from it's parent
     * @param node the node to remove
     */
    public void removeLayerFromParent(ContextTreeNode node) {

        if (node.getUserObject() instanceof MapLayer && ((ContextTreeNode) node.getParent()).getUserObject() instanceof MapContext) {

            MapContext context = (MapContext) ((ContextTreeNode)node.getParent()).getUserObject();
            MapLayer layer = (MapLayer) node.getUserObject();

            context.removeLayer(layer);
        }
    }

    /**
     * mode a specific node
     * @param Child the node to move
     * @param parent the new father node
     * @param index the position of the child node
     */
    public void moveLayer(ContextTreeNode Child, ContextTreeNode parent, int index) {

        if (Child.getUserObject() instanceof MapLayer && parent.getUserObject() instanceof MapContext) {

            MapContext context = (MapContext) parent.getUserObject();
            MapLayer layer = (MapLayer) Child.getUserObject();

            index = context.getLayerCount() - 1 - index;
            if (index < 0) {
                index = 0;
            }
            if (index > context.getLayerCount()) {
                index = context.getLayerCount();
            }
            if (index > parent.getChildCount()) {
                index = 0;
            }
            int begin = context.indexOf(layer);

            context.moveLayer(begin, index);
        }
    }
////////////////////////////////////////////////////////////////////////////////
// FIREEVENT AND LISTENERS /////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * generate a treeevent for an added node
     * @param mapcontext the added mapcontext
     * @param position the position of the mapcontext in the tree
     */
    public void fireContextAdded(MapContext mapcontext, int position) {
        TreeEvent kevent = new TreeEvent(this, mapcontext, position);

        TreeListener[] list = getTreeListeners();
        for (int i = 0; i < list.length; i++) {
            list[i].ContextAdded(kevent);
        }
    }

    /**
     * generate a treeevent for a mapcontext removed
     * @param mapcontext the removed mapcontext
     * @param position the last position of the mapcontext
     */
    public void fireContextRemoved(MapContext mapcontext, int position) {
        TreeEvent event = new TreeEvent(this, mapcontext, position);

        TreeListener[] list = getTreeListeners();
        for (int i = 0; i < list.length; i++) {
            list[i].ContextRemoved(event);
        }
    }

    /**
     * generate a treeevent for an activated mapcontext
     * @param mapcontext the activated mapcontext (null if none activated)
     * @param index the position of the activated context
     */
    public void fireContextActivated(MapContext mapcontext, int index) {
        TreeEvent event = new TreeEvent(this, mapcontext, index);

        TreeListener[] list = getTreeListeners();
        for (int i = 0; i < list.length; i++) {
            list[i].ContextActivated(event);
        }
    }

    /**
     * generate a treeevent for a moving context
     * @param mapcontext the moving mapcontext
     * @param begin the start position of the mapcontext
     * @param end the end position of the mapcontext
     */
    public void fireContextMoved(MapContext mapcontext, int begin, int end) {
        TreeEvent event = new TreeEvent(this, mapcontext, begin, end);

        TreeListener[] list = getTreeListeners();
        for (int i = 0; i < list.length; i++) {
            list[i].ContextMoved(event);
        }
    }

    /**
     * add treeListener to Model
     * @param ker the new listener
     */
    public void addTreeListener(TreeListener ker) {
        listeners.add(TreeListener.class, ker);
    }

    /**
     * remove treeListener from Model
     * @param ker the listner to remove
     */
    public void removeTreeListener(TreeListener ker) {
        listeners.remove(TreeListener.class, ker);
    }

    /**
     * get treeListeners list
     * @return the listener's table
     */
    public TreeListener[] getTreeListeners() {
        return listeners.getListeners(TreeListener.class);
    }
////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT LISTENER /////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * when a layer is added
     * @param mle the event
     */
    public void layerAdded(MapLayerListEvent mle) {
        MapContext context = (MapContext) mle.getSource();

        int i = 0;
        boolean find = false;
        while (i < getRoot().getChildCount() && !find) {

            if (((ContextTreeNode) getRoot().getChildAt(i)).getUserObject().equals(context)) {


                ContextTreeNode layer = new ContextTreeNode(this);
                layer.setUserObject(mle.getLayer());

                ContextTreeNode father = (ContextTreeNode) getRoot().getChildAt(i);


                int index = mle.getToIndex();

                index = context.getLayerCount() - 1 - mle.getToIndex();
                if (index > father.getChildCount()) {
                    index = father.getChildCount();
                }
                if (index < 0) {
                    index = 0;
                }
                insertNodeInto(layer, father, index);
            }
            i++;
        }
    }

    /**
     * when a layer is removed
     * @param mle the event
     */
    public void layerRemoved(MapLayerListEvent mle) {
        MapContext context = (MapContext) mle.getSource();

        int i = 0;
        boolean find = false;
        while (i < getRoot().getChildCount() && !find) {

            if (((ContextTreeNode) getRoot().getChildAt(i)).getUserObject().equals(context)) {

                ContextTreeNode father = (ContextTreeNode) getRoot().getChildAt(i);

                for (int t = 0; t < father.getChildCount(); t++) {
                    ContextTreeNode node = (ContextTreeNode) father.getChildAt(t);

                    if (mle.getLayer().equals(node.getUserObject())) {
                        removeNodeFromParent(node);
                    }
                }
            }
            i++;
        }
    }

    /**
     * when a layer changed
     * @param mle the event
     */
    public void layerChanged(MapLayerListEvent mle) {
        MapContext context = (MapContext) mle.getSource();

        int i = 0;
        boolean find = false;
        while (i < getRoot().getChildCount() && !find) {

            if (((ContextTreeNode) getRoot().getChildAt(i)).getUserObject().equals(context)) {

                ContextTreeNode father = (ContextTreeNode) getRoot().getChildAt(i);

                for (int t = 0; t < father.getChildCount(); t++) {
                    ContextTreeNode node = (ContextTreeNode) father.getChildAt(t);

                    if (mle.getLayer().equals(node.getUserObject())) {
                        modelSupport.fireChildChanged(new TreePath(getPathToRoot(father)), father.getIndex(node), node);
                    }
                }
            }
            i++;
        }
    }

    /**
     * when a layer moved
     * @param mle the event
     */
    public void layerMoved(MapLayerListEvent mle) {
        MapContext context = (MapContext) mle.getSource();

        int i = 0;
        boolean find = false;
        while (i < getRoot().getChildCount() && !find) {

            if (((ContextTreeNode) getRoot().getChildAt(i)).getUserObject().equals(context)) {

                ContextTreeNode father = (ContextTreeNode) getRoot().getChildAt(i);


                MapLayer layerA = mle.getLayer();
                ContextTreeNode nodeA = null;
                int indiceA = 0;

                for (int t = 0; t < father.getChildCount(); t++) {
                    ContextTreeNode node = (ContextTreeNode) father.getChildAt(t);

                    if (layerA.equals(node.getUserObject())) {
                        nodeA = node;
                        indiceA = t;
                    }
                }

                indiceA = context.getLayerCount() - 1 - indiceA;
                if (indiceA < 0) {
                    indiceA = 0;
                }
                if (indiceA > context.getLayerCount()) {
                    indiceA = context.getLayerCount();
                }
                if (indiceA > father.getChildCount()) {
                    indiceA = 0;
                }
                int depart = mle.getFromIndex();
                int fin = mle.getToIndex();

                if (indiceA == fin) {
                    int echange = fin;
                    fin = depart;
                    depart = echange;
                }

                removeNodeFromParent(nodeA);

                int index = fin;

                index = father.getChildCount() - index;
                if (index > father.getChildCount()) {
                    index = father.getChildCount();
                }
                if (index < 0) {
                    index = 0;
                }
                insertNodeInto(nodeA, father, index);
            }
            i++;
        }
    }
}