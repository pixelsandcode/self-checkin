package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.rx.preferences.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.LoginRequest;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    SelfCheckInApp.get(this).inject(this);
    Timber.d("Created");
    this.login();
    /*getFragmentManager().beginTransaction().replace(R.id.container, new LoginFragment()).commit();*/
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

  public void login() {
    Call<ApiResponse> call = authenticationService.login(new LoginRequest("admin@matchbox.com", "adminadmin"));
    call.enqueue(new Callback<ApiResponse>() {
      @Override public void onResponse(Response<ApiResponse> response, Retrofit retrofit) {
        if (response.isSuccess()) {
          Timber.d("LoggedIn", response.body());
        } else {
          Timber.d("Response", response.body());
        }
      }

      @Override public void onFailure(Throwable t) {
        Timber.d("Login Failed", t.getMessage());
      }
    });
  }
}
