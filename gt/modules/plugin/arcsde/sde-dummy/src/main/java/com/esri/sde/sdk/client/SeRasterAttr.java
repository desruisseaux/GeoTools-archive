package com.esri.sde.sdk.client;

import java.awt.Dimension;

public class SeRasterAttr {

    public int getPixelType() { return -1; }
    public int getTileHeight() { return -1; }
    public int getTileWidth() { return -1; }
    public SeRasterBand[] getBands() throws SeException { return null; }
    public int getMaxLevel() { return -1; }
    public boolean skipLevelOne() { return false; }
    public SeExtent getExtentByLevel(int i) throws SeException { return null; }
    public int getImageWidthByLevel(int i) { return -1; }
    public int getImageHeightByLevel(int i) { return -1; }
    public SDEPoint getImageOffsetByLevel(int i) { return null; }
    public int getTilesPerRowByLevel(int i) { return -1; }
    public int getTilesPerColByLevel(int i) { return -1; }
    public int getNumBands() { return -1; }
    public SeRasterBand getBandInfo(int i) { return null; }
    public SeObjectId getRasterColumnId() { return null; }
    public SeExtent getExtent() throws SeException { return null; }
    
}
