package mysamples.grid;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mysamples.common.EventHandler;
import mysamples.grid.model.CellModel;
import mysamples.grid.model.Equal;
import mysamples.common.udim.UDim;
import mysamples.search.CollectionSearchEngine;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Der GridEditor erlaubt das einfügen und entfernen von optionalen Daten,
 * die einer Zelle per multidimensionalen Koordinaten zur Verfügung gestellt werden.
 * Auch das Suchen per Datensatz, Model oder Position ist möglich auf synchrone oder
 * asynchrone Weise wird unterstützt.
 * Jede Änderung an der Menge der Datensätze bewirkt ein Aktualisieren entsprechender
 * Zellen.
 * Egal wieviele Dimensionen (mind. 2) eine Koordinate hat, per PositionMapping
 * kann zwischen verschiedenen 2-dimensionalen Ansichten gewechselt werden, was
 * ebenfalls zu einer Aktualisierung führt.
 * Es sei erwähnt, dass mit Aktualisierung lediglich das Triggern des UpdateEvents
 * gemeint ist.
 *
 * @author Bjoern Frohberg, mydata GmbH
 */
public class GridEditor<T> {
    
    private final ObservableList<CellModel<T>>                                      dataGridCells;
    private final CollectionSearchEngine<GridEditor<T>, CellModel<T>, CellModel<T>> searchEngineCells;
    private final CollectionSearchEngine<GridEditor<T>, CellModel<T>, UDim>         searchEnginePosition;
    private final CollectionSearchEngine<GridEditor<T>, CellModel<T>, T>            searchEngineData;
    private final ChangeListener<CellModel<T>>                                      cellUpdateListener;
    private final Map<Integer, PositionViewMapper>                                  viewMappers;
    private final Map<UDim, CellView<T>>                                            componentsCurrentViewID;
    private final UDim                                                              dimensions;
    private final Equal<T>                                                          indicator;
    private final Observable<Double>                                                scale;
    
    // variable
    private       List<CellModel<T>>                                      visibleCellsInOrder;
    private       CellConfig                                              visibleCellsConfig;
    private       PositionViewMapper                                      visibleCellMapper;
    private       int                                                     activeViewID;
    private final Collection<EventHandler<GridEditor<T>, UpdateCellArgs>> updateEditorListeners;
    
    public GridEditor(int viewID, PositionViewMapper vConf, UDim dimensions, Equal<T> indicator) {
        this.dimensions = dimensions;
        this.indicator = indicator;
        
        updateEditorListeners = new LinkedHashSet<>();
        dataGridCells = FXCollections.observableArrayList();
        cellUpdateListener = (observable, oldValue, newValue) -> GridEditor.this.onCellChanged(newValue);
        dataGridCells.addListener(this::onDataGridCellsChanged);
        
        searchEngineCells = new CollectionSearchEngine<>(this, dataGridCells);
        searchEnginePosition = new CollectionSearchEngine<>(this, dataGridCells, CellModel::getPosition);
        searchEngineData = new CollectionSearchEngine<>(this, dataGridCells, CellModel::getData);
        
        componentsCurrentViewID = new HashMap<>();
        
        viewMappers = new HashMap<>();
        registerView(viewID, vConf);
        
        scale = new Observable<>();
        scale.setValue(1d);
        scale.addListener(this::onScaleChanged);
    }
    
    public CollectionSearchEngine<GridEditor<T>, CellModel<T>, T> getSearchEngineData() {
        return searchEngineData;
    }
    
    public CollectionSearchEngine<GridEditor<T>, CellModel<T>, CellModel<T>> getSearchEngineCells() {
        return searchEngineCells;
    }
    
    public CollectionSearchEngine<GridEditor<T>, CellModel<T>, UDim> getSearchEnginePosition() {
        return searchEnginePosition;
    }
    
    private void onScaleChanged(ObservableValue<? extends Double> scaleProperty, Double old, Double now) {
        updateLayout(false, visibleCellsInOrder);
    }
    
    public void registerView(int viewID, PositionViewMapper vConf) {
        viewMappers.put(viewID, vConf);
    }
    
