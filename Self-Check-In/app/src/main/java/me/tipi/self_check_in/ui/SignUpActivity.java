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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.microblink.recognizers.RecognitionResults;
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
import me.tipi.self_check_in.KioskService;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.LanguagePrefrence;
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
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.fragments.AvatarFragment;
import me.tipi.self_check_in.ui.fragments.DateFragment;
import me.tipi.self_check_in.ui.fragments.EmailFragment;
import me.tipi.self_check_in.ui.fragments.HostelTermsFragment;
import me.tipi.self_check_in.ui.fragments.IdentityFragment;
import me.tipi.self_check_in.ui.fragments.LandingFragment;
import me.tipi.self_check_in.ui.fragments.LanguageFragment;
import me.tipi.self_check_in.ui.fragments.MainFragment;
import me.tipi.self_check_in.ui.fragments.OCRFragment;
import me.tipi.self_check_in.ui.fragments.PassportFragment;
import me.tipi.self_check_in.ui.fragments.QuestionFragment;
import me.tipi.self_check_in.ui.fragments.ScanIDFragment;
import me.tipi.self_check_in.ui.fragments.SuccessSignUpFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {
  @Inject Bus bus;
  @Inject Guest guest;
  @Inject AppContainer appContainer;
  @Inject AuthenticationService authenticationService;
  @Inject
  @Named(ApiConstants.USER_NAME)
  Preference<String> username;
  @Inject
  @Named(ApiConstants.PASSWORD)
  Preference<String> password;
  @Inject Tracker tracker;
  @Inject LanguagePrefrence languagePrefrence;

  @Bind(R.id.settingBtn) ImageView settingButton;
  @Bind(R.id.resetBtn) ImageView resetButton;
  @Bind(R.id.backBtn) ImageView backButtonView;

  private MaterialDialog loading;
  private int loginCount = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    loading = new MaterialDialog.Builder(this)
        .content("Please wait...")
        .cancelable(false)
        .progress(true, 0)
        .build();
    setLanguage("en");
    guest.user_key = null;
    guest.firstName = null;
    guest.lastName = null;
    guest.email = null;
    showMainFragment();

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.edit().putBoolean(KioskService.PREF_KIOSK_MODE, true).apply();
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    checkForCrashes();
    if (bus != null) {
      bus.register(this);
    }

    tracker.setScreenName(getResources().getString(R.string.guest_landing));
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    unregisterManagers();
    if (bus != null) {
      bus.unregister(this);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unregisterManagers();
    if (guest != null) {
      guest = null;
    }
  }

  @Override public void onBackPressed() {
    Fragment questionFragment = getSupportFragmentManager().findFragmentByTag(QuestionFragment.TAG);
    Fragment successFragment = getSupportFragmentManager().findFragmentByTag(SuccessSignUpFragment.TAG);
    Fragment scanFragment = getSupportFragmentManager().findFragmentByTag(ScanIDFragment.TAG);
    Fragment ocrFragment = getSupportFragmentManager().findFragmentByTag(OCRFragment.TAG);
    if (questionFragment != null && questionFragment.isVisible()) {
      reset();
    }

    if (successFragment != null && successFragment.isVisible()) {
      reset();
    }

    if (scanFragment != null && scanFragment.isVisible()) {
      clearData();
    }

    if (ocrFragment != null && ocrFragment.isVisible()) {
      if (guest.passportPath != null && !TextUtils.isEmpty(guest.passportPath)) {
        guest.passportPath = null;
      }
    }

    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
      return;
    }

    super.onBackPressed();
  }

  public void setLanguage(String language) {
    languagePrefrence.set(language);
    Locale locale = new Locale(languagePrefrence.get());
    Resources res = getResources();
    DisplayMetrics displayMetrics = res.getDisplayMetrics();
    Configuration configuration = res.getConfiguration();
    configuration.locale = locale;
    res.updateConfiguration(configuration, displayMetrics);
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

  public void showLanguageFragment() {
    LanguageFragment fragment = LanguageFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(LanguageFragment.TAG).commit();
  }

  public void showLandingFragment() {
    LandingFragment fragment = LandingFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment).addToBackStack(LandingFragment.TAG).commit();
  }

  public void showIdentityFragment(RecognitionResults results) {
    IdentityFragment fragment = IdentityFragment.newInstance(this, results);
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
        .replace(R.id.container_main, fragment, SuccessSignUpFragment.TAG)
        .addToBackStack(SuccessSignUpFragment.TAG).commit();
  }

  public void showScanFragment() {
    OCRFragment fragment = OCRFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, OCRFragment.TAG).addToBackStack(OCRFragment.TAG).commit();
  }

  public void showEmailFragment() {
    EmailFragment fragment = EmailFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, EmailFragment.TAG)
        .addToBackStack(EmailFragment.TAG).commit();
  }

  public void showScanIDFragment() {
    ScanIDFragment fragment = ScanIDFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, ScanIDFragment.TAG)
        .addToBackStack(ScanIDFragment.TAG).commit();
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

  public void submit() {
    Timber.w("Entered submit method");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    if (guest.avatarPath == null) {
      Timber.w("AVATAR pref is null");
      Snackbar.make(appContainer.bind(SignUpActivity.this), "Your avatar didn't save successfully, please take another selfie", Snackbar.LENGTH_LONG).show();
      return;
    }

    if (TextUtils.isEmpty(guest.avatarPath)) {
      Timber.w("AVATAR path not saved it is: %s", guest.avatarPath);
      Snackbar.make(appContainer.bind(SignUpActivity.this), "Your avatar didn't save successfully, please take another selfie", Snackbar.LENGTH_LONG).show();
      return;
    }

    if (guest.passportPath == null || TextUtils.isEmpty(guest.passportPath)) {
      Timber.w("Passport path not saved it is: %s", guest.passportPath);
      Snackbar.make(appContainer.bind(SignUpActivity.this), "Your passport image didn't save successfully, please re-capture", Snackbar.LENGTH_LONG).show();
      return;
    }

    @SuppressWarnings("ConstantConditions")
    TypedFile avatarFile = new TypedFile("image/jpeg", new File(guest.avatarPath));
    @SuppressWarnings("ConstantConditions")
    TypedFile passportFile = new TypedFile("image/jpeg", new File(guest.passportPath));

    String fullName = "";

    if (guest.firstName != null && !TextUtils.isEmpty(guest.firstName)) {
      if (guest.lastName != null && !TextUtils.isEmpty(guest.lastName)) {
        fullName = String.format(Locale.US, "%s  %s", guest.firstName.trim(), guest.lastName.trim()).trim();
      } else {
        Timber.w("We don't have last name. it is: %s", guest.lastName);
      }
    } else {
      Timber.w("We don't have first name. it is: %s", guest.firstName);
    }

    loading.show();
    Timber.w("sending data with values  %s", guest.toString());


    authenticationService.addGuest(
        avatarFile,
        passportFile,
        guest.email,
        fullName,
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
            Timber.w("--------PROCESS FINISHED------- with guest_key: %s", apiResponse.data.guest_key);
            guest.guest_key = apiResponse.data.guest_key;
            guest.name = apiResponse.data.name;
            guest.check_in_code = apiResponse.data.check_in_code;
            // Send overall success time
            long elapsed = Math.abs(guest.time - System.currentTimeMillis());
            long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
            tracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.overall_time))
                .setAction("Check-In")
                .setLabel("Sign Up")
                .setValue(diffSeconds).build());
            tracker.send(new HitBuilders.EventBuilder("Check-in", "Create").build());
            showQuestionFragment();
          }

          @Override public void failure(RetrofitError error) {
            loading.dismiss();
            Timber.w("--------PROCESS Terminated-------");
            if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
              Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
              return;
            }

            if (error.getResponse() != null) {
              if (error.getResponse().getStatus() == 409) {
                Timber.w("ERROR %s, status: %s", error.getMessage(), error.getResponse().getStatus());
                showSuccessFragment();
              } else if (error.getResponse().getStatus() == 401) {
                Timber.w("ERROR %s", error.getMessage());
                login();
              } else if (error.getResponse().getStatus() == 400) {
                Timber.w("ERROR %s, status: %s", error.getMessage(), error.getResponse().getStatus());
                new MaterialDialog.Builder(SignUpActivity.this)
                    .cancelable(true)
                    .autoDismiss(true)
                    .title("Bad info")
                    .content("Please check your check-in date again and retry")
                    .positiveText("OK").build().show();
              } else {
                // Timber.e("ERROR" + error.toString() +
                //    "error body = " + error.getBody().toString() + "error kind = " + error.getKind().toString());
                Timber.w("ERROR %s", error.getMessage() != null ? error.getMessage() : error.toString());
                Toast.makeText(SignUpActivity.this, R.string.something_wrong_try_again + error.getMessage(), Toast.LENGTH_LONG).show();
              }
            } else {
              // Timber.e("ERROR %s", error.toString());
              Timber.w("ERROR %s", error.getMessage() != null ? error.getMessage() : error.toString());
              Toast.makeText(SignUpActivity.this, R.string.something_wrong_try_again + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
          }
        }
    );

  }

  @Subscribe
  public void onClaimEvent(ClaimEvent event) {
    Timber.i("Entered claim event");
    Timber.w("Claiming with data: Guest key = %s - Email = %s", guest.user_key, guest.email);
    loading.show();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    authenticationService.claim(guest.user_key, new ClaimRequest(
            guest.email,
            new Booking(
                guest.referenceCode != null ? guest.referenceCode : null,
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
                .setCategory(getString(R.string.overall_time))
                .setAction("Check-In")
                .setLabel("Claim")
                .setValue(diffSeconds).build());
            showQuestionFragment();
          }

          @Override public void failure(RetrofitError error) {
            loading.dismiss();
            if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
              Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
              return;
            }

            if (error.getResponse() != null && error.getResponse().getStatus() == 400) {
              Timber.w("ERROR %s - status: %s", error.getMessage(), error.getResponse().getStatus());
              new MaterialDialog.Builder(SignUpActivity.this)
                  .cancelable(true)
                  .autoDismiss(true)
                  .title("Bad info")
                  .content(R.string.check_from_date)
                  .positiveText("OK").build().show();
              Timber.w("Please check your check-in date again and retry!");
              return;
            }

            Timber.w("Claim error : %s", error.getMessage() != null ? error.getMessage() : error.toString());
            Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_SHORT)
                .show();
          }
        });
  }

  /**
   * Go to find activity.
   *
   * @param view the view
   */
  public void goToFindActivity(View view) {
    tracker.send(new HitBuilders.EventBuilder("Process", "Find").build());
    guest.time = System.currentTimeMillis();
    Timber.w("---------GOING TO FIND USER--------");
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
    Timber.w("----------GOING TO SIGN UP----------");
    showEmailFragment();
  }

  /**
   * Go to landing.
   *
   * @param view the view
   */
  public void goToLanding(View view) {
    Timber.w("----------PROCESS STARTED----------");
    loading.show();
    firstLogin();
  }

  public void firstLogin() {
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse response, Response response2) {
        loading.dismiss();
        Timber.d("LoggedIn");
        showLanguageFragment();
      }

      @Override public void failure(RetrofitError error) {
        loading.dismiss();
        if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
          Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.ask_staff_login, Snackbar.LENGTH_LONG).show();
          Timber.w("cannot login in pink after retry");
          return;
        }

        Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_INDEFINITE)
            .setAction("RETRY", new View.OnClickListener() {
              @Override public void onClick(View v) {
                firstLogin();
              }
            }).show();
        Timber.w("firstLogin Error: %s", error.getMessage() != null ? error.getMessage() : error.toString());
      }
    });
  }

  /**
   * Login.
   */
  private void login() {

    if (loginCount >= 2) {

      Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.ask_staff_login, Snackbar.LENGTH_LONG).show();
      Timber.w("cannot login when got authentication error in submit after retry!");
      return;
    }

    loginCount++;
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse response, Response response2) {
        Timber.d("LoggedIn");
        submit();
      }

      @Override public void failure(RetrofitError error) {
        if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
          Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
          Timber.d("Please check the WI-FI connection!");
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          login();
          return;
        }

        Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_LONG).show();
        Timber.w("Submit login Error: %s", error.getMessage() != null ? error.getMessage() : error.toString());
      }
    });
  }

  /**
   * Reset.
   */
  public void reset() {
//    removePhoto();
    clearData();
    Intent intent = getIntent();
    finish();
    startActivity(intent);
  }

  private void clearData() {
    if (guest != null) {
      guest.user_key = null;
      guest.guest_key = null;
      guest.email = null;
      guest.firstName = null;
      guest.lastName = null;
      guest.checkInDate = null;
      guest.checkOutDate = null;
      guest.city = null;
      guest.country = null;
      guest.dob = null;
      guest.passportNumber = null;
      guest.referenceCode = null;
      guest.passportPath = null;
      guest.avatarPath = null;
    }
  }

//  public void removePhoto() {
//    if (guest.passportPath != null) {
//      File passportPhoto = new File(guest.passportPath);
//
//      if (passportPhoto.exists()) {
//        if (passportPhoto.delete()) {
//          Timber.w("passport photo deleted");
//        }
//      }
//    }
//
//    if (guest.avatarPath != null) {
//      File avatarPhoto = new File(guest.avatarPath);
//
//      if (avatarPhoto.exists()) {
//        if (avatarPhoto.delete()) {
//          Timber.w("avatar photo deleted");
//        }
//      }
//    }
//  }

  private void checkForCrashes() {
    CrashManager.register(this);
  }

/*  private void checkForUpdates() {
    // Remove this for store builds!
    UpdateManager.register(this);
  }*/

  private void unregisterManagers() {
    UpdateManager.unregister();
  }
}
