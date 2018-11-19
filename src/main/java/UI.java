import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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

import java.sql.*;

public class UI extends Application implements Runnable {

    private static final Logger log = LogManager.getLogger(UI.class);
    private Connection mainConnection = null;

    // Setting up the UI Window
    @Override
    public void start(Stage stage) throws Exception {
        setConnection();
        Main.applicationOpen.set(true);
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        // Create the database
        stage.setTitle("MetricsCollector Home");

        Label descriptionLabel = new Label("Select one of the following metrics from the drop-down box and click the Submit button:");
        Label javaFXInfo = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);

        String availableMetrics[] = {"Power"};

        final ComboBox dropdown = new ComboBox(FXCollections.observableArrayList(availableMetrics));

        Button button = new Button("Submit");
        button.resize(500, 50);

        EventHandler<MouseEvent> clickSubmitEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (dropdown.getValue().equals("Power")) {
                        Stage powerStage = new Stage();
                        updatePowerTableWindow(mainConnection, powerStage);
                    }
                } catch (Exception e){
                    log.warn("No Metric selected");
                }
            }
        };

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
//        updatePowerTableWindow(mainConnection, );
    }

    // Gracefully closing the application
    @Override
    public void stop() throws Exception {
        Main.applicationOpen.set(false);
        System.out.println("UI Closing. Open?:  " + Main.applicationOpen); //Sysout
        super.stop();
        System.out.println("stop");

    }

    void updatePowerTableWindow(Connection connection, Stage powerStage) throws SQLException {
        long currentTimeStamp = System.currentTimeMillis();
        String powerQuery = "SELECT * FROM POWER WHERE (TIMESTAMP <= " + currentTimeStamp + ") AND (TIMESTAMP >= " + (currentTimeStamp - 6000) + ")";
        System.out.println(powerQuery);
        Statement powerTableStatement = connection.createStatement();
        ResultSet powerValues = powerTableStatement.executeQuery(powerQuery);

        NumberAxis xAxis = new NumberAxis("X-Axis", 0d, 60d, 5);
        xAxis.setLabel("Time in Seconds");

        NumberAxis yAxis = new NumberAxis("Y-Axis", 0d, 100d, 5);

        yAxis.setLabel("Battery Percentage");

        LineChart lineChart = new LineChart(xAxis, yAxis);

        XYChart.Series dataSeries1 = new XYChart.Series();
        dataSeries1.setName("Recent datapoints");

        while(powerValues.next()){
            long timeStamp = powerValues.getLong("TIMESTAMP");
            timeStamp = (System.currentTimeMillis() - timeStamp)/100;
            System.out.println(timeStamp);
            double batteryPercentage = powerValues.getDouble("BATTERYPERCENTAGE");
            System.out.println(batteryPercentage);
            dataSeries1.getData().add(new XYChart.Data( timeStamp, batteryPercentage));
        }

        lineChart.getData().add(dataSeries1);
        lineChart.setPrefSize(800, 600);
        VBox vbox = new VBox(lineChart);

        Pane javaFXInfoPane = new Pane(vbox);
        Scene homeMenuScene = new Scene(javaFXInfoPane, 800, 600);

        powerStage.setScene(homeMenuScene);
        powerStage.show();
        powerTableStatement.close();

    }

    void setConnection() throws Exception {
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
}