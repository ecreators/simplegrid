package mysamples.grid.v2.api;

import java.awt.*;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public interface IGridCellRenderer<T, CellUI extends Component> {
    
    CellUI apply(Dimension gridCounts, CellViewModel<T> cellModel);
    
    void updateView(CellViewModel<T> model, CellUI view);
}
