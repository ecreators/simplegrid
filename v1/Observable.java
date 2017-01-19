package mysamples.grid.v1;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class Observable<T> implements Cloneable, ObservableValue<T> {
    
    private final Collection<ChangeListener<? super T>> changeListeners;
    private final Collection<InvalidationListener> invalidationListeners;
    private T nowValue;
   
    public Observable() {
        changeListeners = new LinkedHashSet<>();
        invalidationListeners = new LinkedHashSet<>();
    }
    
    public void setValue(T value) {
        T oldValue = nowValue;
        this.nowValue = value;
        notifyChanged(oldValue);
    }
    
    public final void notifyChanged(T oldValue) {
        changeListeners.forEach(h -> h.changed(this, oldValue, nowValue));
        invalidationListeners.forEach(h -> h.invalidated(this));
    }
    
    @Override
    public void addListener(ChangeListener<? super T> listener) {
        changeListeners.add(listener);
    }
    
    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        changeListeners.remove(listener);
    }
    
    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }
    
    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }
    
    @Override
    public T getValue() {
        return nowValue;
    }
    
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return cloneThis();
    }
    
    public final <R extends Observable> R cloneThis() {
        Object clone;
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        //noinspection unchecked
        return (R) getClass().cast(clone);
    }
}
