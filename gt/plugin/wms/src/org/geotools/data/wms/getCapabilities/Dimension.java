/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

/**
 * @author rgould
 *
 * The Dimension element declares the existance of a dimension 
 */
public class Dimension {
    private String name;
    private String units;
    private String unitSymbol;
    
    
    /**
     * @param name
     * @param units
     */
    public Dimension(String name, String units) {
        super();
        this.name = name;
        this.units = units;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUnits() {
        return units;
    }
    public void setUnits(String units) {
        this.units = units;
    }
    public String getUnitSymbol() {
        return unitSymbol;
    }
    public void setUnitSymbol(String unitSymbol) {
        this.unitSymbol = unitSymbol;
    }
}
