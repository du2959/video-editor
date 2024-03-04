package com.example.videoeditor.model;

public class AudioVideoOption {

  private OptionMode mode;
  private VideoOptionType videoOption;
  private AudioOptionType audioOption;

  public AudioVideoOption(OptionMode mode, VideoOptionType videoOption, AudioOptionType audioOption) {
    this.mode = mode;
    this.videoOption = videoOption;
    this.audioOption = audioOption;
  }

  public OptionMode getMode() {
    return mode;
  }

  public void setMode(OptionMode mode) {
    this.mode = mode;
  }

  public VideoOptionType getVideoOption() {
    return videoOption;
  }

  public void setVideoOption(VideoOptionType videoOption) {
    this.videoOption = videoOption;
  }

  public AudioOptionType getAudioOption() {
    return audioOption;
  }

  public void setAudioOption(AudioOptionType audioOption) {
    this.audioOption = audioOption;
  }

  @Override
  public String toString() {
    return "AudioVideoOption{" +
            "mode=" + mode +
            ", videoOption=" + videoOption +
            ", audioOption=" + audioOption +
            '}';
  }
}
