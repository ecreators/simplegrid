package mysamples.grid.v1;

import mysamples.common.Action;
import mysamples.common.udim.UDim;
import mysamples.common.udim.dim2D.size.Size2DInt32;
import mysamples.grid.v1.model.CellModel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static java.lang.Math.*;

/**
 * Die GridView stellt die Zellen von GridEditor dar, wann immer diese aktualisiert
 * werden. Bei der Darstellung wird Seitenverhältnis von Breite:Zeile beachtet, Minimale Zellgröße,
 * Skalierungsfaktor und die darstellbare Eltern-Container (Zeichenfläche) berücksichtigt,
 * wie auch das Streckverhalten.
 *
 * @author Bjoern Frohberg, mydata GmbH
 */
public class GridView<T> extends JPanel {
    
    private final GridEditor<T>            gridEditor;
    private final Map<UDim, CellView<T>>   cells;
    private final AspectedGridBagLayout<T> layout;
    private       Action<CellView<T>>      cellRenderer;
    private       StretchSideMode          stretchMode;
    private       double                   scale;
    private       Size2DInt32              scaleSize;
    private       boolean                  scrollable;
    
    public GridView(GridEditor<T> gridEditor) {
        this.cells = new HashMap<>();
        this.gridEditor = gridEditor;
        this.gridEditor.addEditListener(this::onCellUpdate);
        stretchMode = StretchSideMode.HORIZONTAL_STRETCH;
        scale = 1d;
        scrollable = true;
        
        layout = new AspectedGridBagLayout<>(this);
        setLayout(layout);
    }
    
    public void setStretchMode(StretchSideMode stretchMode) {
        this.stretchMode = stretchMode;
        invalidate();
    }
    
    public void setCellRenderer(Action<CellView<T>> cellRenderer) {
        this.cellRenderer = cellRenderer;
    }
    
    public void activateView(int viewID) {
        gridEditor.activateView(viewID);
    }
    
    private void onCellUpdate(GridEditor<T> sender, GridEditor.UpdateCellArgs e) {
        if(e.isReinit()) {
            cells.clear();
            removeAll();
            
            Dimension cellMinSize = e.getConfig().getCellMinSize();
            if(cellMinSize == null) {
                cellMinSize = new Dimension(0, 0);
            }
            
            layout.columnWidths = new int[e.getConfig().getColumnCount() + 1];
            layout.columnWeights = new double[e.getConfig().getColumnCount() + 1];
            Arrays.fill(layout.columnWeights, e.getConfig().getAspectRatio());
            Arrays.fill(layout.columnWidths, (int) round(cellMinSize.width * e.getConfig().getScale()));
            if(stretchMode == StretchSideMode.VERTICAL_STRETCH) {
                layout.columnWeights[layout.columnWeights.length - 1] = 1;
            } else {
                layout.columnWeights[layout.columnWeights.length - 1] = 0;
            }
            
            layout.rowHeights = new int[e.getConfig().getRowCount() + 1];
            layout.rowWeights = new double[e.getConfig().getRowCount() + 1];
            Arrays.fill(layout.rowWeights, 1 / e.getConfig().getAspectRatio());
            Arrays.fill(layout.rowHeights, (int) round(cellMinSize.height * e.getConfig().getScale()));
            if(stretchMode == StretchSideMode.HORIZONTAL_STRETCH) {
                layout.rowWeights[layout.rowWeights.length - 1] = 1;
            } else {
                layout.rowWeights[layout.rowWeights.length - 1] = 0;
            }
        }
        
        for (UDim coordinate : e.getViewCells()) {
            if(e.isReinit()) {
                createCell(e, coordinate, e.getViewCells().indexOf(coordinate));
            } else {
                updateCell(e, coordinate);
            }
        }
    }
    
    private void createCell(GridEditor.UpdateCellArgs e, UDim coordinate, int index) {
        CellView<T> view = new CellView<>(coordinate, e.getMapper(), this::getCellRenderer, index);
        cells.put(coordinate, view);
        add(view, gbc(getColumn(e, coordinate), getRow(e, coordinate)));
        updateCell(e, coordinate);
    }
    
    private void updateCell(GridEditor.UpdateCellArgs e, UDim coordinate) {
        java.util.List<CellModel<T>> cellOne = gridEditor.getSearchEngineCells().findSync(m -> m.getPosition().equals(coordinate));
        cells.get(coordinate).setDataModel(gridEditor, cellOne);
    }
    
    private Action<CellView<T>> getCellRenderer(CellView<T> view) {
        return this.cellRenderer;
    }
    
    private static GridBagConstraints gbc(int column, int row) {
        return new GridBagConstraints(column, row,
                                      1, 1,
                                      1, 1,
                                      GridBagConstraints.NORTHWEST,
                                      GridBagConstraints.BOTH,
                                      new Insets(0, 0, 0, 0),
                                      0, 0);
    }
    
    private int getRow(GridEditor.UpdateCellArgs e, UDim coordinate) {
        return coordinate.getV(e.getMapper().getVerticalAxisIndex());
    }
    
