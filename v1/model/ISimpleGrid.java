package mysamples.grid.v1.model;

import mysamples.grid.v1.GridEditor;
import mysamples.grid.v1.GridView;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface ISimpleGrid<T> {
    
    GridEditor<T> edit();
    
    void setStretchMode(GridView.StretchSideMode stretch);
    
    void setScale(double scaleFactor);
    
    void scaleToSize(int width, int height);
}
