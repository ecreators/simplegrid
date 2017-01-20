package mysamples.grid.v2;

import java.awt.*;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface GridLayoutManager extends LayoutManager {
    @Override
    default void addLayoutComponent(String name, Component comp) {
    }
    
    @Override
    default void removeLayoutComponent(Component comp) {
    }
    
    @Override
    Dimension preferredLayoutSize(Container parent);
    
    @Override
    Dimension minimumLayoutSize(Container parent);
    
    @Override
    void layoutContainer(Container parent);
}
