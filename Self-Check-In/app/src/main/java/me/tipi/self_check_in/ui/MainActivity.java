/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject @Named(ApiConstants.PASSPORT) Preference<String> passportPath;
  @Inject @Named(ApiConstants.HOSTEL_NAME) Preference<String> hostelName;
  @Inject @Named(ApiConstants.HOSTEL_KEY) Preference<String> hostelKey;

  @Inject AppContainer appContainer;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  //@Bind(R.id.forgot_password_text) TextView forgotPassView;
  //@Bind(R.id.reset_password_button) Button resetPasswordButton;

  private MaterialDialog loading;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    ButterKnife.bind(this);
    SelfCheckInApp.get(this).inject(this);
    typeface.setTypeface(this, "SF-UI-Text-Regular.otf");
    loading = new MaterialDialog.Builder(this)
        .content("Loading")
        .cancelable(false)
        .progress(true, 0)
        .build();

    if (username.isSet() && password.isSet()) {
      this.login();
    } else {
      showLoginFragment();
    }

    Timber.d("Created");
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    tracker.setScreenName(getResources().getString(R.string.hostel_login));
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    View view = getCurrentFocus();

    int x = (int) ev.getX();
    int y = (int) ev.getY();

    if(view instanceof EditText){
      EditText innerView = (EditText) getCurrentFocus();

      if (ev.getAction() == MotionEvent.ACTION_UP &&
          !getLocationOnScreen(innerView).contains(x, y)) {

        InputMethodManager input = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
        input.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  protected Rect getLocationOnScreen(EditText mEditText) {
    Rect mRect = new Rect();
    int[] location = new int[2];

    mEditText.getLocationOnScreen(location);

    mRect.left = location[0];
    mRect.top = location[1];
    mRect.right = location[0] + mEditText.getWidth();
    mRect.bottom = location[1] + mEditText.getHeight();

    return mRect;
  }

  /**
   * Show login fragment.
   */
  private void showLoginFragment() {
    getFragmentManager().beginTransaction()
        .replace(R.id.container, LoginFragment.newInstance(this)).commit();

  }

  /**
   * Login.
   */
  public void login() {
    loading.show();
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse response, Response response2) {
        loading.dismiss();
        Timber.d("LoggedIn");
        avatarPath.delete();
        passportPath.delete();
        hostelName.set(response.data.name);
        hostelKey.set(response.data.doc_key);
        tracker.send(new HitBuilders.TimingBuilder("Login", "Logged In", System.currentTimeMillis()).build());
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        finish();
      }

      @Override public void failure(RetrofitError error) {
        loading.dismiss();
        showLoginFragment();

        if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
          Snackbar.make(appContainer.bind(MainActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          Snackbar.make(appContainer.bind(MainActivity.this), "Your email/password doesn't match!", Snackbar.LENGTH_LONG).show();
        } else {
          Snackbar.make(appContainer.bind(MainActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_LONG).show();
        }
      }
    });
  }

  public void forgetPassword(View view) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dashboard.tipi.me/#/forgot"));
    startActivity(browserIntent);
  }
}
