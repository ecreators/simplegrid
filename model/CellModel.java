package mysamples.grid.model;

import javafx.beans.value.ObservableValue;
import mysamples.common.udim.UDim;
import mysamples.grid.Observable;

import java.util.Objects;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
@SuppressWarnings("ALL")
public class CellModel<T> extends Observable<CellModel<T>> {
    
    private final UDim          position;
    private final Equal<T>      identicator;
    private final Observable<T> data;
    
    public CellModel(UDim position, Equal<T> identicator) {
        this.position = position;
        this.identicator = identicator;
        this.position.consumed();
        this.data = new Observable<>();
        this.data.addListener((observable, oldValue, newValue) -> onUpdateData());
        setValue(this);
    }
    
    private void onUpdateData() {
        notifyChanged(this);
    }
    
    public UDim getPosition() {
        return position;
    }
    
    public void setData(T data) {
        this.data.setValue(data);
    }
    
    public T getData() {
        return data.getValue();
    }
    
    public ObservableValue<T> getDataProperty() {
        return data;
    }
    
    @Override
    public int hashCode() {
        return position.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CellModel && (obj.hashCode() == hashCode() && isEqualData(((CellModel<T>) obj).getData(), getData()));
    }
    
    private boolean isEqualData(T a, T b) {
        if(identicator == null) {
            return Objects.equals(a, b);
        }
        return identicator.isEqual(a, b);
    }
}
