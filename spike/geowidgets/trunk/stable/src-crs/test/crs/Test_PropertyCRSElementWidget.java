package test.crs;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.*;
import org.geowidgets.crs.model.ICRSModel;
import org.geowidgets.crs.widgets.propertysheet.CRSElementPropertyViewer;
import org.geowidgets.framework.GWFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.datum.*;

public class Test_PropertyCRSElementWidget extends Window{

    public Test_PropertyCRSElementWidget(){
        super(new Shell());
    }

    /**
     * @param args
     */
    public static void main(String[] args){
        Test_PropertyCRSElementWidget test = new Test_PropertyCRSElementWidget();
        test.setBlockOnOpen(true);
        test.open();
        Display.getCurrent().dispose();
    }
    
    public Control createContents(Composite parent) {
        parent.getShell().setSize(300,500);
        parent.getShell().setLocation(10,100);
        parent.getShell().setText("CRS property view test");
        parent.setLayout(new FillLayout());
        try{
            //checkEllipsoid(parent);
            //checkPrimeMeridian(parent);
            //checkDatum(parent);
            //checkGCRS(parent);
            checkPCRS(parent);
        } catch (Exception e){e.printStackTrace();}
        return parent;
    }
    
    protected void checkEllipsoid(Composite parent){
        Ellipsoid el = DefaultEllipsoid.WGS84;
        CRSElementPropertyViewer<Ellipsoid> tree
                = new CRSElementPropertyViewer<Ellipsoid>(parent, SWT.NONE, el);
                
    }
    protected void checkPrimeMeridian(Composite parent){
        PrimeMeridian pm = DefaultPrimeMeridian.GREENWICH;
        CRSElementPropertyViewer<PrimeMeridian> tree
                = new CRSElementPropertyViewer<PrimeMeridian>(parent, SWT.NONE, pm);        
    }
    
    protected void checkDatum(Composite parent){
        GeodeticDatum gd = DefaultGeodeticDatum.WGS84;
        CRSElementPropertyViewer<GeodeticDatum> tree
                = new CRSElementPropertyViewer<GeodeticDatum>(parent, SWT.NONE, gd);        
    }
    
    protected void checkGCRS(Composite parent){
        GeographicCRS crs = DefaultGeographicCRS.WGS84;
        CRSElementPropertyViewer<GeographicCRS> tree
                = new CRSElementPropertyViewer<GeographicCRS>(parent, SWT.NONE, crs);        
    }

    protected void checkPCRS(Composite parent){
        //Create a projected CRS
        try{
            ICRSModel crsModel = GWFactoryFinder.getCRSModel();
            /*
            Conversion conv = crsModel.getDefaultObject(Conversion.class, null);
            CartesianCS cCS = crsModel.getDefaultObject(CartesianCS.class, null);
            GeographicCRS gCRS = DefaultGeographicCRS.WGS84;
            
            FactoryGroup fg = new FactoryGroup();
            ProjectedCRS crs = fg.createProjectedCRS(
                    Collections.singletonMap("name", "Custom PCRS"), gCRS, conv, cCS); //$NON-NLS-1$
            */
            ProjectedCRS crs = crsModel.getDefaultObject(ProjectedCRS.class, "2D");
            CRSElementPropertyViewer<ProjectedCRS> tree
                    = new CRSElementPropertyViewer<ProjectedCRS>(parent, SWT.NONE, crs);
        } catch (FactoryException e){e.printStackTrace();}
    }
    
}
