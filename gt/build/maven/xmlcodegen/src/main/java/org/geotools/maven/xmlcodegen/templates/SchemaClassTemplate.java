package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import java.io.*;
import org.opengis.feature.type.Schema;
import org.geotools.maven.xmlcodegen.*;
import org.geotools.feature.*;
import org.opengis.feature.type.TypeName;
import org.apache.xml.serialize.*;
import org.eclipse.xsd.*;

public class SchemaClassTemplate
{
  protected static String nl;
  public static synchronized SchemaClassTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    SchemaClassTemplate result = new SchemaClassTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "";
  protected final String TEXT_2 = NL + NL + NL + "import org.geotools.feature.AttributeType;" + NL + "import org.geotools.feature.AttributeTypeFactory;" + NL + "import org.geotools.feature.Name;" + NL + "import org.geotools.feature.type.SchemaImpl;" + NL;
  protected final String TEXT_3 = NL + "import ";
  protected final String TEXT_4 = ";";
  protected final String TEXT_5 = NL + NL + "public class ";
  protected final String TEXT_6 = "Schema extends SchemaImpl {" + NL;
  protected final String TEXT_7 = NL + "\t/**" + NL + "\t * <p>" + NL + " \t *  <pre>" + NL + " \t *   <code>";
  protected final String TEXT_8 = NL + " \t *  ";
  protected final String TEXT_9 = NL + "\t *" + NL + "\t *    </code>" + NL + "\t *   </pre>" + NL + "\t * </p>" + NL + "\t *" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static final AttributeType ";
  protected final String TEXT_10 = "_TYPE = " + NL + "\t\tAttributeTypeFactory.newAttributeType( \"";
  protected final String TEXT_11 = "\", ";
  protected final String TEXT_12 = " );";
  protected final String TEXT_13 = NL + NL + "\tpublic ";
  protected final String TEXT_14 = "Schema() {" + NL + "\t\tsuper(\"";
  protected final String TEXT_15 = "\");" + NL + "\t\t";
  protected final String TEXT_16 = NL + "\t\tput(new Name( \"";
  protected final String TEXT_17 = "\", \"";
  protected final String TEXT_18 = "\" ),";
  protected final String TEXT_19 = "_TYPE);";
  protected final String TEXT_20 = NL + "\t}" + NL + "}";

  public String generate(Object argument)
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
     	
	Object[] arguments = (Object[]) argument;
	Schema schema = (Schema) arguments[0];

	String prefix = (String) arguments[1];
	prefix = prefix.toUpperCase();
	
	SchemaGenerator sg = (SchemaGenerator) arguments[2];

    stringBuffer.append(TEXT_2);
    
	HashMap ns2import = new HashMap();
	for (Iterator itr = sg.getImports().iterator(); itr.hasNext();) {
		Schema imported = (Schema)itr.next();
		String fullClassName = imported.getClass().getName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1);
		
		ns2import.put(imported.namespace().getURI(), className);

    stringBuffer.append(TEXT_3);
    stringBuffer.append(fullClassName);
    stringBuffer.append(TEXT_4);
    
	}

    stringBuffer.append(TEXT_5);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_6);
    
	for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
		AttributeType type = (AttributeType) itr.next();
		
		String name = type.getName();
		String binding = type.getType().getName() + ".class";

    stringBuffer.append(TEXT_7);
    
	  XSDTypeDefinition xsdType = sg.getXSDType(type);
	  OutputFormat output = new OutputFormat();
  	  output.setOmitXMLDeclaration(true);
  	  output.setIndenting(true);

	  StringWriter writer = new StringWriter();
	  XMLSerializer serializer = new XMLSerializer(writer,output);
	
	  try {
	    serializer.serialize(xsdType.getElement());
	  }
	  catch (IOException e) {
	    e.printStackTrace();
	    return null;
	  }
	  
  	  String[] lines = writer.getBuffer().toString().split("\n");
  	  for (int i = 0; i < lines.length; i++) {

    stringBuffer.append(TEXT_8);
    stringBuffer.append(lines[i].replaceAll("<","&lt;").replaceAll(">","&gt;"));
    
  	  }

    stringBuffer.append(TEXT_9);
    stringBuffer.append(name.toUpperCase());
    stringBuffer.append(TEXT_10);
    stringBuffer.append(name);
    stringBuffer.append(TEXT_11);
    stringBuffer.append(binding);
    stringBuffer.append(TEXT_12);
    
	}

    stringBuffer.append(TEXT_13);
    stringBuffer.append(prefix);
    stringBuffer.append(TEXT_14);
    stringBuffer.append(schema.toURI());
    stringBuffer.append(TEXT_15);
    
	for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
		AttributeType type = (AttributeType) itr.next();

    stringBuffer.append(TEXT_16);
    stringBuffer.append(schema.namespace().getURI());
    stringBuffer.append(TEXT_17);
    stringBuffer.append(type.getName());
    stringBuffer.append(TEXT_18);
    stringBuffer.append(type.getName().toUpperCase());
    stringBuffer.append(TEXT_19);
    
	}

    stringBuffer.append(TEXT_20);
    return stringBuffer.toString();
  }
}
