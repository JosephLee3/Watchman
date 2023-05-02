package com.kunminx.puremusic.data.record;

import static net.steamcrafted.materialiconlib.MaterialDrawableBuilder.IconValue.JSON;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.kunminx.puremusic.MainActivity;
import com.kunminx.puremusic.domain.event.ConfigValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现录音
 *
 * @author chenmy0709
 * @version V001R001C01B001
 */
public class AudioRecorder {
  //音频输入-麦克风
  private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
  //采用频率
  //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
  //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
  private final static int AUDIO_SAMPLE_RATE = 16000;
  //声道 单声道
  private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  //编码
  private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  // 缓冲区字节大小
  private int bufferSizeInBytes = 0;

  //录音对象
  private AudioRecord audioRecord;

  //录音状态
  private Status status = Status.STATUS_NO_READY;

  //文件名
  private String fileName;

  //录音文件
  private List<String> filesName = new ArrayList<>();


  /**
   * 类级的内部类，也就是静态类的成员式内部类，该内部类的实例与外部类的实例
   * 没有绑定关系，而且只有被调用时才会装载，从而实现了延迟加载
   */
  private static class AudioRecorderHolder {
    /**
     * 静态初始化器，由JVM来保证线程安全
     */
    private static AudioRecorder instance = new AudioRecorder();
  }

  private AudioRecorder() {
  }

  public static AudioRecorder getInstance() {
    return AudioRecorderHolder.instance;
  }

