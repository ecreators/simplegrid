package mysamples.grid.v2.api;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class CellViewModel<T> {
    
    public static final String DATA_PROPERTY                = "data";
    public static final String VALUE_PROPERTY               = "value";
    public static final String BEAN_UPDATE_NOTIFIER_HANDLER = "beanUpdateNotifierHandler";
    public static final String CELL_COORDINATE              = "cellCoordinate";
    
    private final IGrid.Coordinate      cellCoordinate;
    private final Map<String, Object>   parameter;
    private final PropertyChangeSupport beanUpdateNotifierHandler;
    private       T                     data;
    
    public CellViewModel(IGrid.Coordinate coord) {
        this.cellCoordinate = coord;
        this.beanUpdateNotifierHandler = new SwingPropertyChangeSupport(this, true);
        this.parameter = new HashMap<>();
        this.parameter.put(CELL_COORDINATE, cellCoordinate);
        this.parameter.put(BEAN_UPDATE_NOTIFIER_HANDLER, beanUpdateNotifierHandler);
        this.parameter.put(DATA_PROPERTY, data);
    }
    
    public PropertyChangeSupport getPropertyChangeEvent() {
        return beanUpdateNotifierHandler;
    }
    
    public IGrid.Coordinate getCellCoordinate() {
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