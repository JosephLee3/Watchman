package com.kunminx.puremusic.data.record;

/**
 * 获取录音的音频流,用于拓展的处理
 */
public interface RecordStreamListener {
  void recordOfByte(byte[] data, int begin, int end);
}
