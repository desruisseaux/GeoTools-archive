/*
 * Created on Jun 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.net.URL;
import java.util.List;

/**
 * @author rgould
 *
 * This is a data model for the OGC WMS Service metadata. Feel free to extend it
 * to other OGC services.
 * 
 * Name, Title and OnlineResource are Required. Everything else is optional.
 */
public class Service {
	
	/** The name of the Service (machine readible, typically one word) - Required */
	private String name;
	
	/** The title for the service (human readible) - Required */
	private String title;
	
	/** The URL pointing to where this Service can be accessed - Required */
	private URL onlineResource;
	
	/** Contains Strings specifying keywords that apply to the Service. Can be used for searching, etc */
	private List keywordList;
	
	/** Abstract allows a description providing more information about the Service */
	private String _abstract;
	
	/** Information about a contact person for the service. */
	private ContactInformation contactInformation;
	
	/** Defaults to "none" if no fees are specified */
	private String fees = "none";
	
	/** Defaults to "none" if no access constraints are specified */
	private String accessConstraints = "none";
	
	
    /**
     * @param name
     * @param title
     * @param onlineResource
     */
    public Service(String name, String title, URL onlineResource) {
        super();
        this.name = name;
        this.title = title;
        this.onlineResource = onlineResource;
    }
    public String get_abstract() {
        return _abstract;
    }
    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }
    public String getAccessConstraints() {
        return accessConstraints;
    }
    public void setAccessConstraints(String accessConstraints) {
        this.accessConstraints = accessConstraints;
    }
    public ContactInformation getContactInformation() {
        return contactInformation;
    }
    public void setContactInformation(ContactInformation contactInformation) {
        this.contactInformation = contactInformation;
    }
    public String getFees() {
        return fees;
    }
    public void setFees(String fees) {
        this.fees = fees;
    }
    public List getKeywordList() {
        return keywordList;
    }
    public void setKeywordList(List keywordList) {
        this.keywordList = keywordList;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
