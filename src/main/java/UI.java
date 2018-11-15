import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UI extends Application implements Runnable {

    private static final Logger log = LogManager.getLogger(UI.class);

    // Setting up the UI Window
    @Override
    public void start(Stage stage) {
        Main.applicationOpen.set(true);
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        stage.setTitle("MetricsCollector Home");

        Label descriptionLabel = new Label("Select one of the following metrics from the drop-down box and click the Submit button:");
        Label javaFXInfo = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);

        String availableMetrics[] = {"Power"};

        ComboBox dropdown = new ComboBox(FXCollections.observableArrayList(availableMetrics));

        Button button = new Button("Submit");
        button.resize(500, 50);

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
}