package org.geotools.metadata.iso19115;

import java.net.URL;

import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.util.InternationalString;

public class OnLineResource extends MetaData implements
		org.opengis.metadata.citation.OnLineResource {
	private OnLineFunction function;
	private InternationalString description;
	private String applicationProfile;
	private String protocol;
	private URL linkage;
    public String getApplicationProfile() {
        return applicationProfile;
    }
    public void setApplicationProfile(String applicationProfile) {
        this.applicationProfile = applicationProfile;
    }
    public InternationalString getDescription() {
        return description;
    }
    public void setDescription(InternationalString description) {
        this.description = description;
    }
    public OnLineFunction getFunction() {
        return function;
    }
    public void setFunction(OnLineFunction function) {
        this.function = function;
    }
    public URL getLinkage() {
        return linkage;
    }
    public void setLinkage(URL linkage) {
        this.linkage = linkage;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
