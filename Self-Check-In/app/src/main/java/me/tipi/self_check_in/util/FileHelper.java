/**
 * Copyright (c) 2015-2016 www.oliol.com.
 * Created by Ashkan Hesaraki.
 */

package me.tipi.self_check_in.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class FileHelper {

  /**
   * Gets resized file.
   *
   * @param context    the context
   * @param uri        the uri
   * @param sdkVersion the sdk version
   * @param dstWidth   the dst width
   * @param dstHeight  the dst height
   * @return the resized file
   */
  public static File getResizedFile(final Context context, final Uri uri, int sdkVersion, int dstWidth, int dstHeight) {
    File destFile = null;
    String path;
    if (sdkVersion < 19) {
      path = FileHelper.getPathForUnderKitKat(context, uri);
    } else {
      path = FileHelper.getPathForKitKat(context, uri);
    }
    try {
      int inWidth;
      int inHeight;

      InputStream in = new FileInputStream(path);

      // decode image size (decode metadata only, not the whole image)
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(in, null, options);
      in.close();
      in = null;

      // save width and height
      inWidth = options.outWidth;
      inHeight = options.outHeight;

      // decode full image pre-resized
      in = new FileInputStream(path);
      options = new BitmapFactory.Options();
      // calc rought re-size (this is no exact resize)
      options.inSampleSize = Math.max(inWidth / dstWidth, inHeight / dstHeight);
      // decode full image
      Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

      // calc exact destination size
      Matrix m = new Matrix();
      RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
      RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
      m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
      float[] values = new float[9];
      m.getValues(values);

      // resize bitmap
      Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);

      // save image
      try {
        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File file = new File(context.getCacheDir(), fileName);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        destFile = file;
      } catch (Exception e) {
        Log.e("Image", e.getMessage(), e);
      }
    } catch (IOException e) {
      Log.e("Image", e.getMessage(), e);
    }

    return destFile;
  }


  public static File getResizedFileRotated(final Context context, final Uri uri, int sdkVersion, int dstWidth, int dstHeight) {
    File destFile = null;
    String path;
    if (sdkVersion < 19) {
      path = FileHelper.getPathForUnderKitKat(context, uri);
    } else {
      path = FileHelper.getPathForKitKat(context, uri);
    }
    try {
      int inWidth;
      int inHeight;

      InputStream in = new FileInputStream(path);

      // decode image size (decode metadata only, not the whole image)
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(in, null, options);
      in.close();
      in = null;

      // save width and height
      inWidth = options.outWidth;
      inHeight = options.outHeight;

      // decode full image pre-resized
      in = new FileInputStream(path);
      options = new BitmapFactory.Options();
      // calc rought re-size (this is no exact resize)
      options.inSampleSize = Math.max(inWidth / dstWidth, inHeight / dstHeight);
      // decode full image
      Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

      // calc exact destination size
      Matrix m = new Matrix();
      RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
      RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
      m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
      float[] values = new float[9];
      m.getValues(values);

      // resize bitmap
      Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);


      ExifInterface ei = new ExifInterface(path);
      int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

      Timber.d("ORIENTATION: %s", String.valueOf(orientation));
      switch(orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          rotateImage(resizedBitmap, 90);
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          rotateImage(resizedBitmap, 180);
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          rotateImage(resizedBitmap, 270);
        // etc.
      }

      // save image
      try {
        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File file = new File(context.getCacheDir(), fileName);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        destFile = file;
      } catch (Exception e) {
        Log.e("Image", e.getMessage(), e);
      }
    } catch (IOException e) {
      Log.e("Image", e.getMessage(), e);
    }

    return destFile;
  }
  /**
   * Get the value of the data column for this Uri. This is useful for
   * MediaStore Uris, and other file-based ContentProviders.
   *
   * @param context       The context.
   * @param uri           The Uri to query.
   * @param selection     (Optional) Filter used in the query.
   * @param selectionArgs (Optional) Selection arguments used in the query.
   * @return The value of the _data column, which is typically a file path.
   */
  public static String getDataColumn(Context context, Uri uri, String selection,
                                     String[] selectionArgs) {

    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {
        column
    };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
          null);
      if (cursor != null && cursor.moveToFirst()) {
        final int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(index);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
    return null;
  }


  /**
   * Is external storage document boolean.
   *
   * @param uri The Uri to check.
   * @return Whether the Uri authority is ExternalStorageProvider.
   */
  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  /**
   * Is downloads document boolean.
   *
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  /**
   * Is media document boolean.
   *
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  /**
   * Is google photos uri boolean.
   *
   * @param uri The Uri to check.
   * @return Whether the Uri authority is Google Photos.
   */
  public static boolean isGooglePhotosUri(Uri uri) {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }


  /**
   * Get a file path from a Uri. This will get the the path for Storage Access
   * Framework Documents, as well as the _data field for the MediaStore and
   * other file-based ContentProviders.
   *
   * @param context The context.
   * @param uri     The Uri to query.
   * @return the path for kit kat
   * @author paulburke
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static String getPathForKitKat(final Context context, final Uri uri) {

    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
          return Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        // TODO handle non-primary volumes
      }
      // DownloadsProvider
      else if (isDownloadsDocument(uri)) {

        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(
            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
      }
      // MediaProvider
      else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{
            split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
      }
    }
    // MediaStore (and general)
    else if ("content".equalsIgnoreCase(uri.getScheme())) {

      // Return the remote address
      if (isGooglePhotosUri(uri))
        return uri.getLastPathSegment();

      return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

  /**
   * Gets path for under kit kat.
   *
   * @param context the context
   * @param uri     the uri
   * @return the path for under kit kat
   */
  public static String getPathForUnderKitKat(final Context context, final Uri uri) {
    if (uri == null) {
      return null;
    }
    String[] projection = {MediaStore.Images.Media.DATA};
    Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
    if (cursor != null) {
      int column_index = cursor
          .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    }
    return uri.getPath();
  }

  public static Bitmap rotateImage(Bitmap source, float angle) {
    Bitmap retVal;

    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

    return retVal;
  }
}
