package test.crs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.geowidgets.crs.widgets.swt.UnitComboBox;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.units.model.EPSG_UnitModel;
import org.geowidgets.units.model.IUnitModel;

/** Test_UnitCombo
 * 
 * @author Matthias Basler
 */
public class Test_SWTUnitCombo {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(300,300);
        shell.setLayout(new GridLayout(1, false));
        shell.setText("Unit dropdown test.");
        
        init(shell);
        shell.layout();
        shell.pack(true);
        
        shell.open();
        while(!shell.isDisposed()){
            if(!display.readAndDispatch())
            display.sleep();
        }
        display.dispose();
    }
        
    public static void init(Composite parent){   
        boolean sorted = true;
        
        Point size = new Point(100,20);

        final Label label = new Label(parent, SWT.NONE);
        label.setSize(size);
        //label.setText("Nothing selected yet.");
        
        final Label l1 = new Label(parent, SWT.NONE);
        l1.setSize(size);
        l1.setText("Units implementation via javax.units.Unit - linear units.");
        //l1.setLayoutData(0);
        final UnitComboBox ucb1 = new UnitComboBox(parent, IUnitModel.UNIT_LINEAR, sorted);
        ucb1.getCombo().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                label.setText("Symbol: " + ucb1.getSelectedUnit().toString());
                label.getParent().layout(true);
            }
        });
        
        final Label l2 = new Label(parent, SWT.NONE);
        l2.setSize(size);
        l2.setText("Units implementation via javax.units.Unit - angular units.");
        final UnitComboBox ucb2 = new UnitComboBox(parent, IUnitModel.UNIT_ANGULAR, sorted);
        ucb2.getCombo().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                label.setText("Symbol: " + ucb2.getSelectedUnit().toString());
                label.getParent().layout(true);
            }
        });

        try{
            GWFactoryFinder.setUnitModel(EPSG_UnitModel.getDefault());
            
            final Label l3 = new Label(parent, SWT.NONE);
            l3.setSize(size);
            l3.setText("Units implementation via EPSG - linear units.");
            final UnitComboBox ucb3 = new UnitComboBox(parent, IUnitModel.UNIT_LINEAR, sorted);
            ucb3.getCombo().addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent arg0) {
                    label.setText("Symbol: " + ucb3.getSelectedUnit().toString());
                    label.getParent().layout(true);
                }
            });
            
            final Label l4 = new Label(parent, SWT.NONE);
            l4.setSize(size);
            l4.setText("Units implementation via EPSG - angular units.");
            final UnitComboBox ucb4 = new UnitComboBox(parent, IUnitModel.UNIT_ANGULAR, sorted);
            ucb4.getCombo().addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent arg0) {
                    label.setText("Symbol: " + ucb4.getSelectedUnit().toString());
                    label.getParent().layout(true);
                }
            });
        } catch (Exception e){e.printStackTrace();}

    }
}
