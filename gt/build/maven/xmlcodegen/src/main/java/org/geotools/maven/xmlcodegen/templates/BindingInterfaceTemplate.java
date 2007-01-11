package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import org.eclipse.xsd.*;
import org.geotools.xml.*;
import org.geotools.maven.xmlcodegen.*;

public class BindingInterfaceTemplate
{
  protected static String nl;
  public static synchronized BindingInterfaceTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    BindingInterfaceTemplate result = new BindingInterfaceTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = NL + "import javax.xml.namespace.QName;" + NL + "" + NL + "/**" + NL + " * This interface contains the qualified names of all the types,elements, and " + NL + " * attributes in the ";
  protected final String TEXT_2 = " schema." + NL + " *" + NL + " * @generated" + NL + " */" + NL + "public interface ";
  protected final String TEXT_3 = " {" + NL + "" + NL + "\t/** @generated */" + NL + "\tpublic static final String NAMESPACE = \"";
  protected final String TEXT_4 = "\";" + NL + "\t" + NL + "\t/* Type Definitions */";
  protected final String TEXT_5 = NL + "\t/** @generated */" + NL + "\tpublic static final QName ";
  protected final String TEXT_6 = " = " + NL + "\t\tnew QName(\"";
  protected final String TEXT_7 = "\",\"";
  protected final String TEXT_8 = "\");";
  protected final String TEXT_9 = NL + NL + "\t/* Elements */";
  protected final String TEXT_10 = NL + "\t/** @generated */" + NL + "\tpublic static final QName ";
  protected final String TEXT_11 = " = " + NL + "\t\tnew QName(\"";
  protected final String TEXT_12 = "\",\"";
  protected final String TEXT_13 = "\");";
  protected final String TEXT_14 = NL + NL + "\t/* Attributes */" + NL + "\t";
  protected final String TEXT_15 = NL + "\t/** @generated */" + NL + "\tpublic static final QName ";
  protected final String TEXT_16 = " = " + NL + "\t\tnew QName(\"";
  protected final String TEXT_17 = "\",\"";
  protected final String TEXT_18 = "\");";
  protected final String TEXT_19 = NL + NL + "}" + NL + "\t";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     	
	XSDSchema schema = (XSDSchema)argument ;
	String ns = schema.getTargetNamespace();
	String prefix = Schemas.getTargetPrefix( schema );
	

    stringBuffer.append(TEXT_1);
    stringBuffer.append(schema.getTargetNamespace());
    stringBuffer.append(TEXT_2);
    stringBuffer.append(prefix.toUpperCase());
    stringBuffer.append(TEXT_3);
    stringBuffer.append( ns );
    stringBuffer.append(TEXT_4);
    
	List types = GeneratorUtils.allTypes( schema );
	for (Iterator itr = types.iterator(); itr.hasNext();) {
		XSDTypeDefinition type = (XSDTypeDefinition)itr.next();
		if (type.getName() == null) continue;
		if (!ns.equals(type.getTargetNamespace())) continue;
		

    stringBuffer.append(TEXT_5);
    stringBuffer.append(type.getName());
    stringBuffer.append(TEXT_6);
    stringBuffer.append(ns);
    stringBuffer.append(TEXT_7);
    stringBuffer.append(type.getName());
    stringBuffer.append(TEXT_8);
    
	}

    stringBuffer.append(TEXT_9);
    
	List elements = schema.getElementDeclarations();
	for (Iterator itr = elements.iterator(); itr.hasNext();) {
		XSDElementDeclaration element = (XSDElementDeclaration)itr.next();
		if (element.getName() == null) continue;
		if (!ns.equals(element.getTargetNamespace())) continue;

    stringBuffer.append(TEXT_10);
    stringBuffer.append(element.getName());
    stringBuffer.append(TEXT_11);
    stringBuffer.append(ns);
    stringBuffer.append(TEXT_12);
    stringBuffer.append(element.getName());
    stringBuffer.append(TEXT_13);
    
	}

    stringBuffer.append(TEXT_14);
    
	List attributes = schema.getAttributeDeclarations();
	for (Iterator itr = attributes.iterator(); itr.hasNext();) {
		XSDAttributeDeclaration attribute = (XSDAttributeDeclaration)itr.next();
		if (attribute.getName() == null) continue;
		if (!ns.equals(attribute.getTargetNamespace())) continue;

    stringBuffer.append(TEXT_15);
    stringBuffer.append(attribute.getName());
    stringBuffer.append(TEXT_16);
    stringBuffer.append(ns);
    stringBuffer.append(TEXT_17);
    stringBuffer.append(attribute.getName());
    stringBuffer.append(TEXT_18);
    
	}

    stringBuffer.append(TEXT_19);
    return stringBuffer.toString();
  }
}
