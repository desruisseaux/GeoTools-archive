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
 * The Extent element indicates what values along a dimension are valid.
 */
public class Extent {
    private String value;
    
    private String name;
    private String _default;
    
    /**
     * @param value
     * @param name
     */
    public Extent(String value, String name) {
        super();
        this.value = value;
        this.name = name;
    }
    public String get_default() {
        return _default;
    }
    public void set_default(String _default) {
        this._default = _default;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
