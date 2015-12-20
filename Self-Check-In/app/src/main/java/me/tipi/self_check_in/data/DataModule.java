package me.tipi.self_check_in.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.threeten.bp.Clock;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.ApiModule;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Module(
    includes = ApiModule.class,
    complete = false,
    library = true
)
public final class DataModule {
  static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

  @Provides @Singleton SharedPreferences provideSharedPreferences(Application app) {
    return app.getSharedPreferences("tipiSelfCheckin", MODE_PRIVATE);
  }

  @Provides @Singleton RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
    return RxSharedPreferences.create(prefs);
  }

  @Provides @Singleton @Named(ApiConstants.USER_NAME)
  Preference<String> provideUsername(RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.USER_NAME);
  }

  @Provides @Singleton @Named(ApiConstants.PASSWORD)
  Preference<String> providePassword(RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.PASSWORD);
  }

  @Provides @Singleton @Named(ApiConstants.AVATAR)
  Preference<String> provideAvatar(RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.AVATAR);
  }

  @Provides @Singleton @Named(ApiConstants.PASSPORT)
  Preference<String> providePassport(RxSharedPreferences prefs) {
    return prefs.getString(ApiConstants.PASSPORT);
  }

  @Provides @Singleton Clock provideClock() {
    return Clock.systemDefaultZone();
  }

  @Provides @Singleton OkHttpClient provideOkHttpClient(Application app) {
    return createOkHttpClient(app);
  }

  @Provides @Singleton Picasso providePicasso(Application app, OkHttpClient client) {
    return new Picasso.Builder(app)
        .downloader(new OkHttpDownloader(client))
        .listener(new Picasso.Listener() {
          @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
            Timber.e(e, "Failed to load image: %s", uri);
          }
        })
        .build();
  }

  static OkHttpClient createOkHttpClient(Application app) {
    OkHttpClient client = new OkHttpClient();

    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    client.setCookieHandler(cookieManager);
    client.setConnectTimeout(30, SECONDS);
    client.setReadTimeout(30, SECONDS);
    client.setWriteTimeout(30, SECONDS);

    // Install an HTTP cache in the application cache directory.
    File cacheDir = new File(app.getCacheDir(), "http");
    Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
    client.setCache(cache);

    return client;
  }
}
