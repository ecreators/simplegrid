package mysamples.grid.v2.api;

import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.lang.String.format;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface IGrid<T, UI extends Component> {

    void reloadGrid();

    void setDimensions(int columnCountH, int rowCountV);

    default Dimension getDimension() {
        return new Dimension(1, 1);
    }

    ViewDataMapper<T,UI> getDataViewMapper();

    void setDataViewMapper(ViewDataMapper<T, UI> mapper);

    void updateCellsWithData(Collection<T> newData);

    void selectData(Collection<T> dataToSelect, boolean selectState);

    void clearSelection();

    List<T> getSelectedData();

    List<T> getData();

    CellViewModel<T> getModelAtIndex(int index);

    List<T> filterData(Predicate<T> condition);

    default int getMaxIndex() {
        Dimension dimension = getDimension();
        return getIndex(new Coordinate(dimension.width - 1, dimension.height - 1));
    }

    /**
     * Berechnet den Index einer Zelle
     */
    default int getIndex(Coordinate coord) {
        switch (getIterationMode()) {
            case TOP_LEFT_HORIZONTAL:
                return coord.row * getDimension().width + coord.column;
            case TOP_LEFT_VERTICAL:
                return coord.column * getDimension().height + coord.row;
        }
        return -1;
    }

    default IterationMode getIterationMode() {
        return IterationMode.TOP_LEFT_HORIZONTAL;
    }

    // Inner Types ...

    enum IterationMode {
        TOP_LEFT_HORIZONTAL,
        TOP_LEFT_VERTICAL;
    }

    interface ViewDataMapper<T, UI extends Component> {

        /**
         * Defines where your data is located. data should have a position mapper.
         */
        Coordinate convertDataToCoordinate(T data);

        /**
         * How data cell is presented.
         */
        UI renderCell(Dimension layout, CellViewModel<T> cellValue);

        /**
         * Find one or many cells by filter. Set filter to {@code null} to find all cells.
         */
        Collection<CellViewModel<T>> findCells(Predicate<CellViewModel<T>> filter);

        /**
         * Faster than {@link #findCells(Predicate)}, because of direct access cell by coordinate. It will find or not
         * a cell aligned in coordinate.
         */
        CellViewModel<T> getCell(int column, int row);
    }

    class CellViewModel<T> {

        public static final String DATA_PROPERTY  = "data";
        public static final String VALUE_PROPERTY = "value";

        private final Coordinate            cellCoordinate;
        private final Map<String, Object>   parameter;
        private final PropertyChangeSupport beanUpdateNotifierHandler;
        private       T                     data;

        public CellViewModel(Coordinate coord) {
            this.cellCoordinate = coord;
            this.parameter = new HashMap<>();
            this.beanUpdateNotifierHandler = new SwingPropertyChangeSupport(this, true);
        }

        public PropertyChangeSupport getPropertyChangeEvent() {
            return beanUpdateNotifierHandler;
        }

        public Coordinate getCellCoordinate() {
            return cellCoordinate;
        }

        public void setData(T data) {
            T oldData = this.data;
            this.data = data;
            beanUpdateNotifierHandler.firePropertyChange(DATA_PROPERTY, oldData, this.data);
        }

        public T getData() {
            return data;
        }

        public <R> R getValue(String key) {
            return (R) parameter.get(key);
        }

        public void setValue(String key, Object value) {
            Object oldValue = parameter.put(key, value);
            beanUpdateNotifierHandler.firePropertyChange(VALUE_PROPERTY, oldValue, value);
        }

        @Override
        public int hashCode() {
            return cellCoordinate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CellViewModel && cellCoordinate.equals(((CellViewModel) obj).cellCoordinate);
        }
    }

    class Coordinate {

        private final int column;
        private final int row;
        private final int hashcode;

        public Coordinate(int column, int row) {
            this.column = column;
            this.row = row;
            this.hashcode = format("x%d,y%d", column, row).hashCode();
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        @Override
        public int hashCode() {
            return hashcode;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IGrid.Coordinate && ((Coordinate) obj).hashcode == hashcode;
        }
    }
}
