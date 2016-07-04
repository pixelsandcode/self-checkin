/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import timber.log.Timber;

@SuppressWarnings("deprecation") public class AvatarFragment extends Fragment implements SurfaceHolder.Callback {

  @Inject Picasso picasso;
  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.avatar) ImageView avatarView;
  @Bind(R.id.surface_view) SurfaceView preview;
  @Bind(R.id.continue_btn) Button continueButton;
  @Bind(R.id.capture) ImageButton captureButton;

  private SurfaceHolder previewHolder = null;
  private Camera camera = null;
  private boolean inPreview = false;
  MaterialDialog dialog;
  int cameraId;
  File imageFileFolder = null;
  File imageFileName = null;
  MediaScannerConnection msConn;
  Bitmap bm;

  /**
   * Instantiates a new Avatar fragment.
   */
  public AvatarFragment() {
    // Required empty public constructor
  }

  /**
   * New instance avatar fragment.
   *
   * @param context the context
   * @return the avatar fragment
   */
  public static AvatarFragment newInstance(Context context) {
    AvatarFragment fragment = new AvatarFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_avatar, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("OnCreateView");
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    previewHolder = preview.getHolder();
    previewHolder.addCallback(this);
    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    previewHolder.setFixedSize(getActivity().getWindow().getWindowManager()
        .getDefaultDisplay().getWidth(), getActivity().getWindow().getWindowManager()
        .getDefaultDisplay().getHeight());

    dialog = new MaterialDialog.Builder(getActivity())
        .content("Saving Photo")
        .cancelable(false)
        .progress(true, 0)
        .build();

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();

    // Camera
    for(int i=0; i<Camera.getNumberOfCameras(); i++){
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(i, info);
      if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
        cameraId = i;
        break;
      }
    }

    camera = Camera.open(cameraId);

    bus.register(this);
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    if (inPreview) {
      camera.stopPreview();
    }

    camera.release();
    camera = null;
    inPreview = false;
    super.onPause();
    bus.unregister(this);
    Timber.d("AVATAR : %S", "BUS UNREGISTERED");
  }

  @Override public void surfaceCreated(SurfaceHolder holder) {
    try {
      camera.setPreviewDisplay(previewHolder);
    } catch (Throwable t) {
      Timber.e(t, "PreviewDemo %s", "Exception in setPreviewDisplay()");
    }
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Camera.Parameters parameters = camera.getParameters();
    Camera.Size size = getBestPreviewSize(width, height, parameters);

    if (size != null) {
      parameters.setPreviewSize(size.width, size.height);
      camera.setParameters(parameters);
      camera.startPreview();
      setCameraDisplayOrientation(getActivity(), cameraId, camera);
      inPreview = true;
    }
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {

  }

  Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
    public void onPictureTaken(final byte[] data, final Camera camera) {
      dialog.show();
      onPictureTake(data, camera);
    }
  };

  public void onPictureTake(byte[] data, Camera camera) {

    if (data != null) {
      bm = BitmapFactory.decodeByteArray(data, 0, data.length);
      savePhoto();
    }

    avatarView.setVisibility(View.VISIBLE);
    preview.setVisibility(View.GONE);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      //bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));
      if (avatarPath.isSet() && avatarPath.get() != null && avatarView != null) {
        picasso.load(new File(avatarPath.get())).resize(300, 300).centerCrop()
            .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
            .placeholder(R.drawable.avatar_placeholder).into(avatarView);
      }

      new Handler().postDelayed(new Runnable() {
        @Override public void run() {
          startOver();
        }
      }, ApiConstants.START_OVER_TIME);
    }
  }

  /**
   * On launch camera.
   */
  @OnClick({ R.id.avatar, R.id.capture})
  public void onLaunchCamera() {
    camera.takePicture(null, null, photoCallback);
    inPreview = false;

    tracker.send(new HitBuilders.EventBuilder("Image", "Take avatar").build());
  }

  @OnClick(R.id.continue_btn)
  public void change() {
    continueToIdentity();
  }

  public void continueToIdentity() {
    if (avatarPath.isSet()) {
      Timber.v(avatarPath.get());
      ((SignUpActivity)getActivity()).changePage(6);
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Please take a selfie first!", Snackbar.LENGTH_LONG).show();
    }
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }

  private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
    Camera.Size result = null;
    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
      if (size.width <= width && size.height <= height) {
        if (result == null) {
          result = size;
        } else {
          int resultArea = result.width * result.height;
          int newArea = size.width * size.height;
          if (newArea > resultArea) {
            result = size;
          }
        }
      }
    }
    return (result);
  }

  public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
    android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
    android.hardware.Camera.getCameraInfo(cameraId, info);
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360; // compensate the mirror
    } else { // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    camera.setDisplayOrientation(result);
  }

  public void savePhoto() {
    // Create folder
    imageFileFolder = new File(Environment.getExternalStorageDirectory(), "GuestAvatars");
    imageFileFolder.mkdir();
    FileOutputStream out;
    Calendar c = Calendar.getInstance();
    String date = fromInt(c.get(Calendar.MONTH)) + fromInt(c.get(Calendar.DAY_OF_MONTH)) + fromInt(c.get(Calendar.YEAR)) + fromInt(c.get(Calendar.HOUR_OF_DAY)) + fromInt(c.get(Calendar.MINUTE)) + fromInt(c.get(Calendar.SECOND));
    imageFileName = new File(imageFileFolder, date + ".jpg");

    try {
      out = new FileOutputStream(imageFileName);
      ExifInterface exif=new ExifInterface(imageFileName.toString());

      // Rotate
      Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
      if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
        bm= rotate(bm, 90);
      } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
        bm= rotate(bm, 270);
      } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
        bm= rotate(bm, 180);
      } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
        bm= rotate(bm, 90);
      }


      // Crop
      int previewHeight = preview.getHeight();
      int previewWidth = preview.getWidth();
      int imageHeight = avatarView.getHeight();
      int imageWidth = avatarView.getWidth();

      float ratio = Math.min((float) previewWidth / bm.getWidth(), (float) previewHeight / bm.getHeight());
      int width = Math.round(ratio * bm.getWidth());
      int height = Math.round(ratio * bm.getHeight());

      Bitmap newBitmap = Bitmap.createScaledBitmap(bm, width, height, true);
      int bitmapWidth = newBitmap.getWidth();
      int bitmapHeight = newBitmap.getHeight();
      int x = (bitmapWidth - imageWidth) / 2;
      int y = (bitmapHeight - imageHeight) / 2;
      int destinationWidth = bitmapWidth - (x * 2);
      int destinationHeight = bitmapHeight - (y * 2);


      bm = Bitmap.createBitmap(newBitmap, x, y, destinationWidth, destinationHeight);

      bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();
      scanPhoto(imageFileName.getPath());

    } catch (Exception e) {
      e.printStackTrace();
    }


    dialog.dismiss();
    avatarPath.set(imageFileName.getPath());
    captureButton.setVisibility(View.GONE);
    continueButton.setVisibility(View.VISIBLE);
    continueToIdentity();
  }

  public void scanPhoto(final String imageFileName) {
    msConn = new MediaScannerConnection(getActivity(), new MediaScannerConnection.MediaScannerConnectionClient() {
      public void onMediaScannerConnected() {
        msConn.scanFile(imageFileName, null);
        Timber.i("msClient obj in Photo %s", "connection established");
      }

      public void onScanCompleted(String path, Uri uri) {
        msConn.disconnect();
        Timber.i("msClient obj in Photo %s","scan completed");
      }
    });
    msConn.connect();
  }

  public static Bitmap rotate(Bitmap bitmap, int degree) {
    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    Matrix mtx = new Matrix();
    //       mtx.postRotate(degree);
    mtx.setRotate(degree);

    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
  }

  public String fromInt(int val) {
    return String.valueOf(val);
  }
}
