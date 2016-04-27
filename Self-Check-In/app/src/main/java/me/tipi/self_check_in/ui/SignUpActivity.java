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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
import me.tipi.self_check_in.ui.adapters.SignUpAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.ClaimEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.misc.ChangeSwipeViewPager;
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
  @Named(ApiConstants.AVATAR)
  Preference<String> avatarPath;
  @Inject
  @Named(ApiConstants.PASSPORT)
  Preference<String> passportPath;
  @Inject AppContainer appContainer;
  @Inject AuthenticationService authenticationService;
  @Inject
  @Named(ApiConstants.USER_NAME)
  Preference<String> username;
  @Inject
  @Named(ApiConstants.PASSWORD)
  Preference<String> password;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.pager) ChangeSwipeViewPager viewPager;
  @Bind(R.id.settingBtn) ImageView settingButton;
  @Bind(R.id.resetBtn) ImageView resetButton;
  @Bind(R.id.backBtn) ImageView backButtonView;
  @Bind(R.id.tipi_description) TextView description;

  MaterialDialog loading;

  public SignUpAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    loading = new MaterialDialog.Builder(this)
        .content("Submitting")
        .cancelable(false)
        .progress(true, 0)
        .build();

    typeface.setTypeface(description, getString(R.string.font_medium));


    guest.user_key = null;
    guest.name = null;
    guest.email = null;

    adapter = new SignUpAdapter(getSupportFragmentManager(), this);
    viewPager.setAdapter(adapter);
    viewPager.setSwipingEnabled(false);
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    bus.register(this);
    tracker.setScreenName(getResources().getString(R.string.guest_landing));
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
    getPermissionToOpenCameraAndGalley();
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    bus.unregister(this);
  }

  @Override
  public void onBackPressed() {
    if (viewPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else if (viewPager.getCurrentItem() == 7) {
      reset();
    } else if (viewPager.getCurrentItem() == 4) {
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    } else {
      // Otherwise, select the previous step.
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    avatarPath.delete();
    passportPath.delete();
    if (guest != null) {
      guest = null;
    }
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

  /**
   * Start over.
   */
  @OnClick(R.id.resetBtn)
  public void startOver() {
    reset();
    //bus.post(new PagerChangeEvent(0));
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

    if (viewPager.getCurrentItem() != 0) {
      if (viewPager.getCurrentItem() == 4) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
      } else {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
      }
    }

    tracker.send(new HitBuilders.EventBuilder("Sign up", "Back").build());
  }

  /**
   * On pager change.
   *
   * @param event the event
   */
  @Subscribe
  public void onPagerChange(PagerChangeEvent event) {
    viewPager.setCurrentItem(event.page, true);
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
              viewPager.setCurrentItem(6, true);
            }

            @Override public void failure(RetrofitError error) {
              if (error.getResponse() != null) {
                if (error.getResponse().getStatus() == 409) {
                  loading.dismiss();
                  viewPager.setCurrentItem(1, true);
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
            viewPager.setCurrentItem(6);
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
    viewPager.setCurrentItem(1);
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
  private void reset() {
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
}
