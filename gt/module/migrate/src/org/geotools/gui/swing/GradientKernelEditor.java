/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le D�veloppement
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
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;

// JAI dependencies
import javax.media.jai.KernelJAI;
import javax.media.jai.operator.GradientMagnitudeDescriptor; // For Javadoc

// Seagis dependencies
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * A widget for editing the horizontal and vertical kernels for a
 * {@linkplain GradientMagnitudeDescriptor gradient magnitude} operation.
 * This widget combine two {@link KernelEditor} side-by-side: one for the
 * horizontal component and one for the vertical component.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/GradientKernelEditor.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id: GradientKernelEditor.java,v 1.4 2003/07/23 14:16:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see KernelEditor
 * @see GradientMagnitudeDescriptor
 * @see org.geotools.gp.GridCoverageProcessor
 */
public class GradientKernelEditor extends JComponent {
    /**
     * Square root of 2.
     */
    private static final float SQRT2 = 1.4142135623730950488016887242097f;

    /**
     * Horizontal gradient mask according Prewitt (also know as smoothed).
     */
    public static final KernelJAI PREWITT_HORIZONTAL = new KernelJAI(3,3,new float[] {
        -1,  0,  1,
        -1,  0,  1,
        -1,  0,  1,
    });

    /**
     * Vertical gradient mask according Prewitt (also know as smoothed).
     */
    public static final KernelJAI PREWITT_VERTICAL = new KernelJAI(3,3,new float[] {
        -1, -1, -1,
         0,  0,  0,
         1,  1,  1,
    });

    /**
     * Horizontal gradient mask (isotropic).
     */
    public static final KernelJAI ISOTROPIC_HORIZONTAL = new KernelJAI(3,3,new float[] {
        -1,      0,      1,
        -SQRT2,  0,  SQRT2,
        -1,      0,      1,
    });

    /**
     * Vertical gradient mask (isotropic).
     */
    public static final KernelJAI ISOTROPIC_VERTICAL = new KernelJAI(3,3,new float[] {
        -1, -SQRT2, -1,
         0,      0,  0,
         1,  SQRT2,  1,
    });

    /**
     * Horizontal gradient mask according Sobel.
     */
    public static final KernelJAI SOBEL_HORIZONTAL = KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL;

    /**
     * Vertical gradient mask according Sobel.
     */
    public static final KernelJAI SOBEL_VERTICAL = KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL;
    /*
     * NOTE: Sobel masks were interchanged in KernelJAI prior and up to JAI 1.1.2-rc.
     *       See for example J.J. Simpson (1990) in Remote sensing environment, 33:17-33.
     *       This bug was fixed in JAI 1.1.2 final.
     */

    /**
     * Horizontal gradient mask according Kirsch.
     */
    public static final KernelJAI KIRSCH_HORIZONTAL = new KernelJAI(3,3,new float[] {
        -3, -3,  5,
        -3,  0,  5,
        -3, -3,  5,
    });

    /**
     * Vertical gradient mask according Kirsch.
     *
     * @task REVISIT: Why positives numbers are on the first row? This is the opposite
     *       of other vertical gradient masks. Need to verify in J.J. Simpson (1990).
     */
    public static final KernelJAI KIRSCH_VERTICAL = new KernelJAI(3,3,new float[] {
         5,  5,  5,
        -3,  0, -3,
        -3, -3, -3,
    });

    /**
     * The horizontal kernel editor.
     */
    private final KernelEditor kernelH = new Editor(true);

    /**
     * The vertical kernel editor.
     */
    private final KernelEditor kernelV = new Editor(false);

    /**
     * Construct a new editor for gradient kernels.
     */
    public GradientKernelEditor() {
        setLayout(new GridBagLayout());
        final Resources resources = Resources.getResources(getDefaultLocale());

        final Border border = BorderFactory.createCompoundBorder(
                              BorderFactory.createLoweredBevelBorder(),
                              BorderFactory.createEmptyBorder(3,3,3,3));
        kernelH.setBorder(border);
        kernelV.setBorder(border);
        final JLabel labelH, labelV;
        labelH = new JLabel(resources.getString(ResourceKeys.HORIZONTAL_COMPONENT), JLabel.CENTER);
        labelV = new JLabel(resources.getString(ResourceKeys.VERTICAL_COMPONENT),   JLabel.CENTER);
        final GridBagConstraints c = new GridBagConstraints();
        
        c.insets.top = 6;
        c.gridy=0; c.weightx=1; c.gridwidth=1; c.gridheight=1; c.fill=c.BOTH;
        c.gridx=0; add(labelH, c);
        c.gridx=1; add(labelV, c);
        c.gridy=1; c.weighty=1; c.insets.bottom = 6;
        c.gridx=0; c.insets.left=6; c.insets.right=3; add(kernelH, c);
        c.gridx=1; c.insets.left=3; c.insets.right=6; add(kernelV, c);
    }

