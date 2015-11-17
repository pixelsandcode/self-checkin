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

import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.ObjectGraph;
import timber.log.Timber;

public final class SelfCheckInApp extends Application {
  private ObjectGraph objectGraph;

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
