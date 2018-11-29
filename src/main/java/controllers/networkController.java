package controllers;

import main.*;

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
public class networkController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    @FXML AreaChart<Long, Double> networkChart;
    @FXML ChoiceBox<String> network_selector_1;
    @FXML ChoiceBox<String> network_selector_2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSelector_1(getSessions());
        setSelector_2(getColumns());
    }

    // get sessions from session table
    public LinkedHashMap<Long,Long> getSessions() {
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
            network_selector_1.getItems().add(Util.convertLongToDate(map.getKey()) + " to " + Util.convertLongToDate(map.getValue()));
        }
        network_selector_1.setValue(network_selector_1.getItems().get(0));
    }

    // get columns available from the network table
    public ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();
        Database dbObject = new Database();
        try {
            columns = dbObject.getNetworkColumns();
        } catch (Exception e) {
            log.error("getColumns: Failed to get columns ");
        }
        return columns;
    }

    // set selectable column options
    private void setSelector_2(ArrayList<String> columns) {
        for (String col: columns) {
            network_selector_2.getItems().add(col);
        }
        network_selector_2.setValue(network_selector_2.getItems().get(0));
    }

    // clear the chart
    @FXML
    private void clearChart() {
        networkChart.getData().clear();
    }

    // get table data
    @FXML
    public void getNetworkMetrics(ActionEvent actionEvent) {
        getSessionMetrics();
    }

    // If selected was a session, get data from that session
    private void getSessionMetrics() {
        Long startSession;
        Long endSession;
        Double initialPacketCount;
        Long initialTimestamp;

        startSession = Util.convertDateToLong(network_selector_1.getValue().trim().split("to")[0]);
        endSession = Util.convertDateToLong(network_selector_1.getValue().trim().split("to")[1]);
        String columnName = network_selector_2.getValue();

        try {
            Database dbObject = new Database();
            LinkedHashMap<Long, Double> networkMetrics = dbObject.getNetworkMetrics(startSession, endSession, columnName);
            initialPacketCount = networkMetrics.entrySet().iterator().next().getValue();
            initialTimestamp = networkMetrics.entrySet().iterator().next().getKey();
            XYChart.Series<Long, Double> series = new XYChart.Series<>();
            if (columnName.equalsIgnoreCase("sizereceived") || columnName.equalsIgnoreCase("sizesent")) {
                for (Map.Entry<Long, Double> map : networkMetrics.entrySet()) {
                    series.getData().add(new XYChart.Data<>((map.getKey() - initialTimestamp), (map.getValue())));
                }
            } else {
                for (Map.Entry<Long, Double> map : networkMetrics.entrySet()) {
                    series.getData().add(new XYChart.Data<>((map.getKey() - initialTimestamp), (map.getValue() - initialPacketCount)));
                }
            }
            series.setName(columnName);
            networkChart.getData().add(series);
            networkChart.getYAxis().setLabel(columnName);
            for (XYChart.Data<Long, Double> d: series.getData()) {
                if (columnName.equalsIgnoreCase("noofpacketsreceived") || columnName.equalsIgnoreCase("noofpacketssent")) {
                    Tooltip.install(d.getNode(), new Tooltip(
                            "Packets: " + d.getYValue().toString() +
                            "\n" + "Total packets: " + (d.getYValue().longValue() + initialPacketCount) +
                                    "\n" + Util.convertLongToDate(d.getXValue()+ initialTimestamp)));
                } else {
                    Tooltip.install(d.getNode(), new Tooltip(
                            d.getYValue().toString()+"GB" +
                                    "\n" + Util.convertLongToDate(d.getXValue()+ initialTimestamp)));
                }

                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
            networkChart.getXAxis().setTickLabelsVisible(false);
            networkChart.getXAxis().setTickMarkVisible(false);
        } catch (Exception e) {
            log.error("getSessionMetrics: Failed ");
        }
    }

}
