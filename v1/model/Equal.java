package mysamples.grid.v1.model;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface Equal<T> {
    
    boolean isEqual(T a, T b);
}
