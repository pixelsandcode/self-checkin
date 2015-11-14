package me.tipi.self_check_in.data.api;

import com.squareup.moshi.Moshi;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.data.api.models.Guest;
import retrofit.MoshiConverterFactory;
import retrofit.Retrofit;

@Module(
    complete = false,
    library = true
)
public final class ApiModule {
  public static final HttpUrl PRODUCTION_API_URL = HttpUrl.parse(ApiConstants.API_URL);

  @Provides @Singleton HttpUrl provideBaseUrl() {
    return PRODUCTION_API_URL;
  }

  @Provides @Singleton @Named("Api") OkHttpClient provideApiClient(OkHttpClient client, LoggingInterceptor loggingInterceptor) {
    return createApiClient(client, loggingInterceptor);
  }

  @Provides @Singleton
  Retrofit provideRetrofit(HttpUrl baseUrl, @Named("Api") OkHttpClient client, Moshi moshi) {
    return new Retrofit.Builder() //
        .client(client) //
        .baseUrl(baseUrl) //
        .addConverterFactory(MoshiConverterFactory.create(moshi)) //
        .build();
  }

  @Provides @Singleton
  AuthenticationService provideAuthenticationService(Retrofit retrofit) {
    return retrofit.create(AuthenticationService.class);
  }

  @Provides @Singleton
  Guest provideGuest() {
    return new Guest();
  }

  static OkHttpClient createApiClient(OkHttpClient client, LoggingInterceptor loggingInterceptor) {
    client = client.clone();
    client.interceptors().add(loggingInterceptor);
    return client;
  }
}
