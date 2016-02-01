/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.data.api.models.Guest;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.Client;
import retrofit.client.OkClient;

@Module(
    complete = false,
    library = true
)
public final class ApiModule {

  /**
   * Provide base url http url.
   *
   * @return the http url
   */
  @Provides @Singleton
  Endpoint provideEndpoint() {
    return Endpoints.newFixedEndpoint(ApiConstants.API_URL);
  }

  /**
   * Provide client client.
   *
   * @param client the client
   * @return the client
   */
  @Provides @Singleton Client provideClient(OkHttpClient client) {
    return new OkClient(client);
  }

  /**
   * Provide main rest adapter rest adapter.
   *
   * @param endpoint the endpoint
   * @param client   the client
   * @param headers  the headers
   * @return the rest adapter
   */
  @Provides @Singleton
  RestAdapter provideMainRestAdapter(Endpoint endpoint, Client client, ApiHeaders headers) {
    return new RestAdapter.Builder() //
        .setClient(client) //
        .setEndpoint(endpoint)
        .setRequestInterceptor(headers)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setLog(new AndroidLog("TipiSelfRetrofit"))
        .build();
  }

  /**
   * Provide authentication service authentication service.
   *
   * @param restAdapter the restAdapter
   * @return the authentication service
   */
  @Provides @Singleton
  AuthenticationService provideAuthenticationService(RestAdapter restAdapter) {
    return restAdapter.create(AuthenticationService.class);
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
}
