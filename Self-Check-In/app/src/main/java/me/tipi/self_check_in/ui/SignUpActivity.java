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
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.Booking;
import me.tipi.self_check_in.data.api.models.ClaimRequest;
import me.tipi.self_check_in.data.api.models.ClaimResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.ClaimEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.fragments.AvatarFragment;
import me.tipi.self_check_in.ui.fragments.DateFragment;
import me.tipi.self_check_in.ui.fragments.HostelTermsFragment;
import me.tipi.self_check_in.ui.fragments.IdentityFragment;
import me.tipi.self_check_in.ui.fragments.LandingFragment;
import me.tipi.self_check_in.ui.fragments.MainFragment;
import me.tipi.self_check_in.ui.fragments.PassportFragment;
import me.tipi.self_check_in.ui.fragments.QuestionFragment;
import me.tipi.self_check_in.ui.fragments.SuccessSignUpFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {
  private static final int CAMERA_GALLERY_PERMISSIONS_REQUEST = 9000;

  @Inject Bus bus;
  @Inject Guest guest;
  @Inject
  @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject
  @Named(ApiConstants.PASSPORT) Preference<String> passportPath;
  @Inject AppContainer appContainer;
  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject
  @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject Tracker tracker;

  @Bind(R.id.container_main) FrameLayout mainContainer;
  @Bind(R.id.settingBtn) ImageView settingButton;
  @Bind(R.id.resetBtn) ImageView resetButton;
  @Bind(R.id.backBtn) ImageView backButtonView;

  private MaterialDialog loading;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    loading = new MaterialDialog.Builder(this)
        .content("Submitting")
        .cancelable(false)
        .progress(true, 0)
        .build();


    guest.user_key = null;
    guest.name = null;
    guest.email = null;
    showMainFragment();
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    checkForCrashes();
    bus.register(this);
    tracker.setScreenName(getResources().getString(R.string.guest_landing));
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
    getPermissionToOpenCameraAndGalley();
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    unregisterManagers();
    bus.unregister(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unregisterManagers();
    avatarPath.delete();
    passportPath.delete();
    if (guest != null) {
      guest = null;
    }
  }

  @Override public void onBackPressed() {
    Fragment questionFragment = getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG);
    Fragment successFragment = getSupportFragmentManager().findFragmentByTag(SuccessSignUpFragment.TAG);
    if (questionFragment != null && questionFragment.isVisible()) {
      reset();
    }

    if (successFragment != null && successFragment.isVisible()) {
      reset();
    }

    super.onBackPressed();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    // Make sure it's our original READ_CONTACTS request
    if (requestCode == CAMERA_GALLERY_PERMISSIONS_REQUEST) {
      if (grantResults.length == 2 &&
          grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        Timber.d("Permission Granted");
      } else {
        Toast.makeText(SignUpActivity.this, "Sorry you can't use this app without permission", Toast.LENGTH_LONG).show();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    View view = getCurrentFocus();

    int x = (int) ev.getX();
    int y = (int) ev.getY();

    if (view instanceof EditText) {
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

  public void showMainFragment() {
    MainFragment fragment = MainFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.container_main, fragment).commit();
  }

  public void showLandingFragment() {
    LandingFragment fragment = LandingFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(LandingFragment.TAG).commit();
  }

  public void showIdentityFragment() {
    IdentityFragment fragment = IdentityFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(IdentityFragment.TAG).commit();
  }

  public void showPassportFragment() {
    PassportFragment fragment = PassportFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(PassportFragment.TAG).commit();
  }

  public void showDateFragment() {
    DateFragment fragment = DateFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(DateFragment.TAG).commit();
  }

  public void showAvatarFragment() {
    AvatarFragment fragment = AvatarFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(AvatarFragment.TAG).commit();
  }

  public void showTermsFragment() {
    HostelTermsFragment fragment = HostelTermsFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(HostelTermsFragment.TAG).commit();
  }

  public void showQuestionFragment() {
    QuestionFragment fragment = QuestionFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, QuestionFragment.TAG).addToBackStack(QuestionFragment.TAG).commit();
  }

  public void showSuccessFragment() {
    SuccessSignUpFragment fragment = SuccessSignUpFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, SuccessSignUpFragment.TAG).addToBackStack(SuccessSignUpFragment.TAG).commit();
  }

  /**
   * Start over.
   */
  @OnClick(R.id.resetBtn)
  public void startOver() {
    reset();
  }

  /**
   * Back clicked.
   */
  @OnClick(R.id.settingBtn)
  public void settingClicked() {
    startActivity(new Intent(this, SettingActivity.class));
    finish();
  }

  /**
   * Back clicked.
   */
  @OnClick(R.id.backBtn)
  public void backClicked() {
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    onBackPressed();
  }

  /**
   * On back shown.
   *
   * @param event the event
   */
  @Subscribe
  public void onSettingShown(SettingShouldShowEvent event) {
    settingButton.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  /**
   * On back shown.
   *
   * @param event the event
   */
  @Subscribe
  public void onBackShown(BackShouldShowEvent event) {
    backButtonView.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  @Subscribe
  public void onRefreshShouldShow(RefreshShouldShowEvent event) {
    resetButton.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  /**
   * On submit.
   *
   * @param event the event
   */
  @Subscribe
  public void onSubmit(SubmitEvent event) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    if (avatarPath != null && avatarPath.get() != null && passportPath != null && passportPath.get() != null) {
      loading.show();
      @SuppressWarnings("ConstantConditions")
      TypedFile avatarFile = new TypedFile("image/jpeg", new File(avatarPath.get()));
      @SuppressWarnings("ConstantConditions")
      TypedFile passportFile = new TypedFile("image/jpeg", new File(passportPath.get()));
      authenticationService.addGuest(
          avatarFile,
          passportFile,
          guest.email,
          guest.name,
          (guest.city == null || TextUtils.isEmpty(guest.city)) ? null : guest.city,
          (guest.country == null || TextUtils.isEmpty(guest.country)) ? null : guest.country,
          guest.passportNumber,
          guest.dob == null ? null : dateFormat.format(guest.dob),
          guest.referenceCode != null ? guest.referenceCode : null,
          dateFormat.format(guest.checkInDate),
          dateFormat.format(guest.checkOutDate),
          guest.gender,
          new Callback<ClaimResponse>() {
            @Override public void success(ClaimResponse apiResponse, Response response) {
              loading.dismiss();
              Timber.d("Good");
              guest.guest_key = apiResponse.data.guest_key;
              // Send overall success time
              long elapsed = Math.abs(guest.time - System.currentTimeMillis());
              long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
              tracker.send(new HitBuilders.EventBuilder()
                  .setCategory(getString(R.string.overal_time))
                  .setAction("Check-In")
                  .setLabel("Sign Up")
                  .setValue(diffSeconds).build());
              tracker.send(new HitBuilders.EventBuilder("Check-in", "Create").build());
              showQuestionFragment();
            }

            @Override public void failure(RetrofitError error) {
              if (error.getResponse() != null) {
                if (error.getResponse().getStatus() == 409) {
                  loading.dismiss();
                  /*viewPager.setCurrentItem(1, true);*/
                  bus.post(new EmailConflictEvent());
                } else if (error.getResponse().getStatus() == 401) {
                  login();
                } else {
                  loading.dismiss();
                  Snackbar.make(appContainer.bind(SignUpActivity.this), "Connection failed, please try again", Snackbar.LENGTH_LONG).show();
                }
              } else {
                loading.dismiss();
                Snackbar.make(appContainer.bind(SignUpActivity.this), "Connection failed, please try again", Snackbar.LENGTH_LONG).show();
              }
            }
          }
      );
    }

  }

  @Subscribe
  public void onClaimEvent(ClaimEvent event) {
    loading.show();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    authenticationService.claim(guest.user_key, new ClaimRequest(
            guest.email,
            new Booking(
                guest.referenceCode,
                dateFormat.format(guest.checkInDate),
                dateFormat.format(guest.checkOutDate))),
        new Callback<ClaimResponse>() {
          @Override public void success(ClaimResponse apiResponse, Response response) {
            loading.dismiss();
            Timber.d("Claimed");
            guest.guest_key = apiResponse.data.guest_key;
            tracker.send(new HitBuilders.EventBuilder("Check-in", "Claim").build());

            // Send overall success time
            long elapsed = Math.abs(guest.time - System.currentTimeMillis());
            long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
            tracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.overal_time))
                .setAction("Check-In")
                .setLabel("Claim")
                .setValue(diffSeconds).build());
            showQuestionFragment();
          }

          @Override public void failure(RetrofitError error) {
            loading.dismiss();
            Timber.d("Claim error : %s", error.toString());
            Snackbar.make(appContainer.bind(SignUpActivity.this), "Something went wrong, try again", Snackbar.LENGTH_SHORT)
                .show();
          }
        });
  }

  /**
   * Gets permission to open camera and galley.
   */
