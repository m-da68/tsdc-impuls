package ru.tsconnect.tsdcimpuls;

import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("preloader_v1.1.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 340);
        stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getResourceAsStream("icon.png"))));
        Rectangle rect = new Rectangle(560,340);
        rect.setArcHeight(16);
        rect.setArcWidth(16);
        scene.setFill(Color.TRANSPARENT);
        scene.getRoot().setClip(rect);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("TSStation Loader");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}