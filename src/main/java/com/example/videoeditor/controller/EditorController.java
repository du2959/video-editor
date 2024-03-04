package com.example.videoeditor.controller;

import com.example.videoeditor.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EditorController implements Initializable {
  String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
  ExecutorService executor = Executors.newCachedThreadPool();
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  public VBox inputBox;
  public VBox outputBox;
  public Button addFileButton;
  public Label fileCountLabel;
  public Button clearFileButton;
  public ProgressBar loadFileProgress;
  public TableView<AudioVideoFile> fileTable;
  public TableColumn<AudioVideoFile, String> filePathCol, moveCol, removeCol;
  public TableColumn<AudioVideoFile, Long> fileSizeCol, videoDurationCol;
  public TextField outDirText;
  public Button outDirButton;
  public TextField outFileText;
  public ToggleGroup modeRadioGroup;
  public ToggleGroup videoRadioGroup;
  public ToggleGroup audioRadioGroup;
  public RadioButton modeCopyRadio;
  public RadioButton modeMergeRadio;
  public RadioButton modeConcatRadio;
  public VBox rotateBox;
  public RadioButton vnRadio;
  public RadioButton anRadio;
  public RadioButton clipTimeRadio;
  public HBox clipTimeBox;
  public TextField clipTimeStart;
  public TextField clipTimeEnd;
  public TextArea logText;
  public Button startButton;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modeCopyRadio.setTooltip(new Tooltip("复制模式\n将一个源文件导出为一个目标文件"));
    modeMergeRadio.setTooltip(new Tooltip("合并模式\n将一个视频文件和一个音频文件合并为一个目标文件"));
    modeConcatRadio.setTooltip(new Tooltip("拼接模式\n将多个视频文件首尾拼接为一个目标文件"));
    clipTimeEnd.setTooltip(new Tooltip("结束时间必须大于开始时间！\n结束时间允许为00:00:00，表示持续到结尾"));
    writeLogLn("提示：基于FFmpeg和JavaCV工具包实现。");
    writeLogLn("提示：所有操作均为无损（不含解码编码），速度很快，但对文件格式要求严格，格式不一致可能会失败~");
  }

  @FXML
  protected void onAddFileButtonClicked() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("选择多个文件");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("MP4", "*.mp4"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
    );
    List<File> files = fileChooser.showOpenMultipleDialog(addFileButton.getScene().getWindow());
    execLoadFileTask(files);
  }

  @FXML
  protected void onClearFileButtonClicked() {
    fileTable.getItems().clear();
    updateFileCount(0);
    writeLogLn("清空文件列表！");
  }

  @FXML
  protected void onFileDragOver(DragEvent e) {
    if (e.getGestureSource() != fileTable && e.getDragboard().hasFiles()) {
      e.acceptTransferModes(TransferMode.COPY);
    }
    e.consume();
  }

  @FXML
  protected void onFileDragDropped(DragEvent e) {
    Dragboard dragboard = e.getDragboard();
    if (dragboard.hasFiles()) {
      List<File> files = dragboard.getFiles();
      execLoadFileTask(files);
    }
    e.consume();
  }

  @FXML
  protected void onSelectOutDirButtonClicked() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("选择一个文件夹");
    File trgDir = new File(getTrgDir());
    if (trgDir.exists()) {
      directoryChooser.setInitialDirectory(trgDir);
    }
    File outDir = directoryChooser.showDialog(outDirButton.getScene().getWindow());
    if (outDir != null) {
      String outDirAbsolutePath = outDir.getAbsolutePath();
      outDirText.setText(outDirAbsolutePath);
      writeLogLn("已选择文件夹：" + outDir);
    } else {
      writeWarnLn("未选择文件夹！");
    }
  }

  @FXML
  private void onModeChanged() {
    for (Toggle modeToggle : modeRadioGroup.getToggles()) {
      if (modeToggle.isSelected()) {
        int mode = Integer.parseInt(modeToggle.getUserData().toString());
        if (mode == OptionMode.MERGE.getValue() || mode == OptionMode.CONCAT.getValue()) {
          rotateBox.setDisable(true);
          clipTimeRadio.setDisable(true);
          clipTimeBox.setDisable(true);
        } else {
          rotateBox.setDisable(false);
          clipTimeRadio.setDisable(false);
          clipTimeBox.setDisable(false);
        }
        if (mode == OptionMode.MERGE.getValue()) {
          vnRadio.setDisable(true);
          anRadio.setDisable(true);
        } else {
          vnRadio.setDisable(false);
          anRadio.setDisable(false);
        }
        break;
      }
    }
    for (Toggle videoToggle : videoRadioGroup.getToggles()) {
      if (Integer.parseInt(videoToggle.getUserData().toString()) == VideoOptionType.COPY.getValue()) {
        videoToggle.setSelected(true);
        break;
      }
    }
    for (Toggle audioToggle : audioRadioGroup.getToggles()) {
      if (Integer.parseInt(audioToggle.getUserData().toString()) == AudioOptionType.COPY.getValue()) {
        audioToggle.setSelected(true);
        break;
      }
    }
  }

  @FXML
  protected void onStartButtonClicked() {
    List<String> srcFilePathList = getSrcFilePathList();
    String trgFilePath = getTrgFilePath();
    if (srcFilePathList != null && srcFilePathList.size() != 0 && !"".equals(trgFilePath)) {
      writeLogLn("源文件：" + srcFilePathList);
      writeLogLn("目标文件：" + trgFilePath);
      OptionMode optionModeValue = getOptionModeValue();
      final VideoOptionType videoOptionValue = getVideoOptionValue();
      final AudioOptionType audioOptionValue = getAudioOptionValue();
      final AudioVideoOption audioVideoOption = new AudioVideoOption(optionModeValue, videoOptionValue, audioOptionValue);
      writeLogLn("导出选项：" + audioVideoOption);
      File trgDir = new File(getTrgDir());
      if (trgDir.exists() && trgDir.isDirectory()) {
        File trgFile = new File(trgFilePath);
        if (!trgFile.exists()) {
          long startTime = System.currentTimeMillis();
          writeLogLn("正在导出...请稍候...");
          onTaskRunning();
          Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() {
              try {
                switch (optionModeValue) {
                  case COPY -> copyByFfmpeg(srcFilePathList, trgFilePath, videoOptionValue, audioOptionValue);
                  case MERGE -> mergeByFfmpeg(srcFilePathList, trgFilePath);
                  case CONCAT -> concatByFfmpeg(srcFilePathList, trgFilePath, videoOptionValue, audioOptionValue);
                }
              } catch (Exception e) {
                Platform.runLater(() -> writeErrorLn(e.getMessage()));
              }
              return null;
            }
          };
          exportTask.setOnSucceeded(e -> Platform.runLater(() -> {
            writeLogLn("导出已结束，用时 " + (System.currentTimeMillis() - startTime) + " 毫秒！");
            onTaskFinished();
          }));
          executor.submit(exportTask);
        } else {
          writeWarnLn("导出文件已存在！");
        }
      } else {
        writeWarnLn("导出目录不存在！");
      }
    } else {
      writeWarnLn("失败：请检查源文件和导出文件配置项是否合法！");
    }
  }

  private void updateFileProgress(double progress) {
    loadFileProgress.setOpacity(1);
    loadFileProgress.setProgress(progress);
  }

  private void resetFileProgress() {
    loadFileProgress.setProgress(0);
    loadFileProgress.setOpacity(0);
  }

  private void updateFileCount(int n) {
    if (n == 0) {
      fileCountLabel.setText("");
      resetFileProgress();
    } else {
      fileCountLabel.setText("已添加 " + n + " 个文件");
    }
  }

  private void execLoadFileTask(List<File> fileList) {
    List<AudioVideoFile> audioVideoFileList = new ArrayList<>();
    if (fileList != null) {
      onFileAdding();
      Task<Void> loadDataTask = new Task<>() {
        @Override
        protected Void call() {
          int index = 0;
          for (File file : fileList) {
            index++;
            try {
              AudioVideoFile f = toAudioVideoFile(file);
              audioVideoFileList.add(f);
              int finalIndex = index;
              Platform.runLater(() -> updateFileProgress(finalIndex * 1.0 / fileList.size()));
            } catch (Exception e) {
              writeErrorLn(e.getMessage());
            }
          }
          return null;
        }
      };
      loadDataTask.setOnSucceeded(e -> {
        addDataToVideoTable(audioVideoFileList);
        Platform.runLater(() -> {
          writeLogLn("添加文件：" + fileList);
          onFileAdded();
        });
      });
      executor.submit(loadDataTask);
    } else {
      writeWarnLn("未选择文件！");
    }
  }

  private void addDataToVideoTable(List<AudioVideoFile> audioVideoFileList) {
    Iterator<AudioVideoFile> iterator = audioVideoFileList.iterator();
    final ObservableList<AudioVideoFile> items = fileTable.getItems();
    while (iterator.hasNext()) {
      AudioVideoFile v = iterator.next();
      boolean repeat = items.stream().anyMatch(audioVideoFile -> audioVideoFile.getPath().equals(v.getPath()));
      if (repeat) {
        iterator.remove();
      }
    }
    ObservableList<AudioVideoFile> audioVideoFiles = FXCollections.observableArrayList(audioVideoFileList);
    filePathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
    filePathCol.setCellFactory(tableColumn -> new TableCell<>() {
      @Override
      public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(null);
        this.setTooltip(null);
        if (!empty) {
          this.setText(item);
          final int index = this.getIndex();
          AudioVideoFile audioVideoFile = items.get(index);
          String videoCodec = audioVideoFile.getVideoCodec();
          String tip = item + "\n视频编解码器：" + (videoCodec == null ? "无" : videoCodec);
          if (videoCodec != null) {
            tip += "\n分辨率：" + audioVideoFile.getImageWidth() + "×" + audioVideoFile.getImageHeight() +
                    "\n帧率：" + audioVideoFile.getFrameRate();
          }
          String audioCodec = audioVideoFile.getAudioCodec();
          tip += "\n音频编解码器：" + (audioCodec == null ? "无" : audioCodec);
          if (audioCodec != null) {
            tip += "\n声道数：" + audioVideoFile.getAudioChannels() +
                    "\n采样率：" + audioVideoFile.getSampleRate();
          }
          this.setTooltip(new Tooltip(tip));
        }
      }
    });
    fileSizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
    videoDurationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
    moveCol.setCellFactory(tableColumn -> new TableCell<>() {
      @Override
      public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(null);
        this.setGraphic(null);
        if (!empty) {
          HBox hBox = new HBox();
          Button moveUpButton = new Button("↑");
          moveUpButton.setOnMouseClicked(mouseEvent -> {
            final int index = this.getIndex();
            AudioVideoFile audioVideoFile = items.get(index);
            if (index == 0) {
              writeWarnLn("已至最前：" + audioVideoFile.getPath());
            } else {
              AudioVideoFile last = items.get(index - 1);
              items.set(index, last);
              items.set(index - 1, audioVideoFile);
              writeLogLn("上移：" + audioVideoFile.getPath());
            }
          });
          Button moveDownButton = new Button("↓");
          moveDownButton.setOnMouseClicked(mouseEvent -> {
            final int index = this.getIndex();
            AudioVideoFile audioVideoFile = items.get(index);
            if (index == items.size() - 1) {
              writeWarnLn("已至最后：" + audioVideoFile.getPath());
            } else {
              AudioVideoFile next = items.get(index + 1);
              items.set(index, next);
              items.set(index + 1, audioVideoFile);
              writeLogLn("下移：" + audioVideoFile.getPath());
            }
          });
          hBox.getChildren().addAll(moveUpButton, moveDownButton);
          this.setGraphic(hBox);
        }
      }
    });
    removeCol.setCellFactory(tableColumn -> new TableCell<>() {
      @Override
      public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(null);
        this.setGraphic(null);
        if (!empty) {
          Button removeButton = new Button("×");
          this.setGraphic(removeButton);
          removeButton.setOnMouseClicked(mouseEvent -> {
            AudioVideoFile audioVideoFile = this.getTableView().getItems().get(this.getIndex());
            items.remove(audioVideoFile);
            updateFileCount(items.size());
            writeLogLn("移除：" + audioVideoFile.getPath());
          });
        }
      }
    });
    items.addAll(audioVideoFiles);
    updateFileCount(items.size());
  }

  private List<String> getSrcFilePathList() {
    return fileTable.getItems().stream().map(AudioVideoFile::getPath).collect(Collectors.toList());
  }

  private String getTrgDir() {
    return outDirText.getText();
  }

  private String getTrgFilePath() {
    String trgDir = getTrgDir();
    String trgFile = outFileText.getText();
    if ("".equals(trgDir) || "".equals(trgFile) || trgFile.contains(File.separator)) {
      return "";
    }
    if (!trgDir.endsWith(File.separator)) {
      trgDir += File.separator;
    }
    return trgDir + trgFile;
  }

  private OptionMode getOptionModeValue() {
    Toggle selectedToggle = modeRadioGroup.getSelectedToggle();
    Object userData = selectedToggle.getUserData();
    return OptionMode.of(Integer.parseInt(userData.toString()));
  }

  private VideoOptionType getVideoOptionValue() {
    Toggle selectedToggle = videoRadioGroup.getSelectedToggle();
    Object userData = selectedToggle.getUserData();
    return VideoOptionType.of(Integer.parseInt(userData.toString()));
  }

  private AudioOptionType getAudioOptionValue() {
    Toggle selectedToggle = audioRadioGroup.getSelectedToggle();
    Object userData = selectedToggle.getUserData();
    return AudioOptionType.of(Integer.parseInt(userData.toString()));
  }

  private String getClipTimeStart() {
    return clipTimeStart.getText();
  }

  private String getClipTimeEnd() {
    return clipTimeEnd.getText();
  }

  private boolean checkClipTimeFormat(String clipTime) {
    return clipTime.matches("\\d{2}:\\d{2}:\\d{2}");
  }

  private void writeLogLn(String text) {
    writeLn(text, 0);
  }

  private void writeWarnLn(String text) {
    writeLn(text, 1);
  }

  private void writeErrorLn(String text) {
    writeLn(text, 2);
  }

  private void writeLn(String text, int type) {
    Platform.runLater(() -> {
      String logType;
      switch (type) {
        case 2 -> logType = "ERROR";
        case 1 -> logType = "WARN";
        default -> logType = "INFO";
      }
      String currentTime = simpleDateFormat.format(System.currentTimeMillis());
      logText.appendText(String.format("%s %s  %s\n", currentTime, logType, text));
    });
  }

  private void onFileAdding() {
    loadFileProgress.setProgress(0);
    clearFileButton.setDisable(true);
    fileTable.setDisable(true);
    startButton.setDisable(true);
    addFileButton.setDisable(true);
  }

  private void onFileAdded() {
    addFileButton.setDisable(false);
    startButton.setDisable(false);
    fileTable.setDisable(false);
    clearFileButton.setDisable(false);
    loadFileProgress.setProgress(1);
  }

  private void onTaskRunning() {
    inputBox.setDisable(true);
    outputBox.setDisable(true);
    startButton.setDisable(true);
  }

  private void onTaskFinished() {
    startButton.setDisable(false);
    inputBox.setDisable(false);
    outputBox.setDisable(false);
  }

  private AudioVideoFile toAudioVideoFile(File file) throws Exception {
    AudioVideoFile audioVideoFile = new AudioVideoFile();
    FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(file.getAbsolutePath());
    grabber.start();
    audioVideoFile.setPath(file.getAbsolutePath());
    audioVideoFile.setBytes(file.length());
    audioVideoFile.setSeconds(grabber.getLengthInTime() / 1000000.0D);
    audioVideoFile.setVideoCodec(grabber.getVideoCodecName());
    audioVideoFile.setImageWidth(grabber.getImageWidth());
    audioVideoFile.setImageHeight(grabber.getImageHeight());
    audioVideoFile.setFrameRate(grabber.getFrameRate());
    audioVideoFile.setAudioCodec(grabber.getAudioCodecName());
    audioVideoFile.setAudioChannels(grabber.getAudioChannels());
    audioVideoFile.setSampleRate(grabber.getSampleRate());
    grabber.stop();
    return audioVideoFile;
  }

  private void execCmd(List<String> command) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    Process process = pb.start();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        writeLogLn(line);
      }
    }
    process.waitFor();
  }

  private void copyByFfmpeg(List<String> srcFilePathList, String trgFilePath, VideoOptionType videoOption, AudioOptionType audioOption) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(ffmpeg);
    if (srcFilePathList.size() != 1) {
      throw new RuntimeException("错误：复制模式只支持一条视频！");
    }
    String srcFilePath = srcFilePathList.get(0);
    switch (videoOption) {
      case CW_ROTATE_90 -> {
        command.add("-display_rotation:0");
        command.add("270");
      }
      case CCW_ROTATE_90 -> {
        command.add("-display_rotation:0");
        command.add("90");
      }
      case ROTATE_180 -> {
        command.add("-display_rotation:0");
        command.add("180");
      }
    }
    command.add("-i");
    command.add("\"" + srcFilePath + "\"");
    if (videoOption == VideoOptionType.CLIP) {
      String clipTimeStart = getClipTimeStart();
      String clipTimeEnd = getClipTimeEnd();
      if (checkClipTimeFormat(clipTimeStart) && checkClipTimeFormat(clipTimeEnd)) {
        command.add("-ss");
        command.add(clipTimeStart);
        if (!"00:00:00".equals(clipTimeEnd)) {
          command.add("-to");
          command.add(clipTimeEnd);
        }
      } else {
        throw new RuntimeException("错误：截取时间格式不合法！");
      }
    }
    if (videoOption == VideoOptionType.VIDEO_NONE && audioOption == AudioOptionType.AUDIO_NONE) {
      throw new RuntimeException("错误：视频和音频不可同时消除！");
    } else {
      if (videoOption == VideoOptionType.VIDEO_NONE) {
        command.add("-vn");
      } else {
        command.add("-vcodec");
        command.add("copy");
      }
      if (audioOption == AudioOptionType.AUDIO_NONE) {
        command.add("-an");
      } else {
        command.add("-acodec");
        command.add("copy");
      }
    }
    command.add("\"" + trgFilePath + "\"");
    execCmd(command);
  }

  private void mergeByFfmpeg(List<String> srcFilePathList, String trgFilePath) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(ffmpeg);
    if (srcFilePathList.size() != 2) {
      throw new RuntimeException("错误：合并模式只支持导入一条视频和一条音频！");
    }
    command.add("-i");
    command.add("\"" + srcFilePathList.get(0) + "\"");
    command.add("-i");
    command.add("\"" + srcFilePathList.get(1) + "\"");
    command.add("-c:v");
    command.add("copy");
    command.add("-c:a");
    command.add("copy");
    command.add("\"" + trgFilePath + "\"");
    execCmd(command);
  }

  private void concatByFfmpeg(List<String> srcFilePathList, String trgFilePath, VideoOptionType videoOption, AudioOptionType audioOption) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(ffmpeg);
    if (srcFilePathList.size() > 1) {
      command.add("-f");
      command.add("concat");
      command.add("-safe");
      command.add("0");
      String currentDir = System.getProperty("user.dir");
      String tempFilePath = currentDir + File.separator + "videos-temp.txt";
      try {
        File tempFile = File.createTempFile("videos-", ".txt");
        tempFilePath = tempFile.getAbsolutePath();
        FileWriter writer = new FileWriter(tempFile);
        for (String srcFilePath : srcFilePathList) {
          writer.write("file '" + srcFilePath + "'\n");
        }
        writer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      command.add("-i");
      command.add(tempFilePath);
    } else {
      throw new RuntimeException("错误：拼接模式必须导入多个视频文件！");
    }
    if (videoOption == VideoOptionType.VIDEO_NONE && audioOption == AudioOptionType.AUDIO_NONE) {
      throw new RuntimeException("错误：视频和音频不可同时消除！");
    } else {
      if (videoOption == VideoOptionType.VIDEO_NONE) {
        command.add("-vn");
      } else {
        command.add("-vcodec");
        command.add("copy");
      }
      if (audioOption == AudioOptionType.AUDIO_NONE) {
        command.add("-an");
      } else {
        command.add("-acodec");
        command.add("copy");
      }
    }
    command.add("\"" + trgFilePath + "\"");
    execCmd(command);
  }
}
