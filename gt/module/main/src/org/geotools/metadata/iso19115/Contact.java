package org.geotools.metadata.iso19115;

import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.Telephone;
import org.opengis.util.InternationalString;

public class Contact extends MetaData implements
	org.opengis.metadata.citation.Contact {
    
    private InternationalString contactInstructions;
	private InternationalString hoursOfService;
	private OnLineResource onLineResource;
	private Address address;
	private Telephone phone;
	
	public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }
    public InternationalString getContactInstructions() {
        return contactInstructions;
    }
    public void setContactInstructions(InternationalString contactInstructions) {
        this.contactInstructions = contactInstructions;
    }
    public OnLineResource getOnLineResource() {
        return onLineResource;
    }
    public void setOnLineResource(OnLineResource onLineResource) {
        this.onLineResource = onLineResource;
    }
    public Telephone getPhone() {
        return phone;
    }
    public void setPhone(Telephone phone) {
        this.phone = phone;
    }    
    public InternationalString getHoursOfService() {
        return hoursOfService;
    }
    public void setHoursOfService(InternationalString hoursOfService) {
        this.hoursOfService = hoursOfService;
    }
}
