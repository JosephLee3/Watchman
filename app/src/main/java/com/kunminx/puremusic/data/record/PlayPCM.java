package com.kunminx.puremusic.data.record;

import static android.content.ContentValues.TAG;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.kunminx.puremusic.domain.event.ConfigValue;

import java.io.FileInputStream;
import java.io.IOException;


public class PlayPCM {
  // 创建AudioTrack对象
  AudioTrack audioTrack;
  Boolean isPlaying = false;
  Boolean isStop = false;

  // 文件路径名称
  String path = ConfigValue.APP_AUDIO_PATH;



  private Runnable playPCMRecord = new Runnable() {
    @Override
    public void run() {
      // 设置Record参数
      int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
      // 初始化audioTrack
      audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
        AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
      FileInputStream fis = null;
      try {
        audioTrack.play();
        fis = new FileInputStream(path);
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        isPlaying = true;
        while ((len = fis.read(buffer)) != -1 && !isStop) {
//                    Log.d(TAG, "playPCMRecord: len " + len);
          audioTrack.write(buffer, 0, len);
        }

      } catch (Exception e) {
        Log.e(TAG, "playPCMRecord: e : " + e);
      } finally {
        isPlaying = false;
        isStop = false;
        if (audioTrack != null) {
          audioTrack.stop();
          audioTrack = null;
        }
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  };

}
