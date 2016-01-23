/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.ui.MainActivity;
import me.tipi.self_check_in.util.Strings;
import timber.log.Timber;

public class LoginFragment extends Fragment {

  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.email) EditText emailText;
  @Bind(R.id.password) EditText passwordText;
  @Bind(R.id.sub_title) TextView subTitleView;

  /**
   * Instantiates a new Login fragment.
   */
  public LoginFragment() {
    // Required empty public constructor
  }

  /**
   * New instance login fragment.
   *
   * @param context the context
   * @return the login fragment
   */
  public static LoginFragment newInstance(Context context) {
    LoginFragment fragment = new LoginFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_login, container, false);
    ButterKnife.bind(this, rootView);
    typeface.setTypeface(this, getResources().getString(R.string.fonr_regular));
    typeface.setTypeface(subTitleView, getResources().getString(R.string.font_light));

    if (username.isSet() && password.isSet()) {
      emailText.setText(username.get());
      passwordText.setText(password.get());
    }

    Timber.d("onCreateView");
    return rootView;
  }

  @Override public void onPause() {
    super.onPause();
    Timber.d("Paused");
  }

  @Override public void onResume() {
    super.onResume();
    Timber.d("Resumed");
  }

  /**
   * Login.
   */
  @OnClick(R.id.submit_btn)
  public void login() {
    emailText.setError(null);
    passwordText.setError(null);
    boolean cancel = false;
    View focusView = null;

    String enteredEmail = emailText.getText().toString();
    String enteredPassword = passwordText.getText().toString();

    // Check for a valid email address.
    if (TextUtils.isEmpty(enteredEmail)) {
      emailText.setError(getString(R.string.error_field_required));
      focusView = emailText;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailText.setError(getString(R.string.error_invalid_email));
      focusView = emailText;
      cancel = true;
    } else if (TextUtils.isEmpty(enteredPassword)) {
      passwordText.setError(getString(R.string.error_field_required));
      focusView = passwordText;
      cancel = true;
    } else if (enteredPassword.length() < 8) {
      passwordText.setError(getString(R.string.error_incorrect_password));
      focusView = passwordText;
      cancel = true;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    } else {
      username.set(enteredEmail);
      password.set(enteredPassword);
      ((MainActivity) getActivity()).login();
    }
  }
}
