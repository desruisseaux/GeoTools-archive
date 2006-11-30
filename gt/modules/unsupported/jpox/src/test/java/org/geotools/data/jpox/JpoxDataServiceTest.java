
package org.geotools.data.jpox;

import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;

import junit.framework.TestCase;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.jpox.PersistenceManagerFactoryImpl;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;

public class JpoxDataServiceTest extends TestCase {

	private FilterFactory ff;
	private JpoxDataService data;

	protected void setUp() throws Exception {
		PersistenceManagerFactoryImpl pmf = (PersistenceManagerFactoryImpl)JDOHelper.getPersistenceManagerFactory( "jdo.properties" );
		data = new JpoxDataService( pmf );

		ff = CommonFactoryFinder.getFilterFactory( null );
	}

	public void testData() {
		List types = data.getTypeNames();
		assertNotNull( types );
		assertFalse( types.isEmpty() );
		assertTrue( types.get( 0 ) instanceof TypeName );

		TypeName typeName = (TypeName)data.getTypeNames().get( 0 );
		Object descrption = data.describe( typeName );
		assertNotNull( descrption );
		assertTrue( descrption instanceof Class );
	}

	public void testSource() throws Exception {
//		List types = data.getTypeNames();
		TypeName typeName = (TypeName)data.getTypeNames().get( 0 );
		Object descrption = data.describe( typeName );

		// Test "default access" using Transaction.AUTO_COMMIT
		Source source = data.access( typeName );

		assertNotNull( source );
		assertEquals( descrption, source.describe() );
		assertEquals( typeName, source.getName() );

		Collection content = source.content();
		assertNotNull( content );
		assertFalse( content.isEmpty() );

		// test concurrency - source2 uses seperate Transaction
		Source source2 = data.access( typeName );

		Transaction t = new DefaultTransaction( "Source Testing" );
		source2.setTransaction( t );

		Collection content2 = source2.content();
		assertNotNull( content2 );
		assertNotSame( content, content2 );
		assertEquals( content.size(), content2.size() );

		content2.clear();
		assertTrue( content2.isEmpty() );
		assertFalse( content.isEmpty() );

		t.rollback();
		assertFalse( content2.isEmpty() );
		assertEquals( content.size(), content2.size() );
	}

	public void testSource2() throws Exception {
		TypeName typeName1 = (TypeName)data.getTypeNames().get( 0 );
//		TypeName typeName2 = (TypeName)data.getTypeNames().get( 1 );

		Transaction t = new DefaultTransaction( "Source Testing" );

		Source source1 = data.access( typeName1 );
//		Source source2 = data.access( typeName2 );

		source1.setTransaction( t );
//		source2.setTransaction( t );

		Collection victoriaGeneral = source1.content( ff.equals( ff.property( "NAME" ), ff.literal( "Victoria General Hospital" ) ) );
		assertEquals( 1, victoriaGeneral.size() );

		int before = source1.content().size();
		victoriaGeneral.clear(); // remove single hospital

		int after = source1.content().size();
		assertEquals( "remove single", before - 1, after );

//		source2.content().clear();

		assertEquals( 0, victoriaGeneral.size() );

		t.rollback();
		assertEquals( 1, victoriaGeneral.size() ); // no longer empty
//		assertFalse( source2.content().isEmpty() ); // no longered cleared
		assertEquals( before, source1.content().size() ); // just as before
	}
}
