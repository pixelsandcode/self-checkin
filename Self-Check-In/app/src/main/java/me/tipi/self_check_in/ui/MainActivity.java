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
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    SelfCheckInApp.get(this).inject(this);
    Timber.d("Created");
    getFragmentManager().beginTransaction().replace(R.id.container, new LoginFragment()).commit();
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
}
