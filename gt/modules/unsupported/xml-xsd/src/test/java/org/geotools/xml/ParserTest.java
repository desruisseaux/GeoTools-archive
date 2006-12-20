package org.geotools.xml;

import java.util.List;

import org.geotools.ml.MLConfiguration;
import org.geotools.ml.Mail;
import org.geotools.ml.bindings.MLSchemaLocationResolver;

import junit.framework.TestCase;

public class ParserTest extends TestCase {

	public void testParse() throws Exception {

		Parser parser = new Parser( new MLConfiguration() );
		List mails = (List) parser.parse( MLSchemaLocationResolver.class.getResourceAsStream( "mails.xml" ) );
	
		assertEquals( 2, mails.size() );
		
		Mail mail = (Mail) mails.get( 0 );
		assertEquals( 0, mail.getId().intValue() );
		
		mail = (Mail) mails.get( 1 );
		assertEquals( 1, mail.getId().intValue() );
	}
	
	public void testParseValid() throws Exception {
		Parser parser = new Parser( new MLConfiguration() );
		parser.parse( MLSchemaLocationResolver.class.getResourceAsStream( "mails.xml" ) );
	
		assertEquals( 0, parser.getValidationErrors().size() );
	}
	
	public void testParseInValid() throws Exception {
		Parser parser = new Parser( new MLConfiguration() );
		parser.parse( MLSchemaLocationResolver.class.getResourceAsStream( "mails-invalid.xml" ) );
	
		assertFalse( 0 == parser.getValidationErrors().size() );
	}
}