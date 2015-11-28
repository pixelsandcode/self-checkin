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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject AppContainer appContainer;

  @Bind(R.id.main_logo) TextView mainLogoView;

  MaterialDialog loading;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    SelfCheckInApp.get(this).inject(this);
    Timber.d("Created");
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
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
  }

  @Override protected void onStop() {
    super.onStop();
    Timber.d("Stopped");
  }

  /**
   * Show login fragment.
   */
  public void showLoginFragment() {
    mainLogoView.setVisibility(View.INVISIBLE);
    getFragmentManager().beginTransaction().replace(R.id.container, LoginFragment.newInstance(this)).commit();
  }

  /**
   * Login.
   */
  public void login() {
    Call<ApiResponse> call = authenticationService.login(new LoginRequest(username.get(), password.get()));

    loading.show();
    call.enqueue(new Callback<ApiResponse>() {
      @Override public void onResponse(Response<ApiResponse> response, Retrofit retrofit) {
        loading.dismiss();
        if (response.isSuccess()) {
          Timber.d("LoggedIn");
          avatarPath.delete();
          startActivity(new Intent(MainActivity.this, SignUpActivity.class));
          finish();
        } else {
          Timber.d("Response %s", response.body());
          showLoginFragment();
          Snackbar.make(appContainer.bind(MainActivity.this), "Your email/password doesn't match!", Snackbar.LENGTH_LONG).show();
        }
      }

      @Override public void onFailure(Throwable t) {
        loading.dismiss();
        Timber.d("Login Failed");
        Snackbar.make(appContainer.bind(MainActivity.this), "Sorry, Network problem", Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry", new View.OnClickListener() {
              @Override public void onClick(View v) {
                login();
              }
            }).show();
      }
    });
  }
}
