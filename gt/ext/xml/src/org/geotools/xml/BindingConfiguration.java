package org.geotools.xml;

import org.picocontainer.MutablePicoContainer;

/**
 * Resonsible for loading bindings into a container.
 * 
 * <p>
 * Strategy objects live inside a pico container. This provides the strategy 
 * with the following: 
 *  <ul>
 *  	<li>Life cycle
 *  	<li>Dependency management
 *  </ul>
 * </p>
 * <p>
 * 	<h3>Life Cycle</h3>
 * 
 * Strategy components that require notification of life cycle events must 
 * implement the {@link org.picocontainer.Startable}, and 
 * {@link org.picocontainer.Disposable} interfaces.  
 * 
 * </p>
 * 
 * <p>
 * 	<h3>Dependencies</h3>
 * 
 * Pico Container uses a design pattern known as <b>Inversion of Control (IoC)</b>.This 
 * pattern is very useful when the implementation of a particular dependency can 
 * vary. For a detailed description, see <a href=http://www.picocontainer.org/Inversion+of+Control>
 *  Inversion of Control</a> pattern.
 * </p>
 * 
 * <p>
 * To achieve IoC, Pico Container uses a mechanism known as <b>Contstructor 
 * Injection</b>. In this scheme, a particular component specifies all of its 
 * dependencies in its constructor. The container will ensure that the 
 * dependencies are satisfied when the component is instantiated. For an more 
 * detailed explanation, see <a href=http://www.martinfowler.com/articles/injection.html#ConstructorInjectionWithPicocontainer>
 * Constructor Injection</a>.
 * </p>
 * 
 * <p>
 * So how does the container know which implementation to supply to the 
 * component when it is instantiated? An implementation of the dependency must 
 * be registered with the container before the component is instantiated. This 
 * is usually done at parser configuration time. See {@link org.geotools.xml.Configuration
 * for more details}. Consider the following strategy.
 * 
 * <pre>
 * 	<code>
 * 	class MyStrategy implements SimpleStrategy {
 * 		List list;
 * 
 * 		public MyStrategy(List list) {
 * 			this.list = list;
 * 		}
 * 
 * 		public Object parse(InstanceComponent instance, Object value) 
 * 			throws Exception {
 * 	
 * 			list.add(value);
 * 			return list;		
 * 		}
 * 	}
 * 	</code>
 * </pre>
 * 
 * In the above example, our component depends on an object of type List. Since
 * List itself is abstract, at some point an actual concrete implementation of
 * List must be passed into the constructor of MyStrategy. Consider the 
 * following strategy configuration.
 * 
 * <pre>
 * 	<code>
 * 	class MyStrategyConfiguration implements StrategyConfiguration {
 * 		
 * 		public void configure(MutablePicoContainer container) {
 * 			//first register a concrete implemtnation of list
 * 			container.registerComponentImplementation(LinkedList.class);
 * 
 * 			//register the actual component
 * 			QName qName = new QName("http://geotools/org/", "my");
 * 			container.registerComponentImplementation(qName,MyStrategy.class);
 * 		}	
 * 
 * 	}
 * 	</code>
 * </pre>
 * 
 * With the above container configuration, the concrete type of List will be 
 * LinkedList.
 * </p>
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface BindingConfiguration {

	/**
	 * Configures the container which houses the strategy objects.
	 * 
	 * <p>
	 *  A strategy object is looked up in the container by its qualified name.
	 *  The following code snippet illustrates how to register a strategy with 
	 *  the container.
	 *  </p>
	 *  
	 *  <pre>
	 *  	<code>
	 *  void configure(MutablePicoContainer container) {
	 *  	QName qName = FOO.BARTYPE;
	 *  	Class impl = F00BarStrategy.class;
	 *  	container.registerComponentImplementation(qName,impl);
	 *  }
	 *  	</code>
	 *  </pre>
	 */
	void configure(MutablePicoContainer container);
}
