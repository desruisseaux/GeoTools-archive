package com.esri.sde.sdk.client;

public class SeConnection {

	public static final int SE_UNPROTECTED_POLICY = 0;
	
	public SeConnection(String a, int i, String b, String c, String d) {}
	
	public void commitTransaction() throws SeException {}
	public void rollbackTransaction() throws SeException {}
	public void setTransactionAutoCommit(int i) {}
	public void startTransaction() {}
	public String getDatabaseName() throws SeException { return null; }
	public String getUser() throws SeException { return null; }
	public void close() throws SeException {}
	public java.util.List getLayers() throws SeException { return null; }
	public void setConcurrency(int i) {}
	public boolean isClosed() { return false; }
}