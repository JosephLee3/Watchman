package com.kunminx.puremusic.data.record;

import static android.content.ContentValues.TAG;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;


import java.io.FileInputStream;
import java.io.IOException;


public class PlayPCM {
  // 创建AudioTrack对象
  AudioTrack audioTrack;
  Boolean isPlaying = false;
  Boolean isStop = false;
  // 当前播放的pcm文件路径
  String pcmFilePath = "";
  // 当前播放的pcm文件名
  String pcmFileName = ""; // 20230417_115620.pcm
  String pcmUrl = ""; //



  public void playPcmFile(String pcmUrl) {
    this.pcmUrl = pcmUrl;
    try {
      Runnable playPCMRecord = this.playPCMRecord;
      playPCMRecord.run();
    } catch (NullPointerException e) {
      Log.e("Exception", e.getMessage());
//      e.printStackTrace();
    } catch (Exception e) {
      Log.e("Exception", e.getMessage());
//      e.printStackTrace();
    }
  }



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
        fis = new FileInputStream(pcmUrl);
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        isPlaying = true;
        int lengthTotal = 0;
        while ((len = fis.read(buffer)) != -1 && !isStop) {
          audioTrack.write(buffer, 0, len);
          lengthTotal = len;
        }
        Log.d(TAG, "PlayPcm : lengthTotal=" + lengthTotal);
        Log.i("PlayPcm : ", "-------------------------------------");
        Log.i("PlayPcm : ", "Pcm File is Playing.");
        Log.i("PlayPcm : ", "pcmUrl: " + pcmUrl);
        Log.i("PlayPcm : ", "-------------------------------------");

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
