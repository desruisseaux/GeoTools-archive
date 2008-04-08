package org.geotools.gui.swing.process;

import java.util.Map;

import javax.swing.JPanel;

/**
 * This is a descriptor; identifying a page by id (and lazily creating it as needed).
 * 
 * @author Jody
 */
public abstract class JProcessPage {
    /**
     * Used to indicate which page we should start with.
     */
    private static final String DEFAULT = "default";

    /**
     * Used to indicate that we are done and the wizard should close
     */
    public static final String FINISH = "finish";

    private JPanel page;
    private String identifier;
    /**
     * Wizard hosting this process page; we will access wizard.model directly to look up our friends
     * for next and previous.
     */
    private JProcessWizard wizard;
    /**
     * Create a default page.
     */
    public JProcessPage() {
        identifier = DEFAULT;
        page = new JPanel();
    }

    /**
     * Create a page.
     * 
     * @param id identifier
     * @param panel JPanel to use as wizard page
     */
    public JProcessPage( String id, JPanel page ) {
        identifier = id;
        this.page = page;
    }

    public final JPanel getPage() {
        return page;
    }

    public final void setPage( JPanel page ) {
        this.page = page;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final void setIdentifier( String id ) {
        identifier = id;
    }

    final void setJProcessWizard( JProcessWizard w ) {
        wizard = w;
    }

    public final JProcessWizard getJProcessWizard() {
        return wizard;
    }
    public Map<String, JProcessPage> getModel() {
        return wizard.model;
    }

    /**
     * Identifier of the panel to use Next.
     * 
     * @return Return id of the next JProcessPage or null if next should be disabled. You can use
     *         FINISH to indicate the wizard is complete and may be closed.
     */
    public abstract String getNextPageIdentifier();

    /**
     * Identifier of the panel to use Back.
     * 
     * @return Return id of the next JProcessPage or null if next should be disabled.
     */
    public abstract String getBackPageIdentifier();

    /**
     * Called just before the panel is to be displayed.
     */
    public void aboutToDisplayPanel() {
    }

    /**
     * Override this method to perform functionality when the panel itself is displayed.
     */
    public void displayingPanel() {

    }

    /**
     * Override this method to perform functionality just before the panel is to be hidden.
     */
    public void aboutToHidePanel() {

    }

}
