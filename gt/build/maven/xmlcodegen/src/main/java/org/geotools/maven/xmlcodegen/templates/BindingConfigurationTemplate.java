package org.geotools.maven.xmlcodegen.templates;

import java.util.*;
import org.eclipse.xsd.*;
import org.geotools.xml.*;

public class BindingConfigurationTemplate
{
  protected static String nl;
  public static synchronized BindingConfigurationTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    BindingConfigurationTemplate result = new BindingConfigurationTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = NL + "import org.geotools.xml.BindingConfiguration;" + NL + "import org.picocontainer.MutablePicoContainer;" + NL + "" + NL + "/**" + NL + " * Binding configuration for the ";
  protected final String TEXT_2 = " schema." + NL + " *" + NL + " * @generated" + NL + " */" + NL + "public final class ";
  protected final String TEXT_3 = "BindingConfiguration" + NL + "\timplements BindingConfiguration {" + NL + "" + NL + "" + NL + "\t/**" + NL + "\t * @generated modifiable" + NL + "\t */" + NL + "\tpublic void configure(MutablePicoContainer container) {" + NL + "\t";
  protected final String TEXT_4 = NL + "\t\t//Types";
  protected final String TEXT_5 = NL + "\t\tcontainer.registerComponentImplementation(";
  protected final String TEXT_6 = ",";
  protected final String TEXT_7 = ");";
  protected final String TEXT_8 = NL;
  protected final String TEXT_9 = NL + "\t\t//Elements";
  protected final String TEXT_10 = NL + "\t\tcontainer.registerComponentImplementation(";
  protected final String TEXT_11 = ",";
  protected final String TEXT_12 = ");";
  protected final String TEXT_13 = NL;
  protected final String TEXT_14 = NL + "\t\t//Attributes";
  protected final String TEXT_15 = NL + "\t\tcontainer.registerComponentImplementation(";
  protected final String TEXT_16 = ",";
  protected final String TEXT_17 = ");";
  protected final String TEXT_18 = NL + "\t}" + NL + "" + NL + "}";

  public String generate(Object argument)
  {
    StringBuffer stringBuffer = new StringBuffer();
     	
	Object[] arguments = (Object[])argument;
	XSDSchema schema = (XSDSchema)arguments[0];
	List components = (List)arguments[1];
	String ns = schema.getTargetNamespace();
	String prefix = Schemas.getTargetPrefix( schema );
	
	List types = new ArrayList();
	List elements = new ArrayList();
	List attributes = new ArrayList();
	
	for (Iterator itr = components.iterator(); itr.hasNext();) {
		XSDNamedComponent component = (XSDNamedComponent)itr.next();
		if (component instanceof XSDTypeDefinition) {
			types.add(component);
		}
		else if (component instanceof XSDElementDeclaration) {
			elements.add(component);
		}
		else if (component instanceof XSDAttributeDeclaration) {
			attributes.add(component);
		}
	}

    stringBuffer.append(TEXT_1);
    stringBuffer.append(schema.getTargetNamespace());
    stringBuffer.append(TEXT_2);
    stringBuffer.append(prefix.toUpperCase());
    stringBuffer.append(TEXT_3);
    
	if (!types.isEmpty()) {

    stringBuffer.append(TEXT_4);
    
		for (Iterator itr = types.iterator(); itr.hasNext();) {
				XSDTypeDefinition type = (XSDTypeDefinition)itr.next();
				if (type.getName() == null) continue;
				
				String typeQName = prefix.toUpperCase()+"."+type.getName();
				String binding = type.getName().substring(0,1).toUpperCase() + 
					type.getName().substring(1) + "Binding.class";

    stringBuffer.append(TEXT_5);
    stringBuffer.append(typeQName);
    stringBuffer.append(TEXT_6);
    stringBuffer.append(binding);
    stringBuffer.append(TEXT_7);
    
		}
	}

    stringBuffer.append(TEXT_8);
    
	if (!elements.isEmpty()) {

    stringBuffer.append(TEXT_9);
    
		for (Iterator itr = elements.iterator(); itr.hasNext();) {
				XSDNamedComponent named = (XSDNamedComponent)itr.next();
				if (named.getName() == null) continue;
				
				String nQName = prefix.toUpperCase()+"."+named.getName();
				String binding = named.getName().substring(0,1).toUpperCase() + 
					named.getName().substring(1) + "Binding.class";

    stringBuffer.append(TEXT_10);
    stringBuffer.append(nQName);
    stringBuffer.append(TEXT_11);
    stringBuffer.append(binding);
    stringBuffer.append(TEXT_12);
    
		}
	}

    stringBuffer.append(TEXT_13);
    
	if (!attributes.isEmpty()) {

    stringBuffer.append(TEXT_14);
    
		for (Iterator itr = attributes.iterator(); itr.hasNext();) {
				XSDNamedComponent named = (XSDNamedComponent)itr.next();
				if (named.getName() == null) continue;
				
				String nQName = prefix.toUpperCase()+"."+named.getName();
				String binding = named.getName().substring(0,1).toUpperCase() + 
					named.getName().substring(1) + "Binding.class";

    stringBuffer.append(TEXT_15);
    stringBuffer.append(nQName);
    stringBuffer.append(TEXT_16);
    stringBuffer.append(binding);
    stringBuffer.append(TEXT_17);
    
		}
	}

    stringBuffer.append(TEXT_18);
    return stringBuffer.toString();
  }
}
