package com.example.videoeditor.model;

public class AudioVideoFile {

  private String path;
  private long bytes;
  private double seconds;
  private String videoCodec;
  private int imageWidth;
  private int imageHeight;
  private double frameRate;
  private String audioCodec;
  private int audioChannels;
  private int sampleRate;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getBytes() {
    return bytes;
  }

  public void setBytes(long bytes) {
    this.bytes = bytes;
  }

  public double getSeconds() {
    return seconds;
  }

  public void setSeconds(double seconds) {
    this.seconds = seconds;
  }

  public String getVideoCodec() {
    return videoCodec;
  }

  public void setVideoCodec(String videoCodec) {
    this.videoCodec = videoCodec;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public void setImageWidth(int imageWidth) {
    this.imageWidth = imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  public void setImageHeight(int imageHeight) {
    this.imageHeight = imageHeight;
  }

  public double getFrameRate() {
    return frameRate;
  }

  public void setFrameRate(double frameRate) {
    this.frameRate = frameRate;
  }

  public String getAudioCodec() {
    return audioCodec;
  }

  public void setAudioCodec(String audioCodec) {
    this.audioCodec = audioCodec;
  }

  public int getAudioChannels() {
    return audioChannels;
  }

  public void setAudioChannels(int audioChannels) {
    this.audioChannels = audioChannels;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  public String getSize() {
    if (bytes > 1024 * 1024 * 1024) {
      long temp = bytes * 100 / (1024 * 1024 * 1024);
      double gB = temp / 100.0;
      return gB + " GB";
    } else {
      long temp = bytes * 100 / (1024 * 1024);
      double mB = temp / 100.0;
      return mB + " MB";
    }
  }

  public String getDuration() {
    int h = (int) (seconds / 3600);
    int m = (int) ((seconds % 3600) / 60);
    int s = (int) (seconds % 60);
    return String.format("%02d:%02d:%02d", h, m, s);
  }
}
