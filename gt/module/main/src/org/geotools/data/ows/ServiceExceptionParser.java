package org.geotools.data.ows;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.geotools.ows.ServiceException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ServiceExceptionParser {

	public static ServiceException parse(InputStream inputStream) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(inputStream);
		
		Element root = document.getRootElement();
		List serviceExceptions = root.getChildren("ServiceException");
				
		/*
		 * ServiceExceptions with codes get bumped to the top of the list.
		 */
		List codes = new ArrayList();
		List noCodes = new ArrayList();
		for (int i = 0; i < serviceExceptions.size(); i++) {
			Element element = (Element) serviceExceptions.get(i);
			ServiceException exception = parseSE(element);
			if (exception.getCode() != null && exception.getCode().length() != 0 ) {
				codes.add(exception);
			} else {
				noCodes.add(exception);
			}
		}
		
		/*
		 * Now chain them.
		 */
		ServiceException firstException = null;
		ServiceException recentException = null;
		for (int i = 0; i < codes.size(); i++) {
			ServiceException exception = (ServiceException) codes.get(i);
			if (firstException == null) {
				firstException = exception;
				recentException = exception;
			} else {
				recentException.setNext(exception);
				recentException = exception;
			}
		}
		codes = null;
		for (int i = 0; i < noCodes.size(); i++) {
			ServiceException exception = (ServiceException) noCodes.get(i);
			if (firstException == null) {
				firstException = exception;
				recentException = exception;
			} else {
				recentException.setNext(exception);
				recentException = exception;
			}
		}
		noCodes = null;
		
		return firstException;		
	}

	private static ServiceException parseSE(Element element) {
		String errorMessage = element.getText();
		String code = element.getAttributeValue("code");
		
		return new ServiceException(errorMessage, code);
	}

}
