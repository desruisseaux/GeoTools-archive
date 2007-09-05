package org.geotools.ml;

import java.math.BigInteger;

public class Mail {

	BigInteger id;
	Envelope envelope;
	String body;
	Attachment[] attachments;
	
	public Mail(BigInteger id ,String body, Envelope envelope, Attachment[] attachments) {
		super();
		
		this.id = id;
		this.body = body;
		this.envelope = envelope;
		this.attachments = attachments;
	}
	
	public BigInteger getId() {
		return id;
	}
	
	public String getBody() {
		return body;
	}
	
	public Envelope getEnvelope() {
		return envelope;
	}
	
	public Attachment[] getAttachments() {
		return attachments;
	}
}

