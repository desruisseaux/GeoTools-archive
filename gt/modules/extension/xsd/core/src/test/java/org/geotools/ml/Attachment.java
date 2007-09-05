package org.geotools.ml;

public class Attachment {
	String name;
	MimeType mimeType;
	String content;
	
	public Attachment(String name, MimeType mimeType, String content) {
		this.name = name;
		this.mimeType = mimeType;
		this.content = content;
	}
}
