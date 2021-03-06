package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class controls the main.fxml file and populates options
 * for available metric that can be selected and launches them
 */
public class mainController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    @FXML private ChoiceBox<String> metricSelectionBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArrayList<String> tableNames = fetchTableNames();
        assert tableNames != null;
        setMetricNamesToChoiceBox(tableNames);
    }

    // update choice box with values
    private void setMetricNamesToChoiceBox(ArrayList<String> metricNames) {
        metricSelectionBox.getItems().add("Static");
        for (String s: metricNames) {
            metricSelectionBox.getItems().add(s);
        }
        metricSelectionBox.setValue(metricSelectionBox.getItems().get(0));
    }

    // get values for the choice box
    private ArrayList<String> fetchTableNames() {
        return Database.tablesAvailable;
    }

    // Launch the appropriate windows for that selected metric
    @FXML
    private void launchMetricPlotter() {
        String selectedMetric = metricSelectionBox.getValue();
        Parent root1;
        try {
            root1 = FXMLLoader.load(getClass().getResource("../" + selectedMetric.toLowerCase()+".fxml"));
            Stage stage = new Stage();
            stage.setTitle(selectedMetric + " Metrics");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            log.error("launchMetricPlotter: Could not load " + selectedMetric + " window");
            try {
                throw new Exception("launchMetricPlotter: Could not load " + selectedMetric + " window");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }


    }
}
