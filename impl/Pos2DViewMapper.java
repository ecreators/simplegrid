package mysamples.grid.impl;

import mysamples.common.udim.UDim;
import mysamples.common.udim.dim2D.pos.Pos2DInt32;
import mysamples.grid.PositionViewMapper;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class Pos2DViewMapper implements PositionViewMapper {
    
    @Override
    public UDim convert(int x, int y) {
        return new Pos2DInt32(x, y);
    }
    
    @Override
    public int getHorizontalAxisIndex() {
        return Pos2DInt32.X;
    }
    
    @Override
    public int getVerticalAxisIndex() {
        return Pos2DInt32.Y;
    }
}
