/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in;

import android.app.Application;
import android.content.Context;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.threetenabp.AndroidThreeTen;

import me.tipi.self_check_in.data.DataModule;
import me.tipi.self_check_in.data.api.ApiModule;
import me.tipi.self_check_in.ui.UiModule;
import me.tipi.self_check_in.util.FileLogger;
import timber.log.Timber;

public final class SelfCheckInApp extends Application {
  private Tracker analyticTracker;
  private SelfCheckInComponent component;

  @Override public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);
    Timber.plant(new FileLogger());
    component = DaggerSelfCheckInComponent.builder()
        .apiModule(new ApiModule())
        .dataModule(new DataModule())
        .uiModule(new UiModule())
        .selfCheckInModule(new SelfCheckInModule(this))
        .build();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } /*else {
      // TODO Crashlytics.start(this);
      // TODO Timber.plant(new CrashlyticsTree());
    }*/

    TypefaceHelper.initialize(this);
  }

  @Override public void onTerminate() {
    TypefaceHelper.destroy();
    super.onTerminate();
  }

  public SelfCheckInComponent getComponent() {
    return component;
  }

  synchronized public Tracker getDefaultTracker() {
    if (analyticTracker == null) {
      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
      if (BuildConfig.DEBUG) {
        analytics.setDryRun(true);
        analytics.getLogger()
            .setLogLevel(Logger.LogLevel.VERBOSE);
      }

      // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
      analyticTracker = analytics.newTracker(R.xml.global_tracker);
    }

    return analyticTracker;
  }


  /**
   * Get self check in app.
   *
   * @param context the context
   * @return the self check in app
   */
  public static SelfCheckInApp get(Context context) {
    return (SelfCheckInApp) context.getApplicationContext();
  }
}
