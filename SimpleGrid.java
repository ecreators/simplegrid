package mysamples.grid;

import mysamples.common.Action;
import mysamples.common.LayoutManagerAdapter;
import mysamples.common.udim.UDim;
import mysamples.common.udim.dim2D.pos.IPos2D;
import mysamples.common.udim.dim2D.size.Size2DInt32;
import mysamples.grid.impl.Pos2DViewMapper;
import mysamples.grid.model.Equal;

import javax.swing.*;
import java.awt.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.round;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class SimpleGrid<T> extends JPanel {
    
    private final ScrollableCellGrid<T> cellGridView;
    private final GridView              columnHeaderView;
    private final GridView              rowHeaderView;
    private final GridView              columnFooterView;
    private final GridView              rowFooterView;
    private final GridEditor<?>         rowHeaderEditor;
    private final GridEditor<?>         columnHeaderEditor;
    private final Pos2DViewMapper       demiSizeMapper;
    
    public SimpleGrid(int viewID, PositionViewMapper mapper, UDim size, Equal<T> comparator) {
        // component
        cellGridView = new ScrollableCellGrid<>(viewID,
                                                mapper,
                                                size,
                                                comparator);
        cellGridView.setName("Cells View");
        
        demiSizeMapper = new Pos2DViewMapper();
        
        Size2DInt32 columnHeaderSize = new Size2DInt32(size.getV(mapper.getHorizontalAxisIndex()), 1);
        columnHeaderEditor = new GridEditor<>(viewID, demiSizeMapper, columnHeaderSize, null);
        columnHeaderView = initColumnHeader(new GridView<>(columnHeaderEditor));
        columnHeaderView.setName("Column Header View");
        columnFooterView = initColumnHeader(new GridView<>(columnHeaderEditor));
        columnFooterView.setName("Column Footer View");
        
        Size2DInt32 rowHeaderSize = new Size2DInt32(1, size.getV(mapper.getVerticalAxisIndex()));
        rowHeaderEditor = new GridEditor<>(viewID, demiSizeMapper, rowHeaderSize, null);
        rowHeaderView = initRowHeader(new GridView<>(rowHeaderEditor));
        rowHeaderView.setName("Row Header View");
        rowFooterView = initRowHeader(new GridView<>(rowHeaderEditor));
        rowFooterView.setName("Row Footer View");
        
        layout3x3();
        
        // GBL
        add(columnHeaderView);
        add(columnFooterView);
        add(cellGridView);
        add(rowHeaderView);
        add(rowFooterView);
    }
    
    private <TIgnore> GridView<TIgnore> initColumnHeader(GridView<TIgnore> columnHeaderGridView) {
        columnHeaderGridView.setStretchMode(GridView.StretchSideMode.HORIZONTAL_STRETCH);
        columnHeaderGridView.setCellRenderer(cellView -> {
            if(cellView.getComponentCount() == 0) {
                Object name = 1 + cellView.getModel().getPosition().getV(IPos2D.X);
                cellView.add(new JLabel(name.toString()));
                cellView.invalidate();
            }
        });
        return columnHeaderGridView;
    }
    
    private <TIgnore> GridView<TIgnore> initRowHeader(GridView<TIgnore> rowHeaderGridView) {
        rowHeaderGridView.setStretchMode(GridView.StretchSideMode.VERTICAL_STRETCH);
        rowHeaderGridView.setCellRenderer(cellView -> {
            if(cellView.getComponentCount() == 0) {
                Object name = 1 + cellView.getModel().getPosition().getV(IPos2D.Y);
                cellView.add(new JLabel(name.toString()));
                cellView.invalidate();
            }
        });
        return rowHeaderGridView;
    }
    
    private void layout3x3() {
    /*
    +---+-------------+---+
    | x | COL HEADER  | x |
    +---+-------------+---+
    |   |             |   |
    | R |             | R |
    | O |             | O |
    | W |             | W |
    |   |             |   |
    | H |    CELL     | F |
    | E | SCROLLVIEW  | O |
    | A |             | O |
    | D |             | T |
    | E |             | E |
    | R |             | R |
    |   |             |   |
    +---+-------------+---+
    | x | COL FOOTER  | x |
    +---+-------------+---+
    */
        
        LayoutManagerAdapter layout = parent -> {
            
            int rowHeaderWidth     = calulateRenderWidth(rowHeaderView);
            int columnHeaderHeight = calulateRenderHeight(columnHeaderView);
            int cellsWidth         = parent.getBounds().width - 2 * rowHeaderWidth;
            int cellsHeight        = parent.getBounds().height - 2 * columnHeaderHeight;
            
            // COLUMN HEADER x:ROW HEADER.width, y:0
            int x = rowHeaderWidth;
            int y = 0;
            columnHeaderView.setBounds(x, y, cellsWidth, columnHeaderHeight);
            System.out.println("columnHeaderView rect: " + columnHeaderView.getBounds());
            
            // ROW HEADER x:0, y:COLUMN HEADER.height
            x = 0;
            y += columnHeaderHeight;
            rowHeaderView.setBounds(x, y, rowHeaderWidth, cellsHeight);
            System.out.println("rowHeaderView rect: " + rowHeaderView.getBounds());
            
            // CELLS x:ROW HEADER.width, y:COLUMN HEADER.height
            x = rowHeaderWidth;
            y = columnHeaderHeight;
            cellGridView.setBounds(x, y, cellsWidth, cellsHeight);
            System.out.println("cellGridView rect: " + cellGridView.getBounds());
            
            // COLUMN FOOTER x:ROW HEADER.width, y:COLUMN HEADER.height + cells.height
            x = rowHeaderWidth;
            y = columnHeaderHeight + cellsHeight;
            columnFooterView.setBounds(x, y, cellsWidth, columnHeaderHeight);
            System.out.println("columnFooterView rect: " + columnFooterView.getBounds());
            
            // ROW FOOTER x:ROW HEADER.width + cells.width, y:COLUMN HEADER.height
            x = rowHeaderWidth + cellsWidth;
            y = columnHeaderHeight;
            rowFooterView.setBounds(x, y, rowHeaderWidth, cellsHeight);
        };
        setLayout(layout);
    }
    
    private static int calulateRenderHeight(Component view) {
        return min(view.getMaximumSize() == null
                   ? Integer.MAX_VALUE
                   : view.getMaximumSize().height,
                   max(view.getBounds().height,
                       view.getMinimumSize() == null
                       ? 0
                       : view.getMinimumSize().height));
    }
    
    private static int calulateRenderWidth(Component view) {
        return min(view.getMaximumSize() == null
                   ? Integer.MAX_VALUE
                   : view.getMaximumSize().width,
                   max(view.getBounds().width,
                       view.getMinimumSize() == null
                       ? 0
                       : view.getMinimumSize().width));
    }
    
    public void setCellRenderer(Action<CellView<T>> cellRenderer) {
        cellGridView.setCellRenderer(cellRenderer);
    }
    
    public void registerView(int viewID, PositionViewMapper mapper) {
        columnHeaderEditor.registerView(viewID, demiSizeMapper);
        rowHeaderEditor.registerView(viewID, demiSizeMapper);
        cellGridView.edit().registerView(viewID, mapper);
    }
    
    public void activateView(int viewID) {
        columnHeaderEditor.activateView(viewID);
        rowHeaderEditor.activateView(viewID);
        cellGridView.activateView(viewID);
    }
    
    public void setStretchMode(GridView.StretchSideMode stretchMode) {
        cellGridView.setStretchMode(stretchMode);
    }
    
    public void setScale(double scaleFactor) {
        columnHeaderView.setScale(scaleFactor);
        columnFooterView.setScale(scaleFactor);
        rowHeaderView.setScale(scaleFactor);
        rowFooterView.setScale(scaleFactor);
        cellGridView.setScale(scaleFactor);
    }
    
    public void scaleToSize(int width, int height) {
        columnHeaderView.scaleToSize(width, height);
        columnFooterView.scaleToSize(width, height);
        rowHeaderView.scaleToSize(width, height);
        rowFooterView.scaleToSize(width, height);
        
        // TODO shrink including headers-Sizes
        cellGridView.scaleToSize(width, height);
    }
    
    public void disbaleScroll() {
        cellGridView.disableScroll();
    }
    
}
