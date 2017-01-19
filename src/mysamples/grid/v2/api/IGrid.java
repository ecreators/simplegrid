package mysamples.grid.v2.api;

import com.sun.javafx.scene.paint.GradientUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface IGrid<T> {
    
    void setDimensions(int columnH, int rowsV);
    
    void setDataViewMapper(Function<T, GradientUtils.Point> mapper);
    
    interface ViewDataMapper<T, UI extends Component> {
        
        /**
         * Defines where your data is located. data should have a position mapper.
         */
        Coord convertDataToCoordinate(T data);
        
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
        
        private final Coord                 cellCoordinate;
        private final Map<String, Object>   parameter;
        private final PropertyChangeSupport beanUpdateNotifierHandler;
        private       T                     data;
        
        public CellViewModel(Coord coord) {
            this.cellCoordinate = coord;
            this.parameter = new HashMap<>();
            this.beanUpdateNotifierHandler = new SwingPropertyChangeSupport(this, true);
        }
        
        public PropertyChangeSupport getPropertyChangeEvent() {
            return beanUpdateNotifierHandler;
        }
        
        public Coord getCellCoordinate() {
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
    
    class Coord {
        
        private final int column;
        private final int row;
        private final int hashcode;
        
        public Coord(int column, int row) {
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
            return obj instanceof Coord && ((Coord) obj).hashcode == hashcode;
        }
    }
}
