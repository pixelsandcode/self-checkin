package me.tipi.self_check_in.util;

/**
 * Created by arsalan on 8/10/16.
 */
import android.util.Log;


import timber.log.Timber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileLogger extends Timber.DebugTree {
  static Logger mLogger = LoggerFactory.getLogger(FileLogger.class);

  @Override
  protected void log(int priority, String tag, String message, Throwable t) {
    if (priority == Log.VERBOSE) {
      return;
    }

    String logMessage = tag + ": " + message;
    switch (priority) {
      case Log.DEBUG:
        mLogger.debug(logMessage);
        break;
      case Log.INFO:
        mLogger.info(logMessage);
        break;
      case Log.WARN:
        mLogger.warn(logMessage);
        break;
      case Log.ERROR:
        mLogger.error(logMessage);
        break;
    }
  }
}
