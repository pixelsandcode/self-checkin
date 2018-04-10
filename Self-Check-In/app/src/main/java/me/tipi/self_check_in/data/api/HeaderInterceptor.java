package me.tipi.self_check_in.data.api;

import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Khashayar on 24/02/2018.
 */

public class HeaderInterceptor implements Interceptor {
  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    Headers moreHeaders = request.headers().newBuilder()
        .set("Accept", "application/json;versions=1") // existing header UPDATED if available, else added.
        .build();

    //request = request.newBuilder()
    //    .addHeader("Accept", "application/json;versions=1")
    //    .build();

    request = request.newBuilder().headers(moreHeaders).build();
    Response response = chain.proceed(request);

    //
    //request.addHeader("Accept", "application/json;versions=1");
    //
    //if (isNetworkAvailable()) {
    //  int maxAge = 1; // read from cache for 1 minute
    //  builder.addHeader("Cache-Control", "public, max-age=" + maxAge);
    //} else {
    //  int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
    //  builder.addHeader("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
    //}
    return response;
  }
}
