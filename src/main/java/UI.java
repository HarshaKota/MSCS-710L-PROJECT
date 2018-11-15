import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class UI extends Application implements Runnable {

    private static final Logger log = LogManager.getLogger(UI.class);
    private Connection connection = null;


    // Setting up the UI Window
    @Override
    public void start(Stage stage) throws Exception {
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
                    Stage powerStage = new Stage();
                    if (dropdown.getValue().equals("Power")) {
                        createPowerWindow(powerStage);
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

    public void createPowerWindow(Stage stage) throws Exception {
        String databaseUrl = "jdbc:sqlite:MetricCollector.db";
        String databaseClassName = "org.sqlite.JDBC";
        Database database = new Database();
        database.establishDatabaseConnection(databaseClassName, databaseUrl);
        long currentTimeStamp = System.currentTimeMillis();
        String powerQuery = "SELECT * FROM POWER WHERE (TIMESTAMP <= " + currentTimeStamp + ") AND (TIMESTAMP >= " + (currentTimeStamp - 6000) + ")";
        System.out.println(powerQuery);
        Statement powerTableStatement = connection.createStatement();
        ResultSet powerValues = powerTableStatement.executeQuery(powerQuery);
        while(powerValues.next()){
            System.out.println(powerValues.getLong("TIMESTAMP"));
        }
        powerTableStatement.close();


        Label javaFXInfo = new Label("Hello, JavaFX " + ", running on Java " + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);
        Pane javaFXInfoPane = new Pane(javaFXInfo);
        Scene homeMenuScene = new Scene(javaFXInfoPane, 800, 600);

        stage.setScene(homeMenuScene);
        stage.show();
    }
}