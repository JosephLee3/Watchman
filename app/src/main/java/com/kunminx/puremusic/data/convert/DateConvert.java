package com.kunminx.puremusic.data.convert;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConvert {


  /**
   * pcmNameToDateTimeStr
   *
   * @param dateTimeStr: 20230504_081437.pcm
   * @return
   */
  public String pcmNameToDateTimeStr(String dateTimeStr) {
    String dateStr = dateTimeStr.substring(0, 8);
    String timeStr = dateTimeStr.substring(9, 15);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
    Date date = null;
    try {
      date = dateFormat.parse(dateStr + " " + timeStr);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    //2011 Mar21 11:37
//    SimpleDateFormat showDateFormat = new SimpleDateFormat("yyyy MMMd HH:mm");
    //Mar21 11:37
      SimpleDateFormat showDateFormat = new SimpleDateFormat("MMMd HH:mm");
//    Log.d("pcmNameToDateTimeStr:", "=== testFile ===");
    Log.d("pcmNameToDateTimeStr:", showDateFormat.format(date));
    return showDateFormat.format(date);
  }
}
