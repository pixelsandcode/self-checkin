/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.f2prateek.rx.preferences.Preference;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.ui.adapters.LoginAdapter;
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

  @Bind(R.id.pager) ChangeSwipeViewPager viewPager;

  public LoginAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_user);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);
    Timber.d("Created");

    adapter = new LoginAdapter(getSupportFragmentManager(), this);
    viewPager.setAdapter(adapter);
    viewPager.setSwipingEnabled(false);

  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    bus.unregister(this);
  }

  @Override protected void onRestart() {
    super.onRestart();
    Timber.d("Restart");
    login();
  }

  @Override
  public void onBackPressed() {
    if (viewPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else {
      // Otherwise, select the previous step.
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
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
   * Start over.
   */
  @OnClick(R.id.resetBtn)
  public void startOver() {
    reset();
    bus.post(new PagerChangeEvent(0));
  }

  /**
   * Go to sign up.
   *
   * @param view the view
   */
  public void goToSignUp(View view) {
    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

  /**
   * Reset.
   */
  private void reset() {
    if (guest != null) {
      guest = null;
    }

    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

  /**
   * Login.
   */
  private void login() {
    authenticationService.login(new LoginRequest(username.get(), password.get()), new Callback<Response>() {
      @Override public void success(Response response, Response response2) {
        Timber.d("LoggedIn");
      }

      @Override public void failure(RetrofitError error) {
        if (error.getResponse().getStatus() == 401) {
          Snackbar.make(appContainer.bind(FindUserActivity.this), "Your email/password doesn't match!", Snackbar.LENGTH_LONG).show();
        } else {
          Snackbar.make(appContainer.bind(FindUserActivity.this), "Connection failed, please try again", Snackbar.LENGTH_LONG).show();
        }
        startActivity(new Intent(FindUserActivity.this, MainActivity.class));
        finish();
      }
    });
  }
}