    public void activateView(int viewID) {
        activeViewID = viewID;
        visibleCellMapper = viewMappers.get(viewID);
        if(visibleCellMapper != null) {
            visibleCellsInOrder = getViewModelsOrdered(visibleCellMapper);
            
            int columnCount = dimensions.getV(visibleCellMapper.getHorizontalAxisIndex());
            int rowCount    = dimensions.getV(visibleCellMapper.getVerticalAxisIndex());
            
            visibleCellsConfig = new CellConfig(visibleCellMapper.getAspect(), visibleCellMapper.getCellMinSize(), scale.getValue(),
                                                columnCount,
                                                rowCount);
            updateLayout(true, visibleCellsInOrder);
        }
    }
    
    protected void updateLayout(boolean reinit, List<CellModel<T>> cells) {
        if(visibleCellMapper != null) {
            List<UDim> positions = cells.stream().map(CellModel::getPosition).collect(Collectors.toList());
            UpdateCellArgs args = new UpdateCellArgs(reinit,
                                                     positions,
                                                     visibleCellsConfig,
                                                     visibleCellMapper);
            updateEditorListeners.stream().collect(Collectors.toList()).forEach(h -> h.onCallback(this, args));
        }
    }
    
    /**
     * Diese Methode bestimmt die geordneten Zellen (ViewModel's), die sich durch
     * die Ansicht theoretisch ergeben. Der {@link PositionViewMapper} fungiert hierbei als Konverter
     * zwischen der multidimensionalen Koordinate und einer TopView-2D-Koordinate, wie die Betrachtung
     * einer Seite eines Würfels, beispielsweise oder eine Seite einer Fläche in eines 3-Dimensionalen
     * Hexagons.
     *
     * @param mapper
     * @return
     */
    private List<CellModel<T>> getViewModelsOrdered(PositionViewMapper mapper) {
        List<CellModel<T>> cellsForView = new ArrayList<>();
        int                hzAxisI      = mapper.getHorizontalAxisIndex();
        int                vtAxisI      = mapper.getVerticalAxisIndex();
        
        boolean reverseH = mapper.iterateReversedHorizontal();
        boolean reverseV = mapper.iterateReversedVertical();
        int     evenMin  = 0;
        int     evenMax  = dimensions.getV(vtAxisI);
        int     oddMin   = 0;
        int     oddMax   = dimensions.getV(hzAxisI);
        int     evenMove = 1;
        int     oddMove  = 1;
        
        PositionViewMapper.IterationMode iterationMode = mapper.getIterationMode();
        if(iterationMode != PositionViewMapper.IterationMode.Y_THEN_X) {
            evenMax = dimensions.getV(hzAxisI);
            oddMax = dimensions.getV(vtAxisI);
        }
        
        if(reverseH) {
            evenMin = evenMax;
            evenMax = -1;
            evenMove = -1;
        }
        
        if(reverseV) {
            oddMin = oddMax;
            oddMax = -1;
            oddMove = -1;
        }
        
        StringBuilder sb    = new StringBuilder();
        int[][]       array = new int[evenMax][oddMax];
        int i=0;
        for (int o = oddMin; o < oddMax; o += oddMove) {
            if(sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append("[");
            for (int e = evenMin; e < evenMax; e += evenMove) {
                UDim pos;
                if(iterationMode == PositionViewMapper.IterationMode.X_THEN_Y) {
                    pos = mapper.convert(e,o);
                } else {
                    pos = mapper.convert(o,e);
                }
                verifyDimensionsOrThrow(pos);
                array[e][o] = i++;
                if(e > 0) {
                    sb.append(", ");
                }
                sb.append(array[e][o]);
                
                CellModel<T> cellOrNull = searchEngineCells.findSync(m -> m.getPosition().equals(pos)).stream().findFirst().orElse(null);
                if(cellOrNull == null) {
                    CellModel<T> model = new CellModel<>(pos, indicator);
                    cellOrNull = model;
                    dataGridCells.addAll(Collections.singleton(model));
                } else {
                    System.out.append("C!=").append(Arrays.toString(new int[]{e, o})).append(System.lineSeparator());
                }
                cellsForView.add(cellOrNull);
                
                // vorwärts
                
                // X_THEN_Y
                // x-y:0 = 0,1,2
                // x-y:1 = 3,4,5
                
                // X_THEN_Y
                // y-x:0 = 0,2,4
                // y-x:1 = 1,3,5
            }
            sb.append("]");
        }

        System.out.println("Created Views:" + System.lineSeparator() + sb.toString());
        return cellsForView;
    }
    
    private void verifyDimensionsOrThrow(UDim pos) {
        if(pos.getDimensions() != dimensions.getDimensions()) {
            throw new IllegalArgumentException("Pos dimensions count inequivalent to dimensions-dimensions count!");
        }
    }
    
    private void onDataGridCellsChanged(ListChangeListener.Change<? extends CellModel<T>> change) {
        // die Zellen-Update registrieren
        if(change.next()) {
            if(change.wasAdded()) {
                change.getAddedSubList().forEach(this::registerCell);
            } else if(change.wasRemoved()) {
                change.getRemoved().forEach(this::unregisterCell);
            }
        }
    }
    
    private void unregisterCell(CellModel<T> cell) {
        cell.removeListener(cellUpdateListener);
    }
    
    private void registerCell(CellModel<T> cell) {
        cell.addListener(cellUpdateListener);
    }
    
    public void addEditListener(EventHandler<GridEditor<T>, UpdateCellArgs> listener) {
        updateEditorListeners.add(listener);
    }
    
    private void onCellChanged(CellModel<T> cell) {
        
    }
    
    private void onCellDataChanged(CellModel<T> cell, T dataOld, T dataNow) {
        
    }
    
    public int getColumn(UDim position) {
        return position.getV(viewMappers.get(activeViewID).getHorizontalAxisIndex());
    }
    
    public int getRow(UDim position) {
        return position.getV(viewMappers.get(activeViewID).getVerticalAxisIndex());
    }
    
    public int getColumnCount() {
        return dimensions.getV(viewMappers.get(activeViewID).getHorizontalAxisIndex());
    }
    
    public int getRowCount() {
        return dimensions.getV(viewMappers.get(activeViewID).getVerticalAxisIndex());
    }
    
    public float getAspectRation() {
        return viewMappers.get(activeViewID).getAspect();
    }
    
    public int getMinimumWidth() {
        Dimension cellMinSize = viewMappers.get(activeViewID).getCellMinSize();
        return cellMinSize == null
               ? 0
               : cellMinSize.width;
    }
    
    public int getMinimumHeight() {
        Dimension cellMinSize = viewMappers.get(activeViewID).getCellMinSize();
        return cellMinSize == null
               ? 0
               : cellMinSize.height;
    }
    
    public Object getColumnName(int columnIndex) {
        return columnIndex;
    }
    
    public static final class UpdateCellArgs {
        
        private final boolean            reinit;
        private final List<UDim>         viewCells;
        private final CellConfig         config;
        private final PositionViewMapper mapper;
        
        private UpdateCellArgs(boolean reinit, List<UDim> updatedViewCells, CellConfig config, PositionViewMapper mapper) {
            this.reinit = reinit;
            this.viewCells = updatedViewCells;
            this.config = config;
            this.mapper = mapper;
        }
        
        public boolean isReinit() {
            return reinit;
        }
        
        public List<UDim> getViewCells() {
            return viewCells;
        }
        
        public CellConfig getConfig() {
            return config;
        }
        
        public PositionViewMapper getMapper() {
            return mapper;
        }
    }
    
    public static final class CellConfig {
        
        private final float     aspectRatio;
        private final Dimension cellMinSize;
        private final double    scale;
        private final int       columnCount;
        private final int       rowCount;
        
        public CellConfig(float aspectRatio, Dimension cellMinSize, double scale, int columnCount, int rowCount) {
            this.aspectRatio = aspectRatio;
            this.cellMinSize = cellMinSize;
            this.scale = scale;
            this.columnCount = columnCount;
            this.rowCount = rowCount;
        }
        
        public float getAspectRatio() {
            return aspectRatio;
        }
        
        public Dimension getCellMinSize() {
            return cellMinSize;
        }
        
        public double getScale() {
            return scale;
        }
        
        public int getColumnCount() {
            return columnCount;
        }
        
        public int getRowCount() {
            return rowCount;
        }
    }
}
