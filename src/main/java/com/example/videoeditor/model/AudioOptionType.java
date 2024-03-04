package com.example.videoeditor.model;

public enum AudioOptionType {

  COPY(0), // 复制流
  AUDIO_NONE(1), // 消除音频
  ;

  private final int value;

  AudioOptionType(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public static AudioOptionType of(int optionValue) {
    for (AudioOptionType optionType : AudioOptionType.values()) {
      if (optionType.value == optionValue) {
        return optionType;
      }
    }
    return COPY;
  }
}