  /**
   * 创建录音对象
   */
  @SuppressLint("MissingPermission")
  public void createAudio(String fileName, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
    // 获得缓冲区字节大小
    bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
      channelConfig, channelConfig);
    audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    this.fileName = fileName;
  }

  /**
   * 创建默认的录音对象
   *
   * @param fileName 文件名
   */
  @SuppressLint("MissingPermission")
  public void createDefaultAudio(String fileName) {
    // 获得缓冲区字节大小
    bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
      AUDIO_CHANNEL, AUDIO_ENCODING);
    audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
    this.fileName = fileName;
    status = Status.STATUS_READY;
  }


  /**
   * 开始录音
   *
   * @param listener 音频流的监听
   */
  public void startRecord(final RecordStreamListener listener) {

    if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
      throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
    }
    if (status == Status.STATUS_START) {
      throw new IllegalStateException("正在录音");
    }
    Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());
    audioRecord.startRecording();

    new Thread(new Runnable() {
      @Override
      public void run() {
        writeDataTOFile(listener);
      }
    }).start();
  }





  /**
   * 暂停录音
   */
  public void pauseRecord() {
    Log.d("AudioRecorder", "===pauseRecord===");
    if (status != Status.STATUS_START) {
      throw new IllegalStateException("没有在录音");
    } else {
      audioRecord.stop();
      status = Status.STATUS_PAUSE;
    }
  }

  /**
   * 停止录音
   */
  public void stopRecord() {
    Log.d("AudioRecorder", "===stopRecord===");
    this.getStatus();
    if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
      throw new IllegalStateException("录音尚未开始");
    } else {
      audioRecord.stop();
      status = Status.STATUS_STOP;
      ConfigValue.AUDIO_FILE_NAME = ConfigValue.RECORDING_AUDIO_FILE_NAME;
      release();
    }
  }



  public void getPcmFiles() {
    Log.d("AudioRecorder", "===getPcmFiles===");
    try {
      List<File> fileList = FileUtil.getPcmFiles();
      Log.d("AudioRecorder", fileList.get(0).getParent());
      for (int i=0; i<fileList.size(); i++) {
        System.out.println(fileList.get(i).getName());
        ConfigValue.AUDIO_FILE_NAME = fileList.get(fileList.size()-1).getName();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  // Get last pcm audio file
  public void getLastPcmFile() {
    Log.d("AudioRecorder", "=== getLastPcmFile ===");
    try {
      List<File> fileList = FileUtil.getPcmFiles();
      String lastFileName = "";
      String tmpName = "";
      for (File file : fileList) {
        tmpName = file.getName();
        int result = tmpName.compareTo(lastFileName);
        if (result > 0) {
          lastFileName = tmpName;
        }
      }
      System.out.println("lastFileName=" + lastFileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  /**
   * 释放资源
   */
  public void release() {
    Log.d("AudioRecorder", "===release===");
    //假如有暂停录音
    try {
      if (filesName.size() > 0) {
        List<String> filePaths = new ArrayList<>();
        for (String fileName : filesName) {
          filePaths.add(FileUtil.getPcmFileAbsolutePath(fileName));
        }
        //清除
        filesName.clear();
        //将多个pcm文件转化为wav文件
//        mergePCMFilesToWAVFile(filePaths);

      } else {
        //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
        //会报空指针 NullPointerException
        // 将单个pcm文件转化为wav文件
        //Log.d("AudioRecorder", "=====makePCMFileToWAVFile======");
        //makePCMFileToWAVFile();
      }
    } catch (IllegalStateException e) {
      throw new IllegalStateException(e.getMessage());
    }

    if (audioRecord != null) {
      audioRecord.release();
      audioRecord = null;
    }

    status = Status.STATUS_NO_READY;
  }

  /**
   * 取消录音
   */
  public void canel() {
    filesName.clear();
    fileName = null;
    if (audioRecord != null) {
      audioRecord.release();
      audioRecord = null;
    }

    status = Status.STATUS_NO_READY;
  }


  /**
   * 将音频信息写入文件
   *
   * @param listener 音频流的监听
   */
  private void writeDataTOFile(RecordStreamListener listener) {
    //
//    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/LinkTestFolder");
//    if (!folder.exists()) {
//      if (folder.mkdirs()) {
//        // Folder created successfully
//        Log.i("LinkTestFolder", "Folder created successfully");
//      } else {
//        // Failed to create folder
//        Log.i("LinkTestFolder", "Folder created Failed");
//      }
//    } else {
//      // Folder already exists
//      Log.i("LinkTestFolder", "Folder already exists");
//    }
    //

    //DIRECTORY_PODCASTS
//    Context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
//    Context.getExternalFilesDir();
    //



    // new一个byte数组用来存一些字节数据，大小为缓冲区大小
    byte[] audiodata = new byte[bufferSizeInBytes];

    FileOutputStream fos = null;
    int readsize = 0;
    try {
      String currentFileName = fileName;
      if (status == Status.STATUS_PAUSE) {
        //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
        currentFileName += filesName.size();

      }
      filesName.add(currentFileName);
//      String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "WatchMan";
      File file = new File(FileUtil.getPcmFileAbsolutePath(currentFileName));
      if (file.exists()) {
        file.delete();
      }
      fos = new FileOutputStream(file);// 建立一个可存取字节的文件
      // RECORDING_AUDIO_FILE_NAME
      ConfigValue.RECORDING_AUDIO_FILE_NAME = currentFileName + ".pcm";
    } catch (IllegalStateException e) {
      Log.e("AudioRecorder", e.getMessage());
      throw new IllegalStateException(e.getMessage());
    } catch (FileNotFoundException e) {
      Log.e("AudioRecorder", e.getMessage());

    }
    //将录音状态设置成正在录音状态
    status = Status.STATUS_START;
    while (status == Status.STATUS_START) {
      readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
      if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
        try {
          fos.write(audiodata);
          if (listener != null) {
            //用于拓展业务
            listener.recordOfByte(audiodata, 0, audiodata.length);
          }
        } catch (IOException e) {
          Log.e("AudioRecorder", e.getMessage());
        }
      }
    }
    try {
      if (fos != null) {
        fos.close();// 关闭写入流
      }
    } catch (IOException e) {
      Log.e("AudioRecorder", e.getMessage());
    }
  }


  public void playPcmFile(String pcmFileName) {
    PlayPCM mPlayPCM = new PlayPCM();
    mPlayPCM.playPcmFile(ConfigValue.APP_AUDIO_PATH, pcmFileName);
  }

  /**
   * 将pcm合并成wav
   *
   * @param filePaths
   */
  private void mergePCMFilesToWAVFile(final List<String> filePaths) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (PcmToWav.mergePCMFilesToWAVFile(filePaths, FileUtil.getWavFileAbsolutePath(fileName))) {
          //操作成功
        } else {
          //操作失败
          Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
          throw new IllegalStateException("mergePCMFilesToWAVFile fail");
        }
        fileName = null;
      }
    }).start();
  }

  /**
   * 将单个pcm文件转化为wav文件
   */
  private void makePCMFileToWAVFile() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (PcmToWav.makePCMFileToWAVFile(FileUtil.getPcmFileAbsolutePath(fileName), FileUtil.getWavFileAbsolutePath(fileName), true)) {
          //操作成功
        } else {
          //操作失败
          Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
          throw new IllegalStateException("makePCMFileToWAVFile fail");
        }
        fileName = null;
      }
    }).start();
  }

  /**
   * 获取录音对象的状态
   *
   * @return
   */
  public Status getStatus() {
    Log.d("AudioRecorder Status:", this.status.name());
    return status;
  }

  /**
   * 获取本次录音文件的个数
   *
   * @return
   */
  public int getPcmFilesCount() {
    return filesName.size();
  }

  /**
   * 录音对象的状态
   */
  public enum Status {
    //未开始
    STATUS_NO_READY,
    //预备
    STATUS_READY,
    //录音
    STATUS_START,
    //暂停
    STATUS_PAUSE,
    //停止
    STATUS_STOP
  }




  ///////////////////////////////////////////////////////////
  /**
   * 将一个pcm文件转化为wav文件
   * @param pcmPath         pcm文件路径
   * @param destinationPath 目标文件路径(wav)
   * @param deletePcmFile   是否删除源文件
   * @return
   */
  public static boolean makePCMFileToWAVFile(String pcmPath, String destinationPath, boolean deletePcmFile) {
    byte buffer[] = null;
    int TOTAL_SIZE = 0;
    File file = new File(pcmPath);
    if (!file.exists()) {
      return false;
    }
    TOTAL_SIZE = (int) file.length();
    // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
    WaveHeader header = new WaveHeader();
    // 长度字段 = 内容的大小（TOTAL_SIZE) +
    // 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
    header.fileLength = TOTAL_SIZE + (44 - 8);
    header.FmtHdrLeth = 16;
    header.BitsPerSample = 16;
    header.Channels = 2;
    header.FormatTag = 0x0001;
    header.SamplesPerSec = 8000;
    header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
    header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
    header.DataHdrLeth = TOTAL_SIZE;

    byte[] h = null;
    try {
      h = header.getHeader();
    } catch (IOException e1) {
      Log.e("PcmToWav", e1.getMessage());
      return false;
    }

    if (h.length != 44) // WAV标准，头部应该是44字节,如果不是44个字节则不进行转换文件
      return false;

    // 先删除目标文件
    File destfile = new File(destinationPath);
    if (destfile.exists())
      destfile.delete();

    // 合成的pcm文件的数据，写到目标文件
    try {

      buffer = new byte[1024 * 4]; // Length of All Files, Total Size
      InputStream inStream = null;
      OutputStream ouStream = null;

      ouStream = new BufferedOutputStream(new FileOutputStream(
        destinationPath));
      ouStream.write(h, 0, h.length);
      inStream = new BufferedInputStream(new FileInputStream(file));
      int size = inStream.read(buffer);
      while (size != -1) {
        ouStream.write(buffer);
        size = inStream.read(buffer);
      }
      inStream.close();
      ouStream.close();
    } catch (FileNotFoundException e) {
      Log.e("PcmToWav", e.getMessage());
      return false;
    } catch (IOException ioe) {
      Log.e("PcmToWav", ioe.getMessage());
      return false;
    }
    if (deletePcmFile) {
      file.delete();
    }
//    Log.i("PcmToWav", "makePCMFileToWAVFile  success!" + new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date()));
    return true;
  }
  ///////////////////////////////////////////////////////////
}
