package org.geotools.validation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DefaultRepository;
import org.geotools.resources.TestData;
import org.geotools.validation.xml.ValidationException;
import org.geotools.validation.xml.XMLReader;
import org.geotools.data.Repository;

/**
 * A proper test fixture for the ValidationProcessor (and friends to hit).
 * <p>
 * For geoserver developers this environment is similar to UserBasic.
 * Where possible names have been forced to agree with geoserver.
 * </p>
 * @author Jody Garnett
 */
public class TestFixture {
	Repository data = new DefaultRepository();
	public Map pluginDTOs;
	public Map testSuiteDTOs;
	public ValidationProcessor processor;
	public DefaultRepository repository;
	
	public TestFixture() throws Exception {
		pluginDTOs = XMLReader.loadPlugIns(TestData.file( this, "plugins" ));
		testSuiteDTOs = XMLReader.loadValidations( TestData.file( this, "validation" ), pluginDTOs );		
		processor = new ValidationProcessor();
		processor.load(pluginDTOs, testSuiteDTOs );
		repository = new DefaultRepository();
		repository.load( TestData.file( this, "registry.properties" ));
	}
}
