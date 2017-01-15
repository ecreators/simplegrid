package mysamples.grid;

import mysamples.common.Action;
import mysamples.common.udim.UDim;
import mysamples.common.utils.UIUtils;
import mysamples.grid.model.Equal;
import mysamples.grid.model.ISimpleGrid;

import javax.swing.*;

/**
 * Diese Komponente zeigt einen scrollbaren Bereich,
 * der alle Zellen in einerm Grid visualisiert.
 *
 * @author Bjoern Frohberg, mydata GmbH
 */
public class ScrollableCellGrid<T> extends JScrollPane implements ISimpleGrid<T> {
    
    private final GridEditor<T> gridEditor;
    private final GridView<T>   gridView;
    private final int           initViewID;
    
    public ScrollableCellGrid(int viewID, PositionViewMapper mapper, UDim size, Equal<T> indicator) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.initViewID = viewID;
        this.gridEditor = new GridEditor<>(viewID, mapper, size, indicator);
        this.gridView = new GridView<>(gridEditor);
        setViewportView(gridView);
        
        // init
        UIUtils.normalizeScroll(getVerticalScrollBar(), 25, 3);
        UIUtils.normalizeScroll(getHorizontalScrollBar(), 25, 3);
    }
    
    public void disableScroll() {
        gridView.setScrollable(false);
    }
    
    public void setCellRenderer(Action<CellView<T>> cellRenderer) {
        gridView.setCellRenderer(cellRenderer);
    }
    
    public GridView<T> getGridView() {
        return gridView;
    }
    
    public int getInitViewID() {
        return initViewID;
    }
    
    public void activateView(int viewID) {
        gridView.activateView(viewID);
    }
    
    @Override
    public GridEditor<T> edit() {
        return gridEditor;
    }
    
    @Override
    public void setStretchMode(GridView.StretchSideMode stretch) {
        gridView.setStretchMode(stretch);
    }
    
    @Override
    public void setScale(double scaleFactor) {
        gridView.setScale(scaleFactor);
    }
    
    @Override
    public void scaleToSize(int width, int height) {
        gridView.scaleToSize(width, height);
    }
}