    /**
     * Add a set of predefined kernels. This convenience method invokes {@link
     * KernelEditor#addDefaultKernels()} on both {@linkplain #getHorizontalEditor
     * horizontal} and {@linkplain #getVerticalEditor vertical} kernel editors.
     * The default implementation for those editors will add a set of Sobel kernels.
     */
    public void addDefaultKernels() {
        kernelH.addDefaultKernels();
        kernelV.addDefaultKernels();
    }

    /**
     * Returns the horizontal kernel editor.
     */
    public KernelEditor getHorizontalEditor() {
        return kernelH;
    }

    /**
     * Returns the vertical kernel editor.
     */
    public KernelEditor getVerticalEditor() {
        return kernelV;
    }

    /**
     * A kernel editor for horizontal or vertical gradient kernel.
     *
     * @version $Id: GradientKernelEditor.java,v 1.4 2003/07/23 14:16:16 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class Editor extends KernelEditor {
        /**
         * <code>true</code> if this editor is for the horizontal component,
         * or <code>false</code> for the vertical component.
         */
        private final boolean horizontal;

        /**
         * Construct a new kernel editor for the specified component.
         */
        public Editor(final boolean horizontal) {
            super();
            this.horizontal = horizontal;
        }

        /**
         * Add a set of predefined kernels.
         */
        public void addDefaultKernels() {
            final String GRADIENT_MASKS = getResources().getString(ResourceKeys.GRADIENT_MASKS);
            final KernelJAI prewitt, isotropic, kirsch, sobel;
            if (horizontal) {
                prewitt   = PREWITT_HORIZONTAL;
                isotropic = ISOTROPIC_HORIZONTAL;
                kirsch    = KIRSCH_HORIZONTAL;
                sobel     = SOBEL_HORIZONTAL;
            } else {
                prewitt   = PREWITT_VERTICAL;
                isotropic = ISOTROPIC_VERTICAL;
                kirsch    = KIRSCH_VERTICAL;
                sobel     = SOBEL_VERTICAL;
            }
            addKernel(GRADIENT_MASKS, "Prewitt",        prewitt);
            addKernel(GRADIENT_MASKS, "Isotropic",      isotropic);
            addKernel(GRADIENT_MASKS, "Kirsch",         kirsch);
            addKernel(GRADIENT_MASKS, "Sobel 3\u00D73", sobel);
            final StringBuffer buffer = new StringBuffer("Sobel ");
            final int base = buffer.length();
            for (int i=5; i<=15; i+=2) {
                buffer.setLength(base);
                buffer.append(i).append('\u00D7').append(i);
                addKernel(GRADIENT_MASKS, buffer.toString(), getSobel(i, horizontal));
            }
            setKernel(sobel);
        }

        /**
         * Retourne une extension de l'op�rateur de Sobel. Pour chaque �l�ment dont la position
         * par rapport � l'�l�ment central est (x,y), on calcule la composante horizontale avec
         * le cosinus de l'angle divis� par la distance. On peut l'�crire comme suit:
         *
         * <blockquote><pre>
         *     cos(atan(y/x)) / sqrt(x�+y�)
         * </pre></blockquote>
         *
         * En utilisant l'identit� 1/cos� = (1+tan�), on peut r��crire l'�quation comme suit:
         *
         * <blockquote><pre>
         *     x / (x�+y�)
         * </pre></blockquote>
         *
         * @param size Taille de la matrice. Doit �tre un nombre positif et impair.
         * @param horizontal <code>true</code> pour l'op�rateur horizontal,
         *        or <code>false</code> pour l'op�rateur vertical.
         */
        private static KernelJAI getSobel(final int size, final boolean horizontal) {
            final int key = size/2;
            final float[] data = new float[size*size];
            for (int y=key; y>=0; y--) {
                int row1 = (key-y)*size + key;
                int row2 = (key+y)*size + key;
                final int y2 = y*y;
                for (int x=key; x!=0; x--) {
                    final int x2 = x*x;
                    final float v = (float) (2.0*x / (x2+y2));
                    if (horizontal) {
                        data[row1-x] = data[row2-x] = -v;
                        data[row1+x] = data[row2+x] = +v;
                    } else {
                        // Swap x and y.
                        row1 = (key-x)*size + key;
                        row2 = (key+x)*size + key;
                        data[row1-y] = data[row1+y] = -v;
                        data[row2-y] = data[row2+y] = +v;
                    }
                }
            }
            return new KernelJAI(size, size, key, key, data);
        }
    }
}
