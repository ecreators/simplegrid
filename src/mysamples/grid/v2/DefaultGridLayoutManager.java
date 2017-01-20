package mysamples.grid.v2;

import mysamples.grid.v2.api.IGrid;

import java.awt.*;

import static java.lang.Integer.max;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class DefaultGridLayoutManager implements GridLayoutManager {
    
    private final IGrid<?, ?> grid;
    private       Dimension   cellSize;
    private       Dimension   headerSize;
    
    public DefaultGridLayoutManager(IGrid<?, ?> grid) {
        this.grid = grid;
    }
    
    private Dimension calculateMustSize(IGrid<?, ?> grid) {
        Dimension cellLayout = grid.getDimension();
        
        int cellSizeWidth  = grid.getDataViewMapper().getCellWidth();
        int cellSizeHeight = grid.getDataViewMapper().getCellHeight();
        cellSize = new Dimension(cellSizeWidth, cellSizeHeight);
        
        int rowHeaderWidth     = grid.getDataViewMapper().getRowHeaderWidth();
        int columnHeaderHeight = grid.getDataViewMapper().getColumnHeaderHeight();
        headerSize = new Dimension(rowHeaderWidth, columnHeaderHeight);
        
        int width  = cellLayout.width * cellSizeWidth + rowHeaderWidth;
        int height = cellLayout.height * cellSizeHeight + columnHeaderHeight;
        
        return new Dimension(width, height);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container gridPresenter) {
        Dimension preferredSize = calculateMustSize(grid);
        gridPresenter.setPreferredSize(preferredSize);
        return preferredSize;
    }
    
    @Override
    public Dimension minimumLayoutSize(Container gridPresenter) {
        Dimension minimumSize = calculateMustSize(grid);
        gridPresenter.setMinimumSize(minimumSize);
        return minimumSize;
    }
    
    @Override
    public void layoutContainer(Container gridPresenter) {
        Dimension minimumSize   = gridPresenter.getMinimumSize();
        Dimension preferredSize = gridPresenter.getPreferredSize();
        if(minimumSize != null && preferredSize != null && cellSize != null && headerSize != null) {
            Dimension layout = new Dimension(max(minimumSize.width, preferredSize.width),
                                             max(minimumSize.height, preferredSize.height));
            
            int x = 0;
            int y = 0;
            for (int i = 0; i < gridPresenter.getComponents().length; i++) {
                Component component = gridPresenter.getComponent(i);
                // columnHeader
                if(i < grid.getDimension().width) {
                    x = ?;
                    y = 0;
                }
                // rowHeader
                else if(i < grid.getDimension().width * grid.getDimension().height) {
                    x = 0;
                    y = ?;
                }
                // cells
                else {
                    IGrid.CellPresenter cell  = ((IGrid.CellPresenter) component);
                    int                 index = grid.getIndex(cell.getModel().getCellCoordinate());
                    x = ?;
                    y = ?;
                    cell.setBounds(x, y, cellSize.width, cellSize.height);
                }
            }
        }
    }
}