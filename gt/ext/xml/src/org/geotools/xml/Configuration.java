/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml;

import org.picocontainer.MutablePicoContainer;


/**
 * Responsible for configuring a parser runtime environment.
 *
 * <p>
 * Implementations have the following responsibilites:
 *
 * <ul>
 *         <li>Configuration of bindinds.
 *  <li>Configuration of context used by bindings.
 *  <li>Supplying specialied handlers for looking up schemas.
 * </ul>
 * </p>
 *
 * <h3>Binding Configuration</h3>
 * <p>
 *  In able for a particular binding to be found during a parse, the
 *  configuration must first populate a container with said binding. A binding
 *  is stored in as a key value pait. The key is the qualified name of the type '
 *  being bound to. The value is the class of the binding. For instance, the
 *  following configures a container with a binding for type <b>xs:string</b>
 *
 *  <pre>
 *          <code>
 *  void configureBindings(MutablePicoContainer container) {
 *           container.registerComponentImplementation(XS.STRING,XSStringBinding.class);
 *  }
 *          </code>
 *  </pre>
 *
 *  Usually it is desirable to populate a container with a set of bindings from
 *  a specific schema. For instance, the following configuration populates the
 *  binding container with all the bindings for types defined in XML schema
 *  itself.
 *
 *  <pre>
 *          <code>
 *  void configureBindings(MutablePicoContainer container) {
 *           new XSBindingConfiguration().configure(container);
 *  }
 *          </code>
 *  </pre>
 *
 *  Instances of type {@link org.geotools.xml.BindingConfiguration} are used to
 *  populate a container with all the bindings from a particular schema.
 * </p>
 *
 * <h3>Context Configuration</h3>
 * <p>
 * Many bindings have dependencies on other types of objects. The pattern used
 * to satisfy these dependencies is known as <b>Constructor Injection</b>. Which
 * means that any dependencies a binding has is passed to it in its constructor.
 * For instance, the following binding has a dependency on java.util.List.
 *
 * <pre>
 *         <code>
 * class MyBinding implements SimpleBinding {
 *
 *                List list;
 *
 *                 public MyBinding(List list) {
 *                         this.list = list;
 *                 }
 * }
 *         </code>
 * </pre>
 *
 * Before a binding can be created, the container in which it is housed in must
 * be able to satisfy all of its dependencies. It is the responsibility of the
 * configuration to statisfy this criteria. This is known as configuring the
 * binding context. The following is a suitable configuration for the above
 * binding.
 *
 * <pre>
 *         <code>
 * class MyConfiguration implements Configuration {
 *
 *                void configureContext(MutablePicoContainer container) {
 *                        container.registerComponentImplementation(ArrayList.class);
 *                }
 * }
 *         </code>
 * </pre>
 *
 * <p>
 *  <h3>Schema Resolution</h3>
 * XML instance documents often contain schema uri references that are invalid.
 * A configuration has the ability to supply a specialized schema resolvers and
 * locators. This prevents the parser from following an invalid schema uri and
 * prevents an errors that may occur as a result.
 * </p>
 *
 * <p>
 * An instance of {@link org.eclipse.xsd.util.XSDSchemaLocationResolver} can be
 * used to override a schemaLocation referencing another schema. An instance of
 * {@link org.eclipse.xsd.util.XSDSchemaLocator} can be used override the
 * actual schema encountered from a particular schemaLocation.
 *
 * In order to supply a custom schema resolver or locator, an implementation can
 * be inserted into the context at configuration time.
 * <pre>
 *         <code>
 * class MyConfiguration implements Configuration {
 *
 *                void configureContext(MutablePicoContainer container) {
 *                        container.registerComponentImplementation(MySchemaLocator.class);
 *                }
 * }
 *         </code>
 * </pre>
 *
 * </p>
 * <p>
 * The XSDSchemaLocator and XSDSchemeLocationResolver implementations are used
 * in a couple of scenarios. The first is when the <b>schemaLocation</b>
 * attribute of the root element of the instance document is being parsed.
 * The schemaLocation attribute has the form:
 *
 * <pre>
 * <code>
 *         schemaLocation="namespace location namespace location ..."
 * </code>
 * </pre>
 *
 * In which (namespace,location) tuples are listed. For each each namespace
 * encountered when parsing the schemaLocation attribute, an appropriate
 * resolver / locator is looked up. If an override is not aviable, the framework
 * attempts to resolve the location part of the tuple into a schema.
 *
 * The second scenario occurs when the parsing of a schema encounters an
 * <b>import</b> or an <b>include<b> element. These elements have the form:
 *
 *  <pre>
 *  <code>
 *      &lt;import namespace="" schemaLocation=""/&gt;
 *        </code>
 *  </pre>
 *
 *  and:
 *
 *  <pre>
 *  <code>
 *      &lt;include schemaLocation=""&gt;
 *  </code>
 *        </pre>
 *
 *        respectivley. Similar to above, the schemaLocation (and namespace in the
 *        case of an import) are used to find an override. If not found they are
 *        resolved directly.
 * </p>
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 * @see org.geotools.xml.BindingConfiguration
 */
public interface Configuration {
    /**
     * Configures a container which houses all the bindings used during a parse.
     *
     * @param container The container housing the binding objects.
     */
    void configureBindings(MutablePicoContainer container);

    /**
     * Configures the root context to be used when parsing elements.
     *
     * @param container The container representing the context.
     */
    void configureContext(MutablePicoContainer container);

    /**
     * Returns an array of objects used to lookup schemas.
     *
     * <p>This method allows a particular configuration to specialize a schema
     * lookup for a particular namespace. This method can simply return null if
     * no specialization is needed.<p>
     *
     * <p>
     * The XSDSchemaLocator interface contains a single method:
     *
     * <pre>
     * <code>
     * XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI,  String rawSchemaLocationURI, String resolvedSchemaLocationURI);
     * </code>
     * </pre>
     *
     * Implementations must <B>never</B> assume that any of the paramters passed
     * into the locateSchema method are non-null.
     * </p>
     *
     * @return An array of locators, or null if none are need.
     */

    //XSDSchemaLocator[] getSchemaLocators();
}
