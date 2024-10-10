package com.sound_collage;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    public static Stage mainStage;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/com/sound_collage/view/main-view.fxml")));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Sound collage");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                System.exit(0);
            }
        });

        mainStage = stage;
    }

    public static void main(String[] args) {
        launch();
    }
}