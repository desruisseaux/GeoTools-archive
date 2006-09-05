package com.esri.sde.sdk.client;

import com.esri.sde.sdk.client.SeTable.SeTableStats;

public class SeQuery {
	
	public SeQuery(SeConnection c, String[] s, SeSqlConstruct y) {}
	
	public static final int SE_OPTIMIZE = 0;
	
	public void prepareQuery() {}
	public SeExtent calculateLayerExtent(SeQueryInfo i) { return null; }
	public void close() throws SeException {}
	public void execute()throws SeException {}
	public void flushBufferedWrites() {}
	public void cancel(boolean b) {}
	public void setRowLocking(int i) {}
	public SeRow fetch() { return null; }
	public void setSpatialConstraints(int i, boolean b, SeFilter[] f) {}
	public SeTableStats calculateTableStatistics(String s, int i, SeQueryInfo q, int j) { return null; }

}
