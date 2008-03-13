package com.esri.sde.sdk.client;

import java.util.Calendar;
import java.util.logging.Level;

public class SeRow {
	
	public static /* GEOT-947 final*/ int SE_IS_NULL_VALUE = 0;
	public static /* GEOT-947 final*/ int SE_IS_REPEATED_FEATURE = 1;
	public static /* GEOT-947 final*/ int SE_IS_ALREADY_FETCHED = 2;
	public static /* GEOT-947 final*/ int SE_IS_NOT_NULL_VALUE = 3;
	
	public SeColumnDefinition[] getColumns() { return null; }
	public Object getObject(int i) throws SeException { return null; }
	public SeColumnDefinition getColumnDef(int i) { return null; }
	public void setInteger(int i, Integer b) {}
	public void setShort(int i, Short s) {}
	public void setFloat(int i, Float f) {}
	public void setDouble(int i, Double d) {}
	public void setString(int i, String s) {}
	public void setTime(int i, Calendar c) {}
	public void setShape(int i, SeShape s) {}
	public void setRaster(int i, SeRasterAttr a) {}
	public SeRasterAttr getRaster(int i) { return null; }
	public SeRasterTile getRasterTile() { return null; }
	public short getNumColumns() { return -1; }
	public int getIndicator(int i) { return -1; }
    public Level getInteger(int i) throws SeException {return null;}
    public void setLong(int index, Long value) {}
	
	
}
