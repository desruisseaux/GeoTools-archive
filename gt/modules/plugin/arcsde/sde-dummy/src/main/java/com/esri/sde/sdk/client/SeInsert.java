package com.esri.sde.sdk.client;

import java.io.IOException;

public class SeInsert {
	
	public SeInsert(SeConnection c) {}
	
	public void intoTable(String s, String[] t) {}
	public void setWriteMode(boolean b) {}
	public SeRow getRowToSet() { return null; }
	public void execute() throws SeException{}
	public void close()throws SeException {}

    public SeObjectId lastInsertedRowId() {return null;}

    public void flushBufferedWrites()throws IOException {}

}
