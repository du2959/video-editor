package com.example.videoeditor.model;

public enum OptionMode {

  COPY(0), // 复制
  MERGE(1), // 合并一个视频流和一个音频流
  CONCAT(2), // 首尾拼接多个视频
  ;

  private final int value;

  OptionMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public static OptionMode of(int optionValue) {
    for (OptionMode mode : OptionMode.values()) {
      if (mode.value == optionValue) {
        return mode;
      }
    }
    return COPY;
  }
}
