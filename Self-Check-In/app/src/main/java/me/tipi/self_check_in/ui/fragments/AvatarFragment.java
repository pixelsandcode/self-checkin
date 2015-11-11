package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.f2prateek.rx.preferences.Preference;
import com.squareup.otto.Bus;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.util.Helpers;
import timber.log.Timber;

public class AvatarFragment extends Fragment {
  public final String APP_TAG = "Tipi";
  public final static int CAPTURE_IMAGE_REQUEST_CODE = 1034;
  public String photoFileName = "avatar.jpg";

  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;

  @Bind(R.id.avatar) ImageView avatarView;

  public File avatarFile;

  public AvatarFragment() {
    // Required empty public constructor
  }

  public static AvatarFragment newInstance(Context context) {
    AvatarFragment fragment = new AvatarFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_avatar, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("OnCreateView");
    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
      if (resultCode == SignUpActivity.RESULT_OK) {
        Uri takenPhotoUri = getPhotoFileUri();
        // by this point we have the camera photo on disk
        Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
        Bitmap resizedBitmap = Helpers.scaleToFitWidth(takenImage, 500);
        // Load the taken image into a preview
        avatarView.setImageBitmap(resizedBitmap);
      } else { // Result was a failure
        Snackbar.make(appContainer.bind(getActivity()), "Picture wasn't taken!", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  @OnClick(R.id.captureBtn)
  public void onLaunchCamera() {
    // create Intent to take a picture and return control to the calling application
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri()); // set the image file name
    // Start the image capture intent to take photo
    startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
  }

  public Uri getPhotoFileUri() {
    // Only continue if the SD Card is mounted
    if (isExternalStorageAvailable()) {
      // Get safe storage directory for photos
      File mediaStorageDir = new File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_TAG);

      // Create the storage directory if it does not exist
      if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
        Timber.d("failed to create directory");
      }

      // Return the file target for the photo based on filename
      avatarFile = Helpers.makeFileFromPath(mediaStorageDir.getPath());
      Uri uri =  Uri.fromFile(avatarFile);
      avatarPath.set(mediaStorageDir.getPath());
      return uri;
    }

    return null;
  }

  @OnClick(R.id.continue_btn)
  public void continueToIdentity() {
    if (avatarPath.isSet()) {
      bus.post(new PagerChangeEvent(1));
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Avatar is required!", Snackbar.LENGTH_LONG).show();
    }
  }

  private boolean isExternalStorageAvailable() {
    String state = Environment.getExternalStorageState();
    return state.equals(Environment.MEDIA_MOUNTED);
  }
}
