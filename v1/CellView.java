package mysamples.grid.v1;

import mysamples.common.Action;
import mysamples.grid.v1.model.CellModel;
import mysamples.common.udim.UDim;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class CellView<T> extends JPanel {
    
    private final UDim                                       coordinate;
    private final PositionViewMapper                         mapper;
    private final Function<CellView<T>, Action<CellView<T>>> rendering;
    private       T                                          data;
    private       CellModel<T>                               model;
    private final int                                        index;
    
    public CellView(UDim coordinate, PositionViewMapper mapper, Function<CellView<T>, Action<CellView<T>>> rendering, int index) {
        this.coordinate = coordinate;
        this.mapper = mapper;
        this.rendering = rendering;
        this.index = index;
    }
    
    void setDataModel(GridEditor<T> sender, List<CellModel<T>> result) {
        if(result.size() == 1) {
            model = result.get(0);
            setData(model.getData());
        } else {
            model = null;
            setData(null);
        }
    }
    
    public CellModel<T> getModel() {
        return model;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
        if(rendering != null) {
            Action<CellView<T>> renderer = rendering.apply(this);
            if(renderer != null) {
                renderer.invoke(this);
            }
        }
    }
    
    public int getIndex() {
        return index;
    }
}
