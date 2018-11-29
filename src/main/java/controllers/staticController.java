package controllers;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import main.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class staticController implements Initializable {

    private static final Logger log = LogManager.getLogger(UI.class);

    @FXML
    TextArea staticMetricTextBox;
    @FXML ChoiceBox<String> static_selector_1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        staticMetricTextBox.setFont(new Font("Arial", 16));
        // set the text area gradient style below if you're into that
        //staticMetricTextBox.setStyle("-fx-background-color: linear-gradient(#ffafbd, #ffc3a0);");
        setSelector_1(getStaticMetricNames());
    }

    // gives the names of all the static metrics that will be available
    private ArrayList<String> getStaticMetricNames() {
        ArrayList<String> staticMetricNames = new ArrayList<>();
        staticMetricNames.add("System Metrics");
        staticMetricNames.add("Processor Metrics");
        staticMetricNames.add("Disk Metrics");
        staticMetricNames.add("File System Metrics");
        staticMetricNames.add("Network Metrics");

        return staticMetricNames;
    }
    // set selectable session options
    private void setSelector_1(ArrayList<String> staticMetricNames) {
        for (String name: staticMetricNames) {
            static_selector_1.getItems().add(name);
        }
        static_selector_1.setValue(static_selector_1.getItems().get(0));
    }

    // clear the chart
    @FXML
    private void clearTextArea() {
        staticMetricTextBox.setText("");
    }

    // If selected was a session, get data from that session
    @FXML
    private void getStaticMetrics() {
        String metricName = static_selector_1.getValue();

        staticMetricCollector smc = new staticMetricCollector();

        switch (metricName) {
            case "System Metrics": setTextBox(smc.getStaticSystemMetrics()); break;
            case "Processor Metrics": setTextBox(smc.getStaticProcessorMetrics()); break;
            case "Disk Metrics": setTextBox(smc.getStaticDiskMetrics()); break;
            case "File System Metrics": setTextBox(smc.getStaticFileSystemMetrics()); break;
            case "Network Metrics": setTextBox(smc.getStaticNetworkMetrics()); break;
            default: {
                log.error("Something went wrong in fetching the static metrics. Please restart the program");
                ArrayList<String> errorCase = new ArrayList<>();
                errorCase.add("Something went wrong in fetching the static metrics. Please restart the program");
                setTextBox(errorCase);
            }
        }
    }


    // set the label with requested static metric data
    private void setTextBox(ArrayList<String> staticMetricData) {
        for (String s: staticMetricData) staticMetricTextBox.setText(staticMetricTextBox.getText() + s + "\n");
    }

}