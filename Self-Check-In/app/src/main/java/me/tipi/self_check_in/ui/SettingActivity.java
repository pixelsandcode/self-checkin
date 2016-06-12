/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.tipi.self_check_in.BuildConfig;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import timber.log.Timber;

public class SettingActivity extends AppCompatActivity {

  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;

  @Bind(R.id.version) TextView versionTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    typeface.setTypeface(this, getResources().getString(R.string.font_regular));
    versionTextView.setText(String.format("Current Version %s", BuildConfig.VERSION_NAME));

    Timber.d("Created");
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  public void goToDownload(View view) {
    tracker.send(new HitBuilders.EventBuilder("Download", "Tapped").build());
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiConstants.BASE_DOWNLOAD_PAGE));
    startActivity(browserIntent);
  }

  public void logout(View view) {
    username.delete();
    password.delete();
    tracker.send(new HitBuilders.EventBuilder("Logout", "Tapped").build());
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }

  public void goToCheckIn(View view) {
    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

}
