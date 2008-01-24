/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.gui.swing.toolbox.tools.shapecreation;

/**
 *
 * @author Administrateur
 */
class Data {

    public static enum TYPE{
        INTEGER,
        LONG,
        DOUBLE,
        STRING,
        DATE
    }
    
    
    public String name = "name";
    public TYPE type = TYPE.STRING;
    
}
