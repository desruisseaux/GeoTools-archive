package org.geotools.metadata.iso19115;

import java.net.URL;
import java.util.Locale;

import org.opengis.metadata.citation.OnLineFunction;

public class OnLineResource extends MetaData implements
		org.opengis.metadata.citation.OnLineResource {

	private OnLineFunction function;
	private String description;
	private String applicationProfile;
	private String protocol;
	private URL linkage;

	public URL getLinkage() {
		return linkage;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getApplicationProfile() {
		return applicationProfile;
	}
	public String getDescription(Locale arg0) {
		return description;
	}
	public OnLineFunction getFunction() {
		return function;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setApplicationProfile(String applicationProfile) {
		this.applicationProfile = applicationProfile;
	}
	public void setFunction(OnLineFunction function) {
		this.function = function;
	}
	public void setLinkage(URL linkage) {
		this.linkage = linkage;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
