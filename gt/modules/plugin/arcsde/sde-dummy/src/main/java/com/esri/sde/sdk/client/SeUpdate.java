package com.esri.sde.sdk.client;

public class SeUpdate {
	
	public SeUpdate(SeConnection c) {}
	
	public void toTable(String s, String[] y, String x) {}
	public void setWriteMode(boolean b) {}
	public SeRow getRowToSet() { return null; }
	public void execute() {}
	public void close() {}

    public SeRow singleRow(SeObjectId seObjectId, String typeName, String[] rowColumnNames) throws SeException{
        return null;
    }
}
