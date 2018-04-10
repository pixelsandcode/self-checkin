/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences2.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.util.Locale;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Named;
import me.tipi.self_check_in.KioskService;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.LanguagePreference;
import me.tipi.self_check_in.data.PrinterPreference;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AppCallback;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject NetworkRequestManager networkRequestManager;
  @Inject LanguagePreference languagePreference;
  @Inject PrinterPreference printerPreference;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject @Named(ApiConstants.HOSTEL_NAME) Preference<String> hostelName;
  @Inject @Named(ApiConstants.HOSTEL_KEY) Preference<String> hostelKey;
  @Inject @Named(ApiConstants.KIOSK_NAME) Preference<String> kioskNamePref;

  @Inject AppContainer appContainer;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  private MaterialDialog loading;
  private static final int PERMISSION_REQUEST_CODE = 8000;
  private static final int CAMERA_GALLERY_PERMISSIONS_REQUEST = 9000;

  long oneMinute = 60000;
  long period = 5 * oneMinute;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).getSelfCheckInComponent().inject(this);
    setLanguage();
    ButterKnife.bind(this);
    typeface.setTypeface(this, "SF-UI-Text-Regular.otf");
    printerPreference.set(false);
    loading = new MaterialDialog.Builder(this).content("Loading")
        .cancelable(false)
        .progress(true, 0)
        .build();

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.edit().putBoolean(KioskService.PREF_KIOSK_MODE, false).apply();
    Timber.w("KIOSK mode is OFF");

    if (!checkWriteStoragePermission()) {
      requestWriteStoragePermission();
    }

    if (TextUtils.isEmpty(kioskNamePref.get())) {
      String[] kioskNames = getResources().getStringArray(R.array.kiosk_names);
      String randomName = kioskNames[new Random().nextInt(kioskNames.length)];
      if (randomName != null && !TextUtils.isEmpty(randomName)) {
        kioskNamePref.set(randomName);
        Timber.w("Kiosk named randomly: %s", randomName);
      } else {
        Timber.w("Failed setting random name");
      }
    }

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

  @TargetApi(Build.VERSION_CODES.M) private boolean checkWriteStoragePermission() {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED;
  }

  private void requestWriteStoragePermission() {
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    switch (permsRequestCode) {
      case 8000:
        boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        getPermissionToOpenCameraAndGalley();
        break;
    }

    // Make sure it's our original READ_CONTACTS request
    if (permsRequestCode == CAMERA_GALLERY_PERMISSIONS_REQUEST) {
      if (grantResults.length == 2
          &&
          grantResults[0] == PackageManager.PERMISSION_GRANTED
          && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        Timber.v("Camera Permission Granted");
      } else {
        Toast.makeText(MainActivity.this, "Sorry you can't use this app without permission",
            Toast.LENGTH_LONG).show();
        Timber.w("Camera permission denied");
      }
    } else {
      super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
    }
  }

  /**
   * Gets permission to open camera and galley.
   */
  // Called when the user is performing an action which requires the app to show camera
  @TargetApi(Build.VERSION_CODES.M) public void getPermissionToOpenCameraAndGalley() {
    // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
    // checking the build version since Context.checkSelfPermission(...) is only available
    // in Marshmallow
    // 2) Always check for permission (even if permission has already been granted)
    // since the user can revoke permissions at any time through Settings
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {

      // The permission is NOT already granted.
      // Check if the user has been asked about this permission already and denied
      // it. If so, we want to give more explanation about why the permission is needed.
      if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
        // Show our own UI to explain to the user why we need to read the contacts
        // before actually requesting the permission and showing the default UI
        Timber.d("Should ask again for permission");
      }

      // Fire off an async request to actually get the permission
      // This will show the standard permission request dialog UI
      requestPermissions(
          new String[] { Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE },
          CAMERA_GALLERY_PERMISSIONS_REQUEST);
    }
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    View view = getCurrentFocus();

    int x = (int) ev.getX();
    int y = (int) ev.getY();

    if (view instanceof EditText) {
      EditText innerView = (EditText) getCurrentFocus();

      if (ev.getAction() == MotionEvent.ACTION_UP && !getLocationOnScreen(innerView).contains(x,
          y)) {

        InputMethodManager input =
            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        .replace(R.id.container, LoginFragment.newInstance(this))
        .commit();
  }

  /**
   * Login.
   */
  public void login() {
    loading.show();

    networkRequestManager
        .callLoginApi(new LoginRequest(username.get(), password.get()), new AppCallback() {
          @Override public void onRequestSuccess(Call call, Response response) {

            loading.dismiss();

            LoginResponse loginResponse = (LoginResponse) response.body();

            Timber.d("LoggedIn");
            hostelName.set(loginResponse.data.name);
            hostelKey.set(loginResponse.data.doc_key);

            tracker.send(new HitBuilders.TimingBuilder("Login", "Logged In",
                System.currentTimeMillis()).build());

            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();
          }

          @Override public void onApiNotFound(Call call, BaseResponse response) {
            loading.dismiss();
            showLoginFragment();
          }

          @Override public void onRequestFail(Call call, BaseResponse response) {
            loading.dismiss();
            showLoginFragment();
            Snackbar.make(appContainer.bind(MainActivity.this), R.string.something_wrong_try_again,
                Snackbar.LENGTH_LONG).show();
            Timber.w("ERROR: claim login error = %s",
                response.getMessage() != null ? response.getMessage() : response.toString());

            period = Math.round(period * 1.5);
            new Handler().postDelayed(new Runnable() {
              @Override public void run() {
                Timber.w("trying to login again after %d millis", period);
                login();
              }
            }, period);
          }

          @Override public void onRequestFail(Call call, Throwable t) {
            Timber.e(t);
          }

          @Override public void onBadRequest(Call call, BaseResponse response) {
            loading.dismiss();
            showLoginFragment();
          }

          @Override public void onAuthError(Call call, BaseResponse response) {
            loading.dismiss();
            showLoginFragment();
            Snackbar.make(appContainer.bind(MainActivity.this),
                R.string.enter_correct_email_password, Snackbar.LENGTH_LONG).show();
            Timber.w("Error Logging in with user: %s and password %s", username.get(),
                password.get());
          }

          @Override public void onServerError(Call call, BaseResponse response) {
            loading.dismiss();
            showLoginFragment();
            Snackbar.make(appContainer.bind(MainActivity.this), R.string.no_connection,
                Snackbar.LENGTH_LONG).show();
          }

          @Override public void onRequestTimeOut(Call call, Throwable t) {
            loading.dismiss();
            showLoginFragment();
          }

          @Override public void onNullResponse(Call call) {
            loading.dismiss();
            showLoginFragment();
          }
        });
  }

  public void forgetPassword(View view) {
    Intent browserIntent =
        new Intent(Intent.ACTION_VIEW, Uri.parse("http://dashboard.tipi.me/#/forgot"));
    startActivity(browserIntent);
  }

  public void setLanguage() {
    Locale locale = new Locale(languagePreference.get());
    Resources res = getResources();
    DisplayMetrics displayMetrics = res.getDisplayMetrics();
    Configuration configuration = res.getConfiguration();
    configuration.locale = locale;
    res.updateConfiguration(configuration, displayMetrics);
  }
}
