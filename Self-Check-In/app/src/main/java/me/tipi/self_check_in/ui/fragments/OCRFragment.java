package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.detectors.DetectorResult;
import com.microblink.detectors.quad.QuadDetectorResult;
import com.microblink.hardware.SuccessCallback;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.image.Image;
import com.microblink.image.ImageType;
import com.microblink.metadata.DetectionMetadata;
import com.microblink.metadata.ImageMetadata;
import com.microblink.metadata.Metadata;
import com.microblink.metadata.MetadataListener;
import com.microblink.metadata.MetadataSettings;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.util.Log;
import com.microblink.view.CameraAspectMode;
import com.microblink.view.CameraEventsListener;
import com.microblink.view.OnSizeChangedListener;
import com.microblink.view.OrientationAllowedListener;
import com.microblink.view.recognition.RecognizerView;
import com.microblink.view.recognition.ScanResultListener;
import com.microblink.view.viewfinder.quadview.QuadViewManager;
import com.microblink.view.viewfinder.quadview.QuadViewManagerFactory;
import com.microblink.view.viewfinder.quadview.QuadViewPreset;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SecretKey;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.misc.Config;
import me.tipi.self_check_in.util.ImageUtility;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class OCRFragment extends Fragment implements ScanResultListener, CameraEventsListener,
    OnSizeChangedListener, MetadataListener {

  public static final String TAG = OCRFragment.class.getSimpleName();

  @Inject Guest guest;

  @Bind(R.id.rec_view) RecognizerView mRecognizerView;
  @Bind(R.id.scan_btn) ImageButton scanButton;
  @Bind(R.id.scan_hint) TextView scanHintTextView;

  private int mScansDone = 0;
  RecognitionResults results;

  /** Is torch enabled? */
  private boolean mTorchEnabled = false;
  private Handler mHandler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  /** This is BlinkID's built-in helper for built-in view that draws detection location */
  QuadViewManager mQvManager = null;
  /** MediaPlayer will be used for beep sound */
  private MediaPlayer mMediaPlayer = null;

  public OCRFragment() {
    // Required empty public constructor
  }

  public static OCRFragment newInstance(Context context) {
    OCRFragment fragment = new OCRFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_ocr, container, false);
    ButterKnife.bind(this, view);
    guest.passportPath = null;

    RecognitionSettings recognitionSettings = new RecognitionSettings();
    RecognizerSettings[] settArray = Config.getRecognizerSettings();
    // set recognizer settings array that is used to configure recognition
    recognitionSettings.setRecognizerSettingsArray(settArray);
    mRecognizerView.setRecognitionSettings(recognitionSettings);

    MetadataSettings.ImageMetadataSettings ims = new MetadataSettings.ImageMetadataSettings();
    // sets whether image that was used to obtain valid scanning result will be available
    //ims.setSuccessfulScanFrameEnabled(true);
    ims.setDewarpedImageEnabled(true);
    ims.setSuccessfulScanFrameEnabled(true);


    // In order for scanning to work, you must enter a valid licence key. Without licence key,
    // scanning will not work. Licence key is bound the the package name of your app, so when
    // obtaining your licence key from Microblink make sure you give us the correct package name
    // of your app. You can obtain your licence key at http://microblink.com/login or contact us
    // at http://help.microblink.com.
    // Licence key also defines which recognizers are enabled and which are not. Since the licence
    // key validation is performed on image processing thread in native code, all enabled recognizers
    // that are disallowed by licence key will be turned off without any error and information
    // about turning them off will be logged to ADB logcat.
    try {
      mRecognizerView.setLicenseKey(SecretKey.LICENCE_KEY);
    } catch (InvalidLicenceKeyException e) {
      e.printStackTrace();
      Log.e(TAG, "Invalid licence key!");
      Toast.makeText(getActivity(), "Invalid licence key!", Toast.LENGTH_SHORT).show();
    }

    // scan result listener will be notified when scan result gets available
    mRecognizerView.setScanResultListener(this);

    // camera events listener receives events such as when camera preview has started
    // or there was an error while starting the camera
    mRecognizerView.setCameraEventsListener(this);

    // orientation allowed listener is asked if orientation is allowed when device orientation
    // changes - if orientation is allowed, rotatable views will be rotated to that orientation
    mRecognizerView.setOrientationAllowedListener(new OrientationAllowedListener() {
      @Override
      public boolean isOrientationAllowed(Orientation orientation) {
        // allow all orientations
        return true;
      }
    });

    // on size changed listener is notified whenever the size of the view is changed (for example
    // when transforming the view from portrait to landscape or vice versa)
    mRecognizerView.setOnSizeChangedListener(this);

    // define which metadata will be available in MetadataListener (onMetadataAvailable method)
    MetadataSettings metadataSettings = new MetadataSettings();


    // detection metadata should be available in MetadataListener
    // detection metadata are all metadata objects from com.microblink.metadata.detection package
    metadataSettings.setDetectionMetadataAllowed(true);
    metadataSettings.setImageMetadataSettings(ims);

    // set metadata listener and defined metadata settings
    // metadata listener will obtain selected metadata
    mRecognizerView.setMetadataListener(this, metadataSettings);

    // set initial orientation
    mRecognizerView.setInitialOrientation(Orientation.ORIENTATION_PORTRAIT);

    // set camera aspect mode to FILL - this will use the entire surface
    // for camera preview, instead of letterboxing it
    mRecognizerView.setAspectMode(CameraAspectMode.ASPECT_FILL);

    // create scanner (make sure scan settings and listeners were set prior calling create)
    mRecognizerView.create();

    // after scanner is created, you can add your views to it

    // initialize QuadViewManager
    // Use provided factory method from QuadViewManagerFactory that can instantiate the
    // QuadViewManager based on several presets defined in QuadViewPreset enum. Details about
    // each of them can be found in javadoc. This method automatically adds the QuadView as a
    // child of RecognizerView.
    // Here we use preset which sets up quad view in the same style as used in built-in BlinkID ScanCard activity.
    mQvManager= QuadViewManagerFactory.createQuadViewFromPreset(mRecognizerView, QuadViewPreset.DEFAULT_FROM_SCAN_CARD_ACTIVITY);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    // all activity lifecycle events must be passed on to RecognizerView
    if (mRecognizerView != null) {
      mRecognizerView.resume();
    }

    mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.beep);
    mHandler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
  }

  @Override
  public void onStart() {
    super.onStart();
    // all activity lifecycle events must be passed on to RecognizerView
    if(mRecognizerView != null) {
      mRecognizerView.start();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    // all activity lifecycle events must be passed on to RecognizerView
    if(mRecognizerView != null) {
      mRecognizerView.pause();
    }
    if (mMediaPlayer != null) {
      mMediaPlayer = null;
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    // all activity lifecycle events must be passed on to RecognizerView
    if(mRecognizerView != null) {
      mRecognizerView.stop();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // all activity lifecycle events must be passed on to RecognizerView
    if(mRecognizerView != null) {
      mRecognizerView.destroy();
    }
  }

  /**
   * Plays beep sound.
   */
  private void soundNotification() {
    if (mMediaPlayer != null) {
      Log.d(TAG, "Playing beep sound");
      mMediaPlayer.start();
      mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
          if (mMediaPlayer == null) {
            mp.release();
          }
        }
      });
    }
  }

  @Override
  public void onScanningDone(RecognitionResults results) {
    soundNotification();
    mScansDone++;
    mRecognizerView.pauseScanning();

    if(mScansDone >= 1) {


      BaseRecognitionResult[] resArray = null;
      if (results != null) {
        // get array of recognition results
        this.results = results;
        resArray = results.getRecognitionResults();
      }

      if (resArray == null) {
        android.util.Log.e(TAG, "Unable to retrieve recognition data!");
        showScanButton();
      } else {
        if (guest.passportPath != null) {
           Timber.w("Have ocr data and image, going to Details");
          ((SignUpActivity) getActivity()).showIdentityFragment(results);
        } else {
          Timber.w("We don't have ocr success frame yet");
        }
      }
    } else {
      //Toast.makeText(getActivity(), "Scans done: " + mScansDone, Toast.LENGTH_SHORT).show();
      // resume scanning after two seconds
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          mRecognizerView.resumeScanning(true);
        }
      }, 2000);
    }

  }

  private void showScanButton() {
    scanButton.setVisibility(View.VISIBLE);
    scanHintTextView.setVisibility(View.VISIBLE);
    scanButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        guest.passportPath = null;
        ((SignUpActivity)getActivity()).showPassportFragment();
      }
    });
  }

  @Override
  public void onCameraPreviewStarted() {
    // this method is called just after camera preview has started
    mHandler.postDelayed(new Runnable() {
      @Override public void run() {
        showScanButton();
      }
    }, 30000);
    enableTorchButtonIfPossible();
  }

  @Override
  public void onCameraPreviewStopped() {
    // this method is called just after camera preview has stopped
  }

  @Override
  public void onError(Throwable ex) {
    // This method will be called when opening of camera resulted in exception or
    // recognition process encountered an error.
    // The error details will be given in ex parameter.
    com.microblink.util.Log.e(this, ex, "Error");
    handleError();
  }

  @Override public void onCameraPermissionDenied() {

  }

  @SuppressWarnings("deprecation")
  private void handleError() {
    /*AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle(getString(R.string.error));
    alertDialog.setMessage(getString(R.string.errorDesc));

    alertDialog.setButton(getString(R.string.photopayOK), new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (dialog != null) {
          dialog.dismiss();
        }
        setResult(Activity.RESULT_CANCELED, null);
        finish();
      }
    });
    alertDialog.setCancelable(false);
    alertDialog.show();*/
  }

  @Override
  public void onAutofocusFailed() {
    // this method is called if camera cannot perform autofocus
    // this method is called from background (focusing) thread
    // so make sure you post UI actions on UI thread
  }

  @Override
  public void onAutofocusStarted(Rect[] rects) {
    if(rects == null) {
      Log.i(TAG, "Autofocus started with focusing areas being null");
    } else {
      Log.i(TAG, "Autofocus started");
      for (Rect rect : rects) {
        Log.d(TAG, "Focus area: " + rect.toString());
      }
    }
  }

  @Override
  public void onAutofocusStopped(Rect[] rects) {
    if(rects == null) {
      Log.i(TAG, "Autofocus stopped with focusing areas being null");
    } else {
      Log.i(TAG, "Autofocus stopped");
      for (Rect rect : rects) {
        Log.d(TAG, "Focus area: " + rect.toString());
      }
    }
  }

  @Override
  public void onSizeChanged(int width, int height) {

  }

  @Override
  public void onMetadataAvailable(Metadata metadata) {
    // This method will be called when metadata becomes available during recognition process.
    // Here, for every metadata type that is allowed through metadata settings,
    // desired actions can be performed.
    Timber.w("Metadata available");
    // detection metadata contains detection locations
    if (metadata instanceof DetectionMetadata) {
      // detection location is written inside DetectorResult
      DetectorResult detectorResult = ((DetectionMetadata) metadata).getDetectionResult();
      // DetectorResult can be null - this means that detection has failed
      if (detectorResult == null) {
        if (mQvManager != null) {
          // begin quadrilateral animation to its default position
          // (internally displays FAIL status)
          mQvManager.animateQuadToDefaultPosition();
        }
        // when points of interested have been detected (e.g. QR code), this will be returned as PointsDetectorResult
      } else if (detectorResult instanceof QuadDetectorResult) {
        // begin quadrilateral animation to detected quadrilateral
        mQvManager.animateQuadToDetectionPosition((QuadDetectorResult) detectorResult);
      }
    } else if (metadata instanceof ImageMetadata) {
      // obtain image
      // Please note that Image's internal buffers are valid only
      // until this method ends. If you want to save image for later,
      // obtained a cloned image with image.clone().

      if (guest.passportPath == null || TextUtils.isEmpty(guest.passportPath) || this.results == null) {
        Image image = ((ImageMetadata) metadata).getImage();

        if (image != null && image.getImageType() == ImageType.SUCCESSFUL_SCAN) {
          Timber.w("Metadata type is successful frame image, we go to save it");
          image.clone();
          Timber.w("Ocr meta data captured");
          final Bitmap bitmap = image.convertToBitmap();
          Timber.w("Metadata converted to bitmap");
          final Uri photoUri = ImageUtility.savePassportPicture(getActivity(), bitmap);
          Timber.w("OCR Uri got back from file helper with path: %s", photoUri != null ? photoUri.getPath() : "NO OCR FILE PATH!!!!!");
          guest.passportPath = photoUri.getPath();
          Timber.w("Ocr path saved with path: %s", photoUri.getPath());
        }
      } else {
        Timber.w("We have ocr path and it's : %s", guest.passportPath);
      }
      // to convert the image to Bitmap, call image.convertToBitmap()

      // after this line, image gets disposed. If you want to save it
      // for later, you need to clone it with image.clone()
    }
  }

  private void enableTorchButtonIfPossible() {
    if (mRecognizerView.isCameraTorchSupported()) {
      mRecognizerView.setTorchState(!mTorchEnabled, new SuccessCallback() {
        @Override
        public void onOperationDone(final boolean success) {
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (success) {
                mTorchEnabled = !mTorchEnabled;
              }
            }
          });
        }
      });
    }
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
