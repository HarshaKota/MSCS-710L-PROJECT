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
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class powerController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static  String databaseUrl = "jdbc:sqlite:MetricCollector.db";

    @FXML AreaChart<String, Number> powerChart;
    @FXML ChoiceBox<String> power_selector;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSelector(getSessions());
    }

    // get sessions from session table
    private LinkedHashMap<Long,Long> getSessions() {
        LinkedHashMap<Long, Long> sessions = new LinkedHashMap<>();
        try {
            Database dbObject = new Database(databaseUrl);
            sessions = dbObject.getSessions();
            dbObject.closeDatabaseConnection();
        } catch (Exception e) {
            log.error("getSessions: Failed to get sessions ");
        }
        return sessions;
    }
    // set selectable session options
    private void setSelector(LinkedHashMap<Long, Long> session) {
        for (Map.Entry<Long, Long> map: session.entrySet()) {
            power_selector.getItems().add(Util.convertLongToDate(map.getKey()) + " to " +Util.convertLongToDate(map.getValue()));
        }
        power_selector.setValue(power_selector.getItems().get(0));
    }

    // get power table data
    @FXML
    public void getPowerMetrics(ActionEvent actionEvent) {
        Long startSession;
        Long endSession;

        startSession = Util.convertDateToLong(power_selector.getValue().trim().split("to")[0]);
        endSession = Util.convertDateToLong(power_selector.getValue().trim().split("to")[1]);

        try {
            Database dbObject = new Database(databaseUrl);
            LinkedHashMap<Long, Double> powerMetrics = dbObject.getPowerMetrics(startSession, endSession);
            dbObject.closeDatabaseConnection();
            powerChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<Long, Double> map: powerMetrics.entrySet()) {
                series.getData().add(new XYChart.Data<String, Number>(map.getKey().toString(), map.getValue()));
            }
            powerChart.getData().add(series);
            for (XYChart.Data<String, Number> d: series.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(
                        d.getYValue().toString() + "\n" + Util.convertLongToDate(Long.valueOf(d.getXValue()))));
                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
            powerChart.getXAxis().setTickLabelsVisible(false);
            powerChart.getXAxis().setTickMarkVisible(false);
        } catch (Exception e) {
            log.error("getSessions: Failed to get sessions ");
        }
    }

}
