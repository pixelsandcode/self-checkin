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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
import me.tipi.self_check_in.ui.adapters.LoginAdapter;
import me.tipi.self_check_in.ui.events.AuthenticationFailedEvent;
import me.tipi.self_check_in.ui.events.AuthenticationPassedEvent;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.ClaimEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.misc.ChangeSwipeViewPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class FindUserActivity extends AppCompatActivity {

  @Inject Bus bus;
  @Inject Guest guest;
  @Inject AppContainer appContainer;
  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.pager) ChangeSwipeViewPager viewPager;
  @Bind(R.id.backBtn) ImageView backButtonView;

  private LoginAdapter adapter;
  private MaterialDialog loading;
  private int loginCount = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_user);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);
    Timber.d("Created");

    typeface.setTypeface(this, getResources().getString(R.string.font_medium));

    loading = new MaterialDialog.Builder(this)
        .content("Please wait")
        .cancelable(false)
        .progress(true, 0)
        .build();

    adapter = new LoginAdapter(getSupportFragmentManager(), this);
    viewPager.setAdapter(adapter);
    viewPager.setSwipingEnabled(false);

  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    if (bus != null) {
      bus.register(this);
    }
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    if (bus != null) {
      bus.unregister(this);
    }
  }

  @Override
  public void onBackPressed() {
    if (viewPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else if(viewPager.getCurrentItem() == 4 || viewPager.getCurrentItem() == 3) {
      reset();
    } else {
      // Otherwise, select the previous step.
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
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
   * On pager change.
   *
   * @param event the event
   */
  @Subscribe
  public void onPagerChange(PagerChangeEvent event) {
    viewPager.setCurrentItem(event.page, true);
  }

  @Subscribe
  public void onAuthFailed(AuthenticationFailedEvent event) {
    login();
  }

  @Subscribe
  public void onClaimEvent(ClaimEvent event) {
    loading.show();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Timber.i("Entered claim event");
    Timber.w("Claiming with data: Guest key = %s - Email = %s", guest.user_key, guest.email);
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
            viewPager.setCurrentItem(3);
            tracker.send(new HitBuilders.EventBuilder("Check-in", "Claim").build());

            // Send overall success time
            long elapsed = Math.abs(guest.time - System.currentTimeMillis());
            long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
            tracker.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.overal_time))
                .setAction("Check-In")
                .setLabel("Claim")
                .setValue(diffSeconds).build());
          }

          @Override public void failure(RetrofitError error) {
            loading.dismiss();

            if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
              Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
              return;
            }

            if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
              Timber.w("Login as username: %s password: %s", username.get(), password.get());
              authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
                @Override public void success(LoginResponse response, Response response2) {
                  Timber.d("LoggedIn");
                  bus.post(new ClaimEvent());
                }

                @Override public void failure(RetrofitError error) {

                  if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
                    Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
                    return;
                  }

                  if (error.getResponse().getStatus() == 401) {
                    Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.ask_staff_login, Snackbar.LENGTH_LONG).show();
                    return;
                  }

                  Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_LONG).show();
                  Timber.w("login on find claim error : %s", error.getMessage() != null ? error.getMessage() : error.toString());
                }
              });
            }

            if (error.getResponse() != null && error.getResponse().getStatus() == 400) {
              Timber.e("ERROR %s", error.getMessage());
              new MaterialDialog.Builder(FindUserActivity.this)
                  .cancelable(true)
                  .autoDismiss(true)
                  .title("Bad info")
                  .content("Please check your check-in date again and retry")
                  .positiveText("OK").build().show();
              return;
            }

            Timber.w("Claim error : %s", error.getMessage() != null ? error.getMessage() : error.toString());
            Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_SHORT)
                .show();
          }
        });
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
  @OnClick(R.id.backBtn)
  public void backClicked() {
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    if (viewPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else if(viewPager.getCurrentItem() == 4 || viewPager.getCurrentItem() == 3) {
      reset();
    } else {
      // Otherwise, select the previous step.
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
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

  /**
   * Reset.
   */
  public void reset() {

    if (guest != null) {
      guest.user_key = null;
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

    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

  /**
   * Login.
   */
  private void login() {
    if (loginCount >= 2) {
      Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.ask_staff_login, Snackbar.LENGTH_LONG).show();
      return;
    }

    loginCount++;
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse response, Response response2) {
        Timber.d("LoggedIn");
        bus.post(new AuthenticationPassedEvent());
      }

      @Override public void failure(RetrofitError error) {

        if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
          Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          login();
          return;
        }

        Snackbar.make(appContainer.bind(FindUserActivity.this), R.string.something_wrong_try_again, Snackbar.LENGTH_LONG).show();
        Timber.w("ERROR: claim login error = %s", error.getMessage() != null ? error.getMessage() : error.toString());
      }
    });
  }
}
