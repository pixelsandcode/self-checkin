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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.misc.BigBrotherCameraPreview;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.ImageParameters;
import me.tipi.self_check_in.util.ImageUtility;
import timber.log.Timber;

@SuppressWarnings("deprecation")
public class AvatarFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

  public static final String TAG = PassportFragment.class.getSimpleName();

  private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
  private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

  @Inject Picasso picasso;
  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject Guest guest;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.avatar) ImageView avatarView;
  @Bind(R.id.surface_view) BigBrotherCameraPreview mPreviewView;
  @Bind(R.id.cover_top_view) View topCoverView;
  @Bind(R.id.cover_left_view) View leftCoverView;

  MaterialDialog dialog;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  private int mCameraID;
  private Camera mCamera;
  private SurfaceHolder mSurfaceHolder;

  private boolean mIsSafeToTakePhoto = false;

  private ImageParameters mImageParameters;

  private CameraOrientationListener mOrientationListener;

  private float coverHeight;
  private float coverWidth;
  private float guideWidth;
  private float guideHeight;

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
    SelfCheckInApp.get(context).getSelfCheckInComponent().inject(fragment);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCameraID = getFrontCameraID();
    String mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
    mImageParameters = new ImageParameters();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_avatar, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("OnCreateView");
    if (typeface != null) {
      typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    }

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

        coverHeight = topCoverView.getHeight();
        coverWidth = leftCoverView.getWidth();
        guideHeight = avatarView.getHeight();
        guideWidth = avatarView.getWidth();


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

    if (getActivity() != null) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }

    bus.register(this);
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);

    mOrientationListener.disable();

    // stop the preview
    if (mCamera != null) {
      stopCameraPreview();
      mCamera.release();
      mCamera = null;
    }
  }

  @Override public void onStop() {
    super.onStop();
    handler.removeCallbacks(runnable);
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

  @Override
  public void onPictureTaken(byte[] data, Camera camera) {
    int rotation = getPhotoRotation();

    rotatePicture(rotation, data);
    avatarView.setVisibility(View.VISIBLE);
    mPreviewView.setVisibility(View.GONE);
    setSafeToTakePhoto(true);
  }

  /**
   * Gets photo rotation.
   *
   * @return the photo rotation
   */
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

  /**
   * On launch camera.
   */
  @OnClick({R.id.avatar, R.id.capture})
  public void onLaunchCamera() {
    takePicture();

    tracker.send(new HitBuilders.EventBuilder("Image", "Take avatar").build());
  }

  /**
   * Gets camera.
   *
   * @param cameraID the camera id
   */
  private void getCamera(int cameraID) {
    try {
      mCamera = Camera.open(cameraID);
      mPreviewView.setCamera(mCamera);
    } catch (Exception e) {
      Log.d(TAG, "Can't open camera with id " + cameraID);
      e.printStackTrace();
    }
  }

  /**
   * Restart preview.
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
   * Start camera preview.
   */
  private void startCameraPreview() {
    determineDisplayOrientation();
    setupCamera();

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
   * Stop camera preview.
   */
  private void stopCameraPreview() {
    setSafeToTakePhoto(false);

    // Nulls out callbacks, stops face detection
    mCamera.stopPreview();
  }

  /**
   * Gets front camera id.
   *
   * @return the front camera id
   */
  private int getFrontCameraID() {
    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(i, info);
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        mCameraID = i;
        break;
      }
    }

    return mCameraID;
  }

  /**
   * Continue to identity.
   */
  public void continueToIdentity() {
    if (guest.avatarPath != null && !TextUtils.isEmpty(guest.avatarPath)) {
      Timber.w("Reading path from object before going to check in with path: %s", guest.avatarPath);
      ((SignUpActivity)getActivity()).showDateFragment();
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Please take a selfie!", Snackbar.LENGTH_LONG).show();
    }
  }

  /**
   * Start over.
   */
  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity) getActivity()).reset();
    }
  }

  /**
   * Rotate picture.
   *
   * @param rotation the rotation
   * @param data     the data
   */
  private void rotatePicture(int rotation, byte[] data) {
    Bitmap bitmap = ImageUtility.decodeSampledBitmapFromByte(getActivity(), data);
    Timber.w("Converted camera data to avatar bitmap with width: %s and height: %s", bitmap.getWidth(), bitmap.getHeight());
    if (rotation != 0) {
      Bitmap oldBitmap = bitmap;

      Matrix matrix = new Matrix();
      matrix.postRotate(rotation);

      bitmap = Bitmap.createBitmap(
          oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false
      );

      oldBitmap.recycle();
    }

    int x = Math.round(bitmap.getWidth() - guideWidth) / 2;
    int y = Math.round(bitmap.getHeight() - guideHeight) / 2;
    bitmap = Bitmap.createBitmap(bitmap, x, y, bitmap.getWidth() - x * 2, bitmap.getHeight() - y * 2);
    Timber.w("cropped bitmap to width: %s and height: %s", bitmap.getWidth(), bitmap.getHeight());

    Uri photoUri = ImageUtility.savePassportPicture(getActivity(), bitmap);
    Timber.w("Uri got back from file helper with path: %s", photoUri != null ? photoUri.getPath() : "NO AVATAR FILE PATH!!!!!");
    guest.avatarPath = photoUri.getPath();
    Timber.w("Avatar path saved with path: %s", photoUri.getPath());
    if (photoUri.getPath() != null) {
      picasso.load(photoUri)
          .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
          .into(avatarView);
      continueToIdentity();
    }
  }

  /**
   * Sets camera.
   */
  private void setupCamera() {
    // Never keep a global parameters
    Camera.Parameters parameters = mCamera.getParameters();

    Camera.Size bestPreviewSize = determineBestPreviewSize(parameters);
    Camera.Size bestPictureSize = determineBestPictureSize(parameters);

    parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
    parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


    // Set continuous picture focus, if it's supported
    if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
      parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    // Lock in the changes
    mCamera.setParameters(parameters);
  }

  /**
   * Determine display orientation.
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
   * Determine best preview size camera . size.
   *
   * @param parameters the parameters
   * @return the camera . size
   */
  private Camera.Size determineBestPreviewSize(Camera.Parameters parameters) {
    return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
  }

  /**
   * Determine best picture size camera . size.
   *
   * @param parameters the parameters
   * @return the camera . size
   */
  private Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
    return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
  }

  /**
   * Determine best size camera . size.
   *
   * @param sizes          the sizes
   * @param widthThreshold the width threshold
   * @return the camera . size
   */
  private Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
    Camera.Size bestSize = null;
    Camera.Size size;
    int numOfSizes = sizes.size();
    for (int i = 0; i < numOfSizes; i++) {
      size = sizes.get(i);
      boolean isDesireRatio = (size.width / 4) == (size.height / 3);
      boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

      if (isDesireRatio && isBetterSize) {
        bestSize = size;
      }
    }

    if (bestSize == null) {
      Log.d(TAG, "cannot find the best camera size");
      return sizes.get(sizes.size() - 1);
    }

    return bestSize;
  }

  /**
   * Take picture.
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

  /**
   * Sets safe to take photo.
   *
   * @param isSafeToTakePhoto the is safe to take photo
   */
  private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
    mIsSafeToTakePhoto = isSafeToTakePhoto;
  }

  private static class CameraOrientationListener extends OrientationEventListener {

    private int mCurrentNormalizedOrientation;
    private int mRememberedNormalOrientation;

    /**
     * Instantiates a new Camera orientation listener.
     *
     * @param context the context
     */
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
     * Normalize int.
     *
     * @param degrees the degrees
     * @return the int
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

    /**
     * Remember orientation.
     */
    public void rememberOrientation() {
      mRememberedNormalOrientation = mCurrentNormalizedOrientation;
    }

    /**
     * Gets remembered normal orientation.
     *
     * @return the remembered normal orientation
     */
    public int getRememberedNormalOrientation() {
      rememberOrientation();
      return mRememberedNormalOrientation;
    }
  }
}
