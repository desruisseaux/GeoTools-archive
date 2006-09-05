package com.esri.sde.sdk.client;

public class SeTable {
	
	public SeTable(SeConnection s, String y) throws SeException {}
	
	public String getQualifiedName() { return null; }
	public String getName() { return null; }
	public void addColumn(SeColumnDefinition s) {}
	public void dropColumn(String s) {}
	public void delete() throws SeException {}
	public void create(SeColumnDefinition[] c, String s) {}
	public SeColumnDefinition[] describe() throws SeException { return null; }
	
	public class SeTableStats {
		public static final int SE_COUNT_STATS = 0;
		public int getCount() { return 0; }
	}
	
}
