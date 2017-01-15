package mysamples.grid;

import mysamples.common.udim.UDim;

import java.awt.*;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface PositionViewMapper {
    
    UDim convert(int x, int y);
    
    int getHorizontalAxisIndex();
    
    int getVerticalAxisIndex();
    
    default boolean iterateReversedHorizontal() {
        return false;
    }
    
    default boolean iterateReversedVertical() {
        return false;
    }
    
    default IterationMode getIterationMode() {
        return IterationMode.X_THEN_Y;
    }
    
    default float getAspect() {
        return 1f;
    }
    
    default Dimension getCellMinSize() {
        return null;
    }
    
    enum IterationMode {
        Y_THEN_X,
        X_THEN_Y;
    }
}
