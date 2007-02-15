package org.geotools.data.complex.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.util.CheckedArrayList;

public class TypeMapping implements Serializable{

	private static final long serialVersionUID = 1444252634598922057L;
	private String sourceDataStore;
	private String sourceTypeName;
	private String targetElementName;
	private List groupbyAttributeNames = Collections.EMPTY_LIST;
	private List attributeMappings = Collections.EMPTY_LIST;
		
	public TypeMapping(){
		//no-op
	}
	
	public List getAttributeMappings() {
		return new ArrayList(attributeMappings);
	}
	public void setAttributeMappings(List attributeMappings) {
		this.attributeMappings = new CheckedArrayList(AttributeMapping.class);
		if(attributeMappings != null) {
            this.attributeMappings.addAll(attributeMappings);
        }
	}
	
	public List getGroupbyAttributeNames() {
		return new ArrayList(groupbyAttributeNames);
	}
	
	public void setGroupbyAttributeNames(List groupbyAttributeNames) {
		this.groupbyAttributeNames = new CheckedArrayList(String.class);
		if(groupbyAttributeNames != null){
			this.groupbyAttributeNames.addAll(groupbyAttributeNames);
		}
	}
	
	public String getSourceDataStore() {
		return sourceDataStore;
	}
	
	public void setSourceDataStore(String sourceDataStore) {
		this.sourceDataStore = sourceDataStore;
	}
	
	public String getSourceTypeName() {
		return sourceTypeName;
	}
	
	public void setSourceTypeName(String sourceTypeName) {
		this.sourceTypeName = sourceTypeName;
	}
	
	public String getTargetElementName() {
		return targetElementName;
	}
	
	public void setTargetElementName(String targetElementName) {
		this.targetElementName = targetElementName;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("TypeMappingDTO[")
		.append("sourceDataStore=").append(sourceDataStore)
		.append(",\n sourceTypeName=").append(sourceTypeName)
		.append(",\n targetElementName=").append(targetElementName)
		.append(",\n groupbyAttributeNames=").append(groupbyAttributeNames)
		.append(",\n attributeMappings=").append(attributeMappings)
		.append("]");
		return sb.toString();
		
	}
		
}
