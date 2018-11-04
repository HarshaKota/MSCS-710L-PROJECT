import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                "This is the value of the Application Status " + Main.applicationOpen);
        Scene scene = new Scene(l, 640, 480);
        stage.setScene(scene);
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