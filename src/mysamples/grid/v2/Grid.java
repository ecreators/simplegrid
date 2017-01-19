package mysamples.grid.v2;

import mysamples.grid.v2.api.IGrid;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Bjoern Frohberg, mydata GmbH
 */
public class Grid<T, CellUI extends Component> extends JScrollPane implements IGrid<T, CellUI> {

    private static final String CELL_SELECTED_PROPERTY = "selected";

    private       Dimension                      gridSize;
    private       ViewDataMapper<T, CellUI>      cellMapper;
    private       List<T>                        dataSource;
    private final Map<Integer, CellViewModel<T>> viewModels;
    private       GridPresenter<T, CellUI>       gridPresenter;

    public Grid() {
        gridSize = new Dimension(1, 1);
        viewModels = new HashMap<>();
        gridPresenter = new GridPresenter<>(this);
        setViewportView(gridPresenter);
    }

    public void setDataSource(List<T> dataSource) {
        this.dataSource = dataSource;
        reloadGrid();
    }

    @Override
    public void reloadGrid() {
        // alle cell-Container leeren
        // und je Zelle direkt gleich die neue View einfügen lassen
        gridPresenter.renderCells();
    }

    @Override
    public void setDimensions(int columnCountH, int rowCountV) {
        gridSize = new Dimension(columnCountH, rowCountV);
        gridPresenter = new GridPresenter<>(this);
        setViewportView(gridPresenter);
    }

    @Override
    public Dimension getDimension() {
        return gridSize;
    }

    @Override
    public ViewDataMapper<T, CellUI> getDataViewMapper() {
        return cellMapper;
    }

    @Override
    public void setDataViewMapper(ViewDataMapper<T, CellUI> mapper) {
        this.cellMapper = mapper;
    }

    @Override
    public void updateCellsWithData(Collection<T> newData) {
        List<CellViewModel<T>> cells = getCellViewModelsForData(newData);
        for (CellViewModel<T> cell : cells) {
            for (T data : newData) {
                Coordinate coordinate = cellMapper.convertDataToCoordinate(data);
                if (cell.getCellCoordinate().equals(coordinate)) {
                    cell.setData(data);
                    break;
                }
            }
        }
        notifyUpdated(cells);
    }

    private void notifyUpdated(Collection<CellViewModel<T>> cells) {

    }

    @Override
    public void selectData(Collection<T> dataToSelect, boolean selectState) {
        List<CellViewModel<T>> cells = getCellViewModelsForData(dataToSelect);
        cells.forEach(c -> c.setValue(CELL_SELECTED_PROPERTY, selectState));
    }

    private List<CellViewModel<T>> getCellViewModelsForData(Collection<T> dataToSelect) {
        List<T>          foundData = filterData(dataToSelect::contains);
        List<Coordinate> coords    = foundData.stream().map(cellMapper::convertDataToCoordinate).collect(Collectors.toList());
        return coords.stream().map(coord -> getModelAtIndex(getIndex(coord))).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void clearSelection() {
        for (int index = 0; index < getMaxIndex(); index++) {
            CellViewModel<T> cell = getModelAtIndex(index);
            if (cell != null) {
                cell.setValue(CELL_SELECTED_PROPERTY, false);
            }
        }
    }

    @Override
    public List<T> getSelectedData() {
        List<T> result = new ArrayList<>();
        for (int index = 0; index < getMaxIndex(); index++) {
            CellViewModel<T> cell = getModelAtIndex(index);
            if (cell != null) {
                Boolean selected = cell.getValue(CELL_SELECTED_PROPERTY);
                if (Boolean.TRUE.equals(selected)) {
                    if (cell.getData() != null) {
                        result.add(cell.getData());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<T> getData() {
        return dataSource;
    }

    @Override
    public CellViewModel<T> getModelAtIndex(int index) {
        return viewModels.get(index);
    }

    @Override
    public List<T> filterData(Predicate<T> condition) {
        return dataSource.stream().filter(condition).collect(Collectors.toList());
    }
}