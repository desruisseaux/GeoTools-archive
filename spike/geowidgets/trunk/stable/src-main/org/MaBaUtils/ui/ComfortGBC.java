/** This class stems from another library, published under LGPL. It should
 * at the current state not be considered part of the core GeoWidgets code. */
package org.MaBaUtils.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;

/** A convenience extensions of the <code>java.awt.GridBagConstraints</code>.
 * Allows for a precise control about the settings with much less code.
 *
 * @author  Matthias Basler
 */
public class ComfortGBC extends GridBagConstraints{
    private static final long serialVersionUID = 3906369311680442416L;
    
    /** Creates new ComfortGridBagConstraints for a component. */
    public ComfortGBC() {
        super();
    }
    /** Creates new ComfortGridBagConstraints for a component
     * with a specified location. <p>
     * Set x and/or y = -1 for "Relative".
     */
    public ComfortGBC(int x, int y) {
        super();
        super.gridx = x;
        super.gridy = y;
    }
    /** Creates new ComfortGridBagConstraints for a component
     * with a specified location and size. <p>
     * Set x and/or y = -1 for "Relative".
     * Set gridWidth and/or gridHeight = 0 for "Remainder". <p>
     */
    public ComfortGBC(int x, int y, int gridWidth, int gridHeight) {
        super();
        super.gridx = x;
        super.gridy = y;
        super.gridwidth = gridWidth;
        super.gridheight = gridHeight;
    }
    
    /** Set x and/or y = -1 for "Relative".*/
    public ComfortGBC setPos(int x, int y){
        super.gridx = x;
        super.gridy = y;
        return this;
    }    
    public ComfortGBC setPos(Point pos){
        super.gridx = pos.x;
        super.gridy = pos.y;
        return this;
    }    
    
    /** Set gridWidth and/or gridHeight = 0 for "Remainder".*/
    public ComfortGBC setSize(int gridWidth, int gridHeight){
        super.gridwidth = gridWidth;
        super.gridheight = gridHeight;
        return this;
    }    
    public ComfortGBC setSize(Dimension size){
        super.gridwidth = size.width;
        super.gridheight = size.height;
        return this;
    }    
    
    public ComfortGBC setAnchor(int anchor){
        super.anchor = anchor;          return this;
    }
    public ComfortGBC anchorN(){
        super.anchor = super.NORTH;     return this;
    }
    public ComfortGBC anchorNE(){
        super.anchor = super.NORTHEAST; return this;
    }
    public ComfortGBC anchorE(){
        super.anchor = super.EAST;      return this;
    }
    public ComfortGBC anchorSE(){
        super.anchor = super.SOUTHEAST; return this;
    }
    public ComfortGBC anchorS(){
        super.anchor = super.SOUTH;     return this;
    }
    public ComfortGBC anchorSW(){
        super.anchor = super.SOUTHWEST; return this;
    }
    public ComfortGBC anchorW(){
        super.anchor = super.WEST;      return this;
    }
    public ComfortGBC anchorNW(){
        super.anchor = super.NORTHWEST; return this;
    }
    
    
    public ComfortGBC setWeight(double weightX, double weightY){
        super.weightx = weightX;
        super.weighty = weightY;
        return this;
    }
    
    
    public ComfortGBC setFill(int fillConst){
        super.fill = fillConst;         return this;
    }
    public ComfortGBC fillNot(){
        super.fill = super.NONE;        return this;
    }
    public ComfortGBC fillX(){
        super.fill = super.HORIZONTAL;  return this;
    }
    public ComfortGBC fillY(){
        super.fill = super.VERTICAL;    return this;
    }
    public ComfortGBC fillXY(){
        super.fill = super.BOTH;        return this;
    }
    
    public ComfortGBC setPadding(int ipadX, int ipadY){
        super.ipadx = ipadX;
        super.ipady = ipadY;
        return this;
    }
    
    public ComfortGBC setInsets(Insets insets){
        super.insets = insets;          return this;
    }
    
    public ComfortGBC setInsets(int insets){
        super.insets = new Insets(insets, insets, insets, insets);
        return this;
    }
    /** Note that the argument order is different from the order when building
     * an Insets object! */
    public ComfortGBC setInsets(int left, int top, int right, int bottom){
        super.insets = new Insets(top, left, bottom, right);
        return this;
    }
}