    private int getColumn(GridEditor.UpdateCellArgs e, UDim coordinate) {
        return coordinate.getV(e.getMapper().getHorizontalAxisIndex());
    }
    
    @Override
    public Dimension getPreferredSize() {
        // Über diese Methodewird bestimmt, wie groß die genutzte Fläche
        // der Zellen ist. Dies ist ein FIX, davsonst keine umgebene Scrollfläche entstünde.
        return getViewportSize();
    }
    
    public Dimension getViewportSize() {
        if(scrollable) {
            Dimension childSize = layout.childSize;
            if(childSize != null) {
                int width  = childSize.width * gridEditor.getColumnCount();
                int height = childSize.height * gridEditor.getRowCount();
                return new Dimension(width - 10, height - 10);
            }
        }
        return super.getPreferredSize();
    }
    
    public void setScale(double scaleFactor) {
        scaleFactor = Math.abs(scaleFactor);
        this.scale = scaleFactor;
        this.scaleSize = null;
        invalidate();
    }
    
    public void scaleToSize(int width, int height) {
        scale = 1d;
        scaleSize = new Size2DInt32(max(1, abs(width)), max(1, abs(height)));
        invalidate();
    }
    
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
    
    public Rectangle getCellBounds(int column, int row) {
        return cells.get(new Size2DInt32(column, row)).getBounds();
    }
    
    public enum StretchSideMode {
        HORIZONTAL_STRETCH,
        VERTICAL_STRETCH
    }
    
    private static class AspectedGridBagLayout<T> extends GridBagLayout {
        private final GridEditor<T>   gridEditor;
        private final GridView<T>     container;
        private       int             minHeight;
        private       int             minWidth;
        private       Dimension       containerSize;
        private       float           aspectRation;
        private       StretchSideMode aligningMode;
        private       Dimension       childSize;
        private       double          scale;
        
        public AspectedGridBagLayout(GridView<T> container) {
            this.container = container;
            this.gridEditor = container.gridEditor;
        }
        
        @Override
        public void layoutContainer(Container parent) {
            Map<UDim, CellView<T>>  cellViews   = container.cells;
            Collection<CellView<T>> cells       = cellViews.values();
            int                     columnCount = gridEditor.getColumnCount();
            int                     rowCount    = gridEditor.getRowCount();
            if(cellViews.size() != rowCount * columnCount) {
                System.out.println("Es wurden zu wenig oder zuviele Zellen erzeugt!");
                return;
            }
            
            // hier wird alles aufgenommen, was verändernde Maße betrifft.
            boolean update = childSize != null
                             && aspectRation == gridEditor.getAspectRation()
                             && aligningMode != null && aligningMode == container.stretchMode
                             && gridEditor.getMinimumWidth() == minWidth
                             && gridEditor.getMinimumHeight() == minHeight
                             && scale == container.scale;
            
            Dimension nowSize = parent.getBounds().getSize();
            
            if(update) {
                int oldWidth  = containerSize.width;
                int oldHeight = containerSize.height;
                childSize.width = (int) round(childSize.width * nowSize.width / (double) oldWidth);
                childSize.height = (int) round(childSize.height * nowSize.height / (double) oldHeight);
            }
            
            containerSize = nowSize;
            if(container.scaleSize != null) {
                containerSize = new Dimension(container.scaleSize.getWidth(), container.scaleSize.getHeight());
            }
            aspectRation = gridEditor.getAspectRation();
            minWidth = gridEditor.getMinimumWidth();
            minHeight = gridEditor.getMinimumHeight();
            scale = container.scale;
            
            if(update || childSize == null) {
                updateChildSize();
            }
            
            for (CellView<T> view : cells) {
                CellModel<T> model = view.getModel();
                if(model != null) {
                    
                    int column = gridEditor.getColumn(model.getPosition());
                    int row    = gridEditor.getRow(model.getPosition());
                    
                    int x = column * childSize.width;
                    int y = row * childSize.height;
                    view.setBounds(x, y, childSize.width, childSize.height);
                }
            }
        }
        
        private void updateChildSize() {
            // container size
            System.out.println(String.format("Container <%s> size: %s", container.getName(), containerSize.toString()));
            int width, height;
            // calculate cell size
            
            // stretch
            aligningMode = container.stretchMode;
            switch (aligningMode) {
                case VERTICAL_STRETCH:
                    height = (int) round(containerSize.height / (double) gridEditor.getRowCount());
                    width = round(aspectRation * height);
                    break;
                default:
                case HORIZONTAL_STRETCH:
                    width = (int) round(containerSize.width / (double) gridEditor.getColumnCount());
                    height = round(1 / aspectRation * width);
                    break;
            }
            
            // minimum
            int oldWidth = width;
            width = max(gridEditor.getMinimumWidth(), width);
            double h = width / (double) oldWidth;
            height = (int) round(height * h * scale);
            
            int oldHeight = height;
            height = max(gridEditor.getMinimumHeight(), height);
            double w = height / (double) oldHeight;
            width = (int) round(width * w * scale);
            
            childSize = new Dimension(width, height);
            System.out.println("Child size: " + childSize.toString());
        }
        
    }
}