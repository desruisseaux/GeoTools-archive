package com.esri.sde.sdk.client;

public class SeRegistration {
	
	public static /* GEOT-947 final*/ int SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE = 0;
	public static /* GEOT-947 final*/ int SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER = 1;
	public static /* GEOT-947 final*/ int SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE = 2;
    
	public SeRegistration(SeConnection c, String s) throws SeException{}
	
	public String getRowIdColumnName() { return null; }
	public void setRowIdColumnName(String s) {}
	public int getRowIdColumnType() throws SeException { return -1;}
	public void setRowIdColumnType(int i) {}
	public void alter() {}
	public String getTableName() { return null; }

    public boolean isMultiVersion() {return false;}

    public boolean isView() {return false;}

    public void setMultiVersion(boolean b) {}

}
