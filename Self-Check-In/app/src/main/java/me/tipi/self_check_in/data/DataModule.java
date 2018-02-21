package me.tipi.self_check_in.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import me.tipi.self_check_in.data.api.ApiConstants;
import okhttp3.Cache;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.threeten.bp.Clock;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;

@Module public final class DataModule {
  static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);
  private static final String PREF_USER_AVATAR = "UserAvatar";
  private static final String PREF_PRINTER = "printer";
  private static final String PREF_LANGUAGE = "language";
  private static final String PREF_PASSPORT_IMAGE = "passportImage";

  /**
   * Provide shared preferences shared preferences.
   *
   * @param app the app
   * @return the shared preferences
   */
  @Provides @Singleton SharedPreferences provideSharedPreferences(Application app) {
    return app.getSharedPreferences("tipiSelfCheckin", MODE_PRIVATE);
  }

  /**
   * Provide rx shared preferences rx shared preferences.
   *
   * @param prefs the prefs
   * @return the rx shared preferences
   */
  @Provides @Singleton RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
    return RxSharedPreferences.create(prefs);
  }

  /**
   * Provide username preference.
   *
   * @param prefs the prefs
   * @return the preference
   */
  @Provides @Singleton @Named(ApiConstants.USER_NAME) Preference<String> provideUsername(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.USER_NAME);
  }

  @Provides @Singleton @Named(ApiConstants.HOSTEL_NAME) Preference<String> provideHostelName(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.HOSTEL_NAME);
  }

  @Provides @Singleton @Named(ApiConstants.HOSTEL_KEY) Preference<String> provideHostelKey(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.HOSTEL_KEY);
  }

  @Provides @Singleton @Named(ApiConstants.KIOSK_NAME) Preference<String> provideKioskName(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.KIOSK_NAME);
  }

  @Provides @Singleton PrinterPreference providePrinter(SharedPreferences preferences) {
    return new PrinterPreference(preferences, PREF_PRINTER);
  }

  @Provides @Singleton LanguagePreference provideLanguagePrefrence(SharedPreferences preferences) {
    return new LanguagePreference(preferences, PREF_LANGUAGE, "en");
  }

  /**
   * Provide password preference.
   *
   * @param prefs the prefs
   * @return the preference
   */
  @Provides @Singleton @Named(ApiConstants.PASSWORD) Preference<String> providePassword(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.PASSWORD);
  }

  /**
   * Provide avatar preference.
   *
   * @param prefs the prefs
   * @return the preference
   */
  @Provides @Singleton @Named(ApiConstants.AVATAR) Preference<String> provideAvatar(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.AVATAR);
  }

  /**
   * Provide passport preference.
   *
   * @param prefs the prefs
   * @return the preference
   */
  @Provides @Singleton @Named(ApiConstants.PASSPORT) Preference<String> providePassport(
      RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.PASSPORT);
  }

  /**
   * Provide clock clock.
   *
   * @return the clock
   */
  @Provides @Singleton Clock provideClock() {
    return Clock.systemDefaultZone();
  }

  ///**
  // * Provide ok http client ok http client.
  // *
  // * @param app the app
  // * @return the ok http client
  // */
  //@Provides @Singleton OkHttpClient provideOkHttpClient(Application app) {
  //  return createOkHttpClient(app);
  //}

  /**
   * Provide picasso picasso.
   *
   * @param app the app
   * @param client the client
   * @return the picasso
   */
  @Provides @Singleton Picasso providePicasso(Application app, OkHttpClient client) {
    return new Picasso.Builder(app).downloader(new OkHttp3Downloader(client))
        .listener(new Picasso.Listener() {
          @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
            Timber.e(e, "Failed to load image: %s", uri);
          }
        })
        .build();
  }

  ///**
  // * Create ok http client ok http client.
  // *
  // * @param app the app
  // * @return the ok http client
  // */
  @Provides @Singleton OkHttpClient createOkHttpClient(Application app) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    // Install an HTTP cache in the application cache directory.
    File cacheDir = new File(app.getCacheDir(), "http");
    Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

    builder.cache(cache)
        .cookieJar(new JavaNetCookieJar(cookieManager))
        .connectTimeout(1000, TimeUnit.SECONDS)
        .readTimeout(1000, TimeUnit.SECONDS)
        .writeTimeout(1000, TimeUnit.SECONDS);

    return builder.build();
  }
}
