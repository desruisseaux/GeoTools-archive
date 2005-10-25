package org.geotools.brewer.color;


import java.awt.Color;

/**
 *
 * @author James
 */
public class BrewerPalette {
    
    public PaletteSuitability suitability;
	public SampleScheme sampler;
    Color colors[] = new Color[13];
    
    /** Creates a new instance of BrewerPalette */
    public BrewerPalette() {
    }
    
    /**
     * Holds value of property name.
     */
    private String name;
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Holds value of property type.
     */
    private String type;
    
    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public String getType() {
        
        return this.type;
    }
    
    /**
     * Setter for property type.
     * @param type New value of property type.
     */
    public void setType(String type) {
        this.type = type;
    }
    
    public void setColors(Color[] colors){
        this.colors = colors;
    }
    
    public Color[] getColors(int length){
        int[] lookup = sampler.getSampleScheme(length);
        Color[] result = new Color[length];
        for(int i=0; i<length;i++){
            result[i]=colors[lookup[i]];
        }
        return result;
    }
    
    public Color getColor(int index,int length){
        return getColors(length)[index];
    }
    
}
