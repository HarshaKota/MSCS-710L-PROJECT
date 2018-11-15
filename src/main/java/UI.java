import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UI extends Application implements Runnable {

    private static final Logger log = LogManager.getLogger(UI.class);

    // Setting up the UI Window
    @Override
    public void start(Stage stage) {
        Main.applicationOpen.set(true);

        stage.setTitle("MetricsCollector Home");
        Label descriptionLabel = new Label("Select one of the following metrics and click the Submit button");
        String availableMetrics[] = {"Power"};
        ComboBox dropdown = new ComboBox(FXCollections.observableArrayList(availableMetrics));
        dropdown.resize(600, 25);
        StackPane rootPane = new StackPane();
        dropdown.setTranslateX(0);

        Pane dropDownPane = new Pane(dropdown);
        Pane labelPane = new Pane(descriptionLabel);
        labelPane.setTranslateX(50);
        labelPane.setTranslateY(250);
        dropDownPane.setTranslateY(300);
        dropDownPane.setTranslateX(50);

        rootPane.getChildren().addAll(labelPane, dropDownPane);
        Scene homeMenuScene = new Scene(rootPane, 800, 600);

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label javaFXInfo = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);
//        Scene scene = new Scene(javaFXInfo, 640, 480);
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