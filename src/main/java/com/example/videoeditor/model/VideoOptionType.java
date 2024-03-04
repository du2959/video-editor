package com.example.videoeditor.model;

public enum VideoOptionType {

  COPY(0), // 复制流
  VIDEO_NONE(1), // 消除视频
  CW_ROTATE_90(2), // 顺时针旋转90°
  CCW_ROTATE_90(3), // 逆时针旋转90°
  ROTATE_180(4), // 旋转180°
  CLIP(5), // 截取片段
  ;

  private final int value;

  VideoOptionType(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public static VideoOptionType of(int optionValue) {
    for (VideoOptionType optionType : VideoOptionType.values()) {
      if (optionType.value == optionValue) {
        return optionType;
      }
    }
    return COPY;
  }
}
