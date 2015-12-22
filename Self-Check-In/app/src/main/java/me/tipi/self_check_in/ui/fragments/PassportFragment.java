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
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
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

  @Bind(R.id.passport_photo) ImageView passportView;

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
    if (passportPath != null && passportPath.isSet() && passportPath.get() != null && passportView != null) {
      picasso.load(new File(passportPath.get())).resize(400, 400).centerCrop()
          .placeholder(R.drawable.passport_default).into(passportView);
    }
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
    if (requestCode == CAPTURE_PASSPORT_REQUEST_CODE) {
      if (resultCode == SignUpActivity.RESULT_OK) {
        try {
          File imageFile = FileHelper.getResizedFile(getActivity(), uriSavedPassportImage,
              Build.VERSION.SDK_INT, 500, 500);
          picasso.load(imageFile).resize(400,400).centerCrop().into(passportView);
          // Save taken photo path to show later if not signed up
          passportPath.set(imageFile.getPath());

        } catch (Exception e) {
          Snackbar.make(appContainer.bind(getActivity()), "Picture wasn't taken!", Snackbar.LENGTH_LONG).show();
        }
      }
    }
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      if (passportPath.isSet() && passportPath.get() != null && passportView != null) {
        picasso.load(new File(passportPath.get())).resize(400, 400).centerCrop()
            .placeholder(R.drawable.passport_default).into(passportView);
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
    File imagesFolder = new File(Environment.getExternalStorageDirectory(), "GuestPassports");
    imagesFolder.mkdirs();

    File image = new File(imagesFolder, "scan_" + timeStamp + ".jpg");
    uriSavedPassportImage = Uri.fromFile(image);

    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedPassportImage);
    // Start the image capture intent to take photo
    startActivityForResult(intent, CAPTURE_PASSPORT_REQUEST_CODE);
  }

  /**
   * Continue to identity.
   */
  @OnClick(R.id.continue_btn)
  public void continueToCheckIn() {
    if (passportPath.isSet()) {
      Timber.v(passportPath.get());
      bus.post(new PagerChangeEvent(3));
    } else {
      Snackbar.make(appContainer.bind(getActivity()), "Please Scan your passport first!", Snackbar.LENGTH_LONG).show();
    }
  }

}
