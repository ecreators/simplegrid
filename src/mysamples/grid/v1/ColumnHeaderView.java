package mysamples.grid.v1;

import javax.swing.*;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class ColumnHeaderView extends JLabel {
    
    private final int column;
    private final HeaderType type;
    
    public ColumnHeaderView(int column, HeaderType type, Object columnName) {
        super(columnName.toString());
        this.column = column;
        this.type = type;
    }
    
    public HeaderType getType() {
        return type;
    }
    
    public int getColumn() {
        return column;
    }
    
    public enum HeaderType {
        FIRST,
        LAST;
    }
}
