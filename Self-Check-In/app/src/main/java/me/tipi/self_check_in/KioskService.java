package me.tipi.self_check_in;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import me.tipi.self_check_in.ui.MainActivity;
import timber.log.Timber;

public class KioskService extends Service {

  private static final long INTERVAL = TimeUnit.SECONDS.toMillis(1); // periodic interval to check in seconds -> 2 seconds
  private static final String TAG = KioskService.class.getSimpleName();
  public static final String PREF_KIOSK_MODE = "pref_kiosk_mode";

  private Thread t = null;
  private Context ctx = null;
  private boolean running = false;

  @Override public void onDestroy() {
    Timber.i("Stopping service 'KioskService'");
    running = false;
    super.onDestroy();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.i("Starting service 'KioskService'");
    running = true;
    ctx = this;

    // start a thread that periodically checks if your app is in the foreground
    t = new Thread(new Runnable() {
      @Override
      public void run() {
        do {
          handleKioskMode();
          try {
            Thread.sleep(INTERVAL);
          } catch (InterruptedException e) {
            Timber.i(TAG, "Thread interrupted: 'KioskService'");
          }
        } while(running);
        stopSelf();
      }
    });

    t.start();
    return Service.START_NOT_STICKY;
  }

  private void handleKioskMode() {
    // is Kiosk Mode active?
    if(isKioskModeActive(ctx)) {
      // is App in background?
      if(isInBackground()) {
        restoreApp(); // restore!
      }
    }
  }

  private boolean isInBackground() {
    ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningInfo = am.getRunningAppProcesses();

    return runningInfo.get(0).importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
  }

  private void restoreApp() {
    // Restart activity
    Intent i = new Intent(ctx, MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    ctx.startActivity(i);
  }

  public boolean isKioskModeActive(final Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean(PREF_KIOSK_MODE, false);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
