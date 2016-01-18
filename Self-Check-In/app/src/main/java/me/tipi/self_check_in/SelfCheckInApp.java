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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.ObjectGraph;
import timber.log.Timber;

public final class SelfCheckInApp extends Application {
  private ObjectGraph objectGraph;
  private Tracker analyticTracker;

  @Override public void onCreate() {
    super.onCreate();

    AndroidThreeTen.init(this);

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } /*else {
      // TODO Crashlytics.start(this);
      // TODO Timber.plant(new CrashlyticsTree());
    }*/

    objectGraph = ObjectGraph.create(new SelfCheckInModule(this));
    objectGraph.inject(this);
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
}
