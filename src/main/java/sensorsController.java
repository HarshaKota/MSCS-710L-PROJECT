import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.util.FormatUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class sensorsController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static boolean isLive = false;

    @FXML AreaChart<String, Number> sensorsChart;
    @FXML ChoiceBox<String> sensors_selector_1;
    @FXML ChoiceBox<String> sensors_selector_2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSelector_1(getSessions());
        setSelector_2(getColumns());
    }

    // get sessions from session table
    private LinkedHashMap<Long,Long> getSessions() {
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
        sensors_selector_1.getItems().add("Live");
        for (Map.Entry<Long, Long> map: session.entrySet()) {
            sensors_selector_1.getItems().add(Util.convertLongToDate(map.getKey()) + " to " +Util.convertLongToDate(map.getValue()));
        }
        sensors_selector_1.setValue(sensors_selector_1.getItems().get(0));
    }

    // get columns available from the sensors table
    private ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();
        Database dbObject = new Database();
        try {
            columns = dbObject.getSensorsColumns();
        } catch (Exception e) {
            log.error("getColumns: Failed to get columns ");
        }
        return columns;
    }

    // set selectable column options
    private void setSelector_2(ArrayList<String> columns) {
        for (String col: columns) {
            sensors_selector_2.getItems().add(col);
        }
        sensors_selector_2.setValue(sensors_selector_2.getItems().get(0));
    }

    // clear the chart
    @FXML
    private void clearChart() {
        sensorsChart.getData().clear();
    }

    // get table data
    @FXML
    public void getSensorsMetrics(ActionEvent actionEvent) {
        if (sensors_selector_1.getValue().equals("Live")) {
            isLive = true;
            getLiveSessionMetrics();
        } else {
            isLive = false;
            getSessionMetrics();
        }
    }

    // If selected was live, get Live Metrics
    private static LinkedHashMap<Long, Double> getLiveMetrics(Database dbObject) {
        LinkedHashMap<Long, Double> liveMetrics = new LinkedHashMap<>();
        try {
            liveMetrics = dbObject.getPowerMetrics(null, null);
            Thread.sleep(Main.collectionInterval);
        } catch (Exception e) {
            log.error("getLiveMetrics: Failed to database connection ");
        }

        return liveMetrics;
    }

    // If selected was live
    private void getLiveSessionMetrics() {
        sensorsChart.getData().clear();
        final Database dbObject = new Database();
        LinkedHashMap<Long, Double> metricList = new LinkedHashMap<>();
        LinkedHashMap<Long, Double> metricsFromCallable = new LinkedHashMap<>();
        XYChart.Series<String, Number> liveSeries = new XYChart.Series<>();
        while (isLive) {
            Callable<LinkedHashMap<Long, Double>> metrics = () -> getLiveMetrics(dbObject);
            ExecutorService service = Executors.newFixedThreadPool(1);
            Future<LinkedHashMap<Long, Double>> metricsFuture = service.submit(metrics);
            try {
                metricsFromCallable = metricsFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed while getting live metrics" + e.getClass().getName() + ": " + e.getMessage());
            }
            for (Map.Entry<Long, Double> map : metricsFromCallable.entrySet()) {
                metricList.put(map.getKey(), map.getValue());
            }
            for (Map.Entry<Long, Double> map : metricList.entrySet()) {
                liveSeries.getData().add(new XYChart.Data<>(map.getKey().toString(), map.getValue()));
            }
            if (liveSeries.getData().size() == 50) {
                metricList.remove(Long.valueOf(liveSeries.getData().get(0).getXValue()));
                liveSeries.getData().remove(0);
            }
            sensorsChart.getData().add(liveSeries);
            System.out.println("Chart size = " + liveSeries.getData().size());
        }
    }

    // If selected was a session, get data from that session
    private void getSessionMetrics() {
        Long startSession;
        Long endSession;

        startSession = Util.convertDateToLong(sensors_selector_1.getValue().trim().split("to")[0]);
        endSession = Util.convertDateToLong(sensors_selector_1.getValue().trim().split("to")[1]);
        String columnName = sensors_selector_2.getValue();

        try {
            Database dbObject = new Database();
            LinkedHashMap<Long, Double> cpuMetrics = dbObject.getSensorsMetrics(startSession, endSession, columnName);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<Long, Double> map: cpuMetrics.entrySet()) {
                series.getData().add(new XYChart.Data<>(map.getKey().toString(), map.getValue()));
            }
            series.setName(columnName);
            sensorsChart.getData().add(series);
            sensorsChart.getYAxis().setLabel(columnName);
            for (XYChart.Data<String, Number> d: series.getData()) {

                Tooltip.install(d.getNode(), new Tooltip(
                        d.getYValue().toString() + "\n" +
                                Util.convertLongToDate(Long.valueOf(d.getXValue()))));

                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
            }
            sensorsChart.getXAxis().setTickLabelsVisible(false);
            sensorsChart.getXAxis().setTickMarkVisible(false);
        } catch (Exception e) {
            log.error("getSessionMetrics: Failed ");
        }
    }

}
