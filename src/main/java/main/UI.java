package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class launches the main UI of the program
 * and ensures to notify the main program of its status
 * by updating the applicationOpen
 */
public class UI extends Application implements Runnable {

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
        super.stop();
    }
}