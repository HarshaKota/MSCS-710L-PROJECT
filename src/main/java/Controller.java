import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    private String databaseUrl = "jdbc:sqlite:MetricCollector.db";

    @FXML private ChoiceBox<String> metricSelectionBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArrayList<String> tableNames = fetchTableNames(databaseUrl);
        assert tableNames != null;
        setMetricNamesToChoiceBox(tableNames);
    }

    // update choice box with values
    private void setMetricNamesToChoiceBox(ArrayList<String> metricNames) {
        for (String s: metricNames) {
            metricSelectionBox.getItems().add(s);
        }
    }

    // get values for the choice box
    private ArrayList<String> fetchTableNames(String databaseUrl) {
        Database dbObject;
        try {
            dbObject = new Database(databaseUrl);
            return dbObject.getTables();
        } catch (Exception e) {
            log.error("fetchTableNames: Failed to create database object " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
}
