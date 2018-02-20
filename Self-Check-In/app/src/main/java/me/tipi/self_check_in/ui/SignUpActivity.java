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
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences2.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.microblink.recognizers.RecognitionResults;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import me.tipi.self_check_in.KioskService;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.LanguagePreference;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AppCallback;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.AddGuestCallback;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.Booking;
import me.tipi.self_check_in.data.api.models.ClaimRequest;
import me.tipi.self_check_in.data.api.models.ClaimResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.LoginRequest;
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
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {
  @Inject Bus bus;
  @Inject Guest guest;
  @Inject AppContainer appContainer;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject Tracker tracker;
  @Inject LanguagePreference languagePreference;

  @Bind(R.id.settingBtn) ImageView settingButton;
  @Bind(R.id.resetBtn) ImageView resetButton;
  @Bind(R.id.backBtn) ImageView backButtonView;

  private MaterialDialog loading;
  private int loginCount = 0;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    loading = new MaterialDialog.Builder(this).content("Please wait...")
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
    Timber.w("KIOSK mode is ON");
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
    Fragment successFragment =
        getSupportFragmentManager().findFragmentByTag(SuccessSignUpFragment.TAG);
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
    languagePreference.set(language);
    Locale locale = new Locale(languagePreference.get());
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

  public void showMainFragment() {
    MainFragment fragment = MainFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction().add(R.id.container_main, fragment).commit();
  }

  public void showLanguageFragment() {
    LanguageFragment fragment = LanguageFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(LanguageFragment.TAG)
        .commit();
  }

  public void showLandingFragment() {
    LandingFragment fragment = LandingFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(LandingFragment.TAG)
        .commit();
  }

  public void showIdentityFragment(RecognitionResults results) {
    IdentityFragment fragment = IdentityFragment.newInstance(this, results);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(IdentityFragment.TAG)
        .commit();
  }

  public void showPassportFragment() {
    PassportFragment fragment = PassportFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(PassportFragment.TAG)
        .commit();
  }

  public void showDateFragment() {
    DateFragment fragment = DateFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(DateFragment.TAG)
        .commit();
  }

  public void showAvatarFragment() {
    AvatarFragment fragment = AvatarFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(AvatarFragment.TAG)
        .commit();
  }

  public void showTermsFragment() {
    HostelTermsFragment fragment = HostelTermsFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment)
        .addToBackStack(HostelTermsFragment.TAG)
        .commit();
  }

  public void showQuestionFragment() {
    QuestionFragment fragment = QuestionFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, QuestionFragment.TAG)
        .addToBackStack(QuestionFragment.TAG)
        .commit();
  }

  public void showSuccessFragment() {
    SuccessSignUpFragment fragment = SuccessSignUpFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, SuccessSignUpFragment.TAG)
        .addToBackStack(SuccessSignUpFragment.TAG)
        .commit();
  }

  public void showScanFragment() {
    OCRFragment fragment = OCRFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, OCRFragment.TAG)
        .addToBackStack(OCRFragment.TAG)
        .commit();
  }

  public void showEmailFragment() {
    EmailFragment fragment = EmailFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, EmailFragment.TAG)
        .addToBackStack(EmailFragment.TAG)
        .commit();
  }

  public void showScanIDFragment() {
    ScanIDFragment fragment = ScanIDFragment.newInstance(this);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_main, fragment, ScanIDFragment.TAG)
        .addToBackStack(ScanIDFragment.TAG)
        .commit();
  }

  /**
   * Start over.
   */
  @OnClick(R.id.resetBtn) public void startOver() {
    reset();
  }

  /**
   * Back clicked.
   */
  @OnClick(R.id.settingBtn) public void settingClicked() {
    Timber.w("------Setting button Tapped----");
    new MaterialDialog.Builder(this).title("Verification Needed")
        .content("Please enter hostel password")
        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
        .inputRange(4, 50, ContextCompat.getColor(SignUpActivity.this, R.color.colorAccent))
        .input("Hostel Password", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            Timber.w("Input password is: %s and hostel password is: %s and master password is: %s",
                input.toString(), password.get(), ApiConstants.MASTER_PASSWORD);
            if (password != null && !TextUtils.isEmpty(password.get()) &&
                password.get() != null && password.get().equals(input.toString())) {
              Timber.w("Correct hostel password going to setting");
              startActivity(new Intent(SignUpActivity.this, SettingActivity.class));
              finish();
            } else if (input.toString().equals(ApiConstants.MASTER_PASSWORD)) {
              Timber.w("Correct master password going to setting");
              startActivity(new Intent(SignUpActivity.this, SettingActivity.class));
              finish();
            } else {
              Timber.w("Incorrect password");
              Snackbar.make(appContainer.bind(SignUpActivity.this), "Invalid password",
                  Snackbar.LENGTH_LONG).show();
            }
          }
        })
        .show();
  }

  /**
   * Back clicked.
   */
  @OnClick(R.id.backBtn) public void backClicked() {
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
  @Subscribe public void onSettingShown(SettingShouldShowEvent event) {
    settingButton.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  /**
   * On back shown.
   *
   * @param event the event
   */
  @Subscribe public void onBackShown(BackShouldShowEvent event) {
    backButtonView.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  @Subscribe public void onRefreshShouldShow(RefreshShouldShowEvent event) {
    resetButton.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  public void submit() {
    Timber.w("Entered submit method");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    if (guest.avatarPath == null) {
      Timber.w("AVATAR pref is null");
      Snackbar.make(appContainer.bind(SignUpActivity.this),
          "Your avatar didn't save successfully, please take another selfie", Snackbar.LENGTH_LONG)
          .show();
      return;
    }

    if (TextUtils.isEmpty(guest.avatarPath)) {
      Timber.w("AVATAR path not saved it is: %s", guest.avatarPath);
      Snackbar.make(appContainer.bind(SignUpActivity.this),
          "Your avatar didn't save successfully, please take another selfie", Snackbar.LENGTH_LONG)
          .show();
      return;
    }

    if (guest.passportPath == null || TextUtils.isEmpty(guest.passportPath)) {
      Timber.w("Passport path not saved it is: %s", guest.passportPath);
      Snackbar.make(appContainer.bind(SignUpActivity.this),
          "Your passport image didn't save successfully, please re-capture", Snackbar.LENGTH_LONG)
          .show();
      return;
    }

    String fullName = "";

    if (guest.firstName != null && !TextUtils.isEmpty(guest.firstName)) {
      if (guest.lastName != null && !TextUtils.isEmpty(guest.lastName)) {
        fullName = String.format(Locale.US, "%s  %s", guest.firstName.trim(), guest.lastName.trim())
            .trim();
      } else {
        Timber.w("We don't have last name. it is: %s", guest.lastName);
      }
    } else {
      Timber.w("We don't have first name. it is: %s", guest.firstName);
    }

    loading.show();
    Timber.w("sending data with values  %s", guest.toString());

    NetworkRequestManager.getInstance()
        .callAddGuestApi(new File(guest.avatarPath), new File(guest.passportPath), guest.email,
            fullName, (guest.city == null || TextUtils.isEmpty(guest.city)) ? null : guest.city,
            (guest.country == null || TextUtils.isEmpty(guest.country)) ? null : guest.country,
            guest.passportNumber, guest.dob == null ? null : dateFormat.format(guest.dob),
            guest.referenceCode != null ? guest.referenceCode : null,
            dateFormat.format(guest.checkInDate), dateFormat.format(guest.checkOutDate),
            guest.gender, new AddGuestCallback() {
              @Override public void onRequestSuccess(Call call, Response response) {

                loading.dismiss();

                ClaimResponse claimResponse = (ClaimResponse) response.body();
                Timber.w("--------PROCESS FINISHED------- with guest_key: %s",
                    claimResponse.data.guest_key);
                guest.guest_key = claimResponse.data.guest_key;
                guest.name = claimResponse.data.name;
                Timber.w("Got name from server with value: %s. name stored with value: %s",
                    claimResponse.data.name, guest.name);
                // Send overall success time
                long elapsed = Math.abs(guest.time - System.currentTimeMillis());
                long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
                tracker.send(
                    new HitBuilders.EventBuilder().setCategory(getString(R.string.overall_time))
                        .setAction("Check-In")
                        .setLabel("Sign Up")
                        .setValue(diffSeconds)
                        .build());
                tracker.send(new HitBuilders.EventBuilder("Check-in", "Create").build());
                showQuestionFragment();
              }

              @Override public void onRequestFail(Call call, BaseResponse response) {
                loading.dismiss();
                Toast.makeText(SignUpActivity.this,
                    "Cannot contact server, please check your wifi connection!", Toast.LENGTH_LONG)
                    .show();
              }

              @Override public void onConflict(Call call, BaseResponse response) {
                loading.dismiss();
                Timber.w("ERROR %s, status: %s, kind: %s", response.getMessage(),
                    response.getStatusCode(), "");
                showSuccessFragment();
              }

              @Override public void onBadRequest(Call call, BaseResponse response) {
                loading.dismiss();
                Timber.w("ERROR %s, status: %s, kind: %s", response.getMessage(),
                    response.getStatusCode(), "");
                new MaterialDialog.Builder(SignUpActivity.this).cancelable(true)
                    .autoDismiss(true)
                    .title("Bad info")
                    .content("Please check your check-in date again and retry")
                    .positiveText("OK")
                    .build()
                    .show();
              }

              @Override public void onAuthError(Call call, BaseResponse response) {
                loading.dismiss();
                Timber.w("ERROR %s, status: %s, kind: %s", response.getMessage(),
                    response.getStatusCode(), "");
                login();
              }

              @Override public void onServerError(Call call, BaseResponse response) {
                loading.dismiss();
                Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection,
                    Snackbar.LENGTH_LONG).show();
                Timber.w("ERROR %s, status: %s, kind: %s", response.getMessage(),
                    response.getStatusCode(), "");
              }

              @Override public void onRequestTimeOut(Call call, Throwable t) {
                loading.dismiss();
              }

              @Override public void onNullResponse(Call call) {
                loading.dismiss();
              }
            });
  }

  @Subscribe public void onClaimEvent(ClaimEvent event) {
    Timber.i("Entered claim event");
    Timber.w("Claiming with data: Guest key = %s - Email = %s", guest.user_key, guest.email);
    loading.show();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    NetworkRequestManager.getInstance().callClaimApi(guest.user_key, new ClaimRequest(guest.email,
            new Booking(guest.referenceCode != null ? guest.referenceCode : null, dateFormat.format(guest.checkInDate), dateFormat.format(guest.checkOutDate))),
        new AppCallback() {
          @Override public void onRequestSuccess(Call call, Response response) {
            loading.dismiss();

            ClaimResponse claimResponse = (ClaimResponse) response.body();

            Timber.d("Claimed");
            guest.guest_key = claimResponse.data.guest_key;
            guest.name = claimResponse.data.name;
            tracker.send(new HitBuilders.EventBuilder("Check-in", "Claim").build());

            // Send overall success time
            long elapsed = Math.abs(guest.time - System.currentTimeMillis());
            long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
            tracker.send(
                new HitBuilders.EventBuilder().setCategory(getString(R.string.overall_time))
                    .setAction("Check-In")
                    .setLabel("Claim")
                    .setValue(diffSeconds)
                    .build());
            showQuestionFragment();
          }

          @Override public void onApiNotFound(Call call, BaseResponse response) {
            loading.dismiss();
          }

          @Override public void onRequestFail(Call call, BaseResponse response) {
            loading.dismiss();
            Timber.w("Claim error : %s",
                response.getMessage() != null ? response.getMessage() : response.toString());
            Snackbar.make(appContainer.bind(SignUpActivity.this),
                R.string.something_wrong_try_again, Snackbar.LENGTH_SHORT).show();
          }

          @Override public void onBadRequest(Call call, BaseResponse response) {
            loading.dismiss();
            Timber.w("ERROR %s - status: %s", response.getMessage(),
                response.getStatusCode());
            new MaterialDialog.Builder(SignUpActivity.this).cancelable(true)
                .autoDismiss(true)
                .title("Bad info")
                .content(R.string.check_from_date)
                .positiveText("OK")
                .build()
                .show();
            Timber.w("Please check your check-in date again and retry!");
          }

          @Override public void onAuthError(Call call, BaseResponse response) {
            loading.dismiss();
          }

          @Override public void onServerError(Call call, BaseResponse response) {
            loading.dismiss();
            Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection,
                Snackbar.LENGTH_LONG).show();
          }

          @Override public void onRequestTimeOut(Call call, Throwable t) {
            loading.dismiss();
          }

          @Override public void onNullResponse(Call call) {
            loading.dismiss();
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

    NetworkRequestManager.getInstance()
        .callLoginApi(new LoginRequest(username.get(), password.get()), new AppCallback() {
          @Override public void onRequestSuccess(Call call, Response response) {

            loading.dismiss();
            Timber.d("LoggedIn");
            showLanguageFragment();
          }

          @Override public void onApiNotFound(Call call, BaseResponse response) {

          }

          @Override public void onRequestFail(Call call, BaseResponse response) {
            Snackbar.make(appContainer.bind(SignUpActivity.this),
                R.string.something_wrong_try_again, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                  @Override public void onClick(View v) {
                    firstLogin();
                  }
                })
                .show();
            Timber.w("firstLogin Error: %s",
                response.getMessage() != null ? response.getMessage() : response.toString());
          }

          @Override public void onBadRequest(Call call, BaseResponse response) {

          }

          @Override public void onAuthError(Call call, BaseResponse response) {
          }

          @Override public void onServerError(Call call, BaseResponse response) {
            Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection,
                Snackbar.LENGTH_LONG).show();
          }

          @Override public void onRequestTimeOut(Call call, Throwable t) {

          }

          @Override public void onNullResponse(Call call) {

          }
        });
  }

  /**
   * Login.
   */
  private void login() {

    if (loginCount >= 2) {

      Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.ask_staff_login,
          Snackbar.LENGTH_LONG).show();
      Timber.w("cannot login when got authentication error in submit after retry!");
      return;
    }

    loginCount++;

    NetworkRequestManager.getInstance()
        .callLoginApi(new LoginRequest(username.get(), password.get()), new AppCallback() {
          @Override public void onRequestSuccess(Call call, Response response) {
            Timber.d("LoggedIn");
            submit();
          }

          @Override public void onApiNotFound(Call call, BaseResponse response) {

          }

          @Override public void onRequestFail(Call call, BaseResponse response) {
            Snackbar.make(appContainer.bind(SignUpActivity.this),
                R.string.something_wrong_try_again, Snackbar.LENGTH_LONG).show();
            Timber.w("Submit login Error: %s",
                response.getMessage() != null ? response.getMessage() : response.toString());
          }

          @Override public void onBadRequest(Call call, BaseResponse response) {

          }

          @Override public void onAuthError(Call call, BaseResponse response) {
            login();
          }

          @Override public void onServerError(Call call, BaseResponse response) {
            Snackbar.make(appContainer.bind(SignUpActivity.this), R.string.no_connection,
                Snackbar.LENGTH_LONG).show();
          }

          @Override public void onRequestTimeOut(Call call, Throwable t) {

          }

          @Override public void onNullResponse(Call call) {

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

  private void checkForCrashes() {
    CrashManager.register(this);
  }

  private void unregisterManagers() {
    UpdateManager.unregister();
  }
}
