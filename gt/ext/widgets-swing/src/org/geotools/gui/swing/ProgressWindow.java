/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.gui.swing;

// J2SE dependencies
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JLayeredPane;
import javax.swing.JDesktopPane;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.BoundedRangeModel;
import java.lang.reflect.InvocationTargetException;

// Geotools dependencies
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * Reports progress of a lengthly operation in a window. This implementation can also format
 * warnings. Its method can be invoked from any thread (it doesn't need to be the <cite>Swing</cite>
 * thread), which make it easier to use it from some background thread. Such background thread
 * should have a low priority in order to avoid delaying Swing repaint events.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/ProgressWindow.png"></p>
 * <p>&nbsp;</p>
 *
 * @since 2.0
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ProgressWindow implements ProgressListener {
    /**
     * Largeur initiale de la fenêtre des progrès, en pixels.
     */
    private static final int WIDTH = 360;

    /**
     * Hauteur initiale de la fenêtre des progrès, en pixels.
     */
    private static final int HEIGHT = 120;

    /**
     * Hauteur de la zone de texte qui contiendra des messages d'avertissements.
     */
    private static final int WARNING_HEIGHT = 120;

    /**
     * Largeur de la marge horizontale, en pixels.
     */
    private static final int HMARGIN = 12;

    /**
     * Largeur de la marge verticale, en pixels.
     */
    private static final int VMARGIN = 9;

    /**
     * Nombre d'espaces à placer dans la marge de
     * la fenêtre contenant les messages d'erreurs.
     */
    private static final int WARNING_MARGIN = 8;

    /**
     * Fenêtre affichant les progrès de la longue opération.
     * Il peut s'agir notamment d'un objet {@link JDialog} ou
     * d'un objet {@link JInternalFrame}, dépendamment de la
     * composante parente.
     */
    private final Component window;

    /**
     * Conteneur dans lequel insérer les éléments tels que
     * la barre des progrès. Ca peut être le même objet que
     * {@link #window}, mais pas nécessairement.
     */
    private final JComponent content;

    /**
     * Barre des progrès. La plage de cette barre doit
     * obligatoirement aller au moins de 0 à 100.
     */
    private final JProgressBar progressBar;

    /**
     * Description de l'opération en cours. Des exemples de descriptions
     * seraient "Lecture de l'en-tête" ou "Lecture des données".
     */
    private final JLabel description;

    /**
     * Région dans laquelle afficher les messages d'avertissements.
     * Cet objet doit être de la classe {@link JTextArea}. il ne sera
     * toutefois construit que si des erreurs surviennent effectivement.
     */
    private JComponent warningArea;

    /**
     * Source du dernier message d'avertissement. Cette information est
     * conservée afin d'éviter de répéter la source lors d'éventuels
     * autres messages d'avertissements.
     */
    private String lastSource;
    
    /**
     * Creates a window for reporting progress. The window will not appears immediately.
     * It will appears only when the {@link #started} method will be invoked.
     *
     * @param parent The parent component, or {@code null} if none.
     */
    public ProgressWindow(final Component parent) {
        /*
         * Création de la fenêtre qui contiendra
         * les composantes affichant le progrès.
         */
        Dimension       parentSize;
        final Resources  resources = Resources.getResources(parent!=null ? parent.getLocale() : null);
        final String         title = resources.getString(ResourceKeys.PROGRESSION);
        final JDesktopPane desktop = JOptionPane.getDesktopPaneForComponent(parent);
        if (desktop != null) {
            final JInternalFrame frame = new JInternalFrame(title);
            window                     = frame;
            content                    = new JPanel();
            parentSize                 = desktop.getSize();
            frame.setContentPane(content); // Pour avoir un fond opaque
            frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
            desktop.add(frame, JLayeredPane.PALETTE_LAYER);
        } else {
            final Frame frame    = JOptionPane.getFrameForComponent(parent);
            final JDialog dialog = new JDialog(frame, title);
            window               = dialog;
            content              = (JComponent) dialog.getContentPane();
            parentSize           = frame.getSize();
            if (parentSize.width==0 || parentSize.height==0) {
                parentSize=Toolkit.getDefaultToolkit().getScreenSize();
            }
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setResizable(false);
        }
        window.setBounds((parentSize.width-WIDTH)/2, (parentSize.height-HEIGHT)/2, WIDTH, HEIGHT);
        /*
         * Création de l'étiquette qui décrira l'opération
         * en cours. Au départ, aucun texte ne sera placé
         * dans cette étiquette.
         */
        description = new JLabel();
        description.setHorizontalAlignment(JLabel.CENTER);
        /*
         * Procède à la création de la barre des progrès.
         * Le modèle de cette barre sera retenu pour être
         */
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(6,9,6,9),
                              progressBar.getBorder()));
        /*
         * Dispose les éléments à l'intérieur de la fenêtre.
         * On leur donnera une bordure vide pour laisser un
         * peu d'espace entre eux et les bords de la fenêtre.
         */
        content.setLayout(new GridLayout(2,1));
        content.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createEmptyBorder(VMARGIN,HMARGIN,VMARGIN,HMARGIN),
                          BorderFactory.createEtchedBorder()));
        content.add(description);
        content.add(progressBar);
    }

    /**
     * Returns a localized string for the specified key.
     */
    private String getString(final int key) {
        return Resources.getResources(window.getLocale()).getString(key);
    }

    /**
     * Returns the window title. The default title is "Progress" localized in current locale.
     */
    public String getTitle() {
        return get(Caller.TITLE);
    }

    /**
     * Set the window title. A {@code null} value reset the default title.
     */
    public void setTitle(final String name) {
        set(Caller.TITLE, (name!=null) ? name : getString(ResourceKeys.PROGRESSION));
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return get(Caller.LABEL);
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(final String description) {
        set(Caller.LABEL, description);
    }

    /**
     * Notifies that the operation begins. This method display the windows if it was
     * not already visible.
     */
    public void started() {
        call(Caller.STARTED);
    }

    /**
     * {@inheritDoc}
     */
    public void progress(final float percent) {
        int p=(int) percent; // round toward 0
        if (p<  0) p=  0;
        if (p>100) p=100;
        set(Caller.PROGRESS, p);
    }

    /**
     * Notifies that the operation has finished. The window will disaspears, except
     * if it contains warning or exception stack traces.
     */
    public void complete() {
        call(Caller.COMPLETE);
    }

    /**
     * Releases any resource holds by this window. Invoking this method destroy the window.
     */
    public void dispose() {
        call(Caller.DISPOSE);
    }

    /**
     * Display a warning message under the progres bar. The text area for warning messages
     * will appears only the first time this method is invoked.
     */
    public synchronized void warningOccurred(final String source, String margin,
                                             final String warning)
    {
        final StringBuffer buffer = new StringBuffer(warning.length()+16);
        if (source != lastSource) {
            lastSource = source;
            if (warningArea != null) {
                buffer.append('\n');
            }
            buffer.append(source!=null ? source : getString(ResourceKeys.UNTITLED));
            buffer.append('\n');
        }
        int wm = WARNING_MARGIN;
        if (margin != null) {
            margin = trim(margin);
            if (margin.length() != 0) {
                wm -= (margin.length()+3);
                buffer.append(Utilities.spaces(wm));
                buffer.append('(');
                buffer.append(margin);
                buffer.append(')');
                wm = 1;
            }
        }
        buffer.append(Utilities.spaces(wm));
        buffer.append(warning);
        if (buffer.charAt(buffer.length()-1) != '\n') {
            buffer.append('\n');
        }
        set(Caller.WARNING, buffer.toString());
    }

    /**
     * Display an exception stack trace.
     */
    public void exceptionOccurred(final Throwable exception) {
        ExceptionMonitor.show(window, exception);
    }

    /**
     * Retourne la chaîne {@code margin} sans les
     * éventuelles parenthèses qu'elle pourrait avoir
     * de part et d'autre.
     */
    private static String trim(String margin) {
        margin = margin.trim();
        int lower = 0;
        int upper = margin.length();
        while (lower<upper && margin.charAt(lower+0)=='(') lower++;
        while (lower<upper && margin.charAt(upper-1)==')') upper--;
        return margin.substring(lower, upper);
    }

    /**
     * Interroge une des composantes de la boîte des progrès.
     * L'interrogation sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information désirée. Ce code doit être une
     *         des constantes telles que {@link Caller#TITLE}
     *         ou {@link Caller#LABEL}.
     * @return L'information demandée.
     */
    private String get(final int task) {
        final Caller caller = new Caller(-task);
        SwingUtilities.invokeAndWait(caller);
        return caller.text;
    }

    /**
     * Modifie l'état d'une des composantes de la boîte des progrès.
     * La modification sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information à modifier. Ce code doit être une
     *         des constantes telles que {@link Caller#TITLE}
     *         ou {@link Caller#LABEL}.
     * @param  text Le nouveau texte.
     */
    private void set(final int task, final String text) {
        final Caller caller = new Caller(task);
        caller.text = text;
        EventQueue.invokeLater(caller);
    }

    /**
     * Modifie l'état d'une des composantes de la boîte des progrès.
     * La modification sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information à modifier. Ce code doit être une
     *         des constantes telles que {@link Caller#PROGRESS}.
     * @param  value Nouvelle valeur à affecter à la composante.
     */
    private void set(final int task, final int value) {
        final Caller caller = new Caller(task);
        caller.value = value;
        EventQueue.invokeLater(caller);
    }

    /**
     * Appelle une méthode <i>Swing</i> sans argument.
     * @param  task Méthode à appeler. Ce code doit être une
     *         des constantes telles que {@link Caller#STARTED}
     *         ou {@link Caller#DISPOSE}.
     */
    private void call(final int task) {
        EventQueue.invokeLater(new Caller(task));
    }

    /**
     * Tâche à exécuter dans le thread de <i>Swing</i> pour interroger
     * ou modifier l'état d'une composante. Cette tache est destinée à être appelée par
     * les méthodes {@link EventQueue#invokeLater} et {@link EventQueue#invokeAndWait}.
     * Les tâches possibles sont désignées par des constantes telles que {@link #TITLE}
     * et {@link #LABEL}. Une valeur positive signifie que l'on modifie l'état de cette
     * composante (dans ce cas, il faut d'abord avoir affecté une valeur à {@link #text}),
     * tandis qu'une valeur négative signifie que l'on interroge l'état de la comosante
     * (dans ce cas, il faudra extrait l'état du champ {@link #text}).
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private class Caller implements Runnable {
        /**
         * Constante indiquant que l'on souhaite interroger
         * ou modifier le titre de la boîte des progrès.
         */
        public static final int TITLE=1;

        /**
         * Constante indiquant que l'on souhaite interroger
         * ou modifier la description des progrès.
         */
        public static final int LABEL=2;

        /**
         * Constante indiquant que l'on souhaite modifier
         * la valeur de la barre des progrès.
         */
        public static final int PROGRESS=3;

        /**
         * Constante indiquant que l'on souhaite
         * faire apparaître un avertissement.
         */
        public static final int WARNING=4;

        /**
         * Constante indiquant que l'on souhaite
         * faire apparaître la boîte des progrès.
         */
        public static final int STARTED=5;

        /**
         * Constante indiquant que l'on souhaite
         * faire disparaître la boîte des progrès.
         */
        public static final int COMPLETE=6;

        /**
         * Constante indiquant que l'on souhaite
         * faire disparaître la boîte des progrès.
         */
        public static final int DISPOSE=7;

        /**
         * Constante indiquant la tâche que l'on souhaite effectuer. Il doit s'agir
         * d'une valeur telle que {@link #TITLE} et {@link #LABEL}, ainsi que leurs
         * valeurs négatives.
         */
        private final int task;

        /**
         * Valeur à affecter ou valeur retournée. Pour des valeurs positives de {@link #task},
         * il s'agit de la valeur à affecter à une composante. Pour des valeurs négatives de
         * {@link #task}, il s'agit de la valeur retournée par une composante.
         */
        public String text;

        /**
         * Valeur à affecter à la barre des progrès.
         */
        public int value;

        /**
         * Construit un objet qui effectura la tâche identifiée par la constante {@code task}.
         * Cette constantes doit être une valeur telle que {@link #TITLE} et {@link #LABEL}, ou une
         * de leurs valeurs négatives.
         */
        public Caller(final int task) {
            this.task = task;
        }

        /**
         * Exécute la tâche identifiée par la constante {@link #task}.
         */
        public void run() {
            final BoundedRangeModel model = progressBar.getModel();
            switch (task) {
                case   +LABEL: description.setText(text);  return;
                case   -LABEL: text=description.getText(); return;
                case PROGRESS: model.setValue(value); progressBar.setIndeterminate(false); return;
                case  STARTED: model.setRangeProperties(  0,1,0,100,false); window.setVisible(true); break;
                case COMPLETE: model.setRangeProperties(100,1,0,100,false); window.setVisible(warningArea!=null); break;
            }
            synchronized (ProgressWindow.this) {
                if (window instanceof JDialog) {
                    final JDialog window = (JDialog) ProgressWindow.this.window;
                    switch (task) {
                        case   +TITLE: window.setTitle(text);  return;
                        case   -TITLE: text=window.getTitle(); return;
                        case  STARTED: window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); return;
                        case COMPLETE: window.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);       return;
                        case  DISPOSE: window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                       if (warningArea==null || !window.isVisible()) window.dispose();
                                       return;
                    }
                } else {
                    final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                    switch (task) {
                        case   +TITLE: window.setTitle(text);     return;
                        case   -TITLE: text=window.getTitle();    return;
                        case  STARTED: window.setClosable(false); return;
                        case COMPLETE: window.setClosable(true);  return;
                        case  DISPOSE: window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
                                       if (warningArea==null || !window.isVisible()) window.dispose();
                                       return;
                    }
                }
                /*
                 * Si la tâche spécifiée n'est aucune des tâches énumérées ci-haut,
                 * on supposera que l'on voulait afficher un message d'avertissement.
                 */
                if (warningArea == null) {
                    final JTextArea     warningArea = new JTextArea();
                    final JScrollPane        scroll = new JScrollPane(warningArea);
                    final JPanel              panel = new JPanel(new BorderLayout());
                    final JPanel              title = new JPanel(new BorderLayout());
                    ProgressWindow.this.warningArea = warningArea;
                    warningArea.setFont(Font.getFont("Monospaced"));
                    warningArea.setEditable(false);
                    title.setBorder(BorderFactory.createEmptyBorder(0,HMARGIN,VMARGIN,HMARGIN));
                    panel.add(content,                                     BorderLayout.NORTH);
                    title.add(new JLabel(getString(ResourceKeys.WARNING)), BorderLayout.NORTH );
                    title.add(scroll,                                      BorderLayout.CENTER);
                    panel.add(title,                                       BorderLayout.CENTER);
                    if (window instanceof JDialog) {
                        final JDialog window = (JDialog) ProgressWindow.this.window;
                        window.setContentPane(panel);
                        window.setResizable(true);
                    } else {
                        final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                        window.setContentPane(panel);
                        window.setResizable(true);
                    }
                    window.setSize(WIDTH, HEIGHT+WARNING_HEIGHT);
                    window.setVisible(true); // Seems required in order to force relayout.
                }
                final JTextArea warningArea=(JTextArea) ProgressWindow.this.warningArea;
                warningArea.append(text);
            }
        }
    }
}
