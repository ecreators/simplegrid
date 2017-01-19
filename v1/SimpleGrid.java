package mysamples.grid.v1;

import mysamples.common.Action;
import mysamples.common.udim.UDim;
import mysamples.grid.v1.model.Equal;

import javax.swing.*;
import java.awt.*;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class SimpleGrid<T> extends JPanel {
    
    private final ScrollableCellGrid<T> cellGridView;
    
    public SimpleGrid(int viewID, PositionViewMapper mapper, UDim size, Equal<T> comparator) {
        // component
        cellGridView = new ScrollableCellGrid<>(viewID,
                                                mapper,
                                                size,
                                                comparator);
        cellGridView.setName("Cells View");
        
        setLayout(new OverlayLayout(this));
        this.add(new JPanel() {
            
            @Override
            public boolean isOpaque() {
                return false;
            }
            
            @Override
            public Rectangle getBounds() {
                Dimension sz = SimpleGrid.this.getPreferredSize();
                return new Rectangle(sz);
            }
            
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2D = (Graphics2D) g;
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int hzEnd = cellGridView.edit().getColumnCount() - 1;
                int vtEnd = cellGridView.edit().getRowCount() - 1;
                
                int   gap             = 10;
                int   r               = 16;
                Color lineColor       = Color.LIGHT_GRAY;
                Color backgroundColor = Color.WHITE;
                Font  font            = getFont().deriveFont(14f);
                
                for (int col = 0; col <= hzEnd; col++) {
                    drawColumnDot(g2D, col, gap, r, lineColor, backgroundColor, font);
                }
                
                for (int row = 0; row <= vtEnd; row++) {
                    drawRowDot(g2D, row, gap, r, lineColor, backgroundColor, font);
                }
            }
            
            private void drawRowDot(Graphics2D g, int row, int dotConnectorLength, int dotRadius, Color lineColor, Color backgroundColor, Font font) {
                Rectangle cellFront = cellGridView.getGridView().getCellBounds(0, row);
                int       centerY   = (int) cellFront.getCenterY();
                int       left      = cellGridView.getViewport().getLocation().x;
                int       right     = cellGridView.getViewport().getLocation().x + cellGridView.getViewport().getWidth();
                
                String yText = String.valueOf(getChar(row));
                
                // Header
                drawHandleHorizontal(g, left, centerY, dotConnectorLength, lineColor);
                drawCircle(g, dotRadius, lineColor, backgroundColor, left + dotConnectorLength, centerY - dotRadius);
                drawText(g, left + dotConnectorLength + dotRadius, centerY, yText, font, getForeground(), 2);
    
                // Footer
                drawHandleHorizontal(g, right, centerY, -dotConnectorLength, lineColor);
                drawCircle(g, dotRadius, lineColor, backgroundColor, right - dotConnectorLength - dotRadius * 2, centerY - dotRadius);
                drawText(g, right + dotConnectorLength + dotRadius, centerY, yText, font, getForeground(), 2);
            }
            
            private void drawColumnDot(Graphics2D g, int column, int dotConnectorLength, int dotRadius, Color lineColor, Color backgroundColor, Font font) {
                Rectangle cellTop = cellGridView.getGridView().getCellBounds(column, 0);
                int       centerX = (int) cellTop.getCenterX();
                int       top     = cellGridView.getViewport().getLocation().y;
                int       bottom  = cellGridView.getViewport().getLocation().y + cellGridView.getViewport().getHeight();
                int       centerY = bottom - dotConnectorLength - dotRadius;
                
                String yText = String.valueOf(1 + column);
                
                // Header
                drawHandleVertical(g, centerX, top, +dotConnectorLength, lineColor);
                drawCircle(g, dotRadius, lineColor, backgroundColor, centerX - dotRadius, top + dotConnectorLength);
                drawText(g, centerX, centerY, yText, font, getForeground(), 2);
                
                // Footer
                drawHandleVertical(g, centerX, bottom, -dotConnectorLength, lineColor);
                drawCircle(g, dotRadius, lineColor, backgroundColor, centerX - dotRadius, bottom - dotConnectorLength - dotRadius * 2);
                drawText(g, centerX, centerY, yText, font, getForeground(), 2);
            }
            
            private void drawText(Graphics2D g, int centerX, int centerY, String text, Font font, Color color, int yCorrection) {
                g.setFont(font);
                g.setColor(color);
                drawString(g, centerX, centerY - yCorrection, text);
            }
    
            private void drawCircle(Graphics2D g, int dotRadius, Color lineColor, Color backgroundColor, int x, int y) {
                g.setColor(backgroundColor);
                g.fillOval(x, y, dotRadius * 2, dotRadius * 2);
                g.setColor(lineColor);
                g.drawOval(x, y, dotRadius * 2, dotRadius * 2);
            }
        }, new Integer(2));
        this.add(cellGridView, new Integer(1));
    }
    
    private static void drawHandleHorizontal(Graphics2D g, int x, int y, int length, Color lineColor) {
        g.setColor(lineColor);
        g.drawLine(x, y, x + length, y);
    }
    
    private static void drawHandleVertical(Graphics2D g, int x, int y, int length, Color lineColor) {
        g.setColor(lineColor);
        g.drawLine(x, y, x, y + length);
    }
    
    private static void drawString(Graphics2D g, int centerX, int centerY, String text) {
        int w = g.getFontMetrics().stringWidth(text);
        int h = g.getFontMetrics().getHeight();
        centerX -= (w / 2d);
        centerY += (h / 2d);
        g.drawString(text, centerX, centerY);
    }
    
    private static String getChar(int row) {
        int charByte = 65 + row;
        return String.valueOf((char) (byte) charByte);
    }
    
    
    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }
    
    public void setCellRenderer(Action<CellView<T>> cellRenderer) {
        cellGridView.setCellRenderer(cellRenderer);
    }
    
    public void registerView(int viewID, PositionViewMapper mapper) {
        cellGridView.edit().registerView(viewID, mapper);
    }
    
    public void activateView(int viewID) {
        cellGridView.activateView(viewID);
    }
    
    public void setStretchMode(GridView.StretchSideMode stretchMode) {
        cellGridView.setStretchMode(stretchMode);
    }
    
    public void setScale(double scaleFactor) {
        cellGridView.setScale(scaleFactor);
    }
    
    public void scaleToSize(int width, int height) {
        cellGridView.scaleToSize(width, height);
    }
    
    public void disableScroll() {
        cellGridView.disableScroll();
    }
    
}
