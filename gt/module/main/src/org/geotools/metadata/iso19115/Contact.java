package org.geotools.metadata.iso19115;

import java.util.Locale;

import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.Telephone;

public class Contact extends MetaData implements
		org.opengis.metadata.citation.Contact {

	private String contactInstructions;
	private String hoursOfService;
	private OnLineResource onLineResource;
	private Address address;
	private Telephone phone;
	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Contact#getPhone()
	 */
	public Telephone getPhone() {
		return phone;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Contact#getAddress()
	 */
	public Address getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Contact#getOnLineResource()
	 */
	public OnLineResource getOnLineResource() {
		return onLineResource;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Contact#getHoursOfService(java.util.Locale)
	 */
	public String getHoursOfService(Locale arg0) {
		return hoursOfService;
	}
	public String getHoursOfService() {
		return hoursOfService;
	}
	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Contact#getContactInstructions(java.util.Locale)
	 */
	public String getContactInstructions(Locale arg0) {
		return contactInstructions;
	}
	public String getContactInstructions() {
		return contactInstructions;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public void setContactInstructions(String contactInstructions) {
		this.contactInstructions = contactInstructions;
	}
	public void setHoursOfService(String hoursOfService) {
		this.hoursOfService = hoursOfService;
	}
	public void setOnLineResource(OnLineResource onLineResource) {
		this.onLineResource = onLineResource;
	}
	public void setPhone(Telephone phone) {
		this.phone = phone;
	}
}
