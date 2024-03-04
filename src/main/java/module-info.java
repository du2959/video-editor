module com.example.videoeditor {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.bytedeco.javacv;
  requires org.bytedeco.ffmpeg;

  exports com.example.videoeditor;
  exports com.example.videoeditor.controller;
  exports com.example.videoeditor.model;

  opens com.example.videoeditor to javafx.fxml;
  opens com.example.videoeditor.controller to javafx.fxml;
}