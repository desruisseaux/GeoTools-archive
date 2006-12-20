package org.geotools.ml;

import java.util.Calendar;

public class Envelope {
	String from;
	String to;
	Calendar date;
	String subject;
	Header[] headers;
	
	public Envelope(String from, String to, Calendar date, String subject, Header[] headers) {
		super();
		
		this.date = date;
		this.from = from;
		this.headers = headers;
		this.subject = subject;
		this.to = to;
	}

	public Calendar getDate() {
		return date;
	}

	public String getFrom() {
		return from;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public String getSubject() {
		return subject;
	}

	public String getTo() {
		return to;
	}
	
	
	
	
}
