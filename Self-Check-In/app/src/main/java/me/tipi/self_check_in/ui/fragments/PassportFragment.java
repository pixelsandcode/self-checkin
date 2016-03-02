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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
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
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.util.FileHelper;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class PassportFragment extends Fragment {
  public final static int CAPTURE_PASSPORT_REQUEST_CODE = 1035;

  @Inject Picasso picasso;
  @Inject AppContainer appContainer;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.PASSPORT) Preference<String> passportPath;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.scan) ImageView passportView;
  @Bind(R.id.title) TextView titleView;
  @Bind(R.id.avatar_hint) TextView hintView;
  @Bind(R.id.continue_btn) Button continueButton;

  Uri uriSavedPassportImage;

  /**
   * Instantiates a new Passport fragment.
   */
  public PassportFragment() {
    // Required empty public constructor
  }

  /**
   * New instance passport fragment.
   *
   * @param context the context
   * @return the passport fragment
   */
  public static PassportFragment newInstance(Context context) {
    PassportFragment fragment = new PassportFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_passport, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("OnCreateView");
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    setPassportImage();
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
    if (requestCode == CAPTURE_PASSPORT_REQUEST_CODE) {
      if (resultCode == SignUpActivity.RESULT_OK) {
        try {
          File imageFile = FileHelper.getResizedFile(getActivity(), uriSavedPassportImage,
              Build.VERSION.SDK_INT, 500, 500);
          picasso.load(imageFile).resize(400, 200).centerCrop()
              .into(passportView);
          // Save taken photo path to show later if not signed up
          passportPath.set(imageFile.getPath());
          titleView.setText(getResources().getString(R.string.scan_success_title));
          hintView.setText("Tap on image to rescan\n passport");
          continueButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
          titleView.setText(getResources().getString(R.string.avatar_fail_titel));
          picasso.load(R.drawable.fail_photo).into(passportView);
        }
      }
    }
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));
      setPassportImage();
    }
  }

  /**
   * On launch camera.
   */
  @OnClick(R.id.scan)
  public void onLaunchCamera() {
    // create Intent to take a picture and return control to the calling application
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

    //folder stuff
    File imagesFolder = new File(Environment.getExternalStorageDirectory(), "GuestPassports");
    imagesFolder.mkdirs();

    File image = new File(imagesFolder, "scan_" + timeStamp + ".jpg");
    uriSavedPassportImage = Uri.fromFile(image);

    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedPassportImage);
    // Start the image capture intent to take photo
    startActivityForResult(intent, CAPTURE_PASSPORT_REQUEST_CODE);
    tracker.send(new HitBuilders.EventBuilder("Image", "Take passport").build());
  }

  /**
   * Continue to identity.
   */
  @OnClick(R.id.continue_btn)
  public void continueToCheckIn() {
    if (passportPath.isSet()) {
      Timber.v(passportPath.get());
      bus.post(new SubmitEvent());
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Please Scan your passport first!", Snackbar.LENGTH_LONG).show();
    }
  }

  /**
   * Sets passport image.
   */
  private void setPassportImage() {
    if (passportPath != null && passportPath.isSet() && passportPath.get() != null && passportView != null) {
      picasso.load(new File(passportPath.get())).resize(400, 200).centerCrop()
          .placeholder(R.drawable.avatar_button).into(passportView);
    }
  }

}
