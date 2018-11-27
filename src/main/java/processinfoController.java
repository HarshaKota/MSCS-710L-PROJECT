import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
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

public class processinfoController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    @FXML AreaChart<Long, Double> processinfoChart;
    @FXML ChoiceBox<String> processinfo_selector_1;
    @FXML ChoiceBox<String> processinfo_selector_2;
    @FXML ChoiceBox<String> processinfo_selector_3;
    @FXML Button processinfo_selector_1_button;
    @FXML Button processinfo_selector_2_button;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        processinfo_selector_2.setDisable(true);
        processinfo_selector_2_button.setDisable(true);
        setSelector_1(getSessions());
        setSelector_3(getColumns());
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
            processinfo_selector_1.getItems().add(Util.convertLongToDate(map.getKey()) + " to " +Util.convertLongToDate(map.getValue()));
        }
        processinfo_selector_1.setValue(processinfo_selector_1.getItems().get(0));
        processinfo_selector_1.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            processinfo_selector_2.setDisable(true);
            processinfo_selector_2_button.setDisable(true);
            processinfo_selector_2.getItems().clear();
        });
    }

    // get columns available from the process table
    ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();
        Database dbObject = new Database();
        try {
            columns = dbObject.getProcessinfoColumns();
        } catch (Exception e) {
            log.error("getColumns: Failed to get columns ");
        }
        return columns;
    }

    // set selectable column options
    private void setSelector_3(ArrayList<String> columns) {
        for (String col: columns) {
            processinfo_selector_3.getItems().add(col);
        }
        processinfo_selector_3.setValue(processinfo_selector_3.getItems().get(0));
    }

    // set selectable column options
    @FXML
    private void processinfo_selector_1_button_press(ActionEvent actionEvent) {
        processinfo_selector_2.setDisable(false);
        processinfo_selector_2_button.setDisable(false);
        setSelector_2(getProcessesNames());
    }

    // get processes names during the selected session time
    private ArrayList<String> getProcessesNames() {
        ArrayList<String> processNames = new ArrayList<>();
        try {
            Database dbObject = new Database();
            Long startSession = Util.convertDateToLong(processinfo_selector_1.getValue().trim().split("to")[0]);
            Long endSession = Util.convertDateToLong(processinfo_selector_1.getValue().trim().split("to")[1]);
            processNames =  dbObject.getProcessesNames(startSession, endSession);
        } catch (Exception e) {
            log.error("getProcessesNames: Failed to processes names ");
        }

        return processNames;
    }

    // set processes names selector
    private void setSelector_2(ArrayList<String> processesNames) {
        for (String name: processesNames) {
            processinfo_selector_2.getItems().add(name);
        }
        processinfo_selector_2.setValue(processinfo_selector_2.getItems().get(0));
    }

    // clear the chart
    @FXML
    private void clearChart() {
        processinfoChart.getData().clear();
    }


    // get table data
    @FXML
    public void processinfo_selector_2_button_press(ActionEvent actionEvent) {
        getSessionMetrics();
    }


    // If selected was a session, get data from that session
    private void getSessionMetrics() {
        Long startSession;
        Long endSession;
        Long initialTimestamp;
        String processesName;
        String columnName;

        startSession = Util.convertDateToLong(processinfo_selector_1.getValue().trim().split("to")[0]);
        endSession = Util.convertDateToLong(processinfo_selector_1.getValue().trim().split("to")[1]);
        processesName = processinfo_selector_2.getValue().trim();
        columnName = processinfo_selector_3.getValue().trim();

        try {
            Database dbObject = new Database();
            LinkedHashMap<Long, Double> processinfoMetrics = dbObject.getProcessinfoMetrics(startSession, endSession, columnName, processesName);
            initialTimestamp = processinfoMetrics.entrySet().iterator().next().getKey();
            XYChart.Series<Long, Double> series = new XYChart.Series<>();
            for (Map.Entry<Long, Double> map: processinfoMetrics.entrySet()) {
                series.getData().add(new XYChart.Data<>((map.getKey() - initialTimestamp), map.getValue()));
            }
            series.setName(columnName);
            processinfoChart.getData().add(series);
            processinfoChart.getYAxis().setLabel(columnName);
            for (XYChart.Data<Long, Double> d: series.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(
                        d.getYValue().toString()+"%" + "\n" +
                                Util.convertLongToDate(d.getXValue()+ initialTimestamp)));

                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
            processinfoChart.getXAxis().setTickLabelsVisible(false);
            processinfoChart.getXAxis().setTickMarkVisible(false);
        } catch (Exception e) {
            log.error("getSessionMetrics: Failed ");
        }
    }

}
