/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

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
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.util.FileHelper;
import timber.log.Timber;

public class AvatarFragment extends Fragment {
  public final static int CAPTURE_IMAGE_REQUEST_CODE = 1034;

  @Inject Picasso picasso;
  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Tracker tracker;
  @Inject Guest guest;

  @Bind(R.id.avatar) ImageView avatarView;

  Uri uriSavedImage;

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

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
      if (resultCode == SignUpActivity.RESULT_OK) {
        try {
          File imageFile = FileHelper.getResizedFile(getActivity(), uriSavedImage,
              Build.VERSION.SDK_INT, 500, 500);
          picasso.load(imageFile).resize(400,400).centerCrop().into(avatarView);
          // Save taken photo path to show later if not signed up
          avatarPath.set(imageFile.getPath());

        } catch (Exception e) {
          Snackbar.make(appContainer.bind(getActivity()), "Picture wasn't taken!", Snackbar.LENGTH_LONG).show();
        }
      }
    }
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(false));
      if (avatarPath.isSet() && avatarPath.get() != null && avatarView != null) {
        picasso.load(new File(avatarPath.get())).resize(400, 400).centerCrop()
            .placeholder(R.drawable.avatar_default).into(avatarView);
      }
    }
  }

  /**
   * On launch camera.
   */
  @OnClick(R.id.captureBtn)
  public void onLaunchCamera() {
    // create Intent to take a picture and return control to the calling application
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

    //folder stuff
    File imagesFolder = new File(Environment.getExternalStorageDirectory(), "GuestAvatars");
    imagesFolder.mkdirs();

    File image = new File(imagesFolder, "Avatar_" + timeStamp + ".jpg");
    uriSavedImage = Uri.fromFile(image);

    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
    // Start the image capture intent to take photo
    startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);

    tracker.send(new HitBuilders.EventBuilder("Image", "Take avatar").build());
  }

  /**
   * Continue to identity.
   */
  @OnClick(R.id.continue_btn)
  public void continueToIdentity() {
    if (avatarPath.isSet()) {
      Timber.v(avatarPath.get());
      guest.time = System.currentTimeMillis();
      bus.post(new PagerChangeEvent(1));
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Please take a selfie first!", Snackbar.LENGTH_LONG).show();
    }
  }
}
