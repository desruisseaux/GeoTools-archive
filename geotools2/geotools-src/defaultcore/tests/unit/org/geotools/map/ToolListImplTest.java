/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.map;

import junit.framework.*;
import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.ToolFactory;
import org.geotools.map.ContextFactory;
import org.geotools.map.events.SelectedToolListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;


/**
 * ToolListImplTest.java JUnit based test
 *
 * @author Cameron Shorter
 * @version $Id: ToolListImplTest.java,v 1.1 2003/05/18 09:44:32 camerons Exp $
 */
public class ToolListImplTest extends TestCase implements SelectedToolListener {
    private ToolList globalToolList;
    private Tool globalSelectedTool;

    public ToolListImplTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ToolListImplTest.class);

        return suite;
    }

    /**
     * Called when the selectedTool on a MapPane changes.
     *
     * @param event The associated event.
     */
    public void selectedToolChanged(EventObject event) {
        globalSelectedTool = globalToolList.getSelectedTool();
    }

    ///////////////////////////////////////////////////////////////////////////
    // The tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * setSelectedTool with empty list, should return invalidArguement
     * exception.
     */
    public void testSelectedToolEmpty() {
        try {
            ContextFactory contextFactory = ContextFactory.createFactory();
            ToolList toolList = contextFactory.createToolList();
            ToolFactory toolFactory = ToolFactory.createFactory();
            Tool tool = toolFactory.createPanTool();
            toolList.setSelectedTool(tool);
            fail(
                "toolList.setSelectedTool(tool) should raise an InvalidException when the ToolList is empty");
        } catch (IllegalArgumentException e) {
            // Good, error was caught.
        }
    }

    /**
     * setSelectedTool with a tool that is not in the ToolList. Should return
     * invalidArguement exception
     */
    public void testSelectedToolInvalidTool() {
        try {
            ContextFactory contextFactory = ContextFactory.createFactory();
            ToolList toolList = contextFactory.createToolList();
            ToolFactory toolFactory = ToolFactory.createFactory();

            Tool tool = toolFactory.createZoomTool(0.5);
            toolList.add(tool);

            tool = toolFactory.createZoomTool(2);
            toolList.add(tool);

            tool = toolFactory.createPanTool();
            toolList.setSelectedTool(tool);
            fail(
                "toolList.setSelectedTool(tool) with invalid tool should raise an InvalidException when the ToolList is empty");
        } catch (IllegalArgumentException e) {
            // Good, error was caught.
        }
    }

    /**
     * setSelectedTool with a tool that is in the ToolList. Test
     * set/getSelectedTool. Test SendEvent gets sent.
     */
    public void testSelectedToolNormal() {
        ContextFactory contextFactory = ContextFactory.createFactory();
        ToolList toolList = contextFactory.createToolList();
        ToolFactory toolFactory = ToolFactory.createFactory();

        Tool tool = toolFactory.createZoomTool(0.5);
        toolList.add(tool);

        tool = toolFactory.createZoomTool(2);
        toolList.add(tool);

        tool = toolFactory.createPanTool();
        toolList.add(tool);

        this.globalSelectedTool = null;
        toolList.setSelectedTool(tool);

        assertTrue("Event not sent when selectedTool changes",
            tool == globalSelectedTool);

        Tool tool2;
        tool2 = toolList.getSelectedTool();
        assertTrue("Tool set in setSelectedTool not the same as get",
            tool == tool2);
    }

    /**
     * setSelectedTool with a tool that is in the ToolList. Test
     * set/getSelectedTool. Test SendEvent gets sent.
     */
    public void testRemoveSelectedTool() {
        ContextFactory contextFactory = ContextFactory.createFactory();
        ToolList toolList = contextFactory.createToolList();
        ToolFactory toolFactory = ToolFactory.createFactory();

        Tool tool = toolFactory.createZoomTool(0.5);
        toolList.add(tool);

        tool = toolFactory.createZoomTool(2);
        toolList.add(tool);

        tool = toolFactory.createPanTool();
        toolList.add(tool);

        toolList.setSelectedTool(tool);
        assertTrue("Event not sent when selectedTool changes",
            tool == globalSelectedTool);

        toolList.clear();
        assertTrue("SelectedTool not set to null when the List is emptied",
            globalSelectedTool == null);
    }
}
