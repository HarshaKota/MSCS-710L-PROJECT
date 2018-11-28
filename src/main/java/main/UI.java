package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UI extends Application implements Runnable {

    private static final Logger log = LogManager.getLogger(UI.class);

    // Setting up the main.UI Window
    @Override
    public void start(Stage stage) throws IOException {
        Main.applicationOpen.set(true);
        Parent root = FXMLLoader.load(getClass().getResource("../main.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Metric Collector");
        stage.show();
    }

    // Launching the main.UI Window
    @Override
    public void run() {
        launch();
    }

    // Gracefully closing the application
    @Override
    public void stop() throws Exception {
        Main.applicationOpen.set(false);
        System.out.println("main.UI Closing. Open?:  " + Main.applicationOpen); //Sysout
        super.stop();
    }
}