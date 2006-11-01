package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import java.io.*;
import org.eclipse.xsd.*;
import org.geotools.xml.*;

public class SchemaLocationResolverTemplate
{
  protected static String nl;
  public static synchronized SchemaLocationResolverTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    SchemaLocationResolverTemplate result = new SchemaLocationResolverTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = NL + "import org.eclipse.xsd.XSDSchema;" + NL + "import org.eclipse.xsd.util.XSDSchemaLocationResolver;" + NL + "" + NL + "/**" + NL + " * " + NL + " * @generated" + NL + " */" + NL + "public class ";
  protected final String TEXT_2 = "SchemaLocationResolver implements XSDSchemaLocationResolver {" + NL + "" + NL + "\t/**" + NL + "\t * <!-- begin-user-doc -->" + NL + "\t * <!-- end-user-doc -->" + NL + "\t *" + NL + "\t *\t@generated modifiable" + NL + "\t */" + NL + "\tpublic String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,  String schemaLocationURI) {" + NL + "\t\tif (schemaLocationURI == null)" + NL + "\t\t\treturn null;" + NL + "\t\t\t" + NL + "\t\t//if no namespace given, assume default for the current schema" + NL + "\t\tif ((namespaceURI == null || \"\".equals(namespaceURI)) && xsdSchema != null) {" + NL + "\t\t\tnamespaceURI = xsdSchema.getTargetNamespace();" + NL + "\t\t}" + NL + "\t\t\t";
  protected final String TEXT_3 = NL + "\t\tif (\"";
  protected final String TEXT_4 = "\".equals(namespaceURI)) {" + NL + "\t\t\tif (schemaLocationURI.endsWith(\"";
  protected final String TEXT_5 = "\")) {" + NL + "\t\t\t\treturn getClass().getResource(\"";
  protected final String TEXT_6 = "\").toString();" + NL + "\t\t\t}" + NL + "\t\t}";
  protected final String TEXT_7 = NL + "\t\t" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "}";

  public String generate(Object argument)
  {
    StringBuffer stringBuffer = new StringBuffer();
     	
	Object[] args = (Object[])argument;
	XSDSchema schema = (XSDSchema)args[0] ;
	List includes = (List)args[1];
	List namespaces = (List)args[2];
	
	String ns = schema.getTargetNamespace();
	String prefix = Schemas.getTargetPrefix( schema );

    stringBuffer.append(TEXT_1);
    stringBuffer.append(prefix.toUpperCase());
    stringBuffer.append(TEXT_2);
    
	for (int i = 0; i < includes.size(); i++) {
		File include = (File)includes.get(i);	
		String namespace = (String)namespaces.get(i);

    stringBuffer.append(TEXT_3);
    stringBuffer.append(namespace);
    stringBuffer.append(TEXT_4);
    stringBuffer.append(include.getName());
    stringBuffer.append(TEXT_5);
    stringBuffer.append(include.getName());
    stringBuffer.append(TEXT_6);
    
	}

    stringBuffer.append(TEXT_7);
    return stringBuffer.toString();
  }
}
