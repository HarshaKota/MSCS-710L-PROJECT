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
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();

    private static final Logger log = LogManager.getLogger(UI.class);
    private Connection mainConnection = null;
    private Stage powerStage = null;
    private XYChart.Series powerSeries = new XYChart.Series();

    // Setting up the UI Window
    @Override
    public void start(Stage stage) throws Exception {
        setConnection();
        Main.applicationOpen.set(true);
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        final Scene powerScene = createPowerTableWindow();


        // Create the database
        stage.setTitle("MetricsCollector Home");

        Label descriptionLabel = new Label("Select one of the following metrics from the drop-down box and click the Submit button:");
        Label javaFXInfo = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);
        ArrayList<String> availableMetrics = new ArrayList<>();
        if(TableCreationChecks.checkPowerSource(hal.getPowerSources())){
            availableMetrics.add("Power");
        }

        final ComboBox dropdown = new ComboBox(FXCollections.observableArrayList(availableMetrics));

        Button button = new Button("Submit");
        button.resize(500, 50);

        EventHandler<MouseEvent> clickSubmitEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (dropdown.getValue().equals("Power")) {
                        updatePowerTableWindow(mainConnection, powerScene);
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
                            if (powerStage.isShowing()) {
                                updatePowerTableWindow(mainConnection, powerScene);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                };

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
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
        System.out.println();
        System.out.println("start");

    }

    // Launching the UI Window
    @Override
    public void run() {
        launch();
        System.out.println("Run");

    }

    // Gracefully closing the application
    @Override
    public void stop() throws Exception {
        Main.applicationOpen.set(false);

        System.out.println("UI Closing. Open?:  " + Main.applicationOpen); //Sysout
        super.stop();
        System.out.println("stop");

    }

    Scene createPowerTableWindow(){
        NumberAxis xAxis = new NumberAxis("X-Axis", 0d, 120d, 5);
        xAxis.setLabel("Time in Seconds");

        NumberAxis yAxis = new NumberAxis("Y-Axis", 0d, 100d, 5);
        yAxis.setLabel("Battery Percentage");

        LineChart lineChart = new LineChart(xAxis, yAxis);
        powerSeries.setName("Recent Data points");
        lineChart.getData().add(powerSeries);
        lineChart.setPrefSize(800, 400);
        lineChart.setCursor(Cursor.CROSSHAIR);

        VBox vbox = new VBox(lineChart);

        Pane lineChartPane = new Pane(vbox);
        Scene powerScene = new Scene(lineChartPane, 800, 600);
        powerStage = new Stage();


        return powerScene;
    }

    void updatePowerTableWindow(Connection connection, Scene powerScene) throws SQLException {
        long currentTimeStamp = System.currentTimeMillis();
        String powerQuery = "SELECT * FROM POWER WHERE (TIMESTAMP <= " + currentTimeStamp + ") AND (TIMESTAMP >= " + (currentTimeStamp - 13000) + ")";
        System.out.println(powerQuery);
        Statement powerTableStatement = connection.createStatement();
        ResultSet powerValues = powerTableStatement.executeQuery(powerQuery);
        powerSeries.getData().clear();
        while(powerValues.next()){
            long timeStamp = powerValues.getLong("TIMESTAMP");
            timeStamp = (System.currentTimeMillis() - timeStamp)/100;
            System.out.println(timeStamp);
            double batteryPercentage = powerValues.getDouble("BATTERYPERCENTAGE");
            System.out.println(batteryPercentage);
            powerSeries.getData().add(new XYChart.Data( timeStamp, batteryPercentage));
            final XYChart.Data<Long, Double> data = new XYChart.Data<>(timeStamp, batteryPercentage);
            data.setNode(
                    new HoveredThresholdNode(batteryPercentage)
            );
            powerSeries.getData().add(data);
        }
        PowerSource[] ps= hal.getPowerSources();
        double currentBatteryPercentage = ps[0].getRemainingCapacity() * 100d;
        powerSeries.getData().add(new XYChart.Data(0,currentBatteryPercentage));
        final XYChart.Data<Long, Double> data = new XYChart.Data<>((long)0.0, currentBatteryPercentage);
        data.setNode(
                new HoveredThresholdNode(currentBatteryPercentage)
        );

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
        HoveredThresholdNode(double value) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(value);

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
        }

        private Label createDataThresholdLabel(double value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

}