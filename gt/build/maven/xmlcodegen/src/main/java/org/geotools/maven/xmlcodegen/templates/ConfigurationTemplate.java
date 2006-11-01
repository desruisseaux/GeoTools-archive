package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import java.io.*;
import org.eclipse.xsd.*;
import org.geotools.xml.*;

public class ConfigurationTemplate
{
  protected static String nl;
  public static synchronized ConfigurationTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    ConfigurationTemplate result = new ConfigurationTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "import org.eclipse.xsd.util.XSDSchemaLocationResolver;\t" + NL + "import org.geotools.xml.BindingConfiguration;" + NL + "import org.geotools.xml.Configuration;" + NL + "" + NL + "/**" + NL + " * Parser configuration for the ";
  protected final String TEXT_2 = " schema." + NL + " *" + NL + " * @generated" + NL + " */" + NL + "public class ";
  protected final String TEXT_3 = "Configuration extends Configuration {" + NL + "" + NL + "    /**" + NL + "     * Creates a new configuration." + NL + "     * " + NL + "     * @generated" + NL + "     */     " + NL + "    public ";
  protected final String TEXT_4 = "Configuration() {" + NL + "       super();" + NL + "       " + NL + "       //TODO: add dependencies here" + NL + "    }" + NL + "    " + NL + "    /**" + NL + "     * @return the schema namespace uri: ";
  protected final String TEXT_5 = "." + NL + "     * @generated" + NL + "     */" + NL + "    public String getNamespaceURI() {" + NL + "    \treturn ";
  protected final String TEXT_6 = ".NAMESPACE;" + NL + "    }" + NL + "    " + NL + "    /**" + NL + "     * @return the uri to the the ";
  protected final String TEXT_7 = " ." + NL + "     * @generated" + NL + "     */" + NL + "    public String getSchemaFileURL() {" + NL + "        return getSchemaLocationResolver().resolveSchemaLocation( " + NL + "           null, getNamespaceURI(), \"";
  protected final String TEXT_8 = "\"" + NL + "        );" + NL + "    }" + NL + "    " + NL + "    /**" + NL + "     * @return new instanceof {@link ";
  protected final String TEXT_9 = "BindingConfiguration%>}." + NL + "     */    " + NL + "    public BindingConfiguration getBindingConfiguration() {" + NL + "     \treturn new ";
  protected final String TEXT_10 = "BindingConfiguration();" + NL + "    }" + NL + "    " + NL + "    /**" + NL + "     * @return A new instance of {@link ";
  protected final String TEXT_11 = "SchemaLocationResolver%>}." + NL + "     */" + NL + "    public XSDSchemaLocationResolver getSchemaLocationResolver() {" + NL + "    \treturn new ";
  protected final String TEXT_12 = "SchemaLocationResolver();" + NL + "    }" + NL + "} ";

  public String generate(Object argument)
  {
    StringBuffer stringBuffer = new StringBuffer();
     	
	XSDSchema schema = (XSDSchema)argument;
	String namespace = schema.getTargetNamespace();
	String prefix = Schemas.getTargetPrefix( schema ).toUpperCase();
	
	String file = new File( schema.eResource().getURI().toFileString() ).getName();

    stringBuffer.append(TEXT_1);
    stringBuffer.append(namespace);
    stringBuffer.append(TEXT_2);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_3);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_4);
    stringBuffer.append(namespace);
    stringBuffer.append(TEXT_5);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_6);
    stringBuffer.append(file);
    stringBuffer.append(TEXT_7);
    stringBuffer.append(file);
    stringBuffer.append(TEXT_8);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_9);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_10);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_11);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_12);
    return stringBuffer.toString();
  }
}
