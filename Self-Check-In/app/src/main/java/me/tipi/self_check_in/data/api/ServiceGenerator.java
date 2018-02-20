package me.tipi.self_check_in.data.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import me.tipi.self_check_in.SelfCheckInApp;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;

/**
 * Created by k.monem on 6/5/2016.
 */
public class ServiceGenerator {

  static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

  private static ServiceGenerator instance;

  private Retrofit retrofit;
  private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
  private Retrofit.Builder builder;

  private ServiceGenerator() {
    init();
  }

  public static ServiceGenerator getInstance() {

    if (instance == null) {
      instance = new ServiceGenerator();
    }
    return instance;
  }

  private void init() {
    builder = new Retrofit.Builder().baseUrl(ApiConstants.getApiBaseUrl())
        .addConverterFactory(GsonConverterFactory.create());

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    httpClient.addInterceptor(logging);

    httpClient.connectTimeout(20, TimeUnit.SECONDS);
    httpClient.readTimeout(5, TimeUnit.SECONDS);

    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    // Install an HTTP cache in the application cache directory.
    File cacheDir = new File(SelfCheckInApp.get().getCacheDir(), "http");
    Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

    httpClient.cache(cache)
        .cookieJar(new JavaNetCookieJar(cookieManager))
        .connectTimeout(1000, TimeUnit.SECONDS)
        .readTimeout(1000, TimeUnit.SECONDS)
        .writeTimeout(1000, TimeUnit.SECONDS);

    httpClient.addInterceptor(new Interceptor() {
      @Override public okhttp3.Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Request newRequest;

        Request.Builder builder = request.newBuilder();

        builder.addHeader("Accept", "application/json;versions=1");

        if (isNetworkAvailable()) {
          int maxAge = 1; // read from cache for 1 minute
          builder.addHeader("Cache-Control", "public, max-age=" + maxAge);
        } else {
          int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
          builder.addHeader("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
        }
        newRequest = builder.build();
        return chain.proceed(newRequest);
      }
    });

    OkHttpClient client = httpClient.build();
    retrofit = builder.client(client).build();
  }

  public <S> S createService(Class<S> serviceClass) {
    return retrofit.create(serviceClass);
  }

  public Retrofit getRetrofit() {
    return retrofit;
  }

  /**
   * Is network available boolean.
   *
   * @return a boolean indicating if network is available
   */
  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) SelfCheckInApp.get().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
}
