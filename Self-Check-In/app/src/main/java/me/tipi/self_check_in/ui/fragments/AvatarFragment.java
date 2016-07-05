/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import java.io.IOException;

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
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.FileHelper;
import me.tipi.self_check_in.util.ImageParameters;
import me.tipi.self_check_in.util.ImageUtility;
import timber.log.Timber;

@SuppressWarnings("deprecation")
public class AvatarFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

  public static final String TAG = PassportFragment.class.getSimpleName();

  @Inject Picasso picasso;
  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.avatar) ImageView avatarView;
  @Bind(R.id.surface_view) SurfaceView mPreviewView;
  @Bind(R.id.continue_btn) Button continueButton;
  @Bind(R.id.capture) ImageButton captureButton;

  MaterialDialog dialog;

  private int mCameraID;
  private String mFlashMode;
  private Camera mCamera;
  private SurfaceHolder mSurfaceHolder;

  private boolean mIsSafeToTakePhoto = false;

  private ImageParameters mImageParameters;

  private CameraOrientationListener mOrientationListener;

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

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCameraID = getFrontCameraID();
    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
    mImageParameters = new ImageParameters();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_avatar, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("OnCreateView");
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));

    dialog = new MaterialDialog.Builder(getActivity())
        .content("Saving Photo")
        .cancelable(false)
        .progress(true, 0)
        .build();

    return rootView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mOrientationListener.enable();
    mPreviewView.getHolder().addCallback(AvatarFragment.this);


    mImageParameters.mIsPortrait =
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

    ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        mImageParameters.mPreviewWidth = mPreviewView.getWidth();
        mImageParameters.mPreviewHeight = mPreviewView.getHeight();

        mImageParameters.mCoverWidth = mImageParameters.mCoverHeight
            = mImageParameters.calculateCoverWidthHeight();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
          mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
      }
    });
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    mOrientationListener = new CameraOrientationListener(context);
  }

  @Override public void onResume() {
    super.onResume();

    if (mCamera == null) {
      restartPreview();
    }

    bus.register(this);
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
    Timber.d("AVATAR : %S", "BUS UNREGISTERED");
  }

  @Override public void onStop() {

    mOrientationListener.disable();

    // stop the preview
    if (mCamera != null) {
      stopCameraPreview();
      mCamera.release();
      mCamera = null;
    }

    super.onStop();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    mSurfaceHolder = holder;

    getCamera(mCameraID);
    startCameraPreview();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
  }

  /**
   * A picture has been taken
   * @param data
   * @param camera
   */
  @Override
  public void onPictureTaken(byte[] data, Camera camera) {
    int rotation = getPhotoRotation();

    rotatePicture(rotation, data);
    avatarView.setVisibility(View.VISIBLE);
    mPreviewView.setVisibility(View.GONE);
    setSafeToTakePhoto(true);
  }

  private int getPhotoRotation() {
    int rotation;
    int orientation = mOrientationListener.getRememberedNormalOrientation();
    Camera.CameraInfo info = new Camera.CameraInfo();
    Camera.getCameraInfo(mCameraID, info);

    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      rotation = (info.orientation - orientation + 360) % 360;
    } else {
      rotation = (info.orientation + orientation) % 360;
    }

    return rotation;
  }

  /*public void onPictureTake(byte[] data, Camera camera) {

    if (data != null) {
      bm = BitmapFactory.decodeByteArray(data, 0, data.length);
      savePhoto();
    }

    avatarView.setVisibility(View.VISIBLE);
    preview.setVisibility(View.GONE);
  }*/

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
    takePicture();

    tracker.send(new HitBuilders.EventBuilder("Image", "Take avatar").build());
  }

  private void getCamera(int cameraID) {
    try {
      mCamera = Camera.open(cameraID);
    } catch (Exception e) {
      Log.d(TAG, "Can't open camera with id " + cameraID);
      e.printStackTrace();
    }
  }

  /**
   * Restart the camera preview
   */
  private void restartPreview() {
    if (mCamera != null) {
      stopCameraPreview();
      mCamera.release();
      mCamera = null;
    }

    getCamera(mCameraID);
    startCameraPreview();
  }

  /**
   * Start the camera preview
   */
  private void startCameraPreview() {
    determineDisplayOrientation();

    try {
      mCamera.setPreviewDisplay(mSurfaceHolder);
      mCamera.startPreview();

      setSafeToTakePhoto(true);
    } catch (IOException e) {
      Log.d(TAG, "Can't start camera preview due to IOException " + e);
      e.printStackTrace();
    }
  }

  /**
   * Stop the camera preview
   */
  private void stopCameraPreview() {
    setSafeToTakePhoto(false);

    // Nulls out callbacks, stops face detection
    mCamera.stopPreview();
  }

  private int getFrontCameraID() {
    for(int i = 0; i < Camera.getNumberOfCameras(); i++){
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(i, info);
      if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
        mCameraID = i;
        break;
      }
    }

    return mCameraID;
  }

  private int getBackCameraID() {
    return Camera.CameraInfo.CAMERA_FACING_BACK;
  }

  @OnClick(R.id.continue_btn)
  public void change() {
    continueToIdentity();
  }

  public void continueToIdentity() {
    if (avatarPath.isSet()) {
      Timber.v(avatarPath.get());
      bus.post(new SubmitEvent());
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

  private void rotatePicture(int rotation, byte[] data) {
    Bitmap bitmap = ImageUtility.decodeSampledBitmapFromByte(getActivity(), data);
//        Log.d(TAG, "original bitmap width " + bitmap.getWidth() + " height " + bitmap.getHeight());
    if (rotation != 0) {
      Bitmap oldBitmap = bitmap;

      Matrix matrix = new Matrix();
      matrix.postRotate(rotation);

      bitmap = Bitmap.createBitmap(
          oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false
      );

      oldBitmap.recycle();
    }

    avatarView.setImageBitmap(bitmap);
    Uri photoUri = ImageUtility.savePicture(getActivity(), bitmap);
    File saved = FileHelper.getResizedFile(getActivity(), photoUri, Build.VERSION.SDK_INT, 500, 500);
    avatarPath.set(saved.getPath());
    continueToIdentity();
  }

  /**
   * Determine the current display orientation and rotate the camera preview
   * accordingly
   */
  private void determineDisplayOrientation() {
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(mCameraID, cameraInfo);

    // Clockwise rotation needed to align the window display to the natural position
    int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;

    switch (rotation) {
      case Surface.ROTATION_0: {
        degrees = 0;
        break;
      }
      case Surface.ROTATION_90: {
        degrees = 90;
        break;
      }
      case Surface.ROTATION_180: {
        degrees = 180;
        break;
      }
      case Surface.ROTATION_270: {
        degrees = 270;
        break;
      }
    }

    int displayOrientation;

    // CameraInfo.Orientation is the angle relative to the natural position of the device
    // in clockwise rotation (angle that is rotated clockwise from the natural position)
    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      // Orientation is angle of rotation when facing the camera for
      // the camera image to match the natural orientation of the device
      displayOrientation = (cameraInfo.orientation + degrees) % 360;
      displayOrientation = (360 - displayOrientation) % 360;
    } else {
      displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
    }

    mImageParameters.mDisplayOrientation = displayOrientation;
    mImageParameters.mLayoutOrientation = degrees;

    mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
  }

  /**
   * Take a picture
   */
  private void takePicture() {

    if (mIsSafeToTakePhoto) {
      setSafeToTakePhoto(false);

      mOrientationListener.rememberOrientation();

      // Shutter callback occurs after the image is captured. This can
      // be used to trigger a sound to let the user know that image is taken
      Camera.ShutterCallback shutterCallback = null;

      // Raw callback occurs when the raw image data is available
      Camera.PictureCallback raw = null;

      // postView callback occurs when a scaled, fully processed
      // postView image is available.
      Camera.PictureCallback postView = null;

      // jpeg callback occurs when the compressed image is available
      mCamera.takePicture(shutterCallback, raw, postView, this);
    }
  }

  private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
    mIsSafeToTakePhoto = isSafeToTakePhoto;
  }

  /**
   * When orientation changes, onOrientationChanged(int) of the listener will be called
   */
  private static class CameraOrientationListener extends OrientationEventListener {

    private int mCurrentNormalizedOrientation;
    private int mRememberedNormalOrientation;

    public CameraOrientationListener(Context context) {
      super(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onOrientationChanged(int orientation) {
      if (orientation != ORIENTATION_UNKNOWN) {
        mCurrentNormalizedOrientation = normalize(orientation);
      }
    }

    /**
     * @param degrees Amount of clockwise rotation from the device's natural position
     * @return Normalized degrees to just 0, 90, 180, 270
     */
    private int normalize(int degrees) {
      if (degrees > 315 || degrees <= 45) {
        return 0;
      }

      if (degrees > 45 && degrees <= 135) {
        return 90;
      }

      if (degrees > 135 && degrees <= 225) {
        return 180;
      }

      if (degrees > 225 && degrees <= 315) {
        return 270;
      }

      throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
    }

    public void rememberOrientation() {
      mRememberedNormalOrientation = mCurrentNormalizedOrientation;
    }

    public int getRememberedNormalOrientation() {
      rememberOrientation();
      return mRememberedNormalOrientation;
    }
  }
}
