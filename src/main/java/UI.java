import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

import java.sql.*;
import java.util.ArrayList;

public class UI extends Application implements Runnable {
    private SystemInfo si = new SystemInfo();
    private HardwareAbstractionLayer hal = si.getHardware();

    private static final Logger log = LogManager.getLogger(UI.class);
    private Connection mainConnection = null;
    private XYChart.Series powerSeries = new XYChart.Series();
    Stage powerStage = null;

    // Setting up the UI Window
    @Override
    public void start(Stage stage) throws Exception {
        setConnection();
        powerStage = new Stage();

        Main.applicationOpen.set(true);
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        final LineChart lineChart = createPowerTableWindow();


        // Create the database
        stage.setTitle("MetricsCollector Home");

        Label descriptionLabel = new Label("Select one of the following metrics from the drop-down box and click the Submit button:");
        Label javaFXInfo = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);
        ArrayList<String> availableMetrics = new ArrayList<>();
        if(TableCreationChecks.checkPowerSource(hal.getPowerSources())){
            availableMetrics.add("Power");
        }

        final ComboBox<String> dropdown = new ComboBox<>(FXCollections.observableArrayList(availableMetrics));

        Button button = new Button("Submit");
        button.resize(500, 50);

        EventHandler<MouseEvent> clickSubmitEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (dropdown.getValue().equals("Power")) {
                        updatePowerTableWindow(mainConnection, lineChart);
                    }
                } catch (Exception e){
                    log.warn("No Metric selected");
                }
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable updater = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if(powerStage != null) {
                                if (powerStage.isShowing()) {
                                    updatePowerTableWindow(mainConnection, lineChart);
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                };

                while (Main.applicationOpen.get()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }

                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
            }


        });

        thread.setDaemon(true);
        thread.start();
        button.setOnMouseClicked(clickSubmitEvent);

        dropdown.resize(600, 25);

        StackPane rootPane = new StackPane();

        Pane buttonPane = new Pane(button);
        buttonPane.setTranslateX(675);
        buttonPane.setTranslateY(300);
        buttonPane.setTranslateZ(5);

        Pane dropDownPane = new Pane(dropdown);
        final Rectangle outputClip = new Rectangle(600,50);
        dropDownPane.setTranslateY(300);
        dropDownPane.setTranslateX(50);
        dropDownPane.setClip(outputClip);

        Pane labelPane = new Pane(descriptionLabel);
        labelPane.setTranslateX(50);
        labelPane.setTranslateY(250);

        Pane javaFXInfoPane = new Pane(javaFXInfo);
        javaFXInfoPane.setTranslateX(50);
        javaFXInfoPane.setTranslateY(500);

        rootPane.getChildren().addAll(javaFXInfoPane, labelPane, buttonPane, dropDownPane);
        Scene homeMenuScene = new Scene(rootPane, 800, 600);

        stage.setScene(homeMenuScene);
        stage.show();

    }

    // Launching the UI Window
    @Override
    public void run() {
        launch();

    }

    // Gracefully closing the application
    @Override
    public void stop() throws Exception {
        Main.applicationOpen.set(false);

        System.out.println("UI Closing. Open?:  " + Main.applicationOpen); //Sysout
        super.stop();

    }

    private LineChart createPowerTableWindow(){
        NumberAxis xAxis = new NumberAxis("X-Axis", 0d, 120d, 5);
        xAxis.setLabel("Time in Seconds");

        NumberAxis yAxis = new NumberAxis("Y-Axis", 0d, 100d, 5);
        yAxis.setLabel("Battery Percentage");

        LineChart lineChart = new LineChart(xAxis, yAxis);
        powerSeries.setName("Recent Data points");
        lineChart.getData().add(powerSeries);
        lineChart.setPrefSize(800, 400);
        lineChart.setCursor(Cursor.CROSSHAIR);

        return lineChart;
    }

    void updatePowerTableWindow(Connection connection, LineChart lineChart) throws SQLException {
        long currentTimeStamp = System.currentTimeMillis();
        String powerQuery = "SELECT * FROM POWER WHERE (TIMESTAMP <= " + currentTimeStamp + ") AND (TIMESTAMP >= " + (currentTimeStamp - 13000) + ")";
        Statement powerTableStatement = connection.createStatement();
        ResultSet powerValues = powerTableStatement.executeQuery(powerQuery);

        powerSeries.getData().clear();
        while(powerValues.next()){
            long timeStamp = powerValues.getLong("TIMESTAMP");
            timeStamp = (System.currentTimeMillis() - timeStamp)/100;
            double batteryPercentage = powerValues.getDouble("BATTERYPERCENTAGE");
            powerSeries.getData().add(new XYChart.Data( timeStamp, batteryPercentage/2));
            String powerStatus;
            int powerStatusIndicator = powerValues.getInt("POWERSTATUS");
            if(powerStatusIndicator == 0){
                powerStatus = "Not Charging";
            }else{
                powerStatus = "Charging";
            }
            final XYChart.Data<Long, Double> data = new XYChart.Data<>(timeStamp, batteryPercentage/2);
            data.setNode(
                    new HoveredThresholdNode("Battery Percentage: " + batteryPercentage/2 + "\n Power Status: " + powerStatus)
            );
            powerSeries.getData().add(data);
        }
        PowerSource[] ps= hal.getPowerSources();
        double currentBatteryPercentage = ps[0].getRemainingCapacity() * 100d;
        powerSeries.getData().add(new XYChart.Data(0,currentBatteryPercentage/2));
        String currentPowerStatus;
        double powerIndicator = ps[0].getTimeRemaining();
        if(powerIndicator < -1d){
            currentPowerStatus = "Charging";
        }else{
            currentPowerStatus = "Not Charging";
        }

        Label powerStatusLabel = new Label("Power Status: " + currentPowerStatus);
        VBox vbox = new VBox(lineChart);
        Pane lineChartPane = new Pane(vbox);
        lineChartPane.setTranslateZ(5);
        Pane powerStatusPane = new Pane(powerStatusLabel);
        powerStatusPane.setTranslateY(400);
        powerStatusPane.setTranslateX(25);

        StackPane rootPane = new StackPane();
        rootPane.getChildren().addAll(lineChartPane, powerStatusPane);

        Scene powerScene = new Scene(rootPane, 800, 600);
        powerStage.setScene(powerScene);
        powerStage.show();
        powerTableStatement.close();
    }

    private void setConnection() throws Exception {
        try {
            String databaseClassName = "org.sqlite.JDBC";
            Class.forName(databaseClassName);
            mainConnection = DriverManager.getConnection(Main.databaseUrl);
            mainConnection.setAutoCommit(true);
        } catch (NullPointerException | SQLException | ClassNotFoundException e) {
            log.error("establishDatabaseConnection: Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("establishDatabaseConnection: Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
        }

    }
    /** a node which displays a value on hover, but is otherwise empty */
    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(String labelName) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(labelName);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
            translateZProperty().setValue(10);
        }

        private Label createDataThresholdLabel(String labelName) {
            final Label label = new Label(labelName + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

}