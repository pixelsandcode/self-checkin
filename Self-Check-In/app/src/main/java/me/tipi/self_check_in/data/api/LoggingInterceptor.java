/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.threeten.bp.Clock;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okio.Buffer;
import timber.log.Timber;

/**
 * Verbose logging of network calls, which includes path, headers, and times.
 */
@Singleton
public final class LoggingInterceptor implements Interceptor {
  private final Clock clock;

  /**
   * Instantiates a new Logging interceptor.
   *
   * @param clock the clock
   */
  @Inject public LoggingInterceptor(Clock clock) {
    this.clock = clock;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    long startMs = clock.millis();
    Timber.v("Sending request %s%s", request.url(), prettyHeaders(request.headers()));
    Timber.v("Request log %s", bodyToString(request));

    Response response = chain.proceed(request);

    long tookMs = clock.millis() - startMs;
    Timber.v("Received response (%s) for %s in %sms%s", response.code(), response.request().url(),
        tookMs, prettyHeaders(response.headers()));

    return response;
  }

  /**
   * Pretty headers string.
   *
   * @param headers the headers
   * @return the string
   */
  private String prettyHeaders(Headers headers) {
    if (headers.size() == 0) return "";

    StringBuilder builder = new StringBuilder();
    builder.append("\n  Headers:");

    for (int i = 0; i < headers.size(); i++) {
      builder.append("\n    ").append(headers.name(i)).append(": ").append(headers.value(i));
    }

    return builder.toString();
  }

  /**
   * Body to string string.
   *
   * @param request the request
   * @return the string
   */
  private static String bodyToString(final Request request) {

    try {
      final Request copy = request.newBuilder().build();
      final Buffer buffer = new Buffer();
      if (copy.body() != null) {
        copy.body().writeTo(buffer);
      }

      return buffer.readUtf8();
    } catch (final IOException e) {
      return "did not work";
    }
  }

}
