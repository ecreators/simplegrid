package mysamples.grid.model;

import mysamples.grid.GridEditor;
import mysamples.grid.GridView;
import mysamples.grid.PositionViewMapper;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface ISimpleGrid<T> {
    
    GridEditor<T> edit();
    
    void setStretchMode(GridView.StretchSideMode stretch);
    
    void setScale(double scaleFactor);
    
    void scaleToSize(int width, int height);
}
