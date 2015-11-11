package me.tipi.self_check_in.util;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Date;

public class Helpers {

  public static String fileName = "avatar.jpg";
    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
      float factor = width / (float) b.getWidth();
      return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
      float factor = height / (float) b.getHeight();
      return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    public static File makeFileFromPath(String path) {
      return new File(path + File.separator + new Date().getTime() + File.separator + fileName);
    }
}
