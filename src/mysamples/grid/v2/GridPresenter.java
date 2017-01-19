package mysamples.grid.v2;

import mysamples.grid.v2.api.IGrid;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;

/**
 * @author Bjoern Frohberg, MyData GmbH
 */
public class GridPresenter<T, CellUI extends Component> extends JComponent {

    private final IGrid<T, CellUI>                                      owner;
    private final Dimension                                             gridSize;
    private       BiFunction<Dimension, IGrid.CellViewModel<T>, CellUI> cellRenderer;
    private       boolean                                               requireLayout;

    public GridPresenter(IGrid<T, CellUI> owner) {
        this.owner = owner;
        this.gridSize = owner.getDimension();
        createColumnHeaders();
        createRowHeaders();
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
        this.cellRenderer = owner.getDataViewMapper()::renderCell;
        int firstCellComponentIndex = gridSize.width * gridSize.height;

        // insert - first call
        if (firstCellComponentIndex >= getComponentCount()) {
            createCells();
        }

        // update - recycle ui
        for (int componentI = firstCellComponentIndex; componentI < getComponentCount(); componentI++) {
            Component c = getComponent(componentI);
            if (c instanceof CellPresenter) {
                updateCell((CellPresenter) c);
            }
        }
    }

    private void updateCell(CellPresenter cellPresenter) {
        cellPresenter.updateConent();
        cellPresenter.validate();
    }

    private void createCells() {
        int maxIndex = owner.getMaxIndex();
        for (int index = 0; index < maxIndex; index++) {
            // Jede sichtbare Zelle benoetigt ein Model!
            add(new CellPresenter<>(gridSize, getOrCreateCellViewModel(index), cellRenderer));
        }

        if (requireLayout) {
            validate();
            requireLayout = false;
        }
    }

    private IGrid.CellViewModel<T> getOrCreateCellViewModel(int index) {
        IGrid.CellViewModel<T> model = owner.getModelAtIndex(index);
        // wtf! - not yet given?
        if (model == null) {
            requireLayout = true;
            int column = getColumn(index);
            int row    = getRow(index);
            model = new IGrid.CellViewModel<>(new IGrid.Coordinate(column, row));
            // Sorry bro, no data! ;-)
        }
        return model;
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

    private static class CellPresenter<T, CellUI extends Component> extends JPanel {

        private final Dimension                                             gridSize;
        private final IGrid.CellViewModel<T>                                model;
        private final BiFunction<Dimension, IGrid.CellViewModel<T>, CellUI> cellRenderer;
        private       CellUI                                                view;

        public CellPresenter(Dimension gridSize, IGrid.CellViewModel<T> model, BiFunction<Dimension, IGrid.CellViewModel<T>, CellUI> cellRenderer) {
            this.gridSize = gridSize;
            this.model = model;
            this.cellRenderer = cellRenderer;
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }

        public void updateConent() {
            if (getComponentCount() == 0) {
                view = cellRenderer.apply(gridSize, model);
                if (view != null) {
                    add(view);
                    invalidate();
                }
            } else {
                cellRenderer.updateView(model, view);
            }
        }
    }

    private class RowHeaderView extends JLabel {

        private final int row;

        public RowHeaderView(int row) {
            this.row = row;
        }
    }

    private class ColumnHeaderView extends JLabel {

        private final int column;

        public ColumnHeaderView(int column) {
            this.column = column;
        }
    }
}
