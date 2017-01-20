package mysamples.grid.v2;

import mysamples.grid.v2.api.CellViewModel;
import mysamples.grid.v2.api.IGridCellRenderer;
import mysamples.grid.v2.api.IGrid;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

/**
 * @author Bjoern Frohberg, MyData GmbH
 */
public class GridPresenter<T, CellUI extends Component> extends JComponent {
    
    private final IGrid<T, CellUI>             owner;
    private final Dimension                    gridSize;
    private       IGridCellRenderer<T, CellUI> cellRenderer;
    private       boolean                      requireLayout;
    
    public GridPresenter(IGrid<T, CellUI> owner) {
        this.owner = owner;
        this.gridSize = owner.getDimension();
        createColumnHeaders();
        createRowHeaders();
    }
    
    private static boolean isColumnHeader(Component c) {
        return c instanceof GridPresenter.ColumnHeaderView;
    }
    
    private static boolean isRowHeader(Component c) {
        return c instanceof GridPresenter.RowHeaderView;
    }
    
    private void createRowHeaders() {
        for (int row = 0; row < gridSize.height; row++) {
            add(new RowHeaderView(row));
        }
    }
    
    private void createColumnHeaders() {
        for (int column = 0; column < gridSize.width; column++) {
            add(new ColumnHeaderView(column));
        }
    }
    
    public void renderCells() {
        this.cellRenderer = new IGridCellRenderer<T, CellUI>() {
            @Override
            public CellUI apply(Dimension gridCounts, CellViewModel<T> cellModel) {
                return owner.getDataViewMapper().renderCell(gridCounts, cellModel);
            }
            
            @Override
            public void updateView(CellViewModel<T> model, CellUI view) {
                owner.getDataViewMapper().updateCell(model, view);
            }
        };
        int firstCellComponentIndex = gridSize.width * gridSize.height;
        
        // insert - first call
        if(firstCellComponentIndex >= getComponentCount()) {
            createCells();
        }
        
        // update - recycle ui
        for (int componentI = firstCellComponentIndex; componentI < getComponentCount(); componentI++) {
            Component c = getComponent(componentI);
            if(c instanceof IGrid.CellPresenter) {
                updateCell((IGrid.CellPresenter) c);
            }
        }
    }
    
    private void updateCell(IGrid.CellPresenter<?, ?> cellPresenter) {
        cellPresenter.updateConent();
        cellPresenter.validate();
    }
    
    private void createCells() {
        int maxIndex = owner.getMaxIndex();
        for (int index = 0; index < maxIndex; index++) {
            // Jede sichtbare Zelle benoetigt ein Model!
            add(new IGrid.CellPresenter<>(gridSize, getOrCreateCellViewModel(index), cellRenderer));
        }
        
        if(requireLayout) {
            validate();
            requireLayout = false;
        }
    }
    
    private CellViewModel<T> getOrCreateCellViewModel(int index) {
        CellViewModel<T> model = owner.getModelAtIndex(index);
        // wtf! - not yet given?
        if(model == null) {
            requireLayout = true;
            int column = getColumn(index);
            int row    = getRow(index);
            model = new CellViewModel<>(new IGrid.Coordinate(column, row));
            // Sorry bro, no data! ;-)
        }
        model.getPropertyChangeEvent().removePropertyChangeListener(this::onCellChange);
        model.getPropertyChangeEvent().addPropertyChangeListener(this::onCellChange);
        return model;
    }
    
    @SuppressWarnings("unchecked")
    private void onCellChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if(source instanceof Component) {
            CellUI                         cellContent = (CellUI) source;
            IGrid.CellPresenter<T, CellUI> cell        = (IGrid.CellPresenter<T, CellUI>) cellContent.getParent();
            if(cell != null) {
                cell.getCellRenderer().updateView(cell.getModel(), cell.getView());
            }
        }
    }
    
    private int getRow(int index) {
        switch (owner.getIterationMode()) {
            case TOP_LEFT_HORIZONTAL:
                int column = getColumn(index);
                return (index - column) / gridSize.width;
            case TOP_LEFT_VERTICAL:
                return index % gridSize.height;
        }
        return -1;
    }
    
    private int getColumn(int index) {
        switch (owner.getIterationMode()) {
            case TOP_LEFT_HORIZONTAL:
                return index % gridSize.width;
            case TOP_LEFT_VERTICAL:
                int row = getRow(index);
                return (index - row) / gridSize.height;
        }
        return -1;
    }
    
    private class RowHeaderView extends JLabel {
        
        private final int row;
        
        private RowHeaderView(int row) {
            this.row = row;
        }
    }
    
    private class ColumnHeaderView extends JLabel {
        
        private final int column;
        
        private ColumnHeaderView(int column) {
            this.column = column;
        }
    }
}
