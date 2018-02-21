/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.f2prateek.rx.preferences2.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.io.File;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import me.tipi.self_check_in.BuildConfig;
import me.tipi.self_check_in.KioskService;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.PrinterPreference;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AppCallback;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class SettingActivity extends AppCompatActivity {

  @Inject NetworkRequestManager networkRequestManager;
  @Inject AppContainer appContainer;
  @Inject Tracker tracker;
  @Inject PrinterPreference printerPreference;
  @Inject TypefaceHelper typeface;
  @Inject @Named(ApiConstants.USER_NAME) Preference<String> username;
  @Inject @Named(ApiConstants.PASSWORD) Preference<String> password;
  @Inject
  @Named(ApiConstants.KIOSK_NAME)
  Preference<String> kioskNamePref;

  @Bind(R.id.version) TextView versionTextView;
  @Bind(R.id.print) TextView print;
  @Bind(R.id.kiosk_name) TextView kioskName;
  private MaterialDialog loading;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SelfCheckInApp.get(this).getSelfCheckInComponent().inject(this);
    ButterKnife.bind(this);

    typeface.setTypeface(this, getResources().getString(R.string.font_regular));
    versionTextView.setText(String.format("Current Version %s", BuildConfig.VERSION_NAME));

    if (printerPreference.get()) {
      print.setText(getString(R.string.printer_on));
    } else {
      print.setText(getString(R.string.printer_off));
    }

    handleKioskModeText();

    loading = new MaterialDialog.Builder(this)
        .content("Loading")
        .cancelable(false)
        .progress(true, 0)
        .build();

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.edit().putBoolean(KioskService.PREF_KIOSK_MODE, false).apply();
    Timber.w("KIOSK mode is OFF");
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

  @Override public void onBackPressed() {
    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

  public void goToDownload(View view) {
    tracker.send(new HitBuilders.EventBuilder("Download", "Tapped").build());
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiConstants.BASE_DOWNLOAD_PAGE));
    startActivity(browserIntent);
  }

  public void sendLog(View view) {
    File logFile = new File("/storage/emulated/0/Download/kiosk.txt");

    String kioskName = (kioskNamePref != null && kioskNamePref.get() != null) ? kioskNamePref.get() : null;
    if (kioskName != null) {
      if (logFile.exists()) {
        loading.show();
        networkRequestManager.callSendLogApi(kioskName, logFile,
            new AppCallback() {
              @Override public void onRequestSuccess(Call call, Response response) {
                loading.dismiss();
                new MaterialDialog.Builder(SettingActivity.this)
                    .title("Thank you!")
                    .content("Log file sent successfully")
                    .cancelable(true)
                    .positiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                      @Override
                      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                      }
                    })
                    .build().show();
              }

              @Override public void onRequestFail(Call call, BaseResponse response) {
                loading.dismiss();
                Timber.w("ERROR %s", response.getMessage() != null ? response.getMessage() : response.toString());
                Toast.makeText(SettingActivity.this, R.string.something_wrong_try_again + response.getMessage(), Toast.LENGTH_SHORT).show();
              }

              @Override public void onApiNotFound(Call call, BaseResponse response) {
                loading.dismiss();
              }

              @Override public void onBadRequest(Call call, BaseResponse response) {
                loading.dismiss();
              }

              @Override public void onAuthError(Call call, BaseResponse response) {
                loading.dismiss();
              }

              @Override public void onServerError(Call call, BaseResponse response) {
                loading.dismiss();
                Snackbar.make(appContainer.bind(SettingActivity.this), R.string.no_connection, Snackbar.LENGTH_LONG).show();
              }

              @Override public void onRequestTimeOut(Call call, Throwable t) {
                loading.dismiss();
              }

              @Override public void onNullResponse(Call call) {
                loading.dismiss();
              }
            });
      } else {
        Timber.w("logFile is null");
      }
    } else {
      Timber.w("Kiosk name is null!!!");
    }
  }

/*  public void goToEmail(View view) {
    Intent emailIntent = new Intent(Intent.ACTION_SEND);
    String emailTo = getResources().getString(R.string.log_email_to);
    String emailSubject = getResources().getString(R.string.log_email_subject);
    String emailText = getResources().getString(R.string.log_email_text);
    emailIntent.setData(Uri.parse("mailto:"));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo});
    emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/storage/emulated/0/Download/kiosk.txt")));
    emailIntent.setType("text/plain");
    startActivity(Intent.createChooser(emailIntent, "Send mail"));
  }*/

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

  public void exitApp(View view) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.edit().putBoolean(KioskService.PREF_KIOSK_MODE, false).apply();
    this.finishAffinity();
  }

  public void changeKioskName(View view) {
    new MaterialDialog.Builder(this)
        .title("Enter a Name")
        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        .inputRange(3, 50, ContextCompat.getColor(SettingActivity.this, R.color.colorAccent))
        .input("New Hostel Name", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            if (!TextUtils.isEmpty(input.toString())) {
              kioskNamePref.set(input.toString());
              handleKioskModeText();
            } else {
              Toast.makeText(SettingActivity.this, "Can't rename to empty text", Toast.LENGTH_LONG).show();
            }
          }
        })
        .show();
  }

  @OnClick(R.id.print)
  public void onClick() {
    printerPreference.set(!printerPreference.get());
    if (printerPreference.get()) {
      print.setText(getString(R.string.printer_on));
    } else {
      print.setText(getString(R.string.printer_off));
    }
  }

  private void handleKioskModeText() {
    if (kioskNamePref != null && kioskNamePref.get() != null && !TextUtils.isEmpty(kioskNamePref.get())) {
      kioskName.setText(String.format(Locale.US, "%s Kiosk", kioskNamePref.get()));
    }
  }
}