// Called when the user is performing an action which requires the app to show camera
  @TargetApi(Build.VERSION_CODES.M)
  public void getPermissionToOpenCameraAndGalley() {
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
      if (shouldShowRequestPermissionRationale(
          Manifest.permission.CAMERA)) {
        // Show our own UI to explain to the user why we need to read the contacts
        // before actually requesting the permission and showing the default UI
        Timber.d("Should ask again for permission");
      }

      // Fire off an async request to actually get the permission
      // This will show the standard permission request dialog UI
      requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
          CAMERA_GALLERY_PERMISSIONS_REQUEST);
    }
  }

  /**
   * Go to find activity.
   *
   * @param view the view
   */
  public void goToFindActivity(View view) {
    tracker.send(new HitBuilders.EventBuilder("Process", "Find").build());
    guest.time = System.currentTimeMillis();
    startActivity(new Intent(this, FindUserActivity.class));
  }

  /**
   * Go to find activity.
   *
   * @param view the view
   */
  public void goToIdentity(View view) {
    tracker.send(new HitBuilders.EventBuilder("Process", "Create").build());
    guest.time = System.currentTimeMillis();
    showIdentityFragment();
  }

  /**
   * Go to landing.
   *
   * @param view the view
   */
  public void goToLanding(View view) {
    showLandingFragment();
    //showPassportFragment();
  }

  /**
   * Login.
   */
  private void login() {
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse response, Response response2) {
        Timber.d("LoggedIn");
        bus.post(new SubmitEvent());
      }

      @Override public void failure(RetrofitError error) {
        if (error.getResponse().getStatus() == 401) {
          Snackbar.make(appContainer.bind(SignUpActivity.this), "Your email/password doesn't match!", Snackbar.LENGTH_LONG).show();
        } else {
          Snackbar.make(appContainer.bind(SignUpActivity.this), "Connection failed, please try again", Snackbar.LENGTH_LONG).show();
        }
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
      }
    });
  }

  /**
   * Reset.
   */
  public void reset() {
    avatarPath.delete();
    passportPath.delete();
    if (guest != null) {
      guest.user_key = null;
      guest.email = null;
      guest.name = null;
      guest.checkInDate = null;
      guest.checkOutDate = null;
      guest.city = null;
      guest.country = null;
      guest.dob = null;
      guest.passportNumber = null;
      guest.referenceCode = null;
    }

    Intent intent = getIntent();
    finish();
    startActivity(intent);
  }

  private void checkForCrashes() {
    CrashManager.register(this);
  }

  private void checkForUpdates() {
    // Remove this for store builds!
    UpdateManager.register(this);
  }

  private void unregisterManagers() {
    UpdateManager.unregister();
  }
}
