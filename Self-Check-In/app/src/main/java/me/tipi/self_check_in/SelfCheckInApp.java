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
import android.content.IntentFilter;
import android.os.PowerManager;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.threetenabp.AndroidThreeTen;
import me.tipi.self_check_in.di.DaggerSelfCheckInComponent;
import me.tipi.self_check_in.di.SelfCheckInComponent;
import me.tipi.self_check_in.util.FileLogger;
import timber.log.Timber;

public final class SelfCheckInApp extends Application {

  private SelfCheckInComponent selfCheckInComponent;
  private Tracker analyticTracker;
  private PowerManager.WakeLock wakeLock;
  private OnScreenOffReceiver onScreenOffReceiver;
  private static Context appContext;

  @Override public void onCreate() {
    super.onCreate();

    appContext = getApplicationContext();

    selfCheckInComponent =
        DaggerSelfCheckInComponent.builder().selfCheckInModule(new SelfCheckInModule(this)).build();
    AndroidThreeTen.init(this);
    Timber.plant(new FileLogger());
    if (BuildConfig.DEBUG || BuildConfig.STG) {
      Timber.plant(new Timber.DebugTree());
    } /*else {
      // TODO Crashlytics.start(this);
      // TODO Timber.plant(new CrashlyticsTree());
    }*/

    TypefaceHelper.initialize(this);
    startKioskService();
    //registerKioskModeScreenOffReceiver();
  }

  @Override public void onTerminate() {
    TypefaceHelper.destroy();
    super.onTerminate();
  }

  synchronized public Tracker getDefaultTracker() {
    if (analyticTracker == null) {
      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
      if (BuildConfig.DEBUG || BuildConfig.STG) {
        analytics.setDryRun(true);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
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

  public static SelfCheckInApp get() {
    return (SelfCheckInApp) appContext;
  }

  public SelfCheckInComponent getSelfCheckInComponent() {
    return selfCheckInComponent;
  }

  private void startKioskService() { // ... and this method
    startService(new Intent(this, KioskService.class));
  }

  private void registerKioskModeScreenOffReceiver() {
    // register screen off receiver
    final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    onScreenOffReceiver = new OnScreenOffReceiver();
    registerReceiver(onScreenOffReceiver, filter);
  }

  public PowerManager.WakeLock getWakeLock() {
    if (wakeLock == null) {
      // lazy loading: first call, create wakeLock via PowerManager.
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
          "wakeup");
    }
    return wakeLock;
  }
}
