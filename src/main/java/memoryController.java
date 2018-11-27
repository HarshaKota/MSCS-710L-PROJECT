import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class memoryController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    @FXML AreaChart<String, Number> memoryChart;
    @FXML ChoiceBox<String> memory_selector_1;
    @FXML ChoiceBox<String> memory_selector_2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSelector_1(getSessions());
        setSelector_2(getColumns());
    }

    // get sessions from session table
    LinkedHashMap<Long,Long> getSessions() {
        LinkedHashMap<Long, Long> sessions = new LinkedHashMap<>();
        try {
            Database dbObject = new Database();
            sessions = dbObject.getSessions();
        } catch (Exception e) {
            log.error("getSessions: Failed to get sessions ");
        }
        return sessions;
    }
    // set selectable session options
    private void setSelector_1(LinkedHashMap<Long, Long> session) {
        for (Map.Entry<Long, Long> map: session.entrySet()) {
            memory_selector_1.getItems().add(Util.convertLongToDate(map.getKey()) + " to " +Util.convertLongToDate(map.getValue()));
        }
        memory_selector_1.setValue(memory_selector_1.getItems().get(0));
    }

    // get columns available from the memory table
    private ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();
        Database dbObject = new Database();
        try {
            columns = dbObject.getMemoryColumns();
        } catch (Exception e) {
            log.error("getColumns: Failed to get columns ");
        }
        return columns;
    }

    // set selectable column options
    private void setSelector_2(ArrayList<String> columns) {
        memory_selector_2.getItems().add(columns.get(0));
        memory_selector_2.setValue(memory_selector_2.getItems().get(0));
    }

    // clear the chart
    @FXML
    private void clearChart() {
        memoryChart.getData().clear();
    }

    // get table data
    @FXML
    public void getMemoryMetrics(ActionEvent actionEvent) {
        getSessionMetrics();
    }

    // If selected was a session, get data from that session
    private void getSessionMetrics() {
        Long startSession;
        Long endSession;

        startSession = Util.convertDateToLong(memory_selector_1.getValue().trim().split("to")[0]);
        endSession = Util.convertDateToLong(memory_selector_1.getValue().trim().split("to")[1]);
        String columnName = memory_selector_2.getValue();

        try {
            Database dbObject = new Database();
            LinkedHashMap<Long, Double> memoryMetrics = dbObject.getMemoryMetrics(startSession, endSession, columnName);
            double totalMemory = (dbObject.getTotalMemory()*10.0)/10.0;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<Long, Double> map: memoryMetrics.entrySet()) {
                series.getData().add(new XYChart.Data<>(map.getKey().toString(), map.getValue()));
            }
            series.setName("Ram Used / " + totalMemory+"GB");
            memoryChart.getData().add(series);
            memoryChart.getYAxis().setLabel(columnName);
            for (XYChart.Data<String, Number> d: series.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(
                        d.getYValue().toString()+"GB"+"/"+totalMemory+"GB" +
                                "\n" + Util.convertLongToDate(Long.valueOf(d.getXValue()))));
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
            memoryChart.getXAxis().setTickLabelsVisible(false);
            memoryChart.getXAxis().setTickMarkVisible(false);
        } catch (Exception e) {
            log.error("getSessionMetrics: Failed ");
        }
    }

}
