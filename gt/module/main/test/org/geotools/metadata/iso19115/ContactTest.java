package org.geotools.metadata.iso19115;

import java.util.List;

import junit.framework.TestCase;

public class ContactTest extends TestCase {
	public void testContact(){
		Telephone phone = new Telephone();
		phone.setFacsimile("555-1234");
		phone.setVoice("555-VOICE");
		
		Contact contact = new Contact();
		contact.setPhone( phone );
		contact.setContactInstruction("n/a");
				
		assertEquals( phone, contact.getPhone() );
		assertEquals( "n/a", contact.getContactInstruction() );
		
		assertEquals( 21, contact.elements().size() );
		System.out.println( contact.getEntityType().getElements() );
	}
	public void xtestElementAccess(){
		Telephone phone = new Telephone();
		phone.setFacsimile("555-1234");
		phone.setVoice("555-VOICE");
		
		Contact contact = new Contact();
		contact.setPhone( phone );
		
		assertEquals( phone, contact.getElement( "phone" ) );
		
		assertEquals( "555-VOICE", contact.getElement( "phone/voice" ) );
		
		Object both = contact.getElement( "phone/*" );
		assertNotNull( both );
		assertTrue( both instanceof List );
		List list = (List) both;
		assertTrue( list.contains( "555-VOICE" ) );
		assertTrue( list.contains( "555-1234" ) );		
	}
}
