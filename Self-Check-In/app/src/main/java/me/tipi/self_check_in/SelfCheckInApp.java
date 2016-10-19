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
import android.content.Intent;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.ObjectGraph;
import me.tipi.self_check_in.util.FileLogger;
import timber.log.Timber;

public final class SelfCheckInApp extends Application {
  private ObjectGraph objectGraph;
  private Tracker analyticTracker;

  @Override public void onCreate() {
    super.onCreate();

    AndroidThreeTen.init(this);
    Timber.plant(new FileLogger());
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } /*else {
      // TODO Crashlytics.start(this);
      // TODO Timber.plant(new CrashlyticsTree());
    }*/

    objectGraph = ObjectGraph.create(new SelfCheckInModule(this));
    objectGraph.inject(this);

    TypefaceHelper.initialize(this);
    startKioskService();
  }

  @Override public void onTerminate() {
    TypefaceHelper.destroy();
    super.onTerminate();
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
   * Inject.
   *
   * @param o the o
   */
  public void inject(Object o) {
    objectGraph.inject(o);
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

  private void startKioskService() { // ... and this method
    startService(new Intent(this, KioskService.class));
  }
}
