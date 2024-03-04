package com.example.videoeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VideoEditorApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(VideoEditorApplication.class.getResource("fxml/index.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 800, 600);
    stage.setTitle("简易视频编辑工具");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
