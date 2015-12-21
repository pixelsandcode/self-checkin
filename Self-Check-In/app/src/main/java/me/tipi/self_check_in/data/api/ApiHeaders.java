/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RequestInterceptor;

@Singleton
final class ApiHeaders implements RequestInterceptor {
  private final Application app;

  /**
   * Instantiates a new Api headers.
   *
   * @param app the app
   */
  @Inject
  public ApiHeaders(Application app) {
    this.app = app;
  }

  @Override
  public void intercept(RequestFacade request) {

    request.addHeader("Accept", "application/json;versions=1");
    //request.addHeader("Accept-Encoding", "gzip");
    if (isNetworkAvailable()) {
      int maxAge = 1; // read from cache for 1 minute
      request.addHeader("Cache-Control", "public, max-age=" + maxAge);
    } else {
      int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
      request.addHeader("Cache-Control",
          "public, only-if-cached, max-stale=" + maxStale);
    }
  }

  /**
   * Is network available boolean.
   *
   * @return a boolean indicating if network is available
   */
  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager
        = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
}
