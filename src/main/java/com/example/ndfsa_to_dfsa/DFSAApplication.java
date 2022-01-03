package com.example.ndfsa_to_dfsa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DFSAApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DFSAApplication.class.getResource("DFSA-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 454, 502);
        stage.setTitle("DFSA");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}