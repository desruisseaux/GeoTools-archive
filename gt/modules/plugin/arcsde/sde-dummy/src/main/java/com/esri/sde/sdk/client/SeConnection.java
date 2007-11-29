package com.esri.sde.sdk.client;

public class SeConnection {

	public static int SE_TRYLOCK_POLICY = 0;
    public static /* GEOT-947 final*/ int SE_UNPROTECTED_POLICY = 0;
	
	public SeConnection(String a, int i, String b, String c, String d) {}
	
	public void commitTransaction() throws SeException {}
	public void rollbackTransaction() throws SeException {}
	public int setTransactionAutoCommit(int i) { return -1;}
	public void startTransaction() throws SeException{}
	public String getDatabaseName() throws SeException { return null; }
	public String getUser() throws SeException { return null; }
	public void close() throws SeException {}
	public java.util.Vector getLayers() throws SeException { return null; }
	public void setConcurrency(int i)throws SeException {}
	public boolean isClosed() { return false; }
	public SeRelease getRelease() { return null; }
}