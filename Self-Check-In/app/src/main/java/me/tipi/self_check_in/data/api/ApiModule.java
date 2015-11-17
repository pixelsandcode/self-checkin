/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.data.api.models.Guest;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

@Module(
    complete = false,
    library = true
)
public final class ApiModule {
  public static final HttpUrl PRODUCTION_API_URL = HttpUrl.parse(ApiConstants.API_URL);

  /**
   * Provide base url http url.
   *
   * @return the http url
   */
  @Provides @Singleton HttpUrl provideBaseUrl() {
    return PRODUCTION_API_URL;
  }

  /**
   * Provide api client ok http client.
   *
   * @param client             the client
   * @param loggingInterceptor the logging interceptor
   * @return the ok http client
   */
  @Provides @Singleton @Named("Api") OkHttpClient provideApiClient(OkHttpClient client, LoggingInterceptor loggingInterceptor) {
    return createApiClient(client, loggingInterceptor);
  }

  /**
   * Provide retrofit retrofit.
   *
   * @param baseUrl the base url
   * @param client  the client
   * @return the retrofit
   */
  @Provides @Singleton
  Retrofit provideRetrofit(HttpUrl baseUrl, @Named("Api") OkHttpClient client) {
    return new Retrofit.Builder() //
        .client(client) //
        .baseUrl(baseUrl) //
        .addConverterFactory(GsonConverterFactory.create()) //
        .build();
  }

  /**
   * Provide authentication service authentication service.
   *
   * @param retrofit the retrofit
   * @return the authentication service
   */
  @Provides @Singleton
  AuthenticationService provideAuthenticationService(Retrofit retrofit) {
    return retrofit.create(AuthenticationService.class);
  }

  /**
   * Provide guest guest.
   *
   * @return the guest
   */
  @Provides @Singleton
  Guest provideGuest() {
    return new Guest();
  }

  /**
   * Create api client ok http client.
   *
   * @param client             the client
   * @param loggingInterceptor the logging interceptor
   * @return the ok http client
   */
  static OkHttpClient createApiClient(OkHttpClient client, LoggingInterceptor loggingInterceptor) {
    client = client.clone();
    client.interceptors().add(loggingInterceptor);
    return client;
  }
}
