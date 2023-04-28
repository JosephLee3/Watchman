/*
 * Copyright 2018-present KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kunminx.puremusic.data.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunminx.architecture.data.response.DataResult;
import com.kunminx.architecture.data.response.ResponseStatus;
import com.kunminx.architecture.data.response.ResultSource;
import com.kunminx.architecture.utils.Utils;
import com.kunminx.puremusic.R;
import com.kunminx.puremusic.data.api.APIs;
import com.kunminx.puremusic.data.api.AccountService;
import com.kunminx.puremusic.data.bean.DownloadState;
import com.kunminx.puremusic.data.bean.LibraryInfo;
import com.kunminx.puremusic.data.bean.TestAlbum;
import com.kunminx.puremusic.data.bean.User;
import com.kunminx.puremusic.data.record.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create by KunMinX at 19/10/29
 */
public class DataRepository {

  private static final DataRepository S_REQUEST_MANAGER = new DataRepository();

  private DataRepository() {
  }

  public static DataRepository getInstance() {
    return S_REQUEST_MANAGER;
  }

  private final Retrofit retrofit;

  {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(8, TimeUnit.SECONDS)
      .readTimeout(8, TimeUnit.SECONDS)
      .writeTimeout(8, TimeUnit.SECONDS)
      .addInterceptor(logging)
      .build();
    retrofit = new Retrofit.Builder()
      .baseUrl(APIs.BASE_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build();
  }



  /**
   * getAudioList
   *
   * Audio Name: DateTime Timestamp, location X Y,
   * 14506545456545546_FD544505545454545_45454655454
   * 14506545456545546_null
   *
   * Photo:Current Photo FRONT CAM, photo name: one same
   * @param result
   */
  public void getAudioList(DataResult.Result<TestAlbum> result) {
    List<File> pcmFileList = null;

    try {
      pcmFileList = FileUtil.getPcmFiles();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // artistItem
    TestAlbum.TestArtist artistItem = new TestAlbum.TestArtist();
    artistItem.setName("Angle");

    // musicList
    List<TestAlbum.TestMusic> musicList = new ArrayList<>();
    TestAlbum.TestMusic musicItem;
//    // musicItem: 1
//    musicItem.setMusicId("001");
//    musicItem.setTitle("Tomorrow");
//    musicItem.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-570ed96eb055ef17.png");
//    musicItem.setUrl("bensound-sunny.mp3");
//    musicItem.setArtist(artistItem);
//    musicList.add(musicItem);
//    // musicItem: 2
//    musicItem = new TestAlbum.TestMusic();
//    musicItem.setMusicId("002");
//    musicItem.setTitle("Sunny");
//    musicItem.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-8a7d311f2a758d4c.png");
//    musicItem.setUrl("bensound-sunny.mp3");
//    musicItem.setArtist(artistItem);
//    musicList.add(musicItem);
//    // musicItem: 3
//    musicItem = new TestAlbum.TestMusic();
//    musicItem.setMusicId("003");
//    musicItem.setTitle("Energy");
//    musicItem.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-9f034d4886c8fe77.png");
//    musicItem.setUrl("bensound-sunny.mp3");
//    musicItem.setArtist(artistItem);
//    musicList.add(musicItem);


    // musicItem: 4 - 0
    musicItem = new TestAlbum.TestMusic();
    musicItem.setMusicId("004");
    musicItem.setTitle("Studio");
    musicItem.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-9f034d4886c8fe77.png");
    musicItem.setUrl("bensound-sunny.mp3");
    musicItem.setArtist(artistItem);
    musicList.add(musicItem);

    // pcmFile: 4
    if (pcmFileList != null) {
      for (int i=0; i<pcmFileList.size(); i++) {
        musicItem = new TestAlbum.TestMusic();
        musicItem.setMusicId("00" + (i+4));
        musicItem.setTitle(pcmFileList.get(0).getName());
        musicItem.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-9f034d4886c8fe77.png");
        musicItem.setUrl("bensound-sunny.mp3");
        musicItem.setArtist(artistItem);
        musicList.add(musicItem);


        System.out.println(pcmFileList.get(0).getName());
      }
    }


    // TestAlbum
    TestAlbum mTestAlbum = new TestAlbum();
    mTestAlbum.setAlbumId("001");
    mTestAlbum.setTitle("Cute");
    mTestAlbum.setSummary("BenSound");
    mTestAlbum.setArtist(artistItem); // 灌入ArtistItem
    mTestAlbum.setMusics(musicList); // MusicItem
    mTestAlbum.setCoverImg("https://upload-images.jianshu.io/upload_images/57036-570ed96eb055ef17.png");


    result.onResult(new DataResult<>(mTestAlbum, new ResponseStatus()));
  }



  public void getFreeMusic(DataResult.Result<TestAlbum> result) {

    Gson gson = new Gson();
    Type type = new TypeToken<TestAlbum>() {
    }.getType();
    TestAlbum testAlbum = gson.fromJson(Utils.getApp().getString(R.string.free_music_json), type);

    result.onResult(new DataResult<>(testAlbum, new ResponseStatus()));
  }

  public void getLibraryInfo(DataResult.Result<List<LibraryInfo>> result) {
    Gson gson = new Gson();
    Type type = new TypeToken<List<LibraryInfo>>() {
    }.getType();
    List<LibraryInfo> list = gson.fromJson(Utils.getApp().getString(R.string.library_json), type);

    result.onResult(new DataResult<>(list, new ResponseStatus()));
  }

  @SuppressLint("CheckResult")
  public void downloadFile(DataResult.Result<DownloadState> result) {
    final DownloadState[] originState = {new DownloadState()};
    Observable.interval(100, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(aLong -> {
        DownloadState newState = new DownloadState();
        if (originState[0].isForgive || originState[0].progress == 100) {
          return;
        }

        if (originState[0].progress < 100) {
          newState = new DownloadState(false, originState[0].progress + 1, null);
          originState[0] = newState;
          Log.d("---", "下载进度 " + originState[0].progress + "%");
        }

        result.onResult(new DataResult<>(newState, new ResponseStatus()));
        Log.d("---", "回推状态");
      });
  }

  private Call<String> mUserCall;

  public void login(User user, DataResult.Result<String> result) {
    mUserCall = retrofit.create(AccountService.class).login(user.getName(), user.getPassword());
    mUserCall.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
        ResponseStatus responseStatus = new ResponseStatus(
          String.valueOf(response.code()), response.isSuccessful(), ResultSource.NETWORK);
        result.onResult(new DataResult<>(response.body(), responseStatus));
        mUserCall = null;
      }

      @Override
      public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
        result.onResult(new DataResult<>(null,
          new ResponseStatus(t.getMessage(), false, ResultSource.NETWORK)));
        mUserCall = null;
      }
    });
  }

  public void cancelLogin() {
    if (mUserCall != null && !mUserCall.isCanceled()) {
      mUserCall.cancel();
      mUserCall = null;
    }
  }

}
