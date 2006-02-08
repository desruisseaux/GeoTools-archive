/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.display.canvas;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;


/**
 * Tests {@link ReferencedCanvas} and {@link ReferencedGraphic}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReferencedObjectTest extends TestCase implements PropertyChangeListener {
    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the suite of tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ReferencedObjectTest.class);
        return suite;
    }

    /**
     * The last property change event fired by the canvas.
     */
    private PropertyChangeEvent event;

    /**
     * Constructs the test case.
     */
    public ReferencedObjectTest(final String name) {
        super(name);
    }

    /**
     * Invoked when a canvas property changed.
     */
    public void propertyChange(final PropertyChangeEvent event) {
        this.event = event;
    }

    /**
     * Returns the specified graphics as a list.
     */
    private static List asList(DummyGraphic g1, DummyGraphic g2, DummyGraphic g3) {
        return Arrays.asList(new DummyGraphic[] {g1, g2, g3});
    }

    /**
     * Tests basic graphic operations.
     */
    public void testGraphic() {
        final DummyGraphic graphic = new DummyGraphic();
        graphic.addPropertyChangeListener(this);
        /*
         * Tests the Z order hint.
         */
        graphic.setZOrderHint(12);
        assertEquals(12, graphic.getZOrderHint(), 0);
        assertEquals(DisplayObject.Z_ORDER_HINT_PROPERTY, event.getPropertyName());
        assertEquals(new Double(12), event.getNewValue());
        /*
         * Tests the name property.
         */
        graphic.setName("Dummy");
        assertEquals("Dummy", graphic.getName());
        assertEquals(DisplayObject.NAME_PROPERTY, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("Dummy", event.getNewValue());
        /*
         * Tests the visible property.
         */
        assertTrue(graphic.getVisible());
        graphic.setVisible(false);
        assertEquals(DisplayObject.VISIBLE_PROPERTY, event.getPropertyName());
        assertEquals(Boolean.TRUE,  event.getOldValue());
        assertEquals(Boolean.FALSE, event.getNewValue());
        /*
         * Tests disposal.
         */
        assertEquals(1, graphic.listeners.getPropertyChangeListeners().length);
        graphic.dispose();
        assertEquals(0, graphic.listeners.getPropertyChangeListeners().length);
    }

    /**
     * Tests basic canvas operations.
     */
    public void testCanvas() {
        final DummyCanvas canvas = new DummyCanvas();
        canvas.addPropertyChangeListener(this);
        /*
         * Tests rendering hints.
         */
        assertNull(canvas.getImplHint("KEY_RENDERING"));
        canvas.setImplHint("KEY_RENDERING", RenderingHints.VALUE_RENDER_QUALITY);
        assertSame(RenderingHints.VALUE_RENDER_QUALITY, canvas.getImplHint("KEY_RENDERING"));
        assertSame(RenderingHints.VALUE_RENDER_QUALITY, canvas.getImplHint("key_rendering"));
        assertSame(RenderingHints.VALUE_RENDER_QUALITY, canvas.getImplHint("keyRendering" ));
        assertSame(RenderingHints.VALUE_RENDER_QUALITY, canvas.getImplHint("KeyRendering" ));
        /*
         * Tests the title property.
         */
        assertNull(canvas.getTitle());
        canvas.setTitle("Dummy");
        assertEquals("Dummy", canvas.getTitle());
        assertEquals(DisplayObject.TITLE_PROPERTY, event.getPropertyName());
        assertNull(event.getOldValue());
        assertEquals("Dummy", event.getNewValue());
        /*
         * Tests the addition of a first graphic.
         */
        assertEquals(DefaultEngineeringCRS.GENERIC_2D, canvas.getObjectiveCRS());
        assertTrue(((GeneralEnvelope) canvas.getEnvelope()).isNull());
        assertTrue(canvas.getGraphics().isEmpty());
        final DummyGraphic graphic1 = new DummyGraphic();
        assertEquals(DefaultEngineeringCRS.CARTESIAN_2D, graphic1.getObjectiveCRS());
        assertTrue(((GeneralEnvelope) graphic1.getEnvelope()).isInfinite());
        assertNull(graphic1.getCanvas());
        assertSame(graphic1, canvas.add(graphic1));
        assertSame(canvas, graphic1.getCanvas());
        assertEquals(DefaultEngineeringCRS.CARTESIAN_2D, canvas.getObjectiveCRS());
        assertTrue(((GeneralEnvelope) canvas.getEnvelope()).isInfinite());
        /*
         * Tests the addition of a second graphic.
         */
        final DummyGraphic graphic2 = new DummyGraphic();
        assertSame(graphic2, canvas.add(graphic2));
        assertEquals(DisplayObject.GRAPHICS_PROPERTY, event.getPropertyName());
        assertEquals(1, ((List) event.getOldValue()).size());
        assertEquals(2, ((List) event.getNewValue()).size());
        assertSame(canvas.getGraphics(), event.getNewValue());
        /*
         * Tests the addition of a third graphic.
         */
        final DummyGraphic graphic3 = new DummyGraphic();
        assertSame(graphic3, canvas.add(graphic3));
        assertEquals(asList(graphic1, graphic2, graphic3), canvas.getGraphics());
        graphic3.setZOrderHint(1);
        assertEquals(asList(graphic3, graphic1, graphic2), canvas.getGraphics());
        graphic2.setZOrderHint(2);
        assertEquals(asList(graphic3, graphic2, graphic1), canvas.getGraphics());
        graphic2.setZOrderHint(1);
        assertEquals(asList(graphic2, graphic3, graphic1), canvas.getGraphics());
        graphic1.setZOrderHint(1);
        assertEquals(asList(graphic1, graphic2, graphic3), canvas.getGraphics());
        assertSame(graphic2, canvas.add(graphic2));
        assertEquals(asList(graphic1, graphic2, graphic3), canvas.getGraphics());
        /*
         * Tests envelope changes.
         */
        // TODO
        /*
         * Tests disposal.
         */
        assertEquals(1, canvas.listeners.getPropertyChangeListeners().length);
        canvas.dispose();
        assertEquals(0, canvas.listeners.getPropertyChangeListeners().length);
    }
}
