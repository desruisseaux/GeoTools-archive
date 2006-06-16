package org.geotools.gui.swing;

import java.util.EventListener;



public interface HighlightChangeListener extends EventListener{
    public void highlightChanged(HighlightChangedEvent e);
}
